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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

public class SuntimesTheme
{
    public static final String THEME_KEY = "theme_";
    public static final String THEME_NAME = "name";
    public static final String THEME_VERSION = "version";
    public static final String THEME_ISDEFAULT = "isDefault";
    public static final String THEME_DISPLAYSTRING = "display";
    public static final String THEME_BACKGROUND = "backgroundID";
    public static final String THEME_PADDING_LEFT = "padding_left";
    public static final String THEME_PADDING_TOP = "padding_top";
    public static final String THEME_PADDING_RIGHT = "padding_right";
    public static final String THEME_PADDING_BOTTOM = "padding_bottom";
    public static final String THEME_TEXTCOLOR = "textcolor";
    public static final String THEME_TITLECOLOR = "titlecolor";
    public static final String THEME_TIMECOLOR = "timecolor";
    public static final String THEME_TIMESUFFIXCOLOR = "timesuffixcolor";
    public static final String THEME_SUNRISECOLOR = "sunrisecolor";
    public static final String THEME_SUNSETCOLOR = "sunsetcolor";
    public static final String THEME_TITLESIZE = "titlesize";

    private ThemeDescriptor descriptor;

    protected String themeName;
    protected int themeVersion;
    protected boolean themeIsDefault;
    protected String themeDisplayString;

    protected int themeBackground;
    protected int[] themePadding = {0, 0, 0, 0};
    private int[] themePaddingPixels = {-1, -1, -1, -1};
    protected int themeTitleColor;
    protected float themeTitleSize;
    protected int themeTextColor;
    protected int themeTimeColor;
    protected int themeSunriseTextColor;
    protected int themeSunsetTextColor;
    protected int themeTimeSuffixColor;

    public SuntimesTheme()
    {
    }

    public SuntimesTheme(SuntimesTheme otherTheme)
    {
        this.themeVersion = otherTheme.themeVersion;
        this.themeName = otherTheme.themeName;
        this.themeIsDefault = otherTheme.themeIsDefault;
        this.themeDisplayString = otherTheme.themeDisplayString;
        this.themeBackground = otherTheme.themeBackground;

        this.themePadding[0] = otherTheme.themePadding[0];
        this.themePadding[1] = otherTheme.themePadding[1];
        this.themePadding[2] = otherTheme.themePadding[2];
        this.themePadding[3] = otherTheme.themePadding[3];

        this.themeTextColor = otherTheme.themeTextColor;
        this.themeTitleColor = otherTheme.themeTitleColor;
        this.themeTimeColor = otherTheme.themeTimeColor;
        this.themeTimeSuffixColor = otherTheme.themeTimeSuffixColor;
        this.themeSunriseTextColor = otherTheme.themeSunriseTextColor;
        this.themeSunsetTextColor = otherTheme.themeSunsetTextColor;
        this.themeTitleSize = otherTheme.themeTitleSize;
    }

    public boolean initTheme( Context context, String themesPrefix, String themeName, SuntimesTheme defaultTheme )
    {
        SharedPreferences themes = context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE);
        String theme = themePrefix(themeName);

        this.themeVersion = themes.getInt( theme + THEME_VERSION, defaultTheme.themeVersion );
        this.themeName = themes.getString( theme + THEME_NAME, defaultTheme.themeName );
        this.themeIsDefault = themes.getBoolean( theme + THEME_ISDEFAULT, false );
        this.themeDisplayString = themes.getString( theme + THEME_DISPLAYSTRING, defaultTheme.themeDisplayString );
        this.themeBackground = themes.getInt( theme + THEME_BACKGROUND, defaultTheme.themeBackground );

        this.themePadding[0] = themes.getInt( theme + THEME_PADDING_LEFT, defaultTheme.themePadding[0] );
        this.themePadding[1] = themes.getInt( theme + THEME_PADDING_TOP, defaultTheme.themePadding[1] );
        this.themePadding[2] = themes.getInt( theme + THEME_PADDING_RIGHT, defaultTheme.themePadding[2] );
        this.themePadding[3] = themes.getInt( theme + THEME_PADDING_BOTTOM, defaultTheme.themePadding[3] );

