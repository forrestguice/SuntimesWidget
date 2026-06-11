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

public class LightThemeMD3T extends LightThemeMD3
{
    public static final String THEMEDEF_NAME = "lightmd3_translucent";
    private static ThemeDescriptor THEMEDEF_DESCRIPTOR = null;

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.LIGHT_MONET_T;
    public static final int THEMEDEF_BACKGROUND_COLOR_ID = R.color.monet_light_card_bg_trans;

    public LightThemeMD3T(Context context)
    {
        super(context);
        this.themeName = THEMEDEF_NAME;
        this.themeDisplayString = context.getString(R.string.themes_widgetThemes_light_md3_translucent);
        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeBackgroundColor = ContextCompat.getColor(context, THEMEDEF_BACKGROUND_COLOR_ID);
    }

    public static ThemeDescriptor themeDescriptor(Context context)
    {
        if (THEMEDEF_DESCRIPTOR == null) {
            THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, context.getString(R.string.themes_widgetThemes_light_md3_translucent), THEMEDEF_VERSION, THEMEDEF_BACKGROUND.name(), Color.LTGRAY);
        }
        return THEMEDEF_DESCRIPTOR;
    }

}
