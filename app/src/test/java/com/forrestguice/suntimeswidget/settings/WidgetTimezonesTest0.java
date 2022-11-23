/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorFactory;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4ASimpleSuntimesCalculator;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * non-instrumented tests moved from androidTest/settings/WidgetTimezonesTest
 */
@SuppressWarnings("ConstantConditions")
public class WidgetTimezonesTest0
{
    @Test
    public void test_timezone_apparentSolarTime()
    {
        TimeZone timezone1 = new WidgetTimezones.ApparentSolarTime(-112, "Apparent Solar Time (Test)");
        for (int i=0; i<10000; i++) {
            test_timezone(timezone1, 16, 20, 0);
        }
    }

    /**@Test
    public void test_timezone_apparentSolarTime1()
    {
        SuntimesCalculatorFactory factory = new SuntimesCalculatorFactory((Context)null, Time4ASimpleSuntimesCalculator.getDescriptor());
        SuntimesCalculator calculator = factory.createCalculator(new Location("test","35", "-112"), TimeZone.getDefault());
        TimeZone timezone0 = new WidgetTimezones.ApparentSolarTime(-112, "Apparent Solar Time (Test 0)", calculator);
        for (int i=0; i<1; i++) {
            test_timezone(timezone0, 16, 20, 0);
        }
    }*/

    @Test
    public void test_timezone_localMeanTime()
    {
        TimeZone timezone = new WidgetTimezones.LocalMeanTime(-112, "Local Mean Time (Test)");
        test_timezone(timezone, 16, 20, 0);
    }

    protected void test_timezone(TimeZone timezone, int hour, int minute, int second)
    {
        Calendar calendar = Calendar.getInstance(timezone);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        assertEquals(calendar.get(Calendar.HOUR_OF_DAY), hour);
        assertEquals(calendar.get(Calendar.MINUTE), minute);
        assertEquals(calendar.get(Calendar.SECOND), second);
    }
}
