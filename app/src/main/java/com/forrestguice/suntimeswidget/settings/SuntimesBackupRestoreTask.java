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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesWidgetListActivity;
import com.forrestguice.suntimeswidget.settings.WidgetSettingsImportTask.ContentValuesJson;
import com.forrestguice.suntimeswidget.tiles.ClockTileService;
import com.forrestguice.suntimeswidget.tiles.NextEventTileService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.forrestguice.suntimeswidget.settings.WidgetSettingsImportTask.IMPORT_METHOD_DIRECTIMPORT;
import static com.forrestguice.suntimeswidget.settings.WidgetSettingsImportTask.IMPORT_METHOD_MAKEBESTGUESS;
import static com.forrestguice.suntimeswidget.settings.WidgetSettingsImportTask.IMPORT_METHOD_RESTOREBACKUP;

public class SuntimesBackupRestoreTask extends AsyncTask<Uri, Void, SuntimesBackupRestoreTask.TaskResult>
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

    public SuntimesBackupRestoreTask(Context context) {
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
            //noinspection CharsetObjectCanBeUsed
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.setLenient(true);
            try {
                readBackupItem(context, reader, data);

            } finally {
                reader.close();
                in.close();
            }
        } else {
            Log.w("ImportSettings", "Unsupported; skipping import");
            in.close();
        }
    }

    @TargetApi(11)
    protected void readBackupItem(Context context, JsonReader reader, Map<String, ContentValues[]> data) throws IOException
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
     * @param context Context context
     * @param keys the set of backup keys within allValues that should be restored
     * @param method 2:directImport (copy widget ids as-is), 1:bestGuess (reassign widget ids (best guess)), 0:restoreBackup (import as backup for later (when the launcher initiates restoration))
     * @param allValues a map containing backupKey:ContentValue[]; e.g. "AppSettings":ContentValues[], "WidgetSettings":ContentValues[], ...
     * @return number of items imported
     */
    public static int importSettings(Context context, int method, Set<String> keys, StringBuilder report, Map<String, ContentValues[]> allValues)
    {
        int c = 0;

        if (keys.contains(SuntimesBackupTask.KEY_APPSETTINGS)) {
            c += (SuntimesBackupRestoreTask.importAppSettings(context, report, allValues.get(SuntimesBackupTask.KEY_APPSETTINGS)) ? 1 : 0);
        }

        if (keys.contains(SuntimesBackupTask.KEY_WIDGETSETTINGS))
        {
            switch (method)
            {
                case IMPORT_METHOD_DIRECTIMPORT:    // direct import
                    c += SuntimesBackupRestoreTask.importWidgetSettings(context, null, false, report, allValues.get(SuntimesBackupTask.KEY_WIDGETSETTINGS));
                    break;

                case IMPORT_METHOD_MAKEBESTGUESS:    // best guess
                    c += SuntimesBackupRestoreTask.importWidgetSettingsBestGuess(context, report, allValues.get(SuntimesBackupTask.KEY_WIDGETSETTINGS));
                    break;

                case IMPORT_METHOD_RESTOREBACKUP:
                default:   // backup import (writes to backup prefix, individual widgets restore themselves later when triggered)
                    c += SuntimesBackupRestoreTask.importWidgetSettings(context, WidgetSettingsMetadata.BACKUP_PREFIX_KEY, true, report, allValues.get(SuntimesBackupTask.KEY_WIDGETSETTINGS));
                    WidgetSettingsImportTask.restoreFromBackup(context,
                            new int[] {0, ClockTileService.CLOCKTILE_APPWIDGET_ID, NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID},    // these lines should be the same
                            new int[] {0, ClockTileService.CLOCKTILE_APPWIDGET_ID, NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID});   // because the ids are unchanged
                    break;
            }
        }

        if (keys.contains(SuntimesBackupTask.KEY_ALARMITEMS)) {
            c += SuntimesBackupRestoreTask.importAlarmItems(context, report, allValues.get(SuntimesBackupTask.KEY_ALARMITEMS));
        }

        if (keys.contains(SuntimesBackupTask.KEY_EVENTITEMS)) {
            c += SuntimesBackupRestoreTask.importEventItems(context, report, allValues.get(SuntimesBackupTask.KEY_EVENTITEMS));
        }

        return c;
    }

    /**
     * importAppSettings
     */
    public static boolean importAppSettings(Context context, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        if (contentValues != null)
        {
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
            for (ContentValues values : contentValues)
            {
                if (values != null) {
                    //WidgetSettingsImportTask.importValues(prefs, values, prefix, null, includeMetadata);  // TODO
                    report.append(context.getString(R.string.restorebackup_dialog_report_format, SuntimesBackupTask.displayStringForBackupKey(context, SuntimesBackupTask.KEY_APPSETTINGS)));
                    report.append("\n");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * importAlarmItems
     */
    public static int importAlarmItems(Context context, StringBuilder report, @Nullable ContentValues... contentValues) {
        return 0;  // TODO
    }

    /**
     * importEventItems
     */
    public static int importEventItems(Context context, StringBuilder report, @Nullable ContentValues... contentValues) {
        return 0;  // TODO
    }

    /**
     * importPlaceItems
     */
    public static int importPlaceItems(Context context, StringBuilder report, @Nullable ContentValues... contentValues) {
        return 0;  // TODO
    }

    /**
     * importWidgetSettings
     */
    public static int importWidgetSettings(Context context, String prefix, boolean includeMetadata, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        if (contentValues != null)
        {
            SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
            int c = 0;
            for (ContentValues values : contentValues)
            {
                Long id = WidgetSettingsImportTask.findAppWidgetIdFromFirstKey(values);
                WidgetSettingsMetadata.WidgetMetadata metadata = WidgetSettingsMetadata.WidgetMetadata.getMetaDataFromValues(values);
                WidgetSettingsImportTask.importValues(prefs, values, prefix, null, includeMetadata);
                report.append(context.getString(R.string.importwidget_dialog_report_format, id + "", metadata.getWidgetClassName()));
                report.append("\n");
                c++;
            }
            return c;
        } else return 0;
    }

    /**
     * Tries to match contentValues to existing widgetIds based on available metadata.
     * @return suggested appWidget:ContentValues mapping
     */
    protected static Map<Integer,ContentValues> makeBestGuess(Context context, ContentValues... contentValues)
    {
        ArrayList<WidgetSettingsMetadata.WidgetMetadata> unusedKeys = new ArrayList<>();
        ArrayList<ContentValues> unusedValues = new ArrayList<>();
        for (int i=0; i<contentValues.length; i++)
        {
            ContentValues values = contentValues[i];
            WidgetSettingsMetadata.WidgetMetadata key = WidgetSettingsMetadata.WidgetMetadata.getMetaDataFromValues(values);
            unusedKeys.add(key);
            unusedValues.add(values);
        }

        ArrayList<Integer> widgetIds = new ArrayList<>();
        for (Class widgetClass : SuntimesWidgetListActivity.WidgetListAdapter.ALL_WIDGETS) {
            widgetIds.addAll(SuntimesBackupTask.getAllWidgetIds(context, widgetClass));
        }
        widgetIds.add(0);
        widgetIds.add(ClockTileService.CLOCKTILE_APPWIDGET_ID);
        widgetIds.add(NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID);

        Map<Integer, ContentValues> suggested = new HashMap<>();
        for (Integer appWidgetId : widgetIds)
        {
            WidgetSettingsMetadata.WidgetMetadata metadata = WidgetSettingsMetadata.loadMetaData(context, appWidgetId);
            if (unusedKeys.contains(metadata))
            {
                //Log.d("DEBUG", "makeBestGuess: " + appWidgetId + " :: " + metadata.getWidgetClassName());
                int i = unusedKeys.indexOf(metadata);
                unusedKeys.remove(i);
                ContentValues values = unusedValues.remove(i);
                suggested.put(appWidgetId, values);
            }
        }
        return suggested;
    }

    public static int importWidgetSettingsBestGuess(Context context, StringBuilder report, ContentValues... contentValues)
    {
        WidgetSettingsExportTask.addWidgetMetadata(context);
        Map<Integer, ContentValues> suggested = makeBestGuess(context, contentValues);
        int numMatches = suggested.size();
        if (numMatches > 0)     // matched some
        {
            SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
            for (Integer appWidgetId : suggested.keySet())
            {
                ContentValues values = suggested.get(appWidgetId);
                WidgetSettingsImportTask.importValues(prefs, values, appWidgetId);

                String widgetClassName = WidgetSettingsMetadata.loadMetaData(context, appWidgetId).getWidgetClassName();
                report.append(context.getString(R.string.importwidget_dialog_report_format, appWidgetId + "", widgetClassName));
                report.append("\n");
            }
            return numMatches;

        } else {               // matched none
            return 0;
        }
    }

    /**
     * showIOResultSnackbar
     */
    public static void showIOResultSnackbar(final Context context, View view, boolean result, int numResults, @Nullable CharSequence report)
    {
        //Toast.makeText(context, context.getString(R.string.msg_import_success, context.getString(R.string.configAction_settings)), Toast.LENGTH_SHORT).show();
        //Toast.makeText(context, context.getString(R.string.msg_import_failure, context.getString(R.string.msg_import_label_file)), Toast.LENGTH_SHORT).show();
        CharSequence message = (result ? context.getString(R.string.msg_import_success, context.getResources().getQuantityString(R.plurals.itemsPlural, numResults, numResults))
                : context.getString(R.string.msg_import_failure, context.getString(R.string.msg_import_label_file)));
        SuntimesBackupTask.showIOResultSnackbar(context, view, result, message, report);
    }

}