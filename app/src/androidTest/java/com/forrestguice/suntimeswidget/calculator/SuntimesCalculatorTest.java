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
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.TimeZone;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SuntimesCalculatorTest
{
    private Context mockContext;

    private WidgetSettings.Location[] locations = new WidgetSettings.Location[]
    {
            new WidgetSettings.Location("test0", "0", "0", "0"),             // 0, all zero
            new WidgetSettings.Location("test1", "35", "-112"),                      // 1, no altitude
            new WidgetSettings.Location("test2", "35", "-112", "-14"),       // 2, negative altitude
            new WidgetSettings.Location("test3", "35", "-112", "14")
    };

    @Before
    public void setup()
    {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
    }

    @Test
    public void test_init()
    {
        TimeZone timezone = TimeZone.getDefault();
        for (WidgetSettings.Location location : locations)
        {
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset.SunriseSunsetSuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.time4a.Time4ASimpleSuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.time4a.Time4ANOAASuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.time4a.Time4ACCSuntimesCalculator.getDescriptor());
            test_init(location, timezone, com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator.getDescriptor());
        }
    }
    public void test_init(WidgetSettings.Location location, TimeZone timezone, SuntimesCalculatorDescriptor descriptor)
    {
        SuntimesCalculatorFactory calculatorFactory = new SuntimesCalculatorFactory(mockContext, descriptor);
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);
        assertNotNull(calculator);
        assertTrue("calculator name (" + calculator.name() + ") should match descriptor (" + descriptor.name() + ")", calculator.name().equals(descriptor.name()));
    }

}
