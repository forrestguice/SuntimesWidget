package com.forrestguice.suntimeswidget.calculator;

import com.forrestguice.suntimeswidget.UnlistedTest;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.TemporalType;
import net.time4j.calendar.astro.SolarTime;
import net.time4j.calendar.astro.StdSolarCalculator;
import net.time4j.tz.ZonalOffset;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

@Category(UnlistedTest.class)
public class SunPositionTest1
{
    @Test
    public void test_SunPosition()
    {
        // setup
        double latitude = 33.45579;
        double longitude = -111.9485;  // Phoenix, AZ
        int altitude = 360;

        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar date0 = Calendar.getInstance(tz);
        date0.setTimeInMillis(1639791526);   // Dec 17, 2021
        PlainDate plainDate = calendarToPlainDate(date0, tz);

        SolarTime solarTime = SolarTime.ofLocation(latitude, longitude, altitude, StdSolarCalculator.TIME4J);
        SolarTime.Calculator calculator = solarTime.getCalculator();

        // find elevation at given date/time
        Moment moment0 = TemporalType.JAVA_UTIL_DATE.translate(date0.getTime());
        net.time4j.calendar.astro.SunPosition position0 = net.time4j.calendar.astro.SunPosition.at(moment0, solarTime);
        double elevation0 = position0.getElevation();

        // find date/time of given elevation
        double geodeticAngle = StdSolarCalculator.TIME4J.getGeodeticAngle(latitude, altitude);
        double zenith = 90 + geodeticAngle - elevation0;
        Moment moment1 = calculator.sunset(plainDate, latitude, longitude, zenith);
        Calendar date1 = momentToCalendar(moment1, tz);

        //noinspection PointlessArithmeticExpression
        double toleranceMs = 1 * 60 * 1000;    // 1min
        long difference = date1.getTimeInMillis() - date0.getTimeInMillis();
        assertTrue("expected time near " + date0.getTimeInMillis() + " (within " + (toleranceMs / (1000d * 60d)) + " min); " + date1.getTimeInMillis() + " differs by " + (difference / (1000d * 60d)) + " min",
                Math.abs(difference) < toleranceMs);
    }

    public static Calendar momentToCalendar(Moment moment, TimeZone timezone)
    {
        Calendar retValue = null;
        if (moment != null)
        {
            retValue = new GregorianCalendar();
            retValue.setTimeZone(timezone);
            retValue.setTime(TemporalType.JAVA_UTIL_DATE.from(moment));
        }
        return retValue;
    }

    public static PlainDate calendarToPlainDate(Calendar input, TimeZone timezone)
    {
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(input.getTime());
        ZonalOffset zonalOffset = ZonalOffset.ofTotalSeconds(timezone.getOffset(input.getTimeInMillis()) / 1000);
        return moment.toZonalTimestamp(zonalOffset).toDate();
    }
}
