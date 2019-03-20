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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import android.util.TypedValue;
import android.view.View;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class LocationConfigDialog extends DialogFragment
{
    public static final String KEY_LOCATION_HIDETITLE = "hidetitle";
    public static final String KEY_LOCATION_HIDEMODE = "hidemode";

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
    }

    /**
     * @param savedInstanceState a Bundle containing previously saved dialog state
     * @return an AlertDialog ready for display
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreateDialog(savedInstanceState);

        final FragmentActivity myParent = getActivity();
        dialogContent = new com.forrestguice.suntimeswidget.LocationConfigView(myParent);
        dialogContent.setHideTitle(hideTitle);
        dialogContent.setHideMode(hideMode);
        dialogContent.init(myParent, true);

        Resources r = getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setTitle(myParent.getString(R.string.location_dialog_title));
        builder.setView(dialogContent, 0, padding, 0, 0);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, myParent.getString(R.string.location_dialog_cancel),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialogContent.cancelGetFix();
                        dialog.dismiss();

                        if (onCanceled != null)
                        {
                            onCanceled.onClick(dialog, which);
                        }
                    }
                }
        );

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, myParent.getString(R.string.location_dialog_ok), new DialogInterface.OnClickListener()
        {
            @Override public void onClick(DialogInterface dialogInterface, int i) {/** EMPTY */}
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface)
            {
                // set the dialog's onAccept listener /after/ the dialog is shown; explicit call to `dismiss` req.
                // http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dialogContent.cancelGetFix();
                        if (dialogListener != null &&
                                dialogListener.saveSettings(myParent, dialogContent.getLocationMode(), dialogContent.getLocation()))
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

        if (savedInstanceState != null)
        {
            loadSettings(savedInstanceState);

        } else if (presetData != null) {
            dialogContent.loadSettings(myParent, presetData);

        } else if (presetLocation != null) {
            setLocation(getContext(), presetLocation);
        }
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

        if (dialogContent != null) {
            dialogContent.loadSettings(getActivity(), bundle);
        }
    }

}

