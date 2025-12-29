/**
    Copyright (C) 2017-2022 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Convert2Diamond")
public abstract class ExportTask extends AsyncTask<Object, Object, ExportTask.ExportResult>
{
    public static String FILE_PROVIDER_AUTHORITY() {
        return BuildConfig.APPLICATION_ID + ".fileprovider";
    }

    public static final long MIN_WAIT_TIME = 2000;
    public static final long CACHE_MAX = 256000;

    protected WeakReference<Context> contextRef;

    protected Uri exportUri = null;
    protected String exportTarget;
    protected File exportFile;
    protected int numEntries;
    public final String newLine = System.getProperty("line.separator");

    protected boolean isPaused = false;
    public void pauseTask()
    {
        isPaused = true;
    }
    public void resumeTask()
    {
        isPaused = false;
    }
    public boolean isPaused()
    {
        return isPaused;
    }

    public ExportTask(Context context, String exportTarget)
    {
        this(context, exportTarget, false, false);
    }
    public ExportTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        this.contextRef = new WeakReference<Context>(context);
        this.exportTarget = exportTarget;
        this.saveToCache = saveToCache;
        this.useExternalStorage = useExternalStorage;
    }
    public ExportTask(Context context, Uri exportUri)
    {
        this.contextRef = new WeakReference<Context>(context);
        this.exportTarget = null;
        this.exportUri = exportUri;
        this.saveToCache = false;
        this.useExternalStorage = false;
    }

    /**
     * Property: use external storage
     */
    protected boolean useExternalStorage = false;
    public boolean willUseExternalStorage()
    {
        return useExternalStorage;
    }

    /**
     * Property: save/export to the cache
     */
    protected boolean saveToCache = false;
    public boolean willSaveToCache() { return saveToCache; }

    /**
     * Property: overwrite existing file
     */
    protected boolean overwriteTarget = false;
    public boolean getOverwriteFlag() { return overwriteTarget; }
    public void setOverwriteFlag(boolean value) { this.overwriteTarget = value; }

    /**
     * Property: file ext
     */
    protected String ext = ".csv";
    public String getExt()
    {
        return ext;
    }
    public void setExt( String ext )
    {
        this.ext = ext;
    }

    /**
     * Property: mimeType
     */
    protected String mimeType = "";
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * onPreExecute
     * Runs before task begins.
     */
    @Override
    protected void onPreExecute()
    {
        numEntries = 0;
        signalStarted();
    }

    /**
     * doInBackground
     */
    @Override
    protected ExportResult doInBackground(Object... params)
    {
        final Context context = contextRef.get();
        if (context == null)
        {
            Log.w("ExportTask", "Reference (weak) to context is null at start of doInBackground; cancelling...");
            return new ExportResult(false, exportUri, null, "");
        }

        long startTime = System.currentTimeMillis();

        //
        // Step 1: get a handle to the exportFile
        // (from the external cache, external dl dir, or internal cache)
        //
        String storageState = Environment.getExternalStorageState();
        boolean tryExternalStorage = useExternalStorage && storageState.equals(Environment.MEDIA_MOUNTED);
        if (tryExternalStorage)
        {
            if (saveToCache)         // save to: external cache
            {
                Log.d("ExportTask", "saving to external cache");
                try {
                    cleanupExternalCache(context);
                    exportFile = File.createTempFile(exportTarget, ext, context.getExternalCacheDir());

                } catch (IOException e) {
                    Log.w("ExportTask", "Canceling export; failed to create external temp file.");
                    return new ExportResult(false, null, null, "");
                }

            } else {                 // save to: external download dir
                Log.d("ExportTask", "saving to external download dir");
                File exportPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                exportFile = new File(exportPath, exportTarget);

                boolean targetExists = exportFile.exists();
                if (targetExists && !overwriteTarget)
                {
                    Log.w("ExportTask", "Canceling export; the target already exists (and overwrite flag is false). " + exportFile.getAbsolutePath());
                    return new ExportResult(false, exportUri,  exportFile, mimeType);

                } else if (targetExists) {
                    int c = 0;
                    String outFile;
                    do {
                        outFile = exportTarget + "-" + c + ext;
                        c++;
                    } while ((exportFile = new File(exportPath, outFile)).exists());
                }
            }

        } else if (saveToCache) {    // save to: internal cache
            Log.d("ExportTask", "saving to internal cache");
            try {
                cleanupInternalCache(context);
                exportFile = File.createTempFile(exportTarget, ext, context.getCacheDir());

            } catch (IOException e) {
                Log.w("ExportTask", "Canceling export; failed to create internal temp file.");
                return new ExportResult(false, exportUri, null, "");
            }

        } else if (exportUri != null) {    // save to user provided URI
            Log.d("ExportTask", "saving to uri: " + exportUri);
            exportFile = null;

        } else {
            Log.w("ExportTask", "Canceling export; external storage is unavailable.");
            return new ExportResult(false, exportUri, null, "");
        }

        //
        // Step 2: save to file
        //
        boolean exported = false;
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream((exportUri != null)
                            ? context.getContentResolver().openOutputStream(exportUri)
                            : new FileOutputStream(exportFile)
            );
            exported = export(context, out);

        } catch (IOException e) {
            Log.w("ExportTask", "FAILED to write to the export target! " + exportFile.getAbsolutePath() + " :: " + e);

        } finally {
            //
            // Step 3: cleanup
            //
            if (out != null)
            {
                try {
                    out.close();
                } catch (IOException e2) {
                    Log.w("ExportTask", "FAILED to close the export target! " + exportFile.getAbsolutePath() + " :: " + e2);
                }
            }
            cleanup(context);
        }

        //
        // Step 4: wait for UI to spin a second, then return
        //
        long endTime = System.currentTimeMillis();
        while ((endTime - startTime) < MIN_WAIT_TIME || isPaused)
        {
            endTime = System.currentTimeMillis();
        }
        return new ExportResult(exported, exportUri, exportFile, mimeType);
    }

    protected abstract boolean export(Context context, BufferedOutputStream out) throws IOException;

    protected void cleanup(Context context) {}

    /**
     * Runs after the task completes.
     * @param results an ExportResult object wrapping the result
     */
    @Override
    protected void onPostExecute(ExportResult results)
    {
        signalFinished(results);
    }

    /**
     * Export Result
     */
    public static class ExportResult
    {
        public ExportResult( boolean result, Uri exportUri, File exportFile, String mimeType )
        {
            this.result = result;
            this.exportUri = exportUri;
            this.exportFile = exportFile;
            this.mimeType = mimeType;
        }

        private final boolean result;
        public boolean getResult() { return result; }

        private final Uri exportUri;
        public Uri getExportUri() { return exportUri; }

        private final File exportFile;
        public File getExportFile() { return exportFile; }

        private final String mimeType;
        public String getMimeType() {
            return mimeType;
        }
    }

    /**
     * Export Progress
     */
    public static class ExportProgress
    {
        public ExportProgress(int current, int max, String msg)
        {
            progressNow = current;
            progressMax = max;
            progressMsg = msg;
        }

        private final int progressNow;
        public int getProgress() { return progressNow; }

        private final int progressMax;
        public int getProgressMax() { return progressMax; }

        private final String progressMsg;
        public String getProgressMsg() { return progressMsg; }
    }

    /**
     * Cleanup the internal cache.
     */
    protected void cleanupInternalCache(Context context)
    {
        File cacheDir = context.getCacheDir();
        cleanupCache(cacheDir, CACHE_MAX);
    }

    /**
     * Cleanup the external cache.
     */
    protected void cleanupExternalCache(Context context)
    {
        File cacheDir = context.getExternalCacheDir();
        cleanupCache(cacheDir, CACHE_MAX);
    }

    /**
     */
    protected void cleanupCache(File cacheDir, long cacheLimit)
    {
        long cacheSize = cacheSize(cacheDir);
        if (cacheSize > cacheLimit)
        {
            File[] cacheFiles = cacheDir.listFiles();
            Arrays.sort(cacheFiles, new Comparator<File>()
            {
                public int compare(File file1, File file2)
                {
                    return Long.compare(file1.lastModified(), file2.lastModified());
                }
            });

            int i = 0, deletedSize = 0;
            do {
                deletedSize += cacheFiles[i].length();
                if (!cacheFiles[i].delete())
                {
                    Log.w("ExportTask", "Failed to cleanup cache; unable to delete " + cacheFiles[i].getPath());
                }
                i++;
            } while (cacheSize - deletedSize > CACHE_MAX);
        }
    }

    /**
     */
    protected static long cacheSize(File dir)
    {
        long result = 0;
        if (dir != null && dir.exists())
        {
            for (File file : dir.listFiles())
            {
                result += (file.isDirectory()) ? cacheSize(file)
                        : file.length();
            }
        }
        return result;
    }

    /**
     * Task Listener
     */
    public static abstract class TaskListener
    {
        public void onStarted() {}
        public void onFinished( ExportResult result ) {}
    }
    protected TaskListener taskListener = null;
    public void setTaskListener( TaskListener listener )
    {
        taskListener = listener;
    }
    public void clearTaskListener()
    {
        taskListener = null;
    }
    private void signalStarted()
    {
        if (taskListener != null)
        {
            taskListener.onStarted();
        }
    }
    private void signalFinished( ExportResult result )
    {
        if (taskListener != null)
        {
            taskListener.onFinished(result);
        }
    }

    /**
     */
    public static void shareResult(Context context, File file, String mimeType)
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            Uri shareURI = FileProvider.getUriForFile(context, ExportTask.FILE_PROVIDER_AUTHORITY(), file);
            shareIntent.putExtra(Intent.EXTRA_STREAM, shareURI);
            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.msg_export_to)));

        } catch (Exception e) {
            Log.e("ExportTask", "shareResult: Failed to share file URI! " + e);
        }
    }

    public static Intent getOpenFileIntent(String mimeType)
    {
        Intent intent;
        if (Build.VERSION.SDK_INT >= 19)
        {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.setType(mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    @TargetApi(19)
    public static Intent getCreateFileIntent(String suggestedFileName, String mimeType)
    {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.putExtra(Intent.EXTRA_TITLE, suggestedFileName);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        return intent;
    }

    @Nullable
    public static String getFileName(@Nullable ContentResolver resolver, @Nullable Uri uri)
    {
        if (resolver != null && uri != null)
        {
            Cursor cursor = resolver.query(uri, null, null, null, null);
            if (cursor != null)
            {
                cursor.moveToFirst();
                int i = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String filename = ((i >= 0) ? cursor.getString(i) : null);
                cursor.close();
                return filename;
            }
        }
        return null;
    }

    public static HashMap<String,String> toMap(ContentValues values)
    {
        HashMap<String,String> map = new HashMap<>();
        if (Build.VERSION.SDK_INT >= 11)
        {
            for (String key : values.keySet()) {
                map.put(key, values.getAsString(key));
            }
        } else {
            for (Map.Entry<String,Object> entry : values.valueSet()) {
                Object value = entry.getValue();
                map.put(entry.getKey(), ((value != null) ? value.toString() : null));
            }
        }
        return map;
    }

    /* https://stackoverflow.com/a/59211956 */
    public static ContentValues toContentValues(Map<String, Object> map)
    {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            String key = entry.getKey();
            Object obj = entry.getValue();

            if (obj instanceof Integer) {
                values.put(key, (Integer) obj);

            } else if (obj instanceof Long) {
                values.put(key, (Long) obj);

            } else if (obj instanceof Short) {
                values.put(key, (Short) obj);

            } else if (obj instanceof Float) {
                values.put(key, (Float) obj);

            } else if (obj instanceof Double) {
                values.put(key, (Double) obj);

            } else if (obj instanceof Byte) {
                values.put(key, (Byte) obj);

            } else if (obj instanceof Boolean) {
                values.put(key, (Boolean) obj);

            } else if (obj instanceof String) {
                values.put(key, (String) obj);
            }
        }
        return values;
    }

}
