/**
    Copyright (C) 2025 Forrest Guice
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
import android.util.Log;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SuntimesRiseSetDatasetTest
{
    private Context context;

    @Before
    public void setup() {
        context = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
    }

    @Test
    public void test_twilightLength()
    {
        Location location = new Location("Warsaw", "52.2319", "21.0067", "143");

        Calendar calendar0 = Calendar.getInstance();
        calendar0.set(2025, 0, 1);

        SuntimesRiseSetDataset data0 = new SuntimesRiseSetDataset(context, 0);
        data0.setLocation(location);
        data0.setTodayIs(calendar0);

        for (int i=0; i<365; i++)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar0.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_YEAR, i);

            SuntimesRiseSetDataset data = new SuntimesRiseSetDataset(data0);
            data.setTodayIs(calendar);
            data.calculateData(context);
            assertTrue(data.isCalculated());

            long[] duration_civil = data.civilTwilightLength();
            assertNotNull(duration_civil);
            assertEquals("duration array should have 2 parts", 2, duration_civil.length);
            assertDurationLessEquals("civil " + i, duration_civil, SIX_HOURS);
            assertDurationGreaterEquals("civil " + i, duration_civil, 0);

            long[] duration_nautical = data.nauticalTwilightLength();
            assertNotNull(duration_nautical);
            assertEquals("duration array should have 2 parts", 2, duration_nautical.length);
            assertDurationLessEquals("nautical " + i, duration_nautical, SIX_HOURS);
            assertDurationGreaterEquals("nautical " + i, duration_nautical, 0);

            long[] duration_astro = data.astroTwilightLength();
            assertNotNull(duration_astro);
            assertEquals("duration array should have 2 parts", 2, duration_astro.length);
            assertDurationLessEquals("astro " + i, duration_astro, SIX_HOURS);
            assertDurationGreaterEquals("astro " + i, duration_astro, 0);
        }
    }

    private static final long SIX_HOURS = 1000 * 60 * 60 * 6;

    private void assertDurationLessEquals(String message, long[] duration, long value) {
        assertTrue(message + " :: " + duration[0] + " not less than " + value, duration[0] <= value);
        assertTrue(message + " :: " + duration[1] + " not less than " + value, duration[1] <= value);
    }

    private void assertDurationGreaterEquals(String message, long[] duration, long value) {
        assertTrue(message + " :: " + duration[0] + " not greater than " + value, duration[0] >= value);
        assertTrue(message + "::" + duration[1] + " not greater than " + value, duration[1] >= value);
    }
}
