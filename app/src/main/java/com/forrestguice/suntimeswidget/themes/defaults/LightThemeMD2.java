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

public class LightThemeMD2 extends LightTheme1
{
    public static final String THEMEDEF_NAME = "lightmd2";
    public static final int THEMEDEF_VERSION = BuildConfig.VERSION_CODE;
    private static ThemeDescriptor THEMEDEF_DESCRIPTOR = null;

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.LIGHT_MD2;
    public static final int THEMEDEF_BACKGROUND_COLOR_ID = R.color.card_bg_light;
    public static final int[] THEMEDEF_PADDING = {8, 8, 8, 8};

    public static final int THEMEDEF_TIMECOLOR_ID = R.color.text_accent_light;

    //public static final float THEMEDEF_TITLESIZE = 14;
    //public static final float THEMEDEF_TEXTSIZE = 14;
    //public static final float THEMEDEF_TIMESIZE = 16;
    //public static final float THEMEDEF_TIMESUFFIXSIZE = 8;

    public LightThemeMD2(Context context)
    {
        super(context);

        this.themeVersion = THEMEDEF_VERSION;
        this.themeName = THEMEDEF_NAME;
        this.themeIsDefault = true;
        this.themeDisplayString = context.getString(R.string.themes_widgetThemes_light_md2);

        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeBackgroundColor = ContextCompat.getColor(context, THEMEDEF_BACKGROUND_COLOR_ID);
        this.themePadding = THEMEDEF_PADDING;

        this.themeTimeColor = ContextCompat.getColor(context, THEMEDEF_TIMECOLOR_ID);
        
        //this.themeTimeSuffixColor = ContextCompat.getColor(context, R.color.monet_dark_text_secondary);
        //this.themeTextColor = ContextCompat.getColor(context, R.color.monet_dark_text_secondary);
        //this.themeAccentColor = ContextCompat.getColor(context, R.color.monet_dark_text_accent);

        //this.themeTitleSize = THEMEDEF_TITLESIZE;
        //this.themeTextSize = THEMEDEF_TEXTSIZE;
        //this.themeTimeSize = THEMEDEF_TIMESIZE;
        //this.themeTimeSuffixSize = THEMEDEF_TIMESUFFIXSIZE;
    }

    public static ThemeDescriptor themeDescriptor(Context context)
    {
        if (THEMEDEF_DESCRIPTOR == null) {
            THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, context.getString(R.string.themes_widgetThemes_light_md2), THEMEDEF_VERSION, THEMEDEF_BACKGROUND.name(), Color.LTGRAY);
        }
        return THEMEDEF_DESCRIPTOR;
    }

}
