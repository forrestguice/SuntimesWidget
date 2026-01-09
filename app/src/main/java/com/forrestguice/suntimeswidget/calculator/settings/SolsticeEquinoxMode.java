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

public enum SolsticeEquinoxMode
{
    CROSS_SPRING("Midpoint", "Spring cross-quarter"),
    EQUINOX_SPRING("Equinox", "Spring Equinox"),

    CROSS_SUMMER("Midpoint", "Summer cross-quarter"),
    SOLSTICE_SUMMER("Solstice", "Summer Solstice"),

    CROSS_AUTUMN("Midpoint", "Autumn cross-quarter"),
    EQUINOX_AUTUMNAL("Equinox", "Autumnal Equinox"),

    CROSS_WINTER("Midpoint", "Winter cross-quarter"),
    SOLSTICE_WINTER("Solstice", "Winter Solstice");

    public static boolean shortDisplayStrings = false;

    private String shortDisplayString;
    private String longDisplayString;

    private SolsticeEquinoxMode(@NonNull String shortDisplayString, @NonNull String longDisplayString)
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

    public static SolsticeEquinoxMode[] values(boolean southernHemisphere) {
        return (southernHemisphere) ? new SolsticeEquinoxMode[] { CROSS_AUTUMN, EQUINOX_AUTUMNAL, CROSS_WINTER, SOLSTICE_WINTER, CROSS_SPRING, EQUINOX_SPRING, CROSS_SUMMER, SOLSTICE_SUMMER } : values();
    }

    public static SolsticeEquinoxMode[] partialValues(boolean southernHemisphere) {
        return (southernHemisphere) ? new SolsticeEquinoxMode[] { EQUINOX_AUTUMNAL, SOLSTICE_WINTER, EQUINOX_SPRING, SOLSTICE_SUMMER }
                                    : new SolsticeEquinoxMode[] { EQUINOX_SPRING, SOLSTICE_SUMMER, EQUINOX_AUTUMNAL, SOLSTICE_WINTER };
    }
}
