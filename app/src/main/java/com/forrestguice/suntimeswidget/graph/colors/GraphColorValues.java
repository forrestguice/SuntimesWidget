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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.ColorValues;

/**
 * ColorValues
 */
public class GraphColorValues extends ColorValues
{
    public static final String TAG_GRAPH = "graph";

    public static final String COLOR_DAY = "color_day";
    public static final String COLOR_NIGHT = "color_night";
    public static final String COLOR_CIVIL = "color_civil";
    public static final String COLOR_NAUTICAL = "color_nautical";
    public static final String COLOR_ASTRONOMICAL = "color_astronomical";

    public static final String COLOR_SUN_FILL = "color_sunFill";
    public static final String COLOR_SUN_STROKE = "color_sunStroke";

    public static final String COLOR_MOON_FILL = "color_moonFill";
    public static final String COLOR_MOON_STROKE = "color_moonStroke";

    public static final String COLOR_POINT_FILL = "color_pointFill";
    public static final String COLOR_POINT_STROKE = "color_pointStroke";

    public static final String COLOR_AXIS = "color_axis";
    public static final String COLOR_GRID_MAJOR = "color_grid_major";
    public static final String COLOR_GRID_MINOR = "color_grid_minor";
    public static final String COLOR_LABELS = "color_labels";
    public static final String COLOR_LABELS_BG = "color_labels_bg";

    public static final String COLOR_SPRING = "color_spring";
    public static final String COLOR_SUMMER = "color_summer";
    public static final String COLOR_AUTUMN = "color_autumn";
    public static final String COLOR_WINTER = "color_winter";

    public static final String COLOR_SUNPATH_DAY_STROKE = "color_sunpath_day_stroke";
    public static final String COLOR_SUNPATH_DAY_FILL = "color_sunpath_day_fill";
    public static final String COLOR_SUNPATH_NIGHT_STROKE = "color_sunpath_night_stroke";
    public static final String COLOR_SUNPATH_NIGHT_FILL = "color_sunpath_night_fill";

    public static final String COLOR_MOONPATH_DAY_STROKE = "color_moonpath_day_stroke";
    public static final String COLOR_MOONPATH_DAY_FILL = "color_moonpath_day_fill";
    public static final String COLOR_MOONPATH_NIGHT_STROKE = "color_moonpath_night_stroke";
    public static final String COLOR_MOONPATH_NIGHT_FILL = "color_moonpath_night_fill";

    public String[] getColorKeys() {
        return new String[] {
                COLOR_DAY, COLOR_CIVIL, COLOR_NAUTICAL, COLOR_ASTRONOMICAL, COLOR_NIGHT,
                COLOR_SUN_FILL, COLOR_SUN_STROKE, COLOR_MOON_FILL, COLOR_MOON_STROKE,
                COLOR_POINT_FILL, COLOR_POINT_STROKE,
                COLOR_AXIS, COLOR_GRID_MAJOR, COLOR_GRID_MINOR,
                COLOR_LABELS, COLOR_LABELS_BG,
                COLOR_SPRING, COLOR_SUMMER, COLOR_AUTUMN, COLOR_WINTER,
                COLOR_SUNPATH_DAY_STROKE, COLOR_SUNPATH_DAY_FILL,
                COLOR_SUNPATH_NIGHT_STROKE, COLOR_SUNPATH_NIGHT_FILL,
                COLOR_MOONPATH_DAY_STROKE, COLOR_MOONPATH_DAY_FILL,
                COLOR_MOONPATH_NIGHT_STROKE, COLOR_MOONPATH_NIGHT_FILL
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.graphColor_day, R.attr.graphColor_civil, R.attr.graphColor_nautical, R.attr.graphColor_astronomical, R.attr.graphColor_night,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke, R.attr.moonriseColor, R.attr.moonsetColor,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
                R.attr.graphColor_axis, R.attr.graphColor_grid,     // grid_major
                R.attr.graphColor_grid,     // grid_minor
                R.attr.graphColor_labels, R.attr.graphColor_labels_bg,
                R.attr.springColor, R.attr.summerColor, R.attr.fallColor, R.attr.winterColor,
                R.attr.graphColor_day, R.attr.graphColor_day,        // sunpath_day
                R.attr.graphColor_nautical, R.attr.graphColor_nautical,    // sunpath_night
                R.attr.moonriseColor, R.attr.moonriseColor,          // moonpath_day
                R.attr.moonsetColor, R.attr.moonsetColor             // moonpath_night
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.timeMode_day, R.string.timeMode_civil, R.string.timeMode_nautical, R.string.timeMode_astronomical, R.string.timeMode_night,
                0, 0, 0, 0,
                R.string.graph_option_points, R.string.graph_option_points,
                R.string.graph_option_axis, R.string.graph_option_grid, R.string.graph_option_grid,
                R.string.graph_option_labels, R.string.graph_option_labels,
                R.string.configLabel_themeColorSpring, R.string.configLabel_themeColorSummer, R.string.configLabel_themeColorFall, R.string.configLabel_themeColorWinter,
                0, 0, 0, 0,    // TODO: labels
                0, 0, 0, 0,    // TODO: labels
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.graphColor_day_dark, R.color.graphColor_civil_dark, R.color.graphColor_nautical_dark, R.color.graphColor_astronomical_dark, R.color.graphColor_night_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
                R.color.moonIcon_color_rising_dark, R.color.moonIcon_color_setting_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
                R.color.graphColor_axis_dark, R.color.graphColor_grid_dark,  R.color.graphColor_grid_dark,
                R.color.graphColor_labels_dark, R.color.graphColor_labels_bg_dark,
                R.color.springColor_dark, R.color.summerColor_dark, R.color.fallColor_dark, R.color.winterColor_dark,
                R.color.graphColor_day_dark, R.color.graphColor_day_dark,
                R.color.graphColor_nautical_dark, R.color.graphColor_nautical_dark,
                R.color.moonIcon_color_rising_dark, R.color.moonIcon_color_rising_dark,
                R.color.moonIcon_color_setting_dark, R.color.moonIcon_color_setting_dark
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.graphColor_day_light, R.color.graphColor_civil_light, R.color.graphColor_nautical_light, R.color.graphColor_astronomical_light, R.color.graphColor_night_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
                R.color.moonIcon_color_rising_light, R.color.moonIcon_color_setting_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light, R.color.graphColor_axis_dark, R.color.graphColor_grid_light, R.color.graphColor_grid_light,
                R.color.graphColor_labels_light, R.color.graphColor_labels_bg_light,
                R.color.springColor_light, R.color.summerColor_light, R.color.fallColor_light, R.color.winterColor_light,
                R.color.graphColor_day_light, R.color.graphColor_day_light,
                R.color.graphColor_nautical_light, R.color.graphColor_nautical_light,
                R.color.moonIcon_color_rising_light, R.color.moonIcon_color_rising_light,
                R.color.moonIcon_color_setting_light, R.color.moonIcon_color_setting_light
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.YELLOW, Color.CYAN, Color.BLUE, Color.DKGRAY, Color.BLACK,
                Color.DKGRAY, Color.DKGRAY, Color.DKGRAY, Color.DKGRAY,
                Color.DKGRAY, Color.DKGRAY,
                Color.DKGRAY, Color.DKGRAY, Color.DKGRAY,
                Color.DKGRAY, Color.BLACK,
                Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE,
                Color.YELLOW, Color.YELLOW, Color.BLUE, Color.BLUE,
                Color.LTGRAY, Color.LTGRAY, Color.CYAN, Color.CYAN
        };
    }

