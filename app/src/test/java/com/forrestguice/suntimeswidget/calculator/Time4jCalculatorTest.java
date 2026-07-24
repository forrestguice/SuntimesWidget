package com.forrestguice.suntimeswidget.calculator;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4ACCSuntimesCalculator;

import net.time4j.tz.ZonalOffset;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

public class Time4jCalculatorTest
{
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void test_zonalOffset_0_165()
    {
        for (double lon = 0; lon <= 165; lon += 0.0001d) {
            test_zonalOffset_atLongitude(lon);
        }
        for (double lon = 0; lon >= -165; lon -= 0.0001d) {
            test_zonalOffset_atLongitude(lon);
        }
    }
    @Test
    public void test_zonalOffset_165_180()
    {
        for (double lon = 165; lon <= 180; lon += 0.1) {
            test_zonalOffset_atLongitude(lon);
        }
        for (double lon = -165; lon >= -180; lon -= 0.1) {
            test_zonalOffset_atLongitude(lon);
        }
    }
    @Test
    @Ignore("test expected to fail")
    public void test_zonalOffset_165_180_01()
    {
        for (double lon = 165; lon <= 180; lon += 0.01) {
            test_zonalOffset_atLongitude(lon);
        }
        for (double lon = -165; lon >= -180; lon -= 0.01) {
            test_zonalOffset_atLongitude(lon);
        }
    }
    public void test_zonalOffset_atLongitude(double lon)
    {
        try {
            ZonalOffset.atLongitude(new BigDecimal(lon));
        } catch (IllegalArgumentException e) {
            collector.addError(new IllegalArgumentException(e.getMessage() + " :: " + lon));
        }
    }

    @Test
    public void test_illegalZonalOffset_0_165_CC() {
        test_illegalZonalOffset_0_165(new Time4ACCSuntimesCalculator());
    }
    @Test
    public void test_illegalZonalOffset_0_165_4J() {
        test_illegalZonalOffset_0_165(new Time4A4JSuntimesCalculator());
    }
    public void test_illegalZonalOffset_0_165(SuntimesCalculator calculator)
    {
        for (double lon = 0; lon <= 165; lon += 0.001) {
            test_illegalZonalOffset(lon, calculator);
        }
        for (double lon = 0; lon >= -165; lon -= 0.001) {
            test_illegalZonalOffset(lon, calculator);
        }
    }

    @Test
    public void test_illegalZonalOffset_165_170_CC() {
        test_illegalZonalOffset_165_170(new Time4ACCSuntimesCalculator());
    }
    @Test
    public void test_illegalZonalOffset_165_170_4J() {
        test_illegalZonalOffset_165_170(new Time4A4JSuntimesCalculator());
    }
    public void test_illegalZonalOffset_165_170(SuntimesCalculator calculator)
    {
        for (double lon = 165; lon <= 170; lon += 0.01) {
            test_illegalZonalOffset(lon, calculator);
        }
        for (double lon = -165; lon >= -170; lon -= 0.01) {
            test_illegalZonalOffset(lon, calculator);
        }
    }

    public void test_illegalZonalOffset(double longitude, SuntimesCalculator calculator)
    {
        Location location = new Location("Test", "0", "" + longitude, "0");
        calculator.init(location, "UTC");
        test_illegalZonalOffset(calculator);
    }
    public void test_illegalZonalOffset(SuntimesCalculator calculator)
    {
        try {
            Calendar event = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance());
        } catch (IllegalArgumentException e) {
            collector.addError(new IllegalArgumentException(e.getMessage() + " :: longitude: " + calculator.getLocation().getLongitudeAsDouble()));
        }
    }

    private static final BigDecimal MRD = new BigDecimal(1000000000);
    private static final BigDecimal DECIMAL_240 = new BigDecimal(240);

    protected boolean isFractional(double v)
    {
        BigDecimal longitude = new BigDecimal(v);
        BigDecimal offset = longitude.multiply(DECIMAL_240);
        BigDecimal integral = offset.setScale(0, RoundingMode.DOWN);
        BigDecimal delta = offset.subtract(integral);
        BigDecimal decimal = delta.setScale(9, RoundingMode.HALF_UP).multiply(MRD);
        int total = integral.intValueExact();
        int fraction = decimal.intValueExact();
        return (fraction != 0);
    }

}
