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
import android.os.Parcel;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.ColorValues;

/**
 * ColorValues
 */
public abstract class GraphColorValues extends ColorValues
{
    public static final String COLOR_DAY = "color_day";
    public static final String COLOR_NIGHT = "color_night";
    public static final String COLOR_CIVIL = "color_civil";
    public static final String COLOR_NAUTICAL = "color_nautical";
    public static final String COLOR_ASTRONOMICAL = "color_astronomical";

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

    public static final String COLOR_SUNPATH_DAY = "color_sunpath_day";
    public static final String COLOR_SUNPATH_DAY_CLOSED = "color_sunpath_day_closed";
    public static final String COLOR_SUNPATH_NIGHT = "color_sunpath_night";
    public static final String COLOR_SUNPATH_NIGHT_CLOSED = "color_sunpath_night_closed";

    public static final String COLOR_MOONPATH_DAY = "color_moonpath_day";
    public static final String COLOR_MOONPATH_DAY_CLOSED = "color_moonpath_day_closed";
    public static final String COLOR_MOONPATH_NIGHT = "color_moonpath_night";
    public static final String COLOR_MOONPATH_NIGHT_CLOSED = "color_moonpath_night_closed";

    public abstract int[] getColorAttrs();
    public abstract int[] getColorLabelsRes();
    public abstract int[] getColorsResDark();
    public abstract int[] getColorsResLight();
    public abstract int[] getColorsFallback();

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

}
