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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings;
import com.forrestguice.suntimeswidget.widgets.ClockWidgetSettings;
import com.forrestguice.support.app.AlertDialog;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WidgetSettingsImportTask extends AsyncTask<Uri, ContentValues, WidgetSettingsImportTask.TaskResult>
{
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
                    readData(context, in, items);
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

    protected void readData(Context context, InputStream in, ArrayList<ContentValues> items) throws IOException
    {
        BufferedInputStream bufferedIn = new BufferedInputStream(in);
        if (SuntimesBackupLoadTask.containsBackupItem(bufferedIn)) {
            readItemsFromBackup(context, bufferedIn, items);

        } else {
            ContentValuesJson.readItems(context, bufferedIn, items);
        }
    }

    protected static void readItemsFromBackup(Context context, BufferedInputStream in, ArrayList<ContentValues> items) throws IOException
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            //noinspection CharsetObjectCanBeUsed
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.setLenient(true);
            try {
                Map<String,ContentValues[]> data = new HashMap<>();
                SuntimesBackupLoadTask.readBackupItem(context, reader, data);
                items.addAll(Arrays.asList(data.get(SuntimesBackupTask.KEY_WIDGETSETTINGS)));

            } finally {
                reader.close();
                in.close();
            }

        } else {
            Log.w("ImportSettings", "Unsupported; skipping import");
            in.close();
        }
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

        private final boolean result;
        public boolean getResult()
        {
            return result;
        }

        private final ContentValues[] items;
        public ContentValues[] getItems()
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
     * ContentValuesJson
     */
    public static class ContentValuesJson
    {
        public static final String TAG = "ContentValuesJson";

        /**
         * Currently reads..
         *     [{ ContentValues }, ...]
         *     { ContentValues }
         */
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

        @TargetApi(11)
        protected static void skipJsonItem(JsonReader reader) throws IOException
        {
            switch (reader.peek()) {
                case BEGIN_ARRAY: skipJsonArray(reader); break;
                case BEGIN_OBJECT: skipJsonObject(reader); break;
                default: reader.skipValue(); break;
            }
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
        Set<Map.Entry<String, Object>> entries = values.valueSet();
        for (Map.Entry<String, Object> entry : entries)
        {
            String[] parts = entry.getKey().split("_");
            parts[1] = Integer.toString(replacementId);
            String k = TextUtils.join("_", parts);
            v = putValueInto(v, k, entry.getValue());
        }
        return v;
    }

    /**
     * @param prefs SharedPreferences
     * @param prefix prefix string
     * @param appWidgetId appWidgetId; null to delete all ids
     * @return true items were removed, false otherwise
     */
    public static boolean deleteValues(SharedPreferences prefs, String prefix, Integer appWidgetId)
    {
        Map<String, ?> map = prefs.getAll();
        Set<String> keys = map.keySet();

        String keyPrefix = (appWidgetId != null ? prefix + appWidgetId : prefix);
        SharedPreferences.Editor editor = prefs.edit();
        boolean result = false;
        for (String key : keys)
        {
            if (key.startsWith(keyPrefix)) {
                editor.remove(key);
                result = true;
            }
        }
        editor.apply();
        return result;
    }

    public static boolean copyValues(SharedPreferences prefs, int fromAppWidgetId, int toAppWidgetId) {
        return copyValues(prefs, WidgetSettings.PREF_PREFIX_KEY, fromAppWidgetId, prefs.edit(), WidgetSettings.PREF_PREFIX_KEY, toAppWidgetId);
    }
    public static boolean copyValues(SharedPreferences fromPrefs, String fromPrefix, int fromAppWidgetId, SharedPreferences.Editor toPrefs, String toPrefix, int toAppWidgetId)
    {
        Map<String, ?> map = fromPrefs.getAll();
        Set<String> keys = map.keySet();

        boolean result = false;
        for (String key : keys)
        {
            if (key.startsWith(fromPrefix + fromAppWidgetId))
            {
                String[] keyParts = key.split("_");
                keyParts[0] = toPrefix;
                keyParts[1] = toAppWidgetId + "";
                String toKey = TextUtils.join("_", keyParts);

                if (map.get(key).getClass().equals(String.class)) {
                    toPrefs.putString(toKey, (String) map.get(key));
                    result = true;

                } else if (map.get(key).getClass().equals(Integer.class)) {
                    toPrefs.putInt(toKey, (Integer) map.get(key));
                    result = true;

                } else if (map.get(key).getClass().equals(Long.class)) {
                    toPrefs.putLong(toKey, (Long) map.get(key));
                    result = true;

                } else if (map.get(key).getClass().equals(Float.class)) {
                    toPrefs.putFloat(toKey, (Float) map.get(key));
                    result = true;

                } else if (map.get(key).getClass().equals(Boolean.class)) {
                    toPrefs.putBoolean(toKey, (Boolean) map.get(key));
                    result = true;
                }
            }
        }
        toPrefs.apply();
        return result;
    }

    public static boolean importValue(SharedPreferences.Editor prefs, Class<?> type, String key, Object value)
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
    public static void importValues(SharedPreferences.Editor prefs, ContentValues values, @Nullable String toPrefix, @Nullable Long appWidgetId) {
        importValues(prefs, values, toPrefix, appWidgetId, false);
    }
    public static void importValues(SharedPreferences.Editor prefs, ContentValues values, @Nullable String toPrefix, @Nullable Long appWidgetId, boolean includeMetadata)
    {
        Map<String,Class> prefTypes = WidgetSettings.getPrefTypes();
        prefTypes.putAll(AlarmWidgetSettings.getPrefTypes());
        prefTypes.putAll(ClockWidgetSettings.getPrefTypes());
        prefTypes.putAll(CalendarSettings.getPrefTypes());
        prefTypes.putAll(WidgetActions.getPrefTypes());
        prefTypes.putAll(WorldMapWidgetSettings.getPrefTypes());
        if (includeMetadata) {
            prefTypes.putAll(WidgetSettingsMetadata.getPrefTypes());
        }
        importValues(prefs, prefTypes, values, true, toPrefix, appWidgetId, "WidgetSettings");
    }

    public static void importValues(SharedPreferences.Editor prefs, Map<String,Class> prefTypes, ContentValues values, boolean hasPrefix, @Nullable String toPrefix, @Nullable Long appWidgetId, String tag)
    {
        Set<Map.Entry<String, Object>> entries = values.valueSet();
        for (Map.Entry<String, Object> entry : entries)
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                Log.w(tag, "import: skipping " + key + "... contains null value");
                continue;
            }

            String k = key;
            String k0 = k;
            if (hasPrefix)
            {
                String[] keyParts = key.split("_");
                if (toPrefix != null) {
                    keyParts[0] = toPrefix.endsWith("_") ? toPrefix.substring(0, toPrefix.length() - 1) : toPrefix;
                }
                if (appWidgetId != null) {
                    keyParts[1] = appWidgetId + "";
                }

                k = TextUtils.join("_", keyParts);    // full replacement key
                k0 = k.replaceFirst(keyParts[0] + "_" + keyParts[1], "");    // replacement key w/out prefix
            }

            if (prefTypes.containsKey(k0))
            {
                Class<?> expectedType = prefTypes.get(k0);
                Class<?> valueType = value.getClass();
                if (valueType.equals(expectedType)) {
                    importValue(prefs, expectedType, k, value);    // types match (direct cast)

                } else {
                    if (expectedType.equals(String.class)) {
                        importValue(prefs, String.class, k, value.toString());    // int, long, double, or bool as String

                    } else if (expectedType.equals(Boolean.class)) {
                        if (valueType.equals(String.class))    // bool as String
                        {
                            String s = (String) value;
                            if (s.toLowerCase(Locale.ROOT).equals("true") || s.toLowerCase(Locale.ROOT).equals("false")) {
                                importValue(prefs, Boolean.class, k, Boolean.parseBoolean(s));
                            } else Log.w(tag, "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + s + " (String)");
                        } else Log.w(tag, "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + valueType.getSimpleName());

                    } else if (expectedType.equals(Integer.class)) {
                        if (valueType.equals(String.class)) {    // int as String
                            try {
                                importValue(prefs, Integer.class, k, Integer.parseInt((String) value));
                            } catch (NumberFormatException e) {
                                Log.w(tag, "import: skipping " + k + "... " + e);
                            }
                        } else Log.w(tag, "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + valueType.getSimpleName());

                    } else if (expectedType.equals(Long.class)) {
                        if (valueType.equals(String.class)) {    // long as String
                            try {
                                importValue(prefs, Long.class, k, Long.parseLong((String) value));
                            } catch (NumberFormatException e) {
                                Log.w(tag, "import: skipping " + k + "... " + e);
                            }
                        } else Log.w(tag, "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + valueType.getSimpleName());

                    } else if (expectedType.equals(Double.class)) {
                        if (valueType.equals(String.class)) {    // double as String
                            try {
                                importValue(prefs, Double.class, k, Double.parseDouble((String) value));
                            } catch (NumberFormatException e) {
                                Log.w(tag, "import: skipping " + k + "... " + e);
                            }
                        } else Log.w(tag, "import: skipping " + k + "... expected " + expectedType.getSimpleName() + ", found " + valueType.getSimpleName());
                    }
                }
            } else {
                Log.w(tag, "import: skipping " + k0 + "... unrecognized key");
            }
        }
        prefs.apply();
    }

    @Nullable
    public static Long findAppWidgetIdFromFirstKey(ContentValues values)
    {
        Set<Map.Entry<String, Object>> entries = values.valueSet();
        if (entries.size() > 0)
        {
            for (Map.Entry<String,Object> entry : entries)
            {
                try {
                    String key = entry.getKey();
                    String[] parts = ((key != null) ? key.split("_") : new String[0]);
                    if (parts.length > 2) {
                        return Long.parseLong(parts[1]);
                    }

                } catch (NumberFormatException | NullPointerException e) {
                    Log.w("WidgetSettings", "failed to find widget id from keys.. " + e);
                }
            }
        }
        return null;
    }

    /**
     * @param oldAppWidgetIds array of old widget ids
     * @param newAppWidgetIds array of new replacement widget ids
     * @return array of true/false results for each old/new pair of old/new ids
     */
    public static boolean[] restoreFromBackup(Context context, int[] oldAppWidgetIds, int[] newAppWidgetIds) {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        return restoreFromBackup(prefs, oldAppWidgetIds, newAppWidgetIds);
    }
    public static boolean[] restoreFromBackup(SharedPreferences prefs, int[] oldAppWidgetIds, int[] newAppWidgetIds) {
        return restoreFromBackup(prefs, prefs.edit(), oldAppWidgetIds, newAppWidgetIds);
    }
    public static boolean[] restoreFromBackup(SharedPreferences fromPrefs, SharedPreferences.Editor toPrefs, int[] oldAppWidgetIds, int[] newAppWidgetIds)
    {
        if (oldAppWidgetIds != null && newAppWidgetIds != null
                && oldAppWidgetIds.length == newAppWidgetIds.length)
        {
            boolean[] results = new boolean[oldAppWidgetIds.length];
            for (int i=0; i<oldAppWidgetIds.length; i++)
            {
                Log.i("WidgetSettings", "restoreFromBackup: " + oldAppWidgetIds[i] + " -> " + newAppWidgetIds[i]);
                results[i] = copyValues(fromPrefs, WidgetSettingsMetadata.BACKUP_PREFIX_KEY, oldAppWidgetIds[i], toPrefs, WidgetSettings.PREF_PREFIX_KEY, newAppWidgetIds[i]);
                //if (results[i]) {
                //    deleteValues(fromPrefs, WidgetSettingsMetadata.BACKUP_PREFIX_KEY, oldAppWidgetIds[i]);
                //}
            }
            return results;

        } else {
            Log.e("WidgetSettings", "restoreFromBackup: arrays must be non-null with matching length! ignoring request...");
            return new boolean[] { false };
        }
    }

    public static void clearBackup(Context context, SharedPreferences prefs ) {
        deleteValues(prefs, WidgetSettingsMetadata.BACKUP_PREFIX_KEY, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int IMPORT_WIDGETS_METHOD_RESTOREBACKUP = 0;
    public static final int IMPORT_WIDGETS_METHOD_MAKEBESTGUESS = 1;
    public static final int IMPORT_WIDGETS_METHOD_DIRECTIMPORT = 2;
    public static final int[] IMPORT_WIDGETS_METHODS = new int[] {IMPORT_WIDGETS_METHOD_RESTOREBACKUP, IMPORT_WIDGETS_METHOD_MAKEBESTGUESS, IMPORT_WIDGETS_METHOD_DIRECTIMPORT};

    public static void chooseWidgetSettingsImportMethod(final Context context, final int[] methods, @NonNull final DialogInterface.OnClickListener onClickListener)
    {
        final CharSequence[] items = new CharSequence[methods.length];
        for (int i=0; i<items.length; i++) {
            items[i] = displayStringForImportMethod(context, methods[i]);
        }
        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.restorebackup_dialog_item_widgetsettings))
                .setIcon(R.drawable.ic_action_widget)
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { /* EMPTY */ }
                })
                .setPositiveButton(context.getString(R.string.configAction_import), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        int p = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        onClickListener.onClick(dialog, methods[p]);
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), null);
        confirm.show();
    }
    protected static CharSequence displayStringForImportMethod(Context context, int method)
    {
        switch (method) {
            case IMPORT_WIDGETS_METHOD_DIRECTIMPORT: return SuntimesUtils.fromHtml(context.getString(R.string.importwidget_dialog_item_direct));
            case IMPORT_WIDGETS_METHOD_MAKEBESTGUESS: return SuntimesUtils.fromHtml(context.getString(R.string.importwidget_dialog_item_bestguess));
            case IMPORT_WIDGETS_METHOD_RESTOREBACKUP: return SuntimesUtils.fromHtml(context.getString(R.string.importwidget_dialog_item_restorebackup));
            default: return method + "";
        }
    }

}