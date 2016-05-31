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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Contacts;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.getfix.GetFixDatabaseAdapter;
import com.forrestguice.suntimeswidget.getfix.GetFixHelper;
import com.forrestguice.suntimeswidget.getfix.GetFixTask;
import com.forrestguice.suntimeswidget.getfix.GetFixUI;
import com.forrestguice.suntimeswidget.getfix.GetFixUI1;
import com.forrestguice.suntimeswidget.getfix.GetFixUI2;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.math.BigDecimal;

public class LocationConfigView extends LinearLayout
{
    private Context myParent;

    public LocationConfigView(Context context)
    {
        super(context);
        init(context, false);
    }

    public LocationConfigView(Context context, boolean asDialog)
    {
        super(context);
        init(context, asDialog);
    }

    public LocationConfigView(Context context, AttributeSet attribs)
    {
        super(context, attribs);

        init(context, false);
    }

    private void init(Context context, boolean asDialog)
    {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate((asDialog ? R.layout.layout_dialog_location2 : R.layout.layout_settings_location2), this);
        myParent = context;
        initViews(context);

        if (!isInEditMode())
        {
            loadSettings(context);
        }

        setMode(mode);
        populateLocationList();
    }

    public WidgetSettings.Location getLocation()
    {
        String name = text_locationName.getText().toString();
        String latitude = text_locationLat.getText().toString();
        String longitude = text_locationLon.getText().toString();

        try {
            BigDecimal lat = new BigDecimal(latitude);
            BigDecimal lon = new BigDecimal(longitude);

        } catch (NumberFormatException e) {
            Log.e("getLocation", "invalid location! falling back to default; " + e.toString());
            name = WidgetSettings.PREF_DEF_LOCATION_LABEL;
            latitude = WidgetSettings.PREF_DEF_LOCATION_LATITUDE;
            longitude = WidgetSettings.PREF_DEF_LOCATION_LONGITUDE;
        }

        return new WidgetSettings.Location(name, latitude, longitude);
    }

    public WidgetSettings.LocationMode getLocationMode()
    {
        final WidgetSettings.LocationMode[] locationModes = WidgetSettings.LocationMode.values();
        WidgetSettings.LocationMode locationMode = locationModes[ spinner_locationMode.getSelectedItemPosition() ];
        return locationMode;
    }

    /**
     * Property: appwidget id
     */
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public int getAppWidgetId()
    {
        return appWidgetId;
    }
    public void setAppWidgetId(int value)
    {
        appWidgetId = value;
        loadSettings(myParent);
    }

    /**
     * Property: hide title
     */
    private boolean hideTitle = false;
    public boolean getHideTitle() { return hideTitle; }
    public void setHideTitle(boolean value)
    {
        hideTitle = value;
        TextView groupTitle = (TextView)findViewById(R.id.appwidget_location_grouptitle);
        groupTitle.setVisibility( (hideTitle ? View.GONE : View.VISIBLE) );
    }

    /** Property: mode (auto, select, edit/add) */
    private LocationDialogMode mode = LocationDialogMode.MODE_CUSTOM_SELECT;
    public LocationDialogMode getMode()
    {
        return mode;
    }
    public void setMode( LocationDialogMode mode )
    {
        FrameLayout autoButtonLayout = (FrameLayout)findViewById(R.id.appwidget_location_auto_layout);

        this.mode = mode;
        switch (mode)
        {
            case MODE_AUTO:
                labl_locationLon.setEnabled(false);
                text_locationLon.setEnabled(false);
                labl_locationLat.setEnabled(false);
                text_locationLat.setEnabled(false);

                labl_locationName.setEnabled(false);
                text_locationName.setEnabled(false);

                spin_locationName.setSelection(GetFixDatabaseAdapter.findPlaceByName(myParent.getString(R.string.gps_lastfix_title_found), getFixAdapter.getCursor()));
                spin_locationName.setEnabled(false);
                flipper.setDisplayedChild(1);

                autoButtonLayout.setVisibility(View.VISIBLE);
                button_edit.setVisibility(View.GONE);
                button_save.setVisibility(View.GONE);
                flipper2.setDisplayedChild(1);
                break;

            case MODE_CUSTOM_ADD:
            case MODE_CUSTOM_EDIT:
                labl_locationLon.setEnabled(true);
                text_locationLon.setEnabled(true);
                labl_locationLat.setEnabled(true);
                text_locationLat.setEnabled(true);

                labl_locationName.setEnabled(true);
                text_locationName.setEnabled(true);
                spin_locationName.setEnabled(false);
                flipper.setDisplayedChild(0);
                text_locationName.requestFocus();

                autoButtonLayout.setVisibility(View.GONE);
                button_edit.setVisibility(View.GONE);
                button_save.setVisibility(View.VISIBLE);
                flipper2.setDisplayedChild(0);
                break;

            case MODE_CUSTOM_SELECT:
            default:
                labl_locationLon.setEnabled(false);
                text_locationLon.setEnabled(false);
                labl_locationLat.setEnabled(false);
                text_locationLat.setEnabled(false);

                labl_locationName.setEnabled(true);
                text_locationName.setEnabled(false);
                spin_locationName.setEnabled(true);
                flipper.setDisplayedChild(1);

                autoButtonLayout.setVisibility(View.GONE);
                button_edit.setVisibility(View.VISIBLE);
                button_save.setVisibility(View.GONE);
                flipper2.setDisplayedChild(1);
                break;
        }
    }

