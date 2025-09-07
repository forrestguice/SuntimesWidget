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
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimeswidget.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An AsyncTask that registers a LocationListener, starts listening for
 * location updates, and then waits a predetermined amount of time for a
 * good location fix to be acquired; updates progress.
 */
@SuppressWarnings("Convert2Diamond")
public class GetFixTask extends AsyncTask<Object, Location, Location>
{
    public static final String TAG = "GetFixTask";

    public static final int MIN_ELAPSED = 1000 * 3;        // wait at least 3s before settling on a fix
    public static final int MAX_ELAPSED = 1000 * 60;       // wait at most a minute for a fix
    public static final int MAX_AGE = 1000 * 60 * 15;      // consider fixes over 15min be "too old"
    public static final int MAX_AGE_NONE = 0;
    public static final int MAX_AGE_ANY = -1;

    public static final String FUSED_PROVIDER = "fused";    // LocationManager.FUSED_PROVIDER (api31+)
    public static final String[] LOCATION_PROVIDERS = new String[] { LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, FUSED_PROVIDER };

    private final String[] locationProviders;
    protected String[] initLocationProviders(@NonNull Context context)
    {
        List<String> providers = new ArrayList<String>(Arrays.asList(LOCATION_PROVIDERS));
        List<String> notRequested = new ArrayList<>();
        for (String provider : providers) {
            if (!LocationHelperSettings.isProviderRequested(context, provider)) {
                notRequested.add(provider);
            }
        }
        providers.removeAll(notRequested);
        if (providers.isEmpty()) {
            providers.add(LocationManager.PASSIVE_PROVIDER);  // passive mode when none requested
        }
        return providers.toArray(new String[0]);
    }

    private final WeakReference<LocationHelper> helperRef;
    public GetFixTask(Context parent, LocationHelper helper)
    {
        locationManager = (LocationManager)parent.getSystemService(Context.LOCATION_SERVICE);
        this.helperRef = new WeakReference<LocationHelper>(helper);
        this.locationProviders = initLocationProviders(parent);
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
    private final LocationManager locationManager;
    private final LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            if (location != null)
            {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onLocationChanged [" + location.getProvider() + "]: " + location.toString());
                }

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
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "onLocationChanged: found location: " + locationAge + " <= " + maxAge);
                        }
                    } else if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onLocationChanged: ignoring location (too old): " + locationAge + " > " + maxAge);
                    }
                } else {
                    bestFix.addToFilter(location, locationTime);
                    onProgressUpdate(bestFix.getLocation());
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onLocationChanged: added location: " + locationAge + " <= " + maxAge);
                    }
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

    public static long calculateLocationAge(android.location.Location location)
    {
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
        return now - locationTime;
    }

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
                String[] providers = passiveMode
                        ? new String[] { LocationManager.PASSIVE_PROVIDER }
                        : locationProviders;  //: new String[] { LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, FUSED_PROVIDER };

                if (maxAge != MAX_AGE_NONE)
                {
                    for (String provider : providers) {
                        try {
                            if (locationManager.isProviderEnabled(provider)) {
                                locationListener.onLocationChanged(locationManager.getLastKnownLocation(provider));
                            }
                        } catch (IllegalArgumentException | SecurityException e) {
                            Log.e(TAG, "unable to access location provider; " + provider + "; " + e);
                        }
                    }
                }

                if (bestFix == null)
                {
                    for (String provider : providers)
                    {
                        try {
                            if (locationManager.isProviderEnabled(provider)) {
                                requestLocationUpdates(locationManager, provider, locationListener);
                            }
                        } catch (IllegalArgumentException | SecurityException e) {
                            Log.e(TAG, "unable to access location provider; " + provider + "; " + e);
                        }
                    }
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

    protected void requestLocationUpdates(LocationManager locationManager, String provider, LocationListener listener)
    {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "requesting location updates from: " + provider);
        }
        locationManager.requestLocationUpdates(provider, 0, 0, listener);
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
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "stopped location listener");
            }
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
            uiObj.onResult(result, elapsedTime, false);
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
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "stopped location listener");
            }
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
            uiObj.onResult(result, elapsedTime, true);
        }
        signalCancelled();
    }

    private final ArrayList<GetFixTaskListener> listeners = new ArrayList<GetFixTaskListener>();
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
     * Implementation based on "simple Kalman filter" code by Stochastically in the stackoverflow answer
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
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "initFilter: init to " + location.toString());
            }
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
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "addToFilter: accuracy now " + location.getAccuracy());
                }
            }
        }
    }
}
