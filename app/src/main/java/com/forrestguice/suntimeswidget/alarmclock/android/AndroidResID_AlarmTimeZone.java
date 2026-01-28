/**
    Copyright (C) 2018-2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock.android;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmTimeZone;

public class AndroidResID_AlarmTimeZone implements AlarmTimeZone.ResID_AlarmTimeZone
{
    public static AlarmTimeZone.ResID_AlarmTimeZone get() {
        return new AndroidResID_AlarmTimeZone();
    }

    public int string_timezoneMode_current() {
        return R.string.timezoneMode_current;
    }
    public int string_time_localMean() {
        return R.string.time_localMean;
    }
    public int string_time_apparent() {
        return R.string.time_apparent;
    }
}
