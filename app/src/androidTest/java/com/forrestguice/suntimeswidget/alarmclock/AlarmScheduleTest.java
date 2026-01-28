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

import android.util.Log;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.UnlistedTest;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.util.InstrumentationUtils;
import com.forrestguice.util.SuntimesJUnitTestRunner;
import com.forrestguice.util.android.AndroidResources;

import junit.framework.AssertionFailedError;

import org.junit.Before;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
@RunWith(SuntimesJUnitTestRunner.class)
public class AlarmScheduleTest
{
    public static final Location TEST_LOCATION = new Location("Phoenix", "33.45", "-111.94", "1263");
    public static final long SCHEDULE_WITHIN_MS = 10000;

    public Context context;
    public static final TimeDateDisplay utils = new TimeDateDisplay();

    @Rule
    public RetryRule retry = new RetryRule(3);

    @Before
    public void init() {
        context = InstrumentationUtils.getContext();
        EventUri.setBuildConfigInfo(new EventUri.BuildConfigInfo() {
            @Override
            public String AUTHORITY_ROOT() {
                return BuildConfig.SUNTIMES_AUTHORITY_ROOT;
            }
        });
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
            Calendar event = AlarmScheduler.updateAlarmTime_sunEvent(context, eventID, alarm.location, alarm.offset, alarm.repeating, alarm.repeatingDays, now);
            assertNotNull(event);
            if (event0 != null) {
                assertTrue(event.after(event0));
            }
            event0 = event;

            Log.i("TEST", utils.calendarDateTimeDisplayString(AndroidResources.wrap(context), event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " + (event.getTimeZone().inDaylightTime(event.getTime()) ? "[dst]" : "") );
            now = event;
            now.add(Calendar.SECOND, 1);
            c++;
        }
    }

    public static final Location location0 = new Location("Helsinki", "60", "25", "0");

    @Test
    public void test_updateAlarmTime_moonPhaseEvents()
    {
        SuntimesMoonData data = AlarmScheduler.getData_moonEvent(context, location0);
        data.setTodayIs(getCalendar(2024, Calendar.JUNE, 1, 18, 8));
        data.calculate(context);

        Calendar newMoon = data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.NEW);
        Calendar firstQuarter = data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FIRST_QUARTER);
        Calendar fullMoon = data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FULL);
        Calendar thirdQuarter = data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.THIRD_QUARTER);

        SuntimesCalculator.MoonPhase[] phases = new SuntimesCalculator.MoonPhase[] { SuntimesCalculator.MoonPhase.NEW, SuntimesCalculator.MoonPhase.FIRST_QUARTER, SuntimesCalculator.MoonPhase.FULL, SuntimesCalculator.MoonPhase.THIRD_QUARTER };
        Calendar[] calendars = new Calendar[] { newMoon, firstQuarter, fullMoon, thirdQuarter };

        for (int i=0; i<phases.length; i++)
        {
            Calendar calendar = calendars[i];
            calendar.add(Calendar.SECOND, 1);
            int month = calendar.get(Calendar.MONTH);
            int nextMonth = (month + 1);
            test_updateAlarmTime_moonPhaseEvent(nextMonth, SolarEvents.valueOf(phases[i]), calendar, phases[i].name());
        }
    }

    @Test
    public void test_updateAlarmTime_moonPhaseEvents_fullMoon()
    {
        Calendar now = getCalendar(2024, Calendar.JUNE, 21, 18, 8);
        test_updateAlarmTime_moonPhaseEvent(Calendar.JULY, SolarEvents.FULLMOON, now, "Full Moon");
    }
    @Test
    public void test_updateAlarmTime_moonPhaseEvents_firstQuarter()
    {
        Calendar now = getCalendar(2024, Calendar.JUNE, 13, 22, 19);
        test_updateAlarmTime_moonPhaseEvent(Calendar.JULY, SolarEvents.FIRSTQUARTER, now, "First Quarter");

        now = getCalendar(2024, Calendar.JULY, 13, 15, 49);
        test_updateAlarmTime_moonPhaseEvent(Calendar.AUGUST, SolarEvents.FIRSTQUARTER, now, "First Quarter");
    }
    @Test
    public void test_updateAlarmTime_moonPhaseEvents_thirdQuarter()
    {
        Calendar now = getCalendar(2024, Calendar.MAY, 30, 14, 54);
        test_updateAlarmTime_moonPhaseEvent(Calendar.JUNE, SolarEvents.THIRDQUARTER, now, "Third Quarter");

        now = getCalendar(2024, Calendar.JUNE, 28, 14, 54);
        test_updateAlarmTime_moonPhaseEvent(Calendar.JULY, SolarEvents.THIRDQUARTER, now, "Third Quarter");
    }
    @Test
    public void test_updateAlarmTime_moonPhaseEvent_newMoon()
    {
        Calendar now = getCalendar(2024, Calendar.JUNE, 6, 11, 16);
        test_updateAlarmTime_moonPhaseEvent(Calendar.JULY, SolarEvents.NEWMOON, now, "New Moon");
    }
    public void test_updateAlarmTime_moonPhaseEvent(int expectedMonth, SolarEvents eventID, Calendar now, String tag)
    {
        int c = 0, n = 24;
        while (c < n)
        {
            Calendar event = AlarmScheduler.updateAlarmTime_moonPhaseEvent(context, eventID, location0, 0, true, null, now);
            Log.i("TEST", tag + " " + c + " :: " + utils.calendarDateTimeDisplayString(AndroidResources.wrap(context), now, true, false) + " :: " + utils.calendarDateTimeDisplayString(AndroidResources.wrap(context), event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " + (event.getTimeZone().inDaylightTime(event.getTime()) ? "[dst]" : "") );
            assertEquals(expectedMonth, event.get(Calendar.MONTH));
            now.add(Calendar.HOUR, 1);
            c++;
        }
    }

    @Test
    public void test_updateAlarmTime_clockTime_utc() {
        test_updateAlarmTime_clockTime(TEST_LOCATION, TimeZones.TZID_UTC, 11, 0);
    }
    @Test
    public void test_updateAlarmTime_clockTime_lmt() {
        test_updateAlarmTime_clockTime(TEST_LOCATION, TimeZones.LocalMeanTime.TIMEZONEID, 11, 0);
    }
    @Test
    public void test_updateAlarmTime_clockTime_ltst() {
        test_updateAlarmTime_clockTime(TEST_LOCATION, TimeZones.ApparentSolarTime.TIMEZONEID, 11, 0);
    }

    public void test_updateAlarmTime_clockTime(Location location, String tzID, int hour, int minute)
    {
        AssertionFailedError e0 = null;
        Calendar now = getCalendar(2026, 0, 0, minute, 0);
        for (int i=0; i<365; i++)
        {
            try {
                now.add(Calendar.DAY_OF_YEAR, 1);
                now.set(Calendar.HOUR_OF_DAY, hour);
                test_updateAlarmTime_clockTime(now, hour, 0, location, tzID);

            } catch (AssertionFailedError e) {
                if (e0 != null) {
                    e0 = new AssertionFailedError(e0.getMessage() + ",\n" + e.getMessage());
                } else e0 = e;
            }
        }
        if (e0 != null) {
            throw e0;
        }
    }
    public void test_updateAlarmTime_clockTime(Calendar now, int hour, int minute, Location location, String tzID)
    {
        AlarmClockItem alarm = AlarmNotificationsTest.createAlarmClockItem(true);
        alarm.location = location;
        alarm.timezone = tzID;
        alarm.hour = hour;
        alarm.minute = minute;

        int c = 0, n = 3;
        Calendar event0 = null;
        Calendar event1 = Calendar.getInstance(AlarmClockItem.AlarmTimeZone.getTimeZone(TimeZones.LocalMeanTime.TIMEZONEID, alarm.location));

        while (c < n)
        {
            Calendar event = AlarmScheduler.updateAlarmTime_clockTime(alarm.hour, alarm.minute, alarm.timezone, alarm.location, alarm.offset, alarm.repeating, alarm.repeatingDays, now);
            assertNotNull(event);
            if (event0 != null) {
                assertTrue(event.after(event0));
                long d = event.getTimeInMillis() - event0.getTimeInMillis();
                assertTrue("scheduled events are too close! " + new TimeDeltaDisplay().timeDeltaLongDisplayString(d) + " apart :: ["
                                + new TimeDateDisplay().calendarDateTimeDisplayString(AndroidResources.wrap(context), event0, true, true, true) + "] -> ["
                                + new TimeDateDisplay().calendarDateTimeDisplayString(AndroidResources.wrap(context), event, true, true, true) + "]"
                        , (d > 12 * 60 * 60 * 1000));

                assertTrue("scheduled events are too far! " + new TimeDeltaDisplay().timeDeltaLongDisplayString(d) + " apart :: ["
                                + new TimeDateDisplay().calendarDateTimeDisplayString(AndroidResources.wrap(context), event0, true, true, true) + "] -> ["
                                + new TimeDateDisplay().calendarDateTimeDisplayString(AndroidResources.wrap(context), event, true, true, true) + "]"
                        , (d <= 24.1 * 60 * 60 * 1000));
            }

            boolean result = AlarmScheduler.updateAlarmTime((Context)null, alarm, now, true);
            assertTrue(result);
            assertEquals(event.getTimeInMillis(), alarm.timestamp);
            assertEquals("alarm.hour value should remain unchanged", hour, alarm.hour);
            assertEquals("alarm.minute value should remain unchanged", minute, alarm.minute);

            event0 = event;
            now.setTimeInMillis(event.getTimeInMillis());
            now.add(Calendar.SECOND, 1);
            c++;

            event1.setTimeInMillis(event.getTimeInMillis());
            Log.i("TEST", utils.calendarDateTimeDisplayString(null, event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " +
                    utils.calendarDateTimeDisplayString(null, event1, true, true).toString() + " [" + event1.getTimeZone().getID() + "] ");
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
            AlarmScheduler.t_updateAlarmTime_brokenLoop = false;
            AlarmScheduler.t_updateAlarmTime_runningLoop = false;

            alarm.modified = false;
            AlarmScheduler.updateAlarmTime(context, alarm, now, true);
            assertTrue(alarm.modified);

            Calendar event = Calendar.getInstance(now.getTimeZone());
            event.setTimeInMillis(alarm.timestamp);
            Log.i("TEST", utils.calendarDateTimeDisplayString(AndroidResources.wrap(context), event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " + (event.getTimeZone().inDaylightTime(event.getTime()) ? "[dst]" : "") );

            assertFalse(AlarmScheduler.t_updateAlarmTime_brokenLoop);
            assertFalse(AlarmScheduler.t_updateAlarmTime_runningLoop);
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
        //noinspection StatementWithEmptyBody
        while (System.currentTimeMillis() < (now + withinMs) && AlarmScheduler.t_updateAlarmTime_runningLoop) {
            /* busy wait.. the service needs to finish the command within 1000ms to pass the test */
        }
        assertFalse("failed to finish within " + withinMs + "ms!", AlarmScheduler.t_updateAlarmTime_runningLoop);
    }

}