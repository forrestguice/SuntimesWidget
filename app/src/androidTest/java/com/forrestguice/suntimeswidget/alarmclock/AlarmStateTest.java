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
import android.os.Parcel;

import com.forrestguice.util.SuntimesJUnitTestRunner;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(SuntimesJUnitTestRunner.class)
public class AlarmStateTest
{
    @Test
    public void test_AlarmState_parcelable()
    {
        int i = 1;
        for (int state : AlarmState.VALUES)
        {
            AlarmState item0 = new AlarmState(i, state);
            test_AlarmState_parcelable(item0);
            i++;
        }
    }
    public void test_AlarmState_parcelable(AlarmState item0)
    {
        Parcel parcel0 = Parcel.obtain();
        item0.writeToParcel(parcel0, 0);
        parcel0.setDataPosition(0);

        AlarmState item = (AlarmState) AlarmState.CREATOR.createFromParcel(parcel0);
        assertNotNull(item);
        test_equals(item0, item);
    }

    @Test
    public void test_AlarmState_contentValues()
    {
        int i = 1;
        for (int state : AlarmState.VALUES)
        {
            ContentValues values0 = new ContentValues();
            values0.put(AlarmDatabaseAdapter.KEY_STATE_ALARMID, i);
            values0.put(AlarmDatabaseAdapter.KEY_STATE, state);
            AlarmState state0 = new AlarmState(values0);
            ContentValues values1 = state0.asContentValues();
            test_equals(values0, values1);
            i++;
        }
    }

    public static void test_equals(ContentValues values0, ContentValues values)
    {
        assertEquals(values0.getAsLong(AlarmDatabaseAdapter.KEY_STATE_ALARMID), values.getAsLong(AlarmDatabaseAdapter.KEY_STATE_ALARMID));
        assertEquals(values0.getAsLong(AlarmDatabaseAdapter.KEY_STATE), values.getAsLong(AlarmDatabaseAdapter.KEY_STATE));
    }

    public static void test_equals(AlarmState item0, AlarmState item)
    {
        assertEquals(item0.getAlarmID(), item.getAlarmID());
        assertEquals(item0.getState(), item.getState());
        assertEquals(item0.isModified(), item.isModified());
    }

}
