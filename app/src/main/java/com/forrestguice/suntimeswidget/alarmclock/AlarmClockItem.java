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
import android.util.Log;

import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class AlarmClockItem
{
    protected long rowID = -1;
    protected boolean enabled = false;
    protected boolean repeating = false;
    protected long timestamp = 0;
    protected long offset = 0;
    protected String label = null;
    protected SolarEvents event = null;
    protected WidgetSettings.Location location = null;
    protected String ringtoneName = null;
    protected String ringtoneURI = null;
    protected boolean vibrate = false;

    protected int icon = 0;
    protected boolean modified = false;

    public AlarmClockItem() {}

    public AlarmClockItem(ContentValues alarm)
    {
        rowID = alarm.getAsLong(AlarmClockDatabaseAdapter.KEY_ROWID);
        enabled = (alarm.getAsInteger(AlarmClockDatabaseAdapter.KEY_ALARM_ENABLED) == 1);
        repeating = (alarm.getAsInteger(AlarmClockDatabaseAdapter.KEY_ALARM_REPEATING) == 1);
        timestamp = alarm.getAsLong(AlarmClockDatabaseAdapter.KEY_ALARM_DATETIME);
        label = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_LABEL);

        String locLat = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_LATITUDE);
        String locLon = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_LONGITUDE);
        if (locLat !=  null && locLon != null)
        {
            String locLabel = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_PLACELABEL);
            String locAlt = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_ALTITUDE);
            location = new WidgetSettings.Location(locLabel, locLat, locLon, locAlt);
        } else location = null;

        String eventString = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_SOLAREVENT);
        if (eventString != null)
        {
            try {
                event = SolarEvents.valueOf(eventString);
            } catch (IllegalArgumentException e) {
                Log.w("AlarmClockItem", "Failed to load SolarEvent '" + eventString);
                event = null;
            }
        }

        vibrate = (alarm.getAsInteger(AlarmClockDatabaseAdapter.KEY_ALARM_VIBRATE) == 1);
        ringtoneName = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_RINGTONE_NAME);
        ringtoneURI = alarm.getAsString(AlarmClockDatabaseAdapter.KEY_ALARM_RINGTONE_URI);
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
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_DATETIME_OFFSET, offset);
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_LABEL, label);

        if (location != null)
        {
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_PLACELABEL, location.getLabel());
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_LATITUDE, location.getLatitude());
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_LONGITUDE, location.getLongitude());
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_ALTITUDE, location.getAltitude());
        }

        if (event != null)
        {
            values.put(AlarmClockDatabaseAdapter.KEY_ALARM_SOLAREVENT, event.name());
        }

        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_VIBRATE, (vibrate ? 1 : 0));
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_RINGTONE_NAME, ringtoneName);
        values.put(AlarmClockDatabaseAdapter.KEY_ALARM_RINGTONE_URI, ringtoneURI);
        return values;
    }
}
