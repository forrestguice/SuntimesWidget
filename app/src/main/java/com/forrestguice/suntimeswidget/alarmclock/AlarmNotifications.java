/**
    Copyright (C) 2018 Forrest Guice
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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.SpannableString;
import android.util.Log;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmDismissActivity;

import java.io.IOException;
import java.util.Calendar;

public class AlarmNotifications extends BroadcastReceiver
{
    public static final String TAG = "AlarmReceiver";

    public static final String ACTION_SHOW = "show";                // sound an alarm
    public static final String ACTION_SILENT = "silent";            // silence an alarm (but don't dismiss it)
    public static final String ACTION_DISMISS = "dismiss";          // dismiss an alarm
    public static final String ACTION_SNOOZE = "snooze";            // snooze an alarm
    public static final String ACTION_SCHEDULE = "schedule";        // enable (schedule) an alarm
    public static final String ACTION_DISABLE = "disable";          // disable an alarm
    public static final String ACTION_TIMEOUT = "timeoute";         // timeout an alarm

    public static final String EXTRA_NOTIFICATION_ID = "notificationID";
    public static final String ALARM_NOTIFICATION_TAG = "suntimesalarm";

    public static final String PREF_KEY_ALARM_SILENCEAFTER = "app_alarms_silenceafter";
    public static final long PREF_DEF_ALARM_SILENCEAFTER = 10 * 60 * 1000;   // 10 min

    public static final String PREF_KEY_ALARM_SNOOZE = "app_alarms_snooze";
    public static final long PREF_DEF_ALARM_SNOOZE = 10 * 60 * 1000;   // 10 min

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

        if (action != null)
        {
            if (action.equals(ACTION_DISMISS) || action.equals(ACTION_SNOOZE) ||
                    action.equals(ACTION_DISABLE) || action.equals(ACTION_TIMEOUT))
            {
                Intent dismissNotification = NotificationService.getDismissIntent(context, data);
                context.startService(dismissNotification);

                if (action.equals(ACTION_DISMISS))
                {
                    Intent dismissNotificationDrawer =  new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    context.sendBroadcast(dismissNotificationDrawer);
                }

            } else if (action.equals(ACTION_SILENT)) {
                Intent silenceNotification = NotificationService.getSilenceIntent(context, data);
                context.startService(silenceNotification);
            }

            AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(context);
            itemTask.setAlarmItemTaskListener(createAlarmOnReceiveListener(context, action));
            itemTask.execute(ContentUris.parseId(data));
        }
    }

    /**
     * @param context
     * @return
     */
    private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener showAlarmListOnAlarmChanged(final Context context)
    {
        return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener() {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item) {
                Intent intent = new Intent(context, AlarmClockActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        };
    }

    private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener signalFullscreenActivityOnAlarmChanged(final Context context, final Uri data)
    {
        return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
        {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item)
            {
                if (result)
                {
                    Log.d(TAG, "trigger fullscreenActivity update: " + data);
                    Intent intent = new Intent(AlarmDismissActivity.BROADCAST_UPDATE);
                    intent.setData(data);
                    context.sendBroadcast(intent);

                    Log.d(TAG, "trigger notification update: " + data);
                    Intent snoozeNotification = NotificationService.getSnoozeIntent(context, data);
                    context.startService(snoozeNotification);
                }
            }
        };
    }

    /**
     */
    private AlarmDatabaseAdapter.AlarmStateUpdateTask.AlarmStateUpdateTaskListener performActionOnStateChanged(final Context context, final String action, final AlarmClockItem item)
    {
        return new AlarmDatabaseAdapter.AlarmStateUpdateTask.AlarmStateUpdateTaskListener()
        {
            @Override
            public void onFinished(Boolean result)
            {
                Intent intent = getAlarmIntent(context, item.getUri(), (int)item.rowID);
                intent.setAction(action);
                context.sendBroadcast(intent);
            }
        };
    }

    /**
     */
    private AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener createAlarmOnReceiveListener(final Context context, final String action)
    {
        return new AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener()
        {
            @Override
            public void onItemLoaded(final AlarmClockItem item)
            {
                if (item != null)
                {
                    AlarmDatabaseAdapter.AlarmStateUpdateTask updateState = new AlarmDatabaseAdapter.AlarmStateUpdateTask(context);
                    AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);

                    if (action.equals(ACTION_DISMISS) || action.equals(ACTION_TIMEOUT))
                    {
                        ////////////////////////////////////////////////////////////////////////////
                        // Dismiss Alarm
                        ////////////////////////////////////////////////////////////////////////////
                        int nextState = (action.equals(ACTION_TIMEOUT) ? AlarmState.STATE_TIMEOUT : AlarmState.STATE_DISMISSED);
                        if (AlarmState.transitionState(item.state, nextState))
                        {
                            if (nextState == AlarmState.STATE_TIMEOUT)
                            {
                                Intent intent = getFullScreenIntent(context, item.getUri(), (int)item.rowID);
                                intent.setAction(AlarmNotifications.ACTION_TIMEOUT);
                                context.startActivity(intent);
                            }
                            AlarmState.transitionState(item.state, AlarmState.STATE_NONE);

                            final String action;
                            if (!item.repeating)
                            {
                                Log.i(TAG, "Dismissed: Non-repeating; disabling.. " + item.rowID);
                                action = ACTION_DISABLE;

                            } else {
                                Log.i(TAG, "Dismissed: Repeating; re-scheduling.." + item.rowID);
                                action = ACTION_SCHEDULE;
                            }

                            AlarmDatabaseAdapter.AlarmStateUpdateTask.AlarmStateUpdateTaskListener performAction = performActionOnStateChanged(context, action, item);
                            if (item.state != null)
                            {
                                updateState.setTaskListener(performAction);
                                updateState.execute(item.state);
                            }
                        }

                    } else if (action.equals(ACTION_TIMEOUT)) {
                        ////////////////////////////////////////////////////////////////////////////
                        // Timeout Alarm
                        ////////////////////////////////////////////////////////////////////////////
                        if (AlarmState.transitionState(item.state, AlarmState.STATE_TIMEOUT))
                        {
                            Log.i(TAG, "Timeout: " + item.rowID);
                            item.modified = true;
                            updateItem.setTaskListener(signalFullscreenActivityOnAlarmChanged(context, item.getUri()));
                            updateItem.execute(item);
                        }

                    } else if (action.equals(ACTION_DISABLE)) {
                        ////////////////////////////////////////////////////////////////////////////
                        // Disable Alarm
                        ////////////////////////////////////////////////////////////////////////////
                        if (AlarmState.transitionState(item.state, AlarmState.STATE_DISABLED))
                        {
                            Log.i(TAG, "Disabled: " + item.rowID);
                            item.enabled = false;
                            item.modified = true;
                            updateItem.setTaskListener(showAlarmListOnAlarmChanged(context));
                            updateItem.execute(item);
                        }

                    } else if (action.equals(ACTION_SCHEDULE)) {
                        ////////////////////////////////////////////////////////////////////////////
                        // Schedule Alarm
                        ////////////////////////////////////////////////////////////////////////////
                        if (AlarmState.transitionState(item.state, AlarmState.STATE_NONE))
                        {
                            // TODO: SCHEDULED_DISTANT vs SCHEDULED..
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_SCHEDULED_DISTANT))
                            {
                                // TODO: schedule alarm, set timestamp values on item
                                Log.i(TAG, "Scheduled: " + item.rowID + ", " + item.timestamp);
                                showAlarmEnabledMessage(context, item);

                                AlarmDatabaseAdapter.AlarmStateUpdateTask.AlarmStateUpdateTaskListener testShow = performActionOnStateChanged(context, AlarmNotifications.ACTION_SHOW, item);
                                if (item.state != null) {
                                    updateState.setTaskListener(testShow);   // test by triggering immediately // TODO: remove this block, replace w/ AlarmManager
                                    updateState.execute(item.state);
                                }
                            }
                        }

                    } else if (action.equals(ACTION_SNOOZE) && item.type == AlarmClockItem.AlarmType.ALARM) {
                        ////////////////////////////////////////////////////////////////////////////
                        // Snooze Alarm
                        ////////////////////////////////////////////////////////////////////////////
                        if (AlarmState.transitionState(item.state, AlarmState.STATE_SNOOZING))
                        {
                            long snooze = loadSnoozePref(context);
                            long snoozeAlarmTime = item.timestamp + snooze;  // TODO
                            Log.i(TAG, "Snoozing: " + item.rowID + ", " + snoozeAlarmTime);

                            // TODO: schedule snoozed alarm
                            //AlarmState.transitionState(item.state, AlarmState.STATE_DISMISSED);  // TODO: remove this line, replace w/ AlarmManager
                            item.modified = true;
                            updateItem.setTaskListener(signalFullscreenActivityOnAlarmChanged(context, item.getUri()));
                            updateItem.execute(item);
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
                                Intent showNotification = NotificationService.getShowIntent(context, item.getUri());
                                context.startService(showNotification);

                            } else {
                                Log.i(TAG, "Show: " + item.rowID + "(Notification)");
                                showNotification(context, item);
                            }

                            if (item.state != null) {
                                updateState.execute(item.state);
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * showAlarmEnabledMessage
     * @param context
     * @param item
     */
    protected static void showAlarmEnabledMessage(@NonNull Context context, @NonNull AlarmClockItem item)
    {
        Calendar now = Calendar.getInstance();
        SuntimesUtils.initDisplayStrings(context);
        SuntimesUtils.TimeDisplayText alarmText = utils.timeDeltaLongDisplayString(now.getTimeInMillis(), item.timestamp);
        String alarmString = context.getString(R.string.alarmenabled_toast, alarmText.getValue());
        SpannableString alarmDisplay = SuntimesUtils.createBoldSpan(null, alarmString, alarmText.getValue());
        Toast msg = Toast.makeText(context, alarmDisplay, Toast.LENGTH_SHORT);
        msg.show();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Start playing sound / vibration for given alarm.
     * @param context
     * @param alarm AlarmClockItem
     */
    public static void startAlert(@NonNull final Context context, @NonNull AlarmClockItem alarm)
    {
        initPlayer(context,false);
        if (isPlaying) {
            stopAlert(context);
        }
        isPlaying = true;

        if (alarm.vibrate && vibrator != null)
        {
            int repeatFrom = (alarm.type == AlarmClockItem.AlarmType.ALARM ? 0 : -1);
            vibrator.vibrate(loadDefaultVibratePattern(context, alarm.type), repeatFrom);
        }

        Uri soundUri = ((alarm.ringtoneURI != null && !alarm.ringtoneURI.isEmpty()) ? Uri.parse(alarm.ringtoneURI) : null);
        if (soundUri != null)
        {
            final boolean isAlarm = (alarm.type == AlarmClockItem.AlarmType.ALARM);
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
                        mediaPlayer.setNextMediaPlayer(null);
                        if (audioManager != null) {
                            audioManager.requestAudioFocus(null, streamType, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                        }
                        mediaPlayer.start();
                    }
                });
                player.prepareAsync();

            } catch (IOException e) {
                Log.e(TAG, "startAlert: Failed to setDataSource! " + soundUri.toString());
            }
        }
    }

    /**
     * Stop playing sound / vibration.
     */
    public static void stopAlert(Context context)
    {
        if (vibrator != null) {
            vibrator.cancel();
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

    private static boolean isPlaying = false;
    private static MediaPlayer player = null;
    private static Vibrator vibrator = null;
    private static AudioManager audioManager;
    private static void initPlayer(final Context context, boolean reinit)
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
                        stopAlert(context);                            // so manually stop them after playing once
                    }
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static long loadSilenceAfterPref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getLong(PREF_KEY_ALARM_SILENCEAFTER, PREF_DEF_ALARM_SILENCEAFTER);
    }

    public static long loadSnoozePref(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getLong(PREF_KEY_ALARM_SNOOZE, PREF_DEF_ALARM_SNOOZE);
    }

    public static long[] loadDefaultVibratePattern(Context context, AlarmClockItem.AlarmType type)
    {
        switch (type)
        {
            case NOTIFICATION:
            case ALARM:
            default:                    // TODO
                return new long[] {0, 400, 200, 400, 800};   // 0 immediate start, 400ms buzz, 200ms break, 400ms buzz, 800ms break [repeat]
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static Intent getFullScreenIntent(Context context, Uri data, int notificationID)
    {
        Intent intent = new Intent(context, AlarmDismissActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(data);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationID);
        return intent;
    }

    public static Intent getAlarmIntent(Context context, Uri data, int notificationID)
    {
        Intent intent = new Intent(context, AlarmNotifications.class);
        intent.setData(data);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationID);
        return intent;
    }

    /**
     * createNotification
     * @param context Context
     * @param alarm AlarmClockItem
     */
    public static Notification createNotification(Context context, @NonNull AlarmClockItem alarm, int notificationID)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        String emptyLabel = ((alarm.event != null) ? alarm.event.getShortDisplayString() : context.getString(R.string.alarmOption_solarevent_none));
        String notificationTitle = (alarm.label == null || alarm.label.isEmpty() ? emptyLabel : alarm.label);
        String notificationMsg = notificationTitle;
        int notificationIcon = ((alarm.type == AlarmClockItem.AlarmType.NOTIFICATION) ? R.drawable.ic_action_notification : R.drawable.ic_action_alarms);
        int notificationColor = ContextCompat.getColor(context, R.color.sunIcon_color_setting_dark);

        builder.setDefaults( Notification.DEFAULT_LIGHTS );
        //builder.setStyle(new NotificationCompat.MediaStyle());

        if (alarm.type == AlarmClockItem.AlarmType.ALARM)
        {
            // ALARM
            builder.setCategory( NotificationCompat.CATEGORY_ALARM );
            builder.setPriority( NotificationCompat.PRIORITY_MAX );
            builder.setOngoing(true);
            builder.setAutoCancel(false);

            Intent fullScreenIntent = getFullScreenIntent(context, alarm.getUri(), notificationID);
            PendingIntent alarmFullScreen = PendingIntent.getActivity(context, alarm.hashCode(), fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent snoozeIntent = getAlarmIntent(context, alarm.getUri(), notificationID);
            snoozeIntent.setAction(ACTION_SNOOZE);
            PendingIntent pendingSnooze = PendingIntent.getBroadcast(context, alarm.hashCode(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent dismissIntent = getAlarmIntent(context, alarm.getUri(), notificationID);
            dismissIntent.setAction(ACTION_DISMISS);
            PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, alarm.hashCode(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setFullScreenIntent(alarmFullScreen, true);       // at discretion of system to use this intent (or to show a heads up notification instead)

            if (alarm.state != null)
            {
                int alarmState = alarm.state.getState();
                switch (alarmState)
                {
                    case AlarmState.STATE_SNOOZING:
                        notificationMsg = "Snoozing";  // TODO
                        notificationIcon = R.drawable.ic_action_snooze;
                        builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), pendingDismiss);
                        break;

                    case AlarmState.STATE_TIMEOUT:
                        notificationMsg = "Timed out";  // TODO
                        break;

                    default:
                        builder.addAction(R.drawable.ic_action_snooze, context.getString(R.string.alarmAction_snooze), pendingSnooze);
                        builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), pendingDismiss);
                        break;
                }
            } else {
                builder.addAction(R.drawable.ic_action_snooze, context.getString(R.string.alarmAction_snooze), pendingSnooze);
                builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), pendingDismiss);
            }

        } else {
            // NOTIFICATION
            builder.setCategory( NotificationCompat.CATEGORY_REMINDER );
            builder.setPriority( NotificationCompat.PRIORITY_HIGH );
            builder.setOngoing(false);
            builder.setAutoCancel(true);

            Intent dismissIntent = getAlarmIntent(context, alarm.getUri(), notificationID);
            dismissIntent.setAction(ACTION_DISMISS);
            PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, notificationID, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    /**
     * showNotification
     * Use this method to display the notification without a foreground service.
     * @see NotificationService to display a notification that lives longer than the receiver.
     * @param context
     * @param item
     */
    public static void showNotification(Context context, @NonNull AlarmClockItem item)
    {
        int notificationID = (int)item.rowID;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = createNotification(context, item, notificationID);
        notificationManager.notify(ALARM_NOTIFICATION_TAG, notificationID, notification);
        startAlert(context, item);
    }

    /**public static void updateNotification(Context context, @NonNull AlarmClockItem item)
    {
        int notificationID = (int)item.rowID;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = createNotification(context, item, notificationID);
        notificationManager.notify(ALARM_NOTIFICATION_TAG, notificationID, notification);
    }*/

    /**
     * dismissNotification
     * Use this method to dismiss notifications not started as a foreground service.
     * @param context
     * @param notificationID
     */
    public static void dismissNotification(Context context, int notificationID)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(ALARM_NOTIFICATION_TAG, notificationID);
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
        public int onStartCommand(Intent intent, int flags, int startId)
        {
            super.onStartCommand(intent, flags, startId);

            String action = intent.getAction();
            Uri data = intent.getData();
            if (AlarmNotifications.ACTION_SHOW.equals(action) && data != null)
            {
                Log.d(TAG, "ACTION_SHOW");
                final long notificationID = ContentUris.parseId(data);
                AlarmDatabaseAdapter.AlarmItemTask alarmTask = new AlarmDatabaseAdapter.AlarmItemTask(getApplicationContext());
                alarmTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener()
                {
                    @Override
                    public void onItemLoaded(AlarmClockItem alarm) {
                        Context context = getApplicationContext();
                        Notification notification = AlarmNotifications.createNotification(context, alarm, (int)notificationID);
                        startForeground((int)notificationID, notification);
                        AlarmNotifications.startAlert(context, alarm);
                    }
                });
                alarmTask.execute(notificationID);

            } else if (AlarmNotifications.ACTION_DISMISS.equals(action) && data != null) {
                long notificationID = ContentUris.parseId(data);
                Log.d(TAG, "ACTION_DISMISS: " + notificationID);
                AlarmNotifications.stopAlert(getApplicationContext());
                stopForeground(true);

            } else if (AlarmNotifications.ACTION_SILENT.equals(action)) {
                Log.d(TAG, "ACTION_SILENT");
                AlarmNotifications.stopAlert(getApplicationContext());

            } else if (AlarmNotifications.ACTION_SNOOZE.equals(action) && data != null) {
                Log.d(TAG, "ACTION_SNOOZE");
                final long notificationID = ContentUris.parseId(data);
                AlarmDatabaseAdapter.AlarmItemTask alarmTask = new AlarmDatabaseAdapter.AlarmItemTask(getApplicationContext());
                alarmTask.setAlarmItemTaskListener(updateNotificationTask((int)notificationID));
                alarmTask.execute(notificationID);

            } else if (AlarmNotifications.ACTION_TIMEOUT.equals(action) && data != null) {
                Log.d(TAG, "ACTION_TIMEOUT");
                final long notificationID = ContentUris.parseId(data);
                AlarmDatabaseAdapter.AlarmItemTask alarmTask = new AlarmDatabaseAdapter.AlarmItemTask(getApplicationContext());
                alarmTask.setAlarmItemTaskListener(updateNotificationTask((int)notificationID));
                alarmTask.execute(notificationID);

            } else {
                Log.w(TAG, "Unrecognized action: " + action);
            }
            return START_STICKY;
        }

        private AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener updateNotificationTask(final int notificationID)
        {
            return new AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener()
            {
                @Override
                public void onItemLoaded(AlarmClockItem alarm) {
                    Context context = getApplicationContext();
                    Notification notification = AlarmNotifications.createNotification(context, alarm, (int)notificationID);
                    startForeground((int)notificationID, notification);
                }
            };
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent)
        {
            return null;
        }

        public static Intent getShowIntent(Context context, Uri data)
        {
            Intent intent = getNotificationIntent(context, data);
            intent.setAction(ACTION_SHOW);
            return intent;
        }

        public static Intent getDismissIntent(Context context, Uri data)
        {
            Intent intent = getNotificationIntent(context, data);
            intent.setAction(ACTION_DISMISS);
            return intent;
        }

        public static Intent getSilenceIntent(Context context, Uri data)
        {
            Intent intent = getNotificationIntent(context, data);
            intent.setAction(ACTION_SILENT);
            return intent;
        }

        public static Intent getSnoozeIntent(Context context, Uri data)
        {
            Intent intent = getNotificationIntent(context, data);
            intent.setAction(ACTION_SNOOZE);
            return intent;
        }

        public static Intent getTimeoutIntent(Context context, Uri data)
        {
            Intent intent = getNotificationIntent(context, data);
            intent.setAction(ACTION_TIMEOUT);
            return intent;
        }

        private static Intent getNotificationIntent(Context context, Uri data)
        {
            Intent intent = new Intent(context, NotificationService.class);
            intent.setData(data);
            return intent;
        }
    }

}
