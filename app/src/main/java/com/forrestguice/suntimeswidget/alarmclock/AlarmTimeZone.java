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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.util.Resources;

import java.util.TimeZone;

public enum AlarmTimeZone
{
    APPARENT_SOLAR_TIME(TimeZones.ApparentSolarTime.TIMEZONEID, TimeZones.ApparentSolarTime.TIMEZONEID),
    LOCAL_MEAN_TIME(TimeZones.LocalMeanTime.TIMEZONEID, TimeZones.LocalMeanTime.TIMEZONEID),
    SYSTEM_TIME("System Time Zone", null);

    private String displayString;
    private final String tzID;

    private AlarmTimeZone(@NonNull String displayString, @Nullable String tzID)
    {
        this.displayString = displayString;
        this.tzID = tzID;
    }

    @Nullable
    public String timeZoneID() {
        return tzID;
    }

    @NonNull
    public String toString() {
        return displayString;
    }

    @NonNull
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

    public static void initDisplayStrings( Resources context, ResID_AlarmTimeZone r )
    {
        SYSTEM_TIME.setDisplayString(context.getString(r.string_timezoneMode_current()));
        LOCAL_MEAN_TIME.setDisplayString(context.getString(r.string_time_localMean()));
        APPARENT_SOLAR_TIME.setDisplayString(context.getString(r.string_time_apparent()));
    }

    @NonNull
    public TimeZone getTimeZone(@Nullable Location location) {
        return AlarmTimeZone.getTimeZone(timeZoneID(), location);
    }

    @NonNull
    public static TimeZone getTimeZone(@Nullable String tzID, @Nullable Location location)
    {
        if (location == null || tzID == null) {
            return TimeZone.getDefault();

        } else if (tzID.equals(APPARENT_SOLAR_TIME.timeZoneID())) {
            return new TimeZones.ApparentSolarTime(location.getLongitudeAsDouble(), APPARENT_SOLAR_TIME.displayString());

        } else if (tzID.equals(LOCAL_MEAN_TIME.timeZoneID())) {
            return new TimeZones.LocalMeanTime(location.getLongitudeAsDouble(), LOCAL_MEAN_TIME.displayString());

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

    public interface ResID_AlarmTimeZone
    {
        int string_timezoneMode_current();
        int string_time_localMean();
        int string_time_apparent();
    }
}
