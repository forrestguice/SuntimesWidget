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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.forrestguice.annotation.Nullable;

/**
 * AlarmState
 *                |----------------------------------------------|
 *                |                                              |
 *                |--> DISABLED <-->|          |<-----[S]        |
 *                                  |          |                 |
 *        |-- SCHEDULED_DISTANT <-->|<-----> NONE <---------|    |
 *        |                         |                       |    |
 *        |--- SCHEDULED_SOON <---->|      |---> DISMISSED -|--->|
 *        |                                |           |    |    |
 *        |------->----|---->--------------|       |-->|    |    |
 *                     |                   |       |        |    |
 *                     |----> SOUNDING ----+---> TIMEOUT ---|    |
 *                     |                   |                     |
 *                     |                   |                     |
 *                     |-<-- SNOOZING <----|-------------------->|
 *
 * Alarms start in the NONE state and immediately transition to DISABLED or SCHEDULED.
 * Alarms are SCHEDULED_SOON within X hrs of the scheduled time. A reminder notification is shown.
 * Alarms are SOUNDING when the notification is served (actively sounding).
 * Alarms are SNOOZING when the notification is snoozed. After the snooze period the alarm is once again SOUNDING.
 * Alarms are DISMISSED when the notification is dismissed. After an alarm is dismissed its state becomes NONE.
 * Alarms are TIMEOUT when the alarm sounds for more than X minutes without user intervention. Same as dismissed.
 */
public class AlarmState implements Parcelable
{
    public static final String TAG = "AlarmReceiverState";

    public static final int STATE_DISABLED = -1;          // alarm is disabled
    public static final int STATE_NONE = 0;               // no state; the state of this alarm tbd
    public static final int STATE_SCHEDULED_DISTANT = 1;  // alarm is scheduled (but not for anytime soon)
    public static final int STATE_SCHEDULED_SOON = 2;     // alarm is scheduled soon.. (within # upcoming hrs)
    public static final int STATE_SOUNDING = 3;           // alarm is sounding right now!
    public static final int STATE_DISMISSED = 4;          // alarm is dismissed by user (until next time)
    public static final int STATE_SNOOZING = 10;          // alarm is snoozed (will sound again soon)
    public static final int STATE_TIMEOUT = 20;           // alarm is dismissed by timeout (wasn't handled by user)

    public static final int[] VALUES = new int[] { STATE_DISABLED, STATE_NONE, STATE_SCHEDULED_DISTANT, STATE_SCHEDULED_SOON, STATE_SOUNDING, STATE_DISMISSED, STATE_SNOOZING, STATE_TIMEOUT };

    private long rowID = -1L;
    private int state = STATE_NONE;
    private boolean modified = false;

    public AlarmState(long id, int value)
    {
        this.rowID = id;
        this.state = value;
    }

    public AlarmState( AlarmState other )
    {
        this.rowID = other.rowID;
        this.state = other.state;
        this.modified = false;
    }

    public AlarmState(ContentValues values)
    {
        rowID = values.getAsLong(AlarmDatabaseAdapter.KEY_STATE_ALARMID);
        state = values.getAsInteger(AlarmDatabaseAdapter.KEY_STATE);
    }

    private AlarmState(Parcel in)
    {
        rowID = in.readLong();
        state = in.readInt();
        modified = (in.readInt() == 1);
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(rowID);
        out.writeInt(state);
        out.writeInt(modified ? 1 : 0);
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
                return (nextState == STATE_NONE || nextState == STATE_SOUNDING || nextState == STATE_SCHEDULED_SOON || nextState == STATE_DISMISSED || nextState == STATE_DISABLED || nextState == STATE_SCHEDULED_DISTANT);

            case STATE_SCHEDULED_SOON:
                return (nextState == STATE_NONE || nextState == STATE_SOUNDING || nextState == STATE_DISMISSED || nextState == STATE_DISABLED || nextState == STATE_SCHEDULED_SOON);

            case STATE_SOUNDING:
                return (nextState == STATE_NONE || nextState == STATE_DISMISSED || nextState == STATE_SNOOZING || nextState == STATE_TIMEOUT || nextState == STATE_DISABLED);

            case STATE_SNOOZING:
                return (nextState == STATE_NONE || nextState == STATE_DISMISSED || nextState == STATE_DISABLED || nextState == STATE_SOUNDING);

            case STATE_TIMEOUT:
                return (nextState == STATE_NONE || nextState == STATE_DISABLED || nextState == STATE_DISMISSED);

            case STATE_DISMISSED:
            case STATE_DISABLED:
                return (nextState == STATE_NONE || nextState == STATE_DISABLED);

            case STATE_NONE:
                return (nextState == STATE_NONE || nextState == STATE_SCHEDULED_DISTANT || nextState == STATE_SCHEDULED_SOON || nextState == STATE_DISABLED);

            default:
                Log.w(TAG, "validTransition: invalid state! " + currentState);
                return false;
        }
    }

    /**
     * transitionState
     */
    public static boolean transitionState(@Nullable AlarmState currentState, int nextState)
    {
        if (currentState == null) {
            Log.i(TAG, "Transitioned from null to " + nextState);
            return true;

        } else if (isValidTransition(currentState.state, nextState)) {
            Log.i(TAG, "Transitioned from " + currentState + " to " + nextState);
            currentState.state = nextState;
            currentState.modified = true;
            return true;
        }
        Log.e(TAG, "Unable to transition state! " + currentState + ", " + nextState);
        return false;
    }

    public String toString()
    {
        return "" + state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<AlarmState> CREATOR = new Parcelable.Creator<AlarmState>()
    {
        public AlarmState createFromParcel(Parcel in) {
            return new AlarmState(in);
        }

        public AlarmState[] newArray(int size) {
            return new AlarmState[size];
        }
    };

}
