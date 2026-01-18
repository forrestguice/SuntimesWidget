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
package com.forrestguice.suntimeswidget.alarmclock.bedtime;

import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.util.prefs.PrefTypeInfo;

import java.util.Map;
import java.util.TreeMap;

/**
 * AlarmSettings
 */
public class BedtimeSettings
{
    public static final String PREF_KEY_BEDTIME_ALARM_ID = "app_bedtime_alarmid";
    public static final String SLOT_WAKEUP_ALARM = "wakeup";
    public static final String SLOT_BEDTIME_NOTIFY = "notify";
    public static final String SLOT_BEDTIME_NOTIFYOFF = "notifyoff";
    public static final String SLOT_BEDTIME_REMINDER = "reminder";
    public static final String[] ALL_SLOTS = new String[] { SLOT_WAKEUP_ALARM, SLOT_BEDTIME_NOTIFY, SLOT_BEDTIME_NOTIFYOFF, SLOT_BEDTIME_REMINDER };
    public static final long ID_NONE = -1;

    public static final String PREF_KEY_SLEEPCYCLE_LENGTH = "app_bedtime_sleepCycleMillis";
    public static final long PREF_DEF_SLEEPCYCLE_LENGTH = 1000 * 60 * 90;  // 90 min

    public static final String PREF_KEY_SLEEPCYCLE_COUNT = "app_bedtime_sleepCycleCount";
    public static final float PREF_DEF_SLEEPCYCLE_COUNT = 5;

    public static final String PREF_KEY_SLEEP_OFFSET = "app_bedtime_sleep_offset";
    public static final long PREF_DEF_SLEEP_OFFSET = 1000 * 60 * 30;  // 30 min

    public static final String PREF_KEY_SLEEP_USE_SLEEPCYCLE = "app_bedtime_sleep_use_sleepcycle";
    public static final boolean PREF_DEF_SLEEP_USE_SLEEPCYCLE = true;

    public static final String PREF_KEY_SLEEP_LENGTH = "app_bedtime_sleep";
    public static final long PREF_DEF_SLEEP_LENGTH = 1000 * 60 * 60 * 6;  // 6h

    public static final String PREF_KEY_BEDTIME_DND = "app_bedtime_dnd";                            // device protected storage
    public static final boolean PREF_DEF_BEDTIME_DND = false;

    public static final String PREF_KEY_BEDTIME_AUTOOFF = "app_bedtime_autooff";                    // device protected storage
    public static final boolean PREF_DEF_BEDTIME_AUTOOFF = true;

    public static final String PREF_KEY_BEDTIME_ALARMOFF = "app_bedtime_alarm_autooff";
    public static final boolean PREF_DEF_BEDTIME_ALARMOFF = true;

    public static final int DND_FILTER_PRIORITY = 2;
    public static final int DND_FILTER_ALARMS = 4;
    public static final String PREF_KEY_BEDTIME_DND_FILTER = "app_bedtime_dnd_filter";              // device protected storage
    public static final int PREF_DEF_BEDTIME_DND_FILTER = DND_FILTER_PRIORITY;

    public static final String PREF_KEY_BEDTIME_DND_RULEBASED = "app_bedtime_dnd_rulebased";        // device protected storage

    public static final String PREF_KEY_BEDTIME_REMINDER = "app_bedtime_reminder";
    public static final boolean PREF_DEF_BEDTIME_REMINDER = false;

    public static final String PREF_KEY_BEDTIME_REMINDER_OFFSET = "app_bedtime_reminder_offset";
    public static final long PREF_DEF_BEDTIME_REMINDER_OFFSET = -1000 * 60 * 15;    // 15m before

    public static final int STATE_BEDTIME_INACTIVE = 0;
    public static final int STATE_BEDTIME_ACTIVE = 1;
    public static final int STATE_BEDTIME_PAUSED = 2;

    public static final String PREF_KEY_BEDTIME_STATE = "app_bedtime_state";                        // device protected storage

    public static final String PREF_KEY_BEDTIME_CATEGORY =  "app_bedtime_category";

