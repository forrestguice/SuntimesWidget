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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.UnlistedTest;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import org.junit.Before;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
@RunWith(AndroidJUnit4.class)
public class AlarmScheduleTest
{
    public static final long SCHEDULE_WITHIN_MS = 1000;

    public Context context;
    public SuntimesUtils utils = new SuntimesUtils();

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void test_updateAlarmTime_sunEvent()
    {
        AlarmClockItem alarm = AlarmNotificationsTest.createAlarmClockItem(true);
        alarm.location = new Location("Helsinki", "60", "25", "0");
        alarm.timezone = "Europe/Hensinki";
        alarm.hour = -1;
        alarm.minute = -1;

        SolarEvents eventID = SolarEvents.SUNRISE;
        Calendar now = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);

        int c = 0, n = 15;
        Calendar event0 = null;
        while (c < n)
        {
            Calendar event = AlarmNotifications.updateAlarmTime_sunEvent(context, eventID, alarm.location, alarm.offset, alarm.repeating, alarm.repeatingDays, now);
            assertNotNull(event);
            if (event0 != null) {
                assertTrue(event.after(event0));
            }
            event0 = event;

            Log.i("TEST", utils.calendarDateTimeDisplayString(context, event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " + (event.getTimeZone().inDaylightTime(event.getTime()) ? "[dst]" : "") );
            now = event;
            now.add(Calendar.SECOND, 1);
            c++;
        }
    }

    public String[] getTestEvents()
    {
        ArrayList<String> events = new ArrayList<>();
        for (SolarEvents event : SolarEvents.values()) {    // SolarEvents enum
            events.add(event.name());
        }
        return events.toArray(new String[0]);
    }

    public String[] getAddonTestEvents()
    {
        ArrayList<String> events = new ArrayList<>();
        events.add("content://suntimes.naturalhour.provider/eventInfo/0_1_0");   // Natural Hour
        events.add("content://suntimes.intervalmidpoints.provider/eventInfo/sunset_astrorise_2_0");   // Interval Midpoints
        return events.toArray(new String[0]);
    }

    /**
     * Addon Alarms
     */
    @Test
    @Category(UnlistedTest.class)
    public void test_updateAlarmTime_addonAlarm()
    {
        Calendar now = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (String event : getAddonTestEvents())
        {
            test_updateAlarmTime_repeatingAlarm(event, null, now);
            test_updateAlarmTime_repeatingAlarm(event, AlarmClockItem.everyday(), now);
            test_updateAlarmTime_alarm(event, now);
        }
    }

    /**
     * Repeating Alarm
     */
    @Test
    public void test_updateAlarmTime_repeatingAlarm_nullDays()
    {
        Calendar now = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (String event : getTestEvents()) {
            test_runnable_finishes(run_updateAlarmTime_repeatingAlarm(event, now, null), SCHEDULE_WITHIN_MS);
        }
        test_runnable_finishes(run_updateAlarmTime_repeatingAlarm(null, now), SCHEDULE_WITHIN_MS);
    }

    @Test
    public void test_updateAlarmTime_repeatingAlarm_emptyDays()
    {
        final Calendar date = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (final String event : getTestEvents()) {
            test_runnable_finishes(run_updateAlarmTime_repeatingAlarm(event, date, new ArrayList<Integer>()), SCHEDULE_WITHIN_MS);
        }
        test_runnable_finishes(run_updateAlarmTime_repeatingAlarm(new ArrayList<Integer>(), date), SCHEDULE_WITHIN_MS);
    }

    @Test
    public void test_updateAlarmTime_repeatingAlarm_everyday()
    {
        Calendar now = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (String event : getTestEvents()) {    // "event" alarms
            test_runnable_finishes(run_updateAlarmTime_repeatingAlarm(event, now, AlarmClockItem.everyday()), SCHEDULE_WITHIN_MS);
        }
        test_runnable_finishes(run_updateAlarmTime_repeatingAlarm(AlarmClockItem.everyday(), now), SCHEDULE_WITHIN_MS);
    }

    protected Runnable run_updateAlarmTime_repeatingAlarm(final ArrayList<Integer> repeatingDays, final Calendar date) {
        return new Runnable() {
            public void run() {
                test_updateAlarmTime_repeatingAlarm(repeatingDays, date);    // "clock time" alarm
            }
        };
    }
    public void test_updateAlarmTime_repeatingAlarm(ArrayList<Integer> repeatingDays, Calendar now)
    {
        AlarmClockItem alarm = AlarmNotificationsTest.createAlarmClockItem(true);
        alarm.location = new Location("Helsinki", "60", "25", "0");
        alarm.timezone = "Europe/Hensinki";
        alarm.repeatingDays = repeatingDays;
        alarm.setEvent(null);                     // "clock time" alarm
        test_updateAlarmTime(alarm, now);
    }

    protected Runnable run_updateAlarmTime_repeatingAlarm(final String event, final Calendar date, final ArrayList<Integer> repeatingDays) {
        return new Runnable() {
            public void run() {
                test_updateAlarmTime_repeatingAlarm(event, repeatingDays, date);    // "event" alarms
            }
        };
    }
    public void test_updateAlarmTime_repeatingAlarm(String eventID, ArrayList<Integer> repeatingDays, Calendar now)
    {
        AlarmClockItem alarm = AlarmNotificationsTest.createAlarmClockItem(true);
        alarm.location = new Location("Helsinki", "60", "25", "0");
        alarm.timezone = "Europe/Hensinki";
        alarm.repeatingDays = repeatingDays;
        alarm.setEvent(eventID);                  // "event" alarm
        alarm.hour = alarm.minute = -1;
        test_updateAlarmTime(alarm, now);
    }

    /**
     * Non-Repeating Alarm
     */
    @Test
    public void test_updateAlarmTime_alarm()
    {
        Calendar now = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (String event : getTestEvents()) {
            test_runnable_finishes(run_updateAlarmTime_alarm(event, now), SCHEDULE_WITHIN_MS);
        }
        test_runnable_finishes(run_updateAlarmTime_alarm(now), SCHEDULE_WITHIN_MS);
    }

    protected Runnable run_updateAlarmTime_alarm(final Calendar now) {
        return new Runnable() {
            @Override
            public void run() {
                test_updateAlarmTime_alarm(now);
            }
        };
    }
    public void test_updateAlarmTime_alarm(Calendar now)
    {
        AlarmClockItem alarm = AlarmNotificationsTest.createAlarmClockItem(false);
        alarm.location = new Location("Helsinki", "60", "25", "0");
        alarm.timezone = "Europe/Hensinki";
        alarm.repeatingDays = AlarmClockItem.everyday();
        alarm.setEvent(null);
        test_updateAlarmTime(alarm, now);
    }

    protected Runnable run_updateAlarmTime_alarm(final String eventID, final Calendar now) {
        return new Runnable() {
            @Override
            public void run() {
                test_updateAlarmTime_alarm(eventID, now);
            }
        };
    }
    public void test_updateAlarmTime_alarm(String eventID, Calendar now)
    {
        AlarmClockItem alarm = AlarmNotificationsTest.createAlarmClockItem(false);
        alarm.location = new Location("Helsinki", "60", "25", "0");
        alarm.timezone = "Europe/Hensinki";
        alarm.hour = alarm.minute = -1;
        alarm.repeatingDays = AlarmClockItem.everyday();
        alarm.setEvent(eventID);
        test_updateAlarmTime(alarm, now);
    }

    public void test_updateAlarmTime(AlarmClockItem alarm, Calendar now)
    {
        int c = 0, n = 15;
        Calendar event0 = null;
        while (c < n)
        {
            AlarmNotifications.t_updateAlarmTime_brokenLoop = false;
            AlarmNotifications.t_updateAlarmTime_runningLoop = false;

            alarm.modified = false;
            AlarmNotifications.updateAlarmTime(context, alarm, now, true);
            assertTrue(alarm.modified);

            Calendar event = Calendar.getInstance(now.getTimeZone());
            event.setTimeInMillis(alarm.timestamp);
            Log.i("TEST", utils.calendarDateTimeDisplayString(context, event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " + (event.getTimeZone().inDaylightTime(event.getTime()) ? "[dst]" : "") );

            assertFalse(AlarmNotifications.t_updateAlarmTime_brokenLoop);
            assertFalse(AlarmNotifications.t_updateAlarmTime_runningLoop);
            if (event0 != null) {
                assertTrue(event.after(event0));
            }
            event0 = event;

            now = event;
            now.add(Calendar.SECOND, 1);
            c++;
        }
    }

    public static Calendar getCalendar(int year, int month, int dayOfMonth, int hour, int minute)
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    protected void test_runnable_finishes(Runnable runnable, long withinMs)
    {
        final Handler onStartHandler = new Handler(Looper.getMainLooper());
        onStartHandler.post(runnable);
        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() < (now + withinMs)) {
            /* busy wait.. the service needs to finish the command within 1000ms to pass the test */
        }
        assertFalse("failed to finish within " + withinMs + "ms!", AlarmNotifications.t_updateAlarmTime_runningLoop);
    }

}