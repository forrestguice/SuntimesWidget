/**
    Copyright (C) 2018-2020 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.core;

/**
 * CalculatorProviderContract
 * @version 4 (0.4.1)
 *
 * Supported URIs have the form: "content://AUTHORITY/query"
 * ..where [AUTHORITY] is "suntimeswidget.calculator.provider"
 * ..where [query] is one of: QUERY_CONFIG (config),
 *                            QUERY_SUN (sun), QUERY_SUNPOS (sunpos),
 *                            QUERY_MOON (moon), QUERY_MOONPOS (moonpos), QUERY_MOONPHASE (moon/phases),
 *                            QUERY_SEASONS (seasons)
 *
 * ------------------------------------------------------------------------------------------------
 * QUERY_CONFIG (config)
 *   The following URIs are supported:
 *       content://suntimeswidget.calculator.provider/config                         .. get the calculator config
 *
 *   The result will be one row containing:
 *       COLUMN_CONFIG_APP_VERSION, COLUMN_CONFIG_APP_VERSION_CODE,
 *       COLUMN_CONFIG_PROVIDER_VERSION, COLUMN_CONFIG_PROVIDER_VERSION_CODE,
 *       COLUMN_CONFIG_LOCALE, COLUMN_CONFIG_APPTHEME,
 *       COLUMN_CONFIG_CALCULATOR, COLUMN_CONFIG_CALCULATOR_FEATURES,
 *       COLUMN_CONFIG_LATITUDE, COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE,
 *       COLUMN_CONFIG_TIMEZONE, COLUMN_CONFIG_APPWIDGETID,
 *       COLUMN_CONFIG_OPTION_TIME_IS24, COLUMN_CONFIG_OPTION_TIME_SECONDS, COLUMN_CONFIG_OPTION_TIME_HOURS,
 *       COLUMN_CONFIG_OPTION_TIME_WEEKS, COLUMN_CONFIG_OPTION_TIME_DATETIME,
 *       COLUMN_CONFIG_OPTION_ALTITUDE, COLUMN_CONFIG_OPTION_WARNINGS, COLUMN_CONFIG_OPTION_TALKBACK
 *       COLUMN_CONFIG_LENGTH_UNITS, COLUMN_CONFIG_OBJECT_HEIGHT, COLUMN_CONFIG_OPTION_FIELDS
 *
 * ------------------------------------------------------------------------------------------------*
 * QUERY_SUN (sun)
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
 * QUERY_SUNPOS (sunpos)
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
 * QUERY_MOON (moon)
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moon                     .. get todays moon (rise, set)
 *       content://suntimeswiget.calculator.provider/moon/[millis]            .. get upcoming moon (timestamp)
 *       content://suntimeswiget.calculator.provider/moon/[millis]-[millis]   .. get upcoming moon for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *       COLUMN_MOON_RISE, COLUMN_MOON_SET
 *
 * ------------------------------------------------------------------------------------------------*
 * QUERY_MOONPOS (moonpos)
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moonpos                   .. get moon position right now
 *       content://suntimeswiget.calculator.provider/moonpos/[millis]          .. get moon position at timestamp
 *
 *   The result will be one row containing:
 *       COLUMN_MOONPOS_AZ, COLUMN_MOONPOS_ALT,
 *       COLUMN_MOONPOS_RA, COLUMN_MOONPOS_DEC,
 *       COLUMN_MOON_PERIGEE, COLUMN_MOON_APOGEE, COLUMN_MOON_DISTANCE
 *       COLUMN_MOONPOS_ILLUMINATION,
 *       COLUMN_MOONPOS_DATE
 * ------------------------------------------------------------------------------------------------*
 * QUERY_MOONPHASE (moonphases)
 *   The following URIs are supported:
 *       content://suntimeswiget.calculator.provider/moonphases                     .. get upcoming moon phases
 *       content://suntimeswiget.calculator.provider/moonphases/[millis]            .. get upcoming moon phases after date (timestamp)
 *       content://suntimeswiget.calculator.provider/moonphases/[millis]-[millis]   .. get upcoming moon phases for range (timestamp)
 *
 *   The result will be one or more rows containing:
 *       COLUMN_MOON_NEW, COLUMN_MOON_FIRST,
 *       COLUMN_MOON_FULL, COLUMN_MOON_THIRD
 *
 * ------------------------------------------------------------------------------------------------
 * QUERY_SEASONS (seasons)
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
 *         cursor.close();  // don't forget to close the cursor when finished
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
 *                      + COLUMN_CONFIG_ALTITUDE + "=? AND "
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
 *
 * ------------------------------------------------------------------------------------------------
 * PERMISSIONS
 *   Access to this provider is restricted to apps that declare the READ_PERMISSION in their manifest.
 *   The permission is granted by the user when an app is installed (and revoked if the provider is
 *   uninstalled/reinstalled).
 *
 *       <uses-permission android:name="suntimeswidget.calculator.permission.READ_PROVIDER" />
 *
 *   Note that calling query without the necessary permission will result in a SecurityException.
 *       try {
 *           Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null)
 *           if (cursor != null) {
 *               ...
 *           }
 *       } catch (SecurityException e) {
 *           // Permissions! We don't have them for some reason. They are either missing from the
 *           // manifest, were revoked, or were never granted (re-installing the app should grant
 *           // permissions).
 *       }*
 *
 * ------------------------------------------------------------------------------------------------
 * CHANGES
 *   0 initial version
 *   1 adds COLUMN_CONFIG_LOCATION; fixes return type of SUN and MOON queries; permission changed to suntimes.permission.READ_CALCULATOR
 *   2 adds COLUMN_MOONPOS_PERIGEE, APOGEE, and COLUMN_MOON_*_DISTANCE
 *   3 adds COLUMN_CONFIG_OPTION_IS24, COLUMN_CONFIG_OPTION_TIME_SECONDS, COLUMN_CONFIG_OPTION_TIME_HOURS, COLUMN_CONFIG_OPTION_TIME_WEEKS
 *     adds COLUMN_CONFIG_OPTION_ALTITUDE, COLUMN_CONFIG_OPTION_WARNINGS, COLUMN_CONFIG_OPTION_TALKBACK, COLUMN_CONFIG_OPTION_FIELDS
 *     adds COLUMN_CONFIG_LENGTH_UNITS, COLUMN_CONFIG_OBJECT_HEIGHT
 *     fixes typo in COLUMN_CONFIG_PROVIDER_VERSION_CODE
 *   4 adds COLUMN_CONFIG_APP_THEME_OVERRIDE
 */
