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

import android.annotation.TargetApi;
import android.content.Context;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.getfix.GetFixUI.LocationProgress;
import com.forrestguice.util.concurrent.ProgressCallable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An AsyncTask that registers a LocationListener, starts listening for
 * location updates, and then waits a predetermined amount of time for a
 * good location fix to be acquired; updates progress.
 */
@SuppressWarnings("Convert2Diamond")
public class GetFixTask extends ProgressCallable<LocationProgress, Location> // AsyncTask<Object, LocationProgress, Location>
{
    public static final String TAG = "LocationTask";

    public static final int MIN_ELAPSED_FF = 1500;           // wait at least 1s before allowing finish (counted from time of "first fix")
    public static final int MIN_ELAPSED = 1000 * 3;        // wait at least 3s before allowing finish (counted from request for update)
    public static final int MAX_ELAPSED = 1000 * 60;       // wait at most a minute for a fix
    public static final int MAX_AGE = 1000 * 60 * 15;      // consider fixes over 15min be "too old"
    public static final int MAX_AGE_NONE = 0;
    public static final int MAX_AGE_ANY = -1;

    public static final String FUSED_PROVIDER = "fused";    // LocationManager.FUSED_PROVIDER (api31+)
    public static final String[] LOCATION_PROVIDERS = new String[] { LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, FUSED_PROVIDER };

