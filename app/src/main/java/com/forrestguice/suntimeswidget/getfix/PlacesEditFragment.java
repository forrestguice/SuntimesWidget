/**
    Copyright (C) 2020-2025 Forrest Guice
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
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
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
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.map.colors.WorldMapColorValuesCollection;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class PlacesEditFragment extends BottomSheetDialogFragment
{
    public static final String KEY_DIALOGTHEME = "dialogtheme";

    public static final String KEY_LOCATION = "location";
    public static final String KEY_LOCATION_LATITUDE = "locationLatitude";
    public static final String KEY_LOCATION_LONGITUDE = "locationLongitude";
    public static final String KEY_LOCATION_ALTITUDE = "locationAltitude";
    public static final String KEY_LOCATION_LABEL = "locationLabel";
    public static final String KEY_LOCATION_COMMENT = "locationComment";

    private EditText text_locationAlt;
    private TextView text_locationAltUnits;
    private EditText text_locationLat;
    private EditText text_locationLon;
    private EditText text_locationName;
    private EditText text_locationComment;

    private ImageButton button_cancel, button_save;

    private ImageButton button_getfix;
    private ProgressBar progress_getfix;
    private TextView progress_getfix_label;

    private TextView text_log;
    private NestedScrollView scroll_log;
    private View layout_log;
    private GnssStatusView gpsStatusView;

    private ImageButton button_map;

    protected ActionMode actionMode = null;
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
    private final GetFixUI getFixUI_editMode = new GetFixUI()
    {
        private LengthUnit lengthUnit = LengthUnit.METRIC;

        protected void setProgressColor(int color)
        {
            if (progress_getfix != null)
            {
                if (Build.VERSION.SDK_INT >= 21) {
                    progress_getfix.setIndeterminateTintList(SuntimesUtils.colorStateList(color, color));
                } else {
                    Drawable d = progress_getfix.getIndeterminateDrawable();
                    d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }
        }
        protected void resetProgressColor()
        {
            if (progress_getfix != null)
            {
                if (Build.VERSION.SDK_INT >= 21) {
                    progress_getfix.setIndeterminateTintList(null);
                } else {
                    Drawable d = progress_getfix.getIndeterminateDrawable();
                    d.setColorFilter(null);
                }
            }
        }

        protected int getDefaultColorIDForAccuracy(double accuracy) {
            if (accuracy <= 0) return 0;
            else if (accuracy <= 10) return R.color.green_a200;
            else if (accuracy <= 110) return R.color.blue_a100;
            else if (accuracy <= 1000) return R.color.yellow_700;
            else return R.color.red_a200;
        }

        protected Integer getColorForAccuracy(Context context, double accuracy)
        {
            /*if (accuracy <= 0) return null;
            else if (accuracy <= 10) return Color.GREEN;
            else if (accuracy <= 110) return Color.BLUE;
            else if (accuracy <= 1000) return Color.YELLOW;
            else return Color.RED;*/

            int i;
            if (accuracy <= 0) i = -1;
            else if (accuracy <= 10) i = 0;
            else if (accuracy <= 110) i = 1;
            else if (accuracy <= 1000) i = 2;
            else i = 3;

            if (i >= 0)
            {
                //int[] attr = new int[] { R.attr.springColor, R.attr.winterColor, R.attr.summerColor, R.attr.tagColor_error};    // TODO: attr
                //TypedArray a = context.obtainStyledAttributes(attr);
                //int color = ContextCompat.getColor(context, a.getResourceId(i, getDefaultColorIDForAccuracy(accuracy)));
                //a.recycle();
                //return color;
                return ContextCompat.getColor(context, getDefaultColorIDForAccuracy(accuracy));

            } else return null;
        }

        @Override
        public void enableUI(boolean value)
        {
            text_locationName.requestFocus();
            text_locationLat.setEnabled(value);
            text_locationLon.setEnabled(value);
            text_locationAlt.setEnabled(value);
            text_locationName.setEnabled(value);
            text_locationComment.setEnabled(value);
        }

        @Override
        public void updateUI(LocationProgress... progress)
        {
            super.updateUI(progress);
            Context context = getActivity();
            if (progress[0] != null && progress_getfix_label != null && context != null)
            {
                double accuracy = progress[0].getAccuracy();
                if (accuracy > 0) {
                    progress_getfix_label.setText(SuntimesUtils.formatAsHeight(context, progress[0].getAccuracy(), lengthUnit, 2, true).toString());
                } else progress_getfix_label.setText("");

                Integer color = getColorForAccuracy(context, accuracy);
                if (color != null) {
                    setProgressColor(color);
                } else resetProgressColor();

                updateLogView(progress[0].getLog());
            }
        }

        @Override
        public void updateUI(android.location.Location... locations)
        {
            Context context = getActivity();
            DecimalFormat formatter = com.forrestguice.suntimeswidget.calculator.core.Location.decimalDegreesFormatter();
            if (locations != null && locations[0] != null && context != null)
            {
                text_locationLat.setText( formatter.format(locations[0].getLatitude()) );
                text_locationLon.setText( formatter.format(locations[0].getLongitude()) );
                text_locationAlt.setText( altitudeDisplayString(locations[0], formatter, WidgetSettings.loadLengthUnitsPref(context, 0)) );
            }
        }

        @Override
        public void showProgress(boolean showProgress)
        {
            if (progress_getfix != null) {
                progress_getfix.setVisibility((showProgress ? View.VISIBLE : View.GONE));
                if (!showProgress) {
                    resetProgressColor();
                }
            }
            if (progress_getfix_label != null)
            {
                if (getActivity() != null) {
                    lengthUnit = WidgetSettings.loadLengthUnitsPref(getActivity(), 0);
                }
                progress_getfix_label.setVisibility((showProgress ? View.VISIBLE : View.GONE));
                if (!showProgress) {
                    progress_getfix_label.setText("");
                }
            }
        }

        @Override
        public void onStart() {
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
        updateComment(item.comment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
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

        if (gpsStatusView != null) {
            gpsStatusView.stopMonitoring();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        FragmentManager fragments = getChildFragmentManager();
        MapCoordinateDialog mapDialog = (MapCoordinateDialog) fragments.findFragmentByTag(DIALOGTAG_MAP);
        if (mapDialog != null) {
            mapDialog.setColorCollection(new WorldMapColorValuesCollection<>(getActivity()));
            mapDialog.setOnAcceptedListener(onMapCoordinateDialogAccepted(mapDialog));
        }

        if (gpsStatusView != null && (gpsStatusView.getVisibility() == View.VISIBLE)) {
            gpsStatusView.startMonitoring();
        }
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
            updateComment(null);
        }

        //triggerActionMode(item);
        return view;
    }

    protected void initViews(Context context, final View content)
    {
        WidgetSettings.initDisplayStrings(context);

        text_locationName = (EditText) content.findViewById(R.id.appwidget_location_name);
        text_locationLat = (EditText) content.findViewById(R.id.appwidget_location_lat);
        text_locationLon = (EditText) content.findViewById(R.id.appwidget_location_lon);
        text_locationAlt = (EditText) content.findViewById(R.id.appwidget_location_alt);
        text_locationAltUnits = (TextView)content.findViewById(R.id.appwidget_location_alt_units);
        text_locationComment = (EditText) content.findViewById(R.id.appwidget_location_comment);

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

        button_map = (ImageButton) content.findViewById(R.id.appwidget_location_mapview);
        if (button_map != null)
        {
            TooltipCompat.setTooltipText(button_map, button_map.getContentDescription());
            button_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMapCoordinateDialog(getActivity());
                }
            });
        }

        progress_getfix_label = (TextView) content.findViewById(R.id.appwidget_location_getfixprogress_label);
        if (progress_getfix_label != null) {
            progress_getfix_label.setVisibility(View.GONE);
        }

        progress_getfix = (ProgressBar) content.findViewById(R.id.appwidget_location_getfixprogress);
        if (progress_getfix != null)
        {
            progress_getfix.setVisibility(View.GONE);
            progress_getfix.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if (getFixHelper != null) {
                        getFixHelper.cancelGetFix();
                    }
                }
            });
        }

        layout_log = content.findViewById(R.id.layout_debug_log);
        if (layout_log != null) {
            layout_log.setVisibility(View.GONE);
        }

        scroll_log = (NestedScrollView) content.findViewById(R.id.scroll_debug_log);

        text_log = (TextView) content.findViewById(R.id.text_debug_log);
        if (text_log != null) {
            text_log.setTextIsSelectable(true);
        }

        gpsStatusView = (GnssStatusView) content.findViewById(R.id.gps_status);
        hideGpsStatusView();

        button_getfix = (ImageButton) content.findViewById(R.id.appwidget_location_getfix);
        TooltipCompat.setTooltipText(button_getfix, button_getfix.getContentDescription());
        button_getfix.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                showGetFixMenu(v.getContext(), v);
            }
        });
        button_getfix.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v) {
                //showGetFixMenu(v.getContext(), v);
                getFix();
                return true;
            }
        });

        getFixHelper = createLocationHelper();
        if (getFixHelper != null) {
            getFixHelper.setFragment(this);
        }
        updateGPSButtonIcons();
    }

    /**
     * GetFix Menu
     */
    protected void showGetFixMenu(Context context, View v) {
        PopupMenu popup = PopupMenuCompat.createMenu(context, v, R.menu.placesgps, onGetFixMenuItemClicked, null);
        Menu menu = popup.getMenu();

        MenuItem logItem = menu.findItem(R.id.action_location_togglelog);
        if (logItem != null) {
            logItem.setVisible(LocationHelperSettings.keepLastLocationLog(context));
            logItem.setChecked(loadLogViewState());
        }

        PopupMenuCompat.forceActionBarIcons(menu);
        popup.show();
    }
    private final PopupMenu.OnMenuItemClickListener onGetFixMenuItemClicked = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.action_location_agps_reload:
                    reloadAGPS();
                    break;

                case R.id.action_location_agps_delete:
                    deleteAGPS();
                    break;

                case R.id.action_location_togglelog:
                    toggleLogView();
                    break;

                case R.id.action_location_quickfix:
                    getFix();
                    break;

                case R.id.action_location_average:
                    averageFix();
                    break;
            }
            return false;
        }
    };

    protected void getFix() {
        getFix(true);
    }
    protected void getFix(boolean autoStop)
    {
        Context context = getActivity();
        if (layout_log != null && context != null) {
            boolean showLogView = LocationHelperSettings.keepLastLocationLog(context) && loadLogViewState()
                    && LocationHelperSettings.isProviderRequested(getActivity(), LocationManager.GPS_PROVIDER);
            layout_log.setVisibility(showLogView ? View.VISIBLE : View.GONE);
        }
        showGpsStatusView();
        if (getFixHelper != null) {
            getFixHelper.getFix(0, autoStop);
        }
    }
    protected void averageFix() {
        getFix(false);
    }

    protected void reloadAGPS() {
        if (getActivity() != null)
        {
            getFixHelper.reloadAGPS(getActivity(), false, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    resetGpsStatusView();
                }
            });
        }
    }
    protected void deleteAGPS() {
        if (getActivity() != null) {
            getFixHelper.reloadAGPS(getActivity(), true, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    resetGpsStatusView();
                }
            });
        }
    }

    protected void showGpsStatusView()
    {
        if (gpsStatusView != null)
        {
            gpsStatusView.setVisibility(LocationHelperSettings.isProviderRequested(getActivity(), LocationManager.GPS_PROVIDER) ? View.VISIBLE : View.GONE);
            if (!gpsStatusView.isMonitoring()) {
                gpsStatusView.startMonitoring();
            }
        }
    }
    protected void hideGpsStatusView() {
        if (gpsStatusView != null) {
            gpsStatusView.setVisibility(View.GONE);
        }
    }
    protected void resetGpsStatusView()
    {
        if (gpsStatusView != null && gpsStatusView.getVisibility() == View.VISIBLE)
        {
            if (Build.VERSION.SDK_INT >= 24) {
                gpsStatusView.updateViews((GnssStatus) null);
            } else gpsStatusView.updateViews((GpsStatus) null);
        }
    }

    protected void toggleLogView()
    {
        if (layout_log != null)
        {
            layout_log.setVisibility(layout_log.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            saveLogViewState(layout_log.getVisibility() == View.VISIBLE);
            if (layout_log.getVisibility() == View.VISIBLE) {
                scrollLogViewToBottom();
            }
        }
    }
    protected void saveLogViewState(boolean value)
    {
        Activity activity = getActivity();
        if (activity != null) {
            SharedPreferences.Editor prefs = activity.getPreferences(0).edit();
            if (prefs != null) {
                prefs.putBoolean("showLog", value);
                prefs.apply();
            }
        }
    }
    protected boolean loadLogViewState()
    {
        SharedPreferences prefs = (getActivity() != null) ? getActivity().getPreferences(0) : null;
        if (prefs != null) {
            return prefs.getBoolean("showLog", false);
        } else return false;
    }

    protected void updateLogView(String log) {
        if (text_log != null) {
            text_log.setText(log);
        }
        scrollLogViewToBottom();
    }
    protected void scrollLogViewToBottom()
    {
        if (scroll_log != null) {
            scroll_log.post(new Runnable() {
                public void run() {
                    if (scroll_log != null) {
                        scroll_log.fullScroll(View.FOCUS_DOWN);
                    }
                }
            });
        }
    }

    public static final String DIALOGTAG_MAP = "mapDialog";
    protected void showMapCoordinateDialog(Context context)
    {
        MapCoordinateDialog dialog = new MapCoordinateDialog();
        dialog.setColorCollection(new WorldMapColorValuesCollection<>(context));
        dialog.setInitialCoordinates(text_locationLon.getText().toString(), text_locationLat.getText().toString());
        dialog.setOnAcceptedListener(onMapCoordinateDialogAccepted(dialog));
        dialog.show(getChildFragmentManager(), DIALOGTAG_MAP);
    }

    private DialogInterface.OnClickListener onMapCoordinateDialogAccepted(final MapCoordinateDialog dialog)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface d, int which)
            {
                double latitude = dialog.getSelectedLatitude();
                text_locationLat.setText(String.format(Locale.getDefault(), "%.3f", latitude));

                double longitude = dialog.getSelectedLongitude();
                text_locationLon.setText(String.format(Locale.getDefault(), "%.3f", longitude));


            }
        };
    }

    /**
     * @param savedInstanceState a Bundle containing previously saved dialog state
     * @return an AlertDialog ready for display
     */
    @SuppressWarnings({"RestrictedApi"})
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
        bundle.putString(KEY_LOCATION_COMMENT, text_locationComment.getText().toString());
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
        String comment = bundle.getString(KEY_LOCATION_COMMENT);

        if (longitude != null && latitude != null)
        {
            com.forrestguice.suntimeswidget.calculator.core.Location location;
            if (altitude != null)
                location = new com.forrestguice.suntimeswidget.calculator.core.Location(label, latitude, longitude, altitude);
            else location = new com.forrestguice.suntimeswidget.calculator.core.Location(label, latitude, longitude);
            updateViews(location);
            updateComment(comment);
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

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(ViewUtils.getBottomSheetResourceID());
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
    private void updateComment(String comment)
    {
        if (text_locationComment != null) {
            text_locationComment.setText((item == null || comment == null) ? "" : comment);
        }
    }

    private void updateAltitudeField(Context context, Location location)
    {
        if (context != null && text_locationAlt != null)
        {
            DecimalFormat formatter = com.forrestguice.suntimeswidget.calculator.core.Location.decimalDegreesFormatter();
            LengthUnit units = WidgetSettings.loadLengthUnitsPref(getContext(), 0);
            switch (units)
            {
                case IMPERIAL:
                    text_locationAlt.setText(formatter.format(LengthUnit.metersToFeet(location.getAltitudeAsDouble())));
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
            LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
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
                    WidgetSettings.loadLengthUnitsPref(getActivity(), 0) == LengthUnit.METRIC);
            item.comment = (text_locationComment != null ? text_locationComment.getText().toString() : item0.comment);

        } else {
            item.rowID = -1;
            item.location = new Location(text_locationName.getText().toString(), text_locationLat.getText().toString(), text_locationLon.getText().toString(), text_locationAlt.getText().toString(),
                    WidgetSettings.loadLengthUnitsPref(getActivity(), 0) == LengthUnit.METRIC);
            item.comment = (text_locationComment != null ? text_locationComment.getText().toString() : "");
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

    public static CharSequence altitudeDisplayString(android.location.Location location, DecimalFormat formatter, LengthUnit units)
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

    private void disableTouchOutsideBehavior()
    {
        if (getShowsDialog())
        {
            Window window = getDialog().getWindow();
            if (window != null) {
                View decorView = window.getDecorView().findViewById(ViewUtils.getTouchOutsideResourceID());
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
                actionMode = activity.startSupportActionMode(actions);
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
    private class PlacesEditActionCompat implements android.support.v7.view.ActionMode.Callback
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.placesedit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem)
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
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }

}

