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

package com.forrestguice.suntimeswidget.colors;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;

/**
 * ColorValues
 */
public abstract class ResourceColorValues extends ColorValues
{
    private static final long serialVersionUID = 1L;

    public abstract String[] getColorKeys();
    public abstract int[] getColorAttrs();
    public abstract int[] getColorLabelsRes();
    public abstract int[] getColorRoles();
    public abstract int[] getColorsResDark();
    public abstract int[] getColorsResLight();
    public abstract int[] getColorsFallback();

    public ResourceColorValues(ColorValues other) {
        super(other);
    }
    public ResourceColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    /*protected ResourceColorValues(Parcel in) {
        super(in);
    }*/
    public ResourceColorValues()
    {
        super();
        if (BuildConfig.DEBUG && (getColorKeys().length != getColorsFallback().length)) {
            throw new AssertionError("COLORS and COLORS_FALLBACK have different lengths! These arrays should be one-to-one.");
        }
        if (BuildConfig.DEBUG && (getColorRoles().length != getColorKeys().length)) {
            throw new AssertionError("COLOR_ROLES and COLOR_KEYS have different lengths! These arrays should be one-to-one.");
        }
        String[] colorKeys = getColorKeys();
        int[] colorRoles = getColorRoles();
        int[] fallbackColors = getColorsFallback();
        for (int i=0; i<colorKeys.length; i++)
        {
            setColor(colorKeys[i], fallbackColors[i]);
            setLabel(colorKeys[i], colorKeys[i]);
            setRole(colorKeys[i], colorRoles[i]);
        }
    }

    public ResourceColorValues(Context context, boolean darkTheme)
    {
        super();
        if (BuildConfig.DEBUG && (getColorKeys().length != getColorAttrs().length)) {
            throw new AssertionError("COLORS and COLORS_ATTR have different lengths! These arrays should be one-to-one." + getColorKeys().length + " != " + getColorAttrs().length);
        }
        if (BuildConfig.DEBUG && (getColorRoles().length != getColorKeys().length)) {
            throw new AssertionError("COLOR_ROLES and COLOR_KEYS have different lengths! These arrays should be one-to-one.");
        }
        String[] colorKeys = getColorKeys();
        int[] colorRoles = getColorRoles();
        int[] labelsResID = getColorLabelsRes();
        int[] defaultResID = darkTheme ? getColorsResDark() : getColorsResLight();
        TypedArray a = context.obtainStyledAttributes(getColorAttrs());
        for (int i=0; i<colorKeys.length; i++) {
            setColor(colorKeys[i], ContextCompat.getColor(context, a.getResourceId(i, defaultResID[i])));
            setLabel(colorKeys[i], (labelsResID[i] != 0) ? context.getString(labelsResID[i]) : colorKeys[i]);
            setRole(colorKeys[i], colorRoles[i]);
        }
        a.recycle();
    }

    public ResourceColorValues(String jsonString) {
        super(jsonString);
    }

    public ColorValues getDefaultValues(Context context, boolean darkTheme)
    {
        ColorValues values = new ColorValues()
        {
            @Override
            public String[] getColorKeys() {
                return ResourceColorValues.this.getColorKeys();
            }
        };

        if (BuildConfig.DEBUG && (getColorRoles().length != getColorKeys().length)) {
            throw new AssertionError("COLOR_ROLES and COLOR_KEYS have different lengths! These arrays should be one-to-one.");
        }

        String[] colorKeys = getColorKeys();
        int[] colorRoles = getColorRoles();
        int[] labelsResID = getColorLabelsRes();
        int[] defaultResID = darkTheme ? getColorsResDark() : getColorsResLight();
        for (int i=0; i<colorKeys.length; i++)
        {
            values.setColor(colorKeys[i], ContextCompat.getColor(context, defaultResID[i]));
            values.setLabel(colorKeys[i], (labelsResID[i] != 0) ? context.getString(labelsResID[i]) : colorKeys[i]);
            values.setRole(colorKeys[i], colorRoles[i]);
        }
        values.setID(darkTheme ? context.getString(R.string.widgetThemes_dark) : context.getString(R.string.widgetThemes_light));
        return values;
    }

}
