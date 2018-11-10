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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.AUTHORITY;

import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_MOONPHASE;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_FIRST;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_FULL;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_NEW;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_MOON_THIRD;

import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.QUERY_SEASONS;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_AUTUMN;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_SUMMER;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_VERNAL;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_WINTER;
import static com.forrestguice.suntimeswidget.calculator.CalculatorProviderContract.COLUMN_SEASON_YEAR;

/**
 * CalculatorProvider
 * @see CalculatorProviderContract
 */
public class CalculatorProvider extends ContentProvider
{
    private static final int URIMATCH_SEASONS = 10;
    private static final int URIMATCH_SEASONS_FOR_YEAR = 20;
    private static final int URIMATCH_SEASONS_FOR_RANGE = 30;
    private static final int URIMATCH_MOONPHASE = 40;
    private static final int URIMATCH_MOONPHASE_FOR_DATE = 50;
    private static final int URIMATCH_MOONPHASE_FOR_RANGE = 60;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, QUERY_SEASONS, URIMATCH_SEASONS);
        uriMatcher.addURI(AUTHORITY, QUERY_SEASONS + "/#", URIMATCH_SEASONS_FOR_YEAR);
        uriMatcher.addURI(AUTHORITY, QUERY_SEASONS + "/*", URIMATCH_SEASONS_FOR_RANGE);
        uriMatcher.addURI(AUTHORITY, QUERY_MOONPHASE, URIMATCH_MOONPHASE);
        uriMatcher.addURI(AUTHORITY, QUERY_MOONPHASE + "/#", URIMATCH_MOONPHASE_FOR_DATE);
        uriMatcher.addURI(AUTHORITY, QUERY_MOONPHASE + "/*", URIMATCH_MOONPHASE_FOR_RANGE);
    }

    @Override
    public boolean onCreate()
    {
        return true;
    }

    private static SuntimesCalculator sunSource, moonSource;
    private static void initCalculator(Context context)
    {
        WidgetSettings.Location location = WidgetSettings.loadLocationPref(context, 0);
        TimeZone timezone = TimeZone.getTimeZone(WidgetSettings.loadTimezonePref(context, 0));

        if (sunSource == null)
        {
            SuntimesCalculatorDescriptor sunSourceDesc = WidgetSettings.loadCalculatorModePref(context, 0);
            SuntimesCalculatorFactory sunSourceFactory = new SuntimesCalculatorFactory(context, sunSourceDesc);
            sunSource = sunSourceFactory.createCalculator(location, timezone);
        }

        if (moonSource == null)
        {
            SuntimesCalculatorDescriptor moonSourceDesc = WidgetSettings.loadCalculatorModePref(context, 0, "moon");
            SuntimesCalculatorFactory moonSourceFactory = new SuntimesCalculatorFactory(context, moonSourceDesc);
            moonSource = moonSourceFactory.createCalculator(location, timezone);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        initCalculator(getContext());
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        Calendar[] range;

        Log.d("CalculatorProvider", "URI: " + uri);
        Cursor retValue = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_SEASONS:
                Log.d("CalculatorProvider", "URIMATCH_SEASONS");
                retValue = querySeasons(new Calendar[] {now, now}, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SEASONS_FOR_YEAR:
                Log.d("CalculatorProvider", "URIMATCH_SEASONS_FOR_YEAR");
                date.set(Calendar.YEAR, (int)ContentUris.parseId(uri));
                retValue = querySeasons(new Calendar[] { date, date }, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SEASONS_FOR_RANGE:
                Log.d("CalculatorProvider", "URIMATCH_SEASONS_FOR_RANGE");
                range = parseYearRange(uri.getLastPathSegment());
                retValue = querySeasons(range, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_MOONPHASE:
                Log.d("CalculatorProvider", "URIMATCH_MOONPHASE");
                retValue = queryMoonPhase(new Calendar[] {now, now}, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_MOONPHASE_FOR_DATE:
                Log.d("CalculatorProvider", "URIMATCH_MOONPHASE_FOR_DATE");
                date.setTimeInMillis(ContentUris.parseId(uri));
                retValue = queryMoonPhase(new Calendar[] {date, date}, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_MOONPHASE_FOR_RANGE:
                Log.d("CalculatorProvider", "URIMATCH_MOONPHASE_FOR_RANGE");
                range = parseDateRange(uri.getLastPathSegment());
                retValue = queryMoonPhase(range, uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                Log.e("CalculatorProvider", "Unrecognized URI! " + uri);
                break;
        }
        return retValue;
    }

    protected Calendar[] parseDateRange(String rangeSegment)
    {
        Calendar[] retValue = new Calendar[2];
        String[] rangeString = rangeSegment.split("-");
        if (rangeString.length == 2)
        {
            try {
                retValue[0] = Calendar.getInstance();
                retValue[0].setTimeInMillis(Long.parseLong(rangeString[0]));

                retValue[1] = Calendar.getInstance();
                retValue[1].setTimeInMillis(Long.parseLong(rangeString[1]) + 1000);

            } catch (NumberFormatException e) {
                Log.w("CalculatorProvider", "Invalid range! " + rangeSegment);
                retValue[0] = retValue[1] = Calendar.getInstance();
            }
        } else {
            Log.w("CalculatorProvider", "Invalid range! " + rangeSegment);
            retValue[0] = retValue[1] = Calendar.getInstance();
        }
        Log.d("DEBUG", "startDate: " + retValue[0].getTimeInMillis() + ", endDate: " + retValue[1].getTimeInMillis());
        return retValue;
    }

    protected Calendar[] parseYearRange(String rangeSegment)
    {
        Calendar[] retValue = new Calendar[2];
        String[] rangeString = rangeSegment.split("-");
        if (rangeString.length == 2)
        {
            try {
                retValue[0] = Calendar.getInstance();
                retValue[0].set(Calendar.YEAR, Integer.parseInt(rangeString[0]));

                retValue[1] = Calendar.getInstance();
                retValue[1].set(Calendar.YEAR, Integer.parseInt(rangeString[1]) + 1);

            } catch (NumberFormatException e) {
                Log.w("CalculatorProvider", "Invalid range! " + rangeSegment);
                retValue[0] = retValue[1] = Calendar.getInstance();
            }
        } else {
            Log.w("CalculatorProvider", "Invalid range! " + rangeSegment);
            retValue[0] = retValue[1] = Calendar.getInstance();
        }
        Log.d("DEBUG", "startDate: " + retValue[0].get(Calendar.YEAR) + ", endDate: " + retValue[1].get(Calendar.YEAR));
        return retValue;
    }

    /**
     * queryMoonPhase
     */
    private Cursor queryMoonPhase(Calendar[] range, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : new String[] { COLUMN_MOON_NEW, COLUMN_MOON_FIRST, COLUMN_MOON_FULL, COLUMN_MOON_THIRD });
        MatrixCursor retValue = new MatrixCursor(columns);
        if (moonSource != null)
        {
            ArrayList<Calendar> events = new ArrayList<>();
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(range[0].getTimeInMillis());
            do {
                events.clear();
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    Calendar event;
                    switch (columns[i])
                    {
                        case COLUMN_MOON_NEW:
                            events.add(event = moonSource.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.NEW , date));
                            row[i] = event.getTimeInMillis();
                            break;

                        case COLUMN_MOON_FIRST:
                            events.add(event = moonSource.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.FIRST_QUARTER, date));
                            row[i] = event.getTimeInMillis();
                            break;

                        case COLUMN_MOON_FULL:
                            events.add(event = moonSource.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.FULL, date));
                            row[i] = event.getTimeInMillis();
                            break;

                        case COLUMN_MOON_THIRD:
                            events.add(event = moonSource.getMoonPhaseNextDate(SuntimesCalculator.MoonPhase.THIRD_QUARTER, date));
                            row[i] = event.getTimeInMillis();
                            break;

                        default:
                            row[i] = null; break;
                    }
                }
                retValue.addRow(row);

                Collections.sort(events);
                Calendar latest = (events.size() > 1) ? events.get(events.size()-1)
                                : (events.size() > 0) ? events.get(0) : null;

                date.setTimeInMillis(latest != null ? latest.getTimeInMillis() + 1000
                                                    : range[1].getTimeInMillis() + 1000);
            } while (date.before(range[1]));

        }  Log.d("DEBUG", "moonSource is null!");
        return retValue;
    }

    /**
     * querySeasons
     */
    private Cursor querySeasons(Calendar[] range, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : new String[] { COLUMN_SEASON_YEAR, COLUMN_SEASON_VERNAL, COLUMN_SEASON_SUMMER, COLUMN_SEASON_AUTUMN, COLUMN_SEASON_WINTER });
        MatrixCursor retValue = new MatrixCursor(columns);
        if (sunSource != null)
        {
            Calendar year = Calendar.getInstance();
            year.setTimeInMillis(range[0].getTimeInMillis());
            do {
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_SEASON_YEAR:
                            row[i] = year.get(Calendar.YEAR); break;
                        case COLUMN_SEASON_VERNAL:
                            row[i] = sunSource.getVernalEquinoxForYear(year).getTimeInMillis(); break;
                        case COLUMN_SEASON_SUMMER:
                            row[i] = sunSource.getSummerSolsticeForYear(year).getTimeInMillis(); break;
                        case COLUMN_SEASON_AUTUMN:
                            row[i] = sunSource.getAutumnalEquinoxForYear(year).getTimeInMillis(); break;
                        case COLUMN_SEASON_WINTER:
                            row[i] = sunSource.getWinterSolsticeForYear(year).getTimeInMillis(); break;
                        default:
                            row[i] = null; break;
                    }
                }
                retValue.addRow(row);
                year.set(Calendar.YEAR, year.get(Calendar.YEAR) + 1);
            } while (year.before(range[1]));

        } else Log.d("DEBUG", "sunSource is null!");
        return retValue;
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
}
