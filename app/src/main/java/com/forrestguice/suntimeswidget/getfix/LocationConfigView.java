/**
    Copyright (C) 2014-2022 Forrest Guice
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

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValuesCollection;
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.app.FragmentCompat;
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.android.AndroidTaskHandler;
import com.forrestguice.util.concurrent.SimpleTaskListener;
import com.forrestguice.util.concurrent.TaskListener;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

public class LocationConfigView extends LinearLayout
{
    public static final String SCHEME_GEO = "geo";

    public static final String KEY_DIALOGMODE = "dialogmode";
    public static final String KEY_LOCATION_MODE = "locationMode";
    public static final String KEY_LOCATION_LATITUDE = "locationLatitude";
    public static final String KEY_LOCATION_LONGITUDE = "locationLongitude";
    public static final String KEY_LOCATION_ALTITUDE = "locationAltitude";
    public static final String KEY_LOCATION_LABEL = "locationLabel";

    private boolean isInitialized = false;

    public LocationConfigView(Context context)
    {
        super(context);
    }

    public LocationConfigView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
    }

    public void init(@NonNull AppCompatActivity activity, boolean asDialog)
    {
        final LayoutInflater inflater = LayoutInflater.from(activity);
        inflater.inflate((asDialog ? R.layout.layout_dialog_location2 : R.layout.layout_settings_location2), this);
        initViews(activity, activity);

        loadSettings(activity);
        setMode(mode);
        populateLocationList();
        isInitialized = true;
    }

    public void init(@NonNull AppCompatActivity activity, boolean asDialog, int appWidgetId)
    {
        this.appWidgetId = appWidgetId;
        init(activity, asDialog);
    }

    public boolean isInitialized() { return isInitialized; }

    protected WeakReference<FragmentCompat> fragmentRef = null;
    public void setFragment(FragmentCompat f) {
        fragmentRef = new WeakReference<>(f);
        //if (getFixHelper != null) {
        //    getFixHelper.setFragment(f);
        //} else Log.w("LocationConfigView", "setFragment: getFixHelper is null!");
    }
    @Nullable
    public FragmentCompat getFragment() {
        return fragmentRef != null ? fragmentRef.get() : null;
    }

    public com.forrestguice.suntimeswidget.calculator.core.Location getLocation(@NonNull Context context)
    {
        String name = text_locationName.getText().toString();
        String latitude = text_locationLat.getText().toString();
        String longitude = text_locationLon.getText().toString();

        LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, appWidgetId);
        String altitude = text_locationAlt.getText().toString();
        if (altitude.trim().isEmpty()) {
            altitude = "0";
            Log.w("LocationConfigView", "empty altitude, supplying 0");
        }

        try {
            BigDecimal lat = new BigDecimal(latitude);
            BigDecimal lon = new BigDecimal(longitude);
            BigDecimal alt = new BigDecimal(altitude);

        } catch (NumberFormatException e) {
            Log.e("getLocation", "invalid location! falling back to default; " + e.toString());
            name = WidgetSettings.PREF_DEF_LOCATION_LABEL;
            latitude = WidgetSettings.PREF_DEF_LOCATION_LATITUDE;
            longitude = WidgetSettings.PREF_DEF_LOCATION_LONGITUDE;
            altitude = WidgetSettings.PREF_DEF_LOCATION_ALTITUDE;
            units = WidgetSettings.PREF_DEF_GENERAL_UNITS_LENGTH;
        }

        return new com.forrestguice.suntimeswidget.calculator.core.Location(name, latitude, longitude, altitude, units == LengthUnit.METRIC);
    }

    public LocationMode getLocationMode() {
        return (LocationMode) spinner_locationMode.getSelectedItem();
    }

    /**
     * Property: appwidget id
     */
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public int getAppWidgetId()
    {
        return appWidgetId;
    }
    public void setAppWidgetId(@NonNull Context context, int value)
    {
        appWidgetId = value;
        loadSettings(context);
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
        if (groupTitle != null) {
            groupTitle.setVisibility( (hideTitle ? View.GONE : View.VISIBLE) );
        }
    }

    /**
     * Property: hide mode
     */
    private boolean hideMode = false;
    public boolean getHideMode()
    {
        return hideMode;
    }
    public void setHideMode(boolean value) {
        setHideMode(value, false);
    }
    public void setHideMode(boolean value, boolean forceUpdate)
    {
        hideMode = value;
        if (hideMode || forceUpdate)
        {
            View locationModeLayout = findViewById(R.id.appwidget_location_mode_layout);
            if (locationModeLayout != null) {
                locationModeLayout.setVisibility( hideMode ? View.GONE : View.VISIBLE );
            }

            View locationModeDivider = findViewById(R.id.appwidget_location_mode_divider);
            if (locationModeDivider != null) {
                locationModeDivider.setVisibility( hideMode ? View.GONE : View.VISIBLE );
            }
        }
    }

    /**
     * Property: collapsed
     */
    private boolean collapse = false;
    public boolean shouldCollapse() {
        return collapse;
    }
    public void setShouldCollapse(boolean value) {
        collapse = value;
    }

    /**
     * Property: showAddButton; the view shows an add button instead of an edit button.
     */
    private boolean showAddButton = false;
    public boolean showAddButton() {
        return showAddButton;
    }
    public void setShowAddButton(boolean value) {
        showAddButton = value;
    }

    /**
     * Property: auto mode allowed
     */
    private boolean autoAllowed = true;
    public boolean getAutoAllowed() { return autoAllowed; }
    public void setAutoAllowed(boolean value)
    {
        autoAllowed = value;
    }

    /** Property: mode (auto, select, edit/add) */
    private LocationViewMode mode = LocationViewMode.MODE_CUSTOM_SELECT;
    public LocationViewMode getMode()
    {
        return mode;
    }
    public void setMode( LocationViewMode mode )
    {
        //Log.d("DEBUG", "LocationViewMode setMode " + mode.name());
        FrameLayout autoButtonLayout = (FrameLayout)findViewById(R.id.appwidget_location_auto_layout);

        if (this.mode != mode)
        {
            getFixHelper.cancelGetFix();
        }

        String name;
        Context context = getContext();
        LocationViewMode previousMode = this.mode;
        this.mode = mode;
        switch (mode)
        {
            case MODE_AUTO:
                labl_locationLon.setEnabled(false);
                text_locationLon.setEnabled(false);
                labl_locationLat.setEnabled(false);
                text_locationLat.setEnabled(false);
                labl_locationAlt.setEnabled(false);
                text_locationAlt.setEnabled(false);
                inputOverlay.setVisibility(View.VISIBLE);
                detailsLayout.setVisibility(View.VISIBLE);

                labl_locationName.setEnabled(false);
                text_locationName.setEnabled(false);

                name = (context != null ? context.getString(R.string.gps_lastfix_title_found) : "Last Position");
                spin_locationName.setSelection(GetFixDatabaseAdapter.findPlaceByName(name, getFixAdapter.getCursor()));
                spin_locationName.setEnabled(false);
                flipper.setDisplayedChild(1);

                autoButtonLayout.setVisibility(View.VISIBLE);
                button_list.setVisibility(View.GONE);
                button_add.setVisibility(View.GONE);
                button_edit.setVisibility(View.GONE);
                button_save.setVisibility(View.GONE);
                button_cancel.setVisibility(View.GONE);
                flipper2.setDisplayedChild(1);
                break;

            case MODE_DISABLED:
                labl_locationLon.setEnabled(false);
                text_locationLon.setEnabled(false);
                labl_locationLat.setEnabled(false);
                text_locationLat.setEnabled(false);
                labl_locationAlt.setEnabled(false);
                text_locationAlt.setEnabled(false);
                inputOverlay.setVisibility(View.GONE);
                detailsLayout.setVisibility(collapse ? View.GONE : View.VISIBLE);

                labl_locationName.setEnabled(false);
                text_locationName.setEnabled(false);
                spin_locationName.setEnabled(false);
                spin_locationName.setVisibility(View.GONE);
                flipper.setDisplayedChild(0);

                autoButtonLayout.setVisibility(View.GONE);
                button_list.setVisibility(View.INVISIBLE);
                button_add.setVisibility(showAddButton ? View.INVISIBLE : View.GONE);
                button_edit.setVisibility(showAddButton ? View.GONE : View.INVISIBLE);
                button_save.setVisibility(View.GONE);
                button_cancel.setVisibility(View.GONE);
                flipper2.setDisplayedChild(1);
                break;

            case MODE_CUSTOM_ADD:
            case MODE_CUSTOM_EDIT:
                labl_locationLon.setEnabled(true);
                text_locationLon.setEnabled(true);
                labl_locationLat.setEnabled(true);
                text_locationLat.setEnabled(true);
                labl_locationAlt.setEnabled(true);
                text_locationAlt.setEnabled(true);
                inputOverlay.setVisibility(View.GONE);
                detailsLayout.setVisibility(View.VISIBLE);

                labl_locationName.setEnabled(true);
                text_locationName.setEnabled(true);
                spin_locationName.setEnabled(false);
                flipper.setDisplayedChild(0);
                text_locationName.requestFocus();

                autoButtonLayout.setVisibility(View.GONE);
                button_list.setVisibility(View.GONE);
                button_add.setVisibility(View.GONE);
                button_edit.setVisibility(View.GONE);
                button_save.setVisibility(View.VISIBLE);
                button_cancel.setVisibility(View.VISIBLE);
                flipper2.setDisplayedChild(0);
                break;

            case MODE_CUSTOM_SELECT:
            default:
                labl_locationLon.setEnabled(false);
                text_locationLon.setEnabled(false);
                labl_locationLat.setEnabled(false);
                text_locationLat.setEnabled(false);
                labl_locationAlt.setEnabled(false);
                text_locationAlt.setEnabled(false);

                detailsLayout.setVisibility(collapse ? View.GONE : View.VISIBLE);
                inputOverlay.setVisibility(detailsLayout.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);

                labl_locationName.setEnabled(true);
                text_locationName.setEnabled(false);
                spin_locationName.setEnabled(true);
                flipper.setDisplayedChild(1);

                autoButtonLayout.setVisibility(View.GONE);
                button_list.setVisibility(View.VISIBLE);
                button_add.setVisibility(showAddButton ? View.VISIBLE : View.GONE);
                button_edit.setVisibility(showAddButton ? View.GONE : View.VISIBLE);
                button_save.setVisibility(View.GONE);
                button_cancel.setVisibility(View.GONE);
                flipper2.setDisplayedChild(1);

                if (previousMode == LocationViewMode.MODE_AUTO) {
                    name = (context != null ? context.getString(R.string.gps_lastfix_title_cached) : "Previous Position");
                    text_locationName.setText(name);
                    populateLocationList();
                }
                break;
        }
        if (viewListener != null) {
            viewListener.onModeChanged(mode);
        }
    }

    private ViewFlipper flipper, flipper2;
    private Spinner spinner_locationMode;

    private TextView labl_locationAlt;
    private EditText text_locationAlt;
    private TextView text_locationAltUnits;

    private TextView labl_locationLat;
    private EditText text_locationLat;

    private TextView labl_locationLon;
    private EditText text_locationLon;

    //private LinearLayout layout_locationName;
    private TextView labl_locationName;
    private Spinner spin_locationName;
    private EditText text_locationName;
    private View inputOverlay;
    private View detailsLayout;

    private ImageButton button_list;
    private ImageButton button_add;
    private ImageButton button_edit;
    private ImageButton button_save;
    private ImageButton button_cancel;

    private ImageButton button_map;

    private ImageButton button_getfix;
    private ProgressBar progress_getfix;
    private final GetFixUI getFixUI_editMode = new GetFixUI()
    {
        @Override
        public void enableUI(boolean value)
        {
            text_locationName.requestFocus();
            text_locationLat.setEnabled(value);
            text_locationLon.setEnabled(value);
            text_locationAlt.setEnabled(value);
            text_locationName.setEnabled(value);
        }

        @Override
        public void updateUI(Location... locations)
        {
            DecimalFormat formatter = com.forrestguice.suntimeswidget.calculator.core.Location.decimalDegreesFormatter();
            if (locations != null && locations[0] != null)
            {
                Context context = getContext();
                LengthUnit units = (context != null ? WidgetSettings.loadLengthUnitsPref(context, appWidgetId) : LengthUnit.METRIC);

                text_locationLat.setText( formatter.format(locations[0].getLatitude()) );
                text_locationLon.setText( formatter.format(locations[0].getLongitude()) );
                text_locationAlt.setText( getAltitudeString(locations[0], formatter, units) );

            } else {
                text_locationLat.setText("");
                text_locationLon.setText("");
                text_locationAlt.setText("");
            }
        }

        @Override
        public void showProgress(boolean showProgress) {
            if (progress_getfix != null) {
                progress_getfix.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public void onStart()
        {
            if (button_getfix != null) {
                button_getfix.setVisibility(View.GONE);
            }
            if (button_map != null) {
                button_map.setVisibility(View.GONE);
            }
        }

        @Override
        public void onResult(LocationResult result)
        {
            if (button_getfix != null) {
                button_getfix.setImageResource((result.getResult() == null) ? ICON_GPS_SEARCHING : ICON_GPS_FOUND);
                button_getfix.setVisibility(View.VISIBLE);
                button_getfix.setEnabled(true);
            }
            if (button_map != null) {
                button_map.setVisibility(View.VISIBLE);
            }
        }

    };

    protected CharSequence getAltitudeString(Location location, DecimalFormat formatter, LengthUnit units)
    {
        switch (units)
        {
            case IMPERIAL:
                return formatter.format(LengthUnit.metersToFeet(location.getAltitude()));

            case METRIC:
            default:
                return formatter.format(location.getAltitude());
        }
    }

    private ImageButton button_auto;
    private ProgressBar progress_auto;
    private final GetFixUI getFixUI_autoMode = new GetFixUI()
    {
        @Override
        public void enableUI(boolean value)
        {
            text_locationLat.setEnabled(false);
            text_locationLon.setEnabled(false);
            text_locationAlt.setEnabled(false);
            text_locationName.setEnabled(false);
        }

        @Override
        public void updateUI(Location... locations)
        {
            DecimalFormat formatter = com.forrestguice.suntimeswidget.calculator.core.Location.decimalDegreesFormatter();
            if (locations != null && locations.length > 0 && locations[0] != null)
            {
                Context context = getContext();
                LengthUnit units = (context != null ? WidgetSettings.loadLengthUnitsPref(context, appWidgetId) : LengthUnit.METRIC);

                text_locationLat.setText( formatter.format(locations[0].getLatitude()) );
                text_locationLon.setText( formatter.format(locations[0].getLongitude()) );
                text_locationAlt.setText( getAltitudeString(locations[0], formatter, units) );
            }
        }

        @Override
        public void showProgress(boolean showProgress)
        {
            progress_auto.setVisibility((showProgress ? View.VISIBLE : View.GONE));
        }

        @Override
        public void onStart()
        {
            button_auto.setVisibility(View.GONE);
        }

        @Override
        public void onResult(LocationResult result)
        {
            button_auto.setImageResource((result.getResult() == null) ? ICON_GPS_SEARCHING : ICON_GPS_FOUND);
            button_auto.setVisibility(View.VISIBLE);
            button_auto.setEnabled(true);
        }
    };

    private LocationHelper getFixHelper;
    private SimpleCursorAdapter getFixAdapter;

    /**
     *
     * @param context a context used to access resources
     */
    protected void initViews( Context context, AppCompatActivity parent )
    {
        //Log.d("DEBUG", "LocationConfigView initViews");
        WidgetSettings.initDisplayStrings(context);

        flipper = (ViewFlipper)findViewById(R.id.view_flip);
        flipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));

        flipper2 = (ViewFlipper)findViewById(R.id.view_flip2);
        flipper2.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
        flipper2.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));

        ArrayAdapter<LocationMode> spinner_locationModeAdapter = new LocationModeAdapter(context, LocationMode.values());
        spinner_locationModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_locationMode = (Spinner)findViewById(R.id.appwidget_location_mode);
        spinner_locationMode.setAdapter(spinner_locationModeAdapter);
        spinner_locationMode.setOnItemSelectedListener(onLocationModeSelected);

        //layout_locationName = (LinearLayout) findViewById(R.id.appwidget_location_name_layout);
        labl_locationName = (TextView) findViewById(R.id.appwidget_location_name_label);
        text_locationName = (EditText) findViewById(R.id.appwidget_location_name);

        String[] from = new String[] {"name"};
        int[] to = new int[] {android.R.id.text1};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            getFixAdapter = new SimpleCursorAdapter(context, R.layout.layout_listitem_locations, null, from, to, 0);
        else getFixAdapter = new SimpleCursorAdapter(context, R.layout.layout_listitem_locations, null, from, to);

        getFixAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin_locationName = (Spinner)findViewById(R.id.appwidget_location_nameSelect);
        spin_locationName.setAdapter(getFixAdapter);
        spin_locationName.setOnItemSelectedListener(onCustomLocationSelected);

        detailsLayout = findViewById(R.id.layout_details);

        inputOverlay = findViewById(R.id.appwidget_location_latlon_overlay);
        inputOverlay.setVisibility(View.GONE);
        inputOverlay.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mode == LocationViewMode.MODE_CUSTOM_SELECT)
                {
                    setMode(LocationViewMode.MODE_CUSTOM_EDIT);
                }
            }
        });

        labl_locationLat = (TextView)findViewById(R.id.appwidget_location_lat_label);
        text_locationLat = (EditText)findViewById(R.id.appwidget_location_lat);

        labl_locationLon = (TextView)findViewById(R.id.appwidget_location_lon_label);
        text_locationLon = (EditText)findViewById(R.id.appwidget_location_lon);

        labl_locationAlt = (TextView)findViewById(R.id.appwidget_location_alt_label);
        text_locationAlt = (EditText)findViewById(R.id.appwidget_location_alt);
        text_locationAltUnits = (TextView)findViewById(R.id.appwidget_location_alt_units);

        button_list = (ImageButton)findViewById(R.id.appwidget_location_list);
        TooltipCompat.setTooltipText(button_list, button_list.getContentDescription());
        button_list.setOnClickListener(onListButtonClicked);

        button_cancel = (ImageButton)findViewById(R.id.appwidget_location_cancel);
        TooltipCompat.setTooltipText(button_cancel, button_cancel.getContentDescription());
        button_cancel.setOnClickListener(onEditCancelButtonClicked);

        // custom mode: toggle edit mode
        button_edit = (ImageButton)findViewById(R.id.appwidget_location_edit);
        TooltipCompat.setTooltipText(button_edit, button_edit.getContentDescription());
        button_edit.setOnClickListener(onEditButtonClicked);

        // custom mode: toggle edit mode (add)
        button_add = (ImageButton)findViewById(R.id.appwidget_location_add);
        button_add.setOnClickListener(onAddButtonClicked);

        // custom mode: save location
        button_save = (ImageButton)findViewById(R.id.appwidget_location_save);
        TooltipCompat.setTooltipText(button_save, button_save.getContentDescription());
        button_save.setOnClickListener(onSaveButtonClicked);

        // custom mode: get coordinates from map
        button_map = (ImageButton) findViewById(R.id.appwidget_location_mapview);
        if (button_map != null) {
            TooltipCompat.setTooltipText(button_map, button_map.getContentDescription());
            button_map.setOnClickListener(onMapButtonClicked);
        }

        // custom mode: get GPS fix
        progress_getfix = (ProgressBar)findViewById(R.id.appwidget_location_getfixprogress);
        if (progress_getfix != null) {
            progress_getfix.setVisibility(View.GONE);
        }

        button_getfix = (ImageButton)findViewById(R.id.appwidget_location_getfix);
        button_getfix.setOnClickListener(onGetFixClicked);
        TooltipCompat.setTooltipText(button_getfix, button_getfix.getContentDescription());

        // auto mode: get GPS fix
        progress_auto = (ProgressBar)findViewById(R.id.appwidget_location_auto_progress);
        progress_auto.setVisibility(View.GONE);

        button_auto = (ImageButton)findViewById(R.id.appwidget_location_auto);
        TooltipCompat.setTooltipText(button_auto, button_auto.getContentDescription());
        button_auto.setOnClickListener(onAutoButtonClicked);

        // 0; getFixUI_editMode
        GetFixHelper helper = new GetFixHelper(parent, getFixUI_editMode, new GetFixHelper.GetFixHelperListener()
        {
            @Override
            public void onRequestPermissions(String[] permissions, int requestID) {
                if (viewListener != null) {
                    viewListener.onRequestPermissions(permissions, requestID);
                } else Log.e("LocationConfigView", "onRequestPermissions: required listener is null!");
            }
        });
        getFixHelper = helper;
        getFixHelper.addUI(getFixUI_autoMode);                           // 1; getFixUI_autoMode
        updateGPSButtonIcons();

        if (hideTitle) {
            setHideTitle(hideTitle);
        }

        if (hideMode) {
            setHideMode(hideMode);
        }
    }

    protected View.OnClickListener onMapButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (getContext() != null) {
                showMapCoordinateDialog(getContext());
            }
        }
    };

    public static final String DIALOGTAG_MAP = "MapDialog";
    protected void showMapCoordinateDialog(@NonNull Context context)
    {
        MapCoordinateDialog dialog = new MapCoordinateDialog();
        dialog.setColorCollection(new WorldMapColorValuesCollection<>(context));
        dialog.setInitialCoordinates(text_locationLon.getText().toString(), text_locationLat.getText().toString());
        dialog.setOnAcceptedListener(onMapCoordinateDialogAccepted(dialog));

        if (getFragment() != null) {
            dialog.show(getFragment().getFragment().getChildFragmentManager(), DIALOGTAG_MAP);
        }
    }

    private DialogInterface.OnClickListener onMapCoordinateDialogAccepted(final MapCoordinateDialog dialog)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                Context context = getContext();
                if (context != null) {
                    double latitude = dialog.getSelectedLatitude(context);
                    double longitude = dialog.getSelectedLongitude(context);
                    text_locationLat.setText(String.format(Locale.getDefault(), "%.3f", latitude));
                    text_locationLon.setText(String.format(Locale.getDefault(), "%.3f", longitude));
                }
            }
        };
    }


    protected View.OnClickListener onGetFixClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            lookupLocation();
        }
    };

    public void lookupLocation() {
        getFixHelper.getFix(0, true);
    }


    public void updateGPSButtonIcons()
    {
        int icon = GetFixUI.ICON_GPS_SEARCHING;
        if (!isInEditMode())
        {
            if (getContext() == null || !getFixHelper.isLocationEnabled(getContext()))
            {
                icon = GetFixUI.ICON_GPS_DISABLED;

            } else if (getFixHelper.hasFix()) {
                icon = GetFixUI.ICON_GPS_FOUND;
            }
        }
        button_getfix.setImageResource(icon);
        button_auto.setImageResource(icon);
    }

    public void onResume()
    {
        //Log.d("DEBUG", "LocationConfigView onResume");
        updateGPSButtonIcons();
        getFixHelper.onResume();
    }

    /**
     * @param location a WidgetSettings.Location instance to update from
     */
    @SuppressLint("SetTextI18n")
    public void updateViews(com.forrestguice.suntimeswidget.calculator.core.Location location)
    {
        text_locationLat.setText(location.getLatitude());
        text_locationLon.setText(location.getLongitude());
        text_locationName.setText(location.getLabel());

        Context context = getContext();
        if (context != null)
        {
            LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, appWidgetId);
            switch (units)
            {
                case IMPERIAL:
                    text_locationAlt.setText( Double.toString(LengthUnit.metersToFeet(location.getAltitudeAsDouble())) );
                    text_locationAltUnits.setText(context.getString(R.string.units_feet_short));
                    break;

                case METRIC:
                default:
                    text_locationAlt.setText(location.getAltitude());
                    text_locationAltUnits.setText(context.getString(R.string.units_meters));
                    break;
            }
        }
    }
    public void updateViews()
    {
        int position = spin_locationName.getSelectedItemPosition();
        if (position >= 0)
        {
            Cursor cursor = getFixAdapter.getCursor();
            cursor.moveToPosition(position);
            if (cursor.getColumnCount() >= 4) {
                updateViews(new com.forrestguice.suntimeswidget.calculator.core.Location(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            }
        }
    }


    /**
     * @param context a context used to access shared prefs
     */
    public void loadSettings(@NonNull Context context)
    {
        //Log.d("DEBUG", "LocationConfigView loadSettings (prefs)");
        if (isInEditMode())
            return;

        LocationMode locationMode = WidgetSettings.loadLocationModePref(context, appWidgetId);
        if (locationMode == LocationMode.CURRENT_LOCATION && !autoAllowed)
        {
            spinner_locationMode.setSelection(LocationViewMode.MODE_CUSTOM_SELECT.ordinal());
        } else {
            spinner_locationMode.setSelection(locationMode.ordinal());
        }

        com.forrestguice.suntimeswidget.calculator.core.Location location = WidgetSettings.loadLocationPref(context, appWidgetId);
        updateViews(location);
    }

    /**
     * @param context a context used to access shared prefs
     * @param bundle a Bundle containing saved state
     */
    public void loadSettings(@NonNull Context context, @NonNull Bundle bundle )
    {
        //Log.d("DEBUG", "LocationConfigView loadSettings (bundle)");

        // restore LocationMode spinner
        String modeString = bundle.getString(KEY_LOCATION_MODE);
        if (modeString != null)
        {
            LocationMode locationMode;
            try {
                locationMode = LocationMode.valueOf(modeString);
            } catch (IllegalArgumentException e) {
                locationMode = WidgetSettings.PREF_DEF_LOCATION_MODE;
            }
            spinner_locationMode.setSelection(locationMode.ordinal());

        } else {
            spinner_locationMode.setSelection(WidgetSettings.PREF_DEF_LOCATION_MODE.ordinal());
        }

        // restore location text fields
        String label = bundle.getString(KEY_LOCATION_LABEL);
        String longitude = bundle.getString(KEY_LOCATION_LONGITUDE);
        String latitude = bundle.getString(KEY_LOCATION_LATITUDE);
        String altitude = bundle.getString(KEY_LOCATION_ALTITUDE);
        com.forrestguice.suntimeswidget.calculator.core.Location location;
        if (longitude != null && latitude != null)
        {
            if (altitude != null)
                location = new com.forrestguice.suntimeswidget.calculator.core.Location(label, latitude, longitude, altitude);
            else location = new com.forrestguice.suntimeswidget.calculator.core.Location(label, latitude, longitude);

        } else {
            Log.w("LocationConfigView", "Bundle contained null lat or lon; falling back to saved prefs.");
            location = WidgetSettings.loadLocationPref(context, appWidgetId);
        }
        updateViews(location);

        // restore dialog (sub)state
        String viewModeString = bundle.getString(KEY_DIALOGMODE);
        if (viewModeString != null)
        {
            LocationViewMode viewMode;
            try {
                viewMode = LocationViewMode.valueOf(viewModeString);
            } catch (IllegalArgumentException e) {
                Log.w("DEBUG", "Bundle contained bad viewModeString! " + e.toString());
                viewMode = LocationViewMode.MODE_CUSTOM_SELECT;
            }
            setMode(viewMode);

            if (viewMode == LocationViewMode.MODE_CUSTOM_SELECT) {
                populateLocationList();
            }
        }

        getFixHelper.loadSettings(bundle);
    }

    /**
     * @param context a context used to access shared prefs
     * @param data a Uri with geo location data
     */
    public void loadSettings(@NonNull Context context, Uri data )
    {
        //Log.d("DEBUG", "LocationConfigView loadSettings (uri)");
        loadSettings(context, bundleData(data, context.getString(R.string.gps_lastfix_title_set)));
    }

    /**
     *
     */
    public boolean saveSettings(@NonNull Context context) {
        return saveSettings(context, appWidgetId);
    }
    public boolean saveSettings(@NonNull Context context, int appWidgetId)
    {
        //Log.d("DEBUG", "LocationConfigView loadSettings (prefs)");

        LocationMode locationMode = getLocationMode();
        WidgetSettings.saveLocationModePref(context, appWidgetId, locationMode);

        if (validateInput(context))
        {
            String latitude = text_locationLat.getText().toString();
            String longitude = text_locationLon.getText().toString();
            String altitude = text_locationAlt.getText().toString();
            String name = text_locationName.getText().toString();
            com.forrestguice.suntimeswidget.calculator.core.Location location = new com.forrestguice.suntimeswidget.calculator.core.Location(name, latitude, longitude, altitude, WidgetSettings.loadLengthUnitsPref(context, appWidgetId) == LengthUnit.METRIC);

            if (appWidgetId == 0) {
                AppSettings.saveLocationPref(context, location);    // this is WidgetSettings.saveLocationPref pluss ide effects like triggering widget and alarm updates
            } else {
                WidgetSettings.saveLocationPref(context, appWidgetId, location);
            }
            return true;
        }
        return false;
    }

    /**
     * @param bundle a Bundle to save to
     * @return true settings were saved
     */
    public boolean saveSettings(@NonNull Bundle bundle)
    {
        //Log.d("DEBUG", "LocationConfigView saveSettings (bundle)");

        LocationMode locationMode = getLocationMode();
        String latitude = text_locationLat.getText().toString();
        String longitude = text_locationLon.getText().toString();
        String altitude = text_locationAlt.getText().toString();
        String name = text_locationName.getText().toString();

        bundle.putString(KEY_DIALOGMODE, mode.name());
        bundle.putString(KEY_LOCATION_MODE, locationMode.name());
        bundle.putString(KEY_LOCATION_LATITUDE, latitude);
        bundle.putString(KEY_LOCATION_LONGITUDE, longitude);
        bundle.putString(KEY_LOCATION_ALTITUDE, altitude);
        bundle.putString(KEY_LOCATION_LABEL, name);

        getFixHelper.saveSettings(bundle);
        return true;
    }

    public static Bundle bundleData( Uri data, String label )
    {
        return bundleData(data, label, LocationViewMode.MODE_CUSTOM_ADD);
    }
    public static Bundle bundleData( Uri data, String label, LocationViewMode viewMode )
    {
        String lat = "";
        String lon = "";
        String alt = "";

        if (data != null && SCHEME_GEO.equals(data.getScheme()))
        {
            String dataString = data.getSchemeSpecificPart();
            String[] dataParts = dataString.split(Pattern.quote("?"));
            if (dataParts.length > 0)
            {
                String geoPath = dataParts[0];
                String[] geoParts = geoPath.split(Pattern.quote(","));
                if (geoParts.length >= 2)
                {
                    lat = geoParts[0];
                    lon = geoParts[1];

                    if (geoParts.length >= 3)
                    {
                        alt = geoParts[2];
                    }
                }
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString(KEY_DIALOGMODE, viewMode.name());
        bundle.putString(KEY_LOCATION_MODE, LocationMode.CUSTOM_LOCATION.name());
        bundle.putString(KEY_LOCATION_LATITUDE, lat);
        bundle.putString(KEY_LOCATION_LONGITUDE, lon);
        bundle.putString(KEY_LOCATION_ALTITUDE, alt);
        bundle.putString(KEY_LOCATION_LABEL, label);
        return bundle;
    }

    /**
     * Cancel any running getfix tasks.
     */
    public void cancelGetFix()
    {
        getFixHelper.cancelGetFix();
    }

    /**
     * Dismiss any "enable GPS" prompts.
     */
    //public void dismissGPSEnabledPrompt() { getFixHelper.dismissGPSEnabledPrompt(); }

    /**
     * @param requestCode the request code that was passed to requestPermissions
     * @param permissions the requested permissions
     * @param grantResults either PERMISSION_GRANTED or PERMISSION_DENIED for each of the requested permissions
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        getFixHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     *
     */
    public void populateLocationList()
    {
        Context context = getContext();
        if (context == null) {
            Log.w("LocationConfigView", "populateLocationList: context is null!");
            return;
        }

        LocationListTask task = new LocationListTask(context, getLocation(context));
        TaskListener<LocationListTask.LocationListTaskResult> taskListener = new SimpleTaskListener<LocationListTask.LocationListTaskResult>()
        {
            @Override
            public void onFinished(LocationListTask.LocationListTaskResult r)
            {
                Cursor result = r.getCursor();
                int selectedIndex = r.getIndex();
                getFixAdapter.changeCursor(result);
                spin_locationName.setSelection(selectedIndex);
            }
        };
        ExecutorUtils.runTask("LocationListTask", AndroidTaskHandler.get(), task, taskListener);
    }

    public void clickLocationSpinner() {
        spin_locationName.performClick();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        cleanupAdapter();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View view, int visibility)
    {
        super.onVisibilityChanged(view, visibility);
        if (visibility != View.VISIBLE) {
            cleanupAdapter();
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible)    // TODO: only called for api 24+ ?
    {
        super.onVisibilityAggregated(isVisible);
        if (!isVisible) {
            cleanupAdapter();
        }
    }

    protected void cleanupAdapter() {
        if (getFixAdapter != null) {
            getFixAdapter.changeCursor(null);    // closes previous cursor
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

    /**
     * Check text fields for validity; as a side-effect sets an error message on fields with invalid
     * values.
     * @return true if all fields valid, false otherwise
     */
    public boolean validateInput(@NonNull Context context)
    {
        boolean isValid = true;

        String latitude = text_locationLat.getText().toString();
        try {
            BigDecimal lat = new BigDecimal(latitude);
            if (lat.doubleValue() < -90d || lat.doubleValue() > 90d)
            {
                isValid = false;
                text_locationLat.setError(context.getString(R.string.location_dialog_error_lat));
            }

        } catch (NumberFormatException e1) {
            isValid = false;
            text_locationLat.setError(context.getString(R.string.location_dialog_error_lat));
        }

        String longitude = text_locationLon.getText().toString();
        try {
            BigDecimal lon = new BigDecimal(longitude);
            if (lon.doubleValue() < -180d || lon.doubleValue() > 180d)
            {
                isValid = false;
                text_locationLon.setError(context.getString(R.string.location_dialog_error_lon));
            }

        } catch (NumberFormatException e2) {
            isValid = false;
            text_locationLon.setError(context.getString(R.string.location_dialog_error_lon));
        }

        String altitude = text_locationAlt.getText().toString();
        if (!altitude.trim().isEmpty())
        {
            try {
                BigDecimal alt = new BigDecimal(altitude);

            } catch (NumberFormatException e3) {
                isValid = false;
                text_locationAlt.setError(context.getString(R.string.location_dialog_error_alt));
            }
        }

        return isValid;
    }

    /**
     * Enum of possible ui states; auto mode, custom (select), custom (add), custom (edit) modes.
     */
    public static enum LocationViewMode
    {
        MODE_AUTO(), MODE_CUSTOM_SELECT(), MODE_CUSTOM_ADD(), MODE_CUSTOM_EDIT(), MODE_DISABLED;
        private LocationViewMode() {}

        public String toString()
        {
            return this.name();
        }

        public int ordinal( LocationViewMode[] array )
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

    /**
     * Copy the location in decimal degrees (DD) to clipboard (locale invariant `lat, lon`)
     */
    public void copyLocationToClipboard(@NonNull Context context)
    {
        copyLocationToClipboard(context, false);
    }
    public void copyLocationToClipboard(@NonNull Context context, boolean silent)
    {
        com.forrestguice.suntimeswidget.calculator.core.Location location = getLocation(context);
        String clipboardText = location.toString();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null)
            {
                ClipData clip = ClipData.newPlainText("lat, lon", clipboardText);
                clipboard.setPrimaryClip(clip);
            }

        } else {
            @SuppressWarnings("deprecation")
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null)
            {
                //noinspection deprecation
                clipboard.setText(clipboardText);
            }
        }

        if (!silent)
        {
            Toast.makeText(context, SuntimesUtils.fromHtml(context.getString(R.string.location_dialog_toast_copied, clipboardText)), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * the location mode (auto, custom) has been selected from a spinner.
     */
    private final Spinner.OnItemSelectedListener onLocationModeSelected = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            LocationMode locationMode = (LocationMode) parent.getItemAtPosition(position);
            //Log.d("DEBUG", "onLocationModeSelected " + locationMode.name());

            LocationViewMode dialogMode;
            if (locationMode == LocationMode.CUSTOM_LOCATION)
            {
                if (mode != LocationViewMode.MODE_CUSTOM_SELECT &&
                    mode != LocationViewMode.MODE_CUSTOM_ADD &&
                    mode != LocationViewMode.MODE_CUSTOM_EDIT)
                {
                    dialogMode = LocationViewMode.MODE_CUSTOM_SELECT;
                    setMode(dialogMode);
                }

            } else {
                if (mode == LocationViewMode.MODE_CUSTOM_ADD ||
                    mode == LocationViewMode.MODE_CUSTOM_EDIT)
                {
                    populateLocationList();  // triggers 'add place'
                }

                dialogMode = LocationViewMode.MODE_AUTO;
                setMode(dialogMode);
            }
        }
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /**
     * a custom location has been selected from a spinner.
     */
    private final Spinner.OnItemSelectedListener onCustomLocationSelected = new Spinner.OnItemSelectedListener()
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            Cursor cursor = getFixAdapter.getCursor();
            cursor.moveToPosition(position);

            if (cursor.getColumnCount() >= 4)
            {
                updateViews(new com.forrestguice.suntimeswidget.calculator.core.Location(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)));
            }
        }
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private final View.OnClickListener onListButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if (onListButtonClickListener != null) {
                 onListButtonClickListener.onClick(view);
            }
        }
    };
    private View.OnClickListener onListButtonClickListener = null;
    public void setOnListButtonClicked(View.OnClickListener listener) {
        onListButtonClickListener = listener;
    }

    private final View.OnClickListener onEditCancelButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            updateViews();   // reset changes
            setMode(LocationViewMode.MODE_CUSTOM_SELECT);
            if (AppSettings.isTelevision(getContext())) {
                button_edit.requestFocus();
            }
        }
    };

    /**
     * LocationConfigViewListener
     */
    public static class LocationConfigViewListener
    {
        public void onModeChanged(LocationViewMode mode) {}
        public void onRequestPermissions(@NonNull String[] permissions, int requestID) {}
    }

    public void setViewListener(LocationConfigViewListener l) {
        viewListener = l;
    }
    private LocationConfigViewListener viewListener = null;

    /**
     * the custom location edit button has been clicked.
     */
    private final View.OnClickListener onEditButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            setMode(LocationViewMode.MODE_CUSTOM_EDIT);
        }
    };

    /**
     * the custom location add button has been clicked.
     */
    private final View.OnClickListener onAddButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            setMode(LocationViewMode.MODE_CUSTOM_EDIT);
            text_locationName.selectAll();
            text_locationName.requestFocus();
        }
    };

    /**
     * the custom location save button has been clicked.
     */
    private final View.OnClickListener onSaveButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Context context = getContext();
            final boolean validInput = (context != null && validateInput(context));
            if (validInput)
            {
                setMode(LocationViewMode.MODE_CUSTOM_SELECT);
                populateLocationList();
                if (AppSettings.isTelevision(context)) {
                    button_edit.requestFocus();
                }
            }

            final GetFixTaskListener cancelGetFixListener = new GetFixTaskListener()
            {
                @Override
                public void onCancelled()
                {
                    if (validInput)
                    {
                        setMode(LocationViewMode.MODE_CUSTOM_SELECT);
                        populateLocationList();
                        if (AppSettings.isTelevision(getContext())) {
                            button_edit.requestFocus();
                        }
                    }
                }
            };
            getFixHelper.removeGetFixTaskListener(cancelGetFixListener);
            getFixHelper.addGetFixTaskListener(cancelGetFixListener);
            getFixHelper.cancelGetFix();
        }
    };

    /**
     * the auto location button has been clicked.
     */
    private final View.OnClickListener onAutoButtonClicked = new OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            getFixHelper.getFix(1, true);
        }
    };

    /**
     *
     */
    @SuppressWarnings("Convert2Diamond")
    private class LocationModeAdapter extends ArrayAdapter<LocationMode>
    {
        private final Context context;
        private final ArrayList<LocationMode> modes;

        public LocationModeAdapter(Context context, ArrayList<LocationMode> modes)
        {
            super(context, R.layout.layout_listitem_locations, modes);
            this.context = context;
            this.modes = modes;
        }

        public LocationModeAdapter(Context context, LocationMode[] modes)
        {
            super(context, R.layout.layout_listitem_locations, modes);
            this.context = context;
            this.modes = new ArrayList<LocationMode>();
            Collections.addAll(this.modes, modes);
        }

        @Override
        public boolean areAllItemsEnabled()
        {
           return autoAllowed;
        }

        @Override
        public boolean isEnabled(int position)
        {
            //noinspection RedundantIfStatement
            if (position == 0 && !autoAllowed)
                return false;
            else return true;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return listItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return listItemView(position, convertView, parent);
        }

        private View listItemView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.layout_listitem_locations, parent, false);
            }

            LocationMode item = modes.get(position);

            //ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            //icon.setImageResource(item.getIcon());

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item.getDisplayString());

            if (item == LocationMode.CURRENT_LOCATION && !autoAllowed)
            {
                text.setTypeface(text.getTypeface(), Typeface.ITALIC);
                text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                text.setTextColor(text.getHintTextColors());
                view.setEnabled(false);
            }

            //TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            //text2.setText(item.toString());

            return view;
        }
    }

}
