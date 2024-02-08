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
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout_1x1_0;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout_2x2_0;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout_3x2_0;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.settings.WidgetSettings.PREF_PREFIX_KEY_APPEARANCE;

public class AlarmWidgetSettings
{
    public static final String MODE_1x1 = "1x1";
    public static final String MODE_2x2 = "2x2";
    public static final String MODE_3x2 = "3x2";
    public static final String PREF_KEY_APPEARANCE_WIDGETMODE_ALARM = "widgetmode_alarm";
    public static final WidgetModeAlarm1x1 PREF_DEF_APPEARANCE_WIDGETMODE_ALARM1x1 = WidgetModeAlarm1x1.WIDGETMODE1x1_0;
    public static final WidgetModeAlarm2x2 PREF_DEF_APPEARANCE_WIDGETMODE_ALARM2x2 = WidgetModeAlarm2x2.WIDGETMODE2x2_0;
    public static final WidgetModeAlarm3x2 PREF_DEF_APPEARANCE_WIDGETMODE_ALARM3x2 = WidgetModeAlarm3x2.WIDGETMODE3x2_0;

    public static final String PREF_PREFIX_KEY_ALARMWIDGET = "_alarmwidget_";

    public static final String PREF_KEY_ALARMWIDGET_TYPES = "alarmtypes";
    public static final String[] PREF_DEF_ALARMWIDGET_TYPES = new String[] { AlarmClockItem.AlarmType.ALARM.name() };
    public static final String[] ALL_TYPES = new String[] { AlarmClockItem.AlarmType.ALARM.name(), AlarmClockItem.AlarmType.NOTIFICATION.name(), AlarmClockItem.AlarmType.NOTIFICATION1.name() };

    public static final String PREF_KEY_ALARMWIDGET_ENABLEDONLY = "enabledonly";
    public static final boolean PREF_DEF_ALARMWIDGET_ENABLEDONLY = true;

    public static final String PREF_KEY_ALARMWIDGET_SORTORDER = "sortorder";
    public static final int PREF_DEF_ALARMWIDGET_SORTORDER = AlarmSettings.SORT_BY_ALARMTIME;

    public static final String PREF_KEY_ALARMWIDGET_SHOWICONS = "showicons";
    public static final boolean PREF_DEF_ALARMWIDGET_SHOWICONS = true;

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static String[] ALL_KEYS = new String[] {
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM + MODE_1x1,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM + MODE_2x2,
            PREF_PREFIX_KEY_APPEARANCE + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM + MODE_3x2,
            PREF_PREFIX_KEY_ALARMWIDGET + PREF_KEY_ALARMWIDGET_TYPES,
            PREF_PREFIX_KEY_ALARMWIDGET + PREF_KEY_ALARMWIDGET_ENABLEDONLY,
            PREF_PREFIX_KEY_ALARMWIDGET + PREF_KEY_ALARMWIDGET_SORTORDER,
    };
    public static String[] BOOL_KEYS = new String[] {
            PREF_PREFIX_KEY_ALARMWIDGET + PREF_KEY_ALARMWIDGET_ENABLEDONLY,
            PREF_PREFIX_KEY_ALARMWIDGET + PREF_KEY_ALARMWIDGET_SHOWICONS,
    };
    public static String[] INT_KEYS = new String[] {
            PREF_PREFIX_KEY_ALARMWIDGET + PREF_KEY_ALARMWIDGET_SORTORDER,
    };

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

