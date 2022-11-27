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
package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;

import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.getfix.PlacesActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class LocationConfigDialog extends BottomSheetDialogFragment
{
    public static final String KEY_LOCATION_HIDETITLE = "hidetitle";
    public static final String KEY_LOCATION_HIDEMODE = "hidemode";
    public static final String KEY_LOCATION_COLLAPSE = "collapse";
    public static final String KEY_LOCATION_SHOWADDBUTTON = "showaddbutton";

    public static final int REQUEST_LOCATION = 30;

    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState)
    {
        super.onInflate(activity, attrs, savedInstanceState);

        TypedArray a = activity.obtainStyledAttributes(attrs,R.styleable.LocationConfigDialog);
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
    private com.forrestguice.suntimeswidget.LocationConfigView dialogContent;
    public com.forrestguice.suntimeswidget.LocationConfigView getDialogContent() { return dialogContent; }

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
        public boolean saveSettings(Context context, WidgetSettings.LocationMode locationMode, Location location)
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

        public boolean saveSettings(Context context, WidgetSettings.LocationMode locationMode, Location location)
        {
            return true;
        }
    }

    /**
     * setLocation
     * @param location
     */
    private Location presetLocation = null;
    public void setLocation(Context context, Location location)
    {
        presetLocation = location;
        if (dialogContent != null) {
            dialogContent.loadSettings(context, LocationConfigView.bundleData(presetLocation.getUri(), presetLocation.getLabel(), LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT));
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
        if (dialogContent != null)
        {
            dialogContent.loadSettings(getActivity(), presetData);
        }
    }

    /**
     * @param requestCode the request code that was passed to requestPermissions
     * @param permissions the requested permissions
     * @param grantResults either PERMISSION_GRANTED or PERMISSION_DENIED for each of the requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (dialogContent != null)
        {
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
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View view = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_location, parent, false);
        final FragmentActivity myParent = getActivity();

        dialogContent = (LocationConfigView) view.findViewById(R.id.locationConfig);
        dialogContent.setHideTitle(hideTitle);
        dialogContent.setHideMode(hideMode);
        dialogContent.setShouldCollapse(collapse);
        dialogContent.setShowAddButton(showAddButton);
        dialogContent.init(myParent, false);
        dialogContent.setFragment(this);

        dialogContent.setOnListButtonClicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlacesActivity.class);
                intent.putExtra(PlacesActivity.EXTRA_ALLOW_PICK, true);
                //intent.putExtra(PlacesActivity.EXTRA_SELECTED, selectedRowID);
                startActivityForResult(intent, REQUEST_LOCATION);
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

        Button btn_cancel = (Button) view.findViewById(R.id.dialog_button_cancel);
        btn_cancel.setOnClickListener(hideFooter ? null : onDialogCancelClick);

        Button btn_accept = (Button) view.findViewById(R.id.dialog_button_accept);
        btn_accept.setOnClickListener(hideFooter ? null : onDialogAcceptClick);

        if (savedInstanceState != null) {
            loadSettings(savedInstanceState);

        } else if (presetData != null) {
            dialogContent.loadSettings(myParent, presetData);

        } else if (presetLocation != null) {
            setLocation(getContext(), presetLocation);
        }
        return view;
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

    /**
     * @param outState a Bundle used to save state
     */
    @Override
    public void onSaveInstanceState( Bundle outState )
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

        if (dialogContent != null) {
            dialogContent.loadSettings(getActivity(), bundle);
        }
    }

    private DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            // EMPTY; placeholder
        }
    };

    private View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
        }
    };

    @Override
    public void onCancel(DialogInterface dialog)
    {
        dialogContent.cancelGetFix();
        dismiss();
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
        }
    }

    private View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            dialogContent.cancelGetFix();
            if (dialogListener != null &&
                    dialogListener.saveSettings(getActivity(), dialogContent.getLocationMode(), dialogContent.getLocation()))
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
    };

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        disableTouchOutsideBehavior();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
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
            Location location = data.getParcelableExtra(PlacesActivity.EXTRA_LOCATION);
            if (location != null) {
                setLocation(getActivity(), location);
            }
        } else {
            getDialogContent().populateLocationList();
        }
    }

    private void disableTouchOutsideBehavior()
    {
        Dialog dialog = getDialog();
        Window window = dialog != null ? dialog.getWindow() : null;
        if (window != null) {
            View decorView = window.getDecorView().findViewById(android.support.design.R.id.touch_outside);
            decorView.setOnClickListener(null);
        }
    }

}

