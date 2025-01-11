package com.forrestguice.suntimeswidget.calculator;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.forrestguice.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.UnlistedTest;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.TemporalType;
import net.time4j.calendar.astro.SolarTime;
import net.time4j.calendar.astro.StdSolarCalculator;
import net.time4j.calendar.astro.Twilight;
import net.time4j.engine.CalendarDate;
import net.time4j.engine.ChronoFunction;
import net.time4j.tz.ZonalOffset;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

@Category(UnlistedTest.class)
public class SunPositionTest
{
    /* Test Fix: add the geodetic angle before comparing elevations; brings twilight elevations into agreement with expected values. */
    public static final boolean APPLY_ALTITUDE_ANGLE = false;

    public static final double TEST_TOLERANCE = 0.01;        // degrees
    public static final double TEST_LATITUDE = 33.45579;
    public static final double TEST_LONGITUDE = -111.9485;    // Phoenix, AZ
    public static final int TEST_ALTITUDE = 360;
    public static final long TEST_DATE = 1639791526;         // Dec 17, 2021
    public static final TimeZone TEST_TIMEZONE = TimeZone.getTimeZone("UTC");
    public static final StdSolarCalculator TEST_CALCULATOR = StdSolarCalculator.TIME4J;

    @Before
    public void setup() {
        date.setTimeInMillis(TEST_DATE);
        solarTime = SolarTime.ofLocation(TEST_LATITUDE, TEST_LONGITUDE, TEST_ALTITUDE, TEST_CALCULATOR);
    }
    private Calendar date = Calendar.getInstance();
    private SolarTime solarTime;

    @Test
    public void test_SunPosition()
    {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(1639791526);   // Dec 17, 2021
        PlainDate plainDate = calendarToPlainDate(date, TimeZone.getTimeZone("UTC"));

        Moment moment0 = TemporalType.JAVA_UTIL_DATE.translate(date.getTime());
        net.time4j.calendar.astro.SunPosition position0 = net.time4j.calendar.astro.SunPosition.at(moment0, solarTime);
        double elevation0 = position0.getElevation();

        double geodeticAngle = TEST_CALCULATOR.getGeodeticAngle(TEST_LATITUDE, TEST_ALTITUDE);
        double zenith = 90 + geodeticAngle - elevation0;

        Moment moment1 = solarTime.getCalculator().sunrise(plainDate, TEST_LATITUDE, TEST_LONGITUDE, zenith);
        net.time4j.calendar.astro.SunPosition position1 = net.time4j.calendar.astro.SunPosition.at(moment1, solarTime);
        double elevation1 = position1.getElevation() + geodeticAngle;

        assertTrue("expects value near " + elevation0 + " (within " + TEST_TOLERANCE + "); " + elevation1,
                Math.abs(elevation0 - elevation1) < TEST_TOLERANCE);
    }

    @Test
    public void test_sunPositionAtSunrise() {
        testElevationAtSunrise(date, solarTime, null, 0, TEST_TOLERANCE);
    }
    @Test
    public void test_sunPositionAtSunset() {
        testElevationAtSunset(date, solarTime, null, 0, TEST_TOLERANCE);
    }

    @Test
    public void test_sunPositionAtCivilRise() {
        testElevationAtSunrise(date, solarTime, Twilight.CIVIL, -6, TEST_TOLERANCE);
    }
    @Test
    public void test_sunPositionAtCivilSunset() {
        testElevationAtSunset(date, solarTime, Twilight.CIVIL, -6, TEST_TOLERANCE);
    }

    @Test
    public void test_sunPositionAtNauticalRise() {
        testElevationAtSunrise(date, solarTime, Twilight.NAUTICAL, -12, TEST_TOLERANCE);
    }
    @Test
    public void test_sunPositionAtNauticalSunset() {
        testElevationAtSunset(date, solarTime, Twilight.NAUTICAL, -12, TEST_TOLERANCE);
    }

    @Test
    public void test_sunPositionAtAstroRise() {
        testElevationAtSunrise(date, solarTime, Twilight.ASTRONOMICAL, -18, TEST_TOLERANCE);
    }
    @Test
    public void test_sunPositionAtAstroSunset() {
        testElevationAtSunset(date, solarTime, Twilight.ASTRONOMICAL, -18, TEST_TOLERANCE);
    }

