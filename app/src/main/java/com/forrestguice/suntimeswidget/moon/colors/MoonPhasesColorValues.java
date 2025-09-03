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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;

import java.io.Serializable;

/**
 * ColorValues
 */
public class MoonPhasesColorValues extends ResourceColorValues implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String COLOR_MOON_NEW = AppColorKeys.COLOR_MOON_NEW;
    public static final String COLOR_MOON_WAXING = AppColorKeys.COLOR_MOON_WAXING;
    public static final String COLOR_MOON_FULL = AppColorKeys.COLOR_MOON_FULL;
    public static final String COLOR_MOON_WANING = AppColorKeys.COLOR_MOON_WANING;

    public String[] getColorKeys() {
        return new String[] {
                COLOR_MOON_NEW, COLOR_MOON_WAXING,
                COLOR_MOON_FULL, COLOR_MOON_WANING,
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.moonNewColor, R.attr.moonWaxingColor,
                R.attr.moonWaxingColor, R.attr.moonWaningColor
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_themeColorMoonNew, R.string.configLabel_themeColorMoonWaxing,
                R.string.configLabel_themeColorMoonFull, R.string.configLabel_themeColorMoonWaning,
        };
    }
    public int[] getColorRoles() {
        return new int[] {
                ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.moonIcon_color_new_dark, R.color.moonIcon_color_waxing,
                R.color.moonIcon_color_full_dark, R.color.moonIcon_color_waning,
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.moonIcon_color_new_light, R.color.moonIcon_color_waxing,
                R.color.moonIcon_color_full_light, R.color.moonIcon_color_waning,
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.DKGRAY, Color.LTGRAY,
                Color.WHITE, Color.LTGRAY,
        };
    }

    public MoonPhasesColorValues(ColorValues other) {
        super(other);
    }
    /*private MoonPhasesColorValues(Parcel in) {
        super(in);
    }*/
    public MoonPhasesColorValues() {
        super();
    }
    public MoonPhasesColorValues(Context context) {
        this(context, true);
    }
    public MoonPhasesColorValues(Context context, boolean darkTheme) {
        super(context, darkTheme);
    }
    public MoonPhasesColorValues(String jsonString) {
        super(jsonString);
    }

    /*public static final Creator<MoonPhasesColorValues> CREATOR = new Creator<MoonPhasesColorValues>()
    {
        public MoonPhasesColorValues createFromParcel(Parcel in) {
            return new MoonPhasesColorValues(in);
        }
        public MoonPhasesColorValues[] newArray(int size) {
            return new MoonPhasesColorValues[size];
        }
    };*/

    public static MoonPhasesColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new MoonPhasesColorValues(new MoonPhasesColorValues().getDefaultValues(context, darkTheme));
    }
}
