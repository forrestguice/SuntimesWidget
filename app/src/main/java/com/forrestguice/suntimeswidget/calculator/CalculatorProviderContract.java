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

/**
 * CalculatorProviderContract
 * @version 0.1.0
 *
 * ------------------------------------------------------------------------------------------------
 * CONFIG
 *   The following URIs are supported:
 *       content://suntimeswidget.calculator.provider/config                         .. get the calculator config
 *
 *   The result will be one row containing:
 *       [COLUMN_LATITUDE(double), COLUMN_LONGITUDE(double), COLUMN_ALTITUDE(double), COLUMN_TIMEZONE(String), COLUMN_LOCALE(String), COLUMN_APPTHEME(String) ]
 *
 * ------------------------------------------------------------------------------------------------*
 * SUN
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/sun                     .. get todays sun (rise, set, twilights)
 *       content://suntimeswiget.calculator.provider/sun/[millis]            .. get upcoming sun (timestamp)
 *       content://suntimeswiget.calculator.provider/sun/[millis]-[millis]   .. get upcoming sun for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *      COLUMN_SUN_ACTUAL_RISE, COLUMN_SUN_ACTUAL_SET,
 *      COLUMN_SUN_CIVIL_RISE, COLUMN_SUN_CIVIL_SET,
 *      COLUMN_SUN_NAUTICAL_RISE, COLUMN_SUN_NAUTICAL_SET,
 *      COLUMN_SUN_ASTRO_RISE, COLUMN_SUN_ASTRO_SET,COLUMN_SUN_NOON,
 *      COLUMN_SUN_GOLDEN_RISE, COLUMN_SUN_GOLDEN_SET,
 *      COLUMN_SUN_BLUE8_RISE, COLUMN_SUN_BLUE8_SET,
 *      COLUMN_SUN_BLUE4_RISE, COLUMN_SUN_BLUE4_SET
 *
 * ------------------------------------------------------------------------------------------------*
 * MOON
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moon                     .. get todays moon (rise, set)
 *       content://suntimeswiget.calculator.provider/moon/[millis]            .. get upcoming moon (timestamp)
 *       content://suntimeswiget.calculator.provider/moon/[millis]-[millis]   .. get upcoming moon for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *       [COLUMN_MOON_RISE(long), COLUMN_MOON_SET(long)]
 *
 * ------------------------------------------------------------------------------------------------*
 * MOON PHASES
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moon/phases                     .. get upcoming moon phases
 *       content://suntimeswiget.calculator.provider/moon/phases/[millis]            .. get upcoming moon phases after date (timestamp)
 *       content://suntimeswiget.calculator.provider/moon/phases/[millis]-[millis]   .. get upcoming moon phases for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *       [COLUMN_MOON_NEW(long), COLUMN_MOON_FIRST(long), COLUMN_MOON_FULL(long), COLUMN_MOON_THIRD(long)]
 *
 * ------------------------------------------------------------------------------------------------
 * SOLSTICE / EQUINOX
 *   The following URIs are supported:
 *       content://suntimeswidget.calculator.provider/seasons                         .. get vernal, summer, autumn, and winter dates for this year
 *       content://suntimeswidget.calculator.provider/seasons/[year]                  .. get vernal, summer, autumn, and winter dates for some year
 *       content://suntimeswidget.calculator.provider/seasons/[year]-[year]           .. get vernal, summer, autumn, and winter dates for range
 *
 *   The result will be one or more rows containing:
 *       [COLUMN_YEAR(int), COLUMM_SEASON_VERNAL(long), COLUMN_SEASON_SUMMER(long), COLUMN_SEASON_AUTUMN(long), COLUMN_SEASON_WINTER(long)]
 *
 * ------------------------------------------------------------------------------------------------
 * Example 1:
 *     String[] projection = new String[] {COLUMN_MOON_NEW, COLUMN_MOON_FIRST, COLUMN_MOON_FULL, COLUMN_MOON_THIRD };*
 *     Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPHASE + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());*
 *     Cursor cursor = resolver.query(uri, projection, null, null, null);
 *     if (cursor != null {
 *         cursor.moveToFirst();
 *         while (!cursor.isAfterLast()) {
 *             Long fullMoonTimeMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_MOON_FULL));   // possibly null..
 *             if (fullMoonTimeMillis != null) {                                                    // if column is not part of or is missing from the projection
 *                 Calendar fullMoonCalendar = Calendar.getInstance();
     *             fullMoonCalendar.setTimeInMillis(fullMoonTimeMillis); *
 *             }
 *             cursor.moveToNext();
 *         }
 *         cursor.close();
 *     }
 *
 * ------------------------------------------------------------------------------------------------
 * WIDGETS
 *   To specify a widget configuration you can provide a `selection` arg that specifies the
 *   appWidgetID. If omitted all URIs default to 0 (the app configuration).
 *
 *     String[] projection = ...   // see example
 *     Uri uri = ...
 *
 *     int appWidgetID = 1000;
 *     String selection = COLUMN_CONFIG_APPWIDGETID + "=?";
 *     String[] selectionArgs = new String[] { appWidgetID };
 *
 *     Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
 *     if (cursor != null) {
 *         // see example 1
 *     }
 *
 * ------------------------------------------------------------------------------------------------
 * LOCATION & TIMEZONE
 *   It is also possible to override the location and timezone using `selection` args.
 *
 *     String[] projection = ...   // see example
 *     Uri uri = ...
 *     String selection = COLUMN_CONFIG_LATITUDE + "=? AND "
 *                      + COLUMN_CONFIG_LONGITUDE + "=? AND "
 *                      + COLUMN_CONFIG_TIMEZONE + "=?";
 *     String[] selectionArgs = new String[] {"33.45", "-111.94", "US/Arizona"};
 *     Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
 */
