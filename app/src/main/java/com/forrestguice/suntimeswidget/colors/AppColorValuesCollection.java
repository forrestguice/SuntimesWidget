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
import android.os.Parcel;

/**
 * ColorValuesCollection
 */
public class AppColorValuesCollection<GraphColorValues> extends ColorValuesCollection<ColorValues>
{
    public static final String PREFS_GRAPH_COLORS = "prefs_graph_colors";

    public AppColorValuesCollection() {
        super();
    }
    public AppColorValuesCollection(Context context) {
        super(context);
    }
    protected AppColorValuesCollection(Parcel in) {
        super(in);
    }

    @Override
    public String getSharedPrefsName() {
        return PREFS_GRAPH_COLORS;
    }

    @Override
    public ColorValues getDefaultColors(Context context) {
        return new AppColorValues(context,  true);
    }

    public static final Creator<AppColorValuesCollection> CREATOR = new Creator<AppColorValuesCollection>()
    {
        public AppColorValuesCollection createFromParcel(Parcel in) {
            return new AppColorValuesCollection<ColorValues>(in);
        }
        public AppColorValuesCollection<ColorValues>[] newArray(int size) {
            return new AppColorValuesCollection[size];
        }
    };

}