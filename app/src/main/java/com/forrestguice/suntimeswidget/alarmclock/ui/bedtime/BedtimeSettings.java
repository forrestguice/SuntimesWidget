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

import android.annotation.TargetApi;
import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.Condition;
import android.service.notification.ConditionProviderService;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * AlarmSettings
 */
public class BedtimeSettings
{
    public static final String PREF_KEY_BEDTIME_ALARM_ID = "app_bedtime_alarmid";
    public static final String SLOT_WAKEUP_ALARM = "wakeup";
    public static final String SLOT_BEDTIME_NOTIFY = "notify";
    public static final String SLOT_BEDTIME_REMINDER = "reminder";
    public static final String[] ALL_SLOTS = new String[] { SLOT_WAKEUP_ALARM, SLOT_BEDTIME_NOTIFY, SLOT_BEDTIME_REMINDER };
    public static final long ID_NONE = -1;

    public static final String PREF_KEY_SLEEPCYCLE_LENGTH = "app_bedtime_sleepCycleMillis";
    public static final int PREF_DEF_SLEEPCYCLE_LENGTH = 1000 * 60 * 90;  // 90 min

    public static final String PREF_KEY_SLEEPCYCLE_COUNT = "app_bedtime_sleepCycleCount";
    public static final float PREF_DEF_SLEEPCYCLE_COUNT = 5;

    public static final String PREF_KEY_SLEEP_OFFSET = "app_bedtime_sleep_offset";
    public static final int PREF_DEF_SLEEP_OFFSET = 1000 * 60 * 30;  // 30 min

    public static final String PREF_KEY_SLEEP_USE_SLEEPCYCLE = "app_bedtime_sleep_use_sleepcycle";
    public static final boolean PREF_DEF_SLEEP_USE_SLEEPCYCLE = true;

    public static final String PREF_KEY_SLEEP_LENGTH = "app_bedtime_sleep";
    public static final int PREF_DEF_SLEEP_LENGTH = 1000 * 60 * 60 * 6;  // 6h

    public static final String PREF_KEY_BEDTIME_DND = "app_bedtime_dnd";
    public static final boolean PREF_DEF_BEDTIME_DND = false;

    public static final int DND_FILTER_PRIORITY = 2;
    public static final int DND_FILTER_ALARMS = 4;
    public static final String PREF_KEY_BEDTIME_DND_FILTER = "app_bedtime_dnd_filter";
    public static final int PREF_DEF_BEDTIME_DND_FILTER = DND_FILTER_PRIORITY;

    public static final String PREF_KEY_BEDTIME_REMINDER = "app_bedtime_reminder";
    public static final boolean PREF_DEF_BEDTIME_REMINDER = false;

    public static final String PREF_KEY_BEDTIME_REMINDER_OFFSET = "app_bedtime_reminder_offset";
    public static final long PREF_DEF_BEDTIME_REMINDER_OFFSET = -1000 * 60 * 15;    // 15m before

    public static final int STATE_BEDTIME_INACTIVE = 0;
    public static final int STATE_BEDTIME_ACTIVE = 1;
    public static final int STATE_BEDTIME_PAUSED = 2;

