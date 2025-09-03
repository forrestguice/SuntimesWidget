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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.suntimeswidget.colors.Color;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;
import com.forrestguice.util.Resources;

import java.io.Serializable;

/**
 * ColorValues
 */
public class MoonApsisColorValues extends ResourceColorValues implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String COLOR_MOON_APOGEE_TEXT = AppColorKeys.COLOR_MOON_APOGEE_TEXT;
    public static final String COLOR_MOON_PERIGEE_TEXT = AppColorKeys.COLOR_MOON_PERIGEE_TEXT;

    public String[] getColorKeys() {
        return new String[] {
                COLOR_MOON_APOGEE_TEXT, COLOR_MOON_PERIGEE_TEXT,
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.table_moonRisingColor, R.attr.table_moonSettingColor,
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_themeColorMoonApogeeText, R.string.configLabel_themeColorMoonPerigeeText
        };
    }
    public int[] getColorRoles() {
        return new int[] {
                ROLE_TEXT, ROLE_TEXT
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.table_moon_rising_dark, R.color.table_moon_setting_dark,
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.table_moon_rising_light, R.color.table_moon_setting_light,
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.LTGRAY, Color.DKGRAY,
        };
    }

    public MoonApsisColorValues(ColorValues other) {
        super(other);
    }
    /*private MoonApsisColorValues(Parcel in) {
        super(in);
    }*/
    public MoonApsisColorValues() {
        super();
    }
    public MoonApsisColorValues(Resources context) {
        this(context, true);
    }
    public MoonApsisColorValues(Resources context, boolean darkTheme) {
        super(context, darkTheme);
    }
    public MoonApsisColorValues(String jsonString) {
        super(jsonString);
    }

    /*public static final Creator<MoonApsisColorValues> CREATOR = new Creator<MoonApsisColorValues>()
    {
        public MoonApsisColorValues createFromParcel(Parcel in) {
            return new MoonApsisColorValues(in);
        }
        public MoonApsisColorValues[] newArray(int size) {
            return new MoonApsisColorValues[size];
        }
    };*/

    public static MoonApsisColorValues getColorDefaults(Resources context, boolean darkTheme) {
        return new MoonApsisColorValues(new MoonApsisColorValues().getDefaultValues(context, darkTheme));
    }
}
