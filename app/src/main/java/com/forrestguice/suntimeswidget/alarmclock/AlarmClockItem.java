/**
    Copyright (C) 2018 Forrest Guice
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

import android.content.ContentValues;

import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class AlarmClockItem
{
    protected int rowID = -1;
    protected boolean enabled = false;
    protected boolean repeating = false;
    protected long timestamp = 0;
    protected long offset = 0;
    protected String label = null;
    protected SolarEvents event = null;
    protected WidgetSettings.Location location = null;
    protected String ringtone = null;
    protected boolean vibrate = false;

    protected int icon = 0;
    protected boolean modified = false;

    public AlarmClockItem() {}

    public AlarmClockItem(ContentValues alarm)
    {
        rowID = alarm.getAsInteger(AlarmClockDatabaseAdapter.KEY_ROWID);
        enabled = (alarm.getAsInteger(AlarmClockDatabaseAdapter.KEY_ALARM_ENABLED) == 1);
        repeating = (alarm.getAsInteger(AlarmClockDatabaseAdapter.KEY_ALARM_REPEATING) == 1);
        timestamp = alarm.getAsLong(AlarmClockDatabaseAdapter.KEY_ALARM_DATETIME);
        label = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_LABEL);
        location = new WidgetSettings.Location(alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_PLACELABEL), alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_LATITUDE), alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_LONGITUDE), alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_ALTITUDE));
        vibrate = (alarm.getAsInteger(AlarmClockDatabaseAdapter.KEY_ALARM_VIBRATE) == 1);
        ringtone = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_RINGTONE);
    }

    public ContentValues asContentValues(boolean withRowID)
    {
        ContentValues values = new ContentValues();
        if (withRowID) {
            values.put(AlarmClockDatabaseAdapter.KEY_ROWID, rowID);
        }
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_ENABLED, (enabled ? 1 : 0));
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_REPEATING, (repeating ? 1 : 0));
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_DATETIME, timestamp);
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_LABEL, label);

        if (location != null)
        {
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_PLACELABEL, location.getLabel());
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_LATITUDE, location.getLatitude());
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_LONGITUDE, location.getLongitude());
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_ALTITUDE, location.getAltitude());
        }

        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_VIBRATE, (vibrate ? 1 : 0));
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_RINGTONE, ringtone);
        return values;
    }
}
