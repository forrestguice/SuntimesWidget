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
import android.os.Bundle;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.util.prefs.PrefTypeInfo;

import java.util.Map;
import java.util.TreeMap;

public class WidgetSettingsMetadata
{
    public static final String BACKUP_PREFIX_KEY = "bckwidget_";
    public static final String PREF_PREFIX_KEY_META = "_meta_";

    public static final String PREF_KEY_META_CLASSNAME = "appWidgetClassName";
    public static final String PREF_KEY_META_VERSIONCODE = "appWidgetVersionCode";

    public static final String PREF_KEY_META_CATEGORY = "appWidgetCategory";
    // AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN; 1; widget can be displayed on the home screen
    // AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD; 2; widget can be displayed on the keyguard
    // AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX; 4; widget can be displayed within a space reserved for the search box


    public static final String PREF_KEY_META_WIDTH_MIN = "appWidgetMinWidth";
    public static final String PREF_KEY_META_WIDTH_MAX = "appWidgetMaxWidth";

    public static final String PREF_KEY_META_HEIGHT_MIN = "appWidgetMinHeight";
    public static final String PREF_KEY_META_HEIGHT_MAX = "appWidgetMaxHeight";

    public static String[] ALL_KEYS = new String[]
    {
            PREF_PREFIX_KEY_META + PREF_KEY_META_CLASSNAME,
            PREF_PREFIX_KEY_META + PREF_KEY_META_VERSIONCODE,
            PREF_PREFIX_KEY_META + PREF_KEY_META_CATEGORY,
            PREF_PREFIX_KEY_META + PREF_KEY_META_WIDTH_MIN,
            PREF_PREFIX_KEY_META + PREF_KEY_META_WIDTH_MIN,
            PREF_PREFIX_KEY_META + PREF_KEY_META_WIDTH_MAX,
            PREF_PREFIX_KEY_META + PREF_KEY_META_HEIGHT_MIN,
            PREF_PREFIX_KEY_META + PREF_KEY_META_HEIGHT_MAX,
    };
    //public static String[] BOOL_KEYS = new String[] { };
    //public static String[] FLOAT_KEYS = new String[] { };
    //public static String[] LONG_KEYS = new String[] { };
    public static String[] INT_KEYS = new String[]
    {
            PREF_PREFIX_KEY_META + PREF_KEY_META_VERSIONCODE,
            PREF_PREFIX_KEY_META + PREF_KEY_META_CATEGORY,
            PREF_PREFIX_KEY_META + PREF_KEY_META_WIDTH_MIN,
            PREF_PREFIX_KEY_META + PREF_KEY_META_WIDTH_MIN,
            PREF_PREFIX_KEY_META + PREF_KEY_META_WIDTH_MAX,
            PREF_PREFIX_KEY_META + PREF_KEY_META_HEIGHT_MIN,
            PREF_PREFIX_KEY_META + PREF_KEY_META_HEIGHT_MAX,
    };

    public static PrefTypeInfo getPrefTypeInfo()
    {
        return new PrefTypeInfo() {
            public String[] allKeys() {
                return ALL_KEYS;
            }
            public String[] intKeys() {
                return INT_KEYS;
            }
            public String[] longKeys() {
                return new String[0];
            }
            public String[] floatKeys() {
                return new String[0];
            }
            public String[] boolKeys() {
                return new String[0];
            }
        };
    }

    private static Map<String,Class<?>> types = null;
    public static Map<String,Class<?>> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            putType(types, Integer.class, INT_KEYS);
            //putType(types, Long.class, LONG_KEYS);
            //putType(types, Float.class, FLOAT_KEYS);
            //putType(types, Boolean.class, BOOL_KEYS);

