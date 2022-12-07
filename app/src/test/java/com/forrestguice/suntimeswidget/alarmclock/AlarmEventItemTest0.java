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

import android.content.ContentResolver;
import android.content.Context;

import com.forrestguice.suntimeswidget.settings.SolarEvents;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class AlarmEventItemTest0
{
    @Test
    public void test_AlarmEventItem_new()
    {
        for (SolarEvents event : SolarEvents.values()) {
            test_AlarmEventItem_new(event);
        }
    }

    public void test_AlarmEventItem_new(SolarEvents event)
    {
        AlarmEvent.AlarmEventItem item0 = new AlarmEvent.AlarmEventItem(event);
        assertTrue(item0.isResolved());
        assertEquals(item0.getEvent(), event);
        assertEquals(item0.getEventID(), event.name());
        assertEquals(item0.getTitle(), event.getLongDisplayString());
        assertEquals(item0.getTitle(), item0.toString());
        assertNotNull(item0.getSummary());
        assertTrue(item0.getSummary().isEmpty());
        assertNull(item0.phrase);
        assertEquals(item0.getIcon(null), event.getIcon());
        assertNull(item0.getUri());

        AlarmEvent.AlarmEventItem item1 = new AlarmEvent.AlarmEventItem(AlarmEventContract.AUTHORITY, "test", (ContentResolver)null);
        assertNull(item1.getEvent());
        assertNotNull(item1.getUri());
        assertTrue(item1.getUri().endsWith("test"));
        assertEquals(AlarmAddon.getEventInfoUri(AlarmEventContract.AUTHORITY, "test"), item1.getUri());

        assertFalse(item1.isResolved());           // due to null ContentResolver
        assertTrue(item1.getTitle().isEmpty());
        assertNull(item1.getSummary());
        assertNull(item1.phrase);
        assertEquals(item1.getUri(), item1.getEventID());
    }

    @Test
    public void test_AlarmEventItem_new_nullContentResolver()
    {
        String[] events0 = new String[] { "test1", "test2", "test3" };
        for (String eventID : events0) {
            test_AlarmEventItem_new(AlarmEventContract.AUTHORITY, eventID);
        }

        String[] events1 = new String[] { SolarEvents.SUNRISE.name(), SolarEvents.NOON.name(), SolarEvents.SUNSET.name() };
        for (String eventID : events1) {
            test_AlarmEventItem_new(eventID);
        }
    }

    public void test_AlarmEventItem_new(String authority, String eventID)
    {
        AlarmEvent.AlarmEventItem item1 = new AlarmEvent.AlarmEventItem(authority, eventID, (ContentResolver)null);
        assertNull(item1.getEvent());
        assertNotNull(item1.getUri());
        assertTrue(item1.getUri().endsWith(eventID));
        assertEquals(AlarmAddon.getEventInfoUri(authority, eventID), item1.getUri());

        assertFalse(item1.isResolved());           // due to null ContentResolver
        assertTrue(item1.getTitle().isEmpty());
        assertNull(item1.getSummary());
        assertNull(item1.phrase);
        assertEquals(item1.getUri(), item1.getEventID());
    }

    public void test_AlarmEventItem_new(String enumName)
    {
        AlarmEvent.AlarmEventItem item1 = new AlarmEvent.AlarmEventItem(enumName, (ContentResolver)null);
        assertFalse(item1.isResolved());    // due to null ContentResolver

        SolarEvents event1 = SolarEvents.valueOf(enumName);
        assertNotNull(item1.getEvent());
        assertEquals(enumName, item1.getEvent().name());
        assertEquals(event1, item1.getEvent());
        assertNull(item1.getUri());

        assertEquals(item1.getTitle(), event1.getLongDisplayString());
        assertEquals(item1.getTitle(), item1.toString());
        assertNotNull(item1.getSummary());
        assertTrue(item1.getSummary().isEmpty());
        assertNull(item1.phrase);
    }

    @Test
    public void test_AlarmEventPhrase_new()
    {
        AlarmEvent.AlarmEventPhrase phrase0 = new AlarmEvent.AlarmEventPhrase("noun");                                 // noun
        assertEquals("noun", phrase0.getNoun());
        assertEquals("other", phrase0.getGender());
        assertEquals(1, phrase0.getQuantity());

        AlarmEvent.AlarmEventPhrase phrase1 = new AlarmEvent.AlarmEventPhrase("noun", "gender", 2);    // noun, gender, quantity
        assertEquals("noun", phrase1.getNoun());
        assertEquals("gender", phrase1.getGender());
        assertEquals(2, phrase1.getQuantity());

        AlarmEvent.AlarmEventPhrase phrase2 = new AlarmEvent.AlarmEventPhrase("noun", null, 3);        // null gender, positive quantity
        assertEquals("noun", phrase2.getNoun());
        assertEquals("other", phrase2.getGender());
        assertEquals(3, phrase2.getQuantity());

        AlarmEvent.AlarmEventPhrase phrase3 = new AlarmEvent.AlarmEventPhrase("noun", null, -1);       // null gender, negative quantity
        assertEquals("noun", phrase3.getNoun());
        assertEquals("other", phrase3.getGender());
        assertEquals(1, phrase3.getQuantity());

        AlarmEvent.AlarmEventPhrase phrase4 = new AlarmEvent.AlarmEventPhrase("noun", "gender", -1);   // negative quantity
        assertEquals("noun", phrase4.getNoun());
        assertEquals("gender", phrase4.getGender());
        assertEquals(-1, phrase4.getQuantity());
    }

    @Test
    public void test_isValidEventID()
    {
        for (SolarEvents event : SolarEvents.values()) {
            assertTrue(AlarmEvent.isValidEventID((Context)null, event.name()));   // expects NPE if test fails here (isValidEventID will pass a null context to AlarmAddon.checkUriPermission)
        }
        assertTrue(AlarmEvent.isValidEventID((Context)null, null));   // a null eventID is also valid; expects NPE if test fails here
    }

}
