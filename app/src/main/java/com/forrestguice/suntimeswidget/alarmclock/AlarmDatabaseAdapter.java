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
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

public class AlarmDatabaseAdapter
{
    public static final String DATABASE_NAME = "suntimesAlarms";
    public static final int DATABASE_VERSION = 1;

    //
    // Table: Alarms
    //
    public static final String KEY_ROWID = "_id";                                                   // row ID
    public static final String DEF_ROWID = KEY_ROWID + " integer primary key autoincrement";

    public static final String KEY_ALARM_TYPE = "alarmType";                                        // type (ALARM, NOTIFICATION)
    public static final String DEF_ALARM_TYPE = KEY_ALARM_TYPE + " text not null";

    public static final String KEY_ALARM_ENABLED = "enabled";                                       // enabled flag (0: false, 1: true)
    public static final String DEF_ALARM_ENABLED = KEY_ALARM_ENABLED + " integer default 0";

    public static final String KEY_ALARM_REPEATING = "repeating";                                   // repeating flag (0: false, 1: true)
    public static final String DEF_ALARM_REPEATING = KEY_ALARM_REPEATING + " integer default 0";

    public static final String KEY_ALARM_REPEATING_DAYS = "repeatdays";                             // repeating days (a list as String; e.g. [Calendar.SUNDAY, Calendar.Monday, ...])
    public static final String DEF_ALARM_REPEATING_DAYS = KEY_ALARM_REPEATING_DAYS + " text";

    public static final String KEY_ALARM_DATETIME = "datetime";                                     // timestamp, the (original) datetime this alarm should sound; one-time alarm if SOLAREVENT is null; repeating-alarm if SOLAREVENT is provided (DATETIME recalculated on repeat).
    public static final String DEF_ALARM_DATETIME = KEY_ALARM_DATETIME + " integer default -1";

    public static final String KEY_ALARM_DATETIME_ADJUSTED = "alarmtime";                           // timestamp, the (adjusted) datetime this alarm should sound; one-time alarm if SOLAREVENT is null; repeating-alarm if SOLAREVENT is provided (DATETIME recalculated on repeat).
    public static final String DEF_ALARM_DATETIME_ADJUSTED = KEY_ALARM_DATETIME_ADJUSTED + " integer default -1";

    public static final String KEY_ALARM_DATETIME_HOUR = "hour";                                    // hour [0,23] (optional); the hour may be used to calculate the alarm datetime
    public static final String DEF_ALARM_DATETIME_HOUR = KEY_ALARM_DATETIME_HOUR + " integer default -1";

    public static final String KEY_ALARM_DATETIME_MINUTE = "minute";                                // minute [0,59] (optional); the minute may be used to calculate the alarm datetime
    public static final String DEF_ALARM_DATETIME_MINUTE = KEY_ALARM_DATETIME_MINUTE + " integer default -1";

    public static final String KEY_ALARM_DATETIME_OFFSET = "timeoffset";                            // timestamp offset; alarm is triggered at some offset (+)before or (-)after timestamp.
    public static final String DEF_ALARM_DATETIME_OFFSET = KEY_ALARM_DATETIME_OFFSET + " integer default 0";

    public static final String KEY_ALARM_LABEL = "alarmlabel";                                      // alarm label (optional)
    public static final String DEF_ALARM_LABEL = KEY_ALARM_LABEL + " text";

    public static final String KEY_ALARM_SOLAREVENT = "event";                                      // SolarEvent ENUM (optional), the ALARM_DATETIME may be (re)calculated using this value
    public static final String DEF_ALARM_SOLAREVENT = KEY_ALARM_SOLAREVENT + " text";

    public static final String KEY_ALARM_PLACELABEL = "place";                                      // place label (optional), the ALARM_LABEL may include this value
    public static final String DEF_ALARM_PLACELABEL = KEY_ALARM_PLACELABEL + " text";

    public static final String KEY_ALARM_LATITUDE = "latitude";                                     // latitude (dd) (optional), the ALARM_DATETIME may be (re)calculated using this value
    public static final String DEF_ALARM_LATITUDE = KEY_ALARM_LATITUDE + " text";

