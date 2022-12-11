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
import android.os.Bundle;
import android.os.Parcel;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AlarmClockItemTest
{
    private Context context;

    @Before
    public void setup() {
        context = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
    }

    @Test
    public void test_AlarmClockItem_parcelable()
    {
        int i = 0;
        int[] states = AlarmState.VALUES;
        for (AlarmClockItem item0 : AlarmDatabaseAdapterTest.createTestItems()) {
            if (item0 != null)
            {
                item0.state = new AlarmState(item0.rowID, states[i % states.length]);
                test_AlarmClockItem_parcelable(item0);
                i++;
            }
        }
    }
    public void test_AlarmClockItem_parcelable(AlarmClockItem item0)
    {
        Parcel parcel0 = Parcel.obtain();
        item0.writeToParcel(parcel0, 0);
        parcel0.setDataPosition(0);

        AlarmClockItem item = (AlarmClockItem) AlarmClockItem.CREATOR.createFromParcel(parcel0);
        test_equals(item0, item);

        assertEquals(item0.type, item.type);
        assertEquals(item0.timestamp, item.timestamp);
        assertEquals(item0.modified, item.modified);
        assertEquals(item0.getUri(), item.getUri());
    }

    @Test
    public void test_alarmClockItem_new()
    {
        AlarmClockItem item0 = new AlarmClockItem();
        item0.type = AlarmClockItem.AlarmType.NOTIFICATION;
        item0.rowID = 0;
        item0.hour = 4;
        item0.minute = 2;
        item0.offset = 18 * 60;
        item0.enabled = true;
        item0.repeating = true;
        item0.setRepeatingDays("2,3,4,5,6");
        item0.vibrate = true;
        item0.modified = true;
        test_alarmClockItem_new(item0);

        AlarmClockItem item1 = new AlarmClockItem();
        item1.type = AlarmClockItem.AlarmType.NOTIFICATION;
        item1.setFlag("TEST1", 1);
        item1.setFlag("TEST2", 2);
        test_alarmClockItem_new(item1);

        AlarmClockItem item2 = new AlarmClockItem();
        item2.type = null;
        item2.repeatingDays = null;
        test_alarmClockItem_new(item2);
    }
    public void test_alarmClockItem_new(AlarmClockItem item0)
    {
        AlarmClockItem item1 = new AlarmClockItem(item0);
        test_equals(item0, item1);

        Bundle bundle = new Bundle();
        bundle.putParcelable("test_parcelable", item1);
        AlarmClockItem item2 = bundle.getParcelable("test_parcelable");
        assertNotNull(item2);
        test_equals(item1, item2);

        ContentValues values = item2.asContentValues(true);
        AlarmClockItem item3 = new AlarmClockItem();
        item3.fromContentValues(context, values);
        test_equals(item2, item3, false, true);
        assertEquals(item2.type != null ? item2.type : AlarmClockItem.AlarmType.ALARM, item3.type);
    }

    public static void test_equals(AlarmClockItem item0, AlarmClockItem item) {
        test_equals(item0, item, true, true);
    }
    public static void test_equals(AlarmClockItem item0, AlarmClockItem item, boolean withType, boolean withState)
    {
        assertNotNull(item);
        assertEquals(item0.rowID, item.rowID);
        assertEquals(item0.enabled, item.enabled);
        assertEquals(item0.label, item.label);
        assertEquals(item0.location, item.location);
        assertEquals(item0.timezone, item.timezone);

        assertEquals(item0.getEvent(), item.getEvent());
        assertEquals(item0.hour, item.hour);
        assertEquals(item0.minute, item.minute);
        assertEquals(item0.repeating, item.repeating);
        assertEquals(item0.getRepeatingDays(), item.getRepeatingDays());

        assertEquals(item0.offset, item.offset);
        assertEquals(item0.alarmtime, item.alarmtime);

        assertEquals(item0.ringtoneURI, item.ringtoneURI);
        assertEquals(item0.ringtoneName, item.ringtoneName);
        assertEquals(item0.vibrate, item.vibrate);
        assertEquals(item0.actionID1, item.actionID1);
        assertEquals(item0.actionID1, item.actionID1);
        if (withType) {
            assertEquals(item0.type, item.type);
        }
        if (withState) {
            assertEquals(item0.getState(), item.getState());
        }
        //assertEquals((item0.type != null ? item0.type : ALARM), item.type);
    }

}
