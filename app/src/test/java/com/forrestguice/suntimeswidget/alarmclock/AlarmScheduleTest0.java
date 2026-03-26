/**
    Copyright (C) 2023 Forrest Guice
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
import android.util.Log;

import com.forrestguice.suntimeswidget.UnlistedTest;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Calendar;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class AlarmScheduleTest0
{
    public static final TimeDateDisplay utils = new TimeDateDisplay();

    @Test
    @Category(UnlistedTest.class)
    public void test_updateAlarmTime_clockTime_lmst()
    {
        int hour = 6;
        int minute = 30;

        AlarmClockItem alarm = createAlarmClockItem(true);
        alarm.location = new Location("Phoenix", "33.45", "-111.94", "1263");
        alarm.timezone = TimeZones.LocalMeanTime.TIMEZONEID;
        alarm.hour = hour;
        alarm.minute = minute;

        alarm.setRepeatingDays(null);
        test_updateAlarmTime_clockTime_lmst(alarm, 1, 7, hour, minute);

        alarm.setRepeatingDays("7");   // 7; every saturday
        test_updateAlarmTime_clockTime_lmst(alarm, 7, 14, hour, minute);
    }

    public void test_updateAlarmTime_clockTime_lmst(AlarmClockItem alarm, int expectedInterval, int n, int hour, int minute)
    {
        int c = 0;
        Calendar event0 = null;
        //Calendar event1 = Calendar.getInstance();
        Calendar now = getCalendar(2023, Calendar.JUNE, 22, 5, 0);

        while (c < n)
        {
            //noinspection ConstantConditions
            Calendar event = AlarmScheduler.updateAlarmTime_clockTime(alarm.hour, alarm.minute, alarm.timezone, alarm.location, alarm.offset, alarm.repeating, alarm.repeatingDays, now);
            assertNotNull(event);
            if (event0 != null) {
                assertTrue(event.after(event0));
                assertEquals(event.getTimeInMillis() - event0.getTimeInMillis(), expectedInterval * 24 * 60 * 60 * 1000);
            }

            boolean result = AlarmScheduler.updateAlarmTime((SuntimesDataSettings) null, alarm, now, true);
            assertTrue(result);
            assertEquals("hour value should remain unchanged", hour, alarm.hour);
            assertEquals("minute value should remain unchanged", minute, alarm.minute);

            /*event1.setTimeInMillis(event.getTimeInMillis());
            Log.i("TEST", utils.calendarDateTimeDisplayString(null, event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " + (event.getTimeZone().inDaylightTime(event.getTime()) ? "[dst]" : "") +
                    utils.calendarDateTimeDisplayString(null, event1, true, true).toString() + " [" + event1.getTimeZone().getID() + "] " + (event1.getTimeZone().inDaylightTime(event1.getTime()) ? "[dst]" : "") +
                    utils.calendarDateTimeDisplayString(null, now, true, true).toString() + " [" + now.getTimeZone().getID() + "] " + (now.getTimeZone().inDaylightTime(now.getTime()) ? "[dst]" : "") );*/

            event0 = event;
            now.setTimeInMillis(event.getTimeInMillis());
            now.add(Calendar.SECOND, 1);
            c++;
        }
    }

    @Test
    @Category(UnlistedTest.class)
    public void test_updateAlarmTime_clockTime_ltst()
    {
        int hour = 6;
        int minute = 30;

        AlarmClockItem alarm = createAlarmClockItem(true);
        alarm.location = new Location("Phoenix", "33.45", "-111.94", "1263");
        alarm.timezone = TimeZones.ApparentSolarTime.TIMEZONEID;
        alarm.hour = hour;
        alarm.minute = minute;

        int c = 0, n = 7;
        Calendar event0 = null;
        Calendar event1 = Calendar.getInstance(AlarmTimeZone.getTimeZone(TimeZones.LocalMeanTime.TIMEZONEID, alarm.location));
        Calendar now = getCalendar(2023, Calendar.JUNE, 22, 7, 0);

        while (c < n)
        {
            assertNotNull(alarm.repeatingDays);
            Calendar event = AlarmScheduler.updateAlarmTime_clockTime(alarm.hour, alarm.minute, alarm.timezone, alarm.location, alarm.offset, alarm.repeating, alarm.repeatingDays, now);
            assertNotNull(event);
            if (event0 != null) {
                assertTrue(event.after(event0));
            }

            boolean result = AlarmScheduler.updateAlarmTime((SuntimesDataSettings) null, alarm, now, true);
            assertTrue(result);
            assertEquals(event.getTimeInMillis(), alarm.timestamp);
            assertEquals("hour value should remain unchanged", hour, alarm.hour);
            assertEquals("minute value should remain unchanged", minute, alarm.minute);

            event0 = event;
            now.setTimeInMillis(event.getTimeInMillis());
            now.add(Calendar.SECOND, 1);
            c++;

            event1.setTimeInMillis(event.getTimeInMillis());
            Log.i("TEST", utils.calendarDateTimeDisplayString(null, event, true, true).toString() + " [" + event.getTimeZone().getID() + "] " +
                    utils.calendarDateTimeDisplayString(null, event1, true, true).toString() + " [" + event1.getTimeZone().getID() + "] ");
        }
    }

    public static AlarmClockItem createAlarmClockItem(boolean repeating)
    {
        AlarmClockItem alarm = new AlarmClockItem();
        alarm.type = AlarmType.ALARM;
        alarm.repeating = repeating;
        alarm.repeatingDays = AlarmItemInterface.everyday();

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