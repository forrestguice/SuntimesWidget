package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class SuntimesWidgetThemes
{
    public static final String PREFS_THEMES = "com.forrestguice.suntimeswidget.themes";

    public static final String THEME_KEY = "theme_";
    public static final String themePrefix(String themeName)
    {
        return THEME_KEY + themeName;
    }

    public static final String THEMEDEF_LIGHT_NAME = "light";
    public static final String THEMEDEF_LIGHT_DISPLAYSTRING = "Light";
    public static final int THEMEDEF_LIGHT_BACKGROUND_ID = R.drawable.bg_widget;
    public static final int THEMEDEF_LIGHT_TEXTCOLOR_ID = android.R.color.tertiary_text_light;
    public static final int THEMEDEF_LIGHT_TITLECOLOR_ID = android.R.color.tertiary_text_light;
    public static final int THEMEDEF_LIGHT_TIMESUFFIXCOLOR_ID = android.R.color.tertiary_text_light;
    public static final int THEMEDEF_LIGHT_SUNRISECOLOR_ID = R.color.sunIcon_color_rising;
    public static final int THEMEDEF_LIGHT_SUNSETCOLOR_ID = R.color.sunIcon_color_setting;

    public static final String THEMEDEF_DARK_NAME = "dark";
    public static final String THEMEDEF_DARK_DISPLAYSTRING = "Dark";
    public static final int THEMEDEF_DARK_BACKGROUND_ID = R.drawable.bg_widget_dark;
    public static final int THEMEDEF_DARK_TEXTCOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int THEMEDEF_DARK_TITLECOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int THEMEDEF_DARK_TIMESUFFIXCOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int THEMEDEF_DARK_SUNRISECOLOR_ID = R.color.sunIcon_color_rising;
    public static final int THEMEDEF_DARK_SUNSETCOLOR_ID = R.color.sunIcon_color_setting;

    public static final void initThemes(Context context)
    {
        Resources resources = context.getResources();

        SharedPreferences.Editor themes = context.getSharedPreferences(PREFS_THEMES, Context.MODE_PRIVATE).edit();
        String lightTheme = SuntimesWidgetThemes.themePrefix(THEMEDEF_LIGHT_NAME);
        String darkTheme =  SuntimesWidgetThemes.themePrefix(THEMEDEF_DARK_NAME);

        themes.putString( lightTheme + SuntimesWidgetTheme.THEME_NAME, THEMEDEF_LIGHT_NAME );
        themes.putString(lightTheme + SuntimesWidgetTheme.THEME_DISPLAYSTRING, THEMEDEF_LIGHT_DISPLAYSTRING);
        themes.putInt( lightTheme + SuntimesWidgetTheme.THEME_BACKGROUND, THEMEDEF_LIGHT_BACKGROUND_ID);
        themes.putInt( lightTheme + SuntimesWidgetTheme.THEME_TEXTCOLOR, resources.getColor(THEMEDEF_LIGHT_TEXTCOLOR_ID) );
        themes.putInt( lightTheme + SuntimesWidgetTheme.THEME_TITLECOLOR, resources.getColor(THEMEDEF_LIGHT_TITLECOLOR_ID) );
        themes.putInt( lightTheme + SuntimesWidgetTheme.THEME_TIMESUFFIXCOLOR, resources.getColor(THEMEDEF_LIGHT_TIMESUFFIXCOLOR_ID) );
        themes.putInt( lightTheme + SuntimesWidgetTheme.THEME_SUNRISECOLOR, resources.getColor(THEMEDEF_LIGHT_SUNRISECOLOR_ID) );
        themes.putInt( lightTheme + SuntimesWidgetTheme.THEME_SUNSETCOLOR, resources.getColor(THEMEDEF_LIGHT_SUNSETCOLOR_ID) );

        themes.putString( darkTheme + SuntimesWidgetTheme.THEME_NAME, THEMEDEF_DARK_NAME );
        themes.putString( darkTheme + SuntimesWidgetTheme.THEME_DISPLAYSTRING, THEMEDEF_DARK_DISPLAYSTRING);
        themes.putInt( darkTheme + SuntimesWidgetTheme.THEME_BACKGROUND, THEMEDEF_DARK_BACKGROUND_ID );
        themes.putInt( darkTheme + SuntimesWidgetTheme.THEME_TEXTCOLOR, resources.getColor(THEMEDEF_DARK_TEXTCOLOR_ID) );
        themes.putInt( darkTheme + SuntimesWidgetTheme.THEME_TITLECOLOR, resources.getColor(THEMEDEF_DARK_TITLECOLOR_ID) );
        themes.putInt( darkTheme + SuntimesWidgetTheme.THEME_TIMESUFFIXCOLOR, resources.getColor(THEMEDEF_DARK_TIMESUFFIXCOLOR_ID) );
        themes.putInt( darkTheme + SuntimesWidgetTheme.THEME_SUNRISECOLOR, resources.getColor(THEMEDEF_DARK_SUNRISECOLOR_ID) );
        themes.putInt( darkTheme + SuntimesWidgetTheme.THEME_SUNSETCOLOR, resources.getColor(THEMEDEF_DARK_SUNSETCOLOR_ID) );

        themes.commit();

        addValue(new ThemeDescriptor(THEMEDEF_LIGHT_NAME, THEMEDEF_LIGHT_DISPLAYSTRING));   // todo: i18n
        addValue(new ThemeDescriptor(THEMEDEF_DARK_NAME, THEMEDEF_DARK_DISPLAYSTRING));   // todo: i18n
    }

    public static ArrayList<ThemeDescriptor> themes = new ArrayList<>();

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
        ThemeDescriptor[] values = SuntimesWidgetThemes.values();
        for (int i=0; i<values.length; i++)
        {
            ThemeDescriptor theme = values[i];
            if (theme.name().equals(value) || value.equals("any"))
            {
                return values[i];
            }
        }
        throw new InvalidParameterException("Theme value for " + value + " not found.");
    }

    public static SuntimesWidgetTheme loadTheme(Context context, String themeName)
    {
        SuntimesWidgetTheme theme = new SuntimesWidgetTheme(context);
        theme.initTheme(context, themeName);
        return theme;
    }

    /**
     * ThemeDescriptor : class
     */
    public static class ThemeDescriptor implements Comparable
    {
        String name;
        String displayString;

        public ThemeDescriptor(String name, String displayString)
        {
            this.name = name;
            this.displayString = displayString;
        }

        public String name()
        {
            return name;
        }

        public String toString()
        {
            return displayString;
        }

        public int ordinal()
        {
            int ordinal = -1;
            ThemeDescriptor[] values = SuntimesWidgetThemes.values();
            for (int i=0; i<values.length; i++)
            {
                ThemeDescriptor theme = values[i];
                if (theme.name().equals(this.name))
                {
                    ordinal = i;
                    break;
                }
            }
            return ordinal;
        }

        @Override
        public boolean equals(Object another)
        {
            ThemeDescriptor other = (ThemeDescriptor)another;
            return name.equals(other.name());
        }

        @Override
        public int compareTo(Object another)
        {
            ThemeDescriptor other = (ThemeDescriptor)another;
            return name.compareTo(other.name());
        }
    }
}
