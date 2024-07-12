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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.ui.bedtime.BedtimeSettings;
import com.forrestguice.suntimeswidget.colors.AppColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.getfix.GetFixDatabaseAdapter;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValuesCollection;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.tiles.AlarmTileService;
import com.forrestguice.suntimeswidget.tiles.ClockTileService;
import com.forrestguice.suntimeswidget.tiles.NextEventTileService;
import com.forrestguice.suntimeswidget.widgets.WidgetListAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SuntimesBackupRestoreTask extends AsyncTask<Void, Void, SuntimesBackupRestoreTask.TaskResult>
{
    public static final String TAG = "RestoreBackup";

    protected final WeakReference<Context> contextRef;
    public SuntimesBackupRestoreTask(Context context) {
        contextRef = new WeakReference<>(context);
    }

    protected Map<String, ContentValues[]> data = new HashMap<>();    // all backup data
    protected Set<String> keys = new TreeSet<>();                     // keys to restore

    public void setData(Map<String, ContentValues[]> d) {
        data = d;
    }
    public void setKeys(Set<String> included) {
        keys = included;
    }

    protected Map<String, Integer> methods = new HashMap<>();
    public void setMethod(String key, int method) {
        methods.put(key, method);
    }
    public void setMethods(Map<String,Integer> values) {
        methods = values;
    }

    @Override
    protected void onPreExecute()
    {
        //Log.d(TAG, "onPreExecute");
        if (taskListener != null) {
            taskListener.onStarted();
        }
    }

    @Override
    protected TaskResult doInBackground(Void... params)
    {
        Log.d(TAG, "doInBackground: starting");
        long startTime = System.currentTimeMillis();

        int c = 0;
        boolean result = false;
        Exception error = null;
        StringBuilder report = new StringBuilder();

        Context context = contextRef.get();
        if (context != null && data != null && keys != null)
        {
            try {
                c = importSettings(context, keys, methods, report, data);
                result = true;
                error = null;

            } catch (Exception e) {
                Log.e(TAG, "Failed to restore backup: " + e);
                result = false;
                error = e;
            }
        } else {
            result = false;
            error = null;
        }

        Log.d(TAG, "doInBackground: waiting");
        long endTime = System.currentTimeMillis();
        while ((endTime - startTime) < SuntimesBackupLoadTask.MIN_WAIT_TIME) {
            endTime = System.currentTimeMillis();
        }

        Log.d(TAG, "doInBackground: finishing");
        return new TaskResult(result, report.toString(), c, error);
    }

    @Override
    protected void onProgressUpdate(Void... progressItems) {
        super.onProgressUpdate(progressItems);
    }

    @Override
    protected void onPostExecute( TaskResult result )
    {
        //Log.d(TAG, "onPostExecute: " + result.getResult());
        if (taskListener != null) {
            taskListener.onFinished(result);
        }
    }

    /**
     * TaskResult
     */
    public static class TaskResult
    {
        public TaskResult(boolean result, String report, int numResults, Exception e)
        {
            this.result = result;
            this.report = report;
            this.numResults = numResults;
            this.e = e;
        }

        private final boolean result;
        public boolean getResult() {
            return result;
        }

        private final String report;
        public String getReport() {
            return report;
        }

        private final int numResults;
        public int getNumResults() {
            return numResults;
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
     * @param methods 2:directImport (copy widget ids as-is), 1:bestGuess (reassign widget ids (best guess)), 0:restoreBackup (import as backup for later (when the launcher initiates restoration))
     * @param allValues a map containing backupKey:ContentValue[]; e.g. "AppSettings":ContentValues[], "WidgetSettings":ContentValues[], ...
     * @return number of items imported
     */
    public static int importSettings(Context context, Set<String> keys, Map<String, Integer> methods, StringBuilder report, Map<String, ContentValues[]> allValues)
    {
        int c = 0;

        if (keys.contains(SuntimesBackupTask.KEY_ACTIONS)) {
            c += (importActions(context, report, allValues.get(SuntimesBackupTask.KEY_ACTIONS)) ? 1 : 0);
        }

        if (keys.contains(SuntimesBackupTask.KEY_EVENTITEMS)) {
            c += importEventItems(context, report, allValues.get(SuntimesBackupTask.KEY_EVENTITEMS));
        }

        if (keys.contains(SuntimesBackupTask.KEY_PLACEITEMS))
        {
            int method = (methods.containsKey(SuntimesBackupTask.KEY_PLACEITEMS))
                    ? methods.get(SuntimesBackupTask.KEY_PLACEITEMS) : IMPORT_PLACES_METHOD_ADDALL;
            c += importPlaceItems(context, method, report, allValues.get(SuntimesBackupTask.KEY_PLACEITEMS));
        }

        if (keys.contains(SuntimesBackupTask.KEY_APPSETTINGS)) {
            c += (importAppSettings(context, report, allValues.get(SuntimesBackupTask.KEY_APPSETTINGS)) ? 1 : 0);
        }

        if (keys.contains(SuntimesBackupTask.KEY_COLORS)) {
            c += importAppColors(context, SuntimesBackupTask.KEY_COLORS_APPCOLORS, 0, report, allValues.get(SuntimesBackupTask.KEY_COLORS_APPCOLORS));
            c += importMapColors(context, SuntimesBackupTask.KEY_COLORS_MAPCOLORS, 0, report, allValues.get(SuntimesBackupTask.KEY_COLORS_MAPCOLORS));
        }

        if (keys.contains(SuntimesBackupTask.KEY_ALARMITEMS))
        {
            int method = (methods.containsKey(SuntimesBackupTask.KEY_ALARMITEMS))
                    ? methods.get(SuntimesBackupTask.KEY_ALARMITEMS) : IMPORT_ALARMS_METHOD_ADDALL;
            c += importAlarmItems(context, method, report, allValues.get(SuntimesBackupTask.KEY_ALARMITEMS));
        }

        if (keys.contains(SuntimesBackupTask.KEY_WIDGETTHEMES)) {
            c += importWidgetThemes(context, report, allValues.get(SuntimesBackupTask.KEY_WIDGETTHEMES));
        }

        if (keys.contains(SuntimesBackupTask.KEY_WIDGETSETTINGS))
        {
            int method = (methods.containsKey(SuntimesBackupTask.KEY_WIDGETSETTINGS))
                    ? methods.get(SuntimesBackupTask.KEY_WIDGETSETTINGS) : IMPORT_WIDGETS_METHOD_RESTOREBACKUP;
            c += importWidgetSettings(context, method, report, allValues.get(SuntimesBackupTask.KEY_WIDGETSETTINGS));
        }

        return c;
    }

    /**
     * importAppSettings
     */
    protected static boolean importAppSettings(Context context, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        Map<String,Class> prefTypes = AppSettings.getPrefTypes();
        prefTypes.putAll(AlarmSettings.getPrefTypes());
        prefTypes.putAll(BedtimeSettings.getPrefTypes());
        prefTypes.putAll(AppColorValuesCollection.getPrefTypes());
        prefTypes.putAll(WorldMapColorValuesCollection.getPrefTypes());

        if (contentValues != null)
        {
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
            for (ContentValues values : contentValues)
            {
                if (values != null) {
                    WidgetSettingsImportTask.importValues(prefs, prefTypes, values, false, null, null, "AppSettings");
                    report.append(context.getString(R.string.restorebackup_dialog_report_format, SuntimesBackupTask.displayStringForBackupKey(context, SuntimesBackupTask.KEY_APPSETTINGS)));
                    report.append("\n");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * importColors
     */
    protected static int importAppColors(Context context, String key, int method, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        return importColors(context, key, method, new ColorValuesImporter()
        {
            @Override
            public ColorValues createColorValues(Context context) {
                return new AppColorValues(context, true);
            }
            @Override
            public ColorValuesCollection<ColorValues> createColorValuesCollection(Context context) {
                return new AppColorValuesCollection<ColorValues>();
            }
        }, report, contentValues);
    }
    protected static int importMapColors(Context context, String key, int method, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        return importColors(context, key, method, new ColorValuesImporter()
        {
            @Override
            public ColorValues createColorValues(Context context) {
                return new WorldMapColorValues(context, true);
            }
            @Override
            public ColorValuesCollection<ColorValues> createColorValuesCollection(Context context) {
                return new WorldMapColorValuesCollection<ColorValues>();
            }
        }, report, contentValues);
    }

    protected static int importColors(Context context, String key, int method, ColorValuesImporter importer, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        int c = 0;
        if (contentValues != null)
        {
            ColorValuesCollection<ColorValues> collection = importer.createColorValuesCollection(context);
            for (ContentValues values : contentValues)
            {
                if (values != null)
                {
                    String colorsID = values.getAsString(ColorValues.KEY_ID);
                    if (colorsID != null)
                    {
                        ColorValues v = importer.createColorValues(context);
                        v.loadColorValues(values);
                        collection.setColors(context, colorsID, v);
                        c++;
                    }
                }
            }
        }

        report.append(context.getString(R.string.restorebackup_dialog_report_format1, SuntimesBackupTask.displayStringForBackupKey(context, key), c+""));
        report.append("\n");
        return c;
    }
    public abstract static class ColorValuesImporter
    {
        public abstract ColorValues createColorValues(Context context);
        public abstract ColorValuesCollection<ColorValues> createColorValuesCollection(Context context);
    }

    /**
     * importWidgetSettings
     */
    protected static int importWidgetSettings(Context context, int method, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        report.append(context.getString(R.string.restorebackup_dialog_report_format, SuntimesBackupTask.displayStringForBackupKey(context, SuntimesBackupTask.KEY_WIDGETSETTINGS)));
        report.append("\n");

        int c = 0;
        switch (method)
        {
            case IMPORT_WIDGETS_METHOD_DIRECTIMPORT:    // direct import
                c += importWidgetSettings(context, null, false, report, contentValues);
                break;

            case IMPORT_WIDGETS_METHOD_MAKEBESTGUESS:    // best guess
                c += importWidgetSettingsBestGuess(context, report, contentValues);
                break;

            case IMPORT_WIDGETS_METHOD_RESTOREBACKUP:
            default:   // backup import (writes to backup prefix, individual widgets restore themselves later when triggered)
                c += importWidgetSettings(context, WidgetSettingsMetadata.BACKUP_PREFIX_KEY, true, report, contentValues);
                WidgetSettingsImportTask.restoreFromBackup(context,
                        new int[] {0, ClockTileService.CLOCKTILE_APPWIDGET_ID, NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID, AlarmTileService.ALARMTILE_APPWIDGET_ID},    // these lines should be the same
                        new int[] {0, ClockTileService.CLOCKTILE_APPWIDGET_ID, NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID, AlarmTileService.ALARMTILE_APPWIDGET_ID});   // because the ids are unchanged
                break;
        }
        return c;
    }

    /**
     * importWidgetThemes
     */
    protected static int importWidgetThemes(Context context, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        int c = 0;
        if (contentValues != null)
        {
            for (ContentValues values : contentValues)
            {
                SuntimesTheme theme = new SuntimesTheme(values);
                SuntimesTheme.ThemeDescriptor descriptor = theme.saveTheme(context, WidgetThemes.PREFS_THEMES);
                WidgetThemes.addValue(context, descriptor);
                c++;
            }
        }
        report.append(context.getString(R.string.restorebackup_dialog_report_format1, SuntimesBackupTask.displayStringForBackupKey(context, SuntimesBackupTask.KEY_WIDGETTHEMES), c+""));
        report.append("\n");
        return c;
    }

    /**
     * importAlarmItems
     */
    protected static int importAlarmItems(Context context, int method, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        int c = 0;
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context);
        db.open();
        if (method == IMPORT_ALARMS_METHOD_CLEAR) {
            db.clearAlarms();
        }
        if (contentValues != null)
        {
            for (ContentValues values : contentValues)
            {
                if (values != null)
                {
                    if (values.containsKey(AlarmDatabaseAdapter.KEY_ROWID)) {
                        values.remove(AlarmDatabaseAdapter.KEY_ROWID);    // clear rowID (insert as new items)
                    }
                    db.addAlarm(values);
                    c++;
                }
            }
        }

        db.close();
        report.append(context.getString(R.string.restorebackup_dialog_report_format1, SuntimesBackupTask.displayStringForBackupKey(context, SuntimesBackupTask.KEY_ALARMITEMS), c+""));
        report.append("\n");
        return c;
    }

    /**
     * importEventItems
     */
    protected static int importEventItems(Context context, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        int c = 0;
        if (contentValues != null)
        {
            for (ContentValues values : contentValues)
            {
                if (values != null) {
                    EventSettings.saveEvent(context, new EventSettings.EventAlias(values));
                    c++;
                }
            }
        }
        report.append(context.getString(R.string.restorebackup_dialog_report_format1, SuntimesBackupTask.displayStringForBackupKey(context, SuntimesBackupTask.KEY_EVENTITEMS), c+""));
        report.append("\n");
        return c;
    }

    /**
     * importPlaceItems
     */
    protected static int importPlaceItems(Context context, int method, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        int c = 0;
        GetFixDatabaseAdapter db = new GetFixDatabaseAdapter(context);
        db.open();
        if (method == IMPORT_PLACES_METHOD_CLEAR) {
            db.clearPlaces();
        }
        if (contentValues != null)
        {
            for (ContentValues values : contentValues)
            {
                if (values != null)
                {
                    if (values.containsKey(GetFixDatabaseAdapter.KEY_ROWID)) {
                        values.remove(GetFixDatabaseAdapter.KEY_ROWID);    // clear rowID (insert as new items)
                    }
                    if (db.addPlace(values) >= 0) {
                        c++;
                    }
                }
            }
        }

        db.close();
        report.append(context.getString(R.string.restorebackup_dialog_report_format1, SuntimesBackupTask.displayStringForBackupKey(context, SuntimesBackupTask.KEY_PLACEITEMS), c+""));
        report.append("\n");
        return c;
    }

    /**
     * importActions
     */
    protected static boolean importActions(Context context, StringBuilder report, @Nullable ContentValues... contentValues)
    {
        if (contentValues != null)
        {
            int c = 0;
            for (ContentValues values : contentValues) {
                if (WidgetActions.saveActionLaunchPref(context, values, 0)) {
                    c++;
                }
            }
            report.append(context.getString(R.string.restorebackup_dialog_report_format1, SuntimesBackupTask.displayStringForBackupKey(context, SuntimesBackupTask.KEY_ACTIONS), c+""));
            report.append("\n");
            return true;
        }
        return false;
    }

    /**
     * importWidgetSettings
     */
    protected static int importWidgetSettings(Context context, String prefix, boolean includeMetadata, StringBuilder report, @Nullable ContentValues... contentValues)
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
        for (Class widgetClass : WidgetListAdapter.ALL_WIDGETS) {
            widgetIds.addAll(SuntimesBackupTask.getAllWidgetIds(context, widgetClass));
        }
        widgetIds.add(0);
        widgetIds.add(ClockTileService.CLOCKTILE_APPWIDGET_ID);
        widgetIds.add(NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID);
        widgetIds.add(AlarmTileService.ALARMTILE_APPWIDGET_ID);

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final int IMPORT_WIDGETS_METHOD_RESTOREBACKUP = WidgetSettingsImportTask.IMPORT_WIDGETS_METHOD_RESTOREBACKUP;    // 0
    public static final int IMPORT_WIDGETS_METHOD_MAKEBESTGUESS = WidgetSettingsImportTask.IMPORT_WIDGETS_METHOD_MAKEBESTGUESS;    // 1
    public static final int IMPORT_WIDGETS_METHOD_DIRECTIMPORT = WidgetSettingsImportTask.IMPORT_WIDGETS_METHOD_DIRECTIMPORT;      // 2
    public static final int[] IMPORT_WIDGETS_METHODS = WidgetSettingsImportTask.IMPORT_WIDGETS_METHODS;

    public static final int IMPORT_PLACES_METHOD_CLEAR = 10;      // clear all, then insert
    public static final int IMPORT_PLACES_METHOD_ADDALL = 20;     // insert all (may result in duplicates)
    public static final int IMPORT_PLACES_METHOD_IGNORE = 30;     // insert values (ignore if existing)
    public static final int IMPORT_PLACES_METHOD_OVERWRITE = 40;  // insert values (update if existing)
    public static final int[] IMPORT_PLACES_METHODS = new int[] { IMPORT_PLACES_METHOD_CLEAR, IMPORT_PLACES_METHOD_ADDALL }; // TODO: implement IMPORT_PLACES_METHOD_IGNORE, IMPORT_PLACES_METHOD_OVERWRITE };

    public static final int IMPORT_ALARMS_METHOD_CLEAR = 100;      // clear all, then insert
    public static final int IMPORT_ALARMS_METHOD_ADDALL = 200;     // insert all (may result in duplicates)
    public static final int[] IMPORT_ALARMS_METHODS = new int[] { IMPORT_ALARMS_METHOD_CLEAR, IMPORT_ALARMS_METHOD_ADDALL };

    public static void chooseImportMethod(final Context context, final String key, final int[] methods, @NonNull final DialogInterface.OnClickListener onClickListener)
    {
        final CharSequence[] items = new CharSequence[methods.length];
        for (int i=0; i<items.length; i++) {
            items[i] = displayStringForImportMethod(context, methods[i]);
        }
        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(dialogTitleForImportKey(context, key))
                .setIcon(dialogIconForImportKey(context, key))
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
    protected static CharSequence dialogTitleForImportKey(Context context, String key) {
        return SuntimesBackupTask.displayStringForBackupKey(context, key);
    }
    protected static int dialogIconForImportKey(Context context, String key)
    {
        switch (key)
        {
            case SuntimesBackupTask.KEY_PLACEITEMS: return R.drawable.ic_action_place;
            case SuntimesBackupTask.KEY_APPSETTINGS: return R.drawable.ic_action_settings;
            case SuntimesBackupTask.KEY_WIDGETSETTINGS: return R.drawable.ic_action_widget;
            case SuntimesBackupTask.KEY_ALARMITEMS: return R.drawable.ic_action_alarms;
            case SuntimesBackupTask.KEY_EVENTITEMS: default: return R.drawable.ic_action_copy;
        }
    }
    protected static CharSequence displayStringForImportMethod(Context context, int method)
    {
        switch (method)
        {
            case IMPORT_WIDGETS_METHOD_DIRECTIMPORT: return SuntimesUtils.fromHtml(context.getString(R.string.importwidget_dialog_item_direct));
            case IMPORT_WIDGETS_METHOD_MAKEBESTGUESS: return SuntimesUtils.fromHtml(context.getString(R.string.importwidget_dialog_item_bestguess));
            case IMPORT_WIDGETS_METHOD_RESTOREBACKUP: return SuntimesUtils.fromHtml(context.getString(R.string.importwidget_dialog_item_restorebackup));

            case IMPORT_ALARMS_METHOD_ADDALL: return SuntimesUtils.fromHtml(context.getString(R.string.importalarms_dialog_item_addall));
            case IMPORT_ALARMS_METHOD_CLEAR: return SuntimesUtils.fromHtml(context.getString(R.string.importalarms_dialog_item_clear));

            case IMPORT_PLACES_METHOD_ADDALL: return SuntimesUtils.fromHtml(context.getString(R.string.importplaces_dialog_item_addall));
            case IMPORT_PLACES_METHOD_CLEAR: return SuntimesUtils.fromHtml(context.getString(R.string.importplaces_dialog_item_clear));
            case IMPORT_PLACES_METHOD_IGNORE: return SuntimesUtils.fromHtml(context.getString(R.string.importplaces_dialog_item_ignore));
            case IMPORT_PLACES_METHOD_OVERWRITE: return SuntimesUtils.fromHtml(context.getString(R.string.importplaces_dialog_item_overwrite));

            default: return method + "";
        }
    }

    /**
     * BackupKeyObserver
     */
    public static class BackupKeyObserver
    {
        private final HashMap<String, Boolean> items = new HashMap<>();
        private final Set<String> remainingKeys = new TreeSet<String>();

        @SuppressLint("UseSparseArrays")
        public BackupKeyObserver(String[] keys, ObserverListener listener)
        {
            this.observerListener = listener;
            for (String key : keys) {
                items.put(key, false);
                remainingKeys.add(key);
            }
        }

        public void observeNext()
        {
            for (String key : remainingKeys)
            {
                remainingKeys.remove(key);
                if (observerListener != null) {
                    observerListener.onObservingItem(this, key);
                }
                break;
            }
        }

        public void notify(String key)
        {
            items.put(key, true);
            if (observerListener != null)
            {
                observerListener.onObservedItem(this, key);
                if (observedAll()) {
                    observerListener.onObservedAll(this);
                } else observeNext();
            }
        }

        public boolean observedAll()
        {
            boolean retValue = true;
            for (Boolean value : items.values()) {
                retValue = retValue && value;
            }
            return retValue;
        }

        private final ObserverListener observerListener;
        public static abstract class ObserverListener
        {
            public void onObservingItem(BackupKeyObserver observer, String key) {}
            public void onObservedItem(BackupKeyObserver observer, String key) {}
            public void onObservedAll(BackupKeyObserver observer) {}
        }
    }

}