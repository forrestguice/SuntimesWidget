/**
   Copyright (C) 2026 Forrest Guice
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

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.content.ContextCompat;

public class SystemThemeMD2 extends DarkThemeMD2
{
    public static final String THEMEDEF_NAME = "sysmd2";
    public static final int THEMEDEF_VERSION = BuildConfig.VERSION_CODE;
    private static ThemeDescriptor THEMEDEF_DESCRIPTOR = null;

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.SYSTEM_MD2;
    public static final int THEMEDEF_BACKGROUND_COLOR_ID = R.color.card_bg_contrast;

    public static final int THEMEDEF_SUNRISECOLOR_ID = R.color.sunIcon_color_rising;
    public static final int THEMEDEF_SUNSETCOLOR_ID = R.color.sunIcon_color_setting;
    public static final int THEMEDEF_MOONRISECOLOR_ID = R.color.moonIcon_color_rising;
    public static final int THEMEDEF_MOONSETCOLOR_ID = R.color.moonIcon_color_setting;

    public static final int THEMEDEF_MOONWANINGCOLOR_ID = R.color.moonIcon_color_waning;
    public static final int THEMEDEF_MOONNEWCOLOR_ID = R.color.moonIcon_color_new;
    public static final int THEMEDEF_MOONWAXINGCOLOR_ID = R.color.moonIcon_color_waxing;
    public static final int THEMEDEF_MOONFULLCOLOR_ID = R.color.moonIcon_color_full;

    public static final int THEMEDEF_DAYCOLOR_ID = R.color.graphColor_day;
    public static final int THEMEDEF_CIVILCOLOR_ID = R.color.graphColor_civil;
    public static final int THEMEDEF_NAUTICALCOLOR_ID = R.color.graphColor_nautical;
    public static final int THEMEDEF_ASTROCOLOR_ID = R.color.graphColor_astronomical;
    public static final int THEMEDEF_NIGHTCOLOR_ID = R.color.graphColor_night;

    public static final int THEMEDEF_SPRINGCOLOR_ID = R.color.springColor;
    public static final int THEMEDEF_SUMMERCOLOR_ID = R.color.summerColor;
    public static final int THEMEDEF_FALLCOLOR_ID = R.color.fallColor;
    public static final int THEMEDEF_WINTERCOLOR_ID = R.color.winterColor;

    public static final int THEMEDEF_MAP_BACKGROUNDCOLOR_ID = R.color.map_background;
    public static final int THEMEDEF_MAP_FOREGROUNDCOLOR_ID = R.color.map_foreground;
    public static final int THEMEDEF_MAP_SHADOWCOLOR_ID = R.color.map_sunshadow;
    public static final int THEMEDEF_MAP_HIGHLIGHTCOLOR_ID = R.color.map_moonlight;

    public static final int THEMEDEF_ACCENT_ID = R.color.text_accent;
    public static final int THEMEDEF_ACTION_ID = R.color.btn_tint_pressed;

    public SystemThemeMD2(Context context)
    {
        super(context);
        this.themeName = THEMEDEF_NAME;
        this.themeDisplayString = context.getString(R.string.themes_widgetThemes_sys_md2);

        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeBackgroundColor = ContextCompat.getColor(context, THEMEDEF_BACKGROUND_COLOR_ID);

        this.themeTimeColor = ContextCompat.getColor(context, R.color.monet_text_accent);
        this.themeTimeSuffixColor = ContextCompat.getColor(context, R.color.monet_text_secondary);
        this.themeTextColor = ContextCompat.getColor(context, R.color.monet_text_secondary);
        this.themeAccentColor = ContextCompat.getColor(context, R.color.monet_text_accent);

        this.themeSunriseTextColor = ContextCompat.getColor(context, THEMEDEF_SUNRISECOLOR_ID);
        this.themeSunriseIconColor = this.themeSunriseTextColor;

        this.themeSunsetTextColor = ContextCompat.getColor(context, THEMEDEF_SUNSETCOLOR_ID);
        this.themeSunsetIconColor = this.themeSunsetTextColor;

        //this.themeNoonTextColor = ContextCompat.getColor(context, R.color.sunIcon_color_noon_dark);    // TODO
        //this.themeNoonIconColor = ContextCompat.getColor(context, R.color.sunIcon_color_noon_dark);    // TODO
        //this.themeNoonIconStrokeColor = ContextCompat.getColor(context, R.color.sunIcon_color_noonBorder_dark);    // TODO

        this.themeSunriseIconStrokeColor = this.themeSunsetIconColor;
        this.themeSunsetIconStrokeColor = this.themeSunriseIconColor;

        this.themeMoonriseTextColor = ContextCompat.getColor(context, THEMEDEF_MOONRISECOLOR_ID);
        this.themeMoonsetTextColor = ContextCompat.getColor(context, THEMEDEF_MOONSETCOLOR_ID);
        this.themeMoonWaningColor = ContextCompat.getColor(context, THEMEDEF_MOONWANINGCOLOR_ID);
        this.themeMoonNewColor = ContextCompat.getColor(context, THEMEDEF_MOONNEWCOLOR_ID);
        this.themeMoonWaxingColor = ContextCompat.getColor(context, THEMEDEF_MOONWAXINGCOLOR_ID);
        this.themeMoonFullColor = ContextCompat.getColor(context, THEMEDEF_MOONFULLCOLOR_ID);

        this.themeMoonWaningTextColor = ContextCompat.getColor(context, THEMEDEF_MOONWANINGCOLOR_ID);
        this.themeMoonWaxingTextColor = ContextCompat.getColor(context, THEMEDEF_MOONWAXINGCOLOR_ID);
        this.themeMoonNewTextColor = this.themeMoonNewColor;
        this.themeMoonFullTextColor = this.themeMoonFullColor;
        //this.themeMoonNewTextColor = ContextCompat.getColor(context, R.color.moonIcon_color_new_text_dark);    // TODO
        //this.themeMoonFullTextColor = ContextCompat.getColor(context, R.color.moonIcon_color_full_text_dark);    // TODO

        this.themeDayColor = ContextCompat.getColor(context, THEMEDEF_DAYCOLOR_ID);
        this.themeCivilColor = ContextCompat.getColor(context, THEMEDEF_CIVILCOLOR_ID);
        this.themeNauticalColor = ContextCompat.getColor(context, THEMEDEF_NAUTICALCOLOR_ID);
        this.themeAstroColor = ContextCompat.getColor(context, THEMEDEF_ASTROCOLOR_ID);
        this.themeNightColor = ContextCompat.getColor(context, THEMEDEF_NIGHTCOLOR_ID);
        this.themeGraphPointFillColor = ContextCompat.getColor(context, R.color.graphColor_pointFill);
        this.themeGraphPointStrokeColor = ContextCompat.getColor(context, R.color.graphColor_pointStroke);

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
            THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, context.getString(R.string.themes_widgetThemes_sys_md2), THEMEDEF_VERSION, THEMEDEF_BACKGROUND.name(), Color.DKGRAY);
        }
        return THEMEDEF_DESCRIPTOR;
    }

}