public interface CalculatorProviderContract
{
    String AUTHORITY = "suntimeswidget.calculator.provider";
    String READ_PERMISSION = "suntimes.permission.READ_CALCULATOR";
    String VERSION_NAME = "v0.4.1";
    int VERSION_CODE = 4;

    /**
     * CONFIG
     */
    String COLUMN_CONFIG_PROVIDER_VERSION = "config_provider_version";             // String (provider version string)
    String COLUMN_CONFIG_PROVIDER_VERSION_CODE = "config_provider_version_code";   // int (provider version code)
    String COLUMN_CONFIG_PROVIDER_VERSION_CODE_V2 = "config_pvodier_version_code"; // int (key has typo in v0-v2; fixed v3)
    String COLUMN_CONFIG_APP_VERSION = "config_app_version";                       // String (app version string)
    String COLUMN_CONFIG_APP_VERSION_CODE = "config_app_version_code";             // int (app version code)
    String COLUMN_CONFIG_APP_THEME = "config_app_theme";                           // String (base: dark, light, daynight)
    String COLUMN_CONFIG_APP_THEME_OVERRIDE = "config_app_theme_override";         // String (themeName)
    String COLUMN_CONFIG_LOCALE = "config_locale";                                 // String (localeCode)
    String COLUMN_CONFIG_LOCATION = "location";                                    // String (locationName)
    String COLUMN_CONFIG_LATITUDE = "latitude";                                    // String (dd)
    String COLUMN_CONFIG_LONGITUDE = "longitude";                                  // String (dd)
    String COLUMN_CONFIG_ALTITUDE = "altitude";                                    // String (meters)
    String COLUMN_CONFIG_TIMEZONE = "timezone";                                    // String (timezoneID)
    String COLUMN_CONFIG_APPWIDGETID = "appwidgetid";                              // int
    String COLUMN_CONFIG_CALCULATOR = "calculator";                                // String (calculatorName)
    String COLUMN_CONFIG_CALCULATOR_FEATURES = "calculator_features";              // int[] (SuntimesCalculator.FEATURE flags)

