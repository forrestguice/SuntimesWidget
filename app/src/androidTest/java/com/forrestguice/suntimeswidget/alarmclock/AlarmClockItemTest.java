package com.forrestguice.suntimeswidget.alarmclock;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem.AlarmType.ALARM;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AlarmClockItemTest
{
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
