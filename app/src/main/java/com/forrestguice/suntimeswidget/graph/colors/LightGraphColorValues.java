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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.ColorValues;

/**
 * ColorValues
 */
public class LightGraphColorValues extends GraphColorValues implements Parcelable
{
    public static final String TAG_LIGHTGRAPH = "lightgraph";

    public static final String[] COLORS = new String[]
    {
            COLOR_DAY, COLOR_CIVIL, COLOR_NAUTICAL, COLOR_ASTRONOMICAL, COLOR_NIGHT,
            COLOR_POINT_FILL, COLOR_POINT_STROKE, COLOR_AXIS, COLOR_GRID_MAJOR, COLOR_GRID_MINOR,
            COLOR_LABELS, COLOR_LABELS_BG,
            COLOR_SPRING, COLOR_SUMMER, COLOR_AUTUMN, COLOR_WINTER
    };
    protected static final int[] COLORS_ATTR = new int[]
    {
            R.attr.graphColor_day, R.attr.graphColor_civil, R.attr.graphColor_nautical, R.attr.graphColor_astronomical, R.attr.graphColor_night,
            R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke, R.attr.graphColor_axis,
            R.attr.graphColor_grid,     // grid_major
            R.attr.graphColor_grid,     // grid_minor
            R.attr.graphColor_labels, R.attr.graphColor_labels_bg,
            R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor,
    };
    protected static final int[] COLORS_RES_DARK = new int[]
    {
            R.color.graphColor_day_dark, R.color.graphColor_civil_dark, R.color.graphColor_nautical_dark, R.color.graphColor_astronomical_dark, R.color.graphColor_night_dark,
            R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark, R.color.graphColor_axis_dark, R.color.graphColor_grid_dark,  R.color.graphColor_grid_dark,
            R.color.graphColor_labels_dark, R.color.graphColor_labels_bg_dark,
            R.color.springColor_dark, R.color.summerColor_dark, R.color.fallColor_dark, R.color.winterColor_dark
    };
    protected static final int[] COLORS_RES_LIGHT = new int[]
    {
            R.color.graphColor_day_light, R.color.graphColor_civil_light, R.color.graphColor_nautical_light, R.color.graphColor_astronomical_light, R.color.graphColor_night_light,
            R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light, R.color.graphColor_axis_dark, R.color.graphColor_grid_light, R.color.graphColor_grid_light,
            R.color.graphColor_labels_light, R.color.graphColor_labels_bg_light,
            R.color.springColor_light, R.color.summerColor_light, R.color.fallColor_light, R.color.winterColor_light
    };
    public static final int[] LABELS_RESID = new int[]
    {
            R.string.timeMode_day, R.string.timeMode_civil, R.string.timeMode_nautical, R.string.timeMode_astronomical, R.string.timeMode_night,
            R.string.graph_option_points, R.string.graph_option_points, R.string.graph_option_axis, R.string.graph_option_grid, R.string.graph_option_grid,
            R.string.graph_option_labels, R.string.graph_option_labels,
            R.string.configLabel_themeColorSpring, R.string.configLabel_themeColorSummer, R.string.configLabel_themeColorFall, R.string.configLabel_themeColorWinter
    };
    protected static final int[] COLORS_FALLBACK = new int[]
    {
            Color.YELLOW, Color.CYAN, Color.BLUE, Color.DKGRAY, Color.BLACK,
            Color.DKGRAY, Color.DKGRAY, Color.DKGRAY, Color.DKGRAY, Color.DKGRAY,
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
    public LightGraphColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    private LightGraphColorValues(Parcel in) {
        super(in);
    }
    public LightGraphColorValues() {
        super();
    }
    public LightGraphColorValues(Context context) {
        this(context, true);
    }
    public LightGraphColorValues(Context context, boolean darkTheme) {
        super(context, darkTheme);
    }
    public LightGraphColorValues(String jsonString) {
        super(jsonString);
    }

    public static final Creator<LightGraphColorValues> CREATOR = new Creator<LightGraphColorValues>()
    {
        public LightGraphColorValues createFromParcel(Parcel in) {
            return new LightGraphColorValues(in);
        }
        public LightGraphColorValues[] newArray(int size) {
            return new LightGraphColorValues[size];
        }
    };

    public static LightGraphColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new LightGraphColorValues(new LightGraphColorValues().getDefaultValues(context, darkTheme));
    }
}
