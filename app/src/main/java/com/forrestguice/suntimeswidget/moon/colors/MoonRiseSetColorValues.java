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

package com.forrestguice.suntimeswidget.moon.colors;

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
public class MoonRiseSetColorValues extends ResourceColorValues implements Parcelable
{
    public static final String COLOR_RISING_MOON = AppColorKeys.COLOR_RISING_MOON;
    public static final String COLOR_RISING_MOON_TEXT = AppColorKeys.COLOR_RISING_MOON_TEXT;

    public static final String COLOR_SETTING_MOON = AppColorKeys.COLOR_SETTING_MOON;
    public static final String COLOR_SETTING_MOON_TEXT = AppColorKeys.COLOR_SETTING_MOON_TEXT;

    public String[] getColorKeys() {
        return new String[] {
                COLOR_RISING_MOON, COLOR_RISING_MOON_TEXT,
                COLOR_SETTING_MOON, COLOR_SETTING_MOON_TEXT
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.moonriseColor, R.attr.table_moonRisingColor,
                R.attr.moonsetColor, R.attr.table_moonSettingColor
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_themeColorMoonrise, R.string.configLabel_themeColorMoonrise_text,
                R.string.configLabel_themeColorMoonset, R.string.configLabel_themeColorMoonset_text,
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.moonIcon_color_rising_dark, R.color.table_moon_rising_dark,
                R.color.moonIcon_color_setting_dark, R.color.table_moon_setting_dark,
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.moonIcon_color_rising_light,  R.color.table_moon_rising_light,
                R.color.moonIcon_color_setting_light, R.color.table_moon_setting_light
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.LTGRAY, Color.LTGRAY,
                Color.DKGRAY, Color.DKGRAY,
        };
    }

    public MoonRiseSetColorValues(ColorValues other) {
        super(other);
    }
    public MoonRiseSetColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    private MoonRiseSetColorValues(Parcel in) {
        super(in);
    }
    public MoonRiseSetColorValues() {
        super();
    }
    public MoonRiseSetColorValues(Context context) {
        this(context, true);
    }
    public MoonRiseSetColorValues(Context context, boolean darkTheme) {
        super(context, darkTheme);
    }
    public MoonRiseSetColorValues(String jsonString) {
        super(jsonString);
    }

    public static final Creator<MoonRiseSetColorValues> CREATOR = new Creator<MoonRiseSetColorValues>()
    {
        public MoonRiseSetColorValues createFromParcel(Parcel in) {
            return new MoonRiseSetColorValues(in);
        }
        public MoonRiseSetColorValues[] newArray(int size) {
            return new MoonRiseSetColorValues[size];
        }
    };

    public static MoonRiseSetColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new MoonRiseSetColorValues(new MoonRiseSetColorValues().getDefaultValues(context, darkTheme));
    }
}
