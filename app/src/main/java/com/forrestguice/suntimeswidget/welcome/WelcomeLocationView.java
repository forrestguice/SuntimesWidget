/**
    Copyright (C) 2022-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.welcome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.getfix.BuildPlacesTask;
import com.forrestguice.suntimeswidget.getfix.LocationConfigDialog;
import com.forrestguice.suntimeswidget.getfix.LocationConfigView;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.app.FragmentManagerCompat;
import com.forrestguice.util.ExecutorUtils;
import com.forrestguice.util.concurrent.TaskListener;

/**
 * WelcomeLocationFragment
 */
public class WelcomeLocationView extends WelcomeView
{
    public static final int IMPORT_REQUEST = 1100;

    private Button button_addPlaces, button_importPlaces, button_lookupLocation;
    private ProgressBar progress_addPlaces;
    private View layout_permissions;

    public WelcomeLocationView(AppCompatActivity activity) {
        super(activity, R.layout.layout_welcome_location);
    }

    public static WelcomeLocationView newInstance(AppCompatActivity activity) {
        return new WelcomeLocationView(activity);
    }

    @Override
    public void initViews(Context context, View view)
    {
        super.initViews(context, view);

        button_addPlaces = (Button) view.findViewById(R.id.button_build_places);
        if (button_addPlaces != null) {
            button_addPlaces.setOnClickListener(onAddPlacesClicked());
        }

        button_importPlaces = (Button) view.findViewById(R.id.button_import_places);
        if (button_importPlaces != null) {
            button_importPlaces.setOnClickListener(onImportPlacesClicked());
        }

        button_lookupLocation = (Button) view.findViewById(R.id.button_lookup_location);
        if (button_lookupLocation != null) {
            button_lookupLocation.setOnClickListener(onLookupLocationClicked());
        }

        layout_permissions = view.findViewById(R.id.layout_permissions);
        if (layout_permissions != null) {
            layout_permissions.setVisibility(View.GONE);   // toggled visible by locationConfig
        }

        LocationConfigDialog locationConfig = getLocationConfigDialog();
        if (locationConfig != null) {
            locationConfig.setDialogListener(locationConfigListener());
        }

        progress_addPlaces = (ProgressBar) view.findViewById(R.id.progress_build_places);
        toggleProgress(false);
    }

    private LocationConfigDialog.LocationConfigDialogListener locationConfigListener()
    {
        return new LocationConfigDialog.LocationConfigDialogListener()
        {
            @Override
            public void onEditModeChanged(LocationConfigView.LocationViewMode mode)
            {
                switch (mode) {
                    case MODE_CUSTOM_ADD:
                    case MODE_CUSTOM_EDIT:
                        togglePermissionsText(true); break;
                    default: togglePermissionsText(false); break;
                }
            }
        };
    }

    protected void togglePermissionsText(boolean value) {
        if (layout_permissions != null) {
            layout_permissions.setVisibility(value ? View.VISIBLE : View.GONE);
        }
    }

