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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;
import java.util.TreeMap;

public class WidgetSettingsMetadata
{
    public static final String PREF_PREFIX_KEY_META = "_meta_";

    public static final String PREF_KEY_META_CLASSNAME = "className";
    public static final String PREF_KEY_META_VERSIONCODE = "versionCode";

    public static String[] ALL_KEYS = new String[]
    {
            PREF_PREFIX_KEY_META + PREF_KEY_META_CLASSNAME,
            PREF_PREFIX_KEY_META + PREF_KEY_META_VERSIONCODE,
    };
    public static String[] BOOL_KEYS = new String[] { };
    public static String[] FLOAT_KEYS = new String[] { };
    public static String[] LONG_KEYS = new String[] { };
    public static String[] INT_KEYS = new String[] { };

    private static Map<String,Class> types = null;
    public static Map<String,Class> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            putType(types, Long.class, LONG_KEYS);
            putType(types, Float.class, FLOAT_KEYS);
            putType(types, Integer.class, INT_KEYS);
            putType(types, Boolean.class, BOOL_KEYS);

            for (String key : ALL_KEYS) {                // all others are type String
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }
    private static void putType(Map<String,Class> map, Class type, String... keys) {
        for (String key : keys) {
            map.put(key, type);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * WidgetMetaData
     */
    public static class WidgetMetaData
    {
        private String className = null;
        private int versionCode = -1;

        public WidgetMetaData(String widgetClassName, int versionCode) {
            this.className = widgetClassName;
        }

        public String getWidgetClassName() {
            return className;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public static WidgetMetaData getMetaDataFromValues(@NonNull ContentValues values)
        {
            Long values_id = WidgetSettingsImportTask.findAppWidgetIdFromFirstKey(values);
            return WidgetSettingsMetadata.WidgetMetaData.getMetaDataFromValues(values, values_id);
        }

        public static WidgetMetaData getMetaDataFromValues(@NonNull ContentValues values, @Nullable Long appWidgetId)
        {
            if (appWidgetId != null)
            {
                String key_className = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_CLASSNAME;
                String key_versionCode = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_VERSIONCODE;
                String widgetClassName = (values.containsKey(key_className) ? values.getAsString(key_className) : null);
                int versionCode = (values.containsKey(key_versionCode) ? values.getAsInteger( key_versionCode) : -1);
                return new WidgetMetaData(widgetClassName, versionCode);
            } else return null;
        }
    }

    public static void saveMetaData(Context context, int appWidgetId, WidgetMetaData metadata)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_META;
        prefs.putString(prefs_prefix + PREF_KEY_META_CLASSNAME, metadata.getWidgetClassName());
        prefs.putInt(prefs_prefix + PREF_KEY_META_VERSIONCODE, metadata.getVersionCode());
        prefs.apply();
    }

    public static WidgetMetaData loadMetaData(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_META;
        String className = prefs.getString(prefs_prefix + PREF_KEY_META_CLASSNAME, null);
        int versionCode = prefs.getInt(prefs_prefix + PREF_KEY_META_VERSIONCODE, -1);
        return new WidgetMetaData(className, versionCode);
    }

    public static void deleteMetaData(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_META;
        prefs.remove(prefs_prefix + PREF_KEY_META_CLASSNAME);
        prefs.remove(prefs_prefix + PREF_KEY_META_VERSIONCODE);
        prefs.apply();
    }

}
