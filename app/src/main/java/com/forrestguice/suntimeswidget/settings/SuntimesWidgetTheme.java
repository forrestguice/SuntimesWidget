package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SuntimesWidgetTheme
{
    public static final String THEME_NAME = "_name";
    public static final String THEME_DISPLAYSTRING = "_display";
    public static final String THEME_BACKGROUND = "_backgroundID";
    public static final String THEME_TEXTCOLOR = "_textcolor";
    public static final String THEME_TITLECOLOR = "_titlecolor";
    public static final String THEME_TIMESUFFIXCOLOR = "_timesuffixcolor";
    public static final String THEME_SUNRISECOLOR = "_sunrisecolor";
    public static final String THEME_SUNSETCOLOR = "_sunsetcolor";

    public static final String THEMEDEF_DEF_NAME = SuntimesWidgetThemes.THEMEDEF_DARK_NAME ;
    public static final String THEMEDEF_DEF_DISPLAYSTRING = SuntimesWidgetThemes.THEMEDEF_DARK_DISPLAYSTRING;
    public static final int THEMEDEF_DEF_BACKGROUND_ID = SuntimesWidgetThemes.THEMEDEF_DARK_BACKGROUND_ID;
    public static final int THEMEDEF_DEF_TEXTCOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_TEXTCOLOR_ID;
    public static final int THEMEDEF_DEF_TITLECOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_TITLECOLOR_ID;
    public static final int THEMEDEF_DEF_TIMESUFFIXCOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_TIMESUFFIXCOLOR_ID;
    public static final int THEMEDEF_DEF_SUNRISECOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_SUNRISECOLOR_ID;
    public static final int THEMEDEF_DEF_SUNSETCOLOR_ID = SuntimesWidgetThemes.THEMEDEF_DARK_SUNSETCOLOR_ID;

    private String themeName;
    private String themeDisplayString;
    private int themeBackground;
    private int themeTitleColor;
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
        this.themeName = THEMEDEF_DEF_NAME;
        this.themeDisplayString = THEMEDEF_DEF_DISPLAYSTRING;
        this.themeBackground = THEMEDEF_DEF_BACKGROUND_ID;
        this.themeTextColor = context.getResources().getColor(THEMEDEF_DEF_TEXTCOLOR_ID);
        this.themeTitleColor = context.getResources().getColor(THEMEDEF_DEF_TITLECOLOR_ID);
        this.themeTimeSuffixColor = context.getResources().getColor(THEMEDEF_DEF_TIMESUFFIXCOLOR_ID);
        this.themeSunriseTextColor = context.getResources().getColor(THEMEDEF_DEF_SUNRISECOLOR_ID);
        this.themeSunsetTextColor = context.getResources().getColor(THEMEDEF_DEF_SUNSETCOLOR_ID);
    }

    public boolean initTheme( Context context, String themeName )
    {
        SharedPreferences themes = context.getSharedPreferences(SuntimesWidgetThemes.PREFS_THEMES, Context.MODE_PRIVATE);
        String theme = SuntimesWidgetThemes.themePrefix(themeName);

        this.themeName = themes.getString( theme + THEME_NAME, THEMEDEF_DEF_NAME );
        this.themeDisplayString = themes.getString( theme + THEME_DISPLAYSTRING, THEMEDEF_DEF_DISPLAYSTRING );
        this.themeBackground = themes.getInt( theme + THEME_BACKGROUND, THEMEDEF_DEF_BACKGROUND_ID);
        this.themeTextColor = themes.getInt( theme + THEME_TEXTCOLOR, context.getResources().getColor(THEMEDEF_DEF_TEXTCOLOR_ID));
        this.themeTitleColor = themes.getInt( theme + THEME_TITLECOLOR, context.getResources().getColor(THEMEDEF_DEF_TITLECOLOR_ID));
        this.themeTimeSuffixColor = themes.getInt(theme + THEME_TIMESUFFIXCOLOR, context.getResources().getColor(THEMEDEF_DEF_TIMESUFFIXCOLOR_ID));
        this.themeSunriseTextColor = themes.getInt(theme + THEME_SUNRISECOLOR, context.getResources().getColor(THEMEDEF_DEF_SUNRISECOLOR_ID));
        this.themeSunsetTextColor = themes.getInt(theme + THEME_SUNSETCOLOR, context.getResources().getColor(THEMEDEF_DEF_SUNSETCOLOR_ID));

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