        this.themeTextColor = themes.getInt( theme + THEME_TEXTCOLOR, defaultTheme.themeTextColor );
        this.themeTitleColor = themes.getInt( theme + THEME_TITLECOLOR, defaultTheme.themeTitleColor );
        this.themeTimeColor = themes.getInt( theme + THEME_TIMECOLOR, defaultTheme.themeTimeColor );
        this.themeTimeSuffixColor = themes.getInt( theme + THEME_TIMESUFFIXCOLOR, defaultTheme.themeTimeSuffixColor );
        this.themeSunriseTextColor = themes.getInt( theme + THEME_SUNRISECOLOR, defaultTheme.themeSunriseTextColor );
        this.themeSunsetTextColor = themes.getInt( theme + THEME_SUNSETCOLOR, defaultTheme.themeSunsetTextColor );
        this.themeTitleSize = themes.getFloat( theme + THEME_TITLESIZE, defaultTheme.themeTitleSize );

        return true;
    }

    public ThemeDescriptor saveTheme(SharedPreferences themes)
    {
        SharedPreferences.Editor themePrefs = themes.edit();
        String themePrefix = themePrefix(this.themeName);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_VERSION, this.themeVersion);
        themePrefs.putString(themePrefix + SuntimesTheme.THEME_NAME, this.themeName);
        themePrefs.putBoolean(themePrefix + SuntimesTheme.THEME_ISDEFAULT, this.themeIsDefault);
        themePrefs.putString(themePrefix + SuntimesTheme.THEME_DISPLAYSTRING, this.themeDisplayString);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_BACKGROUND, this.themeBackground);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_PADDING_LEFT, this.themePadding[0]);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_PADDING_TOP, this.themePadding[1]);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_PADDING_RIGHT, this.themePadding[2]);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_PADDING_BOTTOM, this.themePadding[3]);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_TEXTCOLOR, this.themeTextColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_TITLECOLOR, this.themeTitleColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_TIMECOLOR, this.themeTimeColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_TIMESUFFIXCOLOR, this.themeTimeSuffixColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SUNRISECOLOR, this.themeSunriseTextColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SUNSETCOLOR, this.themeSunsetTextColor);
        themePrefs.putFloat(themePrefix + SuntimesTheme.THEME_TITLESIZE, this.themeTitleSize);

        themePrefs.apply();

        //noinspection UnnecessaryLocalVariable
        ThemeDescriptor themeDescriptor = themeDescriptor();
        return themeDescriptor;
    }

    public void deleteTheme(SharedPreferences themes)
    {
        if (themeIsDefault)
        {
            Log.w("deleteTheme", themeName + " is flagged default; ignoring request to delete.");
            return;
        }

        SharedPreferences.Editor themePrefs = themes.edit();
        String themePrefix = themePrefix(this.themeName);

        themePrefs.remove(themePrefix + SuntimesTheme.THEME_VERSION);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_NAME);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_ISDEFAULT);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_DISPLAYSTRING);

        themePrefs.remove(themePrefix + SuntimesTheme.THEME_BACKGROUND);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_PADDING_LEFT);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_PADDING_TOP);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_PADDING_RIGHT);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_PADDING_BOTTOM);

        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TEXTCOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TITLECOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TIMECOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TIMESUFFIXCOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_SUNRISECOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_SUNSETCOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TITLESIZE);

        themePrefs.apply();
    }

    public String themeName()
    {
        return this.themeName;
    }

    public int themeVersion()
    {
        return themeVersion;
    }

    public boolean isDefault()
    {
        return themeIsDefault;
    }

    public String themeDisplayString()
    {
        return themeDisplayString;
    }

    public ThemeDescriptor themeDescriptor()
    {
        if (descriptor == null)
        {
            descriptor = new ThemeDescriptor(this.themeName, this.themeDisplayString, this.themeVersion);
        }
        return descriptor;
    }

    public int getTitleColor()
    {
        return themeTitleColor;
    }

    public float getTitleSizeSp()
    {
        return themeTitleSize;
    }

    public int getTextColor()
    {
        return themeTextColor;
    }

    public int getTimeColor()
    {
        return themeTimeColor;
    }

    public int getTimeSuffixColor()
    {
        return themeTimeSuffixColor;
    }

    public int getSunriseTextColor()
    {
        return themeSunriseTextColor;
    }

    public int getSunsetTextColor()
    {
        return themeSunsetTextColor;
    }

    public int getBackgroundId()
    {
        return themeBackground;
    }

    public int[] getPadding()
    {
        return themePadding;
    }

    public int[] getPaddingPixels(Context context)
    {
        if (themePaddingPixels[0] == -1)
        {
            themePaddingPixels = new int[themePadding.length];
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            for (int i=0; i<themePadding.length; i++)
            {
                themePaddingPixels[i] = (int)((metrics.density * this.themePadding[i]) + 0.5f);
            }
        }
        return themePaddingPixels;
    }

    public boolean isInstalled(SharedPreferences themes)
    {
        return SuntimesTheme.isInstalled(themes, themeDescriptor());
    }
    public static boolean isInstalled(SharedPreferences themes, ThemeDescriptor theme)
    {
        String themePrefix = themePrefix(theme.name());
        int installedVersion = themes.getInt(themePrefix + SuntimesTheme.THEME_VERSION, -1);
        return (installedVersion >= theme.version());
    }

    public static String themePrefix(String themeName)
    {
        return THEME_KEY + themeName + "_";
    }

    ////////////////////////////////////////////////
    ////////////////////////////////////////////////

    public static class ThemeDescriptor implements Comparable
    {
        private final String name;
        private String displayString;
        private final int version;

        public ThemeDescriptor(String name, Context context, String themesPrefix)
        {
            SharedPreferences themesPref = context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE);
            String themePrefix = SuntimesTheme.themePrefix(name);
            String themeName = themesPref.getString(themePrefix + THEME_NAME, "");
            if (themeName.equals(name))
            {
                this.name = name;
                this.displayString = themesPref.getString(themePrefix + THEME_DISPLAYSTRING, "");
                this.version = themesPref.getInt(themePrefix + THEME_VERSION, -1);

            } else {
                this.name = "";
                this.displayString = "";
                this.version = -1;
            }
        }

        public ThemeDescriptor(String name, String displayString, int version)
        {
            this.name = name;
            this.displayString = displayString;
            this.version = version;
        }

        public boolean isValid()
        {
            return (!name.isEmpty() && !displayString.isEmpty() && version > -1);
        }

        public void updateDescriptor(Context context, String themesPrefix)
        {
            String themePrefix = SuntimesTheme.themePrefix(name);
            SharedPreferences themesPref = context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE);
            this.displayString = themesPref.getString(themePrefix + THEME_DISPLAYSTRING, "");
        }

        public String name() {
            return name;
        }

        public String displayString()
        {
            return displayString;
        }

        public String toString() {
            return displayString;
        }

        public int version() {
            return version;
        }

        public int ordinal(ThemeDescriptor[] values)
        {
            int ordinal = -1;
            for (int i = 0; i < values.length; i++)
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
            if (another == null || !(another instanceof ThemeDescriptor))
            {
                return false;

            } else {
                ThemeDescriptor other = (ThemeDescriptor) another;
                return name.equals(other.name());
            }
        }

        @Override
        public int compareTo(@NonNull Object another)
        {
            ThemeDescriptor other = (ThemeDescriptor)another;
            return name.compareTo(other.name());
        }
    }

}
