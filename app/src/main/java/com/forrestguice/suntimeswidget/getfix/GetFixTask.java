/**
    Copyright (C) 2014-2019 Forrest Guice
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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An AsyncTask that registers a LocationListener, starts listening for
 * gps updates, and then waits a predetermined amount of time for a
 * good location fix to be acquired; updates progress.
 */
@SuppressWarnings("Convert2Diamond")
public class GetFixTask extends AsyncTask<Object, Location, Location>
{
    public static final String TAG = "GetFixTask";

    public static final int MIN_ELAPSED = 1000 * 5;        // wait at least 5s before settling on a fix
    public static final int MAX_ELAPSED = 1000 * 60;       // wait at most a minute for a fix
    public static final int MAX_AGE = 1000 * 60 * 5;       // consider fixes over 5min be "too old"
    public static final int MAX_AGE_NONE = 0;
    public static final int MAX_AGE_ANY = -1;

    private WeakReference<LocationHelper> helperRef;
    public GetFixTask(Context parent, LocationHelper helper)
    {
        locationManager = (LocationManager)parent.getSystemService(Context.LOCATION_SERVICE);
        this.helperRef = new WeakReference<LocationHelper>(helper);
    }

    public AsyncTask<Object, Location, Location> executeTask(Object... params)
    {
        if (Build.VERSION.SDK_INT >= 11) {
            return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            return execute(params);
        }
    }

    /**
     * Property: minimum amount of time that must elapse while searching for a location.
     */
    private int minElapsed = MIN_ELAPSED;
    public int getMinElapsed()
    {
        return minElapsed;
    }
    public void setMinElapsed( int timeInMs )
    {
        minElapsed = timeInMs;
    }

    /**
     * Property: maximum amount of time that may elapsed while searching for a location.
     */
    private int maxElapsed = MAX_ELAPSED;
    public int getMaxElapsed()
    {
        return maxElapsed;
    }
    public void setMaxElapsed( int timeInMs )
    {
        maxElapsed = timeInMs;
    }

    /**
     * Property: maximum amount of time a fix may age before its considered out-of-date.
     */
    private int maxAge = MAX_AGE;
    public int getMaxAge()
    {
        return maxAge;
    }
    public void setMaxAge( int timeInMs )
    {
        maxAge = timeInMs;
    }

    private long startTime, stopTime, elapsedTime;
    private FilteredLocation bestFix;
    private LocationManager locationManager;
    private LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            if (location != null)
            {
                Log.d(TAG, "onLocationChanged [" + location.getProvider() + "]: " + location.toString());

                long now;
                long locationTime;
                if (Build.VERSION.SDK_INT >= 17)
                {
                    now = TimeUnit.NANOSECONDS.toMillis(SystemClock.elapsedRealtimeNanos());
                    locationTime = TimeUnit.NANOSECONDS.toMillis(location.getElapsedRealtimeNanos());
                } else {
                    now = System.currentTimeMillis();
                    locationTime = location.getTime();
                }
                long locationAge = now - locationTime;

                if (bestFix == null) {
                    if (maxAge == MAX_AGE_ANY || maxAge == MAX_AGE_NONE || locationAge <= maxAge) {
                        bestFix = new FilteredLocation(location, locationTime, maxAge, 3);
                        onProgressUpdate(bestFix.getLocation());
                    }
                } else {
                    bestFix.addToFilter(location, locationTime);
                    onProgressUpdate(bestFix.getLocation());
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };

    /**
     * Prepares UI objects, signals onStarted listeners, and (re)sets flags in preparation for getting a location.
     */
    @Override
    protected void onPreExecute()
    {
        final LocationHelper helper = helperRef.get();
        if (helper != null)
        {
            GetFixUI uiObj = helper.getUI();
            uiObj.onStart();
            uiObj.showProgress(true);
            uiObj.enableUI(false);
        }

        signalStarted();
        if (helper != null)
        {
            helper.setGettingFix(true);
        }
        bestFix = null;
        elapsedTime = 0;
        startTime = stopTime = System.currentTimeMillis();
    }

    /**
     * 1. Checks each LocationProvider (gps, net, passive) and gets lastPosition from each.
     * 2. Starts listening for location updates on providers (based on availability).
     * 3. Busy spin (if necessary) so the task always consumes at least minElapsed time.
     * @return the "best fix" we were able to obtain (potentially null)
     */
    @Override
    protected Location doInBackground(Object... params)
    {
        final boolean passiveMode = (params.length > 0) ? (Boolean)params[0]
                                                        : false;

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable()
        {
            public void run()
            {
                try {
                    boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    boolean passiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

                    if (maxAge != MAX_AGE_NONE)
                    {
                        locationListener.onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                        locationListener.onLocationChanged(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                        locationListener.onLocationChanged(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
                    }

                    if (passiveMode && passiveEnabled)
                    {
                        // passive provider only
                        Log.d(TAG, "starting location listener; now requesting updates from PASSIVE_PROVIDER...");
                        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListener);

                    } else if (!gpsEnabled && netEnabled) {
                        // network provider only
                        Log.d(TAG, "starting location listener; now requesting updates from NETWORK_PROVIDER...");
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                    } else if (gpsEnabled && !netEnabled) {
                        // gps provider only
                        Log.d(TAG, "starting location listener; now requesting updates from GPS_PROVIDER...");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    } else //noinspection ConstantConditions
                        if (gpsEnabled && netEnabled) {
                        // gps + network provider
                        Log.d(TAG, "starting location listener; now requesting updates from GPS_PROVIDER && NETWORK_PROVIDER...");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                    } else if (passiveEnabled) {
                        // fallback to passive provider
                        Log.d(TAG, "starting location listener; now requesting updates from PASSIVE_PROVIDER...");
                        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListener);

                    } else {
                        // err: no providers at all!
                        Log.e(TAG, "unable to start locationListener ... No usable LocationProvider found! a provider should be enabled before starting this task.");
                    }

                } catch (SecurityException e) {
                    Log.e(TAG, "unable to start locationListener ... Permissions! we don't have them.. checkPermissions should be called before starting this task. " + e);
                }
            }
        });

        while (elapsedTime < maxElapsed && !isCancelled())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // e.printStackTrace();   // silent
            }

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;

