/**
    Copyright (C) 2014-2023 Forrest Guice
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
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * A helper class that helps to manage a GetFixTask; has methods for starting/stopping the task;
 * allows a single task to run at a time.
 */
@SuppressWarnings("Convert2Diamond")
public class GetFixHelper implements LocationHelper
{
    public static final String KEY_LOCATION_GETTINGFIX = "gettingfix";
    public static final String KEY_LOCATION_GOTFIX = "gotfix";
    public static final String KEY_LOCATION_UIINDEX = "uiindex";

    public static final String DIALOGTAG_ENABLEGPS = "enablegps";
    public static final String DIALOGTAG_KEEPTRYING = "keeptrying";

    public static final int REQUEST_GETFIX_LOCATION = 1;

    public GetFixTask getFixTask = null;
    public Location fix = null;

    public boolean gettingFix = false;
    public boolean wasGettingFix = false;
    public boolean gotFix = false;

    private FragmentActivity myParent;
    private ArrayList<GetFixUI> uiObj = new ArrayList<GetFixUI>();
    private int uiIndex = 0;

    public GetFixHelper(FragmentActivity parent, GetFixUI ui)
    {
        myParent = parent;
        addUI(ui);
    }

    public void setFragment(Fragment f) {
        fragmentRef = new WeakReference<>(f);
    }
    public Fragment getFragment() {
        return fragmentRef != null ? fragmentRef.get() : null;
    }
    private WeakReference<Fragment> fragmentRef = null;

