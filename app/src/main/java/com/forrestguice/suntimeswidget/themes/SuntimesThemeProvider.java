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
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import java.util.HashMap;

import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.AUTHORITY;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEME;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEMES;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEMES_PROJECTION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.QUERY_THEME_PROJECTION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_DISPLAYSTRING;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ISDEFAULT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NAME;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PROVIDER_VERSION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_VERSION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.VERSION_CODE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.VERSION_NAME;

/**
 * SuntimesThemeProvider
 * @see SuntimesThemeContract
 */
public class SuntimesThemeProvider extends ContentProvider
{
    private static final int URIMATCH_THEME = 0;
    private static final int URIMATCH_THEMES = 10;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, QUERY_THEME, URIMATCH_THEME);
        uriMatcher.addURI(AUTHORITY, QUERY_THEMES, URIMATCH_THEMES);
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

            case URIMATCH_THEMES:
                Log.d(getClass().getSimpleName(), "URIMATCH_LIST");
                retValue = queryThemes(uri, projection, selectionMap, sortOrder);
                break;

            default:
                Log.e(getClass().getSimpleName(), "Unrecognized URI! " + uri);
                break;
        }
        return retValue;
    }

    /**
     * queryThemes
     */
    private Cursor queryThemes(@NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        Context context = getContext();
        String[] columns = (projection != null ? projection : QUERY_THEMES_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);

        if (context != null)
        {
            for (SuntimesTheme.ThemeDescriptor themeDesc : WidgetThemes.getValues()) {
                retValue.addRow(createRow(themeDesc, columns));
            }
        }
        return retValue;
    }

    private Object[] createRow(SuntimesTheme.ThemeDescriptor themeDesc, String[] columns)
    {
        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case THEME_PROVIDER_VERSION:
                    row[i] = VERSION_NAME;
                    break;

                case THEME_PROVIDER_VERSION_CODE:
                    row[i] = VERSION_CODE;
                    break;

                case THEME_NAME:
                    row[i] = themeDesc.name();
                    break;

                case THEME_VERSION:
                    row[i] = themeDesc.version();
                    break;

                case THEME_ISDEFAULT:
                    row[i] = (themeDesc.isDefault() ? 1 : 0);
                    break;

                case THEME_DISPLAYSTRING:
                    row[i] = themeDesc.displayString();
                    break;

                default:
                    row[i] = null;
                    break;
            }
        }
        return row;
    }

    /**
     * queryTheme
     */
    private Cursor queryTheme(@NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selection, @Nullable String sortOrder)
    {
        Context context = getContext();
        String[] columns = (projection != null ? projection : QUERY_THEME_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);

        if (context != null)
        {
            String themeID = uri.getLastPathSegment();
            SuntimesTheme theme = WidgetThemes.loadTheme(context, themeID);
            retValue.addRow(createRow(theme, columns));

        } else Log.e(getClass().getSimpleName(), "context is null!");
        return retValue;
    }

    private Object[] createRow(SuntimesTheme theme, String[] columns)
    {
        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case THEME_PROVIDER_VERSION:
                    row[i] = VERSION_NAME;
                    break;

                case THEME_PROVIDER_VERSION_CODE:
                    row[i] = VERSION_CODE;
                    break;

                case THEME_NAME:
                    row[i] = theme.themeName();
                    break;

                case THEME_VERSION:
                    row[i] = theme.themeVersion();
                    break;

                case THEME_ISDEFAULT:
                    row[i] = (theme.isDefault() ? 1 : 0);
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
        return row;
    }

}