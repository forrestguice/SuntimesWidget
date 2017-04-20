/**
    Copyright (C) 2017 Forrest Guice
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
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AppSettingsTest extends SuntimesActivityTestBase
{
    private Context context;

    @Before
    public void init()
    {
        context = activityRule.getActivity();
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_localeModePref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    @Test
    public void test_localePref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_themePref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    @Test
    public void test_showWarningsPref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    @Test
    public void test_showLightmapPref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    @Test
    public void test_clockTapPref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    @Test
    public void test_dateTapPref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    @Test
    public void test_noteTapPref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_timeZoneSortPref()
    {
        AppSettings.setTimeZoneSortPref(context, WidgetTimezones.TimeZoneSort.SORT_BY_ID);
        WidgetTimezones.TimeZoneSort sort2 = AppSettings.loadTimeZoneSortPref(context);
        assertTrue("pref should be SORT_BY_ID but was " + sort2, sort2 == WidgetTimezones.TimeZoneSort.SORT_BY_ID);

        AppSettings.setTimeZoneSortPref(context, WidgetTimezones.TimeZoneSort.SORT_BY_OFFSET);
        WidgetTimezones.TimeZoneSort sort1 = AppSettings.loadTimeZoneSortPref(context);
        assertTrue("pref should be SORT_BY_OFFSET but was " + sort1, sort1 == WidgetTimezones.TimeZoneSort.SORT_BY_OFFSET);

        AppSettings.setTimeZoneSortPref(context, AppSettings.PREF_DEF_UI_TIMEZONESORT);
        WidgetTimezones.TimeZoneSort sort0 = AppSettings.loadTimeZoneSortPref(context);
        assertTrue("pref should be default (SORT_BY_ID) but was " + sort0, sort0 == AppSettings.PREF_DEF_UI_TIMEZONESORT && sort0 == WidgetTimezones.TimeZoneSort.SORT_BY_ID);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_gpsMaxAgePref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    @Test
    public void test_gpsMinElapsedPref()
    {
        assertTrue("STUB: TODO", true == false);
    }

    @Test
    public void test_gpsMaxElapsedPref()
    {
        assertTrue("STUB: TODO", true == false);
    }
}
