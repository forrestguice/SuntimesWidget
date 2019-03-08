/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;

import android.content.Context;
import android.content.Intent;

import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmDismissActivity;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class AlarmNotifications extends BroadcastReceiver
{
    public static final String TAG = "AlarmReceiver";

    public static final String ACTION_SHOW = "suntimeswidget.alarm.show";                // sound an alarm
    public static final String ACTION_SILENT = "suntimeswidget.alarm.silent";            // silence an alarm (but don't dismiss it)
    public static final String ACTION_DISMISS = "suntimeswidget.alarm.dismiss";          // dismiss an alarm
    public static final String ACTION_SNOOZE = "suntimeswidget.alarm.snooze";            // snooze an alarm
    public static final String ACTION_SCHEDULE = "suntimeswidget.alarm.schedule";        // enable (schedule) an alarm
    public static final String ACTION_RESCHEDULE = "suntimeswidget.alarm.reschedule";    // reschedule; same as schedule but prev alarmtime is cleared
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
    public static void showTimeUntilToast(Context context, View view, @NonNull AlarmClockItem item)
    {
        if (context != null)
        {
            Calendar now = Calendar.getInstance();
            SuntimesUtils.initDisplayStrings(context);
            SuntimesUtils.TimeDisplayText alarmText = utils.timeDeltaLongDisplayString(now.getTimeInMillis(), item.timestamp + item.offset);
            String alarmString = context.getString(R.string.alarmenabled_toast, item.type.getDisplayString(), alarmText.getValue());
            SpannableString alarmDisplay = SuntimesUtils.createBoldSpan(null, alarmString, alarmText.getValue());
            Snackbar snackbar = Snackbar.make(view, alarmDisplay, Toast.LENGTH_SHORT);
            themeSnackbar(context, snackbar, null);
            snackbar.show();
            //Toast.makeText(context, alarmDisplay, Toast.LENGTH_SHORT).show();

        } else Log.e(TAG, "showTimeUntilToast: context is null!");
    }

    @SuppressLint("ResourceType")
    public static void themeSnackbar(Context context, Snackbar snackbar, Integer[] colorOverrides)
    {
        Integer[] colors = new Integer[] {null, null, null};
        int[] colorAttrs = { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor };
        TypedArray a = context.obtainStyledAttributes(colorAttrs);
        colors[0] = ContextCompat.getColor(context, a.getResourceId(0, android.R.color.primary_text_dark));
        colors[1] = ContextCompat.getColor(context, a.getResourceId(1, R.color.text_accent_dark));
        colors[2] = ContextCompat.getColor(context, a.getResourceId(2, R.color.card_bg_dark));
        a.recycle();

        if (colorOverrides != null && colorOverrides.length == colors.length) {
            for (int i=0; i<colors.length; i++) {
                if (colorOverrides[i] != null) {
                    colors[i] = colorOverrides[i];
                }
            }
        }

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(colors[2]);
        snackbar.setActionTextColor(colors[1]);

        TextView snackbarText = (TextView)snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        if (snackbarText != null) {
            snackbarText.setTextColor(colors[0]);
            snackbarText.setMaxLines(3);
        }
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

    protected static void cancelAlarmTimeout(Context context, String action, Uri data)
    {
        if (context != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Log.d(TAG, "cancelAlarmTimeout: " + action + ": " + data);
                alarmManager.cancel(getPendingIntent(context, action, data));
            } else Log.e(TAG, "cancelAlarmTimeout: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmTimeout: context is null!");
    }

    protected static void cancelAlarmTimeouts(Context context, Uri data)
    {
        if (context != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Log.d(TAG, "cancelAlarmTimeouts: " + data);
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SILENT, data));
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_TIMEOUT, data));
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SHOW, data));
                alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SCHEDULE, data));
            } else Log.e(TAG, "cancelAlarmTimeouts: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmTimeouts: context is null!");
    }

    protected static void cancelAlarmTimeouts(Context context, Long[] alarmIds)
    {
        if (context != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null)
            {
                for (long alarmId : alarmIds)
                {
                    Uri data = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmId);
                    alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SILENT, data));
                    alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_TIMEOUT, data));
                    alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SHOW, data));
                    alarmManager.cancel(getPendingIntent(context, AlarmNotifications.ACTION_SCHEDULE, data));
                }
            } else Log.e(TAG, "cancelAlarmTimeouts: AlarmManager is null!");
        } else Log.e(TAG, "cancelAlarmTimeouts: context is null!");
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

        if (alarm.vibrate && vibrator != null)
        {
            int repeatFrom = (alarm.type == AlarmClockItem.AlarmType.ALARM ? 0 : -1);
            vibrator.vibrate(AlarmSettings.loadPrefVibratePattern(context, alarm.type), repeatFrom);
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
                        if (Build.VERSION.SDK_INT >= 16) {
                            mediaPlayer.setNextMediaPlayer(null);
                        }
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
    public static void stopAlert()
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

        String emptyLabel = ((alarm.event != null) ? alarm.event.getShortDisplayString() : context.getString(R.string.alarmOption_solarevent_none));
        String notificationTitle = (alarm.label == null || alarm.label.isEmpty() ? emptyLabel : alarm.label);
        String notificationMsg = notificationTitle;
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
                    if (Build.VERSION.SDK_INT < 21)
                    {
                        builder.setCategory( NotificationCompat.CATEGORY_REMINDER );
                        builder.setPriority( NotificationCompat.PRIORITY_LOW );
                        notificationMsg = context.getString(R.string.alarmAction_upcomingMsg);
                        builder.setWhen(alarm.alarmtime);
                        builder.setContentIntent(pendingView);
                        builder.setAutoCancel(false);
                        builder.setOngoing(true);
                    } else return null;  // don't show reminder for api 21+ (uses notification provided by setAlarm instead)
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
        public int onStartCommand(Intent intent, int flags, int startId)
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
                    itemTask.setAlarmItemTaskListener(createAlarmOnReceiveListener(getApplicationContext(), action));
                    itemTask.execute(ContentUris.parseId(data));

                } else {
                    if (AlarmNotifications.ACTION_SCHEDULE.equals(action) || Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_TIME_CHANGED.equals(action))
                    {
                        Log.d(TAG, action + ": schedule all");
                        AlarmDatabaseAdapter.AlarmListTask alarmListTask = new AlarmDatabaseAdapter.AlarmListTask(getApplicationContext());
                        alarmListTask.setParam_enabledOnly(true);
                        alarmListTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                            @Override
                            public void onItemsLoaded(Long[] ids)
                            {
                                for (long id : ids)
                                {
                                    AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(getApplicationContext());
                                    itemTask.setAlarmItemTaskListener(createAlarmOnReceiveListener(getApplicationContext(), AlarmNotifications.ACTION_RESCHEDULE));
                                    itemTask.execute(id);
                                }
                            }
                        });
                        alarmListTask.execute();

                    } else if (AlarmNotifications.ACTION_DELETE.equals(action)) {
                        Log.d(TAG, "ACTION_DELETE: clear all");
                        AlarmNotifications.stopAlert();
                        AlarmDatabaseAdapter.AlarmListTask alarmListTask = new AlarmDatabaseAdapter.AlarmListTask(getApplicationContext());
                        alarmListTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmListTask.AlarmListTaskListener() {
                            @Override
                            public void onItemsLoaded(Long[] ids)
                            {
                                cancelAlarmTimeouts(getApplicationContext(), ids);
                                AlarmDatabaseAdapter.AlarmDeleteTask clearTask = new AlarmDatabaseAdapter.AlarmDeleteTask(getApplicationContext());
                                clearTask.setTaskListener(onClearedState(getApplicationContext()));
                                clearTask.execute();
                            }
                        });
                        alarmListTask.execute();

                    } else Log.w(TAG, "onStartCommand: null data!");
                }
            } else Log.w(TAG, "onStartCommand: null intent!");

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
                                    item.alarmtime = 0;
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
                                cancelAlarmTimeouts(context, item.getUri());

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
                                cancelAlarmTimeouts(context, item.getUri());

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
                                cancelAlarmTimeouts(context, item.getUri());

                                AlarmDatabaseAdapter.AlarmDeleteTask deleteTask = new AlarmDatabaseAdapter.AlarmDeleteTask(context);
                                deleteTask.setTaskListener(onDeletedState(context));
                                deleteTask.execute(item.rowID);
                            }

                        } else if (action.equals(ACTION_SCHEDULE) || (action.equals(ACTION_RESCHEDULE))) {
                            ////////////////////////////////////////////////////////////////////////////
                            // Schedule Alarm
                            ////////////////////////////////////////////////////////////////////////////
                            if (AlarmState.transitionState(item.state, AlarmState.STATE_NONE))
                            {
                                cancelAlarmTimeouts(context, item.getUri());

                                long now = Calendar.getInstance().getTimeInMillis();
                                if (item.alarmtime <= now || item.alarmtime == 0 || action.equals(ACTION_RESCHEDULE))
                                {
                                    // expired alarm/notification
                                    if (item.enabled)    // enabled; reschedule alarm/notification
                                    {
                                        Log.d(TAG, "(Re)Scheduling: " + item.rowID);
                                        updateAlarmTime(context, item);     // sets item.hour, item.minute, item.timestamp (calculates the eventTime)
                                        item.alarmtime = item.timestamp + item.offset;     // scheduled sounding time (-before/+after eventTime by some offset)

                                    } else {    // disabled; this alarm should have been dismissed
                                        Log.d(TAG, "Dismissing: " + item.rowID);
                                        sendBroadcast(getAlarmIntent(context, ACTION_DISMISS, item.getUri()));
                                        return;
                                    }
                                }

                                int nextState = AlarmState.STATE_SCHEDULED_DISTANT;
                                AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onScheduledState;
                                if (item.type == AlarmClockItem.AlarmType.ALARM)
                                {
                                    long reminderWithin = AlarmSettings.loadPrefAlarmUpcoming(context);
                                    boolean verySoon = (((item.alarmtime - now) < reminderWithin) || reminderWithin <= 0);
                                    nextState = (verySoon ? AlarmState.STATE_SCHEDULED_SOON : AlarmState.STATE_SCHEDULED_DISTANT);
                                    if (verySoon)
                                    {
                                        Log.i(TAG, "Scheduling: " + item.rowID + " :: very soon");
                                        onScheduledState = onScheduledSoonState(context);
                                    } else {
                                        Log.i(TAG, "Scheduling: " + item.rowID + " :: distant");
                                        onScheduledState = onScheduledDistantState(context);
                                    }
                                } else {
                                    Log.i(TAG, "Scheduling: " + item.rowID);
                                    onScheduledState = onScheduledNotification(context);
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
                                cancelAlarmTimeouts(context, item.getUri());

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
                                    cancelAlarmTimeouts(context, item.getUri());
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
                        context.sendBroadcast(intent);  // trigger followup action
                    }

                    Intent serviceIntent = getServiceIntent(context);
                    startService(serviceIntent);                                   // keep service running after stopping foreground notification
                    stopForeground(true );    // remove notification (will kill running tasks)
                    dismissNotification(context, (int)item.rowID);                 // dismiss upcoming reminders
                    context.sendBroadcast(getFullscreenBroadcast(item.getUri()));  // dismiss fullscreen activity
                    stopService(serviceIntent);
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

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onTimeoutState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
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

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onShowState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    Log.d(TAG, "State Saved (onShow)");
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        showAlarmPlayingToast(getApplicationContext(), item);
                        context.sendBroadcast(getFullscreenBroadcast(item.getUri()));   // update fullscreen activity
                    }
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
                    Intent serviceIntent = getServiceIntent(context);
                    startService(serviceIntent);  // keep service running after stopping foreground notification
                    stopForeground(true);     // remove notification (will kill running tasks)
                    context.startActivity(getAlarmListIntent(context, item.rowID));   // open the alarm list
                    dismissNotification(context, (int)item.rowID);                    // dismiss upcoming reminders
                    context.sendBroadcast(getFullscreenBroadcast(item.getUri()));     // dismiss fullscreen activity
                    stopService(serviceIntent);
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener onDeletedState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener()
            {
                @Override
                public void onFinished(Boolean result, Long itemID)
                {
                    Log.d(TAG, "Alarm Deleted (onDeleted)");
                    Intent serviceIntent = getServiceIntent(context);
                    startService(serviceIntent);  // keep service running after stopping foreground notification

                    stopForeground(true);                                      // dismiss active notification (will kill running tasks)
                    dismissNotification(context, itemID.intValue());                                                                   // dismiss upcoming reminders
                    context.sendBroadcast(getFullscreenBroadcast(ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, itemID)));     // dismiss fullscreen activity

                    Intent alarmListIntent = getAlarmListIntent(context, itemID);
                    alarmListIntent.setAction(AlarmNotifications.ACTION_DELETE);
                    context.startActivity(alarmListIntent);   // open the alarm list

                    stopService(serviceIntent);
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener onClearedState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmDeleteTask.AlarmClockDeleteTaskListener()
            {
                @Override
                public void onFinished(Boolean result, Long itemID)
                {
                    Log.d(TAG, "Alarms Cleared (onCleared)");
                    Intent serviceIntent = getServiceIntent(context);
                    startService(serviceIntent);  // keep service running after stopping foreground notification

                    context.sendBroadcast(getFullscreenBroadcast(null));     // dismiss fullscreen activity
                    stopForeground(true);                         // dismiss active notifications
                    dismissNotifications(context);                                // dismiss upcoming reminders

                    Intent alarmListIntent = getAlarmListIntent(context, itemID);
                    alarmListIntent.setAction(AlarmNotifications.ACTION_DELETE);
                    context.startActivity(alarmListIntent);   // open the alarm list

                    stopService(serviceIntent);
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onScheduledNotification(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.NOTIFICATION)
                    {
                        Log.d(TAG, "State Saved (onScheduledNotification)");
                        addAlarmTimeout(context, ACTION_SHOW, item.getUri(), item.alarmtime);
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onScheduledDistantState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        Log.d(TAG, "State Saved (onScheduledDistant)");
                        long transitionAt = item.alarmtime - AlarmSettings.loadPrefAlarmUpcoming(context) + 1000;
                        addAlarmTimeout(context, ACTION_SCHEDULE, item.getUri(), transitionAt);
                        //context.startActivity(getAlarmListIntent(context, item.rowID));   // open the alarm list
                        dismissNotification(context, (int)item.rowID);
                    }
                }
            };
        }

        private AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener onScheduledSoonState(final Context context)
        {
            return new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
            {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item)
                {
                    if (item.type == AlarmClockItem.AlarmType.ALARM)
                    {
                        Log.d(TAG, "State Saved (onScheduledSoon)");
                        addAlarmTimeout(context, ACTION_SHOW, item.getUri(), item.alarmtime);
                        if (AlarmSettings.loadPrefAlarmUpcoming(context) > 0) {
                            showNotification(context, item, true);             // show upcoming reminder
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
     */
    public static void updateAlarmTime(Context context, final AlarmClockItem item)
    {
        Calendar eventTime = Calendar.getInstance();
        if (item.location != null && item.event != null)
        {
            switch (item.event.getType())
            {
                case SolarEvents.TYPE_MOON:
                    eventTime = updateAlarmTime_moonEvent(context, item.event, item.location, item.offset, item.repeating, item.repeatingDays);
                    break;

                case SolarEvents.TYPE_SUN:
                    eventTime = updateAlarmTime_sunEvent(context, item.event, item.location, item.offset, item.repeating, item.repeatingDays);
                    break;
            }
        } else {
            eventTime = updateAlarmTime_clockTime(item.hour, item.minute, item.offset, item.repeating, item.repeatingDays);
        }
        item.hour = eventTime.get(Calendar.HOUR_OF_DAY);
        item.minute = eventTime.get(Calendar.MINUTE);
        item.timestamp = eventTime.getTimeInMillis();
        item.modified = true;
    }

    private static Calendar updateAlarmTime_sunEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays)
    {
        WidgetSettings.TimeMode timeMode = event.toTimeMode();
        SuntimesRiseSetData sunData = new SuntimesRiseSetData(context, 0);
        sunData.setLocation(location);
        sunData.setTimeMode(timeMode != null ? timeMode : WidgetSettings.TimeMode.OFFICIAL);

        Calendar now = Calendar.getInstance();
        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime;

        Calendar day = Calendar.getInstance();
        sunData.setTodayIs(day);
        sunData.calculate();
        eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
        eventTime.set(Calendar.SECOND, 0);
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);

        while (now.after(alarmTime)
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            Log.w("AlarmReceiverItem", "updateAlarmTime: sunEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            sunData.setTodayIs(day);
            sunData.calculate();
            eventTime = (event.isRising() ? sunData.sunriseCalendarToday() : sunData.sunsetCalendarToday());
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        return eventTime;
    }

    private static Calendar updateAlarmTime_moonEvent(Context context, @NonNull SolarEvents event, @NonNull Location location, long offset, boolean repeating, ArrayList<Integer> repeatingDays)
    {
        SuntimesMoonData moonData = new SuntimesMoonData(context, 0);
        moonData.setLocation(location);

        Calendar now = Calendar.getInstance();
        Calendar alarmTime = Calendar.getInstance();

        Calendar day = Calendar.getInstance();
        moonData.setTodayIs(day);
        moonData.calculate();
        Calendar eventTime = (event.isRising() ? moonData.moonriseCalendarToday() : moonData.moonsetCalendarToday());
        eventTime.set(Calendar.SECOND, 0);
        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);

        while (now.after(alarmTime)
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            Log.w("AlarmReceiverItem", "updateAlarmTime: moonEvent advancing by 1 day..");
            day.add(Calendar.DAY_OF_YEAR, 1);
            moonData.setTodayIs(day);
            moonData.calculate();
            eventTime = (event.isRising() ? moonData.moonriseCalendarToday() : moonData.moonsetCalendarToday());
            eventTime.set(Calendar.SECOND, 0);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        return eventTime;
    }

    private static Calendar updateAlarmTime_clockTime(int hour, int minute, long offset, boolean repeating, ArrayList<Integer> repeatingDays)
    {
        Calendar now = Calendar.getInstance();
        Calendar alarmTime = Calendar.getInstance();
        Calendar eventTime = Calendar.getInstance();

        eventTime.set(Calendar.SECOND, 0);
        if (hour >= 0 && hour < 24) {
            eventTime.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute >= 0 && minute < 60) {
            eventTime.set(Calendar.MINUTE, minute);
        }

        alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        while (now.after(alarmTime)
                || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
        {
            Log.w("AlarmReceiverItem", "updateAlarmTime: clock time " + hour + ":" + minute + " (+" + offset + ") advancing by 1 day..");
            eventTime.add(Calendar.DAY_OF_YEAR, 1);
            alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
        }
        return eventTime;
    }

}
