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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        String pattern0 = getPatternString(patterns) + getPatternAtString(DataSubstitutions.ALL_PATTERNS_AT, DataSubstitutions.ALL_AT_SUFFIXES);
        String result0 = DataSubstitutions.displayStringForTitlePattern0(context, pattern0, (SuntimesData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesRiseSetData data1 = new SuntimesRiseSetData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern0(context, pattern0 + "%M%o%m", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m") || result1.contains("%o"));
    }

    @Test
    public void test_displayStringForTitlePattern_data()
    {
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(context, pattern0, (SuntimesData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesClockData data1 = new SuntimesClockData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(context, pattern0, (SuntimesData) data1);
        assertFalse("result should not be empty", result1.isEmpty());
    }

    @Test
    public void test_displayStringForTitlePattern_clockData()
    {
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%m%M", (SuntimesClockData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesClockData data1 = new SuntimesClockData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%m%M", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m"));
    }

    @Test
    public void test_displayStringForTitlePattern_sunData()
    {
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m", (SuntimesRiseSetData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesRiseSetData data1 = new SuntimesRiseSetData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m") || result1.contains("%o"));
    }

    @Test
    public void test_displayStringForTitlePattern_moonData()
    {
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m%i", (SuntimesMoonData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesMoonData data1 = new SuntimesMoonData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m%i", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m") || result1.contains("%o") || result1.contains("%i"));
    }

    @Test
    public void test_displayStringForTitlePattern_equinoxData()
    {
        String pattern0 = getPatternString(patterns);
        String result0 = DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m", (SuntimesEquinoxSolsticeData) null);
        assertTrue("result should be empty", result0.isEmpty());   // null data; all patterns should have been replaced with ""

        SuntimesEquinoxSolsticeData data1 = new SuntimesEquinoxSolsticeData(context, 0);
        data1.calculate(context);
        String result1 = DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m", data1);
        assertFalse("result should not be empty", result1.isEmpty());
        assertFalse("result should not contain patterns", result1.contains("%M") || result1.contains("%m") || result1.contains("%o"));
    }

}
