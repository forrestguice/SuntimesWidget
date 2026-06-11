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

public class SystemThemeMD3 extends SystemThemeMD2
{
    public static final String THEMEDEF_NAME = "sysmd3";
    public static final int THEMEDEF_VERSION = BuildConfig.VERSION_CODE;
    private static ThemeDescriptor THEMEDEF_DESCRIPTOR = null;

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.SYSTEM_MONET;
    public static final int THEMEDEF_BACKGROUND_COLOR_ID = R.color.monet_card_bg;


    public SystemThemeMD3(Context context)
    {
        super(context);
        this.themeName = THEMEDEF_NAME;
        this.themeDisplayString = context.getString(R.string.themes_widgetThemes_sys_md3);

        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeBackgroundColor = ContextCompat.getColor(context, THEMEDEF_BACKGROUND_COLOR_ID);

        this.themeTimeColor = ContextCompat.getColor(context, R.color.monet_text_accent);
        this.themeTimeSuffixColor = ContextCompat.getColor(context, R.color.monet_text_secondary);
        this.themeTextColor = ContextCompat.getColor(context, R.color.monet_text_secondary);
        this.themeAccentColor = ContextCompat.getColor(context, R.color.monet_text_accent);
    }

    public static ThemeDescriptor themeDescriptor(Context context)
    {
        if (THEMEDEF_DESCRIPTOR == null) {
            THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, context.getString(R.string.themes_widgetThemes_sys_md3), THEMEDEF_VERSION, THEMEDEF_BACKGROUND.name(), Color.DKGRAY);
        }
        return THEMEDEF_DESCRIPTOR;
    }

}
