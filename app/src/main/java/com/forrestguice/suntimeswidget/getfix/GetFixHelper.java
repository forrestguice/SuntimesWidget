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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;

/**
 * A helper class that helps to manage a GetFixTask; has methods for starting/stopping the task;
 * allows a single task to run at a time.
 */
public class GetFixHelper
{
    public static final String PREF_KEY_GETFIX_MINELAPSED = "getFix_minElapsed";
    public static final String PREF_KEY_GETFIX_MAXELAPSED = "getFix_maxElapsed";
    public static final String PREF_KEY_GETFIX_MAXAGE = "getFix_maxAge";

    public GetFixTask getFixTask = null;
    public boolean gettingFix = false;

    private Context myParent;
    private GetFixUI uiObj;

    public GetFixHelper(Context parent, GetFixUI ui)
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

    public boolean isGPSEnabled()
    {
        LocationManager locationManager = (LocationManager) myParent.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private AlertDialog gpsPrompt = null;
    public void showGPSEnabledPrompt()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setMessage(myParent.getString(R.string.gps_dialog_msg))
                .setCancelable(false)
                .setPositiveButton(myParent.getString(R.string.gps_dialog_ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, final int id)
                    {
                        myParent.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        gpsPrompt = null;
                    }
                })
                .setNegativeButton(myParent.getString(R.string.gps_dialog_cancel), new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        dialog.cancel();
                        gpsPrompt = null;
                    }
                });

        gpsPrompt = builder.create();
        gpsPrompt.show();
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

}
