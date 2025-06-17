/**
   Copyright (C) 2014-2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.themes.defaults;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public class DarkTheme extends SuntimesTheme
{
    public static final String THEMEDEF_NAME = "dark";
    public static final int THEMEDEF_VERSION = BuildConfig.VERSION_CODE;
    private static ThemeDescriptor THEMEDEF_DESCRIPTOR = null;

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.DARK;
    public static final int THEMEDEF_BACKGROUND_COLOR_ID = R.color.widget_bg_dark;
    public static final int[] THEMEDEF_PADDING = {2, 4, 4, 4};

    public static final float THEMEDEF_TITLESIZE = 10;
    public static final float THEMEDEF_TEXTSIZE = 10;
    public static final float THEMEDEF_TIMESIZE = 12;
    public static final float THEMEDEF_TIMESUFFIXSIZE = 6;

    public static final boolean THEMEDEF_TITLEBOLD = false;
    public static final boolean THEMEDEF_TIMEBOLD = false;

    public static final int THEMEDEF_RISEICON_STROKEWIDTH = 0;
    public static final int THEMEDEF_SETICON_STROKEWIDTH = 0;
    public static final int THEMEDEF_NOONICON_STROKEWIDTH = 3;
    public static final int THEMEDEF_MOONFULL_STROKEWIDTH = 2;
    public static final int THEMEDEF_MOONNEW_STROKEWIDTH = 2;

    public static final int THEMEDEF_TITLECOLOR_ID = android.R.color.white;
    public static final int THEMEDEF_TEXTCOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int THEMEDEF_SUNRISECOLOR_ID = R.color.sunIcon_color_rising_dark;
    public static final int THEMEDEF_SUNSETCOLOR_ID = R.color.sunIcon_color_setting_dark;
    public static final int THEMEDEF_TIMECOLOR_ID = android.R.color.primary_text_dark;
    public static final int THEMEDEF_TIMESUFFIXCOLOR_ID = android.R.color.tertiary_text_dark;

    public static final int THEMEDEF_MOONWANINGCOLOR_ID = R.color.moonIcon_color_waning;
    public static final int THEMEDEF_MOONNEWCOLOR_ID = R.color.moonIcon_color_new_dark;
    public static final int THEMEDEF_MOONWAXINGCOLOR_ID = R.color.moonIcon_color_waxing;
    public static final int THEMEDEF_MOONFULLCOLOR_ID = R.color.moonIcon_color_full_dark;

    public static final int THEMEDEF_MOONRISECOLOR_ID = R.color.moonIcon_color_rising_dark;
    public static final int THEMEDEF_MOONSETCOLOR_ID = R.color.moonIcon_color_setting_dark;

    public static final int THEMEDEF_DAYCOLOR_ID = R.color.graphColor_day_dark;
    public static final int THEMEDEF_CIVILCOLOR_ID = R.color.graphColor_civil_dark;
    public static final int THEMEDEF_NAUTICALCOLOR_ID = R.color.graphColor_nautical_dark;
    public static final int THEMEDEF_ASTROCOLOR_ID = R.color.graphColor_astronomical_dark;
    public static final int THEMEDEF_NIGHTCOLOR_ID = R.color.graphColor_night_dark;

    public static final int THEMEDEF_SPRINGCOLOR_ID = R.color.springColor_dark;
    public static final int THEMEDEF_SUMMERCOLOR_ID = R.color.summerColor_dark;
    public static final int THEMEDEF_FALLCOLOR_ID = R.color.fallColor_dark;
    public static final int THEMEDEF_WINTERCOLOR_ID = R.color.winterColor_dark;

    public static final int THEMEDEF_MAP_BACKGROUNDCOLOR_ID = R.color.map_background_dark;
    public static final int THEMEDEF_MAP_FOREGROUNDCOLOR_ID = R.color.map_foreground_dark;
    public static final int THEMEDEF_MAP_SHADOWCOLOR_ID = R.color.map_sunshadow_dark;
    public static final int THEMEDEF_MAP_HIGHLIGHTCOLOR_ID = R.color.map_moonlight_dark;

    public static final int THEMEDEF_ACCENT_ID = R.color.text_accent_dark;
    public static final int THEMEDEF_ACTION_ID = R.color.btn_tint_pressed_dark;

    public DarkTheme(Context context)
    {
        super();

        this.themeVersion = THEMEDEF_VERSION;
        this.themeName = THEMEDEF_NAME;
        this.themeIsDefault = true;
        this.themeDisplayString = context.getString(R.string.widgetThemes_dark1);

        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeBackgroundColor = ContextCompat.getColor(context, THEMEDEF_BACKGROUND_COLOR_ID);
        this.themePadding = THEMEDEF_PADDING;

        this.themeTitleSize = THEMEDEF_TITLESIZE;
        this.themeTextSize = THEMEDEF_TEXTSIZE;
        this.themeTimeSize = THEMEDEF_TIMESIZE;
        this.themeTimeSuffixSize = THEMEDEF_TIMESUFFIXSIZE;

        this.themeTitleColor = ContextCompat.getColor(context, THEMEDEF_TITLECOLOR_ID);
        this.themeTextColor = ContextCompat.getColor(context, THEMEDEF_TEXTCOLOR_ID);
        this.themeTimeColor = ContextCompat.getColor(context, THEMEDEF_TIMECOLOR_ID);
        this.themeTimeSuffixColor = ContextCompat.getColor(context, THEMEDEF_TIMESUFFIXCOLOR_ID);

        this.themeTitleBold = THEMEDEF_TITLEBOLD;
        this.themeTimeBold = THEMEDEF_TIMEBOLD;

        this.themeSunriseTextColor = ContextCompat.getColor(context, THEMEDEF_SUNRISECOLOR_ID);
        this.themeSunriseIconColor = this.themeSunriseTextColor;
        this.themeSunriseIconStrokeWidth = THEMEDEF_RISEICON_STROKEWIDTH;

        this.themeSunsetTextColor = ContextCompat.getColor(context, THEMEDEF_SUNSETCOLOR_ID);
        this.themeSunsetIconColor = this.themeSunsetTextColor;
        this.themeSunsetIconStrokeWidth = THEMEDEF_SETICON_STROKEWIDTH;

        this.themeNoonTextColor = ContextCompat.getColor(context, R.color.sunIcon_color_noon_dark);
        this.themeNoonIconColor = ContextCompat.getColor(context, R.color.sunIcon_color_noon_dark);
        this.themeNoonIconStrokeColor = ContextCompat.getColor(context, R.color.sunIcon_color_noonBorder_dark);
        this.themeNoonIconStrokeWidth = THEMEDEF_NOONICON_STROKEWIDTH;

        this.themeSunriseIconStrokeColor = this.themeSunsetIconColor;
        this.themeSunsetIconStrokeColor = this.themeSunriseIconColor;

        this.themeMoonriseTextColor = ContextCompat.getColor(context, THEMEDEF_MOONRISECOLOR_ID);
        this.themeMoonsetTextColor = ContextCompat.getColor(context, THEMEDEF_MOONSETCOLOR_ID);
        this.themeMoonWaningColor = ContextCompat.getColor(context, THEMEDEF_MOONWANINGCOLOR_ID);
        this.themeMoonNewColor = ContextCompat.getColor(context, THEMEDEF_MOONNEWCOLOR_ID);
        this.themeMoonWaxingColor = ContextCompat.getColor(context, THEMEDEF_MOONWAXINGCOLOR_ID);
        this.themeMoonFullColor = ContextCompat.getColor(context, THEMEDEF_MOONFULLCOLOR_ID);

        this.themeMoonWaningTextColor = ContextCompat.getColor(context, THEMEDEF_MOONWANINGCOLOR_ID);
        this.themeMoonNewTextColor = ContextCompat.getColor(context, R.color.moonIcon_color_new_text_dark);
        this.themeMoonWaxingTextColor = ContextCompat.getColor(context, THEMEDEF_MOONWAXINGCOLOR_ID);
        this.themeMoonFullTextColor = ContextCompat.getColor(context, R.color.moonIcon_color_full_text_dark);

        this.themeMoonFullStroke = THEMEDEF_MOONFULL_STROKEWIDTH;
        this.themeMoonNewStroke = THEMEDEF_MOONNEW_STROKEWIDTH;

        this.themeDayColor = ContextCompat.getColor(context, THEMEDEF_DAYCOLOR_ID);
        this.themeCivilColor = ContextCompat.getColor(context, THEMEDEF_CIVILCOLOR_ID);
        this.themeNauticalColor = ContextCompat.getColor(context, THEMEDEF_NAUTICALCOLOR_ID);
        this.themeAstroColor = ContextCompat.getColor(context, THEMEDEF_ASTROCOLOR_ID);
        this.themeNightColor = ContextCompat.getColor(context, THEMEDEF_NIGHTCOLOR_ID);
        this.themeGraphPointFillColor = ContextCompat.getColor(context, R.color.graphColor_pointFill_dark);
        this.themeGraphPointStrokeColor = ContextCompat.getColor(context, R.color.graphColor_pointStroke_dark);

        this.themeSpringColor = ContextCompat.getColor(context, THEMEDEF_SPRINGCOLOR_ID);
        this.themeSummerColor = ContextCompat.getColor(context, THEMEDEF_SUMMERCOLOR_ID);
        this.themeFallColor = ContextCompat.getColor(context, THEMEDEF_FALLCOLOR_ID);
        this.themeWinterColor = ContextCompat.getColor(context, THEMEDEF_WINTERCOLOR_ID);

        this.themeMapBackgroundColor = ContextCompat.getColor(context, THEMEDEF_MAP_BACKGROUNDCOLOR_ID);
        this.themeMapForegroundColor = ContextCompat.getColor(context, THEMEDEF_MAP_FOREGROUNDCOLOR_ID);
        this.themeMapShadowColor = ContextCompat.getColor(context, THEMEDEF_MAP_SHADOWCOLOR_ID);
        this.themeMapHighlightColor = ContextCompat.getColor(context, THEMEDEF_MAP_HIGHLIGHTCOLOR_ID);

        this.themeAccentColor = ContextCompat.getColor(context, THEMEDEF_ACCENT_ID);
        this.themeActionColor = ContextCompat.getColor(context, THEMEDEF_ACTION_ID);

    }

    public static ThemeDescriptor themeDescriptor(Context context)
    {
        if (THEMEDEF_DESCRIPTOR == null) {
            THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, context.getString(R.string.widgetThemes_dark1), THEMEDEF_VERSION, ThemeBackground.DARK.name(), Color.DKGRAY);
        }
        return THEMEDEF_DESCRIPTOR;
    }

}