    private final boolean passiveMode;
    private final String[] locationProviders;
    protected String[] initLocationProviders(@NonNull Context context)
    {
        List<String> providers = new ArrayList<String>(Arrays.asList(LOCATION_PROVIDERS));
        List<String> notRequested = new ArrayList<>();
        for (int i=0; i<providers.size(); i++)
        {
            String provider = providers.get(i);
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
    public GetFixTask(Context parent, LocationHelper helper, boolean passiveMode)
    {
        this.log_flag = LocationHelperSettings.keepLastLocationLog(parent);
        this.locationManager = (LocationManager)parent.getSystemService(Context.LOCATION_SERVICE);
        this.helperRef = new WeakReference<LocationHelper>(helper);
        this.locationProviders = initLocationProviders(parent);
        this.passiveMode = passiveMode;
    }

    /**
     * Property: autoStop; true (default) the task stops early when a fix is found, false the task continues until interrupted or maxElapsed has passed
     */
    private boolean auto_stop = true;
    public boolean autoStop() {
        return auto_stop;
    }
    public void setAutoStop(boolean flag) {
        auto_stop = flag;
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
     * Property: minimum amount of time to continue searching after acquiring the "first fix".
     */
    private int minElapsed1 = MIN_ELAPSED_FF;
    public int getMinElapsedSinceFirstFix() {
        return minElapsed1;
    }
    public void setMinElapsedSinceFirstFix(int millis) {
        minElapsed1 = millis;
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
    @Nullable
    private Long firstFixTime = null;
    @Nullable
    private FilteredLocation bestFix;
    private final LocationManager locationManager;

    private final GetFixTaskLocationListener locationListener = new GetFixTaskLocationListener();

    private class GetFixTaskLocationListener implements LocationListener
    {
        @Override
        public synchronized void onLocationChanged(Location location) {
            boolean result = onLocationChanged(TAG_LOCATION_CHANGED, location);
            if (firstFixTime == null && result) {
                firstFixTime = System.currentTimeMillis();
            }
        }

        /**
         * @param tag tag
         * @param location location
         * @return true location was used, false location was discarded
         */
        public synchronized boolean onLocationChanged(String tag, Location location)
        {
            if (location != null)
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
                long locationAge = now - locationTime;
                if (passesFilter(locationAge, maxAge))
                {
                    if (bestFix == null) {
                        bestFix = new FilteredLocation(location, locationTime, maxAge, 3);
                        Log_d(TAG, listenerLogLine(tag, location, "PASS: init: " + locationAge + "ms " + (maxAge > 0 ? " <= " + maxAge + "ms" : "") + ": " + location.toString()));

                    } else {
                        bestFix.addToFilter(location, locationTime);
                        Log_d(TAG, listenerLogLine(tag, location, "PASS: adding: " + locationAge + "ms " + (maxAge > 0 ? " <= " + maxAge + "ms" : "") + ": " + location.toString()));
                    }
                    signalProgress();
                    return true;

                } else {
                    Log_d(TAG, listenerLogLine(tag, location, "FAIL: too old: " + locationAge + " > " + maxAge));
                    return false;
                }
            }
            return false;
        }
        private boolean passesFilter(long locationAge, int maxAge) {
            return (maxAge == MAX_AGE_ANY || maxAge == MAX_AGE_NONE || locationAge <= maxAge);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log_d(TAG, provider.toUpperCase() + ": provider status: " + getStatusDisplay(status));
        }
        protected String getStatusDisplay(int status) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE: return "OUT_OF_SERVICE";
                case LocationProvider.AVAILABLE: return "AVAILABLE";
                case LocationProvider.TEMPORARILY_UNAVAILABLE: return "TEMPORARILY_UNAVAILABLE";
                default: return "";
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log_d(TAG, provider.toUpperCase() + ": provider enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log_d(TAG, provider.toUpperCase() + ": provider disabled");
        }
    }

    protected String listenerLogLine(String tag, Location location, String message) {
        return listenerLogLine(tag, location.getProvider(), message);
    }
    protected String listenerLogLine(String tag, String provider, String message) {
        return provider.toUpperCase() + ": " + tag + ": " + message;
    }
    private static final String TAG_LOCATION_CHANGED = "location";
    private static final String TAG_LAST_LOCATION = "lastLocation";

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
    public void onPreExecute()
    {
        clearLog();
        final LocationHelper helper = helperRef.get();
        if (helper != null)
        {
            GetFixUI uiObj = helper.getUI();
            uiObj.onStart();
            uiObj.showProgress(true);
            uiObj.enableUI(false);
        }

        if (helper != null)
        {
            helper.setGettingFix(true);
        }
        bestFix = null;
        firstFixTime = null;
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

    public Location call() throws Exception
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable()
        {
            public void run()
            {
                String[] providers = passiveMode
                        ? new String[] { LocationManager.PASSIVE_PROVIDER }
                        : locationProviders;  //: new String[] { LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, FUSED_PROVIDER };

                if (maxAge != MAX_AGE_NONE)
                {
                    for (int i=0; i<providers.length; i++)
                    {
                        String provider = providers[i];
                        try {
                            if (locationManager.isProviderEnabled(provider)) {
                                boolean usedLastLocation = locationListener.onLocationChanged(TAG_LAST_LOCATION, locationManager.getLastKnownLocation(provider));
                                if (usedLastLocation) {
                                    break;
                                }
                            }
                        } catch (IllegalArgumentException | SecurityException e) {
                            Log_e(TAG, listenerLogLine(TAG_LAST_LOCATION, provider, "unable to access provider; " + e));
                        }
                    }
                }
                signalProgress();

                if (bestFix == null)
                {
                    for (int i=0; i<providers.length; i++)
                    {
                        String provider = providers[i];
                        try {
                            if (locationManager.isProviderEnabled(provider))
                            {
                                if (provider.equals(LocationManager.GPS_PROVIDER) && log_flag) {
                                    addGpsStatusListener(locationManager);
                                }
                                requestLocationUpdates(locationManager, provider, locationListener);
                            }
                        } catch (IllegalArgumentException | SecurityException e) {
                            Log_e(TAG, listenerLogLine(TAG_LOCATION_CHANGED, provider, "unable to access provider; " + e));
                        }
                    }
                }
                signalProgress();
            }
        });

        while (elapsedTime < maxElapsed && !isCancelled())
        {
            try {
                //noinspection BusyWait
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log_i(TAG, "interrupted! " + e);
                break;
            }

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;                                                // total time elapsed
            long elapsedTime1 = stopTime - (firstFixTime != null ? firstFixTime : stopTime);   // elapsed since first fix

            if (auto_stop && bestFix != null && (elapsedTime > minElapsed) &&
                    (firstFixTime == null || elapsedTime1 > minElapsed1)   // bestFix is either a good "last location", or the result of updating for over minDuration
            ) {
                break;
            }
        }
        return ((bestFix != null) ? bestFix.getLocation() : null);
    }

    protected void requestLocationUpdates(LocationManager locationManager, String provider, LocationListener listener)
    {
        Log_d(TAG, listenerLogLine("listener", provider, "requesting updates..."));
        locationManager.requestLocationUpdates(provider, 0, 0, listener);
    }

    /**
     * @param locations a list of android.location.Location to be displayed during progress update
     */
    @Override
    public void onProgressUpdate(Collection<LocationProgress> locations)
    {
        final LocationHelper helper = helperRef.get();
        if (helper != null)
        {
            GetFixUI uiObj = helper.getUI();
            uiObj.updateUI(locations.toArray(new LocationProgress[0]));
        }
    }

    /**
     * Stops location updates, triggers a final progress update, resets ui, and signals onFinished listeners.
     * @param result the "best fix" that could be obtained (potentially null)
     */
    @Override
    public void onPostExecute(Location result)
    {
        try {
            removeGpsStatusListener(locationManager);
            locationManager.removeUpdates(locationListener);
            if (BuildConfig.DEBUG) {
                Log_d(TAG, "Listener: finished (success)");
            }
        } catch (SecurityException e) {
            Log_e(TAG, "Listener: unable to stop... Permissions! we don't have them... checkPermissions should be called before using this task! " + e);
        }

        final LocationHelper helper = helperRef.get();
        if (helper != null)
        {
            helper.setGettingFix(false);

            GetFixUI uiObj = helper.getUI();
            uiObj.showProgress(false);
            uiObj.enableUI(true);
            uiObj.onResult(new GetFixUI.LocationResult(result, elapsedTime, false, getLog()));
        }
    }

    /**
     * Same as onPostExecute except onCancelled is signalled instead of onFinished.
     * @param result the "best fix" that could be obtained (potentially null)
     */
    @Override
    public void onCancelled(Location result)
    {
        try {
            removeGpsStatusListener(locationManager);
            locationManager.removeUpdates(locationListener);
            if (BuildConfig.DEBUG) {
                Log_d(TAG, "Listener: cancelled");
            }
        } catch (SecurityException e) {
            Log_e(TAG, "Listener: unable to cancel... Permissions! we don't have them... checkPermissions should be called before using this task! " + e);
        }

        LocationHelper helper = helperRef.get();
        if (helper != null)
        {
            helper.setGettingFix(false);

            GetFixUI uiObj = helper.getUI();
            uiObj.showProgress(false);
            uiObj.enableUI(true);
            uiObj.onResult(new GetFixUI.LocationResult(result, elapsedTime, true, getLog()));
        }
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

    protected void signalCancelled(Location result)
    {
        for (GetFixTaskListener listener : listeners)
        {
            if (listener != null) {
                listener.onCancelled(result);
            }
        }
    }

    protected void signalProgress() {
        publishProgress(new LocationProgress((bestFix != null ? bestFix.getLocation() : null), System.currentTimeMillis() - startTime, getLog()));
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * addGpsStatusListener
     */
    protected void addGpsStatusListener(LocationManager locationManager)
    {
        if (Build.VERSION.SDK_INT >= 28) {
            Log_d(TAG, "GPS: " + locationManager.getGnssHardwareModelName() + " " + locationManager.getGnssYearOfHardware());
        }
        if (Build.VERSION.SDK_INT >= 24) {
            locationManager.registerGnssStatusCallback(gnssStatusCallback());
        } else {
            locationManager.addGpsStatusListener(gpsStatusListener);
        }
    }

    protected void removeGpsStatusListener(LocationManager locationManager)
    {
        if (Build.VERSION.SDK_INT >= 24) {
            if (gnssStatusCallback != null) {
                locationManager.unregisterGnssStatusCallback((GnssStatus.Callback) gnssStatusCallback);
            }
        } else {
            locationManager.removeGpsStatusListener(gpsStatusListener);
        }
    }

    @TargetApi(24)
    private Object gnssStatusCallback = null;

    @TargetApi(24)
    private GnssStatus.Callback gnssStatusCallback()
    {
        gnssStatusCallback = new GnssStatus.Callback()
        {
            @Override
            public void onStarted() {
                super.onStarted();
                Log_d(TAG, "GPS: started; key: " + GpsDebugDisplay.getSatelliteKey() + "; t_" + elapsedTime);
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log_d(TAG, "GPS: stopped; t_" + elapsedTime);
            }

            @Override
            public void onFirstFix(int ttffMillis)
            {
                super.onFirstFix(ttffMillis);
                Location location = (bestFix != null) ? bestFix.getLocation() : null;
                Log_d(TAG, "GPS: timeToFirstFix: " + ttffMillis
                        + (location != null ? ", accuracy: " + location.getAccuracy() + "m" : "")
                        + "; t_" + elapsedTime);
                signalProgress();
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status)
            {
                super.onSatelliteStatusChanged(status);

                int c = GpsDebugDisplay.countSatellitesWithSignal(status);
                int n = status.getSatelliteCount();
                Log_d(TAG, "GPS: status: " + (c > 9 ? c : " " + c) + "/" + n
                        + " (" + GpsDebugDisplay.getSatelliteReport(status, false) + ")"
                        + " (" + GpsDebugDisplay.getConstellationCount(status, false) + ")"
                        + "; t_" + elapsedTime);
                signalProgress();
            }
        };
        return (GnssStatus.Callback) gnssStatusCallback;
    }

    /**
     * GpsStatus: api 23 and lower
     */
    private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener()
    {
        @Override
        public void onGpsStatusChanged(int event)
        {
            int numSatellites = 0;
            GpsStatus status = locationManager.getGpsStatus(null);
            switch (event)
            {
                case GpsStatus.GPS_EVENT_STARTED:
                    Log_d(TAG, "GPS: started; key: " + GpsDebugDisplay.getSatelliteKey() + "; t_" + elapsedTime);
                    break;

                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    int timeToFirstFix = locationManager.getGpsStatus(null).getTimeToFirstFix();
                    numSatellites = GpsDebugDisplay.getSatelliteCount(GpsDebugDisplay.getSatelliteList(status, true), numSatellites);
                    Log_d(TAG, "GPS: timeToFirstFix: " + timeToFirstFix + "; " + numSatellites + " satellites (" + GpsDebugDisplay.getSatelliteReport(GpsDebugDisplay.getSatelliteList(status, true)) + "); t_" + elapsedTime);
                    signalProgress();
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    Log_d(TAG, "GPS: stopped: t_" + elapsedTime);
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    numSatellites = GpsDebugDisplay.getSatelliteCount(GpsDebugDisplay.getSatelliteList(status, true), 0);
                    Log_d(TAG, "GPS: status: " + numSatellites + "/" + GpsDebugDisplay.getSatelliteCount(GpsDebugDisplay.getSatelliteList(status, false), 0) + " (" + GpsDebugDisplay.getSatelliteReport(GpsDebugDisplay.getSatelliteList(status, false)) + "); t_" + elapsedTime);
                    signalProgress();
                    break;
            }
        }
    };

    //////////////////////////////////////////////////

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
        private final long maxAge;        // millis
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
                Log.d(TAG, "Filter: init to " + location.toString());
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
                    Log.d(TAG, "Filter: adding " + location + ", accuracy now " + location.getAccuracy());
                }
            }
        }
    }

    //////////////////////////////////////////////////

    public void Log_e(String tag, String msg) {
        Log.e(tag, msg);
        if (log_flag) {
            appendLog("E/: " + msg);
        }
    }
    public void Log_w(String tag, String msg) {
        Log.e(tag, msg);
        if (log_flag) {
            appendLog("W/: " + msg);
        }
    }
    public void Log_i(String tag, String msg) {
        Log.i(tag, msg);
        if (log_flag) {
            appendLog("I/: " + msg);
        }
    }
    public void Log_d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
        if (log_flag) {
            appendLog("D/: " + msg);
        }
    }
    private synchronized void appendLog(String line) {
        log = log + "\n" + line;
    }
    private synchronized void clearLog() {
        log = "";
    }
    public synchronized String getLog() {
        return log;
    }
    private String log = "";
    private final boolean log_flag;    // set to `LocationHelperSettings.keepLastLocationLog(parent)` from constructor
}
