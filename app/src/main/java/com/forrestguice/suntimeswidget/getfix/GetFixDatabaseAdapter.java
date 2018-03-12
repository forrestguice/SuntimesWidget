/**
    Copyright (C) 2014-2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.getfix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class GetFixDatabaseAdapter
{
    private static final String DATABASE_NAME = "suntimes";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ROWID = "_id";
    public static final String DEF_ROWID = KEY_ROWID + " integer primary key autoincrement";

    public static final String KEY_PLACE_NAME = "name";
    public static final String DEF_PLACE_NAME = KEY_PLACE_NAME + " text not null";

    public static final String KEY_PLACE_LATITUDE = "latitude";
    public static final String DEF_PLACE_LATITUDE = KEY_PLACE_LATITUDE + " text not null";

    public static final String KEY_PLACE_LONGITUDE = "longitude";
    public static final String DEF_PLACE_LONGITUDE = KEY_PLACE_LONGITUDE + " text not null";

    public static final String KEY_PLACE_ALTITUDE = "altitude";
    public static final String DEF_PLACE_ALTITUDE = KEY_PLACE_ALTITUDE + " text";

    public static final String KEY_PLACE_COMMENT = "comment";
    public static final String DEF_PLACE_COMMENT = KEY_PLACE_COMMENT + " text";

    private static final String TABLE_PLACES = "places";
    private static final String TABLE_PLACES_CREATE_COLS = DEF_ROWID + ", "
                                                         + DEF_PLACE_NAME + ", "
                                                         + DEF_PLACE_LATITUDE + ", "
                                                         + DEF_PLACE_LONGITUDE + ", "
                                                         + DEF_PLACE_ALTITUDE + ", "
                                                         + DEF_PLACE_COMMENT;
    private static final String TABLE_PLACES_CREATE = "create table " + TABLE_PLACES + " (" + TABLE_PLACES_CREATE_COLS + ");";

    private static final String[] QUERY_PLACES_MINENTRY = new String[] {KEY_ROWID, KEY_PLACE_NAME};
    private static final String[] QUERY_PLACES_FULLENTRY = new String[] {KEY_ROWID, KEY_PLACE_NAME, KEY_PLACE_LATITUDE, KEY_PLACE_LONGITUDE, KEY_PLACE_ALTITUDE, KEY_PLACE_COMMENT};

    /**
     *
     */
    private final Context context;
    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;

    public GetFixDatabaseAdapter(Context context)
    {
        this.context = context;
    }

    /**
     * Open the database
     * @return a reference the (now open) database adapter
     * @throws SQLException if failed to open
     */
    public GetFixDatabaseAdapter open() throws SQLException
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
     * Get the number of places in the database
     * @return number of places
     */
    public int getPlaceCount()
    {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLACES, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * Get a Cursor over places in the database.
     * @param n get first n results (n <= 0 for complete list)
     * @param fullEntry true get all place data, false get display name only
     * @return a Cursor into the database
     */
    public Cursor getAllPlaces(int n, boolean fullEntry)
    {
        String[] QUERY = (fullEntry) ? QUERY_PLACES_FULLENTRY : QUERY_PLACES_MINENTRY;
        Cursor cursor =  (n > 0) ? database.query( TABLE_PLACES, QUERY, null, null, null, null, "_id DESC", n+"" )
                                 : database.query( TABLE_PLACES, QUERY, null, null, null, null, "_id DESC" );
        if (cursor != null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Get a place from the database
     * @param row the rowID to get
     * @return a Cursor into the database
     * @throws SQLException if query failed
     */
    public Cursor getPlace(long row) throws SQLException
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String[] QUERY = QUERY_PLACES_FULLENTRY;
        Cursor cursor = database.query( true, TABLE_PLACES, QUERY,
                                        KEY_ROWID + "=" + row, null,
                                        null, null, null, null );
        if (cursor != null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getPlace(String name, boolean fullEntry) throws SQLException
    {
        String[] QUERY = (fullEntry) ? QUERY_PLACES_FULLENTRY : QUERY_PLACES_MINENTRY;
        Cursor cursor = database.query( true, TABLE_PLACES, QUERY,
                KEY_PLACE_NAME + " = ?", new String[] { name },
                null, null, null, null );
        if (cursor != null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Add a place to the database.
     * @param place a Location object describing the place
     * @return the rowID of the newly added place or -1 if an error
     */
    public long addPlace( WidgetSettings.Location place )
    {
        ContentValues values = new ContentValues();
        values.put(KEY_PLACE_NAME, place.getLabel());
        values.put(KEY_PLACE_LATITUDE, place.getLatitude());
        values.put(KEY_PLACE_LONGITUDE, place.getLongitude());
        values.put(KEY_PLACE_ALTITUDE, place.getAltitude());
        values.put(KEY_PLACE_COMMENT, "");
        return database.insert(TABLE_PLACES, null, values);
    }

    public void updatePlace( WidgetSettings.Location place )
    {
        ContentValues values = new ContentValues();
        values.put(KEY_PLACE_NAME, place.getLabel());
        values.put(KEY_PLACE_LATITUDE, place.getLatitude());
        values.put(KEY_PLACE_LONGITUDE, place.getLongitude());
        values.put(KEY_PLACE_ALTITUDE, place.getAltitude());
        values.put(KEY_PLACE_COMMENT, "");
        database.update(TABLE_PLACES, values,  "name = ?", new String[] { place.getLabel() });
    }

    public static int findPlaceByName(String name, Cursor cursor)
    {
        int position = -1;
        if (cursor == null)
            return position;

        for (int i = 0; i < cursor.getCount(); i++)
        {
            cursor.moveToPosition(i);
            String itemName = cursor.getString(1);
            if (itemName.contentEquals(name))
            {
                position = i;
                break;
            }
        }
        return position;
    }

    public String addPlaceCSV_header()
    {
        String separator = ", ";
        //noinspection UnnecessaryLocalVariable
        String line = KEY_PLACE_NAME + separator +
                KEY_PLACE_LATITUDE + separator +
                KEY_PLACE_LONGITUDE + separator +
                KEY_PLACE_ALTITUDE + separator +
                KEY_PLACE_COMMENT;
        return line;
    }
    public String addPlaceCSV_row( ContentValues place )
    {
        String separator = ", ";
        //noinspection UnnecessaryLocalVariable
        String line = place.getAsString(KEY_PLACE_NAME) + separator +
                      place.getAsString(KEY_PLACE_LATITUDE) + separator +
                      place.getAsString(KEY_PLACE_LONGITUDE) + separator +
                      place.getAsString(KEY_PLACE_ALTITUDE) + separator +
                      place.getAsString(KEY_PLACE_COMMENT);
        return line;
    }

    /**
     * Remove a place from the database
     * @param row the rowID to remove
     * @return true if the place was removed
     */
    public boolean removePlace(long row)
    {
        return database.delete(TABLE_PLACES, KEY_ROWID + "=" + row, null) > 0;
    }


    /**
     * Clear all places from the database
     * @return true places have been cleared
     */
    public boolean clearPlaces()
    {
        return database.delete(TABLE_PLACES, null, null) > 0;
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
                    db.execSQL(TABLE_PLACES_CREATE);
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
