/**
    Copyright (C) 2025 Forrest Guice
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
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout_1x1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.ClockLayout_1x1_1;

import java.util.Map;
import java.util.TreeMap;

import static com.forrestguice.suntimeswidget.settings.WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;

public class ClockWidgetSettings
{
    public static final String MODE_1x1 = "1x1";
    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK = "widgetmode_clock";
    public static final WidgetModeClock1x1 PREF_DEF_APPEARANCE_WIDGETMODE_CLOCK1x1 = WidgetModeClock1x1.CLOCK0;

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static String[] ALL_KEYS = new String[] {
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK + MODE_1x1,
    };
    public static String[] BOOL_KEYS = new String[] {};
    public static String[] INT_KEYS = new String[] {};

    private static Map<String,Class> types = null;
    public static Map<String,Class> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            WidgetSettings.putType(types, Integer.class, INT_KEYS);
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

    public static void saveClockModePref(Context context, int appWidgetId, String value, String suffix)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK + suffix, value);
        prefs.apply();
    }
    public static void deleteClockModePref(Context context, int appWidgetId, String suffix)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK + suffix);
        prefs.apply();
    }
    public static String loadClockModePref(Context context, int appWidgetId, String suffix, String defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_CLOCK + suffix, defaultValue);
    }

    //////////////////////////////////////////////////

    public static WidgetModeClock1x1 loadClock1x1ModePref(Context context, int appWidgetId)
    {
        String modeString = loadClockModePref(context, appWidgetId, MODE_1x1, PREF_DEF_APPEARANCE_WIDGETMODE_CLOCK1x1.name());
        WidgetModeClock1x1 widgetMode;
        try {
            widgetMode = WidgetModeClock1x1.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_CLOCK1x1;
            Log.w("loadClock1x1ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_CLOCK1x1.name() + "'.");
        }
        return widgetMode;
    }
    public static ClockLayout loadClock1x1ModePref_asLayout(Context context, int appWidgetId)
    {
        ClockLayout layout;
        WidgetModeClock1x1 mode = loadClock1x1ModePref(context, appWidgetId);
        switch (mode)
        {
            case CLOCK1: layout = new ClockLayout_1x1_1(); break;
            case CLOCK0:
            default: layout = new ClockLayout_1x1_0(); break;
        }
        return layout;
    }


    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * WidgetModeClock1x1
     */
    public static enum WidgetModeClock1x1 implements WidgetSettings.WidgetModeDisplay
    {
        CLOCK0("Clock 0", R.layout.layout_widget_clock_1x1_0),
        CLOCK1("Clock 1", R.layout.layout_widget_clock_1x1_1);

        private final int layoutID;
        private String displayString;

        private WidgetModeClock1x1(String displayString, int layoutID)
        {
            this.displayString = displayString;
            this.layoutID = layoutID;
        }

        public int getLayoutID() {
            return layoutID;
        }

        public String toString() {
            return displayString;
        }

        public String getDisplayString() {
            return displayString;
        }

        public void setDisplayString( String displayString ) {
            this.displayString = displayString;
        }

        public static void initDisplayStrings( Context context ) {
            //CLOCK0.setDisplayString(context.getString(R.string.configAction_clock));
            //CLOCK1.setDisplayString(context.getString(R.string.configAction_clock));
        }

        public static boolean supportsLayout(int layoutID)
        {
            for (WidgetSettings.WidgetModeDisplay mode : values()) {
                if (mode.getLayoutID() == layoutID) {
                    return true;
                }
            }
            return false;
        }
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * @param context context
     */
    public static void initDisplayStrings( Context context ) {
        WidgetModeClock1x1.initDisplayStrings(context);
    }

    /**
     * @param context context
     * @param appWidgetId appWidgetId
     */
    public static void deletePrefs(Context context, int appWidgetId) {
        deleteClockModePref(context, appWidgetId, MODE_1x1);
    }

}
