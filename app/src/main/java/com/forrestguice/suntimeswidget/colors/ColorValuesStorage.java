// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020-2024 Forrest Guice
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

import android.content.SharedPreferences;

import com.forrestguice.colors.ColorValues;

import java.util.HashMap;

public abstract class ColorValuesStorage
{
    public static void loadColorValues(ColorValues colors, SharedPreferences prefs, String prefix)
    {
        colors.setID(loadColorValuesID(prefs, prefix));
        colors.setLabel(loadColorValuesLabel(prefs, prefix));
        for (String key : colors.getColorKeys()) {
            colors.setColor(key, prefs.getInt(prefix + key, colors.getFallbackColor()));
        }
    }
    public static String loadColorValuesID(SharedPreferences prefs, String prefix) {
        return prefs.getString(prefix + ColorValues.KEY_ID, null);
    }
    public static String loadColorValuesLabel(SharedPreferences prefs, String prefix) {
        return prefs.getString(prefix + ColorValues.KEY_LABEL, null);
    }
    public static int loadColorValuesColor(SharedPreferences prefs, String prefix, String key, int defaultColor) {
        return prefs.getInt(prefix + key, defaultColor);
    }
    public static int[] loadColorValuesColors(SharedPreferences prefs, String prefix, int defaultColor, String... keys)
    {
        int[] retValue = new int[keys != null ? keys.length : 0];
        if (keys != null) {
            for (int i=0; i<retValue.length; i++) {
                retValue[i] = prefs.getInt(prefix + keys[i], defaultColor);
            }
        }
        return retValue;
    }

    public static void putColorsInto(ColorValues colors, SharedPreferences prefs, String prefix)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefix + ColorValues.KEY_ID, colors.getID());
        editor.putString(prefix + ColorValues.KEY_LABEL, colors.getLabel());
        for (String key : colors.getColorKeys()) {
            editor.putInt(prefix + key, (Integer) colors.getValues().get(key));
        }
        editor.apply();
    }
    public static void putColorsInto(ColorValues colors, HashMap<String, Object> other) {
        other.putAll(colors.getValues());
    }

    public static void removeColorsFrom(ColorValues colors, SharedPreferences prefs, String prefix)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(prefix + ColorValues.KEY_ID);
        editor.remove(prefix + ColorValues.KEY_LABEL);
        for (String key : colors.getColorKeys()) {
            editor.remove(prefix + key);
        }
        editor.apply();
    }

}