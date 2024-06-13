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

import android.content.Context;
import android.os.Parcel;

import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;

/**
 * ColorValuesCollection
 */
public class WorldMapColorValuesCollection<WorldMapColorValues> extends ColorValuesCollection<ColorValues>
{
    public static final String PREFS_WORLDMAP_COLORS = "prefs_worldmap_colors";

    public WorldMapColorValuesCollection() {
        super();
    }
    public WorldMapColorValuesCollection(Context context) {
        super(context);
    }
    protected WorldMapColorValuesCollection(Parcel in) {
        super(in);
    }

    @Override
    public String getSharedPrefsName() {
        return PREFS_WORLDMAP_COLORS;
    }

    @Override
    public ColorValues getDefaultColors(Context context) {
        return new com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues(context,  true);
    }

    public static final Creator<WorldMapColorValuesCollection> CREATOR = new Creator<WorldMapColorValuesCollection>()
    {
        public WorldMapColorValuesCollection createFromParcel(Parcel in) {
            return new WorldMapColorValuesCollection<ColorValues>(in);
        }
        public WorldMapColorValuesCollection<ColorValues>[] newArray(int size) {
            return new WorldMapColorValuesCollection[size];
        }
    };

}
