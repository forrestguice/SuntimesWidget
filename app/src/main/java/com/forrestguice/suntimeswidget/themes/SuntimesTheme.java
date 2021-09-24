/**
    Copyright (C) 2014-2020 Forrest Guice
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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ACCENTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ACTIONCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_ASTROCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_BACKGROUND;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_BACKGROUND_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_CIVILCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_DAYCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_DISPLAYSTRING;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_FALLCOLOR;
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
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOON_STROKE_DEF;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOON_STROKE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_MOON_STROKE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NAME;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NAUTICALCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NIGHTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH_DEF;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_BOTTOM;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_LEFT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_RIGHT;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_PADDING_TOP;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH_DEF;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_FILL_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_COLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH_DEF;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SPRINGCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUMMERCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUNRISECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_SUNSETCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTSIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTSIZE_DEF;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTSIZE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TEXTSIZE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMEBOLD;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESIZE_DEF;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESIZE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESIZE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXCOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE_DEF;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLEBOLD;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLECOLOR;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLESIZE;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLESIZE_DEF;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLESIZE_MAX;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_TITLESIZE_MIN;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_VERSION;
import static com.forrestguice.suntimeswidget.themes.SuntimesThemeContract.THEME_WINTERCOLOR;

public class SuntimesTheme
{
    public static final String THEME_KEY = "theme_";

    private ThemeDescriptor descriptor;

    protected String themeName;
    protected int themeVersion;
    protected boolean themeIsDefault;
    protected String themeDisplayString;

    protected ThemeBackground themeBackground;
    protected int themeBackgroundColor = Color.DKGRAY;
    protected int[] themePadding = {0, 0, 0, 0};
    private int[] themePaddingPixels = {-1, -1, -1, -1};

    protected int themeTitleColor;
    protected int themeTextColor;
    protected int themeTimeColor;
    protected int themeTimeSuffixColor;
    protected int themeActionColor;
    protected int themeAccentColor;

    protected int themeSunriseTextColor;
    protected int themeSunriseIconColor;
    protected int themeSunriseIconStrokeColor;
    protected int themeSunriseIconStrokeWidth;
    protected int themeSunriseIconStrokePixels = -1;

    protected int themeNoonTextColor;
    protected int themeNoonIconColor;
    protected int themeNoonIconStrokeColor;
    protected int themeNoonIconStrokeWidth;
    protected int themeNoonIconStrokePixels = -1;

    protected int themeSunsetTextColor;
    protected int themeSunsetIconColor;
    protected int themeSunsetIconStrokeColor;
    protected int themeSunsetIconStrokeWidth;
    protected int themeSunsetIconStrokePixels = -1;

    protected int themeDayColor;
    protected int themeCivilColor;
    protected int themeNauticalColor;
    protected int themeAstroColor;
    protected int themeNightColor;

    protected int themeSpringColor;
    protected int themeSummerColor;
    protected int themeFallColor;
    protected int themeWinterColor;

    protected int themeMapBackgroundColor;
    protected int themeMapForegroundColor;
    protected int themeMapShadowColor;
    protected int themeMapHighlightColor;

    protected int themeMoonriseTextColor;
    protected int themeMoonsetTextColor;
    protected int themeMoonWaningColor;
    protected int themeMoonNewColor;
    protected int themeMoonWaxingColor;
    protected int themeMoonFullColor;

    protected int themeMoonFullStroke;
    protected int themeMoonFullStrokePixels = -1;

    protected int themeMoonNewStroke;
    protected int themeMoonNewStrokePixels = -1;

    protected float themeTitleSize = THEME_TITLESIZE_DEF;
    protected float themeTextSize = THEME_TEXTSIZE_DEF;
    protected float themeTimeSize = THEME_TIMESIZE_DEF;
    protected float themeTimeSuffixSize = THEME_TIMESUFFIXSIZE_DEF;

    protected boolean themeTitleBold = false;
    protected boolean themeTimeBold = false;

    public SuntimesTheme()
    {
    }

    public SuntimesTheme(SuntimesTheme otherTheme)
    {
        this.themeVersion = otherTheme.themeVersion;
        this.themeName = otherTheme.themeName;
        this.themeIsDefault = otherTheme.themeIsDefault;
        this.themeDisplayString = otherTheme.themeDisplayString;

        this.themeBackground = otherTheme.themeBackground;
        this.themeBackgroundColor = otherTheme.themeBackgroundColor;
        this.themePadding[0] = otherTheme.themePadding[0];
        this.themePadding[1] = otherTheme.themePadding[1];
        this.themePadding[2] = otherTheme.themePadding[2];
        this.themePadding[3] = otherTheme.themePadding[3];

        this.themeTextColor = otherTheme.themeTextColor;
        this.themeTitleColor = otherTheme.themeTitleColor;
        this.themeTimeColor = otherTheme.themeTimeColor;
        this.themeTimeSuffixColor = otherTheme.themeTimeSuffixColor;

        this.themeSunriseTextColor = otherTheme.themeSunriseTextColor;
        this.themeSunriseIconColor = otherTheme.themeSunriseIconColor;
        this.themeSunriseIconStrokeColor = otherTheme.themeSunriseIconStrokeColor;
        this.themeSunriseIconStrokeWidth = otherTheme.themeSunriseIconStrokeWidth;

        this.themeNoonTextColor = otherTheme.themeNoonTextColor;
        this.themeNoonIconColor = otherTheme.themeNoonIconColor;
        this.themeNoonIconStrokeColor = otherTheme.themeNoonIconStrokeColor;
        this.themeNoonIconStrokeWidth = otherTheme.themeNoonIconStrokeWidth;

        this.themeSunsetTextColor = otherTheme.themeSunsetTextColor;
        this.themeSunsetIconColor = otherTheme.themeSunsetIconColor;
        this.themeSunsetIconStrokeColor = otherTheme.themeSunsetIconStrokeColor;
        this.themeSunsetIconStrokeWidth = otherTheme.themeSunsetIconStrokeWidth;

        this.themeMoonriseTextColor = otherTheme.themeMoonriseTextColor;
        this.themeMoonsetTextColor = otherTheme.themeMoonsetTextColor;
        this.themeMoonWaningColor = otherTheme.themeMoonWaningColor;
        this.themeMoonNewColor = otherTheme.themeMoonNewColor;
        this.themeMoonWaxingColor = otherTheme.themeMoonWaxingColor;
        this.themeMoonFullColor = otherTheme.themeMoonFullColor;

        this.themeMoonFullStroke = otherTheme.themeMoonFullStroke;
        this.themeMoonNewStroke = otherTheme.themeMoonNewStroke;

        this.themeDayColor = otherTheme.themeDayColor;
        this.themeCivilColor = otherTheme.themeCivilColor;
        this.themeNauticalColor = otherTheme.themeNauticalColor;
        this.themeAstroColor = otherTheme.themeAstroColor;
        this.themeNightColor = otherTheme.themeNightColor;

        this.themeSpringColor = otherTheme.themeSpringColor;
        this.themeSummerColor = otherTheme.themeSummerColor;
        this.themeFallColor = otherTheme.themeFallColor;
        this.themeWinterColor = otherTheme.themeWinterColor;

        this.themeMapBackgroundColor = otherTheme.themeMapBackgroundColor;
        this.themeMapForegroundColor = otherTheme.themeMapForegroundColor;
        this.themeMapShadowColor = otherTheme.themeMapShadowColor;
        this.themeMapHighlightColor = otherTheme.themeMapHighlightColor;

        this.themeTitleSize = otherTheme.themeTitleSize;
        this.themeTextSize = otherTheme.themeTextSize;
        this.themeTimeSize = otherTheme.themeTimeSize;
        this.themeTimeSuffixSize = otherTheme.themeTimeSuffixSize;

        this.themeTitleBold = otherTheme.themeTitleBold;
        this.themeTimeBold = otherTheme.themeTimeBold;
    }

    public boolean initTheme( Context context, String themesPrefix, String themeName, SuntimesTheme defaultTheme )
    {
        long bench_start = System.nanoTime();

        SharedPreferences themes = context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE);
        String theme = themePrefix(themeName);

        this.themeVersion = themes.getInt(theme + THEME_VERSION, defaultTheme.themeVersion);
        this.themeName = themes.getString(theme + THEME_NAME, defaultTheme.themeName);
        this.themeIsDefault = themes.getBoolean(theme + THEME_ISDEFAULT, false);
        this.themeDisplayString = themes.getString(theme + THEME_DISPLAYSTRING, defaultTheme.themeDisplayString);

        this.themeBackground = defaultTheme.themeBackground;
        String backgroundName;
        try {
            backgroundName = themes.getString(theme + THEME_BACKGROUND, null);
            if (backgroundName != null)
            {
                try {
                    this.themeBackground = ThemeBackground.valueOf(backgroundName);
                } catch (IllegalArgumentException e) {
                    Log.w("initTheme", "unable to find theme background " + backgroundName);
                    this.themeBackground = ThemeBackground.DARK;
                }
            }
        } catch (ClassCastException e) {
            Log.w("initTheme", "legacy theme: " + themeName);
            int backgroundID = themes.getInt(theme + THEME_BACKGROUND, 0);
            this.themeBackground = ThemeBackground.getThemeBackground(backgroundID);
        }

        this.themeBackgroundColor = themes.getInt( theme + THEME_BACKGROUND_COLOR, defaultTheme.getBackgroundColor() );

        this.themePadding[0] = themes.getInt( theme + THEME_PADDING_LEFT, defaultTheme.themePadding[0] );
        this.themePadding[1] = themes.getInt( theme + THEME_PADDING_TOP, defaultTheme.themePadding[1] );
        this.themePadding[2] = themes.getInt( theme + THEME_PADDING_RIGHT, defaultTheme.themePadding[2] );
        this.themePadding[3] = themes.getInt( theme + THEME_PADDING_BOTTOM, defaultTheme.themePadding[3] );

        this.themeTextColor = themes.getInt( theme + THEME_TEXTCOLOR, defaultTheme.themeTextColor );
        this.themeTitleColor = themes.getInt( theme + THEME_TITLECOLOR, defaultTheme.themeTitleColor );
        this.themeTimeColor = themes.getInt( theme + THEME_TIMECOLOR, defaultTheme.themeTimeColor );
        this.themeTimeSuffixColor = themes.getInt( theme + THEME_TIMESUFFIXCOLOR, defaultTheme.themeTimeSuffixColor );
        this.themeActionColor = themes.getInt( theme + THEME_ACTIONCOLOR, defaultTheme.themeActionColor );
        this.themeAccentColor = themes.getInt( theme + THEME_ACCENTCOLOR, defaultTheme.themeAccentColor );

        this.themeSunriseTextColor = themes.getInt( theme + THEME_SUNRISECOLOR, defaultTheme.themeSunriseTextColor );
        this.themeSunriseIconColor = themes.getInt( theme + THEME_RISEICON_FILL_COLOR, defaultTheme.themeSunriseIconColor );
        this.themeSunriseIconStrokeColor = themes.getInt( theme + THEME_RISEICON_STROKE_COLOR, defaultTheme.themeSunriseIconStrokeColor );
        this.themeSunriseIconStrokeWidth = themes.getInt( theme + THEME_RISEICON_STROKE_WIDTH, defaultTheme.themeSunriseIconStrokeWidth );

        this.themeNoonTextColor = themes.getInt( theme + THEME_NOONCOLOR, defaultTheme.themeNoonTextColor );
        this.themeNoonIconColor = themes.getInt( theme + THEME_NOONICON_FILL_COLOR, defaultTheme.themeNoonIconColor );
        this.themeNoonIconStrokeColor = themes.getInt( theme + THEME_NOONICON_STROKE_COLOR, defaultTheme.themeNoonIconStrokeColor );
        this.themeNoonIconStrokeWidth = themes.getInt( theme + THEME_NOONICON_STROKE_WIDTH, defaultTheme.themeNoonIconStrokeWidth );

        this.themeSunsetTextColor = themes.getInt( theme + THEME_SUNSETCOLOR, defaultTheme.themeSunsetTextColor );
        this.themeSunsetIconColor = themes.getInt( theme + THEME_SETICON_FILL_COLOR, defaultTheme.themeSunsetIconColor );
        this.themeSunsetIconStrokeColor = themes.getInt( theme + THEME_SETICON_STROKE_COLOR, defaultTheme.themeSunsetIconStrokeColor );
        this.themeSunsetIconStrokeWidth = themes.getInt( theme + THEME_SETICON_STROKE_WIDTH, defaultTheme.themeSunsetIconStrokeWidth );

        this.themeMoonriseTextColor = themes.getInt( theme + THEME_MOONRISECOLOR, defaultTheme.themeMoonriseTextColor );
        this.themeMoonsetTextColor = themes.getInt( theme + THEME_MOONSETCOLOR, defaultTheme.themeMoonsetTextColor );
        this.themeMoonWaningColor = themes.getInt( theme + THEME_MOONWANINGCOLOR, defaultTheme.themeMoonWaningColor );
        this.themeMoonNewColor = themes.getInt( theme + THEME_MOONNEWCOLOR, defaultTheme.themeMoonNewColor );
        this.themeMoonWaxingColor = themes.getInt( theme + THEME_MOONWAXINGCOLOR, defaultTheme.themeMoonWaxingColor );
        this.themeMoonFullColor = themes.getInt( theme + THEME_MOONFULLCOLOR, defaultTheme.themeMoonFullColor );

        this.themeMoonFullStroke = themes.getInt( theme + THEME_MOONFULL_STROKE_WIDTH, defaultTheme.themeMoonFullStroke );
        this.themeMoonNewStroke = themes.getInt( theme + THEME_MOONNEW_STROKE_WIDTH, defaultTheme.themeMoonNewStroke );

        this.themeDayColor = themes.getInt( theme + THEME_DAYCOLOR, defaultTheme.themeDayColor );
        this.themeCivilColor = themes.getInt( theme + THEME_CIVILCOLOR, defaultTheme.themeCivilColor );
        this.themeNauticalColor = themes.getInt( theme + THEME_NAUTICALCOLOR, defaultTheme.themeNauticalColor );
        this.themeAstroColor = themes.getInt( theme + THEME_ASTROCOLOR, defaultTheme.themeAstroColor );
        this.themeNightColor = themes.getInt( theme + THEME_NIGHTCOLOR, defaultTheme.themeNightColor );

        this.themeSpringColor = themes.getInt( theme + THEME_SPRINGCOLOR, defaultTheme.themeSpringColor );
        this.themeSummerColor = themes.getInt( theme + THEME_SUMMERCOLOR, defaultTheme.themeSummerColor );
        this.themeFallColor = themes.getInt( theme + THEME_FALLCOLOR, defaultTheme.themeFallColor );
        this.themeWinterColor = themes.getInt( theme + THEME_WINTERCOLOR, defaultTheme.themeWinterColor );

        this.themeMapBackgroundColor = themes.getInt(theme + THEME_MAP_BACKGROUNDCOLOR, defaultTheme.themeMapBackgroundColor);
        this.themeMapForegroundColor = themes.getInt(theme + THEME_MAP_FOREGROUNDCOLOR, defaultTheme.themeMapForegroundColor);
        this.themeMapShadowColor = themes.getInt(theme + THEME_MAP_SHADOWCOLOR, defaultTheme.themeMapShadowColor);
        this.themeMapHighlightColor = themes.getInt(theme + THEME_MAP_HIGHLIGHTCOLOR, defaultTheme.themeMapHighlightColor);

        this.themeTitleSize = themes.getFloat( theme + THEME_TITLESIZE, defaultTheme.themeTitleSize );
        this.themeTextSize = themes.getFloat( theme + THEME_TEXTSIZE, defaultTheme.themeTextSize );
        this.themeTimeSize = themes.getFloat( theme + THEME_TIMESIZE, defaultTheme.themeTimeSize );
        this.themeTimeSuffixSize = themes.getFloat( theme + THEME_TIMESUFFIXSIZE, defaultTheme.themeTimeSuffixSize );

        this.themeTitleBold = themes.getBoolean( theme + THEME_TITLEBOLD, defaultTheme.themeTitleBold );
        this.themeTimeBold = themes.getBoolean( theme + THEME_TIMEBOLD, defaultTheme.themeTimeBold );

        long bench_end = System.nanoTime();
        Log.d("DEBUG", "init theme: " + this.themeName() + " :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
        return true;
    }

    public ThemeDescriptor saveTheme(Context context, String themesPrefix)
    {
        return saveTheme(context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE));
    }

    public ThemeDescriptor saveTheme(SharedPreferences themes)
    {
        SharedPreferences.Editor themePrefs = themes.edit();
        String themePrefix = themePrefix(this.themeName);

        themePrefs.putInt(themePrefix + THEME_VERSION, this.themeVersion);
        themePrefs.putString(themePrefix + THEME_NAME, this.themeName);
        themePrefs.putBoolean(themePrefix + THEME_ISDEFAULT, this.themeIsDefault);
        themePrefs.putString(themePrefix + THEME_DISPLAYSTRING, this.themeDisplayString);

        themePrefs.putString(themePrefix + THEME_BACKGROUND, this.themeBackground.name());
        themePrefs.putInt(themePrefix + THEME_BACKGROUND_COLOR, this.themeBackgroundColor);

        themePrefs.putInt(themePrefix + THEME_PADDING_LEFT, this.themePadding[0]);
        themePrefs.putInt(themePrefix + THEME_PADDING_TOP, this.themePadding[1]);
        themePrefs.putInt(themePrefix + THEME_PADDING_RIGHT, this.themePadding[2]);
        themePrefs.putInt(themePrefix + THEME_PADDING_BOTTOM, this.themePadding[3]);

        themePrefs.putInt(themePrefix + THEME_TEXTCOLOR, this.themeTextColor);
        themePrefs.putInt(themePrefix + THEME_TITLECOLOR, this.themeTitleColor);
        themePrefs.putInt(themePrefix + THEME_TIMECOLOR, this.themeTimeColor);
        themePrefs.putInt(themePrefix + THEME_TIMESUFFIXCOLOR, this.themeTimeSuffixColor);
        themePrefs.putInt(themePrefix + THEME_ACTIONCOLOR, this.themeActionColor);
        themePrefs.putInt(themePrefix + THEME_ACCENTCOLOR, this.themeAccentColor);

        themePrefs.putInt(themePrefix + THEME_SUNRISECOLOR, this.themeSunriseTextColor);
        themePrefs.putInt(themePrefix + THEME_RISEICON_FILL_COLOR, this.themeSunriseIconColor);
        themePrefs.putInt(themePrefix + THEME_RISEICON_STROKE_COLOR, this.themeSunriseIconStrokeColor);
        themePrefs.putInt(themePrefix + THEME_RISEICON_STROKE_WIDTH, this.themeSunriseIconStrokeWidth);

        themePrefs.putInt(themePrefix + THEME_NOONCOLOR, this.themeNoonTextColor);
        themePrefs.putInt(themePrefix + THEME_NOONICON_FILL_COLOR, this.themeNoonIconColor);
        themePrefs.putInt(themePrefix + THEME_NOONICON_STROKE_COLOR, this.themeNoonIconStrokeColor);
        themePrefs.putInt(themePrefix + THEME_NOONICON_STROKE_WIDTH, this.themeNoonIconStrokeWidth);

        themePrefs.putInt(themePrefix + THEME_SUNSETCOLOR, this.themeSunsetTextColor);
        themePrefs.putInt(themePrefix + THEME_SETICON_FILL_COLOR, this.themeSunsetIconColor);
        themePrefs.putInt(themePrefix + THEME_SETICON_STROKE_COLOR, this.themeSunsetIconStrokeColor);
        themePrefs.putInt(themePrefix + THEME_SETICON_STROKE_WIDTH, this.themeSunsetIconStrokeWidth);

        themePrefs.putInt(themePrefix + THEME_MOONRISECOLOR, this.themeMoonriseTextColor);
        themePrefs.putInt(themePrefix + THEME_MOONSETCOLOR, this.themeMoonsetTextColor);
        themePrefs.putInt(themePrefix + THEME_MOONWANINGCOLOR, this.themeMoonWaningColor);
        themePrefs.putInt(themePrefix + THEME_MOONNEWCOLOR, this.themeMoonNewColor);
        themePrefs.putInt(themePrefix + THEME_MOONWAXINGCOLOR, this.themeMoonWaxingColor);
        themePrefs.putInt(themePrefix + THEME_MOONFULLCOLOR, this.themeMoonFullColor);

        themePrefs.putInt(themePrefix + THEME_MOONFULL_STROKE_WIDTH, this.themeMoonFullStroke);
        themePrefs.putInt(themePrefix + THEME_MOONNEW_STROKE_WIDTH, this.themeMoonNewStroke);

        themePrefs.putInt(themePrefix + THEME_DAYCOLOR, this.themeDayColor);
        themePrefs.putInt(themePrefix + THEME_CIVILCOLOR, this.themeCivilColor);
        themePrefs.putInt(themePrefix + THEME_NAUTICALCOLOR, this.themeNauticalColor);
        themePrefs.putInt(themePrefix + THEME_ASTROCOLOR, this.themeAstroColor);
        themePrefs.putInt(themePrefix + THEME_NIGHTCOLOR, this.themeNightColor);

        themePrefs.putInt(themePrefix + THEME_SPRINGCOLOR, this.themeSpringColor);
        themePrefs.putInt(themePrefix + THEME_SUMMERCOLOR, this.themeSummerColor);
        themePrefs.putInt(themePrefix + THEME_FALLCOLOR, this.themeFallColor);
        themePrefs.putInt(themePrefix + THEME_WINTERCOLOR, this.themeWinterColor);

        themePrefs.putInt(themePrefix + THEME_MAP_BACKGROUNDCOLOR, this.themeMapBackgroundColor);
        themePrefs.putInt(themePrefix + THEME_MAP_FOREGROUNDCOLOR, this.themeMapForegroundColor);
        themePrefs.putInt(themePrefix + THEME_MAP_SHADOWCOLOR, this.themeMapShadowColor);
        themePrefs.putInt(themePrefix + THEME_MAP_HIGHLIGHTCOLOR, this.themeMapHighlightColor);

        themePrefs.putFloat(themePrefix + THEME_TITLESIZE, this.themeTitleSize);
        themePrefs.putFloat(themePrefix + THEME_TEXTSIZE, this.themeTextSize);
        themePrefs.putFloat(themePrefix + THEME_TIMESIZE, this.themeTimeSize);
        themePrefs.putFloat(themePrefix + THEME_TIMESUFFIXSIZE, this.themeTimeSuffixSize);

        themePrefs.putBoolean(themePrefix + THEME_TITLEBOLD, this.themeTitleBold);
        themePrefs.putBoolean(themePrefix + THEME_TIMEBOLD, this.themeTimeBold);

        themePrefs.apply();

        //noinspection UnnecessaryLocalVariable
        ThemeDescriptor themeDescriptor = themeDescriptor();
        return themeDescriptor;
    }

    public ContentValues toContentValues()
    {
        ContentValues values = new ContentValues();
        values.put(THEME_VERSION, this.themeVersion);
        values.put(THEME_NAME, this.themeName);
        values.put(THEME_ISDEFAULT, this.themeIsDefault);
        values.put(THEME_DISPLAYSTRING, this.themeDisplayString);

        values.put(THEME_BACKGROUND, this.themeBackground.name());
        values.put(THEME_BACKGROUND_COLOR, this.themeBackgroundColor);

        values.put(THEME_PADDING_LEFT, this.themePadding[0]);
        values.put(THEME_PADDING_TOP, this.themePadding[1]);
        values.put(THEME_PADDING_RIGHT, this.themePadding[2]);
        values.put(THEME_PADDING_BOTTOM, this.themePadding[3]);

        values.put(THEME_TEXTCOLOR, this.themeTextColor);
        values.put(THEME_TITLECOLOR, this.themeTitleColor);
        values.put(THEME_TIMECOLOR, this.themeTimeColor);
        values.put(THEME_TIMESUFFIXCOLOR, this.themeTimeSuffixColor);
        values.put(THEME_ACTIONCOLOR, this.themeActionColor);
        values.put(THEME_ACCENTCOLOR, this.themeAccentColor);

        values.put(THEME_SUNRISECOLOR, this.themeSunriseTextColor);
        values.put(THEME_RISEICON_FILL_COLOR, this.themeSunriseIconColor);
        values.put(THEME_RISEICON_STROKE_COLOR, this.themeSunriseIconStrokeColor);
        values.put(THEME_RISEICON_STROKE_WIDTH, this.themeSunriseIconStrokeWidth);

        values.put(THEME_NOONCOLOR, this.themeNoonTextColor);
        values.put(THEME_NOONICON_FILL_COLOR, this.themeNoonIconColor);
        values.put(THEME_NOONICON_STROKE_COLOR, this.themeNoonIconStrokeColor);
        values.put(THEME_NOONICON_STROKE_WIDTH, this.themeNoonIconStrokeWidth);

        values.put(THEME_SUNSETCOLOR, this.themeSunsetTextColor);
        values.put(THEME_SETICON_FILL_COLOR, this.themeSunsetIconColor);
        values.put(THEME_SETICON_STROKE_COLOR, this.themeSunsetIconStrokeColor);
        values.put(THEME_SETICON_STROKE_WIDTH, this.themeSunsetIconStrokeWidth);

        values.put(THEME_MOONRISECOLOR, this.themeMoonriseTextColor);
        values.put(THEME_MOONSETCOLOR, this.themeMoonsetTextColor);
        values.put(THEME_MOONWANINGCOLOR, this.themeMoonWaningColor);
        values.put(THEME_MOONNEWCOLOR, this.themeMoonNewColor);
        values.put(THEME_MOONWAXINGCOLOR, this.themeMoonWaxingColor);
        values.put(THEME_MOONFULLCOLOR, this.themeMoonFullColor);

        values.put(THEME_MOONFULL_STROKE_WIDTH, this.themeMoonFullStroke);
        values.put(THEME_MOONNEW_STROKE_WIDTH, this.themeMoonNewStroke);

        values.put(THEME_DAYCOLOR, this.themeDayColor);
        values.put(THEME_CIVILCOLOR, this.themeCivilColor);
        values.put(THEME_NAUTICALCOLOR, this.themeNauticalColor);
        values.put(THEME_ASTROCOLOR, this.themeAstroColor);
        values.put(THEME_NIGHTCOLOR, this.themeNightColor);

        values.put(THEME_SPRINGCOLOR, this.themeSpringColor);
        values.put(THEME_SUMMERCOLOR, this.themeSummerColor);
        values.put(THEME_FALLCOLOR, this.themeFallColor);
        values.put(THEME_WINTERCOLOR, this.themeWinterColor);

        values.put(THEME_MAP_BACKGROUNDCOLOR, this.themeMapBackgroundColor);
        values.put(THEME_MAP_FOREGROUNDCOLOR, this.themeMapForegroundColor);
        values.put(THEME_MAP_SHADOWCOLOR, this.themeMapShadowColor);
        values.put(THEME_MAP_HIGHLIGHTCOLOR, this.themeMapHighlightColor);

        values.put(THEME_TITLESIZE, this.themeTitleSize);
        values.put(THEME_TEXTSIZE, this.themeTextSize);
        values.put(THEME_TIMESIZE, this.themeTimeSize);
        values.put(THEME_TIMESUFFIXSIZE, this.themeTimeSuffixSize);

        values.put(THEME_TITLEBOLD, this.themeTitleBold);
        values.put(THEME_TIMEBOLD, this.themeTimeBold);

        return values;
    }

    public void deleteTheme(Context context, String themesPrefix)
    {
        deleteTheme(context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE));
    }
    public void deleteTheme(SharedPreferences themes)
    {
        if (themeIsDefault)
        {
            Log.w("deleteTheme", themeName + " is flagged default; ignoring request to delete.");
            return;
        }

        SharedPreferences.Editor themePrefs = themes.edit();
        String themePrefix = themePrefix(this.themeName);

        themePrefs.remove(themePrefix + THEME_VERSION);
        themePrefs.remove(themePrefix + THEME_NAME);
        themePrefs.remove(themePrefix + THEME_ISDEFAULT);
        themePrefs.remove(themePrefix + THEME_DISPLAYSTRING);

        themePrefs.remove(themePrefix + THEME_BACKGROUND);
        themePrefs.remove(themePrefix + THEME_BACKGROUND_COLOR);
        themePrefs.remove(themePrefix + THEME_PADDING_LEFT);
        themePrefs.remove(themePrefix + THEME_PADDING_TOP);
        themePrefs.remove(themePrefix + THEME_PADDING_RIGHT);
        themePrefs.remove(themePrefix + THEME_PADDING_BOTTOM);

        themePrefs.remove(themePrefix + THEME_TEXTCOLOR);
        themePrefs.remove(themePrefix + THEME_TITLECOLOR);
        themePrefs.remove(themePrefix + THEME_TIMECOLOR);
        themePrefs.remove(themePrefix + THEME_TIMESUFFIXCOLOR);
        themePrefs.remove(themePrefix + THEME_ACTIONCOLOR);
        themePrefs.remove(themePrefix + THEME_ACCENTCOLOR);

        themePrefs.remove(themePrefix + THEME_SUNRISECOLOR);
        themePrefs.remove(themePrefix + THEME_RISEICON_FILL_COLOR);
        themePrefs.remove(themePrefix + THEME_RISEICON_STROKE_COLOR);
        themePrefs.remove(themePrefix + THEME_RISEICON_STROKE_WIDTH);

        themePrefs.remove(themePrefix + THEME_NOONCOLOR);
        themePrefs.remove(themePrefix + THEME_NOONICON_FILL_COLOR);
        themePrefs.remove(themePrefix + THEME_NOONICON_STROKE_COLOR);
        themePrefs.remove(themePrefix + THEME_NOONICON_STROKE_WIDTH);

        themePrefs.remove(themePrefix + THEME_SUNSETCOLOR);
        themePrefs.remove(themePrefix + THEME_SETICON_FILL_COLOR);
        themePrefs.remove(themePrefix + THEME_SETICON_STROKE_COLOR);
        themePrefs.remove(themePrefix + THEME_SETICON_STROKE_WIDTH);

        themePrefs.remove(themePrefix + THEME_MOONRISECOLOR);
        themePrefs.remove(themePrefix + THEME_MOONSETCOLOR);
        themePrefs.remove(themePrefix + THEME_MOONWANINGCOLOR);
        themePrefs.remove(themePrefix + THEME_MOONNEWCOLOR);
        themePrefs.remove(themePrefix + THEME_MOONWAXINGCOLOR);
        themePrefs.remove(themePrefix + THEME_MOONFULLCOLOR);

        themePrefs.remove(themePrefix + THEME_MOONFULL_STROKE_WIDTH);
        themePrefs.remove(themePrefix + THEME_MOONNEW_STROKE_WIDTH);

        themePrefs.remove(themePrefix + THEME_DAYCOLOR);
        themePrefs.remove(themePrefix + THEME_CIVILCOLOR);
        themePrefs.remove(themePrefix + THEME_NAUTICALCOLOR);
        themePrefs.remove(themePrefix + THEME_ASTROCOLOR);
        themePrefs.remove(themePrefix + THEME_NIGHTCOLOR);

        themePrefs.remove(themePrefix + THEME_SPRINGCOLOR);
        themePrefs.remove(themePrefix + THEME_SUMMERCOLOR);
        themePrefs.remove(themePrefix + THEME_FALLCOLOR);
        themePrefs.remove(themePrefix + THEME_WINTERCOLOR);

        themePrefs.remove(themePrefix + THEME_MAP_BACKGROUNDCOLOR);
        themePrefs.remove(themePrefix + THEME_MAP_FOREGROUNDCOLOR);

        themePrefs.remove(themePrefix + THEME_TITLESIZE);
        themePrefs.remove(themePrefix + THEME_TEXTSIZE);
        themePrefs.remove(themePrefix + THEME_TIMESIZE);
        themePrefs.remove(themePrefix + THEME_TIMESUFFIXSIZE);

        themePrefs.remove(themePrefix + THEME_TITLEBOLD);
        themePrefs.remove(themePrefix + THEME_TIMEBOLD);

        themePrefs.apply();
    }

    public String themeName()
    {
        return this.themeName;
    }

    public int themeVersion()
    {
        return themeVersion;
    }

    public boolean isDefault()
    {
        return themeIsDefault;
    }

    public String themeDisplayString()
    {
        return themeDisplayString;
    }

    public ThemeDescriptor themeDescriptor()
    {
        if (descriptor == null)
        {
            descriptor = new ThemeDescriptor(this.themeName, this.themeDisplayString, this.themeVersion);
        }
        return descriptor;
    }

    public int getTitleColor()
    {
        return themeTitleColor;
    }

    public boolean getTitleBold()
    {
        return themeTitleBold;
    }

    public int getTextColor()
    {
        return themeTextColor;
    }

    public int getTimeColor()
    {
        return themeTimeColor;
    }

    public boolean getTimeBold()
    {
        return themeTimeBold;
    }

    public int getTimeSuffixColor()
    {
        return themeTimeSuffixColor;
    }

    public int getSunriseTextColor()
    {
        return themeSunriseTextColor;
    }

    public int getSunriseIconColor()
    {
        return themeSunriseIconColor;
    }

    public int getSunriseIconStrokeColor()
    {
        return themeSunriseIconStrokeColor;
    }

    public int getSunriseIconStrokeWidth()
    {
        return (themeSunriseIconStrokeWidth < THEME_RISEICON_STROKE_WIDTH_MIN) ? (int)THEME_RISEICON_STROKE_WIDTH_DEF :
                (themeSunriseIconStrokeWidth > THEME_RISEICON_STROKE_WIDTH_MAX) ? (int)THEME_RISEICON_STROKE_WIDTH_MAX : themeSunriseIconStrokeWidth;
    }

    public int getSunriseIconStrokePixels(Context context)
    {
        int strokeWidth = getSunriseIconStrokeWidth();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        themeSunriseIconStrokePixels = (strokeWidth > 0 ? (int)((metrics.density * getSunriseIconStrokeWidth()) + 0.5f) : 0);
        return themeSunriseIconStrokePixels;
    }

    public int getNoonTextColor()
    {
        return themeNoonTextColor;
    }

    public int getNoonIconColor()
    {
        return themeNoonIconColor;
    }

    public int getNoonIconStrokeColor()
    {
        return themeNoonIconStrokeColor;
    }

    public int getNoonIconStrokeWidth()
    {
        return (themeNoonIconStrokeWidth < THEME_NOONICON_STROKE_WIDTH_MIN) ? (int)THEME_NOONICON_STROKE_WIDTH_DEF :
                (themeNoonIconStrokeWidth > THEME_NOONICON_STROKE_WIDTH_MAX) ? (int)THEME_NOONICON_STROKE_WIDTH_MAX : themeNoonIconStrokeWidth;
    }

    public int getNoonIconStrokePixels(Context context)
    {
        int strokeWidth = getNoonIconStrokeWidth();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        themeNoonIconStrokePixels = (strokeWidth > 0 ? (int)((metrics.density * strokeWidth) + 0.5f) : 0);
        return themeNoonIconStrokePixels;
    }

    public int getMoonWaningColor()
    {
        return themeMoonWaningColor;
    }

    public int getMoonNewColor()
    {
        return themeMoonNewColor;
    }

    public int getMoonWaxingColor()
    {
        return themeMoonWaxingColor;
    }

    public int getMoonFullColor()
    {
        return themeMoonFullColor;
    }

    public int getMoonriseTextColor()
    {
        return themeMoonriseTextColor;
    }

    public int getMoonsetTextColor()
    {
        return themeMoonsetTextColor;
    }

    public int getSunsetTextColor()
    {
        return themeSunsetTextColor;
    }

    public int getSunsetIconColor()
    {
        return themeSunsetIconColor;
    }

    public int getSunsetIconStrokeColor()
    {
        return themeSunsetIconStrokeColor;
    }

    public int getSunsetIconStrokeWidth()
    {
        return (themeSunsetIconStrokeWidth < THEME_SETICON_STROKE_WIDTH_MIN) ? (int)THEME_SETICON_STROKE_WIDTH_DEF :
                (themeSunsetIconStrokeWidth > THEME_SETICON_STROKE_WIDTH_MAX) ? (int)THEME_SETICON_STROKE_WIDTH_MAX : themeSunsetIconStrokeWidth;
    }

    public int getSunsetIconStrokePixels(Context context)
    {
        int strokeWidth = getSunsetIconStrokeWidth();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        themeSunsetIconStrokePixels = (strokeWidth > 0 ? (int)((metrics.density * strokeWidth) + 0.5f) : 0);
        return themeSunsetIconStrokePixels;
    }

    public int getMoonFullStroke()
    {
        return (themeMoonFullStroke < THEME_MOON_STROKE_MIN) ? (int)THEME_MOON_STROKE_DEF :
                (themeMoonFullStroke > THEME_MOON_STROKE_MAX) ? (int)THEME_MOON_STROKE_MAX : themeMoonFullStroke;
    }

    public int getMoonFullStrokePixels(Context context)
    {
        int strokeWidth = getMoonFullStroke();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        themeMoonFullStrokePixels = (strokeWidth > 0 ? (int)((metrics.density * strokeWidth) + 0.5f) : 0);
        return themeMoonFullStrokePixels;
    }

    public int getMoonNewStroke()
    {
        return (themeMoonNewStroke < THEME_MOON_STROKE_MIN) ? (int)THEME_MOON_STROKE_DEF :
                (themeMoonNewStroke > THEME_MOON_STROKE_MAX) ? (int)THEME_MOON_STROKE_MAX : themeMoonNewStroke;
    }

    public int getMoonNewStrokePixels(Context context)
    {
        int strokeWidth = getMoonNewStroke();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        themeMoonNewStrokePixels = (strokeWidth > 0 ? (int)((metrics.density * strokeWidth) + 0.5f) : 0);
        return themeMoonNewStrokePixels;
    }

    public int getDayColor()
    {
        return themeDayColor;
    }
    public int getCivilColor()
    {
        return themeCivilColor;
    }
    public int getNauticalColor()
    {
        return themeNauticalColor;
    }
    public int getAstroColor()
    {
        return themeAstroColor;
    }
    public int getNightColor()
    {
        return themeNightColor;
    }

    public int getSpringColor()
    {
        return themeSpringColor;
    }
    public int getSummerColor()
    {
        return themeSummerColor;
    }
    public int getFallColor()
    {
        return themeFallColor;
    }
    public int getWinterColor()
    {
        return themeWinterColor;
    }
    public int getSeasonColor(WidgetSettings.SolsticeEquinoxMode event)
    {
        switch (event)
        {
            case SOLSTICE_WINTER:
                return getWinterColor();
            case EQUINOX_AUTUMNAL:
                return getFallColor();
            case SOLSTICE_SUMMER:
                return getSummerColor();
            case EQUINOX_SPRING:
            default:
                return getSpringColor();
        }
    }

    public int getMapBackgroundColor()
    {
        return themeMapBackgroundColor;
    }

    public int getMapForegroundColor()
    {
        return themeMapForegroundColor;
    }

    public int getMapShadowColor()
    {
        return themeMapShadowColor;
    }

    public int getMapHighlightColor()
    {
        return themeMapHighlightColor;
    }

    public float getTitleSizeSp()
    {
        return (themeTitleSize < THEME_TITLESIZE_MIN) ? THEME_TITLESIZE_DEF :
                (themeTitleSize > THEME_TITLESIZE_MAX) ? THEME_TITLESIZE_MAX : themeTitleSize;
    }

    public float getTextSizeSp()
    {
        return (themeTextSize < THEME_TEXTSIZE_MIN) ? THEME_TEXTSIZE_DEF :
                (themeTextSize > THEME_TEXTSIZE_MAX) ? THEME_TEXTSIZE_MAX : themeTextSize;
    }

    public float getTimeSizeSp()
    {
        return (themeTimeSize < THEME_TIMESIZE_MIN) ? THEME_TIMESIZE_DEF :
                (themeTimeSize > THEME_TIMESIZE_MAX) ? THEME_TIMESIZE_MAX : themeTimeSize;
    }

    public float getTimeSuffixSizeSp()
    {
        return (themeTimeSuffixSize < THEME_TIMESUFFIXSIZE_MIN) ? THEME_TIMESUFFIXSIZE_DEF :
                (themeTimeSuffixSize > THEME_TIMESUFFIXSIZE_MAX) ? THEME_TIMESUFFIXSIZE_MAX : themeTimeSuffixSize;
    }

    public ThemeBackground getBackground()
    {
        return themeBackground;
    }

    public int getBackgroundColor()
    {
        return themeBackgroundColor;
    }

    public int getActionColor()
    {
        return themeActionColor;
    }

    public int getAccentColor()
    {
        return themeAccentColor;
    }

    public int[] getPadding()
    {
        return themePadding;
    }

    public int[] getPaddingPixels(Context context)
    {
        if (themePaddingPixels[0] == -1)
        {
            themePaddingPixels = new int[themePadding.length];
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            for (int i=0; i<themePadding.length; i++)
            {
                themePaddingPixels[i] = (int)((metrics.density * this.themePadding[i]) + 0.5f);
            }
        }
        return themePaddingPixels;
    }

    public boolean isInstalled(SharedPreferences themes)
    {
        return SuntimesTheme.isInstalled(themes, themeDescriptor());
    }
    public static boolean isInstalled(SharedPreferences themes, ThemeDescriptor theme)
    {
        String themePrefix = themePrefix(theme.name());
        int installedVersion = themes.getInt(themePrefix + THEME_VERSION, -1);
        return (installedVersion >= theme.version());
    }

    public static String themePrefix(String themeName)
    {
        return THEME_KEY + themeName + "_";
    }

    ////////////////////////////////////////////////
    ////////////////////////////////////////////////

    public static class ThemeDescriptor implements Comparable
    {
        private final String name;
        private String displayString;
        private String backgroundName;
        private int backgroundColor;
        private final int version;
        private final boolean isDefault;

        public ThemeDescriptor(String name, Context context, String themesPrefix)
        {
            SharedPreferences themesPref = context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE);
            String themePrefix = SuntimesTheme.themePrefix(name);
            String themeName = themesPref.getString(themePrefix + THEME_NAME, "");
            if (themeName.equals(name))
            {
                this.name = name;
                this.displayString = themesPref.getString(themePrefix + THEME_DISPLAYSTRING, "");
                this.backgroundName = themesPref.getString(themePrefix + THEME_BACKGROUND, null);
                this.backgroundColor = themesPref.getInt(themePrefix + THEME_BACKGROUND_COLOR, Color.TRANSPARENT);
                this.version = themesPref.getInt(themePrefix + THEME_VERSION, -1);
                this.isDefault = themesPref.getBoolean(themePrefix + THEME_ISDEFAULT, false);

            } else {
                this.name = "";
                this.displayString = "";
                this.backgroundName = null;
                this.backgroundColor = Color.TRANSPARENT;
                this.version = -1;
                this.isDefault = false;
            }
        }

        public ThemeDescriptor(String name, String displayString, int version)
        {
            this.name = name;
            this.displayString = displayString;
            this.backgroundName = null;
            this.backgroundColor = Color.TRANSPARENT;
            this.version = version;
            this.isDefault = false;
        }

        public boolean isValid()
        {
            return (!name.isEmpty() && !displayString.isEmpty() && version > -1);
        }

        public void updateDescriptor(Context context, String themesPrefix)
        {
            String themePrefix = SuntimesTheme.themePrefix(name);
            SharedPreferences themesPref = context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE);
            this.displayString = themesPref.getString(themePrefix + THEME_DISPLAYSTRING, "");
        }

        public String name() {
            return name;
        }

        public String getBackgroundName() {
            return backgroundName;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public String displayString()
        {
            return displayString;
        }

        public String toString() {
            return displayString;
        }

        public int version() {
            return version;
        }

        public boolean isDefault()
        {
            return isDefault;
        }

        public int ordinal(ThemeDescriptor[] values)
        {
            int ordinal = -1;
            for (int i = 0; i < values.length; i++)
            {
                ThemeDescriptor theme = values[i];
                if (theme.name().equals(this.name))
                {
                    ordinal = i;
                    break;
                }
            }
            return ordinal;
        }

        @Override
        public boolean equals(Object another)
        {
            if (another == null || !(another instanceof ThemeDescriptor))
            {
                return false;

            } else {
                ThemeDescriptor other = (ThemeDescriptor) another;
                return name.equals(other.name());
            }
        }

        @Override
        public int compareTo(@NonNull Object another)
        {
            ThemeDescriptor other = (ThemeDescriptor)another;
            return name.compareTo(other.name());
        }
    }


    /**
     * ThemeBackground
     */
    public enum ThemeBackground
    {
        COLOR(-1, "Colour", true),
        DARK(R.drawable.bg_widget_dark, "Dark", false),
        LIGHT(R.drawable.bg_widget, "Light", false),
        TRANSPARENT(android.R.color.transparent, "Transparent", false);

        private int resID;
        private String displayString;
        private boolean customColors = false;

        private ThemeBackground(int resId, String displayString, boolean customColors )
        {
            this.resID = resId;
            this.displayString = displayString;
            this.customColors = customColors;
        }

        public int getResID()
        {
            return resID;
        }

        public boolean supportsCustomColors()
        {
            return customColors;
        }

        public String getDisplayString()
        {
            return displayString;
        }
        public void setDisplayString( String displayString )
        {
            this.displayString = displayString;
        }

        @Override
        public String toString()
        {
            return displayString;
        }

        public static void initDisplayStrings( Context context )
        {
            DARK.setDisplayString(context.getString(R.string.configLabel_themeBackground_dark));
            LIGHT.setDisplayString(context.getString(R.string.configLabel_themeBackground_light));
            TRANSPARENT.setDisplayString(context.getString(R.string.configLabel_themeBackground_trans));
            COLOR.setDisplayString(context.getString(R.string.configLabel_themeBackground_color));
        }

        @NonNull
        public static ThemeBackground getThemeBackground( int resID )
        {
            ThemeBackground[] backgrounds = ThemeBackground.values();
            //noinspection ForLoopReplaceableByForEach
            for (int i=0; i<backgrounds.length; i++)
            {
                if (backgrounds[i] != null && backgrounds[i].getResID() == resID)
                {
                    return backgrounds[i];
                }
            }
            return ThemeBackground.DARK;
        }
    }

}
