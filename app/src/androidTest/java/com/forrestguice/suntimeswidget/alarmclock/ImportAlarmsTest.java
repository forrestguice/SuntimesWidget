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
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.calculator.core.Location;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem.AlarmType.ALARM;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem.AlarmType.NOTIFICATION;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ImportAlarmsTest extends SuntimesActivityTestBase
{
    private Context mockContext;

    @Before
    public void setup() {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
    }

    @Test
    public void test_readAlarmClockItems0()
    {
        AlarmClockItem[] items0 = createTestItems();
        int n = items0.length;

        // to json array
        StringBuilder s = new StringBuilder("[");
        for (int i=0; i<n; i++) {
            s.append(AlarmClockItemImportTask.AlarmClockItemJson.toJson(items0[i]));
            if (i < (n-1)) {
                s.append(", ");
            }
        }
        s.append("]");

        // and back again
        InputStream in = new ByteArrayInputStream(s.toString().getBytes());
        ArrayList<AlarmClockItem> items = new ArrayList<>();
        try
        {
            AlarmClockItemImportTask.AlarmClockItemJson.readAlarmClockItems(mockContext, in, items);
            assertEquals(n, items.size());
            for (int i=0; i <items0.length; i++) {
                test_equals(items0[i], items.get(i));
            }

        } catch (IOException e) {
            fail("IOException! " + e);
        }
    }

    @Test
    public void test_readAlarmClockItems1()
    {
        AlarmClockItem[] items = createTestItems();
        AlarmClockItem item0 = items[0];
        String json0 = AlarmClockItemImportTask.AlarmClockItemJson.toJson(item0);
        AlarmClockItem item1 = items[1];
        String json1 = AlarmClockItemImportTask.AlarmClockItemJson.toJson(item1);

        test_import(json0, items[0]);                                                   // valid (single obj)
        test_import("[" + json0 + "]", items[0]);                             // valid (array; single obj)
        test_import("[" + json0 + ", " + json1 + "]", items[0], items[1]);    // valid (array; unique)
        test_import("[" + json1 + ", " + json1 + "]", items[1], items[1]);    // valid (array; duplicates)
        test_import("[[" + json0 + ", " + json1 + "]]", items[0], items[1]);  // valid (nested-array)
        test_import("[[" + json0 + ", " + json1 + "]]", items[0], items[1]);  // valid (nested-array)
        test_import("[,,[" + json0 + ", " + json1 + "]]", items[0], items[1]);
        test_import("[,[],[" + json0 + ", " + json1 + "]]", items[0], items[1]);

        test_import("[" + json0 + ", " + json1 + "]]", items[0], items[1]);   // invalid (nested-array; missing start bracket .. should read objects anyway)
        test_import("[[" + json0 + ", " + json1 + "]", items[0], items[1]);   // invalid (nested-array; missing end bracket .. should read objects anyway)
        test_import("[" + json0 + ", " + json1, items[0], items[1]);          // invalid (array; missing end bracket .. should read objects anyway)
        test_import(json0 + ", " + json1 + "]", items[0]);                    // invalid (array; missing start bracket .. should read first object only)

        test_import(json0.substring(0, json0.length()-1), items[0]);                    // invalid (single obj; missing end-bracket)
        test_import(json0.substring(1, json0.length()-1), null);               // invalid (single obj; missing brackets)
        test_import(json0 + ", " + json1, items[0]);                          // invalid (multiple objs outside array .. should read first object only)
        test_import(json0 + json1, items[0]);                                 // invalid (multiple objs outside array, missing separator .. should read first object only)
        test_import("[]", null);                                      // invalid (empty)
        test_import("\n\n\n\t\n\n", false, null);
        test_import("", false, null);
    }

    @Test
    public void test_readAlarmClockItems2()
    {
        AlarmClockItem[] items = createTestItems();
        AlarmClockItem item0 = items[0];
        ContentValues values0 = item0.asContentValues(true);

        // remove optional values (to defaults)
        values0.remove(AlarmDatabaseAdapter.KEY_ROWID);                   // -1L
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_TYPE);              // ALARM (default)
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_ENABLED);           // false
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_REPEATING);         // false
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_REPEATING_DAYS);    // null

        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_LABEL);             // null
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_PLACELABEL);        // null
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_ACTION0);           // null
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_ACTION1);           // null

        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_VIBRATE);           // false
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_NAME);     // null
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_RINGTONE_URI);      // null

        HashMap<String,String> map0 = AlarmClockItemImportTask.AlarmClockItemJson.toMap(values0);
        test_import(new JSONObject(map0).toString(), true);

        // remove additional values
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_SOLAREVENT);         // null
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_LATITUDE);           // null
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_LONGITUDE);          // null
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_ALTITUDE);           // nul
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_TIMEZONE);           // null

        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_OFFSET);    // 0
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_HOUR);      // -1
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_MINUTE);    // -1
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_DATETIME);
        values0.remove(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_ADJUSTED);

        HashMap<String,String> map1 = AlarmClockItemImportTask.AlarmClockItemJson.toMap(values0);
        test_import(new JSONObject(map1).toString(), true);
    }

    protected void test_import(String testString, boolean expected)
    {
        InputStream in = new ByteArrayInputStream(testString.getBytes());
        ArrayList<AlarmClockItem> items = new ArrayList<>();
        try {
            AlarmClockItemImportTask.AlarmClockItemJson.readAlarmClockItems(mockContext, in, items);
            if (!expected) {   // when !expected, the following line shouldn't be reached..
                fail("This test should have failed with an IOException..");
            }
        } catch (IOException e) {
            if (expected) {
                fail("IOException! " + e);
            }
        }
    }

    protected void test_import(String testString, AlarmClockItem... oracle) {
        test_import(testString, true, oracle);
    }
    protected void test_import(String testString, boolean expected, AlarmClockItem... oracle)
    {
        InputStream in = new ByteArrayInputStream(testString.getBytes());
        ArrayList<AlarmClockItem> items = new ArrayList<>();
        try {
            AlarmClockItemImportTask.AlarmClockItemJson.readAlarmClockItems(mockContext, in, items);
            assertEquals((oracle != null ? oracle.length : 0), items.size());
            if (oracle != null && expected) {
                for (int i = 0; i < oracle.length; i++) {
                    test_equals(items.get(i), oracle[i]);
                }
            }
            if (!expected) {   // when !expected, the following line shouldn't be reached..
                fail("This test should have failed with an IOException..");
            }
        } catch (IOException e) {
            if (expected) {
                fail("IOException! " + e);
            }
        }
    }

    protected AlarmClockItem[] createTestItems()
    {
        Location location0 = new Location(TESTLOC_0_LABEL, TESTLOC_0_LAT, TESTLOC_0_LON);
        Location location1 = new Location(TESTLOC_1_LABEL, TESTLOC_1_LAT, TESTLOC_1_LON, TESTLOC_1_ALT);

        String[] events = new String[] {"SUNRISE", "SUNSET", "CIVILRISE", null, "MOONRISE", "MOONSET"};
        boolean[] enabled = new boolean[] {true, false, false, true, true, false};
        boolean[] vibrate = new boolean[] {false, true, true, true, false, false};
        boolean[] repeating = new boolean[] {false, false, true, true, false, false};
        Location[] locations = new Location[] {location0, location0, location1, location1, location0, null};
        String[] timezones = new String[] {TESTTZID_0, TESTTZID_1, TESTTZID_2, TESTTZID_1, TESTTZID_2, null};
        String[] repeatDays = new String[] {"", "0,1", "0,1,2,3", "1,2", null, "0,1,2,3,4,5,6"};
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
            items[i].enabled = enabled[i];
        }
        return items;
    }

    protected void test_equals(AlarmClockItem item0, AlarmClockItem item)
    {
        assertNotNull(item);
        assertEquals(item0.rowID, item.rowID);
        assertEquals(item0.getEvent(), item.getEvent());
        assertEquals(item0.location, item.location);
        assertEquals(item0.timezone, item.timezone);
        assertEquals(item0.enabled, item.enabled);
        assertEquals(item0.vibrate, item.vibrate);
        assertEquals(item0.ringtoneName, item.ringtoneName);
        assertEquals(item0.ringtoneURI, item.ringtoneURI);
        assertEquals(item0.hour, item.hour);
        assertEquals(item0.minute, item.minute);
        assertEquals(item0.alarmtime, item.alarmtime);
        assertEquals(item0.offset, item.offset);
        assertEquals(item0.repeating, item.repeating);
        assertEquals(item0.getRepeatingDays(), item.getRepeatingDays());
        assertEquals(item0.label, item.label);
        assertEquals((item0.type != null ? item0.type : ALARM), item.type);
        assertEquals(item0.actionID0, item.actionID0);
        assertEquals(item0.actionID1, item.actionID1);
    }

}
