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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.tiles.AlarmTileService;
import com.forrestguice.suntimeswidget.tiles.ClockTileService;
import com.forrestguice.suntimeswidget.tiles.NextEventTileService;
import com.forrestguice.suntimeswidget.widgets.WidgetListAdapter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WidgetSettingsExportTask extends ExportTask
{
    public static final String FILEEXT = ".txt";
    public static final String MIMETYPE = "text/plain";

    public WidgetSettingsExportTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        initTask();
    }
    public WidgetSettingsExportTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        initTask();
    }
    public WidgetSettingsExportTask(Context context, Uri uri)
    {
        super(context, uri);
        initTask();
    }

    protected void initTask()
    {
        ext = FILEEXT;
        mimeType = MIMETYPE;
    }

    /**
     * writes
     *   [{ ContentValues }, ...]
     */
    @Override
    public boolean export( Context context, BufferedOutputStream out ) throws IOException
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        writeWidgetSettingsJSONArray(context, prefs, appWidgetIds, out);
        return true;
    }

    /**
     * writes
     *   [{ ContentValues }, ...]
     */
    public static void writeWidgetSettingsJSONArray(Context context, SharedPreferences widgetPrefs, List<Integer> appWidgetIds, BufferedOutputStream out) throws IOException
    {
        int n = appWidgetIds.size();
        out.write("[".getBytes());               // writes a json array
        for (int i=0; i<n; i++)
        {
            Integer appWidgetId = appWidgetIds.get(i);
            if (appWidgetId != null)
            {
                String json = WidgetSettingsImportTask.ContentValuesJson.toJson(toContentValues(widgetPrefs, appWidgetId));
                out.write(json.getBytes());
                if (i != n-1) {
                    out.write(", \n".getBytes());
                }
            }
        }
        out.write("]".getBytes());
        out.flush();
    }

    /**
     * @param value export single appWidgetId
     */
    public void setAppWidgetId(int value)
    {
        appWidgetIds.clear();
        appWidgetIds.add(value);
    }
    public void setAppWidgetIds(ArrayList<Integer> values)
    {
        appWidgetIds.clear();
        appWidgetIds.addAll(values);
    }
    protected ArrayList<Integer> appWidgetIds = new ArrayList<>();

    public static ContentValues toContentValues(SharedPreferences prefs) {
        return toContentValues(prefs, null);
    }

    /**
     * @param prefs SharedPreferences
     * @param appWidgetId keys for appWidgetId, or null for all keys
     * @return ContentValues
     */
    public static ContentValues toContentValues(SharedPreferences prefs, @Nullable Integer appWidgetId)
    {
        Map<String, ?> map = prefs.getAll();
        Set<String> keys = map.keySet();

        ContentValues values = new ContentValues();
        for (String key : keys)
        {
            boolean isMatch = (appWidgetId == null || key.startsWith(WidgetSettings.PREF_PREFIX_KEY + appWidgetId));
            if (!isMatch) {
                continue;
            }

            if (map.get(key).getClass().equals(String.class))
            {
                //Log.d("DEBUG", key + " is String");
                values.put(key, prefs.getString(key, null));

            } else if (map.get(key).getClass().equals(Integer.class)) {
                //Log.d("DEBUG", key + " is Integer");
                values.put(key, prefs.getInt(key, -1));

            } else if (map.get(key).getClass().equals(Long.class)) {
                //Log.d("DEBUG", key + " is Long");
                values.put(key, prefs.getLong(key, -1));

            } else if (map.get(key).getClass().equals(Float.class)) {
                //Log.d("DEBUG", key + " is Long");
                values.put(key, prefs.getFloat(key, -1));

            } else if (map.get(key).getClass().equals(Boolean.class)) {
                //Log.d("DEBUG", key + " is boolean");
                values.put(key, prefs.getBoolean(key, false));
            }
        }
        return values;
    }

    public static void addWidgetMetadata(Context context)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        String packageName = context.getPackageName();
        for (Class<?> widgetClass : WidgetListAdapter.ALL_WIDGETS)
        {
            Bundle bundle = new Bundle();
            bundle.putString(WidgetSettingsMetadata.PREF_KEY_META_CLASSNAME, widgetClass.getSimpleName());
            bundle.putInt(WidgetSettingsMetadata.PREF_KEY_META_VERSIONCODE, BuildConfig.VERSION_CODE);

            int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(packageName, widgetClass.getName()));
            for (int id : widgetIds) {
                WidgetSettingsMetadata.saveMetaData(context, id, bundle);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString(WidgetSettingsMetadata.PREF_KEY_META_CLASSNAME, "SuntimesActivity");
        bundle.putInt(WidgetSettingsMetadata.PREF_KEY_META_VERSIONCODE, BuildConfig.VERSION_CODE);
        WidgetSettingsMetadata.saveMetaData(context, 0, bundle);

        bundle.putString(WidgetSettingsMetadata.PREF_KEY_META_CLASSNAME, ClockTileService.class.getSimpleName());
        WidgetSettingsMetadata.saveMetaData(context, ClockTileService.CLOCKTILE_APPWIDGET_ID, bundle);

        bundle.putString(WidgetSettingsMetadata.PREF_KEY_META_CLASSNAME, NextEventTileService.class.getSimpleName());
        WidgetSettingsMetadata.saveMetaData(context, NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID, bundle);

        bundle.putString(WidgetSettingsMetadata.PREF_KEY_META_CLASSNAME, AlarmTileService.class.getSimpleName());
        WidgetSettingsMetadata.saveMetaData(context, AlarmTileService.ALARMTILE_APPWIDGET_ID, bundle);
    }

}
