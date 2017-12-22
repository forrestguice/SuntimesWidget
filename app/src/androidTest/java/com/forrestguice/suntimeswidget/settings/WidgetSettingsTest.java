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

import java.util.Calendar;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class WidgetSettingsTest extends SuntimesActivityTestBase
{
    @Test
    public void test_timeFormatModePref()
    {
        Context context = activityRule.getActivity();
        int appWidgetId = Integer.MAX_VALUE;

        WidgetSettings.saveTimeFormatModePref(context, appWidgetId, WidgetSettings.TimeFormatMode.MODE_SYSTEM);
        WidgetSettings.TimeFormatMode mode3 = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        assertTrue("mode should be system but was " + mode3, mode3 == WidgetSettings.TimeFormatMode.MODE_SYSTEM);

        WidgetSettings.saveTimeFormatModePref(context, appWidgetId, WidgetSettings.TimeFormatMode.MODE_24HR);
        WidgetSettings.TimeFormatMode mode2 = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        assertTrue("mode should be 24 hr but was " + mode2, mode2 == WidgetSettings.TimeFormatMode.MODE_24HR);

        WidgetSettings.saveTimeFormatModePref(context, appWidgetId, WidgetSettings.TimeFormatMode.MODE_12HR);
        WidgetSettings.TimeFormatMode mode1 = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        assertTrue("mode should be 12 hr but was " + mode1, mode1 == WidgetSettings.TimeFormatMode.MODE_12HR);

        WidgetSettings.deleteTimeFormatModePref(context, appWidgetId);
        WidgetSettings.TimeFormatMode mode0 = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        assertTrue("mode should be default (system) but was " + mode0, mode0 == WidgetSettings.PREF_DEF_APPEARANCE_TIMEFORMATMODE &&
                mode0 == WidgetSettings.TimeFormatMode.MODE_SYSTEM);
    }

    private Context context;
    private int appWidgetId = Integer.MAX_VALUE;

    @Before
    public void init()
    {
        context = activityRule.getActivity();
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_calculatorModePref()
    {
        assertTrue("STUB", true == false);  // TODO
    }

    @Test
    public void test_timeModePref()
    {
        assertTrue("STUB", true == false);  // TODO
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_dateModePref()
    {
        WidgetSettings.saveDateModePref(context, appWidgetId, WidgetSettings.DateMode.CURRENT_DATE);
        WidgetSettings.DateMode mode2 = WidgetSettings.loadDateModePref(context, appWidgetId);
        assertTrue("mode should be CURRENT_DATE but was " + mode2, mode2 == WidgetSettings.DateMode.CURRENT_DATE);

        WidgetSettings.saveDateModePref(context, appWidgetId, WidgetSettings.DateMode.CUSTOM_DATE);
        WidgetSettings.DateMode mode1 = WidgetSettings.loadDateModePref(context, appWidgetId);
        assertTrue("mode should be CUSTOM_DATE but was " + mode1, mode1 == WidgetSettings.DateMode.CUSTOM_DATE);

        WidgetSettings.deleteDateModePref(context, appWidgetId);
        WidgetSettings.DateMode mode0 = WidgetSettings.loadDateModePref(context, appWidgetId);
        assertTrue("mode should be default (CURRENT_DATE) but was " + mode0, mode0 == WidgetSettings.PREF_DEF_DATE_MODE && mode0 == WidgetSettings.DateMode.CURRENT_DATE);
    }

    @Test
    public void test_datePref()
    {
        WidgetSettings.DateInfo date3 = new WidgetSettings.DateInfo(Calendar.getInstance());
        assertTrue("date should be set", date3.isSet());

        int y = 2017;
        int m = 6;
        int d = 27;
        WidgetSettings.DateInfo date2 = new WidgetSettings.DateInfo(y, m, d);
        WidgetSettings.DateInfo date1 = new WidgetSettings.DateInfo(y, m, d);
        assertTrue("dates should match", date2.equals(date1));

        WidgetSettings.saveDatePref(context, appWidgetId, date1);
        WidgetSettings.DateInfo info1 = WidgetSettings.loadDatePref(context, appWidgetId);
        assertTrue("dates should match (" + date1.getYear() + "." + date1.getMonth() + "." + date1.getDay() + " != " + info1.getYear() + "." + info1.getMonth() + "." + info1.getDay() + ")", info1.equals(date1));

        WidgetSettings.DateInfo date0 = new WidgetSettings.DateInfo(WidgetSettings.PREF_DEF_DATE_YEAR, WidgetSettings.PREF_DEF_DATE_MONTH, WidgetSettings.PREF_DEF_DATE_DAY);
        WidgetSettings.deleteDatePref(context, appWidgetId);
        WidgetSettings.DateInfo info0 = WidgetSettings.loadDatePref(context, appWidgetId);
        assertTrue("dates should match (" + WidgetSettings.PREF_DEF_DATE_YEAR + "." + WidgetSettings.PREF_DEF_DATE_MONTH + "." + WidgetSettings.PREF_DEF_DATE_DAY + " != " + info0.getYear() + "." + info0.getMonth() + "." + info0.getDay() + ")", info0.equals(date0) && !info0.isSet());
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_timezoneModePref()
    {
        WidgetSettings.saveTimezoneModePref(context, appWidgetId, WidgetSettings.TimezoneMode.CURRENT_TIMEZONE);
        WidgetSettings.TimezoneMode mode2 = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        assertTrue("mode should be CURRENT but was " + mode2, mode2 == WidgetSettings.TimezoneMode.CURRENT_TIMEZONE);

        WidgetSettings.saveTimezoneModePref(context, appWidgetId, WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);
        WidgetSettings.TimezoneMode mode1 = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        assertTrue("mode should be CUSTOM but was " + mode1, mode1 == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);

        WidgetSettings.deleteTimezoneModePref(context, appWidgetId);
        WidgetSettings.TimezoneMode mode0 = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        assertTrue("mode should be default (CURRENT) but was " + mode0, mode0 == WidgetSettings.PREF_DEF_TIMEZONE_MODE && mode0 == WidgetSettings.TimezoneMode.CURRENT_TIMEZONE);
    }

    @Test
    public void test_timezonePref()
    {
        assertTrue("STUB", true == false);  // TODO
    }

    @Test
    public void test_solarTimeModePref()
    {
        WidgetSettings.saveSolarTimeModePref(context, appWidgetId, WidgetSettings.SolarTimeMode.LOCAL_MEAN_TIME);
        WidgetSettings.SolarTimeMode mode2 = WidgetSettings.loadSolarTimeModePref(context, appWidgetId);
        assertTrue("mode should be LOCAL_MEAN_TIME but was " + mode2, mode2 == WidgetSettings.SolarTimeMode.LOCAL_MEAN_TIME);

        WidgetSettings.saveSolarTimeModePref(context, appWidgetId, WidgetSettings.SolarTimeMode.APPARENT_SOLAR_TIME);
        WidgetSettings.SolarTimeMode mode1 = WidgetSettings.loadSolarTimeModePref(context, appWidgetId);
        assertTrue("mode should be APPARENT_SOLAR_TIME but was " + mode1, mode1 == WidgetSettings.SolarTimeMode.APPARENT_SOLAR_TIME);

        WidgetSettings.deleteSolarTimeModePref(context, appWidgetId);
        WidgetSettings.SolarTimeMode mode0 = WidgetSettings.loadSolarTimeModePref(context, appWidgetId);
        assertTrue("mode should be default (LOCAL_MEAN_TIME) but was " + mode0, mode0 == WidgetSettings.SolarTimeMode.LOCAL_MEAN_TIME && mode0 == WidgetSettings.PREF_DEF_TIMEZONE_SOLARMODE);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_locationModePref()
    {
        WidgetSettings.saveLocationModePref(context, appWidgetId, WidgetSettings.LocationMode.CUSTOM_LOCATION);
        WidgetSettings.LocationMode mode2 = WidgetSettings.loadLocationModePref(context, appWidgetId);
        assertTrue("mode should be CUSTOM but was " + mode2, mode2 == WidgetSettings.LocationMode.CUSTOM_LOCATION);

        WidgetSettings.saveLocationModePref(context, appWidgetId, WidgetSettings.LocationMode.CURRENT_LOCATION);
        WidgetSettings.LocationMode mode1 = WidgetSettings.loadLocationModePref(context, appWidgetId);
        assertTrue("mode should be CURRENT but was " + mode1, mode1 == WidgetSettings.LocationMode.CURRENT_LOCATION);

        WidgetSettings.deleteLocationModePref(context, appWidgetId);
        WidgetSettings.LocationMode mode0 = WidgetSettings.loadLocationModePref(context, appWidgetId);
        assertTrue("mode should be default (CUSTOM) but was " + mode0, mode0 == WidgetSettings.PREF_DEF_LOCATION_MODE && mode0 == WidgetSettings.LocationMode.CUSTOM_LOCATION);
    }

    @Test public void test_locationPref()
    {
        assertTrue("STUB", true == false);  // TODO
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_actionModePref()
    {
        WidgetSettings.saveActionModePref(context, appWidgetId, WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG);
        WidgetSettings.ActionMode mode3 = WidgetSettings.loadActionModePref(context, appWidgetId);
        assertTrue("mode should be ONTAP_LAUNCH_CONFIG but was " + mode3, mode3 == WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG);

        WidgetSettings.saveActionModePref(context, appWidgetId, WidgetSettings.ActionMode.ONTAP_DONOTHING);
        WidgetSettings.ActionMode mode2 = WidgetSettings.loadActionModePref(context, appWidgetId);
        assertTrue("mode should be ONTAP_DONOTHING but was " + mode2, mode2 == WidgetSettings.ActionMode.ONTAP_DONOTHING);

        WidgetSettings.saveActionModePref(context, appWidgetId, WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY);
        WidgetSettings.ActionMode mode1 = WidgetSettings.loadActionModePref(context, appWidgetId);
        assertTrue("mode should be ONTAP_LAUNCH_ACTIVITY but was " + mode1, mode1 == WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY);

        WidgetSettings.deleteActionModePref(context, appWidgetId);
        WidgetSettings.ActionMode mode0 = WidgetSettings.loadActionModePref(context, appWidgetId);
        assertTrue("mode should be default (ONTAP_LAUNCH_CONFIG) but was " + mode0, mode0 == WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG && mode0 == WidgetSettings.PREF_DEF_ACTION_MODE);
    }

    @Test
    public void test_actionLaunchPref()
    {
        String value2 = "com.forrestguice.suntimeswidget.SuntimesActivity";
        WidgetSettings.saveActionLaunchPref(context, appWidgetId, value2);
        String pref2 = WidgetSettings.loadActionLaunchPref(context, appWidgetId);
        assertTrue("pref should be " + value2 + " but was " + pref2, pref2.equals(value2));

        String value1 = "test value 1";
        WidgetSettings.saveActionLaunchPref(context, appWidgetId, value1);
        String pref1 = WidgetSettings.loadActionLaunchPref(context, appWidgetId);
        assertTrue("pref should be " + value1 + " but was " + pref1, pref1.equals(value1));

        String value0 = "com.forrestguice.suntimeswidget.SuntimesActivity";
        WidgetSettings.deleteActionLaunchPref(context, appWidgetId);
        String pref0 = WidgetSettings.loadActionLaunchPref(context, appWidgetId);
        assertTrue("pref should be default (" + value0 + ") but was " + pref0, pref0.equals(value0) && value0.equals(WidgetSettings.PREF_DEF_ACTION_LAUNCH));
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_themePref()
    {
        assertTrue("STUB", true == false);  // TODO
    }

    @Test
    public void test_showTitlePref()
    {
        WidgetSettings.saveShowTitlePref(context, appWidgetId, false);
        boolean pref2 = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        assertTrue("pref should be false but was " + pref2, !pref2);

        WidgetSettings.saveShowTitlePref(context, appWidgetId, true);
        boolean pref1 = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        assertTrue("pref should be true but was " + pref1, pref1);

        WidgetSettings.deleteShowTitlePref(context, appWidgetId);
        boolean pref0 = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        assertTrue("mode should be default (false) but was " + pref0, !pref0 && pref0 == WidgetSettings.PREF_DEF_APPEARANCE_SHOWTITLE);
    }

    @Test
    public void test_titleTextPref()
    {
        String title2 = "Widget Title";
        WidgetSettings.saveTitleTextPref(context, appWidgetId, title2);
        String pref2 = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        assertTrue("pref should be \"" + title2 + "\" but was \"" + pref2 + "\"", pref2.equals(title2));

        String title1 = "%M Widget %m %lat %lon %loc %t %s Title %%";
        WidgetSettings.saveTitleTextPref(context, appWidgetId, title1);
        String pref1 = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        assertTrue("pref should be \"" + title1 + "\" but was \"" + pref1 + "\"", pref1.equals(title1));

        WidgetSettings.deleteTitleTextPref(context, appWidgetId);
        String pref0 = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        assertTrue("pref should be default (empty string) but was \"" + pref0 + "\"", pref0.equals(WidgetSettings.PREF_DEF_APPEARANCE_TITLETEXT) && pref0.equals(""));
    }

    @Test
    public void test_allowResizePref()
    {
        WidgetSettings.saveAllowResizePref(context, appWidgetId, true);
        boolean pref2 = WidgetSettings.loadAllowResizePref(context, appWidgetId);
        assertTrue("pref should be true but was " + pref2, pref2);

        WidgetSettings.saveAllowResizePref(context, appWidgetId, false);
        boolean pref1 = WidgetSettings.loadAllowResizePref(context, appWidgetId);
        assertTrue("pref should be false but was " + pref1, !pref1);

        WidgetSettings.deleteAllowResizePref(context, appWidgetId);
        boolean pref0 = WidgetSettings.loadAllowResizePref(context, appWidgetId);
        assertTrue("mode should be default (true) but was " + pref0, pref0 && pref0 == WidgetSettings.PREF_DEF_APPEARANCE_ALLOWRESIZE);
    }

    @Test
    public void test_1x1ModePref()
    {
        assertTrue("STUB", true == false);  // TODO
    }

    @Test
    public void test_compareModePref()
    {
        WidgetSettings.saveCompareModePref(context, appWidgetId, WidgetSettings.CompareMode.TOMORROW);
        WidgetSettings.CompareMode mode2 = WidgetSettings.loadCompareModePref(context, appWidgetId);
        assertTrue("mode should be TOMORROW but was " + mode2, mode2 == WidgetSettings.CompareMode.TOMORROW);

        WidgetSettings.saveCompareModePref(context, appWidgetId, WidgetSettings.CompareMode.YESTERDAY);
        WidgetSettings.CompareMode mode1 = WidgetSettings.loadCompareModePref(context, appWidgetId);
        assertTrue("mode should be YESTERDAY but was " + mode1, mode1 == WidgetSettings.CompareMode.YESTERDAY);

        WidgetSettings.deleteCompareModePref(context, appWidgetId);
        WidgetSettings.CompareMode mode0 = WidgetSettings.loadCompareModePref(context, appWidgetId);
        assertTrue("mode should be default (TOMORROW) but was " + mode0, mode0 == WidgetSettings.CompareMode.TOMORROW && mode0 == WidgetSettings.PREF_DEF_GENERAL_COMPAREMODE);
    }

    @Test
    public void test_showComparePref()
    {
        WidgetSettings.saveShowComparePref(context, appWidgetId, true);
        boolean showCompare = WidgetSettings.loadShowComparePref(context, appWidgetId);
        assertTrue("showCompare should be true but was " + showCompare, showCompare);

        WidgetSettings.saveShowComparePref(context, appWidgetId, false);
        showCompare = WidgetSettings.loadShowComparePref(context, appWidgetId);
        assertTrue("showCompare should be false was " + showCompare, !showCompare);

        WidgetSettings.deleteShowComparePref(context, appWidgetId);
        showCompare = WidgetSettings.loadShowComparePref(context, appWidgetId);
        assertTrue("showNoon should be default (true) but was " + showCompare, showCompare && showCompare == WidgetSettings.PREF_DEF_GENERAL_SHOWCOMPARE);
    }

    @Test
    public void test_showNoonPref()
    {
        WidgetSettings.saveShowNoonPref(context, appWidgetId, false);
        boolean showNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        assertTrue("showNoon should be false but was " + showNoon, !showNoon);

        WidgetSettings.saveShowNoonPref(context, appWidgetId, true);
        showNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        assertTrue("showNoon should be true was " + showNoon, showNoon);

        WidgetSettings.deleteShowNoonPref(context, appWidgetId);
        showNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        assertTrue("showNoon should be default (false) but was " + showNoon, !showNoon && showNoon == WidgetSettings.PREF_DEF_GENERAL_SHOWNOON);
    }

    @Test
    public void test_showSecondsPref()
    {
        WidgetSettings.saveShowSecondsPref(context, appWidgetId, false);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        assertTrue("showSeconds should be false but was " + showSeconds, !showSeconds);

        WidgetSettings.saveShowSecondsPref(context, appWidgetId, true);
        showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        assertTrue("showSeconds should be true but was " + showSeconds, showSeconds);

        WidgetSettings.deleteShowSecondsPref(context, appWidgetId);
        showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        assertTrue("showSeconds should be default (false) but was " + showSeconds, !showSeconds && showSeconds == WidgetSettings.PREF_DEF_GENERAL_SHOWSECONDS);
    }

}
