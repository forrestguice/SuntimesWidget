/**
    Copyright (C) 2025 Forrest Guice
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

import com.forrestguice.util.res.TypedArray;

public class AndroidTypedArray implements TypedArray
{
    private android.content.res.TypedArray typedArray;
    public AndroidTypedArray(android.content.res.TypedArray array) {
        typedArray = array;
    }

    @Override
    public boolean getBoolean(int index, boolean defaultValue) {
        return typedArray.getBoolean(index, defaultValue);
    }

    @Override
    public int getColor(int index, int defaultValue) {
        return typedArray.getColor(index, defaultValue);
    }

    @Override
    public float getFloat(int index, float defaultValue) {
        return typedArray.getFloat(index, defaultValue);
    }

    @Override
    public int getInt(int index, int defaultValue) {
        return typedArray.getInt(index, defaultValue);
    }

    @Override
    public int getResourceId(int index, int defaultValue) {
        if (typedArray != null) {
            return typedArray.getResourceId(index, defaultValue);
        } else return defaultValue;
    }

    @Override
    public String getString(int index) {
        return typedArray.getString(index);
    }

    @Override
    public boolean hasValue(int index) {
        return typedArray.hasValue(index);
    }

    @Override
    public int length() {
        return typedArray.length();
    }

    @Override
    public void recycle() {
        typedArray.recycle();
        typedArray = null;
    }
}
