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
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.design.view.ActionModeHelper;
import com.forrestguice.support.design.widget.BottomSheetDialogFragment;
import com.forrestguice.support.design.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.TooltipCompat;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class PlacesEditFragment extends BottomSheetDialogFragment
{
    public static final String KEY_DIALOGTHEME = "dialogtheme";

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

    private ImageButton button_cancel, button_save;

    private ImageButton button_getfix;
    private ProgressBar progress_getfix;

    protected ActionModeHelper.ActionModeInterface actionMode = null;
    protected PlacesEditActionCompat actions = new PlacesEditActionCompat();

    public PlacesEditFragment()
    {
        super();
        setArguments(new Bundle());
    }

    protected LocationHelper getFixHelper = null;
    public void setLocationHelper( @Nullable LocationHelper helper ) {
        getFixHelper = helper;
    }
    @Nullable
    protected LocationHelper createLocationHelper() {
        return null;
    }

    public GetFixUI getFixUI() {
        return getFixUI_editMode;
    }
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
            if (locations != null && locations[0] != null)
            {
                text_locationLat.setText( formatter.format(locations[0].getLatitude()) );
                text_locationLon.setText( formatter.format(locations[0].getLongitude()) );
                text_locationAlt.setText( altitudeDisplayString(locations[0], formatter, WidgetSettings.loadLengthUnitsPref(getContext(), 0)) );
            }
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
        void onCanceled(PlaceItem place);
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
        if (getFixHelper != null) {
            getFixHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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

    public void setDialogThemOverride(@Nullable Integer resID)
    {
        if (resID != null) {
            getArguments().putInt(KEY_DIALOGTHEME, resID);
        } else getArguments().remove(KEY_DIALOGTHEME);
    }
    @Nullable
    protected Integer getDialogThemeOverride()
    {
        int resID = getArguments().getInt(KEY_DIALOGTHEME, -1);
        return (resID >= 0 ? resID : null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState)
    {
        Integer appTheme = getDialogThemeOverride();
        View view = ((appTheme != null) ? inflater.cloneInContext(new ContextThemeWrapper(getActivity(), appTheme)).inflate(R.layout.layout_dialog_place, parent, false)
                                        : inflater.inflate(R.layout.layout_dialog_place, parent, false));
        initViews(getActivity(), view);

        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);

        } else if (item != null) {
            setPlace(item);

        } else {
            updateViews(null);
        }

        //triggerActionMode(item);
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

        if (text_locationAlt != null) {
            text_locationAlt.setNextFocusDownId(R.id.save_button);
        }

        button_save = (ImageButton) content.findViewById(R.id.save_button);
        if (button_save != null) {
            TooltipCompat.setTooltipText(button_save, button_save.getContentDescription());
            button_save.setOnClickListener(onSaveButtonClicked);
        }

        button_cancel = (ImageButton) content.findViewById(R.id.cancel_button);
        if (button_cancel != null) {
            TooltipCompat.setTooltipText(button_cancel, button_cancel.getContentDescription());
            button_cancel.setOnClickListener(onCancelButtonClicked);
        }

        progress_getfix = (ProgressBar) content.findViewById(R.id.appwidget_location_getfixprogress);
        progress_getfix.setVisibility(View.GONE);

        button_getfix = (ImageButton) content.findViewById(R.id.appwidget_location_getfix);
        TooltipCompat.setTooltipText(button_getfix, button_getfix.getContentDescription());
        button_getfix.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (getFixHelper != null) {
                    getFixHelper.getFix(0);
                }
            }
        });

        getFixHelper = createLocationHelper();
        if (getFixHelper != null) {
            getFixHelper.setFragment(this);
        }
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
        if (getFixHelper != null) {
            getFixHelper.saveSettings(bundle);
        }
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
        if (getFixHelper != null) {
            getFixHelper.loadSettings(bundle);
        }
    }

    private final DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface)
        {
            expandSheet(dialogInterface);
            disableTouchOutsideBehavior();

            if (AppSettings.isTelevision(getActivity())) {
                button_cancel.requestFocus();
            }
        }
    };

    @Override
    public void onCancel(DialogInterface dialog)
    {
        cancelGetFix();
        dismiss();
        if (actionMode != null) {
            actionMode.finish();
        }
        if (listener != null) {
            listener.onCanceled(item);
        }
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetBehaviorCompat behavior = initBottomSheetBehavior(dialog);
        if (behavior != null)
        {
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehaviorCompat.STATE_EXPANDED);
        }
    }

    public void cancelGetFix() {
        if (getFixHelper != null) {
            getFixHelper.cancelGetFix();
        }
    }

    public void updateGPSButtonIcons()
    {
        int icon = GetFixUI.ICON_GPS_DISABLED;
        if (getFixHelper != null)
        {
            icon = GetFixUI.ICON_GPS_SEARCHING;
            if (!getFixHelper.isLocationEnabled(getContext())) {
                icon = GetFixUI.ICON_GPS_DISABLED;

            } else if (getFixHelper.hasFix()) {
                icon = GetFixUI.ICON_GPS_FOUND;
            }
        }
        button_getfix.setImageResource(icon);
        button_getfix.setVisibility(getFixHelper != null ? View.VISIBLE : View.GONE);
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
        if (text_locationName == null || text_locationLat == null || text_locationLon == null || text_locationAlt == null) {
            return;
        }
        if (item == null || item.location == null)
        {
            text_locationLat.setText("");
            text_locationLon.setText("");
            text_locationName.setText("");
            text_locationAlt.setText("");

        } else {
            text_locationLat.setText(location.getLatitude());
            text_locationLon.setText(location.getLongitude());
            text_locationName.setText(location.getLabel());
            updateAltitudeField(getActivity(), location);
        }
        updateAltitudeLabel(getActivity());
    }

    private void updateAltitudeField(Context context, Location location)
    {
        if (context != null && text_locationAlt != null)
        {
            DecimalFormat formatter = com.forrestguice.suntimeswidget.calculator.core.Location.decimalDegreesFormatter();
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(getContext(), 0);
            switch (units)
            {
                case IMPERIAL:
                    text_locationAlt.setText(formatter.format(WidgetSettings.LengthUnit.metersToFeet(location.getAltitudeAsDouble())));
                    break;

                case METRIC:
                default: text_locationAlt.setText(formatter.format(location.getAltitudeAsDouble()));
                    break;
            }
        }
    }

    private void updateAltitudeLabel(Context context)
    {
        if (context != null && text_locationAltUnits != null)
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(getContext(), 0);
            switch (units)
            {
                case IMPERIAL:
                    text_locationAltUnits.setText(context.getString(R.string.units_feet_short));
                    break;

                case METRIC:
                default: text_locationAltUnits.setText(context.getString(R.string.units_meters));
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
            item.isDefault = item0.isDefault;

        } else {
            item.rowID = -1;
            item.location = new Location(text_locationName.getText().toString(), text_locationLat.getText().toString(), text_locationLon.getText().toString(), text_locationAlt.getText().toString(),
                    WidgetSettings.loadLengthUnitsPref(getActivity(), 0) == WidgetSettings.LengthUnit.METRIC);
        }
        return item;
    }

    private View.OnClickListener onCancelButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onCancel(getDialog());
        }
    };

    private View.OnClickListener onSaveButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            savePlace();
        }
    };

    protected void savePlace()
    {
        final PlaceItem returnValue = createPlaceItem(item);
        final boolean validInput = validateInput();
        if (validInput)
        {
            if (listener != null) {
                listener.onAccepted(returnValue);
            }
        }

        if (getFixHelper != null)
        {
            final GetFixTaskListener cancelGetFixListener = new GetFixTaskListener()
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
    }

    public boolean validateInput()
    {
        Context myParent = getActivity();
        boolean isValid = true;

        String name = text_locationName.getText().toString();
        if (name.trim().isEmpty())
        {
            isValid = false;
            text_locationName.setError(myParent.getString(R.string.location_dialog_error_name));
        }

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
                return formatter.format(WidgetSettings.LengthUnit.metersToFeet(location.getAltitude()));

            case METRIC:
            default:
                return formatter.format(location.getAltitude());
        }
    }

    private void disableTouchOutsideBehavior()
    {
        if (getShowsDialog())
        {
            Window window = getDialog().getWindow();
            if (window != null) {
                View decorView = window.getDecorView().findViewById(android.support.design.R.id.touch_outside);
                decorView.setOnClickListener(null);
            }
        }
    }

    /**
     * triggerActionMode
     */
    protected void triggerActionMode(PlaceItem item)
    {
        if (actionMode == null)
        {
            if (item != null)
            {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                actionMode = ((activity != null) ? ActionModeHelper.wrap(activity.startSupportActionMode(ActionModeHelper.wrap(actions))) : null);
                if (actionMode != null) {
                    updateActionMode(getActivity(), item);
                }
            }
        } else {
            updateActionMode(getActivity(), item);
        }
    }

    protected void updateActionMode(Context context, PlaceItem item)
    {
        if (actionMode != null) {
            actionMode.setTitle(item.location != null ? item.location.getLabel() : "");
            actionMode.setSubtitle("");
        } else {
            triggerActionMode(item);
        }
    }

    /**
     * PlacesEditActionCompat
     */
    private class PlacesEditActionCompat implements ActionModeHelper.ActionModeCallback
    {
        @Override
        public boolean onCreateActionMode(ActionModeHelper.ActionModeInterface mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.placesedit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionModeHelper.ActionModeInterface mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionModeHelper.ActionModeInterface mode, MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.savePlace:
                    savePlace();
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionModeHelper.ActionModeInterface mode) {
            actionMode = null;
        }
    }

}

