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
import android.os.Parcel;

import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;

/**
 * ColorValuesCollection
 */
public class GraphColorValuesCollection<GraphColorValues> extends ColorValuesCollection<ColorValues>
{
    public static final String PREFS_GRAPH_COLORS = "prefs_graph_colors";

    public GraphColorValuesCollection() {
        super();
    }
    public GraphColorValuesCollection(Context context) {
        super(context);
    }
    protected GraphColorValuesCollection(Parcel in) {
        super(in);
    }

    @Override
    public String getSharedPrefsName() {
        return PREFS_GRAPH_COLORS;
    }

    @Override
    public ColorValues getDefaultColors(Context context) {
        return new com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues(context);
    }

    public static final Creator<GraphColorValuesCollection> CREATOR = new Creator<GraphColorValuesCollection>()
    {
        public GraphColorValuesCollection createFromParcel(Parcel in) {
            return new GraphColorValuesCollection<ColorValues>(in);
        }
        public GraphColorValuesCollection<ColorValues>[] newArray(int size) {
            return new GraphColorValuesCollection[size];
        }
    };

}
