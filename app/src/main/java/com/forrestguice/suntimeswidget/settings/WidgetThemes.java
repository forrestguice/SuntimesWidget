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

package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme.ThemeDescriptor;

import com.forrestguice.suntimeswidget.themes.DarkTheme;
import com.forrestguice.suntimeswidget.themes.DarkThemeTrans;
import com.forrestguice.suntimeswidget.themes.LightTheme;
import com.forrestguice.suntimeswidget.themes.LightThemeTrans;

public class WidgetThemes
{
    public static final String PREFS_THEMES = "com.forrestguice.suntimeswidget.themes";

    private static SuntimesTheme defaultTheme = null;
    private static boolean initialized = false;

    public static final void initThemes(Context context)
    {
        SharedPreferences themePref = context.getSharedPreferences(PREFS_THEMES, Context.MODE_PRIVATE);

        addValue(LightTheme.THEMEDEF_DESCRIPTOR);
        if (!SuntimesTheme.isInstalled(themePref, LightTheme.THEMEDEF_DESCRIPTOR))
        {
            LightTheme theme = new LightTheme(context);
            theme.saveTheme(themePref);
        }

        addValue(LightThemeTrans.THEMEDEF_DESCRIPTOR);
        if (!SuntimesTheme.isInstalled(themePref, LightThemeTrans.THEMEDEF_DESCRIPTOR))
        {
            LightThemeTrans theme = new LightThemeTrans(context);
            theme.saveTheme(themePref);
        }

        addValue(DarkTheme.THEMEDEF_DESCRIPTOR);
        if (!SuntimesTheme.isInstalled(themePref, DarkTheme.THEMEDEF_DESCRIPTOR))
        {
            DarkTheme theme = new DarkTheme(context);
            theme.saveTheme(themePref);
        }

        addValue(DarkThemeTrans.THEMEDEF_DESCRIPTOR);
        if (!SuntimesTheme.isInstalled(themePref, DarkThemeTrans.THEMEDEF_DESCRIPTOR))
        {
            DarkThemeTrans theme = new DarkThemeTrans(context);
            theme.saveTheme(themePref);
        }

        defaultTheme = new DarkTheme(context);
        initialized = true;
    }

    private static ArrayList<ThemeDescriptor> themes = new ArrayList<ThemeDescriptor>();

    public static boolean hasValue( ThemeDescriptor theme )
    {
        return themes.contains(theme);
    }

    public static void addValue( ThemeDescriptor theme )
    {
        if (!themes.contains(theme))
        {
            themes.add(theme);
        }
    }

    public static void removeValue( ThemeDescriptor theme )
    {
        themes.remove(theme);
    }

    public static ThemeDescriptor[] values()
    {
        ThemeDescriptor[] array = new ThemeDescriptor[themes.size()];
        for (int i=0; i<themes.size(); i++)
        {
            array[i] = themes.get(i);
        }
        return array;
    }

    public static ThemeDescriptor valueOf(String value)
    {
        value = value.trim().toLowerCase();
        ThemeDescriptor[] values = WidgetThemes.values();
        for (int i=0; i<values.length; i++)
        {
            ThemeDescriptor theme = values[i];
            if (theme.name().equals(value) || value.equals("any"))
            {
                return theme;
            }
        }
        throw new InvalidParameterException("Value for " + value + " not found.");
    }

    public static SuntimesTheme loadTheme(Context context, String themeName)
    {
        if (!initialized)
        {
            initThemes(context);
        }

        SuntimesTheme theme = new SuntimesTheme();
        theme.initTheme(context, PREFS_THEMES, themeName, defaultTheme);
        return theme;
    }

}
