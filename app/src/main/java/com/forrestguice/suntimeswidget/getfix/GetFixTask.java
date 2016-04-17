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

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * An AsyncTask that registers a LocationListener, starts listening for
 * gps updates, and then waits a predetermined amount of time for a
 * good location fix to be acquired; updates progress.
 */
public class GetFixTask extends AsyncTask<String, Location, Location>
{
    private static final int MIN_ELAPSED = 1000 * 5;        // wait at least 5s before settling on a fix
    private static final int MAX_ELAPSED = 1000 * 60;       // wait at most a minute for a fix
    private static final int MAX_AGE = 1000 * 60 * 5;       // consider fixes over 5min be "too old"

    private GetFixHelper helper;
    private Activity myParent;
    private GetFixUI uiObj;
    public GetFixTask(Activity parent, GetFixHelper helper)
    {
        myParent = parent;
        this.helper = helper;
        uiObj = helper.getUI();
    }

    private long startTime, stopTime, elapsedTime;
    private Location bestFix, lastFix;
    private LocationManager locationManager;
    private LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            lastFix = location;
            if (isBetterFix(lastFix, bestFix))
            {
                bestFix = lastFix;
                onProgressUpdate(bestFix);
            }
        }

        private boolean isBetterFix(Location location, Location location2)
        {
            if (location2 == null)
            {
                return true;

            } else if (location != null) {
                if ((location.getTime() - location2.getTime()) > MAX_AGE)
                {
                    return true;  // more than 5min since last fix; assume the latest fix is better

                } else if (location.getAccuracy() < location2.getAccuracy()) {
                    return true;  // accuracy is a measure of radius of certainty; smaller values are more accurate
                }
            }
            return false;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };

    @Override
    protected void onPreExecute()
    {
        uiObj.gpsButton.setVisibility(View.GONE);
        uiObj.showProgress(true);
        uiObj.enableUI(false);

        bestFix = null;
        helper.gettingFix = true;
        elapsedTime = 0;
        startTime = stopTime = System.currentTimeMillis();
    }

    @Override
    protected Location doInBackground(String... params)
    {
        locationManager = (LocationManager)myParent.getSystemService(Context.LOCATION_SERVICE);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable()
        {
            public void run()
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        });


        while (elapsedTime < MAX_ELAPSED && !isCancelled())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;

            if (bestFix != null && elapsedTime > MIN_ELAPSED)
            {
                break;
            }
        }
        return bestFix;
    }

    @Override
    protected void onProgressUpdate(Location... locations)
    {
        uiObj.updateUI(locations);
    }

    @Override
    protected void onPostExecute(Location result)
    {
        locationManager.removeUpdates(locationListener);
        helper.gettingFix = false;

        uiObj.showProgress(false);
        uiObj.enableUI(true);
        uiObj.onResult(result);
    }

    @Override
    protected void onCancelled(Location result)
    {
        locationManager.removeUpdates(locationListener);
        helper.gettingFix = false;

        uiObj.showProgress(false);
        uiObj.enableUI(true);
        uiObj.onResult(result);
    }
}
