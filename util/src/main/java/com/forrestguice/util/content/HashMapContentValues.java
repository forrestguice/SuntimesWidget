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

package com.forrestguice.util.content;

import java.util.HashMap;
import java.util.Set;

public class HashMapContentValues implements ContentValues
{
    protected final HashMap<String, Object> map;

    public HashMapContentValues() {
        map = new HashMap<>();
    }

    public HashMapContentValues(HashMap<String, Object> values) {
        map = values;
    }

    public static ContentValues wrap(HashMap<String, Object> values) {
        return new HashMapContentValues(values);
    }

    @Override
    public ContentValues newInstance() {
        return new HashMapContentValues();
    }

    @Override
    public Object getNativeObject() {
        return map;
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void remove(String key) {
        map.remove(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void putNull(String key) {
        map.put(key, null);
    }

    @Override
    public void putAll(ContentValues other)
    {
        for (String key : other.keySet()) {
            map.put(key, other.get(key));
        }
    }

    @Override
    public void put(String key, String value) {
        map.put(key, value);
    }

    @Override
    public void put(String key, Byte value) {
        map.put(key, value);
    }

    @Override
    public void put(String key, Integer value) {
        map.put(key, value);
    }

    @Override
    public void put(String key, Long value) {
        map.put(key, value);
    }

    @Override
    public void put(String key, Float value) {
        map.put(key, value);
    }

    @Override
    public void put(String key, Short value) {
        map.put(key, value);
    }

    @Override
    public void put(String key, Double value) {
        map.put(key, value);
    }

    @Override
    public void put(String key, Boolean value) {
        map.put(key, value);
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    public <T> T getAsType(String key)
    {
        Object o = map.get(key);
        if (o != null)
        {
            try {
                return (T) o;

            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getAsString(String key) {
        return getAsType(key);
    }

    @Override
    public Long getAsLong(String key) {
        return getAsType(key);
    }

    @Override
    public Boolean getAsBoolean(String key) {
        return getAsType(key);
    }

    @Override
    public Integer getAsInteger(String key) {
        return getAsType(key);
    }

    @Override
    public Double getAsDouble(String key) {
        return getAsType(key);
    }

    @Override
    public Byte getAsByte(String key) {
        return getAsType(key);
    }

    @Override
    public Float getAsFloat(String key) {
        return getAsType(key);
    }

    @Override
    public Short getAsShort(String key) {
        return getAsType(key);
    }
}
