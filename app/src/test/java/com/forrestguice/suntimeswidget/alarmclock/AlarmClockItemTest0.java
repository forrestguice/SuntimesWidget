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

import org.junit.Test;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem.AlarmType.ALARM;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class AlarmClockItemTest0
{
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
        item0.vibrate = true;
        item0.modified = true;
        test_alarmClockItem_new(item0);

        AlarmClockItem item1 = new AlarmClockItem();
        item1.type = AlarmClockItem.AlarmType.NOTIFICATION;
        test_alarmClockItem_new(item1);

        AlarmClockItem item2 = new AlarmClockItem();
        item2.repeatingDays = null;
        item2.type = null;

        AlarmClockItem item3 = new AlarmClockItem(item2);
        test_equals(item2, item3, true, true);
        //assertEquals(item2.type, item3.type);
    }

    public void test_alarmClockItem_new(AlarmClockItem item0)
    {
        AlarmClockItem item1 = new AlarmClockItem(item0);
        test_equals(item0, item1);
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
