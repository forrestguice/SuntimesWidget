/**
   Copyright (C) 2014-2017 Forrest Guice
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
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
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
        SharedPreferences themePref = getSharedPreferences(context);
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
        SharedPreferences.Editor themePref = getSharedPreferences(context).edit();
        themePref.putStringSet(THEMES_KEY + THEMES_INSTALLED, themes.keySet());
        themePref.apply();
    }

    public static SharedPreferences getSharedPreferences(Context context)
    {
        return context.getSharedPreferences(PREFS_THEMES, Context.MODE_PRIVATE);
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

            riseTime = Calendar.getInstance();
            riseTime.set(Calendar.HOUR_OF_DAY, 7);
            riseTime.set(Calendar.MINUTE, 0);

            setTime = Calendar.getInstance();
            setTime.set(Calendar.HOUR_OF_DAY, 19);
            setTime.set(Calendar.MINUTE, 0);

            setRiseSet(riseTime, setTime);
        }

        private boolean showAddButton = false;
        public void showAddButton( boolean value )
        {
            showAddButton = value;
        }
        public boolean showingAddButton()
        {
            return showAddButton;
        }

        private Calendar riseTime, setTime;
        private SuntimesUtils.TimeDisplayText riseText, setText;
        public void setRiseSet(Calendar rise, Calendar set)
        {
            riseTime = rise;
            setTime = set;

            SuntimesUtils utils = new SuntimesUtils();
            riseText = utils.calendarTimeShortDisplayString(context, riseTime);
            setText = utils.calendarTimeShortDisplayString(context, setTime);
        }

        public int ordinal( String themeName )
        {
            for (int i=0; i<themes.length; i++)
            {
                if (themes[i].name().equals(themeName))
                {
                    return (showAddButton ? i + 1 : i);
                }
            }
            return -1;
        }

        @Override
        public int getCount()
        {
            if (showAddButton)
                return themes.length+1;
            else return themes.length;
        }

        @Override
        public Object getItem(int position)
        {
            if (showAddButton && position > 0)
            {
                return themes[position - 1];
            } else if (position >= 0) {
                return themes[position];
            }
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
                if (position > 0 || !showAddButton)
                {
                    int p = (showAddButton ? position - 1 : position);
                    SuntimesTheme theme = WidgetThemes.loadTheme(context, themes[p].name());
                    view = layoutInflater.inflate(R.layout.layout_griditem_theme, parent, false);

                    TextView titleView = (TextView) view.findViewById(R.id.text_title);
                    titleView.setText(theme.themeDisplayString());
                    titleView.setTextColor(theme.getTitleColor());
                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.getTitleSizeSp());

                    TextView riseView = (TextView) view.findViewById(R.id.text_time_sunrise);
                    riseView.setTextColor(theme.getSunriseTextColor());
                    riseView.setText(riseText.getValue());
                    riseView.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.getTimeSizeSp());

                    TextView riseViewSuffix = (TextView) view.findViewById(R.id.text_time_sunrise_suffix);
                    riseViewSuffix.setTextColor(theme.getTimeSuffixColor());
                    riseViewSuffix.setText(riseText.getSuffix());
                    riseViewSuffix.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.getTimeSuffixSizeSp());

                    TextView setView = (TextView) view.findViewById(R.id.text_time_sunset);
                    setView.setTextColor(theme.getSunsetTextColor());
                    setView.setText(setText.getValue());
                    setView.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.getTimeSizeSp());

                    TextView setViewSuffix = (TextView) view.findViewById(R.id.text_time_sunset_suffix);
                    setViewSuffix.setTextColor(theme.getTimeSuffixColor());
                    setViewSuffix.setText(setText.getSuffix());
                    setViewSuffix.setTextSize(TypedValue.COMPLEX_UNIT_SP, theme.getTimeSuffixSizeSp());

                    View layout = view.findViewById(R.id.widgetframe_inner);
                    try {
                        layout.setBackgroundResource(theme.getBackgroundId());
                    } catch (Resources.NotFoundException e) {
                        Log.w("ThemeGridAdapter", "background resource not found! " + theme.getBackgroundId());
                        // TODO: bug here; happens when resourceIds are changed for whatever reason, making ID stored in the
                        // theme invalid; can be avoided for default themes by bumping their version (causing reinstall).
                        // might also be fixed serializing/deserializing the background field to obtain a valid id.
                    }
                    int[] padding = theme.getPaddingPixels(context);
                    layout.setPadding(padding[0], padding[1], padding[2], padding[3]);

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
