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

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.Location;

public class LocationListTask extends AsyncTask<Object, Object, LocationListTask.LocationListTaskResult>
{
    private GetFixDatabaseAdapter db;
    private Location selected;

    public LocationListTask(Context context, Location selected)
    {
        db = new GetFixDatabaseAdapter(context.getApplicationContext());
        this.selected = selected;
    }

    @Override
    protected LocationListTaskResult doInBackground(Object... params)
    {
        String selectedPlaceName = selected.getLabel();
        String selectedPlaceLat = selected.getLatitude();
        String selectedPlaceLon = selected.getLongitude();
        String selectedPlaceAlt = selected.getAltitude();

        db.open();
        Cursor cursor = db.getAllPlaces(0, true);
        if (GetFixDatabaseAdapter.findPlaceByName(selectedPlaceName, cursor) == -1)
        {
            Log.i("LocationListTask", "Place not found, adding it.. " + selectedPlaceName + ":" + selectedPlaceLat + "," + selectedPlaceLon + " [" +  selectedPlaceAlt + "]");
            db.addPlace(selected);
            closeCursor(cursor);
            cursor = db.getAllPlaces(0, true);
        }

        Cursor selectedCursor = db.getPlace(selectedPlaceName, true);
        String selectedLat = selectedCursor.getString(selectedCursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_LATITUDE));
        String selectedLon = selectedCursor.getString(selectedCursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_LONGITUDE));
        String selectedAlt = selectedCursor.getString(selectedCursor.getColumnIndex(GetFixDatabaseAdapter.KEY_PLACE_ALTITUDE));
        closeCursor(selectedCursor);

        if (!selectedLat.equals(selectedPlaceLat) || !selectedLon.equals(selectedPlaceLon) || !selectedAlt.equals(selectedPlaceAlt))
        {
            Log.i("LocationListTask", "Place modified; saving it.. " + selectedPlaceName + ":" + selectedPlaceLat + "," + selectedPlaceLon + " [" +  selectedPlaceAlt + "]");
            db.updatePlace(selected);
            closeCursor(cursor);
            cursor = db.getAllPlaces(0, true);
        }

        LocationListTaskResult result = null;
        if (cursor != null)
        {
            int selectedIndex = GetFixDatabaseAdapter.findPlaceByName(selected.getLabel(), cursor);
            if (selectedIndex < 0) {
                Log.w("LocationListTask", "Place selection not found! " + selectedPlaceName + ":" + selectedPlaceLat + "," + selectedPlaceLon + " [" + selectedPlaceAlt + "]");
            } // else Log.d("LocationListTask", "Place selection: " + selectedPlaceName + ":" + selectedPlaceLat + "," + selectedPlaceLon + " [" +  selectedPlaceAlt + "]");

            result = new LocationListTaskResult(cursor, selectedIndex);
        }
        db.close();
        return result;    // the caller has responsibility for eventually closing returned Cursor
    }

    private void closeCursor(@Nullable Cursor cursor) {
        if (cursor != null) {
             cursor.close();
        }
    }

    @Override
    protected void onPostExecute(LocationListTaskResult result)
    {
        if (result != null)
        {
            signalOnLoaded(result);
        }
    }

    /**
     *
     */
    public abstract static class LocationListTaskListener
    {
        public abstract void onLoaded( @NonNull Cursor result, int selectedIndex );
    }

    public LocationListTaskListener getTaskListener()
    {
        return taskListener;
    }
    public void setTaskListener( LocationListTaskListener listener )
    {
        taskListener = listener;
    }

    private LocationListTaskListener taskListener;
    private void signalOnLoaded( LocationListTaskResult result )
    {
        if (taskListener != null)
        {
            taskListener.onLoaded(result.getCursor(), result.getIndex());
        }
    }

    /**
     *
     */
    public static class LocationListTaskResult
    {
        private final Cursor cursor;
        public Cursor getCursor() { return cursor; }

        private final int index;
        public int getIndex() { return index; }

        public LocationListTaskResult( Cursor cursor, int index )
        {
            this.cursor = cursor;
            this.index = index;
        }
    }

}
