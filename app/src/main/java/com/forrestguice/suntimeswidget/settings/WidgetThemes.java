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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme.ThemeDescriptor;

import com.forrestguice.suntimeswidget.themes.DarkTheme;
import com.forrestguice.suntimeswidget.themes.DarkThemeTrans;
import com.forrestguice.suntimeswidget.themes.LightTheme;
import com.forrestguice.suntimeswidget.themes.LightThemeTrans;

public class WidgetThemes
{
    public static final String PREFS_THEMES = "com.forrestguice.suntimeswidget.themes";

    public static final String THEMES_KEY = "themes_";
    public static final String THEMES_INSTALLED = "installed";

    private static SuntimesTheme defaultTheme = null;
    private static boolean initialized = false;

    public static void initThemes(Context context)
    {
        SharedPreferences themePref = context.getSharedPreferences(PREFS_THEMES, Context.MODE_PRIVATE);
        Set<String> themesToProcess = themePref.getStringSet(THEMES_KEY + THEMES_INSTALLED, themes.keySet());
        for (String themeName : themesToProcess)
        {
            ThemeDescriptor themeDesc = loadDescriptor(context, themeName);
            if (themeDesc != null)
            {
                addValue(context, themeDesc);
            } else {
                Log.w("initThemes", themeName + " does not seem to be installed; ignoring...");
            }
        }

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

        saveInstalledList(context);
        defaultTheme = new DarkTheme(context);
        initialized = true;
    }

    private static HashMap<String, ThemeDescriptor> themes = new HashMap<>();

    public static boolean hasValue( ThemeDescriptor theme )
    {
        return themes.containsValue(theme);
    }

    public static void addValue( ThemeDescriptor theme )
    {
        addValue(null, theme);
    }
    public static void addValue( Context context, ThemeDescriptor theme )
    {
        if (!themes.containsValue(theme))
        {
            themes.put(theme.name(), theme);
            if (context != null)
            {
                saveInstalledList(context);
            }
        }
    }

    public static boolean removeValue(Context context, ThemeDescriptor theme)
    {
        boolean removed = (themes.remove(theme.name()) != null);
        if (context != null && removed)
        {
            saveInstalledList(context);
        }
        return removed;
    }

    public static ThemeDescriptor[] values()
    {
        return themes.values().toArray(new ThemeDescriptor[themes.values().size()]);
    }

    public static ThemeDescriptor valueOf(String themeName)
    {
        return themes.get(themeName);
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

    public static ThemeDescriptor loadDescriptor(Context context, String themeName)
    {
        ThemeDescriptor desc = new ThemeDescriptor(themeName, context, PREFS_THEMES);
        return (desc.isValid() ? desc : null);
    }

    public static void saveInstalledList(Context context)
    {
        SharedPreferences.Editor themePref = context.getSharedPreferences(PREFS_THEMES, Context.MODE_PRIVATE).edit();
        themePref.putStringSet(THEMES_KEY + THEMES_INSTALLED, themes.keySet());
        themePref.apply();
    }

    //////////////////////////////////////////////////////////////////////

    /**
     * ThemeGridAdapter
     */
    public static class ThemeGridAdapter extends BaseAdapter
    {
        private final Context context;
        private final SuntimesTheme.ThemeDescriptor[] themes;

        public ThemeGridAdapter(Context context, SuntimesTheme.ThemeDescriptor[] themes)
        {
            this.context = context;
            this.themes = themes;
        }

        public int ordinal( String themeName )
        {
            for (int i=0; i<themes.length; i++)
            {
                if (themes[i].name().equals(themeName))
                    return i+1;
            }
            return -1;
        }

        @Override
        public int getCount()
        {
            return themes.length+1;
        }

        @Override
        public Object getItem(int position)
        {
            if (position > 0)
                return themes[position-1];
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;
            if (convertView != null)
            {
                return convertView;

            } else {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                if (position > 0)
                {
                    view = layoutInflater.inflate(R.layout.layout_griditem_theme, parent, false);
                    View layout = view.findViewById(R.id.griditem);
                    TextView textView = (TextView) view.findViewById(android.R.id.text1);

                    SuntimesTheme theme = WidgetThemes.loadTheme(context, themes[position - 1].name());
                    textView.setText(theme.themeDisplayString());
                    textView.setTextColor(theme.getTitleColor());
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.getTitleSizeSp());
                    layout.setBackgroundResource(theme.getBackgroundId());

                } else {
                    view = layoutInflater.inflate(R.layout.layout_griditem_addtheme, parent, false);
                }
                return view;
            }
        }
    }

    /**
     * ThemeListAdapter
     */
    public static class ThemeListAdapter extends BaseAdapter
    {
        private final Context context;
        private final SuntimesTheme.ThemeDescriptor[] themes;
        private int layoutId, dropDownLayoutId;

        public ThemeListAdapter(Context context, int layoutId, int dropDownLayoutId, SuntimesTheme.ThemeDescriptor[] themes)
        {
            this.context = context;
            this.layoutId = layoutId;
            this.dropDownLayoutId = dropDownLayoutId;
            this.themes = themes;
        }

        @Override
        public int getCount()
        {
            return themes.length;
        }

        @Override
        public Object getItem(int position)
        {
            return themes[position];
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        public ThemeDescriptor[] values()
        {
            return themes;
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            View view;
            if (convertView != null)
            {
                return convertView;

            } else {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(dropDownLayoutId, parent, false);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(themes[position].displayString());
                return view;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;
            if (convertView != null)
            {
                return convertView;

            } else {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                view = layoutInflater.inflate(layoutId, parent, false);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(themes[position].displayString());
                return view;
            }
        }
    }

}
