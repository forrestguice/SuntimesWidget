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

package com.forrestguice.util.prefs;

public class PreferenceManager
{
    private static PreferenceManagerInterface manager = null;
    public static void init(PreferenceManagerInterface i) {
        manager = i;
    }

    public static SharedPreferences getDefaultSharedPreferences(Object context) {
        return (manager != null ? manager.getDefaultSharedPreferences(context) : null);
    }
}
