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

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Solstice and Equinox
 *
 *   The following URIs are supported:
 *       content://com.forrestguice.suntimeswidget.calculator.provider/seasons          .. get vernal, summer, autumn, and winter dates for this year
 *       content://com.forrestguice.suntimeswidget.calculator.provider/seasons/[year]   .. get vernal, summer, autumn, and winter dates for some year
 *
 *   The result will be a row containing timestamps:
 *       [COLUMN_YEAR(int), COLUMM_SEASON_VERNAL(long), COLUMN_SEASON_SUMMER(long), COLUMN_SEASON_AUTUMN(long), COLUMN_SEASON_WINTER(long)]
 */
public class SuntimesCalculatorProvider extends ContentProvider
{
    public static final String AUTHORITY = "com.forrestguice.suntimeswidget.calculator.provider";

    public static final String QUERY_SEASONS = "seasons";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_SEASON_VERNAL = "vernal";
    public static final String COLUMN_SEASON_SUMMER = "summer";
    public static final String COLUMN_SEASON_AUTUMN = "autumn";
    public static final String COLUMN_SEASON_WINTER = "winter";

    private static final int URIMATCH_SEASONS = 10;
    private static final int URIMATCH_SEASONS_FORYEAR = 20;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, QUERY_SEASONS, URIMATCH_SEASONS);
        uriMatcher.addURI(AUTHORITY, QUERY_SEASONS + "/#", URIMATCH_SEASONS_FORYEAR);
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
        Cursor retValue = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_SEASONS:
                Calendar now = Calendar.getInstance();
                retValue = querySeasons(now, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SEASONS_FORYEAR:
                Calendar date = Calendar.getInstance();
                date.set(Calendar.YEAR, (int)ContentUris.parseId(uri));
                retValue = querySeasons(date, uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                Log.e("CalculatorProvider", "Unrecognized URI! " + uri);
                break;
        }
        return retValue;
    }

    private Cursor querySeasons(Calendar year, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : new String[] { COLUMN_YEAR, COLUMN_SEASON_VERNAL, COLUMN_SEASON_SUMMER, COLUMN_SEASON_AUTUMN, COLUMN_SEASON_WINTER });
        MatrixCursor retValue = new MatrixCursor(columns);
        if (sunSource != null)
        {
            Object[] row = new Object[columns.length];
            for (int i=0; i<columns.length; i++)
            {
                switch (columns[i])
                {
                    case COLUMN_YEAR:
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
