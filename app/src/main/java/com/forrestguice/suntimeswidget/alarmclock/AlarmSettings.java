/**
    Copyright (C) 2018-2024 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.PrefTypeInfo;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.suntimeswidget.views.Toast;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;

/**
 * AlarmSettings
 */
public class AlarmSettings
{
    public static final String PREF_KEY_ALARM_CATEGORY = "app_alarms_category";
    public static final String PREF_KEY_ALARM_AUTOSTART = "app_alarms_autostart";
    public static final String PREF_KEY_ALARM_BATTERYOPT = "app_alarms_batterytopt";
    public static final String PREF_KEY_ALARM_NOTIFICATIONS = "app_alarms_notifications";
    public static final String PREF_KEY_ALARM_NOTIFICATIONS_FULLSCREEN = "app_alarms_notifications_fullscreen";
    public static final String PREF_KEY_ALARM_VOLUMES = "app_alarms_volumes";
    public static final String PREF_KEY_ALARM_DND_PERMISSION = "app_alarms_dnd_permission";

    public static final String PREF_KEY_ALARM_HARDAREBUTTON_ACTION = "app_alarms_hardwarebutton_action";
    public static final String PREF_DEF_ALARM_HARDAREBUTTON_ACTION = AlarmNotifications.ACTION_SNOOZE;

    public static final String PREF_KEY_ALARM_SILENCEAFTER = "app_alarms_silenceafter";
    public static final int PREF_DEF_ALARM_SILENCEAFTER = -1; // -1 disabled    //1000 * 60 * 15;   // 15 min

    public static final String PREF_KEY_ALARM_TIMEOUT = "app_alarms_timeoutMillis";
    public static final int PREF_DEF_ALARM_TIMEOUT = 1000 * 60 * 20;  // 20 min

    public static final String PREF_KEY_ALARM_SNOOZE = "app_alarms_snoozeMillis";
    public static final int PREF_DEF_ALARM_SNOOZE = 1000 * 60 * 10;  // 10 min

    public static final String PREF_KEY_ALARM_SNOOZE_LIMIT = "app_alarms_snoozeLimit";
    public static final int PREF_DEF_ALARM_SNOOZE_LIMIT = 0;   // unlimited

    public static final String PREF_KEY_ALARM_UPCOMING = "app_alarms_upcomingMillis";
    public static final int PREF_DEF_ALARM_UPCOMING = 1000 * 60 * 60 * 10;  // 10 hours

    public static final String PREF_KEY_ALARM_AUTODISMISS = "app_alarms_notifyDismissMillis";
    public static final int PREF_DEF_ALARM_AUTODISMISS = 1000 * 30;  // 30 seconds

    public static final String PREF_KEY_ALARM_AUTOENABLE = "app_alarms_autoenable";
    public static final boolean PREF_DEF_ALARM_AUTOENABLE = false;

    public static final String PREF_KEY_ALARM_AUTOVIBRATE = "app_alarms_autovibrate";
    public static final boolean PREF_DEF_ALARM_AUTOVIBRATE = false;

    public static final String PREF_KEY_ALARM_RINGTONE_URI_ALARM = "app_alarms_default_alarm_ringtoneuri";     // cached RingtoneManager.getActualDefaultRingtoneUri
    public static final String PREF_KEY_ALARM_RINGTONE_NAME_ALARM = "app_alarms_default_alarm_ringtonename";

    public static final String PREF_KEY_ALARM_RINGTONE_URI_NOTIFICATION = "app_alarms_default_notification_ringtoneuri";
    public static final String PREF_KEY_ALARM_RINGTONE_NAME_NOTIFICATION = "app_alarms_default_notification_ringtonename";

    public static final String VALUE_RINGTONE_DEFAULT = "default";

    public static final String PREF_KEY_ALARM_ALLRINGTONES = "app_alarms_allringtones";
    public static final boolean PREF_DEF_ALARM_ALLRINGTONES = false;

    public static final String PREF_KEY_ALARM_SHOWLAUNCHER = "app_alarms_showlauncher";

    public static final String PREF_KEY_ALARM_POWEROFFALARMS = "app_alarms_poweroffalarms";
    public static final boolean PREF_DEF_ALARM_POWEROFFALARMS = false;

    public static final String PREF_KEY_ALARM_UPCOMING_ALARMID = "app_alarms_upcomingAlarmId";    // the alarm we expect to go off next (cached value)

    public static final String PREF_KEY_ALARM_BOOTCOMPLETED = "app_alarms_bootcompleted";                       // timestamp of boot_completed event
    public static final String PREF_KEY_ALARM_BOOTCOMPLETED_ATELAPSED = "app_alarms_bootcompleted_elapsed";     // elapsed time of boot_completed event (and delay time before running)
    public static final String PREF_KEY_ALARM_BOOTCOMPLETED_DURATION = "app_alarms_bootcompleted_duration";     // boot_completed run time (milliseconds)
    public static final String PREF_KEY_ALARM_BOOTCOMPLETED_RESULT = "app_alarms_bootcompleted_result";         // bool; true boot_completed finished, false it either never ran or failed to complete (cleared on ACTION_SHUTDOWN)
    public static final String PREF_KEY_ALARM_BOOTCOMPLETED_DELAY = "app_alarms_bootcompleted_delayms";         // delays running the AlarmJobService (milliseconds)

