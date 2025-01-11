/**
    Copyright (C) 2022 Forrest Guice
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

import android.database.Cursor;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;

import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.Location;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APPWIDGETID;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_TEXT_SIZE;
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
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.COLUMN_SEASON_SUMMER;
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
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_CONFIG_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOONPHASE_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOONPOS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_MOON_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SEASONS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SUNPOS_PROJECTION;
import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.QUERY_SUN_PROJECTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalculatorProviderTest0
{
    private Calendar TEST_DATE0;

    @Before
    public void setup()
    {
        TEST_DATE0 = Calendar.getInstance();
        TEST_DATE0.set(2018, 0, 1, 0, 0, 0);
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

    /**
     * test_query_config_projection
     */
    @Test
    public void test_query_config_projection()
    {
        String[] TEST_CONFIG_PROJECTION = new String[] {
                COLUMN_CONFIG_APP_VERSION, COLUMN_CONFIG_APP_VERSION_CODE,
                COLUMN_CONFIG_PROVIDER_VERSION, COLUMN_CONFIG_PROVIDER_VERSION_CODE,
                COLUMN_CONFIG_LOCALE, COLUMN_CONFIG_APP_TEXT_SIZE, COLUMN_CONFIG_APP_THEME,
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

    @Test
    public void test_Calendar_getYear()
    {
        int[] years = new int[] { 2000, 250, 1 };
        for (int year : years) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            assertEquals(year, c.get(Calendar.YEAR));
        }
    }

    /*@Test
    public void test_Calendar_getYear1()
    {
        Calendar c0 = Calendar.getInstance();
        c0.set(Calendar.YEAR, 0);
        assertEquals(0, c0.get(Calendar.YEAR));
    }*/

    @Test
    public void test_SuntimesData_nowThen()
    {
        test_SuntimesData_nowThen(1863, Calendar.JULY, 4, 12, 30, 1);
        test_SuntimesData_nowThen(-1000, Calendar.JULY, 4, 12, 30, 1);
    }

    public void test_SuntimesData_nowThen(int year, int month, int dayOfMonth, int hour, int minute, int second)
    {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, hour);
        now.set(Calendar.MINUTE, minute);
        now.set(Calendar.SECOND, second);

        Calendar c0 = Calendar.getInstance();
        c0.set(Calendar.YEAR, year);
        c0.set(Calendar.MONTH, month);
        c0.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        Calendar c1 = SuntimesData.nowThen(now, c0);
        assertEquals(c1.get(Calendar.YEAR), c1.get(Calendar.YEAR));
        assertEquals(month, c1.get(Calendar.MONTH));
        assertEquals(dayOfMonth, c1.get(Calendar.DAY_OF_MONTH));

        assertEquals(hour, c1.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, c1.get(Calendar.MINUTE));
        assertEquals(second, c1.get(Calendar.SECOND));
    }

}
