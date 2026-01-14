/**
    Copyright (C) 2021-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

public enum EventType
{
    DATE,
    EVENTALIAS,
    SOLAREVENT,
    SUN_ELEVATION,
    SHADOWLENGTH,
    DAYPERCENT,
    MOONILLUM,
    MOON_ELEVATION;

    private EventType() {}

    private String displayString = name();
    @NonNull
    public String getDisplayString() {
        return displayString;
    }
    public void setDisplayString(@NonNull String value) {
        displayString = value;
    }
    @NonNull
    public String toString() {
        return displayString;
    }

    public String getSubtypeID(@Nullable String subtype) {
        return (subtype != null) ? name() + "_" + subtype
                : name();
    }

}
