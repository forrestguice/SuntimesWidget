/**
    Copyright (C) 2014-2024 Forrest Guice
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
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.util.TypedValue;

import com.forrestguice.suntimeswidget.alarmclock.bedtime.BedtimeSettings;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValuesSheetActivity;
import com.forrestguice.suntimeswidget.settings.SettingsActivityInterface;
import com.forrestguice.suntimeswidget.settings.WidgetSettingsPreferenceHelper;
import com.forrestguice.suntimeswidget.settings.fragments.AlarmPrefsFragment;
import com.forrestguice.suntimeswidget.settings.fragments.CalendarPrefsFragment;
import com.forrestguice.suntimeswidget.settings.fragments.GeneralPrefsFragment;
import com.forrestguice.suntimeswidget.settings.fragments.LocalePrefsFragment;
import com.forrestguice.suntimeswidget.settings.fragments.PlacesPrefsFragment;
import com.forrestguice.suntimeswidget.settings.fragments.UIPrefsFragment;

import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.actions.ActionListActivity;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import com.forrestguice.suntimeswidget.events.EventListActivity;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.LengthPreference;
import com.forrestguice.suntimeswidget.settings.SummaryListPreference;
import com.forrestguice.suntimeswidget.settings.ActionButtonPreference;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesThemeContract;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;
import com.forrestguice.suntimeswidget.widgets.WidgetListAdapter;

import java.util.List;

import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_APPEARANCE_THEME_DARK;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_APPEARANCE_THEME_LIGHT;

/**
 * A preferences activity for the main app;
 * @see SuntimesConfigActivity0 for widget configuration.
 */
public class SuntimesSettingsActivity extends PreferenceActivity
{
    public static final String LOG_TAG = "SuntimesSettings";

    final static String ACTION_PREFS_GENERAL = "settings.PREFS_GENERAL";
    final static String ACTION_PREFS_ALARMCLOCK = "settings.PREFS_ALARMCLOCK";
    final static String ACTION_PREFS_LOCALE = "settings.PREFS_LOCALE";
    final static String ACTION_PREFS_UI = "settings.PREFS_UI";
    final static String ACTION_PREFS_WIDGETLIST = "settings.PREFS_WIDGETLIST";
    final static String ACTION_PREFS_PLACES = "settings.PREFS_PLACES";

    private Context context;
    private PlacesPrefsFragment.PlacesPrefsBase placesPrefBase = null;
    private String appTheme = null;
    private WidgetSettingsPreferenceHelper helper;

    public SuntimesSettingsActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        //Log.d("DEBUG", "onCreate");
        setResult(RESULT_OK, getResultData());
        context = SuntimesSettingsActivity.this;
        appTheme = AppSettings.loadThemePref(this);
        AppSettings.setTheme(this, appTheme);

        mapLegacyFragments();    // replace any legacy fragment identifiers before calling onCreate
        super.onCreate(icicle);
        initLocale(icicle);
        initLegacyPrefs();

