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

import android.content.ContentValues;
import android.content.SharedPreferences;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class ColorValues implements Serializable
{
    private static final long serialVersionUID = 1L;

    public abstract String[] getColorKeys();
    public int getFallbackColor() { return Color.WHITE; }

    public ColorValues() {}
    public ColorValues(ColorValues other) {
        loadColorValues(other);
    }
    public ColorValues(ContentValues values) {
        loadColorValues(values);
    }
    //protected ColorValues(Parcel in) {
    //    loadColorValues(in);
    //}
    public ColorValues(SharedPreferences prefs, String prefix) {
        loadColorValues(prefs, prefix);
    }
    public ColorValues(String jsonString) {
        loadColorValues(jsonString);
    }

    /*public void loadColorValues(@NonNull Parcel in)
    {
        setID(in.readString());
        setLabel(in.readString());
        for (String key : getColorKeys())
        {
            setColor(key, in.readInt());
            setLabel(key, in.readString());
        }
    }
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(getID());
        dest.writeString(getLabel());
        for (String key : getColorKeys())
        {
            dest.writeInt(values.getAsInteger(key));
            dest.writeString(values.getAsString(key + SUFFIX_LABEL));
        }
    }*/

    public void loadColorValues(@NonNull ColorValues other)
    {
        setID(other.getID());
        setLabel(other.getLabel());
        for (String key : other.getColorKeys())
        {
            setColor(key, other.getColor(key));
            if (other.hasLabel(key)) {
                setLabel(key, other.getLabel(key));
            }
        }
    }

    public void loadColorValues(@NonNull ContentValues values)
    {
        setID(values.getAsString(KEY_ID));
        setLabel(values.getAsString(KEY_LABEL));
        for (String key : getColorKeys())
        {
            setColor(key, values.getAsInteger(key));
            if (values.containsKey(key + SUFFIX_LABEL)) {
                setLabel(key, values.getAsString(key + SUFFIX_LABEL));
            }
        }
    }

    public void loadColorValues(SharedPreferences prefs, String prefix)
    {
        setID(loadColorValuesID(prefs, prefix));
        setLabel(loadColorValuesLabel(prefs, prefix));
        for (String key : getColorKeys()) {
            setColor(key, prefs.getInt(prefix + key, getFallbackColor()));
        }
    }
    public static String loadColorValuesID(SharedPreferences prefs, String prefix) {
        return prefs.getString(prefix + KEY_ID, null);
    }
    public static String loadColorValuesLabel(SharedPreferences prefs, String prefix) {
        return prefs.getString(prefix + KEY_LABEL, null);
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

    public boolean loadColorValues(String jsonString)
    {
        try {
            JSONObject json = new JSONObject(jsonString);
            setID(json.getString(KEY_ID));
            setLabel(json.getString(KEY_LABEL));
            for (String key : getColorKeys())
            {
                setColor(key, json.has(key) ? Color.parseColor(json.getString(key).trim()) : getFallbackColor());
                if (json.has(key + SUFFIX_LABEL)) {
                    setLabel(key, json.getString(key + SUFFIX_LABEL).trim());
                }
            }
            return json.has(KEY_ID);

        } catch (JSONException e) {
            Log.e("ColorValues", "fromJSON: " + e);
            return false;
        }
    }

    public void putColorsInto(SharedPreferences prefs, String prefix)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefix + KEY_ID, getID());
        editor.putString(prefix + KEY_LABEL, getLabel());
        for (String key : getColorKeys()) {
            editor.putInt(prefix + key, values.getAsInteger(key));
        }
        editor.apply();
    }
    public void putColorsInto(ContentValues other) {
        other.putAll(values);
    }

    public void removeColorsFrom(SharedPreferences prefs, String prefix)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(prefix + KEY_ID);
        editor.remove(prefix + KEY_LABEL);
        for (String key : getColorKeys()) {
            editor.remove(prefix + key);
        }
        editor.apply();
    }

    protected ContentValues values = new ContentValues();
    public ContentValues getContentValues() {
        return new ContentValues(values);
    }

    public static final String KEY_ID = "colorValuesID";
    public void setID( String colorsID ) {
        values.put(KEY_ID, colorsID);
    }
    public String getID() {
        return values.getAsString(KEY_ID);
    }

    public static final String KEY_LABEL = "colorValuesLabel";
    public void setLabel( String colorsLabel ) {
        values.put(KEY_LABEL, colorsLabel);
    }
    public String getLabel()
    {
        String label = values.getAsString(KEY_LABEL);
        return (label != null) ? label : getID();
    }

    public static final String SUFFIX_LABEL = "_LABEL";
    public void setLabel(String key, String label) {
        values.put(key + SUFFIX_LABEL, label);
    }
    public String getLabel(String key) {
        String label = values.getAsString(key + SUFFIX_LABEL);
        return (label != null ? label : key);
    }
    public boolean hasLabel(String key) {
        return values.containsKey(key + SUFFIX_LABEL);
    }

    public static final int ROLE_UNKNOWN = 0;
    public static final int ROLE_BACKGROUND = 100;
    public static final int ROLE_BACKGROUND_PRIMARY = 125;
    public static final int ROLE_BACKGROUND_INVERSE = 150;
    public static final int ROLE_FOREGROUND = 200;
    public static final int ROLE_TEXT = 300;
    public static final int ROLE_TEXT_INVERSE = 325;
    public static final int ROLE_TEXT_PRIMARY = 350;
    public static final int ROLE_TEXT_PRIMARY_INVERSE = 375;
    public static final int ROLE_ACCENT = 400;
    public static final int ROLE_ACTION = 500;

    public static final String SUFFIX_ROLE = "_ROLE";
    public void setRole(String key, int role) {
        values.put(key + SUFFIX_ROLE, role);
    }
    public int getRole(String key) {
        Integer role = values.getAsInteger(key + SUFFIX_ROLE);
        return (role != null ? role : ROLE_UNKNOWN);
    }
    public boolean hasRole(String key) {
        return values.containsKey(key + SUFFIX_ROLE);
    }
    @Nullable
    public String findColorWithRole(int role)
    {
        for (String key : values.keySet()) {
            if (getRole(key) == role) {
                return key;
            }
        }
        return null;
    }

    public void setColor(String key, int color) {
        values.put(key, color);
    }

    public int getColor(String key)
    {
        if (values.containsKey(key)) {
            return values.getAsInteger(key);
        } else return getFallbackColor();
    }

    public ArrayList<Integer> getColors()
    {
        ArrayList<Integer> list = new ArrayList<>();
        for (String key : getColorKeys()) {
            list.add(getColor(key));
        }
        return list;
    }

    public int colorKeyIndex(@NonNull String key)
    {
        String[] keys = getColorKeys();
        if (keys != null) {
            for (int i=0; i < keys.length; i++) {
                if (key.equals(keys[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    //@Override
    //public int describeContents() {
    //    return 0;
    //}

    /**
     * @return json
     */
    @Override
    public String toString() {
        return toJSON();
    }

    public String toJSON() {
        return toJSON(false);
    }

    public String toJSON(boolean withLabels)
    {
        JSONObject result = new JSONObject();
        try {
            result.put(KEY_ID, getID());
            result.put(KEY_LABEL, getLabel());
            for (String key : getColorKeys())
            {
                result.put(key, "#" + Integer.toHexString(getColor(key)));
                if (withLabels && hasLabel(key)) {
                    result.put(key + SUFFIX_LABEL, getLabel(key));
                }
            }
        } catch (JSONException e) {
            Log.e("ColorValues", "toJSON: " + e);
        }
        return result.toString();
    }

}