/**
    Copyright (C) 2014-2018 Forrest Guice
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
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

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.getfix.BuildPlacesTask;
import com.forrestguice.suntimeswidget.getfix.ClearPlacesTask;
import com.forrestguice.suntimeswidget.getfix.ExportPlacesTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SummaryListPreference;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.io.File;
import java.util.List;

/**
 * A preferences activity for the main app;
 * @see SuntimesConfigActivity0 for widget configuration.
 */
public class SuntimesSettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    final static String ACTION_PREFS_GENERAL = "com.forrestguice.suntimeswidget.PREFS_GENERAL";
    final static String ACTION_PREFS_LOCALE = "com.forrestguice.suntimeswidget.PREFS_LOCALE";
    final static String ACTION_PREFS_UI = "com.forrestguice.suntimeswidget.PREFS_UI";
    final static String ACTION_PREFS_WIDGETLIST = "com.forrestguice.suntimeswidget.PREFS_WIDGETLIST";
    final static String ACTION_PREFS_PLACES = "com.forrestguice.suntimeswidget.PREFS_PLACES";

    private Context context;
    private PlacesPrefsBase placesPrefBase = null;
    private String appTheme = null;

    public SuntimesSettingsActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setResult(RESULT_OK);
        context = SuntimesSettingsActivity.this;
        appTheme = AppSettings.loadThemePref(this);
        setTheme(AppSettings.themePrefToStyleId(this, appTheme));

        super.onCreate(icicle);
        initLocale(icicle);
        initLegacyPrefs();

        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(onChangedNeedingRebuild);
    }

    /**
     * legacy pref api used for pre honeycomb devices, while honeycomb+ uses the fragment based api.
     */
    private void initLegacyPrefs()
    {
        String action = getIntent().getAction();
        if (action != null)
        {
            //noinspection IfCanBeSwitch
            if (action.equals(ACTION_PREFS_GENERAL))
            {
                //noinspection deprecation
                addPreferencesFromResource(R.xml.preference_general);
                initPref_general();

            } else if (action.equals(ACTION_PREFS_LOCALE)) {
                //noinspection deprecation
                addPreferencesFromResource(R.xml.preference_locale);
                initPref_locale();

            } else if (action.equals(ACTION_PREFS_UI)) {
                //noinspection deprecation
                addPreferencesFromResource(R.xml.preference_userinterface);
                initPref_ui();

            } else if (action.equals(ACTION_PREFS_PLACES)) {
                //noinspection deprecation
                addPreferencesFromResource(R.xml.preference_places);
                initPref_places();

            } else if (action.equals(ACTION_PREFS_WIDGETLIST)) {
                Intent intent = new Intent(this, SuntimesWidgetListActivity.class);
                startActivity(intent);
                finish();

            } else {
                Log.w("initLegacyPrefs", "unhandled action: " + action);
            }

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //noinspection deprecation
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        initLocale(null);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);

        if (placesPrefBase != null)
        {
            placesPrefBase.onResume();
        }
    }

    @Override
    public void onPause()
    {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (placesPrefBase != null)
        {
            placesPrefBase.onStop();
        }
    }

    @Override
    public void onDestroy()
    {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(onChangedNeedingRebuild);
        super.onDestroy();
    }

    private void initLocale(Bundle icicle)
    {
        boolean localeChanged = AppSettings.initLocale(this);
        WidgetSettings.initDefaults(context);

        AppSettings.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings(context);

        boolean themeChanged = false;
        if (icicle != null)
        {
            String prevTheme = icicle.getString(AppSettings.PREF_KEY_APPEARANCE_THEME);
            if (prevTheme == null) {
                prevTheme = appTheme;
            }
            themeChanged = !prevTheme.equals(appTheme);
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) && (localeChanged || themeChanged))
        {
            invalidateHeaders();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(AppSettings.PREF_KEY_APPEARANCE_THEME, appTheme);
    }

    /**
     * @param target the target list to place headers into
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
     * @param fragmentName reference to some fragment (by name)
     * @return true is a PreferenceFragment allowed by this activity
     */
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return GeneralPrefsFragment.class.getName().equals(fragmentName) ||
               LocalePrefsFragment.class.getName().equals(fragmentName) ||
               UIPrefsFragment.class.getName().equals(fragmentName) ||
               PlacesPrefsFragment.class.getName().equals(fragmentName);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener onChangedNeedingRebuild = new SharedPreferences.OnSharedPreferenceChangeListener()
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if (key.equals(AppSettings.PREF_KEY_LOCALE) || key.equals(AppSettings.PREF_KEY_LOCALE_MODE)
                    || key.equals(AppSettings.PREF_KEY_APPEARANCE_THEME))
            {
                //Log.d("SettingsActivity", "Locale change detected; restarting activity");
                updateLocale();
                rebuildActivity();
            }
        }
    };

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_CALCULATOR))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveCalculatorModePref(this, 0, SuntimesCalculatorDescriptor.valueOf(sharedPreferences.getString(key, "missing")));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_CALCULATOR + "_moon"))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveCalculatorModePref(this, 0, "moon", SuntimesCalculatorDescriptor.valueOf(sharedPreferences.getString(key, "missing")));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_APPEARANCE_TIMEFORMATMODE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveTimeFormatModePref(this, 0, WidgetSettings.TimeFormatMode.valueOf(sharedPreferences.getString(key, "missing")));
            updateLocale();
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_TRACKINGMODE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveTrackingModePref(this, 0, WidgetSettings.TrackingMode.valueOf(sharedPreferences.getString(key, "missing")));
	        return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWSECONDS))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowSecondsPref(this, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWSECONDS));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWTIMEDATE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowTimeDatePref(this, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWTIMEDATE));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWWEEKS))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowWeeksPref(this, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWWEEKS));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_SHOWHOURS))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveShowHoursPref(this, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_SHOWHOURS));
            return;
        }
    }

    protected void updateLocale()
    {
        AppSettings.initLocale(this);

        SuntimesWidget0.triggerWidgetUpdate(this, SuntimesWidget0.class);
        SuntimesWidget0.triggerWidgetUpdate(this, SuntimesWidget0_2x1.class);

        SuntimesWidget0.triggerWidgetUpdate(this, SuntimesWidget1.class);

        SuntimesWidget0.triggerWidgetUpdate(this, SolsticeWidget0.class);

        SuntimesWidget0.triggerWidgetUpdate(this, SuntimesWidget2.class);
        SuntimesWidget0.triggerWidgetUpdate(this, SuntimesWidget2_3x1.class);

        SuntimesWidget0.triggerWidgetUpdate(this, MoonWidget0.class);
        SuntimesWidget0.triggerWidgetUpdate(this, MoonWidget0_2x1.class);
        SuntimesWidget0.triggerWidgetUpdate(this, MoonWidget0_3x1.class);
    }

    protected void rebuildActivity()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            invalidateHeaders();
            recreate();

        } else {
            finish();
            startActivity(getIntent());
        }
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * General Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPrefsFragment extends PreferenceFragment
    {
        private SummaryListPreference sunCalculatorPref, moonCalculatorPref;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            AppSettings.initLocale(getActivity());
            Log.i("GeneralPrefsFragment", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_general, false);
            addPreferencesFromResource(R.xml.preference_general);

            sunCalculatorPref = (SummaryListPreference) findPreference(WidgetSettings.keyCalculatorModePref(0));
            moonCalculatorPref = (SummaryListPreference) findPreference(WidgetSettings.keyCalculatorModePref(0, "moon"));
            initPref_general(GeneralPrefsFragment.this);
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
    }

    /**
     * init legacy prefs
     */
    private void initPref_general()
    {
        String key_sunCalc = WidgetSettings.keyCalculatorModePref(0);
        //noinspection deprecation
        SummaryListPreference sunCalculatorPref = (SummaryListPreference)findPreference(key_sunCalc);
        if (sunCalculatorPref != null)
        {
            initPref_calculator(this, sunCalculatorPref);
            loadPref_calculator(this, sunCalculatorPref);
        }

        String key_moonCalc = WidgetSettings.keyCalculatorModePref(0, "moon");
        //noinspection deprecation
        SummaryListPreference moonCalculatorPref = (SummaryListPreference)findPreference(key_moonCalc);
        if (moonCalculatorPref != null)
        {
            initPref_calculator(this, moonCalculatorPref, new int[] {SuntimesCalculator.FEATURE_MOON});
            loadPref_calculator(this, moonCalculatorPref, "moon");
        }

        String key_timeFormat = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + WidgetSettings.PREF_KEY_APPEARANCE_TIMEFORMATMODE;
        ListPreference timeformatPref = (ListPreference)findPreference(key_timeFormat);
        initPref_timeFormat(this, timeformatPref);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_general(PreferenceFragment fragment)
    {
        Context context = fragment.getActivity();

        String key_sunCalc = WidgetSettings.keyCalculatorModePref(0);
        SummaryListPreference calculatorPref = (SummaryListPreference) fragment.findPreference(key_sunCalc);
        if (calculatorPref != null)
        {
            initPref_calculator(context, calculatorPref);
            loadPref_calculator(context, calculatorPref);
        }

        String key_moonCalc = WidgetSettings.keyCalculatorModePref(0, "moon");
        SummaryListPreference moonCalculatorPref = (SummaryListPreference) fragment.findPreference(key_moonCalc);
        if (moonCalculatorPref != null)
        {
            initPref_calculator(context, moonCalculatorPref, new int[] {SuntimesCalculator.FEATURE_MOON});
            loadPref_calculator(context, moonCalculatorPref, "moon");
        }

        String key_timeFormat = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + WidgetSettings.PREF_KEY_APPEARANCE_TIMEFORMATMODE;
        Preference timeformatPref = fragment.findPreference(key_timeFormat);
        initPref_timeFormat(fragment.getActivity(), timeformatPref);
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * Locale Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LocalePrefsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            AppSettings.initLocale(getActivity());
            Log.i("LocalePrefsFragment", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_locale, false);
            addPreferencesFromResource(R.xml.preference_locale);

            initPref_locale(LocalePrefsFragment.this);
        }
    }

    /**
     * init legacy prefs
     */
    private void initPref_locale()
    {
        //String key = AppSettings.PREF_KEY_LOCALE_MODE;
        //ListPreference modePref = (ListPreference)findPreference(key);
        //legacyPrefs.put(key, new LegacyListPref(modePref));

        String key = AppSettings.PREF_KEY_LOCALE;
        //noinspection deprecation
        ListPreference localePref = (ListPreference)findPreference(key);
        //legacyPrefs.put(key, new LegacyListPref(localePref));

        initPref_locale(this, localePref);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_locale(PreferenceFragment fragment)
    {
        Preference localePref = fragment.findPreference(AppSettings.PREF_KEY_LOCALE);
        initPref_locale(fragment.getActivity(), localePref);
    }
    private static void initPref_locale(Activity activity, Preference localePref)
    {
        AppSettings.LocaleMode localeMode = AppSettings.loadLocaleModePref(activity);
        localePref.setEnabled(localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE);
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * Places Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PlacesPrefsFragment extends PreferenceFragment
    {
        private PlacesPrefsBase base;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            AppSettings.initLocale(getActivity());
            Log.i("PlacesPrefsFragment", "Arguments: " + getArguments());
            setRetainInstance(true);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_places, false);
            addPreferencesFromResource(R.xml.preference_places);

            Preference clearPlacesPref = findPreference("places_clear");
            Preference exportPlacesPref = findPreference("places_export");
            Preference buildPlacesPref = findPreference("places_build");
            base = new PlacesPrefsBase(getActivity(), buildPlacesPref, clearPlacesPref, exportPlacesPref);
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
            if (base != null)
            {
                base.setParent(context);
            }
        }

        @Override
        public void onAttach(Activity activity)
        {
            super.onAttach(activity);
            if (base != null)
            {
                base.setParent(activity);
            }
        }
    }

    /**
     * Places Prefs - Base
     */
    private static class PlacesPrefsBase
    {
        public static final String KEY_ISBUILDING = "isbuilding";
        public static final String KEY_ISCLEARING = "isclearing";
        public static final String KEY_ISEXPORTING = "isexporting";

        private Context myParent;
        private ProgressDialog progress;

        private BuildPlacesTask buildPlacesTask = null;
        private boolean isBuilding = false;

        private ClearPlacesTask clearPlacesTask = null;
        private boolean isClearing = false;

        private ExportPlacesTask exportPlacesTask = null;
        private boolean isExporting = false;

        public PlacesPrefsBase(Context context, Preference buildPref, Preference clearPref, Preference exportPref)
        {
            myParent = context;

            if (buildPref != null)
                buildPref.setOnPreferenceClickListener(onClickBuildPlaces);

            if (clearPref != null)
                clearPref.setOnPreferenceClickListener(onClickClearPlaces);

            if (exportPref != null)
                exportPref.setOnPreferenceClickListener(onClickExportPlaces);
        }

        public void setParent( Context context )
        {
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
         * Build Places (click handler)
         */
        private Preference.OnPreferenceClickListener onClickBuildPlaces = new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                if (myParent != null)
                {
                    buildPlacesTask = new BuildPlacesTask(myParent);
                    buildPlacesTask.setTaskListener(buildPlacesListener);
                    buildPlacesTask.execute();
                    return true;
                }
                return false;
            }
        };

        /**
         * Build Places (task handler)
         */
        private BuildPlacesTask.TaskListener buildPlacesListener = new BuildPlacesTask.TaskListener()
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
        private Preference.OnPreferenceClickListener onClickExportPlaces = new Preference.OnPreferenceClickListener()
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
        private ExportPlacesTask.TaskListener exportPlacesListener = new ExportPlacesTask.TaskListener()
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
                    String successMessage = myParent.getString(R.string.msg_export_success, results.getExportFile().getAbsolutePath());
                    Toast.makeText(myParent.getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(results.getExportFile()));
                    shareIntent.setType("text/csv");
                    myParent.startActivity(Intent.createChooser(shareIntent, myParent.getResources().getText(R.string.msg_export_to)));

                } else {
                    File file = results.getExportFile();
                    String path = ((file != null) ? file.getAbsolutePath() : "<path>");
                    String failureMessage = myParent.getString(R.string.msg_export_failure, path);
                    Toast.makeText(myParent.getApplicationContext(), failureMessage, Toast.LENGTH_LONG).show();
                }
            }
        };

        /**
         * Clear Places (click handler)
         */
        private Preference.OnPreferenceClickListener onClickClearPlaces = new Preference.OnPreferenceClickListener()
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
                                    clearPlacesTask = new ClearPlacesTask(myParent);
                                    clearPlacesTask.setTaskListener(clearPlacesListener);
                                    clearPlacesTask.execute((Object[]) null);
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
        private ClearPlacesTask.TaskListener clearPlacesListener = new ClearPlacesTask.TaskListener()
        {
            @Override
            public void onStarted()
            {
                isClearing = true;
                showProgressClearing();
            }

            @Override
            public void onFinished(Boolean result)
            {
                clearPlacesTask = null;
                isClearing = false;
                dismissProgress();
                Toast.makeText(myParent, myParent.getString(R.string.locationcleared_toast_success), Toast.LENGTH_LONG).show();
            }
        };

        private void onStop()
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

        private void onResume()
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

    /**
     * init legacy prefs
     */
    private void initPref_places()
    {
        //noinspection deprecation
        Preference buildPlacesPref = findPreference("places_build");
        //noinspection deprecation
        Preference clearPlacesPref = findPreference("places_clear");
        //noinspection deprecation
        Preference exportPlacesPref = findPreference("places_export");
        placesPrefBase = new PlacesPrefsBase(this, buildPlacesPref, clearPlacesPref, exportPlacesPref);
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

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
            AppSettings.initLocale(getActivity());
            Log.i("UIPrefsFragment", "Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_userinterface, false);
            addPreferencesFromResource(R.xml.preference_userinterface);

            initPref_ui(UIPrefsFragment.this);
        }
    }

    /**
     * init legacy prefs
     */
    private void initPref_ui()
    {
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_ui(PreferenceFragment fragment)
    {
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    private static void initPref_timeFormat(final Activity context, final Preference timeformatPref)
    {
        WidgetSettings.TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(context, 0);
        timeformatPref.setSummary(timeFormatPrefSummary(mode, context));
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

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    private static void initPref_calculator(Context context, final SummaryListPreference calculatorPref)
    {
        initPref_calculator(context, calculatorPref, null);
    }
    private static void initPref_calculator(Context context, final SummaryListPreference calculatorPref, int[] requestedFeatures)
    {
        SuntimesCalculatorDescriptor[] calculators = (requestedFeatures == null ? SuntimesCalculatorDescriptor.values()
                                                                                : SuntimesCalculatorDescriptor.values(requestedFeatures));
        String[] calculatorEntries = new String[calculators.length];
        String[] calculatorValues = new String[calculators.length];
        String[] calculatorSummaries = new String[calculators.length];

        int i = 0;
        for (SuntimesCalculatorDescriptor calculator : calculators)
        {
            calculator.initDisplayStrings(context);
            calculatorEntries[i] = calculatorValues[i] = calculator.name();
            calculatorSummaries[i] = calculator.getDisplayString();
            i++;
        }

        calculatorPref.setEntries(calculatorEntries);
        calculatorPref.setEntryValues(calculatorValues);
        calculatorPref.setEntrySummaries(calculatorSummaries);
    }
    private static void loadPref_calculator(Context context, SummaryListPreference calculatorPref)
    {
        loadPref_calculator(context, calculatorPref, "");
    }
    private static void loadPref_calculator(Context context, SummaryListPreference calculatorPref, String calculatorName)
    {
        if (context != null && calculatorPref != null)
        {
            SuntimesCalculatorDescriptor currentMode = WidgetSettings.loadCalculatorModePref(context, 0, calculatorName);
            int currentIndex = ((currentMode != null) ? calculatorPref.findIndexOfValue(currentMode.name()) : 0);
            calculatorPref.setValueIndex(currentIndex);
            //Log.d("SuntimesSettings", "current mode: " + currentMode + " (" + currentIndex + ")");
        }
    }


}
