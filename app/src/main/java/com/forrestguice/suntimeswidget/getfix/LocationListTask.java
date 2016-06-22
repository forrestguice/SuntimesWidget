/**
    Copyright (C) 2014 Forrest Guice
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
import android.util.Log;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class LocationListTask extends AsyncTask<Object, Object, Cursor>
{
    private GetFixDatabaseAdapter db;
    private WidgetSettings.Location selected;

    public LocationListTask(Context context, WidgetSettings.Location selected)
    {
        db = new GetFixDatabaseAdapter(context.getApplicationContext());
        this.selected = selected;
    }

    @Override
    protected Cursor doInBackground(Object... params)
    {
        String selectedPlaceName = selected.getLabel();
        String selectedPlaceLat = selected.getLatitude();
        String selectedPlaceLon = selected.getLongitude();

        db.open();
        Cursor result = db.getAllPlaces(0, true);
        if (GetFixDatabaseAdapter.findPlaceByName(selectedPlaceName, result) == -1)
        {
            Log.d("LocationListTask", "Place not found, adding it; " + selectedPlaceName + ":" + selectedPlaceLat + "," + selectedPlaceLon);
            db.addPlace(selected);
            result = db.getAllPlaces(0, true);
        }

        Cursor selectedCursor = db.getPlace(selectedPlaceName, true);
        String selectedLat = selectedCursor.getString(2);
        String selectedLon = selectedCursor.getString(3);
        if (!selectedLat.equals(selectedPlaceLat) || !selectedLon.equals(selectedPlaceLon))
        {
            db.updatePlace(selected);
            result = db.getAllPlaces(0, true);
        }

        db.close();
        return result;
    }

    @Override
    protected void onPostExecute(Cursor result)
    {
        if (result != null)
        {
            int index = GetFixDatabaseAdapter.findPlaceByName(selected.getLabel(), result);
            signalOnLoaded(result, index);
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
    private void signalOnLoaded(Cursor result, int selectedIndex )
    {
        if (taskListener != null)
        {
            taskListener.onLoaded(result, selectedIndex);
        }
    }

}
