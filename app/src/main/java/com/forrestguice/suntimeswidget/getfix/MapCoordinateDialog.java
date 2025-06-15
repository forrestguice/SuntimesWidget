/**
    Copyright (C) 2025 Forrest Guice
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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.map.WorldMapTask;
import com.forrestguice.suntimeswidget.map.WorldMapView;
import com.forrestguice.suntimeswidget.map.WorldMapWidgetSettings;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValues;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.util.Locale;

@SuppressWarnings("Convert2Diamond")
public class MapCoordinateDialog extends BottomSheetDialogFragment
{
    public static final String KEY_DIALOG_TITLE = "dialog_title";
    public static final String KEY_DIALOG_LATITUDE_SELECTED = "selected_latitude";
    public static final String KEY_DIALOG_LONGITUDE_SELECTED = "selected_longitude";
    public static final String KEY_DIALOG_LATITUDE_INITIAL = "dialog_latitude";
    public static final String KEY_DIALOG_LONGITUDE_INITIAL = "dialog_longitude";

    public MapCoordinateDialog() {
        setArguments(new Bundle());
    }

    protected int getLayoutResID() {
        return R.layout.layout_dialog_worldmap_picker;
    }

    protected WorldMapView map;
    protected WorldMapTask.WorldMapOptions options;
    protected WorldMapTask.WorldMapProjection projection;

    protected ImageButton marker;
    protected float[] markerCenter = null;

    protected ImageButton acceptButton;
    protected TextView subtitleText;

    protected void initViews(Context context, View dialogContent)
    {
        String title = getDialogTitle();
        TextView titleText = (TextView) dialogContent.findViewById(R.id.dialog_title);
        if (titleText != null && title != null) {
            titleText.setText(title);
        }
        subtitleText = (TextView) dialogContent.findViewById(R.id.dialog_subtitle);

        map = (WorldMapView) dialogContent.findViewById(R.id.info_time_worldmap);
        map.setMapMode(context, WorldMapWidgetSettings.WorldMapWidgetMode.EQUIRECTANGULAR_SIMPLE);
        projection = WorldMapView.getMapProjection(map.getMapMode());

        options = map.getOptions();
        options.showSunPosition = false;
        options.showSunShadow = false;
        options.showMoonPosition = false;
        options.showMoonLight = false;
        options.showMajorLatitudes = true;
        options.showGrid = true;

        if (colors != null)
        {
            boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
            WorldMapColorValues values = (WorldMapColorValues) colors.getSelectedColors(context, (isNightMode ? 1 : 0), WorldMapColorValues.TAG_WORLDMAP);
            if (values != null) {
                options.colors = values;
            } else if (options.colors == null) {
                options.init(context);
            }
        }
        map.themeViews(context);
        map.setOnTouchListener(onMapTouched);

        marker = (ImageButton) dialogContent.findViewById(R.id.info_time_worldmap_marker);
        if (marker != null)
        {
            int colorEnabled = Color.BLACK;    // TODO
            int colorPressed = Color.RED;
            marker.setImageTintList(SuntimesUtils.colorStateList(colorEnabled, colorEnabled, colorPressed));
            marker.setAlpha(0f);    // setVisible from onShow
        }

        acceptButton = (ImageButton) dialogContent.findViewById(R.id.dialog_button_accept);
        if (acceptButton != null) {
            TooltipCompat.setTooltipText(acceptButton, acceptButton.getContentDescription());
            acceptButton.setOnClickListener(onDialogAcceptClick);
        }

        ImageButton cancelButton = (ImageButton) dialogContent.findViewById(R.id.dialog_button_cancel);
        if (cancelButton != null)
        {
            TooltipCompat.setTooltipText(cancelButton, cancelButton.getContentDescription());
            cancelButton.setOnClickListener(onDialogCancelClick);
            if (AppSettings.isTelevision(getActivity())) {
                cancelButton.setFocusableInTouchMode(true);
            }
        }

        Button neutralButton = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
        if (neutralButton != null) {
            neutralButton.setOnClickListener(onDialogNeutralClick);
        }
    }

    protected int getMarkerAlpha() {
        return  3 * 255/4;
    }

    private final View.OnTouchListener onMapTouched = new View.OnTouchListener()
    {
        private double[] mid;
        private boolean isDown = false;

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_UP:
                    isDown = false;
                    setSelectedCoordinates(map.getLatitudeLongitudeAt(event.getX(), event.getY(), mid, map.getWidth(), map.getHeight()));
                    if (marker != null) {
                        setMarkerPosition(event.getX(), event.getY());
                        marker.setPressed(false);
                    }
                    updateDialogSubtitle();
                    return true;

                case MotionEvent.ACTION_DOWN:
                    isDown = true;
                    mid = new double[] { map.getWidth() / 2d, map.getHeight() / 2d };
                    if (marker != null) {
                        setMarkerPosition(event.getX(), event.getY());
                        marker.setPressed(true);
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (isDown) {
                        setSelectedCoordinates(map.getLatitudeLongitudeAt(event.getX(), event.getY(), mid, map.getWidth(), map.getHeight()));
                        setMarkerPosition(event.getX(), event.getY());
                        updateDialogSubtitle();
                    }
                    return true;
                default: return false;
            }
        }
    };

    protected void updateViews()
    {
        updateDialogSubtitle();
        updateMap();
        updateMarker();
    }

    protected void updateMap() {
        if (map != null) {
            options.locations = new double[][] {{ getInitialLatitude(), getInitialLongitude() }};
            map.updateViews(null, true);
        }
    }

    protected void updateMarker()
    {
        if (marker != null && projection != null) {
            int[] coordinates = projection.toBitmapCoords(map.getWidth(), map.getHeight(), new double[] {map.getWidth()/2d, map.getHeight()/2d}, getSelectedLatitude(), getSelectedLongitude());
            setMarkerPosition(coordinates[0], coordinates[1]);
        }
    }
    protected void setMarkerPosition(float x, float y)
    {
        if (marker != null)
        {
            //if (markerCenter == null && marker.getWidth() > 0 && marker.getHeight() > 0) {
                markerCenter = new float[] { marker.getWidth() / 2f, marker.getHeight() / 2f};
            //}
            if (markerCenter != null) {
                marker.setTranslationX(x - markerCenter[0]);
                marker.setTranslationY(y - markerCenter[1]);
            }
        }
    }

    protected void updateDialogSubtitle()
    {
        if (subtitleText != null) {
            String latitudeDisplay = formatCoordinate(getSelectedLatitude());
            String longitudeDisplay = formatCoordinate(getSelectedLongitude());
            subtitleText.setText(getString(R.string.location_format_latlon, latitudeDisplay, longitudeDisplay));
        }
    }

    protected static String formatCoordinate(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(getLayoutResID(), parent, false);
        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }
        updateViews();
        return dialogContent;
    }

    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState ) {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void loadSettings(Activity activity) {
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        //getArgs().putDouble(KEY_DIALOG_LATITUDE_INITIAL, prefs.getFloat(KEY_DIALOG_LATITUDE_SELECTED, (float) getInitialLatitude()));
        //getArgs().putDouble(KEY_DIALOG_LONGITUDE_INITIAL, prefs.getFloat(KEY_DIALOG_LONGITUDE_SELECTED, (float) getInitialLongitude()));
    }
    protected void saveSettings(Activity activity) {
        //SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(activity).edit();  // TODO: use private prefs intead?
        //prefs.putFloat(KEY_DIALOG_LATITUDE_INITIAL, (float) getSelectedLatitude());
        //prefs.putFloat(KEY_DIALOG_LONGITUDE_INITIAL, (float) getSelectedLongitude());
        //prefs.apply();
    }

    protected void loadSettings(Bundle bundle) {}
    protected void saveSettings(Bundle bundle) {}

    @Nullable
    public String getDialogTitle() {
        return getArgs().getString(KEY_DIALOG_TITLE);
    }
    public void setDialogTitle(@Nullable String title) {
        getArgs().putString(KEY_DIALOG_TITLE, title);
    }

    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener ) {
        onAccepted = listener;
    }

    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener ) {
        onCanceled = listener;
    }

    private DialogInterface.OnClickListener onNeutral = null;
    public void setOnNeutralListener( DialogInterface.OnClickListener listener ) {
        onNeutral = listener;
    }

    private DialogInterface.OnShowListener onShowListener;
    public void setOnShowListener( DialogInterface.OnShowListener listener ) {
        onShowListener = listener;
    }

    private ColorValuesCollection<ColorValues> colors;
    public void setColorCollection(ColorValuesCollection<ColorValues> collection) {
        colors = collection;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    protected DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            ViewUtils.initPeekHeight(dialog, R.id.dialog_footer);
            if (onShowListener != null) {
                onShowListener.onShow(dialog);
            }

            if (AppSettings.isTelevision(getActivity())) {
                acceptButton.requestFocus();
            }

            if (marker != null)
            {
                final int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
                marker.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateMarker();
                        marker.animate().alpha(getMarkerAlpha()).setDuration(duration);
                    }
                }, duration);
            }
        }
    };

    protected View.OnClickListener onDialogNeutralClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            if (onNeutral != null) {
                onNeutral.onClick(getDialog(), 0);
            }
        }
    };
    protected View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
        }
    };
    @Override
    public void onCancel(DialogInterface dialog) {
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
        }
    }
    protected View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            dismiss();
            if (onAccepted != null) {
                onAccepted.onClick(getDialog(), 0);
            }
        }
    };

    protected void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void setInitialCoordinates(String longitude, String latitude) {
        try {
            setInitialCoordinates(Double.parseDouble(longitude), Double.parseDouble(latitude));
        } catch (NumberFormatException e) {
            Log.w("setInitialCoordinates", "invalid coordinate " + e);
        }
    }

    public void setInitialCoordinates(double longitude, double latitude) {
        getArgs().putDouble(KEY_DIALOG_LATITUDE_INITIAL, latitude);
        getArgs().putDouble(KEY_DIALOG_LONGITUDE_INITIAL, longitude);
    }
    public double getInitialLatitude() {
        return getArgs().getDouble(KEY_DIALOG_LATITUDE_INITIAL, WidgetSettings.loadLocationPref(getActivity(), 0).getLatitudeAsDouble());
    }
    public double getInitialLongitude() {
        return getArgs().getDouble(KEY_DIALOG_LONGITUDE_INITIAL, WidgetSettings.loadLocationPref(getActivity(), 0).getLongitudeAsDouble());
    }
    public double getSelectedLatitude() {
        return getArgs().getDouble(KEY_DIALOG_LATITUDE_SELECTED, getInitialLatitude());
    }
    public double getSelectedLongitude() {
        return getArgs().getDouble(KEY_DIALOG_LONGITUDE_SELECTED, getInitialLongitude());
    }

    /**
     * @param coordinates [longitude,latitude]
     */
    public void setSelectedCoordinates(@Nullable double[] coordinates) {
        if (coordinates != null) {
            setSelectedCoordinates(coordinates[0], coordinates[1]);
        }
    }
    public void setSelectedCoordinates(double longitude, double latitude)
    {
        getArgs().putDouble(KEY_DIALOG_LATITUDE_SELECTED, latitude);
        getArgs().putDouble(KEY_DIALOG_LONGITUDE_SELECTED, longitude);
    }

    @NonNull
    protected Bundle getArgs()
    {
        Bundle bundle = getArguments();
        if (bundle == null) {
            setArguments(bundle = new Bundle());
        }
        return bundle;
    }
}