    private OnClickListener onLookupLocationClicked()
    {
        return new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LocationConfigDialog locationConfig = getLocationConfigDialog();
                if (locationConfig != null) {
                    locationConfig.addCurrentLocation(getContext());
                }
                if (button_lookupLocation != null) {
                    button_lookupLocation.setEnabled(false);
                    button_lookupLocation.setVisibility(View.GONE);
                }
            }
        };
    }

    //@Override
    public void onActivityResultCompat(int requestCode, int resultCode, Intent data)
    {
        //super.onActivityResultCompat(requestCode, resultCode, data);
        switch (requestCode)
        {
            case IMPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        importPlaces(getContext(), uri);
                    }
                } else {
                    reloadLocationList();
                }
                break;
        }
    }

    private OnClickListener onImportPlacesClicked()
    {
        return new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AppCompatActivity activity = getActivity();
                if (activity != null) {
                    activity.startActivityForResultCompat(BuildPlacesTask.buildPlacesOpenFileIntent(), IMPORT_REQUEST);
                }
            }
        };
    }
    protected void importPlaces(Context context, @NonNull Uri uri)
    {
        BuildPlacesTask task = new BuildPlacesTask(context, new Object[] { false, uri });
        ExecutorUtils.runTask("ImportPlacesTask", task, importPlacesListener);
    }
    private final TaskListener<Integer> importPlacesListener = new TaskListener<Integer>()
    {
        @Override
        public void onStarted() {
            //setRetainInstance(true);
            toggleControlsEnabled(false);
            toggleControlsVisible(false);
            setLocationViewMode(LocationConfigView.LocationViewMode.MODE_DISABLED);
            toggleProgress(true);
        }

        @Override
        public void onFinished(Integer result)
        {
            //setRetainInstance(false);
            toggleProgress(false);
            toggleControlsEnabled(true);
            toggleControlsVisible(true);
            if (result > 0)
            {
                Context context = getContext();
                if (context != null && button_importPlaces != null) {
                    button_importPlaces.setText(context.getString(R.string.locationbuild_toast_success, result.toString()));
                    button_importPlaces.setEnabled(false);
                }
            }
            setLocationViewMode(LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT);
            reloadLocationList();
        }
    };

    private OnClickListener onAddPlacesClicked()
    {
        return new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "onAddPlacesClicked: ");
                if (getContext() != null) {
                    BuildPlacesTask.promptAddWorldPlaces(getContext(), buildPlacesListener);
                }
            }
        };
    }
    private final TaskListener<Integer> buildPlacesListener = new TaskListener<Integer>()
    {
        @Override
        public void onStarted()
        {
            //setRetainInstance(true);
            toggleControlsEnabled(false);
            toggleControlsVisible(false);
            setLocationViewMode(LocationConfigView.LocationViewMode.MODE_DISABLED);
            toggleProgress(true);
        }

        @Override
        public void onFinished(Integer result)
        {
            //setRetainInstance(false);
            toggleProgress(false);
            toggleControlsEnabled(true);
            toggleControlsVisible(true);

            if (result > 0)
            {
                Context context = getContext();
                if (context != null && button_addPlaces != null) {
                    button_addPlaces.setText(context.getString(R.string.locationbuild_toast_success, result.toString()));
                    button_addPlaces.setEnabled(false);
                }
            }

            setLocationViewMode(LocationConfigView.LocationViewMode.MODE_CUSTOM_SELECT);
            reloadLocationList();
        }
    };

    protected void toggleControlsEnabled(boolean value)
    {
        if (button_addPlaces != null) {
            button_addPlaces.setEnabled(value);
        }
        if (button_importPlaces != null) {
            button_importPlaces.setEnabled(value);
        }
        if (button_lookupLocation != null) {
            button_lookupLocation.setEnabled(value);
        }
    }

    protected void toggleControlsVisible(boolean visible)
    {
        if (button_addPlaces != null) {
            button_addPlaces.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
        if (button_importPlaces != null) {
            button_importPlaces.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
        if (button_lookupLocation != null) {
            button_lookupLocation.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    protected void toggleProgress(boolean visible) {
        if (progress_addPlaces != null) {
            progress_addPlaces.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Nullable
    private LocationConfigDialog getLocationConfigDialog()
    {
        if (isAdded())
        {
            FragmentManagerCompat fragments = getFragmentManager();
            return (fragments != null ? (LocationConfigDialog) fragments.findFragmentByTag("LocationConfigDialog") : null);
        }
        return null;
    }

    protected void reloadLocationList()
    {
        LocationConfigDialog locationConfig = getLocationConfigDialog();
        if (locationConfig != null) {
            locationConfig.getDialogContent().populateLocationList();
            locationConfig.getDialogContent().clickLocationSpinner();
        }
    }

    protected void setLocationViewMode( LocationConfigView.LocationViewMode value)
    {
        LocationConfigDialog locationConfig = getLocationConfigDialog();
        if (locationConfig != null) {
            locationConfig.getDialogContent().setMode(value);
        }
    }

    @Override
    public boolean validateInput(Context context)
    {
        LocationConfigDialog locationConfig = getLocationConfigDialog();
        if (locationConfig != null) {
            return locationConfig.getDialogContent().validateInput(context);
        }
        return super.validateInput(context);
    }

    @Override
    public boolean saveSettings(Context context)
    {
        LocationConfigDialog locationConfig = getLocationConfigDialog();
        if (locationConfig != null)
        {
            boolean saved = locationConfig.getDialogContent().saveSettings(context);
            //Log.d("DEBUG", "saveSettings: location " + saved);
            return saved;
        }
        return false;
    }
}
