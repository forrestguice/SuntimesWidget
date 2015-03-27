package com.forrestguice.suntimeswidget.settings;

import android.app.Application;
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
        StringBuilder themePrefix = new StringBuilder(THEME_KEY);
        themePrefix.append(themeName);
        themePrefix.append("_");
        return themePrefix.toString();
    }

    // Theme: Light
    public static final String THEMEDEF_LIGHT_NAME = "light";
    public static final String THEMEDEF_LIGHT_DISPLAYSTRING = "Light";
    public static final int THEMEDEF_LIGHT_BACKGROUND_ID = R.drawable.bg_widget;
    public static final int THEMEDEF_LIGHT_TEXTCOLOR_ID = android.R.color.tertiary_text_light;
    public static final int THEMEDEF_LIGHT_TITLECOLOR_ID = android.R.color.secondary_text_light;
    public static final int THEMEDEF_LIGHT_TIMESUFFIXCOLOR_ID = android.R.color.tertiary_text_light;
    public static final int THEMEDEF_LIGHT_SUNRISECOLOR_ID = R.color.sunIcon_color_rising;
    public static final int THEMEDEF_LIGHT_SUNSETCOLOR_ID = R.color.sunIcon_color_setting;
    public static final float THEMEDEF_LIGHT_TITLESIZE = 10;

    // Theme: Light (no background)
    public static final String THEMEDEF_LIGHT_TRANSPARENT_NAME = "light_transparent";
    public static final String THEMEDEF_LIGHT_TRANSPARENT_DISPLAYSTRING = "Light (transparent)";
    public static final int THEMEDEF_LIGHT_TRANSPARENT_BACKGROUND_ID = android.R.color.transparent;

    // Theme: Dark
    public static final String THEMEDEF_DARK_NAME = "dark";
    public static final String THEMEDEF_DARK_DISPLAYSTRING = "Dark";
    public static final int THEMEDEF_DARK_BACKGROUND_ID = R.drawable.bg_dark;
    public static final int THEMEDEF_DARK_TEXTCOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int THEMEDEF_DARK_TITLECOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int THEMEDEF_DARK_TIMESUFFIXCOLOR_ID = android.R.color.tertiary_text_dark;
    public static final int THEMEDEF_DARK_SUNRISECOLOR_ID = R.color.sunIcon_color_rising;
    public static final int THEMEDEF_DARK_SUNSETCOLOR_ID = R.color.sunIcon_color_setting;
    public static final float THEMEDEF_DARK_TITLESIZE = 10;

    // Theme: Dark (no background)
    public static final String THEMEDEF_DARK_TRANSPARENT_NAME = "dark_transparent";
    public static final String THEMEDEF_DARK_TRANSPARENT_DISPLAYSTRING = "Dark (transparent)";
    public static final int THEMEDEF_DARK_TRANSPARENT_BACKGROUND_ID = android.R.color.transparent;

    // Default Theme: Dark
    public static final String THEMEDEF_DEF_NAME = SuntimesWidgetThemes.THEMEDEF_DARK_NAME ;
    public static final String THEMEDEF_DEF_DISPLAYSTRING = SuntimesWidgetThemes.THEMEDEF_DARK_DISPLAYSTRING;
    public static final int THEMEDEF_DEF_BACKGROUND_ID = SuntimesWidgetThemes.THEMEDEF_DARK_BACKGROUND_ID;
    public static final int THEMEDEF_DEF_TEXTCOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_TEXTCOLOR_ID;
    public static final int THEMEDEF_DEF_TITLECOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_TITLECOLOR_ID;
    public static final int THEMEDEF_DEF_TIMESUFFIXCOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_TIMESUFFIXCOLOR_ID;
    public static final int THEMEDEF_DEF_SUNRISECOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_SUNRISECOLOR_ID;
    public static final int THEMEDEF_DEF_SUNSETCOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_SUNSETCOLOR_ID;
    public static final float THEMEDEF_DEF_TITLESIZE = SuntimesWidgetThemes.THEMEDEF_DARK_TITLESIZE;

    private static boolean initialized = false;
    public static final void initThemes(Context context)
    {
        if (initialized)
        {
            return;
        }

        Resources resources = context.getResources();
        SharedPreferences.Editor themePrefs = context.getSharedPreferences(PREFS_THEMES, Context.MODE_PRIVATE).edit();
        boolean themesAdded = false;

        ThemeDescriptor lightThemeDescriptor = new ThemeDescriptor(THEMEDEF_LIGHT_NAME, THEMEDEF_LIGHT_DISPLAYSTRING);
        if (!SuntimesWidgetThemes.hasValue(lightThemeDescriptor))
        {
            String lightTheme = SuntimesWidgetThemes.themePrefix(THEMEDEF_LIGHT_NAME);
            themePrefs.putString(lightTheme + SuntimesWidgetTheme.THEME_NAME, THEMEDEF_LIGHT_NAME);
            themePrefs.putString(lightTheme + SuntimesWidgetTheme.THEME_DISPLAYSTRING, THEMEDEF_LIGHT_DISPLAYSTRING);
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_BACKGROUND, THEMEDEF_LIGHT_BACKGROUND_ID);
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_TEXTCOLOR, resources.getColor(THEMEDEF_LIGHT_TEXTCOLOR_ID));
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_TITLECOLOR, resources.getColor(THEMEDEF_LIGHT_TITLECOLOR_ID));
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_TIMESUFFIXCOLOR, resources.getColor(THEMEDEF_LIGHT_TIMESUFFIXCOLOR_ID));
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_SUNRISECOLOR, resources.getColor(THEMEDEF_LIGHT_SUNRISECOLOR_ID));
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_SUNSETCOLOR, resources.getColor(THEMEDEF_LIGHT_SUNSETCOLOR_ID));
            themePrefs.putFloat(lightTheme + SuntimesWidgetTheme.THEME_TITLESIZE, THEMEDEF_LIGHT_TITLESIZE);
            addValue(lightThemeDescriptor);
            themesAdded = true;
        }

        ThemeDescriptor lightTransThemeDescriptor = new ThemeDescriptor(THEMEDEF_LIGHT_TRANSPARENT_NAME, THEMEDEF_LIGHT_TRANSPARENT_DISPLAYSTRING);
        if (!SuntimesWidgetThemes.hasValue(lightTransThemeDescriptor))
        {
            String lightTheme = SuntimesWidgetThemes.themePrefix(THEMEDEF_LIGHT_TRANSPARENT_NAME);
            themePrefs.putString(lightTheme + SuntimesWidgetTheme.THEME_NAME, THEMEDEF_LIGHT_TRANSPARENT_NAME);
            themePrefs.putString(lightTheme + SuntimesWidgetTheme.THEME_DISPLAYSTRING, THEMEDEF_LIGHT_TRANSPARENT_DISPLAYSTRING);
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_BACKGROUND, THEMEDEF_LIGHT_TRANSPARENT_BACKGROUND_ID);
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_TEXTCOLOR, resources.getColor(THEMEDEF_LIGHT_TEXTCOLOR_ID));
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_TITLECOLOR, resources.getColor(THEMEDEF_LIGHT_TITLECOLOR_ID));
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_TIMESUFFIXCOLOR, resources.getColor(THEMEDEF_LIGHT_TIMESUFFIXCOLOR_ID));
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_SUNRISECOLOR, resources.getColor(THEMEDEF_LIGHT_SUNRISECOLOR_ID));
            themePrefs.putInt(lightTheme + SuntimesWidgetTheme.THEME_SUNSETCOLOR, resources.getColor(THEMEDEF_LIGHT_SUNSETCOLOR_ID));
            themePrefs.putFloat(lightTheme + SuntimesWidgetTheme.THEME_TITLESIZE, THEMEDEF_LIGHT_TITLESIZE);
            addValue(lightTransThemeDescriptor);
            themesAdded = true;
        }

        ThemeDescriptor darkThemeDescriptor = new ThemeDescriptor(THEMEDEF_DARK_NAME, THEMEDEF_DARK_DISPLAYSTRING);
        if (!SuntimesWidgetThemes.hasValue(darkThemeDescriptor))
        {
            String darkTheme =  SuntimesWidgetThemes.themePrefix(THEMEDEF_DARK_NAME);
            themePrefs.putString(darkTheme + SuntimesWidgetTheme.THEME_NAME, THEMEDEF_DARK_NAME);
            themePrefs.putString(darkTheme + SuntimesWidgetTheme.THEME_DISPLAYSTRING, THEMEDEF_DARK_DISPLAYSTRING);
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_BACKGROUND, THEMEDEF_DARK_BACKGROUND_ID);
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_TEXTCOLOR, resources.getColor(THEMEDEF_DARK_TEXTCOLOR_ID));
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_TITLECOLOR, resources.getColor(THEMEDEF_DARK_TITLECOLOR_ID));
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_TIMESUFFIXCOLOR, resources.getColor(THEMEDEF_DARK_TIMESUFFIXCOLOR_ID));
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_SUNRISECOLOR, resources.getColor(THEMEDEF_DARK_SUNRISECOLOR_ID));
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_SUNSETCOLOR, resources.getColor(THEMEDEF_DARK_SUNSETCOLOR_ID));
            themePrefs.putFloat(darkTheme + SuntimesWidgetTheme.THEME_TITLESIZE, THEMEDEF_DARK_TITLESIZE);
            addValue(darkThemeDescriptor);
            themesAdded = true;
        }

        ThemeDescriptor darkTransThemeDescriptor = new ThemeDescriptor(THEMEDEF_DARK_TRANSPARENT_NAME, THEMEDEF_DARK_TRANSPARENT_DISPLAYSTRING);
        if (!SuntimesWidgetThemes.hasValue(darkTransThemeDescriptor))
        {
            String darkTheme =  SuntimesWidgetThemes.themePrefix(THEMEDEF_DARK_TRANSPARENT_NAME);
            themePrefs.putString(darkTheme + SuntimesWidgetTheme.THEME_NAME, THEMEDEF_DARK_TRANSPARENT_NAME);
            themePrefs.putString(darkTheme + SuntimesWidgetTheme.THEME_DISPLAYSTRING, THEMEDEF_DARK_TRANSPARENT_DISPLAYSTRING);
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_BACKGROUND, THEMEDEF_DARK_TRANSPARENT_BACKGROUND_ID);
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_TEXTCOLOR, resources.getColor(THEMEDEF_DARK_TEXTCOLOR_ID));
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_TITLECOLOR, resources.getColor(THEMEDEF_DARK_TITLECOLOR_ID));
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_TIMESUFFIXCOLOR, resources.getColor(THEMEDEF_DARK_TIMESUFFIXCOLOR_ID));
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_SUNRISECOLOR, resources.getColor(THEMEDEF_DARK_SUNRISECOLOR_ID));
            themePrefs.putInt(darkTheme + SuntimesWidgetTheme.THEME_SUNSETCOLOR, resources.getColor(THEMEDEF_DARK_SUNSETCOLOR_ID));
            themePrefs.putFloat(darkTheme + SuntimesWidgetTheme.THEME_TITLESIZE, THEMEDEF_DARK_TITLESIZE);
            addValue(darkTransThemeDescriptor);
            themesAdded = true;
        }

        if (themesAdded)
        {
            themePrefs.commit();
        }

        initialized = true;
    }

    private static ArrayList<ThemeDescriptor> themes = new ArrayList<>();

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
        if (!initialized)
        {
            initThemes(context);
        }

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
