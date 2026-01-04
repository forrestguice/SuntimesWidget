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
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesDataSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DataSubstitutionsTest
{
    private Context context;

    @Before
    public void setup() {
        context = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
    }

    public static final String[] patterns = new String[] { "%loc", "%lat", "%lon", "%lel", "%t", "%s", "%id", "%d", "%dY", "%dD", "%dd", "%dT", "%dt", "%dm", "%h", "%H", "%eot", "%eot_m", "%%" };
    public static String getPatternString(String[] patterns)
    {
        StringBuilder pattern = new StringBuilder();
        for (int i=0; i<patterns.length; i++) {
            pattern.append(patterns[i]);
        }
        return pattern.toString();
    }
    public static String getPatternAtString(String[] patternsAt, String[] suffixes)
    {
        StringBuilder pattern = new StringBuilder();
        for (String patternAt : patternsAt) {
            for (String suffix : suffixes) {
                pattern.append(patternAt).append(suffix);
            }
        }
        return pattern.toString();
    }

    @Test
    public void test_displayStringForTitlePattern0()
    {
        SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
        String pattern0 = getPatternString(patterns) + getPatternAtString(DataSubstitutions.ALL_PATTERNS_AT, DataSubstitutions.ALL_AT_SUFFIXES);
        String result0 = DataSubstitutions.displayStringForTitlePattern0(contextIntf, pattern0, (SuntimesData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesRiseSetData data1 = new SuntimesRiseSetData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern0(contextIntf, pattern0 + "%M%o%m", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m") || result1.contains("%o"));
    }


    @Test
    public void test_displayStringForTitlePattern0_em()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, 5, 20);

        SuntimesRiseSetData data = new SuntimesRiseSetData(context, 0);
        data.setLocation(new Location("Phoenix", "33.45579", "-111.94580", "385"));
        data.setTodayIs(calendar);
        data.calculate(context);

        HashMap<SolarEvents, String> patterns_em = DataSubstitutions.getPatternsForEvent(DataSubstitutions.PATTERN_em_at, SolarEvents.values());
        for (SolarEvents event : patterns_em.keySet())
        {
            String pattern = patterns_em.get(event);

            long bench_start = System.nanoTime();
            SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
            String result = DataSubstitutions.displayStringForTitlePattern0(contextIntf, pattern, data);
            long bench_end = System.nanoTime();
            Log.d("DEBUG", "displayStringForTitlePattern0: " + ((bench_end - bench_start) / 1000000.0) + " ms");
            assertFalse("result should not contain patterns " + pattern, result.contains(pattern));

            Calendar eventTime = DataSubstitutions.getCalendarForEvent(event, data);
            if (eventTime != null)
            {
                assertFalse("result should not be empty for " + pattern, result.isEmpty());
                assertEquals("result should contain eventTime for " + pattern, eventTime.getTimeInMillis() + "", result);

            } else {
                assertTrue("result should empty for " + pattern, result.isEmpty());
            }
        }
    }

    /**
     * https://github.com/forrestguice/SuntimesWidget/issues/874
     * 1. %eT@sn is incorrect (sunset instead of noon).
     * 2. %eT@sn is missing (empty result for northern locations during summer months).
     */
    @Test
    public void test_displayStringForTitlePattern_issue_874()
    {
        Location location = new Location("Berlin", "52.5243", "13.4105", "40");
        String pattern = "%eT@sn";

        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, 5, 20);

        SuntimesRiseSetData data = new SuntimesRiseSetData(context, 0);
        data.setLocation(location);
        data.setTodayIs(calendar);
        data.calculate(context);

        long bench_start = System.nanoTime();
        SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern0(contextIntf, pattern, data);
        long bench_end = System.nanoTime();
        Log.d("DEBUG", "displayStringForTitlePattern0: " + ((bench_end - bench_start) / 1000000.0) + " ms");
        assertFalse("result should not contain patterns", result1.contains("%eT@sn"));
        assertFalse("result should not be empty", result1.isEmpty());

        SuntimesUtils utils = new SuntimesUtils();
        Calendar eventTime = data.calculator().getSolarNoonCalendarForDate(calendar);
        String displayString = utils.calendarTimeShortDisplayString(context, eventTime, true).toString();
        assertEquals("result should be formatted time of solar noon", displayString, result1);
    }

    @Test
    public void test_displayStringForTitlePattern_issue_874_1()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, 5, 20);

        SuntimesRiseSetData data = new SuntimesRiseSetData(context, 0);
        data.setLocation(new Location("Phoenix", "33.45579", "-111.94580", "385"));
        data.setTodayIs(calendar);
        data.calculate(context);

        SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
        String result = DataSubstitutions.displayStringForTitlePattern0(contextIntf, "%eT@sn", data);
        assertFalse("result should not contain patterns", result.contains("%eT@sn"));
        assertFalse("result should not be empty", result.isEmpty());

        SuntimesUtils utils = new SuntimesUtils();
        Calendar eventTime = data.calculator().getSolarNoonCalendarForDate(calendar);
        String displayString = utils.calendarTimeShortDisplayString(context, eventTime, true).toString();
        assertEquals("result should be formatted time of solar noon", displayString, result);
    }

    @Test
    public void test_displayStringForTitlePattern_data()
    {
        String pattern0 = getPatternString(patterns);
        SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
        String result0 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0, (SuntimesData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesClockData data1 = new SuntimesClockData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0, (SuntimesData) data1);
        assertFalse("result should not be empty", result1.isEmpty());
    }

    @Test
    public void test_displayStringForTitlePattern_clockData()
    {
        SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0 + "%m%M", (SuntimesClockData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesClockData data1 = new SuntimesClockData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0 + "%m%M", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m"));
    }

    @Test
    public void test_displayStringForTitlePattern_sunData()
    {
        SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0 + "%M%o%m", (SuntimesRiseSetData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesRiseSetData data1 = new SuntimesRiseSetData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0 + "%M%o%m", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m") || result1.contains("%o"));
    }

    @Test
    public void test_displayStringForTitlePattern_moonData()
    {
        SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0 + "%M%o%m%i", (SuntimesMoonData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesMoonData data1 = new SuntimesMoonData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0 + "%M%o%m%i", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m") || result1.contains("%o") || result1.contains("%i"));
    }

    @Test
    public void test_displayStringForTitlePattern_equinoxData()
    {
        SuntimesDataSettings contextIntf = new AndroidSuntimesDataSettings(context);
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0 + "%M%o%m", (SuntimesEquinoxSolsticeData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesEquinoxSolsticeData data1 = new SuntimesEquinoxSolsticeData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(contextIntf, pattern0 + "%M%o%m", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m") || result1.contains("%o"));
    }

}