    public GraphColorValues(ColorValues other) {
        super(other);
    }
    public GraphColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    protected GraphColorValues(Parcel in) {
        super(in);
    }
    public GraphColorValues()
    {
        super();
        if (BuildConfig.DEBUG && (getColorKeys().length != getColorsFallback().length)) {
            throw new AssertionError("COLORS and COLORS_FALLBACK have different lengths! These arrays should be one-to-one.");
        }
        String[] colorKeys = getColorKeys();
        int[] fallbackColors = getColorsFallback();
        for (int i=0; i<colorKeys.length; i++) {
            setColor(colorKeys[i], fallbackColors[i]);
            setLabel(colorKeys[i], colorKeys[i]);
        }
    }

    public GraphColorValues(Context context, boolean darkTheme)
    {
        super();
        if (BuildConfig.DEBUG && (getColorKeys().length != getColorAttrs().length)) {
            throw new AssertionError("COLORS and COLORS_ATTR have different lengths! These arrays should be one-to-one.");
        }
        String[] colorKeys = getColorKeys();
        int[] labelsResID = getColorLabelsRes();
        int[] defaultResID = darkTheme ? getColorsResDark() : getColorsResLight();
        TypedArray a = context.obtainStyledAttributes(getColorAttrs());
        for (int i=0; i<colorKeys.length; i++) {
            setColor(colorKeys[i], ContextCompat.getColor(context, a.getResourceId(i, defaultResID[i])));
            setLabel(colorKeys[i], (labelsResID[i] != 0) ? context.getString(labelsResID[i]) : colorKeys[i]);
        }
        a.recycle();
    }

    public GraphColorValues(String jsonString) {
        super(jsonString);
    }

    public static final Creator<GraphColorValues> CREATOR = new Creator<GraphColorValues>()
    {
        public GraphColorValues createFromParcel(Parcel in) {
            return new GraphColorValues(in);
        }
        public GraphColorValues[] newArray(int size) {
            return new GraphColorValues[size];
        }
    };

    public ColorValues getDefaultValues(Context context, boolean darkTheme)
    {
        ColorValues values = new ColorValues()
        {
            @Override
            public String[] getColorKeys() {
                return GraphColorValues.this.getColorKeys();
            }
        };

        String[] colorKeys = getColorKeys();
        int[] labelsResID = getColorLabelsRes();
        int[] defaultResID = darkTheme ? getColorsResDark() : getColorsResLight();
        for (int i=0; i<colorKeys.length; i++)
        {
            values.setColor(colorKeys[i], ContextCompat.getColor(context, defaultResID[i]));
            values.setLabel(colorKeys[i], (labelsResID[i] != 0) ? context.getString(labelsResID[i]) : colorKeys[i]);
        }
        values.setID(darkTheme ? context.getString(R.string.widgetThemes_dark) : context.getString(R.string.widgetThemes_light));
        return values;
    }

    public static GraphColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new GraphColorValues(new GraphColorValues().getDefaultValues(context, darkTheme));
    }

}
