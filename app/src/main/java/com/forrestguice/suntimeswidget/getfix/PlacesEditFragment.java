/**
    Copyright (C) 2014-2019 Forrest Guice
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
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import com.forrestguice.suntimeswidget.LocationConfigView;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class PlacesEditFragment extends BottomSheetDialogFragment
{
    private LocationConfigView dialogContent;
    public LocationConfigView getDialogContent() { return dialogContent; }

    protected DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener ) {
        onAccepted = listener;
    }

    protected DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener ) {
        onCanceled = listener;
    }

    protected FragmentListener listener;
    public void setFragmentListener( FragmentListener value ) {
        listener = value;
    }

    public interface FragmentListener
    {
        //public boolean saveSettings(Context context, WidgetSettings.LocationMode locationMode, Location location);
    }

    private Location presetLocation = null;
    public void setLocation(Context context, Location location)
    {
        presetLocation = location;
        if (dialogContent != null) {
            dialogContent.loadSettings(context, LocationConfigView.bundleData(presetLocation.getUri(), presetLocation.getLabel(), LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT));
        }
    }

    /**
     * Preset data (in the form of a geo URI)
     */
    private Uri presetData = null;
    public void setData(Uri data)
    {
        presetData = data;
        if (dialogContent != null) {
            dialogContent.loadSettings(getActivity(), presetData);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
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
        if (dialogContent != null) {
            dialogContent.cancelGetFix();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (dialogContent != null) {
            dialogContent.onResume();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View view = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_place, parent, false);
        final FragmentActivity myParent = getActivity();

        dialogContent = (LocationConfigView) view.findViewById(R.id.locationConfig);
        dialogContent.setHideTitle(true);
        dialogContent.setHideMode(true);
        dialogContent.init(myParent, false);

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

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void saveSettings(Bundle bundle)
    {
        if (dialogContent != null) {
            dialogContent.saveSettings(bundle);
        }
    }

    protected void loadSettings(Bundle bundle)
    {
        if (dialogContent != null) {
            dialogContent.loadSettings(getActivity(), bundle);
        }
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
        dialogContent.cancelGetFix();
        dismiss();
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
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

}

