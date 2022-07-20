/**
    Copyright (C) 2018-2022 Forrest Guice
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

import android.content.ContentResolver;
import android.content.Context;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.AUTHORITY;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APPWIDGETID;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_THEME;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION_CODE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_CALCULATOR;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_CALCULATOR_FEATURES;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LATITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LENGTH_UNITS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LOCALE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LOCATION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OBJECT_HEIGHT;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_ALTITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_FIELDS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TALKBACK;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_DATETIME;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_HOURS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_IS24;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_SECONDS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_WEEKS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_WARNINGS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_TIMEZONE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_ALT;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_APOGEE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_AZ;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_DATE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_DEC;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_ILLUMINATION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_PERIGEE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOONPOS_RA;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_FIRST;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_FIRST_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_FULL;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_FULL_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_NEW;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_NEW_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_THIRD;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_MOON_THIRD_DISTANCE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_AUTUMN;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_CROSS_AUTUMN;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_CROSS_SPRING;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_CROSS_SUMMER;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_CROSS_WINTER;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_SPRING;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_SUMMER;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_TROPICAL_YEAR_LENGTH;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_VERNAL;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_WINTER;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_YEAR;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUNPOS_ALT;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUNPOS_AZ;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUNPOS_DATE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUNPOS_DEC;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUNPOS_ISDAY;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUNPOS_RA;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ASTRO_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_ASTRO_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_BLUE4_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_BLUE4_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_BLUE8_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_BLUE8_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_CIVIL_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_CIVIL_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_GOLDEN_EVENING;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_GOLDEN_MORNING;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_NAUTICAL_RISE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_NAUTICAL_SET;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SUN_NOON;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_CONFIG;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_CONFIG_PROJECTION;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOON;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOONPHASE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOONPHASE_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOONPOS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOONPOS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOON_PROJECTION;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SEASONS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SEASONS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SUN;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SUNPOS;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SUNPOS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SUN_PROJECTION;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CalculatorProviderTest
{
    private Context mockContext;
    private Calendar TEST_DATE0, TEST_DATE1;
    private SuntimesCalculator sunCalculator, moonCalculator;

    @Before
    public void setup()
    {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
        sunCalculator = getCalculator("");
        moonCalculator = getCalculator("moon");

        TEST_DATE0 = Calendar.getInstance(moonCalculator.getTimeZone());
        TEST_DATE0.set(2018, 0, 1, 0, 0, 0);

        TEST_DATE1 = Calendar.getInstance(moonCalculator.getTimeZone());
        TEST_DATE1.set(2019, 0, 1, 0, 0, 0);
    }

    private SuntimesCalculator getCalculator(String calculatorName)
    {
        Location location = WidgetSettings.loadLocationPref(mockContext, 0);
        TimeZone timezone = TimeZone.getTimeZone(WidgetSettings.loadTimezonePref(mockContext, 0));
        SuntimesCalculatorDescriptor descriptor = WidgetSettings.loadCalculatorModePref(mockContext, 0, calculatorName);
        SuntimesCalculatorFactory factory = new SuntimesCalculatorFactory(mockContext, descriptor);
        return factory.createCalculator(location, timezone);
    }

    @Test
    public void test_columns()
    {
        List<String> columns = new ArrayList<>();
        Collections.addAll(columns, QUERY_CONFIG_PROJECTION);
        Collections.addAll(columns, QUERY_SEASONS_PROJECTION);
        Collections.addAll(columns, QUERY_SUN_PROJECTION);
        Collections.addAll(columns, QUERY_SUNPOS_PROJECTION);
        Collections.addAll(columns, QUERY_MOON_PROJECTION);
        Collections.addAll(columns, QUERY_MOONPOS_PROJECTION);
        Collections.addAll(columns, QUERY_MOONPHASE_PROJECTION);
        test_projectionHasUniqueColumns(columns.toArray(new String[columns.size()]));

        test_query_config_projection();
        test_query_seasons_projection();
        test_query_sun_projection();
        test_query_sunpos_projection();
        test_query_moon_projection();
        test_query_moonpos_projection();
        test_query_moonphase_projection();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CONFIG
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * test_query_config_projection
     */
    @Test
    public void test_query_config_projection()
    {
        String[] TEST_CONFIG_PROJECTION = new String[] {
                COLUMN_CONFIG_APP_VERSION, COLUMN_CONFIG_APP_VERSION_CODE,
                COLUMN_CONFIG_PROVIDER_VERSION, COLUMN_CONFIG_PROVIDER_VERSION_CODE,
                COLUMN_CONFIG_LOCALE, COLUMN_CONFIG_APP_THEME,
                COLUMN_CONFIG_CALCULATOR, COLUMN_CONFIG_CALCULATOR_FEATURES,
                COLUMN_CONFIG_LOCATION, COLUMN_CONFIG_LATITUDE, COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE,
                COLUMN_CONFIG_TIMEZONE, COLUMN_CONFIG_APPWIDGETID,
                COLUMN_CONFIG_OPTION_TIME_IS24, COLUMN_CONFIG_OPTION_TIME_SECONDS, COLUMN_CONFIG_OPTION_TIME_HOURS, COLUMN_CONFIG_OPTION_TIME_WEEKS, COLUMN_CONFIG_OPTION_TIME_DATETIME,
                COLUMN_CONFIG_OPTION_ALTITUDE, COLUMN_CONFIG_OPTION_WARNINGS, COLUMN_CONFIG_OPTION_TALKBACK, COLUMN_CONFIG_LENGTH_UNITS, COLUMN_CONFIG_OBJECT_HEIGHT, COLUMN_CONFIG_OPTION_FIELDS
        };

        List<String> projection = Arrays.asList(QUERY_CONFIG_PROJECTION);
        for (String column : TEST_CONFIG_PROJECTION) {
            test_projectionContainsColumn(column, projection);
        }
    }

    /**
     * test_query_config
     */
    @Test
    public void test_query_config()
    {
        test_query_config_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertTrue("Unable to getContentResolver!", resolver != null);

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_CONFIG);
        String[] projection = QUERY_CONFIG_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_CONFIG", cursor, projection);
        assertTrue("QUERY_CONFIG should return one row.", cursor.getCount() == 1);

        assertTrue("COLUMN_CONFIG_APP_VERSION should be " + BuildConfig.VERSION_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_APP_VERSION)).startsWith(BuildConfig.VERSION_NAME));
        assertTrue("COLUMN_CONFIG_APP_VERSION_CODE should be " + BuildConfig.VERSION_CODE,cursor.getInt(cursor.getColumnIndex(COLUMN_CONFIG_APP_VERSION_CODE)) == BuildConfig.VERSION_CODE);
        assertTrue("COLUMN_CONFIG_PROVIDER_VERSION should be " + CalculatorProviderContract.VERSION_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_PROVIDER_VERSION)).equals(CalculatorProviderContract.VERSION_NAME));
        assertTrue("COLUMN_CONFIG_PROVIDER_VERSION_CODE should be " +  CalculatorProviderContract.VERSION_CODE,cursor.getInt(cursor.getColumnIndex(COLUMN_CONFIG_PROVIDER_VERSION_CODE)) == CalculatorProviderContract.VERSION_CODE);

        int appWidgetID = 0;
        SuntimesCalculatorDescriptor descriptor = WidgetSettings.loadCalculatorModePref(mockContext, appWidgetID);
        assertTrue("COLUMN_CONFIG_CALCULATOR should be " + descriptor.getName(), descriptor.getName().equals(cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_CALCULATOR))));

        Location location = WidgetSettings.loadLocationPref(mockContext, appWidgetID);
        assertTrue("COLUMN_CONFIG_APPWIDGETID should be " + appWidgetID, cursor.getInt(cursor.getColumnIndex(COLUMN_CONFIG_APPWIDGETID)) == appWidgetID);
        assertTrue("COLUMN_CONFIG_LATITUDE should be " + location.getLatitude(), cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_LATITUDE)).equals(location.getLatitude()));
        assertTrue("COLUMN_CONFIG_LONGITUDE should be " + location.getLongitude(), cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_LONGITUDE)).equals(location.getLongitude()));
        assertTrue("COLUMN_CONFIG_ALTITUDE should be " + location.getAltitude(), cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_ALTITUDE)).equals(location.getAltitude()));

        String timezone = WidgetSettings.loadTimezonePref(mockContext, appWidgetID);
        assertTrue("COLUMN_CONFIG_TIMEZONE should be " + timezone, cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_TIMEZONE)).equals(timezone));

        String appTheme = AppSettings.loadThemePref(mockContext);
        assertTrue("COLUMN_CONFIG_APPTHEME should be " + appTheme, cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_APP_THEME)).equals(appTheme));

        AppSettings.LocaleMode localeMode = AppSettings.loadLocaleModePref(mockContext);
        String locale = ((localeMode == AppSettings.LocaleMode.SYSTEM_LOCALE) ? null : AppSettings.loadLocalePref(mockContext));
        if (localeMode == AppSettings.LocaleMode.SYSTEM_LOCALE) {
            assertTrue("COLUMN_CONFIG_LOCALE should be null", cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_LOCALE)) == null);
        } else {
            assertTrue("COLUMN_CONFIG_LOCALE should be " + locale, cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_LOCALE)).equals(locale));
        }
        cursor.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SEASONS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * test_query_seasons_projection
     */
    @Test
    public void test_query_seasons_projection()
    {
        List<String> projection = Arrays.asList(QUERY_SEASONS_PROJECTION);
        assertTrue("default projection contains COLUMN_SEASON_VERNAL", projection.contains(COLUMN_SEASON_VERNAL));
        assertTrue("default projection contains COLUMN_SEASON_SUMMER", projection.contains(COLUMN_SEASON_SUMMER));
        assertTrue("default projection contains COLUMN_SEASON_AUTUMN", projection.contains(COLUMN_SEASON_AUTUMN));
        assertTrue("default projection contains COLUMN_SEASON_WINTER", projection.contains(COLUMN_SEASON_WINTER));
        assertTrue("default projection contains COLUMN_SEASON_YEAR", projection.contains(COLUMN_SEASON_YEAR));
        test_projectionHasUniqueColumns(QUERY_SEASONS_PROJECTION);
    }

    /**
     * test_query_seasons
     */
    @Test
    public void test_query_seasons()
    {
        test_query_seasons_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertTrue("Unable to getContentResolver!", resolver != null);

        // case 0:
        Uri uri0 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SEASONS);
        String[] projection0 = QUERY_SEASONS_PROJECTION;
        Cursor cursor0 = resolver.query(uri0, projection0, null, null, null);
        test_cursorHasColumns("QUERY_SEASONS", cursor0, projection0);
        assertTrue(COLUMN_SEASON_YEAR + " should contain int!", columnIsInt(cursor0, COLUMN_SEASON_YEAR));
        test_allColumnsLong("QUERY_SEASONS", cursor0,
                new String[] { COLUMN_SEASON_CROSS_SPRING, COLUMN_SEASON_CROSS_SUMMER, COLUMN_SEASON_CROSS_AUTUMN, COLUMN_SEASON_CROSS_WINTER, COLUMN_SEASON_TROPICAL_YEAR_LENGTH,
                COLUMN_SEASON_SPRING, COLUMN_SEASON_VERNAL, COLUMN_SEASON_SUMMER, COLUMN_SEASON_AUTUMN, COLUMN_SEASON_WINTER});

        // case 1: year
        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SEASONS + "/" + TEST_DATE0.get(Calendar.YEAR));
        String[] projection1 = QUERY_SEASONS_PROJECTION;
        Cursor cursor1 = resolver.query(uri1, projection1, null, null, null);
        test_cursorHasColumns("QUERY_SEASONS", cursor1, projection1);
        assertTrue(COLUMN_SEASON_YEAR + " should contain int!", columnIsInt(cursor1, COLUMN_SEASON_YEAR));
        test_allColumnsLong("QUERY_SEASONS", cursor1,
                new String[] { COLUMN_SEASON_CROSS_SPRING, COLUMN_SEASON_CROSS_SUMMER, COLUMN_SEASON_CROSS_AUTUMN, COLUMN_SEASON_CROSS_WINTER, COLUMN_SEASON_TROPICAL_YEAR_LENGTH,
                COLUMN_SEASON_SPRING, COLUMN_SEASON_VERNAL, COLUMN_SEASON_SUMMER, COLUMN_SEASON_AUTUMN, COLUMN_SEASON_WINTER});

        // case 2: range
        Uri uri2 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SEASONS + "/" + TEST_DATE0.get(Calendar.YEAR) + "-" + TEST_DATE1.get(Calendar.YEAR));
        String[] projection2 = QUERY_SEASONS_PROJECTION;
        Cursor cursor2 = resolver.query(uri2, projection2, null, null, null);
        test_cursorHasColumns("QUERY_SEASONS", cursor2, projection2);

        /**if (cursor1 != null)
        {
            cursor1.moveToFirst();
            long vernalEquinoxTime = cursor1.getLong(cursor1.getColumnIndex(COLUMN_SEASON_VERNAL));
            long summerSolsticeTime = cursor1.getLong(cursor1.getColumnIndex(COLUMN_SEASON_SUMMER));
            long autumnalEquinoxTime = cursor1.getLong(cursor1.getColumnIndex(COLUMN_SEASON_AUTUMN));
            long winterSolsticeTime = cursor1.getLong(cursor1.getColumnIndex(COLUMN_SEASON_WINTER));
            cursor1.close();

            assertTrue("COLUMN_SEASON_VERNAL result missing", vernalEquinoxTime != 0);
            assertTrue("COLUMN_SEASON_SUMMER result missing", summerSolsticeTime != 0);
            assertTrue("COLUMN_SEASON_AUTUMN result missing", autumnalEquinoxTime != 0);
            assertTrue("COLUMN_SEASON_WINTER result missing", winterSolsticeTime != 0);

            test_dateUTC("COLUMN_SEASON_VERNAL", vernalEquinoxTime,2018, 2, 20, 16, 15);
            test_dateUTC("COLUMN_SEASON_SUMMER", summerSolsticeTime,2018, 5, 21, 10, 7);
            test_dateUTC("COLUMN_SEASON_AUTUMN", autumnalEquinoxTime,2018, 8, 23, 1, 54);
            test_dateUTC("COLUMN_SEASON_WINTER", winterSolsticeTime,2018, 11, 21, 22, 22);
        }*/
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SUN
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * test_query_sun_projection
     */
    @Test
    public void test_query_sun_projection()
    {
        List<String> projection = Arrays.asList(QUERY_SUN_PROJECTION);
        assertTrue("default projection contains COLUMN_SUN_NOON", projection.contains(COLUMN_SUN_NOON));

        assertTrue("default projection contains COLUMN_ACTUAL_RISE", projection.contains(COLUMN_SUN_ACTUAL_RISE));
        assertTrue("default projection contains COLUMN_ACTUAL_SET", projection.contains(COLUMN_SUN_ACTUAL_SET));
        assertTrue("default projection contains COLUMN_CIVIL_RISE", projection.contains(COLUMN_SUN_CIVIL_RISE));
        assertTrue("default projection contains COLUMN_CIVIL_SET", projection.contains(COLUMN_SUN_CIVIL_SET));
        assertTrue("default projection contains COLUMN_NAUTICAL_RISE", projection.contains(COLUMN_SUN_NAUTICAL_RISE));
        assertTrue("default projection contains COLUMN_NAUTICAL_SET", projection.contains(COLUMN_SUN_NAUTICAL_SET));
        assertTrue("default projection contains COLUMN_ASTRO_RISE", projection.contains(COLUMN_SUN_ASTRO_RISE));
        assertTrue("default projection contains COLUMN_ASTRO_SET", projection.contains(COLUMN_SUN_ASTRO_SET));

        assertTrue("default projection contains COLUMN_GOLDEN_MORNING", projection.contains(COLUMN_SUN_GOLDEN_MORNING));
        assertTrue("default projection contains COLUMN_GOLDEN_EVENING", projection.contains(COLUMN_SUN_GOLDEN_EVENING));

        assertTrue("default projection contains COLUMN_SUN_BLUE8_RISE", projection.contains(COLUMN_SUN_BLUE8_RISE));
        assertTrue("default projection contains COLUMN_SUN_BLUE4_RISE", projection.contains(COLUMN_SUN_BLUE4_RISE));
        assertTrue("default projection contains COLUMN_SUN_BLUE8_SET", projection.contains(COLUMN_SUN_BLUE8_SET));
        assertTrue("default projection contains COLUMN_SUN_BLUE4_SET", projection.contains(COLUMN_SUN_BLUE4_SET));

        test_projectionHasUniqueColumns(QUERY_SUN_PROJECTION);
    }

    /**
     * test_query_sun
     */
    @Test
    public void test_query_sun()
    {
        test_query_sun_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertTrue("Unable to getContentResolver!", resolver != null);

        // case 0:
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SUN);
        String[] projection = QUERY_SUN_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_SUN", cursor, projection);
        test_allColumnsLong("QUERY_SUN", cursor, projection);
        test_suntimes(cursor, sunCalculator, Calendar.getInstance());

        // case 1: date
        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SUN + "/" + TEST_DATE0.getTimeInMillis());
        String[] projection1 = QUERY_SUN_PROJECTION;
        Cursor cursor1 = resolver.query(uri1, projection1, null, null, null);
        test_cursorHasColumns("QUERY_SUN", cursor1, projection1);
        test_allColumnsLong("QUERY_SUN", cursor, projection1);
        test_suntimes(cursor1, sunCalculator, TEST_DATE0);

        // case 2: range
        Uri uri2 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SUN + "/" + TEST_DATE0.getTimeInMillis() + "-" + TEST_DATE1.getTimeInMillis());
        String[] projection2 = QUERY_SUN_PROJECTION;
        Cursor cursor2 = resolver.query(uri2, projection2, null, null, null);
        test_cursorHasColumns("QUERY_SUN", cursor2, projection2);
    }

    public void test_suntimes(Cursor cursor, SuntimesCalculator calculator, Calendar date)
    {
        if (cursor != null)
        {
            cursor.moveToFirst();

            Calendar noon0 = calculator.getSolarNoonCalendarForDate(date);
            Calendar noon1 = Calendar.getInstance();
            noon1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_NOON)));
            assertTrue("noon time should match .. " + noon0.getTimeInMillis() + " != " + noon1.getTimeInMillis(), noon0.getTimeInMillis() == noon1.getTimeInMillis());


            Calendar sunrise0 = calculator.getOfficialSunriseCalendarForDate(date);
            Calendar sunrise1 = Calendar.getInstance();
            sunrise1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_ACTUAL_RISE)));
            assertTrue("sunrise time should match .. " + sunrise0.getTimeInMillis() + " != " + sunrise1.getTimeInMillis(), sunrise0.getTimeInMillis() == sunrise1.getTimeInMillis());

            Calendar civilrise0 = calculator.getCivilSunriseCalendarForDate(date);
            Calendar civilrise1 = Calendar.getInstance();
            civilrise1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_CIVIL_RISE)));
            assertTrue("civilrise time should match .. " + civilrise0.getTimeInMillis() + " != " + civilrise1.getTimeInMillis(), civilrise0.getTimeInMillis() == civilrise1.getTimeInMillis());

            Calendar nauticalrise0 = calculator.getNauticalSunriseCalendarForDate(date);
            Calendar nauticalrise1 = Calendar.getInstance();
            nauticalrise1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_NAUTICAL_RISE)));
            assertTrue("nauticalrise time should match .. " + nauticalrise0.getTimeInMillis() + " != " + nauticalrise1.getTimeInMillis(), nauticalrise0.getTimeInMillis() == nauticalrise1.getTimeInMillis());

            Calendar astrorise0 = calculator.getAstronomicalSunriseCalendarForDate(date);
            Calendar astrorise1 = Calendar.getInstance();
            astrorise1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_ASTRO_RISE)));
            assertTrue("astrorise time should match .. " + astrorise0.getTimeInMillis() + " != " + astrorise1.getTimeInMillis(), astrorise0.getTimeInMillis() == astrorise1.getTimeInMillis());


            Calendar sunset0 = calculator.getOfficialSunsetCalendarForDate(date);
            Calendar sunset1 = Calendar.getInstance();
            sunset1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_ACTUAL_SET)));
            assertTrue("sunset time should match .. " + sunset0.getTimeInMillis() + " != " + sunset1.getTimeInMillis(), sunset0.getTimeInMillis() == sunset1.getTimeInMillis());

            Calendar civilset0 = calculator.getCivilSunsetCalendarForDate(date);
            Calendar civilset1 = Calendar.getInstance();
            civilset1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_CIVIL_SET)));
            assertTrue("civilset time should match .. " + civilset0.getTimeInMillis() + " != " + civilset1.getTimeInMillis(), civilset0.getTimeInMillis() == civilset1.getTimeInMillis());

            Calendar nauticalset0 = calculator.getNauticalSunsetCalendarForDate(date);
            Calendar nauticalset1 = Calendar.getInstance();
            nauticalset1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_NAUTICAL_SET)));
            assertTrue("nauticalset time should match .. " + nauticalset0.getTimeInMillis() + " != " + nauticalset1.getTimeInMillis(), nauticalset0.getTimeInMillis() == nauticalset1.getTimeInMillis());

            Calendar astroset0 = calculator.getAstronomicalSunsetCalendarForDate(date);
            Calendar astroset1 = Calendar.getInstance();
            astroset1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_ASTRO_SET)));
            assertTrue("astroset time should match .. " + astroset0.getTimeInMillis() + " != " + astroset1.getTimeInMillis(), astroset0.getTimeInMillis() == astroset1.getTimeInMillis());


            Calendar golden_m0 = calculator.getMorningGoldenHourForDate(date);
            Calendar golden_m1 = Calendar.getInstance();
            golden_m1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_GOLDEN_MORNING)));
            assertTrue("golden morning time should match .. " + golden_m0.getTimeInMillis() + " != " + golden_m1.getTimeInMillis(), golden_m0.getTimeInMillis() == golden_m1.getTimeInMillis());

            Calendar golden_e0 = calculator.getEveningGoldenHourForDate(date);
            Calendar golden_e1 = Calendar.getInstance();
            golden_e1.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_GOLDEN_EVENING)));
            assertTrue("golden evening time should match .. " + golden_e0.getTimeInMillis() + " != " + golden_e1.getTimeInMillis(), golden_e0.getTimeInMillis() == golden_e1.getTimeInMillis());

            Calendar[] blueMorning0 = calculator.getMorningBlueHourForDate(date);
            Calendar[] blueEvening0 = calculator.getEveningBlueHourForDate(date);

            Calendar blueMorning_81 = Calendar.getInstance();
            blueMorning_81.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_BLUE8_RISE)));
            assertTrue("blue morning time should match .. " + blueMorning0[0].getTimeInMillis() + " != " + blueMorning_81.getTimeInMillis(), blueMorning0[0].getTimeInMillis() == blueMorning_81.getTimeInMillis());

            Calendar blueMorning_41 = Calendar.getInstance();
            blueMorning_41.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_BLUE4_RISE)));
            assertTrue("blue morning time should match .. " + blueMorning0[1].getTimeInMillis() + " != " + blueMorning_41.getTimeInMillis(), blueMorning0[1].getTimeInMillis() == blueMorning_41.getTimeInMillis());

            Calendar blueEvening_41 = Calendar.getInstance();
            blueEvening_41.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_BLUE4_SET)));
            assertTrue("blue evening time should match .. " + blueEvening0[0].getTimeInMillis() + " != " + blueEvening_41.getTimeInMillis(), blueEvening0[0].getTimeInMillis() == blueEvening_41.getTimeInMillis());

            Calendar blueEvening_81 = Calendar.getInstance();
            blueEvening_81.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_SUN_BLUE8_SET)));
            assertTrue("blue evening time should match .. " + blueEvening0[1].getTimeInMillis() + " != " + blueEvening_81.getTimeInMillis(), blueEvening0[1].getTimeInMillis() == blueEvening_81.getTimeInMillis());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SUNPOS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * test_query_sunpos_projection
     */
    @Test
    public void test_query_sunpos_projection()
    {
        List<String> projection = Arrays.asList(QUERY_SUNPOS_PROJECTION);
        assertTrue("default projection contains COLUMN_SUNPOS_ALT", projection.contains(COLUMN_SUNPOS_ALT));
        assertTrue("default projection contains COLUMN_SUNPOS_AZ", projection.contains(COLUMN_SUNPOS_AZ));
        assertTrue("default projection contains COLUMN_SUNPOS_RA", projection.contains(COLUMN_SUNPOS_RA));
        assertTrue("default projection contains COLUMN_SUNPOS_DEC", projection.contains(COLUMN_SUNPOS_DEC));
        assertTrue("default projection contains COLUMN_SUNPOS_ISDAY", projection.contains(COLUMN_SUNPOS_ISDAY));
        assertTrue("default projection contains COLUMN_SUNPOS_DATE", projection.contains(COLUMN_SUNPOS_DATE));
        test_projectionHasUniqueColumns(QUERY_SUNPOS_PROJECTION);
    }

    /**
     * test_query_sunpos
     */
    @Test
    public void test_query_sunpos()
    {
        test_query_sunpos_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertTrue("Unable to getContentResolver!", resolver != null);

        // case 0:
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SUNPOS);
        String[] projection = QUERY_SUNPOS_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_SUNPOS", cursor, projection);
        test_allColumnsDouble("QUERY_SUNPOS", cursor, new String[] {COLUMN_SUNPOS_ALT, COLUMN_SUNPOS_AZ, COLUMN_SUNPOS_DEC, COLUMN_SUNPOS_RA} );
        assertTrue("sunpos date column should be long", columnIsLong(cursor, COLUMN_SUNPOS_DATE));

        // case 1: date
        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SUNPOS + "/" + TEST_DATE0.getTimeInMillis());
        String[] projection1 = QUERY_SUNPOS_PROJECTION;
        Cursor cursor1 = resolver.query(uri1, projection1, null, null, null);
        test_cursorHasColumns("QUERY_SUNPOS", cursor1, projection1);
        test_allColumnsDouble("QUERY_SUNPOS", cursor, new String[] {COLUMN_SUNPOS_ALT, COLUMN_SUNPOS_AZ, COLUMN_SUNPOS_DEC, COLUMN_SUNPOS_RA} );
        assertTrue("sunpos date column should be long", columnIsLong(cursor, COLUMN_SUNPOS_DATE));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MOON
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * test_query_moon_projection
     */
    @Test
    public void test_query_moon_projection()
    {
        List<String> projection = Arrays.asList(QUERY_MOON_PROJECTION);
        assertTrue("default projection contains COLUMN_MOON_RISE", projection.contains(COLUMN_MOON_RISE));
        assertTrue("default projection contains COLUMN_MOON_SET", projection.contains(COLUMN_MOON_SET));
        test_projectionHasUniqueColumns(QUERY_MOON_PROJECTION);
    }

    /**
     * test_query_moon
     */
    @Test
    public void test_query_moon()
    {
        test_query_moon_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertTrue("Unable to getContentResolver!", resolver != null);

        // case 0:
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOON);
        String[] projection = QUERY_MOON_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_MOON", cursor, projection);
        test_allColumnsLong("QUERY_MOON", cursor, projection);
        cursor.moveToFirst();
        test_moontimes(cursor, moonCalculator.getMoonTimesForDate(Calendar.getInstance(moonCalculator.getTimeZone())));

        // case 1: date
        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOON + "/" + TEST_DATE0.getTimeInMillis());
        String[] projection1 = QUERY_MOON_PROJECTION;
        Cursor cursor1 = resolver.query(uri1, projection1, null, null, null);
        test_cursorHasColumns("QUERY_MOON", cursor1, projection1);
        test_allColumnsLong("QUERY_MOON", cursor, projection1);
        cursor.moveToFirst();
        test_moontimes(cursor1, moonCalculator.getMoonTimesForDate(TEST_DATE0));

        // case 2: range
        Uri uri2 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOON + "/" + TEST_DATE0.getTimeInMillis() + "-" + TEST_DATE1.getTimeInMillis());
        String[] projection2 = QUERY_MOON_PROJECTION;
        Cursor cursor2 = resolver.query(uri2, projection2, null, null, null);
        test_cursorHasColumns("QUERY_MOON", cursor2, projection2);
    }

    public void test_moontimes(Cursor cursor, @NonNull SuntimesCalculator.MoonTimes oracle)
    {
        if (cursor != null)
        {
            if (oracle.riseTime != null)
            {
                long moonriseMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_MOON_RISE));
                assertTrue("moonrise time should match .. " + oracle.riseTime.getTimeInMillis() + " != " + moonriseMillis,
                        oracle.riseTime.getTimeInMillis() == moonriseMillis);
                Log.d("TEST", "moon rise: " + oracle.riseTime.getTimeInMillis() + " == " + moonriseMillis );
            } else {
                assertTrue("moonrise should not occur today", cursor.isNull(0));
                Log.d("TEST", "moon rise: null");
            }

            if (oracle.setTime != null)
            {
                long moonsetMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_MOON_SET));
                assertTrue("moonset time should match .. " + oracle.setTime + " != " + moonsetMillis,
                        oracle.setTime.getTimeInMillis() == moonsetMillis);
                Log.d("TEST", "moon set: " + oracle.setTime.getTimeInMillis() + " == " + moonsetMillis );
            } else {
                assertTrue("moonset should not occur today", cursor.isNull(1));
                Log.d("TEST", "moon set: null");
            }
        }
    }

    @Test
    public void test_query_moon2()
    {
        // setup..
        TimeZone tz = moonCalculator.getTimeZone();
        Calendar date0 = Calendar.getInstance(tz);
        date0.set(2020, 3, 11, 12, 0, 0);

        String[] projection = QUERY_MOON_PROJECTION;
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOON + "/"
                + (date0.getTimeInMillis() - SUN_PERIOD_MILLIS) + "-" + (date0.getTimeInMillis() + SUN_PERIOD_MILLIS) );

        Calendar[] dates = new Calendar[] { Calendar.getInstance(tz), Calendar.getInstance(tz), Calendar.getInstance(tz) };
        dates[0].setTimeInMillis(date0.getTimeInMillis() - SUN_PERIOD_MILLIS);
        dates[1].setTimeInMillis(date0.getTimeInMillis());
        dates[2].setTimeInMillis(date0.getTimeInMillis() + SUN_PERIOD_MILLIS);

        SuntimesCalculator.MoonTimes[] oracle = new SuntimesCalculator.MoonTimes[3];
        for (int i=0; i<oracle.length; i++) {
            oracle[i] = moonCalculator.getMoonTimesForDate(dates[i]);
        }

        // test..
        ContentResolver resolver = mockContext.getContentResolver();
        assertNotNull("Unable to getContentResolver!", resolver);
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_MOON", cursor, projection);
        test_allColumnsLong("QUERY_MOON", cursor, projection);

        if (cursor != null)
        {
            cursor.moveToFirst();
            for (int i=0; i<3; i++)
            {
                if (cursor.isAfterLast())
                {
                    Log.w(getClass().getSimpleName(), "queryMoonriseMoonset: cursor contains fewer rows than expected (3); got " + i + ": " + dates[i].getTimeInMillis());
                    break;
                }
                test_moontimes(cursor, oracle[i]);
                cursor.moveToNext();
            }
            cursor.close();
        }
    }
    public static final long SUN_PERIOD_MILLIS = 24L * 60 * 60 * 1000;        // 24 hr

    @Test
    public void test_query_moon3()
    {
        // setup..
        TimeZone tz = moonCalculator.getTimeZone();
        Calendar date0 = Calendar.getInstance(tz);
        date0.set(2020, 3, 11, 12, 0, 0);
        SuntimesCalculator.MoonTimes oracle = moonCalculator.getMoonTimesForDate(date0);

        String[] projection = QUERY_MOON_PROJECTION;
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOON + "/" + date0.getTimeInMillis());

        // test..
        ContentResolver resolver = mockContext.getContentResolver();
        assertNotNull("Unable to getContentResolver!", resolver);
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_MOON", cursor, projection);
        test_allColumnsLong("QUERY_MOON", cursor, projection);

        if (cursor != null)
        {
            cursor.moveToFirst();
            test_moontimes(cursor, oracle);
            cursor.close();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MOONPOS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * test_query_moonpos_projection
     */
    @Test
    public void test_query_moonpos_projection()
    {
        List<String> projection = Arrays.asList(QUERY_MOONPOS_PROJECTION);
        assertTrue("default projection contains COLUMN_MOONPOS_ALT", projection.contains(COLUMN_MOONPOS_ALT));
        assertTrue("default projection contains COLUMN_MOONPOS_AZ", projection.contains(COLUMN_MOONPOS_AZ));
        assertTrue("default projection contains COLUMN_MOONPOS_RA", projection.contains(COLUMN_MOONPOS_RA));
        assertTrue("default projection contains COLUMN_MOONPOS_DEC", projection.contains(COLUMN_MOONPOS_DEC));
        assertTrue("default projection contains COLUMN_MOONPOS_DISTANCE", projection.contains(COLUMN_MOONPOS_DISTANCE));
        assertTrue("default projection contains COLUMN_MOONPOS_PERIGEE", projection.contains(COLUMN_MOONPOS_PERIGEE));
        assertTrue("default projection contains COLUMN_MOONPOS_APOGEE", projection.contains(COLUMN_MOONPOS_APOGEE));
        assertTrue("default projection contains COLUMN_MOONPOS_ILLUMINATION", projection.contains(COLUMN_MOONPOS_ILLUMINATION));
        assertTrue("default projection contains COLUMN_MOONPOS_DATE", projection.contains(COLUMN_MOONPOS_DATE));
        test_projectionHasUniqueColumns(QUERY_MOONPOS_PROJECTION);
    }

    /**
     * test_query_moonpos
     */
    @Test
    public void test_query_moonpos()
    {
        test_query_moonpos_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertTrue("Unable to getContentResolver!", resolver != null);

        // case 0:
        Uri uri0 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPOS);
        String[] projection0 = QUERY_MOONPOS_PROJECTION;
        Cursor cursor0 = resolver.query(uri0, projection0, null, null, null);
        test_cursorHasColumns("QUERY_MOONPOS", cursor0, projection0);
        test_allColumnsDouble("QUERY_MOONPOS", cursor0, new String[] {COLUMN_MOONPOS_ALT, COLUMN_MOONPOS_AZ, COLUMN_MOONPOS_DEC, COLUMN_MOONPOS_RA, COLUMN_MOONPOS_DISTANCE} );
        test_allColumnsLong("QUERY_MOONPOS", cursor0, new String[] {COLUMN_MOONPOS_PERIGEE, COLUMN_MOONPOS_APOGEE, COLUMN_MOONPOS_DATE});

        // case 1: date
        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPOS + "/" + TEST_DATE0.getTimeInMillis());
        String[] projection1 = QUERY_MOONPOS_PROJECTION;
        Cursor cursor1 = resolver.query(uri1, projection1, null, null, null);
        test_cursorHasColumns("QUERY_MOONPOS", cursor1, projection1);
        test_allColumnsDouble("QUERY_MOONPOS", cursor1, new String[] {COLUMN_MOONPOS_ALT, COLUMN_MOONPOS_AZ, COLUMN_MOONPOS_DEC, COLUMN_MOONPOS_RA, COLUMN_MOONPOS_DISTANCE} );
        test_allColumnsLong("QUERY_MOONPOS", cursor1, new String[] {COLUMN_MOONPOS_PERIGEE, COLUMN_MOONPOS_APOGEE, COLUMN_MOONPOS_DATE});
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MOONPHASE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * test_query_moonphase_projection
     */
    @Test
    public void test_query_moonphase_projection()
    {
        List<String> projection = Arrays.asList(QUERY_MOONPHASE_PROJECTION);
        assertTrue("default projection contains COLUMN_MOON_NEW", projection.contains(COLUMN_MOON_NEW));
        assertTrue("default projection contains COLUMN_MOON_FIRST", projection.contains(COLUMN_MOON_FIRST));
        assertTrue("default projection contains COLUMN_MOON_FULL", projection.contains(COLUMN_MOON_FULL));
        assertTrue("default projection contains COLUMN_MOON_THIRD", projection.contains(COLUMN_MOON_THIRD));
        assertTrue("default projection contains COLUMN_MOON_NEW_DISTANCE", projection.contains(COLUMN_MOON_NEW_DISTANCE));
        assertTrue("default projection contains COLUMN_MOON_FIRST_DISTANCE", projection.contains(COLUMN_MOON_FIRST_DISTANCE));
        assertTrue("default projection contains COLUMN_MOON_FULL_DISTANCE", projection.contains(COLUMN_MOON_FULL_DISTANCE));
        assertTrue("default projection contains COLUMN_MOON_THIRD_DISTANCE", projection.contains(COLUMN_MOON_THIRD_DISTANCE));
        test_projectionHasUniqueColumns(QUERY_MOONPHASE_PROJECTION);
    }

    /**
     * test_query_moonphase
     */
    @Test
    public void test_query_moonphase()
    {
        test_query_moonphase_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertTrue("Unable to getContentResolver!", resolver != null);

        Calendar startDate = TEST_DATE0;
        Calendar endDate = TEST_DATE1;

        // case 0:
        Uri uri0 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPHASE);
        String[] projection0 = QUERY_MOONPHASE_PROJECTION;
        Cursor cursor0 = resolver.query(uri0, projection0, null, null, null);
        test_cursorHasColumns("QUERY_MOONPHASE", cursor0, projection0);
        test_allColumnsLong("QUERY_MOONPHASE", cursor0, projection0);

        // case 1: date
        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPHASE + "/" + startDate.getTimeInMillis());
        String[] projection1 = QUERY_MOONPHASE_PROJECTION;
        Cursor cursor1 = resolver.query(uri1, projection1, null, null, null);
        test_cursorHasColumns("QUERY_MOONPHASE", cursor1, projection1);
        test_allColumnsLong("QUERY_MOONPHASE", cursor1, projection1);

        // case 2: range
        Uri uri2 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPHASE + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());
        String[] projection2 = QUERY_MOONPHASE_PROJECTION;
        Cursor cursor2 = resolver.query(uri2, projection2, null, null, null);
        test_cursorHasColumns("QUERY_MOONPHASE", cursor2, projection2);
        test_allColumnsLong("QUERY_MOONPHASE", cursor2, projection2);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Query Helpers
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_processSelection()
    {
        String[] selectionArgs0 = new String[] {"33", "-112", "330", "time4a-time4j"};
        String selectionString0 = COLUMN_CONFIG_LATITUDE + "=? AND " + COLUMN_CONFIG_LONGITUDE + "=? AND " + COLUMN_CONFIG_ALTITUDE + "=? AND " + COLUMN_CONFIG_CALCULATOR + "=?";
        String selectionString0_truth = COLUMN_CONFIG_LATITUDE + "=33"
                                        + " AND " + COLUMN_CONFIG_LONGITUDE + "=-112"
                                        + " AND " + COLUMN_CONFIG_ALTITUDE + "=330"
                                        + " AND " + COLUMN_CONFIG_CALCULATOR + "=time4a-time4j";
        String selectionString0_complete = CalculatorProvider.processSelectionArgs(selectionString0, selectionArgs0);
        assertTrue("selectionString should be non-null", selectionString0_complete != null);
        assertTrue("selectionString should match", selectionString0_complete.equals(selectionString0_truth));

        String selectionString1_complete = CalculatorProvider.processSelectionArgs(null, selectionArgs0);
        assertTrue("selectionString should be null", selectionString1_complete == null);

        HashMap<String,String> selection = CalculatorProvider.processSelection(selectionString0_complete);
        assertTrue("processSelection returns non-null", selection != null);
        assertTrue("processSelection returns same number of columns: " + selection.size() + " != " + selectionArgs0.length, selection.size() == selectionArgs0.length);
        assertTrue("CONFIG_LATITUDE matches", selection.get(COLUMN_CONFIG_LATITUDE).equals(selectionArgs0[0]));
        assertTrue("CONFIG_LONGITUDE matches", selection.get(COLUMN_CONFIG_LONGITUDE).equals(selectionArgs0[1]));
        assertTrue("CONFIG_ALTITUDE matches", selection.get(COLUMN_CONFIG_ALTITUDE).equals(selectionArgs0[2]));
        assertTrue("CONFIG_CALCULATOR matches", selection.get(COLUMN_CONFIG_CALCULATOR).equals(selectionArgs0[3]));
    }

    @Test
    public void test_processSelection_location()
    {
        String[][] TEST_LOCATION = new String[][] { {"33", "-112", "330"},
                                                    {null, "180", null},
                                                    {"40", null, null}
        };
        for (String[] location_parts : TEST_LOCATION)
        {
            assertTrue("TEST_LOCATION should contain 3 parts!", location_parts.length == 3);
            HashMap<String,String> selection = createSelection(location_parts);
            Location location = CalculatorProvider.processSelection_location(selection);
            if (location_parts[0] == null || location_parts[1] == null) {
                assertTrue("Location should be null (missing latitude or longitude)", location == null);

            } else {
                assertTrue("Location should be non-null", location != null);
                assertTrue("Location latitude should match!", location.getLatitude().equals(location_parts[0]));
                assertTrue("Location longitude should match!", location.getLongitude().equals(location_parts[1]));
            }
        }
    }
    private HashMap<String,String> createSelection(String[] location)
    {
        HashMap<String,String> selection = new HashMap<>();
        if (location[0] != null) {
            selection.put(COLUMN_CONFIG_LATITUDE, location[0]);
        }
        if (location[1] != null) {
            selection.put(COLUMN_CONFIG_LONGITUDE, location[1]);
        }
        if (location[2] != null) {
            selection.put(COLUMN_CONFIG_ALTITUDE, location[2]);
        }
        return selection;
    }

    @Test
    public void test_parseDateRange()
    {
        Calendar start0 = TEST_DATE0;
        Calendar end0 = Calendar.getInstance();
        end0.setTimeInMillis(TEST_DATE0.getTimeInMillis());
        end0.add(Calendar.DAY_OF_MONTH, 1);
        String segment0 = start0.getTimeInMillis() + "-" + end0.getTimeInMillis();

        long[] range0 = CalculatorProvider.parseDateRange(segment0);
        assertTrue("range should be non-null", range0 != null);
        assertTrue("range should have length of 2", range0.length == 2);
        //assertTrue("startDate should be non-null", range0[0] != null);
        //assertTrue("endDate should be non-null", range0[1] != null);
        assertTrue("startDate should match", range0[0] == start0.getTimeInMillis());
        assertTrue("endDate should match (+1000)", range0[1] == (end0.getTimeInMillis()));

        // TODO: test against invalid ranges
    }

    @Test
    public void test_parseYearRange()
    {
        Calendar now = Calendar.getInstance();
        String[] TEST_SEGMENTS = new String[] {
                "2018-2020",            // valid
                "2018-2010",            // order
                "2018-",                // missing parts
                "-2020",                // missing parts
                "-",                    // missing parts
                "",                     // missing parts
                "number-number"         // not-a-number
        };

        for (String segment : TEST_SEGMENTS)
        {
            int startYear = TEST_DATE0.get(Calendar.YEAR);
            int endYear = startYear;
            String[] parts = segment.split("-");
            if (parts.length == 2) {
                try {
                    startYear = Integer.parseInt(parts[0]);
                    endYear = Integer.parseInt(parts[1]);

                } catch (NumberFormatException e) {
                    startYear = now.get(Calendar.YEAR);
                    endYear = now.get(Calendar.YEAR);
                }
            } else {
                startYear = now.get(Calendar.YEAR);
                endYear = now.get(Calendar.YEAR);
            }

            long[] range = CalculatorProvider.parseYearRange(segment);
            assertTrue("range should be non-null", range != null);
            assertTrue("range should have length of 2", range.length == 2);
            //assertTrue("startYear should be non-null", range[0] != null);
            //assertTrue("endYear should be non-null", range[1] != null);
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(range[0]);
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(range[1]);

            assertTrue(segment + " :: startYear should match: " + start.get(Calendar.YEAR) + " != " + startYear, start.get(Calendar.YEAR) == startYear);
            assertTrue(segment + " :: endYear should match: " + end.get(Calendar.YEAR) + " != " + endYear, end.get(Calendar.YEAR) == endYear);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void test_cursorHasColumns(@NonNull String tag, @Nullable Cursor cursor, @NonNull String[] projection)
    {
        assertTrue(tag + " should return non-null cursor.", cursor != null);
        assertTrue(tag + " should have same number of columns as the projection", cursor.getColumnCount() == projection.length);
        assertTrue("QUERY_MOONPHASE should return one or more rows.", cursor.getCount() >= 1);
        cursor.moveToFirst();
        for (String column : projection) {
            assertTrue(tag + " results should contain " + column, cursor.getColumnIndex(column) >= 0);
        }
    }

    private void test_allColumnsLong(String tag, Cursor cursor, String[] columns)
    {
        if (cursor != null)
        {
            cursor.moveToFirst();
            boolean allColumnsLong = true;
            for (String column : columns)
            {
                boolean isLong = columnIsLong(cursor, column);
                allColumnsLong = (allColumnsLong && isLong);

                if (!isLong) {
                    Log.w(tag, column + " is not long!");
                }
            }
            assertTrue("all columns should contain long!", allColumnsLong);
        }
    }

    private void test_allColumnsDouble(String tag, Cursor cursor, String[] columns)
    {
        if (cursor != null)
        {
            cursor.moveToFirst();
            boolean allColumnsDouble = true;
            for (String column : columns)
            {
                boolean isDouble = columnIsDouble(cursor, column);
                allColumnsDouble = (allColumnsDouble && isDouble);

                if (!isDouble) {
                    Log.w(tag, column + " is not double!");
                }
            }
            assertTrue("all columns should contain double!", allColumnsDouble);
        }
    }

    private boolean columnIsInt(Cursor cursor, String column)
    {
        if (cursor != null) {
            try {
                int index = cursor.getColumnIndex(column);
                if (cursor.getType(index) == Cursor.FIELD_TYPE_INTEGER);
                {
                    int value = cursor.getInt(index);
                    return true;
                }

            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private boolean columnIsDouble(Cursor cursor, String column)
    {
        if (cursor != null) {
            try {
                int index = cursor.getColumnIndex(column);
                if (cursor.getType(index) == Cursor.FIELD_TYPE_FLOAT)
                {
                    double value = cursor.getDouble(index);
                    return true;
                }

            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private boolean columnIsLong(Cursor cursor, String column)
    {
        if (cursor != null) {
            try {
                long value = cursor.getLong(cursor.getColumnIndex(column));
            } catch (NumberFormatException e) {
                Log.d("DEBUG", "columnIsLong: not a long .. " + column);
                return false;
            }
        }
        return true;
    }

    private void test_projectionHasUniqueColumns(String[] projection)
    {
        Set<String> uniqueColumns = new HashSet<>();
        for (String column : projection) {
            assertTrue("Column names are not unique! \"" + column + "\" is used more than once.", !uniqueColumns.contains(column));
            uniqueColumns.add(column);
        }
    }

    private void test_projectionContainsColumn(String column, List<String> projection) {
        assertTrue("projection contains " + column, projection.contains(column));
    }

    private void test_dateUTC(String tag, long timestamp, int year, int month, int day, int hour, int minute)
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp);
        assertTrue(tag + " should match " + year + " " + month + " " + day + " " + hour + " " + minute + " :: " + calendar.toString(),
                calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.DAY_OF_MONTH) == day
                        && calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) == minute);
    }

}
