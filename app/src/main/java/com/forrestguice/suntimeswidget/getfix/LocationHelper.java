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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.forrestguice.support.app.FragmentCompat;

public interface LocationHelper
{
    void saveSettings( Bundle bundle );
    void loadSettings( Bundle bundle );

    void setFragment(FragmentCompat f);
    FragmentCompat getFragment();
    void onResume();

    GetFixUI getUI();
    void addUI( GetFixUI ui );

    boolean getFix();
    void getFix( int i, boolean autoStop );
    void setGettingFix(boolean value);
    boolean gettingFix();
    void cancelGetFix();
    boolean hasFix();

    boolean isLocationEnabled(Context context);
    void fallbackToLastLocation();
    android.location.Location getLastKnownLocation(Context context);

    void addGetFixTaskListener( GetFixTaskListener listener );
    void removeGetFixTaskListener( GetFixTaskListener listener );

    boolean hasLocationPermission(Context context);
    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    void reloadAGPS(Activity context, boolean coldStart);
    void reloadAGPS(Activity context, boolean coldStart, DialogInterface.OnClickListener listener);
}
