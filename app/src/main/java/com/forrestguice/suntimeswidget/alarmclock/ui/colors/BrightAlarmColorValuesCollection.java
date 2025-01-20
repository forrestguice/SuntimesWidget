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

package com.forrestguice.suntimeswidget.alarmclock.ui.colors;

import android.content.Context;
import android.os.Parcel;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.settings.PrefTypeInfo;

import java.util.Map;
import java.util.TreeMap;

/**
 * ColorValuesCollection
 */
public class BrightAlarmColorValuesCollection<T> extends ColorValuesCollection<ColorValues>
{
    public static final String PREFS_BRIGHTALARM_COLORS = "prefs_brightalarm";

    private static final String PREFS_PREFIX = "app_";
    private static final String PREFS_COLLECTION_PREFIX = "brightalarm_";

    public BrightAlarmColorValuesCollection() {
        super();
    }
    public BrightAlarmColorValuesCollection(Context context) {
        super(context);
    }
    protected BrightAlarmColorValuesCollection(Parcel in) {
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
        return null;
    }

    @Nullable
    @Override
    public String getCollectionSharedPrefsName() {
        return PREFS_BRIGHTALARM_COLORS;
    }

    @Override
    public ColorValues getDefaultColors(Context context) {
        return new BrightAlarmColorValues(context,  true);
    }

    public static final Creator<BrightAlarmColorValuesCollection> CREATOR = new Creator<BrightAlarmColorValuesCollection>()
    {
        public BrightAlarmColorValuesCollection createFromParcel(Parcel in) {
            return new BrightAlarmColorValuesCollection<ColorValues>(in);
        }
        public BrightAlarmColorValuesCollection<ColorValues>[] newArray(int size) {
            return new BrightAlarmColorValuesCollection[size];
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String[] ALL_KEYS = new String[] {
            PREFS_PREFIX + "0" + "_" + KEY_SELECTED, PREFS_PREFIX + "1" + "_" + KEY_SELECTED,
            PREFS_PREFIX + "0" + "_" + KEY_SELECTED + "_" + AlarmColorValues.TAG_ALARMCOLORS,
            PREFS_PREFIX + "1" + "_" + KEY_SELECTED + "_" + AlarmColorValues.TAG_ALARMCOLORS,
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String DEFAULT_ID_SUNRISE = "sunrise";
    public static final String DEFAULT_ID_FOLIAGE = "foliage";
    public static final String DEFAULT_ID_BLUESKY = "bluesky";

    @Override
    protected String[] getDefaultColorIDs() {
        return new String[] { DEFAULT_ID_SUNRISE, DEFAULT_ID_FOLIAGE, DEFAULT_ID_BLUESKY};
    }
    @Override
    protected ColorValues getDefaultColors(Context context, @Nullable String colorsID)
    {
        if (colorsID == null) {
            return getDefaultColors(context);
        }

        ColorValues v;
        switch (colorsID)
        {
            case DEFAULT_ID_SUNRISE:
                v = new BrightAlarmColorValues_Sunrise(context, true);
                break;

            case DEFAULT_ID_FOLIAGE:
                v = new BrightAlarmColorValues_Foliage(context, true);
                break;

            case DEFAULT_ID_BLUESKY:
                v = new BrightAlarmColorValues_BlueSky(context, true);
                break;

            default:
                v = getDefaultColors(context);
                break;
        }
        v.setID(colorsID);
        v.setLabel(getDefaultLabel(context, colorsID));
        return v;
    }
    public String getDefaultLabel(Context context, @Nullable String colorsID)
    {
        if (colorsID == null) {
            return context.getString(R.string.brightMode_colors_label_default);
        }
        switch(colorsID)
        {
            case DEFAULT_ID_SUNRISE:
                return context.getString(R.string.brightMode_colors_label_sunrise);

            case DEFAULT_ID_FOLIAGE:
                return context.getString(R.string.brightMode_colors_label_foliage);

            case DEFAULT_ID_BLUESKY:
                return context.getString(R.string.brightMode_colors_label_bluesky);

            default:
                return colorsID;
        }
    }

}
