/**
   Copyright (C) 2014 Forrest Guice
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

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.R;

public class LightTheme extends SuntimesTheme
{
    public static final String THEMEDEF_NAME = "light";
    public static final String THEMEDEF_DISPLAYSTRING = "Light";
    public static final int THEMEDEF_VERSION = 1;
    public static final ThemeDescriptor THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, THEMEDEF_DISPLAYSTRING, THEMEDEF_VERSION);

    public static final int THEMEDEF_BACKGROUND_ID = R.drawable.bg_widget;
    public static final int[] THEMEDEF_PADDING = {2, 4, 4, 4};

    public static final float THEMEDEF_TITLESIZE = 10;
    public static final int THEMEDEF_TITLECOLOR_ID = android.R.color.secondary_text_light;

    public static final int THEMEDEF_TEXTCOLOR_ID = android.R.color.tertiary_text_light;
    public static final int THEMEDEF_SUNRISECOLOR_ID = R.color.sunIcon_color_rising_light;
    public static final int THEMEDEF_TIMECOLOR_ID = android.R.color.primary_text_light;
    public static final int THEMEDEF_SUNSETCOLOR_ID = R.color.sunIcon_color_setting_light;
    public static final int THEMEDEF_TIMESUFFIXCOLOR_ID = android.R.color.tertiary_text_light;

    public LightTheme(Context context)
    {
        super();

        this.themeVersion = THEMEDEF_VERSION;
        this.themeName = THEMEDEF_NAME;
        this.themeDisplayString = THEMEDEF_DISPLAYSTRING;

        this.themeBackground = THEMEDEF_BACKGROUND_ID;
        this.themePadding = THEMEDEF_PADDING;

        this.themeTitleSize = THEMEDEF_TITLESIZE;
        this.themeTitleColor = ContextCompat.getColor(context, THEMEDEF_TITLECOLOR_ID);

        this.themeTextColor = ContextCompat.getColor(context, THEMEDEF_TEXTCOLOR_ID);
        this.themeSunriseTextColor = ContextCompat.getColor(context, THEMEDEF_SUNRISECOLOR_ID);
        this.themeSunsetTextColor = ContextCompat.getColor(context, THEMEDEF_SUNSETCOLOR_ID);
        this.themeTimeColor = ContextCompat.getColor(context, THEMEDEF_TIMECOLOR_ID);
        this.themeTimeSuffixColor = ContextCompat.getColor(context, THEMEDEF_TIMESUFFIXCOLOR_ID);
    }

    public ThemeDescriptor themeDescriptor()
    {
        return LightTheme.THEMEDEF_DESCRIPTOR;
    }
}
