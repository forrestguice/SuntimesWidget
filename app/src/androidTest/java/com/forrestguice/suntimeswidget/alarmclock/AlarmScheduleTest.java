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

import android.util.Log;

import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesDataSettings;
import com.forrestguice.util.SuntimesJUnitTestRunner;
import com.forrestguice.util.android.AndroidResources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
@RunWith(SuntimesJUnitTestRunner.class)
public class AlarmScheduleTest extends AlarmScheduleTestBase
{
    @Rule
    public RetryRule retry = new RetryRule(3);

    @Before
    public void init() {
        super.init();
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
            Calendar event = AlarmScheduler.updateAlarmTime_sunEvent(AndroidSuntimesDataSettings.wrap(context), eventID, alarm.location, alarm.offset, alarm.repeating, alarm.repeatingDays, now);
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
        SuntimesMoonData data = AlarmScheduler.getData_moonEvent(AndroidSuntimesDataSettings.wrap(context), location0);
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
            Calendar event = AlarmScheduler.updateAlarmTime_moonPhaseEvent(AndroidSuntimesDataSettings.wrap(context), eventID, location0, 0, true, null, now);
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
            test_runnable_finishes(run_updateAlarmTime_repeatingAlarm(event, now, AlarmItemInterface.everyday()), SCHEDULE_WITHIN_MS);
        }
        test_runnable_finishes(run_updateAlarmTime_repeatingAlarm(AlarmItemInterface.everyday(), now), SCHEDULE_WITHIN_MS);
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

}