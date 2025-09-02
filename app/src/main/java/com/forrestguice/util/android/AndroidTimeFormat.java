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

import com.forrestguice.util.Log;
import com.forrestguice.util.time.TimeFormatInterface;

import java.lang.ref.WeakReference;

public class AndroidTimeFormat implements TimeFormatInterface
{
    protected WeakReference<Context> contextRef;
    public AndroidTimeFormat(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    public boolean is24HourFormat()
    {
        Context context = contextRef.get();
        if (context != null) {
            return android.text.format.DateFormat.is24HourFormat(context);
        } else {
            Log.e("AndroidTimeFormat", "is24HourFormat; null context!");
            return true;
        }
    }
}
