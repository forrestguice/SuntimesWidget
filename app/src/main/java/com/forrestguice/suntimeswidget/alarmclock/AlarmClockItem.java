/**
    Copyright (C) 2018-2020 Forrest Guice
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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * AlarmClockItem
 */
public class AlarmClockItem implements Parcelable
{
    public static final String AUTHORITY = "com.forrestguice.suntimeswidget.alarmclock";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/alarms");

    public static final int ICON_ALARM = R.drawable.ic_action_alarms;
    public static final int ICON_NOTIFICATION = R.drawable.ic_action_notification;
    public static final int ICON_NOTIFICATION1 = R.drawable.ic_action_notification1;
    public static final int ICON_NOTIFICATION2 = R.drawable.ic_action_notification2;

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
    private String event = null;
    public String timezone = null;
    public Location location = null;
    public String ringtoneName = null;
    public String ringtoneURI = null;
    public boolean vibrate = false;
    public String actionID0 = null;
    public String actionID1 = null;

    public boolean modified = false;
    public AlarmState state = null;

    public AlarmClockItem() {}

    public AlarmClockItem( AlarmClockItem other )
    {
        this.rowID = other.rowID;
        this.type = other.type;
        this.enabled = other.enabled;
        this.label = other.label;

        this.repeating = other.repeating;
        this.repeatingDays = ((other.repeatingDays != null) ? new ArrayList<Integer>(other.repeatingDays) : null);

        this.alarmtime = other.alarmtime;
        this.timestamp = other.timestamp;
        this.hour = other.hour;
        this.minute = other.minute;
        this.offset = other.offset;

        this.location = ((other.location != null) ? new Location(other.location) : null);
        this.event = other.event;
        this.timezone = other.timezone;

        this.vibrate = other.vibrate;
        this.ringtoneName = other.ringtoneName;
        this.ringtoneURI = other.ringtoneURI;
        this.actionID0 = other.actionID0;
        this.actionID1 = other.actionID1;

        modified = other.modified;
        state = (other.state != null) ? new AlarmState(other.state) : null;
    }

    public AlarmClockItem(@Nullable Context context, ContentValues alarm) {
       fromContentValues(context, alarm);
    }

