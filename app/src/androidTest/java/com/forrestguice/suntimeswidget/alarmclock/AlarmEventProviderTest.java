/**
    Copyright (C) 2024-2025 Forrest Guice
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
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidEventSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.events.EventType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_NAME;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_REQUIRES_LOCATION;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_SUPPORTS_OFFSETDAYS;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_SUPPORTS_REPEATING;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_TYPE;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.COLUMN_EVENT_TYPE_LABEL;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_INFO;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_INFO_PROJECTION;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_TYPES;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.QUERY_EVENT_TYPES_PROJECTION;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AlarmEventProviderTest
{
    public Context context;

    @Before
    public void init() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void test_query_eventTypes()
    {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_EVENT_TYPES);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, QUERY_EVENT_TYPES_PROJECTION, null, null, null);
        assertNotNull(cursor);
        test_cursorHasColumns("QUERY_EVENT_TYPES", cursor, QUERY_EVENT_TYPES_PROJECTION);

        HashMap<String, String> types = new HashMap<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            types.put(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE)),
                      cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE_LABEL)));
            cursor.moveToNext();
        }
        cursor.close();

        for (EventType type : EventType.values())
        {
            if (type == EventType.SOLAREVENT) {
                for (int t : SolarEvents.types()) {
                    String k = EventType.SOLAREVENT.getSubtypeID(t + "");
                    assertTrue("eventTypes is missing " + k, types.containsKey(k));
                }
            } else {
                assertTrue("eventTypes is missing " + type.name(), types.containsKey(type.name()));
            }
        }
    }

    @Test
    public void test_query_eventInfo()
    {
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_EVENT_INFO);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, QUERY_EVENT_INFO_PROJECTION, null, null, null);
        assertNotNull(cursor);
        test_cursorHasColumns("QUERY_EVENT_INFO", cursor, QUERY_EVENT_INFO_PROJECTION);
        cursor.close();

        HashMap<String, String> types = new HashMap<>();
        HashMap<String, Integer> supportsRepeating = new HashMap<>();
        HashMap<String, String> requiresLocation = new HashMap<>();
        HashMap<String, String> supportsOffsetDays = new HashMap<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            types.put(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE)));
            supportsRepeating.put(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENT_SUPPORTS_REPEATING)));
            requiresLocation.put(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_REQUIRES_LOCATION)));
            supportsOffsetDays.put(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_SUPPORTS_OFFSETDAYS)));
            cursor.moveToNext();
        }
        cursor.close();

        for (SolarEvents event : SolarEvents.values(SolarEvents.TYPE_SUN)) {
            assertTrue("eventInfo does not include " + event.name(), types.containsKey(event.name()));
            String typeID = EventType.SOLAREVENT.getSubtypeID(SolarEvents.TYPE_SUN + "");
            assertEquals("wrong typeID!", typeID, types.get(event.name()));
            assertEquals(AlarmEventContract.REPEAT_SUPPORT_DAILY, (int) supportsRepeating.get(event.name()));
            assertEquals("true", requiresLocation.get(event.name()));
            assertEquals("false", supportsOffsetDays.get(event.name()));
        }
        for (SolarEvents event : SolarEvents.values(SolarEvents.TYPE_MOON)) {
            assertTrue("eventInfo does not include " + event.name(), types.containsKey(event.name()));
            String typeID = EventType.SOLAREVENT.getSubtypeID(SolarEvents.TYPE_MOON + "");
            assertEquals("wrong typeID!", typeID, types.get(event.name()));
            assertEquals(AlarmEventContract.REPEAT_SUPPORT_DAILY, (int) supportsRepeating.get(event.name()));
            assertEquals("true", requiresLocation.get(event.name()));
            assertEquals("false", supportsOffsetDays.get(event.name()));
        }
        for (SolarEvents event : SolarEvents.values(SolarEvents.TYPE_SEASON)) {
            assertTrue("eventInfo does not include " + event.name(), types.containsKey(event.name()));
            String typeID = EventType.SOLAREVENT.getSubtypeID(SolarEvents.TYPE_SEASON + "");
            assertEquals("wrong typeID!", typeID, types.get(event.name()));
            assertEquals(AlarmEventContract.REPEAT_SUPPORT_BASIC, (int) supportsRepeating.get(event.name()));
            assertEquals("true", requiresLocation.get(event.name()));
            assertEquals("true", supportsOffsetDays.get(event.name()));
        }
        for (SolarEvents event : SolarEvents.values(SolarEvents.TYPE_MOONPHASE)) {
            assertTrue("eventInfo does not include " + event.name(), types.containsKey(event.name()));
            String typeID = EventType.SOLAREVENT.getSubtypeID(SolarEvents.TYPE_MOONPHASE + "");
            assertEquals("wrong typeID!", typeID, types.get(event.name()));
            assertEquals(AlarmEventContract.REPEAT_SUPPORT_BASIC, (int) supportsRepeating.get(event.name()));
            assertEquals("false", requiresLocation.get(event.name()));
            assertEquals("true", supportsOffsetDays.get(event.name()));
        }
    }

    @Test
    public void test_EventType_resolveEventType()
    {
        String[] events = new String[] { "SUN_-6.0|5r", "SHADOW_1:1|5r", "123456789", "SUNSET" };
        EventType[] expected = new EventType[] {
                EventType.SUN_ELEVATION, EventType.SHADOWLENGTH, EventType.DAYPERCENT, EventType.MOONILLUM, EventType.DATE, EventType.SOLAREVENT };

        for (int i=0; i<events.length; i++) {
            assertEquals(expected[i], EventType.resolveEventType(AndroidEventSettings.wrap(context), events[i]));
        }
    }

    private void test_cursorHasColumns(@NonNull String tag, @Nullable Cursor cursor, @NonNull String[] projection)
    {
        assertTrue(tag + " should return non-null cursor.", cursor != null);
        assertTrue(tag + " should have same number of columns as the projection", cursor.getColumnCount() == projection.length);
        assertTrue(tag + " should return one or more rows.", cursor.getCount() >= 1);
        cursor.moveToFirst();
        for (String column : projection) {
            assertTrue(tag + " results should contain " + column, cursor.getColumnIndex(column) >= 0);
        }
    }

}
