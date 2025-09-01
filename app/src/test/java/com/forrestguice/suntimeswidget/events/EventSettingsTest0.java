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

import android.graphics.Color;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class EventSettingsTest0
{
    @Test
    public void test_eventAlias()
    {
        EventType type0 = EventType.SUN_ELEVATION;
        String id0 = "TEST0";
        String label0 = "label0";
        String uri0 = "uri0";
        Integer color0 = Color.GREEN;

        EventAlias alias0 = new EventAlias(EventType.SUN_ELEVATION, id0, label0, color0, uri0, false);
        verify_eventAlias(type0, id0, label0, color0, uri0, alias0);

        EventAlias alias1 = new EventAlias(alias0);
        verify_eventAlias(type0, id0, label0, color0, uri0, alias1);

        EventAlias alias2 = new EventAlias(alias0.toContentValues());
        verify_eventAlias(type0, id0, label0, color0, uri0, alias2);
    }

    protected void verify_eventAlias(EventType type, String id, String label, Integer color, String uri, EventAlias alias)
    {
        assertEquals(type, alias.getType());
        assertEquals(id, alias.getID());
        assertEquals(label, alias.getLabel());
        assertEquals(color, alias.getColor());
        assertEquals(uri, alias.getUri());
    }
}
