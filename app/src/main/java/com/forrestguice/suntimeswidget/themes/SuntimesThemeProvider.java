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
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.calculator.CalculatorProvider;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import java.util.HashMap;

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
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.VERSION_CODE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.VERSION_NAME;

/**
 * SuntimesThemeProvider
 * @see SuntimesThemeContract
 */
public class SuntimesThemeProvider extends ContentProvider
{
    private static final int URIMATCH_THEMES = 0;
    private static final int URIMATCH_THEME = 10;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY(), QUERY_THEMES, URIMATCH_THEMES);                 // content://AUTHORITY/themes
        uriMatcher.addURI(AUTHORITY(), QUERY_THEME + "/*", URIMATCH_THEME);      // content://AUTHORITY/[themeName]
    }

    protected static String AUTHORITY() {
        return BuildConfig.SUNTIMES_AUTHORITY_ROOT + ".theme.provider";
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
                Log.d(getClass().getSimpleName(), "URIMATCH_THEMES");
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
        WidgetThemes.initThemes(context);

        String[] columns = (projection != null ? projection : QUERY_THEMES_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);

        if (context != null)
        {
            int i = 0;
            for (SuntimesTheme.ThemeDescriptor themeDesc : WidgetThemes.getValues()) {
                retValue.addRow(createRow(themeDesc, columns, i++));
            }
        }
        return retValue;
    }

    private Object[] createRow(SuntimesTheme.ThemeDescriptor themeDesc, String[] columns, int rowID)
    {
        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case "_id":
                    row[i] = rowID;
                    break;

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
        WidgetThemes.initThemes(context);

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

                case THEME_BACKGROUND:
                    row[i] = null;  // TODO
                    break;
                case THEME_BACKGROUND_COLOR:
                    row[i] = theme.getBackgroundColor();
                    break;
                case THEME_PADDING_LEFT:
                    row[i] = theme.getPadding()[0];
                    break;
                case THEME_PADDING_TOP:
                    row[i] = theme.getPadding()[1];
                    break;
                case THEME_PADDING_RIGHT:
                    row[i] = theme.getPadding()[2];
                    break;
                case THEME_PADDING_BOTTOM:
                    row[i] = theme.getPadding()[3];
                    break;

                case THEME_TEXTCOLOR:
                    row[i] = theme.getTextColor();
                    break;
                case THEME_TITLECOLOR:
                    row[i] = theme.getTitleColor();
                    break;
                case THEME_TIMECOLOR:
                    row[i] = theme.getTimeColor();
                    break;
                case THEME_TIMESUFFIXCOLOR:
                    row[i] = theme.getTimeSuffixColor();
                    break;
                case THEME_ACTIONCOLOR:
                    row[i] = theme.getActionColor();
                    break;
                case THEME_ACCENTCOLOR:
                    row[i] = theme.getAccentColor();
                    break;

                case THEME_TITLESIZE:
                    row[i] = null;  // TODO
                    break;
                case THEME_TEXTSIZE:
                    row[i] = null;  // TODO
                    break;
                case THEME_TIMESIZE:
                    row[i] = null;  // TODO
                    break;
                case THEME_TIMESUFFIXSIZE:
                    row[i] = null;  // TODO
                    break;

                case THEME_TITLEBOLD:
                    row[i] = theme.getTitleBold() ? 1 : 0;
                    break;
                case THEME_TIMEBOLD:
                    row[i] = theme.getTimeBold() ? 1 : 0;
                    break;

                case THEME_MOONRISECOLOR:
                    row[i] = theme.getMoonriseTextColor();
                    break;
                case THEME_MOONSETCOLOR:
                    row[i] = theme.getMoonsetTextColor();
                    break;

                case THEME_MOONWANINGCOLOR:
                    row[i] = theme.getMoonWaningColor();
                    break;
                case THEME_MOONWAXINGCOLOR:
                    row[i] = theme.getMoonWaxingColor();
                    break;
                case THEME_MOONNEWCOLOR:
                    row[i] = theme.getMoonNewColor();
                    break;
                case THEME_MOONFULLCOLOR:
                    row[i] = theme.getMoonFullColor();
                    break;

                case THEME_MOONFULL_STROKE_WIDTH:
                    row[i] = null;  // TODO
                    break;
                case THEME_MOONNEW_STROKE_WIDTH:
                    row[i] = null;  // TODO
                    break;

                case THEME_NOONICON_FILL_COLOR:
                    row[i] = theme.getNoonIconColor();
                    break;
                case THEME_NOONICON_STROKE_COLOR:
                    row[i] = theme.getNoonIconStrokeColor();
                    break;
                case THEME_NOONICON_STROKE_WIDTH:
                    row[i] = null;  // TODO
                    break;

                case THEME_RISEICON_FILL_COLOR:
                    row[i] = theme.getSunriseIconColor();
                    break;
                case THEME_RISEICON_STROKE_COLOR:
                    row[i] = theme.getSunriseIconStrokeColor();
                    break;
                case THEME_RISEICON_STROKE_WIDTH:
                    row[i] = null;  // TODO
                    break;

                case THEME_SETICON_FILL_COLOR:
                    row[i] = theme.getSunsetIconColor();
                    break;
                case THEME_SETICON_STROKE_COLOR:
                    row[i] = theme.getSunsetIconStrokeColor();
                    break;
                case THEME_SETICON_STROKE_WIDTH:
                    row[i] = null;  // TODO
                    break;

                case THEME_SUNRISECOLOR:
                    row[i] = theme.getSunriseTextColor();
                    break;
                case THEME_NOONCOLOR:
                    row[i] = theme.getNoonTextColor();
                    break;
                case THEME_SUNSETCOLOR:
                    row[i] = theme.getSunsetTextColor();
                    break;

                case THEME_DAYCOLOR:
                    row[i] = theme.getDayColor();
                    break;
                case THEME_CIVILCOLOR:
                    row[i] = theme.getCivilColor();
                    break;
                case THEME_NAUTICALCOLOR:
                    row[i] = theme.getNauticalColor();
                    break;
                case THEME_ASTROCOLOR:
                    row[i] = theme.getAstroColor();
                    break;
                case THEME_NIGHTCOLOR:
                    row[i] = theme.getNightColor();
                    break;
                case THEME_GRAPH_POINT_FILL_COLOR:
                    row[i] = theme.getGraphPointFillColor();
                    break;
                case THEME_GRAPH_POINT_STROKE_COLOR:
                    row[i] = theme.getGraphPointStrokeColor();
                    break;

                case THEME_MAP_BACKGROUNDCOLOR:
                    row[i] = theme.getMapBackgroundColor();
                    break;
                case THEME_MAP_FOREGROUNDCOLOR:
                    row[i] = theme.getMapForegroundColor();
                    break;
                case THEME_MAP_SHADOWCOLOR:
                    row[i] = theme.getMapShadowColor();
                    break;
                case THEME_MAP_HIGHLIGHTCOLOR:
                    row[i] = theme.getMapHighlightColor();
                    break;

                case THEME_SPRINGCOLOR:
                    row[i] = theme.getSpringColor();
                    break;
                case THEME_SUMMERCOLOR:
                    row[i] = theme.getSummerColor();
                    break;
                case THEME_FALLCOLOR:
                    row[i] = theme.getFallColor();
                    break;
                case THEME_WINTERCOLOR:
                    row[i] = theme.getWinterColor();
                    break;

                default:
                    row[i] = null;
                    break;
            }
        }
        return row;
    }

}