public interface CalculatorProviderContract
{
    String AUTHORITY = "suntimeswidget.calculator.provider";

    /**
     * CONFIG
     */
    String COLUMN_CONFIG_APPTHEME = "config_apptheme";
    String COLUMN_CONFIG_LOCALE = "config_locale";
    String COLUMN_CONFIG_LATITUDE = "latitude";
    String COLUMN_CONFIG_LONGITUDE = "longitude";
    String COLUMN_CONFIG_ALTITUDE = "altitude";
    String COLUMN_CONFIG_TIMEZONE = "timezone";
    String COLUMN_CONFIG_APPWIDGETID = "appwidgetid";

    String QUERY_CONFIG = "config";
    String[] QUERY_CONFIG_PROJECTION = new String[] {
            COLUMN_CONFIG_LOCALE, COLUMN_CONFIG_APPTHEME,
            COLUMN_CONFIG_LATITUDE, COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE,
            COLUMN_CONFIG_TIMEZONE, COLUMN_CONFIG_APPWIDGETID
    };

    /**
     * ISDAY
     */
    String COLUMN_ISDAY = "isday";
    String COLUMN_ISDAY_DATETIME = "isday_datetime";

    String QUERY_ISDAY = "isday";
    String[] QUERY_ISDAY_PROJECTION = new String[] {
            COLUMN_ISDAY, COLUMN_ISDAY_DATETIME
    };

    /**
     * SUN
     */
    String COLUMN_SUN_NOON = "solarnoon";
    String COLUMN_SUN_ACTUAL_RISE = "sunrise";
    String COLUMN_SUN_ACTUAL_SET = "sunset";

    String COLUMN_SUN_CIVIL_RISE = "civilrise";
    String COLUMN_SUN_CIVIL_SET = "civilset";
    String COLUMN_SUN_NAUTICAL_RISE = "nauticalrise";
    String COLUMN_SUN_NAUTICAL_SET = "nauticalset";
    String COLUMN_SUN_ASTRO_RISE = "astrorise";
    String COLUMN_SUN_ASTRO_SET = "astroset";

    String COLUMN_SUN_GOLDEN_MORNING = "goldenmorning";
    String COLUMN_SUN_GOLDEN_EVENING = "goldenevening";

