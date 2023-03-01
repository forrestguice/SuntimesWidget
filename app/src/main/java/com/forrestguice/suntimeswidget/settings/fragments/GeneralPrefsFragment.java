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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.WelcomeActivity;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SummaryListPreference;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * General Prefs
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPrefsFragment extends PreferenceFragment
{
    public static final String LOG_TAG = "SuntimesSettings";

    private SummaryListPreference sunCalculatorPref, moonCalculatorPref;
    private CheckBoxPreference useAltitudePref;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppSettings.initLocale(getActivity());
        Log.i(SuntimesSettingsActivity.LOG_TAG, "GeneralPrefsFragment: Arguments: " + getArguments());

        PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_general, false);
        addPreferencesFromResource(R.xml.preference_general);

        sunCalculatorPref = (SummaryListPreference) findPreference(WidgetSettings.keyCalculatorModePref(0));
        moonCalculatorPref = (SummaryListPreference) findPreference(WidgetSettings.keyCalculatorModePref(0, "moon"));

        initPref_general(GeneralPrefsFragment.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(SuntimesSettingsActivity.LOG_TAG, "onActivityResult: " + requestCode + " (" + resultCode + ")");
        switch(requestCode)
        {
            case SuntimesSettingsActivity.REQUEST_WELCOME_SCREEN:
                onWelcomeScreen(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onAttach(Context context)
    {
        super.onAttach(context);
        loadPref_calculator(context, sunCalculatorPref);
        loadPref_calculator(context, moonCalculatorPref, "moon");
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        loadPref_calculator(activity, sunCalculatorPref);
        loadPref_calculator(activity, moonCalculatorPref, "moon");
    }


    private void onWelcomeScreen(int requestCode, int resultCode, Intent data)
    {
        Log.d("DEBUG", "onWelcomeScreen");
        if (!isAdded()) {
            Log.w(SuntimesSettingsActivity.LOG_TAG, "onWelcomeScreen: fragment has not yet been added to activity; ignoring result..");
            return;
        }

        Activity activity = getActivity();
        if (activity instanceof SuntimesSettingsActivity)
        {
            SuntimesSettingsActivity settingsActivity = (SuntimesSettingsActivity) activity;
            settingsActivity.setNeedsRecreateFlag();
            settingsActivity.rebuildActivity();

        } else {
            Log.w(SuntimesSettingsActivity.LOG_TAG, "onWelcomeScreen: parent activity is not SuntimesSettingsActivity; ignoring result..");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void initPref_general(final PreferenceFragment fragment)
    {
        Log.i(LOG_TAG, "initPref_general (fragment)");
        Context context = fragment.getActivity();

        String key_timeFormat = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + WidgetSettings.PREF_KEY_APPEARANCE_TIMEFORMATMODE;
        ListPreference timeformatPref = (ListPreference)fragment.findPreference(key_timeFormat);
        if (timeformatPref != null)
        {
            initPref_timeFormat(fragment.getActivity(), timeformatPref);
            loadPref_timeFormat(fragment.getActivity(), timeformatPref);
        }

        String key_altitudePref = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_LOCATION + WidgetSettings.PREF_KEY_LOCATION_ALTITUDE_ENABLED;
        CheckBoxPreference altitudePref = (CheckBoxPreference)fragment.findPreference(key_altitudePref);
        if (altitudePref != null)
        {
            initPref_altitude(fragment.getActivity(), altitudePref);
            loadPref_altitude(fragment.getActivity(), altitudePref);
        }

        String key_sunCalc = WidgetSettings.keyCalculatorModePref(0);
        SummaryListPreference calculatorPref = (SummaryListPreference) fragment.findPreference(key_sunCalc);
        if (calculatorPref != null)
        {
            initPref_calculator(context, calculatorPref, WidgetSettings.PREF_DEF_GENERAL_CALCULATOR);
            loadPref_calculator(context, calculatorPref);
        }

        String key_moonCalc = WidgetSettings.keyCalculatorModePref(0, "moon");
        SummaryListPreference moonCalculatorPref = (SummaryListPreference) fragment.findPreference(key_moonCalc);
        if (moonCalculatorPref != null)
        {
            initPref_calculator(context, moonCalculatorPref, new int[] {SuntimesCalculator.FEATURE_MOON}, WidgetSettings.PREF_DEF_GENERAL_CALCULATOR_MOON);
            loadPref_calculator(context, moonCalculatorPref, "moon");
        }

        Preference introScreenPref = fragment.findPreference("appwidget_0_intro_screen");
        if (introScreenPref != null)
        {
            introScreenPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    fragment.startActivityForResult(new Intent(fragment.getActivity(), WelcomeActivity.class), SuntimesSettingsActivity.REQUEST_WELCOME_SCREEN);
                    return false;
                }
            });
        }
    }

    public static void initPref_calculator(Context context, final SummaryListPreference calculatorPref, String defaultCalculator)
    {
        initPref_calculator(context, calculatorPref, null, defaultCalculator);
    }
    public static void initPref_calculator(Context context, final SummaryListPreference calculatorPref, int[] requestedFeatures, String defaultCalculator)
    {
        String tagDefault = context.getString(R.string.configLabel_tagDefault);
        String tagPlugin = context.getString(R.string.configLabel_tagPlugin);

        int[] colorAttrs = { R.attr.text_accentColor, R.attr.tagColor_warning };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int colorDefault = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.text_accent_dark));
        @SuppressLint("ResourceType") int colorPlugin = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.warningTag_dark));
        typedArray.recycle();

        SuntimesCalculatorDescriptor[] calculators = (requestedFeatures == null ? SuntimesCalculatorDescriptor.values(context)
                : SuntimesCalculatorDescriptor.values(context, requestedFeatures));
        String[] calculatorEntries = new String[calculators.length];
        String[] calculatorValues = new String[calculators.length];
        CharSequence[] calculatorSummaries = new CharSequence[calculators.length];

        int i = 0;
        for (SuntimesCalculatorDescriptor calculator : calculators)
        {
            calculator.initDisplayStrings(context);
            calculatorEntries[i] = calculatorValues[i] = calculator.getName();

            String displayString = (calculator.getName().equalsIgnoreCase(defaultCalculator))
                    ? context.getString(R.string.configLabel_prefSummaryTagged, calculator.getDisplayString(), tagDefault)
                    : calculator.getDisplayString();

            if (calculator.isPlugin()) {
                displayString = context.getString(R.string.configLabel_prefSummaryTagged, displayString, tagPlugin);
            }

            SpannableString styledSummary = SuntimesUtils.createBoldColorSpan(null, displayString, tagDefault, colorDefault);
            styledSummary = SuntimesUtils.createRelativeSpan(styledSummary, displayString, tagDefault, 1.15f);

            styledSummary = SuntimesUtils.createBoldColorSpan(styledSummary, displayString, tagPlugin, colorPlugin);
            styledSummary = SuntimesUtils.createRelativeSpan(styledSummary, displayString, tagPlugin, 1.15f);

            calculatorSummaries[i] = styledSummary;
            i++;
        }

        calculatorPref.setEntries(calculatorEntries);
        calculatorPref.setEntryValues(calculatorValues);
        calculatorPref.setEntrySummaries(calculatorSummaries);
    }
    public static void loadPref_calculator(Context context, SummaryListPreference calculatorPref)
    {
        loadPref_calculator(context, calculatorPref, "");
    }
    public static void loadPref_calculator(Context context, SummaryListPreference calculatorPref, String calculatorName)
    {
        if (context != null && calculatorPref != null)
        {
            SuntimesCalculatorDescriptor currentMode = WidgetSettings.loadCalculatorModePref(context, 0, calculatorName);
            int currentIndex = ((currentMode != null) ? calculatorPref.findIndexOfValue(currentMode.getName()) : -1);
            if (currentIndex >= 0)
            {
                calculatorPref.setValueIndex(currentIndex);

            } else {
                Log.w(LOG_TAG, "loadPref: Unable to load calculator preference! The list is missing an entry for the descriptor: " + currentMode);
                calculatorPref.setValue(null);  // reset to null (so subsequent selection by user gets saved and fixes this condition)
            }
        }
    }

    public static void initPref_timeFormat(final Activity context, final Preference timeformatPref)
    {
        timeformatPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o)
            {
                timeformatPref.setSummary(timeFormatPrefSummary(WidgetSettings.TimeFormatMode.valueOf((String)o), context));
                return true;
            }
        });
    }

    public static void loadPref_timeFormat(final Activity context, final ListPreference timeformatPref)
    {
        WidgetSettings.TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(context, 0);
        int index = timeformatPref.findIndexOfValue(mode.name());
        if (index < 0)
        {
            index = 0;
            WidgetSettings.TimeFormatMode mode0 = mode;
            mode = WidgetSettings.TimeFormatMode.values()[index];
            Log.w("loadPref", "timeFormat not found (" + mode0 + ") :: loading " + mode.name() + " instead..");
        }
        timeformatPref.setValueIndex(index);
        timeformatPref.setSummary(timeFormatPrefSummary(mode, context));
    }

    public static String timeFormatPrefSummary(WidgetSettings.TimeFormatMode mode, Context context)
    {
        String summary = "%s";
        if (mode == WidgetSettings.TimeFormatMode.MODE_SYSTEM)
        {
            String sysPref = android.text.format.DateFormat.is24HourFormat(context)
                    ? WidgetSettings.TimeFormatMode.MODE_24HR.getDisplayString()
                    : WidgetSettings.TimeFormatMode.MODE_12HR.getDisplayString();
            summary = context.getString(R.string.configLabel_timeFormatMode_systemsummary, "%s", sysPref);
        }
        return summary;
    }

    public static void initPref_altitude(final Activity context, final CheckBoxPreference altitudePref)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionAltitude});
        int drawableID = a.getResourceId(0, R.drawable.baseline_terrain_black_18);
        a.recycle();

        String title = context.getString(R.string.configLabel_general_altitude_enabled) + " [i]";
        int iconSize = (int) context.getResources().getDimension(R.dimen.prefIcon_size);
        ImageSpan altitudeIcon = SuntimesUtils.createImageSpan(context, drawableID, iconSize, iconSize, 0);
        SpannableStringBuilder altitudeSpan = SuntimesUtils.createSpan(context, title, "[i]", altitudeIcon);
        altitudePref.setTitle(altitudeSpan);
    }

    public static void loadPref_altitude(Context context, CheckBoxPreference altitudePref)
    {
        boolean useAltitude = WidgetSettings.loadLocationAltitudeEnabledPref(context, 0);
        altitudePref.setChecked(useAltitude);
    }
}
