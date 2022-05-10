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
package com.forrestguice.suntimeswidget.alarmclock;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;

import java.lang.ref.WeakReference;

/**
 * AlarmSettings
 */
public class AlarmSettings
{
    public static final String PREF_KEY_ALARM_CATEGORY = "app_alarms_category";
    public static final String PREF_KEY_ALARM_BATTERYOPT = "app_alarms_batterytopt";
    public static final String PREF_KEY_ALARM_NOTIFICATIONS = "app_alarms_notifications";
    public static final String PREF_KEY_ALARM_VOLUMES = "app_alarms_volumes";

    public static final String PREF_KEY_ALARM_HARDAREBUTTON_ACTION = "app_alarms_hardwarebutton_action";
    public static final String PREF_DEF_ALARM_HARDAREBUTTON_ACTION = AlarmNotifications.ACTION_SNOOZE;

    public static final String PREF_KEY_ALARM_SILENCEAFTER = "app_alarms_silenceafter";
    public static final int PREF_DEF_ALARM_SILENCEAFTER = -1; // -1 disabled    //1000 * 60 * 15;   // 15 min

    public static final String PREF_KEY_ALARM_TIMEOUT = "app_alarms_timeoutMillis";
    public static final int PREF_DEF_ALARM_TIMEOUT = 1000 * 60 * 20;  // 20 min

    public static final String PREF_KEY_ALARM_SNOOZE = "app_alarms_snoozeMillis";
    public static final int PREF_DEF_ALARM_SNOOZE = 1000 * 60 * 10;  // 10 min

    public static final String PREF_KEY_ALARM_UPCOMING = "app_alarms_upcomingMillis";
    public static final int PREF_DEF_ALARM_UPCOMING = 1000 * 60 * 60 * 10;  // 10 hours

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
    public static final boolean PREF_DEF_ALARM_SHOWLAUNCHER = true;

    public static final String PREF_KEY_ALARM_POWEROFFALARMS = "app_alarms_poweroffalarms";
    public static final boolean PREF_DEF_ALARM_POWEROFFALARMS = false;

    public static final String PREF_KEY_ALARM_UPCOMING_ALARMID = "app_alarms_upcomingAlarmId";    // the alarm we expect to go off next (cached value)

    public static final String PREF_KEY_ALARM_FADEIN = "app_alarms_fadeinMillis";
    public static final int PREF_DEF_ALARM_FADEIN = 1000 * 10;   // 10 s

    public static final int SORT_BY_ALARMTIME = 0;
    public static final int SORT_BY_CREATION = 10;

    public static final String PREF_KEY_ALARM_SORT = "app_alarms_sort";
    public static final int PREF_DEF_ALARM_SORT = SORT_BY_CREATION;

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
    private static long loadStringPrefAsLong(SharedPreferences prefs, String key, long defaultValue)
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

    public static long loadPrefAlarmSnooze(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_SNOOZE, PREF_DEF_ALARM_SNOOZE);
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_SNOOZE, PREF_DEF_ALARM_SNOOZE);
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
            case NOTIFICATION:
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

    public static long loadPrefAlarmFadeIn(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getInt(PREF_KEY_ALARM_FADEIN, PREF_DEF_ALARM_FADEIN);
        } else return loadStringPrefAsLong(prefs, PREF_KEY_ALARM_FADEIN, PREF_DEF_ALARM_FADEIN);
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
    public static String getRingtoneName(Context context, Uri ringtoneUri)
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

    public static Uri getDefaultRingtoneUri(Context context, AlarmClockItem.AlarmType type) {
        return getDefaultRingtoneUri(context, type, false);
    }
    public static Uri getDefaultRingtoneUri(Context context, AlarmClockItem.AlarmType type, boolean resolveDefaults)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String uriString = prefs.getString((type == AlarmClockItem.AlarmType.ALARM) ? PREF_KEY_ALARM_RINGTONE_URI_ALARM : PREF_KEY_ALARM_RINGTONE_URI_NOTIFICATION, VALUE_RINGTONE_DEFAULT);
        if (resolveDefaults && VALUE_RINGTONE_DEFAULT.equals(uriString)) {
            return setDefaultRingtone(context, type);
        } else return (uriString != null ? Uri.parse(uriString) : Uri.parse(VALUE_RINGTONE_DEFAULT));
    }
    public static String getDefaultRingtoneName(Context context, AlarmClockItem.AlarmType type)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString((type == AlarmClockItem.AlarmType.ALARM) ? PREF_KEY_ALARM_RINGTONE_NAME_ALARM : PREF_KEY_ALARM_RINGTONE_NAME_NOTIFICATION, context.getString(R.string.configLabel_tagDefault));
    }

    public static Uri setDefaultRingtone(Context context, AlarmClockItem.AlarmType type)
    {
        Uri uri;
        String key_uri, key_name;
        switch (type)
        {
            case ALARM:
                uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
                key_uri = PREF_KEY_ALARM_RINGTONE_URI_ALARM;
                key_name = PREF_KEY_ALARM_RINGTONE_NAME_ALARM;
                break;
            case NOTIFICATION:
            default:
                uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                key_uri = PREF_KEY_ALARM_RINGTONE_URI_NOTIFICATION;
                key_name = PREF_KEY_ALARM_RINGTONE_NAME_NOTIFICATION;
                break;
        }

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(key_uri, uri.toString());
        prefs.putString(key_name, getRingtoneName(context, uri));
        prefs.apply();
        return uri;
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
            if (context != null) {
                for (AlarmClockItem.AlarmType type : types) {
                    setDefaultRingtone(context, ((type != null) ? type : AlarmClockItem.AlarmType.NOTIFICATION));
                }
                return true;
            } else return false;
        }
    }

}
