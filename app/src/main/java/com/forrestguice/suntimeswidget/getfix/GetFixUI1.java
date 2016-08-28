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

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.text.DecimalFormat;

/**
 * A wrapper class around a set of Views composing "get fix/location" UI; the constructor expects
 * references to the individual views; used by GetFixTask when making progress updates and posting
 * results.
 */
public class GetFixUI1 extends GetFixUI
{
    public EditText locationNameUI = null;
    public EditText locationLatUI = null;
    public EditText locationLonUI = null;
    public ProgressBar locationProgress = null;
    public ImageButton gpsButton;

    public GetFixUI1(EditText nameUI, EditText latUI, EditText lonUI, ProgressBar progress, ImageButton getFixButton)
    {
        locationNameUI = nameUI;
        locationLatUI = latUI;
        locationLonUI = lonUI;
        locationProgress = progress;
        gpsButton = getFixButton;
    }

    @Override
    public void enableUI(boolean value)
    {
        locationNameUI.requestFocus();
        locationLatUI.setEnabled(value);
        locationLonUI.setEnabled(value);
        locationNameUI.setEnabled(value);
    }

    @Override
    public void updateUI(Location... locations)
    {
        DecimalFormat formatter = WidgetSettings.Location.decimalDegreesFormatter();
        locationLatUI.setText( formatter.format(locations[0].getLatitude()) );
        locationLonUI.setText( formatter.format(locations[0].getLongitude()) );
    }

    @Override
    public void showProgress(boolean showProgress)
    {
        locationProgress.setVisibility((showProgress ? View.VISIBLE : View.GONE));
    }

    @Override
    public void onStart()
    {
        gpsButton.setVisibility(View.GONE);
    }

    @Override
    public void onResult(Location result, boolean wasCancelled)
    {
        gpsButton.setImageResource((result == null) ? ICON_GPS_SEARCHING : ICON_GPS_FOUND);
        gpsButton.setVisibility(View.VISIBLE);
        gpsButton.setEnabled(true);
    }

}
