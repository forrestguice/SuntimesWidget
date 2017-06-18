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

    private static SuntimesTheme defaultTheme = null;
    private static boolean initialized = false;

    public static void initThemes(Context context)
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

    private static HashMap<String, ThemeDescriptor> themes = new HashMap<>();

    public static boolean hasValue( ThemeDescriptor theme )
    {
        return themes.containsValue(theme);
    }

    public static void addValue( ThemeDescriptor theme )
    {
        if (!themes.containsValue(theme))
        {
            themes.put(theme.name(), theme);
        }
    }

    public static void removeValue( ThemeDescriptor theme )
    {
        themes.remove(theme.name());
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
                SuntimesTheme theme = WidgetThemes.loadTheme(context, themes[position].name());
                textView.setText(theme.themeDisplayString());
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
                SuntimesTheme theme = WidgetThemes.loadTheme(context, themes[position].name());
                textView.setText(theme.themeDisplayString());
                return view;
            }
        }
    }

}
