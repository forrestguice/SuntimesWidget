// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.cards;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;

/**
 * ColorValues
 */
public class CardColorValues extends ResourceColorValues implements Parcelable
{
    public static final String COLOR_RISING_SUN = AppColorKeys.COLOR_RISING_SUN;
    public static final String COLOR_RISING_SUN_TEXT = AppColorKeys.COLOR_RISING_SUN_TEXT;

    public static final String COLOR_RISING_MOON = AppColorKeys.COLOR_RISING_MOON;
    public static final String COLOR_RISING_MOON_TEXT = AppColorKeys.COLOR_RISING_MOON_TEXT;

    public static final String COLOR_SETTING_SUN = AppColorKeys.COLOR_SETTING_SUN;
    public static final String COLOR_SETTING_SUN_TEXT = AppColorKeys.COLOR_SETTING_SUN_TEXT;

    public static final String COLOR_SETTING_MOON = AppColorKeys.COLOR_SETTING_MOON;
    public static final String COLOR_SETTING_MOON_TEXT = AppColorKeys.COLOR_SETTING_MOON_TEXT;

    public static final String COLOR_SUN_FILL = AppColorKeys.COLOR_SUN_FILL;
    public static final String COLOR_SUN_STROKE = AppColorKeys.COLOR_SUN_STROKE;

    public String[] getColorKeys() {
        return new String[] {
                COLOR_RISING_SUN, COLOR_RISING_SUN_TEXT,
                COLOR_SETTING_SUN, COLOR_SETTING_SUN_TEXT,
                COLOR_RISING_MOON, COLOR_RISING_MOON_TEXT,
                COLOR_SETTING_MOON, COLOR_SETTING_MOON_TEXT,
                COLOR_SUN_FILL, COLOR_SUN_STROKE
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.sunriseColor, R.attr.table_risingColor,
                R.attr.sunsetColor, R.attr.table_settingColor,
                R.attr.moonriseColor, R.attr.table_moonRisingColor,
                R.attr.moonsetColor, R.attr.table_moonSettingColor,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_themeColorSunrise, 0,
                R.string.configLabel_themeColorSunset, 0,
                R.string.configLabel_themeColorMoonrise, 0,
                R.string.configLabel_themeColorMoonset, 0,
                0, 0
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.sunIcon_color_rising_dark, R.color.table_rising_dark,
                R.color.sunIcon_color_setting_dark, R.color.table_setting_dark,
                R.color.moonIcon_color_rising_dark, R.color.table_moon_rising_dark,
                R.color.moonIcon_color_setting_dark, R.color.table_moon_setting_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.sunIcon_color_rising_light,  R.color.table_rising_light,
                R.color.sunIcon_color_setting_light, R.color.table_setting_light,
                R.color.moonIcon_color_rising_light, R.color.table_moon_rising_light,
                R.color.moonIcon_color_setting_light, R.color.table_moon_setting_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.parseColor("#FFD500"), Color.parseColor("#FFD500"),
                Color.parseColor("#FF9900"), Color.parseColor("#FF9900"),
                Color.LTGRAY, Color.LTGRAY,
                Color.DKGRAY, Color.DKGRAY,
                Color.YELLOW, Color.BLACK,
        };
    }

    public CardColorValues(ColorValues other) {
        super(other);
    }
    public CardColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    private CardColorValues(Parcel in) {
        super(in);
    }
    public CardColorValues() {
        super();
    }
    public CardColorValues(Context context) {
        this(context, true);
    }
    public CardColorValues(Context context, boolean darkTheme) {
        super(context, darkTheme);
    }
    public CardColorValues(String jsonString) {
        super(jsonString);
    }

    public static final Creator<CardColorValues> CREATOR = new Creator<CardColorValues>()
    {
        public CardColorValues createFromParcel(Parcel in) {
            return new CardColorValues(in);
        }
        public CardColorValues[] newArray(int size) {
            return new CardColorValues[size];
        }
    };

    public static CardColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new CardColorValues(new CardColorValues().getDefaultValues(context, darkTheme));
    }
}
