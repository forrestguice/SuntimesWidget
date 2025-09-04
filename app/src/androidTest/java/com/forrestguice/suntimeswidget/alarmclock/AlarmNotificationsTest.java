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
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmDismissActivity;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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

    public static final int MAXTIME_BOOT_COMPLETED = 10000;
    public static final int MAXTIME_GENERAL = 5000;
    public static final int MAXTIME_DISMISS = 5000;
    public static final int MAXTIME_DELETE = 2000;
    public static final int MAXTIME_SCHEDULE = 1500;
    public static final int MAXTIME_SHOW = 1000;
    public static final int MAXTIME_SNOOZE = 2000;

    @Test
    public void test_bootCompleted() throws TimeoutException
    {
        Calendar now = Calendar.getInstance();
        ArrayList<Long> rowIDs = new ArrayList<>();
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        db.clearAlarms();
        for (int alarmState : AlarmState.VALUES)
        {
            AlarmClockItem alarm = new AlarmClockItem();
            alarm.type = AlarmClockItem.AlarmType.NOTIFICATION;
            alarm.timezone = TimeZone.getDefault().getID();
            alarm.hour = ((now.get(Calendar.HOUR_OF_DAY) + 1 ) % 24);    // very soon; will display reminder notification
            alarm.minute = now.get(Calendar.MINUTE);
            alarm.alarmtime = 0;
            alarm.setEvent(SolarEvents.SUNRISE.name());
            alarm.location = new Location("TEST", "34", "-111", "0");
            alarm.repeating = true;
            alarm.setRepeatingDays("1,2,3,4,5,6,7");
            alarm.enabled = true;
            long rowID = addAlarmItemToDatabase(alarm);
            assertTrue("failed to create alarm", hasAlarmId(rowID));

            db.updateAlarmState(rowID,  AlarmDatabaseAdapterTest.getAlarmStateValues(rowID, alarmState));
            rowIDs.add(rowID);
        }
        db.close();

        // trigger BOOT_COMPLETE action
        Intent intent = new Intent(AlarmNotifications.getServiceIntent(mockContext));
        intent.setAction(Intent.ACTION_BOOT_COMPLETED);
        test_startCommand_calledStop(intent, true, MAXTIME_BOOT_COMPLETED);

        // verify AlarmState is now "scheduled"
        for (long rowID : rowIDs)
        {
            AlarmState state = getAlarmState(rowID);
            assertTrue("expected 1 (SCHEDULED_DISTANT) or 2 (SCHEDULED_SOON), not " + state.getState(), state.getState() == AlarmState.STATE_SCHEDULED_SOON || state.getState() == AlarmState.STATE_SCHEDULED_DISTANT);
        }
    }

    @Test
    public void test_timeZoneChanged() throws TimeoutException
    {
        Calendar now = Calendar.getInstance();
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        db.clearAlarms();

        int i = 0;
        HashMap<Long, AlarmClockItem> alarms = new HashMap<>();    // create enabled items (should be rescheduled)
        for (int alarmState : AlarmState.VALUES)
        {
            AlarmClockItem alarm = createTestItem_clockTime((i % 24), ((now.get(Calendar.MINUTE) + i) % 60));
            long rowID = addAlarmItemToDatabase(alarm, alarmState);
            assertTrue("failed to create alarm", hasAlarmId(rowID));
            alarms.put(rowID, alarm);
        }

        AlarmClockItem alarm0 = createTestItem_clockTime(7, 30);    // create disabled items (should remain untouched)
        alarm0.enabled = false;
        long alarm0_id = addAlarmItemToDatabase(alarm0, AlarmState.STATE_DISABLED);
        assertTrue("failed to create alarm", hasAlarmId(alarm0_id));
        alarms.put(alarm0_id, alarm0);

        db.close();

        // trigger TIMEZONE_CHANGED action
        AlarmSettings.saveSystemTimeZoneInfo(mockContext, "test", 0);
        assertEquals("test", AlarmSettings.loadSystemTimeZoneID(mockContext));
        assertEquals(0, AlarmSettings.loadSystemTimeZoneOffset(mockContext));

        Intent intent = new Intent(AlarmNotifications.getServiceIntent(mockContext));
        intent.setAction(Intent.ACTION_TIMEZONE_CHANGED);
        test_startCommand_calledStop(intent, true, MAXTIME_GENERAL);
        assertEquals(TimeZone.getDefault().getID(), AlarmSettings.loadSystemTimeZoneID(mockContext));
        assertEquals(TimeZone.getDefault().getOffset(System.currentTimeMillis()), AlarmSettings.loadSystemTimeZoneOffset(mockContext));

        // verify AlarmState is now "scheduled", "sounding", or "snoozing"
        for (long rowID : alarms.keySet())
        {
            AlarmState state = getAlarmState(rowID);
            AlarmState state0 = alarms.get(rowID).state;
            if (alarms.get(rowID).enabled)
            {
                if (state0.getState() == AlarmState.STATE_SOUNDING || state0.getState() == AlarmState.STATE_SNOOZING) {
                    assertEquals("state should be unchanged; expected 3 (SOUNDING), or 10 (SNOOZING), not " + state.getState(), state.getState(), state0.getState());

                } else {
                    assertTrue("state should be scheduled; expected 1 (SCHEDULED_DISTANT) or 2 (SCHEDULED_SOON), not " + state.getState(),
                            state.getState() == AlarmState.STATE_SCHEDULED_SOON || state.getState() == AlarmState.STATE_SCHEDULED_DISTANT);
                }
            } else {
                assertEquals("state should be unchanged for disabled items", state.getState(), state0.getState());
            }
        }
    }

    private AlarmClockItem createTestItem_clockTime(int hour, int minute)
    {
        AlarmClockItem alarm = new AlarmClockItem();
        alarm.type = AlarmClockItem.AlarmType.ALARM;
        alarm.location = new Location("TEST", "34", "-111", "0");
        alarm.timezone = null;
        alarm.setEvent(null);
        alarm.hour = hour;
        alarm.minute = minute;
        alarm.offset = 0;
        alarm.alarmtime = 0;
        alarm.repeating = true;
        alarm.setRepeatingDays("1,2,3,4,5,6,7");
        alarm.enabled = true;
        return alarm;
    }

    @Nullable
    private AlarmState getAlarmState(long rowID)
    {
        AlarmState state = null;
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        Cursor cursor = db.getAlarmState(rowID);
        if (cursor != null)
        {
            cursor.moveToFirst();
            if (!cursor.isAfterLast())
            {
                ContentValues stateValues = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, stateValues);
                state = new AlarmState(stateValues);
            }
            cursor.close();
        }
        db.close();
        return state;
    }

    @Test
    public void test_startCommand_nullData() throws TimeoutException
    {
        Intent intent0 = AlarmNotifications.getServiceIntent(mockContext);
        String[] test_actions0 = new String[] {
                AlarmNotifications.ACTION_UPDATE_UI, Intent.ACTION_TIME_CHANGED,
                AlarmNotifications.ACTION_SHOW, AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_DISMISS,
                AlarmNotifications.ACTION_SNOOZE,  AlarmNotifications.ACTION_SILENT, AlarmNotifications.ACTION_TIMEOUT,
                AlarmNotifications.ACTION_DELETE, Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIMEZONE_CHANGED,    // DELETE should clear all (setup for BOOT_COMPLETED, SCHEDULE, and RESCHEDULE)
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
                AlarmNotifications.ACTION_UPDATE_UI, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent. ACTION_BOOT_COMPLETED,
                AlarmNotifications.ACTION_SCHEDULE, AlarmNotifications.ACTION_RESCHEDULE, AlarmNotifications.ACTION_RESCHEDULE1,
                AlarmNotifications.ACTION_SHOW, AlarmNotifications.ACTION_DISABLE, AlarmNotifications.ACTION_DISMISS,
                AlarmNotifications.ACTION_SNOOZE,  AlarmNotifications.ACTION_SILENT, AlarmNotifications.ACTION_TIMEOUT,
                AlarmNotifications.ACTION_DELETE };

        // bad data (invalid alarmId)
        Uri data = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, -1);
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
        Uri data0 = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmId0);

        // add non-repeating notification (create valid data)
        alarms[1].enabled = true;
        alarms[1].repeating = false;
        alarms[1].type = AlarmClockItem.AlarmType.NOTIFICATION;
        long alarmId1 = addAlarmItemToDatabase(alarms[1]);
        assertTrue("failed to create alarm", hasAlarmId(alarmId1));
        Uri data1 = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmId1);

        // bad data (invalid action when combined with data)
        String[] test_actions1 = new String[] { AlarmNotifications.ACTION_UPDATE_UI, Intent.ACTION_TIME_CHANGED, Intent. ACTION_BOOT_COMPLETED };
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
        Uri data2 = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmId2);

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SCHEDULE }, data2, true, MAXTIME_SCHEDULE);
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SCHEDULED_SOON);

        assertFalse("media player should be stopped", AlarmNotifications.isPlaying());
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SHOW }, data2, false, MAXTIME_SHOW);    // should continue running (showing foreground notification)
        assertTrue("service should be running in the foreground when showing alarm", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SOUNDING);
        assertTrue("media player should be playing", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SNOOZE }, data2, false, MAXTIME_SNOOZE);    // should continue running (still showing foreground notification)
        assertTrue("service should be running in the foreground when snoozing", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SNOOZING);
        assertFalse("media player should be stopped", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SHOW }, data2, false, MAXTIME_SHOW);    // should continue running (showing foreground notification)
        assertTrue("service should be running in the foreground when showing alarm", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SOUNDING);
        assertTrue("media player should be playing", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DISMISS }, data2, true, MAXTIME_DISMISS);
        verify_hasAlarmState(alarmId2, AlarmState.STATE_DISABLED);
        assertFalse("media player should be stopped", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DELETE }, data2, true, MAXTIME_DELETE);
        assertFalse("failed to delete alarm", hasAlarmId(alarmId2));
    }

    /**
     * Notifications
     */

    @Test
    public void test_startCommand_notification() throws TimeoutException {
        test_startCommand_notification(false, "1,2,3,4,5,6,7");
    }
    @Test
    public void test_startCommand_notification_nullDays() throws TimeoutException {
        test_startCommand_notification(false, null);
    }
    @Test
    public void test_startCommand_repeatingNotification() throws TimeoutException {
        test_startCommand_notification( true, "1,2,3,4,5,6,7");
    }
    @Test
    public void test_startCommand_repeatingNotification_nullDays() throws TimeoutException {
        test_startCommand_notification(true, null);
    }

    /**
     * Non-repeating alarm / notification
     */
    @Test
    public void test_startCommand_alarm_noReminder() throws TimeoutException {
        test_startCommand_alarm( 0, false, "1,2,3,4,5,6,7");
    }
    @Test
    public void test_startCommand_alarm_withReminder() throws TimeoutException {
        test_startCommand_alarm(12 * 60 * 60 * 1000, false, null);
    }
    @Test
    public void test_startCommand_alarm_nullDays() throws TimeoutException {
        test_startCommand_alarm(12 * 60 * 60 * 1000, false, null);
    }

    /**
     * Repeating alarm with reminder set to never. bug #665
     */
    @Test
    public void test_startCommand_repeatingAlarm_noReminder() throws TimeoutException {
        test_startCommand_alarm(0, true, "1,2,3,4,5,6,7");
    }
    @Test
    public void test_startCommand_repeatingAlarm_withReminder() throws TimeoutException {
        test_startCommand_alarm(12 * 60 * 60 * 1000, true, "1,2,3,4,5,6,7");
    }
    @Test
    public void test_startCommand_repeatingAlarm_nullDays() throws TimeoutException {
        test_startCommand_alarm(12 * 60 * 60 * 1000, true, null);
    }

    public static AlarmClockItem createAlarmClockItem(boolean repeating)
    {
        AlarmClockItem alarm = new AlarmClockItem();
        alarm.type = AlarmClockItem.AlarmType.ALARM;
        alarm.repeating = repeating;
        alarm.repeatingDays = AlarmClockItem.everyday();

        Calendar now = Calendar.getInstance();
        alarm.timezone = TimeZone.getDefault().getID();
        alarm.hour = ((now.get(Calendar.HOUR_OF_DAY) + 1 ) % 24);
        alarm.minute = now.get(Calendar.MINUTE);
        alarm.alarmtime = 0;
        alarm.offset = 0;
        alarm.setEvent(null);
        alarm.enabled = true;
        return alarm;
    }

    public void test_startCommand_notification(boolean repeating, String repeatingDays) throws TimeoutException
    {
        AlarmClockItem alarm = createAlarmClockItem(repeating);
        alarm.type = AlarmClockItem.AlarmType.NOTIFICATION;
        alarm.setRepeatingDays(repeatingDays);
        test_startCommand_notification(alarm);
    }
    public void test_startCommand_notification(AlarmClockItem alarm) throws TimeoutException
    {
        long alarmId0 = addAlarmItemToDatabase(alarm);
        assertTrue("failed to create notification", hasAlarmId(alarmId0));

        // schedule -> show -> dismiss
        long alarmId2 = addAlarmItemToDatabase(alarm);
        assertTrue("failed to create notification", hasAlarmId(alarmId2));
        Uri data2 = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmId2);

        Intent intent0 = AlarmNotifications.getServiceIntent(mockContext);
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SCHEDULE }, data2, true, MAXTIME_SCHEDULE);
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SCHEDULED_DISTANT);

        assertFalse("media player should be stopped", AlarmNotifications.isPlaying());
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SHOW }, data2, false, MAXTIME_SHOW);    // service should finish (showing normal notification)
        assertFalse("service should not be running in the foreground when showing notification", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SOUNDING);
        assertTrue("media player should be playing", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DISMISS }, data2, true, MAXTIME_DISMISS);
        int expectedState = alarm.repeating ? AlarmState.STATE_SCHEDULED_DISTANT                                           // repeating notifications are now scheduled_
                : AlarmState.STATE_DISABLED;                                                                               // non-repeating notifications are now disabled
        verify_hasAlarmState(alarmId2, expectedState);
        assertFalse("media player should be stopped", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DELETE }, data2, true, MAXTIME_DELETE);
        assertFalse("failed to delete notification", hasAlarmId(alarmId2));
    }

    public void test_startCommand_alarm(int reminderWithinMillis, boolean repeating, String repeatingDays) throws TimeoutException
    {
        AlarmClockItem alarm = createAlarmClockItem(repeating);
        alarm.setRepeatingDays(repeatingDays);
        test_startCommand_alarm(reminderWithinMillis, alarm);
    }

    public void test_startCommand_alarm(int reminderWithinMillis, AlarmClockItem alarm) throws TimeoutException
    {
        AlarmSettings.savePrefAlarmUpcoming(mockContext, reminderWithinMillis);

        long alarmId0 = addAlarmItemToDatabase(alarm);
        assertTrue("failed to create alarm", hasAlarmId(alarmId0));

        // schedule -> show -> snooze -> show -> dismiss alarm
        long alarmId2 = addAlarmItemToDatabase(alarm);
        assertTrue("failed to create alarm", hasAlarmId(alarmId2));
        Uri data2 = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmId2);

        Intent intent0 = AlarmNotifications.getServiceIntent(mockContext);
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SCHEDULE }, data2, true, MAXTIME_SCHEDULE);
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SCHEDULED_SOON);

        assertFalse("media player should be stopped", AlarmNotifications.isPlaying());
        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SHOW }, data2, false, MAXTIME_SHOW);    // should continue running (showing foreground notification)
        assertTrue("service should be running in the foreground when showing alarm", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SOUNDING);
        assertTrue("media player should be playing", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SNOOZE }, data2, false, MAXTIME_SNOOZE);    // should continue running (still showing foreground notification)
        assertTrue("service should be running in the foreground when snoozing", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SNOOZING);
        assertFalse("media player should be stopped", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_SHOW }, data2, false, MAXTIME_SHOW);    // should continue running (showing foreground notification)
        assertTrue("service should be running in the foreground when showing alarm", isForegroundService(mockContext, AlarmNotifications.NotificationService.class));
        verify_hasAlarmState(alarmId2, AlarmState.STATE_SOUNDING);
        assertTrue("media player should be playing", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DISMISS }, data2, true, MAXTIME_DISMISS);
        int expectedState = alarm.repeating ? ((reminderWithinMillis <= 0) ? AlarmState.STATE_SCHEDULED_SOON : AlarmState.STATE_SCHEDULED_DISTANT)     // repeating alarms are now scheduled_
                                      : AlarmState.STATE_DISABLED;                                                                               // non-repeating alarms are now disabled
        verify_hasAlarmState(alarmId2, expectedState);
        assertFalse("media player should be stopped", AlarmNotifications.isPlaying());

        test_startComand_withData_calledStop(intent0, new String[] { AlarmNotifications.ACTION_DELETE }, data2, true, MAXTIME_DELETE);
        assertFalse("failed to delete alarm", hasAlarmId(alarmId2));
    }

    protected void test_startComand_withData_calledStop(Intent intent0, String[] actions, Uri data) throws TimeoutException {
        test_startComand_withData_calledStop(intent0, actions, data, true, 1500);
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

    protected long addAlarmItemToDatabase(AlarmClockItem item, int state)
    {
        long rowID = addAlarmItemToDatabase(item);
        assertTrue("failed to create alarm", hasAlarmId(rowID));

        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        db.updateAlarmState(rowID,  AlarmDatabaseAdapterTest.getAlarmStateValues(rowID, state));
        item.state = new AlarmState(rowID, state);
        return rowID;
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
        Cursor cursor = db.getAlarm(rowId);
        boolean hasValue = (cursor != null && cursor.getCount() > 0);
        cursor.close();
        db.close();
        return hasValue;
    }

    protected void verify_hasAlarmState(long rowId, int state)
    {
        AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        Cursor cursor = db.getAlarmState(rowId);
        AlarmDatabaseAdapterTest.verifyAlarmState(cursor, rowId, state);
        cursor.close();
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
        Uri data = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmID);
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
        Uri data = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmID);
        Intent intent = AlarmNotifications.getFullscreenBroadcast(data);

        assertEquals(AlarmNotifications.ACTION_UPDATE_UI, intent.getAction());
        assertEquals(data.toString(), intent.getData().toString());
    }

    @Test
    public void test_getAlarmListIntent()
    {
        long alarmID = 30;
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        Uri data = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmID);
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
        Uri data = ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, alarmID);
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
    public void test_initPlayer(String channel)
    {
        AlarmNotifications.audioManager = null;
        AlarmNotifications.vibrator = null;
        AlarmNotifications.players.remove(channel);
        AlarmNotifications.initPlayer(mockContext, channel, false);
        verify_initPlayer(channel);
    }
    public void verify_initPlayer(String channel)
    {
        assertNotNull(AlarmNotifications.audioManager);
        assertNotNull(AlarmNotifications.vibrator);
        assertNotNull(AlarmNotifications.players.get(channel));
        assertNotEqual(0, AlarmNotifications.players.get(channel).getAudioSessionId());
    }

    @Test
    public void test_isValidSoundUri()
    {
        String[] invalid = new String[] {"", " ", "http://", "https://", "invalid", "invalid://", "geo://33,-111"};
        assertFalse(AlarmNotifications.isValidSoundUri(null));
        for (String uriString : invalid) {
            Uri uri = Uri.parse(uriString);
            assertFalse(AlarmNotifications.isValidSoundUri(uri));
        }

        String[] valid = new String[] {"content://some/path", "file://some/path", "android.resource://some/path"};
        for (String uriString : valid) {
            Uri uri = Uri.parse(uriString);
            assertTrue(AlarmNotifications.isValidSoundUri(uri));
        }
    }

    @Test
    public void test_startAlert_valid()
    {
        AlarmClockItem alarm0 = new AlarmClockItem();
        alarm0.type = AlarmClockItem.AlarmType.ALARM;
        alarm0.vibrate = false;    // TODO: test vibrate
        alarm0.ringtoneURI = null;                                                                           // silent alarm

        AlarmClockItem alarm1 = new AlarmClockItem(alarm0);
        alarm1.ringtoneURI = AlarmSettings.VALUE_RINGTONE_DEFAULT;                                           // default alarm

        AlarmClockItem alarm2 = new AlarmClockItem(alarm0);
        alarm2.ringtoneURI = AlarmSettings.getFallbackRingtoneUri(mockContext, alarm2.type).toString();      // fallback alarm

        AlarmClockItem notify0 = new AlarmClockItem();
        notify0.type = AlarmClockItem.AlarmType.NOTIFICATION;
        alarm0.vibrate = false;    // TODO: test vibrate
        notify0.ringtoneURI = null;                                                                          // silent notification

        AlarmClockItem notify1 = new AlarmClockItem(notify0);
        notify1.ringtoneURI = AlarmSettings.VALUE_RINGTONE_DEFAULT;                                          // default notification

        AlarmClockItem notify2 = new AlarmClockItem(notify0);
        notify2.ringtoneURI = AlarmSettings.getFallbackRingtoneUri(mockContext, notify2.type).toString();    // fallback notification

        AlarmClockItem[] items = new AlarmClockItem[] { alarm0, alarm1, alarm2, notify0, notify1, notify2 };
        for (AlarmClockItem item : items) {
            test_startAlert(item);
        }
    }

    @Test
    public void test_startAlert_invalid()
    {
        AlarmClockItem alarm0 = new AlarmClockItem();
        alarm0.type = AlarmClockItem.AlarmType.ALARM;
        alarm0.vibrate = false;    // TODO: test vibrate
        alarm0.ringtoneURI = "invalid://sound/uri";

        AlarmClockItem alarm1 = new AlarmClockItem(alarm0);
        alarm1.ringtoneURI = "content://media/dne";

        AlarmClockItem alarm2 = new AlarmClockItem(alarm0);
        alarm2.ringtoneURI = "geo://33,-112";

        AlarmClockItem alarm3 = new AlarmClockItem(alarm0);
        alarm3.ringtoneURI = "invalid";

        AlarmClockItem notify0 = new AlarmClockItem(alarm0);
        notify0.type = AlarmClockItem.AlarmType.NOTIFICATION;

        AlarmClockItem notify1 = new AlarmClockItem(notify0);
        notify1.type = null;

        AlarmClockItem[] items = new AlarmClockItem[] { alarm0, alarm1, alarm2, alarm3, notify0, notify1 };
        for (AlarmClockItem item : items) {
            test_startAlert(item);
        }
    }

    public void test_startAlert(AlarmClockItem item)
    {
        assertTrue("test requires disabling do-not-disturb", AlarmNotifications.passesInterruptionFilter(mockContext, item));
        assertFalse(AlarmNotifications.isPlaying());

        String channel = item.type.name();

        AlarmNotifications.t_player_error = 0;
        AlarmNotifications.startAlert(mockContext, item);
        verify_initPlayer(channel);
        assertTrue(AlarmNotifications.isPlaying(channel));
        assertEquals(item.vibrate, AlarmNotifications.isVibrating);

        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 250)) { /* empty */ }
        assertEquals((item.ringtoneURI != null), AlarmNotifications.isPlaying(channel));
        assertEquals(0, AlarmNotifications.t_player_error);

        AlarmNotifications.stopAlert();
        assertFalse(AlarmNotifications.isPlaying(channel));
        assertFalse(AlarmNotifications.players.get(channel).isPlaying());
        assertFalse(AlarmNotifications.isVibrating);
    }

    @Test
    public void test_startAlertUri_fallback()
    {
        test_startAlertUri_notification(AlarmSettings.getFallbackRingtoneUri(mockContext, AlarmClockItem.AlarmType.NOTIFICATION));
        test_startAlertUri_notification(AlarmSettings.getFallbackRingtoneUri(mockContext, AlarmClockItem.AlarmType.ALARM));
    }

    @Test
    public void test_startAlertUri_notification0_default()
    {
        test_startAlertUri_notification(RingtoneManager.getActualDefaultRingtoneUri(mockContext, RingtoneManager.TYPE_NOTIFICATION));
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
        String[] invalid = new String[] {"", " ", "content", "content://", "content://media/dne", "http://", "https://",
                                         "invalid", "invalid://", "geo://33,-111", "file://dne"};    // expecting IOException
        test_startAlertUri_exception(null, false);
        for (String value : invalid) {
            test_startAlertUri_exception(Uri.parse(value), true);
            test_startAlertUri_exception(Uri.parse(value), false);
        }
    }

    public void test_startAlertUri_alarm(Uri uri, boolean fadeIn)
    {
        // pre-conditions
        assertFalse(AlarmNotifications.isPlaying());
        assertFalse(AlarmNotifications.isFadingIn);
        assertFalse(AlarmNotifications.isVibrating);

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(mockContext).edit();
        prefs.putInt(AlarmSettings.PREF_KEY_ALARM_FADEIN, fadeIn ? 3000 : 0).apply();

        AlarmNotifications.t_player_error = AlarmNotifications.t_player_error_extra = 0;
        String channel = AlarmClockItem.AlarmType.ALARM.name();
        MediaPlayer player = AlarmNotifications.initPlayer(mockContext, channel, true);    // player must be initialized first
        verify_initPlayer(channel);
        AlarmNotifications.t_volume = 0;
        try {
            AlarmNotifications.startAlert(mockContext, player, uri, true);
            assertFalse(AlarmNotifications.isPlaying());    // startAlert(Uri) doesn't toggle isPlaying (or call startVibration)
        } catch (Exception e) {
            Assert.fail("failed to startAlert: " + e);
        }

        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 1000)) {
            /* give it a second; mediaPlayer.start is async */
        }
        assertEquals(0, AlarmNotifications.t_player_error);
        assertTrue(player.isPlaying());
        assertTrue(player.isLooping());
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
        AlarmNotifications.setIsPlaying(channel, true);
        AlarmNotifications.stopAlert(true);
        assertFalse(AlarmNotifications.isPlaying());
        assertFalse(AlarmNotifications.isVibrating);
        assertFalse(player.isPlaying());
    }

    public void test_startAlertUri_notification(Uri uri)
    {
        assertFalse(AlarmNotifications.isPlaying());    // test pre-conditions
        assertFalse(AlarmNotifications.isFadingIn);

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(mockContext).edit();
        prefs.putInt(AlarmSettings.PREF_KEY_ALARM_FADEIN, 0).apply();

        AlarmNotifications.t_player_error = AlarmNotifications.t_player_error_extra = 0;
        String channel = AlarmClockItem.AlarmType.NOTIFICATION.name();
        MediaPlayer player = AlarmNotifications.initPlayer(mockContext, channel, true);    // player must be initialized first
        verify_initPlayer(channel);
        try {
            AlarmNotifications.startAlert(mockContext, player, uri, false);
        } catch (Exception e) {
            Assert.fail("failed to startAlert: " + e);
        }

        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 500)) {
            /* give it a second; the call to mediaPlayer.start is async */
        }
        assertEquals(0, AlarmNotifications.t_player_error);
        assertTrue(player.isPlaying());
        assertFalse(player.isLooping());
        assertEquals(1f, AlarmNotifications.t_volume);
        assertFalse(AlarmNotifications.isFadingIn);

        now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 5000)) {
            /* give it a few seconds for the sound to finish */
        }
        assertFalse(player.isPlaying());
    }

    public void test_startAlertUri_exception(Uri uri, boolean isAlarm)
    {
        String channel = (isAlarm ? AlarmClockItem.AlarmType.ALARM : AlarmClockItem.AlarmType.NOTIFICATION).name();
        MediaPlayer player = AlarmNotifications.initPlayer(mockContext, channel,true);
        verify_initPlayer(channel);
        try {
            AlarmNotifications.startAlert(mockContext, player, uri, isAlarm);
            Assert.fail("should have failed with IOException or SecurityException.. uri: " + uri);    // this line should be unreachable
        } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException e) { /* EMPTY */ }
        assertFalse(player.isPlaying());
    }

    public void test_startAlertUri_errorCode(Uri uri, boolean isAlarm, Integer code)
    {
        AlarmNotifications.t_player_error = AlarmNotifications.t_player_error_extra = 0;
        String channel = (isAlarm ? AlarmClockItem.AlarmType.ALARM : AlarmClockItem.AlarmType.NOTIFICATION).name();
        MediaPlayer player = AlarmNotifications.initPlayer(mockContext, channel, true);
        verify_initPlayer(channel);
        try {
            AlarmNotifications.startAlert(mockContext, player, uri, isAlarm);
        } catch (Exception e) {
            Assert.fail("failed to startAlert: uri: " + uri + ", " + e);
        }
        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + 500)) {
            /* give it a second; mediaPlayer.start is async */
        }
        assertNotEqual(0, AlarmNotifications.t_player_error);    // expecting some error code
        assertFalse(player.isPlaying());
        if (code != null) {
            assertEquals((int)code, AlarmNotifications.t_player_error);
        }
    }

}
