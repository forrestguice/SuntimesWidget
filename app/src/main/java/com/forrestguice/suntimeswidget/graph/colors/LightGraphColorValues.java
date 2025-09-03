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

import android.graphics.Color;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.AppColorKeys;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;
import com.forrestguice.util.Resources;

import java.io.Serializable;

/**
 * ColorValues
 */
public class LightGraphColorValues extends ResourceColorValues implements Serializable
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

    public static final String COLOR_AXIS = AppColorKeys.COLOR_AXIS;
    public static final String COLOR_GRID_MAJOR = AppColorKeys.COLOR_GRID_MAJOR;
    public static final String COLOR_GRID_MINOR = AppColorKeys.COLOR_GRID_MINOR;
    public static final String COLOR_LABELS = AppColorKeys.COLOR_LABELS;
    public static final String COLOR_LABELS_BG = AppColorKeys.COLOR_LABELS_BG;

    public static final String COLOR_SPRING = AppColorKeys.COLOR_SPRING;
    public static final String COLOR_SUMMER = AppColorKeys.COLOR_SUMMER;
    public static final String COLOR_AUTUMN = AppColorKeys.COLOR_AUTUMN;
    public static final String COLOR_WINTER = AppColorKeys.COLOR_WINTER;

    public static final String[] COLORS = new String[]
    {
            COLOR_DAY, COLOR_CIVIL, COLOR_NAUTICAL, COLOR_ASTRONOMICAL, COLOR_NIGHT,
            COLOR_SUN_FILL, COLOR_SUN_STROKE,
            COLOR_POINT_FILL, COLOR_POINT_STROKE,
            COLOR_AXIS, COLOR_GRID_MAJOR, COLOR_GRID_MINOR,
            COLOR_LABELS, COLOR_LABELS_BG,
            COLOR_SPRING, COLOR_SUMMER, COLOR_AUTUMN, COLOR_WINTER
    };
    protected static final int[] COLORS_ATTR = new int[]
    {
            R.attr.graphColor_day, R.attr.graphColor_civil, R.attr.graphColor_nautical, R.attr.graphColor_astronomical, R.attr.graphColor_night,
            R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
            R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
            R.attr.graphColor_axis,
            R.attr.graphColor_grid,     // grid_major
            R.attr.graphColor_grid,     // grid_minor
            R.attr.graphColor_labels, R.attr.graphColor_labels_bg,
            R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor,
    };
    protected static final int[] COLORS_RES_DARK = new int[]
    {
            R.color.graphColor_day_dark, R.color.graphColor_civil_dark, R.color.graphColor_nautical_dark, R.color.graphColor_astronomical_dark, R.color.graphColor_night_dark,
            R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
            R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
            R.color.graphColor_axis_dark, R.color.graphColor_grid_dark,  R.color.graphColor_grid_dark,
            R.color.graphColor_labels_dark, R.color.graphColor_labels_bg_dark,
            R.color.springColor_dark, R.color.summerColor_dark, R.color.fallColor_dark, R.color.winterColor_dark
    };
    protected static final int[] COLORS_RES_LIGHT = new int[]
    {
            R.color.graphColor_day_light, R.color.graphColor_civil_light, R.color.graphColor_nautical_light, R.color.graphColor_astronomical_light, R.color.graphColor_night_light,
            R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
            R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
            R.color.graphColor_axis_dark, R.color.graphColor_grid_light, R.color.graphColor_grid_light,
            R.color.graphColor_labels_light, R.color.graphColor_labels_bg_light,
            R.color.springColor_light, R.color.summerColor_light, R.color.fallColor_light, R.color.winterColor_light
    };
    public static final int[] LABELS_RESID = new int[]
    {
            R.string.timeMode_day, R.string.timeMode_civil, R.string.timeMode_nautical, R.string.timeMode_astronomical, R.string.timeMode_night,
            R.string.configLabel_themeColorGraphSunFill, R.string.configLabel_themeColorGraphSunStroke,
            R.string.configLabel_themeColorGraphPointFill, R.string.configLabel_themeColorGraphPointStroke,
            R.string.configLabel_themeColorGraphAxis, R.string.configLabel_themeColorGraphGridMajor, R.string.configLabel_themeColorGraphGridMinor,
            R.string.configLabel_themeColorGraphLabels, R.string.configLabel_themeColorGraphLabelsBG,
            R.string.configLabel_themeColorSpring, R.string.configLabel_themeColorSummer, R.string.configLabel_themeColorFall, R.string.configLabel_themeColorWinter
    };
    public static final int[] COLOR_ROLES = new int[] {
            ROLE_BACKGROUND, ROLE_BACKGROUND, ROLE_BACKGROUND, ROLE_BACKGROUND, ROLE_BACKGROUND,
            ROLE_FOREGROUND, ROLE_FOREGROUND,
            ROLE_FOREGROUND, ROLE_FOREGROUND,
            ROLE_FOREGROUND, ROLE_FOREGROUND, ROLE_FOREGROUND,
            ROLE_TEXT, ROLE_BACKGROUND,
            ROLE_FOREGROUND, ROLE_FOREGROUND, ROLE_FOREGROUND, ROLE_FOREGROUND
    };
    protected static final int[] COLORS_FALLBACK = new int[]
    {
            Color.YELLOW, Color.CYAN, Color.BLUE, Color.DKGRAY, Color.BLACK,
            Color.YELLOW, Color.BLACK,
            Color.DKGRAY, Color.DKGRAY,
            Color.DKGRAY, Color.DKGRAY, Color.DKGRAY,
            Color.DKGRAY, Color.BLACK,
            Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE
    };

    @Override
    public String[] getColorKeys() {
        return COLORS;
    }

    @Override
    public int[] getColorAttrs() {
        return COLORS_ATTR;
    }
    @Override
    public int[] getColorLabelsRes() {
        return LABELS_RESID;
    }
    @Override
    public int[] getColorRoles() {
        return COLOR_ROLES;
    }
    @Override
    public int[] getColorsResDark() {
        return COLORS_RES_DARK;
    }
    @Override
    public int[] getColorsResLight() {
        return COLORS_RES_LIGHT;
    }
    @Override
    public int[] getColorsFallback() {
        return COLORS_FALLBACK;
    }

    public LightGraphColorValues(ColorValues other) {
        super(other);
    }
    /*private LightGraphColorValues(Parcel in) {
        super(in);
    }*/
    public LightGraphColorValues() {
        super();
    }
    public LightGraphColorValues(Resources context) {
        this(context, true);
    }
    public LightGraphColorValues(Resources context, boolean darkTheme) {
        super(context, darkTheme);
    }

    /*public static final Creator<LightGraphColorValues> CREATOR = new Creator<LightGraphColorValues>()
    {
        public LightGraphColorValues createFromParcel(Parcel in) {
            return new LightGraphColorValues(in);
        }
        public LightGraphColorValues[] newArray(int size) {
            return new LightGraphColorValues[size];
        }
    };*/

    public static LightGraphColorValues getColorDefaults(Resources context, boolean darkTheme) {
        return new LightGraphColorValues(new LightGraphColorValues().getDefaultValues(context, darkTheme));
    }
}
