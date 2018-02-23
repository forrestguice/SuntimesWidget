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

package com.forrestguice.suntimeswidget.getfix;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class BuildPlacesTask extends AsyncTask<Object, Object, Integer>
{
    public static final long MIN_WAIT_TIME = 2000;

    private GetFixDatabaseAdapter db;
    private WeakReference<Context> contextRef;

    private boolean isPaused = false;
    public void pauseTask()
    {
        isPaused = true;
        //Log.d("DEBUG", "BuildPlacesTask paused");
    }
    public void resumeTask()
    {
        isPaused = false;
        //Log.d("DEBUG", "BuildPlacesTask resumed");
    }
    public boolean isPaused()
    {
        return isPaused;
    }

    public BuildPlacesTask(Context context)
    {
        this.contextRef = new WeakReference<Context>(context);
        db = new GetFixDatabaseAdapter(context.getApplicationContext());
    }

    @Override
    protected Integer doInBackground(Object... params)
    {
        int result = 0;
        long startTime = System.currentTimeMillis();
        ArrayList<WidgetSettings.Location> locations = new ArrayList<>();
        try {
            Context context = contextRef.get();
            db.open();
            for (Locale locale : Locale.getAvailableLocales())
            {
                WidgetSettings.Location location = null;
                if (Build.VERSION.SDK_INT >= 17 && context != null)
                {
                    Configuration config = new Configuration(context.getResources().getConfiguration());
                    config.setLocale(locale);

                    Resources resources = context.createConfigurationContext(config).getResources();
                    String label = resources.getString(R.string.default_location_label);
                    String lat = resources.getString(R.string.default_location_latitude);
                    String lon = resources.getString(R.string.default_location_longitude);
                    location = new WidgetSettings.Location(label, lat, lon);
                } // else    // TODO: legacy support

                if (location != null && !locations.contains(location))
                {
                    locations.add(location);
                }
            }

            Collections.sort(locations, new Comparator<WidgetSettings.Location>()
            {
                @Override
                public int compare(WidgetSettings.Location o1, WidgetSettings.Location o2)
                {
                    return o2.getLabel().compareTo(o1.getLabel());  // descending
                }
            });

            Cursor cursor = db.getAllPlaces(0, false);
            for (int i=0; i<locations.size(); i++)
            {
                WidgetSettings.Location location = locations.get(i);
                int p = GetFixDatabaseAdapter.findPlaceByName(location.getLabel(), cursor);
                if (p < 0)    // if not found
                {                 // then add new place
                    db.addPlace(location);
                    result++;
                }
            }

            db.close();

        } catch (SQLException e) {
            Log.e("BuildPlacesTask", "Failed to access database: " + e);
            result = -1;
        }
        long endTime = System.currentTimeMillis();
        while ((endTime - startTime) < MIN_WAIT_TIME || isPaused)
        {
            endTime = System.currentTimeMillis();
        }
        return result;
    }

    @Override
    protected void onPreExecute()
    {
        signalStarted();
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        signalFinished(result);
    }

    /**
     * Event Listener
     */
    private TaskListener taskListener = null;
    public void setTaskListener( TaskListener listener )
    {
        taskListener = listener;
    }
    public void clearTaskListener()
    {
        taskListener = null;
    }
    public static abstract class TaskListener
    {
        public void onStarted() {}
        public void onFinished( Integer result ) {}
    }

    private void signalStarted()
    {
        if (taskListener != null)
            taskListener.onStarted();
    }
    private void signalFinished( Integer result )
    {
        if (taskListener != null)
            taskListener.onFinished(result);
    }

}
