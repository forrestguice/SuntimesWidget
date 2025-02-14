/**
   Copyright (C) 2018 Forrest Guice
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
import com.forrestguice.support.content.ContextCompat;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;

public class DarkThemeTranslucent extends DarkThemeTrans
{
    public static final String THEMEDEF_NAME = "dark_translucent";
    public static final int THEMEDEF_VERSION = BuildConfig.VERSION_CODE;
    private static ThemeDescriptor THEMEDEF_DESCRIPTOR = null;

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.COLOR;
    public static final int THEMEDEF_BACKGROUND_COLOR_ID = R.color.widget_bg_dark;

    public DarkThemeTranslucent(Context context)
    {
        super(context);
        this.themeVersion = THEMEDEF_VERSION;
        this.themeName = THEMEDEF_NAME;
        this.themeIsDefault = true;
        this.themeDisplayString = context.getString(R.string.widgetThemes_dark_translucent);

        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeBackgroundColor = ContextCompat.getColor(context, THEMEDEF_BACKGROUND_COLOR_ID);
    }

    public static ThemeDescriptor themeDescriptor(Context context)
    {
        if (THEMEDEF_DESCRIPTOR == null) {
            THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, context.getString(R.string.widgetThemes_dark_translucent), THEMEDEF_VERSION, ThemeBackground.COLOR.name(), Color.parseColor("#82212121"));
        }
        return THEMEDEF_DESCRIPTOR;
    }
}
