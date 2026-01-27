/**
    Copyright (C) 2018-2022 Forrest Guice
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.concurrent.TaskListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * AlarmDatabaseAdapter
 *
 * @version 4
 *
 * version history:
 * 1 initial version
 * 2 adds column ALARM_TIMEZONE ("timezone")
 * 3 adds column ALARM_ACTION0 ("actionID0"), ALARM_ACTION1 ("actionID1")
 * 4 adds column ALARM_NOTE ("note"), ALARM_FLAGS ("flags"), ALARM_ACTION2 ("actionID2"), ALARM_ACTION3 ("actionID3")
 */
public class AlarmDatabaseAdapter
{
    public static final String DATABASE_NAME = "suntimesAlarms";
    public static final int DATABASE_VERSION = 4;

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

    public static final String KEY_ALARM_TIMEZONE = "timezone";                                     // TZ_ID; applied to clock time when SOLAREVENT is null
    public static final String DEF_ALARM_TIMEZONE = KEY_ALARM_TIMEZONE + " text";

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

    public static final String KEY_ALARM_ACTION0 = "actionID0";                                     // actionID 0 (optional)  .. action on trigger
    public static final String DEF_ALARM_ACTION0 = KEY_ALARM_ACTION0 + " text";

    public static final String KEY_ALARM_ACTION1 = "actionID1";                                     // actionID 1 (optional)  .. action on dismiss
    public static final String DEF_ALARM_ACTION1 = KEY_ALARM_ACTION1 + " text";

    public static final String KEY_ALARM_ACTION2 = "actionID2";                                     // actionID 2 (optional)  .. action on reminder
    public static final String DEF_ALARM_ACTION2 = KEY_ALARM_ACTION2 + " text";

    public static final String KEY_ALARM_ACTION3 = "actionID3";                                     // actionID 3 (optional)  .. <unused/placeholder>
    public static final String DEF_ALARM_ACTION3 = KEY_ALARM_ACTION3 + " text";

    public static final String KEY_ALARM_RINGTONE_NAME = "ringtoneName";                            // ringtone uri (optional)
    public static final String DEF_ALARM_RINGTONE_NAME = KEY_ALARM_RINGTONE_NAME + " text";

    public static final String KEY_ALARM_RINGTONE_URI = "ringtoneURI";                              // ringtone uri (optional)
    public static final String DEF_ALARM_RINGTONE_URI = KEY_ALARM_RINGTONE_URI + " text";

    public static final String KEY_ALARM_NOTE = "note";                                             // note (optional)
    public static final String DEF_ALARM_NOTE = KEY_ALARM_NOTE + " text";

    public static final String KEY_ALARM_FLAGS = "flags";                                           // alarm flags (optional)
    public static final String DEF_ALARM_FLAGS = KEY_ALARM_FLAGS + " text";

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
                                                         + DEF_ALARM_RINGTONE_URI + ", "

                                                         + DEF_ALARM_TIMEZONE + ", "

                                                         + DEF_ALARM_ACTION0 + ", "
                                                         + DEF_ALARM_ACTION1 + ", "
                                                         + DEF_ALARM_ACTION2 + ", "
                                                         + DEF_ALARM_ACTION3 + ", "

                                                         + DEF_ALARM_FLAGS + ", "
                                                         + DEF_ALARM_NOTE;

    private static final String TABLE_ALARMS_CREATE = "create table " + TABLE_ALARMS + " (" + TABLE_ALARMS_CREATE_COLS + ");";
    private static final String[] TABLE_ALARMS_UPGRADE_1_2 = new String[] { "alter table " + TABLE_ALARMS + " add column " + DEF_ALARM_TIMEZONE };
    private static final String[] TABLE_ALARMS_UPGRADE_2_3 = new String[] { "alter table " + TABLE_ALARMS + " add column " + DEF_ALARM_ACTION0,
                                                                            "alter table " + TABLE_ALARMS + " add column " + DEF_ALARM_ACTION1 };
    private static final String[] TABLE_ALARMS_UPGRADE_3_4 = new String[] { "alter table " + TABLE_ALARMS + " add column " + DEF_ALARM_ACTION2,
                                                                            "alter table " + TABLE_ALARMS + " add column " + DEF_ALARM_ACTION3,
                                                                            "alter table " + TABLE_ALARMS + " add column " + DEF_ALARM_FLAGS,
                                                                            "alter table " + TABLE_ALARMS + " add column " + DEF_ALARM_NOTE };
    private static final String[] TABLE_ALARMS_DOWNGRADE = new String[] { "DROP TABLE " + TABLE_ALARMS, TABLE_ALARMS_CREATE };

