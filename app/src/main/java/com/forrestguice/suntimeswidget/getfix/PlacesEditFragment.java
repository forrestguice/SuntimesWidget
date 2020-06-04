/**
    Copyright (C) 2020 Forrest Guice
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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class PlacesEditFragment extends BottomSheetDialogFragment
{
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LOCATION_LATITUDE = "locationLatitude";
    public static final String KEY_LOCATION_LONGITUDE = "locationLongitude";
    public static final String KEY_LOCATION_ALTITUDE = "locationAltitude";
    public static final String KEY_LOCATION_LABEL = "locationLabel";

    private EditText text_locationAlt;
    private TextView text_locationAltUnits;
    private EditText text_locationLat;
    private EditText text_locationLon;
    private EditText text_locationName;

    private ImageButton button_save;
    private ImageButton button_getfix;
    private ProgressBar progress_getfix;

    public PlacesEditFragment()
    {
        super();
        setArguments(new Bundle());
    }

    private GetFixHelper getFixHelper;
    private GetFixUI getFixUI_editMode = new GetFixUI()
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
        public void updateUI(android.location.Location... locations)
        {
            DecimalFormat formatter = com.forrestguice.suntimeswidget.calculator.core.Location.decimalDegreesFormatter();
            text_locationLat.setText( formatter.format(locations[0].getLatitude()) );
            text_locationLon.setText( formatter.format(locations[0].getLongitude()) );
            text_locationAlt.setText( altitudeDisplayString(locations[0], formatter, WidgetSettings.loadLengthUnitsPref(getContext(), 0)) );
        }

        @Override
        public void showProgress(boolean showProgress) {
            progress_getfix.setVisibility((showProgress ? View.VISIBLE : View.GONE));
        }

        @Override
        public void onStart() {
            button_getfix.setVisibility(View.GONE);
        }

        @Override
        public void onResult(android.location.Location result, boolean wasCancelled)
        {
            button_getfix.setImageResource((result == null) ? ICON_GPS_SEARCHING : ICON_GPS_FOUND);
            button_getfix.setVisibility(View.VISIBLE);
            button_getfix.setEnabled(true);
        }
    };

    protected FragmentListener listener;
    public void setFragmentListener( FragmentListener value ) {
        listener = value;
    }

    public interface FragmentListener
    {
        void onCanceled();
        void onAccepted(PlaceItem place);
    }

    private PlaceItem item = null;
    public void setPlace(PlaceItem item)
    {
        this.item = item;
        updateViews(item.location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getFixHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        cancelGetFix();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View view = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_place, parent, false);
        initViews(getActivity(), view);

        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);

        } else if (item != null) {
            setPlace(item);
        }
        return view;
    }

    protected void initViews(Context context, View content)
    {
        WidgetSettings.initDisplayStrings(context);

        text_locationName = (EditText) content.findViewById(R.id.appwidget_location_name);
        text_locationLat = (EditText) content.findViewById(R.id.appwidget_location_lat);
        text_locationLon = (EditText) content.findViewById(R.id.appwidget_location_lon);
        text_locationAlt = (EditText) content.findViewById(R.id.appwidget_location_alt);
        text_locationAltUnits = (TextView)content.findViewById(R.id.appwidget_location_alt_units);

        button_save = (ImageButton) content.findViewById(R.id.appwidget_location_save);
        button_save.setOnClickListener(onSaveButtonClicked);

        progress_getfix = (ProgressBar) content.findViewById(R.id.appwidget_location_getfixprogress);
        progress_getfix.setVisibility(View.GONE);

        button_getfix = (ImageButton) content.findViewById(R.id.appwidget_location_getfix);
        button_getfix.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getFixHelper.getFix(0);
            }
        });

        getFixHelper = new GetFixHelper(getActivity(), getFixUI_editMode);    // 0; getFixUI_editMode
        updateGPSButtonIcons();
    }

    /**
     * @param savedInstanceState a Bundle containing previously saved dialog state
     * @return an AlertDialog ready for display
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle bundle )
    {
        bundle.putParcelable(KEY_LOCATION, item);
        bundle.putString(KEY_LOCATION_LATITUDE, text_locationLat.getText().toString());
        bundle.putString(KEY_LOCATION_LONGITUDE, text_locationLon.getText().toString());
        bundle.putString(KEY_LOCATION_ALTITUDE, text_locationAlt.getText().toString());
        bundle.putString(KEY_LOCATION_LABEL, text_locationName.getText().toString());
        getFixHelper.saveSettings(bundle);
        super.onSaveInstanceState(bundle);
    }

    protected void loadSettings(Bundle bundle)
    {
        item = bundle.getParcelable(KEY_LOCATION);
        String label = bundle.getString(KEY_LOCATION_LABEL);
        String longitude = bundle.getString(KEY_LOCATION_LONGITUDE);
        String latitude = bundle.getString(KEY_LOCATION_LATITUDE);
        String altitude = bundle.getString(KEY_LOCATION_ALTITUDE);

        if (longitude != null && latitude != null)
        {
            com.forrestguice.suntimeswidget.calculator.core.Location location;
            if (altitude != null)
                location = new com.forrestguice.suntimeswidget.calculator.core.Location(label, latitude, longitude, altitude);
            else location = new com.forrestguice.suntimeswidget.calculator.core.Location(label, latitude, longitude);
            updateViews(location);
        }
        getFixHelper.loadSettings(bundle);
    }

    private DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            expandSheet(dialogInterface);
        }
    };

    @Override
    public void onCancel(DialogInterface dialog)
    {
        cancelGetFix();
        dismiss();
        if (listener != null) {
            listener.onCanceled();
        }
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void cancelGetFix() {
        getFixHelper.cancelGetFix();
    }

    public void updateGPSButtonIcons()
    {
        int icon = GetFixUI.ICON_GPS_SEARCHING;
        if (!getFixHelper.isLocationEnabled(getContext())) {
            icon = GetFixUI.ICON_GPS_DISABLED;

        } else if (getFixHelper.gotFix) {
            icon = GetFixUI.ICON_GPS_FOUND;
        }
        button_getfix.setImageResource(icon);
    }

    public static Bundle bundleData( Uri data, String label )
    {
        String lat = "";
        String lon = "";
        String alt = "";

        if (data != null && "geo".equals(data.getScheme()))
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
        bundle.putString(KEY_LOCATION_LATITUDE, lat);
        bundle.putString(KEY_LOCATION_LONGITUDE, lon);
        bundle.putString(KEY_LOCATION_ALTITUDE, alt);
        bundle.putString(KEY_LOCATION_LABEL, label);
        return bundle;
    }

    @SuppressLint("SetTextI18n")
    private void updateViews(com.forrestguice.suntimeswidget.calculator.core.Location location)
    {
        if (text_locationName == null) {
            return;
        }

        if (item == null || item.location == null)
        {
            text_locationLat.setText("");
            text_locationLon.setText("");
            text_locationName.setText("");
            text_locationAlt.setText("");
            text_locationAltUnits.setText("");
            return;
        }

        text_locationLat.setText(location.getLatitude());
        text_locationLon.setText(location.getLongitude());
        text_locationName.setText(location.getLabel());

        Context context = getContext();
        if (context != null)
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(getContext(), 0);
            switch (units)
            {
                case IMPERIAL:
                case USC:
                    text_locationAlt.setText( Double.toString(WidgetSettings.LengthUnit.metersToFeet(location.getAltitudeAsDouble())) );
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

    protected PlaceItem createPlaceItem(PlaceItem item0)
    {
        PlaceItem item = new PlaceItem();
        if (item0 != null)
        {
            item.rowID = item0.rowID;
            item.location = new Location(text_locationName.getText().toString(), text_locationLat.getText().toString(), text_locationLon.getText().toString(), text_locationAlt.getText().toString(),
                    WidgetSettings.loadLengthUnitsPref(getActivity(), 0) == WidgetSettings.LengthUnit.METRIC);

        } else {
            item.rowID = -1;
            item.location = new Location(text_locationName.getText().toString(), text_locationLat.getText().toString(), text_locationLon.getText().toString(), text_locationAlt.getText().toString(),
                    WidgetSettings.loadLengthUnitsPref(getActivity(), 0) == WidgetSettings.LengthUnit.METRIC);
        }
        return item;
    }

    private View.OnClickListener onSaveButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            final PlaceItem returnValue = createPlaceItem(item);
            final boolean validInput = validateInput();
            if (validInput)
            {
                if (listener != null) {
                    listener.onAccepted(returnValue);
                }
            }

            final GetFixTask.GetFixTaskListener cancelGetFixListener = new GetFixTask.GetFixTaskListener()
            {
                @Override
                public void onCancelled()
                {
                    if (validInput)
                    {
                        if (listener != null) {
                            listener.onAccepted(returnValue);
                        }
                    }
                }
            };
            getFixHelper.removeGetFixTaskListener(cancelGetFixListener);
            getFixHelper.addGetFixTaskListener(cancelGetFixListener);
            getFixHelper.cancelGetFix();
        }
    };


    public boolean validateInput()
    {
        Context myParent = getActivity();
        boolean isValid = true;

        String latitude = text_locationLat.getText().toString();
        try {
            BigDecimal lat = new BigDecimal(latitude);
            if (lat.doubleValue() < -90d || lat.doubleValue() > 90d)
            {
                isValid = false;
                text_locationLat.setError(myParent.getString(R.string.location_dialog_error_lat));
            }

        } catch (NumberFormatException e1) {
            isValid = false;
            text_locationLat.setError(myParent.getString(R.string.location_dialog_error_lat));
        }

        String longitude = text_locationLon.getText().toString();
        try {
            BigDecimal lon = new BigDecimal(longitude);
            if (lon.doubleValue() < -180d || lon.doubleValue() > 180d)
            {
                isValid = false;
                text_locationLon.setError(myParent.getString(R.string.location_dialog_error_lon));
            }

        } catch (NumberFormatException e2) {
            isValid = false;
            text_locationLon.setError(myParent.getString(R.string.location_dialog_error_lon));
        }

        String altitude = text_locationAlt.getText().toString();
        if (!altitude.trim().isEmpty())
        {
            try {
                BigDecimal alt = new BigDecimal(altitude);

            } catch (NumberFormatException e3) {
                isValid = false;
                text_locationAlt.setError(myParent.getString(R.string.location_dialog_error_alt));
            }
        }

        return isValid;
    }

    public static CharSequence altitudeDisplayString(android.location.Location location, DecimalFormat formatter, WidgetSettings.LengthUnit units)
    {
        switch (units)
        {
            case IMPERIAL:
            case USC:
                return formatter.format(WidgetSettings.LengthUnit.metersToFeet(location.getAltitude()));

            case METRIC:
            default:
                return formatter.format(location.getAltitude());
        }
    }


}

