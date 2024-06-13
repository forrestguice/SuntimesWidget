// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020 Forrest Guice
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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public abstract class ColorValuesCollection<T extends ColorValues> implements Parcelable
{
    public static final String KEY_COLLECTION = "colorValuesCollection";
    public static final String KEY_SELECTED = "selectedValues";

    public ColorValuesCollection() {}
    public ColorValuesCollection(Context context) {
        loadCollection(getSharedPreferences(context));
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
    protected void loadCollection(SharedPreferences prefs) {
        collection.clear();
        Set<String> ids = prefs.getStringSet(KEY_COLLECTION, null);
        collection.addAll(ids != null ? ids : new TreeSet<String>());
    }
    protected void saveCollection(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_COLLECTION, collection);
        editor.apply();
    }

    protected ColorValues loadColors(Context context, SharedPreferences prefs, String colorsID)
    {
        ColorValues values = getDefaultColors(context);
        values.loadColorValues(prefs, colorsID);
        return values;
    }
    protected void saveColors(SharedPreferences prefs, String colorsID, ColorValues values) {
        values.putColors(prefs, colorsID);
    }
    protected void removeColors(Context context, SharedPreferences prefs, String colorsID) {
        ColorValues values = getColors(context, colorsID);
        if (values != null) {
            values.removeColors(prefs, colorsID);
        }
    }

    public abstract T getDefaultColors(Context context);
    protected HashMap<String, ColorValues> colorValues = new HashMap<>();
    @Nullable
    public ColorValues getColors( Context context, @Nullable String colorsID )
    {
        if (colorsID == null) {
            return null;
        }
        if (!colorValues.containsKey(colorsID))
        {
            ColorValues values = loadColors(context, getSharedPreferences(context), colorsID);
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
    public void setColors(Context context, String colorsID, ColorValues values)
    {
        ColorValues v = getDefaultColors(context);
        v.loadColorValues(values);    // copy colors into a new instance
        colorValues.put(colorsID, v);

        saveColors(getSharedPreferences(context), colorsID, v);
        if (collection.add(colorsID)) {
            saveCollection(getSharedPreferences(context));
        }
    }

    public void removeColors(Context context, String colorsID) {
        colorValues.remove(colorsID);
        removeColors(context, getSharedPreferences(context), colorsID);
        if (collection.remove(colorsID)) {
            saveCollection(getSharedPreferences(context));
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
        return prefs.getString(KEY_SELECTED + "_" + ((tag != null) ? (tag + "_") : "") + appWidgetID, null);
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
        editor.putString(KEY_SELECTED + "_" + ((tag != null) ? (tag + "_") : "") + appWidgetID, colorsID);
        editor.apply();
    }

    public void clearSelectedColorsID(Context context) {
        clearSelectedColorsID(context, 0, null);
    }
    public void clearSelectedColorsID(Context context, int appWidgetID) {
        clearSelectedColorsID(context, appWidgetID, null);
    }
    public void clearSelectedColorsID(Context context, int appWidgetID, @Nullable String tag) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        editor.remove(KEY_SELECTED + "_" + ((tag != null) ? (tag + "_") : "") + appWidgetID);
        editor.apply();
    }

    public abstract String getSharedPrefsName();
    protected SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(getSharedPrefsName(), 0);
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

}