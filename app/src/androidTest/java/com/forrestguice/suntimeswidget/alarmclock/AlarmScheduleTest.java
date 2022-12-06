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
import android.support.test.InstrumentationRegistry;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static android.test.MoreAsserts.assertNotEqual;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
@RunWith(AndroidJUnit4.class)
public class AlarmScheduleTest
{
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

    public SolarEvents[] getTestEvents()
    {
        return new SolarEvents[] { SolarEvents.MORNING_ASTRONOMICAL, SolarEvents.MORNING_NAUTICAL, SolarEvents.MORNING_CIVIL, SolarEvents.SUNRISE, SolarEvents.NOON,
                                   SolarEvents.SUNSET, SolarEvents.EVENING_CIVIL, SolarEvents.EVENING_NAUTICAL, SolarEvents.EVENING_ASTRONOMICAL,           // sun
                                   SolarEvents.MOONRISE, SolarEvents.MOONNOON, SolarEvents.MOONSET, SolarEvents.MOONNIGHT,                                  // moon
                                   SolarEvents.NEWMOON, SolarEvents.FIRSTQUARTER, SolarEvents.FULLMOON, SolarEvents.THIRDQUARTER,
                                   SolarEvents.EQUINOX_SPRING, SolarEvents.SOLSTICE_SUMMER, SolarEvents.EQUINOX_AUTUMNAL, SolarEvents.EQUINOX_AUTUMNAL      // solstice/equinox
        };
    }

    /**
     * Repeating Alarm
     */
    @Test
    public void test_updateAlarmTime_repeatingAlarm_nullDays()
    {
        Calendar now = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (SolarEvents event : getTestEvents()) {
            test_updateAlarmTime_repeatingAlarm(event.name(), null, now);
        }
    }

    @Test
    public void test_updateAlarmTime_repeatingAlarm_everyday()
    {
        Calendar now = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (SolarEvents event : getTestEvents()) {
            test_updateAlarmTime_repeatingAlarm(event.name(), AlarmClockItem.everyday(), now);
        }
    }

    public void test_updateAlarmTime_repeatingAlarm(String eventID, ArrayList<Integer> repeatingDays, Calendar now)
    {
        AlarmClockItem alarm = AlarmNotificationsTest.createAlarmClockItem(true);
        alarm.location = new Location("Helsinki", "60", "25", "0");
        alarm.timezone = "Europe/Hensinki";
        alarm.hour = alarm.minute = -1;
        alarm.repeatingDays = repeatingDays;
        alarm.setEvent(eventID);
        test_updateAlarmTime(alarm, now);
    }

    /**
     * Non-Repeating Alarm
     */
    @Test
    public void test_updateAlarmTime_alarm()
    {
        Calendar now = getCalendar(2022, Calendar.OCTOBER, 26, 7, 0);
        for (SolarEvents event : getTestEvents()) {
            test_updateAlarmTime_alarm(event.name(), now);
        }
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

            AlarmNotifications.updateAlarmTime(context, alarm, now, true);
            Calendar event = Calendar.getInstance(now.getTimeZone());
            event.setTimeInMillis(alarm.timestamp);
            Log.i("TEST", utils.calendarDateTimeDisplayString(context, event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " + (event.getTimeZone().inDaylightTime(event.getTime()) ? "[dst]" : "") );

            assertFalse(AlarmNotifications.t_updateAlarmTime_brokenLoop);
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













































}