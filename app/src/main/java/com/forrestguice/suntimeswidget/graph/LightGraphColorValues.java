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

package com.forrestguice.suntimeswidget.graph;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.ColorValues;

/**
 * ColorValues
 */
public class LightGraphColorValues extends ColorValues implements Parcelable
{
    public static final String COLOR_DAY = "color_day";
    public static final String COLOR_NIGHT = "color_night";
    public static final String COLOR_CIVIL = "color_civil";
    public static final String COLOR_NAUTICAL = "color_nautical";
    public static final String COLOR_ASTRONOMICAL = "color_astronomical";

    public static final String COLOR_POINT_FILL = "color_pointFill";
    public static final String COLOR_POINT_STROKE = "color_pointStroke";
    public static final String COLOR_AXIS = "color_axis";
    public static final String COLOR_GRID = "color_grid";
    public static final String COLOR_LABELS = "color_labels";

    public static final String COLOR_SPRING = "color_spring";
    public static final String COLOR_SUMMER = "color_summer";
    public static final String COLOR_AUTUMN = "color_autumn";
    public static final String COLOR_WINTER = "color_winter";

    public static final String[] COLORS = new String[]
            {
            COLOR_DAY, COLOR_CIVIL, COLOR_NAUTICAL, COLOR_ASTRONOMICAL, COLOR_NIGHT,       // 0-4
            COLOR_POINT_FILL, COLOR_POINT_STROKE, COLOR_AXIS, COLOR_GRID, COLOR_LABELS,    // 5-9
            COLOR_SPRING, COLOR_SUMMER, COLOR_AUTUMN, COLOR_WINTER                         // 10-13
    };
    protected static final int[] COLORS_ATTR = new int[]
    {
            R.attr.graphColor_day,                  // 0
            R.attr.graphColor_civil,                // 1
            R.attr.graphColor_nautical,             // 2
            R.attr.graphColor_astronomical,         // 3
            R.attr.graphColor_night,                // 4
            R.attr.graphColor_pointFill,            // 5
            R.attr.graphColor_pointStroke,          // 6
            R.attr.graphColor_axis,                 // 7
            R.attr.graphColor_grid,                 // 8
            R.attr.graphColor_labels,               // 9
            R.attr.springColor,                     // 10
            R.attr.summerColor,                     // 11
            R.attr.fallColor,                       // 12
            R.attr.winterColor,                     // 13
    };
    protected static final int[] COLORS_RES_DARK = new int[]
    {
            R.color.graphColor_day_dark, R.color.graphColor_civil_dark, R.color.graphColor_nautical_dark, R.color.graphColor_astronomical_dark, R.color.graphColor_night_dark,
            R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark, R.color.graphColor_axis_dark, R.color.graphColor_grid_dark, R.color.graphColor_labels_dark,
            R.color.springColor_dark, R.color.summerColor_dark, R.color.fallColor_dark, R.color.winterColor_dark
    };
    protected static final int[] COLORS_RES_LIGHT = new int[]
    {
            R.color.graphColor_day_light, R.color.graphColor_civil_light, R.color.graphColor_nautical_light, R.color.graphColor_astronomical_light, R.color.graphColor_night_light,
            R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light, R.color.graphColor_axis_dark, R.color.graphColor_grid_light, R.color.graphColor_labels_light,
            R.color.springColor_light, R.color.summerColor_light, R.color.fallColor_light, R.color.winterColor_light
    };
    public static final int[] LABELS_RESID = new int[]
    {
            R.string.timeMode_day, R.string.timeMode_civil, R.string.timeMode_nautical, R.string.timeMode_astronomical, R.string.timeMode_night,
            R.string.graph_option_points, R.string.graph_option_points, R.string.graph_option_axis, R.string.graph_option_grid, R.string.graph_option_labels,
            R.string.configLabel_themeColorSpring, R.string.configLabel_themeColorSummer, R.string.configLabel_themeColorFall, R.string.configLabel_themeColorWinter
    };
    protected static final int[] COLORS_FALLBACK = new int[]
    {
            Color.YELLOW, Color.CYAN, Color.BLUE, Color.DKGRAY, Color.BLACK,
            Color.DKGRAY, Color.DKGRAY, Color.DKGRAY, Color.DKGRAY, Color.DKGRAY,
            Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE
    };

    @Override
    public String[] getColorKeys() {
        return COLORS;
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
    public LightGraphColorValues()
    {
        super();
        if (BuildConfig.DEBUG && (COLORS.length != COLORS_FALLBACK.length)) {
            throw new AssertionError("COLORS and COLORS_FALLBACK have different lengths! These arrays should be one-to-one.");
        }
        for (int i=0; i<COLORS.length; i++) {
            setColor(COLORS[i], COLORS_FALLBACK[i]);
            setLabel(COLORS[i], COLORS[i]);
        }
    }
    public LightGraphColorValues(Context context) {
        this(context, true);
    }
    public LightGraphColorValues(Context context, boolean darkTheme)
    {
        super();
        if (BuildConfig.DEBUG && (COLORS.length != COLORS_ATTR.length)) {
            throw new AssertionError("COLORS and COLORS_ATTR have different lengths! These arrays should be one-to-one.");
        }
        int[] defaultResID = darkTheme ? COLORS_RES_DARK : COLORS_RES_LIGHT;
        TypedArray a = context.obtainStyledAttributes(COLORS_ATTR);
        for (int i=0; i<COLORS.length; i++) {
            setColor(COLORS[i], ContextCompat.getColor(context, a.getResourceId(i, defaultResID[i])));
            setLabel(COLORS[i], context.getString(LABELS_RESID[i]));
        }
        a.recycle();
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

    public static LightGraphColorValues getColorDefaults(Context context, boolean darkTheme)
    {
        LightGraphColorValues values = new LightGraphColorValues();
        int[] defaultResID = darkTheme ? COLORS_RES_DARK : COLORS_RES_LIGHT;
        for (int i=0; i<COLORS.length; i++) {
            values.setColor(COLORS[i], ContextCompat.getColor(context, defaultResID[i]));
            values.setLabel(COLORS[i], context.getString(LABELS_RESID[i]));
        }
        values.setID(darkTheme ? context.getString(R.string.widgetThemes_dark) : context.getString(R.string.widgetThemes_light));
        return values;
    }
}
