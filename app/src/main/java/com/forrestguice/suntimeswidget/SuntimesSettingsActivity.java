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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.getfix.GetFixDatabaseAdapter;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.List;

/**
 * A preferences activity for the main app;
 * @see SuntimesConfigActivity for widget configuration.
 */
public class SuntimesSettingsActivity extends PreferenceActivity
{
    final static String ACTION_PREFS_GENERAL = "com.forrestguice.suntimeswidget.PREFS_GENERAL";
    final static String ACTION_PREFS_UI = "com.forrestguice.suntimeswidget.PREFS_UI";
    final static String ACTION_PREFS_WIDGETLIST = "com.forrestguice.suntimeswidget.PREFS_WIDGETLIST";

    private Context context;

    public SuntimesSettingsActivity()
    {
        super();
    }

    /**
     * OnCreate: the Activity initially created
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);

        String action = getIntent().getAction();
        if (action != null && action.equals(ACTION_PREFS_GENERAL))
        {
            addPreferencesFromResource(R.xml.preference_general);

        } else if (action != null && action.equals(ACTION_PREFS_UI)) {
            addPreferencesFromResource(R.xml.preference_userinterface);

        } else if (action != null && action.equals(ACTION_PREFS_WIDGETLIST)) {
            // TODO

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }

        context = SuntimesSettingsActivity.this;
        WidgetSettings.initDisplayStrings(context);
    }

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            loadHeadersFromResource(R.xml.preference_headers, target);

            TypedValue typedValue = new TypedValue();   // force styled icons on headers
            int[] icActionAttr = new int[] { R.attr.icActionSettings };
            TypedArray a = obtainStyledAttributes(typedValue.data, icActionAttr);
            int settingsIcon = a.getResourceId(0, R.drawable.ic_action_settings);
            a.recycle();

            for (Header header : target)
            {
                if (header.iconRes == 0)
                {
                    header.iconRes = settingsIcon;
                }
            }
        }
    }

    /**
     * General Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPrefsFragment extends PreferenceFragment
    {
        private Context myParent;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i("GeneralPrefsFragment", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_general, false);
            addPreferencesFromResource(R.xml.preference_general);
            loadGeneral();

            Preference myPref = (Preference)findPreference("general_clearplaces");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
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
                                        new ClearPlacesTask(myParent).execute((Object[]) null);
                                    }
                                })
                                .setNegativeButton(myParent.getString(R.string.locationclear_dialog_cancel), null);

                        confirm.show();
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public void onAttach(Context context)
        {
            super.onAttach(context);
            myParent = context;
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);
            myParent = activity;
        }

        public static class ClearPlacesTask extends AsyncTask<Object, Object, Boolean>
        {
            private Context myParent;
            GetFixDatabaseAdapter db;
            ProgressDialog progress;

            public ClearPlacesTask( Context context )
            {
                myParent = context;
                db = new GetFixDatabaseAdapter(context.getApplicationContext());
            }

            public static final long MIN_WAIT_TIME = 2000;

            @Override
            protected Boolean doInBackground(Object... params)
            {
                long startTime = System.currentTimeMillis();
                db.open();
                boolean cleared = db.clearPlaces();
                db.close();
                long endTime = System.currentTimeMillis();

                while ((endTime - startTime) < MIN_WAIT_TIME)
                {
                    endTime = System.currentTimeMillis();
                }
                return cleared;
            }

            @Override
            protected void onPreExecute()
            {
                progress = ProgressDialog.show(myParent, myParent.getString(R.string.locationcleared_dialog_title), myParent.getString(R.string.locationcleared_dialog_message), true);
            }

            @Override
            protected void onPostExecute(Boolean result)
            {
                progress.dismiss();
                Toast.makeText(myParent, myParent.getString(R.string.locationcleared_toast_success), Toast.LENGTH_LONG).show();
            }
        }

        private void loadGeneral()
        {
            SuntimesCalculatorDescriptor[] calculators = SuntimesCalculatorDescriptor.values();
            String[] calculatorEntries = new String[calculators.length];
            String[] calculatorValues = new String[calculators.length];

            int i = 0;
            for (SuntimesCalculatorDescriptor calculator : calculators)
            {
                calculatorEntries[i] = calculatorValues[i] = calculator.name();
                i++;
            }

            ListPreference calculatorPref = (ListPreference)findPreference("appwidget_0_general_calculator");
            calculatorPref.setEntries(calculatorEntries);
            calculatorPref.setEntryValues(calculatorValues);
        }
    }

    /**
     * User Interface Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class UIPrefsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i("UIPrefsFragment", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_userinterface, false);
            addPreferencesFromResource(R.xml.preference_userinterface);
        }
    }

    /**
     *
     * @param fragmentName
     * @return
     */
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return GeneralPrefsFragment.class.getName().equals(fragmentName) ||
               UIPrefsFragment.class.getName().equals(fragmentName);
    }

    /**
     * OnStart: the Activity becomes visible
     */
    /**@Override
    public void onStart()
    {
        super.onStart();
        Context context = SuntimesSettingsActivity.this;
    }*/

    /**
     * OnResume: the user is now interacting w/ the Activity (running state)
     */
    /**@Override
    public void onResume()
    {
        super.onResume();
    }*/

    /**
     * OnPause: the user about to interact w/ another Activity
     */
    /**@Override
    public void onPause()
    {
        super.onPause();
    }*/


    /**
     * OnStop: the Activity no longer visible
     */
    /**@Override
    public void onStop()
    {
        super.onStop();
    }*/

    /**
     * OnDestroy: the activity destroyed
     */
    /**@Override
    public void onDestroy()
    {
        super.onDestroy();
    }*/

}
