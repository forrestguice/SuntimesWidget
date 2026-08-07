/**
    Copyright (C) 2026 Forrest Guice
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

package com.forrestguice.util.android;

import android.os.Build;

import com.forrestguice.util.content.ContentValues;

import java.util.Set;

public class AndroidContentValues implements ContentValues
{
    protected final android.content.ContentValues v;
    public AndroidContentValues(android.content.ContentValues values) {
        v = values;
    }

    public static ContentValues wrap(android.content.ContentValues values) {
        return new AndroidContentValues(values);
    }

    public static ContentValues[] wrap(android.content.ContentValues[] values)
    {
        ContentValues[] r = new ContentValues[values.length];
        for (int i=0; i<r.length; i++) {
            r[i] = AndroidContentValues.wrap(values[i]);
        }
        return r;
    }

    @Override
    public Object getNativeObject() {
        return v;
    }

    @Override
    public boolean containsKey(String key) {
        return v.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return v.keySet();
    }

    @Override
    public int size() {
        return v.size();
    }

    @Override
    public void clear() {
        v.clear();
    }

    @Override
    public void remove(String key) {
        v.remove(key);
    }

    @Override
    public boolean isEmpty() {
        if (Build.VERSION.SDK_INT >= 30) {
            return v.isEmpty();
        } else return false;
    }

    @Override
    public void putNull(String key) {
        v.putNull(key);
    }

    @Override
    public void putAll(ContentValues other) {
        v.putAll((android.content.ContentValues) other.getNativeObject());
    }

    @Override
    public void put(String key, String value) {
        v.put(key, value);
    }

    @Override
    public void put(String key, Byte value) {
        v.put(key, value);
    }

    @Override
    public void put(String key, Integer value) {
        v.put(key, value);
    }

    @Override
    public void put(String key, Long value) {
        v.put(key, value);
    }

    @Override
    public void put(String key, Float value) {
        v.put(key, value);
    }

    @Override
    public void put(String key, Short value) {
        v.put(key, value);
    }

    @Override
    public void put(String key, Double value) {
        v.put(key, value);
    }

    @Override
    public void put(String key, Boolean value) {
        v.put(key, value);
    }

    @Override
    public Object get(String key) {
        return v.get(key);
    }

    @Override
    public String getAsString(String key) {
        return v.getAsString(key);
    }

    @Override
    public Long getAsLong(String key) {
        return v.getAsLong(key);
    }

    @Override
    public Boolean getAsBoolean(String key) {
        return v.getAsBoolean(key);
    }

    @Override
    public Integer getAsInteger(String key) {
        return v.getAsInteger(key);
    }

    @Override
    public Double getAsDouble(String key) {
        return v.getAsDouble(key);
    }

    @Override
    public Byte getAsByte(String key) {
        return v.getAsByte(key);
    }

    @Override
    public Float getAsFloat(String key) {
        return v.getAsFloat(key);
    }

    @Override
    public Short getAsShort(String key) {
        return v.getAsShort(key);
    }
}