            if (bestFix != null && elapsedTime > minElapsed) {
                break;
            }
        }
        return ((bestFix != null) ? bestFix.getLocation() : null);
    }

    /**
     * @param locations a list of android.location.Location to be displayed during progress update
     */
    @Override
    protected void onProgressUpdate(Location... locations)
    {
        final LocationHelper helper = helperRef.get();
        if (helper != null)
        {
            GetFixUI uiObj = helper.getUI();
            uiObj.updateUI(locations);
        }
    }

    /**
     * Stops location updates, triggers a final progress update, resets ui, and signals onFinished listeners.
     * @param result the "best fix" that could be obtained (potentially null)
     */
    @Override
    protected void onPostExecute(Location result)
    {
        try {
            locationManager.removeUpdates(locationListener);
            Log.d(TAG, "stopped location listener");
        } catch (SecurityException e) {
            Log.e(TAG, "unable to stop locationListener ... Permissions! we don't have them... checkPermissions should be called before using this task! " + e);
        }

        final LocationHelper helper = helperRef.get();
        if (helper != null)
        {
            helper.setGettingFix(false);

            GetFixUI uiObj = helper.getUI();
            uiObj.showProgress(false);
            uiObj.enableUI(true);
            uiObj.onResult(result, false);
        }
        signalFinished(result);
    }

    /**
     * Same as onPostExecute except onCancelled is signalled instead of onFinished.
     * @param result the "best fix" that could be obtained (potentially null)
     */
    @Override
    protected void onCancelled(Location result)
    {
        try {
            locationManager.removeUpdates(locationListener);
            Log.d(TAG, "stopped location listener");
        } catch (SecurityException e) {
            Log.e(TAG, "unable to stop locationListener ... Permissions! we don't have them... checkPermissions should be called before using this task! " + e);
        }

        LocationHelper helper = helperRef.get();
        if (helper != null)
        {
            helper.setGettingFix(false);

            GetFixUI uiObj = helper.getUI();
            uiObj.showProgress(false);
            uiObj.enableUI(true);
            uiObj.onResult(result, true);
        }
        signalCancelled();
    }

    private ArrayList<GetFixTaskListener> listeners = new ArrayList<GetFixTaskListener>();
    public void addGetFixTaskListener( GetFixTaskListener listener )
    {
        if (!listeners.contains(listener))
        {
            listeners.add(listener);
        }
    }
    public void removeGetFixTaskListener( GetFixTaskListener listener )
    {
        listeners.remove(listener);
    }
    public void addGetFixTaskListeners( List<GetFixTaskListener> listeners )
    {
        for (GetFixTaskListener listener : listeners)
        {
            addGetFixTaskListener(listener);
        }
    }

    protected void signalStarted()
    {
        for (GetFixTaskListener listener : listeners)
        {
            if (listener != null)
                listener.onStarted();
        }
    }
    protected void signalFinished(Location result)
    {
        for (GetFixTaskListener listener : listeners)
        {
            if (listener != null)
                listener.onFinished(result);
        }
    }
    protected void signalCancelled()
    {
        for (GetFixTaskListener listener : listeners)
        {
            if (listener != null)
                listener.onCancelled();
        }
    }

    /**
     * FilteredLocation
     *
     * Implemention based on "simple Kalman filter" code by Stochastically in the stackoverflow answer
     * at https://stackoverflow.com/questions/1134579/smooth-gps-data
     */
    public static class FilteredLocation
    {
        private Location location;
        private double variance;
        private long locationTime;  // millis
        private long maxAge;        // millis
        private float q;        // meters per second
        private int c = 0;

        public FilteredLocation(Location location0, long locationTime0, long maxAge0, float q0)
        {
            maxAge = maxAge0;
            initFilter(location0, locationTime0, q0);
        }

        public Location getLocation() {
            return location;
        }

        public int getCount() {
            return c;
        }

        public void initFilter(Location location0, long locationTime0, float q0)
        {
            float accuracy0 = location0.getAccuracy();
            variance = Math.pow((accuracy0 < 1 ? 1 : accuracy0), 2);
            location = new Location(location0);
            locationTime = locationTime0;
            q = q0;
            c = 1;
            Log.d(TAG, "initFilter: init to " + location.toString());
        }

        public void addToFilter(Location location1, long locationTime1)
        {
            long timeDiff = locationTime1 - locationTime;
            if (maxAge > 0 && timeDiff > maxAge) {
                initFilter(location1, locationTime1, q);

            } else {
                if (timeDiff > 0) {
                    variance += (timeDiff * q * q) / 1000d;
                    locationTime = locationTime1;
                }

                float accuracy1 = location1.getAccuracy();
                double k = variance / (variance + Math.pow((accuracy1 < 1 ? 1 : accuracy1), 2));
                variance *= (1 - k);
                c++;

                location.setLatitude(location.getLatitude() + (k * (location1.getLatitude() - location.getLatitude())));
                location.setLongitude(location.getLongitude() + (k * (location1.getLongitude() - location.getLongitude())));
                location.setAltitude(location.getAltitude() + (k * (location1.getAltitude() - location.getAltitude())));
                location.setAccuracy((float)Math.sqrt(variance));
                Log.d(TAG, "addToFilter: accuracy now " + location.getAccuracy());
            }
        }
    }
}
