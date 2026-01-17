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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;

import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.app.ActivityResultLauncherCompat;
import com.forrestguice.support.app.DialogBase;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.support.widget.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.app.FragmentCompat;

public class LocationConfigDialog extends BottomSheetDialogBase
{
    public static final String KEY_LOCATION_HIDETITLE = "hidetitle";
    public static final String KEY_LOCATION_HIDEMODE = "hidemode";
    public static final String KEY_LOCATION_COLLAPSE = "collapse";
    public static final String KEY_LOCATION_SHOWADDBUTTON = "showaddbutton";

    public static final int REQUEST_LOCATION = 30;
    private final ActivityResultLauncherCompat startActivityForResult_location = registerForActivityResultCompat(REQUEST_LOCATION);

    protected ImageButton btn_accept, btn_cancel;

    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs, Bundle savedInstanceState)
    {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LocationConfigDialog);
        setHideDialogHeader(a.getBoolean(R.styleable.LocationConfigDialog_hideHeader, hideHeader));
        setHideDialogFooter(a.getBoolean(R.styleable.LocationConfigDialog_hideFooter, hideFooter));
        setHideMode(a.getBoolean(R.styleable.LocationConfigDialog_hideMode, hideMode));
        setHideTitle(a.getBoolean(R.styleable.LocationConfigDialog_hideTitle, hideTitle));
        setShouldCollapse(a.getBoolean(R.styleable.LocationConfigDialog_collapse, collapse));
        setShowAddButton(a.getBoolean(R.styleable.LocationConfigDialog_showAddButton, showAddButton));
        a.recycle();
    }

    /**
     * The dialog content; in this case just a wrapper around a LocationConfigView.
     */
    private LocationConfigView dialogContent;
    public LocationConfigView getDialogContent() { return dialogContent; }

    /**
     * On location accepted listener.
     */
    protected DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    /**
     * On location cancelled listener.
     */
    protected DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

    /***
     * LocationConfigDialogListener
     */
    protected LocationConfigDialogListener defaultDialogListener = new LocationConfigDialogListener()
    {
        @Override
        public boolean saveSettings(Context context, LocationMode locationMode, Location location)
        {
            return dialogContent.saveSettings(context);
        }
    };
    protected LocationConfigDialogListener dialogListener = defaultDialogListener;

    public void setDialogListener( LocationConfigDialogListener listener )
    {
        if (listener == null)
            this.dialogListener = defaultDialogListener;
        else this.dialogListener = listener;
    }

    public static abstract class LocationConfigDialogListener
    {
        public void onEditModeChanged(LocationConfigView.LocationViewMode mode) {
            /* EMPTY */
        }

        public boolean saveSettings(Context context, LocationMode locationMode, Location location)
        {
            return true;
        }
    }

    /**
     * setLocation
     */
    private Location presetLocation = null;
    public void setLocation(Context context, Location location)
    {
        presetLocation = location;
        if (dialogContent != null) {
            dialogContent.loadSettings(context, LocationConfigView.bundleData(Uri.parse(presetLocation.getUri()), presetLocation.getLabel(), LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT));
        }
    }

    public void setMode( LocationConfigView.LocationViewMode mode ) {
        if (dialogContent != null) {
            dialogContent.setMode(mode);
        }
    }

    public void addCurrentLocation(Context context)
    {
        if (dialogContent != null) {
            dialogContent.loadSettings(context, LocationConfigView.bundleData(null, "", LocationConfigView.LocationViewMode.MODE_CUSTOM_ADD));
            dialogContent.lookupLocation();
        }
    }

    /**
     * Show / hide the title widget.
     */
    private boolean hideTitle;
    public void setHideTitle(boolean value)
    {
        hideTitle = value;
        if (dialogContent != null)
        {
            dialogContent.setHideTitle(hideTitle);
        }
    }
    public boolean getHideTitle() { return hideTitle; }

    /**
     * Show / hide add button.
     */
    private boolean showAddButton;
    public void setShowAddButton(boolean value) {
        showAddButton = value;
        if (dialogContent != null) {
            dialogContent.setShowAddButton(showAddButton);
        }
    }
    public boolean showAddButton() { return showAddButton; }

    /**
     * Show / hide the dialog header.
     */
    private boolean hideHeader = false;
    public void setHideDialogHeader(boolean value) {
        hideHeader = value;
    }

    /**
     * Show / hide the dialog buttons.
     */
    private boolean hideFooter = false;
    public void setHideDialogFooter(boolean value) {
        hideFooter = value;
    }

    /***
     * Show / hide the location mode; when hidden the mode is locked to user-defined.
     */
    private boolean hideMode = false;
    public void setHideMode(boolean value)
    {
        hideMode = value;
        if (dialogContent != null)
        {
            dialogContent.setHideMode(hideMode);
        }
    }
    public boolean getHideMode()
    {
        return hideMode;
    }

    /**
     * Collapse the coordinate view when not editing location
     */
    private boolean collapse = false;
    public void setShouldCollapse(boolean value) {
        collapse = value;
    }
    public boolean shouldCollapse() {
        return collapse;
    }

    /**
     * Preset data (in the form of a geo URI)
     */
    private Uri presetData = null;
    public void setData(Uri data)
    {
        presetData = data;
        if (dialogContent != null && getContext() != null) {
            dialogContent.loadSettings(getContext(), presetData);
        }
    }

    /**
     * @param requestCode the request code that was passed to requestPermissions
     * @param permissions the requested permissions
     * @param grantResults either PERMISSION_GRANTED or PERMISSION_DENIED for each of the requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (dialogContent != null) {
            dialogContent.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (dialogContent != null)
        {
            dialogContent.cancelGetFix();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (dialogContent != null)
        {
            dialogContent.onResume();
        }
        expandSheet(getDialog());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState)
    {
        Context context = requireContext();
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));    // hack: contextWrapper required because base theme is not properly applied
        View view = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_location, parent, false);

        dialogContent = (LocationConfigView) view.findViewById(R.id.locationConfig);
        dialogContent.setHideTitle(hideTitle);
        dialogContent.setHideMode(hideMode);
        dialogContent.setShouldCollapse(collapse);
        dialogContent.setShowAddButton(showAddButton);
        dialogContent.init((AppCompatActivity) requireActivity(), false);
        dialogContent.setFragment(FragmentCompat.from(this));

        dialogContent.setOnListButtonClicked(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getContext() != null)
                {
                    Intent intent = new Intent(getContext(), PlacesActivity.class);
                    intent.putExtra(PlacesActivity.EXTRA_ALLOW_PICK, true);
                    //intent.putExtra(PlacesActivity.EXTRA_SELECTED, selectedRowID);
                    startActivityForResult_location.launch(intent);
                } else Log.w("LocationConfigDialog", "onListButtonClicked: activity is null!");
            }
        });
        dialogContent.setViewListener(new LocationConfigView.LocationConfigViewListener() {
            public void onModeChanged(LocationConfigView.LocationViewMode mode) {
                if (dialogListener != null) {
                    dialogListener.onEditModeChanged(mode);
                }
            }
        });

        View header = view.findViewById(R.id.dialog_header);
        if (header != null) {
            header.setVisibility(hideHeader ? View.GONE : View.VISIBLE);
        }

        View footer = view.findViewById(R.id.dialog_footer);
        if (footer != null) {
            footer.setVisibility(hideFooter ? View.GONE : View.VISIBLE);
        }

        btn_cancel = (ImageButton) view.findViewById(R.id.dialog_button_cancel);
        TooltipCompat.setTooltipText(btn_cancel, btn_cancel.getContentDescription());
        btn_cancel.setOnClickListener(hideFooter ? null : onDialogCancelClick);
        if (AppSettings.isTelevision(context)) {
            btn_cancel.setFocusableInTouchMode(true);
        }

        btn_accept = (ImageButton) view.findViewById(R.id.dialog_button_accept);
        TooltipCompat.setTooltipText(btn_accept, btn_accept.getContentDescription());
        btn_accept.setOnClickListener(hideFooter ? null : onDialogAcceptClick);

        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);

        } else if (presetData != null) {
            dialogContent.loadSettings(context, presetData);

        } else if (presetLocation != null) {
            setLocation(context, presetLocation);
        }
        return view;
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

    /**
     * @param outState a Bundle used to save state
     */
    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        //Log.d("DEBUG", "LocationConfigDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * @param bundle a Bundle used to save state
     */
    protected void saveSettings(Bundle bundle)
    {
        //Log.d("DEBUG", "LocationConfigDialog saveSettings (bundle)");
        bundle.putBoolean(KEY_LOCATION_HIDETITLE, hideTitle);
        bundle.putBoolean(KEY_LOCATION_HIDEMODE, hideMode);
        bundle.putBoolean(KEY_LOCATION_COLLAPSE, collapse);
        bundle.putBoolean(KEY_LOCATION_SHOWADDBUTTON, showAddButton);
        if (dialogContent != null)
        {
            dialogContent.saveSettings(bundle);
        }
    }

    /**
     * @param bundle a Bundle used to load state
     */
    protected void loadSettings(Bundle bundle)
    {
        //Log.d("DEBUG", "LocationConfigDialog loadSettings (bundle)");
        hideTitle = bundle.getBoolean(KEY_LOCATION_HIDETITLE);
        setHideTitle(hideTitle);

        hideMode = bundle.getBoolean(KEY_LOCATION_HIDEMODE);
        setHideMode(hideMode);

        collapse = bundle.getBoolean(KEY_LOCATION_COLLAPSE);
        setShouldCollapse(collapse);

        showAddButton = bundle.getBoolean(KEY_LOCATION_SHOWADDBUTTON);
        setShowAddButton(showAddButton);

        if (dialogContent != null && getContext() != null) {
            dialogContent.loadSettings(getContext(), bundle);
        }
    }

    private final DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialog)
        {
            BottomSheetDialogBase.initPeekHeight(dialog, R.id.dialog_footer);

            if (AppSettings.isTelevision(getContext())) {
                btn_accept.requestFocus();
            }
        }
    };

    private final View.OnClickListener onDialogCancelClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getDialog() != null) {
                getDialog().cancel();
            }
        }
    });

    @Override
    public void onCancel(@NonNull DialogInterface dialog)
    {
        dialogContent.cancelGetFix();
        dismiss();
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
        }
    }

    private final View.OnClickListener onDialogAcceptClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            dialogContent.cancelGetFix();
            if (context != null && dialogListener != null &&
                    dialogListener.saveSettings(context, dialogContent.getLocationMode(), dialogContent.getLocation(context)))
            {
                LocationConfigView.LocationViewMode mode = dialogContent.getMode();
                switch (mode)
                {
                    case MODE_CUSTOM_ADD:
                    case MODE_CUSTOM_EDIT:
                        dialogContent.setMode(LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT);
                        dialogContent.populateLocationList();  // triggers 'add place'
                        break;
                }

                dismiss();
                if (onAccepted != null) {
                    onAccepted.onClick(getDialog(), 0);
                }
            }
        }
    });

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        DialogBase.disableTouchOutsideBehavior(getDialog());
    }

    @Override
    public void onActivityResultCompat(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResultCompat(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_LOCATION:
                onLocationResult(resultCode, data);
                break;
        }
    }
    protected void onLocationResult(int resultCode, Intent data)
    {
        boolean adapterModified = data != null && data.getBooleanExtra(PlacesActivity.EXTRA_ADAPTER_MODIFIED, false);
        if (adapterModified) {
            getDialogContent().populateLocationList();
        }

        if (resultCode == Activity.RESULT_OK && data != null)
        {
            Location location = (Location) data.getSerializableExtra(PlacesActivity.EXTRA_LOCATION);
            if (location != null && getContext() != null) {
                setLocation(getContext(), location);
            } else {
                Log.w("LocationDialog", "onLocationResult: the expected result is missing!");
            }
            if (AppSettings.isTelevision(getContext())) {
                btn_accept.requestFocus();
            }

        } else {
            getDialogContent().populateLocationList();
            if (AppSettings.isTelevision(getContext())) {
                btn_cancel.requestFocus();
            }
        }
    }


}

