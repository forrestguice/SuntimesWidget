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
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;

import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WidgetSettingsImportTask extends AsyncTask<Uri, ContentValues, WidgetSettingsImportTask.TaskResult>
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

    public WidgetSettingsImportTask(Context context) {
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
        ArrayList<ContentValues> items = new ArrayList<>();
        Exception error = null;

        Context context = contextRef.get();
        if (context != null && uri != null)
        {
            try {
                InputStream in = context.getContentResolver().openInputStream(uri);
                if (in != null)
                {
                    Log.d(getClass().getSimpleName(), "doInBackground: reading");
                    WidgetSettingsJson.readItems(context, in, items);
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
        return new TaskResult(result, uri, (items != null ? items.toArray(new ContentValues[0]) : null), error);
    }

    @Override
    protected void onProgressUpdate(ContentValues... progressItems) {
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
        public TaskResult(boolean result, Uri uri, @Nullable ContentValues[] items, Exception e)
        {
            this.result = result;
            this.items = items;
            this.uri = uri;
            this.e = e;
        }

        private boolean result;
        public boolean getResult()
        {
            return result;
        }

        private ContentValues[] items;
        public ContentValues[] getItems()
        {
            return items;
        }

        private Uri uri;
        public Uri getUri()
        {
            return uri;
        }

        public int numResults() {
            return (items != null ? items.length : 0);
        }

        private Exception e;
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
     * WidgetSettingsJson
     */
    public static class WidgetSettingsJson
    {
        public static final String TAG = "WidgetSettingsJson";

        public static void readItems(Context context, InputStream in, ArrayList<ContentValues> items) throws IOException
        {
            if (Build.VERSION.SDK_INT >= 11)
            {
                //noinspection CharsetObjectCanBeUsed
                JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
                reader.setLenient(true);
                try {
                    readItems(context, reader, items);
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
        protected static void readItems(Context context, JsonReader reader, ArrayList<ContentValues> items) throws IOException
        {
            switch (reader.peek()) {
                case BEGIN_ARRAY: readItemArray(context, reader, items); break;
                case BEGIN_OBJECT: ContentValues item = readItem(context, reader);
                    if (item != null) {
                        items.add(item);
                    }
                    break;
                default: reader.skipValue(); break;
            }
        }

        @TargetApi(11)
        protected static void readItemArray(Context context, JsonReader reader, ArrayList<ContentValues> items) throws IOException
        {
            try {
                reader.beginArray();
                while (reader.hasNext()) {
                    readItems(context, reader, items);
                }
                reader.endArray();
            } catch (EOFException e) {
                Log.e(TAG, "unexpected end of file! " + e);
            }
        }

        @Nullable
        @TargetApi(11)
        protected static ContentValues readItem(Context context, JsonReader reader)
        {
            Map<String, Object> map = readJsonObject(reader);
            if (map != null)
            {
                try {
                    return ExportTask.toContentValues(map);

                } catch (Exception e) {
                    Log.e(TAG, "readItem: skipping item because of " + e);
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
                            case NUMBER: // int, long, or double
                            case STRING:
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

        public static String toJson(ContentValues values)
        {
            HashMap<String,String> map = ExportTask.toMap(values);
            return new JSONObject(map).toString();
        }

    }

    public static ContentValues putValueInto(ContentValues values, String key, Object value)
    {
        if (value == null) {
            values.putNull(key);

        } else if (value.getClass().equals(String.class)) {
            values.put(key, (String) value);

        } else if (value.getClass().equals(Long.class)) {
            values.put(key, (Long) value);

        } else if (value.getClass().equals(Integer.class)) {
            values.put(key, (Integer) value);

        } else if (value.getClass().equals(Boolean.class)) {
            values.put(key, (Boolean) value);

        } else if (value.getClass().equals(Byte.class)) {
            values.put(key, (Byte) value);

        } else if (value.getClass().equals(Float.class)) {
            values.put(key, (Float) value);

        } else if (value.getClass().equals(Short.class)) {
            values.put(key, (Short) value);

        } else if (value.getClass().equals(Double.class)) {
            values.put(key, (Double) value);
        }
        return values;
    }

    public static ContentValues replaceKeyPrefix(ContentValues values, int replacementId)
    {
        ContentValues v = new ContentValues();
        for (String key : values.keySet())
        {
            String[] parts = key.split("_");
            parts[1] = Integer.toString(replacementId);
            String k = TextUtils.join("_", parts);
            v = putValueInto(v, k, values.get(key));
        }
        return v;
    }

    public static void copyValues(SharedPreferences prefs, int fromAppWidgetId, int toAppWidgetId) {
        copyValues(prefs, WidgetSettings.PREF_PREFIX_KEY, fromAppWidgetId, WidgetSettings.PREF_PREFIX_KEY, toAppWidgetId);
    }
    public static void copyValues(SharedPreferences prefs, String fromPrefix, int fromAppWidgetId, String toPrefix, int toAppWidgetId)
    {
        Map<String, ?> map = prefs.getAll();
        Set<String> keys = map.keySet();
        SharedPreferences.Editor editor = prefs.edit();

        for (String key : keys)
        {
            if (key.startsWith(fromPrefix + fromAppWidgetId))
            {
                String[] keyParts = key.split("_");
                keyParts[0] = toPrefix;
                keyParts[1] = toAppWidgetId + "";
                String toKey = TextUtils.join("_", keyParts);

                if (map.get(key).getClass().equals(String.class)) {
                    editor.putString(toKey, (String) map.get(key));

                } else if (map.get(key).getClass().equals(Integer.class)) {
                    editor.putInt(toKey, (Integer) map.get(key));

                } else if (map.get(key).getClass().equals(Long.class)) {
                    editor.putLong(toKey, (Long) map.get(key));

                } else if (map.get(key).getClass().equals(Float.class)) {
                    editor.putFloat(toKey, (Float) map.get(key));

                } else if (map.get(key).getClass().equals(Boolean.class)) {
                    editor.putBoolean(toKey, (Boolean) map.get(key));
                }
            }
        }
        editor.apply();
    }

    public static boolean importValue(SharedPreferences.Editor prefs, Class type, String key, Object value)
    {
        boolean retValue = true;
        if (type.equals(String.class)) {
            prefs.putString(key, (String) value);

        } else if (type.equals(Integer.class)) {
            prefs.putInt(key, (Integer) value);

        } else if (type.equals(Boolean.class)) {
            prefs.putBoolean(key, (Boolean) value);

        } else if (type.equals(Long.class)) {
            prefs.putLong(key, (Long) value);

        } else if (type.equals(Float.class)) {
            prefs.putFloat(key, (Float) value);
        } else retValue = false;

        if (retValue) {
            Log.i("WidgetSettings", "import: added " + key + " as type " + type.getSimpleName());
        } else Log.w("WidgetSettings", "import: skipping " + key + "... unrecognized type " + type.getSimpleName());

        return retValue;
    }

    public static void importValues(SharedPreferences.Editor prefs, ContentValues values, long appWidgetId) {
        importValues(prefs, values, null, appWidgetId);
    }
    public static void importValues(SharedPreferences.Editor prefs, ContentValues values, @Nullable String toPrefix, long appWidgetId)
    {
        Map<String,Class> prefTypes = WidgetSettings.getPrefTypes();
        prefTypes.putAll(CalendarSettings.getPrefTypes());
        prefTypes.putAll(WidgetActions.getPrefTypes());
        prefTypes.putAll(WorldMapWidgetSettings.getPrefTypes());
        //prefTypes.putAll(WidgetSettingsMetadata.getPrefTypes());    // skip these keys, avoid overwriting existing metadata 

        for (String key : values.keySet())
        {
            Object value = values.get(key);
            if (value == null) {
                Log.w("WidgetSettings", "import: skipping " + key + "... contains null value");
                continue;
            }

            String[] keyParts = key.split("_");
            if (toPrefix != null) {
                keyParts[0] = toPrefix;
            }
            keyParts[1] = appWidgetId + "";
            String k = TextUtils.join("_", keyParts);    // replacement key
            String k0 = k.replaceFirst(WidgetSettings.PREF_PREFIX_KEY + appWidgetId, "");

            if (prefTypes.containsKey(k0))
            {
                Class expectedType = prefTypes.get(k0);
                Class valueType = value.getClass();
                if (valueType.equals(expectedType)) {
                    importValue(prefs, expectedType, k, value);    // types match (direct cast)

                } else {
                    if (expectedType.equals(String.class)) {
                        importValue(prefs, String.class, k, value.toString());    // int, long, double, or bool as String

                    } else if (expectedType.equals(Boolean.class)) {
                        if (valueType.equals(String.class))    // bool as String
                        {
                            String s = (String) value;
                            if (s.toLowerCase().equals("true") || s.toLowerCase().equals("false")) {
                                importValue(prefs, Boolean.class, k, Boolean.parseBoolean(s));
                            } else Log.w("WidgetSettings", "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + s + " (String)");
                        } else Log.w("WidgetSettings", "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + valueType.getSimpleName());

                    } else if (expectedType.equals(Integer.class)) {
                        if (valueType.equals(String.class)) {    // int as String
                            try {
                                importValue(prefs, Integer.class, k, Integer.parseInt((String) value));
                            } catch (NumberFormatException e) {
                                Log.w("WidgetSettings", "import: skipping " + k + "... " + e);
                            }
                        } else Log.w("WidgetSettings", "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + valueType.getSimpleName());

                    } else if (expectedType.equals(Long.class)) {
                        if (valueType.equals(String.class)) {    // long as String
                            try {
                                importValue(prefs, Long.class, k, Long.parseLong((String) value));
                            } catch (NumberFormatException e) {
                                Log.w("WidgetSettings", "import: skipping " + k + "... " + e);
                            }
                        } else Log.w("WidgetSettings", "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + valueType.getSimpleName());

                    } else if (expectedType.equals(Double.class)) {
                        if (valueType.equals(String.class)) {    // double as String
                            try {
                                importValue(prefs, Double.class, k, Double.parseDouble((String) value));
                            } catch (NumberFormatException e) {
                                Log.w("WidgetSettings", "import: skipping " + k + "... " + e);
                            }
                        } else Log.w("WidgetSettings", "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + valueType.getSimpleName());
                    }
                }
            } else {
                Log.w("WidgetSettings", "import: skipping " + k0 + "... unrecognized key");
            }
        }
        prefs.apply();
    }

    public static Long findAppWidgetIdFromFirstKey(ContentValues values)
    {
        String[] keys = values.keySet().toArray(new String[0]);
        if (keys.length > 0)
        {
            try {
                String[] parts = keys[0].split("_");
                return Long.parseLong(parts[1]);

            } catch (NumberFormatException e) {
                Log.w("WidgetSettings", "failed to find widget id from keys.. " + e);
            }
        }
        return null;
    }

}