    public static final String KEY_ALARM_LONGITUDE = "longitude";                                   // longitude (dd) (optional), the ALARM_DATETIME may be (re)calculated from this value
    public static final String DEF_ALARM_LONGITUDE = KEY_ALARM_LONGITUDE + " text";

    public static final String KEY_ALARM_ALTITUDE = "altitude";                                     // altitude (meters) (optional), the ALARM_DATETIME may be (re)calculated from this value
    public static final String DEF_ALARM_ALTITUDE = KEY_ALARM_ALTITUDE + " text";

    public static final String KEY_ALARM_VIBRATE = "vibrate";                                       // vibrate flag (0: false, 1: true)
    public static final String DEF_ALARM_VIBRATE = KEY_ALARM_VIBRATE + " integer default 0";

    public static final String KEY_ALARM_RINGTONE_NAME = "ringtoneName";                            // ringtone uri (optional)
    public static final String DEF_ALARM_RINGTONE_NAME = KEY_ALARM_RINGTONE_NAME + " text";

    public static final String KEY_ALARM_RINGTONE_URI = "ringtoneURI";                              // ringtone uri (optional)
    public static final String DEF_ALARM_RINGTONE_URI = KEY_ALARM_RINGTONE_URI + " text";

    private static final String TABLE_ALARMS = "alarms";
    private static final String TABLE_ALARMS_CREATE_COLS = DEF_ROWID + ", "

                                                         + DEF_ALARM_TYPE + ", "
                                                         + DEF_ALARM_ENABLED + ", "
                                                         + DEF_ALARM_LABEL + ", "

                                                         + DEF_ALARM_REPEATING + ", "
                                                         + DEF_ALARM_REPEATING_DAYS + ", "

                                                         + DEF_ALARM_DATETIME_ADJUSTED + ", "
                                                         + DEF_ALARM_DATETIME + ", "
                                                         + DEF_ALARM_DATETIME_HOUR + ", "
                                                         + DEF_ALARM_DATETIME_MINUTE + ", "
                                                         + DEF_ALARM_DATETIME_OFFSET + ", "

                                                         + DEF_ALARM_SOLAREVENT + ", "

                                                         + DEF_ALARM_PLACELABEL + ", "
                                                         + DEF_ALARM_LATITUDE + ", "
                                                         + DEF_ALARM_LONGITUDE + ", "
                                                         + DEF_ALARM_ALTITUDE + ", "

                                                         + DEF_ALARM_VIBRATE + ", "
                                                         + DEF_ALARM_RINGTONE_NAME + ", "
                                                         + DEF_ALARM_RINGTONE_URI;

    private static final String TABLE_ALARMS_CREATE = "create table " + TABLE_ALARMS + " (" + TABLE_ALARMS_CREATE_COLS + ");";

