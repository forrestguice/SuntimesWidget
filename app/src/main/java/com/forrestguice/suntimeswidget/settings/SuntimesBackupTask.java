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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesWidgetListActivity;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemExportTask;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.events.EventExportTask;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.getfix.GetFixDatabaseAdapter;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.tiles.AlarmTileService;
import com.forrestguice.suntimeswidget.tiles.ClockTileService;
import com.forrestguice.suntimeswidget.tiles.NextEventTileService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Backup all Suntimes settings to json backup file.
 * Backup contents may include: AppSettings, WidgetSettings, AlarmClockItems
 */
public class SuntimesBackupTask extends WidgetSettingsExportTask
{
    public static final String KEY_CLASS = "Class";
    public static final String KEY_BACKUPFILE = "SuntimesBackup";     // file type; "Class":"SuntimesBackup"
    public static final String KEY_VERSION = "Version";               // type version; "version":"107"

    public static final String KEY_APPSETTINGS = "AppSettings";
    public static final String KEY_WIDGETSETTINGS = "WidgetSettings";
    public static final String KEY_WIDGETTHEMES = "WidgetThemes";
    public static final String KEY_ALARMITEMS = "AlarmItems";
    public static final String KEY_EVENTITEMS = "EventItems";
    public static final String KEY_PLACEITEMS = "PlaceItems";
    public static final String KEY_ACTIONS = "Actions";

    public static final String KEY_COLORS = "Colors";
    public static final String KEY_COLORS_APPCOLORS = KEY_COLORS + "_" + "AppColors";
    public static final String KEY_COLORS_MAPCOLORS = KEY_COLORS + "_" + "MapColors";

    public static final String[] ALL_KEYS = new String[] {
            KEY_APPSETTINGS, KEY_COLORS, KEY_WIDGETSETTINGS, KEY_ALARMITEMS, KEY_EVENTITEMS, KEY_PLACEITEMS, KEY_ACTIONS, KEY_WIDGETTHEMES
    };

    public static final String DEF_EXPORT_TARGET = "SuntimesBackup";

