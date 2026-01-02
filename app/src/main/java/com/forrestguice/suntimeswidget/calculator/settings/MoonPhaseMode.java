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

public enum MoonPhaseMode
{
    NEW_MOON("New", "New Moon"),
    FIRST_QUARTER("First Quarter", "First Quarter Moon"),
    FULL_MOON("Full", "Full Moon"),
    THIRD_QUARTER("Third Quarter", "Third Quarter Moon");

    private String shortDisplayString;
    private String longDisplayString;

    public static boolean shortDisplayStrings = false;

    private MoonPhaseMode(String shortDisplayString, String longDisplayString )
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }

    public String toString()
    {
        if (shortDisplayStrings)
            return shortDisplayString;
        else return longDisplayString;
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
}