    public static void saveAlarmWidgetValue(Context context, int appWidgetId, String key, int value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        prefs.putInt(prefs_prefix + key, value);
        prefs.apply();
    }
    public static void saveAlarmWidgetValue(Context context, int appWidgetId, String key, boolean value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        prefs.putBoolean(prefs_prefix + key, value);
        prefs.apply();
    }
    public static void saveAlarmWidgetValue(Context context, int appWidgetId, String key, String value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        prefs.putString(prefs_prefix + key, value);
        prefs.apply();
    }
    public static void saveAlarmWidgetValue(Context context, int appWidgetId, String key, Set<String> value)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        prefs.putStringSet(prefs_prefix + key, value);
        prefs.apply();
    }

    public static int loadAlarmWidgetInt(Context context, int appWidgetId, String key, int defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        return prefs.getInt(prefs_prefix + key, defaultValue);
    }
    public static boolean loadAlarmWidgetBool(Context context, int appWidgetId, String key, boolean defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        return prefs.getBoolean(prefs_prefix + key, defaultValue);
    }
    public static String loadAlarmWidgetString(Context context, int appWidgetId, String key, String defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        return prefs.getString(prefs_prefix + key, defaultValue);
    }
    public static Set<String> loadAlarmWidgetStringSet(Context context, int appWidgetId, String key, String[] defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        Set<String> defValue = new TreeSet<String>(Arrays.asList(defaultValue));
        return prefs.getStringSet(prefs_prefix + key, defValue);
    }

    public static void deleteAlarmWidgetValue(Context context, int appWidgetId, String key) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_ALARMWIDGET;
        prefs.remove(prefs_prefix + key);
        prefs.apply();
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    public static void saveAlarmModePref(Context context, int appWidgetId, String value, String suffix)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.putString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM + suffix, value);
        prefs.apply();
    }
    public static void deleteAlarmModePref(Context context, int appWidgetId, String suffix)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        prefs.remove(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM + suffix);
        prefs.apply();
    }
    public static String loadAlarmModePref(Context context, int appWidgetId, String suffix, String defaultValue)
    {
        SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
        String prefs_prefix = WidgetSettings.PREF_PREFIX_KEY + appWidgetId + PREF_PREFIX_KEY_APPEARANCE;
        return prefs.getString(prefs_prefix + PREF_KEY_APPEARANCE_WIDGETMODE_ALARM + suffix, defaultValue);
    }

    //////////////////////////////////////////////////

    public static WidgetModeAlarm1x1 loadAlarm1x1ModePref(Context context, int appWidgetId)
    {
        String modeString = loadAlarmModePref(context, appWidgetId, MODE_1x1, PREF_DEF_APPEARANCE_WIDGETMODE_ALARM1x1.name());
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
            default: layout = new AlarmLayout_1x1_0(); break;
        }
        return layout;
    }

    //////////////////////////////////////////////////

    public static WidgetModeAlarm2x2 loadAlarm2x2ModePref(Context context, int appWidgetId)
    {
        String modeString = loadAlarmModePref(context, appWidgetId, MODE_2x2, PREF_DEF_APPEARANCE_WIDGETMODE_ALARM2x2.name());
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
            default: layout = new AlarmLayout_2x2_0(); break;
        }
        return layout;
    }

    //////////////////////////////////////////////////

    public static WidgetModeAlarm3x2 loadAlarm3x2ModePref(Context context, int appWidgetId)
    {
        String modeString = loadAlarmModePref(context, appWidgetId, MODE_3x2, PREF_DEF_APPEARANCE_WIDGETMODE_ALARM3x2.name());
        WidgetModeAlarm3x2 widgetMode;
        try {
            widgetMode = WidgetModeAlarm3x2.valueOf(modeString);

        } catch (IllegalArgumentException e) {
            widgetMode = PREF_DEF_APPEARANCE_WIDGETMODE_ALARM3x2;
            Log.w("loadAlarm2x2ModePref", "Failed to load value '" + modeString + "'; using default '" + PREF_DEF_APPEARANCE_WIDGETMODE_ALARM3x2.name() + "'.");
        }
        return widgetMode;
    }
    public static AlarmLayout loadAlarm3x2ModePref_asLayout(Context context, int appWidgetId)
    {
        AlarmLayout layout;
        WidgetModeAlarm3x2 mode = loadAlarm3x2ModePref(context, appWidgetId);
        switch (mode)
        {
            case WIDGETMODE3x2_0:
            default: layout = new AlarmLayout_3x2_0(); break;
        }
        return layout;
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

    /**
     * WidgetModeAlarm3x2
     */
    public static enum WidgetModeAlarm3x2 implements WidgetSettings.WidgetModeDisplay
    {
        WIDGETMODE3x2_0("Upcoming Alarms", R.layout.layout_widget_alarm_2x2_0);

        private final int layoutID;
        private String displayString;

        private WidgetModeAlarm3x2(String displayString, int layoutID)
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
        WidgetModeAlarm3x2.initDisplayStrings(context);
    }

    /**
     * @param context
     * @param appWidgetId
     */
    public static void deletePrefs(Context context, int appWidgetId)
    {
        deleteAlarmModePref(context, appWidgetId, MODE_1x1);
        deleteAlarmModePref(context, appWidgetId, MODE_2x2);
        deleteAlarmModePref(context, appWidgetId, MODE_3x2);
        deleteAlarmWidgetValue(context, appWidgetId, PREF_KEY_ALARMWIDGET_TYPES);
        deleteAlarmWidgetValue(context, appWidgetId, PREF_KEY_ALARMWIDGET_ENABLEDONLY);
        deleteAlarmWidgetValue(context, appWidgetId, PREF_KEY_ALARMWIDGET_SORTORDER);
        deleteAlarmWidgetValue(context, appWidgetId, PREF_KEY_ALARMWIDGET_SHOWICONS);
    }

}
