/**
    Copyright (C) 2017 Forrest Guice
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
import android.database.DatabaseUtils;
import android.util.Log;

import com.forrestguice.suntimeswidget.ExportTask;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportPlacesTask extends ExportTask
{
    private Cursor cursor;
    private GetFixDatabaseAdapter db;

    public ExportPlacesTask(Context context, String exportTarget)
    {
        super(context, exportTarget);
        ext = ".csv";
    }
    public ExportPlacesTask(Context context, String exportTarget, boolean useExternalStorage, boolean saveToCache)
    {
        super(context, exportTarget, useExternalStorage, saveToCache);
        ext = ".csv";
    }

    @Override
    public boolean export( Context context, BufferedOutputStream out ) throws IOException
    {
        cursor = null;
        db = new GetFixDatabaseAdapter(context.getApplicationContext());
        db.open();
        numEntries = db.getPlaceCount();
        out = new BufferedOutputStream(new FileOutputStream(exportFile));
        cursor = db.getAllPlaces(-1, true);
        return exportDatabase(db, cursor, out);
    }

    @Override
    public void cleanup( Context context )
    {
        if (cursor != null)
        {
            cursor.close();
        }
        if (db != null)
        {
            db.close();
        }
    }

    /**
     * @param db a GetFixDatabaseAdapter helper
     * @param cursor a database Cursor pointing to records to export
     * @param out a BufferedOutputStream (open and ready) to export to
     * @return true export was successful, false otherwise
     * @throws IOException if failed to write to out
     */
    private boolean exportDatabase( GetFixDatabaseAdapter db, Cursor cursor, BufferedOutputStream out ) throws IOException
    {
        if (cursor == null)
        {
            Log.w("ExportPlaces", "Canceling export; the database returned a null cursor.");
            return false;
        }

        String csvHeader = db.addPlaceCSV_header() + newLine;
        out.write(csvHeader.getBytes());

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            ContentValues entryValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, entryValues);

            String csvRow = db.addPlaceCSV_row(entryValues) + newLine;
            out.write(csvRow.getBytes());

            cursor.moveToNext();
            i++;

            String msg = entryValues.getAsString(GetFixDatabaseAdapter.KEY_PLACE_NAME);
            ExportProgress progressObj = new ExportProgress(i, numEntries, msg);
            publishProgress(progressObj);
        }
        out.flush();
        return true;
    }

}
