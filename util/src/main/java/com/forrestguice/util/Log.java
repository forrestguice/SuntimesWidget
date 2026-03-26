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

import com.forrestguice.util.log.LogInterface;
import com.forrestguice.util.log.StandardLog;

public class Log
{
    private static LogInterface log = new StandardLog();
    public static void init(LogInterface i) {
        log = i;
    }

    protected static boolean show_d = true;
    public static void setShowDebug(boolean value) {
        show_d = value;
    }

    public static void d(String tag, String data) {
        if (show_d) {
            log.d(tag, data);
        }
    }

    public static void i(String tag, String data) {
        log.i(tag, data);
    }

    public static void e(String tag, String data) {
        log.e(tag, data);
    }

    public static void e(String tag, String data, Throwable t) {
        log.e(tag, data, t);
    }

    public static void w(String tag, String data) {
        log.w(tag, data);
    }

    public static void wtf(String tag, String data) {
        log.wtf(tag, data);
    }
}
