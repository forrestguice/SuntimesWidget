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
import com.forrestguice.suntimeswidget.alarmclock.AlarmType;

public class AndroidResID_AlarmType implements AlarmType.ResID_AlarmType
{
    public static AlarmType.ResID_AlarmType get() {
        return new AndroidResID_AlarmType();
    }

    @Override
    public int string_alarmMode_alarm() {
        return R.string.alarmMode_alarm;
    }

    @Override
    public int string_alarmMode_notification() {
        return R.string.alarmMode_notification;
    }

    @Override
    public int string_alarmMode_notification1() {
        return R.string.alarmMode_notification1;
    }

    @Override
    public int string_alarmMode_notification2() {
        return R.string.alarmMode_notification2;
    }
}
