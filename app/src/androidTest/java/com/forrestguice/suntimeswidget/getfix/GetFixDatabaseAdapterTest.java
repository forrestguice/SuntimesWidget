/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.getfix;

import android.content.Context;

import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GetFixDatabaseAdapterTest
{
    private Context mockContext;
    private GetFixDatabaseAdapter db;
    private WidgetSettings.Location[] locations = new WidgetSettings.Location[] {
            new WidgetSettings.Location("Test Loc0", "35", "-112", "0"),
            new WidgetSettings.Location("Test Loc1", "36", "-111", "1"),
            new WidgetSettings.Location("Test's Loc2", "37", "-110", "2"),    // name contains '
            new WidgetSettings.Location("Test\"s Loc3", "38", "-109", "3"),   // name contains "
            new WidgetSettings.Location("Test`s Loc4", "38", "-109", "3")     // name contains `
    };

    @Before
    public void setup()
    {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
        db = new GetFixDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        db.clearPlaces();
        db.close();
    }

    @Test
    public void test_addPlace()
    {
        int count = 0;
        db.open();
        for (WidgetSettings.Location location : locations)
        {
            long id = db.addPlace(location);
            assertTrue("ID should be >= 0 (was " + id + ")", id != -1);
            count++;

            int n = db.getPlaceCount();
            assertTrue("database should contain " + count + " entry (contained " + n + ")", db.getPlaceCount() == count);
        }
        db.close();
    }

    protected long[] populateDatabase()
    {
        long[] rowID = new long[locations.length];
        for (int i=0; i<locations.length; i++)
        {
            rowID[i] = db.addPlace(locations[i]);
            assertTrue("ID should be >= 0 (was " + rowID[i] + ")", rowID[i] != -1);
        }
        assertTrue("database should be populated (isEmpty)", db.getPlaceCount() == locations.length);
        return rowID;
    }

    protected HashMap<Long, WidgetSettings.Location> mapDatabase(long[] rowID)
    {
        HashMap<Long, WidgetSettings.Location> result = new HashMap<>();
        for (int i=0; i<rowID.length; i++)
        {
            result.put(rowID[i], locations[i]);
        }
        return result;
    }

    @Test
    public void test_removePlace()
    {
        db.open();
        long[] rowID = populateDatabase();

        for (long id : rowID)              // remove items one at a time
        {
            assertTrue("removePlace should return true (returned false)", db.removePlace(id));
            assertTrue("removePlace should return false (already removed)", !db.removePlace(id));
        }
        int count = db.getPlaceCount();
        assertTrue("after test_removePlace db should be empty (has count of " + count + ")" , count == 0);

        boolean removed = db.removePlace(Long.MAX_VALUE);
        assertTrue("removePlace should return false (invalid ID & emptyDB)" , !removed);

        db.close();
    }

    @Test
    public void test_getPlaceByID()
    {
        db.open();

        long[] rowID = populateDatabase();
        for (int i=0; i<rowID.length; i++)           // get items by id
        {
            long id = rowID[i];
            Cursor cursor = db.getPlace(id);

            assertNotNull(cursor);
            assertTrue("cursor should be at first", cursor.getPosition() == 0);
            verifyPlace(cursor, true, id, locations[i]);
        }

        Cursor badCursor = db.getPlace(Long.MAX_VALUE);   // get invalid id (cursor should be empty)
        assertNotNull(badCursor);
        assertTrue("cursor should be empty", badCursor.getCount() == 0);

        db.close();
    }

    @Test
    public void test_getPlaceByName()
    {
        db.open();

        long[] rowID = populateDatabase();
        for (int i=0; i<locations.length; i++)     // get items by name
        {
            String placeName = locations[i].getLabel();
            Cursor cursor = db.getPlace(placeName, true);

            assertNotNull(cursor);
            assertTrue("cursor should be at first", cursor.getPosition() == 0);
            verifyPlace(cursor, true, rowID[i], locations[i]);
        }

        Cursor badCursor = db.getPlace("not in database", true);   // get invalid name (cursor should be empty)
        assertNotNull(badCursor);
        assertTrue("cursor should be empty", badCursor.getCount() == 0);

        db.close();
    }

    protected void verifyPlace(Cursor cursor, boolean fullEntry, long rowID, WidgetSettings.Location location)
    {
        // KEY_ROWID, KEY_PLACE_NAME, KEY_PLACE_LATITUDE, KEY_PLACE_LONGITUDE, KEY_PLACE_ALTITUDE, KEY_PLACE_COMMENT
        assertTrue("rowID should match", cursor.getLong(0) == rowID);
        assertTrue("label should match", cursor.getString(1).equals(location.getLabel()));
        if (fullEntry) {
            assertTrue("latitude should match", cursor.getString(2).equals(location.getLatitude()));
            assertTrue("longitude should match", cursor.getString(3).equals(location.getLongitude()));
            assertTrue("altitude should match", cursor.getString(4).equals(location.getAltitude()));
        }
    }

    @Test
    public void test_getAllPlaces()
    {
        db.open();
        long[] rowID = populateDatabase();
        HashMap<Long, WidgetSettings.Location> map = mapDatabase(rowID);

        // testing n=0, fullEntry=false
        Cursor cursor0 = db.getAllPlaces(0, false);
        assertTrue("cursor should be at first", cursor0.getPosition() == 0);
        assertTrue("cursor should have " + rowID.length + " entries (has " + cursor0.getCount() + ")", cursor0.getCount() == rowID.length);
        while (!cursor0.isAfterLast())
        {
            assertNotNull(cursor0);
            assertTrue("cursor should not be after last", !cursor0.isAfterLast());
            long id = cursor0.getLong(0);
            verifyPlace(cursor0, false, id, map.get(id));
            cursor0.moveToNext();
        }

        // testing n=0, fullEntry=true
        Cursor cursor1 = db.getAllPlaces(0, true);
        assertTrue("cursor should be at first", cursor1.getPosition() == 0);
        assertTrue("cursor should have " + rowID.length + " entries (has " + cursor1.getCount() + ")", cursor1.getCount() == rowID.length);
        while (!cursor1.isAfterLast())
        {
            assertNotNull(cursor1);
            assertTrue("cursor should not be after last", !cursor1.isAfterLast());
            long id = cursor1.getLong(0);
            verifyPlace(cursor1, true, id, map.get(id));
            cursor1.moveToNext();
        }

        // testing n=length-1, fullEntry=false
        Cursor cursor2 = db.getAllPlaces(rowID.length-1, false);   // all entries but last
        assertTrue("cursor should be at first", cursor2.getPosition() == 0);
        assertTrue("cursor should have " + (rowID.length-1) + " entries (has " + cursor2.getCount() + ")", cursor2.getCount() == rowID.length-1);
        while (!cursor2.isAfterLast())
        {
            assertNotNull(cursor2);
            assertTrue("cursor should not be after last", !cursor2.isAfterLast());
            long id = cursor2.getLong(0);
            verifyPlace(cursor2, false, id, map.get(id));
            cursor2.moveToNext();
        }

        db.close();
    }

    @Test
    public void test_clearPlaces()
    {
        db.open();
        populateDatabase();
        assertTrue("clearPlaces test requires a populated database (isEmpty)", db.getPlaceCount() > 0);

        assertTrue("clearPlaces should return true (was false)", db.clearPlaces());
        assertTrue("clearPlaces should return false (already cleared)", !db.clearPlaces());

        int c = db.getPlaceCount();
        assertTrue("after clearPlaces db should be empty (has count of " + c + ")" , c == 0);
        db.close();
    }

    @Test
    public void test_findPlaceByName()
    {
        db.open();
        long[] rowID = populateDatabase();

        Cursor cursor = db.getAllPlaces(0, false);
        for (int i=0; i<rowID.length; i++)
        {
            int position = GetFixDatabaseAdapter.findPlaceByName(locations[i].getLabel(), cursor);
            assertTrue("position should be >= 0 and < " + rowID.length + " (but was " + position + ")", position >= 0 && position < rowID.length);

            cursor.moveToPosition(position);
            verifyPlace(cursor, false, rowID[i], locations[i]);
        }

        int invalidPosition = GetFixDatabaseAdapter.findPlaceByName("not in database", cursor);
        assertTrue("position should be -1 not found (but was " + invalidPosition + ")", invalidPosition == -1);

        db.close();
    }

    @Test
    public void test_updatePlace()
    {
        db.open();
        long[] rowID = populateDatabase();
        for (int i=0; i<rowID.length; i++)
        {
            WidgetSettings.Location location = new WidgetSettings.Location(locations[i].getLabel(), "0", "0", "0");  // update all values to 0
            db.updatePlace(location);
            verifyPlace(db.getPlace(rowID[i]), true, rowID[i], location);
        }
        db.close();
    }
}
