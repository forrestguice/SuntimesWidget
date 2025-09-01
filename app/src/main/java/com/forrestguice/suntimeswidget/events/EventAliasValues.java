/**
    Copyright (C) 2022 Forrest Guice
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

import android.content.ContentValues;

public final class EventAliasValues
{
    public static EventAlias createEventAlias(ContentValues values)
    {
        return new EventAlias(EventType.valueOf(
                values.getAsString(EventSettings.PREF_KEY_EVENT_TYPE)),
                values.getAsString(EventSettings.PREF_KEY_EVENT_ID),
                values.getAsString(EventSettings.PREF_KEY_EVENT_LABEL),
                values.getAsInteger(EventSettings.PREF_KEY_EVENT_COLOR),
                values.getAsString(EventSettings.PREF_KEY_EVENT_URI),
                values.getAsBoolean(EventSettings.PREF_KEY_EVENT_SHOWN)
        );
    }

    public static ContentValues toContentValues(EventAlias alias)
    {
        ContentValues values = new ContentValues();
        values.put(EventSettings.PREF_KEY_EVENT_TYPE, alias.getType().name());
        values.put(EventSettings.PREF_KEY_EVENT_ID, alias.getID());
        values.put(EventSettings.PREF_KEY_EVENT_LABEL, alias.getLabel());
        values.put(EventSettings.PREF_KEY_EVENT_COLOR, alias.getColor());
        values.put(EventSettings.PREF_KEY_EVENT_URI, alias.getUri());
        values.put(EventSettings.PREF_KEY_EVENT_SHOWN, alias.isShown());
        return values;
    }
}
