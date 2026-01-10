/**
    Copyright (C) 2021 Forrest Guice
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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.BuildConfig;

import static com.forrestguice.suntimeswidget.alarmclock.SuntimesAlarmsContract.QUERY_ALARMS;
import static com.forrestguice.suntimeswidget.alarmclock.SuntimesAlarmsContract.QUERY_ALARMSTATE;
import static com.forrestguice.suntimeswidget.alarmclock.SuntimesAlarmsContract.QUERY_ALARMSTATE_PROJECTION;
import static com.forrestguice.suntimeswidget.alarmclock.SuntimesAlarmsContract.QUERY_ALARMS_PROJECTION_MIN;

/**
 * SuntimesAlarmsProvider
 * @see SuntimesAlarmsContract
 */
public class SuntimesAlarmsProvider extends ContentProvider
{
    private static final int URIMATCH_ALARMS = 0;
    private static final int URIMATCH_ALARM = 10;
    private static final int URIMATCH_ALARM_STATE = 20;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY(), QUERY_ALARMS, URIMATCH_ALARMS);                          // content://AUTHORITY/alarms
        uriMatcher.addURI(AUTHORITY(), QUERY_ALARMS + "/*", URIMATCH_ALARM);              // content://AUTHORITY/alarms/[alarmID]
        uriMatcher.addURI(AUTHORITY(), QUERY_ALARMSTATE + "/*", URIMATCH_ALARM_STATE);    // content://AUTHORITY/state/[alarmID]
    }

    private static String AUTHORITY() {
        return BuildConfig.SUNTIMES_AUTHORITY_ROOT + ".alarm.provider";
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * query
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        Cursor retValue = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_ALARMS:
                Log.d(getClass().getSimpleName(), "URIMATCH_ALARMS");
                retValue = queryAlarms(null, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_ALARM:
                Log.d(getClass().getSimpleName(), "URIMATCH_ALARM");
                retValue = queryAlarms(uri.getLastPathSegment(), uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_ALARM_STATE:
                Log.d(getClass().getSimpleName(), "URIMATCH_ALARM_STATE");
                retValue = queryAlarmState(uri.getLastPathSegment(), uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                Log.e(getClass().getSimpleName(), "Unrecognized URI! " + uri);
                break;
        }
        return retValue;
    }

    /**
     * queryAlarms
     */
    private Cursor queryAlarms(String alarmID, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_ALARMS_PROJECTION_MIN);
        MatrixCursor retValue = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context.getApplicationContext());
            db.open();
            Cursor cursor = ((alarmID != null)
                    ? db.getAlarm(Long.parseLong(alarmID), columns, selection, selectionArgs)
                    : db.getAllAlarms(0, columns, selection, selectionArgs));
            copyCursorToMatrixCursor(columns, cursor, retValue);
            cursor.close();
            db.close();
        }
        return retValue;
    }

    /**
     * queryAlarmState
     */
    private Cursor queryAlarmState(@Nullable String alarmID, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_ALARMSTATE_PROJECTION);
        MatrixCursor retValue = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null && alarmID != null)
        {
            AlarmDatabaseAdapter db = new AlarmDatabaseAdapter(context.getApplicationContext());
            db.open();
            Cursor cursor = db.getAlarmState(Long.parseLong(alarmID), selection, selectionArgs);
            copyCursorToMatrixCursor(columns, cursor, retValue);
            cursor.close();
            db.close();
        }
        return retValue;
    }

    private void copyCursorToMatrixCursor(@NonNull String[] columns, @Nullable Cursor cursor0, @NonNull MatrixCursor cursor1)
    {
        if (cursor0 != null) {
            cursor0.moveToFirst();
            while (!cursor0.isAfterLast()) {
                cursor1.addRow(createRowFromCursor(columns, cursor0));
                cursor0.moveToNext();
            }
        }
    }

    private Object[] createRowFromCursor(String[] columns, @NonNull Cursor cursor)
    {
        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            if (Build.VERSION.SDK_INT >= 11)
            {
                switch (cursor.getType(i))
                {
                    case Cursor.FIELD_TYPE_INTEGER:
                        row[i] = cursor.getLong(i);
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        row[i] = cursor.getDouble(i);
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        row[i] = cursor.getBlob(i);
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        row[i] = cursor.getString(i);
                        break;
                    case Cursor.FIELD_TYPE_NULL:
                    default:
                        row[i] = null;
                        break;
                }
            } else {
                row[i] = cursor.getString(i);
            }
        }
        return row;
    }

}