    public static int getBedtimeState(Context context0)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_BEDTIME_STATE, STATE_BEDTIME_INACTIVE);
    }
    public static void setBedtimeState(Context context0, int value)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);    // make available while user is locked
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

    public static boolean loadPrefBedtimeAutoOff(Context context0)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_BEDTIME_AUTOOFF, PREF_DEF_BEDTIME_AUTOOFF);
    }
    public static void savePrefBedtimeAutoOff(Context context0, boolean value)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_BEDTIME_AUTOOFF, value);
        prefs.apply();
    }

    public static boolean loadPrefBedtimeAlarmOff(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_BEDTIME_ALARMOFF, PREF_DEF_BEDTIME_ALARMOFF);
    }
    public static void savePrefBedtimeAlarmOff(Context context, boolean value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_BEDTIME_ALARMOFF, value);
        prefs.apply();
    }

    public static boolean loadPrefBedtimeDoNotDisturb(Context context0)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_BEDTIME_DND, PREF_DEF_BEDTIME_DND);
    }
    public static void savePrefBedtimeDoNotDisturb(Context context0, boolean value)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_BEDTIME_DND, value);
        prefs.apply();
    }

    public static boolean loadPrefBedtimeDoNotDisturbRuleBased(Context context0)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_BEDTIME_DND_RULEBASED, context.getResources().getBoolean(R.bool.def_app_bedtime_dnd_rulebased));
    }
    public static void savePrefBedtimeDoNotDisturbRuleBased(Context context0, boolean value)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_BEDTIME_DND_RULEBASED, value);
        prefs.apply();
    }

    public static int loadPrefBedtimeDoNotDisturbFilter(Context context0)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_BEDTIME_DND_FILTER, PREF_DEF_BEDTIME_DND_FILTER);
    }
    public static void savePrefBedtimeDoNotDisturbFilter(Context context0, int value)
    {
        Context context = AlarmSettings.getDeviceProtectedStorageContext(context0);
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

    public static long getBedtimeOffOffset(Context context) {
        return -60000;   // 1 minute before the wakeup alarm
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
            return prefs.getLong(PREF_KEY_SLEEPCYCLE_LENGTH, PREF_DEF_SLEEPCYCLE_LENGTH);
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
            return prefs.getLong(PREF_KEY_SLEEP_LENGTH, PREF_DEF_SLEEP_LENGTH);
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
                    if (rule != null && rule.getInterruptionFilter() != filter) {
                        rule.setInterruptionFilter(filter);
                        modified = true;
                    }
                    if (rule != null && rule.isEnabled() != enabled) {
                        rule.setEnabled(enabled);
                        modified = true;
                    }
                    if (rule != null && modified) {
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

    public static boolean isAutomaticZenRuleEnabled(Context context)
    {
        NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifications != null && hasDoNotDisturbPermission(context))
        {
            if (Build.VERSION.SDK_INT >= 24)
            {
                Map<String, AutomaticZenRule> rules = notifications.getAutomaticZenRules();
                if (rules.size() > 0)
                {
                    String ruleId = rules.keySet().toArray(new String[0])[0];
                    AutomaticZenRule rule = rules.get(ruleId);
                    if (BuildConfig.DEBUG) {
                        Log.d("DEBUG", "rule is enabled? " + (rule != null ? rule.isEnabled() : "null"));
                    }
                    return (rule != null && rule.isEnabled());

                } else return false;
            } else return false;
        } else return false;
    }

    public static void triggerDoNotDisturb(Context context, boolean value)
    {
        boolean useDndRule = BedtimeSettings.loadPrefBedtimeDoNotDisturbRuleBased(context);
        if (Build.VERSION.SDK_INT >= 24 && useDndRule) {
            BedtimeConditionService.triggerBedtimeAutomaticZenRule(context, value);

        } else if (Build.VERSION.SDK_INT >= 23) {
            NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifications != null && hasDoNotDisturbPermission(context))
            {
                try {
                    int policy = (value ? NotificationManager.INTERRUPTION_FILTER_ALARMS : NotificationManager.INTERRUPTION_FILTER_ALL);
                    notifications.setInterruptionFilter(policy);    // do-not-disturb requires `android.permission.ACCESS_NOTIFICATION_POLICY`
                } catch (SecurityException e) {
                    Log.w("BedtimeSettings", "Failed to toggle do-not-disturb! " + e);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void moveSettingsToDeviceSecureStorage(Context context0)
    {
        Context context1 = AlarmSettings.getDeviceProtectedStorageContext(context0);    // copy values to device protected storage
        if (context0 == context1) {
            return;
        }

        SharedPreferences prefs0 = PreferenceManager.getDefaultSharedPreferences(context0);
        SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context1);
        SharedPreferences.Editor editor0 = prefs0.edit();
        SharedPreferences.Editor editor1 = prefs1.edit();

        if (prefs0.contains(PREF_KEY_BEDTIME_STATE)) {
            editor1.putInt(PREF_KEY_BEDTIME_STATE, prefs0.getInt(PREF_KEY_BEDTIME_STATE, STATE_BEDTIME_INACTIVE));
            editor0.remove(PREF_KEY_BEDTIME_STATE);
        }
        if (prefs0.contains(PREF_KEY_BEDTIME_DND)) {
            editor1.putBoolean(PREF_KEY_BEDTIME_DND, prefs0.getBoolean(PREF_KEY_BEDTIME_DND, PREF_DEF_BEDTIME_DND));
            editor0.remove(PREF_KEY_BEDTIME_DND);
        }
        if (prefs0.contains(PREF_KEY_BEDTIME_DND_RULEBASED)) {
            editor1.putBoolean(PREF_KEY_BEDTIME_DND_RULEBASED, prefs0.getBoolean(PREF_KEY_BEDTIME_DND_RULEBASED, context0.getResources().getBoolean(R.bool.def_app_bedtime_dnd_rulebased)));
            editor0.remove(PREF_KEY_BEDTIME_DND_RULEBASED);
        }
        if (prefs0.contains(PREF_KEY_BEDTIME_DND_FILTER)) {
            editor1.putInt(PREF_KEY_BEDTIME_DND_FILTER, prefs0.getInt(PREF_KEY_BEDTIME_DND_FILTER, PREF_DEF_BEDTIME_DND_FILTER));
            editor0.remove(PREF_KEY_BEDTIME_DND_FILTER);
        }
        if (prefs0.contains(PREF_KEY_BEDTIME_AUTOOFF)) {
            editor1.putBoolean(PREF_KEY_BEDTIME_AUTOOFF, prefs0.getBoolean(PREF_KEY_BEDTIME_AUTOOFF, PREF_DEF_BEDTIME_AUTOOFF));
            editor0.remove(PREF_KEY_BEDTIME_AUTOOFF);
        }
        editor0.apply();
        editor1.apply();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[] {
            PREF_KEY_BEDTIME_ALARM_ID,
            PREF_KEY_SLEEPCYCLE_LENGTH,
            PREF_KEY_SLEEPCYCLE_COUNT,
            PREF_KEY_SLEEP_OFFSET,
            PREF_KEY_SLEEP_USE_SLEEPCYCLE,
            PREF_KEY_SLEEP_LENGTH,
            PREF_KEY_BEDTIME_DND,
            PREF_KEY_BEDTIME_AUTOOFF,
            PREF_KEY_BEDTIME_ALARMOFF,
            PREF_KEY_BEDTIME_DND_FILTER,
            PREF_KEY_BEDTIME_REMINDER,
            PREF_KEY_BEDTIME_REMINDER_OFFSET,
            PREF_KEY_BEDTIME_STATE,
    };
    public static final String[] LONG_KEYS = new String[] {
            PREF_KEY_BEDTIME_ALARM_ID,
            PREF_KEY_SLEEPCYCLE_LENGTH,
            PREF_KEY_SLEEP_LENGTH,
            PREF_KEY_SLEEP_OFFSET,
            PREF_KEY_BEDTIME_REMINDER_OFFSET
    };
    public static final String[] INT_KEYS = new String[] {
            PREF_KEY_BEDTIME_DND_FILTER,
            PREF_KEY_BEDTIME_STATE
    };
    public static final String[] BOOL_KEYS = new String[] {
            PREF_KEY_SLEEP_USE_SLEEPCYCLE,
            PREF_KEY_BEDTIME_DND,
            PREF_KEY_BEDTIME_AUTOOFF,
            PREF_KEY_BEDTIME_ALARMOFF,
            PREF_KEY_BEDTIME_REMINDER
    };
    public static final String[] FLOAT_KEYS = new String[] {
            PREF_KEY_SLEEPCYCLE_COUNT
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
                return LONG_KEYS;
            }
            public String[] floatKeys() {
                return FLOAT_KEYS;
            }
            public String[] boolKeys() {
                return BOOL_KEYS;
            }
        };
    }

    private static Map<String,Class<?>> types = null;
    public static Map<String,Class<?>> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            for (String key : LONG_KEYS) {
                types.put(key, Long.class);
            }
            for (String key : INT_KEYS) {
                types.put(key, Integer.class);
            }
            for (String key : BOOL_KEYS) {
                types.put(key, Boolean.class);
            }
            for (String key : FLOAT_KEYS) {
                types.put(key, Float.class);
            }

            for (String key : ALL_KEYS) {                // all others are type String
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }

}
