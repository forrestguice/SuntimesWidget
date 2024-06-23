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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.PrefTypeInfo;

import java.util.Map;
import java.util.TreeMap;

/**
 * ColorValuesCollection
 */
public class AppColorValuesCollection<T> extends ColorValuesCollection<ColorValues>
{
    public static final String PREFS_APP_COLORS = "prefs_app_colors";

    private static final String PREFS_PREFIX = "app_";
    private static final String PREFS_COLLECTION_PREFIX = "appcolors_";

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
    @NonNull
    protected String getSharedPrefsPrefix() {
        return PREFS_PREFIX;
    }

    @NonNull
    protected String getCollectionSharedPrefsPrefix() {
        return PREFS_COLLECTION_PREFIX;
    }

    @Nullable
    @Override
    public String getSharedPrefsName() {
        return null;    // use default
    }
    @Nullable
    @Override
    public String getCollectionSharedPrefsName() {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[] {
            PREFS_PREFIX + "0" + "_" + KEY_SELECTED, PREFS_PREFIX + "1" + "_" + KEY_SELECTED,
            PREFS_PREFIX + "0" + "_" + KEY_SELECTED + "_" + AppColorValues.TAG_APPCOLORS,
            PREFS_PREFIX + "1" + "_" + KEY_SELECTED + "_" + AppColorValues.TAG_APPCOLORS,
    };

    private static Map<String,Class> types = null;
    public static Map<String,Class> getPrefTypes()
    {
        if (types == null)
        {
            types = new TreeMap<>();
            for (String key : ALL_KEYS) {
                if (!types.containsKey(key)) {
                    types.put(key, String.class);
                }
            }
        }
        return types;
    }

    public static PrefTypeInfo getPrefTypeInfo()
    {
        return new PrefTypeInfo()
        {
            public String[] allKeys() {
                return ALL_KEYS;
            }
            public String[] intKeys() {
                return new String[0];
            }
            public String[] longKeys() {
                return new String[0];
            }
            public String[] floatKeys() {
                return new String[0];
            }
            public String[] boolKeys() {
                return new String[0];
            }
        };
    }

}
