/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.settings;

import com.forrestguice.suntimeswidget.calculator.TimeZones;

public enum SolarTimeMode         // TODO: misnomer (no longer accurate); rename this enum
{
    APPARENT_SOLAR_TIME(TimeZones.ApparentSolarTime.TIMEZONEID, "Apparent Solar Time"),
    LOCAL_MEAN_TIME(TimeZones.LocalMeanTime.TIMEZONEID, "Local Mean Time"),
    LMST(TimeZones.SiderealTime.TZID_LMST, "Local Sidereal Time"),
    GMST(TimeZones.SiderealTime.TZID_GMST, "Greenwich Sidereal Time"),
    UTC(TimeZones.TZID_UTC, "Coordinated Universal Time");

    private final String id;
    private String displayString;

    private SolarTimeMode(String id, String displayString)
    {
        this.id = id;
        this.displayString = displayString;
    }

    public String toString()
    {
        return displayString;
    }

    public String getID()
    {
        return id;
    }

    public String getDisplayString()
    {
        return displayString;
    }

    public void setDisplayString( String displayString )
    {
        this.displayString = displayString;
    }
}
