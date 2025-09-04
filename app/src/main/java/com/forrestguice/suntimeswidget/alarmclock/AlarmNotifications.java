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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;

import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.MessageFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.VolumeShaper;
import android.net.Uri;

import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.alarmclock.bedtime.BedtimeActivity;
import com.forrestguice.suntimeswidget.alarmclock.bedtime.BedtimeSettings;
import com.forrestguice.suntimeswidget.calculator.DataSubstitutions;
import com.forrestguice.suntimeswidget.views.ExecutorUtils;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmDismissActivity;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

public class AlarmNotifications extends BroadcastReceiver
{
    public static final String TAG = "AlarmReceiver";

    public static final String ACTION_SHOW = "suntimeswidget.alarm.show";                // sound an alarm
    public static final String ACTION_SILENT = "suntimeswidget.alarm.silent";            // silence an alarm (but don't dismiss it)
    public static final String ACTION_DISMISS = "suntimeswidget.alarm.dismiss";          // dismiss an alarm
    public static final String ACTION_SNOOZE = "suntimeswidget.alarm.snooze";            // snooze an alarm
    public static final String ACTION_SCHEDULE = "suntimeswidget.alarm.schedule";        // enable (schedule) an alarm
    public static final String ACTION_RESCHEDULE = "suntimeswidget.alarm.reschedule";    // reschedule; same as schedule but prev alarmtime is replaced.
    public static final String ACTION_RESCHEDULE1 = ACTION_RESCHEDULE + "1";             // reschedule + 1; advance schedule by 1 cycle (prev alarmtime used as basis for scheduling)
    public static final String ACTION_DISABLE = "suntimeswidget.alarm.disable";          // disable an alarm
    public static final String ACTION_TIMEOUT = "suntimeswidget.alarm.timeout";          // timeout an alarm
    public static final String ACTION_DELETE = "suntimeswidget.alarm.delete";            // delete an alarm
    public static final String ACTION_UPDATE_UI = "suntimeswidget.alarm.ui.update";
    public static final String ACTION_LOCATION_CHANGED = "suntimeswidget.alarm.location_changed";  // signals home location is changed/reconfigured; reschedule these alarms

    public static final String ACTION_BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;     // signals work to be done during BOOT_COMPLETED
    public static final String ACTION_AFTER_BOOT_COMPLETED = "suntimeswidget.alarm.AFTER_BOOT_COMPLETED";    // signals work to be done sometime shortly after BOOT_COMPLETED
    public static final String ACTION_LOCKED_BOOT_COMPLETED;
    static {
        if (Build.VERSION.SDK_INT >= 24) {
            ACTION_LOCKED_BOOT_COMPLETED = Intent.ACTION_LOCKED_BOOT_COMPLETED;
        } else ACTION_LOCKED_BOOT_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED";
    }

    public static final String ACTION_BEDTIME = "suntimeswidget.alarm.start_bedtime";              // enable bedtime mode
    public static final String ACTION_BEDTIME_PAUSE = "suntimeswidget.alarm.pause_bedtime";        // pause bedtime mode
    public static final String ACTION_BEDTIME_RESUME = "suntimeswidget.alarm.resume_bedtime";      // resume bedtime mode
    public static final String ACTION_BEDTIME_DISMISS = "suntimeswidget.alarm.dismiss_bedtime";    // disable bedtime mode

    public static final String EXTRA_NOTIFICATION_ID = "notificationID";
    public static final String ALARM_NOTIFICATION_TAG = "suntimesalarm";

    public static final String CHANNEL_ID_ALARMS = "suntimes.channel.alarms";
    public static final String CHANNEL_ID_NOTIFICATIONS0 = "suntimes.channel.notifications0";
    public static final String CHANNEL_ID_NOTIFICATIONS1 = "suntimes.channel.notifications1";
    public static final String CHANNEL_ID_MISC = "suntimes.channel.misc";
    public static final String CHANNEL_ID_BEDTIME = "suntimes.channel.bedtime";

    public static final int NOTIFICATION_SERVICE_IS_ACTIVE_ID = -1;

    public static final int NOTIFICATION_SCHEDULE_ALL_ID = -10;
    public static final int NOTIFICATION_SCHEDULE_ALL_DURATION = 4000;

    public static final int NOTIFICATION_BATTERYOPT_WARNING_ID = -20;
    public static final int NOTIFICATION_AUTOSTART_WARNING_ID = -30;

    public static final int NOTIFICATION_BEDTIME_ACTIVE_ID = -1000;
    
    public static final String[] ALARM_ACTIONS = new String[] {
            ACTION_SHOW, ACTION_SILENT, ACTION_DISMISS, ACTION_SNOOZE,
            ACTION_SCHEDULE, ACTION_RESCHEDULE, ACTION_RESCHEDULE1,
            ACTION_DISABLE, ACTION_TIMEOUT, ACTION_DELETE,
            ACTION_UPDATE_UI, ACTION_LOCATION_CHANGED,
            ACTION_AFTER_BOOT_COMPLETED,
    };
    public static final String[] BEDTIME_ACTIONS = new String[] {
            ACTION_BEDTIME, ACTION_BEDTIME_PAUSE, ACTION_BEDTIME_RESUME, ACTION_BEDTIME_DISMISS
    };
    public static final String[] SYSTEM_ACTIONS = new String[] {
            ACTION_BOOT_COMPLETED, ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED
    };

    private static SuntimesUtils utils = new SuntimesUtils();
    private static final long AFTER_BOOT_COMPLETED_DELAY_MS = 10 * 1000;

