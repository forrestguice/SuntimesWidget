/**
    Copyright (C) 2018-2025 Forrest Guice
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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidEventSettings;
import com.forrestguice.suntimeswidget.events.ElevationEvent;
import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.suntimeswidget.events.EventType;
import com.forrestguice.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.SuntimesApplication;

import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import static com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract.*;

/**
 * CalculatorProvider
 * @see CalculatorProviderContract
 */
public class CalculatorProvider extends ContentProvider
{
    private static final int URIMATCH_CONFIG = 0;

    private static final int URIMATCH_SUN = 10;
    private static final int URIMATCH_SUN_FOR_DATE = 20;
    private static final int URIMATCH_SUN_FOR_RANGE = 30;

    private static final int URIMATCH_SUNPOS = 40;
    private static final int URIMATCH_SUNPOS_FOR_DATE = 50;

    private static final int URIMATCH_MOON = 60;
    private static final int URIMATCH_MOON_FOR_DATE = 70;
    private static final int URIMATCH_MOON_FOR_RANGE = 80;

    private static final int URIMATCH_MOONPOS = 90;
    private static final int URIMATCH_MOONPOS_FOR_DATE = 100;

    private static final int URIMATCH_MOONPHASE = 110;
    private static final int URIMATCH_MOONPHASE_FOR_DATE = 120;
    private static final int URIMATCH_MOONPHASE_FOR_RANGE = 130;

    private static final int URIMATCH_SEASONS = 140;
    private static final int URIMATCH_SEASONS_FOR_YEAR = 150;
    private static final int URIMATCH_SEASONS_FOR_RANGE = 160;

    private static UriMatcher uriMatcher;
    private static UriMatcher uriMatcher()
    {
        if (CalculatorProvider.uriMatcher != null) {
            return CalculatorProvider.uriMatcher;
        }
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, QUERY_CONFIG, URIMATCH_CONFIG);

        uriMatcher.addURI(AUTHORITY, QUERY_SUN, URIMATCH_SUN);
        uriMatcher.addURI(AUTHORITY, QUERY_SUN + "/#", URIMATCH_SUN_FOR_DATE);
        uriMatcher.addURI(AUTHORITY, QUERY_SUN + "/*", URIMATCH_SUN_FOR_RANGE);

        uriMatcher.addURI(AUTHORITY, QUERY_SUNPOS, URIMATCH_SUNPOS);
        uriMatcher.addURI(AUTHORITY, QUERY_SUNPOS + "/#", URIMATCH_SUNPOS_FOR_DATE);

        uriMatcher.addURI(AUTHORITY, QUERY_MOON, URIMATCH_MOON);
        uriMatcher.addURI(AUTHORITY, QUERY_MOON + "/#", URIMATCH_MOON_FOR_DATE);
        uriMatcher.addURI(AUTHORITY, QUERY_MOON + "/*", URIMATCH_MOON_FOR_RANGE);

        uriMatcher.addURI(AUTHORITY, QUERY_MOONPOS, URIMATCH_MOONPOS);
        uriMatcher.addURI(AUTHORITY, QUERY_MOONPOS + "/#", URIMATCH_MOONPOS_FOR_DATE);

        uriMatcher.addURI(AUTHORITY, QUERY_MOONPHASE, URIMATCH_MOONPHASE);
        uriMatcher.addURI(AUTHORITY, QUERY_MOONPHASE + "/#", URIMATCH_MOONPHASE_FOR_DATE);
        uriMatcher.addURI(AUTHORITY, QUERY_MOONPHASE + "/*", URIMATCH_MOONPHASE_FOR_RANGE);

