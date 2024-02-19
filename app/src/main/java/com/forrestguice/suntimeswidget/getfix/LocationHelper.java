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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

@SuppressWarnings("Convert2Diamond")
public interface LocationHelper
{
    void saveSettings( Bundle bundle );
    void loadSettings( Bundle bundle );

    void setFragment(Fragment f);
    Fragment getFragment();
    void onResume();

    GetFixUI getUI();
    void addUI( GetFixUI ui );

    boolean getFix();
    void getFix( int i );
    void setGettingFix(boolean value);
    boolean gettingFix();
    void cancelGetFix();
    boolean hasFix();

    boolean isLocationEnabled(Context context);
    void fallbackToLastLocation();
    android.location.Location getLastKnownLocation(Context context);

    void addGetFixTaskListener( GetFixTask.GetFixTaskListener listener );
    void removeGetFixTaskListener( GetFixTask.GetFixTaskListener listener );

    boolean hasLocationPermission(Activity activity);
    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
}
