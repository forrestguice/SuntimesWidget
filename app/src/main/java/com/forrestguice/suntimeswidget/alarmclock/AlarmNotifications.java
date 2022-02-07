/**
    Copyright (C) 2018-2021 Forrest Guice
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
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;

import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.icu.text.MessageFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.R;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.TimeZone;

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

    public static final String EXTRA_NOTIFICATION_ID = "notificationID";
    public static final String ALARM_NOTIFICATION_TAG = "suntimesalarm";

    private static SuntimesUtils utils = new SuntimesUtils();

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
        if (action != null) {
            context.startService(NotificationService.getNotificationIntent(context, action, data));
        } else Log.w(TAG, "onReceive: null action!");
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
            SuntimesUtils.TimeDisplayText alarmText = utils.timeDeltaLongDisplayString(now.getTimeInMillis(), item.timestamp + item.offset);
            String alarmString = context.getString(messageResID, item.type.getDisplayString(), alarmText.getValue());
            SpannableString alarmDisplay = SuntimesUtils.createBoldSpan(null, alarmString, alarmText.getValue());

            if (view != null)
            {
                Snackbar snackbar = Snackbar.make(view, alarmDisplay, duration);
                if (actionText != null && actionListener != null) {
                    snackbar.setAction(actionText, actionListener);
                }
                SuntimesUtils.themeSnackbar(context, snackbar, null);
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
            Toast msg = Toast.makeText(context, context.getString(R.string.alarmAction_silencedMsg), Toast.LENGTH_SHORT);
            msg.show();
        } else Log.e(TAG, "showAlarmSilencedToast: context is null!");
    }

    /**
     */
    protected static void showAlarmPlayingToast(Context context, AlarmClockItem item)
    {
        if (context != null) {
            Toast msg = Toast.makeText(context, context.getString(R.string.alarmAction_playingMsg, item.getLabel(context)), Toast.LENGTH_SHORT);
            msg.show();
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
                alarmManager.setAlarmClock(alarmInfo, getPendingIntent(context, action, data));

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
                    cancelAlarmTimeouts(context, ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmId));
                }
            } else Log.e(TAG, "cancelAlarmTimeouts: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmTimeouts: context is null!");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Find the alarm expected to trigger next and cache its ID in prefs.
     * If using 'power off alarms' this is the alarm that should wake the device.
     * @param context context
     * @param saveResult true save to prefs (and set power off alarm); false no action is performed (the result is available in onFinished)
     * @param onFinished task AlarmListTaskListener
     */
    protected static void findUpcomingAlarm(final Context context, final boolean saveResult, @Nullable final AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener onFinished)
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
    protected static void findUpcomingAlarm(final Context context, @Nullable final AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener onFinished) {
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

    private static final String PACKAGE_POWEROFFALARM = "com.qualcomm.qti.poweroffalarm";
    private static final String ACTION_POWEROFFALARM_SET = "org.codeaurora.poweroffalarm.action.SET_ALARM";
    private static final String ACTION_POWEROFFALARM_CANCEL = "org.codeaurora.poweroffalarm.action.CANCEL_ALARM";
    public static final String PERMISSION_POWEROFFALARM = "org.codeaurora.permission.POWER_OFF_ALARM";
    private static final String EXTRA_POWEROFFALARM_TIME = "time";

    protected static void setPowerOffAlarm(Context context, @NonNull AlarmClockItem alarm) {
        Log.d(TAG, "setPowerOffAlarm: " + alarm.rowID + " at " + alarm.alarmtime);
        context.sendBroadcast(getPowerOffAlarmIntent(ACTION_POWEROFFALARM_SET, alarm.alarmtime));
    }
    protected static void cancelPowerOffAlarm(Context context, @NonNull AlarmClockItem alarm) {
        Log.d(TAG, "cancelPowerOffAlarm: " + alarm.rowID + " at " + alarm.alarmtime);
        context.sendBroadcast(getPowerOffAlarmIntent(ACTION_POWEROFFALARM_CANCEL, alarm.alarmtime));
    }

    protected static Intent getPowerOffAlarmIntent(@Nullable String action, long datetime)
    {
        Intent intent = new Intent((action != null) ? action : ACTION_POWEROFFALARM_SET);
        intent.setPackage(PACKAGE_POWEROFFALARM);
        intent.putExtra(EXTRA_POWEROFFALARM_TIME, datetime);
        if (Build.VERSION.SDK_INT >= 16) {
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        }
        return intent;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static Intent getServiceIntent(Context context)
    {
        return new Intent(context, NotificationService.class);
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
        Intent intent = new Intent(AlarmDismissActivity.ACTION_UPDATE);
        intent.setData(data);
        return intent;
    }

    public static Intent getAlarmListIntent(Context context, Long selectedAlarmId)
    {
        Intent intent = new Intent(context, AlarmClockActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (selectedAlarmId != null) {
            intent.setData(ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, selectedAlarmId));
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
        initPlayer(context,false);
        if (isPlaying) {
            stopAlert();
        }
        isPlaying = true;

        boolean passesFilter = passesInterruptionFilter(context, alarm);
        if (!passesFilter) {
            Log.w(TAG, "startAlert: blocked by `Do Not Disturb`: " + alarm.rowID);
        }

        if (alarm.vibrate && passesFilter) {
            startVibration(context, alarm);
        }

        Uri soundUri = ((alarm.ringtoneURI != null && !alarm.ringtoneURI.isEmpty()) ? Uri.parse(alarm.ringtoneURI) : null);
        if (soundUri != null && passesFilter)
        {
            try {
                startAlert(context, soundUri, (alarm.type == AlarmClockItem.AlarmType.ALARM));

            } catch (IOException e) {    // fallback to default
                Uri defaultUri = AlarmSettings.getDefaultRingtoneUri(context, alarm.type);
                try {
                    startAlert(context, defaultUri, (alarm.type == AlarmClockItem.AlarmType.ALARM));
                } catch (IOException e1) {
                    Log.e(TAG, "startAlert: failed to setDataSource to default! " + defaultUri.toString());
                }
            }
        }

        if (alarm.hasActionID(AlarmClockItem.ACTIONID_MAIN))
        {
            SuntimesData data = getData(context, alarm);
            data.calculate();
            WidgetActions.startIntent(context.getApplicationContext(), 0, alarm.getActionID(AlarmClockItem.ACTIONID_MAIN), data, null, Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }

    private static void startAlert(Context context, @NonNull Uri soundUri, final boolean isAlarm) throws IOException
    {
        final long fadeInMillis = (isAlarm ? AlarmSettings.loadPrefAlarmFadeIn(context) : 0);
        final int streamType = (isAlarm ? AudioManager.STREAM_ALARM : AudioManager.STREAM_NOTIFICATION);
        player.setAudioStreamType(streamType);

        try {
            player.setDataSource(context, soundUri);
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
                        startFadeIn(fadeInMillis);
                    } else player.setVolume(1, 1);

                    mediaPlayer.start();
                }
            });
            player.prepareAsync();

        } catch (IOException e) {
            Log.e(TAG, "startAlert: failed to setDataSource! " + soundUri.toString());
            throw e;
        }
    }

    private static Handler vibrationHandler;
    private static Runnable vibration;
    private static Runnable vibrate(final long[] pattern, final int repeat)
    {
        return vibration = new Runnable()
        {
            @Override
            public void run()
            {
                if (isPlaying && vibrator != null)
                {
                    vibrator.vibrate(pattern, -1);   // manually loop vibration; this triggers a (re)start if vibration was stopped by screen-off.
                    if (isPlaying && repeat >= 0) {             // TODO: better workaround?
                        vibrationHandler.postDelayed(vibration, vibrationLength(pattern));
                    }
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
    private static void startVibration(@NonNull final Context context, @NonNull final AlarmClockItem alarm)
    {
        if (vibrationHandler == null) {
            vibrationHandler = new Handler();
        }
        int repeatFrom = (alarm.type == AlarmClockItem.AlarmType.ALARM) ? 0 : -1;
        vibrationHandler.post(vibrate(AlarmSettings.loadPrefVibratePattern(context, alarm.type), repeatFrom));
    }

    private static int FADEIN_STEP_MILLIS = 50;
    private static Handler fadeHandler;
    private static Runnable fadein;
    private static Runnable fadeIn(final long duration)    // TODO: use VolumeShaper for api 26+
    {
        return fadein = new Runnable()
        {
            private float elapsed = 0;

            @Override
            public void run()
            {
                elapsed += FADEIN_STEP_MILLIS;
                float volume = elapsed / (float) duration;
                player.setVolume(volume, volume);

                //Log.d("DEBUG", "fadeIn: " + elapsed + ":" + volume);
                if ((elapsed + FADEIN_STEP_MILLIS) <= duration) {
                    fadeHandler.postDelayed(fadein, FADEIN_STEP_MILLIS);
                }
            }
        };
    }
    private static void startFadeIn(final long duration)
    {
        if (fadeHandler == null) {
            fadeHandler = new Handler();
        }
        player.setVolume(0, 0);
        fadeHandler.postDelayed(fadeIn(duration), FADEIN_STEP_MILLIS);
    }

    /**
     * Stop playing sound / vibration.
     */
    public static void stopAlert()
    {
        stopAlert(true);
    }
    public static void stopAlert(boolean stopVibrate)
    {
        if (vibrator != null && stopVibrate)
        {
            vibrator.cancel();
            if (vibrationHandler != null) {
                vibrationHandler.removeCallbacks(vibration);
            }
        }

        if (player != null)
        {
            player.stop();
            if (audioManager != null) {
                audioManager.abandonAudioFocus(null);
            }
            player.reset();
        }

        isPlaying = false;
    }

    private static boolean passesInterruptionFilter(Context context, @NonNull AlarmClockItem item)
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
                                (isCategorySet(getNotificationPolicy(notificationManager), PRIORITY_CATEGORY_ALARMS));
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
    private static final int PRIORITY_CATEGORY_ALARMS = 1 << 5;  // TODO: use constants added in api28

    private static boolean isPlaying = false;
    private static MediaPlayer player = null;
    private static Vibrator vibrator = null;
    private static AudioManager audioManager;
    private static void initPlayer(final Context context, @SuppressWarnings("SameParameterValue") boolean reinit)
    {
        if (vibrator == null || reinit) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (audioManager == null || reinit) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        if (player == null || reinit)
        {
            player = new MediaPlayer();
            player.setOnErrorListener(new MediaPlayer.OnErrorListener()
            {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
                {
                    Log.e(TAG, "onError: MediaPlayer error " + what);
                    return false;
                }
            });

            player.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener()
            {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer)
                {
                    if (!mediaPlayer.isLooping()) {                // some sounds (mostly ringtones) have a built-in loop - they repeat despite !isLooping!
                        stopAlert();                            // so manually stop them after playing once
                    }
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * createNotification
     * @param context Context
     * @param alarm AlarmClockItem
     * @return a Notification object (or null if a notification shouldn't be shown)
     */
    public static Notification createNotification(Context context, @NonNull AlarmClockItem alarm)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        String eventString = alarm.getEvent();
        AlarmEvent.AlarmEventItem eventItem = new AlarmEvent.AlarmEventItem(eventString, context.getContentResolver());
        String eventDisplay = (eventString != null) ? eventItem.getTitle() : null;
        if (alarm.offset != 0) {
            eventDisplay = (eventString != null) ? formatOffsetMessage(context, alarm.offset, alarm.timestamp, eventItem)
                                                 : formatOffsetMessage(context, alarm.offset, alarm.timestamp);
        }
        String emptyLabel = ((eventDisplay != null) ? eventDisplay : context.getString(R.string.alarmOption_solarevent_none));

        String notificationTitle = (alarm.label == null || alarm.label.isEmpty() ? emptyLabel : alarm.label);
        String notificationMsg = eventDisplay;
        int notificationIcon = ((alarm.type == AlarmClockItem.AlarmType.NOTIFICATION) ? R.drawable.ic_action_notification : R.drawable.ic_action_alarms);
        int notificationColor = ContextCompat.getColor(context, R.color.sunIcon_color_setting_dark);

        builder.setDefaults( Notification.DEFAULT_LIGHTS );

        PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, alarm.hashCode(), getAlarmIntent(context, ACTION_DISMISS, alarm.getUri()), PendingIntent.FLAG_UPDATE_CURRENT);
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
                        notificationMsg = notificationTitle;
                        notificationTitle = context.getString(R.string.alarmAction_upcomingMsg);
                        builder.setWhen(alarm.alarmtime);
                        builder.setContentIntent(pendingView);
                        builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), pendingDismiss);
                        builder.setAutoCancel(false);
                        builder.setOngoing(true);
                    //} else return null;  // don't show reminder for api 21+ (uses notification provided by setAlarm instead)
                    break;

                case AlarmState.STATE_SNOOZING:
                    builder.setCategory( NotificationCompat.CATEGORY_ALARM );
                    builder.setPriority( NotificationCompat.PRIORITY_MAX );
                    SuntimesUtils.initDisplayStrings(context);
                    SuntimesUtils.TimeDisplayText snoozeText = utils.timeDeltaLongDisplayString(0, AlarmSettings.loadPrefAlarmSnooze(context));
                    notificationMsg = context.getString(R.string.alarmAction_snoozeMsg, snoozeText.getValue());
                    notificationIcon = R.drawable.ic_action_snooze;
                    builder.setFullScreenIntent(alarmFullscreen, true);       // at discretion of system to use this intent (or to show a heads up notification instead)
                    builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), pendingDismiss);
                    if (Build.VERSION.SDK_INT < 16) {
                        builder.setContentIntent(pendingDismiss);    // action buttons require expanded notifications (api 16+)
                    }
                    builder.setAutoCancel(false);
                    builder.setOngoing(true);
                    break;

                case AlarmState.STATE_SOUNDING:
                    builder.setCategory( NotificationCompat.CATEGORY_ALARM );
                    builder.setPriority( NotificationCompat.PRIORITY_MAX );
                    builder.addAction(R.drawable.ic_action_snooze, context.getString(R.string.alarmAction_snooze), pendingSnooze);
                    builder.setProgress(0,0,true);
                    builder.setFullScreenIntent(alarmFullscreen, true);       // at discretion of system to use this intent (or to show a heads up notification instead)
                    builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), pendingDismiss);
                    if (Build.VERSION.SDK_INT < 16) {
                        builder.setContentIntent(pendingDismiss);    // action buttons require expanded notifications (api 16+)
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

        } else {
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
                .setColor(notificationColor)
                .setVisibility( NotificationCompat.VISIBILITY_PUBLIC );
        builder.setOnlyAlertOnce(false);

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
     * @see NotificationService to display a notification that lives longer than the receiver.
     */
    public static void showNotification(Context context, @NonNull AlarmClockItem item)
    {
        showNotification(context, item, false);
    }
    public static void showNotification(Context context, @NonNull AlarmClockItem item, boolean quiet)
    {
        Notification notification = createNotification(context, item);
        if (notification != null)
        {
            int notificationID = (int)item.rowID;
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(ALARM_NOTIFICATION_TAG, notificationID, notification);
        }
        if (!quiet) {
            startAlert(context, item);
        }
    }
    public static void dismissNotification(Context context, int notificationID)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(ALARM_NOTIFICATION_TAG, notificationID);
    }
    public static void dismissNotifications(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * NotificationService
     */
    public static class NotificationService extends Service
    {
        public static final String TAG = "AlarmReceiverService";

        @Override
        public int onStartCommand(final Intent intent, int flags, int startId)
        {
            super.onStartCommand(intent, flags, startId);
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
                        Log.d(TAG, "ACTION_SNOOZE: " + data);
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
                    itemTask.addAlarmItemTaskListener(createAlarmOnReceiveListener(getApplicationContext(), action));
                    itemTask.execute(ContentUris.parseId(data));

                } else {
                    if (AlarmNotifications.ACTION_SCHEDULE.equals(action) || Intent.ACTION_BOOT_COMPLETED.equals(action))
                    {
                        Log.d(TAG, action + ": schedule all");
                        AlarmDatabaseAdapter.AlarmListTask alarmListTask = new AlarmDatabaseAdapter.AlarmListTask(getApplicationContext());
                        alarmListTask.setParam_enabledOnly(true);
                        alarmListTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                            @Override
                            public void onItemsLoaded(Long[] ids)
                            {
                                final AlarmDatabaseAdapter.AlarmListObserver observer = new AlarmDatabaseAdapter.AlarmListObserver(ids, new AlarmDatabaseAdapter.AlarmListObserver.AlarmListObserverListener()
                                {
                                    @Override
                                    public void onObservedAll() {
                                        if (!isForegroundService(NotificationService.this, AlarmNotifications.NotificationService.class))
                                        {
                                            Log.d(TAG, "schedule all completed! stopping service...");
                                            Intent serviceIntent = getServiceIntent(NotificationService.this);
                                            stopService(serviceIntent);
                                        } else Log.d(TAG, "schedule all completed! the foreground service still running.");
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
                                    itemTask.addAlarmItemTaskListener(createAlarmOnReceiveListener(getApplicationContext(), AlarmNotifications.ACTION_RESCHEDULE, notifyObserver));
                                    itemTask.execute(id);
                                }
                            }
                        });
                        alarmListTask.execute();

                    } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                        Log.d(TAG, "TIME_SET received");
                        stopSelf(startId);
                        // TODO: reschedule alarms (but only when deltaT is >reminderPeriod to avoid rescheduling alarms dismissed early)

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

                    } else Log.w(TAG, "onStartCommand: null data!");
                }
            } else Log.w(TAG, "onStartCommand: null intent!");

            return START_STICKY;
        }

        private AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener clearTaskListener = new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener()
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
        public IBinder onBind(Intent intent)
        {
            return null;
        }

        private static Intent getNotificationIntent(Context context, String action, Uri data)
        {
            Intent intent = new Intent(context, NotificationService.class);
            intent.setAction(action);
            intent.setData(data);
            return intent;
        }

        /**
         */
        private AlarmDatabaseAdapter.AlarmItemTaskListener createAlarmOnReceiveListener(final Context context, final String action)
        {
            return createAlarmOnReceiveListener(context, action, null);
        }
        private AlarmDatabaseAdapter.AlarmItemTaskListener createAlarmOnReceiveListener(final Context context, final String action, final @Nullable AlarmDatabaseAdapter.AlarmItemTaskListener chained)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(final Boolean result, final AlarmClockItem item)
                {
                    if (context == null) {
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

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                updateItem.setTaskListener(onDismissedState(context, nextAction, item.getUri()));
                                updateItem.execute(item);    // write state
                            }

                        } else if (action.equals(ACTION_SILENT) && item.type == AlarmClockItem.AlarmType.ALARM) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Silenced Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            Log.i(TAG, "Silenced: " + item.rowID);
                            cancelAlarmTimeout(context, ACTION_SILENT, item.getUri());    // cancel upcoming silence timeout; if user silenced alarm there may be another silence scheduled

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
                                updateItem.setTaskListener(onTimeoutState(context));
                                updateItem.execute(item);  // write state
                            }

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
                                updateItem.setTaskListener(onDisabledState(context));
                                updateItem.execute(item);    // write state
                            }

                        } else if (action.equals(ACTION_DELETE)) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Delete Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_DISABLED))
                            {
                                Log.i(TAG, "Delete: " + item.rowID);
                                cancelAlarmTimeouts(context, item);

                                AlarmDatabaseAdapter.AlarmDeleteTask deleteTask = new AlarmDatabaseAdapter.AlarmDeleteTask(context);
                                deleteTask.setTaskListener(onDeletedState(context));
                                deleteTask.execute(item.rowID);
                            }

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
                                }

                                int nextState = AlarmState.STATE_SCHEDULED_DISTANT;
                                AlarmDatabaseAdapter.AlarmItemTaskListener onScheduledState;
                                if (item.type == AlarmClockItem.AlarmType.ALARM)
                                {
                                    long reminderWithin = AlarmSettings.loadPrefAlarmUpcoming(context);
                                    boolean verySoon = (((item.alarmtime - now) < reminderWithin) || reminderWithin <= 0);
                                    nextState = (verySoon ? AlarmState.STATE_SCHEDULED_SOON : AlarmState.STATE_SCHEDULED_DISTANT);
                                    if (verySoon)
                                    {
                                        Log.i(TAG, "Scheduling: " + item.rowID + " :: very soon");
                                        onScheduledState = onScheduledSoonState(context, chained);
                                    } else {
                                        Log.i(TAG, "Scheduling: " + item.rowID + " :: distant");
                                        onScheduledState = onScheduledDistantState(context, chained);
                                    }
                                } else {
                                    Log.i(TAG, "Scheduling: " + item.rowID);
                                    onScheduledState = onScheduledNotification(context, chained);
                                }

                                if (AlarmState.transitionState(item.state, nextState))
                                {
                                    AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                    updateItem.setTaskListener(onScheduledState);
                                    updateItem.execute(item);  // write state
                                }
                            }

                        } else if (action.equals(ACTION_SNOOZE) && item.type == AlarmClockItem.AlarmType.ALARM) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Snooze Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_SNOOZING))
                            {
                                Log.i(TAG, "Snoozing: " + item.rowID);
                                cancelAlarmTimeouts(context, item);

                                long snoozeUntil = Calendar.getInstance().getTimeInMillis() + AlarmSettings.loadPrefAlarmSnooze(context);
                                addAlarmTimeout(context, ACTION_SHOW, item.getUri(), snoozeUntil);

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                updateItem.setTaskListener(onSnoozeState(context));
                                updateItem.execute(item);    // write state
                            }

                        } else if (action.equals(ACTION_SHOW)) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Show Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_SOUNDING))
                            {
                                if (item.type == AlarmClockItem.AlarmType.ALARM)
                                {
                                    Log.i(TAG, "Show: " + item.rowID + "(Alarm)");
                                    cancelAlarmTimeouts(context, item);
                                    addAlarmTimeouts(context, item.getUri());

                                    dismissNotification(context, (int)item.rowID);
                                    Notification notification = AlarmNotifications.createNotification(context, item);
                                    if (notification != null) {
                                        startForeground((int) item.rowID, notification);
                                    }
                                    AlarmNotifications.startAlert(context, item);

                                } else {
                                    Log.i(TAG, "Show: " + item.rowID + "(Notification)");
                                    showNotification(context, item);
                                }

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                                updateItem.setTaskListener(onShowState(context));
                                updateItem.execute(item);     // write state
                            }
                        }
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onDismissedState(final Context context, final String nextAction, final Uri data)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onDismissed)");
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));   // dismiss notification tray
                    context.sendBroadcast(getFullscreenBroadcast(item.getUri()));    // dismiss fullscreen activity
                    if (item.hasActionID(AlarmClockItem.ACTIONID_DISMISS))           // trigger dismiss action
                    {
                        SuntimesData data = getData(context, item);
                        data.calculate();
                        WidgetActions.startIntent(context.getApplicationContext(), 0, item.getActionID(AlarmClockItem.ACTIONID_DISMISS), data, null, Intent.FLAG_ACTIVITY_NEW_TASK);
                    }

                    if (nextAction != null) {
                        context.sendBroadcast(getAlarmIntent(context, nextAction, data));    // trigger followup action
                    }

                    final Intent serviceIntent = getServiceIntent(context);
                    startService(serviceIntent);                                   // keep service running after stopping foreground notification
                    stopForeground(true );
                    dismissNotification(context, (int)item.rowID);                 // dismiss upcoming reminders

                    if (nextAction == null)
                    {
                        findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {    // find upcoming alarm (then finish)
                            @Override
                            public void onItemsLoaded(Long[] ids) {
                                stopService(serviceIntent);
                            }
                        });
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onSnoozeState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        Log.d(TAG, "State Saved (onSnooze)");
                        Notification notification = AlarmNotifications.createNotification(context, item);
                        if (notification != null) {
                            startForeground((int) item.rowID, notification);  // update notification
                        }
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));  // update fullscreen activity
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onTimeoutState(final Context context)
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
                            startForeground((int)item.rowID, notification);  // update notification
                        }
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));  // update fullscreen activity
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onShowState(final Context context)
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
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onDisabledState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onDisabled)");
                    final Intent serviceIntent = getServiceIntent(context);
                    startService(serviceIntent);  // keep service running after stopping foreground notification

                    findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {    // find upcoming alarm (then finish)
                        @Override
                        public void onItemsLoaded(Long[] ids) {
                            stopForeground(true);     // remove notification (will kill running tasks)
                            context.startActivity(getAlarmListIntent(context, item.rowID));   // open the alarm list
                            dismissNotification(context, (int)item.rowID);                    // dismiss upcoming reminders
                            context.sendBroadcast(getFullscreenBroadcast(item.getUri()));     // dismiss fullscreen activity
                            stopService(serviceIntent);
                        }
                    });
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener onDeletedState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final Long itemID)
                {
                    Log.d(TAG, "Alarm Deleted (onDeleted)");
                    final Intent serviceIntent = getServiceIntent(context);
                    startService(serviceIntent);  // keep service running after stopping foreground notification

                    findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {    // find upcoming alarm (then finish)
                        @Override
                        public void onItemsLoaded(Long[] ids)
                        {
                            stopForeground(true);                                      // dismiss active notification (will kill running tasks)
                            dismissNotification(context, itemID.intValue());                                                                   // dismiss upcoming reminders
                            context.sendBroadcast(getFullscreenBroadcast(ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, itemID)));     // dismiss fullscreen activity

                            Intent alarmListIntent = getAlarmListIntent(context, itemID);
                            alarmListIntent.setAction(AlarmNotifications.ACTION_DELETE);
                            context.startActivity(alarmListIntent);   // open the alarm list
                            stopService(serviceIntent);
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
                    Log.d(TAG, "Alarms Cleared (onCleared)");
                    final Intent serviceIntent = getServiceIntent(context);
                    startService(serviceIntent);  // keep service running after stopping foreground notification

                    findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {    // clear upcoming alarm (then finish)
                        @Override
                        public void onItemsLoaded(Long[] ids)
                        {
                            context.sendBroadcast(getFullscreenBroadcast(null));     // dismiss fullscreen activity
                            stopForeground(true);                         // dismiss active notifications
                            dismissNotifications(context);                                // dismiss upcoming reminders

                            Intent alarmListIntent = getAlarmListIntent(context, itemID);
                            alarmListIntent.setAction(AlarmNotifications.ACTION_DELETE);
                            context.startActivity(alarmListIntent);   // open the alarm list
                            stopService(serviceIntent);
                        }
                    });
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onScheduledNotification(final Context context, @Nullable final AlarmDatabaseAdapter.AlarmItemTaskListener chained)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.NOTIFICATION)
                    {
                        Log.d(TAG, "State Saved (onScheduledNotification)");
                        addAlarmTimeout(context, ACTION_SHOW, item.getUri(), item.alarmtime);
                    }
                    if (chained != null) {
                        chained.onFinished(true, item);
                    } else stopSelf();
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onScheduledDistantState(final Context context, @Nullable final AlarmDatabaseAdapter.AlarmItemTaskListener chained)
        {
            return new AlarmDatabaseAdapter.AlarmItemTaskListener()
            {
                @Override
                public void onFinished(Boolean result, final AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        Log.d(TAG, "State Saved (onScheduledDistant)");
                        long transitionAt = item.alarmtime - AlarmSettings.loadPrefAlarmUpcoming(context) + 1000;
                        addAlarmTimeout(context, ACTION_SCHEDULE, item.getUri(), transitionAt);
                        addAlarmTimeout(context, ACTION_SHOW, item.getUri(), item.alarmtime);
                        //context.startActivity(getAlarmListIntent(context, item.rowID));   // open the alarm list
                        dismissNotification(context, (int)item.rowID);

                        findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                            @Override
                            public void onItemsLoaded(Long[] ids) {
                                if (chained != null) {
                                    chained.onFinished(true, item);
                                } else stopSelf();
                            }
                        });

                    } else {
                        if (chained != null) {
                            chained.onFinished(true, item);
                        } else stopSelf();
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmItemTaskListener onScheduledSoonState(final Context context, final @Nullable AlarmDatabaseAdapter.AlarmItemTaskListener chained)
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

                        if (AlarmSettings.loadPrefAlarmUpcoming(context) > 0) {
                            showNotification(context, item, true);             // show upcoming reminder
                        }

                        findUpcomingAlarm(context, new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                            @Override
                            public void onItemsLoaded(Long[] ids) {
                                if (chained != null) {
                                    chained.onFinished(true, item);
                                }    // the service continues running; items that are "soon" live in the notification tray
                            }
                        });
                    } else {
                        if (chained != null) {
                            chained.onFinished(true, item);
                        }
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
        String eventID = item.getEvent();
        SolarEvents event = SolarEvents.valueOf(eventID, null);
        if (item.location != null && event != null)
        {
            eventTime = updateAlarmTime_solarEvent(context, event, item.location, item.offset, item.repeating, item.repeatingDays, now);

        } else if (eventID != null) {
            eventTime = updateAlarmTime_addonEvent(context.getContentResolver(), eventID, item.location, item.offset, item.repeating, item.repeatingDays, now);

        } else {
            eventTime = updateAlarmTime_clockTime(item.hour, item.minute, item.timezone, item.location, item.offset, item.repeating, item.repeatingDays, now);
        }

        if (eventTime == null) {
            Log.e(TAG, "updateAlarmTime: failed to update " + item + " :: " + item.getEvent() + "@" + item.location);
            return false;
        }

        if (modifyItem)
        {
            item.hour = eventTime.get(Calendar.HOUR_OF_DAY);
            item.minute = eventTime.get(Calendar.MINUTE);
            item.timestamp = eventTime.getTimeInMillis();
            item.modified = true;
        }
        return true;
    }

    @Nullable
    protected static Calendar updateAlarmTime_solarEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
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
    private static Calendar updateAlarmTime_sunEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        SuntimesRiseSetData sunData = getData_sunEvent(context, event, location);

        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        sunData.setTodayIs(day);
        sunData.calculate();
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
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: sunEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            sunData.setTodayIs(day);
            sunData.calculate();
            eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;
        }
        return eventTime;
    }

    @Nullable
    private static Calendar updateAlarmTime_moonEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        SuntimesMoonData moonData = getData_moonEvent(context, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        moonData.setTodayIs(day);
        moonData.calculate();
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
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: moonEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            moonData.setTodayIs(day);
            moonData.calculate();
            eventTime = moonEventCalendar(event, moonData, true);
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }
            c++;        }
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
    private static Calendar updateAlarmTime_moonPhaseEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        SuntimesCalculator.MoonPhase phase = event.toMoonPhase();
        SuntimesMoonData moonData = getData_moonEvent(context, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        moonData.setTodayIs(day);
        moonData.calculate();

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
                return null;
            }

            c++;
            Log.w("AlarmReceiverItem", "updateAlarmTime: moonPhaseEvent advancing to next cycle.. " + c);
            day.add(Calendar.HOUR, (int)(c * (0.25 * 29.53d * 24d)));
            moonData.setTodayIs(day);
            moonData.calculate();
            eventTime = moonData.moonPhaseCalendar(phase);
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        return eventTime;
    }

    @Nullable
    private static Calendar updateAlarmTime_seasonEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        SuntimesEquinoxSolsticeData data = getData_seasons(context, event, location);

        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        data.setTodayIs(day);
        data.calculate();

        Calendar eventTime = data.eventCalendarUpcoming(day);
        eventTime.set(Calendar.SECOND, 0);
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);

        Set<Long> timestamps = new HashSet<>();
        while (now.after(alarmTime))
                // || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))    // does it make sense to enforce repeatingDays for seasons? probably not.
        {
            if (!timestamps.add(alarmTime.getTimeInMillis())) {
                Log.e(TAG, "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                return null;
            }

            Log.w("AlarmReceiverItem", "updateAlarmTime: seasonEvent advancing..");
            day.setTimeInMillis(eventTime.getTimeInMillis() + 1000 * 60 * 60 * 24);
            data.setTodayIs(day);
            data.calculate();
            eventTime = data.eventCalendarUpcoming(day);
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        return eventTime;
    }

    protected static Calendar updateAlarmTime_addonEvent(@Nullable ContentResolver resolver, @NonNull String eventID, @Nullable Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        Log.d(TAG, "updateAlarmTime_addonEvent: eventID: " + eventID + ", offset: " + offset + ", repeating: " + repeating);
        long nowMillis = now.getTimeInMillis();

        Uri uri_id = Uri.parse(eventID);
        Uri uri_calc = Uri.parse(AlarmAddon.getEventCalcUri(uri_id.getAuthority(), uri_id.getLastPathSegment()));
        if (resolver != null)
        {
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

            Cursor cursor = resolver.query(uri_calc, AlarmEventContract.QUERY_EVENT_CALC_PROJECTION, selection, selectionArgs, null);
            if (cursor != null)
            {
                cursor.moveToFirst();
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
    protected static Calendar updateAlarmTime_clockTime(int hour, int minute, String tzID, @Nullable Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays, Calendar now)
    {
        TimeZone timezone = AlarmClockItem.AlarmTimeZone.getTimeZone(tzID, location);
        Log.d(TAG, "updateAlarmTime_clockTime: hour: " + hour + ", minute: " + minute + ", timezone: " + timezone.getID() + ", offset: " + offset + ", repeating: " + repeating);
        Calendar alarmTime = Calendar.getInstance(timezone);
        Calendar eventTime = Calendar.getInstance(timezone);

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
                return null;
            }

            Log.w(TAG, "updateAlarmTime: clock time " + hour + ":" + minute + " (+" + offset + ") advancing by 1 day..");
            eventTime.add(Calendar.DAY_OF_YEAR, 1);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        return eventTime;
    }

    private static SuntimesData getData(Context context, @NonNull AlarmClockItem alarm)
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
    private static SuntimesMoonData getData_moonEvent(Context context, @NonNull Location location)
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

    /**
     * based on solutions at https://stackoverflow.com/questions/6452466/how-to-determine-if-an-android-service-is-running-in-the-foreground
     */
    private static boolean isForegroundService(Context context, @NonNull Class<?> service)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null)
        {
            String className = service.getName();
            List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo s : services)
            {
                if (className.equals(s.service.getClassName())) {
                    return s.foreground;
                }
            }
        }
        return false;
    }

}
