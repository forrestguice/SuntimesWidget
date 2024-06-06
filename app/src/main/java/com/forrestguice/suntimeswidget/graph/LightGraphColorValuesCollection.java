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

import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;

/**
 * ColorValuesCollection
 */
public class LightGraphColorValuesCollection<LightGraphColorValues> extends ColorValuesCollection
{
    public static final String PREFS_LIGHTGRAPH_COLORS = "prefs_lightgraph_colors";

    public LightGraphColorValuesCollection() {
        super();
    }
    public LightGraphColorValuesCollection(Context context) {
        super(context);
    }

    @Override
    public String getSharedPrefsName() {
        return PREFS_LIGHTGRAPH_COLORS;
    }

    @Override
    public ColorValues getDefaultColors(Context context) {
        return new com.forrestguice.suntimeswidget.graph.LightGraphColorValues(context);
    }
}
