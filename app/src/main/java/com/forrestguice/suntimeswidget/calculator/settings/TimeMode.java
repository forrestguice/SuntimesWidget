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

import com.forrestguice.annotation.Nullable;

public enum TimeMode implements RiseSetDataMode
{
    OFFICIAL("Actual", "Actual Time", null),
    CIVIL("Civil", "Civil Twilight", -6d),
    NAUTICAL("Nautical", "Nautical Twilight", -12d),
    ASTRONOMICAL("Astronomical", "Astronomical Twilight", -18d),
    NOON("Noon", "Solar Noon", null),
    GOLD("Golden", "Golden Hour", 6d),
    BLUE8("Blue", "Blue Hour", -8d),      // 8 deg; morning start, evening end
    BLUE4("Blue", "Blue Hour", -4d),      // 4 deg; morning end, evening start
    MIDNIGHT("Midnight", "Solar Midnight", null);

    public static boolean shortDisplayStrings = false;
    private String longDisplayString;
    private String shortDisplayString;

    private TimeMode(String shortDisplayString, String longDisplayString, Double angle)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
        this.angle = angle;
    }

    public String toString()
    {
        if (shortDisplayStrings)
        {
            return shortDisplayString;

        } else {
            return longDisplayString;
        }
    }

    public String getShortDisplayString()
    {
        return shortDisplayString;
    }

    public String getLongDisplayString()
    {
        return longDisplayString;
    }

    public void setDisplayStrings(String shortDisplayString, String longDisplayString)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }

    private final Double angle;
    @Nullable
    public Double angle() {
        return angle;
    }

    @Nullable @Override
    public TimeMode getTimeMode() {
        return this;
    }
}
