/**
   Copyright (C) 2022 Forrest Guice
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;

@TargetApi(31)
public class MonetTheme extends DarkTheme
{
    public static final String THEMEDEF_NAME = "monet";
    public static final String THEMEDEF_DISPLAYSTRING = "Material You";
    public static final int THEMEDEF_VERSION = BuildConfig.VERSION_CODE;

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.COLOR;
    public static final int[] THEMEDEF_PADDING = {2, 4, 4, 4};

    public MonetTheme(Context context)
    {
        super(context);

        this.themeVersion = THEMEDEF_VERSION;
        this.themeName = THEMEDEF_NAME;
        this.themeIsDefault = true;
        this.themeDisplayString = THEMEDEF_DISPLAYSTRING;

        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeBackgroundColor = ContextCompat.getColor(context, R.color.monet_widget_bg);
        this.themePadding = THEMEDEF_PADDING;

        this.themeTitleSize = THEMEDEF_TITLESIZE;
        this.themeTextSize = THEMEDEF_TEXTSIZE;
        this.themeTimeSize = THEMEDEF_TIMESIZE;
        this.themeTimeSuffixSize = THEMEDEF_TIMESUFFIXSIZE;

        this.themeAccentColor = ContextCompat.getColor(context, R.color.monet_text_accent);
        this.themeTitleColor = ContextCompat.getColor(context, R.color.monet_text_primary);
        this.themeTextColor = ContextCompat.getColor(context, R.color.monet_text_secondary);
        this.themeTimeSuffixColor = ContextCompat.getColor(context, R.color.monet_text_tertiary);
        this.themeTimeColor = ContextCompat.getColor(context, R.color.monet_text_time);

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
    }

    public ThemeDescriptor themeDescriptor() {
        return new ThemeDescriptor(THEMEDEF_NAME, THEMEDEF_DISPLAYSTRING, THEMEDEF_VERSION, getBackground().name(), getBackgroundColor());
    }

}
