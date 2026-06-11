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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.content.ContextCompat;

public class DarkThemeMD2T extends DarkThemeMD2
{
    public static final String THEMEDEF_NAME = "darkmd2_translucent";
    private static ThemeDescriptor THEMEDEF_DESCRIPTOR = null;

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.DARK_MD2_T;
    public static final int THEMEDEF_BACKGROUND_COLOR_ID = R.color.card_bg_darktrans;

    public DarkThemeMD2T(Context context)
    {
        super(context);
        this.themeName = THEMEDEF_NAME;
        this.themeDisplayString = context.getString(R.string.themes_widgetThemes_dark_md2_translucent);
        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeBackgroundColor = ContextCompat.getColor(context, THEMEDEF_BACKGROUND_COLOR_ID);
    }

    public static ThemeDescriptor themeDescriptor(Context context)
    {
        if (THEMEDEF_DESCRIPTOR == null) {
            THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, context.getString(R.string.themes_widgetThemes_dark_md2_translucent), THEMEDEF_VERSION, THEMEDEF_BACKGROUND.name(), Color.DKGRAY);
        }
        return THEMEDEF_DESCRIPTOR;
    }

}