    String COLUMN_SUN_BLUE8_RISE = "blue8rise";      // morning blue hour
    String COLUMN_SUN_BLUE4_RISE = "blue4rise";
    String COLUMN_SUN_BLUE8_SET = "blue8set";        // evening blue hour
    String COLUMN_SUN_BLUE4_SET = "blue4set";

    String QUERY_SUN = "sun";
    String[] QUERY_SUN_PROJECTION = new String[] {
            COLUMN_SUN_ACTUAL_RISE, COLUMN_SUN_ACTUAL_SET,
            COLUMN_SUN_CIVIL_RISE, COLUMN_SUN_CIVIL_SET,
            COLUMN_SUN_NAUTICAL_RISE, COLUMN_SUN_NAUTICAL_SET,
            COLUMN_SUN_ASTRO_RISE, COLUMN_SUN_ASTRO_SET,
            COLUMN_SUN_NOON,
            COLUMN_SUN_GOLDEN_MORNING, COLUMN_SUN_GOLDEN_EVENING,
            COLUMN_SUN_BLUE8_RISE, COLUMN_SUN_BLUE8_SET,
            COLUMN_SUN_BLUE4_RISE, COLUMN_SUN_BLUE4_SET
    };

    /**
     * SUNPOS
     */
    String COLUMN_SUNPOS_AZ = "sunpos_azimuth";
    String COLUMN_SUNPOS_ALT = "sunpos_altitude";
    String COLUMN_SUNPOS_RA = "sunpos_ra";
    String COLUMN_SUNPOS_DEC = "sunpos_dec";

    String QUERY_SUNPOS = "sunpos";
    String[] QUERY_SUNPOS_PROJECTION = new String[] {
            COLUMN_SUNPOS_AZ, COLUMN_SUNPOS_ALT,
            COLUMN_SUNPOS_RA, COLUMN_SUNPOS_DEC
    };

    /**
     * MOON
     */
    String COLUMN_MOON_RISE = "moonrise";
    String COLUMN_MOON_SET = "moonset";

    String QUERY_MOON = "moon";
    String[] QUERY_MOON_PROJECTION = new String[] { COLUMN_MOON_RISE, COLUMN_MOON_SET };

    /**
     * MOONPOS
     */
    String COLUMN_MOONPOS_AZ = "moonpos_azimuth";
    String COLUMN_MOONPOS_ALT = "moonpos_altitude";
    String COLUMN_MOONPOS_RA = "moonpos_ra";
    String COLUMN_MOONPOS_DEC = "moonpos_dec";

    String QUERY_MOONPOS = "moonpos";
    String[] QUERY_MOONPOS_PROJECTION = new String[] {
            COLUMN_MOONPOS_AZ, COLUMN_MOONPOS_ALT,
            COLUMN_MOONPOS_RA, COLUMN_MOONPOS_DEC
    };

    /**
     * MOONPHASE
     */
    String COLUMN_MOON_NEW = "moonphase_new";
    String COLUMN_MOON_FIRST = "moonphase_first";
    String COLUMN_MOON_FULL = "moonphase_full";
    String COLUMN_MOON_THIRD = "moonphase_third";

    String QUERY_MOONPHASE = "moon/phases";
    String[] QUERY_MOONPHASE_PROJECTION = new String[] {
            COLUMN_MOON_NEW, COLUMN_MOON_FIRST, COLUMN_MOON_FULL, COLUMN_MOON_THIRD
    };

    /**
     * SEASONS
     */
    String COLUMN_SEASON_YEAR = "season_year";
    String COLUMN_SEASON_VERNAL = "season_vernal";
    String COLUMN_SEASON_SUMMER = "season_summer";
    String COLUMN_SEASON_AUTUMN = "season_autumn";
    String COLUMN_SEASON_WINTER = "season_winter";

    String QUERY_SEASONS = "seasons";
    String[] QUERY_SEASONS_PROJECTION = new String[] {
            COLUMN_SEASON_VERNAL, COLUMN_SEASON_SUMMER, COLUMN_SEASON_AUTUMN, COLUMN_SEASON_WINTER, COLUMN_SEASON_YEAR
    };

}
