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

public enum RiseSetOrder
{
    TODAY("Today"),
    LASTNEXT("Last / Next");

    private String displayString;

    private RiseSetOrder(@NonNull String displayString) {
        this.displayString = displayString;
    }

    @NonNull
    public String toString() {
        return displayString;
    }

    public void setDisplayString(@NonNull String displayString) {
        this.displayString = displayString;
    }
}
