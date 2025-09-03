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

package com.forrestguice.util;

import com.forrestguice.util.time.TimeFormatInterface;

public class SystemTimeFormat
{
    private static TimeFormatInterface format = null;
    public static void init(TimeFormatInterface i) {
        format = i;
    }

    public static boolean is24HourFormat()
    {
        if (format != null) {
            return format.is24HourFormat();
        } else {
            Log.e("SystemTimeFormat", "time format is uninitialized! call `SystemTimeFormat.init` before initial use.");
            return true;
        }
    }
}