            for (String key : ALL_KEYS) {                // all others are type String
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }
    private static void putType(Map<String,Class<?>> map, Class<?> type, String... keys) {
        for (String key : keys) {
            map.put(key, type);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * WidgetMetaData
     */
    public static class WidgetMetadata
    {
        private String className = null;
        private int versionCode = -1;
        private int category = -1;
        private final int[] minDimens = new int[] {-1, -1};
        private final int[] maxDimens = new int[] {-1, -1};

        public WidgetMetadata(WidgetMetadata other)
        {
            this.className = other.className;
            this.versionCode = other.versionCode;
            this.category = other.category;
            this.minDimens[0] = other.minDimens[0];
            this.minDimens[1] = other.minDimens[1];
            this.maxDimens[0] = other.maxDimens[0];
            this.maxDimens[1] = other.maxDimens[1];
        }

        public WidgetMetadata(String widgetClassName)
        {
            this.className = widgetClassName;
            this.versionCode = BuildConfig.VERSION_CODE;
        }

        public WidgetMetadata(String widgetClassName, int versionCode, int category,
                              int minWidth, int minHeight, int maxWidth, int maxHeight)
        {
            this.className = widgetClassName;
            this.versionCode = versionCode;
            this.category = category;
            this.minDimens[0] = minWidth;
            this.minDimens[1] = minHeight;
            this.maxDimens[0] = maxWidth;
            this.maxDimens[1] = maxHeight;
        }

        public WidgetMetadata(String widgetClassName, int versionCode, WidgetMetadata other)
        {
            this.className = widgetClassName;
            this.versionCode = versionCode;
            this.category = other.category;
            this.minDimens[0] = other.minDimens[0];
            this.minDimens[1] = other.minDimens[1];
            this.maxDimens[0] = other.maxDimens[0];
            this.maxDimens[1] = other.maxDimens[1];
        }

        /**
         * @return widget class name
         */
        public String getWidgetClassName() {
            return className;
        }

        /**
         * @return appVersionCode
         */
        public int getVersionCode() {
            return versionCode;
        }

        /**
         * @return category
         */
        public int getCategory() {
            return category;
        }

        /**
         * @return [width, height]
         */
        public int[] getMinDimensions() {
            return minDimens;
        }
        /**
         * @return [width, height]
         */
        public int[] getMaxDimensions() {
            return maxDimens;
        }

        public static WidgetMetadata getMetaDataFromValues(@NonNull ContentValues values)
        {
            Long values_id = WidgetSettingsImportTask.findAppWidgetIdFromFirstKey(values);
            return WidgetMetadata.getMetaDataFromValues(values, values_id);
        }

        public static WidgetMetadata getMetaDataFromValues(@NonNull ContentValues values, @Nullable Long appWidgetId)
        {
            if (appWidgetId != null)
            {
                String key_className = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_CLASSNAME;
                String key_versionCode = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_VERSIONCODE;
                String key_category = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_CATEGORY;
                String key_width_min = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_WIDTH_MIN;
                String key_width_max = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_WIDTH_MAX;
                String key_height_min = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_HEIGHT_MIN;
                String key_height_max = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettingsMetadata.PREF_PREFIX_KEY_META + WidgetSettingsMetadata.PREF_KEY_META_HEIGHT_MAX;

                String widgetClassName = (values.containsKey(key_className) ? values.getAsString(key_className) : null);
                int versionCode = (values.containsKey(key_versionCode) ? values.getAsInteger( key_versionCode) : -1);
                int category = (values.containsKey(key_category) ? values.getAsInteger(key_category) : -1);
                int minWidth = (values.containsKey(key_width_min) ? values.getAsInteger(key_width_min) : -1);
                int minHeight = (values.containsKey(key_height_min) ? values.getAsInteger(key_height_min) : -1);
                int maxWidth = (values.containsKey(key_width_max) ? values.getAsInteger(key_width_max) : -1);
                int maxHeight = (values.containsKey(key_height_max) ? values.getAsInteger(key_height_max) : -1);

                return new WidgetMetadata(widgetClassName, versionCode, category,
                        minWidth, minHeight, maxWidth, maxHeight);

            } else return null;
        }

        @Override
        public int hashCode() {
            return className.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) {
                return true;
            } else if (o == null) {
                return false;
            } else if (getClass() != o.getClass()) {
                return false;
            }

            WidgetMetadata other = (WidgetMetadata) o;
            String widgetClassName1 = other.className;
            if (widgetClassName1 == null) {
                widgetClassName1 = "";
            }
            String widgetClassName = className;
            if (widgetClassName == null) {
                widgetClassName = "";
            }
            return widgetClassName.equals(widgetClassName1);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveMetaData(Context context, int appWidgetId, WidgetMetadata metadata)
    {
        int[] minSize = metadata.getMinDimensions();
        int[] maxSize = metadata.getMaxDimensions();

        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_META;
        prefs.putString(prefs_prefix + PREF_KEY_META_CLASSNAME, metadata.getWidgetClassName());
        prefs.putInt(prefs_prefix + PREF_KEY_META_VERSIONCODE, metadata.getVersionCode());
        prefs.putInt(prefs_prefix + PREF_KEY_META_WIDTH_MIN, minSize[0]);
        prefs.putInt(prefs_prefix + PREF_KEY_META_HEIGHT_MIN, minSize[1]);
        prefs.putInt(prefs_prefix + PREF_KEY_META_WIDTH_MAX, maxSize[0]);
        prefs.putInt(prefs_prefix + PREF_KEY_META_HEIGHT_MAX, maxSize[1]);
        prefs.putInt(prefs_prefix + PREF_KEY_META_CATEGORY, metadata.getCategory());
        prefs.apply();
    }

    public static void saveMetaData(Context context, int appWidgetId, Bundle bundle)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_META;

        if (bundle.containsKey(PREF_KEY_META_CLASSNAME)) {
            prefs.putString(prefs_prefix + PREF_KEY_META_CLASSNAME, bundle.getString(PREF_KEY_META_CLASSNAME));
        }
        if (bundle.containsKey(PREF_KEY_META_VERSIONCODE)) {
            prefs.putInt(prefs_prefix + PREF_KEY_META_VERSIONCODE, bundle.getInt(PREF_KEY_META_VERSIONCODE));
        }

        if (bundle.containsKey(PREF_KEY_META_CATEGORY)) {
            prefs.putInt(prefs_prefix + PREF_KEY_META_CATEGORY, bundle.getInt(PREF_KEY_META_CATEGORY));
        }
        if (bundle.containsKey(PREF_KEY_META_WIDTH_MIN)) {
            prefs.putInt(prefs_prefix + PREF_KEY_META_WIDTH_MIN, bundle.getInt(PREF_KEY_META_WIDTH_MIN));
        }
        if (bundle.containsKey(PREF_KEY_META_HEIGHT_MIN)) {
            prefs.putInt(prefs_prefix + PREF_KEY_META_HEIGHT_MIN, bundle.getInt(PREF_KEY_META_HEIGHT_MIN));
        }
        if (bundle.containsKey(PREF_KEY_META_WIDTH_MAX)) {
            prefs.putInt(prefs_prefix + PREF_KEY_META_WIDTH_MAX, bundle.getInt(PREF_KEY_META_WIDTH_MAX));
        }
        if (bundle.containsKey(PREF_KEY_META_HEIGHT_MAX)) {
            prefs.putInt(prefs_prefix + PREF_KEY_META_HEIGHT_MAX, bundle.getInt(PREF_KEY_META_HEIGHT_MAX));
        }

        prefs.apply();
    }

    public static WidgetMetadata loadMetaData(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_META;
        String className = prefs.getString(prefs_prefix + PREF_KEY_META_CLASSNAME, null);
        int versionCode = prefs.getInt(prefs_prefix + PREF_KEY_META_VERSIONCODE, -1);
        int category = prefs.getInt(prefs_prefix + PREF_KEY_META_CATEGORY, -1);
        int minWidth = prefs.getInt(prefs_prefix + PREF_KEY_META_WIDTH_MIN, -1);
        int minHeight = prefs.getInt(prefs_prefix + PREF_KEY_META_HEIGHT_MIN, -1);
        int maxWidth = prefs.getInt(prefs_prefix + PREF_KEY_META_WIDTH_MAX, -1);
        int maxHeight = prefs.getInt(prefs_prefix + PREF_KEY_META_HEIGHT_MAX, -1);
        return new WidgetMetadata(className, versionCode, category,
                minWidth, minHeight, maxWidth, maxHeight);
    }

    public static void deleteMetaData(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_META;
        prefs.remove(prefs_prefix + PREF_KEY_META_CLASSNAME);
        prefs.remove(prefs_prefix + PREF_KEY_META_VERSIONCODE);
        prefs.remove(prefs_prefix + PREF_KEY_META_CATEGORY);
        prefs.remove(prefs_prefix + PREF_KEY_META_WIDTH_MIN);
        prefs.remove(prefs_prefix + PREF_KEY_META_WIDTH_MAX);
        prefs.remove(prefs_prefix + PREF_KEY_META_HEIGHT_MIN);
        prefs.remove(prefs_prefix + PREF_KEY_META_HEIGHT_MAX);
        prefs.apply();
    }

}
