/**
    Copyright (C) 2026 Forrest Guice
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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.core.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public interface AlarmItemInterface
{
    String FLAG_REMINDER_WITHIN = "reminder";               // milliseconds
    String FLAG_DISMISS_CHALLENGE = "dismissChallenge";    // DismissChallenge enum ordinal (0 disabled)
    String FLAG_SNOOZE = "snoozeMillis";                         // milliseconds
    String FLAG_SNOOZE_LIMIT = "snoozeLimit";                    // 0; unlimited
    String FLAG_SNOOZE_COUNT = "snoozeCount";                    // [0, limit)
    String FLAG_LOCATION_FROM_APP = "locationFromApp";     // use app location

    String getAlarmFlags();
    void setAlarmFlags(String flags);
    boolean hasFlag(@Nullable String flagname);
    boolean flagIsTrue(String flag);
    long getFlag(@Nullable String flagname);
    long getFlag(@Nullable String flagname, long defaultValue);
    boolean setFlag(@NonNull String flag, boolean value);
    boolean setFlag(@NonNull String flag, long value);
    boolean incrementFlag(@NonNull String flag);
    boolean clearFlag(@Nullable String flag);

    AlarmType getType();
    void setType(AlarmType type);

    int getState();
    void setState(int state);

    boolean isEnabled();
    void setEnabled(boolean value);

    String getLabel(String emptyLabel);
    void setLabel(String value);

    void setNote(String value);
    String getNote();

    @Nullable
    String getEvent();
    void setEvent(@Nullable String event);

    long getOffset();
    void setOffset(long offsetMillis);

    int getHour();
    void setHour(int hour);

    int getMinute();
    void setMinute(int minute);

    Location getLocation();
    void setLocation(Location location);

    String getTimeZone();
    void setTimeZone(String tzID);

    long getTimestamp();
    void setTimestamp(long value);

    boolean getVibrate();
    void setVibrate(boolean value);

    String getRingtoneURI();
    void setRingtoneURI(String value);

    String getRingtoneName();
    void setRingtoneName(String value);

    boolean isModified();
    void setModified(boolean value);

    boolean isRepeating();
    void setRepeating(boolean value);

    String getRepeatingDays();
    void setRepeatingDays(@Nullable String repeatingDaysString);
    ArrayList<Integer> getRepeatingDaysArray();
    static ArrayList<Integer> everyday() {
        return new ArrayList<>(Arrays.asList(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY));
    }

    int ACTIONID_MAIN = 0;
    int ACTIONID_DISMISS = 1;
    int ACTIONID_REMINDER = 2;
    int ACTIONID_RESERVED = 3;

    boolean hasActionID(int actionNum);
    String getActionID(int actionNum);
    void setActionID(int actionNum, String actionID);
}
