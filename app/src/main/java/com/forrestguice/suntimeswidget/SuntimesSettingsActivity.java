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
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.List;

/**
 * A preferences activity for the main app;
 * @see SuntimesConfigActivity for widget configuration.
 */
public class SuntimesSettingsActivity extends PreferenceActivity
{
    final static String ACTION_PREFS_ONE = "com.forrestguice.suntimeswidget.PREFS_ONE";

    //protected static SuntimesUtils utils = new SuntimesUtils();

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
        super.onCreate(icicle);

        String action = getIntent().getAction();
        if (action != null && action.equals(ACTION_PREFS_ONE))
        {
            addPreferencesFromResource(R.xml.preference_general);

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
        {
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }

        Context context = SuntimesSettingsActivity.this;
        WidgetSettings.initDisplayStrings(context);
    }

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            loadHeadersFromResource(R.xml.preference_headers, target);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPrefsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            Log.i("args", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_general, false);
            addPreferencesFromResource(R.xml.preference_general);
            loadGeneral();
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

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return GeneralPrefsFragment.class.getName().equals(fragmentName);
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
