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
package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.getfix.GetFixHelper;
import com.forrestguice.suntimeswidget.getfix.GetFixUI;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class LocationDialog extends Dialog
{
    private Activity myParent;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Spinner spinner_locationMode;

    private TextView labl_locationLat;
    private EditText text_locationLat;

    private TextView labl_locationLon;
    private EditText text_locationLon;

    private TextView labl_locationName;
    private EditText text_locationName;

    private ImageButton button_getfix;
    private ProgressBar progress_getfix;

    private GetFixUI getFixUI;
    private GetFixHelper getFixHelper;

    public LocationDialog(Activity c)
    {
        super(c);
        myParent = c;
        setContentView(R.layout.layout_dialog_location);
        setTitle(myParent.getString(R.string.location_dialog_title));
        setCancelable(true);

        initViews(myParent);
        loadSettings(myParent);
    }

    protected void initViews( Context context )
    {
        WidgetSettings.initDisplayStrings(context);

        TextView groupTitle = (TextView)findViewById(R.id.appwidget_location_grouptitle);
        groupTitle.setVisibility(View.GONE);

        ArrayAdapter<WidgetSettings.LocationMode> spinner_locationModeAdapter;
        spinner_locationModeAdapter = new ArrayAdapter<>(myParent, R.layout.layout_listitem_oneline, WidgetSettings.LocationMode.values());
        spinner_locationModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_locationMode = (Spinner)findViewById(R.id.appwidget_location_mode);
        spinner_locationMode.setAdapter(spinner_locationModeAdapter);
        spinner_locationMode.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                final WidgetSettings.LocationMode[] locationModes = WidgetSettings.LocationMode.values();
                WidgetSettings.LocationMode locationMode = locationModes[parent.getSelectedItemPosition()];
                setUseCustomLocation((locationMode == WidgetSettings.LocationMode.CUSTOM_LOCATION));
            }

            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        labl_locationName = (TextView)findViewById(R.id.appwidget_location_name_label);
        text_locationName = (EditText)findViewById(R.id.appwidget_location_name);

        labl_locationLat = (TextView)findViewById(R.id.appwidget_location_lat_label);
        text_locationLat = (EditText)findViewById(R.id.appwidget_location_lat);

        labl_locationLon = (TextView)findViewById(R.id.appwidget_location_lon_label);
        text_locationLon = (EditText)findViewById(R.id.appwidget_location_lon);

        progress_getfix = (ProgressBar)findViewById(R.id.appwidget_location_getfixprogress);
        progress_getfix.setVisibility(View.GONE);

        button_getfix = (ImageButton)findViewById(R.id.appwidget_location_getfix);
        button_getfix.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getFixHelper.getFix();
            }
        });

        getFixUI = new GetFixUI(text_locationName, text_locationLat, text_locationLon, progress_getfix, button_getfix);
        getFixHelper = new GetFixHelper(myParent, getFixUI);

        if (!getFixHelper.isGPSEnabled())
        {
            button_getfix.setImageResource(GetFixUI.ICON_GPS_DISABLED);
        }
    }

    public int getAppWidgetId()
    {
        return appWidgetId;
    }
    public void setAppWidgetId(int value)
    {
        appWidgetId = value;
        loadSettings(myParent);
    }

    private void setUseCustomLocation( boolean value )
    {
        labl_locationLon.setEnabled(value);
        text_locationLon.setEnabled(value);

        labl_locationLat.setEnabled(value);
        text_locationLat.setEnabled(value);

        labl_locationName.setEnabled(value);
        text_locationName.setEnabled(value);
    }

    public void onPrepareDialog()
    {
    }

    protected void loadSettings(Context context)
    {
        WidgetSettings.LocationMode locationMode = WidgetSettings.loadLocationModePref(context, appWidgetId);
        spinner_locationMode.setSelection(locationMode.ordinal());

        WidgetSettings.Location location = WidgetSettings.loadLocationPref(context, appWidgetId);
        text_locationLat.setText(location.getLatitude());
        text_locationLon.setText(location.getLongitude());
        text_locationName.setText(location.getLabel());
    }

    protected void saveSettings(Context context)
    {
        final WidgetSettings.LocationMode[] locationModes = WidgetSettings.LocationMode.values();
        WidgetSettings.LocationMode locationMode = locationModes[ spinner_locationMode.getSelectedItemPosition() ];
        WidgetSettings.saveLocationModePref(context, appWidgetId, locationMode);

        String latitude = text_locationLat.getText().toString();
        String longitude = text_locationLon.getText().toString();
        String name = text_locationName.getText().toString();
        WidgetSettings.Location location = new WidgetSettings.Location(name, latitude, longitude);
        WidgetSettings.saveLocationPref(context, appWidgetId, location);
    }

    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

    public AlertDialog toAlertDialog()
    {
        ViewGroup dialogFrame = (ViewGroup)this.getWindow().getDecorView();
        View dialogContent = dialogFrame.getChildAt(0);
        dialogFrame.removeView(dialogContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.location_dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        getFixHelper.cancelGetFix();
                        dialog.dismiss();

                        if (onCanceled != null)
                        {
                            onCanceled.onClick(dialog, which);
                        }
                    }
                }
        );

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.location_dialog_ok),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        getFixHelper.cancelGetFix();
                        saveSettings(myParent);
                        dialog.dismiss();

                        if (onAccepted != null)
                        {
                            onAccepted.onClick(dialog, which);
                        }
                    }
                }
        );

        dialog.setOnShowListener(new OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface)
            {
                loadSettings(myParent);
            }
        });

        dialog.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialogInterface)
            {
            }
        });

        return dialog;
    }

}
