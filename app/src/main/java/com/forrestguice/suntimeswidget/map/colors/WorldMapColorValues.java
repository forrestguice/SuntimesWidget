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

package com.forrestguice.suntimeswidget.map.colors;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.colors.Color;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ResourceColorValues;
import com.forrestguice.util.Resources;

import java.io.Serializable;

/**
 * ColorValues
 */
public class WorldMapColorValues extends ResourceColorValues implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String TAG_WORLDMAP = "worldmap";

    public static final String COLOR_BACKGROUND = "color_background";
    public static final String COLOR_FOREGROUND = "color_foreground";

    public static final String COLOR_SUN_SHADOW = "color_sun_shadow";
    public static final String COLOR_MOON_LIGHT = "color_moon_light";

    public static final String COLOR_SUN_FILL = "color_sunFill";
    public static final String COLOR_SUN_STROKE = "color_sunStroke";

    public static final String COLOR_MOON_FILL = "color_moonFill";
    public static final String COLOR_MOON_STROKE = "color_moonStroke";

    public static final String COLOR_POINT_FILL = "color_pointFill";
    public static final String COLOR_POINT_STROKE = "color_pointStroke";

    public static final String COLOR_AXIS = "color_axis";
    public static final String COLOR_GRID_MAJOR = "color_grid_major";
    public static final String COLOR_GRID_MINOR = "color_grid_minor";

    public String[] getColorKeys() {
        return new String[] {
                COLOR_BACKGROUND, COLOR_FOREGROUND,
                COLOR_SUN_SHADOW, COLOR_SUN_FILL, COLOR_SUN_STROKE,
                COLOR_MOON_LIGHT, COLOR_MOON_FILL, COLOR_MOON_STROKE,
                COLOR_POINT_FILL, COLOR_POINT_STROKE,
                COLOR_AXIS, COLOR_GRID_MAJOR, COLOR_GRID_MINOR,
        };
    }
    public int[] getColorAttrs() {
        return new int[] {
                0, 0,
                0, R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
                0, R.attr.moonriseColor, R.attr.moonsetColor,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
                R.attr.graphColor_axis, R.attr.graphColor_grid, R.attr.graphColor_grid,
        };
    }
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.configLabel_themeColorMapBackground, R.string.configLabel_themeColorMapForeground,
                R.string.configLabel_themeColorMapSunShadow, R.string.configLabel_themeColorGraphSunFill, R.string.configLabel_themeColorGraphSunStroke,
                R.string.worldmap_dialog_option_moonlight, R.string.configLabel_themeColorGraphMoonFill, R.string.configLabel_themeColorGraphMoonStroke,
                R.string.configLabel_themeColorGraphPointFill, R.string.configLabel_themeColorGraphPointStroke,
                R.string.configLabel_themeColorGraphAxis, R.string.configLabel_themeColorGraphGridMajor, R.string.configLabel_themeColorGraphGridMinor,
        };
    }
    public int[] getColorRoles() {
        return new int[] {
                ROLE_BACKGROUND, ROLE_BACKGROUND,
                ROLE_BACKGROUND, ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_BACKGROUND, ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND,
                ROLE_FOREGROUND, ROLE_FOREGROUND, ROLE_FOREGROUND
        };
    }
    public int[] getColorsResDark() {
        return new int[] {
                R.color.map_background_dark, R.color.map_foreground_dark,
                R.color.map_sunshadow_dark, R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
                R.color.map_moonlight_dark, R.color.moonIcon_color_rising_dark, R.color.moonIcon_color_setting_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
                R.color.graphColor_axis_dark, R.color.graphColor_grid_dark,  R.color.graphColor_grid_dark,
        };
    }
    public int[] getColorsResLight() {
        return new int[] {
                R.color.map_background_light, R.color.map_foreground_light,
                R.color.map_sunshadow_light, R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
                R.color.map_moonlight_light,  R.color.moonIcon_color_rising_light, R.color.moonIcon_color_setting_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
                R.color.graphColor_axis_dark, R.color.graphColor_grid_light, R.color.graphColor_grid_light,
        };
    }
    public int[] getColorsFallback() {
        return new int[] {
                Color.BLUE, Color.TRANSPARENT,            // background, foreground
                Color.BLACK, Color.YELLOW, Color.BLACK,   // sunshadow, sunfill, sunstroke
                Color.LTGRAY, Color.WHITE, Color.BLACK,   // moonlight, moonfill, moonstroke
                Color.MAGENTA, Color.BLACK,               // pointfill, pointstroke
                Color.BLACK, Color.LTGRAY, Color.LTGRAY,  // axis, grid-major, grid-minor
        };
    }

    public WorldMapColorValues(ColorValues other) {
        super(other);
    }
    /*protected WorldMapColorValues(Parcel in) {
        super(in);
    }*/
    public WorldMapColorValues() {
        super();
    }

    public WorldMapColorValues(Resources context, boolean darkTheme) {
        super(context, darkTheme);
    }

    public WorldMapColorValues(String jsonString) {
        super(jsonString);
    }

    /*public static final Creator<WorldMapColorValues> CREATOR = new Creator<WorldMapColorValues>()
    {
        public WorldMapColorValues createFromParcel(Parcel in) {
            return new WorldMapColorValues(in);
        }
        public WorldMapColorValues[] newArray(int size) {
            return new WorldMapColorValues[size];
        }
    };*/

    public static WorldMapColorValues getColorDefaults(Resources context, boolean darkTheme) {
        return new WorldMapColorValues(new WorldMapColorValues().getDefaultValues(context, darkTheme));
    }

}
