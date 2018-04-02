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

import com.forrestguice.suntimeswidget.BuildConfig;

public class DarkThemeTrans extends DarkTheme
{
    public static final String THEMEDEF_NAME = "dark_transparent";
    public static final String THEMEDEF_DISPLAYSTRING = "Dark (transparent)";
    public static final int THEMEDEF_VERSION = BuildConfig.VERSION_CODE;
    public static final ThemeDescriptor THEMEDEF_DESCRIPTOR = new ThemeDescriptor(THEMEDEF_NAME, THEMEDEF_DISPLAYSTRING, THEMEDEF_VERSION);

    public static final ThemeBackground THEMEDEF_BACKGROUND = ThemeBackground.TRANSPARENT;
    public static final boolean THEMEDEF_TITLEBOLD = true;
    public static final boolean THEMEDEF_TIMEBOLD = true;

    public DarkThemeTrans(Context context)
    {
        super(context);
        this.themeVersion = THEMEDEF_VERSION;
        this.themeName = THEMEDEF_NAME;
        this.themeIsDefault = true;
        this.themeDisplayString = THEMEDEF_DISPLAYSTRING;
        this.themeBackground = THEMEDEF_BACKGROUND;
        this.themeTitleBold = THEMEDEF_TITLEBOLD;
        this.themeTimeBold = THEMEDEF_TIMEBOLD;
    }

    public ThemeDescriptor themeDescriptor()
    {
        return DarkThemeTrans.THEMEDEF_DESCRIPTOR;
    }
}
