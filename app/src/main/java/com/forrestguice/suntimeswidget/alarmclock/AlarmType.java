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

package com.forrestguice.suntimeswidget.alarmclock;

import android.content.Context;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;

public enum AlarmType
{
    ALARM("Alarm"),
    NOTIFICATION("Notification"),
    NOTIFICATION1("Notification (ephemeral)"),
    NOTIFICATION2("Notification (persistent)");

    private String displayString;

    private AlarmType(String displayString)
    {
        this.displayString = displayString;
    }

    @NonNull
    public String toString()
    {
        return displayString;
    }

    public String getDisplayString()
    {
        return displayString;
    }

    public void setDisplayString( String displayString )
    {
        this.displayString = displayString;
    }

    public static void initDisplayStrings( Context context )
    {
        ALARM.setDisplayString(context.getString(R.string.alarmMode_alarm));
        NOTIFICATION.setDisplayString(context.getString(R.string.alarmMode_notification));
        NOTIFICATION1.setDisplayString(context.getString(R.string.alarmMode_notification1));
        NOTIFICATION2.setDisplayString(context.getString(R.string.alarmMode_notification2));
    }

    public static AlarmType valueOf(String value, AlarmType defaultType)
    {
        AlarmType retValue = defaultType;
        if (value != null)
        {
            try {
                retValue = AlarmType.valueOf(value);

            } catch (IllegalArgumentException e) {
                Log.w("AlarmType", "valueOf :: failed to load '" + value);
                retValue = defaultType;
            }
        }
        return retValue;
    }
}