    public static enum LocationDialogMode
    {
        MODE_AUTO(), MODE_CUSTOM_SELECT(), MODE_CUSTOM_ADD(), MODE_CUSTOM_EDIT();
        private LocationDialogMode() {}

        public String toString()
        {
            return this.name();
        }

        public int ordinal( LocationDialogMode[] array )
        {
            for (int i=0; i<array.length; i++)
            {
                if (array[i].name().equals(this.name()))
                {
                    return i;
                }
            }
            return -1;
        }
    }

    private ViewFlipper flipper, flipper2;


    private Spinner spinner_locationMode;

    private TextView labl_locationLat;
    private EditText text_locationLat;

    private TextView labl_locationLon;
    private EditText text_locationLon;

    private LinearLayout layout_locationName;
    private TextView labl_locationName;
    private Spinner spin_locationName;
    private EditText text_locationName;

    private ImageButton button_edit;
    private ImageButton button_save;

    private ImageButton button_getfix;
    private ProgressBar progress_getfix;
    private GetFixUI getFixUI_editMode;

    private ImageButton button_auto;
    private ProgressBar progress_auto;
    private GetFixUI getFixUI_autoMode;

    private GetFixHelper getFixHelper;
    private SimpleCursorAdapter getFixAdapter;

    protected void initViews( Context context )
    {
        WidgetSettings.initDisplayStrings(context);

        flipper = (ViewFlipper)findViewById(R.id.view_flip);
        flipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));

        flipper2 = (ViewFlipper)findViewById(R.id.view_flip2);
        flipper2.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
        flipper2.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));

        ArrayAdapter<WidgetSettings.LocationMode> spinner_locationModeAdapter;
        spinner_locationModeAdapter = new ArrayAdapter<>(myParent, R.layout.layout_listitem_oneline, WidgetSettings.LocationMode.values());
        spinner_locationModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_locationMode = (Spinner)findViewById(R.id.appwidget_location_mode);
        spinner_locationMode.setAdapter(spinner_locationModeAdapter);
        spinner_locationMode.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                getFixHelper.cancelGetFix();

                final WidgetSettings.LocationMode[] locationModes = WidgetSettings.LocationMode.values();
                WidgetSettings.LocationMode locationMode = locationModes[parent.getSelectedItemPosition()];
                LocationDialogMode dialogMode = (locationMode == WidgetSettings.LocationMode.CUSTOM_LOCATION) ? LocationDialogMode.MODE_CUSTOM_SELECT : LocationDialogMode.MODE_AUTO;
                setMode(dialogMode);
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        layout_locationName = (LinearLayout) findViewById(R.id.appwidget_location_name_layout);
        labl_locationName = (TextView) findViewById(R.id.appwidget_location_name_label);
        text_locationName = (EditText) findViewById(R.id.appwidget_location_name);

        String[] from = new String[] {"name"};
        int[] to = new int[] {android.R.id.text1};
        getFixAdapter = new SimpleCursorAdapter(myParent, android.R.layout.simple_spinner_item, null, from, to);
        getFixAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin_locationName = (Spinner)findViewById(R.id.appwidget_location_nameSelect);
        spin_locationName.setAdapter(getFixAdapter);
        spin_locationName.setOnItemSelectedListener(new Spinner.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Cursor cursor = getFixAdapter.getCursor();
                cursor.moveToPosition(position);

                if (cursor.getColumnCount() >= 3)
                {
                    updateViews(new WidgetSettings.Location(cursor.getString(1), cursor.getString(2), cursor.getString(3)));
                }
            }

            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        labl_locationLat = (TextView)findViewById(R.id.appwidget_location_lat_label);
        text_locationLat = (EditText)findViewById(R.id.appwidget_location_lat);

        labl_locationLon = (TextView)findViewById(R.id.appwidget_location_lon_label);
        text_locationLon = (EditText)findViewById(R.id.appwidget_location_lon);

        // custom mode: toggle edit mode
        button_edit = (ImageButton)findViewById(R.id.appwidget_location_edit);
        button_edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                setMode(LocationDialogMode.MODE_CUSTOM_EDIT);
            }
        });

        // custom mode: save location
        button_save = (ImageButton)findViewById(R.id.appwidget_location_save);
        button_save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final boolean validInput = validateInput();
                if (validInput)
                {
                    setMode(LocationDialogMode.MODE_CUSTOM_SELECT);
                    populateLocationList();
                }

                final GetFixTask.GetFixTaskListener cancelGetFixListener = new GetFixTask.GetFixTaskListener()
                {
                    @Override
                    public void onCancelled()
                    {
                        if (validInput)
                        {
                            setMode(LocationDialogMode.MODE_CUSTOM_SELECT);
                            populateLocationList();
                        }
                    }
                };
                getFixHelper.removeGetFixTaskListener(cancelGetFixListener);
                getFixHelper.addGetFixTaskListener(cancelGetFixListener);
                getFixHelper.cancelGetFix();
            }
        });

        // custom mode: get GPS fix
        progress_getfix = (ProgressBar)findViewById(R.id.appwidget_location_getfixprogress);
        progress_getfix.setVisibility(View.GONE);

        button_getfix = (ImageButton)findViewById(R.id.appwidget_location_getfix);
        getFixUI_editMode = new GetFixUI1(text_locationName, text_locationLat, text_locationLon, progress_getfix, button_getfix);

        button_getfix.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getFixHelper.getFix(getFixUI_editMode);
            }
        });

        // auto mode: get GPS fix
        progress_auto = (ProgressBar)findViewById(R.id.appwidget_location_auto_progress);
        progress_auto.setVisibility(View.GONE);

        button_auto = (ImageButton)findViewById(R.id.appwidget_location_auto);
        getFixUI_autoMode = new GetFixUI2(text_locationName, text_locationLat, text_locationLon, progress_auto, button_auto);

        button_auto.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getFixHelper.getFix(getFixUI_autoMode);
            }
        });

        getFixHelper = new GetFixHelper(myParent, getFixUI_editMode);
        if (!isInEditMode() && !getFixHelper.isGPSEnabled())
        {
            button_getfix.setImageResource(GetFixUI.ICON_GPS_DISABLED);
            button_auto.setImageResource(GetFixUI.ICON_GPS_DISABLED);
        }
    }

    private void updateViews(WidgetSettings.Location location)
    {
        text_locationLat.setText(location.getLatitude());
        text_locationLon.setText(location.getLongitude());
        text_locationName.setText(location.getLabel());
    }

    protected void loadSettings(Context context)
    {
        WidgetSettings.LocationMode locationMode = WidgetSettings.loadLocationModePref(context, appWidgetId);
        spinner_locationMode.setSelection(locationMode.ordinal());

        WidgetSettings.Location location = WidgetSettings.loadLocationPref(context, appWidgetId);
        updateViews(location);
    }

    protected boolean saveSettings(Context context)
    {
        //final WidgetSettings.LocationMode[] locationModes = WidgetSettings.LocationMode.values();
        //WidgetSettings.LocationMode locationMode = locationModes[ spinner_locationMode.getSelectedItemPosition() ];

        WidgetSettings.LocationMode locationMode = getLocationMode();
        WidgetSettings.saveLocationModePref(context, appWidgetId, locationMode);

        if (validateInput())
        {
            String latitude = text_locationLat.getText().toString();
            String longitude = text_locationLon.getText().toString();
            String name = text_locationName.getText().toString();
            WidgetSettings.Location location = new WidgetSettings.Location(name, latitude, longitude);
            WidgetSettings.saveLocationPref(context, appWidgetId, location);
            return true;
        }

        return false;
    }

    public void cancelGetFix()
    {
        getFixHelper.cancelGetFix();
    }

    public void dismissGPSEnabledPrompt() { getFixHelper.dismissGPSEnabledPrompt(); }

    /**
     * A dialog wrapper around the view.
     */
    public static class LocationConfigDialog extends Dialog
    {
        private Activity myParent;
        private LocationConfigView locationConfigView;
        public LocationConfigView getLocationConfigView() { return locationConfigView; }

        public LocationConfigDialog(Activity context)
        {
            super(context);
            myParent = context;
            setTitle(context.getString(R.string.location_dialog_title));
            setCancelable(true);

            locationConfigView = new LocationConfigView(context, true);
            setContentView(locationConfigView);
        }

        protected DialogInterface.OnClickListener onAccepted = null;
        public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
        {
            onAccepted = listener;
        }

        protected DialogInterface.OnClickListener onCanceled = null;
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
            //builder.setTitle(myParent.getString(R.string.location_dialog_title));
            builder.setView(dialogContent);
            final AlertDialog dialog = builder.create();

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.location_dialog_cancel),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            locationConfigView.cancelGetFix();
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
                        { /* EMPTY */ }
                    }
            );

            dialog.setOnShowListener(new Dialog.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface dialogInterface)
                {
                    locationConfigView.loadSettings(myParent);

                    Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    okButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            Log.d("LocationConfigView", "OK");
                            locationConfigView.cancelGetFix();
                            if (locationConfigView.saveSettings(myParent))
                            {
                                dialog.dismiss();
                                if (onAccepted != null)
                                {
                                    onAccepted.onClick(dialog, 0);
                                }
                            }
                        }
                    });
                }
            });

            dialog.setOnDismissListener(new Dialog.OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface dialogInterface)
                {
                }
            });

            return dialog;
        }
    }


    protected void populateLocationList()
    {
        new LocationListTask().execute((Object[]) null);
    }

    private class LocationListTask extends AsyncTask<Object, Object, Cursor>
    {
        GetFixDatabaseAdapter db = new GetFixDatabaseAdapter(myParent.getApplicationContext());

        @Override
        protected Cursor doInBackground(Object... params)
        {
            db.open();

            WidgetSettings.Location selectedLocation = getLocation();
            String selectedPlaceName = selectedLocation.getLabel();
            String selectedPlaceLat = selectedLocation.getLatitude();
            String selectedPlaceLon = selectedLocation.getLongitude();

            Cursor result = db.getAllPlaces(0, true);
            if (db.findPlaceByName(selectedPlaceName, result) == -1)
            {
                Log.d("populateLocationList", "Place not found, adding it; " + selectedPlaceName + ":" + selectedPlaceLat + "," + selectedPlaceLon);
                db.addPlace(getLocation());
                result = db.getAllPlaces(0, true);
            }

            Cursor selectedCursor = db.getPlace(selectedPlaceName, true);
            String selectedLat = selectedCursor.getString(2);
            String selectedLon = selectedCursor.getString(3);
            if (!selectedLat.equals(selectedPlaceLat) || !selectedLon.equals(selectedPlaceLon))
            {
                db.updatePlace(getLocation());
                result = db.getAllPlaces(0, true);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Cursor result)
        {
            if (result != null)
            {
                getFixAdapter.changeCursor(result);
                db.close();

                String name = text_locationName.getText().toString();
                spin_locationName.setSelection(db.findPlaceByName(name, result));
            }
        }
    }

    /**
     * A ListAdapter of WidgetListItems.
     */
    /**public static class LocationListAdapter extends ArrayAdapter<WidgetSettings.Location>
    {
        private Context context;
        private ArrayList<WidgetSettings.Location> locations;

        public LocationListAdapter(Context context, ArrayList<WidgetSettings.Location> locations)
        {
            super(context, R.layout.layout_listitem_locations, locations);
            this.context = context;
            this.locations = locations;
        }

        public LocationListAdapter(Context context, WidgetSettings.Location[] locations)
        {
            super(context, R.layout.layout_listitem_locations, locations);
            this.context = context;
            this.locations = new ArrayList<>();
            for (WidgetSettings.Location location : locations)
            {
                this.locations.add(location);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return listItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            return listItemView(position, convertView, parent);
        }

        private View listItemView(int position, View convertView, ViewGroup parent)
        {
            WidgetSettings.Location item = locations.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.layout_listitem_locations, parent, false);

            //ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            //icon.setImageResource(item.getIcon());

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item.getLabel());

            //TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            //text2.setText(item.toString());

            return view;
        }
    }*/

    public boolean validateInput()
    {
        boolean isValid = true;

        String latitude = text_locationLat.getText().toString();
        try {
            BigDecimal lat = new BigDecimal(latitude);
        } catch (NumberFormatException e1) {
            isValid = false;
            text_locationLat.setError(myParent.getString(R.string.location_dialog_error_lat));
        }

        String longitude = text_locationLon.getText().toString();
        try {
            BigDecimal lon = new BigDecimal(longitude);
        } catch (NumberFormatException e2) {
            isValid = false;
            text_locationLon.setError(myParent.getString(R.string.location_dialog_error_lon));
        }

        return isValid;
    }

}