    private static final String[] QUERY_ALARMS_MINENTRY = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_DATETIME, KEY_ALARM_LABEL };
    private static final String[] QUERY_ALARMS_FULLENTRY = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_LABEL,
                                                                          KEY_ALARM_REPEATING, KEY_ALARM_REPEATING_DAYS,
                                                                          KEY_ALARM_DATETIME_ADJUSTED, KEY_ALARM_DATETIME, KEY_ALARM_DATETIME_HOUR, KEY_ALARM_DATETIME_MINUTE, KEY_ALARM_DATETIME_OFFSET,
                                                                          KEY_ALARM_SOLAREVENT, KEY_ALARM_PLACELABEL, KEY_ALARM_LATITUDE, KEY_ALARM_LONGITUDE, KEY_ALARM_ALTITUDE,
                                                                          KEY_ALARM_VIBRATE, KEY_ALARM_RINGTONE_NAME, KEY_ALARM_RINGTONE_URI };

    //
    // Table: AlarmState
    //

    public static final String KEY_STATE_ALARMID = "alarmID";
    public static final String DEF_STATE_ALARMID = KEY_STATE_ALARMID + " integer primary key";

    public static final String KEY_STATE = "state";
    public static final String DEF_STATE = KEY_STATE + " integer not null";

    private static final String TABLE_ALARMSTATE = "alarmstate";
    private static final String TABLE_ALARMSTATE_CREATE_COLS = DEF_STATE_ALARMID + ", " + DEF_STATE;
    private static final String TABLE_ALARMSTATE_CREATE = "create table " + TABLE_ALARMSTATE + " (" + TABLE_ALARMSTATE_CREATE_COLS + ");";
    private static final String[] QUERY_ALARMSTATE_FULLENTRY = new String[] { KEY_STATE_ALARMID, KEY_STATE };

    /**
     *
     */
    private final Context context;
    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;

    public AlarmDatabaseAdapter(Context context)
    {
        this.context = context;
    }

    /**
     * Open the database
     * @return a reference the (now open) database adapter
     * @throws SQLException if failed to open
     */
    public AlarmDatabaseAdapter open() throws SQLException
    {
        if (databaseHelper != null)
        {
            databaseHelper.close();
        }
        databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
        return this;
    }

    /**
     * Close the database.
     */
    public void close()
    {
        databaseHelper.close();
        database = null;
    }

    /**
     * Get the number of alarms in the database
     * @return number of alarms
     */
    public int getAlarmCount()
    {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_ALARMS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * Get a Cursor over alarms in the database.
     * @param n get first n results (n <= 0 for complete list)
     * @param fullEntry true get all alarm data, false get display name only
     * @return a Cursor into the database
     */
    public Cursor getAllAlarms(int n, boolean fullEntry)
    {
        String[] QUERY = (fullEntry) ? QUERY_ALARMS_FULLENTRY : QUERY_ALARMS_MINENTRY;
        Cursor cursor =  (n > 0) ? database.query( TABLE_ALARMS, QUERY, null, null, null, null, "_id DESC", n+"" )
                                 : database.query( TABLE_ALARMS, QUERY, null, null, null, null, "_id DESC" );
        if (cursor != null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }
    public Cursor getAllAlarms(int n, boolean fullEntry, boolean enabledOnly)
    {
        String selection = (enabledOnly ? KEY_ALARM_ENABLED + " = ?" : null);    // select enabled
        String[] selectionArgs = (enabledOnly ? new String[] {"1"} : null);    // is 1 (true)
        String[] query = (fullEntry) ? QUERY_ALARMS_FULLENTRY : QUERY_ALARMS_MINENTRY;
        Cursor cursor =  (n > 0) ? database.query( TABLE_ALARMS, query, selection, selectionArgs, null, null, "_id DESC", n+"" )
                                 : database.query( TABLE_ALARMS, query, selection, selectionArgs, null, null, "_id DESC" );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Get an alarm from the database
     * @param row the rowID to get
     * @return a Cursor into the database
     * @throws SQLException if query failed
     */
    public Cursor getAlarm(long row) throws SQLException
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String[] QUERY = QUERY_ALARMS_FULLENTRY;
        Cursor cursor = database.query( true, TABLE_ALARMS, QUERY,
                                        KEY_ROWID + "=" + row, null,
                                        null, null, null, null );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Get an alarm state from the database.
     * @param row the rowID to get
     * @return a Cursor into the database
     * @throws SQLException if query failed
     */
    public Cursor getAlarmState(long row) throws SQLException
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String[] QUERY = QUERY_ALARMSTATE_FULLENTRY;
        Cursor cursor = database.query( true, TABLE_ALARMSTATE, QUERY,
                KEY_STATE_ALARMID + "=" + row, null,
                null, null, null, null );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Add an alarm to the database.
     * @param values ContentValues
     * @return the rowID of the newly added alarm or -1 if an error
     */
    public long addAlarm( ContentValues values )
    {
        long rowID = database.insert(TABLE_ALARMS, null, values);
        if (rowID != -1)
        {
            ContentValues alarmState = new ContentValues();
            alarmState.put(KEY_STATE_ALARMID, rowID);
            alarmState.put(KEY_STATE, AlarmState.STATE_NONE);
            database.insert(TABLE_ALARMSTATE, null, alarmState);
        }
        return rowID;
    }

    public boolean updateAlarm( long row, ContentValues values )
    {
        return database.update(TABLE_ALARMS, values,KEY_ROWID + "=" + row, null) > 0;
    }

    public boolean updateAlarmState( long row, ContentValues values )
    {
        return database.update(TABLE_ALARMSTATE, values, KEY_STATE_ALARMID + "=" + row, null) > 0;
    }

    public String addAlarmCSV_header()
    {
        String separator = ", ";
        //noinspection UnnecessaryLocalVariable
        String line = KEY_ALARM_TYPE + separator +
                KEY_ALARM_ENABLED + separator +
                KEY_ALARM_DATETIME_ADJUSTED + separator +
                KEY_ALARM_DATETIME + separator +
                KEY_ALARM_DATETIME_HOUR + separator +
                KEY_ALARM_DATETIME_MINUTE + separator +
                KEY_ALARM_DATETIME_OFFSET + separator +
                KEY_ALARM_LABEL + separator +
                KEY_ALARM_REPEATING + separator +
                KEY_ALARM_REPEATING_DAYS + separator +
                KEY_ALARM_SOLAREVENT + separator +
                KEY_ALARM_PLACELABEL + separator +
                KEY_ALARM_LATITUDE + separator +
                KEY_ALARM_LONGITUDE + separator +
                KEY_ALARM_ALTITUDE + separator +
                KEY_ALARM_VIBRATE + separator +
                KEY_ALARM_RINGTONE_NAME + separator +
                KEY_ALARM_RINGTONE_URI;
        return line;
    }
    public String addAlarmCSV_row( ContentValues alarm )
    {
        String quote = "\"";
        String separator = ", ";
        //noinspection UnnecessaryLocalVariable
        String line = alarm.getAsString(KEY_ALARM_TYPE) + separator +
                      alarm.getAsInteger(KEY_ALARM_ENABLED) + separator +
                      alarm.getAsLong(KEY_ALARM_DATETIME_ADJUSTED) + separator +
                      alarm.getAsLong(KEY_ALARM_DATETIME) + separator +
                      alarm.getAsInteger(KEY_ALARM_DATETIME_HOUR) + separator +
                      alarm.getAsInteger(KEY_ALARM_DATETIME_MINUTE) + separator +
                      quote + alarm.getAsString(KEY_ALARM_LABEL) + quote + separator +
                      alarm.getAsInteger(KEY_ALARM_REPEATING) + separator +
                      quote + alarm.getAsString(KEY_ALARM_REPEATING_DAYS) + quote + separator +
                      alarm.getAsString(KEY_ALARM_SOLAREVENT) + separator +
                      quote + alarm.getAsString(KEY_ALARM_PLACELABEL) + quote + separator +
                      alarm.getAsString(KEY_ALARM_LATITUDE) + separator +
                      alarm.getAsString(KEY_ALARM_LONGITUDE) + separator +
                      alarm.getAsString(KEY_ALARM_ALTITUDE) + separator +
                      alarm.getAsInteger(KEY_ALARM_VIBRATE) + separator +
                      alarm.getAsString(KEY_ALARM_RINGTONE_NAME) + separator +
                      alarm.getAsString(KEY_ALARM_RINGTONE_URI);
        return line;
    }

    /**
     * Remove an alarm from the database
     * @param row the rowID to remove
     * @return true if the alarm was removed
     */
    public boolean removeAlarm(long row)
    {
        boolean removeAlarm = (database.delete(TABLE_ALARMS, KEY_ROWID + "=" + row, null) > 0);
        boolean removeAlarmState = (database.delete(TABLE_ALARMSTATE, KEY_STATE_ALARMID + "=" + row, null) > 0);
        return removeAlarm && removeAlarmState;
    }

    /**
     * Clear all alarms from the database
     * @return true alarms have been cleared
     */
    public boolean clearAlarms()
    {
        return (database.delete(TABLE_ALARMS, null, null) > 0) &&
               (database.delete(TABLE_ALARMSTATE, null, null) > 0);
    }

    /**
     *
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            switch (DATABASE_VERSION)
            {
                //noinspection ConstantConditions
                case 0:
                default:
                    db.execSQL(TABLE_ALARMS_CREATE);
                    db.execSQL(TABLE_ALARMSTATE_CREATE);
                    break;
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            /**Log.w("GetFixDatabaseAdapter", "Upgrading database from version " + oldVersion + " to " + newVersion);
            switch (oldVersion)
            {
                case 1:
                case 2:
                case 3:
                    break;
            }*/
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * AlarmItemTask
     */
    public static class AlarmItemTask extends AsyncTask<Long, Void, AlarmClockItem>
    {
        protected AlarmDatabaseAdapter db;

        public AlarmItemTask(Context context)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected AlarmClockItem doInBackground(Long... rowIDs)
        {
            AlarmClockItem item = null;
            if (rowIDs.length > 0)
            {
                db.open();
                Cursor cursor0 = db.getAlarm(rowIDs[0]);
                if (cursor0 != null)
                {
                    cursor0.moveToFirst();
                    if (!cursor0.isAfterLast())
                    {
                        ContentValues itemValues = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(cursor0, itemValues);
                        item = new AlarmClockItem(itemValues);

                        Cursor cursor1 = db.getAlarmState(rowIDs[0]);
                        if (cursor1 != null)
                        {
                            cursor1.moveToFirst();
                            if (!cursor1.isAfterLast())
                            {
                                ContentValues stateValues = new ContentValues();
                                DatabaseUtils.cursorRowToContentValues(cursor1, stateValues);
                                item.state = new AlarmState(stateValues);
                            }
                            cursor1.close();
                        }
                    }
                    cursor0.close();
                }
                db.close();
            }
            return item;
        }

        protected void onPostExecute( AlarmClockItem item )
        {
            if (taskListener != null) {
                taskListener.onItemLoaded(item);
            }
        }

        private AlarmItemTaskListener taskListener = null;
        public void setAlarmItemTaskListener( AlarmItemTaskListener listener )
        {
            this.taskListener = listener;
        }

        public static abstract class AlarmItemTaskListener
        {
            public void onItemLoaded( AlarmClockItem item ) {}
        }
    }

    /**
     * AlarmUpdateTask
     */
    public static class AlarmUpdateTask extends AsyncTask<AlarmClockItem, Void, Boolean>
    {
        public static final String TAG = "AlarmReceiverItemTask";

        protected AlarmDatabaseAdapter db;
        private boolean flag_add = false;
        private boolean flag_withState = true;
        private AlarmClockItem lastItem;

        public AlarmUpdateTask(@NonNull Context context)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        public AlarmUpdateTask(@NonNull Context context, boolean flag_add, boolean flag_withState)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
            this.flag_add = flag_add;
            this.flag_withState = flag_withState;
        }

        @Override
        protected Boolean doInBackground(AlarmClockItem... items)
        {
            db.open();
            boolean updated = true;
            for (AlarmClockItem item : items)
            {
                lastItem = item;
                long lastItemID = item.rowID;

                boolean itemUpdated;
                if (flag_add) {
                    lastItem.rowID = db.addAlarm(item.asContentValues(false));
                    itemUpdated = (lastItem.rowID > 0);

                } else {
                    itemUpdated = (db.updateAlarm(item.rowID, item.asContentValues(false)));
                }

                if (itemUpdated && flag_withState && item.state != null) {
                    db.updateAlarmState(lastItemID, item.state.asContentValues());
                }

                updated = updated && itemUpdated;
            }
            db.close();
            return updated;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            Log.d(TAG, "Item Saved: " + lastItem.rowID + ":" + (lastItem.state != null ? lastItem.state.getState() : null));
            if (listener != null)
                listener.onFinished(result, lastItem);
        }

        protected AlarmClockUpdateTaskListener listener = null;
        public void setTaskListener( AlarmClockUpdateTaskListener l )
        {
            listener = l;
        }

        public static abstract class AlarmClockUpdateTaskListener
        {
            public void onFinished(Boolean result, AlarmClockItem item) {}
        }
    }

    /**
     * AlarmDeleteTask
     */
    public static class AlarmDeleteTask extends AsyncTask<Long, Void, Boolean>
    {
        protected AlarmDatabaseAdapter db;
        protected Long lastRowId;

        public AlarmDeleteTask(Context context)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected Boolean doInBackground(Long... rowIDs)
        {
            db.open();
            boolean removed = true;
            if (rowIDs.length > 0)
            {
                for (long rowID : rowIDs) {
                    removed = removed && db.removeAlarm(rowID);
                    lastRowId = rowID;
                }
            } else {
                removed = db.clearAlarms();
                lastRowId = null;
            }
            db.close();
            return removed;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if (listener != null)
                listener.onFinished(result, lastRowId);
        }

        protected AlarmClockDeleteTaskListener listener = null;
        public void setTaskListener( AlarmClockDeleteTaskListener l )
        {
            listener = l;
        }

        public static abstract class AlarmClockDeleteTaskListener
        {
            public void onFinished(Boolean result, Long rowID) {}
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * AlarmStateTask
     */
    public static class AlarmStateTask extends AsyncTask<Long, Void, AlarmState>
    {
        protected AlarmDatabaseAdapter db;

        public AlarmStateTask(Context context)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected AlarmState doInBackground(Long... rowIDs)
        {
            AlarmState state = null;
            if (rowIDs.length > 0)
            {
                db.open();
                Cursor cursor = db.getAlarmState(rowIDs[0]);
                cursor.moveToFirst();
                if (!cursor.isAfterLast())
                {
                    ContentValues entryValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, entryValues);
                    state = new AlarmState(entryValues);
                }
                db.close();
            }
            return state;
        }

        protected void onPostExecute( AlarmState state )
        {
            if (taskListener != null) {
                taskListener.onStateLoaded(state);
            }
        }

        private AlarmStateTaskListener taskListener = null;
        public void setAlarmItemTaskListener( AlarmStateTaskListener listener )
        {
            this.taskListener = listener;
        }

        public static abstract class AlarmStateTaskListener
        {
            public void onStateLoaded( AlarmState state ) {}
        }
    }

    /**
     * AlarmStateUpdateTask
     */
    public static class AlarmStateUpdateTask extends AsyncTask<AlarmState, Void, Boolean>
    {
        public static final String TAG = "AlarmReceiverStateTask";

        protected AlarmDatabaseAdapter db;
        protected AlarmState lastState = null;

        public AlarmStateUpdateTask(Context context)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        @Override
        protected Boolean doInBackground(AlarmState... states)
        {
            db.open();
            boolean updated = true;
            for (AlarmState state : states)
            {
                lastState = state;
                updated = updated && (db.updateAlarmState(state.getAlarmID(), state.asContentValues()));
            }
            db.close();
            return updated;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            Log.d(TAG, "State Saved: " + lastState.getAlarmID() + ":" + lastState.getState());
            if (listener != null)
                listener.onFinished(result);
        }

        protected AlarmStateUpdateTaskListener listener = null;
        public void setTaskListener( AlarmStateUpdateTaskListener l )
        {
            listener = l;
        }

        public static abstract class AlarmStateUpdateTaskListener
        {
            public void onFinished(Boolean result) {}
        }
    }

    /**
     * AlarmListTask
     */
    public static class AlarmListTask extends AsyncTask<Void, Void, Long[]>
    {
        protected AlarmDatabaseAdapter db;

        public AlarmListTask(Context context)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        private boolean param_enabledOnly = false;
        public void setParam_enabledOnly( boolean value ) {
            param_enabledOnly = value;
        }

        @Override
        protected Long[] doInBackground(Void... voids)
        {
            ArrayList<Long> alarmIds = new ArrayList<>();
            db.open();
            Cursor cursor = db.getAllAlarms(0, false, param_enabledOnly);
            while (!cursor.isAfterLast())
            {
                alarmIds.add(cursor.getLong(cursor.getColumnIndex(AlarmDatabaseAdapter.KEY_ROWID)));
                cursor.moveToNext();
            }
            db.close();
            return alarmIds.toArray(new Long[0]);
        }

        protected void onPostExecute( Long[] items )
        {
            if (taskListener != null) {
                taskListener.onItemsLoaded(items);
            }
        }

        private AlarmListTaskListener taskListener = null;
        public void setAlarmItemTaskListener( AlarmListTaskListener listener )
        {
            this.taskListener = listener;
        }

        public static abstract class AlarmListTaskListener
        {
            public void onItemsLoaded(Long[] ids ) {}
        }
    }


}