    public static final String PREF_KEY_ALARM_SYSTEM_TIMEZONE_ID = "app_alarms_systemtz_id";
    public static final String PREF_KEY_ALARM_SYSTEM_TIMEZONE_OFFSET = "app_alarms_systemtz_offset";

    public static final String PREF_KEY_ALARM_BRIGHTMODE = "app_alarms_bright";

    public static final String PREF_KEY_ALARM_BRIGHTMODE_FADEIN = "app_alarms_bright_fadeinMillis";

    //public static final String PREF_KEY_ALARM_BRIGHTMODE_STARTCOLOR = "app_alarms_bright_color_start";
    //public static final String PREF_KEY_ALARM_BRIGHTMODE_ENDCOLOR = "app_alarms_bright_color_end";
    public static final String PREF_KEY_ALARM_BRIGHTMODE_COLORS = "app_alarms_bright_colors";

    public static final String PREF_KEY_ALARM_FADEIN = "app_alarms_fadeinMillis";

    public static final int FADE_HANDLER_LINEAR = 0;
    public static final int FADE_HANDLER_CUBIC = 1;

    public static final int FADE_VSHAPER_LINEAR = 10;    // VolumeShaper (api26+)
    public static final int FADE_VSHAPER_CUBIC = 11;
    public static final int FADE_VSHAPER_SCURVE = 12;

    public static final String PREF_KEY_ALARM_FADEIN_METHOD = "app_alarms_fadeinMethod";    // int as String

    public static final int SORT_BY_ALARMTIME = 0;
    public static final int SORT_BY_CREATION = 10;

    public static final String PREF_KEY_ALARM_SORT = "app_alarms_sort";
    public static final int PREF_DEF_ALARM_SORT = SORT_BY_CREATION;

    public static final String PREF_KEY_ALARM_SORT_ENABLED_FIRST = "app_alarms_sort_enabled_first";
    public static final boolean PREF_DEF_ALARM_SORT_ENABLED_FIRST = false;

    public static final String PREF_KEY_ALARM_SORT_SHOW_OFFSET = "app_alarms_sort_show_offset";
    public static final boolean PREF_DEF_ALARM_SORT_SHOW_OFFSET = false;

    public static final String PREF_KEY_ALARM_DISMISS_CHALLENGE = "app_alarms_dismiss_challenge";
    public static final DismissChallenge PREF_DEF_ALARM_DISMISS_CHALLENGE = DismissChallenge.NONE;