    String COLUMN_CONFIG_OPTION_TIME_IS24 = "option_is24";                         // int (boolean) 24 hour time
    String COLUMN_CONFIG_OPTION_TIME_SECONDS = "option_seconds";                   // int (boolean) show seconds
    String COLUMN_CONFIG_OPTION_TIME_HOURS = "option_hours";                       // int (boolean) show hours+min in spans > a day
    String COLUMN_CONFIG_OPTION_TIME_WEEKS = "option_weeks";                       // int (boolean) divide spans greater than 7d into weeks
    String COLUMN_CONFIG_OPTION_TIME_DATETIME = "option_datetime";                 // int (boolean) include time when displaying dates
    String COLUMN_CONFIG_OPTION_ALTITUDE = "option_altitude";                      // int (boolean) use altitude based refinements
    String COLUMN_CONFIG_OPTION_WARNINGS = "option_warnings";                      // int (boolean) show config warnings
    String COLUMN_CONFIG_OPTION_TALKBACK = "option_talkback";                      // int (boolean) announce ui changes
    String COLUMN_CONFIG_OPTION_FIELDS = "option_fields";                          // byte (bit positions) field visibility (see AppSettings.PREF_KEY_UI_SHOWFIELDS)

    String COLUMN_CONFIG_LENGTH_UNITS = "distance_units";                          // String (enum) METRIC, IMPERIAL
    String COLUMN_CONFIG_OBJECT_HEIGHT = "object_height";                          // float (meters)


    String QUERY_CONFIG = "config";
    String[] QUERY_CONFIG_PROJECTION = new String[] {
            COLUMN_CONFIG_APP_VERSION, COLUMN_CONFIG_APP_VERSION_CODE,
            COLUMN_CONFIG_PROVIDER_VERSION, COLUMN_CONFIG_PROVIDER_VERSION_CODE,
            COLUMN_CONFIG_LOCALE, COLUMN_CONFIG_APP_THEME, COLUMN_CONFIG_APP_THEME_OVERRIDE,
            COLUMN_CONFIG_CALCULATOR, COLUMN_CONFIG_CALCULATOR_FEATURES,
            COLUMN_CONFIG_LOCATION, COLUMN_CONFIG_LATITUDE, COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE,
            COLUMN_CONFIG_TIMEZONE, COLUMN_CONFIG_APPWIDGETID,
            COLUMN_CONFIG_OPTION_TIME_IS24, COLUMN_CONFIG_OPTION_TIME_SECONDS, COLUMN_CONFIG_OPTION_TIME_HOURS, COLUMN_CONFIG_OPTION_TIME_WEEKS, COLUMN_CONFIG_OPTION_TIME_DATETIME,
            COLUMN_CONFIG_OPTION_ALTITUDE, COLUMN_CONFIG_OPTION_WARNINGS, COLUMN_CONFIG_OPTION_TALKBACK, COLUMN_CONFIG_LENGTH_UNITS, COLUMN_CONFIG_OBJECT_HEIGHT, COLUMN_CONFIG_OPTION_FIELDS
    };

    /**
     * SUN
     */
    String COLUMN_SUN_NOON = "solarnoon";                   // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_ACTUAL_RISE = "sunrise";              // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_ACTUAL_SET = "sunset";                // long (timestamp); (broken <= v0.10.2 [returns Calendar])

    String COLUMN_SUN_CIVIL_RISE = "civilrise";             // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_CIVIL_SET = "civilset";               // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_NAUTICAL_RISE = "nauticalrise";       // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_NAUTICAL_SET = "nauticalset";         // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_ASTRO_RISE = "astrorise";             // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_ASTRO_SET = "astroset";               // long (timestamp); (broken <= v0.10.2 [returns Calendar])

    String COLUMN_SUN_GOLDEN_MORNING = "goldenmorning";     // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_GOLDEN_EVENING = "goldenevening";     // long (timestamp); (broken <= v0.10.2 [returns Calendar])

    String COLUMN_SUN_BLUE8_RISE = "blue8rise";             // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_BLUE4_RISE = "blue4rise";             // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_BLUE8_SET = "blue8set";               // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_SUN_BLUE4_SET = "blue4set";               // long (timestamp); (broken <= v0.10.2 [returns Calendar])

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
    String COLUMN_SUNPOS_AZ = "sunpos_azimuth";            // double
    String COLUMN_SUNPOS_ALT = "sunpos_altitude";          // double
    String COLUMN_SUNPOS_RA = "sunpos_ra";                 // double
    String COLUMN_SUNPOS_DEC = "sunpos_dec";               // double
    String COLUMN_SUNPOS_ISDAY = "sunpos_isday";           // boolean
    String COLUMN_SUNPOS_DATE = "sunpos_date";             // long (timestamp)

