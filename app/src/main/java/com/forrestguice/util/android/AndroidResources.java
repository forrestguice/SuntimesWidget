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

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.forrestguice.util.res.TypedArray;

public class AndroidResources implements com.forrestguice.util.Resources
{
    private final Context context;
    public AndroidResources(Context context) {
        this.context = context;
    }

    public static AndroidResources wrap(Context context) {
        return new AndroidResources(context);
    }

    @Override
    public boolean getBoolean(int id) {
        return context.getResources().getBoolean(id);
    }

    @Override
    public int getColor(int id) {
        return ContextCompat.getColor(context, id);
    }

    @Override
    public float getDimension(int id) {
        return context.getResources().getDimension(id);
    }

    @Override
    public int getInteger(int id) {
        return context.getResources().getInteger(id);
    }
    @Override
    public int[] getIntArray(int id) {
        return context.getResources().getIntArray(id);
    }

    @Override
    public String getString(int id) {
        return context.getString(id);
    }
    @Override
    public String getString(int id, Object... formatArgs) {
        return context.getString(id, formatArgs);
    }
    @Override
    public String[] getStringArray(int id) {
        return context.getResources().getStringArray(id);
    }

    @Override
    public String getQuantityString(int id, int quantity) {
        return context.getResources().getQuantityString(id, quantity);
    }
    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs) {
        return context.getResources().getQuantityString(id, quantity, formatArgs);
    }

    @Override
    public TypedArray obtainStyledAttributes(int[] colorAttrs) {
        return new AndroidTypedArray(context.obtainStyledAttributes(colorAttrs));
    }
}
