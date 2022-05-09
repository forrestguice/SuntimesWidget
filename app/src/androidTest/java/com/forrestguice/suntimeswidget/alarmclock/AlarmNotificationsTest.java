/**
    Copyright (C) 2022 Forrest Guice
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

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmDismissActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

import static android.test.MoreAsserts.assertNotEqual;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
@RunWith(AndroidJUnit4.class)
public class AlarmNotificationsTest
{
    @Rule
    public ServiceTestRule serviceRule = new ServiceTestRule();
    public Context mockContext;

    @Before
    public void init() {
        mockContext = InstrumentationRegistry.getTargetContext();
    }

    private final ServiceConnection testConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("DEBUG", "onServiceConnected: " + name);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("DEBUG", "onServiceDisconnected: " + name);
        }
    };

    @Test
    public void test_startCommand_nullData() throws TimeoutException
    {
        Intent intent0 = AlarmNotifications.getServiceIntent(mockContext);
        String[] test_actions0 = new String[] {
                AlarmDismissActivity.ACTION_UPDATE, Intent.ACTION_TIME_CHANGED,
                AlarmNotifications.ACTION_SHOW, AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_DISMISS,
                AlarmNotifications.ACTION_SNOOZE,  AlarmNotifications.ACTION_SILENT, AlarmNotifications.ACTION_TIMEOUT,
                AlarmNotifications.ACTION_DELETE, Intent.ACTION_BOOT_COMPLETED,    // DELETE should clear all (setup for BOOT_COMPLETED, SCHEDULE, and RESCHEDULE)
                AlarmNotifications.ACTION_SCHEDULE, AlarmNotifications.ACTION_RESCHEDULE, AlarmNotifications.ACTION_RESCHEDULE1,};

        for (String action : test_actions0)
        {
            Intent intent = new Intent(intent0);
            intent.setAction(action);
            test_startCommand_calledStop(intent, true, 1500);
        }
    }

    @Test
    public void test_startCommand_withData0() throws TimeoutException
    {
        AlarmClockItem[] alarms = AlarmDatabaseAdapterTest.createTestItems();
        Intent intent0 = AlarmNotifications.getServiceIntent(mockContext);
        String[] test_actions0 = new String[] {
                AlarmDismissActivity.ACTION_UPDATE, Intent.ACTION_TIME_CHANGED, Intent. ACTION_BOOT_COMPLETED,
                AlarmNotifications.ACTION_SCHEDULE, AlarmNotifications.ACTION_RESCHEDULE, AlarmNotifications.ACTION_RESCHEDULE1,
                AlarmNotifications.ACTION_SHOW, AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_DISMISS,
                AlarmNotifications.ACTION_SNOOZE,  AlarmNotifications.ACTION_SILENT, AlarmNotifications.ACTION_TIMEOUT,
                AlarmNotifications.ACTION_DELETE };

        // bad data (invalid alarmId)
        Uri data = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, -1);
        test_startComand_withData_calledStop(intent0, test_actions0, data);

        // add non-repeating alarm for +1hr (create valid data)
        Calendar now = Calendar.getInstance();
        alarms[0].type = AlarmClockItem.AlarmType.ALARM;
        alarms[0].hour = now.get(Calendar.HOUR) + 1;
        alarms[0].minute = now.get(Calendar.MINUTE) + 5;    // soon
        alarms[0].alarmtime = 0;
        alarms[0].setEvent(null);
        alarms[0].enabled = true;
        alarms[0].repeating = false;
        long alarmId0 = addAlarmItemToDatabase(alarms[0]);
        assertTrue("failed to create alarm", hasAlarmId(alarmId0));
        Uri data0 = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmId0);

        // add non-repeating notification (create valid data)
        alarms[1].enabled = true;
        alarms[1].repeating = false;
        alarms[1].type = AlarmClockItem.AlarmType.NOTIFICATION;
        long alarmId1 = addAlarmItemToDatabase(alarms[1]);
        assertTrue("failed to create alarm", hasAlarmId(alarmId1));
        Uri data1 = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmId1);

        // bad data (invalid action when combined with data)
        String[] test_actions1 = new String[] { AlarmDismissActivity.ACTION_UPDATE, Intent.ACTION_TIME_CHANGED, Intent. ACTION_BOOT_COMPLETED };
        test_startComand_withData_calledStop(intent0, test_actions1, data0);

        // good data, bad transition (TIMEOUT is only reachable from SOUNDING)
        String[] test_actions2 = new String[] { AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_SILENT, AlarmNotifications.ACTION_TIMEOUT };
        test_startComand_withData_calledStop(intent0, test_actions2, data0);
        verify_hasAlarmState(alarmId0, AlarmState.STATE_DISABLED);

        // good data, bad transition (SNOOZE is only reachable from SOUNDING)
        String[] test_actions3 = new String[] { AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_SNOOZE };
        test_startComand_withData_calledStop(intent0, test_actions3, data0);
        verify_hasAlarmState(alarmId0, AlarmState.STATE_DISABLED);

        // good data, bad transition (SHOW is only reachable from SCHEDULED_ and SNOOZE)
        String[] test_actions4 = new String[] { AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_SHOW };
        test_startComand_withData_calledStop(intent0, test_actions4, data0);
        verify_hasAlarmState(alarmId0, AlarmState.STATE_DISABLED);

        // good data, bad transition (DISMISS cannot be reached from DISABLED)
        String[] test_actions5 = new String[] { AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_DISMISS };
        test_startComand_withData_calledStop(intent0, test_actions5, data0);
        verify_hasAlarmState(alarmId0, AlarmState.STATE_DISABLED);

        // good data (schedule -> show -> dismiss notification)
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_SCHEDULE }, data1);
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SHOW }, data1);
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DISMISS }, data1);
        verify_hasAlarmState(alarmId1, AlarmState.STATE_DISABLED);

        String[] test_actions9 = new String[] { AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_SCHEDULE, AlarmNotifications.ACTION_SHOW, AlarmNotifications.ACTION_SNOOZE };
        //String[] test_actions10 = new String[] { AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_SCHEDULE, AlarmNotifications.ACTION_SHOW, AlarmNotifications.ACTION_TIMEOUT };
        test_startComand_withData_calledStop(intent0, test_actions9, data1);
        //test_startComand_withData_calledStop(intent0, test_actions10, data1);

        // good data (delete alarm)
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DELETE }, data0);
        assertFalse("failed to delete alarm", hasAlarmId(alarmId0));

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DELETE }, data1);
        assertFalse("failed to delete alarm", hasAlarmId(alarmId1));
    }

    @Test
    public void test_startCommand_withData1() throws TimeoutException
    {
        AlarmClockItem[] alarms = AlarmDatabaseAdapterTest.createTestItems();
        Intent intent0 = AlarmNotifications.getServiceIntent(mockContext);

        // add non-repeating alarm for +1hr (create valid data)
        Calendar now = Calendar.getInstance();
        alarms[0].type = AlarmClockItem.AlarmType.ALARM;
        alarms[0].timezone = TimeZone.getDefault().getID();
        alarms[0].hour = ((now.get(Calendar.HOUR_OF_DAY) + 1 ) % 24);    // very soon; will display reminder notification
        alarms[0].minute = now.get(Calendar.MINUTE);
        alarms[0].alarmtime = 0;
        alarms[0].setEvent(null);
        alarms[0].repeating = false;
        alarms[0].enabled = true;
        long alarmId0 = addAlarmItemToDatabase(alarms[0]);
        assertTrue("failed to create alarm", hasAlarmId(alarmId0));

        // good data (schedule -> show -> dismiss alarm)
        long alarmId2 = addAlarmItemToDatabase(alarms[0]);
        assertTrue("failed to create alarm", hasAlarmId(alarmId2));
        Uri data2 = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmId2);

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SCHEDULE }, data2, true, 1500);
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SCHEDULED_SOON);

        assertFalse("media player should be stopped", AlarmNotifications.isPlaying);
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SHOW }, data2, false, 1000);    // should continue running (showing foreground notification)
        assertTrue("service should be running in the foreground when showing alarm", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SOUNDING);
        assertTrue("media player should be playing", AlarmNotifications.isPlaying);

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SNOOZE }, data2, false, 1000);    // should continue running (still showing foreground notification)
        assertTrue("service should be running in the foreground when snoozing", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SNOOZING);
        assertFalse("media player should be stopped", AlarmNotifications.isPlaying);

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SHOW }, data2, false, 1000);    // should continue running (showing foreground notification)
        assertTrue("service should be running in the foreground when showing alarm", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SOUNDING);
        assertTrue("media player should be playing", AlarmNotifications.isPlaying);

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DISMISS }, data2, true, 3500);
        verify_hasAlarmState(alarmId2, AlarmState.STATE_DISABLED);
        assertFalse("media player should be stopped", AlarmNotifications.isPlaying);

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DELETE }, data2, true, 2000);
        assertFalse("failed to delete alarm", hasAlarmId(alarmId2));
    }

    protected void test_startComand_withData_calledStop(Intent intent0, String[] actions, Uri data) throws TimeoutException {
        test_startComand_withData_calledStop(intent0, actions, data, true, 1000);
    }
    protected void test_startComand_withData_calledStop(Intent intent0, String[] actions, Uri data, boolean shouldStop, int withinMs) throws TimeoutException
    {
        for (String action : actions)
        {
            Intent intent = new Intent(intent0);
            intent.setAction(action);
            intent.setData(data);
            test_startCommand_calledStop(intent, shouldStop, withinMs);
        }
    }

    protected long addAlarmItemToDatabase(AlarmClockItem item)
    {
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        long alarmId = db.addAlarm(item.asContentValues(false));
        Assert.assertTrue("ID should be >= 0 (was " + alarmId + ")", alarmId != -1);
        db.close();
        return alarmId;
    }

    protected boolean hasAlarmId(long rowId)
    {
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        boolean hasValue = (db.getAlarm(rowId).getCount() > 0);
        db.close();
        return hasValue;
    }

    protected void verify_hasAlarmState(long rowId, int state)
    {
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        Cursor cursor = db.getAlarmState(rowId);
        AlarmDatabaseAdapterTest.verifyAlarmState(cursor, rowId, state);
        db.close();
    }

    protected void test_startCommand_calledStop(Intent intent, boolean shouldStop, int withinMs) throws TimeoutException
    {
        AlarmNotifications.ForegroundNotifications.t_hasCalledStopSelf = false;
        serviceRule.startService(intent);
        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + withinMs)) {
            /* busy wait.. the service needs to finish the command within 1000ms to pass the test */
        }
        if (shouldStop) {
            assertTrue("service failed to stopSelf() after handling " + intent.getAction() + ": " + intent.getData(), AlarmNotifications.ForegroundNotifications.t_hasCalledStopSelf);
            assertFalse("service shouldn't be running", isServiceRunning(mockContext, AlarmNotifications.NotificationService.class));
        } else {
            assertFalse("service should remain running after handling " + intent.getAction() + ": " + intent.getData(), AlarmNotifications.ForegroundNotifications.t_hasCalledStopSelf);
            assertTrue("service should be running", isServiceRunning(mockContext, AlarmNotifications.NotificationService.class));
        }
    }

    /**
     * based on solutions at https://stackoverflow.com/questions/6452466/how-to-determine-if-an-android-service-is-running-in-the-foreground
     */
    public static boolean isForegroundService(Context context, @NonNull Class<?> service)
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

    public static boolean isServiceRunning(Context context, @NonNull Class<?> service)
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null)
        {
            String className = service.getName();
            List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo s : services)
            {
                if (className.equals(s.service.getClassName())) {
                    return s.started;
                }
            }
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_getServiceIntent()
    {
        Intent intent = AlarmNotifications.getServiceIntent(mockContext);
        assertEquals(AlarmNotifications.NotificationService.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void test_getFullscreenIntent()
    {
        long alarmID = 10;
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        Uri data = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmID);
        Intent intent = AlarmNotifications.getFullscreenIntent(mockContext, data);

        assertEquals(AlarmDismissActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(data.toString(), intent.getData().toString());
        assertTrue(intent.hasExtra(AlarmNotifications.EXTRA_NOTIFICATION_ID));
        assertEquals(alarmID, intent.getIntExtra(AlarmNotifications.EXTRA_NOTIFICATION_ID, -1));
        assertEquals(flags, intent.getFlags());
    }

    @Test
    public void test_getFullscreenBroadcast()
    {
        long alarmID = 20;
        Uri data = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmID);
        Intent intent = AlarmNotifications.getFullscreenBroadcast(data);

        assertEquals(AlarmDismissActivity.ACTION_UPDATE, intent.getAction());
        assertEquals(data.toString(), intent.getData().toString());
    }

    @Test
    public void test_getAlarmListIntent()
    {
        long alarmID = 30;
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        Uri data = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmID);
        Intent intent = AlarmNotifications.getAlarmListIntent(mockContext, alarmID);

        assertEquals(AlarmClockActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(data.toString(), intent.getData().toString());
        assertTrue(intent.hasExtra(AlarmClockActivity.EXTRA_SELECTED_ALARM));
        assertEquals(alarmID, intent.getLongExtra(AlarmClockActivity.EXTRA_SELECTED_ALARM, -1));
        assertEquals(flags, intent.getFlags());
    }

    @Test
    public void test_getAlarmIntent()
    {
        long alarmID = 40;
        String action = AlarmNotifications.ACTION_SHOW;
        Uri data = ContentUris.withAppendedId(AlarmClockItem.CONTENT_URI, alarmID);
        int flags = Intent.FLAG_RECEIVER_FOREGROUND;
        Intent intent = AlarmNotifications.getAlarmIntent(mockContext, action, data);

        assertEquals(AlarmNotifications.class.getName(), intent.getComponent().getClassName());
        assertEquals(action, intent.getAction());
        assertEquals(data.toString(), intent.getData().toString());
        assertTrue(intent.hasExtra(AlarmNotifications.EXTRA_NOTIFICATION_ID));
        assertEquals(alarmID, intent.getIntExtra(AlarmNotifications.EXTRA_NOTIFICATION_ID, -1));
        assertEquals(flags, intent.getFlags());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_initPlayer()
    {
        AlarmNotifications.audioManager = null;
        AlarmNotifications.vibrator = null;
        AlarmNotifications.player = null;
        AlarmNotifications.initPlayer(mockContext, false);
        verify_initPlayer();
    }
    public void verify_initPlayer()
    {
        assertNotNull(AlarmNotifications.audioManager);
        assertNotNull(AlarmNotifications.vibrator);
        assertNotNull(AlarmNotifications.player);
        assertNotEqual(0, AlarmNotifications.player.getAudioSessionId());
    }

    @Test
    public void test_startAlertUri_notification0()
    {
        //Uri defaultSound = RingtoneManager.getActualDefaultRingtoneUri(mockContext, RingtoneManager.TYPE_NOTIFICATION);
        //test_startAlertUri_notification(defaultSound);

        Uri fallbackSound = Uri.parse("android.resource://" + mockContext.getPackageName() + "/" + R.raw.notifysound);
        test_startAlertUri_notification(fallbackSound);
    }

    @Test
    public void test_startAlertUri_alarm0()
    {
        Uri defaultSound = RingtoneManager.getActualDefaultRingtoneUri(mockContext, RingtoneManager.TYPE_ALARM);
        test_startAlertUri_alarm(defaultSound, true);
        test_startAlertUri_alarm(defaultSound, false);
    }
    @Test
    public void test_startAlertUri_invalid()
    {
        String[] invalid = new String[] {"", " ", "content", "content://", "content://media/dne", "http://", "https://"};    // expecting IOException
        test_startAlertUri_exception(null, false);
        for (String value : invalid) {
            test_startAlertUri_exception(Uri.parse(value), true);
            test_startAlertUri_exception(Uri.parse(value), false);
        }

        String[] invalid1 = new String[] {"invalid", "invalid://", "geo://33,-111"};    // expecting error codes
        for (String value : invalid1) {
            test_startAlertUri_errorCode(Uri.parse(value), true, MediaPlayer.MEDIA_ERROR_UNKNOWN);
            test_startAlertUri_errorCode(Uri.parse(value), false, MediaPlayer.MEDIA_ERROR_UNKNOWN);
        }
    }

    public void test_startAlertUri_alarm(Uri uri, boolean fadeIn)
    {
        // pre-conditions
        assertFalse(AlarmNotifications.isPlaying);
        assertFalse(AlarmNotifications.isFadingIn);
        assertFalse(AlarmNotifications.isVibrating);

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(mockContext).edit();
        prefs.putInt(AlarmSettings.PREF_KEY_ALARM_FADEIN, fadeIn ? 3000 : 0).apply();

        AlarmNotifications.t_player_error = AlarmNotifications.t_player_error_extra = 0;
        AlarmNotifications.initPlayer(mockContext, true);    // player must be initialized first
        verify_initPlayer();
        AlarmNotifications.t_volume = 0;
        try {
            AlarmNotifications.startAlert(mockContext, uri, true);
            assertFalse(AlarmNotifications.isPlaying);    // startAlert(Uri) doesn't toggle isPlaying (or call startVibration)
        } catch (Exception e) {
            Assert.fail("failed to startAlert: " + e);
        }

        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 1000)) {
            /* give it a second; mediaPlayer.start is async */
        }
        assertEquals(0, AlarmNotifications.t_player_error);
        assertTrue(AlarmNotifications.player.isPlaying());
        assertTrue(AlarmNotifications.player.isLooping());
        assertEquals(fadeIn, AlarmNotifications.isFadingIn);

        now = System.currentTimeMillis();
        if (fadeIn) {
            float volume = AlarmNotifications.t_volume;
            while (System.currentTimeMillis() < (now + 3000)) {
                /* wait for fade to finish */
                assertTrue(AlarmNotifications.t_volume >= volume);
                volume = AlarmNotifications.t_volume;
            }
        }
        assertFalse(AlarmNotifications.isFadingIn);
        assertEquals(1f, AlarmNotifications.t_volume);

        // stopAlert
        AlarmNotifications.isPlaying = true;
        AlarmNotifications.stopAlert(true);
        assertFalse(AlarmNotifications.isPlaying);
        assertFalse(AlarmNotifications.isVibrating);
        assertFalse(AlarmNotifications.player.isPlaying());
    }

    public void test_startAlertUri_notification(Uri uri)
    {
        assertFalse(AlarmNotifications.isPlaying);    // test pre-conditions
        assertFalse(AlarmNotifications.isFadingIn);

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(mockContext).edit();
        prefs.putInt(AlarmSettings.PREF_KEY_ALARM_FADEIN, 0).apply();

        AlarmNotifications.t_player_error = AlarmNotifications.t_player_error_extra = 0;
        AlarmNotifications.initPlayer(mockContext, true);    // player must be initialized first
        verify_initPlayer();
        try {
            AlarmNotifications.startAlert(mockContext, uri, false);
        } catch (Exception e) {
            Assert.fail("failed to startAlert: " + e);
        }

        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 500)) {
            /* give it a second; the call to mediaPlayer.start is async */
        }
        assertEquals(0, AlarmNotifications.t_player_error);
        assertTrue(AlarmNotifications.player.isPlaying());
        assertFalse(AlarmNotifications.player.isLooping());
        assertEquals(1f, AlarmNotifications.t_volume);
        assertFalse(AlarmNotifications.isFadingIn);

        now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 5000)) {
            /* give it a few seconds for the sound to finish */
        }
        assertFalse(AlarmNotifications.player.isPlaying());
    }

    public void test_startAlertUri_exception(Uri uri, boolean isAlarm)
    {
        AlarmNotifications.initPlayer(mockContext, true);
        verify_initPlayer();
        try {
            AlarmNotifications.startAlert(mockContext, uri, isAlarm);
            Assert.fail("should have failed with IOException or SecurityException.. uri: " + uri);    // this line should be unreachable
        } catch (IOException | SecurityException e) { /* EMPTY */ }
        assertFalse(AlarmNotifications.player.isPlaying());
    }

    public void test_startAlertUri_errorCode(Uri uri, boolean isAlarm, Integer code)
    {
        AlarmNotifications.t_player_error = AlarmNotifications.t_player_error_extra = 0;
        AlarmNotifications.initPlayer(mockContext, true);
        verify_initPlayer();
        try {
            AlarmNotifications.startAlert(mockContext, uri, isAlarm);
        } catch (Exception e) {
            Assert.fail("failed to startAlert: uri: " + uri + ", " + e);
        }
        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 500)) {
            /* give it a second; mediaPlayer.start is async */
        }
        assertNotEqual(0, AlarmNotifications.t_player_error);    // expecting some error code
        assertFalse(AlarmNotifications.player.isPlaying());
        if (code != null) {
            assertEquals((int)code, AlarmNotifications.t_player_error);
        }
    }

}
