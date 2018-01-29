/**
    Copyright (C) 2017-2018 Forrest Guice
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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings("Convert2Diamond")
public abstract class ExportTask extends AsyncTask<Object, Object, ExportTask.ExportResult>
{
    public static final long MIN_WAIT_TIME = 2000;
    public static final long CACHE_MAX = 256000;

    protected WeakReference<Context> contextRef;

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
            return new ExportResult(false, null);
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
                    return new ExportResult(false, null);
                }

            } else {                 // save to: external download dir
                Log.d("ExportTask", "saving to external download dir");
                File exportPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                exportFile = new File(exportPath, exportTarget);

                boolean targetExists = exportFile.exists();
                if (targetExists && !overwriteTarget)
                {
                    Log.w("ExportTask", "Canceling export; the target already exists (and overwrite flag is false). " + exportFile.getAbsolutePath());
                    return new ExportResult(false, exportFile);

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
                return new ExportResult(false, null);
            }

        } else {
            Log.w("ExportTask", "Canceling export; external storage is unavailable.");
            return new ExportResult(false, null);
        }

        //
        // Step 2: save to file
        //
        boolean exported = false;
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(exportFile));
            exported = export(context, out);

        } catch (IOException e) {
            Log.w("ExportPlaces", "FAILED to write to the export target! " + exportFile.getAbsolutePath() + " :: " + e);

        } finally {
            //
            // Step 3: cleanup
            //
            if (out != null)
            {
                try {
                    out.close();
                } catch (IOException e2) {
                    Log.w("ExportPlaces", "FAILED to close the export target! " + exportFile.getAbsolutePath() + " :: " + e2);
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
        return new ExportResult(exported, exportFile);
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
        public ExportResult( boolean result, File exportFile )
        {
            this.result = result;
            this.exportFile = exportFile;
        }

        private final boolean result;
        public boolean getResult() { return result; }

        private final File exportFile;
        public File getExportFile() { return exportFile; }
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
                    return Long.valueOf(file1.lastModified()).compareTo(file2.lastModified());
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
}