        uriMatcher.addURI(AUTHORITY, QUERY_SEASONS, URIMATCH_SEASONS);
        uriMatcher.addURI(AUTHORITY, QUERY_SEASONS + "/#", URIMATCH_SEASONS_FOR_YEAR);
        uriMatcher.addURI(AUTHORITY, QUERY_SEASONS + "/*", URIMATCH_SEASONS_FOR_RANGE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate()
    {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "CalculatorProvider.onCreate:");
        }
        SuntimesApplication.init(getContext());    // ContentProvider.onCreate always runs before Application.onCreate; initialize here instead to avoid potential race conditions (e.g. a query to the content provider crashes because Application.onCreate is still pending).
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values)
    {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        return 0;
    }

    /**
     * query
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "CalculatorProvider.query: " + uri + ", ready? " + SuntimesApplication.isInitialized());
        }

        HashMap<String, String> selectionMap = processSelection(processSelectionArgs(selection, selectionArgs));
        long now = Calendar.getInstance().getTimeInMillis();
        long date;
        long[] range;
        Cursor retValue = null;

        int uriMatch = uriMatcher().match(uri);
        switch (uriMatch)
        {
            case URIMATCH_CONFIG:
                //Log.d("CalculatorProvider", "URIMATCH_CONFIG");
                retValue = queryConfig(uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_SEASONS:
                //Log.d("CalculatorProvider", "URIMATCH_SEASONS");
                retValue = querySeasons(new long[] {now, now}, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_SEASONS_FOR_YEAR:
                //Log.d("CalculatorProvider", "URIMATCH_SEASONS_FOR_YEAR");
                Calendar dateTime = now(selectionMap);
                dateTime.set(Calendar.YEAR, (int)parseDate(uri, Calendar.getInstance().get(Calendar.YEAR)));
                retValue = querySeasons(new long[] { dateTime.getTimeInMillis(), dateTime.getTimeInMillis() }, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_SEASONS_FOR_RANGE:
                //Log.d("CalculatorProvider", "URIMATCH_SEASONS_FOR_RANGE");
                range = parseYearRange(uri.getLastPathSegment());
                retValue = querySeasons(range, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_SUN:
                //Log.d("CalculatorProvider", "URIMATCH_SUN");
                retValue = querySun(new long[] {now, now}, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_SUN_FOR_DATE:
                //Log.d("CalculatorProvider", "URIMATCH_SUN_FOR_DATE");
                date = parseDate(uri);
                retValue = querySun(new long[] {date, date}, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_SUN_FOR_RANGE:
                //Log.d("CalculatorProvider", "URIMATCH_SUN_FOR_RANGE");
                range = parseDateRange(uri.getLastPathSegment());
                retValue = querySun(range, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_SUNPOS:
                //Log.d("CalculatorProvider", "URIMATCH_SUNPOS");
                retValue = querySunPos(now, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_SUNPOS_FOR_DATE:
                //Log.d("CalculatorProvider", "URIMATCH_SUNPOS_FOR_DATE");
                date = parseDate(uri);
                retValue = querySunPos(date, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_MOON:
                //Log.d("CalculatorProvider", "URIMATCH_MOON");
                retValue = queryMoon(new long[] {now, now}, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_MOON_FOR_DATE:
                //Log.d("CalculatorProvider", "URIMATCH_MOON_FOR_DATE");
                date = parseDate(uri);
                retValue = queryMoon(new long[] {date, date}, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_MOON_FOR_RANGE:
                //Log.d("CalculatorProvider", "URIMATCH_MOON_FOR_RANGE");
                range = parseDateRange(uri.getLastPathSegment());
                retValue = queryMoon(range, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_MOONPOS:
                //Log.d("CalculatorProvider", "URIMATCH_MOONPOS");
                retValue = queryMoonPos(now, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_MOONPOS_FOR_DATE:
                //Log.d("CalculatorProvider", "URIMATCH_MOONPOS_FOR_DATE");
                date = parseDate(uri);
                retValue = queryMoonPos(date, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_MOONPHASE:
                //Log.d("CalculatorProvider", "URIMATCH_MOONPHASE");
                retValue = queryMoonPhase(new long[] {now, now}, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_MOONPHASE_FOR_DATE:
                //Log.d("CalculatorProvider", "URIMATCH_MOONPHASE_FOR_DATE");
                date = parseDate(uri);
                retValue = queryMoonPhase(new long[] {date, date}, uri, projection, selectionMap, sortOrder);
                break;
            case URIMATCH_MOONPHASE_FOR_RANGE:
                //Log.d("CalculatorProvider", "URIMATCH_MOONPHASE_FOR_RANGE");
                range = parseDateRange(uri.getLastPathSegment());
                retValue = queryMoonPhase(range, uri, projection, selectionMap, sortOrder);
                break;

            default:
                Log.e("CalculatorProvider", "Unrecognized URI! " + uri);
                break;
        }
        return retValue;
    }

    /**
     * queryConfig
     */
    private Cursor queryConfig(@NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        Context context = getContext();
        String[] columns = (projection != null ? projection : QUERY_CONFIG_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);

        if (context != null)
        {
            int appWidgetID = 0;
            if (selection.containsKey(COLUMN_CONFIG_APPWIDGETID)) {
                String id = selection.get(COLUMN_CONFIG_APPWIDGETID);
                appWidgetID = Integer.parseInt(id != null ? id : "0");
            }

            SuntimesCalculator calculator = initSunCalculator(getContext(), selection);
            if (calculator != null)
            {
                Location location = null;
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_CONFIG_APP_VERSION:
                            row[i] = BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? " [" + BuildConfig.BUILD_TYPE + "]" : "");
                            break;

                        case COLUMN_CONFIG_APP_VERSION_CODE:
                            row[i] = BuildConfig.VERSION_CODE;
                            break;

                        case COLUMN_CONFIG_PROVIDER_VERSION:
                            row[i] = CalculatorProviderContract.VERSION_NAME;
                            break;

                        case COLUMN_CONFIG_PROVIDER_VERSION_CODE:
                        case COLUMN_CONFIG_PROVIDER_VERSION_CODE_V2:
                            row[i] = CalculatorProviderContract.VERSION_CODE;
                            break;

                        case COLUMN_CONFIG_LOCALE:
                            AppSettings.LocaleMode localeMode = AppSettings.loadLocaleModePref(context);
                            row[i] = ((localeMode == AppSettings.LocaleMode.SYSTEM_LOCALE) ? null : AppSettings.loadLocalePref(context));
                            break;

                        case COLUMN_CONFIG_APP_TEXT_SIZE:
                            row[i] = AppSettings.loadTextSizePref(context);
                            break;

                        case COLUMN_CONFIG_APP_THEME:
                            row[i] = AppSettings.loadThemePref(context);
                            break;

                        case COLUMN_CONFIG_APP_THEME_OVERRIDE:
                            row[i] = AppSettings.getThemeOverride(context, AppSettings.loadThemePref(context));
                            break;

                        case COLUMN_CONFIG_LOCATION:
                            if (location == null) {
                                location = WidgetSettings.loadLocationPref(context, appWidgetID);
                            }
                            row[i] = location.getLabel();
                            break;

                        case COLUMN_CONFIG_LATITUDE:
                            if (location == null) {
                                location = WidgetSettings.loadLocationPref(context, appWidgetID);
                            }
                            row[i] = location.getLatitude();
                            break;

                        case COLUMN_CONFIG_LONGITUDE:
                            if (location == null) {
                                location = WidgetSettings.loadLocationPref(context, appWidgetID);
                            }
                            row[i] = location.getLongitude();
                            break;

                        case COLUMN_CONFIG_ALTITUDE:
                            if (location == null) {
                                location = WidgetSettings.loadLocationPref(context, appWidgetID);
                            }
                            row[i] = location.getAltitude();
                            break;

                        case COLUMN_CONFIG_TIMEZONE:
                            row[i] = initTimeZone(context, appWidgetID).getID();
                            break;

                        case COLUMN_CONFIG_TIMEZONEMODE:
                            row[i] = WidgetSettings.loadTimezoneModePref(context, appWidgetID).name();
                            break;

                        case COLUMN_CONFIG_SOLARTIMEMODE:
                            row[i] = WidgetSettings.loadSolarTimeModePref(context, appWidgetID).name();
                            break;

                        case COLUMN_CONFIG_APPWIDGETID:
                            row[i] = appWidgetID;
                            break;

                        case COLUMN_CONFIG_CALCULATOR:
                            row[i] = calculator.name();
                            break;

                        case COLUMN_CONFIG_CALCULATOR_FEATURES:
                            row[i] = calculator.getSupportedFeatures();
                            break;

                        case COLUMN_CONFIG_OPTION_TIME_IS24:
                            TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(context, 0);
                            boolean is24 = (mode == TimeFormatMode.MODE_SYSTEM || mode == TimeFormatMode.MODE_SUNTIMES) ? android.text.format.DateFormat.is24HourFormat(context)
                                    : (mode == TimeFormatMode.MODE_24HR);
                            row[i] = (is24 ? 1 : 0);
                            break;

                        case COLUMN_CONFIG_OPTION_TIME_SECONDS:
                            row[i] = (WidgetSettings.loadShowSecondsPref(context, appWidgetID) ? 1 : 0);
                            break;

                        case COLUMN_CONFIG_OPTION_TIME_HOURS:
                            row[i] = (WidgetSettings.loadShowHoursPref(context, appWidgetID) ? 1 : 0);
                            break;

                        case COLUMN_CONFIG_OPTION_TIME_WEEKS:
                            row[i] = (WidgetSettings.loadShowWeeksPref(context, appWidgetID) ? 1 : 0);
                            break;

                        case COLUMN_CONFIG_OPTION_TIME_DATETIME:
                            row[i] = (WidgetSettings.loadShowTimeDatePref(context, appWidgetID) ? 1 : 0);
                            break;

                        case COLUMN_CONFIG_OPTION_ALTITUDE:
                            row[i] = (WidgetSettings.loadLocationAltitudeEnabledPref(context, 0) ? 1 : 0);
                            break;

                        case COLUMN_CONFIG_OPTION_WARNINGS:
                            row[i] = (AppSettings.loadShowWarningsPref(context) ? 1 : 0);
                            break;

                        case COLUMN_CONFIG_OPTION_TALKBACK:
                            row[i] = (AppSettings.loadVerboseAccessibilityPref(context) ? 1 : 0);
                            break;

                        case COLUMN_CONFIG_LENGTH_UNITS:
                            row[i] = (WidgetSettings.loadLengthUnitsPref(context, appWidgetID).name());
                            break;

                        case COLUMN_CONFIG_OBJECT_HEIGHT:
                            row[i] = WidgetSettings.loadObserverHeightPref(context, appWidgetID);
                            break;

                        case COLUMN_CONFIG_OPTION_FIELDS:
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                            row[i] = pref.getInt(AppSettings.PREF_KEY_UI_SHOWFIELDS, AppSettings.PREF_DEF_UI_SHOWFIELDS);
                            break;

                        default:
                            row[i] = null;
                            break;
                    }
                }
                retValue.addRow(row);

            } else Log.e("CalculatorProvider", "queryConfig: sunSource " + appWidgetID + " is null!");
        } else Log.e("CalculatorProvider", "queryConfig: context is null!");
        return retValue;
    }
    private static final String COLUMN_CONFIG_PROVIDER_VERSION_CODE_V2 = "config_pvodier_version_code";    // key has typo in v0-v2; fixed v3

    /**
     * querySun
     */
    private Cursor querySun(long[] range, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_SUN_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        SuntimesCalculator calculator = initSunCalculator(getContext(), selection);
        if (calculator != null)
        {
            Calendar day = Calendar.getInstance(calculator.getTimeZone());
            day.setTimeInMillis(range[0]);

            Calendar endDay = Calendar.getInstance(calculator.getTimeZone());
            endDay.setTimeInMillis(range[1] + 1000);      // +1000ms (make range[1] inclusive)

            do {
                Map<String,SuntimesCalculator.SunPosition> positions = new HashMap<>();
                SuntimesCalculator.SunPosition position;

                Calendar calendar;
                Calendar[] morningBlueHour = null, eveningBlueHour = null;
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_SUN_ACTUAL_RISE:
                            calendar = calculator.getOfficialSunriseCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;
                        case COLUMN_SUN_ACTUAL_SET:
                            calendar = calculator.getOfficialSunsetCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_CIVIL_RISE:
                            calendar = calculator.getCivilSunriseCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;
                        case COLUMN_SUN_CIVIL_SET:
                            calendar = calculator.getCivilSunsetCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_NAUTICAL_RISE:
                            calendar = calculator.getNauticalSunriseCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;
                        case COLUMN_SUN_NAUTICAL_SET:
                            calendar = calculator.getNauticalSunsetCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_ASTRO_RISE:
                            calendar = calculator.getAstronomicalSunriseCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;
                        case COLUMN_SUN_ASTRO_SET:
                            calendar = calculator.getAstronomicalSunsetCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_NOON:
                            calendar = calculator.getSolarNoonCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_MIDNIGHT:
                            calendar = calculator.getSolarMidnightCalendarForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_GOLDEN_EVENING:
                            calendar = calculator.getEveningGoldenHourForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;
                        case COLUMN_SUN_GOLDEN_MORNING:
                            calendar =  calculator.getMorningGoldenHourForDate(day);
                            row[i] = (calendar != null) ? calendar.getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_BLUE8_RISE:
                            if (morningBlueHour == null) {
                                morningBlueHour = calculator.getMorningBlueHourForDate(day);
                            }
                            row[i] = (morningBlueHour[0] != null) ? morningBlueHour[0].getTimeInMillis() : null;
                            break;
                        case COLUMN_SUN_BLUE4_RISE:
                            if (morningBlueHour == null) {
                                morningBlueHour = calculator.getMorningBlueHourForDate(day);
                            }
                            row[i] = (morningBlueHour[1] != null) ? morningBlueHour[1].getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_BLUE4_SET:
                            if (eveningBlueHour == null) {
                                eveningBlueHour = calculator.getEveningBlueHourForDate(day);
                            }
                            row[i] = (eveningBlueHour[0] != null) ? eveningBlueHour[0].getTimeInMillis() : null;
                            break;
                        case COLUMN_SUN_BLUE8_SET:
                            if (eveningBlueHour == null) {
                                eveningBlueHour = calculator.getEveningBlueHourForDate(day);
                            }
                            row[i] = (eveningBlueHour[1] != null) ? eveningBlueHour[1].getTimeInMillis() : null;
                            break;

                        case COLUMN_SUN_NOON_AZ: case COLUMN_SUN_NOON_ALT:
                        case COLUMN_SUN_NOON_RA: case COLUMN_SUN_NOON_DEC:
                            calendar = calculator.getSolarNoonCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_NOON, columns[i], positions);
                            break;

                        case COLUMN_SUN_MIDNIGHT_AZ: case COLUMN_SUN_MIDNIGHT_ALT:
                        case COLUMN_SUN_MIDNIGHT_RA: case COLUMN_SUN_MIDNIGHT_DEC:
                            calendar = calculator.getSolarMidnightCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_MIDNIGHT, columns[i], positions);
                            break;

                        case COLUMN_SUN_ACTUAL_RISE_AZ: case COLUMN_SUN_ACTUAL_RISE_ALT:
                        case COLUMN_SUN_ACTUAL_RISE_RA: case COLUMN_SUN_ACTUAL_RISE_DEC:
                            calendar = calculator.getOfficialSunriseCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_ACTUAL_RISE, columns[i], positions);
                            break;
                        case COLUMN_SUN_ACTUAL_SET_AZ: case COLUMN_SUN_ACTUAL_SET_ALT:
                        case COLUMN_SUN_ACTUAL_SET_RA: case COLUMN_SUN_ACTUAL_SET_DEC:
                            calendar = calculator.getOfficialSunsetCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_ACTUAL_SET, columns[i], positions);
                            break;

                        case COLUMN_SUN_CIVIL_RISE_AZ: case COLUMN_SUN_CIVIL_RISE_ALT:
                        case COLUMN_SUN_CIVIL_RISE_RA: case COLUMN_SUN_CIVIL_RISE_DEC:
                            calendar = calculator.getCivilSunriseCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_CIVIL_RISE, columns[i], positions);
                            break;
                        case COLUMN_SUN_CIVIL_SET_AZ: case COLUMN_SUN_CIVIL_SET_ALT:
                        case COLUMN_SUN_CIVIL_SET_RA: case COLUMN_SUN_CIVIL_SET_DEC:
                            calendar = calculator.getCivilSunsetCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_CIVIL_SET, columns[i], positions);
                            break;

                        case COLUMN_SUN_NAUTICAL_RISE_AZ: case COLUMN_SUN_NAUTICAL_RISE_ALT:
                        case COLUMN_SUN_NAUTICAL_RISE_RA: case COLUMN_SUN_NAUTICAL_RISE_DEC:
                            calendar = calculator.getNauticalSunriseCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_NAUTICAL_RISE, columns[i], positions);
                            break;
                        case COLUMN_SUN_NAUTICAL_SET_AZ: case COLUMN_SUN_NAUTICAL_SET_ALT:
                        case COLUMN_SUN_NAUTICAL_SET_RA: case COLUMN_SUN_NAUTICAL_SET_DEC:
                            calendar = calculator.getNauticalSunsetCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_NAUTICAL_SET, columns[i], positions);
                            break;

                        case COLUMN_SUN_ASTRO_RISE_AZ: case COLUMN_SUN_ASTRO_RISE_ALT:
                        case COLUMN_SUN_ASTRO_RISE_RA: case COLUMN_SUN_ASTRO_RISE_DEC:
                            calendar = calculator.getAstronomicalSunriseCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_ASTRO_RISE, columns[i], positions);
                            break;
                        case COLUMN_SUN_ASTRO_SET_AZ: case COLUMN_SUN_ASTRO_SET_ALT:
                        case COLUMN_SUN_ASTRO_SET_RA: case COLUMN_SUN_ASTRO_SET_DEC:
                            calendar = calculator.getAstronomicalSunsetCalendarForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_ASTRO_SET, columns[i], positions);
                            break;

                        case COLUMN_SUN_GOLDEN_MORNING_AZ: case COLUMN_SUN_GOLDEN_MORNING_ALT:
                        case COLUMN_SUN_GOLDEN_MORNING_RA: case COLUMN_SUN_GOLDEN_MORNING_DEC:
                            calendar = calculator.getMorningGoldenHourForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_GOLDEN_MORNING, columns[i], positions);
                            break;
                        case COLUMN_SUN_GOLDEN_EVENING_AZ: case COLUMN_SUN_GOLDEN_EVENING_ALT:
                        case COLUMN_SUN_GOLDEN_EVENING_RA: case COLUMN_SUN_GOLDEN_EVENING_DEC:
                            calendar = calculator.getEveningGoldenHourForDate(day);
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_GOLDEN_EVENING, columns[i], positions);
                            break;

                        case COLUMN_SUN_BLUE8_RISE_AZ: case COLUMN_SUN_BLUE8_RISE_ALT:
                        case COLUMN_SUN_BLUE8_RISE_RA: case COLUMN_SUN_BLUE8_RISE_DEC:
                            calendar = calculator.getMorningBlueHourForDate(day)[0];
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_BLUE8_RISE, columns[i], positions);
                            break;
                        case COLUMN_SUN_BLUE8_SET_AZ: case COLUMN_SUN_BLUE8_SET_ALT:
                        case COLUMN_SUN_BLUE8_SET_RA: case COLUMN_SUN_BLUE8_SET_DEC:
                            calendar = calculator.getEveningBlueHourForDate(day)[1];
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_BLUE8_SET, columns[i], positions);
                            break;

                        case COLUMN_SUN_BLUE4_RISE_AZ: case COLUMN_SUN_BLUE4_RISE_ALT:
                        case COLUMN_SUN_BLUE4_RISE_RA: case COLUMN_SUN_BLUE4_RISE_DEC:
                            calendar = calculator.getMorningBlueHourForDate(day)[1];
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_BLUE4_RISE, columns[i], positions);
                            break;
                        case COLUMN_SUN_BLUE4_SET_AZ: case COLUMN_SUN_BLUE4_SET_ALT:
                        case COLUMN_SUN_BLUE4_SET_RA: case COLUMN_SUN_BLUE4_SET_DEC:
                            calendar = calculator.getEveningBlueHourForDate(day)[0];
                            row[i] = getPositionValueForSunKey(calculator, calendar, COLUMN_SUN_BLUE4_SET, columns[i], positions);
                            break;

                        default:
                            row[i] = getCustomEventValueForSunKey(getContext(), calculator, columns[i], day, positions);
                            break;
                    }
                }
                retValue.addRow(row);
                day.add(Calendar.DAY_OF_YEAR, 1);
            } while (day.before(endDay));

        } else Log.w("CalculatorProvider", "querySun: sunSource is null!");
        return retValue;
    }

    @Nullable
    protected static Object getCustomEventValueForSunKey(Context context, SuntimesCalculator calculator, String column, Calendar day, Map<String,SuntimesCalculator.SunPosition> positions)
    {
        String eventID = column;     // e.g CUSTOM0r, CUSTOM0s, CUSTOM0r_azimuth, etc
        String positionSuffix = null;    // e.g. _azimuth, _altitude, _ra, _dec

        if (column.endsWith(_POSITION_AZ)) {
            eventID = eventID.substring(0, eventID.lastIndexOf(_POSITION_AZ));
            positionSuffix = _POSITION_AZ;

        } else if (column.endsWith(_POSITION_ALT)) {
            eventID = eventID.substring(0, eventID.lastIndexOf(_POSITION_ALT));
            positionSuffix = _POSITION_ALT;

        } else if (column.endsWith(_POSITION_RA)) {
            eventID = eventID.substring(0, eventID.lastIndexOf(_POSITION_RA));
            positionSuffix = _POSITION_RA;

        } else if (column.endsWith(_POSITION_DEC)) {
            eventID = eventID.substring(0, eventID.lastIndexOf(_POSITION_DEC));
            positionSuffix = _POSITION_DEC;
        }

        if (EventType.resolveEventType(AndroidEventSettings.wrap(context), column) == EventType.EVENTALIAS)
        {
            String aliasID = eventID;   // e.g. CUSTOM0
            String aliasSuffix = "";    // e.g. r, s
            if (eventID.endsWith(ElevationEvent.SUFFIX_RISING) || eventID.endsWith(ElevationEvent.SUFFIX_SETTING)) {
                aliasID = aliasID.substring(0, eventID.length() - 1);
                aliasSuffix = eventID.substring(eventID.length() - 1);
            }

            EventAlias alias = EventSettings.loadEvent(AndroidEventSettings.wrap(context), aliasID);
            EventType aliasType = alias.getType();
            if (aliasType == EventType.SUN_ELEVATION || aliasType == EventType.SHADOWLENGTH || aliasType == EventType.DAYPERCENT)
            {
                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(day.getTimeInMillis());
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);

                boolean isRising = (aliasSuffix.endsWith(ElevationEvent.SUFFIX_RISING));
                Object[] aliasValues = AlarmEventProvider.createRow(context, alias, isRising, new String[] { AlarmEventContract.COLUMN_EVENT_TIMEMILLIS },
                        AlarmEventContract.EXTRA_ALARM_NOW + "=?", new String[] { Long.toString(now.getTimeInMillis()) });

                if (aliasValues != null && aliasValues.length > 0 && aliasValues[0] != null)
                {
                    Long eventTime = (Long) aliasValues[0];
                    if (positionSuffix == null)
                    {
                        if (BuildConfig.DEBUG) {
                            Log.d("DEBUG", eventID + " is a " + aliasType + " that occurs at " + eventTime + " (" + new SuntimesUtils().calendarDateTimeDisplayString(context, eventTime) + ")");
                        }
                        return eventTime;
                    }

                    switch (positionSuffix)
                    {
                        case _POSITION_AZ: case _POSITION_ALT:
                        case _POSITION_RA: case _POSITION_DEC:
                            Object positionValue  = getPositionValueForSunKey(calculator, eventTime, eventID, column, positions);
                            if (BuildConfig.DEBUG) {
                                Log.d("DEBUG", eventID + " is a " + aliasType + " with " + positionSuffix + " of " + positionValue);
                            }
                            return positionValue;

                        default:
                            Log.w("CalculatorProvider", "Unrecognized column (position suffix not supported): " + column + " (" + positionSuffix + ")");
                            return null;
                    }
                } else {
                    Log.w("CalculatorProvider", "Failed to retrieve values for custom event: " + column);
                    return null;
                }
            } else {
                Log.w("CalculatorProvider", "Unrecognized column (type not supported): " + column + " (" + aliasType +")");
                return null;
            }
        } else {
            Log.w("CalculatorProvider", "Unrecognized column (not a custom event): " + column);
            return null;
        }
    }

    protected static Double getPositionValueForSunKey(@NonNull SuntimesCalculator calculator, @Nullable Long eventTime, String rootKey, String fullKey, Map<String, SuntimesCalculator.SunPosition> positions)
    {
        if (eventTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(eventTime);
            return getPositionValueForSunKey(calculator, calendar, rootKey, fullKey, positions);
        } else return null;
    }
    protected static Double getPositionValueForSunKey(@NonNull SuntimesCalculator calculator, @Nullable Calendar calendar, String rootKey, String fullKey, Map<String, SuntimesCalculator.SunPosition> positions)
    {
        if (!positions.containsKey(rootKey)) {
            positions.put(rootKey, calendar != null ? calculator.getSunPosition(calendar) : null);
        }
        SuntimesCalculator.SunPosition position = positions.get(rootKey);

        String ROOT_KEY_PLUS_POSITION_AZ = rootKey + _POSITION_AZ;
        String ROOT_KEY_PLUS_POSITION_ALT = rootKey + _POSITION_ALT;
        String ROOT_KEY_PLUS_POSITION_RA = rootKey + _POSITION_RA;
        String ROOT_KEY_PLUS_POSITION_DEC = rootKey + _POSITION_DEC;
        String[] ROOT_KEY_PLUS_POSITION = new String[] { ROOT_KEY_PLUS_POSITION_AZ, ROOT_KEY_PLUS_POSITION_ALT, ROOT_KEY_PLUS_POSITION_RA, ROOT_KEY_PLUS_POSITION_DEC };    // 0, 1, 2, 3

        int j = -1;    // index into ROOT_KEY_PLUS_POSITION
        for (int i=0; i<ROOT_KEY_PLUS_POSITION.length; i++)
        {
            if (ROOT_KEY_PLUS_POSITION[i].equals(fullKey)) {
                j = i;
                break;
            }
        }
        switch (j)
        {
            case 0: return (position != null ? position.azimuth : null);
            case 1: return (position != null ? position.elevation : null);
            case 2: return (position != null ? position.rightAscension : null);
            case 3: return (position != null ? position.declination : null);
        }
        return null;
    }

    public static final String COLUMN_SUN_NOON_AZ = COLUMN_SUN_NOON + _POSITION_AZ;
    public static final String COLUMN_SUN_NOON_ALT = COLUMN_SUN_NOON + _POSITION_ALT;
    public static final String COLUMN_SUN_NOON_RA = COLUMN_SUN_NOON + _POSITION_RA;
    public static final String COLUMN_SUN_NOON_DEC = COLUMN_SUN_NOON + _POSITION_DEC;

    public static final String COLUMN_SUN_MIDNIGHT_AZ = COLUMN_SUN_MIDNIGHT + _POSITION_AZ;
    public static final String COLUMN_SUN_MIDNIGHT_ALT = COLUMN_SUN_MIDNIGHT + _POSITION_ALT;
    public static final String COLUMN_SUN_MIDNIGHT_RA = COLUMN_SUN_MIDNIGHT + _POSITION_RA;
    public static final String COLUMN_SUN_MIDNIGHT_DEC = COLUMN_SUN_MIDNIGHT + _POSITION_DEC;

    public static final String COLUMN_SUN_ACTUAL_RISE_AZ = COLUMN_SUN_ACTUAL_RISE + _POSITION_AZ;
    public static final String COLUMN_SUN_ACTUAL_RISE_ALT = COLUMN_SUN_ACTUAL_RISE + _POSITION_ALT;
    public static final String COLUMN_SUN_ACTUAL_RISE_RA = COLUMN_SUN_ACTUAL_RISE + _POSITION_RA;
    public static final String COLUMN_SUN_ACTUAL_RISE_DEC = COLUMN_SUN_ACTUAL_RISE + _POSITION_DEC;

    public static final String COLUMN_SUN_ACTUAL_SET_AZ = COLUMN_SUN_ACTUAL_SET + _POSITION_AZ;
    public static final String COLUMN_SUN_ACTUAL_SET_ALT = COLUMN_SUN_ACTUAL_SET + _POSITION_ALT;
    public static final String COLUMN_SUN_ACTUAL_SET_RA = COLUMN_SUN_ACTUAL_SET + _POSITION_RA;
    public static final String COLUMN_SUN_ACTUAL_SET_DEC = COLUMN_SUN_ACTUAL_SET + _POSITION_DEC;

    public static final String COLUMN_SUN_CIVIL_RISE_AZ = COLUMN_SUN_CIVIL_RISE + _POSITION_AZ;
    public static final String COLUMN_SUN_CIVIL_RISE_ALT = COLUMN_SUN_CIVIL_RISE + _POSITION_ALT;
    public static final String COLUMN_SUN_CIVIL_RISE_RA = COLUMN_SUN_CIVIL_RISE + _POSITION_RA;
    public static final String COLUMN_SUN_CIVIL_RISE_DEC = COLUMN_SUN_CIVIL_RISE + _POSITION_DEC;

    public static final String COLUMN_SUN_CIVIL_SET_AZ = COLUMN_SUN_CIVIL_SET + _POSITION_AZ;
    public static final String COLUMN_SUN_CIVIL_SET_ALT = COLUMN_SUN_CIVIL_SET + _POSITION_ALT;
    public static final String COLUMN_SUN_CIVIL_SET_RA = COLUMN_SUN_CIVIL_SET + _POSITION_RA;
    public static final String COLUMN_SUN_CIVIL_SET_DEC = COLUMN_SUN_CIVIL_SET + _POSITION_DEC;

    public static final String COLUMN_SUN_NAUTICAL_RISE_AZ = COLUMN_SUN_NAUTICAL_RISE + _POSITION_AZ;
    public static final String COLUMN_SUN_NAUTICAL_RISE_ALT = COLUMN_SUN_NAUTICAL_RISE + _POSITION_ALT;
    public static final String COLUMN_SUN_NAUTICAL_RISE_RA = COLUMN_SUN_NAUTICAL_RISE + _POSITION_RA;
    public static final String COLUMN_SUN_NAUTICAL_RISE_DEC = COLUMN_SUN_NAUTICAL_RISE + _POSITION_DEC;

    public static final String COLUMN_SUN_NAUTICAL_SET_AZ = COLUMN_SUN_NAUTICAL_SET + _POSITION_AZ;
    public static final String COLUMN_SUN_NAUTICAL_SET_ALT = COLUMN_SUN_NAUTICAL_SET + _POSITION_ALT;
    public static final String COLUMN_SUN_NAUTICAL_SET_RA = COLUMN_SUN_NAUTICAL_SET + _POSITION_RA;
    public static final String COLUMN_SUN_NAUTICAL_SET_DEC = COLUMN_SUN_NAUTICAL_SET + _POSITION_DEC;

    public static final String COLUMN_SUN_ASTRO_RISE_AZ = COLUMN_SUN_ASTRO_RISE + _POSITION_AZ;
    public static final String COLUMN_SUN_ASTRO_RISE_ALT = COLUMN_SUN_ASTRO_RISE + _POSITION_ALT;
    public static final String COLUMN_SUN_ASTRO_RISE_RA = COLUMN_SUN_ASTRO_RISE + _POSITION_RA;
    public static final String COLUMN_SUN_ASTRO_RISE_DEC = COLUMN_SUN_ASTRO_RISE + _POSITION_DEC;

    public static final String COLUMN_SUN_ASTRO_SET_AZ = COLUMN_SUN_ASTRO_SET + _POSITION_AZ;
    public static final String COLUMN_SUN_ASTRO_SET_ALT = COLUMN_SUN_ASTRO_SET + _POSITION_ALT;
    public static final String COLUMN_SUN_ASTRO_SET_RA = COLUMN_SUN_ASTRO_SET + _POSITION_RA;
    public static final String COLUMN_SUN_ASTRO_SET_DEC = COLUMN_SUN_ASTRO_SET + _POSITION_DEC;

    public static final String COLUMN_SUN_GOLDEN_MORNING_AZ = COLUMN_SUN_GOLDEN_MORNING + _POSITION_AZ;
    public static final String COLUMN_SUN_GOLDEN_MORNING_ALT = COLUMN_SUN_GOLDEN_MORNING + _POSITION_ALT;
    public static final String COLUMN_SUN_GOLDEN_MORNING_RA = COLUMN_SUN_GOLDEN_MORNING + _POSITION_RA;
    public static final String COLUMN_SUN_GOLDEN_MORNING_DEC = COLUMN_SUN_GOLDEN_MORNING + _POSITION_DEC;

    public static final String COLUMN_SUN_GOLDEN_EVENING_AZ = COLUMN_SUN_GOLDEN_EVENING + _POSITION_AZ;
    public static final String COLUMN_SUN_GOLDEN_EVENING_ALT = COLUMN_SUN_GOLDEN_EVENING + _POSITION_ALT;
    public static final String COLUMN_SUN_GOLDEN_EVENING_RA = COLUMN_SUN_GOLDEN_EVENING + _POSITION_RA;
    public static final String COLUMN_SUN_GOLDEN_EVENING_DEC = COLUMN_SUN_GOLDEN_EVENING + _POSITION_DEC;

    public static final String COLUMN_SUN_BLUE8_RISE_AZ = COLUMN_SUN_BLUE8_RISE + _POSITION_AZ;
    public static final String COLUMN_SUN_BLUE8_RISE_ALT = COLUMN_SUN_BLUE8_RISE + _POSITION_ALT;
    public static final String COLUMN_SUN_BLUE8_RISE_RA = COLUMN_SUN_BLUE8_RISE + _POSITION_RA;
    public static final String COLUMN_SUN_BLUE8_RISE_DEC = COLUMN_SUN_BLUE8_RISE + _POSITION_DEC;

    public static final String COLUMN_SUN_BLUE8_SET_AZ = COLUMN_SUN_BLUE8_SET + _POSITION_AZ;
    public static final String COLUMN_SUN_BLUE8_SET_ALT = COLUMN_SUN_BLUE8_SET + _POSITION_ALT;
    public static final String COLUMN_SUN_BLUE8_SET_RA = COLUMN_SUN_BLUE8_SET + _POSITION_RA;
    public static final String COLUMN_SUN_BLUE8_SET_DEC = COLUMN_SUN_BLUE8_SET + _POSITION_DEC;

    public static final String COLUMN_SUN_BLUE4_RISE_AZ = COLUMN_SUN_BLUE4_RISE + _POSITION_AZ;
    public static final String COLUMN_SUN_BLUE4_RISE_ALT = COLUMN_SUN_BLUE4_RISE + _POSITION_ALT;
    public static final String COLUMN_SUN_BLUE4_RISE_RA = COLUMN_SUN_BLUE4_RISE + _POSITION_RA;
    public static final String COLUMN_SUN_BLUE4_RISE_DEC = COLUMN_SUN_BLUE4_RISE + _POSITION_DEC;

    public static final String COLUMN_SUN_BLUE4_SET_AZ = COLUMN_SUN_BLUE4_SET + _POSITION_AZ;
    public static final String COLUMN_SUN_BLUE4_SET_ALT = COLUMN_SUN_BLUE4_SET + _POSITION_ALT;
    public static final String COLUMN_SUN_BLUE4_SET_RA = COLUMN_SUN_BLUE4_SET + _POSITION_RA;
    public static final String COLUMN_SUN_BLUE4_SET_DEC = COLUMN_SUN_BLUE4_SET + _POSITION_DEC;

    /**
     * querySunPos
     */
    private Cursor querySunPos(long dateMillis, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_SUNPOS_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        SuntimesCalculator calculator = initSunCalculator(getContext(), selection);
        if (calculator != null)
        {
            Calendar datetime = Calendar.getInstance(calculator.getTimeZone());
            datetime.setTimeInMillis(dateMillis);
            SuntimesCalculator.SunPosition position = calculator.getSunPosition(datetime);
            if (position != null)
            {
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_SUNPOS_ALT:
                            row[i] = position.elevation;
                            break;

                        case COLUMN_SUNPOS_AZ:
                            row[i] = position.azimuth;
                            break;

                        case COLUMN_SUNPOS_RA:
                            row[i] = position.rightAscension;
                            break;

                        case COLUMN_SUNPOS_DEC:
                            row[i] = position.declination;
                            break;

                        case COLUMN_SUNPOS_ISDAY:
                            row[i] = calculator.isDay(datetime);
                            break;

                        case COLUMN_SUNPOS_EOT:
                            row[i] = calculator.equationOfTime(datetime);
                            break;

                        case COLUMN_SUNPOS_DATE:
                            row[i] = dateMillis;
                            break;

                        default:
                            row[i] = null;
                            break;
                    }
                }
                retValue.addRow(row);

            } else Log.w("CalculatorProvider", "querySunPos: sunSource returned null position! " + calculator.name());
        } else Log.w("CalculatorProvider", "querySunPos: sunSource is null!");
        return retValue;
    }

    /**
     * queryMoon
     */
    private Cursor queryMoon(long[] range, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_MOON_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        SuntimesCalculator calculator = initMoonCalculator(getContext(), selection);
        if (calculator != null)
        {
            Calendar day = Calendar.getInstance(calculator.getTimeZone());
            day.setTimeInMillis(range[0]);

            Calendar endDay = Calendar.getInstance(calculator.getTimeZone());
            endDay.setTimeInMillis(range[1] + 1000);    // +1000ms (make range[1] inclusive)

            do {
                SuntimesCalculator.MoonTimes moontimes = null;
                SuntimesCalculator.MoonPosition position_rising = null, position_setting = null;
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_MOON_RISE:
                            moontimes = (moontimes == null ? calculator.getMoonTimesForDate(day) : moontimes);
                            row[i] = (moontimes.riseTime) != null ? moontimes.riseTime.getTimeInMillis() : null;
                            break;
                        case COLUMN_MOON_SET:
                            moontimes = (moontimes == null ? calculator.getMoonTimesForDate(day) : moontimes);
                            row[i] = (moontimes.setTime) != null ? moontimes.setTime.getTimeInMillis() : null;
                            break;

                        case COLUMN_MOON_RISE_AZ: case COLUMN_MOON_RISE_ALT:
                        case COLUMN_MOON_RISE_RA: case COLUMN_MOON_RISE_DEC:
                        case COLUMN_MOON_RISE_DISTANCE:
                            moontimes = (moontimes == null ? calculator.getMoonTimesForDate(day) : moontimes);
                            position_rising = (position_rising == null && moontimes.riseTime != null) ? calculator.getMoonPosition(moontimes.riseTime) : position_rising;
                            switch (columns[i])
                            {
                                case COLUMN_MOON_RISE_AZ: row[i] = (position_rising != null ? position_rising.azimuth : null); break;
                                case COLUMN_MOON_RISE_ALT: row[i] = (position_rising != null ? position_rising.elevation : null); break;
                                case COLUMN_MOON_RISE_RA: row[i] = (position_rising != null ? position_rising.rightAscension : null); break;
                                case COLUMN_MOON_RISE_DEC: row[i] = (position_rising != null ? position_rising.declination : null); break;
                                case COLUMN_MOON_RISE_DISTANCE: row[i] = (position_rising != null ? position_rising.distance : null); break;
                            }
                            break;

                        case COLUMN_MOON_SET_AZ: case COLUMN_MOON_SET_ALT:
                        case COLUMN_MOON_SET_RA: case COLUMN_MOON_SET_DEC:
                        case COLUMN_MOON_SET_DISTANCE:
                            moontimes = (moontimes == null ? calculator.getMoonTimesForDate(day) : moontimes);
                            position_setting = (position_setting == null && moontimes.setTime != null) ? calculator.getMoonPosition(moontimes.setTime) : position_setting;
                            switch (columns[i])
                            {
                                case COLUMN_MOON_SET_AZ: row[i] = (position_setting != null ? position_setting.azimuth : null); break;
                                case COLUMN_MOON_SET_ALT: row[i] = (position_setting != null ? position_setting.elevation : null); break;
                                case COLUMN_MOON_SET_RA: row[i] = (position_setting != null ? position_setting.rightAscension : null); break;
                                case COLUMN_MOON_SET_DEC: row[i] = (position_setting != null ? position_setting.declination : null); break;
                                case COLUMN_MOON_SET_DISTANCE: row[i] = (position_setting != null ? position_setting.distance : null); break;
                            }
                            break;

                        case COLUMN_MOON_RISE_ILLUM:
                            moontimes = (moontimes == null ? calculator.getMoonTimesForDate(day) : moontimes);
                            row[i] = (moontimes.riseTime) != null ? calculator.getMoonIlluminationForDate(moontimes.riseTime) : null;
                            break;
                        case COLUMN_MOON_SET_ILLUM:
                            moontimes = (moontimes == null ? calculator.getMoonTimesForDate(day) : moontimes);
                            row[i] = (moontimes.setTime) != null ? calculator.getMoonIlluminationForDate(moontimes.setTime) : null;
                            break;

                        default:
                            row[i] = null;
                            break;
                    }
                }
                retValue.addRow(row);
                day.add(Calendar.DAY_OF_YEAR, 1);
            } while (day.before(endDay));

        } else Log.w("CalculatorProvider", "queryMoon: moonSource is null!");
        return retValue;
    }

    public static final String COLUMN_MOON_RISE_AZ = COLUMN_MOON_RISE + _POSITION_AZ;
    public static final String COLUMN_MOON_RISE_ALT = COLUMN_MOON_RISE + _POSITION_ALT;
    public static final String COLUMN_MOON_RISE_RA = COLUMN_MOON_RISE + _POSITION_RA;
    public static final String COLUMN_MOON_RISE_DEC = COLUMN_MOON_RISE + _POSITION_DEC;

    public static final String COLUMN_MOON_SET_AZ = COLUMN_MOON_SET + _POSITION_AZ;
    public static final String COLUMN_MOON_SET_ALT = COLUMN_MOON_SET + _POSITION_ALT;
    public static final String COLUMN_MOON_SET_RA = COLUMN_MOON_SET + _POSITION_RA;
    public static final String COLUMN_MOON_SET_DEC = COLUMN_MOON_SET + _POSITION_DEC;

    /**
     * queryMoonPos
     */
    private Cursor queryMoonPos(long dateMillis, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_MOONPOS_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        SuntimesCalculator calculator = initMoonCalculator(getContext(), selection);
        if (calculator != null)
        {
            Calendar datetime = Calendar.getInstance(calculator.getTimeZone());
            datetime.setTimeInMillis(dateMillis);
            SuntimesCalculator.MoonPosition position = calculator.getMoonPosition(datetime);
            if (position != null)
            {
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_MOONPOS_ALT:
                            row[i] = position.elevation;
                            break;

                        case COLUMN_MOONPOS_AZ:
                            row[i] = position.azimuth;
                            break;

                        case COLUMN_MOONPOS_RA:
                            row[i] = position.rightAscension;
                            break;

                        case COLUMN_MOONPOS_DEC:
                            row[i] = position.declination;
                            break;

                        case COLUMN_MOONPOS_DISTANCE:
                            row[i] = position.distance;
                            break;

                        case COLUMN_MOONPOS_PERIGEE:
                            row[i] = calculator.getMoonPerigeeNextDate(datetime).getTimeInMillis();
                            break;

                        case COLUMN_MOONPOS_APOGEE:
                            row[i] = calculator.getMoonApogeeNextDate(datetime).getTimeInMillis();
                            break;

                        case COLUMN_MOONPOS_ILLUMINATION:
                            row[i] = calculator.getMoonIlluminationForDate(datetime);
                            break;

                        case COLUMN_MOONPOS_DATE:
                            row[i] = dateMillis;
                            break;

                        default:
                            row[i] = null;
                            break;
                    }
                }
                retValue.addRow(row);

            } else Log.w("CalculatorProvider", "queryMoonPos: moonSource returned null position! " + calculator.name());
        } else Log.w("CalculatorProvider", "queryMoonPos: moonSource is null!");
        return retValue;
    }

    /**
     * queryMoonPhase
     */
    private Cursor queryMoonPhase(long[] range, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_MOONPHASE_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        SuntimesCalculator calculator = initMoonCalculator(getContext(), selection);
        if (calculator != null)
        {
            ArrayList<Calendar> events = new ArrayList<>();
            HashMap<SuntimesCalculator.MoonPhase, Calendar> events1 = new HashMap<>();

            Calendar date = Calendar.getInstance(calculator.getTimeZone());
            date.setTimeInMillis(range[0]);

            Calendar endDate = Calendar.getInstance(calculator.getTimeZone());
            endDate.setTimeInMillis(range[1] + 1000);   // +1000ms (make range[1] inclusive)

            do {
                events.clear();
                events1.clear();
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    Calendar event;
                    SuntimesCalculator.MoonPosition position;
                    switch (columns[i])
                    {
                        case COLUMN_MOON_NEW:
                            events.add(event = initEventValue(SuntimesCalculator.MoonPhase.NEW, events1, calculator, date));
                            row[i] = event.getTimeInMillis();
                            break;

                        case COLUMN_MOON_FIRST:
                            events.add(event = initEventValue(SuntimesCalculator.MoonPhase.FIRST_QUARTER, events1, calculator, date));
                            row[i] = event.getTimeInMillis();
                            break;

                        case COLUMN_MOON_FULL:
                            events.add(event = initEventValue(SuntimesCalculator.MoonPhase.FULL, events1, calculator, date));
                            row[i] = event.getTimeInMillis();
                            break;

                        case COLUMN_MOON_THIRD:
                            events.add(event = initEventValue(SuntimesCalculator.MoonPhase.THIRD_QUARTER, events1, calculator, date));
                            row[i] = event.getTimeInMillis();
                            break;

                        case COLUMN_MOON_NEW_DISTANCE:
                            events.add(event = initEventValue(SuntimesCalculator.MoonPhase.NEW, events1, calculator, date));
                            position = ((event != null) ? calculator.getMoonPosition(event) : null);
                            row[i] = ((position != null) ? position.distance : null);
                            break;

                        case COLUMN_MOON_FIRST_DISTANCE:
                            events.add(event = initEventValue(SuntimesCalculator.MoonPhase.FIRST_QUARTER, events1, calculator, date));
                            position = ((event != null) ? calculator.getMoonPosition(event) : null);
                            row[i] = ((position != null) ? position.distance : null);
                            break;

                        case COLUMN_MOON_FULL_DISTANCE:
                            events.add(event = initEventValue(SuntimesCalculator.MoonPhase.FULL, events1, calculator, date));
                            position = ((event != null) ? calculator.getMoonPosition(event) : null);
                            row[i] = ((position != null) ? position.distance : null);
                            break;

                        case COLUMN_MOON_THIRD_DISTANCE:
                            events.add(event = initEventValue(SuntimesCalculator.MoonPhase.THIRD_QUARTER, events1, calculator, date));
                            position = ((event != null) ? calculator.getMoonPosition(event) : null);
                            row[i] = ((position != null) ? position.distance : null);
                            break;

                        default:
                            row[i] = null;
                            break;
                    }
                }
                retValue.addRow(row);

                Collections.sort(events);
                Calendar latest = (events.size() > 1) ? events.get(events.size()-1)
                                : (events.size() > 0) ? events.get(0) : null;

                date.setTimeInMillis(latest != null ? latest.getTimeInMillis() + 1000
                                                    : range[1] + 1000);
            } while (date.before(endDate));

        } else Log.w("CalculatorProvider", "queryMoonPhase: moonSource is null!");
        return retValue;
    }

    private Calendar initEventValue(@NonNull SuntimesCalculator.MoonPhase phase, @NonNull HashMap<SuntimesCalculator.MoonPhase, Calendar> events, @NonNull SuntimesCalculator calculator, @NonNull Calendar date)
    {
        Calendar event = events.get(phase);
        if (event == null) {
            events.put(phase, event = calculator.getMoonPhaseNextDate(phase, date));
        }
        return event;
    }

    /**
     * querySeasons
     */
    private Cursor querySeasons(long[] range, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_SEASONS_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);
        SuntimesCalculator calculator = initSunCalculator(getContext(), selection);
        if (calculator != null)
        {
            Calendar year = Calendar.getInstance(calculator.getTimeZone());
            year.setTimeInMillis(range[0]);

            Calendar year0 = Calendar.getInstance(calculator.getTimeZone());
            year0.setTimeInMillis(year.getTimeInMillis());
            year0.add(Calendar.YEAR, -1);

            Calendar endYear = Calendar.getInstance(calculator.getTimeZone());
            endYear.setTimeInMillis(range[1]);
            endYear.add(Calendar.YEAR, 1);                   // +1 year (make range[1] inclusive)

            do {
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    Calendar event;
                    switch (columns[i])
                    {
                        case COLUMN_SEASON_YEAR:
                            row[i] = year.get(Calendar.YEAR);
                            break;

                        case COLUMN_SEASON_TROPICAL_YEAR_LENGTH:
                            row[i] = calculator.getTropicalYearLength(year);
                            break;

                        case COLUMN_SEASON_CROSS_SPRING:
                            event = SuntimesData.midpoint(calculator.getSpringEquinoxForYear(year), calculator.getSummerSolsticeForYear(year));
                            row[i] = (event != null ? event.getTimeInMillis() : null);
                            break;

                        case COLUMN_SEASON_CROSS_AUTUMN:
                            event = SuntimesData.midpoint(calculator.getAutumnalEquinoxForYear(year), calculator.getWinterSolsticeForYear(year));
                            row[i] = (event != null ? event.getTimeInMillis() : null);
                            break;

                        case COLUMN_SEASON_CROSS_SUMMER:
                            event = (calculator.getLocation().getLatitudeAsDouble() >= 0)
                                    ? SuntimesData.midpoint(calculator.getSummerSolsticeForYear(year), calculator.getAutumnalEquinoxForYear(year))
                                    : SuntimesData.midpoint(calculator.getSummerSolsticeForYear(year0), calculator.getAutumnalEquinoxForYear(year));
                            row[i] = (event != null ? event.getTimeInMillis() : null);
                            break;

                        case COLUMN_SEASON_CROSS_WINTER:
                            event = (calculator.getLocation().getLatitudeAsDouble() >= 0)
                                    ? SuntimesData.midpoint(calculator.getWinterSolsticeForYear(year0), calculator.getSpringEquinoxForYear(year))
                                    : SuntimesData.midpoint(calculator.getWinterSolsticeForYear(year), calculator.getSpringEquinoxForYear(year));
                            row[i] = (event != null ? event.getTimeInMillis() : null);
                            break;

                        case COLUMN_SEASON_VERNAL:
                        case COLUMN_SEASON_SPRING:
                            event = calculator.getSpringEquinoxForYear(year);
                            row[i] = (event != null ? event.getTimeInMillis() : null);
                            break;

                        case COLUMN_SEASON_SUMMER:
                            event = calculator.getSummerSolsticeForYear(year);
                            row[i] = (event != null ? event.getTimeInMillis() : null);
                            break;

                        case COLUMN_SEASON_AUTUMN:
                            event = calculator.getAutumnalEquinoxForYear(year);
                            row[i] = (event != null ? event.getTimeInMillis() : null);
                            break;

                        case COLUMN_SEASON_WINTER:
                            event = calculator.getWinterSolsticeForYear(year);
                            row[i] = (event != null ? event.getTimeInMillis() : null);
                            break;

                        default:
                            row[i] = null;
                            break;
                    }
                }
                retValue.addRow(row);
                year.add(Calendar.YEAR, 1);
                year0.add(Calendar.YEAR, 1);
            } while (year.before(endYear));

        } else Log.w("CalculatorProvider", "querySeasons: sunSource is null!");
        return retValue;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Calculator Init
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private SuntimesCalculator initCalculator(Context context, HashMap<String,String> selection, String calculatorName)
    {
        int appWidgetID = 0;
        if (selection.containsKey(COLUMN_CONFIG_APPWIDGETID)) {
            String id = selection.get(COLUMN_CONFIG_APPWIDGETID);
            appWidgetID = Integer.parseInt(id != null ? id : "0");
        }

        String timezoneID = selection.get(COLUMN_CONFIG_TIMEZONE);
        TimeZone timezone = (timezoneID != null ? TimeZone.getTimeZone(timezoneID) : null);
        Location location = processSelection_location(selection);

        SuntimesCalculatorDescriptor descriptor = null;
        String calculator = selection.get(COLUMN_CONFIG_CALCULATOR);
        if (calculator != null) {
            descriptor = SuntimesCalculatorDescriptor.valueOf(calculator);
        }

        if (location == null && timezone == null && descriptor == null) {
            if (calculatorName != null && calculatorName.equals("moon"))
                return initMoonCalculator(context, appWidgetID);
            else return initSunCalculator(context, appWidgetID);

        } else {
            if (location == null) {
                location = WidgetSettings.loadLocationPref(context, appWidgetID);
            }
            if (timezone == null) {
                timezone = initTimeZone(context, appWidgetID);
            }
            if (descriptor == null) {
                descriptor = (calculatorName == null ? WidgetSettings.loadCalculatorModePref(context, appWidgetID)
                        : WidgetSettings.loadCalculatorModePref(context, appWidgetID, calculatorName));
            }
            SuntimesCalculatorFactory factory = new SuntimesCalculatorFactory(descriptor);
            //Log.d("CalculatorProvider", "initCalculator: " + location.getLabel() + " :: " + location.toString());
            return factory.createCalculator(location, timezone);
        }
    }

    private static SparseArray<SuntimesCalculator> sunSource = new SparseArray<>();    // sun source for appWidgetID (app is 0)
    private static SuntimesCalculator initSunCalculator(Context context, int appWidgetID)
    {
        SuntimesCalculator retValue = sunSource.get(appWidgetID);   // lazy init
        if (retValue == null)
        {
            WidgetSettings.initDefaults(context);
            Location location = WidgetSettings.loadLocationPref(context, appWidgetID);
            TimeZone timezone = initTimeZone(context, appWidgetID);
            SuntimesCalculatorDescriptor descriptor = WidgetSettings.loadCalculatorModePref(context, appWidgetID);
            SuntimesCalculatorFactory factory = new SuntimesCalculatorFactory(descriptor);
            sunSource.put(appWidgetID, (retValue = factory.createCalculator(location, timezone)));
            //Log.d("CalculatorProvider", "initSunCalculator: " + location.getLabel() + " :: " + location.toString());
        } //else Log.d("CalculatorProvider", "initSunCalculator: using pre-existing calculator");
        return retValue;
    }
    private SuntimesCalculator initSunCalculator(Context context, HashMap<String,String> selection) {
        return initCalculator(context, selection, null);
    }

    private static SparseArray<SuntimesCalculator> moonSource = new SparseArray<>();   // moon source for appWidgetID (app is 0)
    private static SuntimesCalculator initMoonCalculator(Context context, int appWidgetID)
    {
        SuntimesCalculator retValue = moonSource.get(appWidgetID);
        if (retValue == null)    // lazy init
        {
            WidgetSettings.initDefaults(context);
            Location location = WidgetSettings.loadLocationPref(context, appWidgetID);
            TimeZone timezone = initTimeZone(context, appWidgetID);
            SuntimesCalculatorDescriptor descriptor = WidgetSettings.loadCalculatorModePref(context, 0, "moon");      // always use app calculator (0)
            SuntimesCalculatorFactory factory = new SuntimesCalculatorFactory(descriptor);
            moonSource.put(appWidgetID, (retValue = factory.createCalculator(location, timezone)));
            //Log.d("CalculatorProvider", "initMoonCalculator: " + location.getLabel() + " :: " + location.toString());
        } //else Log.d("CalculatorProvider", "initMoonCalculator: using pre-existing calculator");
        return retValue;
    }
    private SuntimesCalculator initMoonCalculator(Context context, HashMap<String,String> selection) {
        return initCalculator(context, selection, "moon");
    }

    private static TimeZone initTimeZone(Context context, int appWidgetID)
    {
        switch (WidgetSettings.loadTimezoneModePref(context, appWidgetID)) {
            case CURRENT_TIMEZONE: return TimeZone.getDefault();
            case CUSTOM_TIMEZONE: default: return TimeZone.getTimeZone(WidgetSettings.loadTimezonePref(context, appWidgetID));
        }
    }

    public Calendar now(HashMap<String,String> selection) {
        return Calendar.getInstance(getTimeZone(getContext(), selection));
    }

    public static void clearCachedConfig(int appWidgetID)
    {
        sunSource.remove(appWidgetID);
        moonSource.remove(appWidgetID);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Query Helpers
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * processSelectionArgs
     * A query helper method; inserts arguments into selection string.
     * @param selection a selection string (as passed to query)
     * @param selectionArgs a list of selection arguments
     * @return a completed selection string containing substituted arguments
     */
    @Nullable
    public static String processSelectionArgs(@Nullable String selection, @Nullable String[] selectionArgs)
    {
        String retValue = selection;
        if (selectionArgs != null && selection != null)
        {
            for (int i=0; i<selectionArgs.length; i++)
            {
                if (selectionArgs[i] != null)
                {
                    if (retValue.contains("?")) {
                        retValue = retValue.replaceFirst("\\?", selectionArgs[i]);         // TODO: optimize; String.replace taking up most of the query time..

                    } else {
                        Log.w("CalendarProvider", "processSelectionArgs: Too many arguments! Given " + selectionArgs.length + " arguments, but selection contains only " + (i+1));
                        break;
                    }
                }
            }
        }
        return retValue;
    }

    /**
     * processSelection
     * A query helper method; extracts selection columns/values to HashMap.
     * @param selection a completed selection string (@see processSelectionArgs)
     * @return a HashMap containing <COLUMN_NAME, VALUE> pairs
     */
    public static HashMap<String, String> processSelection(@Nullable String selection)
    {
        HashMap<String, String> retValue = new HashMap<>();
        if (selection != null)
        {
            String[] expressions = selection.split(" or | OR | and | AND ");  // just separators in this context (all interpreted the same)
            for (String expression : expressions)
            {
                String[] parts = expression.split("=");
                if (parts.length == 2) {
                    retValue.put(parts[0].trim(), parts[1].trim());
                } else Log.w("CalendarProvider", "processSelection: Too many parts! " + expression);
            }
        }
        return retValue;
    }

    /**
     * processSelection_location
     * A query helper method; creates a Location object from selection values (@see processSelection).
     * @param selection a completed selection string (@see processSelectionArgs)
     * @return a WidgetSettings.Location created from the selection (or null if selection is missing COLUMN_CONFIG_LATITUDE or COLUMN_CONFIG_LONGITUDE)
     */
    @Nullable
    public static Location processSelection_location(@NonNull HashMap<String,String> selection)
    {
        Location location = null;
        if (selection.containsKey(COLUMN_CONFIG_LATITUDE) || selection.containsKey(COLUMN_CONFIG_LONGITUDE))
        {
            String value_latitude = selection.get(COLUMN_CONFIG_LATITUDE);
            String value_longitude = selection.get(COLUMN_CONFIG_LONGITUDE);
            if (value_latitude != null && value_longitude != null)
            {
                boolean hasAltitude = selection.containsKey(COLUMN_CONFIG_ALTITUDE);
                location = (hasAltitude) ? new Location("", value_latitude, value_longitude, selection.get(COLUMN_CONFIG_ALTITUDE))
                        : new Location("", value_latitude, value_longitude);
                if (hasAltitude) {
                    location.setUseAltitude(true);
                }
            }
        }
        return location;
    }

    /**
     * parseDate
     */
    protected long parseDate(@Nullable Uri uri) {
        return parseDate(uri, Calendar.getInstance().getTimeInMillis());
    }

    /**
     * parseDate
     * @param uri Uri
     * @param fallback date returned if unable to parse uri
     */
    protected long parseDate(@Nullable Uri uri, long fallback)
    {
        if (uri != null)
        {
            try {
                return ContentUris.parseId(uri);
            } catch (NumberFormatException e) {
                Log.e("CalculatorProvider", "invalid argument! " + e);
            }
        }
        return fallback;
    }

    /**
     * parseDateRange
     * A query helper method; get startDate and endDate from a "timestamp-timestamp" range value.
     * @param rangeSegment startMillis-endMillis
     * @return a long[2] containing [0]startDateMillis, [1]endDateMillis.
     */
    public static long[] parseDateRange(@Nullable String rangeSegment)
    {
        long[] retValue = new long[2];
        String[] rangeString = ((rangeSegment != null) ? rangeSegment.split("-") : new String[0]);
        if (rangeString.length == 2)
        {
            try {
                retValue[0] = Long.parseLong(rangeString[0]);
                retValue[1] = Long.parseLong(rangeString[1]);

            } catch (NumberFormatException e) {
                Log.w("CalculatorProvider", "Invalid range! " + rangeSegment);
                retValue[0] = retValue[1] = Calendar.getInstance().getTimeInMillis();
            }
        } else {
            Log.w("CalculatorProvider", "Invalid range! " + rangeSegment);
            retValue[0] = retValue[1] = Calendar.getInstance().getTimeInMillis();
        }
        //Log.d("DEBUG", "startDate: " + retValue[0] + ", endDate: " + retValue[1]);
        return retValue;
    }

    /**
     * parseYearRange
     * A query helper method; get startDate and endDate from a "startYear-endYear" range value.
     * @param rangeSegment startYear-endYear
     * @return a Calendar[2] containing [0](startDate), [1](endDate).
     */
    public static long[] parseYearRange(@Nullable String rangeSegment)
    {
        Calendar[] retValue = new Calendar[2];
        String[] rangeString = ((rangeSegment != null) ? rangeSegment.split("-") : new String[0]);
        if (rangeString.length == 2)
        {
            try {
                retValue[0] = Calendar.getInstance();
                retValue[0].set(Calendar.YEAR, Integer.parseInt(rangeString[0]));

                retValue[1] = Calendar.getInstance();
                retValue[1].set(Calendar.YEAR, Integer.parseInt(rangeString[1]));

            } catch (NumberFormatException e) {
                Log.w("CalculatorProvider", "Invalid range! " + rangeSegment);
                retValue[0] = retValue[1] = Calendar.getInstance();
            }
        } else {
            Log.w("CalculatorProvider", "Invalid range! " + rangeSegment);
            retValue[0] = retValue[1] = Calendar.getInstance();
        }
        //Log.d("DEBUG", "startDate: " + retValue[0].get(Calendar.YEAR) + ", endDate: " + retValue[1].get(Calendar.YEAR));
        return new long[] { retValue[0].getTimeInMillis(), retValue[1].getTimeInMillis() };
    }

    /**
     * getTimeZone
     * @param selection selection override
     * @return TimeZone object
     */
    public static TimeZone getTimeZone(Context context, HashMap<String,String> selection)
    {
        String tzID = selection.get(COLUMN_CONFIG_TIMEZONE);
        if (tzID != null) {
            return TimeZone.getTimeZone(tzID);

        } else {
            int appWidgetID = 0;
            if (selection.containsKey(COLUMN_CONFIG_APPWIDGETID)) {
                String id = selection.get(COLUMN_CONFIG_APPWIDGETID);
                appWidgetID = Integer.parseInt(id != null ? id : "0");
            }
            return TimeZone.getTimeZone(WidgetSettings.loadTimezonePref(context, appWidgetID));
        }
    }

}
