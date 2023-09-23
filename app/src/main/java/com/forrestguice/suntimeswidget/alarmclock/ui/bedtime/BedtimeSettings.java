/**
    Copyright (C) 2018-2022 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock.ui.bedtime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;

/**
 * AlarmSettings
 */
public class BedtimeSettings
{
    public static final String PREF_KEY_BEDTIME_ALARM_ID = "app_bedtime_alarmid";
    public static final String SLOT_WAKEUP_ALARM = "wakeup";
    public static final String SLOT_BEDTIME_NOTIFY = "notify";
    public static final long ID_NONE = -1;

    public static final String PREF_KEY_SLEEPCYCLE_LENGTH = "app_alarms_sleepCycleMillis";
    public static final int PREF_DEF_SLEEPCYCLE_LENGTH = 1000 * 60 * 90;  // 90 min

    public static final String PREF_KEY_SLEEPCYCLE_COUNT = "app_alarms_sleepCycleCount";
    public static final float PREF_DEF_SLEEPCYCLE_COUNT = 5;

    public static long loadPrefSleepCycleMs(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_SLEEPCYCLE_LENGTH, PREF_DEF_SLEEPCYCLE_LENGTH);
        } else return AlarmSettings.loadStringPrefAsLong(prefs, PREF_KEY_SLEEPCYCLE_LENGTH, PREF_DEF_SLEEPCYCLE_LENGTH);
    }
    public static void savePrefSleepCycleMs(Context context, long value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_SLEEPCYCLE_LENGTH, value);
        prefs.apply();
    }

    public static float loadPrefSleepCycleCount(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getFloat(PREF_KEY_SLEEPCYCLE_COUNT, PREF_DEF_SLEEPCYCLE_COUNT);
    }
    public static void savePrefSleepCycleCount(Context context, float value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putFloat(PREF_KEY_SLEEPCYCLE_COUNT, value);
        prefs.apply();
    }

    public static long loadAlarmID(Context context, String slot)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getLong(PREF_KEY_BEDTIME_ALARM_ID + "_" + slot, ID_NONE);
        } else return AlarmSettings.loadStringPrefAsLong(prefs, PREF_KEY_BEDTIME_ALARM_ID + "_" + slot, ID_NONE);
    }
    public static void saveAlarmID(Context context, String slot, long alarmID) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_BEDTIME_ALARM_ID + "_" + slot, alarmID);
        prefs.apply();
    }
    public static void clearAlarmID(Context context, String slot)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.remove(PREF_KEY_BEDTIME_ALARM_ID + "_" + slot);
        prefs.apply();
    }

}
