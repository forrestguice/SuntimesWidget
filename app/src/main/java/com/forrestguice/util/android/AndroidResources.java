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

import com.forrestguice.util.Resources;

public class AndroidResources implements Resources
{
    private final Context context;
    public AndroidResources(Context context) {
        this.context = context;
    }

    public static AndroidResources wrap(Context context) {
        return new AndroidResources(context);
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
    public String getQuantityString(int id, int quantity) {
        return context.getResources().getQuantityString(id, quantity);
    }
    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs) {
        return context.getResources().getQuantityString(id, quantity, formatArgs);
    }
}
