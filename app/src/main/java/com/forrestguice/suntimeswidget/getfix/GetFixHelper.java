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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;

/**
 * A helper class that helps to manage a GetFixTask; has methods for starting/stopping the task;
 * allows a single task to run at a time.
 */
public class GetFixHelper
{
    public static final String KEY_LOCATION_GETTINGFIX = "gettingfix";

    public static final int REQUEST_GETFIX_LOCATION = 1;

    public static final String PREF_KEY_GETFIX_MINELAPSED = "getFix_minElapsed";
    public static final String PREF_KEY_GETFIX_MAXELAPSED = "getFix_maxElapsed";
    public static final String PREF_KEY_GETFIX_MAXAGE = "getFix_maxAge";

    public GetFixTask getFixTask = null;
    public boolean gettingFix = false;

    private Activity myParent;
    private GetFixUI uiObj;

    public GetFixHelper(Activity parent, GetFixUI ui)
    {
        myParent = parent;
        uiObj = ui;
    }

    public GetFixUI getUI()
    {
        return uiObj;
    }

    /**
     * Get a fix; main entry point for GPS "get fix" button in location settings.
     * Spins up a GetFixTask; allows only one such task to execute at a time.
     */
    public void getFix()
    {
        if (!gettingFix)
        {
            if (hasGPSPermissions(myParent, REQUEST_GETFIX_LOCATION))
            {
                if (isGPSEnabled())
                {
                    SharedPreferences prefs = myParent.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
                    getFixTask = new GetFixTask(myParent, this);

                    int minElapsed = prefs.getInt(PREF_KEY_GETFIX_MINELAPSED, GetFixTask.MIN_ELAPSED);
                    getFixTask.setMinElapsed(minElapsed);

                    int maxElapsed = prefs.getInt(PREF_KEY_GETFIX_MAXELAPSED, GetFixTask.MAX_ELAPSED);
                    getFixTask.setMaxElapsed(maxElapsed);

                    int maxAge = prefs.getInt(PREF_KEY_GETFIX_MAXAGE, GetFixTask.MAX_AGE);
                    getFixTask.setMaxAge(maxAge);

                    getFixTask.addGetFixTaskListeners(listeners);
                    getFixTask.execute();

                } else {
                    showGPSEnabledPrompt();
                }
            }
        }
    }
    public void getFix( GetFixUI uiObj )
    {
        if (!gettingFix)
        {
            this.uiObj = uiObj;
            getFix();
        }
    }

    /**
     * Cancel acquiring a location fix (cancels running task(s)).
     */
    public void cancelGetFix()
    {
        if (gettingFix && getFixTask != null)
        {
            getFixTask.cancel(true);
        }
    }

    public boolean hasGPSPermissions(Activity activity, int requestID)
    {
        int permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean hasPermission = (permission == PackageManager.PERMISSION_GRANTED);
        Log.d("hasGPSPermissions", "" + hasPermission);

        if (!hasPermission)
        {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestID);
        }

        return hasPermission;
    }

    public boolean isGettingFix()
    {
        return gettingFix;
    }

    public boolean isGPSEnabled()
    {
        return isGPSEnabled(myParent);
    }
    public static boolean isGPSEnabled(Context context)
    {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private AlertDialog gpsPrompt = null;
    public void showGPSEnabledPrompt()
    {
        gpsPrompt = GetFixHelper.createGPSEnabledPrompt(myParent);
        gpsPrompt.show();
    }
    public static AlertDialog createGPSEnabledPrompt( final Context context )
    {
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

    public void dismissGPSEnabledPrompt()
    {
        if (gpsPrompt != null)
        {
            gpsPrompt.dismiss();
        }
    }

    private ArrayList<GetFixTask.GetFixTaskListener> listeners = new ArrayList<>();
    public void addGetFixTaskListener( GetFixTask.GetFixTaskListener listener )
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
    public void removeGetFixTaskListener( GetFixTask.GetFixTaskListener listener )
    {
        listeners.remove(listener);
        if (getFixTask != null)
        {
            getFixTask.removeGetFixTaskListener(listener);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
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
        Log.d("DEBUG", "GetFixHelper loadSettings (bundle)");
        boolean wasGettingFix = bundle.getBoolean(KEY_LOCATION_GETTINGFIX);
        if (wasGettingFix)
        {
            Log.d("DEBUG", "GetFixHelper loadSettings ... was previously getting fix");
            //getFix();
        }
    }

    public void saveSettings( Bundle bundle )
    {
        Log.d("DEBUG", "GetFixHelper saveSettings (bundle)");
        bundle.putBoolean(KEY_LOCATION_GETTINGFIX, gettingFix);
    }

}
