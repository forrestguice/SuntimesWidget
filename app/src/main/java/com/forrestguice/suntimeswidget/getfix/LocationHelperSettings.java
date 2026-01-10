/**
    Copyright (C) 2024 Forrest Guice
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressWarnings("Convert2Diamond")
public class LocationHelperSettings
{
    public static final String PREF_KEY_LOCATION_MIN_ELAPSED = "getFix_minElapsed";
    public static final String PREF_KEY_LOCATION_MAX_ELAPSED = "getFix_maxElapsed";
    public static final String PREF_KEY_LOCATION_MAX_AGE = "getFix_maxAge";

    public static final String PREF_KEY_LOCATION_TIME = "getFix_time";    // time of last automatic request

    public static final String PREF_KEY_LOCATION_PASSIVE = "getFix_passiveMode";
    public static final boolean PREF_DEF_LOCATION_PASSIVE = false;

    public static final String PREF_KEY_LOCATION_PROVIDER_ = "getFix_provider_";

    public static int loadPrefGpsMaxAge(SharedPreferences prefs, int defaultValue)
    {
        int retValue;
        try {
            String maxAgeString = prefs.getString(PREF_KEY_LOCATION_MAX_AGE, defaultValue+"");
            retValue = Integer.parseInt(maxAgeString);
        } catch (NumberFormatException e) {
            Log.e("loadPrefGPSMaxAge", "Bad setting! " + e);
            retValue = defaultValue;
        }
        return retValue;
    }

    /**
     * @param prefs an instance of SharedPreferences
     * @param defaultValue the default min elapsed value if pref can't be loaded
     * @return the gps min elapsed value (milliseconds)
     */
    public static int loadPrefGpsMinElapsed(SharedPreferences prefs, int defaultValue)
    {
        int retValue;
        try {
            String minAgeString = prefs.getString(PREF_KEY_LOCATION_MIN_ELAPSED, defaultValue+"");
            retValue = Integer.parseInt(minAgeString);
        } catch (NumberFormatException e) {
            Log.e("loadPrefGPSMinElapsed", "Bad setting! " + e);
            retValue = defaultValue;
        }
        return retValue;
    }

    /**
     * @param prefs an instance of SharedPreferences
     * @param defaultValue the default max elapsed value if pref can't be loaded
     * @return the gps max elapsed value (milliseconds)
     */
    public static int loadPrefGpsMaxElapsed(SharedPreferences prefs, int defaultValue)
    {
        int retValue;
        try {
            String maxElapsedString = prefs.getString(PREF_KEY_LOCATION_MAX_ELAPSED, defaultValue+"");
            retValue = Integer.parseInt(maxElapsedString);
        } catch (NumberFormatException e) {
            Log.e("loadPrefGPSMaxElapsed", "Bad setting! " + e);
            retValue = defaultValue;
        }
        return retValue;
    }

    /**
     * @param context context
     * @param provider location provider (e.g. GPS)
     * @return true location provider is requested by user, false provider should be ignored
     */
    public static boolean isProviderRequested( Context context, String provider ) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_LOCATION_PROVIDER_ + provider, true);
    }

    /**
     * @return true use the passive provider (don't prompt when other providers are disabled), false use the gps/network provider (prompt when disabled)
     */
    public static boolean loadPrefGpsPassiveMode( Context context )
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_KEY_LOCATION_PASSIVE, PREF_DEF_LOCATION_PASSIVE);
    }

    public static boolean lastAutoLocationIsStale(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return timeSinceLastAutoLocationRequest(context) > LocationHelperSettings.loadPrefGpsMaxAge(prefs, GetFixTask.MAX_AGE);
    }
    public static long timeSinceLastAutoLocationRequest(Context context) {
        return System.currentTimeMillis() - lastAutoLocationRequest(context);
    }
    public static long lastAutoLocationRequest(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        long t = 0;
        try {
            t = pref.getLong(PREF_KEY_LOCATION_TIME, 0);

        } catch (ClassCastException e) {
            try {
                t = Long.parseLong(pref.getString(PREF_KEY_LOCATION_TIME, "0"));
                Log.w("lastAutoLocationRequest", "lastAutoLocationRequest has the wrong type! found Long as String.");

            } catch (NumberFormatException | ClassCastException e1) {
                Log.e("lastAutoLocationRequest", "Failed to get last auto location request time: " + e1);
            }
        }
        return t;
    }
    public static void saveLastAutoLocationRequest(Context context, long value)
    {
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
        pref.putLong(PREF_KEY_LOCATION_TIME, value);
        pref.apply();
    }

}
