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
import android.os.Parcel;
import android.os.Parcelable;

import com.forrestguice.suntimeswidget.colors.ColorValues;

/**
 * ColorValues
 */
public class LineGraphColorValues extends GraphColorValues implements Parcelable
{
    @Override
    public String[] getColorKeys() {
        return super.getColorKeys();
    }

    @Override
    public int[] getColorAttrs() {
        return super.getColorAttrs();
    }
    @Override
    public int[] getColorLabelsRes() {
        return super.getColorLabelsRes();
    }
    @Override
    public int[] getColorsResDark() {
        return super.getColorsResDark();
    }
    @Override
    public int[] getColorsResLight() {
        return super.getColorsResLight();
    }
    @Override
    public int[] getColorsFallback() {
        return super.getColorsFallback();
    }

    public LineGraphColorValues(ColorValues other) {
        super(other);
    }
    public LineGraphColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    private LineGraphColorValues(Parcel in) {
        super(in);
    }
    public LineGraphColorValues() {
        super();
    }
    public LineGraphColorValues(Context context) {
        this(context, true);
    }
    public LineGraphColorValues(Context context, boolean darkTheme) {
        super(context, darkTheme);
    }
    public LineGraphColorValues(String jsonString) {
        super(jsonString);
    }

    public static final Creator<LineGraphColorValues> CREATOR = new Creator<LineGraphColorValues>()
    {
        public LineGraphColorValues createFromParcel(Parcel in) {
            return new LineGraphColorValues(in);
        }
        public LineGraphColorValues[] newArray(int size) {
            return new LineGraphColorValues[size];
        }
    };

    public static LineGraphColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new LineGraphColorValues(new LineGraphColorValues().getDefaultValues(context, darkTheme));
    }
}
