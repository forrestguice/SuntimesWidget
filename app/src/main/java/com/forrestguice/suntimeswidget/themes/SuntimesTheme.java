/**
    Copyright (C) 2014-2018 Forrest Guice
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

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class SuntimesTheme
{
    public static final String THEME_KEY = "theme_";
    public static final String THEME_NAME = "name";
    public static final String THEME_VERSION = "version";
    public static final String THEME_ISDEFAULT = "isDefault";
    public static final String THEME_DISPLAYSTRING = "display";

    public static final String THEME_BACKGROUND = "backgroundID";
    public static final String THEME_PADDING = "padding";
    public static final String THEME_PADDING_LEFT = "padding_left";
    public static final String THEME_PADDING_TOP = "padding_top";
    public static final String THEME_PADDING_RIGHT = "padding_right";
    public static final String THEME_PADDING_BOTTOM = "padding_bottom";

    public static final String THEME_TEXTCOLOR = "textcolor";
    public static final String THEME_TITLECOLOR = "titlecolor";
    public static final String THEME_TIMECOLOR = "timecolor";
    public static final String THEME_TIMESUFFIXCOLOR = "timesuffixcolor";

    public static final String THEME_SUNRISECOLOR = "sunrisecolor";
    public static final String THEME_NOONCOLOR = "nooncolor";
    public static final String THEME_SUNSETCOLOR = "sunsetcolor";
    public static final String THEME_MOONRISECOLOR = "moonrisecolor";
    public static final String THEME_MOONSETCOLOR = "moonsetcolor";

    public static final String THEME_NOONICON_FILL_COLOR = "noonicon_fillcolor";
    public static final String THEME_NOONICON_STROKE_COLOR = "noonicon_strokecolor";
    public static final String THEME_NOONICON_STROKE_WIDTH = "noonicon_strokewidth";
    public static final float THEME_NOONICON_STROKE_WIDTH_MIN = 0.0f;
    public static final float THEME_NOONICON_STROKE_WIDTH_DEF = 3.0f;
    public static final float THEME_NOONICON_STROKE_WIDTH_MAX = 7.0f;

    public static final String THEME_RISEICON_FILL_COLOR = "riseicon_fillcolor";
    public static final String THEME_RISEICON_STROKE_COLOR = "riseicon_strokecolor";
    public static final String THEME_RISEICON_STROKE_WIDTH = "riseicon_strokewidth";
    public static final float THEME_RISEICON_STROKE_WIDTH_MIN = 0.0f;
    public static final float THEME_RISEICON_STROKE_WIDTH_DEF = 0.0f;
    public static final float THEME_RISEICON_STROKE_WIDTH_MAX = 7.0f;

    public static final String THEME_SETICON_FILL_COLOR = "seticon_fillcolor";
    public static final String THEME_SETICON_STROKE_COLOR = "seticon_strokecolor";
    public static final String THEME_SETICON_STROKE_WIDTH = "seticon_strokewidth";
    public static final float THEME_SETICON_STROKE_WIDTH_MIN = 0.0f;
    public static final float THEME_SETICON_STROKE_WIDTH_DEF = 0.0f;
    public static final float THEME_SETICON_STROKE_WIDTH_MAX = 7.0f;

    public static final String THEME_SPRINGCOLOR = "springcolor";
    public static final String THEME_SUMMERCOLOR = "summercolor";
    public static final String THEME_FALLCOLOR = "fallcolor";
    public static final String THEME_WINTERCOLOR = "wintercolor";

    public static final String THEME_TITLESIZE = "titlesize";
    public static final float THEME_TITLESIZE_MIN = 6.0f;
    public static final float THEME_TITLESIZE_DEF = 10.0f;
    public static final float THEME_TITLESIZE_MAX = 32.0f;

    public static final String THEME_TEXTSIZE = "textsize";
    public static final float THEME_TEXTSIZE_MIN = 6.0f;
    public static final float THEME_TEXTSIZE_DEF = 10.0f;
    public static final float THEME_TEXTSIZE_MAX = 32.0f;

    public static final String THEME_TIMESIZE = "timesize";
    public static final float THEME_TIMESIZE_MIN = 6.0f;
    public static final float THEME_TIMESIZE_DEF = 12.0f;
    public static final float THEME_TIMESIZE_MAX = 32.0f;

    public static final String THEME_TIMESUFFIXSIZE = "timesuffixsize";
    public static final float THEME_TIMESUFFIXSIZE_MIN = 4.0f;
    public static final float THEME_TIMESUFFIXSIZE_DEF = 6.0f;
    public static final float THEME_TIMESUFFIXSIZE_MAX = 32.0f;

    private ThemeDescriptor descriptor;

    protected String themeName;
    protected int themeVersion;
    protected boolean themeIsDefault;
    protected String themeDisplayString;

    protected int themeBackground;
    protected int[] themePadding = {0, 0, 0, 0};
    private int[] themePaddingPixels = {-1, -1, -1, -1};

    protected int themeTitleColor;
    protected int themeTextColor;
    protected int themeTimeColor;
    protected int themeTimeSuffixColor;

    protected int themeSunriseTextColor;
    protected int themeSunriseIconColor;
    protected int themeSunriseIconStrokeColor;
    protected int themeSunriseIconStrokeWidth;
    protected int themeSunriseIconStrokePixels = -1;

    protected int themeNoonTextColor;
    protected int themeNoonIconColor;
    protected int themeNoonIconStrokeColor;
    protected int themeNoonIconStrokeWidth;
    protected int themeNoonIconStrokePixels = -1;

    protected int themeSunsetTextColor;
    protected int themeSunsetIconColor;
    protected int themeSunsetIconStrokeColor;
    protected int themeSunsetIconStrokeWidth;
    protected int themeSunsetIconStrokePixels = -1;

    protected int themeSpringColor;
    protected int themeSummerColor;
    protected int themeFallColor;
    protected int themeWinterColor;

    protected int themeMoonriseTextColor;
    protected int themeMoonsetTextColor;

    protected float themeTitleSize = THEME_TITLESIZE_DEF;
    protected float themeTextSize = THEME_TEXTSIZE_DEF;
    protected float themeTimeSize = THEME_TIMESIZE_DEF;
    protected float themeTimeSuffixSize = THEME_TIMESUFFIXSIZE_DEF;

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
        this.themeSunriseIconColor = otherTheme.themeSunriseIconColor;
        this.themeSunriseIconStrokeColor = otherTheme.themeSunriseIconStrokeColor;
        this.themeSunriseIconStrokeWidth = otherTheme.themeSunriseIconStrokeWidth;

        this.themeNoonTextColor = otherTheme.themeNoonTextColor;
        this.themeNoonIconColor = otherTheme.themeNoonIconColor;
        this.themeNoonIconStrokeColor = otherTheme.themeNoonIconStrokeColor;
        this.themeNoonIconStrokeWidth = otherTheme.themeNoonIconStrokeWidth;

        this.themeSunsetTextColor = otherTheme.themeSunsetTextColor;
        this.themeSunsetIconColor = otherTheme.themeSunsetIconColor;
        this.themeSunsetIconStrokeColor = otherTheme.themeSunsetIconStrokeColor;
        this.themeSunsetIconStrokeWidth = otherTheme.themeSunsetIconStrokeWidth;

        this.themeMoonriseTextColor = otherTheme.themeMoonriseTextColor;
        this.themeMoonsetTextColor = otherTheme.themeMoonsetTextColor;

        this.themeSpringColor = otherTheme.themeSpringColor;
        this.themeSummerColor = otherTheme.themeSummerColor;
        this.themeFallColor = otherTheme.themeFallColor;
        this.themeWinterColor = otherTheme.themeWinterColor;

        this.themeTitleSize = otherTheme.themeTitleSize;
        this.themeTextSize = otherTheme.themeTextSize;
        this.themeTimeSize = otherTheme.themeTimeSize;
        this.themeTimeSuffixSize = otherTheme.themeTimeSuffixSize;
    }

    public boolean initTheme( Context context, String themesPrefix, String themeName, SuntimesTheme defaultTheme )
    {
        SharedPreferences themes = context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE);
        String theme = themePrefix(themeName);

        this.themeVersion = themes.getInt(theme + THEME_VERSION, defaultTheme.themeVersion);
        this.themeName = themes.getString(theme + THEME_NAME, defaultTheme.themeName);
        this.themeIsDefault = themes.getBoolean(theme + THEME_ISDEFAULT, false);
        this.themeDisplayString = themes.getString(theme + THEME_DISPLAYSTRING, defaultTheme.themeDisplayString);

        this.themeBackground = defaultTheme.themeBackground;
        String backgroundName;
        try {
            backgroundName = themes.getString(theme + THEME_BACKGROUND, null);
            if (backgroundName != null)
            {
                try {
                    this.themeBackground = ThemeBackground.valueOf(backgroundName).getResID();
                } catch (IllegalArgumentException e) {
                    Log.w("initTheme", "unable to find theme background " + backgroundName);
                    this.themeBackground = ThemeBackground.DARK.getResID();
                }
            }
        } catch (ClassCastException e) {
            Log.w("initTheme", "legacy theme: " + themeName);
            int backgroundID = themes.getInt(theme + THEME_BACKGROUND, 0);
            this.themeBackground = ThemeBackground.getThemeBackground(backgroundID).getResID();
        }

        this.themePadding[0] = themes.getInt( theme + THEME_PADDING_LEFT, defaultTheme.themePadding[0] );
        this.themePadding[1] = themes.getInt( theme + THEME_PADDING_TOP, defaultTheme.themePadding[1] );
        this.themePadding[2] = themes.getInt( theme + THEME_PADDING_RIGHT, defaultTheme.themePadding[2] );
        this.themePadding[3] = themes.getInt( theme + THEME_PADDING_BOTTOM, defaultTheme.themePadding[3] );

        this.themeTextColor = themes.getInt( theme + THEME_TEXTCOLOR, defaultTheme.themeTextColor );
        this.themeTitleColor = themes.getInt( theme + THEME_TITLECOLOR, defaultTheme.themeTitleColor );
        this.themeTimeColor = themes.getInt( theme + THEME_TIMECOLOR, defaultTheme.themeTimeColor );
        this.themeTimeSuffixColor = themes.getInt( theme + THEME_TIMESUFFIXCOLOR, defaultTheme.themeTimeSuffixColor );

        this.themeSunriseTextColor = themes.getInt( theme + THEME_SUNRISECOLOR, defaultTheme.themeSunriseTextColor );
        this.themeSunriseIconColor = themes.getInt( theme + THEME_RISEICON_FILL_COLOR, defaultTheme.themeSunriseIconColor );
        this.themeSunriseIconStrokeColor = themes.getInt( theme + THEME_RISEICON_STROKE_COLOR, defaultTheme.themeSunriseIconStrokeColor );
        this.themeSunriseIconStrokeWidth = themes.getInt( theme + THEME_RISEICON_STROKE_WIDTH, defaultTheme.themeSunriseIconStrokeWidth );

        this.themeNoonTextColor = themes.getInt( theme + THEME_NOONCOLOR, defaultTheme.themeNoonTextColor );
        this.themeNoonIconColor = themes.getInt( theme + THEME_NOONICON_FILL_COLOR, defaultTheme.themeNoonIconColor );
        this.themeNoonIconStrokeColor = themes.getInt( theme + THEME_NOONICON_STROKE_COLOR, defaultTheme.themeNoonIconStrokeColor );
        this.themeNoonIconStrokeWidth = themes.getInt( theme + THEME_NOONICON_STROKE_WIDTH, defaultTheme.themeNoonIconStrokeWidth );

        this.themeSunsetTextColor = themes.getInt( theme + THEME_SUNSETCOLOR, defaultTheme.themeSunsetTextColor );
        this.themeSunsetIconColor = themes.getInt( theme + THEME_SETICON_FILL_COLOR, defaultTheme.themeSunsetIconColor );
        this.themeSunsetIconStrokeColor = themes.getInt( theme + THEME_SETICON_STROKE_COLOR, defaultTheme.themeSunsetIconStrokeColor );
        this.themeSunsetIconStrokeWidth = themes.getInt( theme + THEME_SETICON_STROKE_WIDTH, defaultTheme.themeSunsetIconStrokeWidth );

        this.themeMoonriseTextColor = themes.getInt( theme + THEME_MOONRISECOLOR, defaultTheme.themeMoonriseTextColor );
        this.themeMoonsetTextColor = themes.getInt( theme + THEME_MOONSETCOLOR, defaultTheme.themeMoonsetTextColor );

        this.themeSpringColor = themes.getInt( theme + THEME_SPRINGCOLOR, defaultTheme.themeSpringColor );
        this.themeSummerColor = themes.getInt( theme + THEME_SUMMERCOLOR, defaultTheme.themeSummerColor );
        this.themeFallColor = themes.getInt( theme + THEME_FALLCOLOR, defaultTheme.themeFallColor );
        this.themeWinterColor = themes.getInt( theme + THEME_WINTERCOLOR, defaultTheme.themeWinterColor );

        this.themeTitleSize = themes.getFloat( theme + THEME_TITLESIZE, defaultTheme.themeTitleSize );
        this.themeTextSize = themes.getFloat( theme + THEME_TEXTSIZE, defaultTheme.themeTextSize );
        this.themeTimeSize = themes.getFloat( theme + THEME_TIMESIZE, defaultTheme.themeTimeSize );
        this.themeTimeSuffixSize = themes.getFloat( theme + THEME_TIMESUFFIXSIZE, defaultTheme.themeTimeSuffixSize );

        return true;
    }

    public ThemeDescriptor saveTheme(Context context, String themesPrefix)
    {
        return saveTheme(context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE));
    }

    public ThemeDescriptor saveTheme(SharedPreferences themes)
    {
        SharedPreferences.Editor themePrefs = themes.edit();
        String themePrefix = themePrefix(this.themeName);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_VERSION, this.themeVersion);
        themePrefs.putString(themePrefix + SuntimesTheme.THEME_NAME, this.themeName);
        themePrefs.putBoolean(themePrefix + SuntimesTheme.THEME_ISDEFAULT, this.themeIsDefault);
        themePrefs.putString(themePrefix + SuntimesTheme.THEME_DISPLAYSTRING, this.themeDisplayString);

        themePrefs.putString(themePrefix + SuntimesTheme.THEME_BACKGROUND, ThemeBackground.getThemeBackground(this.themeBackground).name());
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_PADDING_LEFT, this.themePadding[0]);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_PADDING_TOP, this.themePadding[1]);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_PADDING_RIGHT, this.themePadding[2]);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_PADDING_BOTTOM, this.themePadding[3]);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_TEXTCOLOR, this.themeTextColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_TITLECOLOR, this.themeTitleColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_TIMECOLOR, this.themeTimeColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_TIMESUFFIXCOLOR, this.themeTimeSuffixColor);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SUNRISECOLOR, this.themeSunriseTextColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_RISEICON_FILL_COLOR, this.themeSunriseIconColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_RISEICON_STROKE_COLOR, this.themeSunriseIconStrokeColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_RISEICON_STROKE_WIDTH, this.themeSunriseIconStrokeWidth);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_NOONCOLOR, this.themeNoonTextColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_NOONICON_FILL_COLOR, this.themeNoonIconColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_NOONICON_STROKE_COLOR, this.themeNoonIconStrokeColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_NOONICON_STROKE_WIDTH, this.themeNoonIconStrokeWidth);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SUNSETCOLOR, this.themeSunsetTextColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SETICON_FILL_COLOR, this.themeSunsetIconColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SETICON_STROKE_COLOR, this.themeSunsetIconStrokeColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SETICON_STROKE_WIDTH, this.themeSunsetIconStrokeWidth);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_MOONRISECOLOR, this.themeMoonriseTextColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_MOONSETCOLOR, this.themeMoonsetTextColor);

        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SPRINGCOLOR, this.themeSpringColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_SUMMERCOLOR, this.themeSummerColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_FALLCOLOR, this.themeFallColor);
        themePrefs.putInt(themePrefix + SuntimesTheme.THEME_WINTERCOLOR, this.themeWinterColor);

        themePrefs.putFloat(themePrefix + SuntimesTheme.THEME_TITLESIZE, this.themeTitleSize);
        themePrefs.putFloat(themePrefix + SuntimesTheme.THEME_TEXTSIZE, this.themeTextSize);
        themePrefs.putFloat(themePrefix + SuntimesTheme.THEME_TIMESIZE, this.themeTimeSize);
        themePrefs.putFloat(themePrefix + SuntimesTheme.THEME_TIMESUFFIXSIZE, this.themeTimeSuffixSize);

        themePrefs.apply();

        //noinspection UnnecessaryLocalVariable
        ThemeDescriptor themeDescriptor = themeDescriptor();
        return themeDescriptor;
    }

    public void deleteTheme(Context context, String themesPrefix)
    {
        deleteTheme(context.getSharedPreferences(themesPrefix, Context.MODE_PRIVATE));
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
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_RISEICON_FILL_COLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_RISEICON_STROKE_COLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_RISEICON_STROKE_WIDTH);

        themePrefs.remove(themePrefix + SuntimesTheme.THEME_NOONCOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_NOONICON_FILL_COLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_NOONICON_STROKE_COLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_NOONICON_STROKE_WIDTH);

        themePrefs.remove(themePrefix + SuntimesTheme.THEME_SUNSETCOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_SETICON_FILL_COLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_SETICON_STROKE_COLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_SETICON_STROKE_WIDTH);

        themePrefs.remove(themePrefix + SuntimesTheme.THEME_MOONRISECOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_MOONSETCOLOR);

        themePrefs.remove(themePrefix + SuntimesTheme.THEME_SPRINGCOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_SUMMERCOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_FALLCOLOR);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_WINTERCOLOR);

        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TITLESIZE);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TEXTSIZE);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TIMESIZE);
        themePrefs.remove(themePrefix + SuntimesTheme.THEME_TIMESUFFIXSIZE);

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

    public int getSunriseIconColor()
    {
        return themeSunriseIconColor;
    }

    public int getSunriseIconStrokeColor()
    {
        return themeSunriseIconStrokeColor;
    }

    public int getSunriseIconStrokeWidth()
    {
        return (themeSunriseIconStrokeWidth < THEME_RISEICON_STROKE_WIDTH_MIN) ? (int)THEME_RISEICON_STROKE_WIDTH_DEF :
                (themeSunriseIconStrokeWidth > THEME_RISEICON_STROKE_WIDTH_MAX) ? (int)THEME_RISEICON_STROKE_WIDTH_MAX : themeSunriseIconStrokeWidth;
    }

    public int getSunriseIconStrokePixels(Context context)
    {
        //if (themeSunriseIconStrokePixels == -1)
        //{
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            themeSunriseIconStrokePixels = (int)((metrics.density * getSunriseIconStrokeWidth()) + 0.5f);
        //}
        return themeSunriseIconStrokePixels;
    }

    public int getNoonTextColor()
    {
        return themeNoonTextColor;
    }

    public int getNoonIconColor()
    {
        return themeNoonIconColor;
    }

    public int getNoonIconStrokeColor()
    {
        return themeNoonIconStrokeColor;
    }

    public int getNoonIconStrokeWidth()
    {
        return (themeNoonIconStrokeWidth < THEME_NOONICON_STROKE_WIDTH_MIN) ? (int)THEME_NOONICON_STROKE_WIDTH_DEF :
                (themeNoonIconStrokeWidth > THEME_NOONICON_STROKE_WIDTH_MAX) ? (int)THEME_NOONICON_STROKE_WIDTH_MAX : themeNoonIconStrokeWidth;
    }

    public int getNoonIconStrokePixels(Context context)
    {
        //if (themeNoonIconStrokePixels == -1)
        //{
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            themeNoonIconStrokePixels = (int)((metrics.density * getNoonIconStrokeWidth()) + 0.5f);
        //}
        return themeNoonIconStrokePixels;
    }

    public int getMoonriseTextColor()
    {
        return themeMoonriseTextColor;
    }

    public int getMoonsetTextColor()
    {
        return themeMoonsetTextColor;
    }

    public int getSunsetTextColor()
    {
        return themeSunsetTextColor;
    }

    public int getSunsetIconColor()
    {
        return themeSunsetIconColor;
    }

    public int getSunsetIconStrokeColor()
    {
        return themeSunsetIconStrokeColor;
    }

    public int getSunsetIconStrokeWidth()
    {
        return (themeSunsetIconStrokeWidth < THEME_SETICON_STROKE_WIDTH_MIN) ? (int)THEME_SETICON_STROKE_WIDTH_DEF :
                (themeSunsetIconStrokeWidth > THEME_SETICON_STROKE_WIDTH_MAX) ? (int)THEME_SETICON_STROKE_WIDTH_MAX : themeSunsetIconStrokeWidth;
    }

    public int getSunsetIconStrokePixels(Context context)
    {
        //if (themeSunsetIconStrokePixels == -1)
        //{
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            themeSunsetIconStrokePixels = (int)((metrics.density * getSunsetIconStrokeWidth()) + 0.5f);
        //}
        return themeSunsetIconStrokePixels;
    }

    public int getSpringColor()
    {
        return themeSpringColor;
    }
    public int getSummerColor()
    {
        return themeSummerColor;
    }
    public int getFallColor()
    {
        return themeFallColor;
    }
    public int getWinterColor()
    {
        return themeWinterColor;
    }
    public int getSeasonColor(WidgetSettings.SolsticeEquinoxMode event)
    {
        switch (event)
        {
            case SOLSTICE_WINTER:
                return getWinterColor();
            case EQUINOX_AUTUMNAL:
                return getFallColor();
            case SOLSTICE_SUMMER:
                return getSummerColor();
            case EQUINOX_VERNAL:
            default:
                return getSpringColor();
        }
    }

    public float getTitleSizeSp()
    {
        return (themeTitleSize < THEME_TITLESIZE_MIN) ? THEME_TITLESIZE_DEF :
                (themeTitleSize > THEME_TITLESIZE_MAX) ? THEME_TITLESIZE_MAX : themeTitleSize;
    }

    public float getTextSizeSp()
    {
        return (themeTextSize < THEME_TEXTSIZE_MIN) ? THEME_TEXTSIZE_DEF :
                (themeTextSize > THEME_TEXTSIZE_MAX) ? THEME_TEXTSIZE_MAX : themeTextSize;
    }

    public float getTimeSizeSp()
    {
        return (themeTimeSize < THEME_TIMESIZE_MIN) ? THEME_TIMESIZE_DEF :
                (themeTimeSize > THEME_TIMESIZE_MAX) ? THEME_TIMESIZE_MAX : themeTimeSize;
    }

    public float getTimeSuffixSizeSp()
    {
        return (themeTimeSuffixSize < THEME_TIMESUFFIXSIZE_MIN) ? THEME_TIMESUFFIXSIZE_DEF :
                (themeTimeSuffixSize > THEME_TIMESUFFIXSIZE_MAX) ? THEME_TIMESUFFIXSIZE_MAX : themeTimeSuffixSize;
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
