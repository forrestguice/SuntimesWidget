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
import android.location.GpsSatellite;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    public static final String TAG = "LocationTask";

    public static final int MIN_ELAPSED = 1000 * 3;        // wait at least 3s before settling on a fix
    public static final int MAX_ELAPSED = 1000 * 60;       // wait at most a minute for a fix
    public static final int MAX_AGE = 1000 * 60 * 15;      // consider fixes over 15min be "too old"
    public static final int MAX_AGE_NONE = 0;
    public static final int MAX_AGE_ANY = -1;

    public static final String FUSED_PROVIDER = "fused";    // LocationManager.FUSED_PROVIDER (api31+)
    public static final String[] LOCATION_PROVIDERS = new String[] { LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, FUSED_PROVIDER };

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
    public GetFixTask(Context parent, LocationHelper helper)
    {
        this.log_flag = LocationHelperSettings.keepLastLocationLog(parent);
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

    private final GetFixTaskLocationListener locationListener = new GetFixTaskLocationListener();
    private class GetFixTaskLocationListener implements LocationListener
    {
        @Override
        public synchronized void onLocationChanged(Location location) {
            onLocationChanged(TAG_LOCATION_CHANGED, location);
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
                    onProgressUpdate(bestFix.getLocation());
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
            Log_d(TAG, provider.toUpperCase() + ": provider status changed: " + getStatusDisplay(status));
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
    private static final String TAG_LOCATION_CHANGED = "check location";
    private static final String TAG_LAST_LOCATION = "last location";

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
        clearLog();
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
        Log_d(TAG, listenerLogLine("listener", provider, "requesting updates..."));
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
            uiObj.onResult(new GetFixUI.LocationResult(result, elapsedTime, false, gps_numSatellites, getLog()));
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
            uiObj.onResult(new GetFixUI.LocationResult(result, elapsedTime, true, gps_numSatellites, getLog()));
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
     * addGpsStatusListener
     */
    protected void addGpsStatusListener(LocationManager locationManager)
    {
        if (Build.VERSION.SDK_INT >= 28) {
            Log_d(TAG, "GPS: " + locationManager.getGnssHardwareModelName() + " (" + locationManager.getGnssYearOfHardware() + ")");
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

    private int gps_numSatellites = 0;
    private int gps_timeToFirstFix;

    /**
     * GnssStatus: api 24+
     */
    @TargetApi(24)
    public static class GnssStatusDisplay
    {
        public static List<Integer> getSatellites(GnssStatus status)
        {
            ArrayList<Integer> indices = new ArrayList<>();
            for (int i=0; i<status.getSatelliteCount(); i++) {
                if (status.getCn0DbHz(i) != 0) {
                    indices.add(i);
                }
            }
            return indices;
        }
        public static String getSignalToNoiseRatio(GnssStatus status)
        {
            StringBuilder result = new StringBuilder();
            if (status != null)
            {
                int c = 0;
                for (int i : getSatellites(status))
                {
                    if (c != 0) {
                        result.append("|");
                    }
                    result.append(status.getCn0DbHz(i));
                    c++;
                }
            }
            return result.toString();
        }
        public static String getConstellationCount(GnssStatus status)
        {
            StringBuilder result = new StringBuilder();
            HashMap<Integer, Integer> count = countConstellations(status);

            int c = 0;
            for (int type : count.keySet())
            {
                if (c != 0) {
                    result.append("|");
                }
                result.append(constellationTypeLabel(type)).append(" ");
                result.append(count.get(type));
                c++;
            }

            return result.toString();
        }
        public static HashMap<Integer, Integer> countConstellations(@Nullable GnssStatus status)
        {
            HashMap<Integer, Integer> count = new HashMap<>();
            if (status != null) {
                for (int i : getSatellites(status)) {
                    int type = status.getConstellationType(i);
                    int c = (count.containsKey(type) ? count.get(type) : 0);
                    count.put(type, ++c);
                }
            }
            return count;
        }
        public static String constellationTypeLabel(int type)
        {
            switch (type) {
                case GnssStatus.CONSTELLATION_GPS: return "GPS";
                case GnssStatus.CONSTELLATION_SBAS: return "SBAS";
                case GnssStatus.CONSTELLATION_GLONASS: return "GLONASS";
                case GnssStatus.CONSTELLATION_QZSS: return "QZSS";
                case GnssStatus.CONSTELLATION_BEIDOU: return "BEIDOU";
                case GnssStatus.CONSTELLATION_GALILEO: return "GALILEO";
                case GnssStatus.CONSTELLATION_UNKNOWN: default: return "UNKNOWN";
            }
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
                Log_d(TAG, "GPS: started; t_" + elapsedTime);
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
                gps_timeToFirstFix = ttffMillis;
                gps_numSatellites = GnssStatusDisplay.getSatellites(lastStatus).size();
                Log_d(TAG, "GPS: timeToFirstFix: " + gps_timeToFirstFix + "; "
                        + gps_numSatellites + " satellites" + " (" + GnssStatusDisplay.getSignalToNoiseRatio(lastStatus)
                        + "); t_" + elapsedTime);
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                lastStatus = status;
                gps_numSatellites = GnssStatusDisplay.getSatellites(status).size();
                Log_d(TAG, "GPS: status: " + gps_numSatellites + " satellites"
                        + " (" + GnssStatusDisplay.getConstellationCount(status)
                        + "); t_" + elapsedTime);
            }
            private GnssStatus lastStatus = null;
        };
        return (GnssStatus.Callback) gnssStatusCallback;
    }

    /**
     * GpsStatus: api 23 and lower
     */
    private final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener()
    {
        private List<GpsSatellite> getSatelliteList()
        {
            ArrayList<GpsSatellite> list = new ArrayList<>();
            for (GpsSatellite satellite : locationManager.getGpsStatus(null).getSatellites()) {
                if (satellite.usedInFix()) {
                    list.add(satellite);
                }
            }
            return list;
        }
        private int getSatelliteCount(List<GpsSatellite> list, int current) {
            int i = list.size();
            return (i != 0 ? i : current);
        }
        private String getSignalToNoiseRatio(List<GpsSatellite> list)
        {
            StringBuilder result = new StringBuilder();
            for (int i=0; i<list.size(); i++) {
                if (i != 0) {
                    result.append("|");
                }
                result.append(list.get(i).getSnr());
            }
            return result.toString();
        }

        @Override
        public void onGpsStatusChanged(int event)
        {
            switch (event)
            {
                case GpsStatus.GPS_EVENT_STARTED:
                    Log_d(TAG, "GPS: started: t_" + elapsedTime);
                    break;

                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    gps_timeToFirstFix = locationManager.getGpsStatus(null).getTimeToFirstFix();
                    gps_numSatellites = getSatelliteCount(getSatelliteList(), gps_numSatellites);
                    Log_d(TAG, "GPS: timeToFirstFix: " + gps_timeToFirstFix + "; " + gps_numSatellites + " satellites (" + getSignalToNoiseRatio(getSatelliteList()) + "); t_" + elapsedTime);
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    Log_d(TAG, "GPS: stopped: t_" + elapsedTime);
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    int numSatellites = getSatelliteCount(getSatelliteList(), 0);
                    Log_d(TAG, "GPS: status: " + numSatellites + " satellites; t_" + elapsedTime);
                    break;
            }
        }
    };

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
