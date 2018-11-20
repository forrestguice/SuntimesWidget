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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;

import android.content.Context;
import android.content.Intent;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
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

    public static final String ACTION_SHOW = "suntimeswidget.alarm.show";                // sound an alarm
    public static final String ACTION_SILENT = "suntimeswidget.alarm.silent";            // silence an alarm (but don't dismiss it)
    public static final String ACTION_DISMISS = "suntimeswidget.alarm.dismiss";          // dismiss an alarm
    public static final String ACTION_SNOOZE = "suntimeswidget.alarm.snooze";            // snooze an alarm
    public static final String ACTION_SCHEDULE = "suntimeswidget.alarm.schedule";        // enable (schedule) an alarm
    public static final String ACTION_DISABLE = "suntimeswidget.alarm.disable";          // disable an alarm
    public static final String ACTION_TIMEOUT = "suntimeswidget.alarm.timeout";         // timeout an alarm

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
        String dataTag = intent.getStringExtra("tag");
        Log.d(TAG, "onReceive: " + action + ", " + data + " (" + dataTag + ")");
        if (action != null) {
            context.startService(NotificationService.getNotificationIntent(context, action, data));
        } else Log.w(TAG, "onReceive: null action!");
    }

    /**
     * @param context
     * @param item
     */
    protected static void showAlarmEnabledToast(Context context, @NonNull AlarmClockItem item)
    {
        if (context != null)
        {
            Calendar now = Calendar.getInstance();
            SuntimesUtils.initDisplayStrings(context);
            SuntimesUtils.TimeDisplayText alarmText = utils.timeDeltaLongDisplayString(now.getTimeInMillis(), item.timestamp);
            String alarmString = context.getString(R.string.alarmenabled_toast, alarmText.getValue());
            SpannableString alarmDisplay = SuntimesUtils.createBoldSpan(null, alarmString, alarmText.getValue());
            Toast msg = Toast.makeText(context, alarmDisplay, Toast.LENGTH_SHORT);
            msg.show();

        } else Log.e(TAG, "showAlarmEnabledToast: context is null!");
    }

    /**
     */
    protected static void showAlarmSilencedToast(Context context, Uri data)
    {
        if (context != null) {
            Toast msg = Toast.makeText(context, context.getString(R.string.alarmAction_silencedMsg), Toast.LENGTH_SHORT);
            msg.show();

        } else Log.e(TAG, "showAlarmSilencedToast: context is null!");
    }

    /**
     */
    protected static void showAlarmPlayingToast(Context context, Uri data)
    {
        if (context != null) {
            Toast msg = Toast.makeText(context, context.getString(R.string.alarmAction_playingMsg), Toast.LENGTH_SHORT);
            msg.show();

        } else Log.e(TAG, "showAlarmPlayingToast: context is null!");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected static void addAlarmSnooze(Context context, Uri data)
    {
        Log.d(TAG, "addAlarmSnooze: " + data);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            long snoozeMillis = AlarmSettings.loadPrefAlarmSnooze(context);
            long alarmAt = Calendar.getInstance().getTimeInMillis() + snoozeMillis;

            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmAt, getPendingIntent(context, AlarmNotifications.ACTION_SHOW, data, "addAlarmPlay"));
            } else alarmManager.set(AlarmManager.RTC_WAKEUP, alarmAt, getPendingIntent(context, AlarmNotifications.ACTION_SHOW, data, "addAlarmPlay"));

        } else Log.e(TAG, "addAlarmSnooze: AlarmManager is null!");
    }

    protected static void addAlarmTimeouts(Context context, Uri data, int notificationID)
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

                    if (Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, silenceAt, getPendingIntent(context, AlarmNotifications.ACTION_SILENT, data, "addAlarmTimeouts"));
                    else alarmManager.set(AlarmManager.RTC_WAKEUP, silenceAt, getPendingIntent(context, AlarmNotifications.ACTION_SILENT, data, "addAlarmTimeouts"));
                }

                long timeoutMillis = AlarmSettings.loadPrefAlarmTimeout(context);
                if (timeoutMillis > 0)
                {
                    Log.d(TAG, "addAlarmTimeouts: timeout after " + timeoutMillis);
                    long timeoutAt = Calendar.getInstance().getTimeInMillis() + timeoutMillis;

                    if (Build.VERSION.SDK_INT >= 19)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeoutAt, getPendingIntent(context, AlarmNotifications.ACTION_TIMEOUT, data, "addAlarmTimeouts"));
                    else alarmManager.set(AlarmManager.RTC_WAKEUP, timeoutAt, getPendingIntent(context, AlarmNotifications.ACTION_TIMEOUT, data, "addAlarmTimeouts"));
                }

            } else Log.e(TAG, "addAlarmTimeout: AlarmManager is null!");
        } else Log.e(TAG, "addAlarmTimeout: context is null!");
    }

    protected static void cancelAlarmShow(Context context, Uri data)
    {
        if (context != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Log.d(TAG, "cancelAlarmShow: " + data);
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SHOW, data, "addAlarmPlay"));
            } else Log.e(TAG, "cancelAlarmShow: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmShow: context is null!");
    }

    protected static void cancelAlarmSilence(Context context, Uri data)
    {
        if (context != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Log.d(TAG, "cancelAlarmSilence: " + data);
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SILENT, data, "addAlarmTimeouts"));
            } else Log.e(TAG, "cancelAlarmSilence: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmSilence: context is null!");
    }

    protected static void cancelAlarmTimeouts(Context context, Uri data)
    {
        if (context != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Log.d(TAG, "cancelAlarmTimeouts: " + data);
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SILENT, data, "addAlarmTimeouts"));
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_TIMEOUT, data, "addAlarmTimeouts"));
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SHOW, data, "addAlarmTimeouts"));
            } else Log.e(TAG, "cancelAlarmTimeouts: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmTimeouts: context is null!");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        Intent intent = new Intent(AlarmDismissActivity.BROADCAST_UPDATE);
        intent.setData(data);
        return intent;
    }

    public static Intent getAlarmListIntent(Context context)
    {
        Intent intent = new Intent(context, AlarmClockActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        intent.putExtra(EXTRA_NOTIFICATION_ID, (int)ContentUris.parseId(data));
        return intent;
    }

    public static PendingIntent getPendingIntent(Context context, String action, Uri data, String tag)
    {
        Intent intent = getAlarmIntent(context, action, data);
        intent.putExtra("tag", tag);
        return PendingIntent.getBroadcast(context, (int)ContentUris.parseId(data), intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
            vibrator.vibrate(AlarmSettings.loadDefaultVibratePattern(context, alarm.type), repeatFrom);
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
        stopAlert(true);
    }
    public static void stopAlert(boolean stopVibrate)
    {
        if (vibrator != null && stopVibrate) {
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

        Intent dismissIntent = getAlarmIntent(context, ACTION_DISMISS, alarm.getUri());
        dismissIntent.putExtra("tag", "notification");
        PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, alarm.hashCode(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarm.type == AlarmClockItem.AlarmType.ALARM)
        {
            // ALARM
            builder.setCategory( NotificationCompat.CATEGORY_ALARM );
            builder.setPriority( NotificationCompat.PRIORITY_MAX );
            builder.setOngoing(true);
            builder.setAutoCancel(false);

            Intent fullScreenIntent = getFullscreenIntent(context, alarm.getUri());
            PendingIntent alarmFullscreen = PendingIntent.getActivity(context, (int)alarm.rowID, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent snoozeIntent = getAlarmIntent(context, ACTION_SNOOZE, alarm.getUri());
            snoozeIntent.putExtra("tag", "notification");
            PendingIntent pendingSnooze = PendingIntent.getBroadcast(context, (int)alarm.rowID, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setFullScreenIntent(alarmFullscreen, true);       // at discretion of system to use this intent (or to show a heads up notification instead)

            if (alarm.state != null)
            {
                int alarmState = alarm.state.getState();
                switch (alarmState)
                {
                    case AlarmState.STATE_SNOOZING:
                        SuntimesUtils.initDisplayStrings(context);
                        SuntimesUtils.TimeDisplayText snoozeText = utils.timeDeltaLongDisplayString(0, AlarmSettings.loadPrefAlarmSnooze(context));
                        notificationMsg = context.getString(R.string.alarmAction_snoozeMsg, snoozeText.getValue());
                        notificationIcon = R.drawable.ic_action_snooze;
                        break;

                    case AlarmState.STATE_TIMEOUT:
                        notificationMsg = context.getString(R.string.alarmAction_timeoutMsg);
                        notificationIcon = R.drawable.ic_action_timeout;
                        break;

                    default:
                        builder.addAction(R.drawable.ic_action_snooze, context.getString(R.string.alarmAction_snooze), pendingSnooze);
                        break;
                }
                builder.addAction(R.drawable.ic_action_cancel, context.getString(R.string.alarmAction_dismiss), pendingDismiss);

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
                showAlarmPlayingToast(getApplicationContext(), data);

            } else if (AlarmNotifications.ACTION_DISMISS.equals(action) && data != null) {
                Log.d(TAG, "ACTION_DISMISS: " + data);
                AlarmNotifications.stopAlert(getApplicationContext());

            } else if (AlarmNotifications.ACTION_DISABLE.equals(action) && data != null) {
                Log.d(TAG, "ACTION_DISABLE: " + data);
                AlarmNotifications.stopAlert(getApplicationContext());

            } else if (AlarmNotifications.ACTION_SILENT.equals(action)) {
                Log.d(TAG, "ACTION_SILENT: " + data);
                AlarmNotifications.stopAlert(false);
                showAlarmSilencedToast(getApplicationContext(), data);

            } else if (AlarmNotifications.ACTION_SNOOZE.equals(action) && data != null) {
                Log.d(TAG, "ACTION_SNOOZE: " + data);
                AlarmNotifications.stopAlert(getApplicationContext());

            } else if (AlarmNotifications.ACTION_TIMEOUT.equals(action) && data != null) {
                Log.d(TAG, "ACTION_TIMEOUT: " + data);
                AlarmNotifications.stopAlert(getApplicationContext());

            } else {
                Log.w(TAG, "Unrecognized action: " + action);
            }

            AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(getApplicationContext());
            itemTask.setAlarmItemTaskListener(createAlarmOnReceiveListener(getApplicationContext(), action));
            itemTask.execute(ContentUris.parseId(data));
            return START_STICKY;
        }

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
        private AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener createAlarmOnReceiveListener(final Context context, final String action)
        {
            return new AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener()
            {
                @Override
                public void onItemLoaded(final AlarmClockItem item)
                {
                    if (item != null)
                    {
                        if (context == null) {
                            Log.e(TAG, "Null context!");
                        }

                        if (action.equals(ACTION_DISMISS))
                        {
                            ////////////////////////////////////////////////////////////////////////////
                            // Dismiss Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_DISMISSED))
                            {
                                cancelAlarmTimeouts(context, item.getUri());

                                AlarmState.transitionState(item.state, AlarmState.STATE_NONE);
                                final String nextAction;
                                if (!item.repeating)
                                {
                                    Log.i(TAG, "Dismissed: Non-repeating; disabling.. " + item.rowID);
                                    nextAction = ACTION_DISABLE;

                                } else {
                                    Log.i(TAG, "Dismissed: Repeating; re-scheduling.." + item.rowID);
                                    nextAction = ACTION_SCHEDULE;
                                }

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
                                updateItem.setTaskListener(onDismissedState(context, nextAction, item.getUri()));
                                updateItem.execute(item);    // write state
                            }

                        } else if (action.equals(ACTION_SILENT) && item.type == AlarmClockItem.AlarmType.ALARM) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Silenced Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            Log.i(TAG, "Silenced: " + item.rowID);
                            cancelAlarmSilence(context, item.getUri());    // cancel upcoming silence timeout; if user silenced alarm there may be another silence scheduled

                        } else if (action.equals(ACTION_TIMEOUT ) && item.type == AlarmClockItem.AlarmType.ALARM) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Timeout Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_TIMEOUT))
                            {
                                Log.i(TAG, "Timeout: " + item.rowID);
                                cancelAlarmTimeouts(context, item.getUri());

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
                                updateItem.setTaskListener(onTimeoutState(context));
                                updateItem.execute(item);  // write state
                            }

                        } else if (action.equals(ACTION_DISABLE)) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Disable Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_DISABLED))
                            {
                                Log.i(TAG, "Disabled: " + item.rowID);
                                cancelAlarmTimeouts(context, item.getUri());

                                item.enabled = false;
                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
                                updateItem.setTaskListener(onDisabledState(context));
                                updateItem.execute(item);    // write state
                            }

                        } else if (action.equals(ACTION_SCHEDULE)) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Schedule Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_NONE))
                            {
                                cancelAlarmTimeouts(context, item.getUri());

                                // TODO: SCHEDULED_DISTANT vs SCHEDULED..
                                if (AlarmState.transitionState(item.state, AlarmState.STATE_SCHEDULED_DISTANT))
                                {
                                    // TODO: schedule alarm, set timestamp values on item
                                    Log.i(TAG, "Scheduled: " + item.rowID + ", " + item.timestamp);
                                    showAlarmEnabledToast(context, item);

                                    if (item.state != null)
                                    {
                                        AlarmDatabaseAdapter.AlarmStateUpdateTask updateState = new AlarmDatabaseAdapter.AlarmStateUpdateTask(context);
                                        updateState.setTaskListener(new AlarmDatabaseAdapter.AlarmStateUpdateTask.AlarmStateUpdateTaskListener()
                                        {
                                            @Override
                                            public void onFinished(Boolean result)
                                            {
                                                Intent intent = getAlarmIntent(context, ACTION_SHOW, item.getUri());
                                                intent.putExtra("tag", "performActionOnStateChanged");
                                                context.sendBroadcast(intent);
                                            }
                                        });   // test by triggering immediately // TODO: remove this block, replace w/ AlarmManager
                                        updateState.execute(item.state);  // write state
                                    }
                                }
                            }

                        } else if (action.equals(ACTION_SNOOZE) && item.type == AlarmClockItem.AlarmType.ALARM) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Snooze Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_SNOOZING))
                            {
                                Log.i(TAG, "Snoozing: " + item.rowID);
                                cancelAlarmTimeouts(context, item.getUri());
                                addAlarmSnooze(context, item.getUri());

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
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
                                    cancelAlarmTimeouts(context, item.getUri());
                                    addAlarmTimeouts(context, item.getUri(), (int)item.rowID);

                                } else {
                                    Log.i(TAG, "Show: " + item.rowID + "(Notification)");
                                    showNotification(context, item);
                                }

                                item.modified = true;
                                AlarmDatabaseAdapter.AlarmUpdateTask updateItem = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
                                updateItem.setTaskListener(onShowState(context));
                                updateItem.execute(item);     // write state
                            }
                        }
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onDismissedState(final Context context, final String nextAction, final Uri data)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onDismissed)");
                    sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));   // dismiss notification tray

                    if (nextAction != null) {
                        Intent intent = getAlarmIntent(context, nextAction, data);
                        intent.putExtra("tag", "performActionOnStateChanged");
                        context.sendBroadcast(intent);  // trigger followup action

                    } else {
                        stopForeground(true);   // remove notification (will kill running tasks)
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onSnoozeState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (result)
                    {
                        Log.d(TAG, "State Saved (onSnooze)");
                        Notification notification = AlarmNotifications.createNotification(context, item, (int)item.rowID);
                        startForeground((int)item.rowID, notification);  // update notification
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));  // update fullscreen activity
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onTimeoutState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onTimeout)");
                    Notification notification = AlarmNotifications.createNotification(context, item, (int)item.rowID);
                    startForeground((int)item.rowID, notification);  // update notification
                    context.sendBroadcast(getFullscreenBroadcast(item.getUri()));  // update fullscreen activity
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onShowState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onShow)");
                    Context context = getApplicationContext();
                    Notification notification = AlarmNotifications.createNotification(context, item, (int)item.rowID);
                    startForeground((int)item.rowID, notification);        // trigger the notification
                    AlarmNotifications.startAlert(context, item);          // play sound/vibration
                    context.sendBroadcast(getFullscreenBroadcast(item.getUri()));   // update fullscreen activity
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onDisabledState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onDisabled)");
                    context.startActivity(getAlarmListIntent(context));   // open the alarm list
                    stopForeground(true);     // remove notification (will kill running tasks)
                }
            };
        }

    }

}
