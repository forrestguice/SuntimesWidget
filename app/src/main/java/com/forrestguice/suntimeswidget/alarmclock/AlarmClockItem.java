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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * AlarmClockItem
 */
public class AlarmClockItem implements AlarmItemInterface, Parcelable
{
    public static final int ICON_ALARM = R.drawable.ic_action_alarms;
    public static final int ICON_NOTIFICATION = R.drawable.ic_action_notification;
    public static final int ICON_NOTIFICATION1 = R.drawable.ic_action_notification1;
    public static final int ICON_NOTIFICATION2 = R.drawable.ic_action_notification2;

    public long rowID = -1L;
    @Nullable
    public AlarmType type = AlarmType.ALARM;
    public boolean enabled = false;
    public boolean repeating = false;
    @Nullable
    public ArrayList<Integer> repeatingDays = null;
    public long alarmtime = -1L;
    public long timestamp = -1L;
    public int hour = -1, minute = -1;
    public long offset = 0;
    @Nullable
    public String label = null;
    @Nullable
    public String note = null;
    @Nullable
    private String event = null;
    @Nullable
    public String timezone = null;
    @Nullable
    public Location location = null;
    @Nullable
    public String ringtoneName = null;
    @Nullable
    public String ringtoneURI = null;
    public boolean vibrate = false;
    @Nullable
    public String actionID0 = null;
    @Nullable
    public String actionID1 = null;
    @Nullable
    public String actionID2 = null;
    @Nullable
    public String actionID3 = null;

    @Nullable
    protected HashMap<String, Long> alarmFlags = null;

    public boolean modified = false;
    @Nullable
    public AlarmState state = null;

    public AlarmClockItem() {}

    public AlarmClockItem( AlarmClockItem other )
    {
        this.rowID = other.rowID;
        this.type = other.type;
        this.enabled = other.enabled;
        this.label = other.label;
        this.note = other.note;

        this.repeating = other.repeating;
        this.repeatingDays = ((other.repeatingDays != null) ? new ArrayList<Integer>(other.repeatingDays) : null);

        this.alarmtime = other.alarmtime;
        this.timestamp = other.timestamp;
        this.hour = other.hour;
        this.minute = other.minute;
        this.offset = other.offset;

        this.location = ((other.location != null) ? new Location(other.location) : null);
        this.event = other.event;
        this.eventItem = null;
        this.timezone = other.timezone;

        this.vibrate = other.vibrate;
        this.ringtoneName = other.ringtoneName;
        this.ringtoneURI = other.ringtoneURI;
        this.actionID0 = other.actionID0;
        this.actionID1 = other.actionID1;
        this.actionID2 = other.actionID2;
        this.actionID3 = other.actionID3;
        this.alarmFlags = (other.alarmFlags != null ? new HashMap<>(other.alarmFlags) : null);

        modified = other.modified;
        state = (other.state != null) ? new AlarmState(other.state) : null;
    }

    public AlarmClockItem(@Nullable Context context, ContentValues alarm) {
       fromContentValues(context, alarm);
    }

