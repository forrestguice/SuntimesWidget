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
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout_1x1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout_2x2_0;

import java.util.Map;
import java.util.TreeMap;

public class AlarmWidgetSettings
{
    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_ALARM1x1 = "widgetmode_alarm1x1";
    public static final WidgetModeAlarm1x1 PREF_DEF_APPEARANCE_WIDGETMODE_ALARM1x1 = WidgetModeAlarm1x1.WIDGETMODE1x1_0;

    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_ALARM2x2 = "widgetmode_alarm2x2";
    public static final WidgetModeAlarm2x2 PREF_DEF_APPEARANCE_WIDGETMODE_ALARM2x2 = WidgetModeAlarm2x2.WIDGETMODE2x2_0;

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static String[] ALL_KEYS = new String[] {
            WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM1x1,
            WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM2x2,
    };

    private static Map<String,Class> types = null;
    public static Map<String,Class> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
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

    public static void saveAlarm1x1ModePref(Context context, int appWidgetId, WidgetModeAlarm1x1 mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM1x1, mode.name());
        prefs.apply();
    }
    public static WidgetModeAlarm1x1 loadAlarm1x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM1x1, PREF_DEF_APPEARANCE_WIDGETMODE_ALARM1x1.name());

        WidgetModeAlarm1x1 widgetMode;
        try {
            widgetMode = WidgetModeAlarm1x1.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_ALARM1x1;
            Log.w("loadAlarm1x1ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_ALARM1x1.name() + "'.");
        }
        return widgetMode;
    }
    public static AlarmLayout loadAlarm1x1ModePref_asLayout(Context context, int appWidgetId)
    {
        AlarmLayout layout;
        WidgetModeAlarm1x1 mode = loadAlarm1x1ModePref(context, appWidgetId);
        switch (mode)
        {
            case WIDGETMODE1x1_0:
            default:
                layout = new AlarmLayout_1x1_0();
                break;
        }
        return layout;
    }
    public static void deleteAlarm1x1ModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM1x1);
        prefs.apply();
    }

    //////////////////////////////////////////////////

    public static void saveAlarm2x2ModePref(Context context, int appWidgetId, WidgetModeAlarm2x2 mode)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM2x2, mode.name());
        prefs.apply();
    }
    public static WidgetModeAlarm2x2 loadAlarm2x2ModePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        String modeString = prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM2x2, PREF_DEF_APPEARANCE_WIDGETMODE_ALARM2x2.name());

        WidgetModeAlarm2x2 widgetMode;
        try {
            widgetMode = WidgetModeAlarm2x2.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_ALARM2x2;
            Log.w("loadAlarm2x2ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_ALARM2x2.name() + "'.");
        }
        return widgetMode;
    }
    public static AlarmLayout loadAlarm2x2ModePref_asLayout(Context context, int appWidgetId)
    {
        AlarmLayout layout;
        WidgetModeAlarm2x2 mode = loadAlarm2x2ModePref(context, appWidgetId);
        switch (mode)
        {
            case WIDGETMODE2x2_0:
            default:
                layout = new AlarmLayout_2x2_0();
                break;
        }
        return layout;
    }
    public static void deleteAlarm2x2ModePref(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM2x2);
        prefs.apply();
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * WidgetModeAlarm1x1
     */
    public static enum WidgetModeAlarm1x1 implements WidgetSettings.WidgetModeDisplay
    {
        WIDGETMODE1x1_0("Next Alarm", R.layout.layout_widget_alarm_1x1_0);

        private final int layoutID;
        private String displayString;

        private WidgetModeAlarm1x1(String displayString, int layoutID)
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
            //WIDGETMODE2x1_BOTH_1.setDisplayString(context.getString(R.string.widgetMode1x1_moonriseset));   // TODO
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

    /**
     * WidgetModeAlarm2x2
     */
    public static enum WidgetModeAlarm2x2 implements WidgetSettings.WidgetModeDisplay
    {
        WIDGETMODE2x2_0("Upcoming Alarms", R.layout.layout_widget_alarm_2x2_0);

        private final int layoutID;
        private String displayString;

        private WidgetModeAlarm2x2(String displayString, int layoutID)
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
            //WIDGETMODE2x1_BOTH_1.setDisplayString(context.getString(R.string.widgetMode1x1_moonriseset));   // TODO
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
     * @param context
     */
    public static void initDisplayStrings( Context context )
    {
        WidgetModeAlarm1x1.initDisplayStrings(context);
        WidgetModeAlarm2x2.initDisplayStrings(context);
    }

    /**
     * @param context
     * @param appWidgetId
     */
    public static void deletePrefs(Context context, int appWidgetId)
    {
        deleteAlarm1x1ModePref(context, appWidgetId);
        deleteAlarm2x2ModePref(context, appWidgetId);
    }

}
