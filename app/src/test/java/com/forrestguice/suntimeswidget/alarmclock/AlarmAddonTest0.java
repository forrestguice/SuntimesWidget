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

package com.forrestguice.suntimeswidget.alarmclock;

import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class AlarmAddonTest0
{
    @Test
    public void test_AlarmAddon_getEventInfoUri()
    {
        assertEquals("suntimeswidget.event.provider", AlarmEventContract.AUTHORITY);
        assertEquals("eventInfo", AlarmEventContract.QUERY_EVENT_INFO);
        for (SolarEvents event : SolarEvents.values()) {
            test_AlarmAddon_getEventInfoUri(AlarmEventContract.AUTHORITY, AlarmEventContract.QUERY_EVENT_INFO, event.name());
        }
    }

    @Test
    public void test_AlarmAddon_getEventCalcUri()
    {
        assertEquals("suntimeswidget.event.provider", AlarmEventContract.AUTHORITY);
        assertEquals("eventCalc", AlarmEventContract.QUERY_EVENT_CALC);
        for (SolarEvents event : SolarEvents.values()) {
            test_AlarmAddon_getEventCalcUri(AlarmEventContract.AUTHORITY, AlarmEventContract.QUERY_EVENT_CALC, event.name());
        }
    }

    public void test_AlarmAddon_getEventInfoUri(String authority, String query, String eventID)
    {
        String infoUri1 = EventUri.getEventInfoUri(authority, eventID);
        assertTrue(infoUri1.startsWith("content://"));
        assertTrue(infoUri1.endsWith(eventID));

        String infoUri0 = "content://" + authority + "/" + query + "/" + eventID;
        assertEquals(infoUri0, infoUri1);
    }

    public void test_AlarmAddon_getEventCalcUri(String authority, String query, String eventID)
    {
        String calcUri1 = EventUri.getEventCalcUri(authority, eventID);
        assertTrue(calcUri1.startsWith("content://"));
        assertTrue(calcUri1.endsWith(eventID));

        String calcUri0 = "content://" + authority + "/" + query + "/" + eventID;
        assertEquals(calcUri0, calcUri1);
    }
}
