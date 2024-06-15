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
import android.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.R;

/**
 * ColorValuesCollection
 */
public class AppColorValuesCollection<GraphColorValues> extends ColorValuesCollection<ColorValues>
{
    public static final String PREFS_APP_COLORS = "prefs_graph_colors";   // TODO: change

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
        return PREFS_APP_COLORS;
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

    @Nullable
    public static AppColorValues initSelectedColors(@Nullable Context context)
    {
        if (context != null) {
            AppColorValuesCollection<AppColorValues> colors = new AppColorValuesCollection<>();
            boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
            return (AppColorValues) colors.getSelectedColors(context, (isNightMode ? 1 : 0), AppColorValues.TAG_APPCOLORS);

        } else {
            return null;
        }
    }

}
