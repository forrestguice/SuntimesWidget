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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import com.forrestguice.suntimeswidget.calculator.core.Location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Random;

import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTLOC_0_LABEL;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTLOC_0_LAT;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTLOC_0_LON;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTLOC_1_ALT;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTLOC_1_LABEL;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTLOC_1_LAT;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTLOC_1_LON;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTTZID_0;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTTZID_1;
import static com.forrestguice.suntimeswidget.SuntimesActivityTestBase.TESTTZID_2;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem.AlarmType.ALARM;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem.AlarmType.NOTIFICATION;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AlarmDatabaseAdapterTest
{
    private Context mockContext;
    private AlarmDatabaseAdapter db;
    private AlarmClockItem[] alarms = new AlarmClockItem[0];

    @Before
    public void setup()
    {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");

        db = new AlarmDatabaseAdapter(mockContext.getApplicationContext());
        db.open();
        db.clearAlarms();
        db.close();

        alarms = createTestItems();
    }

    @Test
    public void test_getAlarmByID()
    {
        db.open();

        long[] rowID = populateDatabase();
        for (int i=0; i<rowID.length; i++)           // get items by id
        {
            long id = rowID[i];
            if (id == -1L) {
                continue;
            }
            Cursor cursor = db.getAlarm(id);

            assertNotNull(cursor);
            assertTrue("cursor should be at first", cursor.getPosition() == 0);
            verifyAlarm(cursor, true, id, alarms[i].asContentValues(true));
        }

        Cursor badCursor = db.getAlarm(Long.MAX_VALUE);   // get invalid id (cursor should be empty)
        assertNotNull(badCursor);
        assertTrue("cursor should be empty", badCursor.getCount() == 0);
        db.close();
    }

    @Test
    public void test_getAllAlarms()
    {
        db.open();
        long[] rowID = populateDatabase();
        HashMap<Long, AlarmClockItem> map = mapDatabase(rowID);

        // testing n=0, fullEntry=false
        Cursor cursor0 = db.getAllAlarms(0, false);
        assertTrue("cursor should be at first", cursor0.getPosition() == 0);
        assertTrue("cursor should have " + map.size() + " entries (has " + cursor0.getCount() + ")", cursor0.getCount() == map.size());
        while (!cursor0.isAfterLast())
        {
            assertNotNull(cursor0);
            assertTrue("cursor should not be after last", !cursor0.isAfterLast());
            long id = cursor0.getLong(0);
            verifyAlarm(cursor0, false, id, map.get(id).asContentValues(true));
            cursor0.moveToNext();
        }

        // testing n=0, fullEntry=true
        Cursor cursor1 = db.getAllAlarms(0, true);
        assertTrue("cursor should be at first", cursor1.getPosition() == 0);
        assertTrue("cursor should have " + map.size() + " entries (has " + cursor1.getCount() + ")", cursor1.getCount() == map.size());
        while (!cursor1.isAfterLast())
        {
            assertNotNull(cursor1);
            assertTrue("cursor should not be after last", !cursor1.isAfterLast());
            long id = cursor1.getLong(0);
            verifyAlarm(cursor1, true, id, map.get(id).asContentValues(true));
            cursor1.moveToNext();
        }

        // testing n=length-1, fullEntry=false
        Cursor cursor2 = db.getAllAlarms(map.size()-1, false);   // all entries but last
        assertTrue("cursor should be at first", cursor2.getPosition() == 0);
        assertTrue("cursor should have " + (map.size()-1) + " entries (has " + cursor2.getCount() + ")", cursor2.getCount() == map.size()-1);
        while (!cursor2.isAfterLast())
        {
            assertNotNull(cursor2);
            assertTrue("cursor should not be after last", !cursor2.isAfterLast());
            long id = cursor2.getLong(0);
            verifyAlarm(cursor2, false, id, map.get(id).asContentValues(true));
            cursor2.moveToNext();
        }

        db.close();
    }

    @Test
    public void test_getAllAlarmsByState()
    {
        db.open();
        long[] rowID = populateDatabase();
        HashMap<Long, AlarmClockItem> map = mapDatabase(rowID);

        Cursor cursor0 = db.getAllAlarmsByState(0, AlarmState.STATE_TIMEOUT);       // expected result is 0; all items should be state NONE
        assertTrue("cursor should be at first", cursor0.getPosition() == 0);
        assertTrue("cursor should have 0 entries (has " + cursor0.getCount() + ")", cursor0.getCount() == 0);

        for (int i=0; i<rowID.length; i++) {
            if (rowID[i] == -1) {
                continue;
            }
            ContentValues alarmState = setAlarmState(rowID[i], AlarmState.STATE_TIMEOUT);    // sets all TIMEOUT
            verifyAlarmState(db.getAlarmState(rowID[i]), rowID[i], alarmState);
        }

        Cursor cursor1 = db.getAllAlarmsByState(0, AlarmState.STATE_SOUNDING, AlarmState.STATE_TIMEOUT);    // SOUNDING or TIMEOUT
        assertTrue("cursor should be at first", cursor1.getPosition() == 0);
        assertTrue("cursor should have " + map.size() + " entries (has " + cursor1.getCount() + ")", cursor1.getCount() == map.size());

        while (!cursor1.isAfterLast())
        {
            assertNotNull(cursor1);
            assertTrue("cursor should not be after last", !cursor1.isAfterLast());
            long id = cursor1.getLong(0);

            ContentValues alarmState = getAlarmStateValues(id, AlarmState.STATE_TIMEOUT);
            verifyAlarmState(cursor1, id, alarmState);
            cursor1.moveToNext();
        }

        db.close();
    }

    protected HashMap<Long, AlarmClockItem> mapDatabase(long[] rowID)
    {
        HashMap<Long, AlarmClockItem> result = new HashMap<>();
        for (int i=0; i<rowID.length; i++) {
            if (rowID[i] != -1L) {
                result.put(rowID[i], alarms[i]);
            }
        }
        return result;
    }

    @Test
    public void test_updateAlarmState()
    {
        db.open();
        long[] rowID = populateDatabase();
        for (int i=0; i<rowID.length; i++)
        {
            if (rowID[i] == -1) {
                continue;
            }
            ContentValues alarmState = setAlarmState(rowID[i], AlarmState.STATE_TIMEOUT);
            verifyAlarmState(db.getAlarmState(rowID[i]), rowID[i], alarmState);
        }
        db.close();
    }

    private ContentValues setAlarmState(long rowID, int state)
    {
        ContentValues alarmState = getAlarmStateValues(rowID, state);
        db.updateAlarmState(rowID, alarmState);
        return alarmState;
    }

    public static ContentValues getAlarmStateValues(long rowID, int state)
    {
        ContentValues alarmState = new ContentValues();
        alarmState.put(AlarmDatabaseAdapter.KEY_STATE, state);
        alarmState.put(AlarmDatabaseAdapter.KEY_STATE_ALARMID, rowID);
        return alarmState;
    }

    @Test
    public void test_updateAlarm()
    {
        db.open();
        long[] rowID = populateDatabase();
        for (int i=0; i<rowID.length; i++)
        {
            if (rowID[i] == -1) {
                continue;
            }

            ContentValues values;
            Random random = new Random();    // randomly swap values with another (valid) alarm
            do {
                values = alarms[random.nextInt(alarms.length)].asContentValues(false);
            } while (values.get(AlarmDatabaseAdapter.KEY_ALARM_TYPE) == null);

            db.updateAlarm(rowID[i], values);
            verifyAlarm(db.getAlarm(rowID[i]), true, rowID[i], values);
        }
        db.close();
    }

    protected void verifyAlarm(Cursor cursor, boolean fullEntry, long rowID, ContentValues values)
    {
        // KEY_ROWID
        assertTrue("rowID should match", cursor.getLong(0) == rowID);
        assertTrue("type should match", cursor.getString(1).equals(values.getAsString(AlarmDatabaseAdapter.KEY_ALARM_TYPE)));
        assertTrue("enabled should match", cursor.getInt(2) == values.getAsInteger(AlarmDatabaseAdapter.KEY_ALARM_ENABLED));
        // TODO: more columns
    }

    public static void verifyAlarmState(Cursor cursor, long rowID, ContentValues values)
    {
        assertTrue("alarmID should match", cursor.getInt(0) == rowID);
        assertTrue("alarmID should match", cursor.getInt(0) == values.getAsInteger(AlarmDatabaseAdapter.KEY_STATE_ALARMID));
        assertTrue("state should match", cursor.getInt(1) == values.getAsInteger(AlarmDatabaseAdapter.KEY_STATE));
    }

    public static void verifyAlarmState(Cursor cursor, long rowId, int state)
    {
        assertEquals("alarmID should match", rowId, cursor.getInt(0));
        assertEquals("state should match", state, cursor.getInt(1));
    }

    @Test
    public void test_addAlarm()
    {
        int count = 0;
        db.open();
        for (AlarmClockItem alarm : alarms)
        {
            if (alarm.type == null) {
                continue;
            }
            long id = db.addAlarm(alarm.asContentValues(false));
            assertTrue("ID should be >= 0 (was " + id + ")", id != -1);
            count++;

            int n = db.getAlarmCount();
            assertTrue("database should contain " + count + " entry (contained " + n + ")", db.getAlarmCount() == count);
        }
        db.close();
    }

    @Test
    public void test_removeAlarm()
    {
        db.open();
        long[] rowID = populateDatabase();

        for (long id : rowID)              // remove items one at a time
        {
            if (id == -1) {
                continue;
            }
            assertTrue("removeAlarm should return true (returned false)", db.removeAlarm(id));
            assertTrue("removeAlarm should return false (already removed)", !db.removeAlarm(id));
        }
        int count = db.getAlarmCount();
        assertTrue("after test_removeAlarm db should be empty (has count of " + count + ")" , count == 0);

        boolean removed = db.removeAlarm(Long.MAX_VALUE);
        assertTrue("removeAlarm should return false (invalid ID & emptyDB)" , !removed);

        db.close();
    }

    @Test
    public void test_clearAlarms()
    {
        db.open();
        populateDatabase();
        assertTrue("clearAlarms test requires a populated database (isEmpty)", db.getAlarmCount() > 0);

        assertTrue("clearAlarms should return true (was false)", db.clearAlarms());
        assertTrue("clearAlarms should return false (already cleared)", !db.clearAlarms());

        int c = db.getAlarmCount();
        assertTrue("after clearAlarms db should be empty (has count of " + c + ")" , c == 0);
        db.close();
    }

    protected long[] populateDatabase()
    {
        int c = 0;
        long[] rowID = new long[alarms.length];
        for (int i=0; i<alarms.length; i++)
        {
            ContentValues values = alarms[i].asContentValues(false);
            rowID[i] = db.addAlarm(values);

            if (values.get(AlarmDatabaseAdapter.KEY_ALARM_TYPE) != null) {                          // skip testing items with null type - these are invalid..
                assertTrue("ID should be >= 0 (was " + rowID[i] + ")", rowID[i] != -1);
                c++;
            } else {
                assertTrue("ID should be -1 (invalid type)", rowID[i] == -1);
            }
        }
        assertTrue("database should be populated (isEmpty)", db.getAlarmCount() == c);
        return rowID;
    }

    public static AlarmClockItem[] createTestItems()
    {
        Location location0 = new Location(TESTLOC_0_LABEL, TESTLOC_0_LAT, TESTLOC_0_LON);
        Location location1 = new Location(TESTLOC_1_LABEL, TESTLOC_1_LAT, TESTLOC_1_LON, TESTLOC_1_ALT);

        String[] events = new String[] {"SUNRISE", "SUNSET", "MORNING_CIVIL", null, "MOONRISE", "MOONSET"};
        boolean[] enabled = new boolean[] {true, false, false, true, true, false};
        boolean[] vibrate = new boolean[] {false, true, true, true, false, false};
        boolean[] repeating = new boolean[] {false, false, true, true, false, false};
        Location[] locations = new Location[] {location0, location0, location1, location1, location0, null};
        String[] timezones = new String[] {TESTTZID_0, TESTTZID_1, TESTTZID_2, TESTTZID_1, TESTTZID_2, null};
        String[] repeatDays = new String[] {"", "1", "0,1,2,3", "1,2", null, "1,2,3,4,5,6"};   // 0 is invalid value
        String[] flags = new String[] {"", "flag1=true", "flag1=true,flag2=false,flag3=true", "flag1=true,flag2=burrito", "flag3=false", null};
        AlarmClockItem.AlarmType[] types = new AlarmClockItem.AlarmType[] { ALARM, ALARM, NOTIFICATION, null, ALARM, ALARM };
        int[] hours = new int[] {6, 18, 5, 19, 12, 6};
        int[] minutes = new int[] {30, 10, 0, 1, 59, 6};

        int n = events.length;
        AlarmClockItem[] items = new AlarmClockItem[n];
        for (int i=0; i<n; i++)
        {
            items[i] = new AlarmClockItem();
            items[i].rowID = i;
            items[i].label = "TEST" + i;
            items[i].note = "NOTE" + i;
            items[i].type = types[i];
            items[i].setEvent(events[i]);
            items[i].location = locations[i];
            items[i].timezone = timezones[i];
            items[i].repeating = repeating[i];
            items[i].hour = hours[i];
            items[i].minute = minutes[i];
            items[i].setRepeatingDays(repeatDays[i]);
            items[i].alarmtime = i;
            items[i].offset = i;
            items[i].vibrate = vibrate[i];
            items[i].ringtoneName = "TEST_RING" + i;
            items[i].ringtoneURI = "content://TEST_RING" + i;
            items[i].actionID0 = null;
            items[i].actionID1 = "TEST_ACTION" + i;
            items[i].actionID2 = "TEST_ACTION" + i;
            items[i].actionID3 = "TEST_ACTION" + i;
            items[i].setAlarmFlags(flags[i]);
            items[i].enabled = enabled[i];
        }
        return items;
    }
}