    public void testElevationAtSunrise(Calendar date, SolarTime solarTime, @Nullable Twilight twilight, double expectedElevation, double toleranceDegrees)
    {
        ChronoFunction<CalendarDate, Moment> sunrise = (twilight != null ? solarTime.sunrise(twilight) : solarTime.sunrise());
        Moment moment = calendarToPlainDate(date, TEST_TIMEZONE).get(sunrise);
        net.time4j.calendar.astro.SunPosition position = net.time4j.calendar.astro.SunPosition.at(moment, solarTime);
        testElevationAtPosition(position, expectedElevation, toleranceDegrees);
    }
    public void testElevationAtSunset(Calendar date, SolarTime solarTime, @Nullable Twilight twilight, double expectedElevation, double toleranceDegrees)
    {
        ChronoFunction<CalendarDate, Moment> sunset = (twilight != null ? solarTime.sunset(twilight) : solarTime.sunset());
        Moment moment = calendarToPlainDate(date, TEST_TIMEZONE).get(sunset);
        net.time4j.calendar.astro.SunPosition position = net.time4j.calendar.astro.SunPosition.at(moment, solarTime);
        testElevationAtPosition(position, expectedElevation, toleranceDegrees);
    }

    @Test
    public void test_sunPositionAtMorningGoldenHour() {
        testRisingAngle(6, TEST_TOLERANCE);
    }
    @Test
    public void test_sunPositionAtEveningGoldenHour() {
        testSettingAngle(6, TEST_TOLERANCE);
    }
    @Test
    public void test_sunPositionAt10Degrees() {
        testSettingAngle(10, TEST_TOLERANCE);
    }
    @Test
    public void test_sunPositionAt25Degrees() {
        testSettingAngle(25, TEST_TOLERANCE);
    }

    public void testRisingAngle(double angle, double toleranceDegrees)
    {
        double geodeticAngle = TEST_CALCULATOR.getGeodeticAngle(TEST_LATITUDE, TEST_ALTITUDE);
        double zenith = 90 + geodeticAngle - angle;
        PlainDate plainDate = calendarToPlainDate(date, TEST_TIMEZONE);
        Moment moment = solarTime.getCalculator().sunrise(plainDate, TEST_LATITUDE, TEST_LONGITUDE, zenith);
        net.time4j.calendar.astro.SunPosition position = net.time4j.calendar.astro.SunPosition.at(moment, solarTime);
        testElevationAtPosition(position, angle, toleranceDegrees);
    }
    public void testSettingAngle(double angle, double toleranceDegrees)
    {
        double geodeticAngle = TEST_CALCULATOR.getGeodeticAngle(TEST_LATITUDE, TEST_ALTITUDE);
        double zenith = 90 + geodeticAngle - angle;
        PlainDate plainDate = calendarToPlainDate(date, TEST_TIMEZONE);
        Moment moment = solarTime.getCalculator().sunset(plainDate, TEST_LATITUDE, TEST_LONGITUDE, zenith);
        net.time4j.calendar.astro.SunPosition position = net.time4j.calendar.astro.SunPosition.at(moment, solarTime);
        testElevationAtPosition(position, angle, toleranceDegrees);
    }

    public void testElevationAtPosition(net.time4j.calendar.astro.SunPosition position, double expectedElevation, double toleranceDegrees)
    {
        double elevation = position.getElevation();

        if (APPLY_ALTITUDE_ANGLE) {
            elevation += TEST_CALCULATOR.getGeodeticAngle(TEST_LATITUDE, TEST_ALTITUDE);
        }

        assertTrue("elevation should be near " + expectedElevation + " (within " + toleranceDegrees + "); " + elevation,
                Math.abs(expectedElevation - elevation) < toleranceDegrees);
    }

    /**
     * this utility method used routinely by Time4ASuntimesCalculator
     */
    public static PlainDate calendarToPlainDate(Calendar input, TimeZone timezone)
    {
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(input.getTime());
        ZonalOffset zonalOffset = ZonalOffset.ofTotalSeconds(timezone.getOffset(input.getTimeInMillis()) / 1000);
        return moment.toZonalTimestamp(zonalOffset).toDate();
    }
}