    public static final String PREF_KEY_ALARM_CLEARALL = "app_alarms_clearall";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[]
    {
            PREF_KEY_ALARM_CATEGORY, PREF_KEY_ALARM_AUTOSTART,
            PREF_KEY_ALARM_BATTERYOPT, PREF_KEY_ALARM_NOTIFICATIONS,
            PREF_KEY_ALARM_VOLUMES, PREF_KEY_ALARM_HARDAREBUTTON_ACTION,
            PREF_KEY_ALARM_SILENCEAFTER, PREF_KEY_ALARM_TIMEOUT,
            PREF_KEY_ALARM_SNOOZE, PREF_KEY_ALARM_SNOOZE_LIMIT,
            PREF_KEY_ALARM_UPCOMING, PREF_KEY_ALARM_AUTODISMISS,
            PREF_KEY_ALARM_AUTOENABLE, PREF_KEY_ALARM_AUTOVIBRATE,
            PREF_KEY_ALARM_RINGTONE_URI_ALARM, PREF_KEY_ALARM_RINGTONE_NAME_ALARM,
            PREF_KEY_ALARM_RINGTONE_URI_NOTIFICATION, PREF_KEY_ALARM_RINGTONE_NAME_NOTIFICATION,
            PREF_KEY_ALARM_ALLRINGTONES, PREF_KEY_ALARM_SHOWLAUNCHER,
            PREF_KEY_ALARM_POWEROFFALARMS, PREF_KEY_ALARM_UPCOMING_ALARMID,
            PREF_KEY_ALARM_SYSTEM_TIMEZONE_ID, PREF_KEY_ALARM_SYSTEM_TIMEZONE_OFFSET,
            PREF_KEY_ALARM_BRIGHTMODE, PREF_KEY_ALARM_BRIGHTMODE_FADEIN, // PREF_KEY_ALARM_BRIGHTMODE_STARTCOLOR, PREF_KEY_ALARM_BRIGHTMODE_ENDCOLOR,
            PREF_KEY_ALARM_FADEIN, PREF_KEY_ALARM_FADEIN_METHOD, PREF_KEY_ALARM_DISMISS_CHALLENGE,
            PREF_KEY_ALARM_SORT, PREF_KEY_ALARM_SORT_ENABLED_FIRST, PREF_KEY_ALARM_SORT_SHOW_OFFSET,
            PREF_KEY_ALARM_BOOTCOMPLETED, PREF_KEY_ALARM_BOOTCOMPLETED_ATELAPSED, PREF_KEY_ALARM_BOOTCOMPLETED_DURATION, PREF_KEY_ALARM_BOOTCOMPLETED_RESULT,
    };
    public static final String[] LONG_KEYS = new String[] {
            PREF_KEY_ALARM_UPCOMING_ALARMID,
            PREF_KEY_ALARM_SYSTEM_TIMEZONE_OFFSET,
            PREF_KEY_ALARM_BOOTCOMPLETED, PREF_KEY_ALARM_BOOTCOMPLETED_ATELAPSED, PREF_KEY_ALARM_BOOTCOMPLETED_DURATION,
    };
    public static final String[] INT_KEYS = new String[] {
            PREF_KEY_ALARM_SILENCEAFTER, PREF_KEY_ALARM_TIMEOUT,
            PREF_KEY_ALARM_SNOOZE, PREF_KEY_ALARM_SNOOZE_LIMIT,
            PREF_KEY_ALARM_UPCOMING, PREF_KEY_ALARM_AUTODISMISS,
            PREF_KEY_ALARM_FADEIN, PREF_KEY_ALARM_SORT,
            PREF_KEY_ALARM_BRIGHTMODE_FADEIN, // PREF_KEY_ALARM_BRIGHTMODE_STARTCOLOR, PREF_KEY_ALARM_BRIGHTMODE_ENDCOLOR,
    };
    public static final String[] BOOL_KEYS = new String[]
    {
            PREF_KEY_ALARM_AUTOENABLE, PREF_KEY_ALARM_AUTOVIBRATE,
            PREF_KEY_ALARM_ALLRINGTONES, PREF_KEY_ALARM_SHOWLAUNCHER, PREF_KEY_ALARM_POWEROFFALARMS,
            PREF_KEY_ALARM_SORT_ENABLED_FIRST, PREF_KEY_ALARM_SORT_SHOW_OFFSET,
            PREF_KEY_ALARM_BOOTCOMPLETED_RESULT,
            PREF_KEY_ALARM_BRIGHTMODE,
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
            for (String key : LONG_KEYS) {
                types.put(key, Long.class);
            }
            for (String key : INT_KEYS) {
                types.put(key, Integer.class);
            }
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean hasAlarmSupport(Context context) {
        return true;      //return !AppSettings.isTelevision(context);    // TODO: return false for tvs that lack NotificationManager
    }

    public static int loadPrefAlarmSort(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KEY_ALARM_SORT, PREF_DEF_ALARM_SORT);
    }
    public static void savePrefAlarmSort(Context context, int value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_KEY_ALARM_SORT, value);
        prefs.apply();
    }

    public static boolean loadPrefAlarmSortEnabledFirst(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_ALARM_SORT_ENABLED_FIRST, PREF_DEF_ALARM_SORT_ENABLED_FIRST);
    }
    public static void savePrefAlarmSortEnabledFirst(Context context, boolean value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_ALARM_SORT_ENABLED_FIRST, value);
        prefs.apply();
    }

    public static boolean loadPrefAlarmSortShowOffset(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_ALARM_SORT_SHOW_OFFSET, PREF_DEF_ALARM_SORT_SHOW_OFFSET);
    }
    public static void savePrefAlarmSortShowOffset(Context context, boolean value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_ALARM_SORT_SHOW_OFFSET, value);
        prefs.apply();
    }

    public static String loadPrefOnHardwareButtons(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_ALARM_HARDAREBUTTON_ACTION, PREF_DEF_ALARM_HARDAREBUTTON_ACTION);
    }

    public static long loadPrefAlarmSilenceAfter(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_SILENCEAFTER, PREF_DEF_ALARM_SILENCEAFTER);
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_SILENCEAFTER, PREF_DEF_ALARM_SILENCEAFTER);
    }

    public static long loadPrefAlarmTimeout(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_TIMEOUT, PREF_DEF_ALARM_TIMEOUT);
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_TIMEOUT, PREF_DEF_ALARM_TIMEOUT);
    }

    public static long loadPrefAlarmAutoDismiss(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_AUTODISMISS, PREF_DEF_ALARM_AUTODISMISS);
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_AUTODISMISS, PREF_DEF_ALARM_AUTODISMISS);
    }

    public static boolean loadPrefAlarmAutoEnable(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_ALARM_AUTOENABLE, PREF_DEF_ALARM_AUTOENABLE);
    }

    public static boolean loadPrefPowerOffAlarms(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_ALARM_POWEROFFALARMS, PREF_DEF_ALARM_POWEROFFALARMS);
    }

    @TargetApi(10)
    public static long loadStringPrefAsLong(SharedPreferences prefs, String key, long defaultValue)
    {
        try {
            return Long.parseLong(prefs.getString(key, defaultValue + ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Show a reminder when the alarm is within upcoming milliseconds.
     * Note: The reminder won't be shown if the value is less equal zero (disabled).
     * @param context Context
     * @return millisecond value
     */
    public static long loadPrefAlarmUpcoming(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_UPCOMING, PREF_DEF_ALARM_UPCOMING);
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_UPCOMING, PREF_DEF_ALARM_UPCOMING);
    }

    public static void savePrefAlarmUpcomingReminder(Context context, long value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (Build.VERSION.SDK_INT >= 11) {
            prefs.putInt(PREF_KEY_ALARM_UPCOMING, (int) value);
        } else prefs.putString(PREF_KEY_ALARM_UPCOMING, value + "");
        prefs.apply();
    }

    public static void savePrefAlarmUpcoming(Context context, int value) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_KEY_ALARM_UPCOMING, value);
        prefs.apply();
    }

    public static long loadPrefAlarmSnooze(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_SNOOZE, PREF_DEF_ALARM_SNOOZE);
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_SNOOZE, PREF_DEF_ALARM_SNOOZE);
    }
    public static long loadPrefAlarmSnoozeLimit(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_SNOOZE_LIMIT, PREF_DEF_ALARM_SNOOZE_LIMIT);
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_SNOOZE_LIMIT, PREF_DEF_ALARM_SNOOZE_LIMIT);
    }

    public static boolean loadPrefVibrateDefault(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_ALARM_AUTOVIBRATE, PREF_DEF_ALARM_AUTOVIBRATE);
    }


    public static long[] loadPrefVibratePattern(Context context, AlarmClockItem.AlarmType type)
    {
        switch (type)
        {
            case NOTIFICATION: case NOTIFICATION1: case NOTIFICATION2:
            case ALARM:
            default:                    // TODO
                return new long[] {0, 400, 200, 400, 800};   // 0 immediate start, 400ms buzz, 200ms break, 400ms buzz, 800ms break [repeat]
        }
    }

    public static boolean loadPrefAllRingtones(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_ALARM_ALLRINGTONES, PREF_DEF_ALARM_ALLRINGTONES);
    }

    public static boolean loadPrefAlarmBrightMode(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_ALARM_BRIGHTMODE, context.getResources().getBoolean(R.bool.def_app_alarms_bright));
    }

    public static long loadPrefAlarmBrightFadeIn(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_BRIGHTMODE_FADEIN, Integer.parseInt(context.getString(R.string.def_app_alarms_bright_fadein)));
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_BRIGHTMODE_FADEIN, Long.parseLong(context.getString(R.string.def_app_alarms_bright_fadein)));
    }

    /*public static int[] loadPrefAlarmBrightColors(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return new int[] {
                    prefs.getInt(PREF_KEY_ALARM_BRIGHTMODE_STARTCOLOR, ContextCompat.getColor(context, R.color.def_app_alarms_bright_color_start)),
                    prefs.getInt(PREF_KEY_ALARM_BRIGHTMODE_ENDCOLOR, ContextCompat.getColor(context, R.color.def_app_alarms_bright_color_end))
            };
        } else {
            return new int[]{
                    (int) loadStringPrefAsLong(prefs, PREF_KEY_ALARM_BRIGHTMODE_STARTCOLOR, ContextCompat.getColor(context, R.color.def_app_alarms_bright_color_start)),
                    (int) loadStringPrefAsLong(prefs, PREF_KEY_ALARM_BRIGHTMODE_ENDCOLOR, ContextCompat.getColor(context, R.color.def_app_alarms_bright_color_end))
            };
        }
    }*/

    public static long loadPrefAlarmFadeIn(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_FADEIN, Integer.parseInt(context.getString(R.string.def_app_alarms_fadein)));
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_FADEIN, Long.parseLong(context.getString(R.string.def_app_alarms_fadein)));
    }
    public static int loadPrefAlarmFadeInMethod(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return Integer.parseInt(prefs.getString(PREF_KEY_ALARM_FADEIN_METHOD, context.getString(R.string.def_app_alarms_fadein_method)));
        } else return (int) loadStringPrefAsLong(prefs, PREF_KEY_ALARM_FADEIN_METHOD, Long.parseLong(context.getString(R.string.def_app_alarms_fadein_method)));
    }

    public static void saveSystemTimeZoneInfo(Context context) {
        saveSystemTimeZoneInfo(context, TimeZone.getDefault().getID(), TimeZone.getDefault().getOffset(System.currentTimeMillis()));
    }
    public static void saveSystemTimeZoneInfo(Context context, String id, long offset)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(PREF_KEY_ALARM_SYSTEM_TIMEZONE_ID, id);
        prefs.putLong(PREF_KEY_ALARM_SYSTEM_TIMEZONE_OFFSET, offset);
        prefs.apply();
    }
    @Nullable
    public static String loadSystemTimeZoneID(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREF_KEY_ALARM_SYSTEM_TIMEZONE_ID, null);
    }
    public static long loadSystemTimeZoneOffset(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(PREF_KEY_ALARM_SYSTEM_TIMEZONE_OFFSET, TimeZone.getDefault().getOffset(System.currentTimeMillis()));
    }

    public static void saveUpcomingAlarmId(Context context, @Nullable Long alarmId)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (alarmId != null) {
            prefs.putLong(PREF_KEY_ALARM_UPCOMING_ALARMID, alarmId);
        } else prefs.remove(PREF_KEY_ALARM_UPCOMING_ALARMID);
        prefs.apply();
    }
    @Nullable
    public static Long loadUpcomingAlarmId(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long retValue = prefs.getLong(PREF_KEY_ALARM_UPCOMING_ALARMID, -1);
        return (retValue != -1) ? retValue : null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @NonNull
    public static String getRingtoneName(Context context, @Nullable Uri ringtoneUri)
    {
        String ringtoneName = "";
        Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);      // TODO: getRingtone takes up to 100ms!
        if (ringtone != null) {
            ringtoneName = ringtone.getTitle(context);
            ringtone.stop();
        }
        return ringtoneName;
    }

    public static String getRingtoneTitle(@NonNull Context context, @NonNull Uri uri, @NonNull Ringtone ringtone, boolean isAudioFile)
    {
        String ringtoneTitle = ringtone.getTitle(context);
        ringtone.stop();

        String retValue = ringtoneTitle;
        if (isAudioFile)
        {
            Cursor cursor = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    retValue = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                    cursor.close();
                }

            } catch (IllegalArgumentException e) {
                String[] filePath = ringtoneTitle.split("/");
                String fileName = filePath[filePath.length - 1];
                retValue = fileName == null ? null
                        : ((fileName.contains(".")) ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName);

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return retValue;
    }

    public static Uri getFallbackRingtoneUri(Context context, AlarmClockItem.AlarmType type) {
        return Uri.parse(SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/"
                + (type == AlarmClockItem.AlarmType.ALARM ? R.raw.alarmsound : R.raw.notifysound));
    }

    public static final long MAX_WAIT_MS = 990;
    public static Uri getDefaultRingtoneUri(Context context, AlarmClockItem.AlarmType type) {
        return getDefaultRingtoneUri(context, type, false);
    }
    public static Uri getDefaultRingtoneUri(final Context context, final AlarmClockItem.AlarmType type, boolean resolveDefaults)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String uriString = prefs.getString((type == AlarmClockItem.AlarmType.ALARM) ? PREF_KEY_ALARM_RINGTONE_URI_ALARM : PREF_KEY_ALARM_RINGTONE_URI_NOTIFICATION, VALUE_RINGTONE_DEFAULT);
        if (resolveDefaults && VALUE_RINGTONE_DEFAULT.equals(uriString)) {
            return ExecutorUtils.getResult("defaultRingtoneUri", new Callable<Uri>()
            {
                public Uri call() {
                    Uri result = new AlarmSettings().setDefaultRingtone(context, type);
                    return (result != null ? result : Uri.parse(VALUE_RINGTONE_DEFAULT));
                }
            }, MAX_WAIT_MS);
        } else return (uriString != null ? Uri.parse(uriString) : Uri.parse(VALUE_RINGTONE_DEFAULT));
    }
    public static String getDefaultRingtoneName(Context context, AlarmClockItem.AlarmType type)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString((type == AlarmClockItem.AlarmType.ALARM) ? PREF_KEY_ALARM_RINGTONE_NAME_ALARM : PREF_KEY_ALARM_RINGTONE_NAME_NOTIFICATION, context.getString(R.string.configLabel_tagDefault));
    }

    @Nullable
    public Uri getActualDefaultRingtoneUri(Context context, int type) {
        return RingtoneManager.getActualDefaultRingtoneUri(context, type);
    }

    /**
     * Caches the default ringtone uri.
     * @return the default uri (or VALUE_RINGTONE_DEFAULT if not set)
     */
    public Uri setDefaultRingtone(Context context, AlarmClockItem.AlarmType type)
    {
        Uri uri;
        String key_uri, key_name;
        switch (type)
        {
            case ALARM:
                uri = getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
                key_uri = PREF_KEY_ALARM_RINGTONE_URI_ALARM;
                key_name = PREF_KEY_ALARM_RINGTONE_NAME_ALARM;
                break;
            case NOTIFICATION: case NOTIFICATION1: case NOTIFICATION2:
            default:
                uri = getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                key_uri = PREF_KEY_ALARM_RINGTONE_URI_NOTIFICATION;
                key_name = PREF_KEY_ALARM_RINGTONE_NAME_NOTIFICATION;
                break;
        }

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (uri != null)
        {
            prefs.putString(key_uri, uri.toString());
            prefs.putString(key_name, getRingtoneName(context, uri));
            prefs.apply();
            return uri;

        } else {
            prefs.putString(key_uri, VALUE_RINGTONE_DEFAULT);
            prefs.remove(key_name);
            prefs.apply();
            return Uri.parse(VALUE_RINGTONE_DEFAULT);
        }
    }

    public static void setDefaultRingtoneUris(Context context)
    {
        CacheDefaultRingtoneTask task = new CacheDefaultRingtoneTask(context);
        task.execute(AlarmClockItem.AlarmType.ALARM, AlarmClockItem.AlarmType.NOTIFICATION);
    }

    /**
     * CacheDefaultRingtoneTask
     */
    public static class CacheDefaultRingtoneTask extends AsyncTask<AlarmClockItem.AlarmType, Void, Boolean>
    {
        WeakReference<Context> contextRef;
        public CacheDefaultRingtoneTask(Context context) {
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(AlarmClockItem.AlarmType... types)
        {
            Context context = contextRef.get();
            AlarmSettings settings = new AlarmSettings();
            if (context != null) {
                for (AlarmClockItem.AlarmType type : types) {
                    settings.setDefaultRingtone(context, ((type != null) ? type : AlarmClockItem.AlarmType.NOTIFICATION));
                }
                return true;
            } else return false;
        }
    }

    @TargetApi(26)
    public static void openChannelSettings(@NonNull Context context, @NonNull AlarmClockItem.AlarmType type)
    {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, AlarmNotifications.createNotificationChannel(context, type));
        context.startActivity(intent);
    }

    /**
     * isChannelMuted
     * @return true if NotificationChannel is blocked
     */
    public static boolean isChannelMuted(Context context, @NonNull AlarmClockItem.AlarmType type)
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
            {
                String channelID = AlarmNotifications.createNotificationChannel(context, type);
                NotificationChannel channel = notificationManager.getNotificationChannel(channelID);
                return (channel.getImportance() == NotificationManager.IMPORTANCE_NONE);
            }
        }
        return false;
    }

    /**
     * areNotificationsAllowedOnLockScreen
     * @return true notifications allowed on lock screen
     */
    public static boolean areNotificationsAllowedOnLockScreen(Context context, AlarmClockItem.AlarmType type)
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            // https://stackoverflow.com/questions/43438978/get-status-of-setting-control-notifications-on-your-lock-screen
            boolean globalValue = (Settings.Secure.getInt(context.getContentResolver(), "lock_screen_show_notifications", -1) > 0);

            if (Build.VERSION.SDK_INT >= 26)
            {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                {
                    String channelID = AlarmNotifications.createNotificationChannel(context, type);
                    NotificationChannel channel = notificationManager.getNotificationChannel(channelID);
                    return (globalValue && (channel.getLockscreenVisibility() != Notification.VISIBILITY_SECRET));

                } else {
                    return globalValue;
                }
            } else {
                return globalValue;
            }
        } else {
            return true;
        }
    }

    /**
     * @return true device has been unlocked at least once (app now has access to credential protected storage)
     */
    @TargetApi(24)
    public static boolean isUserUnlocked(Context context)
    {
        if (Build.VERSION.SDK_INT >= 24)
        {
            UserManager userManager = (UserManager) context.getSystemService(Service.USER_SERVICE);
            if (userManager != null) {
                return userManager.isUserUnlocked();
            }
        }
        return true;
    }

    /**
     * @return true device has a lock screen
     */
    public static boolean isDeviceSecure(Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= 23)
        {
            KeyguardManager manager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (manager != null) {
                return manager.isDeviceSecure();
            }
        }
        return false;
    }

    public static Context getDeviceProtectedStorageContext(Context context)
    {
        if (Build.VERSION.SDK_INT >= 24) {
            return context.createDeviceProtectedStorageContext();
        }
        return context;
    }

    /**
     * @return true optimization is disabled (recommended), false optimization is enabled (alarms may be delayed or fail to sound)
     */
    public static boolean isIgnoringBatteryOptimizations(Context context)
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        }
        return true;
    }

    /***
     * Some device manufacturers are worse than others; https://dontkillmyapp.com/
     * This method checks the device manufacturer against a list of known offenders.
     * @return true this device is likely to have aggressive (alarm breaking) battery optimizations
     */
    public static boolean aggressiveBatteryOptimizations(Context context)
    {
        String[] manufacturers = context.getResources().getStringArray(R.array.aggressive_battery_known_offenders);
        for (String manufacturer : manufacturers) {
            if (manufacturer != null && manufacturer.equalsIgnoreCase(Build.MANUFACTURER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * https://dontkillmyapp.com/sony
     * @return true device is sony and "stamina mode" is enabled, false device is not sony or "stamina mode" is disabled
     */
    public static boolean isSonyStaminaModeEnabled(Context context) {
        try {
            return (isSony() && android.provider.Settings.Secure.getInt(context.getContentResolver(), "somc.stamina_mode", 0) > 0);
        } catch (Exception e) {
            Log.w("AlarmSettings", "isSonyStaminaModeEnabled: " + e);
            return false;
        }
    }
    public static boolean isSony() {
        return "sony".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static boolean isInRareOrRestrictedBucket(Context context)
    {
        if (Build.VERSION.SDK_INT >= 28)
        {
            UsageStatsManager statsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (statsManager != null) {
                int bucket = statsManager.getAppStandbyBucket();
                return (bucket == STANDBY_BUCKET_RESTRICTED || bucket == UsageStatsManager.STANDBY_BUCKET_RARE);
            }
        }
        return false;
    }
    public static final int STANDBY_BUCKET_RESTRICTED = 0x0000002d;    // TODO: replace this with UsageStatsManager.STANDBY_BUCKET_RESTRICTED after targeting api30+

    /**
     * https://dontkillmyapp.com/xiomi
     * @return true autostart is disabled (xiomi devices only)
     */
    public static boolean isAutostartDisabled(Context context)
    {
        if (isXiomi()) {
            return (XiomiAutostartDetect.getAutostartState_xiomi(context) == XiomiAutostartDetect.STATE_DISABLED);
        } else return false;
    }
    public static boolean hasAutostartSettings(Context context) {
        return isXiomi();
    }
    public static void openAutostartSettings(Context context)
    {
        Intent intent = getAutostartSettingsIntent(context);
        if (intent != null) {
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e("AlarmSettings", "Failed to launch autostart settings Intent: " + e);
            }
        } else Log.e("AlarmSettings", "Failed to launch autostart settings Intent: null");
    }

    @Nullable
    public static Intent getAutostartSettingsIntent(Context context) {
        if (isXiomi()) {
            return getAutostartSettingsIntent_xiomi(context);
        } else return null;
    }

    public static Intent getAutostartSettingsIntent_xiomi(Context context) {
        return new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
    }
    public static boolean isXiomi() {
        return "xiomi".equalsIgnoreCase(Build.MANUFACTURER);
    }

    public static CharSequence autostartMessage(Context context)
    {
        if (AlarmSettings.isAutostartDisabled(context))
        {
            int[] colorAttrs = { R.attr.tagColor_warning };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            int colorWarning = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.warningTag_dark));
            typedArray.recycle();

            String disabledString = context.getString(R.string.configLabel_alarms_autostart_off);
            String summaryString = context.getString(R.string.configLabel_alarms_autostart_summary, disabledString);
            return SuntimesUtils.createColorSpan(null, summaryString, disabledString, colorWarning);

        } else {
            return context.getString(R.string.configLabel_alarms_autostart_summary, context.getString(R.string.configLabel_alarms_autostart_on));
        }
    }

    public static CharSequence batteryOptimizationMessage(Context context)
    {
        int[] colorAttrs = { R.attr.tagColor_warning };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int colorWarning = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.warningTag_dark));
        typedArray.recycle();

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (AlarmSettings.isIgnoringBatteryOptimizations(context)) {
                return context.getString(R.string.configLabel_alarms_optWhiteList_listed);

            } else {
                String unlisted = context.getString(AlarmSettings.aggressiveBatteryOptimizations(context) ? R.string.configLabel_alarms_optWhiteList_unlisted_aggressive : R.string.configLabel_alarms_optWhiteList_unlisted);
                return SuntimesUtils.createColorSpan(null, unlisted, unlisted, colorWarning);
            }
        } else return "";
    }

    /**
     * Recommended; this Intent shows the optimization list (and the user must find and select the app)
     */
    @TargetApi(23)
    public static Intent getRequestIgnoreBatteryOptimizationSettingsIntent(Context context) {
        return new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
    }

    /**
     * This Intent goes directly to the app's optimization settings.
     * Requires permission `android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
     */
    @TargetApi(23)
    @SuppressLint("BatteryLife")
    public static Intent getRequestIgnoreBatteryOptimizationIntent(Context context) {
        return new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + context.getPackageName()));
    }

    public static void openBatteryOptimizationSettings(final Context context)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                context.startActivity(AlarmSettings.getRequestIgnoreBatteryOptimizationSettingsIntent(context));
            } catch (ActivityNotFoundException e) {
                Log.e("AlarmSettings", "Failed to launch battery optimization settings Intent: " + e);
            }
        }
    }
    public static void requestIgnoreBatteryOptimization(final Context context)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                context.startActivity(AlarmSettings.getRequestIgnoreBatteryOptimizationIntent(context));
            } catch (ActivityNotFoundException e) {
                Log.e("AlarmSettings", "Failed to launch battery optimization request Intent: " + e);
            }
        }
    }

    /**
     * shows the screen to manage permissions for full screen intents.
     */
    @TargetApi(34)
    public static Intent getFullScreenIntentSettingsIntent(Context context)
    {
        Intent intent = new Intent(ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }
    public static final String ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT = "android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT";    // TODO: remove and use constant from api29+

    public static void openFullScreenIntentSettings(Context context)
    {
        if (Build.VERSION.SDK_INT >= 34) {
            try {
                context.startActivity(AlarmSettings.getFullScreenIntentSettingsIntent(context));

            } catch (ActivityNotFoundException e) {
                Log.e("AlarmSettings", "Failed to launch 'fullscreen intent settings': " + e);
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * BootCompletedInfo
     */
    public static class BootCompletedInfo
    {
        private final boolean result;
        private final long timeMillis, atElapsedMillis, durationMillis;
        public BootCompletedInfo(long timeMillis, long atElapsedMillis, long durationMillis, boolean result) {
            this.timeMillis = timeMillis;
            this.atElapsedMillis = atElapsedMillis;
            this.durationMillis = durationMillis;
            this.result = result;
        }
        public long getTimeMillis() {
            return timeMillis;
        }
        public long getAtElapsedMillis() {
            return atElapsedMillis;
        }
        public long getDurationMillis() {
            return durationMillis;
        }
        public boolean getResult() {
            return result;
        }
    }

    public static BootCompletedInfo loadPrefLastBootCompleted(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new BootCompletedInfo(prefs.getLong(PREF_KEY_ALARM_BOOTCOMPLETED, -1),
                prefs.getLong(PREF_KEY_ALARM_BOOTCOMPLETED_ATELAPSED, -1),
                prefs.getLong(PREF_KEY_ALARM_BOOTCOMPLETED_DURATION, -1),
                prefs.getBoolean(PREF_KEY_ALARM_BOOTCOMPLETED_RESULT, false));
    }
    public static void savePrefLastBootCompleted(Context context, BootCompletedInfo info) {
        savePrefLastBootCompleted(context, new BootCompletedInfo(info.getTimeMillis(), info.getAtElapsedMillis(), info.getDurationMillis(), info.getResult()));
    }
    public static void savePrefLastBootCompleted(Context context, long timeMillis, long atElapsedMillis, long durationMillis) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_ALARM_BOOTCOMPLETED, timeMillis);
        prefs.putLong(PREF_KEY_ALARM_BOOTCOMPLETED_ATELAPSED, atElapsedMillis);
        prefs.putLong(PREF_KEY_ALARM_BOOTCOMPLETED_DURATION, durationMillis);
        prefs.putBoolean(PREF_KEY_ALARM_BOOTCOMPLETED_RESULT, true);
        prefs.apply();
    }
    public static void savePrefLastBootCompleted_started(Context context, long atElapsedMillis)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_ALARM_BOOTCOMPLETED_ATELAPSED, atElapsedMillis);
        prefs.putBoolean(PREF_KEY_ALARM_BOOTCOMPLETED_RESULT, false);
        prefs.apply();
    }
    public static void savePrefLastBootCompleted_finished(Context context, long timeMillis, long durationMillis)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_KEY_ALARM_BOOTCOMPLETED, timeMillis);
        prefs.putLong(PREF_KEY_ALARM_BOOTCOMPLETED_DURATION, durationMillis);
        prefs.putBoolean(PREF_KEY_ALARM_BOOTCOMPLETED_RESULT, true);
        prefs.apply();
    }

    public static boolean bootCompletedWasRun(Context context)
    {
        BootCompletedInfo info = loadPrefLastBootCompleted(context);
        return (info.getTimeMillis() >= timeOfLastBoot());
    }
    public static long timeOfLastBoot() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    public static final long AFTER_BOOT_COMPLETED_DELAY_MS = 5 * 1000;    // wait this long before running AlarmJobService
    public static final long AFTER_BOOT_COMPLETED_WINDOW_MS = 25 * 1000;    // job to be run between [delay, delay + window]

    public static long bootCompletedDelay(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int delay = prefs.getInt(PREF_KEY_ALARM_BOOTCOMPLETED_DELAY, (int) AFTER_BOOT_COMPLETED_DELAY_MS);
        return Math.max(delay, AFTER_BOOT_COMPLETED_DELAY_MS);
    }

    /**
     * DismissChallenge
     */
    public static enum DismissChallenge
    {
        NONE("None"),
        MATH("Math Problem"),
        ADDON("Addon");

        private DismissChallenge(String displayString)
        {
            this.displayString = displayString;
        }

        private String displayString;
        public String getDisplayString()
        {
            return displayString;
        }
        public void setDisplayString(String value)
        {
            displayString = value;
        }
        public static void initDisplayStrings(Context context)
        {
            NONE.setDisplayString(context.getString(R.string.alarmDismiss_none));
            MATH.setDisplayString(context.getString(R.string.alarmDismiss_math));
        }
        public String toString()
        {
            return displayString;
        }

        public static DismissChallenge valueOf(int ordinal, DismissChallenge defaultValue)
        {
            DismissChallenge[] values = values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            } else return defaultValue;
        }
        public static DismissChallenge valueOf(String name, DismissChallenge defaultValue)
        {
            DismissChallenge retValue;
            try {
                retValue = DismissChallenge.valueOf(name);
            } catch (IllegalArgumentException e) {
                retValue = defaultValue;
            }
            return retValue;
        }

        public void setID(long id) {
            this.id = id;
        }
        public long getID() {
            return id;
        }
        protected long id = ordinal();
    }

    public static DismissChallenge loadDismissChallengePref(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return DismissChallenge.valueOf(prefs.getString(PREF_KEY_ALARM_DISMISS_CHALLENGE, PREF_DEF_ALARM_DISMISS_CHALLENGE.name()), PREF_DEF_ALARM_DISMISS_CHALLENGE);
    }

    /**
     * initDisplayStrings
     * @param context
     */
    public static void initDisplayStrings(Context context) {
        DismissChallenge.initDisplayStrings(context);
    }

    public static boolean loadPrefShowLauncher(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_ALARM_SHOWLAUNCHER, context.getResources().getBoolean(R.bool.def_app_alarms_showlauncher));
    }
    public static void savePrefShowLauncher(Context context, boolean value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_KEY_ALARM_SHOWLAUNCHER, value);
        prefs.apply();
        setShowLauncherIcon(context, value);
    }
    public static void setShowLauncherIcon(Context context, boolean value)
    {
        ComponentName componentName = new ComponentName(context, "com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivityLauncher");
        int state = (value ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
    }


    /**
     * PowerOffAlarmInfo
     */
    public static class PowerOffAlarmInfo
    {
        public static final String ACTION_SET = "poweroffalarm.action.SET_ALARM";
        public static final String ACTION_CANCEL = "poweroffalarm.action.CANCEL_ALARM";

        public static final WidgetActions.LaunchType DEF_LAUNCHTYPE = WidgetActions.LaunchType.BROADCAST;
        public static final String DEF_PACKAGE_POWEROFFALARM = "com.qualcomm.qti.poweroffalarm";
        public static final String DEF_ACTION_POWEROFFALARM_SET = "org.codeaurora.poweroffalarm.action.SET_ALARM";
        public static final String DEF_ACTION_POWEROFFALARM_CANCEL = "org.codeaurora.poweroffalarm.action.CANCEL_ALARM";
        public static final String DEF_PERMISSION_POWEROFFALARM = "org.codeaurora.permission.POWER_OFF_ALARM";
        public static final String DEF_EXTRA_POWEROFFALARM_TIME = "time";

        public WidgetActions.LaunchType getLaunchType() {
            return DEF_LAUNCHTYPE;
        }
        public String getPackage() {
            return DEF_PACKAGE_POWEROFFALARM;
        }
        public String getSetAction() {
            return DEF_ACTION_POWEROFFALARM_SET;
        }
        public String getCancelAction() {
            return DEF_ACTION_POWEROFFALARM_CANCEL;
        }
        public String getPermission() {
            return DEF_PERMISSION_POWEROFFALARM;
        }
        public String getTimeExtra() {
            return DEF_EXTRA_POWEROFFALARM_TIME;
        }
    }

    public static PowerOffAlarmInfo loadPowerOffAlarmInfo(Context context)
    {
        return new PowerOffAlarmInfo();  // TODO
    }

    public static Intent getPowerOffAlarmIntent(Context context, @Nullable String action, long datetime)
    {
        AlarmSettings.PowerOffAlarmInfo info = loadPowerOffAlarmInfo(context);
        if (action == null) {
            action = info.getSetAction();
        }
        if (action.equals(PowerOffAlarmInfo.ACTION_SET)) {
            action = info.getSetAction();
        }
        if (action.equals(PowerOffAlarmInfo.ACTION_CANCEL)) {
            action = info.getCancelAction();
        }

        Intent intent = new Intent(action);
        intent.setPackage(info.getPackage());
        intent.putExtra(info.getTimeExtra(), datetime);
        if (Build.VERSION.SDK_INT >= 16) {
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        }
        return intent;
    }

}
