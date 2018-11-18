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
import android.content.BroadcastReceiver;
import android.content.ContentUris;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmDismissActivity;

import java.io.IOException;

public class AlarmNotifications extends BroadcastReceiver
{
    public static final String ACTION_SHOW = "show";
    public static final String ACTION_SILENT = "silent";
    public static final String ACTION_DISMISS = "dismiss";
    public static final String ACTION_SNOOZE = "snooze";

    public static final String EXTRA_NOTIFICATION_ID = "notificationID";
    public static final String ALARM_NOTIFICATION_TAG = "suntimesalarm";

    public static final String PREF_KEY_ALARM_SILENCEAFTER = "app_alarms_silenceafter";
    public static final long PREF_DEF_ALARM_SILENCEAFTER = 10 * 60 * 1000;   // 10 min

    public static final String PREF_KEY_ALARM_SNOOZE = "app_alarms_snooze";
    public static final long PREF_DEF_ALARM_SNOOZE = 10 * 60 * 1000;   // 10 min

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
        Log.d("AlarmNotifications", "onReceive: " + action + ", " + data);

        if (action != null)
        {
            int notificationID = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
            if (action.equals(ACTION_DISMISS) || action.equals(ACTION_SNOOZE))
            {
                stopAlert(context);
                dismissNotification(context, notificationID);

            } else if (action.equals(ACTION_SILENT)) {
                stopAlert(context);
            }

            AlarmDatabaseAdapter.AlarmItemTask itemTask = new AlarmDatabaseAdapter.AlarmItemTask(context);
            itemTask.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener()
            {
                @Override
                public void onItemLoaded(AlarmClockItem item)
                {
                    if (item != null)
                    {
                        AlarmDatabaseAdapter.AlarmUpdateTask updateTask = new AlarmDatabaseAdapter.AlarmUpdateTask(context);
                        updateTask.setTaskListener(new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
                        {
                            @Override
                            public void onFinished(Boolean result)
                            {
                                Intent intent = new Intent(context, AlarmClockActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        });

                        if (action.equals(ACTION_SILENT))
                        {
                            // TODO

                        } else if (action.equals(ACTION_DISMISS)) {
                            if (!item.repeating)
                            {
                                item.enabled = false;
                                item.modified = true;
                                updateTask.execute(item);
                            }

                        } else if (action.equals(ACTION_SNOOZE)) {
                            long snooze = loadSnoozePref(context);
                            // TODO

                        //} else if (action.equals(ACTION_SHOW_FULLSCREEN)) {
                            //showAlarmFullScreen(context, item);

                        } else if (action.equals(ACTION_SHOW)) {
                            showNotification(context, item);
                        }
                    }
                }
            });
            itemTask.execute(ContentUris.parseId(data));
        }
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
                Log.e("startAlert", "Failed to setDataSource! " + soundUri.toString());
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
                    Log.d("DEBUG", "MediaPlayer error " + what);
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

    public static Intent getFullScreenIntent(Context context, AlarmClockItem alarm, int notificationID)
    {
        Intent intent = new Intent(context, AlarmDismissActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(alarm.getUri());
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationID);
        return intent;
    }


    public static Intent getDismissAlarmIntent(Context context, AlarmClockItem alarm, int notificationID)
    {
        Intent intent = new Intent(context, AlarmNotifications.class);
        intent.setAction(ACTION_DISMISS);
        intent.setData(alarm.getUri());
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationID);
        return intent;
    }

    public static Intent getSnoozeAlarmIntent(Context context, AlarmClockItem alarm, int notificationID)
    {
        Intent intent = new Intent(context, AlarmNotifications.class);
        intent.setAction(ACTION_SNOOZE);
        intent.setData(alarm.getUri());
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

        builder.setContentTitle(notificationTitle)
                .setContentText(notificationMsg)
                .setSmallIcon(notificationIcon)
                .setColor(notificationColor)
                .setOnlyAlertOnce(false);

        builder.setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility( NotificationCompat.VISIBILITY_PUBLIC );

        if (alarm.type == AlarmClockItem.AlarmType.ALARM)
        {
            // ALARM
            builder.setCategory( NotificationCompat.CATEGORY_ALARM );
            builder.setOngoing(true);
            builder.setAutoCancel(false);

            Intent fullScreenIntent = getFullScreenIntent(context, alarm, notificationID);
            PendingIntent alarmFullScreen = PendingIntent.getActivity(context, alarm.hashCode(), fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setFullScreenIntent(alarmFullScreen, true);       // at discretion of system to use this intent (or to show a heads up notification instead)

            Intent snoozeIntent = getSnoozeAlarmIntent(context, alarm, notificationID);
            PendingIntent pendingSnooze = PendingIntent.getBroadcast(context, alarm.hashCode(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_action_alarms, context.getString(R.string.alarmAction_snooze), pendingSnooze);

            Intent dismissIntent = getDismissAlarmIntent(context, alarm, notificationID);
            PendingIntent pendingDismiss = PendingIntent.getBroadcast(context, alarm.hashCode(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.alarmAction_dismiss), pendingDismiss);

            //PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(), 0);
            //builder.setContentIntent(contentIntent);

        } else {
            // NOTIFICATION
            builder.setCategory( NotificationCompat.CATEGORY_REMINDER );
            builder.setOngoing(false);
            builder.setAutoCancel(true);
            PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(), 0);
            builder.setContentIntent(contentIntent);
        }

        return builder.build();
    }

    /**
     * showNotification
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

    /**
     * @param context
     * @param notificationID
     */
    public static void dismissNotification(Context context, int notificationID)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(ALARM_NOTIFICATION_TAG, notificationID);
    }

    /**
     * showAlarmFullScreen
     */
    public static void showAlarmFullScreen(Context context, @NonNull AlarmClockItem item)
    {
        Intent intent = new Intent(context, AlarmDismissActivity.class);
        intent.putExtra(AlarmDismissActivity.EXTRA_ALARMID, item.rowID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
