/**
    Copyright (C) 2018 Forrest Guice
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

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.AUTHORITY;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_APPTHEME;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_APPWIDGETID;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_CALCULATOR;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_CALCULATOR_FEATURES;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_LATITUDE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_LOCALE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_CONFIG_TIMEZONE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOONPOS_ALT;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOONPOS_AZ;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOONPOS_DATE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOONPOS_DEC;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOONPOS_ILLUMINATION;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOONPOS_RA;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_FIRST;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_FULL;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_NEW;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_RISE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_SET;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_THIRD;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_AUTUMN;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_SUMMER;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_VERNAL;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_WINTER;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_YEAR;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUNPOS_ALT;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUNPOS_AZ;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUNPOS_DATE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUNPOS_DEC;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUNPOS_ISDAY;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUNPOS_RA;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_ASTRO_RISE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_ASTRO_SET;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_BLUE4_RISE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_BLUE4_SET;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_BLUE8_RISE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_BLUE8_SET;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_CIVIL_RISE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_CIVIL_SET;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_GOLDEN_EVENING;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_GOLDEN_MORNING;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_NAUTICAL_RISE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_NAUTICAL_SET;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SUN_NOON;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_CONFIG;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_CONFIG_PROJECTION;

import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_MOON;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_MOONPHASE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_MOONPHASE_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_MOONPOS;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_MOONPOS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_MOON_PROJECTION;

import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_SEASONS;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_SEASONS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_SUN;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_SUNPOS;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_SUNPOS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_SUN_PROJECTION;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CalculatorProviderTest
{
    private Context mockContext;

    @Before
    public void setup()
    {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
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
        List<String> projection = Arrays.asList(QUERY_CONFIG_PROJECTION);
        assertTrue("default projection contains COLUMN_CONFIG_CALCULATOR", projection.contains(COLUMN_CONFIG_CALCULATOR));
        assertTrue("default projection contains COLUMN_CONFIG_CALCULATOR_FEATURES", projection.contains(COLUMN_CONFIG_CALCULATOR_FEATURES));
        assertTrue("default projection contains COLUMN_CONFIG_APPWIDGETID", projection.contains(COLUMN_CONFIG_APPWIDGETID));
        assertTrue("default projection contains COLUMN_CONFIG_APPTHEME", projection.contains(COLUMN_CONFIG_APPTHEME));
        assertTrue("default projection contains COLUMN_CONFIG_LOCALE", projection.contains(COLUMN_CONFIG_LOCALE));
        assertTrue("default projection contains COLUMN_CONFIG_LATITUDE", projection.contains(COLUMN_CONFIG_LATITUDE));
        assertTrue("default projection contains COLUMN_CONFIG_LONGITUDE", projection.contains(COLUMN_CONFIG_LONGITUDE));
        assertTrue("default projection contains COLUMN_CONFIG_ALTITUDE", projection.contains(COLUMN_CONFIG_ALTITUDE));
        assertTrue("default projection contains COLUMN_CONFIG_TIMEZONE", projection.contains(COLUMN_CONFIG_TIMEZONE));
        test_projectionHasUniqueColumns(QUERY_CONFIG_PROJECTION);
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

        int appWidgetID = 0;
        SuntimesCalculatorDescriptor descriptor = WidgetSettings.loadCalculatorModePref(mockContext, appWidgetID);
        assertTrue("COLUMN_CONFIG_CALCULATOR should be " + descriptor.getName(), descriptor.getName().equals(cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_CALCULATOR))));

        WidgetSettings.Location location = WidgetSettings.loadLocationPref(mockContext, appWidgetID);
        assertTrue("COLUMN_CONFIG_APPWIDGETID should be " + appWidgetID, cursor.getInt(cursor.getColumnIndex(COLUMN_CONFIG_APPWIDGETID)) == appWidgetID);
        assertTrue("COLUMN_CONFIG_LATITUDE should be " + location.getLatitude(), cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_LATITUDE)).equals(location.getLatitude()));
        assertTrue("COLUMN_CONFIG_LONGITUDE should be " + location.getLongitude(), cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_LONGITUDE)).equals(location.getLongitude()));
        assertTrue("COLUMN_CONFIG_ALTITUDE should be " + location.getAltitude(), cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_ALTITUDE)).equals(location.getAltitude()));

        String timezone = WidgetSettings.loadTimezonePref(mockContext, appWidgetID);
        assertTrue("COLUMN_CONFIG_TIMEZONE should be " + timezone, cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_TIMEZONE)).equals(timezone));

        String appTheme = AppSettings.loadThemePref(mockContext);
        assertTrue("COLUMN_CONFIG_APPTHEME should be " + appTheme, cursor.getString(cursor.getColumnIndex(COLUMN_CONFIG_APPTHEME)).equals(appTheme));

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

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SEASONS);
        String[] projection = QUERY_SEASONS_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_SEASONS", cursor, projection);

        // TODO

        if (cursor != null) {
            cursor.close();
        }
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

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SUN);
        String[] projection = QUERY_SUN_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_SUN", cursor, projection);

        // TODO

        if (cursor != null) {
            cursor.close();
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

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SUNPOS);
        String[] projection = QUERY_SUNPOS_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_SUNPOS", cursor, projection);

        // TODO

        if (cursor != null) {
            cursor.close();
        }
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

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOON);
        String[] projection = QUERY_MOON_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_MOON", cursor, projection);

        // TODO

        if (cursor != null) {
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

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPOS);
        String[] projection = QUERY_MOONPOS_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_MOONPOS", cursor, projection);

        // TODO

        if (cursor != null) {
            cursor.close();
        }
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

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPHASE);
        String[] projection = QUERY_MOONPHASE_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_MOONPHASE", cursor, projection);

        // TODO

        if (cursor != null) {
            cursor.close();
        }
    }

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

    private void test_projectionHasUniqueColumns(String[] projection)
    {
        Set<String> uniqueColumns = new HashSet<>();
        for (String column : projection) {
            assertTrue("Column names are not unique! \"" + column + "\" is used more than once.", !uniqueColumns.contains(column));
            uniqueColumns.add(column);
        }
    }

}
