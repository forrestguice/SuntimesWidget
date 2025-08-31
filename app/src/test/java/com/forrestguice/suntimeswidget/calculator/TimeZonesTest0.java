/**
    Copyright (C) 2019-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator;

import android.test.FlakyTest;

import com.forrestguice.suntimeswidget.UnlistedTest;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import net.time4j.Moment;
import net.time4j.TemporalType;
import net.time4j.calendar.astro.SolarTime;
import net.time4j.calendar.astro.StdSolarCalculator;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * non-instrumented tests moved from androidTest/settings/WidgetTimezonesTest
 */
@SuppressWarnings("ConstantConditions")
@Category(UnlistedTest.class)
public class TimeZonesTest0
{
    public static Location TEST_LOCATION = new Location("test","35", "-112");

    @Test
    public void test_timezone_apparentSolarTime_nullCalculator()
    {
        TimeZone tz0 = new TimeZones.ApparentSolarTime(-112, "Apparent Solar Time (Test: no args)");
        test_timezone(tz0, 16, 20, 0);
        assertNull(((TimeZones.ApparentSolarTime) tz0).getCalculator());
        assertTrue(tz0.useDaylightTime());
        assertTrue(tz0.inDaylightTime(new Date()));

        TimeZone tz1 = new TimeZones.ApparentSolarTime(-112, "Apparent Solar Time (Test: null calculator)", null);
        test_timezone(tz1, 7, 30, 15);
        assertNull(((TimeZones.ApparentSolarTime) tz1).getCalculator());
        assertTrue(tz1.useDaylightTime());
        assertTrue(tz1.inDaylightTime(new Date()));
    }

    @Test
    public void test_timezone_apparentSolarTime_SunriseSunsetJava()
    {
        test_timezone_apparentSolarTime_withCalculator(
                DefaultCalculatorDescriptors.SunriseSunsetJava(),
                TEST_LOCATION, 16, 20, 0);
    }

    @Test
    public void test_timezone_apparentSolarTime_CarmenSunriseSunset()
    {
        test_timezone_apparentSolarTime_withCalculator(
                DefaultCalculatorDescriptors.CarmenSunriseSunset(),
                TEST_LOCATION, 16, 20, 0);
    }

    @Test
    @FlakyTest
    public void test_timezone_apparentSolarTime_Time4J()
    {
        test_timezone_apparentSolarTime_withCalculator(
                DefaultCalculatorDescriptors.Time4A_4J(),
                TEST_LOCATION, 16, 20, 0);
    }

    public void test_timezone_apparentSolarTime_withCalculator(SuntimesCalculatorDescriptor descriptor, Location location, int hour, int minute, int second)
    {
        SuntimesCalculatorFactory factory = new SuntimesCalculatorFactory(descriptor);
        SuntimesCalculator calculator = factory.createCalculator(location, TimeZone.getDefault());
        TimeZone timezone = new TimeZones.ApparentSolarTime(location.getLongitudeAsDouble(), "Apparent Solar Time (Test: " + descriptor.getName() + ")", calculator);

        assertNotNull(((TimeZones.ApparentSolarTime) timezone).getCalculator());
        assertTrue(timezone.useDaylightTime());
        assertTrue(timezone.inDaylightTime(new Date()));
        test_timezone(timezone, hour, minute, second);
    }

    @Test
    @FlakyTest
    public void test_timezone_apparentSolarTime1_Time4J() {
        test_timezone_apparentSolarTime_offset_withCalculator(DefaultCalculatorDescriptors.Time4A_4J(), TEST_LOCATION, Calendar.getInstance());
    }

    public void test_timezone_apparentSolarTime_offset_withCalculator(SuntimesCalculatorDescriptor descriptor1, Location location, Calendar calendar)
    {
        SuntimesCalculatorFactory factory = new SuntimesCalculatorFactory(descriptor1);
        SuntimesCalculator calculator = factory.createCalculator(location, TimeZone.getDefault());

        TimeZones.ApparentSolarTime tz1 = new TimeZones.ApparentSolarTime(location.getLongitudeAsDouble(), "Apparent Solar Time (Test: " + descriptor1.getName() + ")", calculator);
        TimeZones.ApparentSolarTime tz2 = new TimeZones.ApparentSolarTime(location.getLongitudeAsDouble(), "Apparent Solar Time (Test: null", null);
        test_isApproximate(tz1.getOffset(calendar.getTimeInMillis()), tz2.getOffset(calendar.getTimeInMillis()), 60 * 1000);
    }

    @Test
    public void test_eot_Time4J()
    {
        Calendar calendar = Calendar.getInstance();
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(calendar.getTime());
        double eot0 = SolarTime.equationOfTime(moment, StdSolarCalculator.TIME4J.name());
        assertEquals(SolarTime.equationOfTime(moment, StdSolarCalculator.TIME4J.name()), eot0);
        test_eot(eot0, DefaultCalculatorDescriptors.Time4A_4J(), calendar);
    }

    public void test_eot(double eot0, SuntimesCalculatorDescriptor descriptor, Calendar calendar)
    {
        SuntimesCalculatorFactory factory = new SuntimesCalculatorFactory(descriptor);
        SuntimesCalculator calculator = factory.createCalculator(TEST_LOCATION, TimeZone.getDefault());
        double eot1 = calculator.equationOfTime(calendar);
        assertEquals(eot0, eot1);

        int eot2 = TimeZones.ApparentSolarTime.equationOfTimeOffset(calendar.getTimeInMillis(), calculator);
        assertEquals(eot2, TimeZones.ApparentSolarTime.equationOfTimeOffset(calendar.getTimeInMillis(), calculator));
    }

    protected void test_isApproximate(int value, int value1) {
        test_isApproximate(value, value1, 1);
    }
    protected void test_isApproximate(int value, int value1, int d) {
        assertTrue(Math.abs(value1 - value) <= d);
    }

    @Test
    public void test_timezone_localMeanTime()
    {
        TimeZone timezone = new TimeZones.LocalMeanTime(-112, "Local Mean Time (Test)");
        test_timezone(timezone, 16, 20, 0);
        assertFalse(timezone.useDaylightTime());
        assertFalse(timezone.inDaylightTime(new Date()));
    }

    @Test
    public void test_timezone_default()
    {
        TimeZone timezone = TimeZone.getDefault();
        test_timezone(timezone, 16, 20, 0);
    }

    protected void test_timezone(TimeZone timezone, int hour0, int minute0, int second0)
    {
        Calendar calendar = Calendar.getInstance(timezone);
        calendar.set(Calendar.HOUR_OF_DAY, hour0);
        calendar.set(Calendar.MINUTE, minute0);
        calendar.set(Calendar.SECOND, second0);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        int d_seconds = (second - second0) + ((minute * 60) - (minute0 * 60)) + ((hour * 60 * 60) - (hour0 * 60 * 60));
        assertEquals(0, d_seconds);

        assertEquals("[" + hour + ":" + minute + "]", hour0, hour);
        assertEquals("[" + hour + ":" + minute + "]", minute0, minute);
        assertEquals("[" + hour + ":" + minute + ":" + second0 + "]", second0, second);
    }

}
