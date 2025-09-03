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

package com.forrestguice.suntimeswidget.equinox;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.colors.Color;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;
import com.forrestguice.util.Resources;

import java.io.Serializable;

/**
 * EquinoxColorValues
 */
public class EquinoxColorValues extends ResourceColorValues implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String COLOR_SPRING_TEXT = AppColorKeys.COLOR_SPRING_TEXT;
    public static final String COLOR_SUMMER_TEXT = AppColorKeys.COLOR_SUMMER_TEXT;
    public static final String COLOR_AUTUMN_TEXT = AppColorKeys.COLOR_AUTUMN_TEXT;
    public static final String COLOR_WINTER_TEXT = AppColorKeys.COLOR_WINTER_TEXT;

    public String[] getColorKeys() {
        return new String[] {
                COLOR_SPRING_TEXT,
                COLOR_SUMMER_TEXT,
                COLOR_AUTUMN_TEXT,
                COLOR_WINTER_TEXT,
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.table_springColor,
                R.attr.table_summerColor,
                R.attr.table_fallColor,
                R.attr.table_winterColor,
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_themeColorSpring_text,
                R.string.configLabel_themeColorSummer_text,
                R.string.configLabel_themeColorFall_text,
                R.string.configLabel_themeColorWinter_text,
        };
    }
    public int[] getColorRoles() {
        return new int[] {
                ROLE_TEXT,
                ROLE_TEXT,
                ROLE_TEXT,
                ROLE_TEXT
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.springColor_dark,
                R.color.summerColor_dark,
                R.color.fallColor_dark,
                R.color.winterColor_dark,
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.springColor_light,
                R.color.summerColor_light,
                R.color.fallColor_light,
                R.color.winterColor_light,
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.parseColor("#AAEB5B"),
                Color.parseColor("#FFD500"),
                Color.parseColor("#FF9900"),
                Color.parseColor("#37BBF0"),
        };
    }

    public EquinoxColorValues(ColorValues other) {
        super(other);
    }
    /*protected EquinoxColorValues(Parcel in) {
        super(in);
    }*/
    public EquinoxColorValues() {
        super();
    }

    public EquinoxColorValues(Resources context, boolean darkTheme) {
        super(context, darkTheme);
    }

    /*public static final Creator<EquinoxColorValues> CREATOR = new Creator<EquinoxColorValues>()
    {
        public EquinoxColorValues createFromParcel(Parcel in) {
            return new EquinoxColorValues(in);
        }
        public EquinoxColorValues[] newArray(int size) {
            return new EquinoxColorValues[size];
        }
    };*/

    public static EquinoxColorValues getColorDefaults(Resources context, boolean darkTheme) {
        return new EquinoxColorValues(new EquinoxColorValues().getDefaultValues(context, darkTheme));
    }

}
