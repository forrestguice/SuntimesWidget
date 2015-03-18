package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SuntimesWidgetTheme
{
    public static final String THEME_NAME = "name";
    public static final String THEME_DISPLAYSTRING = "display";
    public static final String THEME_BACKGROUND = "backgroundID";
    public static final String THEME_TEXTCOLOR = "textcolor";
    public static final String THEME_TITLECOLOR = "titlecolor";
    public static final String THEME_TIMESUFFIXCOLOR = "timesuffixcolor";
    public static final String THEME_SUNRISECOLOR = "sunrisecolor";
    public static final String THEME_SUNSETCOLOR = "sunsetcolor";
    public static final String THEME_TITLESIZE = "titlesize";

    private String themeName;
    private String themeDisplayString;
    private int themeBackground;
    private int themeTitleColor;
    private float themeTitleSize;
    private int themeTextColor;
    private int themeSunriseTextColor;
    private int themeSunsetTextColor;
    private int themeTimeSuffixColor;

    public SuntimesWidgetTheme(Context context)
    {
        initDefault(context);
    }

    private void initDefault(Context context)
    {
        this.themeName = SuntimesWidgetThemes.THEMEDEF_DEF_NAME;
        this.themeDisplayString = SuntimesWidgetThemes.THEMEDEF_DEF_DISPLAYSTRING;
        this.themeBackground = SuntimesWidgetThemes.THEMEDEF_DEF_BACKGROUND_ID;
        this.themeTextColor = context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_TEXTCOLOR_ID);
        this.themeTitleColor = context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_TITLECOLOR_ID);
        this.themeTimeSuffixColor = context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_TIMESUFFIXCOLOR_ID);
        this.themeSunriseTextColor = context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_SUNRISECOLOR_ID);
        this.themeSunsetTextColor = context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_SUNSETCOLOR_ID);
        this.themeTitleSize = SuntimesWidgetThemes.THEMEDEF_DEF_TITLESIZE;
    }

    public boolean initTheme( Context context, String themeName )
    {
        SharedPreferences themes = context.getSharedPreferences(SuntimesWidgetThemes.PREFS_THEMES, Context.MODE_PRIVATE);
        String theme = SuntimesWidgetThemes.themePrefix(themeName);

        this.themeName = themes.getString( theme + THEME_NAME, SuntimesWidgetThemes.THEMEDEF_DEF_NAME );
        this.themeDisplayString = themes.getString( theme + THEME_DISPLAYSTRING, SuntimesWidgetThemes.THEMEDEF_DEF_DISPLAYSTRING );
        this.themeBackground = themes.getInt( theme + THEME_BACKGROUND, SuntimesWidgetThemes.THEMEDEF_DEF_BACKGROUND_ID);
        this.themeTextColor = themes.getInt( theme + THEME_TEXTCOLOR, context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_TEXTCOLOR_ID));
        this.themeTitleColor = themes.getInt( theme + THEME_TITLECOLOR, context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_TITLECOLOR_ID));
        this.themeTimeSuffixColor = themes.getInt(theme + THEME_TIMESUFFIXCOLOR, context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_TIMESUFFIXCOLOR_ID));
        this.themeSunriseTextColor = themes.getInt(theme + THEME_SUNRISECOLOR, context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_SUNRISECOLOR_ID));
        this.themeSunsetTextColor = themes.getInt(theme + THEME_SUNSETCOLOR, context.getResources().getColor(SuntimesWidgetThemes.THEMEDEF_DEF_SUNSETCOLOR_ID));
        this.themeTitleSize = themes.getFloat(theme + THEME_TITLESIZE, SuntimesWidgetThemes.THEMEDEF_DEF_TITLESIZE);
        return true;
    }

    public String getThemeName()
    {
        return themeName;
    }

    public String getThemeDisplayString()
    {
        return themeDisplayString;
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

    public int getThemeTimeSuffixColor()
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

}
