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
package com.forrestguice.suntimeswidget.widgets;

import android.content.Context;
import android.content.SharedPreferences;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SolsticeWidgetSettings
{
    public static final String PREF_PREFIX_KEY_SOLSTICEWIDGET = "_solsticewidget_";

    public static final String PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER = "showcrossquarter";
    public static final boolean PREF_DEF_SOLSTICEWIDGET_SHOWCROSSQUARTER = AppSettings.PREF_DEF_UI_SHOWCROSSQUARTER;

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static String[] ALL_KEYS = new String[] {
            PREF_PREFIX_KEY_SOLSTICEWIDGET + PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER,
    };
    public static String[] BOOL_KEYS = new String[] {
            PREF_PREFIX_KEY_SOLSTICEWIDGET + PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER,
    };

    private static Map<String,Class<?>> types = null;
    public static Map<String,Class<?>> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            WidgetSettings.putType(types, Boolean.class, BOOL_KEYS);

            for (String key : ALL_KEYS) {
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static void saveWidgetValue(Context context, int appWidgetId, String key, int value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        prefs.putInt(prefs_prefix + key, value);
        prefs.apply();
    }
    public static void saveWidgetValue(Context context, int appWidgetId, String key, boolean value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        prefs.putBoolean(prefs_prefix + key, value);
        prefs.apply();
    }
    public static void saveWidgetValue(Context context, int appWidgetId, String key, String value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        prefs.putString(prefs_prefix + key, value);
        prefs.apply();
    }
    public static void saveWidgetValue(Context context, int appWidgetId, String key, Set<String> value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        prefs.putStringSet(prefs_prefix + key, value);
        prefs.apply();
    }

    public static int loadWidgetInt(Context context, int appWidgetId, String key, int defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        return prefs.getInt(prefs_prefix + key, defaultValue);
    }
    public static boolean loadWidgetBool(Context context, int appWidgetId, String key, boolean defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        return prefs.getBoolean(prefs_prefix + key, defaultValue);
    }
    public static String loadWidgetString(Context context, int appWidgetId, String key, String defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        return prefs.getString(prefs_prefix + key, defaultValue);
    }
    public static Set<String> loadWidgetStringSet(Context context, int appWidgetId, String key, String[] defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        Set<String> defValue = new TreeSet<String>(Arrays.asList(defaultValue));
        return prefs.getStringSet(prefs_prefix + key, defValue);
    }

    public static void deleteWidgetValue(Context context, int appWidgetId, String key) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_SOLSTICEWIDGET;
        prefs.remove(prefs_prefix + key);
        prefs.apply();
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * @param context context
     */
    public static void initDisplayStrings( Context context ) {
        /* EMPTY */
    }

    /**
     * @param context context
     * @param appWidgetId appWidgetId
     */
    public static void deletePrefs(Context context, int appWidgetId) {
        deleteWidgetValue(context, appWidgetId, PREF_KEY_SOLSTICEWIDGET_SHOWCROSSQUARTER);
    }

}
