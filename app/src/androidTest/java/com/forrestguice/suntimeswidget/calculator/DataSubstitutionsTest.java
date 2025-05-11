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

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DataSubstitutionsTest
{
    private Context context;

    @Before
    public void setup() {
        context = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
    }

    public static final String[] patterns = new String[] { "%loc", "%lat", "%lon", "%lel", "%t", "%s", "%id", "%d", "%dY", "%dD", "%dd", "%dT", "%dt", "%dm", "%%" };
    public static String getPatternString(String[] patterns)
    {
        StringBuilder pattern0 = new StringBuilder();
        for (int i=0; i<patterns.length; i++) {
            pattern0.append(patterns[i]);
        }
        return pattern0.toString();
    }

    @Test
    public void test_displayStringForTitlePattern_data()
    {
        String pattern0 = getPatternString(patterns);
        String[] result0 = new String[] {
                DataSubstitutions.displayStringForTitlePattern(context, pattern0, (SuntimesData) null),
        };
        for (String r0 : result0) {
            assertTrue("result should be empty", r0.isEmpty());   // null data; all patterns should have been replaced with ""
        }
    }

    @Test
    public void test_displayStringForTitlePattern_sunData()
    {
        String pattern0 = getPatternString(patterns);
        String[] result0 = new String[] {
                DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m", (SuntimesRiseSetData) null),
        };
        for (String r0 : result0) {
            assertTrue("result should be empty", r0.isEmpty());   // null data; all patterns should have been replaced with ""
        }
    }

    @Test
    public void test_displayStringForTitlePattern_moonData()
    {
        String pattern0 = getPatternString(patterns);
        String[] result0 = new String[] {
                DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m%i", (SuntimesMoonData) null)
        };
        for (String r0 : result0) {
            assertTrue("result should be empty", r0.isEmpty());   // null data; all patterns should have been replaced with ""
        }
    }

    @Test
    public void test_displayStringForTitlePattern_equinoxData()
    {
        String pattern0 = getPatternString(patterns);
        String[] result0 = new String[] {
                DataSubstitutions.displayStringForTitlePattern(context, pattern0 + "%M%o%m", (SuntimesEquinoxSolsticeData) null)
        };
        for (String r0 : result0) {
            assertTrue("result should be empty", r0.isEmpty());   // null data; all patterns should have been replaced with ""
        }
    }

}
