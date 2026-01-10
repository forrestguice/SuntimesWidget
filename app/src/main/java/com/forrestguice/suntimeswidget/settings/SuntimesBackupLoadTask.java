/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettingsImportTask.ContentValuesJson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SuntimesBackupLoadTask extends AsyncTask<Uri, Void, SuntimesBackupLoadTask.TaskResult>
{
    public static final String TAG = "RestoreBackup";
    public static final long MIN_WAIT_TIME = 2000;

    protected final WeakReference<Context> contextRef;

    protected boolean isPaused = false;
    public void pauseTask() {
        isPaused = true;
    }
    public void resumeTask() {
        isPaused = false;
    }
    public boolean isPaused() {
        return isPaused;
    }

    public SuntimesBackupLoadTask(Context context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute()
    {
        Log.d(getClass().getSimpleName(), "onPreExecute");
        if (taskListener != null) {
            taskListener.onStarted();
        }
    }

    @Override
    protected TaskResult doInBackground(Uri... params)
    {
        Log.d(TAG, "doInBackground: starting");
        Uri uri = null;
        if (params.length > 0) {
            uri = params[0];
        }

        long startTime = System.currentTimeMillis();
        boolean result = false;
        Map<String, ContentValues[]> data = new HashMap<>();
        Exception error = null;

        Context context = contextRef.get();
        if (context != null && uri != null)
        {
            try {
                InputStream in = context.getContentResolver().openInputStream(uri);
                if (in != null)
                {
                    Log.d(TAG, "doInBackground: reading");
                    readData(context, in, data);
                    result = true;
                    error = null;

                } else {
                    Log.e(TAG, "Failed to import from " + uri + ": null input stream!");
                    result = false;
                    error = null;
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to import from " + uri + ": " + e);
                result = false;
                data = null;
                error = e;
            }
        }

        Log.d(TAG, "doInBackground: waiting");
        long endTime = System.currentTimeMillis();
        while ((endTime - startTime) < MIN_WAIT_TIME || isPaused) {
            endTime = System.currentTimeMillis();
        }

        Log.d(TAG, "doInBackground: finishing");
        return new TaskResult(result, uri, data, error);
    }

    protected void readData(Context context, InputStream in, Map<String, ContentValues[]> data) throws IOException
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            BufferedInputStream bufferedIn = new BufferedInputStream(in);
            if (!containsBackupItem(bufferedIn)) {
                Log.w(TAG, "This does not look like a valid backup file; trying to load it anyway...");
            }

            //noinspection CharsetObjectCanBeUsed
            JsonReader reader = new JsonReader(new InputStreamReader(bufferedIn, "UTF-8"));
            reader.setLenient(true);
            try {
                readBackupItem(context, reader, data);

            } finally {
                reader.close();
                in.close();
            }

        } else {
            Log.w(TAG, "Unsupported; skipping import");
            in.close();
        }
    }

    @TargetApi(11)
    public static void readBackupItem(Context context, JsonReader reader, Map<String, ContentValues[]> data) throws IOException
    {
        if (reader.peek() == JsonToken.BEGIN_OBJECT)
        {
            reader.beginObject();
            while (reader.hasNext())
            {
                String key = reader.nextName();
                if (reader.hasNext())
                {
                    switch (reader.peek())
                    {
                        case BEGIN_ARRAY:
                        case BEGIN_OBJECT:
                            ArrayList<ContentValues> items = new ArrayList<>();
                            ContentValuesJson.readItems(context, reader, items);
                            data.put(key, items.toArray(new ContentValues[0]));
                            break;

                        default:
                            reader.skipValue();
                            break;
                    }
                }
            }
            reader.endObject();

        } else {
            ContentValuesJson.skipJsonItem(reader);
        }
    }

    /**
     * @return true if beginning of stream indicates it contains a backup json object; marks/resets the stream
     */
    @TargetApi(11)
    public static boolean containsBackupItem(BufferedInputStream in) throws IOException
    {
        in.mark(Integer.MAX_VALUE);    // mark starting position
        JsonReader reader = new JsonReader(new InputStreamReader(in));
        reader.setLenient(true);

        boolean retValue = false;
        if (reader.peek() == JsonToken.BEGIN_OBJECT)
        {
            reader.beginObject();
            if (reader.peek() == JsonToken.NAME)
            {
                String key = reader.nextName();
                if (SuntimesBackupTask.KEY_CLASS.equals(key))
                {
                    if (reader.peek() == JsonToken.STRING)
                    {
                        String type = reader.nextString();
                        retValue = (SuntimesBackupTask.KEY_BACKUPFILE.equals(type));

                        if (!retValue) {
                            Log.w(TAG, "containsBackupItem: " + SuntimesBackupTask.KEY_CLASS + " should be " + SuntimesBackupTask.KEY_BACKUPFILE + " (found " + type + ")");
                        }
                    } else {
                        Log.w(TAG, "containsBackupItem: " + SuntimesBackupTask.KEY_CLASS + " expects a String (found " + reader.peek() + ")");
                    }
                } else {
                    Log.w(TAG, "containsBackupItem: " + SuntimesBackupTask.KEY_CLASS + " should be first item but it is missing!");
                }
            } else {
                Log.w(TAG, "containsBackupItem: " + SuntimesBackupTask.KEY_CLASS + " should be first item but it is missing!");
            }
        }
        in.reset();    // reset to starting mark
        return retValue;
    }

    @Override
    protected void onProgressUpdate(Void... progressItems) {
        super.onProgressUpdate(progressItems);
    }

    @Override
    protected void onPostExecute( TaskResult result )
    {
        Log.d(TAG, "onPostExecute: " + result.getResult());
        if (taskListener != null) {
            taskListener.onFinished(result);
        }
    }

    /**
     * TaskResult
     */
    public static class TaskResult
    {
        public TaskResult(boolean result, Uri uri, @Nullable Map<String, ContentValues[]> items, Exception e)
        {
            this.result = result;
            this.items = items;
            this.uri = uri;
            this.e = e;
        }

        private final boolean result;
        public boolean getResult() {
            return result;
        }

        private final Map<String, ContentValues[]> items;
        public Map<String, ContentValues[]> getItems() {
            return items;
        }

        private final Uri uri;
        public Uri getUri() {
            return uri;
        }

        public int numResults() {
            return (items != null ? items.size() : 0);
        }

        private final Exception e;
        public Exception getException() {
            return e;
        }
    }

    /**
     * TaskListener
     */
    public static abstract class TaskListener
    {
        public void onStarted() {}
        public void onFinished( TaskResult result ) {}
    }
    protected TaskListener taskListener = null;
    public void setTaskListener( TaskListener listener ) {
        taskListener = listener;
    }
    public void clearTaskListener() {
        taskListener = null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * showIOResultSnackbar
     */
    public static void showIOResultSnackbar(@Nullable final Context context, View view, boolean result, int numResults, @Nullable CharSequence report)
    {
        if (context == null) {
            return;
        }
        //Toast.makeText(context, context.getString(R.string.msg_import_success, context.getString(R.string.configAction_settings)), Toast.LENGTH_SHORT).show();
        //Toast.makeText(context, context.getString(R.string.msg_import_failure, context.getString(R.string.msg_import_label_file)), Toast.LENGTH_SHORT).show();
        CharSequence message = (result ? context.getString(R.string.msg_import_success, context.getResources().getQuantityString(R.plurals.itemsPlural, numResults, numResults))
                : context.getString(R.string.msg_import_failure, context.getString(R.string.msg_import_label_file)));
        SuntimesBackupTask.showIOResultSnackbar(context, view, null, result, message, report);
    }

}