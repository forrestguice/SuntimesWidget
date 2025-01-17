/**
    Copyright (C) 2020 Forrest Guice
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

package com.forrestguice.suntimeswidget.themes;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.AUTHORITY;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEME;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEMES;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEMES_PROJECTION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEME_PROJECTION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ACCENTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ACTIONCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ASTROCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_BACKGROUND;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_BACKGROUND_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_CIVILCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_DAYCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_DISPLAYSTRING;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_FALLCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_GRAPH_POINT_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_GRAPH_POINT_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ISDEFAULT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MAP_BACKGROUNDCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MAP_FOREGROUNDCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MAP_HIGHLIGHTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MAP_SHADOWCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONFULLCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONFULL_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONNEWCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONNEW_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONRISECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONSETCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONWANINGCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOONWAXINGCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NAME;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NAUTICALCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NIGHTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_BOTTOM;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_LEFT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_RIGHT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_TOP;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PROVIDER_VERSION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SPRINGCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUMMERCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUNRISECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUNSETCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTSIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMEBOLD;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLEBOLD;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLESIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_VERSION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_WINTERCOLOR;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SuntimesThemeProviderTest
{
    private Context mockContext;

    @Before
    public void setup() {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
        WidgetThemes.initThemes(mockContext);
    }

    @Test
    public void test_columns()
    {
        List<String> columns0 = new ArrayList<>();
        Collections.addAll(columns0, QUERY_THEME_PROJECTION);
        test_projectionHasUniqueColumns(columns0.toArray(new String[columns0.size()]));

        List<String> columns1 = new ArrayList<>();
        Collections.addAll(columns1, QUERY_THEMES_PROJECTION);
        test_projectionHasUniqueColumns(columns1.toArray(new String[columns1.size()]));

        test_query_theme_projection();
        test_query_themes_projection();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void test_query_theme_projection()
    {
        String[] TEST_THEMES_PROJECTION = new String[] {
                THEME_PROVIDER_VERSION, THEME_PROVIDER_VERSION_CODE,
                THEME_NAME, THEME_VERSION, THEME_ISDEFAULT, THEME_DISPLAYSTRING,
                THEME_BACKGROUND, THEME_BACKGROUND_COLOR,
                THEME_PADDING_LEFT, THEME_PADDING_TOP, THEME_PADDING_RIGHT, THEME_PADDING_BOTTOM,
                THEME_TEXTCOLOR, THEME_TITLECOLOR, THEME_TIMECOLOR, THEME_TIMESUFFIXCOLOR, THEME_ACTIONCOLOR, THEME_ACCENTCOLOR,
                THEME_SUNRISECOLOR, THEME_NOONCOLOR, THEME_SUNSETCOLOR,
                THEME_MOONRISECOLOR, THEME_MOONSETCOLOR, THEME_MOONWANINGCOLOR, THEME_MOONWAXINGCOLOR, THEME_MOONNEWCOLOR, THEME_MOONFULLCOLOR,
                THEME_MOONFULL_STROKE_WIDTH, THEME_MOONNEW_STROKE_WIDTH,
                THEME_NOONICON_FILL_COLOR, THEME_NOONICON_STROKE_COLOR, THEME_NOONICON_STROKE_WIDTH,
                THEME_RISEICON_FILL_COLOR, THEME_RISEICON_STROKE_COLOR, THEME_RISEICON_STROKE_WIDTH,
                THEME_SETICON_FILL_COLOR, THEME_SETICON_STROKE_COLOR, THEME_SETICON_STROKE_WIDTH,
                THEME_DAYCOLOR, THEME_CIVILCOLOR, THEME_NAUTICALCOLOR, THEME_ASTROCOLOR, THEME_NIGHTCOLOR, THEME_GRAPH_POINT_FILL_COLOR, THEME_GRAPH_POINT_STROKE_COLOR,
                THEME_SPRINGCOLOR, THEME_SUMMERCOLOR, THEME_FALLCOLOR, THEME_WINTERCOLOR,
                THEME_MAP_BACKGROUNDCOLOR, THEME_MAP_FOREGROUNDCOLOR, THEME_MAP_SHADOWCOLOR, THEME_MAP_HIGHLIGHTCOLOR,
                THEME_TITLESIZE, THEME_TITLEBOLD, THEME_TEXTSIZE, THEME_TIMESIZE, THEME_TIMEBOLD, THEME_TIMESUFFIXSIZE
        };

        List<String> projection = Arrays.asList(QUERY_THEME_PROJECTION);
        for (String column : TEST_THEMES_PROJECTION) {
            test_projectionContainsColumn(column, projection);
        }
    }

    /**
     * test_query_theme
     */
    @Test
    public void test_query_theme()
    {
        String[] projection = QUERY_THEME_PROJECTION;
        test_query_theme_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertNotNull(resolver);

        String themeName0 = "dark";
        SuntimesTheme oracle0 = WidgetThemes.loadTheme(mockContext, themeName0);
        Uri uri0 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_THEME + "/" + themeName0);
        Cursor cursor0 = resolver.query(uri0, projection, null, null, null);
        test_cursorHasColumns("QUERY_THEME", cursor0, projection);
        test_theme(cursor0, oracle0);
        cursor0.close();

        String themeName1 = "light";
        SuntimesTheme oracle1 = WidgetThemes.loadTheme(mockContext, themeName1);
        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_THEME + "/" + themeName1);
        Cursor cursor1 = resolver.query(uri1, projection, null, null, null);
        test_cursorHasColumns("QUERY_THEME", cursor1, projection);
        test_theme(cursor1, oracle1);
        cursor1.close();
    }

    private void test_theme(Cursor cursor) {
        test_theme(cursor, null);
    }
    private void test_theme(Cursor cursor, @Nullable SuntimesTheme oracle)
    {
        assertTrue("THEME_PROVIDER_VERSION " + THEME_PROVIDER_VERSION, cursor.getString(cursor.getColumnIndex(THEME_PROVIDER_VERSION)).startsWith(SuntimesThemeContract.VERSION_NAME));
        assertEquals("THEME_PROVIDER_VERSION_CODE", cursor.getInt(cursor.getColumnIndex(THEME_PROVIDER_VERSION_CODE)), SuntimesThemeContract.VERSION_CODE);

        if (oracle != null)
        {
            assertEquals("THEME_NAME", cursor.getString(cursor.getColumnIndex(THEME_NAME)), oracle.themeName());
            assertEquals("THEME_VERSION", cursor.getInt(cursor.getColumnIndex(THEME_VERSION)), oracle.themeVersion());
            assertEquals("THEME_ISDEFAULT", (cursor.getInt(cursor.getColumnIndex(THEME_ISDEFAULT)) == 1), oracle.isDefault());
            assertEquals("THEME_DISPLAYSTRING", cursor.getString(cursor.getColumnIndex(THEME_DISPLAYSTRING)), oracle.themeDisplayString());

            // TODO: THEME_BACKGROUND
            assertEquals("THEME_BACKGROUND_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_BACKGROUND_COLOR)), oracle.getBackgroundColor());
            assertEquals("THEME_PADDING_LEFT", cursor.getInt(cursor.getColumnIndex(THEME_PADDING_LEFT)), oracle.getPadding()[0]);
            assertEquals("THEME_PADDING_TOP", cursor.getInt(cursor.getColumnIndex(THEME_PADDING_TOP)), oracle.getPadding()[1]);
            assertEquals("THEME_PADDING_RIGHT", cursor.getInt(cursor.getColumnIndex(THEME_PADDING_RIGHT)), oracle.getPadding()[2]);
            assertEquals("THEME_PADDING_BOTTOM", cursor.getInt(cursor.getColumnIndex(THEME_PADDING_BOTTOM)), oracle.getPadding()[3]);

            assertEquals("THEME_TEXTCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_TEXTCOLOR)), oracle.getTextColor());
            assertEquals("THEME_TITLECOLOR", cursor.getInt(cursor.getColumnIndex(THEME_TITLECOLOR)), oracle.getTitleColor());
            assertEquals("THEME_TIMECOLOR", cursor.getInt(cursor.getColumnIndex(THEME_TIMECOLOR)), oracle.getTimeColor());
            assertEquals("THEME_TIMESUFFIXCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_TIMESUFFIXCOLOR)), oracle.getTimeSuffixColor());
            assertEquals("THEME_ACTIONCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_ACTIONCOLOR)), oracle.getActionColor());
            assertEquals("THEME_ACCENTCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_ACCENTCOLOR)), oracle.getAccentColor());

            // TODO: THEME_TITLESIZE, THEME_TEXTSIZE, THEME_TIMESIZE, THEME_TIMESUFFIXSIZE

            assertEquals("THEME_TITLEBOLD", cursor.getInt(cursor.getColumnIndex(THEME_TITLEBOLD)) == 1, oracle.getTitleBold());
            assertEquals("THEME_TIMEBOLD", cursor.getInt(cursor.getColumnIndex(THEME_TITLEBOLD)) == 1, oracle.getTitleBold());

            assertEquals("THEME_MOONRISECOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MOONRISECOLOR)), oracle.getMoonriseTextColor());
            assertEquals("THEME_MOONSETCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MOONSETCOLOR)), oracle.getMoonsetTextColor());

            assertEquals("THEME_MOONWANINGCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MOONWANINGCOLOR)), oracle.getMoonWaningColor());
            assertEquals("THEME_MOONWAXINGCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MOONWAXINGCOLOR)), oracle.getMoonWaxingColor());
            assertEquals("THEME_MOONNEWCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MOONNEWCOLOR)), oracle.getMoonNewColor());
            assertEquals("THEME_MOONFULLCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MOONFULLCOLOR)), oracle.getMoonFullColor());

            // TODO: THEME_MOONFULL_STROKE_WIDTH, THEME_MOONNEW_STROKE_WIDTH,

            assertEquals("THEME_NOONICON_FILL_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_NOONICON_FILL_COLOR)), oracle.getNoonIconColor());
            assertEquals("THEME_NOONICON_STROKE_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_NOONICON_STROKE_COLOR)), oracle.getNoonIconStrokeColor());
            // TODO: THEME_NOONICON_STROKE_WIDTH,

            assertEquals("THEME_RISEICON_FILL_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_RISEICON_FILL_COLOR)), oracle.getSunriseIconColor());
            assertEquals("THEME_RISEICON_STROKE_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_RISEICON_STROKE_COLOR)), oracle.getSunriseIconStrokeColor());
            // TODO: THEME_RISEICON_STROKE_WIDTH

            assertEquals("THEME_SETICON_FILL_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_SETICON_FILL_COLOR)), oracle.getSunsetIconColor());
            assertEquals("THEME_SETICON_STROKE_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_SETICON_STROKE_COLOR)), oracle.getSunsetIconStrokeColor());
            // TODO: THEME_SETICON_STROKE_WIDTH

            assertEquals("THEME_SUNRISECOLOR", cursor.getInt(cursor.getColumnIndex(THEME_SUNRISECOLOR)), oracle.getSunriseTextColor());
            assertEquals("THEME_NOONCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_NOONCOLOR)), oracle.getNoonTextColor());
            assertEquals("THEME_SUNSETCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_SUNSETCOLOR)), oracle.getSunsetTextColor());

            assertEquals("THEME_DAYCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_DAYCOLOR)), oracle.getDayColor());
            assertEquals("THEME_CIVILCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_CIVILCOLOR)), oracle.getCivilColor());
            assertEquals("THEME_NAUTICALCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_NAUTICALCOLOR)), oracle.getNauticalColor());
            assertEquals("THEME_ASTROCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_ASTROCOLOR)), oracle.getAstroColor());
            assertEquals("THEME_NIGHTCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_NIGHTCOLOR)), oracle.getNightColor());
            assertEquals("THEME_GRAPH_POINT_FILL_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_GRAPH_POINT_FILL_COLOR)), oracle.getGraphPointFillColor());
            assertEquals("THEME_GRAPH_POINT_STROKE_COLOR", cursor.getInt(cursor.getColumnIndex(THEME_GRAPH_POINT_STROKE_COLOR)), oracle.getGraphPointStrokeColor());

            assertEquals("THEME_MAP_BACKGROUNDCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MAP_BACKGROUNDCOLOR)), oracle.getMapBackgroundColor());
            assertEquals("THEME_MAP_FOREGROUNDCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MAP_FOREGROUNDCOLOR)), oracle.getMapForegroundColor());
            assertEquals("THEME_MAP_SHADOWCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MAP_SHADOWCOLOR)), oracle.getMapShadowColor());
            assertEquals("THEME_MAP_HIGHLIGHTCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_MAP_HIGHLIGHTCOLOR)), oracle.getMapHighlightColor());

            assertEquals("THEME_SPRINGCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_SPRINGCOLOR)), oracle.getSpringColor());
            assertEquals("THEME_SUMMERCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_SUMMERCOLOR)), oracle.getSummerColor());
            assertEquals("THEME_FALLCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_FALLCOLOR)), oracle.getFallColor());
            assertEquals("THEME_WINTERCOLOR", cursor.getInt(cursor.getColumnIndex(THEME_WINTERCOLOR)), oracle.getWinterColor());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * test_query_themes_projection
     */
    @Test
    public void test_query_themes_projection()
    {
        String[] TEST_THEMES_PROJECTION = new String[] {
                THEME_PROVIDER_VERSION, THEME_PROVIDER_VERSION_CODE,
                THEME_NAME, THEME_VERSION, THEME_ISDEFAULT, THEME_DISPLAYSTRING
        };

        List<String> projection = Arrays.asList(QUERY_THEMES_PROJECTION);
        for (String column : TEST_THEMES_PROJECTION) {
            test_projectionContainsColumn(column, projection);
        }
    }

    /**
     * test_query_themes
     */
    @Test
    public void test_query_themes()
    {
        test_query_themes_projection();

        ContentResolver resolver = mockContext.getContentResolver();
        assertNotNull(resolver);

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_THEMES);
        String[] projection = QUERY_THEMES_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_THEMES", cursor, projection);
        test_theme(cursor);

        cursor.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void test_cursorHasColumns(@NonNull String tag, @Nullable Cursor cursor, @NonNull String[] projection)
    {
        assertTrue(tag + " should return non-null cursor.", cursor != null);
        assertTrue(tag + " should have same number of columns as the projection", cursor.getColumnCount() == projection.length);
        assertTrue(tag + " should return one or more rows.", cursor.getCount() >= 1);
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

}
