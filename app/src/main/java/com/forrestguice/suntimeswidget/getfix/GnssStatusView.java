/**
    Copyright (C) 2025 Forrest Guice
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

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

public abstract class GnssStatusView extends FrameLayout
{
    public static final String TAG = "GpsStatusView";

    protected LocationManager locationManager;

    public GnssStatusView(Context context) {
        super(context);
        init(context);
    }
    public GnssStatusView(Context context, AttributeSet attribs) {
        super(context, attribs);
        init(context);
    }

    public void init(Context context)
    {
        initLayout(context);
        initViews(context);
        loadSettings(context);
    }

    protected abstract void initLayout(Context context);

    protected void initViews(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void loadSettings(Context context) {
        if (isInEditMode()) {
            return;
        }
    }
    public void loadSettings(Context context, Bundle bundle ) {
    }
    public boolean saveSettings(Context context) {
        return false;
    }
    public boolean saveSettings(Bundle bundle) {
        return true;
    }

    @Override
    protected void onDetachedFromWindow()
    {
        cleanup();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View view, int visibility)
    {
        super.onVisibilityChanged(view, visibility);
        if (visibility != View.VISIBLE) {
            cleanup();
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible)
    {
        super.onVisibilityAggregated(isVisible);
        if (!isVisible) {
            cleanup();
        }
    }

    protected void cleanup() {
        stopMonitoring();
    }

    //////////////////////////////////////////////////

    protected void onGnssStarted() {}
    protected void onGnssStopped() {}
    protected void onGnssFirstFix(long ttffMillis) {}

    @TargetApi(24)
    protected abstract void updateViews(@Nullable GnssStatus status);
    protected abstract void updateViews(@Nullable GpsStatus status);

    //////////////////////////////////////////////////

    public boolean hasPermission()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            Context context = getContext();
            if (context != null) {
                return (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
            } else return false;
        } else return true;
    }

    protected boolean isMonitoring = false;
    public boolean isMonitoring() {
        return isMonitoring;
    }

    protected Object statusCallback;

    /**
     * startMonitoring
     */
    public void startMonitoring()
    {
        if (hasPermission())
        {
            if (Build.VERSION.SDK_INT >= 24) {
                locationManager.registerGnssStatusCallback((GnssStatus.Callback)(statusCallback = gnssStatusCallback()));
            } else {
                locationManager.addGpsStatusListener((GpsStatus.Listener)(statusCallback = gpsStatusListener()));
            }
            isMonitoring = true;
            Log.d(TAG, "started monitoring...");
        }
    }

    /**
     * stopMonitoring
     */
    public void stopMonitoring()
    {
        if (hasPermission())
        {
            if (statusCallback != null) {
                if (Build.VERSION.SDK_INT >= 24) {
                    locationManager.unregisterGnssStatusCallback((GnssStatus.Callback) statusCallback);
                    updateViews((GnssStatus) null);
                } else {
                    locationManager.removeGpsStatusListener((GpsStatus.Listener) statusCallback);
                    updateViews((GpsStatus) null);
                }
                isMonitoring = false;
                Log.d(TAG, "stopped monitoring...");
            }
        }
    }

    @TargetApi(24)
    protected GnssStatus.Callback gnssStatusCallback()
    {
        return new GnssStatus.Callback()
        {
            @Override
            public void onStarted() {
                super.onStarted();
                onGnssStarted();
            }
            @Override
            public void onStopped() {
                super.onStopped();
                onGnssStopped();
            }
            @Override
            public void onFirstFix(int ttffMillis) {
                super.onFirstFix(ttffMillis);
                onGnssFirstFix(ttffMillis);
            }
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                updateViews(status);
            }
        };
    }

    /**
     * GpsStatus: api 23 and lower
     */
    protected GpsStatus.Listener gpsStatusListener()
    {
        return new GpsStatus.Listener()
        {
            @Override
            public void onGpsStatusChanged(int event) {
                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    updateViews(locationManager.getGpsStatus(null));
                } else if (event == GpsStatus.GPS_EVENT_STARTED) {
                    onGnssStarted();
                } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
                    onGnssStopped();
                } else if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                    onGnssFirstFix(System.currentTimeMillis());
                }
            }
        };
    }

    /**
     * SatelliteItem
     */
    public static class SatelliteItem implements Comparable<SatelliteItem>
    {
        public boolean isStale = false;

        public final int i;
        public final int id;
        public final int constellation;

        public double azimuth;
        public double elevation;

        public double cnr;
        public static final double MAX_CNR = 60d;    // db-hertz

        public boolean hasAlmanac = false;
        public boolean hasEphemeris = false;
        public boolean usedInFix = false;

        public SatelliteItem(int i, int svid, int constellation) {
            this.i = i;
            this.id = svid;
            this.constellation = constellation;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof SatelliteItem)) {
                return false;

            } else {
                SatelliteItem that = (SatelliteItem) obj;
                return (this.constellation == that.constellation)
                        && (this.id == that.id);
            }
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public int compareTo(@NonNull SatelliteItem o) {
            if (o != null) {
                if (constellation == o.constellation)
                    return Integer.compare(id, o.id);
                else return Integer.compare(constellation, o.constellation);
            } else return 1;
        }
    }
}
