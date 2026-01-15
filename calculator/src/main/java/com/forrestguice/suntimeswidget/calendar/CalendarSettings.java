/**
    Copyright (C) 2022-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.calendar;

import com.forrestguice.util.prefs.PrefTypeInfo;
import com.forrestguice.util.ContextInterface;
import com.forrestguice.util.prefs.SharedPreferences;

import java.util.Map;
import java.util.TreeMap;

import static com.forrestguice.suntimeswidget.calendar.CalendarSettingsInterface.*;

public class CalendarSettings
{
    public static final String PREFS_NAME = "com.forrestguice.suntimeswidget";
    public static final String PREF_PREFIX_KEY = "appwidget_";

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS;
    static
    {
        CalendarMode[] modes = CalendarMode.values();
        ALL_KEYS = new String[modes.length + 3];
        for (int i=0; i<modes.length; i++) {
            ALL_KEYS[i] = PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_FORMATPATTERN + "_" + modes[i].name();
        }
        int i = modes.length;
        ALL_KEYS[i] = PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_FORMATPATTERN;
        ALL_KEYS[i+1] = PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_SHOWDATE;
        ALL_KEYS[i+2] = PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_MODE;
    }

    public static final String[] BOOL_KEYS = {
            PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_SHOWDATE
    };

    public static PrefTypeInfo getPrefTypeInfo()
    {
        return new PrefTypeInfo() {
            public String[] allKeys() {
                return ALL_KEYS;
            }
            public String[] intKeys() {
                return new String[0];
            }
            public String[] longKeys() {
                return new String[0];
            }
            public String[] floatKeys() {
                return new String[0];
            }
            public String[] boolKeys() {
                return BOOL_KEYS;
            }
        };
    }

    private static Map<String,Class> types = null;
    public static Map<String,Class> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            for (String key : BOOL_KEYS) {
                types.put(key, Boolean.class);
            }
            for (String key : ALL_KEYS) {                // all others are type String
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveCalendarFlag(ContextInterface context, int appWidgetId, String key, boolean value) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.putBoolean(prefs_prefix + key, value);
        prefs.apply();
    }

    public static boolean loadCalendarFlag(ContextInterface context, int appWidgetId, String key, boolean defValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        return prefs.getBoolean(prefs_prefix + key, defValue);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveCalendarModePref(ContextInterface context, int appWidgetId, CalendarMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.putString(prefs_prefix + PREF_KEY_CALENDAR_MODE, mode.name());
        prefs.apply();
    }
    public static CalendarMode loadCalendarModePref(ContextInterface context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_CALENDAR_MODE, PREF_DEF_CALENDAR_MODE.name());

        CalendarMode mode;
        try {
            mode = CalendarMode.valueOf(modeString);
        } catch (IllegalArgumentException e) {
            mode = PREF_DEF_CALENDAR_MODE;
        }
        return mode;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveCalendarFormatPatternPref(ContextInterface context, int appWidgetId, String tag, String formatString)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.putString(prefs_prefix + PREF_KEY_CALENDAR_FORMATPATTERN + "_" + tag, formatString);
        prefs.apply();
    }
    public static String loadCalendarFormatPatternPref(ContextInterface context, int appWidgetId, String tag)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        return prefs.getString(prefs_prefix + PREF_KEY_CALENDAR_FORMATPATTERN + "_" + tag, defaultCalendarFormatPattern(tag));
    }
    public static void deleteCalendarFormatPatternPref(ContextInterface context, int appWidgetId, String tag)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.remove(prefs_prefix + PREF_KEY_CALENDAR_FORMATPATTERN + "_" + tag);
        prefs.apply();
    }
    public static String defaultCalendarFormatPattern(String tag)
    {
        CalendarMode mode;
        try {
            mode = CalendarMode.valueOf(tag);
        } catch (IllegalArgumentException e) {
            mode = PREF_DEF_CALENDAR_MODE;
        }
        return mode.getDefaultPattern();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void deleteCalendarPref(ContextInterface context, int appWidgetId, String key)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.remove(prefs_prefix + key);
        prefs.apply();
    }

    public static void deletePrefs(ContextInterface context, int appWidgetId)
    {
        deleteCalendarPref(context, appWidgetId, PREF_KEY_CALENDAR_SHOWDATE);
        deleteCalendarPref(context, appWidgetId, PREF_KEY_CALENDAR_MODE);
        for (CalendarMode mode : CalendarMode.values()) {
            deleteCalendarFormatPatternPref(context, appWidgetId, mode.name());
        }
    }

}
