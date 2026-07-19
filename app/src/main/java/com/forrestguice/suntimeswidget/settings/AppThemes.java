/**
    Copyright (C) 2014-2026 Forrest Guice
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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.support.app.AppCompatDelegateHelper;

import static com.forrestguice.suntimeswidget.settings.AppSettings.TextSize;

public class AppThemes
{
    public static final String THEME_DARK = DarkThemeInfo.THEME_DARK;
    public static final String THEME_LIGHT = LightThemeInfo.THEME_LIGHT;
    public static final String THEME_DAYNIGHT = DayNightThemeInfo.THEME_DAYNIGHT;
    public static final String THEME_SYSTEM = SystemThemeInfo.THEME_SYSTEM;
    public static final String THEME_SYSTEM1 = System1ThemeInfo.THEME_SYSTEM1;
    public static final String THEME_DARK1 = DarkTheme1Info.THEME_DARK1;
    public static final String THEME_LIGHT1 = LightTheme1Info.THEME_LIGHT1;
    public static final String THEME_MONET_SYSTEM = MonetSystemThemeInfo.THEME_MONET_SYSTEM;
    public static final String THEME_MONET_DARK = MonetDarkThemeInfo.THEME_MONET_DARK;
    public static final String THEME_MONET_LIGHT = MonetLightThemeInfo.THEME_MONET_LIGHT;
    public static final String THEME_HOLO_DARK = HoloBlueDarkThemeInfo.THEME_HOLO_DARK;
    public static final String THEME_HOLO_LIGHT = HoloLightThemeInfo.THEME_HOLO_LIGHT;

    public static final String[] THEMES = new String[] { AppSettings.THEME_DEFAULT, THEME_DARK, THEME_LIGHT, THEME_DAYNIGHT, THEME_SYSTEM,
            THEME_SYSTEM1, THEME_DARK1, THEME_LIGHT1,
            THEME_MONET_SYSTEM, THEME_MONET_DARK, THEME_MONET_LIGHT,
            THEME_HOLO_DARK, THEME_HOLO_LIGHT
    };

    @NonNull
    public static AppThemeInfo loadThemeInfo(@Nullable String extendedThemeName)
    {
        AppThemeInfo retValue;
        if (extendedThemeName == null) {
            retValue = info_defaultTheme;
        } else if (extendedThemeName.startsWith(THEME_DAYNIGHT)) {
            retValue = info_dayNightTheme;
        } else {
            retValue = info_defaultTheme;
            for (AppThemeInfo info : appThemeInfo()) {
                if (extendedThemeName.startsWith(info.getThemeName())) {
                    retValue = info;
                    break;
                }
            }
        }
        if (Build.VERSION.SDK_INT < retValue.requiredTargetSdkVersion()) {
            retValue = info_defaultTheme;
            Log.w("loadThemeInfo", retValue.getThemeName() + " requires " + retValue.requiredTargetSdkVersion() + "; falling back to default...");
        }
        return retValue;
    }
    private static final AppThemeInfo info_darkTheme = new DarkThemeInfo();
    private static final AppThemeInfo info_lightTheme = new LightThemeInfo();
    private static final AppThemeInfo info_dayNightTheme = new DayNightThemeInfo();
    private static final AppThemeInfo info_systemTheme = new SystemThemeInfo();
    private static final AppThemeInfo info_system1Theme = new System1ThemeInfo();
    private static final AppThemeInfo info_dark1Theme = new DarkTheme1Info();
    private static final AppThemeInfo info_light1Theme = new LightTheme1Info();

    private static final AppThemeInfo info_monet_systemTheme = new MonetSystemThemeInfo();
    private static final AppThemeInfo info_monet_darkTheme = new MonetDarkThemeInfo();
    private static final AppThemeInfo info_monet_lightTheme = new MonetLightThemeInfo();

    private static final AppThemeInfo info_holo_darkTheme = new HoloBlueDarkThemeInfo();
    private static final AppThemeInfo info_holo_orange_darkTheme = new HoloOrangeDarkThemeInfo();
    private static final AppThemeInfo info_holo_purple_darkTheme = new HoloPurpleDarkThemeInfo();
    private static final AppThemeInfo info_holo_green_darkTheme = new HoloGreenDarkThemeInfo();
    private static final AppThemeInfo info_holo_red_darkTheme = new HoloRedDarkThemeInfo();

    private static final AppThemeInfo info_holo_lightTheme = new HoloLightThemeInfo();
    private static final AppThemeInfo info_holo_orange_lightTheme = new HoloOrangeLightThemeInfo();
    private static final AppThemeInfo info_holo_purple_lightTheme = new HoloPurpleLightThemeInfo();
    private static final AppThemeInfo info_holo_green_lightTheme = new HoloGreenLightThemeInfo();
    private static final AppThemeInfo info_holo_red_lightTheme = new HoloRedLightThemeInfo();

    private static final AppThemeInfo info_defaultTheme = info_systemTheme;

    public static AppThemeInfo[] appThemeInfo()
    {
        if (Build.VERSION.SDK_INT >= 31) {
            return new AppThemeInfo[] {
                    info_systemTheme, info_darkTheme, info_lightTheme,
                    info_system1Theme, info_dark1Theme, info_light1Theme,
                    info_monet_systemTheme, info_monet_darkTheme, info_monet_lightTheme,
                    info_holo_darkTheme, info_holo_orange_darkTheme, info_holo_red_darkTheme, info_holo_purple_darkTheme, info_holo_green_darkTheme,
                    info_holo_lightTheme, info_holo_orange_lightTheme, info_holo_red_lightTheme, info_holo_purple_lightTheme, info_holo_green_lightTheme,
            };
        } else {
            return new AppThemeInfo[] {
                    info_systemTheme, info_darkTheme, info_lightTheme,
                    info_system1Theme, info_dark1Theme, info_light1Theme,
                    info_holo_darkTheme, info_holo_orange_darkTheme, info_holo_red_darkTheme, info_holo_purple_darkTheme, info_holo_green_darkTheme,
                    info_holo_lightTheme, info_holo_orange_lightTheme, info_holo_red_lightTheme, info_holo_purple_lightTheme, info_holo_green_lightTheme,
            };
        }
    }

    /**
     * AppThemeInfo
     */
    public abstract static class AppThemeInfo
    {
        public abstract int getStyleId(Context context, TextSize textSize, @Nullable SuntimesRiseSetData data);
        @NonNull
        public abstract String getThemeName();

        /**
         * @return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_NO;
         */
        public abstract int getDefaultNightMode();

        public String getExtendedThemeName(TextSize textSize) {
            return getExtendedThemeName(getThemeName(), textSize.name());
        }
        public String getExtendedThemeName(String textSize) {
            return getExtendedThemeName(getThemeName(), textSize);
        }

        public int requiredTargetSdkVersion() {
            return 0;
        }

        public String getDisplayString(Context context) {
            return getThemeName();
        }
        @NonNull
        public String toString() {
            return getThemeName();
        }

        public static String getExtendedThemeName(String themeName, String textSize) {
            return themeName + "_" + textSize;
        }
        public static TextSize getTextSize(String extendedThemeName) {
            String[] parts = extendedThemeName.split("_");
            return TextSize.valueOf((parts.length > 0 ? parts[parts.length-1] : TextSize.NORMAL.name()), TextSize.NORMAL);
        }
    }

    public static class SystemThemeInfo extends AppThemeInfo
    {
        public static final String THEME_SYSTEM = "system";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_SYSTEM;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_System_Small;
                case LARGE: return R.style.AppTheme_System_Large;
                case XLARGE: return R.style.AppTheme_System_XLarge;
                case NORMAL: default: return R.style.AppTheme_System;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_systemDefault);
        }
    }

    public static class LightThemeInfo extends AppThemeInfo
    {
        public static final String THEME_LIGHT = "light";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Light_Small;
                case LARGE: return R.style.AppTheme_Light_Large;
                case XLARGE: return R.style.AppTheme_Light_XLarge;
                case NORMAL: default: return R.style.AppTheme_Light;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_lightTheme);
        }
    }

    public static class DarkThemeInfo extends AppThemeInfo
    {
        public static final String THEME_DARK = "dark";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Dark_Small;
                case LARGE: return R.style.AppTheme_Dark_Large;
                case XLARGE: return R.style.AppTheme_Dark_XLarge;
                case NORMAL: default: return R.style.AppTheme_Dark;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_darkTheme);
        }
    }

    public static class DayNightThemeInfo extends AppThemeInfo
    {
        public static final String THEME_DAYNIGHT = "daynight";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_DAYNIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return (isDay == null) ? AppCompatDelegateHelper.MODE_NIGHT_FOLLOW_SYSTEM
                    : (isDay ? AppCompatDelegateHelper.MODE_NIGHT_NO : AppCompatDelegateHelper.MODE_NIGHT_YES);
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            if (data == null)
            {
                data = new SuntimesRiseSetData(context, AppWidgetManager.INVALID_APPWIDGET_ID);
                data.initCalculator();
            }
            isDay = data.isDay();
            switch (size) {
                case SMALL: return (isDay ? R.style.AppTheme_Light_Small : R.style.AppTheme_Dark_Small);
                case LARGE: return (isDay ? R.style.AppTheme_Light_Large : R.style.AppTheme_Dark_Large);
                case NORMAL: default: return (isDay ? R.style.AppTheme_Light : R.style.AppTheme_Dark);
            }
        }
        private Boolean isDay = null;
        public void setIsDay(boolean value) {
            isDay = value;
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_nightMode);
        }
    }

    public static class LightTheme1Info extends AppThemeInfo
    {
        public static final String THEME_LIGHT1 = "contrast_light";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_LIGHT1;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Light1_Small;
                case LARGE: return R.style.AppTheme_Light1_Large;
                case XLARGE: return R.style.AppTheme_Light1_XLarge;
                case NORMAL: default: return R.style.AppTheme_Light1;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_lightTheme1);
        }
    }

    public static class DarkTheme1Info extends AppThemeInfo
    {
        public static final String THEME_DARK1 = "contrast_dark";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_DARK1;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Dark1_Small;
                case LARGE: return R.style.AppTheme_Dark1_Large;
                case XLARGE: return R.style.AppTheme_Dark1_XLarge;
                case NORMAL: default: return R.style.AppTheme_Dark1;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_darkTheme1);
        }
    }

    public static class System1ThemeInfo extends AppThemeInfo
    {
        public static final String THEME_SYSTEM1 = "contrast_system";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_SYSTEM1;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_System1_Small;
                case LARGE: return R.style.AppTheme_System1_Large;
                case XLARGE: return R.style.AppTheme_System1_XLarge;
                case NORMAL: default: return R.style.AppTheme_System1;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_systemDefault1);
        }
    }

    public static class MonetSystemThemeInfo extends AppThemeInfo
    {
        public static final String THEME_MONET_SYSTEM = "monet_system";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_MONET_SYSTEM;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_System_Monet_Small;
                case LARGE: return R.style.AppTheme_System_Monet_Large;
                case XLARGE: return R.style.AppTheme_System_Monet_XLarge;
                case NORMAL: default: return R.style.AppTheme_System_Monet;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_systemMonet);
        }
        @Override
        public int requiredTargetSdkVersion() {
            return 31;
        }
    }

    public static class MonetDarkThemeInfo extends AppThemeInfo
    {
        public static final String THEME_MONET_DARK = "monet_dark";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_MONET_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Dark_Monet_Small;
                case LARGE: return R.style.AppTheme_Dark_Monet_Large;
                case XLARGE: return R.style.AppTheme_Dark_Monet_XLarge;
                case NORMAL: default: return R.style.AppTheme_Dark_Monet;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_darkMonet);
        }
        @Override
        public int requiredTargetSdkVersion() {
            return 31;
        }
    }

    public static class MonetLightThemeInfo extends AppThemeInfo
    {
        public static final String THEME_MONET_LIGHT = "monet_light";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_MONET_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Light_Monet_Small;
                case LARGE: return R.style.AppTheme_Light_Monet_Large;
                case XLARGE: return R.style.AppTheme_Light_Monet_XLarge;
                case NORMAL: default: return R.style.AppTheme_Light_Monet;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_lightMonet);
        }
        @Override
        public int requiredTargetSdkVersion() {
            return 31;
        }
    }

    public static class HoloBlueDarkThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_DARK = "holo_dark";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Dark_Small;
                case LARGE: return R.style.AppTheme_Holo_Dark_Large;
                case XLARGE: return R.style.AppTheme_Holo_Dark_XLarge;
                case NORMAL: default: return R.style.AppTheme_Holo_Dark;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_darkHolo_blue);
        }
    }
    public static class HoloLightThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_LIGHT = "holo_light";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Light_Small;
                case LARGE: return R.style.AppTheme_Holo_Light_Large;
                case XLARGE: return R.style.AppTheme_Holo_Light_XLarge;
                case NORMAL: default: return R.style.AppTheme_Holo_Light;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_lightHolo_blue);
        }
    }

    public static class HoloOrangeDarkThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_ORANGE_DARK = "holo_orange_dark";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_ORANGE_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Dark_Small_Orange;
                case LARGE: return R.style.AppTheme_Holo_Dark_Large_Orange;
                case XLARGE: return R.style.AppTheme_Holo_Dark_XLarge_Orange;
                case NORMAL: default: return R.style.AppTheme_Holo_Dark_Orange;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_darkHolo_orange);
        }
    }

    public static class HoloRedDarkThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_RED_DARK = "holo_red_dark";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_RED_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Dark_Small_Red;
                case LARGE: return R.style.AppTheme_Holo_Dark_Large_Red;
                case XLARGE: return R.style.AppTheme_Holo_Dark_XLarge_Red;
                case NORMAL: default: return R.style.AppTheme_Holo_Dark_Red;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_darkHolo_red);
        }
    }

    public static class HoloPurpleDarkThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_PURPLE_DARK = "holo_purple_dark";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_PURPLE_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Dark_Small_Purple;
                case LARGE: return R.style.AppTheme_Holo_Dark_Large_Purple;
                case XLARGE: return R.style.AppTheme_Holo_Dark_XLarge_Purple;
                case NORMAL: default: return R.style.AppTheme_Holo_Dark_Purple;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_darkHolo_purple);
        }
    }

    public static class HoloGreenDarkThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_GREEN_DARK = "holo_green_dark";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_GREEN_DARK;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_YES;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Dark_Small_Green;
                case LARGE: return R.style.AppTheme_Holo_Dark_Large_Green;
                case XLARGE: return R.style.AppTheme_Holo_Dark_XLarge_Green;
                case NORMAL: default: return R.style.AppTheme_Holo_Dark_Green;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_darkHolo_green);
        }
    }


    public static class HoloOrangeLightThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_ORANGE_LIGHT = "holo_orange_light";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_ORANGE_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Light_Small_Orange;
                case LARGE: return R.style.AppTheme_Holo_Light_Large_Orange;
                case XLARGE: return R.style.AppTheme_Holo_Light_XLarge_Orange;
                case NORMAL: default: return R.style.AppTheme_Holo_Light_Orange;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_lightHolo_orange);
        }
    }

    public static class HoloRedLightThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_RED_LIGHT = "holo_red_light";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_RED_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Light_Small_Red;
                case LARGE: return R.style.AppTheme_Holo_Light_Large_Red;
                case XLARGE: return R.style.AppTheme_Holo_Light_XLarge_Red;
                case NORMAL: default: return R.style.AppTheme_Holo_Light_Red;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_lightHolo_red);
        }
    }

    public static class HoloPurpleLightThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_PURPLE_LIGHT = "holo_purple_light";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_PURPLE_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Light_Small_Purple;
                case LARGE: return R.style.AppTheme_Holo_Light_Large_Purple;
                case XLARGE: return R.style.AppTheme_Holo_Light_XLarge_Purple;
                case NORMAL: default: return R.style.AppTheme_Holo_Light_Purple;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_lightHolo_purple);
        }
    }

    public static class HoloGreenLightThemeInfo extends AppThemeInfo
    {
        public static final String THEME_HOLO_GREEN_LIGHT = "holo_green_light";

        @NonNull
        @Override
        public String getThemeName() {
            return THEME_HOLO_GREEN_LIGHT;
        }
        @Override
        public int getDefaultNightMode() {
            return AppCompatDelegateHelper.MODE_NIGHT_NO;
        }
        @Override
        public int getStyleId(Context context, TextSize size, SuntimesRiseSetData data) {
            switch (size) {
                case SMALL: return R.style.AppTheme_Holo_Light_Small_Green;
                case LARGE: return R.style.AppTheme_Holo_Light_Large_Green;
                case XLARGE: return R.style.AppTheme_Holo_Light_XLarge_Green;
                case NORMAL: default: return R.style.AppTheme_Holo_Light_Green;
            }
        }
        @Override
        public String getDisplayString(Context context) {
            return context.getString(R.string.themes_appThemes_lightHolo_green);
        }
    }

}
