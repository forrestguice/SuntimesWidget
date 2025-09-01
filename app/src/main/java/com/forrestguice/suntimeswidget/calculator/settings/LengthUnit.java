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

public enum LengthUnit
{
    METRIC("Metric"),
    IMPERIAL("Imperial");

    private LengthUnit(String displayString)
    {
        this.displayString = displayString;
    }

    private String displayString;
    public String getDisplayString()
    {
        return displayString;
    }
    public void setDisplayString(String value)
    {
        displayString = value;
    }
    public String toString()
    {
        return displayString;
    }

    public static double metersToFeet(double meters) {
        return 3.28084d * meters;
    }
    public static double feetToMeters(double feet) {
        return (feet * (1d / 3.28084d) );
    }

    public static double kilometersToMiles(double kilometers) {
        return 0.62137 * kilometers;
    }
}