    public SuntimesBackupTask(Context context, String exportTarget) {
        super(context, exportTarget);
    }
    public SuntimesBackupTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache) {
        super(context, exportTarget, useExternalStorage, saveToCache);
    }
    public SuntimesBackupTask(Context context, Uri uri) {
        super(context, uri);
    }

    /**
     * @param key KEY_APPSETTINGS, KEY_WIDGETSETTINGS, KEY_ALARMITEMS
     * @param value true, false
     */
    public void includeInBackup(String key, boolean value) {
        includedKeys.put(key, value);
    }
    public void includeInBackup(String... keys) {
        for (String key : keys) {
            includeInBackup(key, true);
        }
    }
    public void includeInBackup(String[] keys, boolean[] include) {
        for (int i=0; i<keys.length; i++) {
            includeInBackup(keys[i], (i<include.length && include[i]));
        }
    }
    public void includeAll() {
        includeInBackup(ALL_KEYS);
    }
    protected Map<String,Boolean> includedKeys = new HashMap<>();

    @Override
    public boolean export( Context context, BufferedOutputStream out ) throws IOException
    {
        writeBackupJSONObject(context, out);
        return true;
    }

    /**
     * writes
     *   {
     *     "Type": "SuntimesBackup"
     *     "Version": "107"
     *     "AppSettings": { ContentValues }
     *     "WidgetSettings": [{ ContentValues }, ...]
     *     "AlarmItems": [{ AlarmClockItem }, ...]
     *   }
     */
    protected void writeBackupJSONObject( Context context, BufferedOutputStream out ) throws IOException
    {
        out.write("{".getBytes());
        out.write(("\"" + KEY_CLASS + "\": \"" + KEY_BACKUPFILE + "\",\n").getBytes());                // declare type (expected to be the first item)
        out.write(("\"" + KEY_VERSION + "\": " + BuildConfig.VERSION_CODE).getBytes());                // declare version (expected to be the second item)
        int c = 2;    // keys written

        if (includedKeys.containsKey(KEY_APPSETTINGS) && includedKeys.get(KEY_APPSETTINGS))
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_APPSETTINGS + "\": ").getBytes());    // include AppSettings
            SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            writeAppSettingsJSONObject(context, appPrefs, out);
            c++;
        }

        if (includedKeys.containsKey(KEY_WIDGETSETTINGS) && includedKeys.get(KEY_WIDGETSETTINGS) && appWidgetIds.size() > 0)
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_WIDGETSETTINGS + "\": ").getBytes());    // include WidgetSettings
            SharedPreferences widgetPrefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
            writeWidgetSettingsJSONArray(context, widgetPrefs, getAllWidgetIds(context), out);
            c++;
        }

        if (includedKeys.containsKey(KEY_ALARMITEMS) && includedKeys.get(KEY_ALARMITEMS))
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_ALARMITEMS + "\": ").getBytes());    // include AlarmItems
            AlarmDatabaseAdapter alarmDb = new AlarmDatabaseAdapter(context);
            writeAlarmItemsJSONArray(context, alarmDb, out);
            c++;
        }

        if (includedKeys.containsKey(KEY_EVENTITEMS) && includedKeys.get(KEY_EVENTITEMS))
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_EVENTITEMS + "\": ").getBytes());    // include EventItems
            List<EventSettings.EventAlias> events = EventSettings.loadEvents(context, AlarmEventProvider.EventType.SUN_ELEVATION);
            EventExportTask.writeEventItemsJSONArray(context, events.toArray(new EventSettings.EventAlias[0]), out);
            c++;
        }

        if (includedKeys.containsKey(KEY_PLACEITEMS) && includedKeys.get(KEY_PLACEITEMS))
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_PLACEITEMS + "\": ").getBytes());    // include PlacesItems
            GetFixDatabaseAdapter placesDb = new GetFixDatabaseAdapter(context);
            writePlaceItemsJSONArray(context, placesDb, out);
            c++;
        }

        if (includedKeys.containsKey(KEY_ACTIONS) && includedKeys.get(KEY_ACTIONS))
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_ACTIONS + "\": ").getBytes());    // include ActionItems
            String[] actions = WidgetActions.loadActionLaunchList(context, 0).toArray(new String[0]);
            writeActionsJSONArray(context, actions, out);
            c++;
        }

        if (includedKeys.containsKey(KEY_WIDGETTHEMES) && includedKeys.get(KEY_WIDGETTHEMES))
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            out.write(("\"" + KEY_WIDGETTHEMES + "\": ").getBytes());    // include Widget Themes
            SharedPreferences themePrefs = context.getSharedPreferences(WidgetThemes.PREFS_THEMES, Context.MODE_PRIVATE);
            writeWidgetThemesJSONArray(context, themePrefs, out);
            c++;
        }

        out.write("}".getBytes());
        out.flush();
    }

    /**
     * writes
     *   { ContentValues }
     */
    public static void writeAppSettingsJSONObject(Context context, SharedPreferences appPrefs, BufferedOutputStream out) throws IOException
    {
        String json = WidgetSettingsImportTask.ContentValuesJson.toJson(toContentValues(appPrefs));
        out.write(json.getBytes());
        out.flush();
    }

    /**
     * writes
     *   { ContentValues }
     */
    public static void writeActionsJSONArray(Context context, String[] actions, BufferedOutputStream out) throws IOException
    {
        out.write("[".getBytes());

        for (int i=0; i<actions.length; i++)
        {
            ContentValues values = WidgetActions.loadActionLaunchPref(context, 0, actions[i]);
            String json = WidgetSettingsImportTask.ContentValuesJson.toJson(values);
            out.write(json.getBytes());

            if (i != actions.length-1) {
                out.write(", ".getBytes());
            }
        }
        out.write("]".getBytes());
        out.flush();
    }

    /**
     * writes
     *   [{ PlaceItem }, ...]
     */
    public static void writePlaceItemsJSONArray(Context context, GetFixDatabaseAdapter db, BufferedOutputStream out) throws IOException
    {
        ArrayList<ContentValues> values = new ArrayList<>();
        db.open();
        Cursor cursor = db.getAllPlaces(0, true);
        while (!cursor.isAfterLast())
        {
            ContentValues placeValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, placeValues);
            values.add(placeValues);
            cursor.moveToNext();
        }
        db.close();
        writeContentValuesJSONArray(context, values.toArray(new ContentValues[0]), out);
    }

    /**
     * writes
     *   [{ ContentValues }, ...]
     */
    public static void writeContentValuesJSONArray(Context context, ContentValues[] items, BufferedOutputStream out) throws IOException
    {
        out.write("[".getBytes());
        for (int i=0; i<items.length; i++)
        {
            String jsonString = WidgetSettingsImportTask.ContentValuesJson.toJson(items[i]);
            out.write(jsonString.getBytes());
            if (i != items.length-1) {
                out.write(", ".getBytes());
            }
        }
        out.write("]".getBytes());
        out.flush();
    }

    /**
     * writes
     *   [{ AlarmClockItem }, ...]
     */
    public static void writeAlarmItemsJSONArray(Context context, AlarmDatabaseAdapter db, BufferedOutputStream out) throws IOException
    {
        ArrayList<AlarmClockItem> items = new ArrayList<>();
        db.open();
        Cursor cursor = db.getAllAlarms(0, true);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            ContentValues entryValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, entryValues);
            items.add(new AlarmClockItem(context, entryValues));
            cursor.moveToNext();
        }
        db.close();
        AlarmClockItemExportTask.writeAlarmItemsJSONArray(context, items.toArray(new AlarmClockItem[0]), out);
    }

    public static void writeWidgetThemesJSONArray(Context context, SharedPreferences prefs, BufferedOutputStream out) throws IOException
    {
        out.write("[".getBytes());
        int c = 0;
        Set<String> themes = WidgetThemes.loadInstalledList(prefs);
        for (String themeName : themes)
        {
            if (c > 0) {
                out.write(",\n".getBytes());
            }
            SuntimesTheme theme = WidgetThemes.loadTheme(context, themeName);
            String jsonString = WidgetSettingsImportTask.ContentValuesJson.toJson(theme.toContentValues());
            out.write(jsonString.getBytes());
            c++;
        }
        out.write("]".getBytes());
        out.flush();
    }

    /**
     * exportSettings to uri
     * Displays an AlertDialog (chooser), then creates and starts a SuntimesBackupTask.
     */
    public static void exportSettings(final Context context, @Nullable final Uri uri, final ExportTask.TaskListener exportListener)
    {
        Log.i("ExportSettings", "Starting export task: " + uri);
        SuntimesBackupTask.chooseBackupContent(context, SuntimesBackupTask.ALL_KEYS, false, new SuntimesBackupTask.ChooseBackupDialogListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which, String[] keys, boolean[] checked)
            {
                WidgetSettingsExportTask.addWidgetMetadata(context);
                SuntimesBackupTask task = (uri != null ? new SuntimesBackupTask(context, uri)
                        : new SuntimesBackupTask(context, SuntimesBackupTask.DEF_EXPORT_TARGET, true, true));  // export to external cache;
                task.setTaskListener(exportListener);
                task.includeInBackup(keys, checked);
                task.setAppWidgetIds(getAllWidgetIds(context));
                task.execute();
            }
        });
    }

    protected static ArrayList<Integer> getAllWidgetIds(Context context)
    {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Class widgetClass : SuntimesWidgetListActivity.WidgetListAdapter.ALL_WIDGETS) {
            ids.addAll(getAllWidgetIds(context, widgetClass));
        }
        ids.add(0);                                                    // include app config and quick settings tiles
        ids.add(ClockTileService.CLOCKTILE_APPWIDGET_ID);
        ids.add(NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID);
        ids.add(AlarmTileService.ALARMTILE_APPWIDGET_ID);
        return ids;
    }
    protected static ArrayList<Integer> getAllWidgetIds(Context context, Class widgetClass)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        String packageName = context.getPackageName();
        ArrayList<Integer> ids = new ArrayList<>();
        int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(packageName, widgetClass.getName()));
        for (int id : widgetIds) {
            ids.add(id);
        }
        return ids;
    }

    /**
     * displayStringForBackupKey
     * @param context Context
     * @param key backupKey, e.g. KEY_APPSETTINGS
     * @return display string (or the key itself if unrecognized)
     */
    public static CharSequence displayStringForBackupKey(Context context, String key)
    {
        if (SuntimesBackupTask.KEY_APPSETTINGS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_appsettings));
        }
        if (SuntimesBackupTask.KEY_COLORS.equals(key) || SuntimesBackupTask.KEY_COLORS_APPCOLORS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_colors_appcolors));
        }
        if (SuntimesBackupTask.KEY_COLORS_MAPCOLORS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_colors_mapcolors));
        }
        if (SuntimesBackupTask.KEY_WIDGETSETTINGS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_widgetsettings));
        }
        if (SuntimesBackupTask.KEY_WIDGETTHEMES.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_widgetthemes));
        }
        if (SuntimesBackupTask.KEY_ALARMITEMS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_alarmitems));
        }
        if (SuntimesBackupTask.KEY_EVENTITEMS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_eventitems));
        }
        if (SuntimesBackupTask.KEY_PLACEITEMS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_placeitems));
        }
        if (SuntimesBackupTask.KEY_ACTIONS.equals(key)) {
            return SuntimesUtils.fromHtml(context.getString(R.string.restorebackup_dialog_item_actions));
        }
        return key;
    }

    /**
     * ChooseBackupDialogListener
     */
    public interface ChooseBackupDialogListener {
        void onClick(DialogInterface dialog, int which, String[] keys, boolean[] checked);
    }

    /**
     * chooseBackupContent
     * @param context Context
     * @param keys key to choose from
     * @param isImport true importing content, false exporting content
     * @param onClickListener dialog listener
     */
    public static void chooseBackupContent(final Context context, Set<String> keys, boolean isImport, @NonNull final ChooseBackupDialogListener onClickListener) {
        chooseBackupContent(context, keys.toArray(new String[0]), isImport, onClickListener);
    }
    public static void chooseBackupContent(final Context context, final String[] keys, boolean isImport, @NonNull final ChooseBackupDialogListener onClickListener)
    {
        final ArrayList<Pair<Integer,CharSequence>> items = new ArrayList<>();
        final boolean[] checked = new boolean[keys.length];
        for (int i=0; i<keys.length; i++) {
            checked[i] = true;
            items.add(new Pair<Integer, CharSequence>(i, SuntimesBackupTask.displayStringForBackupKey(context, keys[i])));
        }

        Collections.sort(items, new Comparator<Pair<Integer,CharSequence>>()
        {
            @Override
            public int compare(Pair<Integer,CharSequence> o1, Pair<Integer,CharSequence> o2)
            {
                if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else return o1.second.toString().compareTo(o2.second.toString());
            }
        });

        CharSequence[] displayStrings = new CharSequence[items.size()];
        for (int i=0; i<displayStrings.length; i++) {
            displayStrings[i] = items.get(i).second;
        }

        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(isImport ? R.string.configAction_restoreBackup : R.string.configAction_createBackup))
                .setIcon(isImport ? R.drawable.ic_action_copy : R.drawable.ic_action_save)
                .setMultiChoiceItems(displayStrings, Arrays.copyOf(checked, checked.length), new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        int i = items.get(which).first;
                        checked[i] = isChecked;
                        //Log.d("DEBUG", "setChecked: " + i+":"+checked[i]);
                    }
                })
                .setPositiveButton(context.getString(isImport ? R.string.configAction_import : R.string.configAction_export), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onClickListener.onClick(dialog, AlertDialog.BUTTON_POSITIVE, keys, checked);
                        /*for (int i=0; i<checked.length; i++) {
                            Log.d("DEBUG", "checked: " + i+":"+checked[i]);
                        }*/
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), null);
        confirm.show();
    }

    /**
     * showIOResultSnackbar
     */
    public static void showIOResultSnackbar(final Context context, final View view, @Nullable Uri shareUri, boolean result, final CharSequence message, @Nullable final CharSequence report)
    {
        if (context != null && view != null)
        {
            Snackbar snackbar = Snackbar.make(view, message, (result ? 7000 : Snackbar.LENGTH_LONG));

            if (report != null) {
                snackbar.setAction(context.getString(R.string.configAction_info), onClickShowReport(context, message, report));

            } else if (result && shareUri != null) {
                snackbar.setAction(context.getString(R.string.configAction_share), onClickShareUri(context, shareUri));
            }

            SuntimesUtils.themeSnackbar(context, snackbar, null);
            snackbar.show();
        }
    }

    private static View.OnClickListener onClickShareUri(final Context context, final Uri uri)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.configAction_share)));
            }
        };
    }

    private static View.OnClickListener onClickShowReport(final Context context, final CharSequence message, final CharSequence report)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context).setTitle(message)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setMessage(report);
                dialog.show();
            }
        };
    }

}