        helper = new WidgetSettingsPreferenceHelper()
        {
            @Override
            public Context getContext() {
                return SuntimesSettingsActivity.this;
            }
            @Override
            public void updateLocale() {
                SuntimesSettingsActivity.this.updateLocale();
            }
            @Override
            public void rebuildActivity() {
                SuntimesSettingsActivity.this.rebuildActivity();
            }
            @Override
            public void setNeedsRecreateFlag() {
                SuntimesSettingsActivity.this.setNeedsRecreateFlag();
            }
        };
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(onChangedNeedingRebuild);
    }

    protected void mapLegacyFragments()
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            Intent intent = getIntent();
            if (intent.hasExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT))
            {
                String fragment = intent.getStringExtra(EXTRA_SHOW_FRAGMENT);
                String legacyPattern = getPackageName() + ".SuntimesSettingsActivity$";
                if (fragment != null && fragment.startsWith(legacyPattern))
                {
                    String pattern = getPackageName() + ".settings.fragments.";
                    intent.putExtra(EXTRA_SHOW_FRAGMENT, fragment.replace(legacyPattern, pattern));
                }
            }
        }
    }

    public Intent getResultData() {
        boolean value = getIntent().getBooleanExtra(SettingsActivityInterface.RECREATE_ACTIVITY, false);
        //Log.d("DEBUG", "getResultData: needsRecreate? " + value);
        return new Intent().putExtra(SettingsActivityInterface.RECREATE_ACTIVITY, value);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Log.d(LOG_TAG, "onActivityResult: " + requestCode + " (" + resultCode + ")");
        switch(requestCode)
        {
            case SettingsActivityInterface.REQUEST_HEADER:
                setResult(RESULT_OK, data);
                break;

            case SettingsActivityInterface.REQUEST_PICKTHEME_DARK:
            case SettingsActivityInterface.REQUEST_PICKTHEME_LIGHT:
                onPickTheme(requestCode, resultCode, data);
                break;

            case SettingsActivityInterface.REQUEST_TAPACTION_CLOCK:
            case SettingsActivityInterface.REQUEST_TAPACTION_DATE0:
            case SettingsActivityInterface.REQUEST_TAPACTION_DATE1:
            case SettingsActivityInterface.REQUEST_TAPACTION_NOTE:
                onPickAction(requestCode, resultCode, data);
                break;

            case SettingsActivityInterface.REQUEST_MANAGE_EVENTS:
                onManageEvents(requestCode, resultCode, data);
                break;

            case SettingsActivityInterface.REQUEST_PICKCOLORS_BRIGHTALARM:
            case SettingsActivityInterface.REQUEST_PICKCOLORS_DARK:
            case SettingsActivityInterface.REQUEST_PICKCOLORS_LIGHT:
                onPickColors(requestCode, resultCode, data);
                break;
        }
    }

    private String prefKeyForRequestCode(int requestCode)
    {
        switch(requestCode)
        {
            case SettingsActivityInterface.REQUEST_PICKTHEME_DARK: return AppSettings.PREF_KEY_APPEARANCE_THEME_DARK;
            case SettingsActivityInterface.REQUEST_PICKTHEME_LIGHT: return AppSettings.PREF_KEY_APPEARANCE_THEME_LIGHT;
            case SettingsActivityInterface.REQUEST_TAPACTION_CLOCK: return AppSettings.PREF_KEY_UI_CLOCKTAPACTION;
            case SettingsActivityInterface.REQUEST_TAPACTION_DATE0: return AppSettings.PREF_KEY_UI_DATETAPACTION;
            case SettingsActivityInterface.REQUEST_TAPACTION_DATE1: return AppSettings.PREF_KEY_UI_DATETAPACTION1;
            case SettingsActivityInterface.REQUEST_TAPACTION_NOTE:  return AppSettings.PREF_KEY_UI_NOTETAPACTION;
            case SettingsActivityInterface.REQUEST_PICKCOLORS_BRIGHTALARM: return AlarmSettings.PREF_KEY_ALARM_BRIGHTMODE_COLORS;
            case SettingsActivityInterface.REQUEST_PICKCOLORS_DARK: case SettingsActivityInterface.REQUEST_PICKCOLORS_LIGHT:
            default: return null;
        }
    }

    private void onPickAction(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK) {

            String selection = data.getStringExtra(ActionListActivity.SELECTED_ACTIONID);
            boolean adapterModified = data.getBooleanExtra(ActionListActivity.ADAPTER_MODIFIED, false);
            //Log.d("onPickAction", "Picked " + selection + " (adapterModified:" + adapterModified + ")");

            if (selection != null)
            {
                String key = prefKeyForRequestCode(requestCode);
                if (key != null)
                {
                    SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    pref.putString(key, selection);
                    pref.apply();
                }
                rebuildActivity();
            }

            if (adapterModified) {
                rebuildActivity();
            }
        }
    }

    private void onPickTheme(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            String selection = data.getStringExtra(SuntimesThemeContract.THEME_NAME);
            boolean adapterModified = data.getBooleanExtra(WidgetThemeListActivity.ADAPTER_MODIFIED, false);
            //Log.d("onPickTheme", "Picked " + selection + " (adapterModified:" + adapterModified + ")");

            if (selection != null)
            {
                String key = prefKeyForRequestCode(requestCode);
                if (key != null)
                {
                    SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    pref.putString(key, selection);
                    pref.apply();
                }
                rebuildActivity();
                Toast.makeText(context, context.getString(R.string.restart_required_message), Toast.LENGTH_LONG).show();

            } else if (adapterModified) {
                rebuildActivity();
            }
        }
    }

    private void onPickColors(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            String selection = data.getStringExtra(ColorValuesSheetActivity.EXTRA_SELECTED_COLORS_ID);
            int appWidgetID = data.getIntExtra(ColorValuesSheetActivity.EXTRA_APPWIDGET_ID, 0);
            String colorTag = data.getStringExtra(ColorValuesSheetActivity.EXTRA_COLORTAG);
            ColorValuesCollection<ColorValues> collection = data.getParcelableExtra(ColorValuesSheetActivity.EXTRA_COLLECTION);
            //Log.d("DEBUG", "onPickColors: " + selection);

            if (collection != null) {
                collection.setSelectedColorsID(context, selection, appWidgetID, colorTag);
            }

            String key = prefKeyForRequestCode(requestCode);
            if (key != null)
            {
                SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                pref.putString(key, selection);
                pref.apply();

            } else {
                rebuildActivity();
            }
        }
    }

    private void onManageEvents(int requestCode, int resultCode, @Nullable Intent data)
    {
        boolean adapterModified = ((data != null) && data.getBooleanExtra(ActionListActivity.ADAPTER_MODIFIED, false));

        if (resultCode == RESULT_OK)
        {
            String eventID = ((data != null) ? data.getStringExtra(EventListActivity.SELECTED_EVENTID) : null);
            if (eventID != null) {
                EventSettings.setShown(context, eventID, true);
                adapterModified = true;
            }
        }

        if (adapterModified) {
            setNeedsRecreateFlag();
            rebuildActivity();
        }
    }

    /**
     * legacy pref api used for pre honeycomb devices, while honeycomb+ uses the fragment based api.
     */
    private void initLegacyPrefs()
    {
        String action = getIntent().getAction();
        if (action != null)
        {
            Log.i(LOG_TAG, "initLegacyPrefs: action: " + action);

            //noinspection IfCanBeSwitch
            if (action.equals(ACTION_PREFS_GENERAL))
            {
                //noinspection deprecation
                addPreferencesFromResource(R.xml.preference_general);
                initPref_general();

            } else if (action.equals(ACTION_PREFS_ALARMCLOCK)) {
                addPreferencesFromResource(R.xml.preference_alarms);
                initPref_alarms();

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
                overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);

            } else {
                Log.w(LOG_TAG, "initLegacyPrefs: unhandled action: " + action);
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
        //Log.d("DEBUG", "onResume");
        initLocale(null);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(helper);

        if (placesPrefBase != null) {
            placesPrefBase.onResume();
        }
    }

    @Override
    public void onPause()
    {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(helper);
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (placesPrefBase != null) {
            placesPrefBase.onStop();
        }
    }

    @Override
    public void onDestroy()
    {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(onChangedNeedingRebuild);
        super.onDestroy();
    }

    private void initLocale(@Nullable Bundle icicle)
    {
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.initDefaults(context);

        AppSettings.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings(context);

        boolean themeChanged = false;
        if (icicle != null)
        {
            String prevTheme = icicle.getString(AppSettings.PREF_KEY_APPEARANCE_THEME);
            if (prevTheme == null) {
                prevTheme = getIntent().getStringExtra(AppSettings.PREF_KEY_APPEARANCE_THEME);
            }
            if (prevTheme == null) {
                prevTheme = appTheme;
            }
            themeChanged = !prevTheme.equals(appTheme);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            if (themeChanged) {
                //Log.d("DEBUG", "theme changed: " + themeChanged);
                invalidateHeaders();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(AppSettings.PREF_KEY_APPEARANCE_THEME, appTheme);
        //Log.d("DEBUG", "onSaveInstanceState: " + appTheme);
    }

    /**
     * forces styled icons on headers
     * @param target the target list to place headers into
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            loadHeadersFromResource(R.xml.preference_headers, target);

            TypedValue typedValue = new TypedValue();
            int[] icActionAttr = new int[] { R.attr.icActionSettings, R.attr.icActionLocale, R.attr.icActionPlace, R.attr.icActionCalendar, R.attr.icActionAppearance, R.attr.icActionWidgets, R.attr.icActionAlarm };
            TypedArray a = obtainStyledAttributes(typedValue.data, icActionAttr);
            int settingsIcon = a.getResourceId(0, R.drawable.ic_action_settings);
            int localeIcon = a.getResourceId(1, R.drawable.ic_action_locale);
            int placesIcon = a.getResourceId(2, R.drawable.ic_action_place);
            int calendarIcon = a.getResourceId(3, R.drawable.ic_calendar);
            int paletteIcon = a.getResourceId(4, R.drawable.ic_palette);
            int widgetIcon = a.getResourceId(5, R.drawable.ic_action_widget);
            int alarmIcon = a.getResourceId(6, R.drawable.ic_action_alarms);
            a.recycle();

            for (Header header : target)
            {
                if (header.iconRes == 0)
                {
                    if (header.fragment != null)
                    {
                        if (header.fragment.endsWith("LocalePrefsFragment")) {
                            header.iconRes = localeIcon;
                        } else if (header.fragment.endsWith("PlacesPrefsFragment")) {
                            header.iconRes = placesIcon;
                        } else if (header.fragment.endsWith("CalendarPrefsFragment")) {
                            header.iconRes = calendarIcon;
                        } else if (header.fragment.endsWith("AlarmPrefsFragment")) {
                            header.iconRes = alarmIcon;
                        } else if (header.fragment.endsWith("UIPrefsFragment")) {
                            header.iconRes = paletteIcon;
                        } else header.iconRes = settingsIcon;
                    } else {
                        if (header.id == R.id.prefHeaderWidgets)
                            header.iconRes = widgetIcon;
                        //else if (header.id == R.id.prefHeaderAlarmClock)
                            //header.iconRes = alarmIcon;
                        //else if (header.id == R.id.prefHeaderCalendar)
                            //header.iconRes = calendarIcon;
                        else header.iconRes = settingsIcon;
                    }
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
               AlarmPrefsFragment.class.getName().equals(fragmentName) ||
               CalendarPrefsFragment.class.getName().equals(fragmentName) ||
               LocalePrefsFragment.class.getName().equals(fragmentName) ||
               UIPrefsFragment.class.getName().equals(fragmentName) ||
               PlacesPrefsFragment.class.getName().equals(fragmentName);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener onChangedNeedingRebuild = new SharedPreferences.OnSharedPreferenceChangeListener()
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if (key.equals(AppSettings.PREF_KEY_NAVIGATION_MODE) || key.equals(AppSettings.PREF_KEY_LOCALE) || key.equals(AppSettings.PREF_KEY_LOCALE_MODE)
                    || key.equals(AppSettings.PREF_KEY_APPEARANCE_THEME) || key.equals(PREF_KEY_APPEARANCE_THEME_DARK) || key.equals(PREF_KEY_APPEARANCE_THEME_LIGHT))
            {
                //Log.d("SettingsActivity", "theme/locale change detected; restarting activity");
                setNeedsRecreateFlag();
                if (key.equals(AppSettings.PREF_KEY_LOCALE) || key.equals(AppSettings.PREF_KEY_LOCALE_MODE)) {
                    updateLocale();
                }
                rebuildActivity();
            }
        }
    };

    public void setNeedsRecreateFlag() {
        //Log.d("DEBUG", "setNeedsRecreateFlag");
        getIntent().putExtra(SettingsActivityInterface.RECREATE_ACTIVITY, true);
        setResult(RESULT_OK, getResultData());
    }

    protected void updateLocale()
    {
        AppSettings.initLocale(this);

        for (Class widgetClass : WidgetListAdapter.ALL_WIDGETS) {
            SuntimesWidget0.triggerWidgetUpdate(this, widgetClass);
        }
    }

    public void rebuildActivity()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            invalidateHeaders();
            recreate();   //  jagged transition (but acts as a configuration change within lifecycle)
            // TODO: smooth transition

        } else {
            finish();
            startActivity(getIntent());  // smooth transition (but does not trigger onSaveInstanceState)
            overridePendingTransition(R.anim.transition_restart_in, R.anim.transition_restart_out);
        }
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * init legacy prefs
     */
    private void initPref_general()
    {
        Log.i(LOG_TAG, "initPref_general (legacy)");

        String key_timeFormat = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + WidgetSettings.PREF_KEY_APPEARANCE_TIMEFORMATMODE;
        ListPreference timeformatPref = (ListPreference)findPreference(key_timeFormat);
        GeneralPrefsFragment.initPref_timeFormat(this, timeformatPref);

        String key_altitudePref = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_LOCATION + WidgetSettings.PREF_KEY_LOCATION_ALTITUDE_ENABLED;
        CheckBoxPreference altitudePref = (CheckBoxPreference)findPreference(key_altitudePref);
        GeneralPrefsFragment.initPref_altitude(this, altitudePref);

        String key_sunCalc = WidgetSettings.keyCalculatorModePref(0);
        //noinspection deprecation
        SummaryListPreference sunCalculatorPref = (SummaryListPreference)findPreference(key_sunCalc);
        if (sunCalculatorPref != null)
        {
            GeneralPrefsFragment.initPref_calculator(this, sunCalculatorPref, WidgetSettings.PREF_DEF_GENERAL_CALCULATOR);
            GeneralPrefsFragment.loadPref_calculator(this, sunCalculatorPref);
        }

        String key_moonCalc = WidgetSettings.keyCalculatorModePref(0, "moon");
        //noinspection deprecation
        SummaryListPreference moonCalculatorPref = (SummaryListPreference)findPreference(key_moonCalc);
        if (moonCalculatorPref != null)
        {
            GeneralPrefsFragment.initPref_calculator(this, moonCalculatorPref, new int[] {SuntimesCalculator.FEATURE_MOON}, WidgetSettings.PREF_DEF_GENERAL_CALCULATOR_MOON);
            GeneralPrefsFragment.loadPref_calculator(this, moonCalculatorPref,"moon");
        }
    }

    /**
     * init legacy prefs
     */
    private void initPref_locale()
    {
        Preference localeModePref = findPreference(AppSettings.PREF_KEY_LOCALE_MODE);
        ListPreference localePref = (ListPreference) findPreference(AppSettings.PREF_KEY_LOCALE);
        LocalePrefsFragment.initPref_locale(this, localeModePref, localePref);
    }

    /**
     * init legacy prefs
     */
    private void initPref_places()
    {
        //noinspection deprecation
        Preference managePlacesPref = findPreference("places_manage");
        //noinspection deprecation
        Preference buildPlacesPref = findPreference("places_build");
        //noinspection deprecation
        Preference clearPlacesPref = findPreference("places_clear");
        //noinspection deprecation
        Preference exportPlacesPref = findPreference("places_export");
        placesPrefBase = new PlacesPrefsFragment.PlacesPrefsBase(this, managePlacesPref, buildPlacesPref, clearPlacesPref, exportPlacesPref);
    }

    /**
     * init legacy prefs
     */
    private void initPref_ui()
    {
        boolean[] showFields = AppSettings.loadShowFieldsPref(this);
        for (int i = 0; i<AppSettings.NUM_FIELDS; i++)
        {
            CheckBoxPreference field = (CheckBoxPreference)findPreference(AppSettings.PREF_KEY_UI_SHOWFIELDS + "_" + i);
            if (field != null) {
                UIPrefsFragment.initPref_ui_field(field, this, i, showFields[i]);
            }
        }

        // TODO: tapActions

        ActionButtonPreference overrideTheme_light = (ActionButtonPreference)findPreference(PREF_KEY_APPEARANCE_THEME_LIGHT);
        UIPrefsFragment.initPref_ui_themeOverride(this, overrideTheme_light, PREF_KEY_APPEARANCE_THEME_LIGHT);
        UIPrefsFragment.loadPref_ui_themeOverride(this, overrideTheme_light, PREF_KEY_APPEARANCE_THEME_LIGHT);

        ActionButtonPreference overrideTheme_dark = (ActionButtonPreference)findPreference(AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);
        UIPrefsFragment.initPref_ui_themeOverride(this, overrideTheme_dark, AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);
        UIPrefsFragment.loadPref_ui_themeOverride(this, overrideTheme_dark, AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);

        UIPrefsFragment.updatePref_ui_themeOverride(AppSettings.loadThemePref(this), overrideTheme_dark, overrideTheme_light);

        String key_observerHeight = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_GENERAL + WidgetSettings.PREF_KEY_GENERAL_OBSERVERHEIGHT;
        LengthPreference observerHeightPref = (LengthPreference) findPreference(key_observerHeight);
        if (observerHeightPref != null)
        {
            UIPrefsFragment.initPref_observerHeight(this, observerHeightPref);
            UIPrefsFragment.loadPref_observerHeight(this, observerHeightPref);
        }

        Preference manage_events = findPreference("manage_events");
        if (manage_events != null) {
            manage_events.setOnPreferenceClickListener(UIPrefsFragment.getOnManageEventsClickedListener(SuntimesSettingsActivity.this));
        }

        PreferenceCategory category = (PreferenceCategory) findPreference("custom_events");
        UIPrefsFragment.initPref_ui_customevents(this, category);
    }

    /**
     * init legacy prefs
     */
    private void initPref_alarms()
    {
        Preference batteryOptimization = findPreference(AlarmSettings.PREF_KEY_ALARM_BATTERYOPT);
        PreferenceCategory alarmsCategory = (PreferenceCategory)findPreference(AlarmSettings.PREF_KEY_ALARM_CATEGORY);
        AlarmPrefsFragment.removePrefFromCategory(batteryOptimization, alarmsCategory);

        Preference autostart = findPreference(AlarmSettings.PREF_KEY_ALARM_AUTOSTART);
        AlarmPrefsFragment.removePrefFromCategory(autostart, alarmsCategory);

        Preference dndPermission = findPreference(AlarmSettings.PREF_KEY_ALARM_DND_PERMISSION);
        PreferenceCategory bedtimeCategory = (PreferenceCategory)findPreference(BedtimeSettings.PREF_KEY_BEDTIME_CATEGORY);
        AlarmPrefsFragment.removePrefFromCategory(dndPermission, bedtimeCategory);
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
    }

    @Override
    public void onHeaderClick(PreferenceActivity.Header header, int position)
    {
        super.onHeaderClick(header, position);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    @TargetApi(14)
    @Override
    public void startWithFragment(String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, @StringRes int titleRes, @StringRes int shortTitleRes)
    {
        Intent intent = onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);
        if (resultTo != null) {
            resultTo.startActivityForResult(intent, resultRequestCode);
        } else {
            startActivityForResult(intent, SettingsActivityInterface.REQUEST_HEADER);
        }
    }

}
