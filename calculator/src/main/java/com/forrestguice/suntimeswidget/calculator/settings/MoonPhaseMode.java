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

import com.forrestguice.annotation.NonNull;

public enum MoonPhaseMode
{
    NEW_MOON("New", "New Moon"),
    FIRST_QUARTER("First Quarter", "First Quarter Moon"),
    FULL_MOON("Full", "Full Moon"),
    THIRD_QUARTER("Third Quarter", "Third Quarter Moon");

    private String shortDisplayString;
    private String longDisplayString;

    public static boolean shortDisplayStrings = false;

    private MoonPhaseMode(@NonNull String shortDisplayString, @NonNull String longDisplayString )
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }

    @NonNull
    public String toString() {
        if (shortDisplayStrings)
            return shortDisplayString;
        else return longDisplayString;
    }

    @NonNull
    public String getShortDisplayString() {
        return shortDisplayString;
    }

    @NonNull
    public String getLongDisplayString() {
        return longDisplayString;
    }

    public void setDisplayStrings(@NonNull String shortDisplayString, @NonNull String longDisplayString)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }
}
