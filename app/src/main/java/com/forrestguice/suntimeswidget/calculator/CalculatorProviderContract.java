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
 *       COLUMN_CONFIG_LOCALE, COLUMN_CONFIG_APPTHEME,
 *       COLUMN_CONFIG_CALCULATOR, COLUMN_CONFIG_CALCULATOR_FEATURES,
 *       COLUMN_CONFIG_LATITUDE, COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE,
 *       COLUMN_CONFIG_TIMEZONE, COLUMN_CONFIG_APPWIDGETID
 *
 * ------------------------------------------------------------------------------------------------*
 * SUN
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/sun                     .. get todays sun (rise, set, twilights)
 *       content://suntimeswiget.calculator.provider/sun/[millis]            .. get upcoming sun (timestamp)
 *       content://suntimeswiget.calculator.provider/sun/[millis]-[millis]   .. get upcoming sun for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *       COLUMN_SUN_NOON,
 *       COLUMN_SUN_ACTUAL_RISE,      COLUMN_SUN_ACTUAL_SET,
 *       COLUMN_SUN_CIVIL_RISE,       COLUMN_SUN_CIVIL_SET,
 *       COLUMN_SUN_NAUTICAL_RISE,    COLUMN_SUN_NAUTICAL_SET,
 *       COLUMN_SUN_ASTRO_RISE,       COLUMN_SUN_ASTRO_SET,COLUMN_SUN_NOON,
 *       COLUMN_SUN_GOLDEN_MORNING,   COLUMN_SUN_GOLDEN_EVENING,
 *       COLUMN_SUN_BLUE8_RISE,       COLUMN_SUN_BLUE8_SET,
 *       COLUMN_SUN_BLUE4_RISE,       COLUMN_SUN_BLUE4_SET
 *
 * ------------------------------------------------------------------------------------------------*
 * SUNPOS
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/sunpos                   .. get sun position right now
 *       content://suntimeswiget.calculator.provider/sunpos/[millis]          .. get sun position at timestamp
 *
 *   The result will be one row containing:
 *       COLUMN_SUNPOS_AZ, COLUMN_SUNPOS_ALT,
 *       COLUMN_SUNPOS_RA, COLUMN_SUNPOS_DEC,
 *       COLUMN_SUNPOS_ISDAY, COLUMN_SUNPOS_DATE
 *
 * ------------------------------------------------------------------------------------------------*
 * MOON
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moon                     .. get todays moon (rise, set)
 *       content://suntimeswiget.calculator.provider/moon/[millis]            .. get upcoming moon (timestamp)
 *       content://suntimeswiget.calculator.provider/moon/[millis]-[millis]   .. get upcoming moon for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *       COLUMN_MOON_RISE, COLUMN_MOON_SET
 *
 * ------------------------------------------------------------------------------------------------*
 * MOONPOS
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moonpos                   .. get moon position right now
 *       content://suntimeswiget.calculator.provider/moonpos/[millis]          .. get moon position at timestamp
 *
 *   The result will be one row containing:
 *       COLUMN_MOONPOS_AZ, COLUMN_MOONPOS_ALT,
 *       COLUMN_MOONPOS_RA, COLUMN_MOONPOS_DEC,
 *       COLUMN_MOONPOS_ILLUMINATION,
 *       COLUMN_MOONPOS_DATE
 * ------------------------------------------------------------------------------------------------*
 * MOON PHASES
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moon/phases                     .. get upcoming moon phases
 *       content://suntimeswiget.calculator.provider/moon/phases/[millis]            .. get upcoming moon phases after date (timestamp)
 *       content://suntimeswiget.calculator.provider/moon/phases/[millis]-[millis]   .. get upcoming moon phases for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *       COLUMN_MOON_NEW, COLUMN_MOON_FIRST,
 *       COLUMN_MOON_FULL, COLUMN_MOON_THIRD
 *
 * ------------------------------------------------------------------------------------------------
 * SOLSTICE / EQUINOX
 *   The following URIs are supported:
 *       content://suntimeswidget.calculator.provider/seasons                         .. get vernal, summer, autumn, and winter dates for this year
 *       content://suntimeswidget.calculator.provider/seasons/[year]                  .. get vernal, summer, autumn, and winter dates for some year
 *       content://suntimeswidget.calculator.provider/seasons/[year]-[year]           .. get vernal, summer, autumn, and winter dates for range
 *
 *   The result will be one or more rows containing:
 *       COLUMM_SEASON_VERNAL, COLUMN_SEASON_SUMMER,
 *       COLUMN_SEASON_AUTUMN, COLUMN_SEASON_WINTER, COLUMN_YEAR
 *
 * ------------------------------------------------------------------------------------------------
 * Example: Date of the Full Moon
 *
 *     // The projection specifies the data (columns) to be requested.
 *     // Pass a null projection to use the default (full projection), or construct a projection containing specific columns...
 *     String[] projection = new String[] { COLUMN_MOON_FULL };   // full moon only
 *
 *     // Create a URI pointing to the provider (e.g. "content://suntimeswiget.calculator.provider/moon/phases"
 *     // Some URI's (like "moon/phases") support timestamps (milliseconds), or timestamp-timestamp ranges.
 *     Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_MOONPHASE + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());    // get all full moons between startDate and endDate
 *
 *     // Use ContentResolver.query to get a cursor to the data..
 *     ContentResolver resolver = context.getContentResolver();
 *     Cursor cursor = resolver.query(uri, projection, null, null, null);
 *     if (cursor != null
 *     {
 *         cursor.moveToFirst();                     // Expect at least one row, but a cursor will contain multiple rows if a timestamp-range is used.
 *         while (!cursor.isAfterLast())
 *         {
 *             // each column has its own type; time-based columns are Long (timestamp)
 *             Long fullMoonTimeMillis = cursor.getLong(cursor.getColumnIndex(COLUMN_MOON_FULL));
 *
 *             // always test result for null before trying to use it..
 *             if (fullMoonTimeMillis != null) {             // might be null if column is not part of or is missing from the projection
 *                 Calendar fullMoonCalendar = Calendar.getInstance();
 *                 fullMoonCalendar.setTimeInMillis(fullMoonTimeMillis);
 *                 // ... do something with fullMoonCalendar
 *             }
 *             cursor.moveToNext();
 *         }
 *         cursor.close();
 *     }
 *
 * ------------------------------------------------------------------------------------------------
 * APP & WIDGETS
 *   To specify a widget configuration you can provide a `selection` arg with the appWidgetID.
 *   All URIs default to 0 (the app configuration) if the selection is omitted.
 *
 *     int appWidgetID = 1000;   // use config for widget 1000
 *     String selection = COLUMN_CONFIG_APPWIDGETID + "=?";
 *     String[] selectionArgs = new String[] { appWidgetID };
 *
 *     String[] projection = ...
 *     Uri uri = ...
 *     Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
 *     if (cursor != null) {
 *         ...
 *     }
 *
 *  Note: Calculators are cached by appWidgetID to improve performance when making repeated calls.
 *
 * ------------------------------------------------------------------------------------------------
 * LOCATION, TIMEZONE, CALCULATOR (OVERRIDES)
 *   It is also possible to override the location, timezone, or calculator using `selection` args.
 *
 *     String selection = COLUMN_CONFIG_CALCULATOR +=? AND "      // any combination of these CONFIG_COLUMNS
 *                      + COLUMN_CONFIG_LATITUDE + "=? AND "
 *                      + COLUMN_CONFIG_LONGITUDE + "=? AND "
 *                      + COLUMN_CONFIG_TIMEZONE + "=?";
 *     String[] selectionArgs = new String[] {"time4a-cc", "33.45", "-111.94", "US/Arizona"};
 *
 *     String[] projection = ...
 *     Uri uri = ...
 *     Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
 *     if (cursor != null) {
 *         ...
 *     }
 *
 *   Note: Caching doesn't occur when overriding the configuration; a new instance will be created for each query.
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
    String COLUMN_CONFIG_CALCULATOR = "calculator";
    String COLUMN_CONFIG_CALCULATOR_FEATURES = "calculator_features";

    String QUERY_CONFIG = "config";
    String[] QUERY_CONFIG_PROJECTION = new String[] {
            COLUMN_CONFIG_LOCALE, COLUMN_CONFIG_APPTHEME,
            COLUMN_CONFIG_CALCULATOR, COLUMN_CONFIG_CALCULATOR_FEATURES,
            COLUMN_CONFIG_LATITUDE, COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE,
            COLUMN_CONFIG_TIMEZONE, COLUMN_CONFIG_APPWIDGETID
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
    String COLUMN_SUNPOS_ISDAY = "sunpos_isday";
    String COLUMN_SUNPOS_DATE = "sunpos_date";

    String QUERY_SUNPOS = "sunpos";
    String[] QUERY_SUNPOS_PROJECTION = new String[] {
            COLUMN_SUNPOS_AZ, COLUMN_SUNPOS_ALT,
            COLUMN_SUNPOS_RA, COLUMN_SUNPOS_DEC,
            COLUMN_SUNPOS_ISDAY, COLUMN_SUNPOS_DATE
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
    String COLUMN_MOONPOS_ILLUMINATION = "moonpos_illum";
    String COLUMN_MOONPOS_DATE = "moonpos_date";

    String QUERY_MOONPOS = "moonpos";
    String[] QUERY_MOONPOS_PROJECTION = new String[] {
            COLUMN_MOONPOS_AZ, COLUMN_MOONPOS_ALT,
            COLUMN_MOONPOS_RA, COLUMN_MOONPOS_DEC,
            COLUMN_MOONPOS_ILLUMINATION, COLUMN_MOONPOS_DATE
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
