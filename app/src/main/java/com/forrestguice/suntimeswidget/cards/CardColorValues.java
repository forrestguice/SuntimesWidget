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
    public static final String COLOR_RISING = AppColorKeys.COLOR_RISING;
    public static final String COLOR_RISING_TEXT = AppColorKeys.COLOR_RISING_TEXT;

    public static final String COLOR_SETTING = AppColorKeys.COLOR_SETTING;
    public static final String COLOR_SETTING_TEXT = AppColorKeys.COLOR_SETTING_TEXT;

    public String[] getColorKeys() {
        return new String[] {
                COLOR_RISING, COLOR_RISING_TEXT,
                COLOR_SETTING, COLOR_SETTING_TEXT,
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.sunriseColor, R.attr.table_risingColor,
                R.attr.sunsetColor, R.attr.table_settingColor,
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_themeColorSunrise, 0,
                R.string.configLabel_themeColorSunset, 0,
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.sunIcon_color_rising_dark, R.color.table_rising_dark,
                R.color.sunIcon_color_setting_dark, R.color.table_setting_dark,
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.sunIcon_color_rising_light,  R.color.table_rising_light,
                R.color.sunIcon_color_setting_light, R.color.table_setting_light,
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.parseColor("#FFD500"), Color.parseColor("#FFD500"),
                Color.parseColor("#FF9900"), Color.parseColor("#FF9900"),
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
