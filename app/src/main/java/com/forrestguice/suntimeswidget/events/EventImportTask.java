/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.JsonReader;
import android.util.Log;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.ExportTask;

import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * AsyncTask that reads EventAlias objects from text file (json array).
 * @see EventAlias
 */
public class EventImportTask extends AsyncTask<Uri, EventAlias, EventImportTask.TaskResult>
{
    public static final long MIN_WAIT_TIME = 2000;

    private final WeakReference<Context> contextRef;

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

    public EventImportTask(Context context)
    {
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
        Log.d(getClass().getSimpleName(), "doInBackground: starting");
        Uri uri = null;
        if (params.length > 0) {
            uri = params[0];
        }

        long startTime = System.currentTimeMillis();
        boolean result = false;
        ArrayList<EventAlias> items = new ArrayList<>();
        Exception error = null;

        Context context = contextRef.get();
        if (context != null && uri != null)
        {
            try {
                InputStream in = context.getContentResolver().openInputStream(uri);
                if (in != null)
                {
                    Log.d(getClass().getSimpleName(), "doInBackground: reading");
                    EventAliasJson.readEventAliasItems(context, in, items);
                    result = true;
                    error = null;

                } else {
                    Log.e(getClass().getSimpleName(), "Failed to import from " + uri + ": null input stream!");
                    result = false;
                    error = null;
                }
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "Failed to import from " + uri + ": " + e);
                result = false;
                items = null;
                error = e;
            }
        }

        Log.d(getClass().getSimpleName(), "doInBackground: waiting");
        long endTime = System.currentTimeMillis();
        while ((endTime - startTime) < MIN_WAIT_TIME || isPaused) {
            endTime = System.currentTimeMillis();
        }

        Log.d(getClass().getSimpleName(), "doInBackground: finishing");
        return new TaskResult(result, uri, (items != null ? items.toArray(new EventAlias[0]) : null), error);
    }

    @Override
    protected void onProgressUpdate(EventAlias... progressItems) {
        super.onProgressUpdate(progressItems);
    }

    @Override
    protected void onPostExecute( TaskResult result )
    {
        Log.d(getClass().getSimpleName(), "onPostExecute: " + result.getResult());
        if (taskListener != null) {
            taskListener.onFinished(result);
        }
    }

    /**
     * TaskResult
     */
    public static class TaskResult
    {
        public TaskResult(boolean result, Uri uri, @Nullable EventAlias[] items, Exception e)
        {
            this.result = result;
            this.items = items;
            this.uri = uri;
            this.e = e;
        }

        private final boolean result;
        public boolean getResult()
        {
            return result;
        }

        private final EventAlias[] items;
        public EventAlias[] getItems()
        {
            return items;
        }

        private final Uri uri;
        public Uri getUri()
        {
            return uri;
        }

        public int numResults() {
            return (items != null ? items.length : 0);
        }

        private final Exception e;
        public Exception getException()
        {
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

    /**
     * EventAliasJson
     */
    public static class EventAliasJson
    {
        public static final String TAG = "EventJsonParser";

        public static void readEventAliasItems(Context context, InputStream in, ArrayList<EventAlias> items) throws IOException
        {
            if (Build.VERSION.SDK_INT >= 11)
            {
                //noinspection CharsetObjectCanBeUsed
                JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                reader.setLenient(true);
                try {
                    readEventAliasItems(context, reader, items);
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
        protected static void readEventAliasItems(Context context, JsonReader reader, ArrayList<EventAlias> items) throws IOException
        {
            switch (reader.peek()) {
                case BEGIN_ARRAY: readEventAliasArray(context, reader, items); break;
                case BEGIN_OBJECT: EventAlias item = readEventAlias(context, reader);
                    if (item != null) {
                        items.add(item);
                    }
                    break;
                default: reader.skipValue(); break;
            }
        }

        @TargetApi(11)
        protected static void readEventAliasArray(Context context, JsonReader reader, ArrayList<EventAlias> items) throws IOException
        {
            try {
                reader.beginArray();
                while (reader.hasNext()) {
                    readEventAliasItems(context, reader, items);
                }
                reader.endArray();
            } catch (EOFException e) {
                Log.e(TAG, "unexpected end of file! " + e);
            }
        }

        @Nullable
        @TargetApi(11)
        protected static EventAlias readEventAlias(Context context, JsonReader reader)
        {
            Map<String, Object> map = readJsonObject(reader);
            if (map != null)
            {
                try {
                    return EventAliasValues.createEventAlias(ExportTask.toContentValues(map));

                } catch (Exception e) {
                    Log.e(TAG, "readEventAlias: skipping item because of " + e);
                    return null;
                }
            } else return null;
        }

        @Nullable
        @TargetApi(11)
        protected static Map<String, Object> readJsonObject(JsonReader reader)
        {
            try {
                Map<String, Object> map = new HashMap<>();
                reader.beginObject();
                while (reader.hasNext())
                {
                    String key = reader.nextName();
                    if (reader.hasNext())
                    {
                        Object value = null;
                        switch (reader.peek())
                        {
                            case BEGIN_ARRAY: skipJsonArray(reader); break;
                            case BEGIN_OBJECT: skipJsonObject(reader); break;
                            case BOOLEAN: value = reader.nextBoolean(); break;
                            case NULL: value = null; reader.nextNull(); break;
                            case NUMBER: case STRING:
                            default: value = reader.nextString(); break;
                        }
                        map.put(key, value);
                    }
                }
                reader.endObject();
                return map;

            } catch (IOException e) {
                Log.e(TAG, "readJsonObject: skipping item because of " + e);
                return null;
            }
        }

        @TargetApi(11)
        protected static void skipJsonObject(JsonReader reader) throws IOException
        {
            reader.beginObject();
            while (reader.hasNext()) {
                reader.skipValue();
            }
            reader.endObject();
        }

        @TargetApi(11)
        protected static void skipJsonArray(JsonReader reader) throws IOException
        {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.skipValue();
            }
            reader.endArray();
        }

        public static String toJson(EventAlias item)
        {
            HashMap<String,String> map = ExportTask.toMap(EventAliasValues.toContentValues(item));
            return new JSONObject(map).toString();
        }
    }

}