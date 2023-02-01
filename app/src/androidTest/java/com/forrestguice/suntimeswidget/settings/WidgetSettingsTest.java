/**
    Copyright (C) 2017-2022 Forrest Guice
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4ASimpleSuntimesCalculator;
import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.suntimeswidget.calendar.CalendarSettings;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.themes.defaults.DarkTheme;
import com.forrestguice.suntimeswidget.themes.defaults.LightTheme;
import com.forrestguice.suntimeswidget.themes.defaults.LightThemeTrans;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
@RunWith(AndroidJUnit4.class)
public class WidgetSettingsTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Test
    public void test_nextUpdate()
    {
        Context context = activityRule.getActivity();
        int appWidgetId = Integer.MAX_VALUE;

        WidgetSettings.saveNextSuggestedUpdate(context, appWidgetId, 10);
        long value1 = WidgetSettings.getNextSuggestedUpdate(context, appWidgetId);
        assertTrue("value should be 10", value1 == 10);

        WidgetSettings.deleteNextSuggestedUpdate(context, appWidgetId);
        long value0 = WidgetSettings.getNextSuggestedUpdate(context, appWidgetId);
        assertTrue("value should be -1", value0 == -1 && value0 == WidgetSettings.PREF_DEF_NEXTUPDATE);
    }

    @Test
    public void test_lengthUnitsPref()
    {
        Context context = activityRule.getActivity();
        int appWidgetId = 0;

        WidgetSettings.saveLengthUnitsPref(context, appWidgetId, WidgetSettings.LengthUnit.METRIC);
        WidgetSettings.LengthUnit units3 = WidgetSettings.loadLengthUnitsPref(context, appWidgetId);
        assertTrue("units should be metric, was " + units3, units3 == WidgetSettings.LengthUnit.METRIC);

        WidgetSettings.saveLengthUnitsPref(context, appWidgetId, WidgetSettings.LengthUnit.IMPERIAL);
        WidgetSettings.LengthUnit units2 = WidgetSettings.loadLengthUnitsPref(context, appWidgetId);
        assertTrue("units should be imperial, was " + units2, units2 == WidgetSettings.LengthUnit.IMPERIAL);

        WidgetSettings.deleteLengthUnitsPref(context, appWidgetId);
        WidgetSettings.LengthUnit units0 = WidgetSettings.loadLengthUnitsPref(context, appWidgetId);
        assertTrue("units should be default (" + WidgetSettings.PREF_DEF_GENERAL_UNITS_LENGTH + ") but was " + units0, units0 == WidgetSettings.PREF_DEF_GENERAL_UNITS_LENGTH);

        double meters0 = Math.PI;
        double feet0 = WidgetSettings.LengthUnit.metersToFeet(meters0);
        double meters1 = WidgetSettings.LengthUnit.feetToMeters(feet0);
        assertTrue("conversion should make round trip", (meters1-meters0 < 0.1));

        WidgetSettings.saveLengthUnitsPref(context, 0, WidgetSettings.LengthUnit.METRIC);
        WidgetSettings.saveLengthUnitsPref(context, Integer.MAX_VALUE, WidgetSettings.LengthUnit.IMPERIAL);
        assertEquals(WidgetSettings.LengthUnit.IMPERIAL, WidgetSettings.loadLengthUnitsPref(context, Integer.MAX_VALUE));

        WidgetSettings.deleteLengthUnitsPref(context, Integer.MAX_VALUE);
        assertEquals(WidgetSettings.loadLengthUnitsPref(context, 0), WidgetSettings.loadLengthUnitsPref(context, Integer.MAX_VALUE));
        WidgetSettings.saveLengthUnitsPref(context, 0, WidgetSettings.LengthUnit.IMPERIAL);
        assertEquals(WidgetSettings.loadLengthUnitsPref(context, 0), WidgetSettings.loadLengthUnitsPref(context, Integer.MAX_VALUE));
    }

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
        WidgetSettings.initDisplayStrings(context);
    }

    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void test_calculatorModePref()
    {
        SuntimesCalculatorDescriptor testmode2 = Time4ASimpleSuntimesCalculator.getDescriptor();
        WidgetSettings.saveCalculatorModePref(context, appWidgetId, testmode2);
        SuntimesCalculatorDescriptor pref2 = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        assertTrue("pref should be " + testmode2.getName() +  "but was " + pref2.getName(), pref2.getName().equals(testmode2.getName()));

        SuntimesCalculatorDescriptor testmode1 = SunriseSunsetSuntimesCalculator.getDescriptor();
        WidgetSettings.saveCalculatorModePref(context, appWidgetId, testmode1);
        SuntimesCalculatorDescriptor pref1 = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        assertTrue("pref should be " + testmode1.getName() +  "but was " + pref1.getName(), pref1.getName().equals(testmode1.getName()));

        WidgetSettings.deleteCalculatorModePref(context, appWidgetId);
        SuntimesCalculatorDescriptor pref0 = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        assertTrue("pref should be default but was " + pref0, pref0.getName().equals(WidgetSettings.PREF_DEF_GENERAL_CALCULATOR));
    }

    @Test
    public void test_timeModePref()
    {
        WidgetSettings.saveTimeModePref(context, appWidgetId, WidgetSettings.TimeMode.CIVIL);
        WidgetSettings.RiseSetDataMode pref2 = WidgetSettings.loadTimeModePref(context, appWidgetId);
        assertTrue("pref should be CIVIL but was " + pref2, pref2.equals(WidgetSettings.TimeMode.CIVIL));

        WidgetSettings.saveTimeModePref(context, appWidgetId, WidgetSettings.TimeMode.NAUTICAL);
        WidgetSettings.RiseSetDataMode pref1 = WidgetSettings.loadTimeModePref(context, appWidgetId);
        assertTrue("pref should be NAUTICAL but was " + pref1, pref1.equals(WidgetSettings.TimeMode.NAUTICAL));

        WidgetSettings.deleteTimeModePref(context, appWidgetId);
        WidgetSettings.RiseSetDataMode pref0 = WidgetSettings.loadTimeModePref(context, appWidgetId);
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
        assertTrue("pref should be default (SPRING) but was " + pref0, pref0.equals(WidgetSettings.PREF_DEF_GENERAL_TIMEMODE2) && pref0.equals(WidgetSettings.SolsticeEquinoxMode.EQUINOX_SPRING));
    }

    @Test
    public void test_timeMode3Pref()
    {
        WidgetSettings.saveTimeMode3Pref(context, appWidgetId, WidgetSettings.MoonPhaseMode.FULL_MOON);
        WidgetSettings.MoonPhaseMode pref4 = WidgetSettings.loadTimeMode3Pref(context, appWidgetId);
        assertTrue("pref should be FULL_MOON but was " + pref4, pref4.equals(WidgetSettings.MoonPhaseMode.FULL_MOON));

        WidgetSettings.saveTimeMode3Pref(context, appWidgetId, WidgetSettings.MoonPhaseMode.NEW_MOON);
        WidgetSettings.MoonPhaseMode pref3 = WidgetSettings.loadTimeMode3Pref(context, appWidgetId);
        assertTrue("pref should be NEW_MOON but was " + pref3, pref3.equals(WidgetSettings.MoonPhaseMode.NEW_MOON));

        WidgetSettings.saveTimeMode3Pref(context, appWidgetId, WidgetSettings.MoonPhaseMode.FIRST_QUARTER);
        WidgetSettings.MoonPhaseMode pref2 = WidgetSettings.loadTimeMode3Pref(context, appWidgetId);
        assertTrue("pref should be FIRST_QUARTER but was " + pref2, pref2.equals(WidgetSettings.MoonPhaseMode.FIRST_QUARTER));

        WidgetSettings.saveTimeMode3Pref(context, appWidgetId, WidgetSettings.MoonPhaseMode.THIRD_QUARTER);
        WidgetSettings.MoonPhaseMode pref1 = WidgetSettings.loadTimeMode3Pref(context, appWidgetId);
        assertTrue("pref should be THIRD_QUARTER but was " + pref1, pref1.equals(WidgetSettings.MoonPhaseMode.THIRD_QUARTER));

        WidgetSettings.deleteTimeMode3Pref(context, appWidgetId);
        WidgetSettings.MoonPhaseMode pref0 = WidgetSettings.loadTimeMode3Pref(context, appWidgetId);
        assertTrue("pref should be default (FULL_MOON) but was " + pref0, pref0.equals(WidgetSettings.PREF_DEF_GENERAL_TIMEMODE3) && pref0.equals(WidgetSettings.MoonPhaseMode.FULL_MOON));
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
    public void test_calendarShowDate()
    {
        CalendarSettings.saveCalendarFlag(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE, false);
        boolean value2 = CalendarSettings.loadCalendarFlag(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE, CalendarSettings.PREF_DEF_CALENDAR_SHOWDATE);
        assertFalse("flag should be false but was " + value2, value2);

        CalendarSettings.saveCalendarFlag(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE, true);
        boolean value1 = CalendarSettings.loadCalendarFlag(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE, CalendarSettings.PREF_DEF_CALENDAR_SHOWDATE);
        assertTrue("flag should be true but was " + value1, value1);

        CalendarSettings.deleteCalendarPref(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE);
        boolean value0 = CalendarSettings.loadCalendarFlag(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_SHOWDATE, CalendarSettings.PREF_DEF_CALENDAR_SHOWDATE);
        assertTrue("flag should be " + CalendarSettings.PREF_DEF_CALENDAR_SHOWDATE + " but was " + value0, value0 == CalendarSettings.PREF_DEF_CALENDAR_SHOWDATE);
    }

    @Test
    public void test_calendarModePref()
    {
        CalendarSettings.saveCalendarModePref(context, appWidgetId, CalendarMode.GREGORIAN);
        CalendarMode mode2 = CalendarSettings.loadCalendarModePref(context, appWidgetId);
        assertTrue("mode should be GREGORIAN but was " + mode2, mode2 == CalendarMode.GREGORIAN);

        CalendarSettings.saveCalendarModePref(context, appWidgetId, CalendarMode.PERSIAN);
        CalendarMode mode1 = CalendarSettings.loadCalendarModePref(context, appWidgetId);
        assertTrue("mode should be PERSIAN but was " + mode1, mode1 == CalendarMode.PERSIAN);

        CalendarSettings.deleteCalendarPref(context, appWidgetId, CalendarSettings.PREF_KEY_CALENDAR_MODE);
        CalendarMode mode0 = CalendarSettings.loadCalendarModePref(context, appWidgetId);
        assertTrue("mode should be default (GREGORIAN but was " + mode0, mode0 == CalendarSettings.PREF_DEF_CALENDAR_MODE && mode0 == CalendarMode.GREGORIAN);
    }

    @Test
    public void test_calendarFormatPref()
    {
        String tag = "TEST";
        CalendarSettings.saveCalendarFormatPatternPref(context, appWidgetId, tag, "YYYY");
        String format2 = CalendarSettings.loadCalendarFormatPatternPref(context, appWidgetId, tag);
        assertTrue("mode should be YYYY but was " + format2, "YYYY".equals(format2));

        CalendarSettings.deleteCalendarFormatPatternPref(context, appWidgetId, tag);
        String format0 = CalendarSettings.loadCalendarFormatPatternPref(context, appWidgetId, tag);
        assertTrue("mode should be default but was " + format0, CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_GREGORIAN.equals(format0));
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

    @Test
    public void test_dateOffsetPref()
    {
        WidgetSettings.saveDateOffsetPref(context, appWidgetId, 1);
        assertEquals(1, WidgetSettings.loadDateOffsetPref(context, appWidgetId));

        WidgetSettings.saveDateOffsetPref(context, appWidgetId, 20);
        assertEquals(20, WidgetSettings.loadDateOffsetPref(context, appWidgetId));

        WidgetSettings.deleteDateOffsetPref(context, appWidgetId);
        assertEquals(WidgetSettings.PREF_DEF_DATE_OFFSET, WidgetSettings.loadDateOffsetPref(context, appWidgetId));
        assertEquals(WidgetSettings.PREF_DEF_DATE_OFFSET, 0);
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
        assertTrue("mode should be COLOR but was " + mode1, mode1 == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);

        WidgetSettings.deleteTimezoneModePref(context, appWidgetId);
        WidgetSettings.TimezoneMode mode0 = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        assertTrue("mode should be default (CURRENT) but was " + mode0, mode0 == WidgetSettings.PREF_DEF_TIMEZONE_MODE && mode0 == WidgetSettings.TimezoneMode.CURRENT_TIMEZONE);
    }

    @Test
    public void test_timezonePref()
    {
        String tzid3 = TESTTZID_0;
        WidgetSettings.saveTimezonePref(context, appWidgetId, tzid3, "test");
        String pref3 = WidgetSettings.loadTimezonePref(context, appWidgetId, "test");
        assertTrue("timezone should be " + tzid3 +  " but was " + pref3, pref3.equals(tzid3));

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
        assertTrue("mode should be COLOR but was " + mode2, mode2 == WidgetSettings.LocationMode.CUSTOM_LOCATION);

        WidgetSettings.saveLocationModePref(context, appWidgetId, WidgetSettings.LocationMode.CURRENT_LOCATION);
        WidgetSettings.LocationMode mode1 = WidgetSettings.loadLocationModePref(context, appWidgetId);
        assertTrue("mode should be CURRENT but was " + mode1, mode1 == WidgetSettings.LocationMode.CURRENT_LOCATION);

        WidgetSettings.deleteLocationModePref(context, appWidgetId);
        WidgetSettings.LocationMode mode0 = WidgetSettings.loadLocationModePref(context, appWidgetId);
        assertTrue("mode should be default (COLOR) but was " + mode0, mode0 == WidgetSettings.PREF_DEF_LOCATION_MODE && mode0 == WidgetSettings.LocationMode.CUSTOM_LOCATION);
    }

    @Test public void test_locationPref()
    {
        Location testloc2 = new Location(TESTLOC_0_LABEL, TESTLOC_0_LAT, TESTLOC_0_LON);
        WidgetSettings.saveLocationPref(context, appWidgetId, testloc2);
        Location pref2 = WidgetSettings.loadLocationPref(context, appWidgetId);
        assertTrue("location does not match! " + pref2.getUri() + " != " + testloc2.getUri(), pref2.equals(testloc2));

        Location testloc1 = new Location(TESTLOC_1_LABEL, TESTLOC_1_LAT, TESTLOC_1_LON, TESTLOC_1_ALT);
        WidgetSettings.saveLocationPref(context, appWidgetId, testloc1);
        Location pref1 = WidgetSettings.loadLocationPref(context, appWidgetId);
        assertTrue("location does not match! " + pref1.getUri() + " != " + testloc1.getUri(), pref1.equals(testloc1));

        //Location testloc0 = new Location(WidgetSettings.PREF_DEF_LOCATION_LABEL, WidgetSettings.PREF_DEF_LOCATION_LATITUDE, WidgetSettings.PREF_DEF_LOCATION_LONGITUDE, WidgetSettings.PREF_DEF_LOCATION_ALTITUDE);
        Location testloc0 = WidgetSettings.loadLocationPref(context, 0);
        WidgetSettings.deleteLocationPref(context, appWidgetId);
        Location pref0 = WidgetSettings.loadLocationPref(context, appWidgetId);
        assertTrue("location does not match default! " + pref0.getUri() + " != " + testloc0.getUri(), pref0.equals(testloc0));
    }

    @Test
    public void test_locationAltitudeEnabledPref()
    {
	    WidgetSettings.saveLocationAltitudeEnabledPref(context, appWidgetId, true);
	    boolean isEnabled0 = WidgetSettings.loadLocationAltitudeEnabledPref(context, appWidgetId);
	    assertTrue("value does not match! " + isEnabled0, isEnabled0);

	    WidgetSettings.saveLocationAltitudeEnabledPref(context, appWidgetId, false);
	    boolean isEnabled1 = WidgetSettings.loadLocationAltitudeEnabledPref(context, appWidgetId);
	    assertTrue("value does not match! " + isEnabled1, !isEnabled1);

	    WidgetSettings.deleteLocationAltitudeEnabledPref(context, appWidgetId);
	    boolean isEnabled2 = WidgetSettings.loadLocationAltitudeEnabledPref(context, appWidgetId);
	    assertTrue("value does not match! " + isEnabled2, isEnabled2 == WidgetSettings.PREF_DEF_LOCATION_ALTITUDE_ENABLED);
    }


    @Test
    public void test_locationFromAppPref()
    {
        WidgetSettings.saveLocationFromAppPref(context, appWidgetId, true);
        boolean isEnabled0 = WidgetSettings.loadLocationFromAppPref(context, appWidgetId);
        assertTrue("value does not match! " + isEnabled0, isEnabled0);

        WidgetSettings.saveLocationFromAppPref(context, appWidgetId, false);
        boolean isEnabled1 = WidgetSettings.loadLocationFromAppPref(context, appWidgetId);
        assertTrue("value does not match! " + isEnabled1, !isEnabled1);

        WidgetSettings.deleteLocationFromAppPref(context, appWidgetId);
        boolean isEnabled2 = WidgetSettings.loadLocationFromAppPref(context, appWidgetId);
        assertTrue("value does not match! " + isEnabled2, isEnabled2 == WidgetSettings.PREF_DEF_LOCATION_FROMAPP);
    }

    @Test
    public void test_tzFromAppPref()
    {
        WidgetSettings.saveTimeZoneFromAppPref(context, appWidgetId, true);
        boolean isEnabled0 = WidgetSettings.loadTimeZoneFromAppPref(context, appWidgetId);
        assertTrue("value does not match! " + isEnabled0, isEnabled0);

        WidgetSettings.saveTimeZoneFromAppPref(context, appWidgetId, false);
        boolean isEnabled1 = WidgetSettings.loadTimeZoneFromAppPref(context, appWidgetId);
        assertFalse("value does not match! " + isEnabled1, isEnabled1);

        WidgetSettings.deleteTimeZoneFromAppPref(context, appWidgetId);
        boolean isEnabled2 = WidgetSettings.loadTimeZoneFromAppPref(context, appWidgetId);
        assertEquals("value does not match! " + isEnabled2, isEnabled2, WidgetSettings.PREF_DEF_TIMEZONE_FROMAPP);
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
        String title2 = "title2";
        String desc2 = "desc2";
        int color2 = Color.BLUE;
        String value2 = "com.forrestguice.suntimeswidget.SuntimesActivity";
        String package2 = "com.forrestguice.suntimeswidget";
        String action2 = "ACTION2";
        String data2 = "DATA2";
        String mime2 = "text/plain";
        String extras2 = "EXTRAS2";
        WidgetActions.saveActionLaunchPref(context, title2, desc2, color2, null, appWidgetId, null, value2, package2, WidgetActions.LaunchType.ACTIVITY.name(), action2, data2, mime2, extras2);
        String pref2_value = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, null);
        String pref2_package = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_PACKAGE);
        String pref2_action = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_ACTION);
        String pref2_data = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATA);
        String pref2_mime = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATATYPE);
        String pref2_extra = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_EXTRAS);
        String pref2_title = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
        String pref2_desc = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC);
        int pref2_color = Integer.parseInt(WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_COLOR));
        assertTrue("pref value should be " + value2 + " but was " + pref2_value, pref2_value.equals(value2));
        assertTrue("pref package should be " + value2 + " but was " + pref2_package, pref2_package.equals(package2));
        assertTrue("pref action should be " + action2 + " but was " + pref2_action, pref2_action.equals(action2));
        assertTrue("pref data should be " + data2 + " but was " + pref2_data, pref2_data.equals(data2));
        assertTrue("pref datatype should be " + mime2 + " but was " + pref2_mime, pref2_mime.equals(mime2));
        assertTrue("pref extras should be " + extras2 + " but was " + pref2_extra, pref2_extra.equals(extras2));
        assertTrue("pref title should be " + title2 + " but was " + pref2_title, pref2_title.equals(title2));
        assertTrue("pref desc should be " + desc2 + " but was " + pref2_desc, pref2_desc.equals(desc2));
        assertTrue("pref color should be " + color2 + " but was " + pref2_color, pref2_color == color2);

        String title1 = "title2";
        String desc1 = "desc1";
        int color1 = Color.YELLOW;
        String value1 = "test value 1";
        String package1 = "some.package.name";
        String action1 = "ACTION1";
        String data1 = "DATA1";
        String mime1 = "text/html";
        String extras1 = "EXTRAS1";
        WidgetActions.saveActionLaunchPref(context, title1, desc1, color1, null, appWidgetId, null, value1, package1, WidgetActions.LaunchType.ACTIVITY.name(), action1, data1, mime1, extras1);
        String pref1_value = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, null);
        String pref1_package = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_PACKAGE);
        String pref1_action = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_ACTION);
        String pref1_data = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATA);
        String pref1_mime = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATATYPE);
        String pref1_extra = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_EXTRAS);
        String pref1_title = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
        String pref1_desc = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC);
        int pref1_color = Integer.parseInt(WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_COLOR));
        assertTrue("pref value should be " + value1 + " but was " + pref1_value, pref1_value.equals(value1));
        assertTrue("pref package should be " + package1 + " but was " + pref1_package, pref1_package.equals(package1));
        assertTrue("pref action should be " + action1 + " but was " + pref1_action, pref1_action.equals(action1));
        assertTrue("pref data should be " + data1 + " but was " + pref1_data, pref1_data.equals(data1));
        assertTrue("pref datatype should be " + mime1 + " but was " + pref1_mime, pref1_mime.equals(mime1));
        assertTrue("pref extras should be " + extras1 + " but was " + pref1_extra, pref1_extra.equals(extras1));
        assertTrue("pref title should be " + title1 + " but was " + pref1_title, pref1_title.equals(title1));
        assertTrue("pref desc should be " + desc1 + " but was " + pref1_desc, pref1_desc.equals(desc1));
        assertTrue("pref color should be " + color1 + " but was " + pref1_color, pref1_color == color1);

        String value0 = "com.forrestguice.suntimeswidget.SuntimesActivity";
        WidgetActions.deleteActionLaunchPref(context, appWidgetId, null);
        String pref0_value = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, null);
        String pref0_package = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_PACKAGE);
        String pref0_action = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_ACTION);
        String pref0_data = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATA);
        String pref0_mime = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATATYPE);
        String pref0_extra = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_EXTRAS);
        String pref0_title = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
        String pref0_desc = WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC);
        int pref0_color = Integer.parseInt(WidgetActions.loadActionLaunchPref(context, appWidgetId, null, WidgetActions.PREF_KEY_ACTION_LAUNCH_COLOR));
        assertTrue("pref value should be default (" + value0 + ") but was " + pref0_value, pref0_value.equals(value0) && value0.equals(WidgetActions.PREF_DEF_ACTION_LAUNCH));
        assertTrue("pref package should be default (empty) but was " + pref0_package, pref0_package.equals(""));
        assertTrue("pref action should be default (empty) but was " + pref0_action, pref0_action.equals(""));
        assertTrue("pref data should be default (empty) but was " + pref0_data, pref0_data.equals(""));
        assertTrue("pref datatype should be default (empty) but was " + pref0_mime, pref0_mime.equals(""));
        assertTrue("pref extras should be default (empty) but was " + pref0_extra, pref0_extra.equals(""));
        assertTrue("pref title should be default", pref0_title.equals(WidgetActions.PREF_DEF_ACTION_LAUNCH_TITLE));
        assertTrue("pref desc should be default", pref0_desc.equals(WidgetActions.PREF_DEF_ACTION_LAUNCH_DESC));
        assertTrue("pref color should be default", pref0_color == WidgetActions.PREF_DEF_ACTION_LAUNCH_COLOR);
    }

    @Test
    public void test_actionApplyExtras()
    {
        Intent intent0 = new Intent();
        String extraString0 = "one=1&two=2L&three=3d&four=4f&string=string&bool=true&L=L";
        WidgetActions.applyExtras(context, intent0, extraString0, null);
        assertEquals(1, intent0.getIntExtra("one", -1));
        assertEquals(2L, intent0.getLongExtra("two", -1L));
        assertEquals(3d, intent0.getDoubleExtra("three", -1d));
        assertEquals(4f, intent0.getFloatExtra("four", -1f));
        assertEquals("string", intent0.getStringExtra("string"));
        assertEquals("L", intent0.getStringExtra("L"));
        assertTrue("bool", intent0.getBooleanExtra("bool", false));
    }

    @Test
    public void test_StringSetPref()
    {
        Context context = activityRule.getActivity();
        SharedPreferences prefs = context.getSharedPreferences(WidgetActions.PREFS_ACTIONS, 0);

        Set<String> values0 = new TreeSet<>();
        for (int i=0; i<10; i++) {
            values0.add(Integer.toString(i));
        }

        WidgetActions.putStringSet(prefs.edit(), "TEST", values0);
        Set<String> values1 = WidgetActions.getStringSet(prefs, "TEST", null);
        for (String v : values0) {
            assertTrue(values1.contains(v));
        }
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
        assertTrue("mode should be default (false) but was " + pref0, pref0 && pref0 == WidgetSettings.PREF_DEF_APPEARANCE_SHOWLABELS);
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
        assertTrue("mode should be default (true) but was " + pref0, !pref0 && pref0 == WidgetSettings.PREF_DEF_APPEARANCE_ALLOWRESIZE);
    }

    @Test
    public void test_scaleTextPref()
    {
        WidgetSettings.saveScaleTextPref(context, appWidgetId, false);
        boolean pref1 = WidgetSettings.loadScaleTextPref(context, appWidgetId);
        assertFalse("pref should be false but was " + pref1, pref1);

        WidgetSettings.saveScaleTextPref(context, appWidgetId, true);
        boolean pref2 = WidgetSettings.loadScaleTextPref(context, appWidgetId);
        assertTrue("pref should be true but was " + pref2, pref2);

        WidgetSettings.deleteScaleTextPref(context, appWidgetId);
        boolean pref0 = WidgetSettings.loadScaleTextPref(context, appWidgetId);
        assertFalse("mode should be default (false) but was " + pref0, pref0 && pref0 == WidgetSettings.PREF_DEF_APPEARANCE_SCALETEXT);
    }

    @Test
    public void test_scaleBasePref()
    {
        WidgetSettings.saveScaleBasePref(context, appWidgetId, false);
        boolean pref1 = WidgetSettings.loadScaleBasePref(context, appWidgetId);
        assertFalse("pref should be false but was " + pref1, pref1);

        WidgetSettings.saveScaleBasePref(context, appWidgetId, true);
        boolean pref2 = WidgetSettings.loadScaleBasePref(context, appWidgetId);
        assertTrue("pref should be true but was " + pref2, pref2);

        WidgetSettings.deleteScaleBasePref(context, appWidgetId);
        boolean pref0 = WidgetSettings.loadScaleBasePref(context, appWidgetId);
        assertFalse("mode should be default (false) but was " + pref0, pref0 && pref0 == WidgetSettings.PREF_DEF_APPEARANCE_SCALEBASE);
    }

    @Test
    public void test_widgetGravityPref()
    {
        WidgetSettings.saveWidgetGravityPref(context, appWidgetId, WidgetSettings.WidgetGravity.TOP.getPosition());
        int pref2 = WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        assertEquals(WidgetSettings.WidgetGravity.TOP.getPosition(), pref2);

        WidgetSettings.WidgetGravity[] values = WidgetSettings.WidgetGravity.values();
        for (int i=0; i<values.length; i++)
        {
            WidgetSettings.saveWidgetGravityPref(context, appWidgetId, values[i].getPosition());
            assertEquals(values[i].getPosition(), WidgetSettings.loadWidgetGravityPref(context, appWidgetId));
        }

        WidgetSettings.deleteWidgetGravityPref(context, appWidgetId);
        int pref0 = WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        assertEquals(WidgetSettings.PREF_DEF_APPEARANCE_GRAVITY.getPosition(), pref0);
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
    public void test_3x1SunPosModePref()
    {
        WidgetSettings.saveSunPos3x1ModePref(context, appWidgetId, WidgetSettings.WidgetModeSunPos3x1.MODE3x1_LIGHTMAP);
        WidgetSettings.WidgetModeSunPos3x1 pref2 = WidgetSettings.loadSunPos3x1ModePref(context, appWidgetId);
        assertTrue("pref should be LIGHTMAP but was " + pref2, pref2.equals(WidgetSettings.WidgetModeSunPos3x1.MODE3x1_LIGHTMAP));

        WidgetSettings.saveSunPos3x1ModePref(context, appWidgetId, WidgetSettings.WidgetModeSunPos3x1.MODE3x1_LIGHTMAP_SMALL);
        WidgetSettings.WidgetModeSunPos3x1 pref1 = WidgetSettings.loadSunPos3x1ModePref(context, appWidgetId);
        assertTrue("pref should be LIGHTMAP1 but was " + pref1, pref1.equals(WidgetSettings.WidgetModeSunPos3x1.MODE3x1_LIGHTMAP_SMALL));

        WidgetSettings.deleteSunPos3x1ModePref(context, appWidgetId);
        WidgetSettings.WidgetModeSunPos3x1 pref0 = WidgetSettings.loadSunPos3x1ModePref(context, appWidgetId);
        assertTrue("pref should be default (LIGHTMAP) but was " + pref0, pref0.equals(WidgetSettings.PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x1) && pref0.equals(WidgetSettings.WidgetModeSunPos3x1.MODE3x1_LIGHTMAP));
    }

    @Test
    public void test_3x2SunPosModePref()
    {
        WidgetSettings.saveSunPos3x2ModePref(context, appWidgetId, WidgetSettings.WidgetModeSunPos3x2.MODE3x2_LINEGRAPH);
        WidgetSettings.WidgetModeSunPos3x2 pref2 = WidgetSettings.loadSunPos3x2ModePref(context, appWidgetId);
        assertTrue("pref should be LINEGRAPH but was " + pref2, pref2.equals(WidgetSettings.WidgetModeSunPos3x2.MODE3x2_LINEGRAPH));

        WidgetSettings.deleteSunPos3x2ModePref(context, appWidgetId);
        WidgetSettings.WidgetModeSunPos3x2 pref0 = WidgetSettings.loadSunPos3x2ModePref(context, appWidgetId);
        assertTrue("pref should be default (WORLDMAP) but was " + pref0, pref0.equals(WidgetSettings.PREF_DEF_APPEARANCE_WIDGETMODE_SUNPOS3x2) && pref0.equals(WidgetSettings.WidgetModeSunPos3x2.MODE3x2_WORLDMAP));
    }

    @Test
    public void test_sunPosMapModePref()
    {
        WorldMapWidgetSettings.saveSunPosMapModePref(context, appWidgetId, WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE, WorldMapWidgetSettings.MAPTAG_3x2);
        WorldMapWidgetSettings.WorldMapWidgetMode pref2 = WorldMapWidgetSettings.loadSunPosMapModePref(context, appWidgetId, WorldMapWidgetSettings.MAPTAG_3x2);
        assertTrue("pref should be EQUIAZIMUTHAL_SIMPLE but was " + pref2, pref2.equals(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIAZIMUTHAL_SIMPLE));

        WorldMapWidgetSettings.saveSunPosMapModePref(context, appWidgetId,WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_BLUEMARBLE, WorldMapWidgetSettings.MAPTAG_3x2);
        WorldMapWidgetSettings.WorldMapWidgetMode pref1 = WorldMapWidgetSettings.loadSunPosMapModePref(context, appWidgetId, WorldMapWidgetSettings.MAPTAG_3x2);
        assertTrue("pref should be EQUIRECTANGULAR_BLUEMARBLE but was " + pref1, pref1.equals(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_BLUEMARBLE));

        WorldMapWidgetSettings.deleteSunPosMapModePref(context, appWidgetId, WorldMapWidgetSettings.MAPTAG_3x2);
        WorldMapWidgetSettings.WorldMapWidgetMode pref0 = WorldMapWidgetSettings.loadSunPosMapModePref(context, appWidgetId, WorldMapWidgetSettings.MAPTAG_3x2);
        assertTrue("pref should be default (EQUIRECTANGULAR_SIMPLE) but was " + pref0, pref0.equals(WorldMapWidgetSettings.defaultSunPosMapMode(WorldMapWidgetSettings.MAPTAG_3x2)) && pref0.equals(WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE));
    }

    @Test
    public void test_showMajorLatitudesPref()
    {
        WorldMapWidgetSettings.saveWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2, false);
        boolean pref2 = WorldMapWidgetSettings.loadWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2);
        assertTrue("pref should be false but was true", !pref2);

        WorldMapWidgetSettings.saveWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2, true);
        boolean pref1 = WorldMapWidgetSettings.loadWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2);
        assertTrue("pref should be true but was false", pref1);

        WorldMapWidgetSettings.deleteWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2);
        boolean pref0 = WorldMapWidgetSettings.loadWorldMapPref(context, appWidgetId, WorldMapWidgetSettings.PREF_KEY_WORLDMAP_MAJORLATITUDES, WorldMapWidgetSettings.MAPTAG_3x2);
        assertTrue("pref should be false (default) but was true", !pref0);
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
    public void test_trackingLevelPref()
    {
        WidgetSettings.saveTrackingLevelPref(context, appWidgetId, 1);
        int level1 = WidgetSettings.loadTrackingLevelPref(context, appWidgetId);
        assertEquals(1, level1);

        WidgetSettings.saveTrackingLevelPref(context, appWidgetId, 10);
        int level10 = WidgetSettings.loadTrackingLevelPref(context, appWidgetId);
        assertEquals(10, level10);

        WidgetSettings.deleteTrackingLevelPref(context, appWidgetId);
        int level0 = WidgetSettings.loadTrackingLevelPref(context, appWidgetId);
        assertEquals(WidgetSettings.PREF_DEF_GENERAL_TRACKINGLEVEL, level0);
        assertEquals(10, WidgetSettings.PREF_DEF_GENERAL_TRACKINGLEVEL);
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
        boolean showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        assertTrue("showTimeDate should be true but was " + showTimeDate, showTimeDate);

        WidgetSettings.saveShowTimeDatePref(context, appWidgetId, false);
        showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        assertTrue("showTimeDate should be false but was " + showTimeDate, !showTimeDate);

        WidgetSettings.deleteShowTimeDatePref(context, appWidgetId);
        showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        assertTrue("showTimeDate should be default (true) but was " + showTimeDate, showTimeDate && showTimeDate == WidgetSettings.PREF_DEF_GENERAL_SHOWTIMEDATE);
    }

    @Test
    public void test_showAbbrMonthPref()
    {
        WidgetSettings.saveShowAbbrMonthPref(context, appWidgetId, true);
        boolean abbreviate = WidgetSettings.loadShowAbbrMonthPref(context, appWidgetId);
        assertTrue("abbreviate should be true but was " + abbreviate, abbreviate);

        WidgetSettings.saveShowAbbrMonthPref(context, appWidgetId, false);
        abbreviate = WidgetSettings.loadShowAbbrMonthPref(context, appWidgetId);
        assertTrue("abbreviate should be false but was " + abbreviate, !abbreviate);

        WidgetSettings.deleteShowAbbrMonthPref(context, appWidgetId);
        abbreviate = WidgetSettings.loadShowAbbrMonthPref(context, appWidgetId);
        assertTrue("abbreviate should be default (true) but was " + abbreviate, abbreviate && abbreviate == WidgetSettings.PREF_DEF_GENERAL_SHOWABBRMONTH);
    }

    @Test
    public void test_showLocalizeHemispherePref()
    {
        WidgetSettings.saveLocalizeHemispherePref(context, appWidgetId, false);
        boolean value0 = WidgetSettings.loadLocalizeHemispherePref(context, appWidgetId);
        assertFalse("localizeHemisphere should be false but was " + value0, value0);

        WidgetSettings.saveLocalizeHemispherePref(context, appWidgetId, true);
        value0 = WidgetSettings.loadLocalizeHemispherePref(context, appWidgetId);
        assertTrue("localizeHemisphere should be true but was " + value0, value0);

        WidgetSettings.deleteLocalizeHemispherePref(context, appWidgetId);
        value0 = WidgetSettings.loadLocalizeHemispherePref(context, appWidgetId);
        assertTrue("localizeHemisphere should be default (true) but was " + value0, value0 && value0 == WidgetSettings.PREF_DEF_GENERAL_LOCALIZE_HEMISPHERE);
    }

    public static final float FLOAT_TOLERANCE = 0.01f;
    protected boolean equals(float float1, float float2)
    {
        return (Math.abs(float1 - float2) < FLOAT_TOLERANCE);
    }

    @Test
    public void test_riseSetOrderPref()
    {
        WidgetSettings.saveRiseSetOrderPref(context, appWidgetId, WidgetSettings.RiseSetOrder.TODAY);
        WidgetSettings.RiseSetOrder mode = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        assertTrue("riseSetOrder should be TODAY but was " + mode, mode == WidgetSettings.RiseSetOrder.TODAY);

        WidgetSettings.saveRiseSetOrderPref(context, appWidgetId, WidgetSettings.RiseSetOrder.LASTNEXT);
        mode = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        assertTrue("riseSetOrder should be LASTNEXT but was " + mode, mode == WidgetSettings.RiseSetOrder.LASTNEXT);

        WidgetSettings.deleteRiseSetOrderPref(context, appWidgetId);
        mode = WidgetSettings.loadRiseSetOrderPref(context, appWidgetId);
        assertTrue("riseSetOrder should be default (TODAY) but was " + mode, mode == WidgetSettings.RiseSetOrder.TODAY && mode == WidgetSettings.PREF_DEF_GENERAL_RISESETORDER);
    }

}