    private AlarmClockItem(Parcel in)
    {
        rowID = in.readLong();
        type = AlarmType.valueOf(in.readString());
        enabled = (in.readInt() == 1);
        label = in.readString();

        repeating = (in.readInt() == 1);
        setRepeatingDays(in.readString());

        alarmtime = in.readLong();
        timestamp = in.readLong();
        hour = in.readInt();
        minute = in.readInt();
        offset = in.readLong();

        String locLat = in.readString();
        String locLon = in.readString();
        String locLabel = in.readString();
        String locAlt = in.readString();
        boolean useAltitude = (in.readInt() == 1);

        if (locLat != null && locLon != null)
        {
            location = new Location(locLabel, locLat, locLon, locAlt);
            location.setUseAltitude(useAltitude);
        } else location = null;

        event = in.readString();
        timezone = in.readString();

        vibrate =  (in.readInt() == 1);
        ringtoneName = in.readString();
        ringtoneURI = in.readString();
        actionID0 = in.readString();
        actionID1 = in.readString();

        modified = (in.readInt() == 1);
        state = in.readParcelable(AlarmClockItem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(rowID);
        out.writeString(type.name());
        out.writeInt(enabled ? 1 : 0);
        out.writeString(label);

        out.writeInt(repeating ? 1 : 0);
        out.writeString(getRepeatingDays());

        out.writeLong(alarmtime);
        out.writeLong(timestamp);
        out.writeInt(hour);
        out.writeInt(minute);
        out.writeLong(offset);

        out.writeString(location.getLatitude());
        out.writeString(location.getLongitude());
        out.writeString(location.getLabel());
        out.writeString(location.getAltitude());
        out.writeInt(location.useAltitude() ? 1 : 0);

        //out.writeString(event != null ? event.name() : null);
        out.writeString(event);
        out.writeString(timezone);

        out.writeInt(vibrate ? 1 : 0);
        out.writeString(ringtoneName);
        out.writeString(ringtoneURI);
        out.writeString(actionID0);
        out.writeString(actionID1);

        out.writeInt(modified ? 1 : 0);
        out.writeParcelable(state, 0);
    }

    public void fromContentValues(Context context, ContentValues alarm)
    {
        rowID = (alarm.containsKey(AlarmDatabaseAdapter.KEY_ROWID) ? alarm.getAsLong(AlarmDatabaseAdapter.KEY_ROWID) : -1L);
        type = AlarmType.valueOf(alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_TYPE), AlarmType.ALARM);
        enabled = alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_ENABLED) && (alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_ENABLED) == 1);
        label = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_LABEL);

        repeating = alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_REPEATING) && (alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_REPEATING) == 1);
        setRepeatingDays(alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_REPEATING_DAYS));

        alarmtime = (alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_ADJUSTED) ? alarm.getAsLong(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_ADJUSTED) : -1L);
        timestamp = (alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_DATETIME) ? alarm.getAsLong(AlarmDatabaseAdapter.KEY_ALARM_DATETIME) : -1L);
        hour = (alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_HOUR) ? alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_HOUR) : -1);
        minute = (alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_MINUTE) ? alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_MINUTE) : -1);
        offset = (alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_OFFSET) ? alarm.getAsLong(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_OFFSET) : 0);

        String locLat = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_LATITUDE);
        String locLon = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_LONGITUDE);
        if (locLat !=  null && locLon != null)
        {
            String locLabel = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_PLACELABEL);
            String locAlt = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_ALTITUDE);
            location = new Location(locLabel, locLat, locLon, locAlt);

            if (context != null) {
                location.setUseAltitude(WidgetSettings.loadLocationAltitudeEnabledPref(context, 0));
            }

        } else location = null;

        event = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_SOLAREVENT);
        timezone = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_TIMEZONE);

        vibrate = alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_VIBRATE) && (alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_VIBRATE) == 1);
        ringtoneName = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_NAME);
        ringtoneURI = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_URI);
        actionID0 = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_ACTION0);
        actionID1 = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_ACTION1);
    }

    public ContentValues asContentValues(boolean withRowID)
    {
        ContentValues values = new ContentValues();
        if (withRowID) {
            values.put(AlarmDatabaseAdapter.KEY_ROWID, rowID);
        }
        values.put(AlarmDatabaseAdapter.KEY_ALARM_TYPE, type != null ? type.name() : null);
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
            values.put(AlarmDatabaseAdapter.KEY_ALARM_SOLAREVENT, event);
        } else values.putNull(AlarmDatabaseAdapter.KEY_ALARM_SOLAREVENT);

        if (timezone != null) {
            values.put(AlarmDatabaseAdapter.KEY_ALARM_TIMEZONE, timezone);
        } else values.putNull(AlarmDatabaseAdapter.KEY_ALARM_TIMEZONE);

        if (repeatingDays != null) {
            values.put(AlarmDatabaseAdapter.KEY_ALARM_REPEATING_DAYS, getRepeatingDays());
        } else values.putNull(AlarmDatabaseAdapter.KEY_ALARM_REPEATING_DAYS);

        values.put(AlarmDatabaseAdapter.KEY_ALARM_VIBRATE, (vibrate ? 1 : 0));
        values.put(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_NAME, ringtoneName);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_URI, ringtoneURI);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_ACTION0, actionID0);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_ACTION1, actionID1);
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

    @Nullable
    public String getEvent() {
        return event;
    }
    public void setEvent(@Nullable String event) {
        this.event = event;
        eventItem = null;
    }

    private AlarmEvent.AlarmEventItem eventItem = null;
    public AlarmEvent.AlarmEventItem getEventItem(Context context)
    {
        if (eventItem == null) {
            eventItem = new AlarmEvent.AlarmEventItem(getEvent(), context.getContentResolver());
        }
        return eventItem;
    }

    public int getIcon()
    {
        switch (this.type) {
            case ALARM: return ICON_ALARM;
            case NOTIFICATION2: return ICON_NOTIFICATION2;
            case NOTIFICATION1: return ICON_NOTIFICATION1;
            case NOTIFICATION: default: return ICON_NOTIFICATION;
        }
    }

    public String getLabel(Context context)
    {
        switch(type) {
            case ALARM: return context.getString(R.string.alarmMode_alarm);
            case NOTIFICATION: case NOTIFICATION1: case NOTIFICATION2:
            default: return context.getString(R.string.alarmMode_notification);
        }
    }

    /*public String getLabelAlt(Context context)
    {
        SolarEvents solarEvent = SolarEvents.valueOf(event, null);
        return getLabel((solarEvent != null) ? solarEvent.getShortDisplayString() : context.getString(R.string.alarmOption_solarevent_none));   // TODO
    }*/

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

    public boolean hasActionID(int actionNum)
    {
        String value = getActionID(actionNum);
        return (value != null && !value.trim().isEmpty());
    }
    public String getActionID(int actionNum)
    {
        String value;
        switch (actionNum) {
            case ACTIONID_DISMISS: value = actionID1; break;
            case ACTIONID_MAIN: default: value = actionID0; break;
        }
        return (value != null ? value.trim() : null);
    }
    public void setActionID(int actionNum, String actionID)
    {
        String value = (actionID != null  && !actionID.trim().isEmpty() ? actionID.trim() : null);
        switch (actionNum) {
            case ACTIONID_DISMISS: actionID1 = value; break;
            case ACTIONID_MAIN: default: actionID0 = value; break;
        }
    }
    public static final int ACTIONID_MAIN = 0;
    public static final int ACTIONID_DISMISS = 1;

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
        NOTIFICATION("Notification"),
        NOTIFICATION1("Notification (ephemeral)"),
        NOTIFICATION2("Notification (persistent)");

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

    /**
     * AlarmTimeZone
     */
    public static enum AlarmTimeZone
    {
        APPARENT_SOLAR_TIME(WidgetTimezones.ApparentSolarTime.TIMEZONEID, WidgetTimezones.ApparentSolarTime.TIMEZONEID),
        LOCAL_MEAN_TIME(WidgetTimezones.LocalMeanTime.TIMEZONEID, WidgetTimezones.LocalMeanTime.TIMEZONEID),
        SYSTEM_TIME("System Time Zone", null);

        private String displayString;
        private String tzID;

        private AlarmTimeZone(String displayString, String tzID)
        {
            this.displayString = displayString;
            this.tzID = tzID;
        }

        public String timeZoneID() {
            return tzID;
        }

        public String toString()
        {
            return displayString;
        }

        public String displayString() {
            return displayString;
        }

        public static String displayString(String tzID)
        {
            if (tzID == null) {
                return SYSTEM_TIME.displayString();

            } else if (tzID.equals(APPARENT_SOLAR_TIME.timeZoneID())) {
                return APPARENT_SOLAR_TIME.displayString();

            } else if (tzID.equals(LOCAL_MEAN_TIME.timeZoneID())) {
                return LOCAL_MEAN_TIME.displayString;

            } else {
                return TimeZone.getTimeZone(tzID).getDisplayName();
            }
        }

        public void setDisplayString( String displayString ) {
            this.displayString = displayString;
        }

        public static void initDisplayStrings( Context context )
        {
            SYSTEM_TIME.setDisplayString(context.getString(R.string.timezoneMode_current));
            LOCAL_MEAN_TIME.setDisplayString(context.getString(R.string.time_localMean));
            APPARENT_SOLAR_TIME.setDisplayString(context.getString(R.string.time_apparent));
        }

        public TimeZone getTimeZone(Location location) {
            return AlarmTimeZone.getTimeZone(timeZoneID(), location);
        }

        public static TimeZone getTimeZone(String tzID, Location location)
        {
            if (location == null || tzID == null) {
                return TimeZone.getDefault();

            } else if (tzID.equals(APPARENT_SOLAR_TIME.timeZoneID())) {
                return new WidgetTimezones.ApparentSolarTime(location.getLongitudeAsDouble(), APPARENT_SOLAR_TIME.displayString());

            } else if (tzID.equals(LOCAL_MEAN_TIME.timeZoneID())) {
                return new WidgetTimezones.LocalMeanTime(location.getLongitudeAsDouble(), LOCAL_MEAN_TIME.displayString());

            } else {
                return TimeZone.getTimeZone(tzID);
            }
        }

        public static AlarmTimeZone valueOfID(String tzID)
        {
            if (tzID == null) {
                return SYSTEM_TIME;

            } else if (tzID.equals(APPARENT_SOLAR_TIME.timeZoneID())) {
                return APPARENT_SOLAR_TIME;

            } else if (tzID.equals(LOCAL_MEAN_TIME.timeZoneID())) {
                return LOCAL_MEAN_TIME;

            } else {
                return null;
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<AlarmClockItem> CREATOR = new Parcelable.Creator<AlarmClockItem>()
    {
        public AlarmClockItem createFromParcel(Parcel in) {
            return new AlarmClockItem(in);
        }

        public AlarmClockItem[] newArray(int size) {
            return new AlarmClockItem[size];
        }
    };

}
