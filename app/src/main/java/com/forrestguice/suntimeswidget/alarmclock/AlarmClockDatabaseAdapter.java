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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmClockDatabaseAdapter
{
    private static final String DATABASE_NAME = "suntimesAlarms";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ROWID = "_id";                                                   // row ID
    public static final String DEF_ROWID = KEY_ROWID + " integer primary key autoincrement";

    public static final String KEY_ALARM_TYPE = "alarmType";                                        // type (ALARM, NOTIFICATION)
    public static final String DEF_ALARM_TYPE = KEY_ALARM_TYPE + " text not null";

    public static final String KEY_ALARM_ENABLED = "enabled";                                       // enabled flag (0: false, 1: true)
    public static final String DEF_ALARM_ENABLED = KEY_ALARM_ENABLED + " integer default 0";

    public static final String KEY_ALARM_REPEATING = "repeating";                                   // repeating flag (0: false, 1: true)
    public static final String DEF_ALARM_REPEATING = KEY_ALARM_REPEATING + " integer default 0";

    public static final String KEY_ALARM_DATETIME = "datetime";                                     // timestamp, the datetime this alarm should sound; one-time alarm if SOLAREVENT is null; repeating-alarm if SOLAREVENT is provided (DATETIME recalculated on repeat).
    public static final String DEF_ALARM_DATETIME = KEY_ALARM_DATETIME + " integer default -1";

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

    private static final String[] QUERY_ALARMS_MINENTRY = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_REPEATING, KEY_ALARM_DATETIME, KEY_ALARM_LABEL };
    private static final String[] QUERY_ALARMS_FULLENTRY = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_LABEL, KEY_ALARM_REPEATING,
                                                                          KEY_ALARM_DATETIME, KEY_ALARM_DATETIME_HOUR, KEY_ALARM_DATETIME_MINUTE, KEY_ALARM_DATETIME_OFFSET,
                                                                          KEY_ALARM_SOLAREVENT, KEY_ALARM_PLACELABEL, KEY_ALARM_LATITUDE, KEY_ALARM_LONGITUDE, KEY_ALARM_ALTITUDE,
                                                                          KEY_ALARM_VIBRATE, KEY_ALARM_RINGTONE_NAME, KEY_ALARM_RINGTONE_URI };

    /**
     *
     */
    private final Context context;
    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;

    public AlarmClockDatabaseAdapter(Context context)
    {
        this.context = context;
    }

    /**
     * Open the database
     * @return a reference the (now open) database adapter
     * @throws SQLException if failed to open
     */
    public AlarmClockDatabaseAdapter open() throws SQLException
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
        if (cursor != null)
        {
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
        return database.insert(TABLE_ALARMS, null, values);
    }

    public boolean updateAlarm( long row, ContentValues values )
    {
        return database.update(TABLE_ALARMS, values,KEY_ROWID + "=" + row, null) > 0;
    }

    public String addAlarmCSV_header()
    {
        String separator = ", ";
        //noinspection UnnecessaryLocalVariable
        String line = KEY_ALARM_TYPE + separator +
                KEY_ALARM_ENABLED + separator +
                KEY_ALARM_REPEATING + separator +
                KEY_ALARM_DATETIME + separator +
                KEY_ALARM_DATETIME_HOUR + separator +
                KEY_ALARM_DATETIME_MINUTE + separator +
                KEY_ALARM_DATETIME_OFFSET + separator +
                KEY_ALARM_LABEL + separator +
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
                      alarm.getAsInteger(KEY_ALARM_REPEATING) + separator +
                      alarm.getAsLong(KEY_ALARM_DATETIME) + separator +
                      alarm.getAsInteger(KEY_ALARM_DATETIME_HOUR) + separator +
                      alarm.getAsInteger(KEY_ALARM_DATETIME_MINUTE) + separator +
                      quote + alarm.getAsString(KEY_ALARM_LABEL) + quote + separator +
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
        return database.delete(TABLE_ALARMS, KEY_ROWID + "=" + row, null) > 0;
    }

    /**
     * Clear all alarms from the database
     * @return true alarms have been cleared
     */
    public boolean clearAlarms()
    {
        return database.delete(TABLE_ALARMS, null, null) > 0;
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
}