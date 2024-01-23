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

import android.content.Context;
import android.content.SharedPreferences;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Map;
import java.util.TreeMap;

/**
 * @see WidgetSettings
 */
public class CalendarSettings
{
    public static final String PREFS_WIDGET = WidgetSettings.PREFS_WIDGET;
    public static final String PREF_PREFIX_KEY = WidgetSettings.PREF_PREFIX_KEY;

    public static final String PREF_PREFIX_KEY_CALENDAR = "_calendar_";

    public static final String PREF_KEY_CALENDAR_SHOWDATE = "showDate";        // always true for the DateWidget; used by other widget to optionally show a date
    public static final boolean PREF_DEF_CALENDAR_SHOWDATE = false;

     public static final String PREF_KEY_CALENDAR_MODE = "calendarMode";
    public static final CalendarMode PREF_DEF_CALENDAR_MODE = CalendarMode.GREGORIAN;

    public static final String PREF_KEY_CALENDAR_FORMATPATTERN = "calendarFormat";

    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_CHINESE = "EEE, d. MMMM r(U)";   // TODO: review
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_COPTIC = "MMMM d, yyyy";   // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_ETHIOPIAN = "MMMM d, yyyy";   // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_GREGORIAN = "MMMM d, yyyy";
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_HEBREW = "d MMMM yyyy";
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_HIJRI_DIYANET = "MMMM d, yyyy";    // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_HIJRI_UMALQURA = "MMMM d, yyyy";    // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_INDIAN = "MMMM d, yyyy";    // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_JAPANESE = "MMMM d, yyyy";    // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_JULIAN = "MMMM d, yyyy";   // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_KOREAN = "EEE, d. MMMM r(U)";   // TODO: review
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_MINGUO = "MMMM d, yyyy";   // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_PERSIAN = "MMMM d, yyyy";     // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_THAISOLAR = "MMMM d, yyyy";   // TODO
    public static final String PREF_DEF_CALENDAR_FORMATPATTERN_VIETNAMESE = "EEE, d. MMMM r(U)";   // TODO: review

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS;
    static
    {
        CalendarMode[] modes = CalendarMode.values();
        ALL_KEYS = new String[modes.length + 3];
        for (int i=0; i<modes.length; i++) {
            ALL_KEYS[i] = PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_FORMATPATTERN + "_" + modes[i];
        }
        int i = modes.length;
        ALL_KEYS[i] = PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_FORMATPATTERN;
        ALL_KEYS[i+1] = PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_SHOWDATE;
        ALL_KEYS[i+2] = PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_MODE;
    }

    public static final String[] BOOL_KEYS = {
            PREF_PREFIX_KEY_CALENDAR + PREF_KEY_CALENDAR_SHOWDATE
    };

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

    public static void saveCalendarFlag(Context context, int appWidgetId, String key, boolean value) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.putBoolean(prefs_prefix + key, value);
        prefs.apply();
    }

    public static boolean loadCalendarFlag(Context context, int appWidgetId, String key, boolean defValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        return prefs.getBoolean(prefs_prefix + key, defValue);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void saveCalendarModePref(Context context, int appWidgetId, CalendarMode mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.putString(prefs_prefix + PREF_KEY_CALENDAR_MODE, mode.name());
        prefs.apply();
    }
    public static CalendarMode loadCalendarModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
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

    public static void saveCalendarFormatPatternPref(Context context, int appWidgetId, String tag, String formatString)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.putString(prefs_prefix + PREF_KEY_CALENDAR_FORMATPATTERN + "_" + tag, formatString);
        prefs.apply();
    }
    public static String loadCalendarFormatPatternPref(Context context, int appWidgetId, String tag)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET, 0);
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        return prefs.getString(prefs_prefix + PREF_KEY_CALENDAR_FORMATPATTERN + "_" + tag, defaultCalendarFormatPattern(tag));
    }
    public static void deleteCalendarFormatPatternPref(Context context, int appWidgetId, String tag)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
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

    public static void deleteCalendarPref(Context context, int appWidgetId, String key)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET, 0).edit();
        String prefs_prefix = PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_CALENDAR;
        prefs.remove(prefs_prefix + key);
        prefs.apply();
    }

    public static void deletePrefs(Context context, int appWidgetId)
    {
        deleteCalendarPref(context, appWidgetId, PREF_KEY_CALENDAR_SHOWDATE);
        deleteCalendarPref(context, appWidgetId, PREF_KEY_CALENDAR_MODE);
        for (CalendarMode mode : CalendarMode.values()) {
            deleteCalendarFormatPatternPref(context, appWidgetId, mode.name());
        }
    }

    public static void initDisplayStrings( Context context )
    {
        CalendarMode.initDisplayStrings(context);
        CalendarFormat.initDisplayStrings(context);
    }
}