    /**
     * onReceive
     * @param context Context
     * @param intent Intent
     */
    @Override
    public void onReceive(final Context context, Intent intent)
    {
        final String action = intent.getAction();
        Uri data = intent.getData();
        Log.d(TAG, "onReceive: " + action + ", " + data);
        if (action != null)
        {
            if (ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action))
            {
                scheduleAfterBootCompleted(context);
                if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
                    BedtimeSettings.moveSettingsToDeviceSecureStorage(context);
                }
                Log.d(TAG, "onReceive: ACTION_AFTER_BOOT_COMPLETED scheduled for a moment from now...");

            } else if (actionIsPermitted(action)) {
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(NotificationService.getNotificationIntent(context, action, data, intent.getExtras()));
                } else {
                    context.startService(NotificationService.getNotificationIntent(context, action, data, intent.getExtras()));
                }
            } else Log.e(TAG, "onReceive: `" + action + "` is not on the list of permitted actions! Ignoring...");
        } else Log.w(TAG, "onReceive: null action!");
    }

    protected void scheduleAfterBootCompleted(Context context)
    {
        if (Build.VERSION.SDK_INT >= 24) {
            AlarmJobService.scheduleJobAfterBootCompleted(context);

        } else {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                    getAlarmIntent(context, ACTION_AFTER_BOOT_COMPLETED, null), PendingIntent.FLAG_UPDATE_CURRENT);
            long atTime = System.currentTimeMillis() + AFTER_BOOT_COMPLETED_DELAY_MS;
            addTimeout(context, pendingIntent, atTime, AlarmManager.RTC_WAKEUP);
        }
    }

    protected boolean actionIsPermitted(String action)
    {
        for (String a : ALARM_ACTIONS) {
            if (a.equals(action)) {
                return true;
            }
        }
        for (String a : SYSTEM_ACTIONS) {
            if (a.equals(action)) {
                 return true;
            }
        }
        return false;
    }

    /**
     */
    public static void showTimeUntilToast(Context context, View view, @NonNull AlarmClockItem item) {
        showTimeUntilToast(context, view, item, null, null, null, Toast.LENGTH_SHORT);
    }
    public static Snackbar showTimeUntilToast(Context context, View view, @NonNull AlarmClockItem item, @Nullable Integer messageResID, String actionText, View.OnClickListener actionListener, int duration)
    {
        if (context != null)
        {
            if (messageResID == null) {
                messageResID = R.string.alarmenabled_toast;
            }

            Calendar now = Calendar.getInstance();
            SuntimesUtils.initDisplayStrings(context);

            String alarmString;
            SpannableString alarmDisplay;
            switch(item.getState())
            {
                case AlarmState.STATE_TIMEOUT:
                    alarmString = context.getString(R.string.alarmAction_timeoutMsg);
                    alarmDisplay = new SpannableString(alarmString);
                    break;

                default:
                    SuntimesUtils.TimeDisplayText alarmText = utils.timeDeltaLongDisplayString(now.getTimeInMillis(), item.timestamp + item.offset);
                    alarmString = context.getString(messageResID, item.type.getDisplayString(), alarmText.getValue());
                    alarmDisplay = SuntimesUtils.createBoldSpan(null, alarmString, alarmText.getValue());
                    break;
            }

            if (view != null)
            {
                Snackbar snackbar = Snackbar.make(view, alarmDisplay, duration);
                if (actionText != null && actionListener != null) {
                    snackbar.setAction(actionText, actionListener);
                }
                ViewUtils.themeSnackbar(context, snackbar, null);
                snackbar.show();
                return snackbar;

            } else {
                Toast.makeText(context, alarmDisplay, duration).show();
                return null;
            }

        }
        Log.e(TAG, "showTimeUntilToast: context is null!");
        return null;
    }

    /**
     */
    protected static void showAlarmSilencedToast(Context context)
    {
        if (context != null) {
            Toast.makeText(context, context.getString(R.string.alarmAction_silencedMsg), Toast.LENGTH_SHORT).show();
        } else Log.e(TAG, "showAlarmSilencedToast: context is null!");
    }

    /**
     */
    protected static void showAlarmPlayingToast(Context context, AlarmClockItem item)
    {
        if (context != null) {
            Toast.makeText(context, context.getString(R.string.alarmAction_playingMsg, item.getLabel(context)), Toast.LENGTH_SHORT).show();
        } else Log.e(TAG, "showAlarmPlayingToast: context is null!");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * addAlarmTimeout
     * @param context context
     * @param action e.g. ACTION_SHOW, ACTION_SCHEDULE, ACTION_SILENCE, ACTION_SNOOZE, ACTION_TIMEOUT
     * @param data alarm Uri
     * @param timeoutAt long timestamp
     */
    protected static void addAlarmTimeout(Context context, String action, Uri data, long timeoutAt)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            addAlarmTimeout(context, alarmManager, action, data, timeoutAt, AlarmManager.RTC_WAKEUP);
        } else Log.e(TAG, "addAlarmTimeout: AlarmManager is null!");
    }
    protected static void addAlarmTimeout(Context context, @NonNull AlarmManager alarmManager, String action, Uri data, long timeoutAt, int type)
    {
        Log.d(TAG, "addAlarmTimeout: " + action + ": " + data + " (wakeup:" + type + ")");
        if (action.equals(ACTION_SHOW))
        {
            if (Build.VERSION.SDK_INT >= 21)
            {
                PendingIntent showAlarmIntent = PendingIntent.getActivity(context, 0, getAlarmListIntent(context, ContentUris.parseId(data)), 0);
                AlarmManager.AlarmClockInfo alarmInfo = new AlarmManager.AlarmClockInfo(timeoutAt, showAlarmIntent);
                //noinspection MissingPermission
                alarmManager.setAlarmClock(alarmInfo, getPendingIntent(context, action, data));    // TODO:  android.permission.SCHEDULE_EXACT_ALARM required after targeting api31+

            } else if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(type, timeoutAt, getPendingIntent(context, action, data));

            } else alarmManager.set(type, timeoutAt, getPendingIntent(context, action, data));

        } else {
            // ACTION_SCHEDULE, ACTION_SILENCE, ACTION_SNOOZE, ACTION_TIMEOUT
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(type, timeoutAt, getPendingIntent(context, action, data));

            } else if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(type, timeoutAt, getPendingIntent(context, action, data));

            } else alarmManager.set(type, timeoutAt, getPendingIntent(context, action, data));
        }
    }

    protected static void addAlarmTimeouts(Context context, Uri data)
    {
        Log.d(TAG, "addAlarmTimeouts: " + data);
        if (context != null)
        {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null)
            {
                long silenceMillis = AlarmSettings.loadPrefAlarmSilenceAfter(context);
                if (silenceMillis > 0)
                {
                    Log.d(TAG, "addAlarmTimeouts: silence after " + silenceMillis);
                    long silenceAt = Calendar.getInstance().getTimeInMillis() + silenceMillis;
                    addAlarmTimeout(context, alarmManager, ACTION_SILENT, data, silenceAt, AlarmManager.RTC_WAKEUP);
                }

                long timeoutMillis = AlarmSettings.loadPrefAlarmTimeout(context);
                if (timeoutMillis > 0)
                {
                    Log.d(TAG, "addAlarmTimeouts: timeout after " + timeoutMillis);
                    long timeoutAt = Calendar.getInstance().getTimeInMillis() + timeoutMillis;
                    addAlarmTimeout(context, alarmManager, ACTION_TIMEOUT, data, timeoutAt, AlarmManager.RTC_WAKEUP);
                }

            } else Log.e(TAG, "addAlarmTimeout: AlarmManager is null!");
        } else Log.e(TAG, "addAlarmTimeout: context is null!");
    }

    protected static void addTimeout(Context context, PendingIntent pendingIntent, long timeoutAt, int type)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(type, timeoutAt, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(type, timeoutAt, pendingIntent);
            } else alarmManager.set(type, timeoutAt, pendingIntent);
        } else {
            Log.e(TAG, "addTimeout: AlarmManager is null!");
        }
    }

    protected static void addNotificationTimeouts(Context context, Uri data)
    {
        Log.d(TAG, "addNotificationTimeouts: " + data);
        if (context != null)
        {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null)
            {
                long dismissAfterMillis = AlarmSettings.loadPrefAlarmAutoDismiss(context);
                if (dismissAfterMillis > 0)
                {
                    Log.d(TAG, "addNotificationTimeouts: dismiss after " + dismissAfterMillis);
                    long dismissedAt = Calendar.getInstance().getTimeInMillis() + dismissAfterMillis;
                    addAlarmTimeout(context, alarmManager, ACTION_DISMISS, data, dismissedAt, AlarmManager.RTC_WAKEUP);
                }

            } else Log.e(TAG, "addNotificationTimeouts: AlarmManager is null!");
        } else Log.e(TAG, "addNotificationTimeouts: context is null!");
    }

    protected static void cancelAlarmTimeouts(Context context, AlarmClockItem item)
    {
        cancelAlarmTimeouts(context, item.getUri());
        if (AlarmSettings.loadPrefPowerOffAlarms(context)) {
            cancelPowerOffAlarm(context, item);
        }
    }
    protected static void cancelAlarmTimeout(Context context, String action, Uri data) {
        cancelAlarmTimeouts(context, new String[] {action}, data);
    }
    protected static void cancelAlarmTimeouts(Context context, Uri data) {
        cancelAlarmTimeouts(context, new String[] {
                AlarmNotifications.ACTION_SILENT,
                AlarmNotifications.ACTION_TIMEOUT,
                AlarmNotifications.ACTION_DISMISS,
                AlarmNotifications.ACTION_SHOW,
                AlarmNotifications.ACTION_SCHEDULE }, data);
    }
    protected static void cancelAlarmTimeouts(Context context, String[] actions, Uri data)
    {
        if (context != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Log.d(TAG, "cancelAlarmTimeouts: " + data);
                for (String action : actions) {
                    alarmManager.cancel(getPendingIntent(context, action, data));
                }
            } else Log.e(TAG, "cancelAlarmTimeouts: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmTimeouts: context is null!");
    }

    protected static void cancelAlarmTimeouts(final Context context, Long[] alarmIds)
    {
        if (context != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                for (long alarmId : alarmIds) {
                    cancelAlarmTimeouts(context, ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmId));
                }
            } else Log.e(TAG, "cancelAlarmTimeouts: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmTimeouts: context is null!");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void findEnabledAlarms(final Context context, @Nullable final AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener onFinished)
    {
        AlarmDatabaseAdapter.AlarmListTask findTask = new AlarmDatabaseAdapter.AlarmListTask(context);
        findTask.setParam_enabledOnly(true);
        findTask.setAlarmItemTaskListener(onFinished);
        findTask.execute();
    }

    public static void findSoundingAlarms(final Context context, @Nullable final AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener onFinished)
    {
        AlarmDatabaseAdapter.AlarmListTask findTask = new AlarmDatabaseAdapter.AlarmListTask(context);
        findTask.setParam_withAlarmState(AlarmState.STATE_SOUNDING);
        findTask.setAlarmItemTaskListener(onFinished);
        findTask.execute();
    }

    public static void findAppLocationAlarms(final Context context, @Nullable final AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener onFinished)
    {
        AlarmDatabaseAdapter.AlarmListTask findTask = new AlarmDatabaseAdapter.AlarmListTask(context)
        {
            @Override
            protected boolean passesFilter(Cursor cursor, long rowID)
            {
                int index = cursor.getColumnIndex(AlarmDatabaseAdapter.KEY_ALARM_FLAGS);
                String rawFlags = (index >= 0) ? cursor.getString(index) : null;
                flags.clear();
                AlarmClockItem.parseAlarmFlags(flags, rawFlags);
                return AlarmClockItem.flagIsTrue(flags, AlarmClockItem.FLAG_LOCATION_FROM_APP);
            }
            private final HashMap<String,Long> flags = new HashMap<>();
        };
        findTask.setAlarmItemTaskListener(onFinished);
        findTask.setParam_enabledOnly(true);
        findTask.execute();
    }

    /**
     * Find the alarm expected to trigger next and cache its ID in prefs.
     * If using 'power off alarms' this is the alarm that should wake the device.
     * @param context context
     * @param saveResult true save to prefs (and set power off alarm); false no action is performed (the result is available in onFinished)
     * @param onFinished task AlarmListTaskListener
     */
    public static void findUpcomingAlarm(final Context context, final boolean saveResult, @Nullable final AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener onFinished)
    {
        AlarmDatabaseAdapter.AlarmListTask findTask = new AlarmDatabaseAdapter.AlarmListTask(context);
        findTask.setParam_enabledOnly(true);
        findTask.setParam_nowMillis(System.currentTimeMillis());
        findTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener()
        {
            @Override
            public void onItemsLoaded(Long[] ids)
            {
                Log.d(TAG, "findUpcomingAlarm: " + (saveResult ? "saved " : "found ") + ids[0]);
                if (saveResult)
                {
                    AlarmSettings.saveUpcomingAlarmId(context, ids[0]);
                    if (AlarmSettings.loadPrefPowerOffAlarms(context) && ids[0] != null) {
                        setPowerOffAlarm(context, ids[0]);
                    }
                }
                if (onFinished != null) {
                    onFinished.onItemsLoaded(ids);
                }
            }
        });
        findTask.execute();
    }
    public static void findUpcomingAlarm(final Context context, @Nullable final AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener onFinished) {
        findUpcomingAlarm(context, true, onFinished);
    }

    protected static void setPowerOffAlarm(final Context context, long alarmId)
    {
        AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(context);
        itemTask.addAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener() {
            @Override
            public void onFinished(Boolean result, AlarmClockItem alarm) {
                setPowerOffAlarm(context, alarm);
            }
        });
        itemTask.execute(alarmId);
    }
    protected static void cancelPowerOffAlarm(final Context context, long alarmId, @Nullable final AlarmDatabaseAdapter.AlarmItemTaskListener onFinished)
    {
        AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(context);
        itemTask.addAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener() {
            @Override
            public void onFinished(Boolean result, AlarmClockItem alarm) {
                cancelPowerOffAlarm(context, alarm);
                if (onFinished != null) {
                    onFinished.onFinished(result, alarm);
                }
            }
        });
        itemTask.execute(alarmId);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    protected static void setPowerOffAlarm(Context context, @NonNull AlarmClockItem alarm) {
        Log.d(TAG, "setPowerOffAlarm: " + alarm.rowID + " at " + alarm.alarmtime);
        context.sendBroadcast(AlarmSettings.getPowerOffAlarmIntent(context, AlarmSettings.PowerOffAlarmInfo.ACTION_SET, alarm.alarmtime));
    }
    protected static void cancelPowerOffAlarm(Context context, @NonNull AlarmClockItem alarm)
    {
        Log.d(TAG, "cancelPowerOffAlarm: " + alarm.rowID + " at " + alarm.alarmtime);
        context.sendBroadcast(AlarmSettings.getPowerOffAlarmIntent(context, AlarmSettings.PowerOffAlarmInfo.ACTION_CANCEL, alarm.alarmtime));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static Intent getServiceIntent(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        if (Build.VERSION.SDK_INT >= 16) {
            intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        }
        return intent;
    }

    public static Intent getFullscreenIntent(Context context, Uri data)
    {
        Intent intent = new Intent(context, AlarmDismissActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(data);
        intent.putExtra(EXTRA_NOTIFICATION_ID, (int)ContentUris.parseId(data));
        return intent;
    }

    public static Intent getFullscreenBroadcast(Uri data)
    {
        Intent intent = new Intent(ACTION_UPDATE_UI);
        intent.setPackage(BuildConfig.APPLICATION_ID);
        intent.setData(data);
        if (data != null) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return intent;
    }

    public static Intent getBedtimeBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(BuildConfig.APPLICATION_ID);
        return intent;
    }
    public static Intent getManageBedtimeIntent(Context context)
    {
        Intent intent = new Intent(context, BedtimeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getSuntimesIntent(Context context) {
        Intent intent = new Intent(context, SuntimesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static IntentFilter getUpdateBroadcastIntentFilter() {
        return getUpdateBroadcastIntentFilter(true);
    }
    public static IntentFilter getUpdateBroadcastIntentFilter(boolean withData)
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_UI);
        if (withData) {
            filter.addDataScheme("content");
        }
        return filter;
    }

    public static Intent getAlarmListIntent(Context context, Long selectedAlarmId)
    {
        Intent intent = new Intent(context, AlarmClockActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (selectedAlarmId != null) {
            intent.setData(ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, selectedAlarmId));
            intent.putExtra(AlarmClockActivity.EXTRA_SELECTED_ALARM, selectedAlarmId);
        }
        return intent;
    }

    public static Intent getAlarmIntent(Context context, String action, Uri data)
    {
        Intent intent = new Intent(context, AlarmNotifications.class);
        intent.setAction(action);
        intent.setData(data);
        if (Build.VERSION.SDK_INT >= 16) {
            intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);  // on my device (api19) the receiver fails to respond when app is closed unless this flag is set
        }
        intent.putExtra(EXTRA_NOTIFICATION_ID, (data != null ? (int)ContentUris.parseId(data) : null));
        return intent;
    }

    public static PendingIntent getPendingIntent(Context context, String action, Uri data)
    {
        Intent intent = getAlarmIntent(context, action, data);
        return PendingIntent.getBroadcast(context, (int)ContentUris.parseId(data), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Start playing sound / vibration for given alarm.
     */
    public static void startAlert(@NonNull final Context context, @NonNull AlarmClockItem alarm)
    {
        String channel = (alarm.type != null ? alarm.type.name() : AlarmClockItem.AlarmType.ALARM.name());
        MediaPlayer player = initPlayer(context, channel, false);
        if (isPlaying(channel)) {
            stopAlert(channel);
        }
        setIsPlaying(channel, true);

        boolean isAlarm = (alarm.type == AlarmClockItem.AlarmType.ALARM);
        boolean passesFilter = passesInterruptionFilter(context, alarm);
        if (!passesFilter) {
            Log.w(TAG, "startAlert: blocked by `Do Not Disturb`: " + alarm.rowID);
        }

        boolean isMuted = AlarmSettings.isChannelMuted(context, alarm.type);
        if (isMuted) {
            Log.w(TAG, "startAlert: blocked by Notification Channel (muted): " + alarm.rowID);
        }

        if (alarm.vibrate && passesFilter && !isMuted) {
            startVibration(context, alarm);
        }

        Uri soundUri = ((alarm.ringtoneURI != null && !alarm.ringtoneURI.isEmpty()) ? Uri.parse(alarm.ringtoneURI) : null);
        if (soundUri != null && passesFilter && !isMuted)
        {
            if (AlarmSettings.VALUE_RINGTONE_DEFAULT.equals(alarm.ringtoneURI)) {
                soundUri = AlarmSettings.getDefaultRingtoneUri(context, alarm.type, true);
            }

            if (!isValidSoundUri(soundUri)) {
                Log.w(TAG, "startAlert: rejecting sound uri: " + (soundUri != null ? soundUri.toString() : "null") + ".. replacing with default.");
                soundUri = RingtoneManager.getActualDefaultRingtoneUri(context, isAlarm ? RingtoneManager.TYPE_ALARM : RingtoneManager.TYPE_NOTIFICATION);

                if (!isValidSoundUri(soundUri)) {
                    Log.w(TAG, "startAlert: rejecting sound uri: " + (soundUri != null ? soundUri.toString() : "null") + ".. replacing with fallback.");
                    soundUri = AlarmSettings.getFallbackRingtoneUri(context, alarm.type);
                }
            }

            try {
                startAlert(context, player, soundUri, isAlarm);  // (0)

            } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException | NullPointerException e) {    // fallback to default
                Log.e(TAG, "startAlert: failed to play " + (soundUri != null ? soundUri.toString() : "null") + " ..(0) " + e);
                Uri defaultUri = RingtoneManager.getActualDefaultRingtoneUri(context, isAlarm ? RingtoneManager.TYPE_ALARM : RingtoneManager.TYPE_NOTIFICATION);
                try {
                    startAlert(context, player, defaultUri, isAlarm);  // (1)

                } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException | NullPointerException e1) {    // default failed too..
                    Log.e(TAG, "startAlert: failed to play " + (defaultUri != null ? defaultUri.toString() : "null") + " ..(1) " + e);
                    Uri fallbackUri = AlarmSettings.getFallbackRingtoneUri(context, alarm.type);
                    try {
                        startAlert(context, player, fallbackUri, isAlarm);  // (2)

                    } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException | NullPointerException e2) {
                        Log.e(TAG, "startAlert: failed to play " + fallbackUri.toString() + " ..(2) " + e);
                        Toast.makeText(context, context.getString(R.string.alarmAction_alertFailedMsg), Toast.LENGTH_SHORT).show();
                        setIsPlaying(channel, false);
                    }
                }
            }
        }

        if (alarm.hasActionID(AlarmClockItem.ACTIONID_MAIN))
        {
            SuntimesData data = getData(context, alarm);
            data.calculate(context);
            WidgetActions.startIntent(context.getApplicationContext(), 0, alarm.getActionID(AlarmClockItem.ACTIONID_MAIN), data, null, Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }

    protected static void startAlert(final Context context, @NonNull final MediaPlayer player, @NonNull final Uri soundUri, final boolean isAlarm) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException
    {
        if (soundUri == null) {
            throw new IOException("URI must not be null!");
        } else if (soundUri.toString().trim().isEmpty()) {
            throw new IOException("URI must not be empty!");
        } else if (!isValidSoundUri(soundUri)) {
            throw new IOException("URI is not valid! " + soundUri);
        }

        final long fadeInMillis = (isAlarm ? AlarmSettings.loadPrefAlarmFadeIn(context) : 0);
        final int streamType = (isAlarm ? AudioManager.STREAM_ALARM : AudioManager.STREAM_NOTIFICATION);
        player.setAudioStreamType(streamType);

        try {
            player.setDataSource(context, soundUri);
            if (BuildConfig.DEBUG)
            {
                player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra)
                    {
                        Log.e(TAG, "onError: MediaPlayer: " + what + ", " + extra);
                        return false;
                    }
                });
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d(TAG, "onCompletion: MediaPlayer");
                    }
                });
            }

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer)
                {
                    mediaPlayer.setLooping(isAlarm);
                    if (Build.VERSION.SDK_INT >= 16) {
                        mediaPlayer.setNextMediaPlayer(null);
                    }
                    if (audioManager != null) {
                        audioManager.requestAudioFocus(null, streamType, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    }

                    if (fadeInMillis > 0) {
                        startFadeIn(context, mediaPlayer, fadeInMillis);
                    } else player.setVolume(1, t_volume = 1);

                    mediaPlayer.start();
                    Log.i(TAG, "startAlert: playing " + soundUri);
                }
            });
            player.prepareAsync();

        } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException | NullPointerException e) {
            Log.e(TAG, "startAlert: failed to setDataSource! " + soundUri + " .. " + e);
            throw e;
        }
    }

    public static boolean isValidSoundUri(@Nullable Uri uri) {
        String scheme = (uri != null ? uri.getScheme() : null);
        return scheme != null
                && (scheme.equals(ContentResolver.SCHEME_CONTENT)              // content:/
                || scheme.equals(ContentResolver.SCHEME_FILE)                  // file:/
                || scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE));    // android.resource:/
    }

    protected static boolean isVibrating = false;
    private static Handler vibrationHandler;
    private static Runnable vibration;
    private static Runnable vibrate(final String channel, final long[] pattern, final int repeat)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                if (isPlaying(channel) && vibrator != null)
                {
                    isVibrating = true;
                    vibrator.vibrate(pattern, -1);   // manually loop vibration; this triggers a (re)start if vibration was stopped by screen-off.
                    if (isPlaying(channel) && repeat >= 0) {             // TODO: better workaround?
                        vibrationHandler.postDelayed(this, vibrationLength(pattern));
                    } else isVibrating = false;
                }
            }
        };
    }
    private static long vibrationLength(long[] pattern)
    {
        long length = 0;
        for (long duration : pattern) {
            length += duration;
        }
        return length;
    }
    protected static void startVibration(@NonNull final Context context, @NonNull final AlarmClockItem alarm)
    {
        if (vibrationHandler == null) {
            vibrationHandler = new Handler();
        }
        int repeatFrom = (alarm.type == AlarmClockItem.AlarmType.ALARM) ? 0 : -1;
        vibration = vibrate(alarm.type.name(), AlarmSettings.loadPrefVibratePattern(context, alarm.type), repeatFrom);
        vibrationHandler.post(vibration);
    }
    public static void stopVibration()
    {
        if (vibrator != null)
        {
            vibrator.cancel();
            if (vibrationHandler != null) {
                vibrationHandler.removeCallbacks(vibration);
            }
        }
    }

    public static int FADEIN_STEP_MILLIS = 25;
    protected static boolean isFadingIn = false;
    protected static float t_volume = 0;
    protected static Handler fadeHandler;

    private static Runnable fadeIn(@NonNull final Handler handler, @NonNull final MediaPlayer player, final long duration, final int method)
    {
        return new Runnable()
        {
            private final float oneOverDuration = 1f / duration;
            private Long startedAt = null;

            @Override
            public void run()
            {
                isFadingIn = true;
                if (startedAt == null) {
                    startedAt = System.currentTimeMillis();
                }

                float elapsed = (float) (System.currentTimeMillis() - startedAt);
                float x = Math.min(elapsed * oneOverDuration, 1f);    // x[0,1]
                float volume = f(x, method);                          // y[0,1]
                player.setVolume(volume, t_volume = volume);

                //Log.d("DEBUG", "fadeIn: " + elapsed + ":" + volume);
                if ((elapsed + FADEIN_STEP_MILLIS) <= duration) {
                    handler.postDelayed(this, FADEIN_STEP_MILLIS);
                } else {
                    isFadingIn = false;
                    player.setVolume(1f, t_volume = 1f);
                }
            }

            private float f(float x, int method) {
                switch (method) {
                    case AlarmSettings.FADE_HANDLER_CUBIC: return x*x*x;
                    case AlarmSettings.FADE_HANDLER_LINEAR:
                    default: return x;   // y = x
                }
            }
        };
    }

    @NonNull
    private static void startFadeIn(Context context, @Nullable MediaPlayer player, final long duration)
    {
        if (player != null)
        {
            int method = AlarmSettings.loadPrefAlarmFadeInMethod(context);

            if (Build.VERSION.SDK_INT >= 26)
            {
                VolumeShaper.Configuration fadeInConfig = getFadeInVolumeShaperConfig(context, duration, method);
                if (fadeInConfig != null)
                {
                    VolumeShaper fadeInVolume = player.createVolumeShaper(fadeInConfig);    // TODO: VolumeShaper sometimes jumps to full volume for no apparent reason...
                    fadeInVolume.apply(VolumeShaper.Operation.PLAY);
                    return;    // else fall-through to legacy fadeHandler
                }
            }

            player.setVolume(0, t_volume = 0);
            if (fadeHandler == null) {
                fadeHandler = new Handler();
            }
            fadeHandler.postDelayed(fadeIn(fadeHandler, player, duration, method), FADEIN_STEP_MILLIS);

        } else {
            Log.w(TAG, "startFadeIn: null MediaPlayer!");
        }
    }

    @Nullable
    @TargetApi(26)
    public static VolumeShaper.Configuration getFadeInVolumeShaperConfig(Context context, long duration, int method)
    {
        switch (method)
        {
            case AlarmSettings.FADE_VSHAPER_LINEAR:
                return new VolumeShaper.Configuration.Builder(VolumeShaper.Configuration.LINEAR_RAMP).setDuration(duration).build();

            case AlarmSettings.FADE_VSHAPER_SCURVE:
                return new VolumeShaper.Configuration.Builder(VolumeShaper.Configuration.SCURVE_RAMP).setDuration(duration).build();

            case AlarmSettings.FADE_VSHAPER_CUBIC:
                return new VolumeShaper.Configuration.Builder(VolumeShaper.Configuration.CUBIC_RAMP).setDuration(duration).build();

            case AlarmSettings.FADE_HANDLER_LINEAR:
            case AlarmSettings.FADE_HANDLER_CUBIC:
            default:
                return null;
        }
    }

    /**
     * Stop playing sound / vibration.
     */
    public static void stopAlert() {
        stopAlert(true);
    }
    public static void stopAlert(boolean stopVibrate)
    {
        if (stopVibrate) {
            stopVibration();
        }
        for (MediaPlayer player : players.values())
        {
            if (player != null) {
                stopSound(player);
            }
        }
    }

    public static void stopAlert(String channel) {
        stopAlert(channel, true);
    }
    public static void stopAlert(String channel, boolean stopVibrate)
    {
        if (stopVibrate) {
            stopVibration();
        }
        stopSound(players.get(channel));
        setIsPlaying(channel, false);
    }

    public static void stopSound(MediaPlayer player)
    {
        if (player != null)
        {
            player.stop();
            if (audioManager != null) {
                audioManager.abandonAudioFocus(null);
            }
            player.reset();
        }
    }

    protected static boolean passesInterruptionFilter(Context context, @NonNull AlarmClockItem item)
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int filter = (notificationManager != null) ? notificationManager.getCurrentInterruptionFilter()
                                                       : NotificationManager.INTERRUPTION_FILTER_UNKNOWN;
            switch (filter)
            {
                case NotificationManager.INTERRUPTION_FILTER_ALARMS:        // (4) alarms only
                    return (item.type == AlarmClockItem.AlarmType.ALARM);

                case NotificationManager.INTERRUPTION_FILTER_NONE:          // (3) suppress all
                    return false;

                case NotificationManager.INTERRUPTION_FILTER_PRIORITY:      // (2) allow priority
                    if (Build.VERSION.SDK_INT >= 28) {
                        return (item.type == AlarmClockItem.AlarmType.ALARM) &&
                                (isCategorySet(getNotificationPolicy(notificationManager), NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS));
                    } else {
                        return (item.type == AlarmClockItem.AlarmType.ALARM);
                    }

                case NotificationManager.INTERRUPTION_FILTER_ALL:           // (1) allow all
                case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:       // (0) unknown
                default:
                    return true;
            }

        } else if (Build.VERSION.SDK_INT >= 21) {
            try {
                int zenMode = Settings.Global.getInt(context.getContentResolver(), "zen_mode");
                switch (zenMode)
                {
                    case 2: // Settings.Global.ZEN_MODE_NO_INTERRUPTIONS:
                        return false;

                    case 3: // Settings.Global.ZEN_MODE_ALARMS:
                    case 1: // Settings.Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS:
                        return (item.type == AlarmClockItem.AlarmType.ALARM);

                    case 0: // Settings.Global.ZEN_MODE_OFF:
                    default:
                        return true;
                }

            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "interruptionFilter: Setting Not Found: zen_mode .. " + e);
                return true;
            }

        } else return true;
    }

    @TargetApi(23)
    @Nullable
    private static NotificationManager.Policy getNotificationPolicy(NotificationManager notificationManager)
    {
        if (notificationManager != null)
        {
            try {
                NotificationManager.Policy policy = notificationManager.getNotificationPolicy();    // does getting the policy require a permission? conflicting documentation..
                Log.d(TAG, "getNotificationPolicy: " + policy);
                return policy;

            } catch (SecurityException e) {
                Log.e(TAG, "getNotificationPolicy: Access Denied.. " + e);
                return null;
            }
        } else return null;
    }

    @TargetApi(23)
    private static boolean isCategorySet(@Nullable NotificationManager.Policy policy, int category) {
        return (policy != null && ((policy.priorityCategories & category) != 0));
    }
    //private static final int PRIORITY_CATEGORY_ALARMS = 1 << 5;

    protected static final HashMap<String, Boolean> isPlaying = new HashMap<>();
    protected static final HashMap<String, MediaPlayer> players = new HashMap<>();
    protected static Vibrator vibrator = null;
    protected static AudioManager audioManager;
    protected static int t_player_error = 0, t_player_error_extra = 0;

    @NonNull
    protected static MediaPlayer initPlayer(final Context context, final String channel, @SuppressWarnings("SameParameterValue") boolean reinit)
    {
        if (vibrator == null || reinit) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (audioManager == null || reinit) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        MediaPlayer player = players.get(channel);
        if (player == null || reinit)
        {
            player = new MediaPlayer();
            players.put(channel, player);

            player.setOnErrorListener(new MediaPlayer.OnErrorListener()
            {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
                {
                    t_player_error = what;
                    t_player_error_extra = extra;
                    Log.e(TAG, "onError: MediaPlayer error " + what + " (" + extra + ")");
                    return false;
                }
            });

            player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener()
            {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer)
                {
                    if (!mediaPlayer.isLooping()) {                // some sounds (mostly ringtones) have a built-in loop - they repeat despite !isLooping!
                        stopAlert(channel);                            // so manually stop them after playing once
                    }
                }
            });
        }
        return player;
    }

    /**
     * isPlaying
     */
    protected static boolean isPlaying() {
        return isPlaying.containsValue(true);
    }
    protected static boolean isPlaying(String channel)
    {
        Boolean value = isPlaying.get(channel);
        return (value != null ? value : false);
    }
    protected static void setIsPlaying(String channel, boolean value) {
        isPlaying.put(channel, value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getNotificationChannelID(@Nullable AlarmClockItem.AlarmType type)
    {
        if (type == null) {
            return CHANNEL_ID_MISC;
        }
        switch (type)
        {
            case ALARM: return CHANNEL_ID_ALARMS;
            case NOTIFICATION1: return CHANNEL_ID_NOTIFICATIONS1;
            case NOTIFICATION2:
            case NOTIFICATION:
            default: return CHANNEL_ID_NOTIFICATIONS0;
        }
    }

    /**
     * createNotificationChannel
     * @param type AlarmType
     * @return channelID
     */
    @TargetApi(26)
    public static String createNotificationChannel(Context context, @Nullable AlarmClockItem.AlarmType type) {
        return createNotificationChannel(context, getNotificationChannelID(type));
    }
    @TargetApi(26)
    public static String createNotificationChannel(Context context, @Nullable String channelID)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            int importance;
            String title, desc;
            if (channelID == null)
            {
                channelID = CHANNEL_ID_MISC;
                title = context.getString(R.string.notificationChannel_misc_title);
                desc = context.getString(R.string.notificationChannel_misc_desc);
                importance = NotificationManagerCompat.IMPORTANCE_LOW;

            } else {
                switch (channelID)
                {
                    case CHANNEL_ID_BEDTIME:
                        title = context.getString(R.string.notificationChannel_bedtime_title);
                        desc = context.getString(R.string.notificationChannel_bedtime_desc);
                        importance = NotificationManagerCompat.IMPORTANCE_MAX;
                        break;

                    case CHANNEL_ID_ALARMS:
                        title = context.getString(R.string.notificationChannel_alarms_title);
                        desc = context.getString(R.string.notificationChannel_alarms_desc);
                        importance = NotificationManagerCompat.IMPORTANCE_MAX;
                        break;

                    case CHANNEL_ID_NOTIFICATIONS1:
                        title = context.getString(R.string.notificationChannel_notifications1_title);
                        desc = context.getString(R.string.notificationChannel_notifications1_desc);
                        importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
                        break;

                    case CHANNEL_ID_NOTIFICATIONS0:
                    default:
                        title = context.getString(R.string.notificationChannel_notifications0_title);
                        desc = context.getString(R.string.notificationChannel_notifications0_desc);
                        importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
                        break;
                }
            }

            NotificationChannel channel = new NotificationChannel(channelID, title, importance);
            channel.setDescription(desc);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
            return channelID;
        }
        return "";
    }

    public static NotificationCompat.Builder createNotificationBuilder(Context context, @Nullable AlarmClockItem alarm) {
        return createNotificationBuilder(context, ((alarm != null) ? getNotificationChannelID(alarm.type) : null));
    }
    public static NotificationCompat.Builder createNotificationBuilder(Context context, @Nullable String channelID)
    {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new NotificationCompat.Builder(context, createNotificationChannel(context, channelID));
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    /**
     * createNotification
     * @param context Context
     * @param alarm AlarmClockItem
     * @return a Notification object (or null if a notification shouldn't be shown)
     */
    public static Notification createNotification(Context context, @NonNull AlarmClockItem alarm)
    {
        NotificationCompat.Builder builder = createNotificationBuilder(context, alarm);
        SuntimesData data = null;

        String eventString = alarm.getEvent();
        AlarmEvent.AlarmEventItem eventItem = new AlarmEvent.AlarmEventItem(eventString, context.getContentResolver());
        String eventDisplay = (eventString != null) ? eventItem.getTitle() : null;
        if (alarm.offset != 0) {
            eventDisplay = (eventString != null) ? formatOffsetMessage(context, alarm.offset, alarm.timestamp, eventItem)
                                                 : formatOffsetMessage(context, alarm.offset, alarm.timestamp);
        }
        String emptyLabel = ((eventDisplay != null) ? eventDisplay : context.getString(R.string.alarmOption_solarevent_none));

        String notificationTitle = (alarm.label == null || alarm.label.isEmpty() ? emptyLabel : alarm.label);
        String notificationMsg = (eventDisplay != null ? eventDisplay : "");
        if (alarm.note != null)
        {
            if (data == null) {
                data = getData(context, alarm);
                data.calculate(context);
            }
            notificationMsg += ((eventDisplay != null) ? "\n\n" : "") + alarm.note;
            notificationMsg = DataSubstitutions.displayStringForTitlePattern0(context, notificationMsg, data);
        }
        int notificationIcon = alarm.getIcon();

        builder.setDefaults( Notification.DEFAULT_LIGHTS );

        PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, alarm.hashCode(), getAlarmIntent(context, ACTION_DISMISS, alarm.getUri()), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingDismissWithChallenge = PendingIntent.getActivity(context, alarm.hashCode(), getFullscreenIntent(context, alarm.getUri()).setAction(ACTION_DISMISS), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingSnooze = PendingIntent.getBroadcast(context, (int)alarm.rowID, getAlarmIntent(context, ACTION_SNOOZE, alarm.getUri()), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent alarmFullscreen = PendingIntent.getActivity(context, (int)alarm.rowID, getFullscreenIntent(context, alarm.getUri()), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingView = PendingIntent.getActivity(context, alarm.hashCode(), getAlarmListIntent(context, alarm.rowID), PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarm.type == AlarmClockItem.AlarmType.ALARM)
        {
            // ALARM
            int alarmState = alarm.getState();
            switch (alarmState)
            {
                case AlarmState.STATE_TIMEOUT:
                    builder.setCategory( NotificationCompat.CATEGORY_REMINDER );
                    builder.setPriority( NotificationCompat.PRIORITY_HIGH );
                    notificationMsg = context.getString(R.string.alarmAction_timeoutMsg);
                    notificationIcon = R.drawable.ic_action_timeout;
                    builder.setColor(ContextCompat.getColor(context, R.color.alarm_notification_timeout));
                    builder.setFullScreenIntent(alarmFullscreen, true);       // at discretion of system to use this intent (or to show a heads up notification instead)
                    builder.setContentIntent(pendingDismiss);
                    builder.setAutoCancel(false);
                    builder.setOngoing(true);
                    break;

                case AlarmState.STATE_SCHEDULED_SOON:
                    //if (Build.VERSION.SDK_INT < 21)
                    //{
                        builder.setCategory( NotificationCompat.CATEGORY_REMINDER );
                        builder.setPriority( alarm.repeating ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT );
                        notificationMsg = notificationTitle + (!notificationMsg.isEmpty() ? "\n\n" + notificationMsg : "");
                        notificationTitle = context.getString(R.string.alarmAction_upcomingMsg);
                        builder.setWhen(alarm.alarmtime);
                        builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss_early), pendingDismiss);
                        builder.setAutoCancel(false);
                        builder.setOngoing(false);     // allow reminder to be swiped away

                        if (alarm.hasActionID(AlarmClockItem.ACTIONID_REMINDER))    // on-click reminder action
                        {
                            if (data == null) {
                                data = getData(context, alarm);
                                data.calculate(context);
                            }
                            String reminderActionID = alarm.getActionID(AlarmClockItem.ACTIONID_REMINDER);
                            Intent reminderIntent = WidgetActions.createIntent(context, 0, reminderActionID, data, null);
                            if (reminderIntent != null)
                            {
                                String actionTitle = WidgetActions.loadActionLaunchPref(context, 0, reminderActionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
                                builder.addAction(R.drawable.ic_action_extension, actionTitle, PendingIntent.getActivity(context, alarm.hashCode(), reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                            }
                        }
                        builder.setContentIntent(pendingView);
                    //} else return null;  // don't show reminder for api 21+ (uses notification provided by setAlarm instead)
                    break;

                case AlarmState.STATE_SNOOZING:
                    builder.setCategory( NotificationCompat.CATEGORY_ALARM );
                    builder.setPriority( NotificationCompat.PRIORITY_MAX );
                    SuntimesUtils.initDisplayStrings(context);
                    SuntimesUtils.TimeDisplayText snoozeText = utils.timeDeltaLongDisplayString(System.currentTimeMillis()-5000, alarm.alarmtime);
                    notificationMsg = context.getString(R.string.alarmAction_snoozeMsg, snoozeText.getValue());
                    notificationIcon = R.drawable.ic_action_snooze;
                    builder.setColor(ContextCompat.getColor(context, R.color.alarm_notification_snoozing));
                    builder.setFullScreenIntent(alarmFullscreen, true);       // at discretion of system to use this intent (or to show a heads up notification instead)
                    builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), alarm.hasDismissChallenge(context) ? pendingDismissWithChallenge : pendingDismiss);
                    if (Build.VERSION.SDK_INT < 16) {
                        builder.setContentIntent(alarm.hasDismissChallenge(context) ? pendingDismissWithChallenge : pendingDismiss);    // action buttons require expanded notifications (api 16+)
                    }
                    builder.setAutoCancel(false);
                    builder.setOngoing(true);
                    break;

                case AlarmState.STATE_SOUNDING:
                    builder.setCategory( NotificationCompat.CATEGORY_ALARM );
                    builder.setPriority( NotificationCompat.PRIORITY_MAX );
                    builder.addAction(R.drawable.ic_action_snooze, context.getString(R.string.alarmAction_snooze), pendingSnooze);
                    builder.setProgress(0,0,true);
                    builder.setColor(ContextCompat.getColor(context, R.color.alarm_notification_sounding));
                    builder.setFullScreenIntent(alarmFullscreen, true);       // at discretion of system to use this intent (or to show a heads up notification instead)
                    builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), alarm.hasDismissChallenge(context) ? pendingDismissWithChallenge : pendingDismiss);
                    if (Build.VERSION.SDK_INT < 16) {
                        builder.setContentIntent(alarm.hasDismissChallenge(context) ? pendingDismissWithChallenge : pendingDismiss);    // action buttons require expanded notifications (api 16+)
                    }
                    builder.setAutoCancel(false);
                    builder.setOngoing(true);
                    break;

                default:
                    Log.w(TAG, "createNotification: unhandled state: " + alarmState);
                    builder.setCategory( NotificationCompat.CATEGORY_RECOMMENDATION );
                    builder.setPriority( NotificationCompat.PRIORITY_MIN );
                    builder.setAutoCancel(true);
                    builder.setOngoing(false);
                    break;
            }

        } /*else if (alarm.type == AlarmClockItem.AlarmType.NOTIFICATION2) {
            // NOTIFICATION (persistent reminder)
            PendingIntent pendingView1 = PendingIntent.getActivity(context, alarm.hashCode(), getSuntimesIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingDisable = PendingIntent.getBroadcast(context, alarm.hashCode(), getAlarmIntent(context, ACTION_DISABLE, alarm.getUri()), PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingAction = null;
            if (alarm.hasActionID(AlarmClockItem.ACTIONID_MAIN))
            {
                if (data == null) {
                    data = getData(context, alarm);
                    data.calculate();
                }
                Intent actionIntent = WidgetActions.createIntent(context.getApplicationContext(), 0, alarm.getActionID(AlarmClockItem.ACTIONID_MAIN), data, null);
                pendingAction = (actionIntent != null ? PendingIntent.getBroadcast(context, alarm.hashCode(), actionIntent, PendingIntent.FLAG_UPDATE_CURRENT) : null);
            }
            //notificationMsg = "TODO";   // TODO: reminder notification text

            builder.setWhen(alarm.alarmtime);
            builder.setCategory( NotificationCompat.CATEGORY_REMINDER );
            builder.setPriority( NotificationCompat.PRIORITY_HIGH );
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            builder.setContentIntent((pendingAction != null) ? pendingAction : pendingView1);
            builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), pendingDisable);

        }*/ else {
            // NOTIFICATION
            builder.setCategory( NotificationCompat.CATEGORY_REMINDER );
            builder.setPriority( NotificationCompat.PRIORITY_HIGH );
            builder.setOngoing(false);
            builder.setAutoCancel(true);
            builder.setDeleteIntent(pendingDismiss);
            builder.setContentIntent(pendingDismiss);
        }

        builder.setContentTitle(notificationTitle)
                .setContentText(notificationMsg)
                .setSmallIcon(notificationIcon)
                .setVisibility( NotificationCompat.VISIBILITY_PUBLIC );
        builder.setOnlyAlertOnce(false);

        if (notificationMsg.contains("\n"))    // message is more than one line; show "BigTextStyle" notification
        {
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
            style.setBigContentTitle(notificationTitle);
            style.bigText(notificationMsg);
            builder.setStyle(style);
        }

        return builder.build();
    }

    public static Notification createBedtimeModeNotification(Context context)
    {
        NotificationCompat.Builder builder = createNotificationBuilder(context, CHANNEL_ID_BEDTIME);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setCategory(NotificationCompat.CATEGORY_ALARM);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setContentTitle(context.getString(R.string.configLabel_bedtime));
        builder.setSmallIcon(R.drawable.ic_action_bedtime_light);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setOnlyAlertOnce(false);

        boolean isPaused = BedtimeSettings.isBedtimeModePaused(context);
        builder.setContentText(context.getString(isPaused ? R.string.msg_bedtime_paused : R.string.msg_bedtime_active));
        builder.setContentIntent(PendingIntent.getActivity(context, builder.hashCode(), getManageBedtimeIntent(context), PendingIntent.FLAG_UPDATE_CURRENT));

        if (isPaused) {
            PendingIntent pendingResume = PendingIntent.getBroadcast(context, 0, getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_RESUME), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_action_bedtime, context.getString(R.string.configAction_resumeBedtime), pendingResume);
        } else {
            PendingIntent pendingPause = PendingIntent.getBroadcast(context, 0, getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_action_pause, context.getString(R.string.configAction_pauseBedtime), pendingPause);
        }

        PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, 0, getBedtimeBroadcast(AlarmNotifications.ACTION_BEDTIME_DISMISS), 0);
        builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.configAction_dismissBedtime), pendingDismiss);
        return builder.build();
    }

    public static Notification createProgressNotification(Context context) {
        return createProgressNotification(context, null, context.getString(R.string.configLabel_alarms_background_action_message, context.getString(R.string.app_name_alarmclock)));
    }
    public static Notification createProgressNotification(Context context, String message) {
        return createProgressNotification(context, context.getString(R.string.app_name_alarmclock),  message);
    }

    public static Notification createProgressNotification(Context context, @Nullable String title, @NonNull String message)
    {
        NotificationCompat.Builder builder = createNotificationBuilder(context, CHANNEL_ID_MISC);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_PROGRESS);
        builder.setProgress(0,0,true);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        if (title != null) {
            builder.setContentTitle(title);
        }
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.ic_action_alarms_light);
        //builder.setColor(ContextCompat.getColor(context, R.color.sunIcon_color_setting_dark))
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setOnlyAlertOnce(false);

        PendingIntent pendingView = PendingIntent.getActivity(context, message.hashCode(), getAlarmListIntent(context, null), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_action_settings, context.getString(R.string.app_name_alarmclock), pendingView);
        return builder.build();
    }

    public static NotificationCompat.Builder warningNotificationBuilder(Context context)
    {
        NotificationCompat.Builder builder = createNotificationBuilder(context, CHANNEL_ID_MISC);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        builder.setAutoCancel(true);
        builder.setOngoing(false);
        builder.setOnlyAlertOnce(true);
        builder.setContentTitle(context.getString(R.string.app_name_alarmclock));
        builder.setSmallIcon(R.drawable.ic_action_warning);
        builder.setColor(ContextCompat.getColor(context, R.color.alarm_notification_warning));
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return builder;
    }

    public static Notification createWarningNotification(Context context, String message)
    {
        NotificationCompat.Builder builder = warningNotificationBuilder(context);
        builder.setContentText(message);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setBigContentTitle(context.getString(R.string.app_name_alarmclock));
        style.bigText(message);
        builder.setStyle(style);

        return builder.build();
    }

    public static Notification createAutostartWarningNotification(Context context)
    {
        NotificationCompat.Builder builder = warningNotificationBuilder(context);
        String message = context.getString(R.string.autostartWarning).replaceAll("\\[w\\]", "").trim();
        builder.setContentText(message);

        Intent intent = AlarmSettings.getAutostartSettingsIntent(context);
        PendingIntent pendingView = PendingIntent.getActivity(context, builder.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingView);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setBigContentTitle(context.getString(R.string.app_name_alarmclock));
        style.bigText(message);
        builder.setStyle(style);

        return builder.build();
    }

    public static Notification createBatteryOptWarningNotification(Context context)
    {
        NotificationCompat.Builder builder = warningNotificationBuilder(context);
        String message = context.getString(AlarmSettings.aggressiveBatteryOptimizations(context) ? R.string.configLabel_alarms_optWhiteList_unlisted_aggressive  : R.string.configLabel_alarms_optWhiteList_unlisted)
                + "\n\n" + context.getString(R.string.help_battery_optimization, context.getString(R.string.app_name));
        builder.setContentText(message);

        Intent intent = AlarmSettings.getRequestIgnoreBatteryOptimizationSettingsIntent(context);
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
            intent = AlarmSettings.getRequestIgnoreBatteryOptimizationIntent(context);
        }

        PendingIntent pendingView = PendingIntent.getActivity(context, builder.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingView);
        //builder.addAction(R.drawable.ic_action_settings, context.getString(R.string.configLabel_alarms_optWhiteList), pendingView);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setBigContentTitle(context.getString(R.string.app_name_alarmclock));
        style.bigText(message);
        builder.setStyle(style);

        return builder.build();
    }

    public static String formatOffsetMessage(Context context, long offset, long timestamp, @NonNull AlarmEvent.AlarmEventItem event)
    {
        String eventString = event.getEventID();
        if (eventString != null)
        {
            SolarEvents solarEvent = event.getEvent();
            if (solarEvent != null) {
                return formatOffsetMessage(context, offset, solarEvent);

            } else {
                AlarmEvent.AlarmEventPhrase phrase = event.getPhrase(context);
                String eventText = (phrase != null ? phrase.getNoun() : event.getTitle());
                String offsetText = utils.timeDeltaLongDisplayString(0, offset).getValue();

                if (Build.VERSION.SDK_INT >= 24)
                {
                    String gender = (phrase != null ? phrase.getGender() : "other");
                    int quantity = (phrase != null ? phrase.getQuantity() : 1);
                    return formatOffsetMessage(context, offset, offsetText, eventText, quantity, gender);
                } else return formatOffsetMessage(context, offset, offsetText, eventText);
            }
        } else {
            return formatOffsetMessage(context, offset, timestamp);
        }
    }

    public static String formatOffsetMessage(Context context, long offset, @NonNull SolarEvents event)
    {
        int i = event.ordinal();
        String[] eventStrings = context.getResources().getStringArray(R.array.solarevents_long1);
        String eventText = (i >= 0 && i <eventStrings.length) ? eventStrings[i] : event.name();
        String offsetText = utils.timeDeltaLongDisplayString(0, offset).getValue();

        if (Build.VERSION.SDK_INT >= 24) {    // uses SelectFormat so translations match quantity and gender
            int[] eventPlurals = context.getResources().getIntArray(R.array.solarevents_quantity);
            String[] eventGenders = context.getResources().getStringArray(R.array.solarevents_gender);
            int plural = (i >= 0 && i <eventPlurals.length) ? eventPlurals[i] : 1;
            String gender = (i >= 0 && i <eventGenders.length) ? eventGenders[i] : "other";
            return formatOffsetMessage(context, offset, offsetText, eventText, plural, gender);
        } else return formatOffsetMessage(context, offset, offsetText, eventText);
    }
    public static String formatOffsetMessage(Context context, long offset, long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        String eventText = utils.calendarTimeShortDisplayString(context, calendar).toString();
        String offsetText = utils.timeDeltaLongDisplayString(0, offset).getValue();

        if (Build.VERSION.SDK_INT >= 24) {    // uses SelectFormat so translations match quantity and gender
            int plural = SuntimesUtils.is24() ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR);
            String gender = context.getString(R.string.time_gender);
            return formatOffsetMessage(context, offset, offsetText, eventText, plural, gender);
        } else return formatOffsetMessage(context, offset, offsetText, eventText);
    }
    public static String formatOffsetMessage(Context context, long offset, String offsetText, String eventText) {
        return context.getString(((offset < 0) ? R.string.offset_before_msg : R.string.offset_after_msg), offsetText, eventText);
    }
    @TargetApi(24)
    public static String formatOffsetMessage(Context context, long offset, String offsetText, String eventText, int plural, String gender )
    {
        String pattern = context.getString(((offset < 0) ? R.string.offset_before_msg1 : R.string.offset_after_msg1));
        //Log.d("DEBUG", "formatOffsetMessage: " + plural + " : " + gender + "\n" + pattern);
        return new MessageFormat(pattern).format( new Object[] {offsetText, plural, gender, eventText} );
    }

    /**
     * showNotification
     * Use this method to display the notification without a foreground service.
     * @param quiet false call startAlert after showing notification
     * @see NotificationService to display a notification that lives longer than the receiver.
     */
    public static void showNotification(Context context, @NonNull AlarmClockItem item, boolean quiet)
    {
        showNotification(context, createNotification(context, item), (int)item.rowID);
        if (!quiet) {
            startAlert(context, item);
        }
    }
    public static void showNotification(Context context, @Nullable Notification notification, int notificationID)
    {
        if (notification != null) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(ALARM_NOTIFICATION_TAG, notificationID, notification);
            Log.d("DEBUG", "showNotification: " + notificationID);
        }
    }
    public static void dismissNotification(Context context, int notificationID)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(ALARM_NOTIFICATION_TAG, notificationID);
        Log.d("DEBUG", "dismissNotification: " + notificationID);
    }
    public static void dismissNotifications(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
        Log.d("DEBUG", "dismissNotification: ALL");
    }

    /**
     * ForegroundNotifications
     */
    public static final class ForegroundNotifications
    {
        protected int notificationID = 0;
        protected Notification notification = null;
        protected WeakReference<Service> serviceRef;

        public ForegroundNotifications(Service service) {
            serviceRef = new WeakReference<>(service);
        }

        public void startForeground(int id, Notification notification)
        {
            this.notificationID = id;
            this.notification = notification;

            Service service = serviceRef.get();
            if (service != null) {
                service.startForeground(id, notification);
            }
        }
        public void stopForeground( boolean removeNotification )
        {
            this.notificationID = 0;
            this.notification = null;

            Service service = serviceRef.get();
            if (service != null) {
                service.stopForeground(removeNotification);
            }
        }
        public void restartForeground()
        {
            Service service = serviceRef.get();
            if (service != null && notification != null && notificationID != 0) {
                service.startForeground(notificationID, notification);
            }
        }

        public void stopSelf() {
            stopSelf(null);
        }
        public void stopSelf(@Nullable Integer startId)
        {
            if (notification == null || notificationID == NOTIFICATION_SERVICE_IS_ACTIVE_ID)
            {
                if (notificationID == NOTIFICATION_SERVICE_IS_ACTIVE_ID) {
                    Log.i(TAG, "stopSelf: dismissing foreground notification");
                    stopForeground(true);
                }

                Service service = serviceRef.get();
                if (service != null) {
                    Log.i(TAG, "stopSelf: stopping service");
                    t_hasCalledStopSelf = true;
                    if (startId != null)
                        service.stopSelf(startId);
                    else service.stopSelf();
                }
            } else Log.w(TAG, "stopSelf: skipping due to active foreground notification");
        }
        protected static boolean t_hasCalledStopSelf = false;   // used by test framework

        public void showNotification(Context context, @NonNull AlarmClockItem item, boolean quiet) {
            AlarmNotifications.showNotification(context, item, quiet);
        }
        public void showNotification(Context context, @NonNull Notification notification, int notificationID) {
            AlarmNotifications.showNotification(context, notification, notificationID);
        }
        public void dismissNotification(Context context, int notificationID)
        {
            AlarmNotifications.dismissNotification(context, notificationID);
            if (this.notificationID == notificationID) {
                stopForeground(true);
            }
        }

        public void dismissNotifications(Context context)
        {
            AlarmNotifications.dismissNotifications(context);
            if (notification != null) {
                stopForeground(true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * NotificationService
     */
    public static class NotificationService extends Service
    {
        public static final String TAG = "AlarmReceiverService";

        protected final ForegroundNotifications notifications = new ForegroundNotifications(this);

        @Override
        public int onStartCommand(final Intent intent, int flags, final int startId)
        {
            super.onStartCommand(intent, flags, startId);

            if (Build.VERSION.SDK_INT >= 26) {    // api26+ we have 5s to call startForeground; show a generic progress notification while the service is active
                notifications.startForeground(NOTIFICATION_SERVICE_IS_ACTIVE_ID, createProgressNotification(NotificationService.this));
            }

            if (intent != null)
            {
                String action = intent.getAction();
                Uri data = intent.getData();
                if (data != null)
                {
                    if (AlarmNotifications.ACTION_SHOW.equals(action))
                    {
                        Log.d(TAG, "ACTION_SHOW");

                    } else if (AlarmNotifications.ACTION_DISMISS.equals(action)) {
                        Log.d(TAG, "ACTION_DISMISS: " + data);
                        AlarmNotifications.stopAlert();

                    } else if (AlarmNotifications.ACTION_DISABLE.equals(action)) {
                        Log.d(TAG, "ACTION_DISABLE: " + data);
                        AlarmNotifications.stopAlert();

                    } else if (AlarmNotifications.ACTION_SILENT.equals(action)) {
                        Log.d(TAG, "ACTION_SILENT: " + data);
                        AlarmNotifications.stopAlert(false);
                        showAlarmSilencedToast(getApplicationContext());

                    } else if (AlarmNotifications.ACTION_SNOOZE.equals(action)) {
                        Log.d(TAG, "ACTION_SNOOZE: " + data + " :: " + intent.getIntExtra(AlarmClockActivity.EXTRA_ALARM_SNOOZE_DURATION, -1));
                        AlarmNotifications.stopAlert();

                    } else if (AlarmNotifications.ACTION_TIMEOUT.equals(action)) {
                        Log.d(TAG, "ACTION_TIMEOUT: " + data);
                        AlarmNotifications.stopAlert();

                    } else if (AlarmNotifications.ACTION_DELETE.equals(action)) {
                        Log.d(TAG, "ACTION_DELETE: " + data);
                        AlarmNotifications.stopAlert();

                    } else {
                        Log.w(TAG, "onStartCommand: Unhandled action: " + action);
                    }

                    AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(getApplicationContext());
                    itemTask.addAlarmItemTaskListener(createAlarmOnReceiveListener(getApplicationContext(), startId, action, intent.getExtras()));
                    itemTask.execute(ContentUris.parseId(data));

                } else {
                    if (AlarmNotifications.ACTION_LOCKED_BOOT_COMPLETED.equals(action))
                    {
                        Log.d(TAG, "ACTION_LOCKED_BOOT_COMPLETED received");
                        onLockedBootCompleted(getContext());
                        notifications.stopSelf(startId);

                    } else if (AlarmNotifications.ACTION_AFTER_BOOT_COMPLETED.equals(action) || AlarmNotifications.ACTION_SCHEDULE.equals(action)) {
                        Log.d(TAG, action + ": schedule all (prevCompleted=" + AlarmSettings.bootCompletedWasRun(getApplicationContext()) + ")");
                        onAfterBootCompleted(getApplicationContext(), startId);

                    } else if (AlarmNotifications.ACTION_LOCATION_CHANGED.equals(action)) {
                        Log.d(TAG, "ACTION_LOCATION_CHANGED received");
                        notifications.startForeground(NOTIFICATION_SCHEDULE_ALL_ID, createProgressNotification(getApplicationContext(), getString(R.string.app_name_alarmclock), getString(R.string.configLabel_alarms_bootcompleted_action_message)));
                        findAppLocationAlarms(getApplicationContext(), rescheduleTaskListener(startId, null));

                    } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                        Log.d(TAG, "TIMEZONE_CHANGED received");
                        boolean rescheduling = false;
                        TimeZone tz = TimeZone.getDefault();
                        String tzID = tz.getID();
                        String tzID_prev = AlarmSettings.loadSystemTimeZoneID(getApplicationContext());
                        if (!tzID.equals(tzID_prev))
                        {
                            Log.i(TAG, "system tz ID changed from " + tzID_prev + " to " + tzID);
                            long tzOffset = tz.getOffset(System.currentTimeMillis());
                            long tzOffset_prev = AlarmSettings.loadSystemTimeZoneOffset(getApplicationContext());
                            if (tzOffset != tzOffset_prev)
                            {
                                Log.i(TAG, "system tz offset changed from " + tzOffset_prev + " to " + tzOffset);
                                notifications.startForeground(NOTIFICATION_SCHEDULE_ALL_ID, createProgressNotification(getApplicationContext(), getString(R.string.app_name_alarmclock), getString(R.string.configLabel_alarms_bootcompleted_action_message)));
                                findEnabledAlarms(getApplicationContext(), rescheduleTaskListener_clocktime(startId));
                                rescheduling = true;
                            }
                            AlarmSettings.saveSystemTimeZoneInfo(getApplicationContext(), tzID, tzOffset);
                        }
                        if (!rescheduling) {
                            notifications.stopSelf(startId);
                        }

                    } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                        Log.d(TAG, "TIME_CHANGED received");
                        notifications.stopSelf(startId);
                        // TODO: reschedule alarms (but only when deltaT is >reminderPeriod to avoid rescheduling alarms dismissed early)

                    } else if (ACTION_BEDTIME.equals(action)) {
                        Log.d(TAG, "ACTION_BEDTIME");
                        triggerBedtimeMode(getContext(), true);
                        notifications.stopSelf(startId);

                    } else if (ACTION_BEDTIME_PAUSE.equals(action)) {
                        Log.d(TAG, "ACTION_BEDTIME_PAUSE");
                        pauseBedtimeMode(getContext());
                        notifications.stopSelf(startId);

                    } else if (ACTION_BEDTIME_RESUME.equals(action)) {
                        Log.d(TAG, "ACTION_BEDTIME_RESUME");
                        resumeBedtimeMode(getContext());
                        notifications.stopSelf(startId);

                    } else if (ACTION_BEDTIME_DISMISS.equals(action)) {
                        Log.d(TAG, "ACTION_BEDTIME_DISMISS");
                        triggerBedtimeMode(getContext(), false);
                        notifications.stopSelf(startId);

                    } else if (AlarmNotifications.ACTION_DELETE.equals(action)) {
                        Log.d(TAG, "ACTION_DELETE: clear all");
                        AlarmNotifications.stopAlert();
                        if (AlarmSettings.loadPrefPowerOffAlarms(getApplicationContext()))
                        {
                            findUpcomingAlarm(getApplicationContext(), false, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                                @Override
                                public void onItemsLoaded(Long[] ids) {
                                    cancelPowerOffAlarm(getApplicationContext(), ids[0], new AlarmDatabaseAdapter.AlarmItemTaskListener() {
                                        @Override
                                        public void onFinished(Boolean result, AlarmClockItem item) {
                                            AlarmDatabaseAdapter.AlarmListTask clearTask = new AlarmDatabaseAdapter.AlarmListTask(getApplicationContext());
                                            clearTask.setAlarmItemTaskListener(clearTaskListener);
                                            clearTask.execute();
                                        }
                                    });
                                }
                            });
                        } else {
                            AlarmDatabaseAdapter.AlarmListTask clearTask = new AlarmDatabaseAdapter.AlarmListTask(getApplicationContext());
                            clearTask.setAlarmItemTaskListener(clearTaskListener);
                            clearTask.execute();
                        }

                    } else {
                        Log.w(TAG, "onStartCommand: null data!");
                        notifications.stopSelf(startId);
                    }
                }
            } else {
                Log.w(TAG, "onStartCommand: null intent!");
                notifications.stopSelf(startId);
            }

            return START_STICKY;
        }

        protected Context getContext()
        {
            if (Build.VERSION.SDK_INT >= 24)
            {
                return (AlarmSettings.isUserUnlocked(getApplicationContext())
                        ? getApplicationContext()
                        : getApplicationContext().createDeviceProtectedStorageContext());
            }
            return getApplicationContext();
        }

        protected void onLockedBootCompleted(Context context)
        {
            if (BedtimeSettings.isBedtimeModeActive(context))
            {
                if (BedtimeSettings.isBedtimeModePaused(context)) {
                    showNotification(context, createBedtimeModeNotification(context), NOTIFICATION_BEDTIME_ACTIVE_ID);
                } else {
                    triggerBedtimeMode(context, true);
                }
            }
        }

        protected void onAfterBootCompleted(Context context, final int startId)
        {
            final long startTime = SystemClock.elapsedRealtime();
            AlarmSettings.savePrefLastBootCompleted_started(getApplicationContext(), startTime);

            if (Build.VERSION.SDK_INT < 24) {    // ACTION_LOCKED_BOOT_COMPLETED requires api 24+; for older devices run that code here instead
                onLockedBootCompleted(getApplicationContext());
            }

            AlarmDatabaseAdapter.AlarmListTask alarmListTask = new AlarmDatabaseAdapter.AlarmListTask(getApplicationContext());
            alarmListTask.setParam_enabledOnly(true);
            alarmListTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                @Override
                public void onItemsLoaded(final Long[] ids)
                {
                    final AlarmDatabaseAdapter.AlarmListObserver observer = new AlarmDatabaseAdapter.AlarmListObserver(ids, new AlarmDatabaseAdapter.AlarmListObserver.AlarmListObserverListener()
                    {
                        @Override
                        public void onObservedAll() {
                            final long endTime = SystemClock.elapsedRealtime();
                            final long duration = endTime - startTime;
                            AlarmSettings.savePrefLastBootCompleted_finished(getApplicationContext(), System.currentTimeMillis(), duration);
                            Log.d(TAG, "schedule all completed (took " + duration + "ms); " + AlarmSettings.bootCompletedWasRun(getApplicationContext()));
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Context context = getApplicationContext();
                                    sendBroadcast(getFullscreenBroadcast(null));
                                    if (ids.length > 0) {    // show warning if alarms where rescheduled
                                        if (!AlarmSettings.isIgnoringBatteryOptimizations(context)) {
                                            notifications.showNotification(context, createBatteryOptWarningNotification(context), NOTIFICATION_BATTERYOPT_WARNING_ID);
                                        }
                                        if (AlarmSettings.hasAutostartSettings(context) && AlarmSettings.isAutostartDisabled(context)) {
                                            notifications.showNotification(context, createAutostartWarningNotification(context), NOTIFICATION_AUTOSTART_WARNING_ID);
                                        }
                                    }
                                    //notifications.dismissNotification(context, NOTIFICATION_SCHEDULE_ALL_ID);
                                    notifications.stopSelf(startId);
                                }
                            }, (ids.length > 0 ? NOTIFICATION_SCHEDULE_ALL_DURATION : 0));
                        }
                    });

                    if (ids.length == 0) {
                        observer.notify(null);
                        return;
                    }

                    AlarmDatabaseAdapter.AlarmItemTaskListener notifyObserver = new AlarmDatabaseAdapter.AlarmItemTaskListener()
                    {
                        @Override
                        public void onFinished(Boolean result, AlarmClockItem item) {
                            Log.d(TAG, "schedule " + item.rowID + " completed!");
                            observer.notify(item.rowID);
                        }
                    };
                    for (long id : ids)
                    {
                        AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(getApplicationContext());
                        itemTask.addAlarmItemTaskListener(createAlarmOnReceiveListener(getApplicationContext(), startId, AlarmNotifications.ACTION_RESCHEDULE, notifyObserver));
                        itemTask.execute(id);
                    }
                }
            });
            //notifications.startForeground(NOTIFICATION_SCHEDULE_ALL_ID, createProgressNotification(getApplicationContext(), getString(R.string.app_name_alarmclock), getString(R.string.configLabel_alarms_bootcompleted_action_message)));
            alarmListTask.execute();
        }

        public static void triggerBedtimeMode(Context context, boolean value)
        {
            BedtimeSettings.setBedtimeState(context, (value ? BedtimeSettings.STATE_BEDTIME_ACTIVE : BedtimeSettings.STATE_BEDTIME_INACTIVE));

            if (value) {
                showNotification(context, createBedtimeModeNotification(context), NOTIFICATION_BEDTIME_ACTIVE_ID);
            } else dismissNotification(context, NOTIFICATION_BEDTIME_ACTIVE_ID);

            if (BedtimeSettings.loadPrefBedtimeDoNotDisturb(context))
            {
                if (Build.VERSION.SDK_INT >= 23)
                {
                    NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notifications != null && BedtimeSettings.hasDoNotDisturbPermission(context))
                    {
                        if (Build.VERSION.SDK_INT >= 24) {
                            BedtimeSettings.setAutomaticZenRule(context, true);
                        }
                        BedtimeSettings.triggerDoNotDisturb(context, value);
                    }
                }
            }
            context.sendBroadcast(getFullscreenBroadcast(null));
        }
        public static void pauseBedtimeMode(Context context)
        {
            if (BedtimeSettings.isBedtimeModeActive(context))
            {
                BedtimeSettings.setBedtimeState(context, BedtimeSettings.STATE_BEDTIME_PAUSED);
                showNotification(context, createBedtimeModeNotification(context), NOTIFICATION_BEDTIME_ACTIVE_ID);
                if (Build.VERSION.SDK_INT >= 24) {
                    BedtimeSettings.triggerDoNotDisturb(context, false);
                }
                context.sendBroadcast(getFullscreenBroadcast(null));
            }
        }
        public static void resumeBedtimeMode(Context context)
        {
            if (BedtimeSettings.isBedtimeModePaused(context))
            {
                BedtimeSettings.setBedtimeState(context, BedtimeSettings.STATE_BEDTIME_ACTIVE);
                showNotification(context, createBedtimeModeNotification(context), NOTIFICATION_BEDTIME_ACTIVE_ID);
                if (Build.VERSION.SDK_INT >= 24) {
                    BedtimeSettings.triggerDoNotDisturb(context, true);
                }
                context.sendBroadcast(getFullscreenBroadcast(null));
            }
        }

        private AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener rescheduleTaskListener_clocktime(final int startId)
        {
            return rescheduleTaskListener(startId, new AlarmClockItemFilter()
            {
                @Override
                public boolean passesFilter(AlarmClockItem item) {
                    int state = item.state.getState();
                    return (item.getEvent() == null && state != AlarmState.STATE_SOUNDING && state != AlarmState.STATE_SNOOZING);
                }
            });
        }
        public interface AlarmClockItemFilter {
            boolean passesFilter(AlarmClockItem item);
        }

        private AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener rescheduleTaskListener(final int startId, @Nullable final AlarmClockItemFilter filter)
        {
            return new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener()
            {
                @Override
                public void onItemsLoaded(final Long[] ids)
                {
                    final long startedAt = System.currentTimeMillis();
                    final AlarmDatabaseAdapter.AlarmListObserver observer = new AlarmDatabaseAdapter.AlarmListObserver(ids, new AlarmDatabaseAdapter.AlarmListObserver.AlarmListObserverListener() {
                        @Override
                        public void onObservedAll()
                        {
                            long duration = System.currentTimeMillis() - startedAt;
                            Log.d(TAG, "Re-schedule completed; took " + duration + "ms");
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
                            {
                                @Override
                                public void run() {
                                    notifications.dismissNotification(getApplicationContext(), NOTIFICATION_SCHEDULE_ALL_ID);
                                    notifications.stopSelf(startId);
                                }
                            }, (ids.length > 0 ? NOTIFICATION_SCHEDULE_ALL_DURATION : 0));
                        }
                    });
                    if (ids.length == 0) {
                        observer.notify(null);
                        return;
                    }
                    final AlarmDatabaseAdapter.AlarmItemTaskListener notifyObserver = new AlarmDatabaseAdapter.AlarmItemTaskListener() {
                        @Override
                        public void onFinished(Boolean result, AlarmClockItem item) {
                            observer.notify(item.rowID);
                        }
                    };
                    for (long id : ids) {
                        AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(getApplicationContext());
                        itemTask.addAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener() {
                            @Override
                            public void onFinished(Boolean result, final AlarmClockItem item)
                            {
                                if (filter == null || filter.passesFilter(item)) {
                                    createAlarmOnReceiveListener(getApplicationContext(), startId, AlarmNotifications.ACTION_RESCHEDULE, notifyObserver).onFinished(result, item);
                                } else notifyObserver.onFinished(false, item);
                            }
                        });
                        itemTask.execute(id);
                    }
                }
            };
        }

        private final AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener clearTaskListener = new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener()
        {
            @Override
            public void onItemsLoaded(Long[] ids)
            {
                cancelAlarmTimeouts(getApplicationContext(), ids);
                AlarmDatabaseAdapter.AlarmDeleteTask clearTask = new AlarmDatabaseAdapter.AlarmDeleteTask(getApplicationContext());
                clearTask.setTaskListener(onClearedState(getApplicationContext()));
                clearTask.execute();
            }
        };

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return new LocalBinder();
        }
        protected class LocalBinder extends Binder {
            NotificationService getService() {
                return NotificationService.this;
            }
        }

        public static Intent getNotificationIntent(Context context, String action, Uri data, @Nullable Bundle extras)
        {
            Intent intent = new Intent(context, NotificationService.class);
            intent.setAction(action);
            intent.setData(data);
            if (extras != null) {
                intent.putExtras(extras);
            }
            return intent;
        }

        /**
         */
        private AlarmDatabaseAdapter.AlarmItemTaskListener createAlarmOnReceiveListener(final Context context, final int startId, final String action) {
            return createAlarmOnReceiveListener(context, startId, action, null, null);
        }
        private AlarmDatabaseAdapter.AlarmItemTaskListener createAlarmOnReceiveListener(final Context context, int startId, final String action, @Nullable final Bundle bundle) {
            return createAlarmOnReceiveListener(context, startId, action, bundle, null);
        }
        private AlarmDatabaseAdapter.AlarmItemTaskListener createAlarmOnReceiveListener(final Context context, int startId, final String action, final @Nullable AlarmDatabaseAdapter.AlarmItemTaskListener chained) {
            return createAlarmOnReceiveListener(context, startId, action, null, chained);
        }
        private AlarmDatabaseAdapter.AlarmItemTaskListener createAlarmOnReceiveListener(final Context context, final int startId, final String action, @Nullable final Bundle extras, final @Nullable AlarmDatabaseAdapter.AlarmItemTaskListener chained)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(final Boolean result, final AlarmClockItem item)
                {
                    if (context == null) {
                        Log.w(TAG, "context is null!");
                        stopSelf(startId);
                        return;
                    }

                    if (item != null)
                    {
                        if (action.equals(ACTION_DISMISS))
                        {
                            ////////////////////////////////////////////////////////////////////////////
                            // Dismiss Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_DISMISSED))
                            {
                                cancelAlarmTimeouts(context, item);

                                AlarmState.transitionState(item.state, AlarmState.STATE_NONE);
                                final String nextAction;
                                if (!item.repeating)
                                {
                                    Log.i(TAG, "Dismissed: Non-repeating; disabling.. " + item.rowID);
                                    nextAction = ACTION_DISABLE;

                                } else {
                                    boolean dismissedEarly = (Calendar.getInstance().getTimeInMillis() < item.alarmtime);
                                    Log.i(TAG, "Dismissed: Repeating; re-scheduling.." + item.rowID + " [early=" + dismissedEarly + "]");
                                    if (dismissedEarly) {
                                        nextAction = ACTION_RESCHEDULE1;
                                    } else {
                                        nextAction = ACTION_SCHEDULE;
                                        item.alarmtime = 0;
                                    }
                                }

                                item.clearFlag(AlarmClockItem.FLAG_SNOOZE_COUNT);
                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                updateItem.setTaskListener(onDismissedState(context, startId, nextAction, item.getUri()));
                                updateItem.execute(item);    // write state
                            } else notifications.stopSelf(startId);

                        } else if (action.equals(ACTION_SILENT) && item.type == AlarmClockItem.AlarmType.ALARM) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Silenced Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            Log.i(TAG, "Silenced: " + item.rowID);
                            cancelAlarmTimeout(context, ACTION_SILENT, item.getUri());    // cancel upcoming silence timeout; if user silenced alarm there may be another silence scheduled
                            notifications.stopSelf(startId);

                        } else if (action.equals(ACTION_TIMEOUT ) && item.type == AlarmClockItem.AlarmType.ALARM) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Timeout Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_TIMEOUT))
                            {
                                Log.i(TAG, "Timeout: " + item.rowID);
                                cancelAlarmTimeouts(context, item);

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                updateItem.setTaskListener(onTimeoutState(context, startId));
                                updateItem.execute(item);  // write state
                            } else notifications.stopSelf(startId);

                        } else if (action.equals(ACTION_DISABLE)) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Disable Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_DISABLED))
                            {
                                Log.i(TAG, "Disable: " + item.rowID);
                                cancelAlarmTimeouts(context, item);

                                item.enabled = false;
                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                updateItem.setTaskListener(onDisabledState(context, startId));
                                updateItem.execute(item);    // write state
                            } else notifications.stopSelf(startId);

                        } else if (action.equals(ACTION_DELETE)) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Delete Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_DISABLED))
                            {
                                Log.i(TAG, "Delete: " + item.rowID);
                                cancelAlarmTimeouts(context, item);

                                AlarmDatabaseAdapter.AlarmDeleteTask deleteTask = new AlarmDatabaseAdapter.AlarmDeleteTask(context);
                                deleteTask.setTaskListener(onDeletedState(context, startId));
                                deleteTask.execute(item.rowID);
                            } else notifications.stopSelf(startId);

                        } else if (action.equals(ACTION_SCHEDULE) || (action.startsWith(ACTION_RESCHEDULE))) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Schedule Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_NONE))
                            {
                                cancelAlarmTimeouts(context, item);

                                long now = Calendar.getInstance().getTimeInMillis();
                                if (item.alarmtime <= now || item.alarmtime == 0 || action.startsWith(ACTION_RESCHEDULE))
                                {
                                    // expired alarm/notification
                                    if (item.enabled)    // enabled; reschedule alarm/notification
                                    {
                                        Log.d(TAG, "(Re)Scheduling: " + item.rowID);

                                        Calendar scheduledFrom = Calendar.getInstance();
                                        boolean dismissedEarly = (action.equals(ACTION_RESCHEDULE1) && item.alarmtime > 0);
                                        if (dismissedEarly) {
                                            scheduledFrom.setTimeInMillis(item.alarmtime + 60 * 1000);
                                        }
                                        boolean updated = updateAlarmTime(context, item, scheduledFrom, true);     // sets item.hour, item.minute, item.timestamp (calculates the eventTime)
                                        if (updated)
                                        {
                                            item.alarmtime = item.timestamp + item.offset;     // scheduled sounding time (-before/+after eventTime by some offset)
                                            if (dismissedEarly) {
                                                showTimeUntilToast(context, null, item);
                                            }

                                        } else {  // failed to schedule; this alarm needs to be disabled (prevent alarm loop)
                                            Log.d(TAG, "Disabling: " + item.rowID);
                                            sendBroadcast(getAlarmIntent(context, ACTION_DISABLE, item.getUri()));
                                            return;
                                        }

                                    } else {    // disabled; this alarm should have been dismissed
                                        Log.d(TAG, "Dismissing: " + item.rowID);
                                        sendBroadcast(getAlarmIntent(context, ACTION_DISMISS, item.getUri()));
                                        return;
                                    }
                                } else {
                                    Log.i(TAG, "Scheduling: " + item.rowID + " already has a valid alarmTime! (skipped call to updateAlarmTime)");
                                }

                                int nextState = AlarmState.STATE_SCHEDULED_DISTANT;
                                AlarmDatabaseAdapter.AlarmItemTaskListener onScheduledState;
                                if (item.type == AlarmClockItem.AlarmType.ALARM)
                                {
                                    long reminderWithin = item.getFlag(AlarmClockItem.FLAG_REMINDER_WITHIN, AlarmSettings.loadPrefAlarmUpcoming(context));
                                    boolean verySoon = (((item.alarmtime - now) < reminderWithin) || reminderWithin <= 0);
                                    nextState = (verySoon ? AlarmState.STATE_SCHEDULED_SOON : AlarmState.STATE_SCHEDULED_DISTANT);
                                    if (verySoon)
                                    {
                                        Log.i(TAG, "Scheduling: " + item.rowID + " :: very soon");
                                        onScheduledState = onScheduledSoonState(context, startId, chained);
                                    } else {
                                        Log.i(TAG, "Scheduling: " + item.rowID + " :: distant");
                                        onScheduledState = onScheduledDistantState(context, startId, chained);
                                    }
                                } else {
                                    Log.i(TAG, "Scheduling: " + item.rowID);
                                    onScheduledState = onScheduledNotification(context, startId, chained);
                                }

                                if (AlarmState.transitionState(item.state, nextState))
                                {
                                    AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                    updateItem.setTaskListener(onScheduledState);
                                    updateItem.execute(item);  // write state
                                }
                            } else notifications.stopSelf(startId);

                        } else if (action.equals(ACTION_SNOOZE) && item.type == AlarmClockItem.AlarmType.ALARM) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Snooze Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            int snoozeLimit = (int) item.getFlag(AlarmClockItem.FLAG_SNOOZE_LIMIT, AlarmSettings.loadPrefAlarmSnoozeLimit(context));
                            boolean snoozePermitted = (snoozeLimit == 0) || item.getFlag(AlarmClockItem.FLAG_SNOOZE_COUNT, 0) < snoozeLimit;
                            if (!snoozePermitted)
                            {
                                Log.w(TAG, "Snooze blocked; exceeded snooze limit of " + snoozeLimit);
                                notifications.stopSelf(startId);
                                return;
                            }

                            if (AlarmState.transitionState(item.state, AlarmState.STATE_SNOOZING))
                            {
                                Log.i(TAG, "Snoozing: " + item.rowID);
                                cancelAlarmTimeouts(context, item);

                                long snoozeDurationMs = item.getFlag(AlarmClockItem.FLAG_SNOOZE, AlarmSettings.loadPrefAlarmSnooze(context));
                                if (extras != null && extras.containsKey(AlarmClockActivity.EXTRA_ALARM_SNOOZE_DURATION))
                                {
                                    int snoozeDurationMinutes = extras.getInt(AlarmClockActivity.EXTRA_ALARM_SNOOZE_DURATION, -1);
                                    Log.i(TAG, "Snoozing: override duration: " + snoozeDurationMinutes + " minutes");
                                    if (snoozeDurationMinutes > 0) {
                                        if (snoozeDurationMinutes > 59) {
                                            snoozeDurationMinutes = 59;
                                        }
                                        snoozeDurationMs = snoozeDurationMinutes * 60 * 1000;
                                    }
                                }

                                long snoozeUntil = Calendar.getInstance().getTimeInMillis() + snoozeDurationMs;
                                addAlarmTimeout(context, ACTION_SHOW, item.getUri(), snoozeUntil);

                                item.incrementFlag(AlarmClockItem.FLAG_SNOOZE_COUNT);
                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                updateItem.setTaskListener(onSnoozeState(context, startId, snoozeUntil));
                                updateItem.execute(item);    // write state
                            } else notifications.stopSelf(startId);

                        } else if (action.equals(ACTION_SHOW)) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Show Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_SOUNDING))
                            {
                                Log.i(TAG, "Show: " + item.rowID + " (" + item.type + ")");
                                if (item.type == AlarmClockItem.AlarmType.ALARM)
                                {
                                    cancelAlarmTimeouts(context, item);
                                    addAlarmTimeouts(context, item.getUri());

                                    notifications.dismissNotification(context, (int)item.rowID);
                                    Notification notification = AlarmNotifications.createNotification(context, item);
                                    if (notification != null) {
                                        notifications.startForeground((int) item.rowID, notification);
                                    }
                                    AlarmNotifications.startAlert(context, item);

                                } else {
                                    if (item.type == AlarmClockItem.AlarmType.NOTIFICATION1) {
                                        addNotificationTimeouts(context, item.getUri());
                                    }
                                    notifications.showNotification(context, item, false);
                                }

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                updateItem.setTaskListener(onShowState(context, startId));
                                updateItem.execute(item);     // write state
                            } else notifications.stopSelf(startId);
                        } else {
                            // unrecognized action
                            Log.w(TAG, "unrecognized action: " + action);
                            notifications.stopSelf(startId);
                        }
                    } else {
                        // null alarm item (not found)
                        Log.w(TAG, "item not found!");
                        notifications.stopSelf(startId);
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onDismissedState(final Context context, final int startId, final String nextAction, final Uri data)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onDismissed)");
                    if (Build.VERSION.SDK_INT < 31) {
                        //noinspection MissingPermission
                        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));   // dismiss notification tray
                    }
                    context.sendBroadcast(getFullscreenBroadcast(item.getUri()));    // dismiss fullscreen activity
                    if (item.hasActionID(AlarmClockItem.ACTIONID_DISMISS))           // trigger dismiss action
                    {
                        SuntimesData data = getData(context, item);
                        data.calculate(context);
                        WidgetActions.startIntent(context.getApplicationContext(), 0, item.getActionID(AlarmClockItem.ACTIONID_DISMISS), data, null, Intent.FLAG_ACTIVITY_NEW_TASK);
                    }

                    if (item.type != AlarmClockItem.AlarmType.ALARM) {
                        notifications.dismissNotification(context, (int)item.rowID);
                    }

                    if (nextAction != null) {                                                // either SCHEDULE, RESCHEDULE1, or DISABLE
                        notifications.startForeground((int)item.rowID, createProgressNotification(context));    // replace sounding notification
                        context.sendBroadcast(getAlarmIntent(context, nextAction, data));    // trigger followup action
                    }

                    if (nextAction == null)
                    {
                        findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {    // find upcoming alarm (then finish)
                            @Override
                            public void onItemsLoaded(Long[] ids) {
                                notifications.dismissNotification(context, (int)item.rowID);
                                notifications.stopSelf(startId);
                            }
                        });
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onSnoozeState(final Context context, final int startId, final long snoozeUntil)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        Log.d(TAG, "State Saved (onSnooze)");
                        item.alarmtime = snoozeUntil;
                        Notification notification = AlarmNotifications.createNotification(context, item);
                        if (notification != null) {
                            notifications.startForeground((int) item.rowID, notification);  // update notification
                        }
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));  // update fullscreen activity
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onTimeoutState(final Context context, final int startId)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        Log.d(TAG, "State Saved (onTimeout)");
                        Notification notification = AlarmNotifications.createNotification(context, item);
                        if (notification != null) {
                            notifications.startForeground((int)item.rowID, notification);  // update notification
                        }
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));  // update fullscreen activity
                        notifications.stopSelf(startId);
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onShowState(final Context context, final int startId)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onShow)");
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        if (!NotificationManagerCompat.from(context).areNotificationsEnabled())
                        {
                            // when notifications are disabled, fallback to directly starting the fullscreen activity
                            startActivity(getFullscreenIntent(context, item.getUri()));

                        } else {
                            showAlarmPlayingToast(getApplicationContext(), item);
                            context.sendBroadcast(getFullscreenBroadcast(item.getUri()));   // update fullscreen activity
                        }
                        findUpcomingAlarm(context, null);

                    } else {
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));  // update fullscreen activity
                        notifications.stopSelf(startId);
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onDisabledState(final Context context, final int startId)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onDisabled)");
                    findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {    // find upcoming alarm (then finish)
                        @Override
                        public void onItemsLoaded(Long[] ids) {
                            //context.startActivity(getAlarmListIntent(context, item.rowID));         // open the alarm list
                            context.sendBroadcast(getFullscreenBroadcast(item.getUri()));           // dismiss fullscreen activity
                            notifications.dismissNotification(context, (int)item.rowID);
                            notifications.stopSelf(startId);
                        }
                    });
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener onDeletedState(final Context context, final int startId)
        {
            return new AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final Long itemID)
                {
                    Log.d(TAG, "Alarm Deleted (onDeleted)");
                    BedtimeSettings.clearAlarmID(getApplicationContext(), itemID);
                    findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {    // find upcoming alarm (then finish)
                        @Override
                        public void onItemsLoaded(Long[] ids)
                        {
                            Intent updateIntent = getFullscreenBroadcast(ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, itemID));
                            updateIntent.putExtra(ACTION_DELETE, true);    // signal item was deleted
                            context.sendBroadcast(updateIntent);     // dismiss fullscreen activity, update list UIs

                            //Intent alarmListIntent = getAlarmListIntent(context, itemID);
                            //alarmListIntent.setAction(AlarmNotifications.ACTION_DELETE);
                            //context.startActivity(alarmListIntent);                                                                             // open the alarm list

                            notifications.dismissNotification(context, itemID.intValue());
                            notifications.stopSelf(startId);
                        }
                    });
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener onClearedState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final Long itemID)
                {
                    Log.d(TAG, "Alarms Cleared (on Cleared)");
                    BedtimeSettings.clearAlarmIDs(getApplicationContext());
                    findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {    // clear upcoming alarm (then finish)
                        @Override
                        public void onItemsLoaded(Long[] ids)
                        {
                            Intent updateIntent = getFullscreenBroadcast(null);
                            updateIntent.putExtra(ACTION_DELETE, true);
                            context.sendBroadcast(updateIntent);     // dismiss fullscreen activity, update list UIs

                            //Intent alarmListIntent = getAlarmListIntent(context, itemID);
                            //alarmListIntent.setAction(AlarmNotifications.ACTION_DELETE);
                            //context.startActivity(alarmListIntent);                                 // open the alarm list

                            notifications.dismissNotifications(context);
                            notifications.stopSelf();
                        }
                    });
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onScheduledNotification(final Context context, final int startId, @Nullable final AlarmDatabaseAdapter.AlarmItemTaskListener chained)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.NOTIFICATION
                            || item.type == AlarmClockItem.AlarmType.NOTIFICATION1
                            || item.type == AlarmClockItem.AlarmType.NOTIFICATION2)
                    {
                        Log.d(TAG, "State Saved (onScheduledNotification)");
                        addAlarmTimeout(context, ACTION_SHOW, item.getUri(), item.alarmtime);
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));
                    }
                    notifications.dismissNotification(context, (int)item.rowID);
                    if (chained != null) {
                        chained.onFinished(true, item);
                    } else notifications.stopSelf(startId);
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onScheduledDistantState(final Context context, final int startId, @Nullable final AlarmDatabaseAdapter.AlarmItemTaskListener chained)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        Log.d(TAG, "State Saved (onScheduledDistant)");
                        long reminderWithin = item.getFlag(AlarmClockItem.FLAG_REMINDER_WITHIN, AlarmSettings.loadPrefAlarmUpcoming(context));
                        long transitionAt = item.alarmtime - reminderWithin + 1000;
                        addAlarmTimeout(context, ACTION_SCHEDULE, item.getUri(), transitionAt);
                        addAlarmTimeout(context, ACTION_SHOW, item.getUri(), item.alarmtime);
                        //context.startActivity(getAlarmListIntent(context, item.rowID));   // open the alarm list
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));

                        findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                            @Override
                            public void onItemsLoaded(Long[] ids)
                            {
                                notifications.dismissNotification(context, (int)item.rowID);
                                if (chained != null) {
                                    chained.onFinished(true, item);
                                } else notifications.stopSelf(startId);
                            }
                        });

                    } else {
                        if (chained != null) {
                            chained.onFinished(true, item);
                        } else notifications.stopSelf(startId);
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onScheduledSoonState(final Context context, final int startId, final @Nullable AlarmDatabaseAdapter.AlarmItemTaskListener chained)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        Log.d(TAG, "State Saved (onScheduledSoon)");
                        addAlarmTimeout(context, ACTION_SHOW, item.getUri(), item.alarmtime);

                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));

                        findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                            @Override
                            public void onItemsLoaded(Long[] ids)
                            {
                                boolean showReminder = (item.getFlag(AlarmClockItem.FLAG_REMINDER_WITHIN, AlarmSettings.loadPrefAlarmUpcoming(context)) > 0);
                                notifications.dismissNotification(context, (int)item.rowID);
                                if (showReminder) {
                                    notifications.showNotification(context, item, true);             // show upcoming reminder
                                }

                                if (chained != null) {
                                    chained.onFinished(true, item);
                                } else notifications.stopSelf(startId);
                            }
                        });
                    } else {
                        if (chained != null) {
                            chained.onFinished(true, item);
                        } else notifications.stopSelf(startId);
                    }
                }
            };
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * updateAlarmTime
     * @param item AlarmClockItem
     * @return true item was updated, false failed to update item
     */
    public static boolean updateAlarmTime(Context context, final AlarmClockItem item) {
        return updateAlarmTime(context, item, Calendar.getInstance(), true);
    }
    public static boolean updateAlarmTime(Context context, final AlarmClockItem item, Calendar now, boolean modifyItem)
    {
        Calendar eventTime = null;
        boolean modifyHourMinute = true;
        String eventID = item.getEvent();
        SolarEvents event = SolarEvents.valueOf(eventID, null);
        ArrayList<Integer> repeatingDays = (item.repeatingDays != null ? item.repeatingDays : AlarmClockItem.everyday());
        
        if (item.flagIsTrue(AlarmClockItem.FLAG_LOCATION_FROM_APP)) {
            item.location = WidgetSettings.loadLocationPref(context, 0);
        }

        if (item.location != null && event != null)
        {
            eventTime = updateAlarmTime_solarEvent(context, event, item.location, item.offset, item.repeating, repeatingDays, now);

        } else if (eventID != null) {
            eventTime = updateAlarmTime_addonEvent(context, context.getContentResolver(), eventID, item.location, item.offset, item.repeating, repeatingDays, now);

        } else {
            modifyHourMinute = false;    // "clock time" alarms should leave "hour" and "minute" values untouched
            eventTime = updateAlarmTime_clockTime(item.hour, item.minute, item.timezone, item.location, item.offset, item.repeating, repeatingDays, now);
        }

        if (eventTime == null) {
            Log.e(TAG, "updateAlarmTime: failed to update " + item + " :: " + item.getEvent() + "@" + item.location);
            return false;
        }

        if (modifyItem)
        {
            if (modifyHourMinute) {
                item.hour = eventTime.get(Calendar.HOUR_OF_DAY);
                item.minute = eventTime.get(Calendar.MINUTE);
            }
            item.timestamp = eventTime.getTimeInMillis();
            item.modified = true;
        }
        return true;
    }

    @Nullable
    protected static Calendar updateAlarmTime_solarEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        Calendar eventTime = null;
        switch (event.getType())
        {
            case SolarEvents.TYPE_MOON:
                eventTime = updateAlarmTime_moonEvent(context, event, location, offset, repeating, repeatingDays, now);
                break;

            case SolarEvents.TYPE_MOONPHASE:
                eventTime = updateAlarmTime_moonPhaseEvent(context, event, location, offset, repeating, repeatingDays, now);
                break;

            case SolarEvents.TYPE_SEASON:
                eventTime = updateAlarmTime_seasonEvent(context, event, location, offset, repeating, repeatingDays, now);
                break;

            case SolarEvents.TYPE_SUN:
                eventTime = updateAlarmTime_sunEvent(context, event, location, offset, repeating, repeatingDays, now);
                break;
        }
        return eventTime;
    }

    @Nullable
    protected static Calendar updateAlarmTime_sunEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        if (repeatingDays.isEmpty()) {
            //Log.w(TAG, "updateAlarmTime_sunEvent: empty repeatingDays! using EVERYDAY instead..");
            repeatingDays = AlarmClockItem.everyday();
        }

        SuntimesRiseSetData sunData = getData_sunEvent(context, event, location);

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        sunData.setTodayIs(day);
        sunData.calculate(context);
        eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
        if (eventTime != null)
        {
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }

        int c = 0;
        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime)
                || eventTime == null
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            if (!timestamps.add(alarmTime.getTimeInMillis()) && c > 365) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: sunEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            sunData.setTodayIs(day);
            sunData.calculate(context);
            eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    @Nullable
    private static Calendar updateAlarmTime_moonEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        if (repeatingDays.isEmpty()) {
            //Log.w(TAG, "updateAlarmTime_moonEvent: empty repeatingDays! using EVERYDAY instead..");
            repeatingDays = AlarmClockItem.everyday();
        }

        SuntimesMoonData moonData = getData_moonEvent(context, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        moonData.setTodayIs(day);
        moonData.calculate(context);
        Calendar eventTime = moonEventCalendar(event, moonData, true);
        if (eventTime != null)
        {
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        int c = 0;
        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime)
                || eventTime == null
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            if (!timestamps.add(alarmTime.getTimeInMillis()) && c > 365) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: moonEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            moonData.setTodayIs(day);
            moonData.calculate(context);
            eventTime = moonEventCalendar(event, moonData, true);
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    public static Calendar moonEventCalendar(SolarEvents event, SuntimesMoonData data, boolean today)
    {
        if (today)
        {
            switch (event) {
                case MOONNOON: return data.getLunarNoonToday();
                case MOONNIGHT: return data.getLunarMidnightToday();
                case MOONRISE: return data.moonriseCalendarToday();
                case MOONSET: default: return data.moonsetCalendarToday();
            }
        } else {
            switch (event) {
                case MOONNOON: return data.getLunarNoonTomorrow();
                case MOONNIGHT: return data.getLunarMidnightTomorrow();
                case MOONRISE: return data.moonriseCalendarTomorrow();
                case MOONSET: default: return data.moonsetCalendarTomorrow();
            }
        }
    }

    @Nullable
    protected static Calendar updateAlarmTime_moonPhaseEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        SuntimesCalculator.MoonPhase phase = event.toMoonPhase();
        SuntimesMoonData moonData = getData_moonEvent(context, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(now.getTimeInMillis());
        moonData.setTodayIs(day);
        moonData.calculate(context);

        int c = 0;
        Calendar eventTime = moonData.moonPhaseCalendar(phase);
        eventTime.set(Calendar.SECOND, 0);
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);

        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime))
                //|| (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))    // does it make sense to enforce repeatingDays for moon phases? probably not.
        {
            if (!timestamps.add(alarmTime.getTimeInMillis())) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            c++;
            Log.w("AlarmReceiverItem", "updateAlarmTime: moonPhaseEvent advancing to next cycle.. " + c);
            day.setTimeInMillis(eventTime.getTimeInMillis() + (24 * 60 * 60 * 1000));
            moonData.setTodayIs(day);
            moonData.calculate(context);
            eventTime = moonData.moonPhaseCalendar(phase);
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    @Nullable
    private static Calendar updateAlarmTime_seasonEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        SuntimesEquinoxSolsticeData data = getData_seasons(context, event, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        data.setTodayIs(day);
        data.calculate(context);

        Calendar eventTime = data.eventCalendarUpcoming(day);
        eventTime.set(Calendar.SECOND, 0);
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);

        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime))
                // || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))    // does it make sense to enforce repeatingDays for seasons? probably not.
        {
            if (!timestamps.add(alarmTime.getTimeInMillis())) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: seasonEvent advancing..");
            day.setTimeInMillis(eventTime.getTimeInMillis() + 1000 * 60 * 60 * 24);
            data.setTodayIs(day);
            data.calculate(context);
            eventTime = data.eventCalendarUpcoming(day);
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    public static Calendar updateAlarmTime_addonEvent(Context context, @Nullable ContentResolver resolver, @NonNull String eventID, @Nullable Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        if (repeatingDays.isEmpty()) {
            //Log.w(TAG, "updateAlarmTime_addonEvent: empty repeatingDays! using EVERYDAY instead..");
            repeatingDays = AlarmClockItem.everyday();
        }

        Log.d(TAG, "updateAlarmTime_addonEvent: eventID: " + eventID + ", offset: " + offset + ", repeating: " + repeating + ", repeatingDays: " + repeatingDays);
        long nowMillis = now.getTimeInMillis();
        Uri uri_id = Uri.parse(eventID);
        Uri uri_calc = Uri.parse(AlarmAddon.getEventCalcUri(uri_id.getAuthority(), uri_id.getLastPathSegment()));

        StringBuilder repeatingDaysString = new StringBuilder("[");
        if (repeating) {
            for (int i = 0; i < repeatingDays.size(); i++) {
                repeatingDaysString.append(repeatingDays.get(i));
                if (i != repeatingDays.size() - 1) {
                    repeatingDaysString.append(",");
                }
            }
        }
        repeatingDaysString.append("]");

        String[] selectionArgs = new String[] { Long.toString(nowMillis), Long.toString(offset), Boolean.toString(repeating), repeatingDaysString.toString() };
        String selection = AlarmEventContract.EXTRA_ALARM_NOW + "=? AND "
                + AlarmEventContract.EXTRA_ALARM_OFFSET + "=? AND "
                + AlarmEventContract.EXTRA_ALARM_REPEAT + "=? AND "
                + AlarmEventContract.EXTRA_ALARM_REPEAT_DAYS + "=?";

        if (location != null)
        {
            selectionArgs = new String[] { Long.toString(nowMillis), Long.toString(offset), Boolean.toString(repeating), repeatingDaysString.toString(),
                    location.getLatitude(), location.getLongitude(), location.getAltitude() };
            selection += " AND "
                    + CalculatorProviderContract.COLUMN_CONFIG_LATITUDE + "=? AND "
                    + CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE + "=? AND "
                    + CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE + "=?";
        }
        return queryAddonAlarmTimeWithTimeout(resolver, uri_calc, selection, selectionArgs, offset, now, MAX_WAIT_MS);
    }

    public static final long MAX_WAIT_MS = 990;
    protected static Calendar queryAddonAlarmTimeWithTimeout(@Nullable final ContentResolver resolver, final Uri uri_calc, final String selection, final String[] selectionArgs, final long offset, final Calendar now, long timeoutAfter)
    {
        return ExecutorUtils.getResult(TAG, new Callable<Calendar>() {
            public Calendar call() {
                return queryAddonAlarmTime(resolver, uri_calc, selection, selectionArgs, offset, now);
            }
        }, timeoutAfter);
    }

    protected static Calendar queryAddonAlarmTime(@Nullable ContentResolver resolver, Uri uri_calc, String selection, String[] selectionArgs, long offset, Calendar now)
    {
        if (resolver != null)
        {
            long nowMillis = now.getTimeInMillis();
            Cursor cursor = resolver.query(uri_calc, AlarmEventContract.QUERY_EVENT_CALC_PROJECTION, selection, selectionArgs, null);
            if (cursor != null)
            {
                cursor.moveToFirst();
                if (cursor.isAfterLast()) {
                    Log.e(TAG, "updateAlarmTime: failed to query alarm time; result is missing (no rows) :: " + uri_calc);
                    return null;
                }

                int i_eventTime = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_TIMEMILLIS);
                Long eventTimeMillis = i_eventTime >= 0 ? cursor.getLong(i_eventTime) : null;
                cursor.close();

                if (eventTimeMillis != null)
                {
                    if (nowMillis > (eventTimeMillis + offset)) {
                        Log.e(TAG, "updateAlarmTime: failed to query alarm time; result is invalid (past) :: " + uri_calc);
                        return null;
                    }
                    Calendar eventTime = Calendar.getInstance();
                    eventTime.setTimeInMillis(eventTimeMillis);
                    return eventTime;

                } else {
                    Log.e(TAG, "updateAlarmTime: failed to query alarm time; result is missing " + AlarmEventContract.COLUMN_EVENT_TIMEMILLIS + " :: " + uri_calc);
                    return null;
                }
            } else {
                Log.e(TAG, "updateAlarmTime: failed to query alarm time; null cursor!" + uri_calc);
                return null;
            }
        } else {
            Log.e(TAG, "updateAlarmTime: failed to query alarm time; null ContentResolver! " + uri_calc);
            return null;
        }
    }

    @Nullable
    protected static Calendar updateAlarmTime_clockTime(int hour, int minute, String tzID, @Nullable Location location, long offset, boolean repeating, @NonNull ArrayList<Integer> repeatingDays, @NonNull Calendar now)
    {
        t_updateAlarmTime_runningLoop = true;
        if (repeatingDays.isEmpty()) {
            //Log.w(TAG, "updateAlarmTime_clockTime: empty repeatingDays! using EVERYDAY instead..");
            repeatingDays = AlarmClockItem.everyday();
        }

        TimeZone timezone = AlarmClockItem.AlarmTimeZone.getTimeZone(tzID, location);
        Log.d(TAG, "updateAlarmTime_clockTime: hour: " + hour + ", minute: " + minute + ", timezone: " + timezone.getID() + ", offset: " + offset + ", repeating: " + repeating + ", repeatingDays: " + repeatingDays);
        Calendar alarmTime = Calendar.getInstance(timezone);
        Calendar eventTime = Calendar.getInstance(timezone);
        eventTime.setTimeInMillis(now.getTimeInMillis());

        eventTime.set(Calendar.SECOND, 0);
        if (hour >= 0 && hour < 24) {
            eventTime.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute >= 0 && minute < 60) {
            eventTime.set(Calendar.MINUTE, minute);
        }

        Set<Long> timestamps = new HashSet<>();
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        while (now.after(alarmTime)
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            if (!timestamps.add(alarmTime.getTimeInMillis())) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                t_updateAlarmTime_brokenLoop = true;
                t_updateAlarmTime_runningLoop = false;
                return null;
            }

            Log.w(TAG, "updateAlarmTime: clock time " + hour + ":" + minute + " (+" + offset + ") advancing by 1 day..");
            eventTime.add(Calendar.DAY_OF_YEAR, 1);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        t_updateAlarmTime_runningLoop = false;
        return eventTime;
    }

    public static SuntimesData getData(Context context, @NonNull AlarmClockItem alarm)
    {
        SolarEvents event = SolarEvents.valueOf(alarm.getEvent(), null);   // TODO: non SolarEventsEnum
        if (alarm.location != null && event != null)
        {
            switch (event.getType())
            {
                case SolarEvents.TYPE_MOON:
                case SolarEvents.TYPE_MOONPHASE:
                    return getData_moonEvent(context, alarm.location);
                case SolarEvents.TYPE_SEASON:
                    return getData_seasons(context, event, alarm.location);
                case SolarEvents.TYPE_SUN:
                    return getData_sunEvent(context, event, alarm.location);
                default:
                    return getData_clockEvent(context, alarm.location);
            }
        } else {
            return getData_clockEvent(context, WidgetSettings.loadLocationPref(context, 0));
        }
    }

    private static SuntimesRiseSetData getData_sunEvent(Context context, @NonNull SolarEvents event, @NonNull Location location)
    {
        WidgetSettings.TimeMode timeMode = event.toTimeMode();
        SuntimesRiseSetData sunData = new SuntimesRiseSetData(context, 0);
        sunData.setLocation(location);
        sunData.setTimeMode(timeMode != null ? timeMode : WidgetSettings.TimeMode.OFFICIAL);
        sunData.setTodayIs(Calendar.getInstance());
        return sunData;
    }
    protected static SuntimesMoonData getData_moonEvent(Context context, @NonNull Location location)
    {
        SuntimesMoonData moonData = new SuntimesMoonData(context, 0);
        moonData.setLocation(location);
        moonData.setTodayIs(Calendar.getInstance());
        return moonData;
    }
    private static SuntimesEquinoxSolsticeData getData_seasons(Context context, @NonNull SolarEvents event, @NonNull Location location)
    {
        WidgetSettings.SolsticeEquinoxMode season = event.toSolsticeEquinoxMode();
        SuntimesEquinoxSolsticeData data = new SuntimesEquinoxSolsticeData(context, 0);
        data.setTimeMode(season);
        data.setLocation(location);
        data.setTodayIs(Calendar.getInstance());
        return data;
    }
    private static SuntimesClockData getData_clockEvent(Context context, @NonNull Location location)
    {
        SuntimesClockData data = new SuntimesClockData(context, 0);
        data.setLocation(location);
        data.setTodayIs(Calendar.getInstance());
        return data;
    }

    protected static boolean t_updateAlarmTime_brokenLoop = false;   // for testing; set true by updateAlarmTime_ methods if the same timestamp is encountered twice (breaking the loop)
    protected static boolean t_updateAlarmTime_runningLoop = false;  // for testing; set true/false by updateAlarmTime_ methods
}
