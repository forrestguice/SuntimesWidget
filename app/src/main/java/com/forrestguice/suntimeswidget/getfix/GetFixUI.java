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
import android.content.res.TypedArray;
import android.location.Location;

import com.forrestguice.suntimeswidget.R;

/**
 */
public abstract class GetFixUI
{
    public static int ICON_GPS_FOUND = R.drawable.ic_action_location_found;
    public static int ICON_GPS_SEARCHING = R.drawable.ic_action_location_searching;
    public static int ICON_GPS_DISABLED = R.drawable.ic_action_location_off;

    @SuppressWarnings("ResourceType")
    public static void themeIcons(Activity context)
    {
        int[] attrs = new int[] { R.attr.icActionGPS, R.attr.icActionGPS_searching, R.attr.icActionGPS_off };
        TypedArray a = context.obtainStyledAttributes(attrs);
        ICON_GPS_FOUND = a.getResourceId(0, R.drawable.ic_action_location_found);
        ICON_GPS_SEARCHING = a.getResourceId(1, R.drawable.ic_action_location_searching);
        ICON_GPS_DISABLED = a.getResourceId(2, R.drawable.ic_action_location_off);
        a.recycle();
    }

    public abstract void enableUI(boolean value);
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public abstract void updateUI(Location... locations);
    public abstract void showProgress(boolean showProgress);
    public abstract void onStart();
    public abstract void onResult(LocationResult result);

    public void updateUI(LocationProgress... progress)
    {
        Location[] locations = new Location[progress.length];
        for (int i=0; i<progress.length; i++) {
            locations[i] = (progress != null ? progress[i].result : null);
        }
        //noinspection deprecation
        updateUI(locations);
    }

    /**
     * LocationResult
     */
    public static class LocationResult extends LocationProgress
    {
        public LocationResult(Location result, long elapsed, boolean wasCancelled, String log)
        {
            super(result, elapsed, log);
            this.wasCancelled = wasCancelled;
        }

        protected boolean wasCancelled;
        public boolean wasCancelled() {
            return wasCancelled;
        }
    }

    /**
     * LocationProgress
     */
    public static class LocationProgress
    {
        public LocationProgress(Location location, long elapsed, String log) {
            this.result = location;
            this.elapsed = elapsed;
            this.accuracy = (location != null ? location.getAccuracy() : -1);
            this.log = log;
        }
        public LocationProgress(Location location, long elapsed, String log, int signal_i, int signal_n) {
            this(location, elapsed, log);
            this.signal_i = signal_i;
            this.signal_n = signal_n;
        }

        protected Location result;
        public Location getResult() {
            return result;
        }

        protected long elapsed;
        public long getElapsed() {
            return elapsed;
        }

        protected int signal_i = 0;
        public int getSignalI() {
            return signal_i;
        }

        protected int signal_n = 0;
        public int getSignalN() {
            return signal_n;
        }

        protected double accuracy;
        public double getAccuracy() {
            return accuracy;
        }

        protected String log;
        public String getLog() {
            return log;
        }
    }
}
