/**
    Copyright (C) 2017-2018 Forrest Guice
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
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4ASimpleSuntimesCalculator;
import com.forrestguice.suntimeswidget.themes.DarkTheme;
import com.forrestguice.suntimeswidget.themes.LightTheme;
import com.forrestguice.suntimeswidget.themes.LightThemeTrans;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

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
        SuntimesCalculatorDescriptor testmode2 = Time4ASimpleSuntimesCalculator.getDescriptor();
        WidgetSettings.saveCalculatorModePref(context, appWidgetId, testmode2);
        SuntimesCalculatorDescriptor pref2 = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        assertTrue("pref should be " + testmode2.name() +  "but was " + pref2.name(), pref2.name().equals(testmode2.name()));

        SuntimesCalculatorDescriptor testmode1 = SunriseSunsetSuntimesCalculator.getDescriptor();
        WidgetSettings.saveCalculatorModePref(context, appWidgetId, testmode1);
        SuntimesCalculatorDescriptor pref1 = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        assertTrue("pref should be " + testmode1.name() +  "but was " + pref1.name(), pref1.name().equals(testmode1.name()));

        WidgetSettings.deleteCalculatorModePref(context, appWidgetId);
        SuntimesCalculatorDescriptor pref0 = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        assertTrue("pref should be default but was " + pref0, pref0.name().equals(WidgetSettings.PREF_DEF_GENERAL_CALCULATOR));
    }

    @Test
    public void test_timeModePref()
    {
        WidgetSettings.saveTimeModePref(context, appWidgetId, WidgetSettings.TimeMode.CIVIL);
        WidgetSettings.TimeMode pref2 = WidgetSettings.loadTimeModePref(context, appWidgetId);
        assertTrue("pref should be CIVIL but was " + pref2, pref2.equals(WidgetSettings.TimeMode.CIVIL));

        WidgetSettings.saveTimeModePref(context, appWidgetId, WidgetSettings.TimeMode.NAUTICAL);
        WidgetSettings.TimeMode pref1 = WidgetSettings.loadTimeModePref(context, appWidgetId);
        assertTrue("pref should be NAUTICAL but was " + pref1, pref1.equals(WidgetSettings.TimeMode.NAUTICAL));

        WidgetSettings.deleteTimeModePref(context, appWidgetId);
        WidgetSettings.TimeMode pref0 = WidgetSettings.loadTimeModePref(context, appWidgetId);
        assertTrue("pref should be default (OFFICIAL) but was " + pref1, pref0.equals(WidgetSettings.PREF_DEF_GENERAL_TIMEMODE) &&  pref0.equals(WidgetSettings.TimeMode.OFFICIAL));
    }

    @Test
    public void test_timeMode2Pref()
    {
        WidgetSettings.saveTimeMode2Pref(context, appWidgetId, WidgetSettings.SolsticeEquinoxMode.SOLSTICE_SUMMER);
        WidgetSettings.SolsticeEquinoxMode pref2 = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
        assertTrue("pref should be SUMMER but was " + pref2, pref2.equals(WidgetSettings.SolsticeEquinoxMode.SOLSTICE_SUMMER));

        WidgetSettings.saveTimeMode2Pref(context, appWidgetId, WidgetSettings.SolsticeEquinoxMode.SOLSTICE_WINTER);
        WidgetSettings.SolsticeEquinoxMode pref1 = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
        assertTrue("pref should be WINTER but was " + pref1, pref1.equals(WidgetSettings.SolsticeEquinoxMode.SOLSTICE_WINTER));

        WidgetSettings.deleteTimeMode2Pref(context, appWidgetId);
        WidgetSettings.SolsticeEquinoxMode pref0 = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
        assertTrue("pref should be default (VERNAL) but was " + pref0, pref0.equals(WidgetSettings.PREF_DEF_GENERAL_TIMEMODE2) && pref0.equals(WidgetSettings.SolsticeEquinoxMode.EQUINOX_VERNAL));
    }

    @Test
    public void test_timeMode3Pref()
    {
        assertTrue("STUB", true == false);  // TODO
    }

    @Test
    public void test_timeMode2OverridePref()
    {
        WidgetSettings.saveTimeMode2OverridePref(context, appWidgetId, true);
        boolean pref2 = WidgetSettings.loadTimeMode2OverridePref(context, appWidgetId);
        assertTrue("pref should be true but was " + pref2, pref2);

        WidgetSettings.saveTimeMode2OverridePref(context, appWidgetId, false);
        boolean pref1 = WidgetSettings.loadTimeMode2OverridePref(context, appWidgetId);
        assertTrue("pref should be false but was " + pref1, !pref1);

        WidgetSettings.deleteTimeMode2OverridePref(context, appWidgetId);
        boolean pref0 = WidgetSettings.loadTimeMode2OverridePref(context, appWidgetId);
        assertTrue("mode should be default (true) but was " + pref0, pref0 && pref0 == WidgetSettings.PREF_DEF_GENERAL_TIMEMODE2_OVERRIDE);
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
        String tzid2 = TESTTZID_0;
        WidgetSettings.saveTimezonePref(context, appWidgetId, tzid2);
        String pref2 = WidgetSettings.loadTimezonePref(context, appWidgetId);
        assertTrue("timezone should be " + tzid2 +  " but was " + pref2, pref2.equals(tzid2));

        String tzid1 = TESTTZID_1;
        WidgetSettings.saveTimezonePref(context, appWidgetId, tzid1);
        String pref1 = WidgetSettings.loadTimezonePref(context, appWidgetId);
        assertTrue("timezone should be " + tzid1 +  " but was " + pref1, pref1.equals(tzid1));

        String tzid0 = WidgetSettings.PREF_DEF_TIMEZONE_CUSTOM;
        WidgetSettings.deleteTimezonePref(context, appWidgetId);
        String pref0 = WidgetSettings.loadTimezonePref(context, appWidgetId);
        assertTrue("timezone should be default (" + tzid0 +  ") but was " + pref0, pref0.equals(tzid0));
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
        WidgetSettings.Location testloc2 = new WidgetSettings.Location(TESTLOC_0_LABEL, TESTLOC_0_LAT, TESTLOC_0_LON);
        WidgetSettings.saveLocationPref(context, appWidgetId, testloc2);
        WidgetSettings.Location pref2 = WidgetSettings.loadLocationPref(context, appWidgetId);
        assertTrue("location does not match! " + pref2, pref2.equals(testloc2));

        WidgetSettings.Location testloc1 = new WidgetSettings.Location(TESTLOC_1_LABEL, TESTLOC_1_LAT, TESTLOC_1_LON, TESTLOC_1_ALT);
        WidgetSettings.saveLocationPref(context, appWidgetId, testloc1);
        WidgetSettings.Location pref1 = WidgetSettings.loadLocationPref(context, appWidgetId);
        assertTrue("location does not match! " + pref1, pref1.equals(testloc1));

        WidgetSettings.Location testloc0 = new WidgetSettings.Location(WidgetSettings.PREF_DEF_LOCATION_LABEL, WidgetSettings.PREF_DEF_LOCATION_LATITUDE, WidgetSettings.PREF_DEF_LOCATION_LONGITUDE, WidgetSettings.PREF_DEF_LOCATION_ALTITUDE);
        WidgetSettings.deleteLocationPref(context, appWidgetId);
        WidgetSettings.Location pref0 = WidgetSettings.loadLocationPref(context, appWidgetId);
        assertTrue("location does not match default! " + pref0, pref0.equals(testloc0));
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
        WidgetSettings.saveThemePref(context, appWidgetId, LightTheme.THEMEDEF_NAME);
        SuntimesTheme pref2 = WidgetSettings.loadThemePref(context, appWidgetId);
        assertTrue("pref should be \"light\" but was " + pref2.themeName(), pref2.themeName().equals(LightTheme.THEMEDEF_NAME));

        WidgetSettings.saveThemePref(context, appWidgetId, LightThemeTrans.THEMEDEF_NAME);
        SuntimesTheme pref1 = WidgetSettings.loadThemePref(context, appWidgetId);
        assertTrue("pref should be \"light_transparent\" but was " + pref1.themeName(), pref1.themeName().equals(LightThemeTrans.THEMEDEF_NAME));

        WidgetSettings.deleteThemePref(context, appWidgetId);
        SuntimesTheme pref0 = WidgetSettings.loadThemePref(context, appWidgetId);
        assertTrue("pref should be default (\"dark\") but was " + pref0.themeName(), pref0.themeName().equals(WidgetSettings.PREF_DEF_APPEARANCE_THEME) && pref0.themeName().equals(DarkTheme.THEMEDEF_NAME));
    }

    @Test
    public void test_showLabelsPref()
    {
        WidgetSettings.saveShowLabelsPref(context, appWidgetId, false);
        boolean pref2 = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        assertTrue("pref should be false but was " + pref2, !pref2);

        WidgetSettings.saveShowLabelsPref(context, appWidgetId, true);
        boolean pref1 = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        assertTrue("pref should be true but was " + pref1, pref1);

        WidgetSettings.deleteShowLabelsPref(context, appWidgetId);
        boolean pref0 = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        assertTrue("mode should be default (false) but was " + pref0, !pref0 && pref0 == WidgetSettings.PREF_DEF_APPEARANCE_SHOWLABELS);
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
        WidgetSettings.saveSun1x1ModePref(context, appWidgetId, WidgetSettings.WidgetModeSun1x1.WIDGETMODE1x1_SUNRISE);
        WidgetSettings.WidgetModeSun1x1 pref2 = WidgetSettings.loadSun1x1ModePref(context, appWidgetId);
        assertTrue("pref should be SUNRISE but was " + pref2, pref2.equals(WidgetSettings.WidgetModeSun1x1.WIDGETMODE1x1_SUNRISE));

        WidgetSettings.saveSun1x1ModePref(context, appWidgetId, WidgetSettings.WidgetModeSun1x1.WIDGETMODE1x1_SUNSET);
        WidgetSettings.WidgetModeSun1x1 pref1 = WidgetSettings.loadSun1x1ModePref(context, appWidgetId);
        assertTrue("pref should be SUNSET but was " + pref1, pref1.equals(WidgetSettings.WidgetModeSun1x1.WIDGETMODE1x1_SUNSET));

        WidgetSettings.deleteSun1x1ModePref(context, appWidgetId);
        WidgetSettings.WidgetModeSun1x1 pref0 = WidgetSettings.loadSun1x1ModePref(context, appWidgetId);
        assertTrue("pref should be default (BOTH_1) but was " + pref0, pref0.equals(WidgetSettings.PREF_DEF_APPEARANCE_WIDGETMODE_SUN1x1) && pref0.equals(WidgetSettings.WidgetModeSun1x1.WIDGETMODE1x1_BOTH_1));
    }

    @Test
    public void test_1x1SunPosModePref()
    {
        WidgetSettings.saveSunPos1x1ModePref(context, appWidgetId, WidgetSettings.WidgetModeSunPos1x1.MODE1x1_ALTAZ);
        WidgetSettings.WidgetModeSunPos1x1 pref2 = WidgetSettings.loadSunPos1x1ModePref(context, appWidgetId);
        assertTrue("pref should be ALTAZ but was " + pref2, pref2.equals(WidgetSettings.WidgetModeSunPos1x1.MODE1x1_ALTAZ));

        WidgetSettings.saveSunPos1x1ModePref(context, appWidgetId, WidgetSettings.WidgetModeSunPos1x1.MODE1x1_DECRIGHT);
        WidgetSettings.WidgetModeSunPos1x1 pref1 = WidgetSettings.loadSunPos1x1ModePref(context, appWidgetId);
        assertTrue("pref should be DECRIGHT but was " + pref1, pref1.equals(WidgetSettings.WidgetModeSunPos1x1.MODE1x1_DECRIGHT));

        WidgetSettings.deleteSunPos1x1ModePref(context, appWidgetId);
        WidgetSettings.WidgetModeSunPos1x1 pref0 = WidgetSettings.loadSunPos1x1ModePref(context, appWidgetId);
        assertTrue("pref should be default (ALTAZ) but was " + pref0, pref0.equals(WidgetSettings.PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS1x1) && pref0.equals(WidgetSettings.WidgetModeSunPos1x1.MODE1x1_ALTAZ));
    }

    @Test
    public void test_1x1MoonModePref()
    {
        WidgetSettings.saveMoon1x1ModePref(context, appWidgetId, WidgetSettings.WidgetModeMoon1x1.MODE1x1_RISESET);
        WidgetSettings.WidgetModeMoon1x1 pref1 = WidgetSettings.loadMoon1x1ModePref(context, appWidgetId);
        assertTrue("pref should be RISESET but was " + pref1, pref1.equals(WidgetSettings.WidgetModeMoon1x1.MODE1x1_RISESET));

        WidgetSettings.saveMoon1x1ModePref(context, appWidgetId, WidgetSettings.WidgetModeMoon1x1.MODE1x1_PHASE);
        WidgetSettings.WidgetModeMoon1x1 pref2 = WidgetSettings.loadMoon1x1ModePref(context, appWidgetId);
        assertTrue("pref should be PHASE but was " + pref2, pref2.equals(WidgetSettings.WidgetModeMoon1x1.MODE1x1_PHASE));

        WidgetSettings.deleteMoon1x1ModePref(context, appWidgetId);
        WidgetSettings.WidgetModeMoon1x1 pref0 = WidgetSettings.loadMoon1x1ModePref(context, appWidgetId);
        assertTrue("pref should be default (RISESET) but was " + pref0, pref0.equals(WidgetSettings.PREF_DEF_APPEARANCE_WIDGETMODE_MOON1x1) && pref0.equals(WidgetSettings.WidgetModeMoon1x1.MODE1x1_RISESET));
    }

    @Test
    public void test_trackingModePref()
    {
        WidgetSettings.saveTrackingModePref(context, appWidgetId, WidgetSettings.TrackingMode.SOONEST);
        WidgetSettings.TrackingMode mode1 = WidgetSettings.loadTrackingModePref(context, appWidgetId);
        assertTrue("mode should be SOONEST but was " + mode1, mode1 == WidgetSettings.TrackingMode.SOONEST);

        WidgetSettings.saveTrackingModePref(context, appWidgetId, WidgetSettings.TrackingMode.CLOSEST);
        WidgetSettings.TrackingMode mode2 = WidgetSettings.loadTrackingModePref(context, appWidgetId);
        assertTrue("mode should be CLOSEST but was " + mode2, mode2 == WidgetSettings.TrackingMode.CLOSEST);

        WidgetSettings.deleteTrackingModePref(context, appWidgetId);
        WidgetSettings.TrackingMode mode0 = WidgetSettings.loadTrackingModePref(context, appWidgetId);
        assertTrue("mode should be default (SOONEST) but was " + mode0, mode0 == WidgetSettings.TrackingMode.SOONEST && mode0 == WidgetSettings.PREF_DEF_GENERAL_TRACKINGMODE);
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
    public void test_showWeeksPref()
    {
        WidgetSettings.saveShowWeeksPref(context, appWidgetId, false);
        boolean showWeeks = WidgetSettings.loadShowWeeksPref(context, appWidgetId);
        assertTrue("showWeeks should be false but was " + showWeeks, !showWeeks);

        WidgetSettings.saveShowWeeksPref(context, appWidgetId, true);
        showWeeks = WidgetSettings.loadShowWeeksPref(context, appWidgetId);
        assertTrue("showWeeks should be true but was " + showWeeks, showWeeks);

        WidgetSettings.deleteShowWeeksPref(context, appWidgetId);
        showWeeks = WidgetSettings.loadShowWeeksPref(context, appWidgetId);
        assertTrue("showWeeks should be default (false) but was " + showWeeks, !showWeeks && showWeeks == WidgetSettings.PREF_DEF_GENERAL_SHOWWEEKS);
    }

    @Test
    public void test_showHoursPref()
    {
        WidgetSettings.saveShowHoursPref(context, appWidgetId, true);
        boolean showHours = WidgetSettings.loadShowHoursPref(context, appWidgetId);
        assertTrue("showHours should be true but was " + showHours, showHours);

        WidgetSettings.saveShowHoursPref(context, appWidgetId, false);
        showHours = WidgetSettings.loadShowHoursPref(context, appWidgetId);
        assertTrue("showHours should be false but was " + showHours, !showHours);

        WidgetSettings.deleteShowHoursPref(context, appWidgetId);
        showHours = WidgetSettings.loadShowHoursPref(context, appWidgetId);
        assertTrue("showHours should be default (true) but was " + showHours, showHours && showHours == WidgetSettings.PREF_DEF_GENERAL_SHOWHOURS);
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

    @Test
    public void test_observerHeightPref()
    {
        WidgetSettings.saveObserverHeightPref(context, appWidgetId, 1f);
        float height0 = WidgetSettings.loadObserverHeightPref(context, appWidgetId);
        assertTrue("height should be 1 but was " + height0, equals(height0, 1f));

        WidgetSettings.saveObserverHeightPref(context, appWidgetId, 2.5f);
        float height1 = WidgetSettings.loadObserverHeightPref(context, appWidgetId);
        assertTrue("height should be 2.5 but was " + height1, equals(height1, 2.5f));

        WidgetSettings.deleteObserverHeightPref(context, appWidgetId);
        float height2 = WidgetSettings.loadObserverHeightPref(context, appWidgetId);
        assertTrue("height should be default (1.8288) but was " + height2, equals(height2, 1.8288f) && equals(height2, WidgetSettings.PREF_DEF_GENERAL_OBSERVERHEIGHT));
    }

    @Test
    public void test_showTimeDatePref()
    {
        WidgetSettings.saveShowTimeDatePref(context, appWidgetId, true);
        boolean showTimeDate = WidgetSettings.loadShowHoursPref(context, appWidgetId);
        assertTrue("showTimeDate should be true but was " + showTimeDate, showTimeDate);

        WidgetSettings.saveShowTimeDatePref(context, appWidgetId, false);
        showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        assertTrue("showTimeDate should be false but was " + showTimeDate, !showTimeDate);

        WidgetSettings.deleteShowTimeDatePref(context, appWidgetId);
        showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        assertTrue("showTimeDate should be default (true) but was " + showTimeDate, showTimeDate && showTimeDate == WidgetSettings.PREF_DEF_GENERAL_SHOWTIMEDATE);
    }

    public static final float FLOAT_TOLERANCE = 0.01f;
    protected boolean equals(float float1, float float2)
    {
        return (Math.abs(float1 - float2) < FLOAT_TOLERANCE);
    }

}