    private AlarmClockItem(Parcel in)
    {
        rowID = in.readLong();
        type = AlarmType.valueOf(in.readString(), null);
        enabled = (in.readInt() == 1);
        label = in.readString();
        note = in.readString();

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
        actionID2 = in.readString();
        actionID3 = in.readString();

        setAlarmFlags(in.readString());

        modified = (in.readInt() == 1);
        state = in.readParcelable(AlarmClockItem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(rowID);
        out.writeString(type != null ? type.name() : null);
        out.writeInt(enabled ? 1 : 0);
        out.writeString(label);
        out.writeString(note);

        out.writeInt(repeating ? 1 : 0);
        out.writeString(getRepeatingDays());

        out.writeLong(alarmtime);
        out.writeLong(timestamp);
        out.writeInt(hour);
        out.writeInt(minute);
        out.writeLong(offset);

        out.writeString(location != null ? location.getLatitude() : null);
        out.writeString(location != null ? location.getLongitude() : null);
        out.writeString(location != null ? location.getLabel() : null);
        out.writeString(location != null ? location.getAltitude() : null);
        out.writeInt(location != null ? location.useAltitude() ? 1 : 0 : 0);

        //out.writeString(event != null ? event.name() : null);
        out.writeString(event);
        out.writeString(timezone);

        out.writeInt(vibrate ? 1 : 0);
        out.writeString(ringtoneName);
        out.writeString(ringtoneURI);
        out.writeString(actionID0);
        out.writeString(actionID1);
        out.writeString(actionID2);
        out.writeString(actionID3);

        out.writeString(getAlarmFlags());

        out.writeInt(modified ? 1 : 0);
        out.writeParcelable(state, 0);
    }

    public void fromContentValues(Context context, ContentValues alarm)
    {
        rowID = (alarm.containsKey(AlarmDatabaseAdapter.KEY_ROWID) ? alarm.getAsLong(AlarmDatabaseAdapter.KEY_ROWID) : -1L);
        type = AlarmType.valueOf(alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_TYPE), AlarmType.ALARM);
        enabled = alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_ENABLED) && (alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_ENABLED) == 1);
        label = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_LABEL);
        note = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_NOTE);

        repeating = alarm.containsKey(AlarmDatabaseAdapter.KEY_ALARM_REPEATING) && (alarm.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_REPEATING) == 1);
        String days = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_REPEATING_DAYS);
        if (days != null) {
            setRepeatingDays(days);
        } else this.repeatingDays = null;

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
        actionID2 = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_ACTION2);
        actionID3 = alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_ACTION3);

        setAlarmFlags(alarm.getAsString(AlarmDatabaseAdapter.KEY_ALARM_FLAGS));
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
        values.put(AlarmDatabaseAdapter.KEY_ALARM_ACTION2, actionID2);
        values.put(AlarmDatabaseAdapter.KEY_ALARM_ACTION3, actionID3);

        if (alarmFlags != null) {
            values.put(AlarmDatabaseAdapter.KEY_ALARM_FLAGS, getAlarmFlags());
        } else values.putNull(AlarmDatabaseAdapter.KEY_ALARM_FLAGS);

        if (note != null) {
            values.put(AlarmDatabaseAdapter.KEY_ALARM_NOTE, note);
        } else values.putNull(AlarmDatabaseAdapter.KEY_ALARM_NOTE);

        return values;
    }

    @Override
    public void setState(int value)
    {
        if (state == null)
            state = new AlarmState(rowID, value);
        else state.setState(value);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean value) {
        enabled = value;
    }

    @Override
    public void setLabel(String value) {
        this.label = value;
    }

    @Override
    public void setNote(String value) {
        note = value;
    }
    @Override
    public String getNote() {
        return note;
    }

    @Override
    public int getState()
    {
        return (state != null ? state.getState() : AlarmState.STATE_NONE);
    }

    @Override
    public Location getLocation() {
        return location;
    }
    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Nullable
    public String getEvent() {
        return event;
    }
    @Override
    public void setEvent(@Nullable String event) {
        this.event = event;
        eventItem = null;
    }

    @Override
    public long getOffset() {
        return offset;
    }
    @Override
    public void setOffset(long offsetMillis) {
        offset = offsetMillis;
    }

    @Override
    public int getHour() {
        return hour;
    }
    @Override
    public void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    public int getMinute() {
        return minute;
    }
    @Override
    public void setMinute(int minute) {
        this.minute = minute;
    }

    @Override
    public boolean isRepeating() {
        return repeating;
    }
    @Override
    public void setRepeating(boolean value) {
        repeating = value;
    }

    @Override
    public String getTimeZone() {
        return timezone;
    }
    @Override
    public void setTimeZone(String tzID) {
        timezone = tzID;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
    @Override
    public void setTimestamp(long value) {
        timestamp = value;
    }

    @Override
    public boolean getVibrate() {
        return vibrate;
    }
    @Override
    public void setVibrate(boolean value) {
        vibrate = value;
    }

    @Override
    public String getRingtoneURI() {
        return ringtoneURI;
    }
    @Override
    public void setRingtoneURI(String value) {
        ringtoneURI = value;
    }

    @Override
    public String getRingtoneName() {
        return ringtoneName;
    }
    @Override
    public void setRingtoneName(String value) {
        ringtoneName = value;
    }

    @Override
    public boolean isModified() {
        return modified;
    }
    @Override
    public void setModified(boolean value) {
        modified = value;
    }

    @Nullable
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
        if (this.type == null) {
            return ICON_NOTIFICATION;
        }
        switch (this.type) {
            case ALARM: return ICON_ALARM;
            case NOTIFICATION2: return ICON_NOTIFICATION2;
            case NOTIFICATION1: return ICON_NOTIFICATION1;
            case NOTIFICATION: default: return ICON_NOTIFICATION;
        }
    }

    public String getLabel(Context context)
    {
        if (type == null) {
            return context.getString(R.string.alarmMode_notification);
        }
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

    @Override
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

    @Override
    public boolean hasActionID(int actionNum)
    {
        String value = getActionID(actionNum);
        return (value != null && !value.trim().isEmpty());
    }
    @Override
    public String getActionID(int actionNum)
    {
        String value;
        switch (actionNum) {
            case ACTIONID_RESERVED: value = actionID3; break;
            case ACTIONID_REMINDER: value = actionID2; break;
            case ACTIONID_DISMISS: value = actionID1; break;
            case ACTIONID_MAIN: default: value = actionID0; break;
        }
        return (value != null ? value.trim() : null);
    }
    @Override
    public void setActionID(int actionNum, String actionID)
    {
        String value = (actionID != null  && !actionID.trim().isEmpty() ? actionID.trim() : null);
        switch (actionNum) {
            case ACTIONID_RESERVED: actionID3 = value; break;
            case ACTIONID_REMINDER: actionID2 = value; break;
            case ACTIONID_DISMISS: actionID1 = value; break;
            case ACTIONID_MAIN: default: actionID0 = value; break;
        }
    }

    /**
     * setAlarmFlags
     * @param flags as a String, e.g. "flag1=true;flag2=false;..."
     */
    @Override
    public void setAlarmFlags(String flags)
    {
        if (flags != null && !flags.trim().isEmpty())
        {
            if (alarmFlags == null) {
                alarmFlags = new HashMap<String, Long>();
            }
            alarmFlags.clear();
            parseAlarmFlags(alarmFlags, flags);

        } else {
            alarmFlags = null;
        }
    }
    public static void parseAlarmFlags(@NonNull HashMap<String,Long> out, String flags)
    {
        if (flags != null && !flags.trim().isEmpty())
        {
            String[] elements = flags.split(",");
            for (String flag : elements)
            {
                String[] parts = flag.split("=");
                if (parts.length == 2)
                {
                    if (parts[0] != null && isValidFlagName(parts[0]))
                    {
                        try {
                            out.put(parts[0].trim(), (parts[1] != null ? Long.parseLong(parts[1]) : 0L));

                        } catch (NumberFormatException e) {
                            Log.w("AlarmFlags", "setAlarmFlags: invalid flag value: " + e);
                        }
                    } else Log.w("AlarmFlags", "setAlarmFlags: invalid flag name; ignoring: '" + flag + "'");
                } else Log.w("AlarmFlags", "setAlarmFlags: wrong number of elements (" +  parts.length +"); ignoring: '" + flag + "'");
            }
        }
    }

    @Override
    public String getAlarmFlags()
    {
        if (alarmFlags != null)
        {
            StringBuilder result = new StringBuilder();
            for (String key : alarmFlags.keySet()) {
                result.append(key).append("=").append(alarmFlags.get(key)).append(",");
            }
            return ((result.toString().isEmpty()) ? null : result.toString().substring(0, result.length() - 1));
        } else return null;
    }

    public static boolean isValidFlagName(@Nullable String flagname)
    {
        if (flagname != null)
        {
            String[] forbidden = new String[] {"=", ",", ";", "true", "false"};
            for (String search : forbidden) {
                if (flagname.contains(search) || flagname.equalsIgnoreCase(search)) {
                    return false;
                }
            }
            return true;
        } else return false;
    }

    @Override
    public boolean hasFlag(@Nullable String flagname)
    {
        if (alarmFlags != null && flagname != null) {
            return alarmFlags.containsKey(flagname);
        } else return false;
    }
    @Override
    public long getFlag(@Nullable String flagname) {
        return getFlag(flagname, 0L);
    }
    @Override
    public long getFlag(@Nullable String flagname, long defaultValue)
    {
        if (alarmFlags != null && flagname != null) {
            Long value = alarmFlags.get(flagname);
            return (value != null) ? value : defaultValue;
        } else return defaultValue;
    }
    @Override
    public boolean flagIsTrue(@Nullable String flagname) {
        return (getFlag(flagname) != 0L);
    }

    public static boolean flagIsTrue(@NonNull HashMap<String,Long> map, @Nullable String flagname) {
        Long value = map.get(flagname);
        return (value != null && value != 0L);
    }
    @Override
    public boolean setFlag(@NonNull String flag, boolean value) {
        return setFlag(flag, (value ? 1L : 0L));
    }
    @Override
    public boolean setFlag(@NonNull String flag, long value)
    {
        if (isValidFlagName(flag))
        {
            if (alarmFlags == null) {
                alarmFlags = new HashMap<String, Long>();
            }
            alarmFlags.put(flag, value);
            return true;

        } else {
            Log.w("AlarmFlags", "setFlag: invalid flag name; ignoring " + flag);
            return false;
        }
    }

    @Override
    public AlarmType getType() {
        return type;
    }

    @Override
    public void setType(AlarmType type) {
        this.type = type;
    }

    @Override
    public boolean incrementFlag(@NonNull String flag) {
        return setFlag(flag, getFlag(flag) + 1);
    }
    @Override
    public boolean clearFlag(@Nullable String flag)
    {
        if (flag != null && alarmFlags != null && alarmFlags.containsKey(flag))
        {
            alarmFlags.remove(flag);
            return true;
        } else return false;
    }

    /**
     * repeatsEveryDay
     * @return true if repeatingDays contains every day (or is null)
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
        } else return true;
    }

    /**
     * getRepeatingDays
     * @return a stringlist representation of repeatingDays Array (e.g. "0,1,2,3");
     */
    @Override
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

    @Override
    public ArrayList<Integer> getRepeatingDaysArray() {
        return repeatingDays;
    }

    /**
     * getUri
     * @return e.g. content://com.forrestguice.suntimeswidget.alarmclock/alarms/[rowID]
     */
    public Uri getUri() {
        return getUri(rowID);
    }
    public static Uri getUri(long rowID) {
        return ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, rowID);
    }

    /**
     * setRepeatingDays
     * @param repeatingDaysString a stringlist representation of repeatingDays Array (e.g. "1,2,3,4,5,6,7"), or null (everyday)
     */
    public void setRepeatingDays(@Nullable String repeatingDaysString)
    {
        repeatingDays = new ArrayList<>();
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

            if (repeatingDaysArray != null) {
                repeatingDays.addAll(Arrays.asList(repeatingDaysArray));
            }
        }
    }

    public boolean hasDismissChallenge(Context context) {
        return (getDismissChallenge(context, false) != AlarmSettings.DismissChallenge.NONE);
    }
    public AlarmSettings.DismissChallenge getDismissChallenge(Context context) {
        return getDismissChallenge(context, false);
    }
    public AlarmSettings.DismissChallenge getDismissChallenge(Context context, boolean queryDisplayStrings)
    {
        AlarmSettings.DismissChallenge challenge = AlarmSettings.loadDismissChallengePref(context);
        if (hasFlag(AlarmClockItem.FLAG_DISMISS_CHALLENGE))
        {
            long value = getFlag(AlarmClockItem.FLAG_DISMISS_CHALLENGE, challenge.getID());
            challenge = AlarmSettings.DismissChallenge.valueOf((int)value, AlarmSettings.DismissChallenge.ADDON);

            if (challenge == AlarmSettings.DismissChallenge.ADDON)
            {
                AlarmSettings.DismissChallenge.ADDON.setID(value);   // temporary assignments; used to pass the ID and displayString to a previous point in the call stack (UI thread only! not thread safe..)
                if (queryDisplayStrings)
                {
                    List<AlarmAddon.DismissChallengeInfo> info = AlarmAddon.queryAlarmDismissChallenges(context, value);
                    if (info != null && info.size() > 0)
                    {
                        AlarmAddon.DismissChallengeInfo addonInfo = info.get(0);
                        if (addonInfo != null) {
                            AlarmSettings.DismissChallenge.ADDON.setDisplayString(addonInfo.getTitle());
                        }
                    }
                }
            }
        }
        return challenge;
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
