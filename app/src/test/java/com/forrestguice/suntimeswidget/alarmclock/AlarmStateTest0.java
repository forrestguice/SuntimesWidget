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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class AlarmStateTest0
{
    @Test
    public void test_AlarmState_other()
    {
        int i = 1;
        for (int state : AlarmState.VALUES)
        {
            AlarmState state0 = new AlarmState(i, state);
            AlarmState state1 = new AlarmState(state0);
            test_equals(state0, state1);
            i++;
        }
    }

    @Test
    public void test_AlarmState_isModified()
    {
        AlarmState state0 = new AlarmState(1, AlarmState.STATE_SOUNDING);
        assertFalse(state0.isModified());

        assertTrue(AlarmState.isValidTransition(state0.getState(), AlarmState.STATE_SNOOZING));
        assertTrue(AlarmState.transitionState(state0, AlarmState.STATE_SNOOZING));
        assertTrue(state0.isModified());
    }

    public static void test_equals(AlarmState item0, AlarmState item)
    {
        assertEquals(item0.getAlarmID(), item.getAlarmID());
        assertEquals(item0.getState(), item.getState());
        assertEquals(item0.isModified(), item.isModified());
    }

}
