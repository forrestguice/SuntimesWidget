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

package com.forrestguice.suntimeswidget.alarmclock;

import android.content.ContentValues;
import android.util.Log;

/**
 * >-- [trigger enabled / disabled] ----------\
 *                                            |             AlarmState
 *                     DISABLED <---\         |
 *                                  |         v
 *        /-- SCHEDULED_DISTANT <---|------- NONE ----------\
 *        |                         |                       |
 *        |--> SCHEDULED_SOON <-----|                       |
 *        |            |                                    |
 *        \----->------|-----------------------> DISMISSED -|
 *           ^         |                   |                |
 *           |         \----> SOUNDING ----+---> TIMEOUT ---/
 *           |                             |
 *           |                             |
 *           \-------------- SNOOZING <----/
 *
 * Alarms start in the NONE state and immediately transition to DISABLED or SCHEDULED_DISTANT (enabled).
 * Alarms are SCHEDULED_SOON within X hrs of the scheduled time.
 * Alarms are SOUNDING when the notification is served (actively sounding).
 * Alarms are SNOOZING when the notification is snoozed. After the snooze period the alarm is once again SOUNDING.
 * Alarms are DISMISSED when the notification is dismissed. After an alarm is dismissed its state becomes NONE.
 * Alarms are TIMEOUT when the alarm sounds for more than X minutes without user intervention. Same as dismissed.
 */
public class AlarmState
{
    public static final int STATE_DISABLED = -1;          // alarm is disabled
    public static final int STATE_NONE = 0;               // no state; the state of this alarm tbd
    public static final int STATE_SCHEDULED_DISTANT = 1;  // alarm is scheduled (but not for anytime soon)
    public static final int STATE_SCHEDULED_SOON = 2;     // alarm is scheduled soon.. (within # upcoming hrs)
    public static final int STATE_SOUNDING = 3;           // alarm is sounding right now!
    public static final int STATE_DISMISSED = 4;          // alarm is dismissed by user (until next time)
    public static final int STATE_SNOOZING = 10;          // alarm is snoozed (will sound again soon)
    public static final int STATE_TIMEOUT = 20;           // alarm is dismissed by timeout (wasn't handled by user)

    private long rowID = -1L;
    private int state = STATE_NONE;
    private boolean modified = false;

    public AlarmState() {}

    public AlarmState(ContentValues values)
    {
        rowID = values.getAsLong(AlarmDatabaseAdapter.KEY_STATE_ALARMID);
        state = values.getAsInteger(AlarmDatabaseAdapter.KEY_STATE);
    }

    public ContentValues asContentValues()
    {
        ContentValues values = new ContentValues();
        values.put(AlarmDatabaseAdapter.KEY_STATE_ALARMID, rowID);
        values.put(AlarmDatabaseAdapter.KEY_STATE, state);
        return values;
    }

    public long getAlarmID()
    {
        return rowID;
    }

    public int getState()
    {
        return state;
    }
    public void setState( int value )
    {
        state = value;
    }

    public boolean isModified()
    {
        return modified;
    }

    /**
     * @param currentState start state
     * @param nextState end state
     * @return true if transition is legal, false otherwise
     */
    public static boolean isValidTransition(int currentState, int nextState)
    {
        switch (currentState)
        {
            case STATE_SCHEDULED_DISTANT:
                return (nextState == STATE_SOUNDING || nextState == STATE_SCHEDULED_SOON || nextState == STATE_DISMISSED);

            case STATE_SCHEDULED_SOON:
                return (nextState == STATE_SOUNDING || nextState == STATE_DISMISSED);

            case STATE_SOUNDING:
                return (nextState == STATE_DISMISSED || nextState == STATE_SNOOZING || nextState == STATE_TIMEOUT);

            case STATE_SNOOZING:
                return (nextState == STATE_DISMISSED || nextState == STATE_SOUNDING);

            case STATE_TIMEOUT:
                return (nextState == STATE_NONE);

            case STATE_DISMISSED:
                return (nextState == STATE_NONE);

            case STATE_DISABLED:
                return (nextState == STATE_NONE);

            case STATE_NONE:
                return (nextState == STATE_SCHEDULED_DISTANT || nextState == STATE_SCHEDULED_SOON || nextState == STATE_DISABLED);

            default:
                Log.w("AlarmState", "validTransition: invalid state! " + currentState);
                return false;
        }
    }

    /**
     * transitionState
     * @param nextState
     * @return
     */
    public boolean transitionState(int nextState)
    {
        if (isValidTransition(state, nextState))
        {
            state = nextState;
            modified = true;
            return true;
        }
        return false;
    }
}
