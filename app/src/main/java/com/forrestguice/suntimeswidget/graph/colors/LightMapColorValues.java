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

package com.forrestguice.suntimeswidget.graph.colors;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.colors.Color;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;
import com.forrestguice.util.Resources;

import java.io.Serializable;

/**
 * ColorValues
 */
public class LightMapColorValues extends ResourceColorValues implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String COLOR_DAY = AppColorKeys.COLOR_DAY;
    public static final String COLOR_NIGHT = AppColorKeys.COLOR_NIGHT;
    public static final String COLOR_CIVIL = AppColorKeys.COLOR_CIVIL;
    public static final String COLOR_NAUTICAL = AppColorKeys.COLOR_NAUTICAL;
    public static final String COLOR_ASTRONOMICAL = AppColorKeys.COLOR_ASTRONOMICAL;

    public static final String COLOR_SUN_FILL = AppColorKeys.COLOR_SUN_FILL;
    public static final String COLOR_SUN_STROKE = AppColorKeys.COLOR_SUN_STROKE;

    public static final String COLOR_POINT_FILL = AppColorKeys.COLOR_POINT_FILL;
    public static final String COLOR_POINT_STROKE = AppColorKeys.COLOR_POINT_STROKE;

    @Override
    public String[] getColorKeys() {
        return new String[] {
                COLOR_DAY, COLOR_CIVIL, COLOR_NAUTICAL, COLOR_ASTRONOMICAL, COLOR_NIGHT,
                COLOR_SUN_FILL, COLOR_SUN_STROKE,
                COLOR_POINT_FILL, COLOR_POINT_STROKE
        };
    }
    @Override
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.graphColor_day, R.attr.graphColor_civil, R.attr.graphColor_nautical, R.attr.graphColor_astronomical, R.attr.graphColor_night,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
        };
    }
    @Override
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.timeMode_day, R.string.timeMode_civil, R.string.timeMode_nautical, R.string.timeMode_astronomical, R.string.timeMode_night,
                R.string.configLabel_themeColorGraphSunFill, R.string.configLabel_themeColorGraphSunStroke,
                R.string.configLabel_themeColorGraphPointFill, R.string.configLabel_themeColorGraphPointStroke,
        };
    }
    public int[] getColorRoles() {
        return new int[] {
                ROLE_BACKGROUND, ROLE_BACKGROUND, ROLE_BACKGROUND, ROLE_BACKGROUND, ROLE_BACKGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND
        };
    }
    @Override
    public int[] getColorsResDark() {
        return new int[] {
                R.color.graphColor_day_dark, R.color.graphColor_civil_dark, R.color.graphColor_nautical_dark, R.color.graphColor_astronomical_dark, R.color.graphColor_night_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
        };
    }
    @Override
    public int[] getColorsResLight() {
        return new int[] {
                R.color.graphColor_day_light, R.color.graphColor_civil_light, R.color.graphColor_nautical_light, R.color.graphColor_astronomical_light, R.color.graphColor_night_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
        };
    }
    @Override
    public int[] getColorsFallback() {
        return new int[] {
                Color.YELLOW, Color.CYAN, Color.BLUE, Color.DKGRAY, Color.BLACK,
                Color.YELLOW, Color.BLACK,
                Color.DKGRAY, Color.DKGRAY,
        };
    }

    public LightMapColorValues(ColorValues other) {
        super(other);
    }
    /*private LightMapColorValues(Parcel in) {
        super(in);
    }*/
    public LightMapColorValues() {
        super();
    }
    public LightMapColorValues(Resources context) {
        this(context, true);
    }
    public LightMapColorValues(Resources context, boolean darkTheme) {
        super(context, darkTheme);
    }

    /*public static final Creator<LightMapColorValues> CREATOR = new Creator<LightMapColorValues>()
    {
        public LightMapColorValues createFromParcel(Parcel in) {
            return new LightMapColorValues(in);
        }
        public LightMapColorValues[] newArray(int size) {
            return new LightMapColorValues[size];
        }
    };*/

    public static LightMapColorValues getColorDefaults(Resources context, boolean darkTheme) {
        return new LightMapColorValues(new LightMapColorValues().getDefaultValues(context, darkTheme));
    }
}