    public int getMinElapsedTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myParent);
        return LocationHelperSettings.loadPrefGpsMinElapsed(prefs, GetFixTask.MIN_ELAPSED);
    }

    /**
     * Get a fix; main entry point for GPS "get fix" button in location settings.
     * Spins up a GetFixTask; allows only one such task to execute at a time.
     * @return true if location permission is granted (and action was taken)
     */
    public boolean getFix()
    {
        if (!gettingFix)
        {
            if (checkGPSPermissions(myParent, REQUEST_GETFIX_LOCATION))
            {
                if (isLocationEnabled(myParent))
                {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myParent);
                    getFixTask = new GetFixTask(myParent, this);
                    getFixTask.setMinElapsed(getMinElapsedTime());

                    int maxElapsed = LocationHelperSettings.loadPrefGpsMaxElapsed(prefs, GetFixTask.MAX_ELAPSED);
                    getFixTask.setMaxElapsed(maxElapsed);

                    int maxAge = LocationHelperSettings.loadPrefGpsMaxAge(prefs, GetFixTask.MAX_AGE);
                    getFixTask.setMaxAge(maxAge);

                    //Log.d("GetFixHelper", "MinElapsed: " + minElapsed);
                    //Log.d("GetFixHelper", "MaxElapsed: " + maxElapsed);
                    //Log.d("GetFixHelper", "MaxAge: " + maxAge);

                    getFixTask.addGetFixTaskListeners(listeners);
                    getFixTask.addGetFixTaskListener( new GetFixTaskListener()
                    {
                        @Override
                        public void onFinished(Location result)
                        {
                            fix = result;
                            gotFix = (fix != null);

                            if (!getFixTask.isCancelled() && !gotFix)
                            {
                                showKeepSearchingPrompt();
                            }
                        }
                    });
                    getFixTask.executeTask(LocationHelperSettings.loadPrefGpsPassiveMode(myParent));

                } else {
                    Log.w("GetFixHelper", "getFix called while GPS disabled; showing a prompt");
                    showGPSEnabledPrompt();
                }
                return true;

            } else {
                Log.w("GetFixHelper", "getFix called without GPS permissions! ignored");
                return false;
            }
        } else {
            Log.w("GetFixHelper", "getFix called while already running! ignored");
            return true;
        }
    }
    public void getFix( int i )
    {
        if (!gettingFix)
        {
            setUiIndex( i );
            getFix();

        } else {
            Log.w("GetFixHelper", "getFix called while already running! ignored");
        }
    }

    @Override
    public boolean gettingFix() {
        return gettingFix;
    }

    @Override
    public void setGettingFix(boolean value) {
        gettingFix = value;
    }

    public void fallbackToLastLocation()
    {
        LocationManager locationManager = (LocationManager)myParent.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null)
        {
            GetFixUI uiObj = getUI();
            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location == null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
                fix = location;
                gotFix = (fix != null);
                uiObj.updateUI(location);
                uiObj.onResult(location, false);

            } catch (SecurityException e) {
                Log.e("GetFixHelper", "unable to fallback to last location ... Permissions! we don't have them.. checkPermissions should be called before calling this method. " + e);
            }
        } else Log.w("GetFixHelper", "unable to fallback to last location ... LocationManager is null!");
    }

    public android.location.Location getLastKnownLocation(Context context) {
        return GetFixHelper.lastKnownLocation(context);
    }

    public static android.location.Location lastKnownLocation(Context context)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null)
        {
            try {
                t_locationProvider = LocationManager.PASSIVE_PROVIDER;
                android.location.Location location = locationManager.getLastKnownLocation(t_locationProvider);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(t_locationProvider = LocationManager.NETWORK_PROVIDER);
                    if (location == null) {
                        location = locationManager.getLastKnownLocation(t_locationProvider = LocationManager.GPS_PROVIDER);
                    }
                }
                return location;

            } catch (SecurityException e) {
                Log.e("lastKnownLocation", "Permissions! we don't have them.. checkPermissions should be called before calling this method. " + e);
                return null;
            }
        } else {
            Log.w("lastKnownLocation", "LocationManager is null!");
            return null;
        }
    }
    protected static String t_locationProvider;

    /**
     * Cancel acquiring a location fix (cancels running task(s)).
     */
    public void cancelGetFix()
    {
        if (gettingFix && getFixTask != null)
        {
            //Log.d("GetFixHelper", "Canceling getFix");
            getFixTask.cancel(true);
        }
    }

    public boolean hasLocationPermission(Activity activity) {
        return (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * @param activity
     * @param requestID used to identify the permission request
     * @return true already has gps permissions, false has no permissions (triggers a request)
     */
    public boolean checkGPSPermissions(final FragmentActivity activity, final int requestID)
    {
        boolean hasPermission = hasLocationPermission(activity);
        //Log.d("checkGPSPermissions", "" + hasPermission);

        if (!hasPermission)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                String permissionMessage = activity.getString(R.string.privacy_permission_location);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(activity.getString(R.string.privacy_permissiondialog_title))
                        .setMessage(fromHtml(permissionMessage))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                requestPermissions(requestID);
                            }
                        });

                if (Build.VERSION.SDK_INT >= 11)
                        builder.setIconAttribute(R.attr.icActionPlace);
                else builder.setIcon(R.drawable.ic_action_place);

                builder.show();

            } else {
                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, requestID);
            }
        }
        return hasPermission;
    }

    protected void requestPermissions(final int requestID) {
        if (getFragment() != null) {
            requestPermissions(getFragment(), requestID);
        } else requestPermissions(myParent, requestID);
    }
    protected void requestPermissions(Activity activity, final int requestID) {
        ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, requestID);
    }
    protected void requestPermissions(Fragment fragment, final int requestID) {
        fragment.requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, requestID);
    }

    public boolean isGettingFix()
    {
        return gettingFix;
    }

    public GetFixUI getUI()
    {
        return uiObj.get(uiIndex);
    }

    /**
     * @param i the UI index (default 0)
     * @return a GetFixUI instance
     */
    public GetFixUI getUI( int i )
    {
        if (i >= 0 && i < uiObj.size())
            return uiObj.get(i);
        else return uiObj.get(0);
    }

    /**
     * @param ui the ui obj to be added to and managed by the helper
     */
    public void addUI( GetFixUI ui )
    {
        uiObj.add(ui);
    }

    /**
     * @return the number of ui objects managed by this helper
     */
    public int numUI()
    {
        return uiObj.size();
    }

    /**
     * @return the index of the ui object currently that is currently set to be used
     */
    public int getUiIndex()
    {
        return uiIndex;
    }

    /**
     * Set the index of the ui object to use when getting a fix.
     * @param i the ui obj index
     * @return true the index was set, false failed to set
     */
    public boolean setUiIndex(int i)
    {
        if (uiIndex >= 0 && uiIndex < uiObj.size())
        {
            uiIndex = i;
            return true;

        } else {
            Log.w("GetFixHelper", "setUiIndex was called with a negative value! " + i);
            return false;
        }
    }

    private ArrayList<GetFixTaskListener> listeners = new ArrayList<GetFixTaskListener>();
    public void addGetFixTaskListener( GetFixTaskListener listener )
    {
        if (!listeners.contains(listener))
        {
            listeners.add(listener);
            if (getFixTask != null)
            {
                getFixTask.addGetFixTaskListener(listener);
            }
        }
    }
    public void removeGetFixTaskListener( GetFixTaskListener listener )
    {
        listeners.remove(listener);
        if (getFixTask != null)
        {
            getFixTask.removeGetFixTaskListener(listener);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_GETFIX_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    getFix();
                }
                break;
        }
    }

    public void loadSettings( Bundle bundle )
    {
        if (bundle == null)
            return;

        //Log.d("DEBUG", "GetFixHelper loadSettings (bundle)");
        wasGettingFix = bundle.getBoolean(KEY_LOCATION_GETTINGFIX);
        gotFix = bundle.getBoolean(KEY_LOCATION_GOTFIX);
        setUiIndex(bundle.getInt(KEY_LOCATION_UIINDEX));
    }

    public void saveSettings( Bundle bundle )
    {
        //Log.d("DEBUG", "GetFixHelper saveSettings (bundle)");
        bundle.putBoolean(KEY_LOCATION_GETTINGFIX, gettingFix);
        bundle.putBoolean(KEY_LOCATION_GOTFIX, gotFix);
        bundle.putInt(KEY_LOCATION_UIINDEX, uiIndex);
    }

    public void onResume()
    {
        //Log.d("DEBUG", "GetFixHelper onResume");
        FragmentManager fragments = myParent.getSupportFragmentManager();

        KeepTryingDialog keepTryingDialog = (KeepTryingDialog) fragments.findFragmentByTag(DIALOGTAG_KEEPTRYING);
        if (keepTryingDialog != null)
        {
            keepTryingDialog.setHelper(this);
        }

        EnableGPSDialog enableGPSDialog = (EnableGPSDialog) fragments.findFragmentByTag(DIALOGTAG_ENABLEGPS);
        if (enableGPSDialog != null)
        {
            enableGPSDialog.setHelper(this);
        }

        if (wasGettingFix)
        {
            Log.w(GetFixTask.TAG, "GetFixHelper was previously getting fix... restarting");
            getFix();
        }
    }

    /**
     * Keep trying dialog fragment; "No fix found. Keep searching? yes, no"
     */
    public static class KeepTryingDialog extends DialogFragment
    {
        private LocationHelper helper;
        public LocationHelper getHelper() { return helper; }
        public void setHelper( LocationHelper helper ) { this.helper = helper; }

        @NonNull @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            final Activity context = getActivity();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getString(R.string.gps_keeptrying_msg))
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.gps_keeptrying_ok), new DialogInterface.OnClickListener()
                    {
                        public void onClick(final DialogInterface dialog, final int id)
                        {
                            helper.getFix();
                        }
                    })
                    .setNegativeButton(context.getString(R.string.gps_keeptrying_cancel), new DialogInterface.OnClickListener()
                    {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                        {
                            dialog.cancel();
                        }
                    })
                    .setNeutralButton(context.getString(R.string.gps_keeptrying_fallback), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            helper.fallbackToLastLocation();
                        }
                    });
            return builder.create();
        }
    }

    public void showKeepSearchingPrompt()
    {
        final KeepTryingDialog dialog = new KeepTryingDialog();
        dialog.setHelper(this);
        dialog.show(myParent.getSupportFragmentManager(), DIALOGTAG_KEEPTRYING);
    }

    /**
     * Enable GPS alert dialog fragment; "Enable GPS? yes, no"
     */
    public static class EnableGPSDialog extends DialogFragment
    {
        public EnableGPSDialog() {}

        private LocationHelper helper;
        public void setHelper(LocationHelper helper)
        {
            this.helper = helper;
        }

        @NonNull @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            final Activity context = getActivity();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(context.getString(R.string.gps_dialog_msg))
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.gps_dialog_ok), new DialogInterface.OnClickListener()
                    {
                        public void onClick(final DialogInterface dialog, final int id)
                        {
                            context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(context.getString(R.string.gps_dialog_cancel), new DialogInterface.OnClickListener()
                    {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                        {
                            dialog.cancel();
                        }
                    });
            return builder.create();
        }
    }

    public boolean isLocationEnabled(Context context)
    {
        boolean allowPassive = LocationHelperSettings.loadPrefGpsPassiveMode(context);
        return isNetProviderEnabled(myParent) || isGPSProviderEnabled(myParent) || (allowPassive && isPassiveProviderEnabled(myParent));
    }

    @Override
    public boolean hasFix() {
        return gotFix;
    }

    public static boolean isGPSProviderEnabled(Context context)
    {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }
    public static boolean isNetProviderEnabled(Context context)
    {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }
    public static boolean isPassiveProviderEnabled(Context context)
    {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return (locationManager != null && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER));
    }

    public void showGPSEnabledPrompt()
    {
        final EnableGPSDialog dialog = new EnableGPSDialog();
        dialog.setHelper(this);
        dialog.show(myParent.getSupportFragmentManager(), DIALOGTAG_ENABLEGPS);
    }


    /**
     * @param htmlString html markup
     * @return an html span
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }

}