    private static final String[] QUERY_ALARMS_MINENTRY = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_DATETIME, KEY_ALARM_LABEL, KEY_ALARM_FLAGS };
    private static final String[] QUERY_ALARMS_FULLENTRY = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_LABEL,
                                                                          KEY_ALARM_REPEATING, KEY_ALARM_REPEATING_DAYS,
                                                                          KEY_ALARM_DATETIME_ADJUSTED, KEY_ALARM_DATETIME, KEY_ALARM_DATETIME_HOUR, KEY_ALARM_DATETIME_MINUTE, KEY_ALARM_DATETIME_OFFSET,
                                                                          KEY_ALARM_SOLAREVENT, KEY_ALARM_PLACELABEL, KEY_ALARM_LATITUDE, KEY_ALARM_LONGITUDE, KEY_ALARM_ALTITUDE,
                                                                          KEY_ALARM_VIBRATE, KEY_ALARM_RINGTONE_NAME, KEY_ALARM_RINGTONE_URI,
                                                                          KEY_ALARM_TIMEZONE, KEY_ALARM_ACTION0, KEY_ALARM_ACTION1, KEY_ALARM_ACTION2, KEY_ALARM_ACTION3, KEY_ALARM_FLAGS, KEY_ALARM_NOTE };

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
    @Nullable
    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;

    public static final String MSG_ILLEGAL_STATE = "database reference is null; was this method called after close() ?";

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
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_ALARMS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * Get the number of alarms in the database where..
     * @param soundUri is equal to given value
     * @return number of alarms using this uri
     */
    public int getAlarmCount(String soundUri)
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        int count = 0;
        Cursor cursor = database.query(TABLE_ALARMS, QUERY_ALARMS_MINENTRY, KEY_ALARM_RINGTONE_URI + " = ?", new String[] { soundUri }, null, null, null);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
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
        return getAllAlarms(n, QUERY, null, null);
    }
    public Cursor getAllAlarms(int n, boolean fullEntry, boolean enabledOnly)
    {
        String selection = (enabledOnly ? KEY_ALARM_ENABLED + " = ?" : null);    // select enabled
        String[] selectionArgs = (enabledOnly ? new String[] {"1"} : null);    // is 1 (true)
        String[] query = (fullEntry) ? QUERY_ALARMS_FULLENTRY : QUERY_ALARMS_MINENTRY;
        return getAllAlarms(n, query, selection, selectionArgs);
    }

    public Cursor getAllAlarmsByState(int n, int... alarmState)
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        StringBuilder selection = new StringBuilder(KEY_STATE + " = ?");
        String[] selectionArgs = new String[alarmState.length];
        selectionArgs[0] = Integer.toString(alarmState[0]);
        for (int i=1; i<alarmState.length; i++) {
            selection.append(" OR " + KEY_STATE + " = ?");
            selectionArgs[i] = Integer.toString(alarmState[i]);
        }

        Cursor cursor =  (n > 0) ? database.query( TABLE_ALARMSTATE, QUERY_ALARMSTATE_FULLENTRY, selection.toString(), selectionArgs, null, null, KEY_STATE_ALARMID + " DESC", n+"" )
                                 : database.query( TABLE_ALARMSTATE, QUERY_ALARMSTATE_FULLENTRY, selection.toString(), selectionArgs, null, null, KEY_STATE_ALARMID + " DESC" );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllAlarms(int n, String[] columns, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        Cursor cursor =  (n > 0) ? database.query( TABLE_ALARMS, columns, selection, selectionArgs, null, null, KEY_ROWID + " DESC", n+"" )
                                 : database.query( TABLE_ALARMS, columns, selection, selectionArgs, null, null, KEY_ROWID + " DESC" );
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
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
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
    public Cursor getAlarm(long row, @NonNull String[] columns, @Nullable String selection, @Nullable String[] selectionArgs) throws SQLException
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        String selection0 = KEY_ROWID + "=" + row;
        if (selection != null) {
            selection0 += " AND " + selection;
        }
        Cursor cursor = database.query( true, TABLE_ALARMS, columns, selection0, selectionArgs,
                null, null, null, null );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Long findUpcomingAlarmId(long nowMillis) throws SQLException {
        return findUpcomingAlarmId(nowMillis, new String[] { AlarmClockItem.AlarmType.ALARM.name() });
    }
    public Long findUpcomingAlarmId(long nowMillis, @Nullable String[] types) throws SQLException
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        String[] columns = new String[] { KEY_ROWID, KEY_ALARM_TYPE, KEY_ALARM_ENABLED, KEY_ALARM_DATETIME_ADJUSTED };
        StringBuilder selection = new StringBuilder(KEY_ALARM_ENABLED + " = ?");
        List<String> selectionArgs = new ArrayList<>(Collections.singletonList("1"));
        if (types != null && types.length > 0)
        {
            boolean multipleTypes = (types.length > 1);

            selection.append(" AND ");
            if (multipleTypes) {
                selection.append("(");
            }
            for (int i=0; i<types.length; i++)
            {
                if (i > 0) {
                    selection.append(" OR ");
                }
                selection.append(KEY_ALARM_TYPE + "= ?");
                selectionArgs.add(types[i]);
            }
            if (multipleTypes) {
                selection.append(")");
            }
        }
        //String selection = KEY_ALARM_TYPE + "= ? AND " + KEY_ALARM_ENABLED + " = ?";
        //String[] selectionArgs = ((type != null)
        //        ? new String[] { "1", type }
        //        : new String[] { "1" });

        Cursor cursor = database.query( true, TABLE_ALARMS, columns, selection.toString(), selectionArgs.toArray(new String[0]), null, null, null, null );
        if (cursor != null)
        {
            Long upcomingAlarmId = null;
            long timeToUpcomingAlarm = Long.MAX_VALUE;

            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                try {
                    long alarmtime = cursor.getLong(cursor.getColumnIndexOrThrow(AlarmDatabaseAdapter.KEY_ALARM_DATETIME_ADJUSTED));
                    int i_rowID = cursor.getColumnIndexOrThrow(AlarmDatabaseAdapter.KEY_ROWID);
                    long timeToAlarm = alarmtime - nowMillis;
                    if (timeToAlarm > 0 && timeToAlarm < timeToUpcomingAlarm) {
                        timeToUpcomingAlarm = timeToAlarm;
                        upcomingAlarmId = cursor.getLong(i_rowID);
                    }
                } catch (IllegalArgumentException e) {
                    Log.w("AlarmDatabaseAdapter", "findUpcomingAlarmId: missing required columns! " + e);
                }
                cursor.moveToNext();
            }
            cursor.close();
            return upcomingAlarmId;
        }
        return null;
    }

    /**
     * Get an alarm state from the database.
     * @param row the rowID to get
     * @return a Cursor into the database
     * @throws SQLException if query failed
     */
    public Cursor getAlarmState(long row) throws SQLException
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
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
    public Cursor getAlarmState(long row, String selection, String[] selectionArgs) throws SQLException
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        String selection0 = KEY_STATE_ALARMID + "=" + row;
        if (selection != null) {
            selection0 += " AND " + selection;
        }
        Cursor cursor = database.query( true, TABLE_ALARMSTATE, QUERY_ALARMSTATE_FULLENTRY, selection0, selectionArgs, null, null, null, null );
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
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
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
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        return database.update(TABLE_ALARMS, values,KEY_ROWID + "=" + row, null) > 0;
    }

    public boolean updateAlarmState( long row, ContentValues values )
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
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
                KEY_ALARM_TIMEZONE + separator +
                KEY_ALARM_PLACELABEL + separator +
                KEY_ALARM_LATITUDE + separator +
                KEY_ALARM_LONGITUDE + separator +
                KEY_ALARM_ALTITUDE + separator +
                KEY_ALARM_VIBRATE + separator +
                KEY_ALARM_RINGTONE_NAME + separator +
                KEY_ALARM_RINGTONE_URI + separator +
                KEY_ALARM_ACTION0 + separator +
                KEY_ALARM_ACTION1 + separator +
                KEY_ALARM_ACTION2 + separator +
                KEY_ALARM_ACTION3 + separator +
                KEY_ALARM_FLAGS + separator +
                KEY_ALARM_NOTE;
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
                      alarm.getAsString(KEY_ALARM_TIMEZONE) + separator +
                      quote + alarm.getAsString(KEY_ALARM_PLACELABEL) + quote + separator +
                      alarm.getAsString(KEY_ALARM_LATITUDE) + separator +
                      alarm.getAsString(KEY_ALARM_LONGITUDE) + separator +
                      alarm.getAsString(KEY_ALARM_ALTITUDE) + separator +
                      alarm.getAsInteger(KEY_ALARM_VIBRATE) + separator +
                      alarm.getAsString(KEY_ALARM_RINGTONE_NAME) + separator +
                      alarm.getAsString(KEY_ALARM_RINGTONE_URI) + separator +
                      alarm.getAsString(KEY_ALARM_ACTION0) + separator +
                      alarm.getAsString(KEY_ALARM_ACTION1) + separator +
                      alarm.getAsString(KEY_ALARM_ACTION2) + separator +
                      alarm.getAsString(KEY_ALARM_ACTION3) + separator +
                      alarm.getAsString(KEY_ALARM_FLAGS) + separator +
                      alarm.getAsString(KEY_ALARM_NOTE);
        return line;
    }

    /**
     * Remove an alarm from the database
     * @param row the rowID to remove
     * @return true if the alarm was removed
     */
    public boolean removeAlarm(long row)
    {
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
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
        if (database == null) {
            throw new IllegalStateException(MSG_ILLEGAL_STATE);
        }
        return (database.delete(TABLE_ALARMS, null, null) > 0) &&
               (database.delete(TABLE_ALARMSTATE, null, null) > 0);
    }

    @TargetApi(19)
    public void releaseUnusedUriPermissions(Context context) {
        releaseUnusedUriPermissions(context, new String[] { ".png" });    // except for image types
    }

    @TargetApi(19)
    public void releaseUnusedUriPermissions(Context context, String[] except)
    {
        ContentResolver resolver = (context != null) ? context.getContentResolver() : null;
        if (resolver != null)
        {
            List<UriPermission> permissions = resolver.getPersistedUriPermissions();
            releasePermissionLoop:
            for (UriPermission permission : permissions)
            {
                Uri uri = permission.getUri();
                String uriString = permission.getUri().toString();
                for (String exceptForType : except) {
                    if (uriString.endsWith(exceptForType))
                        continue releasePermissionLoop;
                }
                int alarmCount = getAlarmCount(uriString);
                if (alarmCount <= 0) {
                    resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Log.i("AlarmDatabaseAdapter", "released uri permission " + permission.getUri().toString());
                } // else Log.d("AlarmDatabaseAdapter", "retaining uri permission " + permission.getUri().toString() + ", used by " + alarmCount + " alarms.");
            }
        }
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

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            switch (DATABASE_VERSION)
            {
                case 0: case 1: case 2: case 3: case 4:
                default:
                    db.execSQL(TABLE_ALARMS_CREATE);
                    db.execSQL(TABLE_ALARMSTATE_CREATE);
                    break;
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w("AlarmDatabaseAdapter", "Upgrading database from version " + oldVersion + " to " + newVersion);
            if (oldVersion == 1)
            {
                switch (newVersion)
                {
                    case 2:
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_1_2);
                        break;
                    case 3:
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_1_2);
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_2_3);
                        break;
                    case 4:
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_1_2);
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_2_3);
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_3_4);
                        break;
                }

            } else if (oldVersion == 2) {
                switch (newVersion)
                {
                    case 3:
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_2_3);
                        break;
                    case 4:
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_2_3);
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_3_4);
                        break;
                }

            } else if (oldVersion == 3) {
                switch (newVersion)
                {
                    case 4:
                        applyUpgrade(db, TABLE_ALARMS_UPGRADE_3_4);
                        break;
                }
            }
        }

        protected void applyUpgrade(SQLiteDatabase db, String[] upgrade)
        {
            for (int i=0; i<upgrade.length; i++) {
                db.execSQL(upgrade[i]);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            for (int i=0; i<TABLE_ALARMS_DOWNGRADE.length; i++) {
                db.execSQL(TABLE_ALARMS_DOWNGRADE[i]);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * AlarmItemTaskResult
     */
    public static class AlarmItemTaskResult
    {
        public AlarmItemTaskResult(@NonNull Boolean result, AlarmClockItem item, AlarmClockItem[] items) {
            this.result = result;
            this.item = item;
            this.items = items;
        }

        private final Boolean result;
        public Boolean getResult() {
            return result;
        }

        private final AlarmClockItem item;
        public AlarmClockItem getItem() {
            return item;
        }

        private final AlarmClockItem[] items;
        public AlarmClockItem[] getItems() {
            return items;
        }
    }

    /**
     * AlarmItemTaskListener
     */
    public static abstract class AlarmItemTaskListener implements TaskListener<AlarmItemTaskResult>
    {
        @Override
        public void onStarted() {}
        @Override
        public void onFinished(AlarmItemTaskResult result) {}
    }

    /**
     * AlarmItemTask
     */
    public static class AlarmItemTask implements Callable<AlarmItemTaskResult>
    {
        private final WeakReference<Context> contextRef;
        protected final AlarmDatabaseAdapter db;
        protected final Long[] rowIDs;

        public AlarmItemTask(Context context, Long rowId) {
            this(context, new Long[] { rowId });
        }
        public AlarmItemTask(Context context, Long[] rowIds)
        {
            contextRef = new WeakReference<>(context);
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
            this.rowIDs = rowIds;
        }

        @Override
        public AlarmItemTaskResult call() throws Exception
        {
            AlarmClockItem item = null;
            if (rowIDs.length > 0)
            {
                db.open();
                item = loadAlarmClockItem(contextRef.get(), db, rowIDs[0]);
                db.close();
            }
            return new AlarmItemTaskResult(true, item, new AlarmClockItem[] { item } );
        }

        public static AlarmClockItem loadAlarmClockItem(Context context, AlarmDatabaseAdapter db, long rowId)
        {
            AlarmClockItem item = null;
            Cursor cursor0 = db.getAlarm(rowId);
            if (cursor0 != null)
            {
                cursor0.moveToFirst();
                if (!cursor0.isAfterLast())
                {
                    ContentValues itemValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor0, itemValues);
                    item = new AlarmClockItem(context, itemValues);

                    Cursor cursor1 = db.getAlarmState(rowId);
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
            return item;
        }

        private final List<AlarmItemTaskListener> taskListeners = new ArrayList<>();
        public void addAlarmItemTaskListener(AlarmItemTaskListener listener ) {
            this.taskListeners.add(listener);
        }
        public void clearAlarmItemTaskListeners() {
            taskListeners.clear();
        }
        public List<AlarmItemTaskListener> getTaskListeners() {
            return taskListeners;
        }
    }

    /**
     * AlarmUpdateTask
     */
    public static class AlarmUpdateTask implements Callable<AlarmItemTaskResult>
    {
        public static final String TAG = "AlarmReceiverItemTask";

        protected final AlarmDatabaseAdapter db;
        private boolean flag_add = false;
        private boolean flag_withState = true;
        private AlarmClockItem lastItem;
        private final AlarmClockItem[] items;

        public AlarmUpdateTask(@NonNull Context context, AlarmClockItem item) {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
            this.items = new AlarmClockItem[] { item } ;
        }
        public AlarmUpdateTask(@NonNull Context context, AlarmClockItem[] items) {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
            this.items = items;
        }
        public AlarmUpdateTask(@NonNull Context context, AlarmClockItem item, boolean flag_add, boolean flag_withState) {
            this(context, item);
            this.flag_add = flag_add;
            this.flag_withState = flag_withState;
        }
        public AlarmUpdateTask(@NonNull Context context, AlarmClockItem[] items, boolean flag_add, boolean flag_withState) {
            this(context, items);
            this.flag_add = flag_add;
            this.flag_withState = flag_withState;
        }

        @Override
        public AlarmItemTaskResult call() throws Exception
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
            //this.items = Arrays.copyOf(items, items.length);
            return new AlarmItemTaskResult(updated, lastItem, items);
        }

        protected AlarmItemTaskListener listener = null;
        public void setTaskListener( AlarmItemTaskListener l ) {
            listener = l;
        }
        public AlarmItemTaskListener getTaskListener() {
            return listener;
        }
    }

    /**
     * AlarmDeleteTask
     */
    public static class AlarmDeleteTask implements Callable<AlarmDeleteTask.TaskResult>
    {
        protected final AlarmDatabaseAdapter db;
        @Nullable
        protected final Long[] rowIDs;

        public AlarmDeleteTask(@NonNull Context context, @Nullable Long[] rowIDs)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
            this.rowIDs = rowIDs;
        }

        @Override
        public TaskResult call() throws Exception
        {
            Long lastRowId = null;
            db.open();
            boolean removed = true;
            if (rowIDs != null && rowIDs.length > 0)
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
            return new TaskResult(removed, lastRowId);
        }

        public static class TaskResult
        {
            public TaskResult(@NonNull Boolean result, @NonNull Long lastRowID)
            {
                this.result = result;
                this.lastRowID = lastRowID;
            }

            private final Boolean result;
            @NonNull
            public Boolean getResult() {
                return result;
            }

            private final Long lastRowID;
            @NonNull
            public Long getLastRowID() {
                return lastRowID;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * AlarmStateTask
     */
    /**public static class AlarmStateTask extends AsyncTask<Long, Void, AlarmState>
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
    }*/

    /**
     * AlarmStateUpdateTask
     */
    /**public static class AlarmStateUpdateTask extends AsyncTask<AlarmState, Void, Boolean>
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
    }*/

    /**
     * AlarmListTask
     */
    public static class AlarmListTask implements Callable<Long[]> // extends AsyncTask<Void, Void, Long[]>
    {
        protected final AlarmDatabaseAdapter db;

        public AlarmListTask(Context context)
        {
            db = new AlarmDatabaseAdapter(context.getApplicationContext());
        }

        private boolean param_enabledOnly = false;
        public void setParam_enabledOnly( boolean value ) {
            param_enabledOnly = value;
        }

        private int[] param_withAlarmState = null;
        public void setParam_withAlarmState(int... state) {
            param_withAlarmState = Arrays.copyOf(state, state.length);
        }

        private Long param_nowMillis = null;    // list all items, else find next upcoming from now
        public void setParam_nowMillis( Long value ) {
            param_nowMillis = value;
        }

        protected boolean passesFilter(Cursor cursor, long rowID) {
            return true;
        }

        @Override
        public Long[] call() throws Exception
        {
            ArrayList<Long> alarmIds = new ArrayList<>();
            db.open();

            if (param_nowMillis != null) {
                alarmIds.add(db.findUpcomingAlarmId(param_nowMillis));

            } else {
                Cursor cursor = (param_withAlarmState != null)
                        ? db.getAllAlarmsByState(0, param_withAlarmState)
                        : db.getAllAlarms(0, false, param_enabledOnly);

                while (!cursor.isAfterLast())
                {
                    int index = cursor.getColumnIndex((param_withAlarmState != null) ? AlarmDatabaseAdapter.KEY_STATE_ALARMID : AlarmDatabaseAdapter.KEY_ROWID);
                    if (index >= 0) {
                        long alarmId = cursor.getLong(index);
                        if (passesFilter(cursor, alarmId)) {
                            alarmIds.add(alarmId);
                        }
                    }
                    cursor.moveToNext();
                }
                cursor.close();
            }

            db.close();
            return alarmIds.toArray(new Long[0]);
        }
    }

    /**
     * AlarmListObserver
     */
    public static class AlarmListObserver
    {
        private final HashMap<Long, Boolean> items;

        @SuppressLint("UseSparseArrays")
        public AlarmListObserver(Long[] alarmList, AlarmListObserverListener listener)
        {
            this.observerListener = listener;
            items = new HashMap<>();
            for (Long alarmId : alarmList) {
                items.put(alarmId, false);
            }
        }

        public void notify(Long alarmId)
        {
            items.put(alarmId, true);
            if (observerListener != null)
            {
                observerListener.onObservedItem(alarmId);
                if (observedAll()) {
                    observerListener.onObservedAll();
                }
            }
        }

        public boolean observedAll()
        {
            boolean retValue = true;
            for (Boolean value : items.values()) {
                retValue = retValue && value;
            }
            return retValue;
        }

        private final AlarmListObserverListener observerListener;
        public static abstract class AlarmListObserverListener
        {
            public void onObservedItem( Long id ) {}
            public void onObservedAll() {}
        }
    }

}