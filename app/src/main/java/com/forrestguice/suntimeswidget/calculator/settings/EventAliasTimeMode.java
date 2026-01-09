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
import com.forrestguice.annotation.Nullable;

import com.forrestguice.suntimeswidget.events.EventAlias;

public class EventAliasTimeMode implements RiseSetDataMode
{
    private final EventAlias event;
    public EventAliasTimeMode(EventAlias event) {
        this.event = event;
    }

    @Nullable
    @Override
    public TimeMode getTimeMode() {
        return null;
    }

    public EventAlias getEvent() {
        return event;
    }

    @Override
    public String name() {
        return event.getID();
    }

    @NonNull
    @Override
    public String toString() {
        return event.getLabel();
    }
}
