// SPDX-License-Identifier: GPL-3.0-or-later
/*
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

import com.forrestguice.util.ContextInterface;
import com.forrestguice.util.Resources;
import com.forrestguice.util.content.ContentResolver;
import com.forrestguice.util.prefs.SharedPreferences;

public class AndroidContext implements ContextInterface
{
    private final android.content.Context context;
    public AndroidContext(android.content.Context context) {
        this.context = context;
    }

    public static ContextInterface wrap(android.content.Context context) {
        return new AndroidContext(context);
    }

    @Override
    public Resources getResources() {
        if (resources == null) {
            resources = AndroidResources.wrap(context);
        }
        return resources;
    }
    private Resources resources;

    @Override
    public String getString(int id) {
        return context.getString(id);
    }

    @Override
    public String getString(int id, Object... formatArgs) {
        return context.getString(id, formatArgs);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int flags) {
        return AndroidSharedPreferences.wrap(context.getSharedPreferences(name, flags));
    }

    @Override
    public ContentResolver getContentResolver() {
        return AndroidContentResolver.wrap(context.getContentResolver());
    }
}