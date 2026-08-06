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

import com.forrestguice.util.log.LogInterface;

public class AndroidLog implements LogInterface
{
    @Override
    public void d(String tag, String data) {
        android.util.Log.d(tag, data);
    }

    @Override
    public void i(String tag, String data) {
        android.util.Log.i(tag, data);
    }

    @Override
    public void e(String tag, String data) {
        android.util.Log.e(tag, data);
    }

    @Override
    public void e(String tag, String data, Throwable t) {
        android.util.Log.e(tag, data, t);
    }

    @Override
    public void w(String tag, String data) {
        android.util.Log.w(tag, data);
    }

    @Override
    public void wtf(String tag, String data) {
        android.util.Log.wtf(tag, data);
    }
}
