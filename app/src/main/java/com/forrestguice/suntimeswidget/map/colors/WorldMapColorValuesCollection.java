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
import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.util.prefs.PrefTypeInfo;
import com.forrestguice.util.android.AndroidResources;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * ColorValuesCollection
 */
public class WorldMapColorValuesCollection<T> extends ColorValuesCollection<ColorValues> implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String PREFS_WORLDMAP_COLORS = "prefs_worldmap_colors";

    private static final String PREFS_PREFIX = "map_";
    private static final String PREFS_COLLECTION_PREFIX = "mapcolors_";

    public WorldMapColorValuesCollection() {
        super();
    }
    public WorldMapColorValuesCollection(Context context) {
        super(context);
    }
    /*protected WorldMapColorValuesCollection(Parcel in) {
        super(in);
    }*/

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
        return null;
    }

    @Nullable
    @Override
    public String getCollectionSharedPrefsName() {
        return PREFS_WORLDMAP_COLORS;
    }

    @Override
    public ColorValues getDefaultColors(Context context) {
        return new WorldMapColorValues(AndroidResources.wrap(context),  true);
    }

    /*public static final Creator<WorldMapColorValuesCollection> CREATOR = new Creator<WorldMapColorValuesCollection>()
    {
        public WorldMapColorValuesCollection createFromParcel(Parcel in) {
            return new WorldMapColorValuesCollection<ColorValues>(in);
        }
        public WorldMapColorValuesCollection<ColorValues>[] newArray(int size) {
            return new WorldMapColorValuesCollection[size];
        }
    };*/

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[] {
            PREFS_PREFIX + "0" + "_" + KEY_SELECTED, PREFS_PREFIX + "1" + "_" + KEY_SELECTED,
            PREFS_PREFIX + "0" + "_" + KEY_SELECTED + "_" + WorldMapColorValues.TAG_WORLDMAP,
            PREFS_PREFIX + "1" + "_" + KEY_SELECTED + "_" + WorldMapColorValues.TAG_WORLDMAP,
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
