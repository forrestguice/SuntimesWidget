/**
    Copyright (C) 2014-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.forrestguice.support.content.FileProvider;
import com.forrestguice.support.design.app.AlertDialog;
import android.util.Log;

import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.getfix.BuildPlacesTask;
import com.forrestguice.suntimeswidget.getfix.ExportPlacesTask;
import com.forrestguice.suntimeswidget.getfix.PlacesActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.views.Toast;

import java.io.File;

/**
 * Places Prefs
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PlacesPrefsFragment extends PreferenceFragment
{
    private PlacesPrefsBase base;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppSettings.initLocale(getActivity());
        Log.i(SuntimesSettingsActivity.LOG_TAG, "PlacesPrefsFragment: Arguments: " + getArguments());
        setRetainInstance(true);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_places, false);
        addPreferencesFromResource(R.xml.preference_places);

        Preference managePlacesPref = findPreference("places_manage");
        Preference clearPlacesPref = findPreference("places_clear");
        Preference exportPlacesPref = findPreference("places_export");
        Preference buildPlacesPref = findPreference("places_build");
        base = new PlacesPrefsBase(getActivity(), managePlacesPref, buildPlacesPref, clearPlacesPref, exportPlacesPref);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        base.onStop();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        base.onResume();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (base != null) {
            base.setParent(getActivity());
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (base != null) {
            base.setParent(activity);
        }
    }

    /**
     * Places Prefs - Base
     */
    public static class PlacesPrefsBase
    {
        //public static final String KEY_ISBUILDING = "isbuilding";
        //public static final String KEY_ISCLEARING = "isclearing";
        //public static final String KEY_ISEXPORTING = "isexporting";

        private Activity myParent;
        private ProgressDialog progress;

        private BuildPlacesTask buildPlacesTask = null;
        private boolean isBuilding = false;

        private BuildPlacesTask clearPlacesTask = null;
        private boolean isClearing = false;

        private ExportPlacesTask exportPlacesTask = null;
        private boolean isExporting = false;

        public PlacesPrefsBase(Activity context, Preference managePref, Preference buildPref, Preference clearPref, Preference exportPref)
        {
            myParent = context;

            if (managePref != null) {
                managePref.setOnPreferenceClickListener(onClickManagePlaces);
            }

            if (buildPref != null)
                buildPref.setOnPreferenceClickListener(onClickBuildPlaces);

            if (clearPref != null)
                clearPref.setOnPreferenceClickListener(onClickClearPlaces);

            if (exportPref != null)
                exportPref.setOnPreferenceClickListener(onClickExportPlaces);
        }

        public void setParent( Activity context ) {
            myParent = context;
        }

        public void showProgressBuilding()
        {
            progress = ProgressDialog.show(myParent, myParent.getString(R.string.locationbuild_dialog_title), myParent.getString(R.string.locationbuild_dialog_message), true);
        }

        public void showProgressClearing()
        {
            progress = ProgressDialog.show(myParent, myParent.getString(R.string.locationcleared_dialog_title), myParent.getString(R.string.locationcleared_dialog_message), true);
        }

        public void showProgressExporting()
        {
            progress = ProgressDialog.show(myParent, myParent.getString(R.string.locationexport_dialog_title), myParent.getString(R.string.locationexport_dialog_message), true);
        }

        public void dismissProgress()
        {
            if (progress != null && progress.isShowing())
            {
                progress.dismiss();
            }
        }

        /**
         * Manage Places (click handler)
         */
        private final Preference.OnPreferenceClickListener onClickManagePlaces = new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                if (myParent != null)
                {
                    Intent intent = new Intent(myParent, PlacesActivity.class);
                    myParent.startActivity(intent);
                    myParent.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
                    return true;
                }
                return false;
            }
        };

        /**
         * Build Places (click handler)
         */
        private final Preference.OnPreferenceClickListener onClickBuildPlaces = new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                if (myParent != null)
                {
                    BuildPlacesTask.promptAddWorldPlaces(myParent, buildPlacesListener);
                    return true;
                }
                return false;
            }
        };

        /**
         * Build Places (task handler)
         */
        private final BuildPlacesTask.TaskListener buildPlacesListener = new BuildPlacesTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                isBuilding = true;
                showProgressBuilding();
            }

            @Override
            public void onFinished(Integer result)
            {
                buildPlacesTask = null;
                isBuilding = false;
                dismissProgress();
                if (result > 0) {
                    Toast.makeText(myParent, myParent.getString(R.string.locationbuild_toast_success, result.toString()), Toast.LENGTH_LONG).show();
                } // else // TODO: fail msg
            }
        };

        /**
         * Export Places (click handler)
         */
        private final Preference.OnPreferenceClickListener onClickExportPlaces = new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                if (myParent != null)
                {
                    exportPlacesTask = new ExportPlacesTask(myParent, "SuntimesPlaces", true, true);  // export to external cache
                    exportPlacesTask.setTaskListener(exportPlacesListener);
                    exportPlacesTask.execute();
                    return true;
                }
                return false;
            }
        };

        /**
         * Export Places (task handler)
         */
        private final ExportPlacesTask.TaskListener exportPlacesListener = new ExportPlacesTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                isExporting = true;
                showProgressExporting();
            }

            @Override
            public void onFinished(ExportPlacesTask.ExportResult results)
            {
                exportPlacesTask = null;
                isExporting = false;
                dismissProgress();

                if (results.getResult())
                {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setType(results.getMimeType());
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        //Uri shareURI = Uri.fromFile(results.getExportFile());  // this URI works until api26 (throws FileUriExposedException)
                        Uri shareURI = FileProvider.getUriForFile(myParent, ExportTask.FILE_PROVIDER_AUTHORITY, results.getExportFile());
                        shareIntent.putExtra(Intent.EXTRA_STREAM, shareURI);

                        String successMessage = myParent.getString(R.string.msg_export_success, results.getExportFile().getAbsolutePath());
                        Toast.makeText(myParent.getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();

                        myParent.startActivity(Intent.createChooser(shareIntent, myParent.getResources().getText(R.string.msg_export_to)));
                        return;   // successful export ends here...

                    } catch (Exception e) {
                        Log.e("ExportPlaces", "Failed to share file URI! " + e);
                    }
                }

                File file = results.getExportFile();    // export failed
                String path = ((file != null) ? file.getAbsolutePath() : "<path>");
                String failureMessage = myParent.getString(R.string.msg_export_failure, path);
                Toast.makeText(myParent.getApplicationContext(), failureMessage, Toast.LENGTH_LONG).show();
            }
        };

        /**
         * Clear Places (click handler)
         */
        private final Preference.OnPreferenceClickListener onClickClearPlaces = new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                if (myParent != null)
                {
                    AlertDialog.Builder confirm = new AlertDialog.Builder(myParent)
                            .setTitle(myParent.getString(R.string.locationclear_dialog_title))
                            .setMessage(myParent.getString(R.string.locationclear_dialog_message))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(myParent.getString(R.string.locationclear_dialog_ok), new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    clearPlacesTask = new BuildPlacesTask(myParent);
                                    clearPlacesTask.setTaskListener(clearPlacesListener);
                                    clearPlacesTask.execute(true);   // clearFlag set to true
                                }
                            })
                            .setNegativeButton(myParent.getString(R.string.locationclear_dialog_cancel), null);

                    confirm.show();
                    return true;
                }
                return false;
            }
        };

        /**
         * Clear Places (task handler)
         */
        private final BuildPlacesTask.TaskListener clearPlacesListener = new BuildPlacesTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                isClearing = true;
                showProgressClearing();
            }

            @Override
            public void onFinished(Integer result)
            {
                clearPlacesTask = null;
                isClearing = false;
                dismissProgress();
                Toast.makeText(myParent, myParent.getString(R.string.locationcleared_toast_success), Toast.LENGTH_LONG).show();
            }
        };

        public void onStop()
        {
            if (isClearing && clearPlacesTask != null)
            {
                clearPlacesTask.pauseTask();
                clearPlacesTask.clearTaskListener();
            }

            if (isExporting && exportPlacesTask != null)
            {
                exportPlacesTask.pauseTask();
                exportPlacesTask.clearTaskListener();
            }

            if (isBuilding && buildPlacesTask != null)
            {
                buildPlacesTask.pauseTask();
                buildPlacesTask.clearTaskListener();
            }

            dismissProgress();
        }

        public void onResume()
        {

            if (isClearing && clearPlacesTask != null && clearPlacesTask.isPaused())
            {
                clearPlacesTask.setTaskListener(clearPlacesListener);
                showProgressClearing();
                clearPlacesTask.resumeTask();
            }

            if (isExporting && exportPlacesTask != null)
            {
                exportPlacesTask.setTaskListener(exportPlacesListener);
                showProgressExporting();
                exportPlacesTask.resumeTask();
            }

            if (isBuilding && buildPlacesTask != null)
            {
                buildPlacesTask.setTaskListener(buildPlacesListener);
                showProgressBuilding();
                buildPlacesTask.resumeTask();
            }
        }
    }
}
