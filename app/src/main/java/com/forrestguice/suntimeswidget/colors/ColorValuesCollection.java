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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public abstract class ColorValuesCollection<T extends ColorValues> implements Parcelable
{
    public static final String KEY_COLLECTION = "colorValuesCollection";
    public static final String KEY_SELECTED = "selectedColors";

    public ColorValuesCollection() {}
    public ColorValuesCollection(Context context) {
        loadCollection(getCollectionSharedPreferences(context));
    }
    protected ColorValuesCollection(Parcel in)
    {
        collection = new TreeSet<>();
        List<String> items = new ArrayList<>();
        in.readStringList(items);
        collection.addAll(items);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        List<String> items = new ArrayList<>(collection);
        dest.writeStringList(items);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    protected Set<String> collection = new TreeSet<String>();
    public String[] getCollection() {
        return collection.toArray(new String[0]);
    }
    protected void loadCollection(SharedPreferences prefs)
    {
        collection.clear();
        collection.addAll(Arrays.asList(getDefaultColorIDs()));
        Set<String> ids = prefs.getStringSet(KEY_COLLECTION, null);
        collection.addAll(ids != null ? ids : new TreeSet<String>());
    }
    protected void saveCollection(SharedPreferences prefs)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_COLLECTION, collection);
        editor.apply();
    }

    protected ColorValues loadColors(Context context, SharedPreferences prefs, String colorsID)
    {
        String prefix = getCollectionSharedPrefsPrefix() + colorsID + "_";
        ColorValues values = getDefaultColors(context, colorsID);
        if (!isDefaultColorID(colorsID)) {
            values.loadColorValues(prefs, prefix);
        }
        return values;
    }
    protected void saveColors(SharedPreferences prefs, String colorsID, ColorValues values)
    {
        String prefix = getCollectionSharedPrefsPrefix() + colorsID + "_";
        values.putColorsInto(prefs, prefix);
    }
    protected void removeColors(Context context, SharedPreferences prefs, String colorsID)
    {
        String prefix = getCollectionSharedPrefsPrefix() + colorsID + "_";
        ColorValues values = getColors(context, prefix);
        if (values != null) {
            values.removeColorsFrom(prefs, prefix);
        }
    }

    /**
     * Override to define additional default ids; also override getDefaultColors to define corresponding
     * colors for those ids.
     * @return array of default ids
     */
    protected String[] getDefaultColorIDs() {
        return new String[0];
    }
    public String getDefaultLabel(Context context, @NonNull String colorsID) {
        return colorsID;
    }
    protected boolean isDefaultColorID(String colorsID)
    {
        if (colorsID == null) {
            return true;
        }
        for (String id : getDefaultColorIDs()) {
            if (id.equals(colorsID)) {
                return true;
            }
        }
        return false;
    }


    public abstract T getDefaultColors(Context context);
    protected T getDefaultColors(Context context, @Nullable String colorsID) {
        return getDefaultColors(context);
    }

    protected HashMap<String, ColorValues> colorValues = new HashMap<>();
    @Nullable
    public ColorValues getColors( Context context, @Nullable String colorsID )
    {
        if (colorsID == null) {
            return null;
        }
        if (!colorValues.containsKey(colorsID))
        {
            ColorValues values = loadColors(context, getCollectionSharedPreferences(context), colorsID);
            if (values != null) {
                colorValues.put(colorsID, values);
            }
        }
        return (colorValues.containsKey(colorsID) ? colorValues.get(colorsID) : null);
    }
    public void setColors(Context context, ColorValues values) {
        String colorsID = values.getID();
        setColors(context, colorsID != null ? colorsID : "", values);
    }
    public void setColors(Context context, @NonNull String colorsID, ColorValues values)
    {
        ColorValues v = getDefaultColors(context, colorsID);
        v.loadColorValues(values);    // copy defined colors into a new instance
        colorValues.put(colorsID, v);

        saveColors(getCollectionSharedPreferences(context), colorsID, v);
        if (collection.add(colorsID)) {
            saveCollection(getCollectionSharedPreferences(context));
        }
    }

    public void removeColors(Context context, String colorsID) {
        colorValues.remove(colorsID);
        removeColors(context, getCollectionSharedPreferences(context), colorsID);
        if (collection.remove(colorsID)) {
            saveCollection(getCollectionSharedPreferences(context));
        }
    }

    public boolean hasColors(String colorsID) {
        return collection.contains(colorsID);
    }

    public void clearCache() {
        colorValues.clear();
    }

    @Nullable
    public ColorValues getSelectedColors(Context context) {
        return getSelectedColors(context, 0, null);
    }
    @Nullable
    public ColorValues getSelectedColors(Context context, int appWidgetID) {
        return getSelectedColors(context, appWidgetID, null);
    }
    @Nullable
    public ColorValues getSelectedColors(Context context, int appWidgetID, @Nullable String tag)
    {
        String selected = getSelectedColorsID(context, appWidgetID, tag);
        if (selected != null) {
            return getColors(context, selected);
        } else return null; //return getDefaultColors(context);
    }

    @Nullable
    public String getSelectedColorsID(Context context) {
        return getSelectedColorsID(context, 0, null);
    }
    @Nullable
    public String getSelectedColorsID(Context context, int appWidgetID) {
        return getSelectedColorsID(context, appWidgetID, null);
    }
    @Nullable
    public String getSelectedColorsID(Context context, int appWidgetID, @Nullable String tag) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(getSelectedColorsKey(appWidgetID, tag), null);
    }

    public void setSelectedColorsID(Context context, @Nullable String colorsID) {
        setSelectedColorsID(context, colorsID, 0, null);
    }
    public void setSelectedColorsID(Context context, @Nullable String colorsID, int appWidgetID) {
        setSelectedColorsID(context, colorsID, appWidgetID, null);
    }
    public void setSelectedColorsID(Context context, @Nullable String colorsID, int appWidgetID, @Nullable String tag)
    {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(getSelectedColorsKey(appWidgetID, tag), colorsID);
        editor.apply();
    }

    public void clearSelectedColorsID(Context context) {
        clearSelectedColorsID(context, 0, null);
    }
    public void clearSelectedColorsID(Context context, int appWidgetID) {
        clearSelectedColorsID(context, appWidgetID, null);
    }
    public void clearSelectedColorsID(Context context, int appWidgetID, @Nullable String tag)
    {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(getSelectedColorsKey(appWidgetID, tag));
        editor.apply();
    }

    public String getSelectedColorsKey(int appWidgetID, @Nullable String tag) {
        return getSharedPrefsPrefix() + appWidgetID + "_" + KEY_SELECTED + ((tag != null) ? ("_" + tag) : "");
    }

    @Nullable
    public String getSelectedColorsLabel(Context context, int appWidgetID, @Nullable String tag)
    {
        String colorsID = getSelectedColorsID(context, appWidgetID, tag);
        return getColorsLabel(context, colorsID);
    }
    @Nullable
    public String getColorsLabel(Context context, @Nullable String colorsID)
    {
        if (colorsID != null)
        {
            if (isDefaultColorID(colorsID)) {
                return getDefaultLabel(context, colorsID);

            } else {
                SharedPreferences prefs = getCollectionSharedPreferences(context);
                String prefix = getCollectionSharedPrefsPrefix() + colorsID + "_";
                return ColorValues.loadColorValuesLabel(prefs, prefix);
            }
        } else return null;
    }
    @Nullable
    public int[] getColors(Context context, @Nullable String colorsID, int defaultValue, String... keys)
    {
        if (colorsID != null)
        {
            if (isDefaultColorID(colorsID))
            {
                if (keys != null)
                {
                    ColorValues values = getDefaultColors(context, colorsID);
                    int[] retValue = new int[keys.length];
                    for (int i=0; i<keys.length; i++) {
                        retValue[i] = values.getColor(keys[i]);
                    }
                    return retValue;

                } else {
                    return new int[] { defaultValue };
                }

            } else {
                SharedPreferences prefs = getCollectionSharedPreferences(context);
                String prefix = getCollectionSharedPrefsPrefix() + colorsID + "_";
                return ColorValues.loadColorValuesColors(prefs, prefix, defaultValue, keys);
            }

        } else return new int[] { defaultValue };
    }

    @Nullable
    public abstract String getSharedPrefsName();
    public SharedPreferences getSharedPreferences(Context context)
    {
        String prefsName = getSharedPrefsName();
        return (prefsName != null)
                ? context.getSharedPreferences(prefsName, 0)
                : PreferenceManager.getDefaultSharedPreferences(context);
    }
    @NonNull
    protected String getSharedPrefsPrefix() {
        return "color_";
    }

    @Nullable
    public abstract String getCollectionSharedPrefsName();
    public SharedPreferences getCollectionSharedPreferences(Context context)
    {
        String prefsName = getCollectionSharedPrefsName();
        return (prefsName != null)
                ? context.getSharedPreferences(prefsName, 0)
                : PreferenceManager.getDefaultSharedPreferences(context);
    }
    @NonNull
    protected String getCollectionSharedPrefsPrefix() {
        return "colors_";
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("---\n[");
        String[] collection = getCollection();
        if (collection.length > 0) {
            for (String colorsId : getCollection()) {
                result.append(colorsId);
                result.append(", ");
            }
            result.replace(result.length()-2, result.length(), "]\n");
        } else result.append("]\n");
        return result.toString();
    }

    /*public String toJSON(Context context)
    {
        StringBuilder result = new StringBuilder();
        result.append("[");

        int c = 0;
        for (String colorsID : collection)
        {
            if (c > 0) {
                result.append(",\n");
            }

            ColorValues colors = getColors(context, colorsID);
            if (colors != null) {
                result.append(colors.toJSON());
                c++;
            }
        }

        result.append("]");
        return result.toString();
    }*/

}