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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * AlarmClockItem
 */
public class AlarmClockItem
{
    public static final String AUTHORITY = "com.forrestguice.suntimeswidget.alarmclock";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/alarms");

    public static final int ICON_ALARM = R.drawable.ic_action_alarms;
    public static final int ICON_NOTIFICATION = R.drawable.ic_action_notification;

    public long rowID = -1L;
    public AlarmType type = AlarmType.ALARM;
    public boolean enabled = false;
    public boolean repeating = false;
    public ArrayList<Integer> repeatingDays = null;
    public long alarmtime = -1L;
    public long timestamp = -1L;
    public int hour = -1, minute = -1;
    public long offset = 0;
    public String label = null;
    public SolarEvents event = null;
    public Location location = null;
    public String ringtoneName = null;
    public String ringtoneURI = null;
    public boolean vibrate = false;

    public boolean modified = false;
    public AlarmState state = null;

    public AlarmClockItem() {}

    public AlarmClockItem(ContentValues alarm)
    {
        rowID = alarm.getAsLong(AlarmDatabaseAdapter.KEY_ROWID);
        type = AlarmType.valueOf(alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_TYPE), AlarmType.ALARM);
        enabled = (alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_ENABLED) == 1);
        label = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_LABEL);

        repeating = (alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_REPEATING) == 1);
        setRepeatingDays(alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_REPEATING_DAYS));

        alarmtime = alarm.getAsLong(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_ADJUSTED);
        timestamp = alarm.getAsLong(AlarmDatabaseAdapter.KEY_ALARM_DATETIME);
        hour = alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_HOUR);
        minute = alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_MINUTE);
        offset = alarm.getAsLong(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_OFFSET);

        String locLat = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_LATITUDE);
        String locLon = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_LONGITUDE);
        if (locLat !=  null && locLon != null)
        {
            String locLabel = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_PLACELABEL);
            String locAlt = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_ALTITUDE);
            location = new Location(locLabel, locLat, locLon, locAlt);
        } else location = null;

        String eventString = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_SOLAREVENT);
        event = SolarEvents.valueOf(eventString, null);

        vibrate = (alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_VIBRATE) == 1);
        ringtoneName = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_NAME);
        ringtoneURI = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_URI);
    }

    public ContentValues asContentValues(boolean withRowID)
    {
        ContentValues values = new ContentValues();
        if (withRowID) {
            values.put(AlarmDatabaseAdapter.KEY_ROWID, rowID);
        }
        values.put(AlarmDatabaseAdapter.KEY_ALARM_TYPE, type.name());
        values.put(AlarmDatabaseAdapter.KEY_ALARM_ENABLED, (enabled ? 1 : 0));
        values.put(AlarmDatabaseAdapter.KEY_ALARM_LABEL, label);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_REPEATING, (repeating ? 1 : 0));

        values.put(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_ADJUSTED, alarmtime);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_DATETIME, timestamp);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_HOUR, hour);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_MINUTE, minute);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_OFFSET, offset);

        if (location != null)
        {
            values.put(AlarmDatabaseAdapter.KEY_ALARM_PLACELABEL, location.getLabel());
            values.put(AlarmDatabaseAdapter.KEY_ALARM_LATITUDE, location.getLatitude());
            values.put(AlarmDatabaseAdapter.KEY_ALARM_LONGITUDE, location.getLongitude());
            values.put(AlarmDatabaseAdapter.KEY_ALARM_ALTITUDE, location.getAltitude());
        }

        if (event != null) {
            values.put(AlarmDatabaseAdapter.KEY_ALARM_SOLAREVENT, event.name());
        } else values.putNull(AlarmDatabaseAdapter.KEY_ALARM_SOLAREVENT);

        if (repeatingDays != null) {
            values.put(AlarmDatabaseAdapter.KEY_ALARM_REPEATING_DAYS, getRepeatingDays());
        } else values.putNull(AlarmDatabaseAdapter.KEY_ALARM_REPEATING_DAYS);

        values.put(AlarmDatabaseAdapter.KEY_ALARM_VIBRATE, (vibrate ? 1 : 0));
        values.put(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_NAME, ringtoneName);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_URI, ringtoneURI);
        return values;
    }

    public void setState(int value)
    {
        if (state == null)
            state = new AlarmState(rowID, value);
        else state.setState(value);
    }

    public int getState()
    {
        return (state != null ? state.getState() : AlarmState.STATE_NONE);
    }

    public int getIcon()
    {
        return ((type == AlarmClockItem.AlarmType.NOTIFICATION) ? ICON_NOTIFICATION : ICON_ALARM);
    }

    public String getLabel(Context context)
    {
        return getLabel((type == AlarmClockItem.AlarmType.ALARM) ? context.getString(R.string.alarmMode_alarm) : context.getString(R.string.alarmMode_notification));
    }

    public String getLabelAlt(Context context)
    {
        return getLabel((event != null) ? event.getShortDisplayString() : context.getString(R.string.alarmOption_solarevent_none));
    }

    public String getLabel(String emptyLabel)
    {
        return (label == null || label.isEmpty() ? emptyLabel : label);
    }

    public Calendar getCalendar()
    {
        if (timestamp >= 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            return calendar;
        } else return null;
    }

    public Calendar getAdjustedCalendar()
    {
        Calendar calendar = getCalendar();
        calendar.setTimeInMillis(alarmtime);
        return calendar;
    }

    /**
     * repeatsEveryDay
     */
    public static boolean repeatsEveryDay(ArrayList<Integer> repeatingDays)
    {
        if (repeatingDays != null)
        {
            if (repeatingDays.size() > 0)
            {
                return (repeatingDays.contains(Calendar.SUNDAY) &&
                        repeatingDays.contains(Calendar.MONDAY) &&
                        repeatingDays.contains(Calendar.TUESDAY) &&
                        repeatingDays.contains(Calendar.WEDNESDAY) &&
                        repeatingDays.contains(Calendar.THURSDAY) &&
                        repeatingDays.contains(Calendar.FRIDAY) &&
                        repeatingDays.contains(Calendar.SATURDAY));
            } else return false;
        } else return false;
    }

    /**
     * getRepeatingDays
     * @return a stringlist representation of repeatingDays Array (e.g. "0,1,2,3");
     */
    public String getRepeatingDays()
    {
        if (repeatingDays != null)
        {
            int n = repeatingDays.size();
            StringBuilder repeatingDaysString = new StringBuilder();
            for (int i=0; i<n; i++)
            {
                Integer day = repeatingDays.get(i);
                repeatingDaysString.append(day.toString());

                boolean isLast = (i == (n - 1));
                if (!isLast) {
                    repeatingDaysString.append(",");
                }
            }
            return repeatingDaysString.toString();
        } else return null;
    }

    /**
     * getUri
     * @return e.g. content://com.forrestguice.suntimeswidget.alarmclock/alarms/[rowID]
     */
    public Uri getUri()
    {
        return ContentUris.withAppendedId(CONTENT_URI, rowID);
    }

    /**
     * setRepeatingDays
     * @param repeatingDaysString a stringlist representation of repeatingDays Array (e.g. "0,1,2,3");
     */
    public void setRepeatingDays(String repeatingDaysString)
    {
        if (repeatingDaysString != null)
        {
            String[] repeatingDaysStringArray = repeatingDaysString.split(",");
            Integer[] repeatingDaysArray = new Integer[repeatingDaysStringArray.length];
            for (int i=0; i<repeatingDaysArray.length; i++) {
                try {
                    repeatingDaysArray[i] = Integer.parseInt(repeatingDaysStringArray[i]);
                } catch (NumberFormatException e) {
                    repeatingDaysArray = null;
                    break;
                }
            }

            if (repeatingDaysArray != null)
            {
                repeatingDays = new ArrayList<>();
                repeatingDays.addAll(Arrays.asList(repeatingDaysArray));
            } else repeatingDays = null;
        } else repeatingDays = null;
    }

    /**
     * AlarmType
     */
    public static enum AlarmType
    {
        ALARM("Alarm"),
        NOTIFICATION("Notification");

        private String displayString;

        private AlarmType(String displayString)
        {
            this.displayString = displayString;
        }

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

}