    public static final String PREF_KEY_BEDTIME_STATE = "app_bedtime_state";
    public static int getBedtimeState(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_BEDTIME_STATE, STATE_BEDTIME_INACTIVE);
    }
    public static void setBedtimeState(Context context, int value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_KEY_BEDTIME_STATE, value);
        prefs.apply();
    }

    /**
     * @return true when not STATE_INACTIVE
     */
    public static boolean isBedtimeModeActive(Context context)
    {
        int state = getBedtimeState(context);
        return (state == STATE_BEDTIME_ACTIVE || state == STATE_BEDTIME_PAUSED);
    }
    public static boolean isBedtimeModePaused(Context context)
    {
        int state = getBedtimeState(context);
        return (state == STATE_BEDTIME_PAUSED);
    }

    public static boolean loadPrefBedtimeDoNotDisturb(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_BEDTIME_DND, PREF_DEF_BEDTIME_DND);
    }
    public static void savePrefBedtimeDoNotDisturb(Context context, boolean value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_BEDTIME_DND, value);
        prefs.apply();
    }

    public static int loadPrefBedtimeDoNotDisturbFilter(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_BEDTIME_DND_FILTER, PREF_DEF_BEDTIME_DND_FILTER);
    }
    public static void savePrefBedtimeDoNotDisturbFilter(Context context, int value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_KEY_BEDTIME_DND_FILTER, value);
        prefs.apply();
    }

    public static boolean loadPrefBedtimeReminder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_BEDTIME_REMINDER, PREF_DEF_BEDTIME_REMINDER);
    }
    public static void savePrefBedtimeReminder(Context context, boolean value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_BEDTIME_REMINDER, value);
        prefs.apply();
    }

    public static long loadPrefBedtimeReminderOffset(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(PREF_KEY_BEDTIME_REMINDER_OFFSET, PREF_DEF_BEDTIME_REMINDER_OFFSET);
    }
    public static void savePrefReminderOffset(Context context, long value) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_BEDTIME_REMINDER_OFFSET, value);
        prefs.apply();
    }

    public static long totalSleepTimeMs(Context context)
    {
        long offset = BedtimeSettings.loadPrefSleepOffsetMs(context);
        boolean useSleepCycle = BedtimeSettings.loadPrefUseSleepCycle(context);
        if (useSleepCycle)
        {
            float numSleepCycles = BedtimeSettings.loadPrefSleepCycleCount(context);
            long sleepCycleMs = BedtimeSettings.loadPrefSleepCycleMs(context);
            return (long)(numSleepCycles * sleepCycleMs) + offset;

        } else {
            return BedtimeSettings.loadPrefSleepMs(context) + offset;
        }
    }

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

    public static boolean loadPrefUseSleepCycle(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_SLEEP_USE_SLEEPCYCLE, PREF_DEF_SLEEP_USE_SLEEPCYCLE);
    }
    public static void savePrefUseSleepCycle(Context context, boolean value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_SLEEP_USE_SLEEPCYCLE, value);
        prefs.apply();
    }

    public static long loadPrefSleepMs(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_SLEEP_LENGTH, PREF_DEF_SLEEP_LENGTH);
        } else return AlarmSettings.loadStringPrefAsLong(prefs, PREF_KEY_SLEEP_LENGTH, PREF_DEF_SLEEP_LENGTH);
    }
    public static void savePrefSleepMs(Context context, long value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_SLEEP_LENGTH, value);
        prefs.apply();
    }

    public static long loadPrefSleepOffsetMs(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getLong(PREF_KEY_SLEEP_OFFSET, PREF_DEF_SLEEP_OFFSET);
        } else return AlarmSettings.loadStringPrefAsLong(prefs, PREF_KEY_SLEEP_OFFSET, PREF_DEF_SLEEP_OFFSET);
    }
    public static void savePrefSleepOffsetMs(Context context, long value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_SLEEP_OFFSET, value);
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
    @Nullable
    public static String clearAlarmID(Context context, @Nullable Long alarmID)
    {
        if (alarmID != null) {
            for (String slot : ALL_SLOTS) {
                if (alarmID == loadAlarmID(context, slot)) {
                    clearAlarmID(context, slot);
                    return slot;
                }
            }
        }
        return null;
    }
    public static void clearAlarmIDs(Context context)
    {
        for (String slot : ALL_SLOTS) {
            clearAlarmID(context, slot);
        }
    }
    public static boolean hasAlarmID(Context context, String slot) {
        return (loadAlarmID(context, slot) != ID_NONE);
    }


    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public static boolean hasDoNotDisturbPermission(Context context)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifications != null) {
                return notifications.isNotificationPolicyAccessGranted();
            } else return true;
        } else return true;
    }

    public static void startDoNotDisturbAccessActivity(Context context)
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            context.startActivity(intent);
        }
    }

    /**
     * @param enabled true set rule enabled, false set rule disabled
     * @return ruleID
     */
    public static String setAutomaticZenRule(Context context, boolean enabled)
    {
        NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifications != null && hasDoNotDisturbPermission(context))
        {
            if (Build.VERSION.SDK_INT >= 24)
            {
                String ruleId;
                AutomaticZenRule rule;
                Map<String, AutomaticZenRule> rules = notifications.getAutomaticZenRules();
                if (rules.size() > 0)
                {
                    ruleId = rules.keySet().toArray(new String[0])[0];
                    rule = rules.get(ruleId);
                    boolean modified = false;

                    int filter = BedtimeConditionService.getAutomaticZenRuleFilter(context);
                    if (rule.getInterruptionFilter() != filter) {
                        rule.setInterruptionFilter(filter);
                        modified = true;
                    }
                    if (rule.isEnabled() != enabled) {
                        rule.setEnabled(enabled);
                        modified = true;
                    }
                    if (modified) {
                        notifications.updateAutomaticZenRule(ruleId, rule);
                        Log.d("BedtimeSettings", "Updated AutomaticZenRule " + ruleId + " (" + enabled + ": " + filter + ")");
                    }

                } else {
                    ruleId = notifications.addAutomaticZenRule(BedtimeConditionService.createAutomaticZenRule(context, enabled));
                    Log.d("BedtimeSettings", "Added AutomaticZenRule " + ruleId + " (" + enabled + ")");
                }
                return ruleId;
            }
            return null;
        }
        return null;
    }

    public static void clearAutomaticZenRules(Context context)
    {
        if (Build.VERSION.SDK_INT >= 24)
        {
            NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifications != null && hasDoNotDisturbPermission(context))
            {
                Map<String, AutomaticZenRule> rules = notifications.getAutomaticZenRules();
                for (String id : rules.keySet()) {
                    notifications.removeAutomaticZenRule(id);
                }
            }
        }
    }

    public static void triggerDoNotDisturb(Context context, boolean value)
    {
        if (Build.VERSION.SDK_INT >= 24) {
            BedtimeConditionService.triggerBedtimeAutomaticZenRule(context, value);

        } else if (Build.VERSION.SDK_INT >= 23) {
            NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifications != null && hasDoNotDisturbPermission(context))
            try {
                int policy = (value ? NotificationManager.INTERRUPTION_FILTER_ALARMS : NotificationManager.INTERRUPTION_FILTER_ALL);
                notifications.setInterruptionFilter(policy);    // do-not-disturb requires `android.permission.ACCESS_NOTIFICATION_POLICY`
            } catch (SecurityException e) {
                Log.w("BedtimeSettings", "Failed to toggle do-not-disturb! " + e);
            }
        }
    }

}
