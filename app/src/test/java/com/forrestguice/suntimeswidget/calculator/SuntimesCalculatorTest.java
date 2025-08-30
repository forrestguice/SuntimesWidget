/**
    Copyright (C) 2018 Forrest Guice
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

import android.content.Context;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import org.junit.Test;
import java.util.TimeZone;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SuntimesCalculatorTest
{
    public static final Location[] TEST_LOCATIONS = new Location[]
    {
            new Location("test0", "0", "0", "0"),             // 0, all zero
            new Location("test1", "35", "-112"),                      // 1, no altitude
            new Location("test2", "35", "-112", "-14"),       // 2, out of bounds (negative altitude)
            new Location("test3", "35", "-112", "14"),        // 3
            new Location("test4", "35", "-112", "10999"),     // 4, at bounds (max altitude)
            new Location("test5", "35", "-112", "11000")      // 5, out of bounds (max altitude + 1)
    };

    @Test
    public void test_init()
    {
        TimeZone timezone = TimeZone.getDefault();
        for (Location location : TEST_LOCATIONS)
        {
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset.SunriseSunsetSuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.time4a.Time4ASimpleSuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.time4a.Time4ANOAASuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.time4a.Time4ACCSuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator.getDescriptor());
        }
    }
    public void test_init(Location location, TimeZone timezone, SuntimesCalculatorDescriptor descriptor)
    {
        SuntimesCalculatorFactory calculatorFactory = new SuntimesCalculatorFactory(descriptor);
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);
        assertNotNull(calculator);
        assertTrue("calculator name (" + calculator.name() + ") should match descriptor (" + descriptor.getName() + ")", calculator.name().equals(descriptor.getName()));
    }

}
