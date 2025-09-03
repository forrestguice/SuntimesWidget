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
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;
import com.forrestguice.util.Resources;

import java.io.Serializable;

/**
 * ColorValues
 */
public class LineGraphColorValues extends ResourceColorValues implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String COLOR_GRAPH_BG = AppColorKeys.COLOR_GRAPH_BG;

    public static final String COLOR_SUN_FILL = AppColorKeys.COLOR_SUN_FILL;
    public static final String COLOR_SUN_STROKE = AppColorKeys.COLOR_SUN_STROKE;

    public static final String COLOR_MOON_FILL = AppColorKeys.COLOR_MOON_FILL;
    public static final String COLOR_MOON_STROKE = AppColorKeys.COLOR_MOON_STROKE;

    public static final String COLOR_POINT_FILL = AppColorKeys.COLOR_POINT_FILL;
    public static final String COLOR_POINT_STROKE = AppColorKeys.COLOR_POINT_STROKE;

    public static final String COLOR_AXIS = AppColorKeys.COLOR_AXIS;
    public static final String COLOR_GRID_MAJOR = AppColorKeys.COLOR_GRID_MAJOR;
    public static final String COLOR_GRID_MINOR = AppColorKeys.COLOR_GRID_MINOR;
    public static final String COLOR_LABELS = AppColorKeys.COLOR_LABELS;
    public static final String COLOR_LABELS_BG = AppColorKeys.COLOR_LABELS_BG;

    public static final String COLOR_SUNPATH_DAY_STROKE = AppColorKeys.COLOR_SUNPATH_DAY_STROKE;
    public static final String COLOR_SUNPATH_DAY_FILL = AppColorKeys.COLOR_SUNPATH_DAY_FILL;
    public static final String COLOR_SUNPATH_NIGHT_STROKE = AppColorKeys.COLOR_SUNPATH_NIGHT_STROKE;
    public static final String COLOR_SUNPATH_NIGHT_FILL = AppColorKeys.COLOR_SUNPATH_NIGHT_FILL;

    public static final String COLOR_MOONPATH_DAY_STROKE = AppColorKeys.COLOR_MOONPATH_DAY_STROKE;
    public static final String COLOR_MOONPATH_DAY_FILL = AppColorKeys.COLOR_MOONPATH_DAY_FILL;
    public static final String COLOR_MOONPATH_NIGHT_STROKE = AppColorKeys.COLOR_MOONPATH_NIGHT_STROKE;
    public static final String COLOR_MOONPATH_NIGHT_FILL = AppColorKeys.COLOR_MOONPATH_NIGHT_FILL;

    public String[] getColorKeys() {
        return new String[] {
                COLOR_GRAPH_BG,
                COLOR_SUN_FILL, COLOR_SUN_STROKE,
                COLOR_MOON_FILL, COLOR_MOON_STROKE,
                COLOR_POINT_FILL, COLOR_POINT_STROKE,
                COLOR_AXIS, COLOR_GRID_MAJOR, COLOR_GRID_MINOR,
                COLOR_LABELS, COLOR_LABELS_BG,
                COLOR_SUNPATH_DAY_FILL, COLOR_SUNPATH_DAY_STROKE,
                COLOR_SUNPATH_NIGHT_FILL, COLOR_SUNPATH_NIGHT_STROKE,
                COLOR_MOONPATH_DAY_FILL, COLOR_MOONPATH_DAY_STROKE,
                COLOR_MOONPATH_NIGHT_FILL, COLOR_MOONPATH_NIGHT_STROKE,
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.graphColor_night,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
                R.attr.moonriseColor, R.attr.moonsetColor,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
                R.attr.graphColor_axis, R.attr.graphColor_grid,     // grid_major
                R.attr.graphColor_grid,     // grid_minor
                R.attr.graphColor_labels, R.attr.graphColor_labels_bg,
                R.attr.graphColor_day, R.attr.graphColor_day,        // sunpath_day
                R.attr.graphColor_nautical, R.attr.graphColor_nautical,    // sunpath_night
                R.attr.moonriseColor, R.attr.moonriseColor,          // moonpath_day
                R.attr.moonsetColor, R.attr.moonsetColor             // moonpath_night
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_themeColorGraphBackground,
                R.string.configLabel_themeColorGraphSunFill, R.string.configLabel_themeColorGraphSunStroke,
                R.string.configLabel_themeColorGraphMoonFill, R.string.configLabel_themeColorGraphMoonStroke,
                R.string.configLabel_themeColorGraphPointFill, R.string.configLabel_themeColorGraphPointStroke,
                R.string.configLabel_themeColorGraphAxis, R.string.configLabel_themeColorGraphGridMajor, R.string.configLabel_themeColorGraphGridMinor,
                R.string.configLabel_themeColorGraphLabels, R.string.configLabel_themeColorGraphLabelsBG,
                R.string.configLabel_themeColorGraphSunPathDayFill, R.string.configLabel_themeColorGraphSunPathDayStroke,
                R.string.configLabel_themeColorGraphSunPathNightFill, R.string.configLabel_themeColorGraphSunPathNightStroke,
                R.string.configLabel_themeColorGraphMoonPathDayFill, R.string.configLabel_themeColorGraphMoonPathDayStroke,
                R.string.configLabel_themeColorGraphMoonPathNightFill, R.string.configLabel_themeColorGraphMoonPathNightStroke,
        };
    }
    public int[] getColorRoles() {
        return new int[] {
                ROLE_BACKGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_TEXT, ROLE_BACKGROUND,
                ROLE_BACKGROUND, ROLE_FOREGROUND,
                ROLE_BACKGROUND, ROLE_FOREGROUND,
                ROLE_BACKGROUND, ROLE_FOREGROUND,
                ROLE_BACKGROUND, ROLE_FOREGROUND
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.graphColor_night_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
                R.color.moonIcon_color_rising_dark, R.color.moonIcon_color_setting_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
                R.color.graphColor_axis_dark, R.color.graphColor_grid_dark,  R.color.graphColor_grid_dark,
                R.color.graphColor_labels_dark, R.color.graphColor_labels_bg_dark,
                R.color.graphColor_day_dark, R.color.graphColor_day_dark,
                R.color.graphColor_nautical_dark, R.color.graphColor_nautical_dark,
                R.color.moonIcon_color_rising_dark, R.color.moonIcon_color_rising_dark,
                R.color.moonIcon_color_setting_dark, R.color.moonIcon_color_setting_dark
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.graphColor_night_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
                R.color.moonIcon_color_rising_light, R.color.moonIcon_color_setting_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light, R.color.graphColor_axis_dark, R.color.graphColor_grid_light, R.color.graphColor_grid_light,
                R.color.graphColor_labels_light, R.color.graphColor_labels_bg_light,
                R.color.graphColor_day_light, R.color.graphColor_day_light,
                R.color.graphColor_nautical_light, R.color.graphColor_nautical_light,
                R.color.moonIcon_color_rising_light, R.color.moonIcon_color_rising_light,
                R.color.moonIcon_color_setting_light, R.color.moonIcon_color_setting_light
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.BLACK,
                Color.DKGRAY, Color.DKGRAY, Color.DKGRAY, Color.DKGRAY,
                Color.DKGRAY, Color.DKGRAY,
                Color.DKGRAY, Color.DKGRAY, Color.DKGRAY,
                Color.DKGRAY, Color.BLACK,
                Color.YELLOW, Color.YELLOW,
                Color.BLUE, Color.BLUE,
                Color.LTGRAY, Color.LTGRAY,
                Color.CYAN, Color.CYAN
        };
    }

    public LineGraphColorValues(ColorValues other) {
        super(other);
    }
    /*private LineGraphColorValues(Parcel in) {
        super(in);
    }*/
    public LineGraphColorValues() {
        super();
    }
    public LineGraphColorValues(Resources context) {
        this(context, true);
    }
    public LineGraphColorValues(Resources context, boolean darkTheme) {
        super(context, darkTheme);
    }

    /*public static final Creator<LineGraphColorValues> CREATOR = new Creator<LineGraphColorValues>()
    {
        public LineGraphColorValues createFromParcel(Parcel in) {
            return new LineGraphColorValues(in);
        }
        public LineGraphColorValues[] newArray(int size) {
            return new LineGraphColorValues[size];
        }
    };*/

    public static LineGraphColorValues getColorDefaults(Resources context, boolean darkTheme) {
        return new LineGraphColorValues(new LineGraphColorValues().getDefaultValues(context, darkTheme));
    }
}
