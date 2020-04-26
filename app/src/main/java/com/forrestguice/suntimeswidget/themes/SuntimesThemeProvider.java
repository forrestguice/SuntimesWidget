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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.CalculatorProvider;
import com.forrestguice.suntimeswidget.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import java.util.HashMap;

import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.AUTHORITY;

import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEME;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEME_PROJECTION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_DISPLAYSTRING;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NAME;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PROVIDER_VERSION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PROVIDER_VERSION_CODE;

/**
 * SuntimesThemeProvider
 * @see SuntimesThemeContract
 */
public class SuntimesThemeProvider extends ContentProvider
{
    private static final int URIMATCH_THEME = 0;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, QUERY_THEME, URIMATCH_THEME);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * query
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        HashMap<String, String> selectionMap = CalculatorProvider.processSelection(CalculatorProvider.processSelectionArgs(selection, selectionArgs));
        Cursor retValue = null;

        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_THEME:
                Log.d(getClass().getSimpleName(), "URIMATCH_THEME");
                retValue = queryTheme(uri, projection, selectionMap, sortOrder);
                break;

            default:
                Log.e(getClass().getSimpleName(), "Unrecognized URI! " + uri);
                break;
        }
        return retValue;
    }

    /**
     * queryTheme
     */
    private Cursor queryTheme(@NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        String themeID = uri.getLastPathSegment();
        Context context = getContext();
        String[] columns = (projection != null ? projection : QUERY_THEME_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);

        if (context != null)
        {
            SuntimesTheme theme = WidgetThemes.loadTheme(context, themeID);
            Object[] row = new Object[columns.length];
            for (int i=0; i<columns.length; i++)
            {
                switch (columns[i])
                {
                    case THEME_PROVIDER_VERSION:
                        row[i] = CalculatorProviderContract.VERSION_NAME;
                        break;

                    case THEME_PROVIDER_VERSION_CODE:
                        row[i] = CalculatorProviderContract.VERSION_CODE;
                        break;

                    case THEME_NAME:
                        row[i] = theme.themeName();
                        break;

                    case THEME_DISPLAYSTRING:
                        row[i] = theme.themeDisplayString();
                        break;

                    // TODO

                    default:
                        row[i] = null;
                        break;
                }
            }
            retValue.addRow(row);

        } else Log.e(getClass().getSimpleName(), "context is null!");
        return retValue;
    }

}
