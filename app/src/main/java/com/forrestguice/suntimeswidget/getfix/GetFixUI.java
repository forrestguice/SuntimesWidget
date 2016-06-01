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

import android.location.Location;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.forrestguice.suntimeswidget.R;

/**
 */
public abstract class GetFixUI
{
    public static final int ICON_GPS_DISABLED = R.drawable.ic_action_location_off;
    public static final int ICON_GPS_SEARCHING = R.drawable.ic_action_location_searching;
    public static final int ICON_GPS_FOUND = R.drawable.ic_action_location_found;

    public abstract void enableUI(boolean value);
    public abstract void updateUI(Location... locations);
    public abstract void showProgress(boolean showProgress);
    public abstract void onStart();
    public abstract void onResult(Location result, boolean wasCancelled);
}