    String QUERY_SUNPOS = "sunpos";
    String[] QUERY_SUNPOS_PROJECTION = new String[] {
            COLUMN_SUNPOS_AZ, COLUMN_SUNPOS_ALT,
            COLUMN_SUNPOS_RA, COLUMN_SUNPOS_DEC,
            COLUMN_SUNPOS_ISDAY, COLUMN_SUNPOS_DATE
    };

    /**
     * MOON
     */
    String COLUMN_MOON_RISE = "moonrise";                  // long (timestamp); (broken <= v0.10.2 [returns Calendar])
    String COLUMN_MOON_SET = "moonset";                    // long (timestamp); (broken <= v0.10.2 [returns Calendar])

    String QUERY_MOON = "moon";
    String[] QUERY_MOON_PROJECTION = new String[] { COLUMN_MOON_RISE, COLUMN_MOON_SET };

    /**
     * MOONPOS
     */
    String COLUMN_MOONPOS_AZ = "moonpos_azimuth";          // double
    String COLUMN_MOONPOS_ALT = "moonpos_altitude";        // double
    String COLUMN_MOONPOS_RA = "moonpos_ra";               // double
    String COLUMN_MOONPOS_DEC = "moonpos_dec";             // double
    String COLUMN_MOONPOS_ILLUMINATION = "moonpos_illum";  // double [0,1]
    String COLUMN_MOONPOS_DATE = "moonpos_date";           // long (timestamp)

    String COLUMN_MOONPOS_PERIGEE = "moonpos_perigee";     // long (timestamp)
    String COLUMN_MOONPOS_APOGEE = "moonpos_apogee";       // long (timestamp)
    String COLUMN_MOONPOS_DISTANCE = "moonpos_distance";   // double (kilometers)

    String QUERY_MOONPOS = "moonpos";
    String[] QUERY_MOONPOS_PROJECTION = new String[] {
            COLUMN_MOONPOS_AZ, COLUMN_MOONPOS_ALT,
            COLUMN_MOONPOS_RA, COLUMN_MOONPOS_DEC,
            COLUMN_MOONPOS_PERIGEE, COLUMN_MOONPOS_APOGEE, COLUMN_MOONPOS_DISTANCE,
            COLUMN_MOONPOS_ILLUMINATION, COLUMN_MOONPOS_DATE
    };

    /**
     * MOONPHASE
     */
    String COLUMN_MOON_NEW = "moonphase_new";                        // long (timestamp)
    String COLUMN_MOON_NEW_DISTANCE = "moonphase_new_distance";      // double (km)

    String COLUMN_MOON_FIRST = "moonphase_first";                    // long (timestamp)
    String COLUMN_MOON_FIRST_DISTANCE = "moonphase_first_distance";  // double (km)

    String COLUMN_MOON_FULL = "moonphase_full";                      // long (timestamp)
    String COLUMN_MOON_FULL_DISTANCE = "moonphase_full_distance";    // double (km)

    String COLUMN_MOON_THIRD = "moonphase_third";                    // long (timestamp)
    String COLUMN_MOON_THIRD_DISTANCE = "moonphase_third_distance";  // double (km)

    String QUERY_MOONPHASE = "moonphases";
    String[] QUERY_MOONPHASE_PROJECTION = new String[] {
            COLUMN_MOON_NEW, COLUMN_MOON_FIRST, COLUMN_MOON_FULL, COLUMN_MOON_THIRD,
            COLUMN_MOON_NEW_DISTANCE, COLUMN_MOON_FIRST_DISTANCE, COLUMN_MOON_FULL_DISTANCE, COLUMN_MOON_THIRD_DISTANCE
    };

    /**
     * SEASONS
     */
    String COLUMN_SEASON_YEAR = "season_year";               // int
    String COLUMN_SEASON_VERNAL = "season_vernal";           // long (timestamp)
    String COLUMN_SEASON_SUMMER = "season_summer";           // long (timestamp)
    String COLUMN_SEASON_AUTUMN = "season_autumn";           // long (timestamp)
    String COLUMN_SEASON_WINTER = "season_winter";           // long (timestamp)

    String QUERY_SEASONS = "seasons";
    String[] QUERY_SEASONS_PROJECTION = new String[] {
            COLUMN_SEASON_VERNAL, COLUMN_SEASON_SUMMER, COLUMN_SEASON_AUTUMN, COLUMN_SEASON_WINTER, COLUMN_SEASON_YEAR
    };

}
