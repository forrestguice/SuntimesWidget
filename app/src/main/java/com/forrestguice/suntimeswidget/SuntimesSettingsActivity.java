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

package com.forrestguice.suntimeswidget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.actions.ActionListActivity;
import com.forrestguice.suntimeswidget.actions.LoadActionDialog;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.CalculatorProvider;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.events.EventListActivity;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.getfix.BuildPlacesTask;
import com.forrestguice.suntimeswidget.getfix.ExportPlacesTask;
import com.forrestguice.suntimeswidget.getfix.PlacesActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.LengthPreference;
import com.forrestguice.suntimeswidget.settings.SummaryListPreference;
import com.forrestguice.suntimeswidget.settings.ActionButtonPreference;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.themes.SuntimesThemeContract;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_DEF_UI_CLOCKTAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_DEF_UI_DATETAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_DEF_UI_DATETAPACTION1;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_DEF_UI_NOTETAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_APPEARANCE_THEME_DARK;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_APPEARANCE_THEME_LIGHT;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_UI_CLOCKTAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_UI_DATETAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_UI_DATETAPACTION1;
import static com.forrestguice.suntimeswidget.settings.AppSettings.PREF_KEY_UI_NOTETAPACTION;
import static com.forrestguice.suntimeswidget.settings.AppSettings.findPermission;

/**
 * A preferences activity for the main app;
 * @see SuntimesConfigActivity0 for widget configuration.
 */
public class SuntimesSettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String LOG_TAG = "SuntimesSettings";

    final static String ACTION_PREFS_GENERAL = "com.forrestguice.suntimeswidget.PREFS_GENERAL";
    final static String ACTION_PREFS_ALARMCLOCK = "com.forrestguice.suntimeswidget.PREFS_ALARMCLOCK";
    final static String ACTION_PREFS_LOCALE = "com.forrestguice.suntimeswidget.PREFS_LOCALE";
    final static String ACTION_PREFS_UI = "com.forrestguice.suntimeswidget.PREFS_UI";
    final static String ACTION_PREFS_WIDGETLIST = "com.forrestguice.suntimeswidget.PREFS_WIDGETLIST";
    final static String ACTION_PREFS_PLACES = "com.forrestguice.suntimeswidget.PREFS_PLACES";

    public static String calendarPackage = "com.forrestguice.suntimescalendars";
    public static String calendarActivity = "com.forrestguice.suntimeswidget.calendar.SuntimesCalendarActivity";

    public static final int REQUEST_PICKTHEME_LIGHT = 20;
    public static final int REQUEST_PICKTHEME_DARK = 30;
    public static final int REQUEST_TAPACTION_CLOCK = 40;
    public static final int REQUEST_TAPACTION_DATE0 = 50;
    public static final int REQUEST_TAPACTION_DATE1 = 60;
    public static final int REQUEST_TAPACTION_NOTE = 70;
    public static final int REQUEST_MANAGE_EVENTS = 80;

    public static final String RECREATE_ACTIVITY = "recreate_activity";

    private Context context;
    private PlacesPrefsBase placesPrefBase = null;
    private String appTheme = null;

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
        setResult(RESULT_OK, getResultData());
        context = SuntimesSettingsActivity.this;
        appTheme = AppSettings.loadThemePref(this);
        AppSettings.setTheme(this, appTheme);

        super.onCreate(icicle);
        initLocale(icicle);
        initLegacyPrefs();

        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(onChangedNeedingRebuild);
    }

    public Intent getResultData() {
        return new Intent().putExtra(RECREATE_ACTIVITY, getIntent().getBooleanExtra(RECREATE_ACTIVITY, false));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(LOG_TAG, "onActivityResult: " + requestCode + " (" + resultCode + ")");
        switch(requestCode)
        {
            case REQUEST_PICKTHEME_DARK:
            case REQUEST_PICKTHEME_LIGHT:
                onPickTheme(requestCode, resultCode, data);
                break;

            case REQUEST_TAPACTION_CLOCK:
            case REQUEST_TAPACTION_DATE0:
            case REQUEST_TAPACTION_DATE1:
            case REQUEST_TAPACTION_NOTE:
                onPickAction(requestCode, resultCode, data);
                break;

            case REQUEST_MANAGE_EVENTS:
                onManageEvents(requestCode, resultCode, data);
                break;
        }
    }

    private String prefKeyForRequestCode(int requestCode)
    {
        switch(requestCode)
        {
            case REQUEST_PICKTHEME_DARK:  return AppSettings.PREF_KEY_APPEARANCE_THEME_DARK;
            case REQUEST_PICKTHEME_LIGHT: return AppSettings.PREF_KEY_APPEARANCE_THEME_LIGHT;
            case REQUEST_TAPACTION_CLOCK: return AppSettings.PREF_KEY_UI_CLOCKTAPACTION;
            case REQUEST_TAPACTION_DATE0: return AppSettings.PREF_KEY_UI_DATETAPACTION;
            case REQUEST_TAPACTION_DATE1: return AppSettings.PREF_KEY_UI_DATETAPACTION1;
            case REQUEST_TAPACTION_NOTE:  return AppSettings.PREF_KEY_UI_NOTETAPACTION;
            default: return null;
        }
    }

    private void onPickAction(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK) {

            String selection = data.getStringExtra(ActionListActivity.SELECTED_ACTIONID);
            boolean adapterModified = data.getBooleanExtra(ActionListActivity.ADAPTER_MODIFIED, false);
            Log.d("onPickAction", "Picked " + selection + " (adapterModified:" + adapterModified + ")");

            if (selection != null)
            {
                SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                pref.putString(prefKeyForRequestCode(requestCode), selection);
                pref.apply();
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
            Log.d("onPickTheme", "Picked " + selection + " (adapterModified:" + adapterModified + ")");

            if (selection != null)
            {
                SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
                pref.putString(prefKeyForRequestCode(requestCode), selection);
                pref.apply();
                rebuildActivity();
                Toast.makeText(context, context.getString(R.string.restart_required_message), Toast.LENGTH_LONG).show();

            } else if (adapterModified) {
                rebuildActivity();
            }
        }
    }

    private void onManageEvents(int requestCode, int resultCode, Intent data)
    {
        boolean adapterModified = data.getBooleanExtra(ActionListActivity.ADAPTER_MODIFIED, false);

        if (resultCode == RESULT_OK)
        {
            String eventID = data.getStringExtra(EventListActivity.SELECTED_EVENTID);
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
                Log.d("DEBUG", "theme changed: " + themeChanged);
                invalidateHeaders();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(AppSettings.PREF_KEY_APPEARANCE_THEME, appTheme);
        Log.d("DEBUG", "onSaveInstanceState: " + appTheme);
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

    private SharedPreferences.OnSharedPreferenceChangeListener onChangedNeedingRebuild = new SharedPreferences.OnSharedPreferenceChangeListener()
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if (key.equals(AppSettings.PREF_KEY_LOCALE) || key.equals(AppSettings.PREF_KEY_LOCALE_MODE)
                    || key.equals(AppSettings.PREF_KEY_APPEARANCE_THEME))
            {
                //Log.d("SettingsActivity", "Locale change detected; restarting activity");
                setNeedsRecreateFlag();
                updateLocale();
                rebuildActivity();
            }
        }
    };

    public void setNeedsRecreateFlag() {
        getIntent().putExtra(RECREATE_ACTIVITY, true);
        setResult(RESULT_OK, getResultData());
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.i(LOG_TAG, "onSharedPreferenceChanged: key: " + key);

        if (key.endsWith(AppSettings.PREF_KEY_PLUGINS_ENABLESCAN))
        {
            SuntimesCalculatorDescriptor.reinitCalculators(this);
            rebuildActivity();
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_CALCULATOR))
        {
            try {
                // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
                // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
                String calcName = sharedPreferences.getString(key, null);
                SuntimesCalculatorDescriptor descriptor = SuntimesCalculatorDescriptor.valueOf(this, calcName);
                WidgetSettings.saveCalculatorModePref(this, 0, descriptor);
                CalculatorProvider.clearCachedConfig(0);
                Log.i(LOG_TAG, "onSharedPreferenceChanged: value: " + calcName + " :: " + descriptor);

            } catch (InvalidParameterException e) {
                Log.e(LOG_TAG, "onPreferenceChanged: Failed to persist sun calculator pref! " + e);
            }
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_CALCULATOR + "_moon"))
        {
            try {
                // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
                // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
                String calcName = sharedPreferences.getString(key, null);
                SuntimesCalculatorDescriptor descriptor = SuntimesCalculatorDescriptor.valueOf(this, calcName);
                WidgetSettings.saveCalculatorModePref(this, 0, "moon", descriptor);
                CalculatorProvider.clearCachedConfig(0);
                Log.i(LOG_TAG, "onSharedPreferenceChanged: value: " + calcName + " :: " + descriptor);

            } catch (InvalidParameterException e) {
                Log.e(LOG_TAG, "onPreferenceChanged: Failed to persist moon calculator pref! " + e);
            }
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_LOCATION_ALTITUDE_ENABLED))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveLocationAltitudeEnabledPref(this, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_LOCATION_ALTITUDE_ENABLED));
            CalculatorProvider.clearCachedConfig(0);
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_APPEARANCE_TIMEFORMATMODE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveTimeFormatModePref(this, 0, WidgetSettings.TimeFormatMode.valueOf(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_APPEARANCE_TIMEFORMATMODE.name())));
            updateLocale();
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_TRACKINGMODE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveTrackingModePref(this, 0, WidgetSettings.TrackingMode.valueOf(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_GENERAL_TRACKINGMODE.name())));
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

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_LOCALIZE_HEMISPHERE))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveLocalizeHemispherePref(this, 0, sharedPreferences.getBoolean(key, WidgetSettings.PREF_DEF_GENERAL_LOCALIZE_HEMISPHERE));
            return;
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_OBSERVERHEIGHT))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            try {
                WidgetSettings.saveObserverHeightPref(this, 0,
                        Float.parseFloat(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_GENERAL_OBSERVERHEIGHT + "")));
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "onPreferenceChangeD: Failed to persist observerHeight: bad value!" + e);
            }
        }

        if (key.endsWith(WidgetSettings.PREF_KEY_GENERAL_UNITS_LENGTH))
        {
            // the pref activity saves to: com.forrestguice.suntimeswidget_preferences.xml,
            // ...but this is a widget setting (belongs in com.forrestguice.suntimeswidget.xml)
            WidgetSettings.saveLengthUnitsPref(this, 0, WidgetSettings.getLengthUnit(sharedPreferences.getString(key, WidgetSettings.PREF_DEF_GENERAL_UNITS_LENGTH.name())));
            rebuildActivity();
            return;
        }

        if (key.endsWith(AppSettings.PREF_KEY_UI_EMPHASIZEFIELD))
        {
            setNeedsRecreateFlag();
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
     * General Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPrefsFragment extends PreferenceFragment
    {
        private SummaryListPreference sunCalculatorPref, moonCalculatorPref;
        private CheckBoxPreference useAltitudePref;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            AppSettings.initLocale(getActivity());
            Log.i(LOG_TAG, "GeneralPrefsFragment: Arguments: " + getArguments());

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
        Log.i(LOG_TAG, "initPref_general (legacy)");

        String key_timeFormat = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_APPEARANCE + WidgetSettings.PREF_KEY_APPEARANCE_TIMEFORMATMODE;
        ListPreference timeformatPref = (ListPreference)findPreference(key_timeFormat);
        initPref_timeFormat(this, timeformatPref);

        String key_altitudePref = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_LOCATION + WidgetSettings.PREF_KEY_LOCATION_ALTITUDE_ENABLED;
        CheckBoxPreference altitudePref = (CheckBoxPreference)findPreference(key_altitudePref);
        initPref_altitude(this, altitudePref);

        String key_sunCalc = WidgetSettings.keyCalculatorModePref(0);
        //noinspection deprecation
        SummaryListPreference sunCalculatorPref = (SummaryListPreference)findPreference(key_sunCalc);
        if (sunCalculatorPref != null)
        {
            initPref_calculator(this, sunCalculatorPref, WidgetSettings.PREF_DEF_GENERAL_CALCULATOR);
            loadPref_calculator(this, sunCalculatorPref);
        }

        String key_moonCalc = WidgetSettings.keyCalculatorModePref(0, "moon");
        //noinspection deprecation
        SummaryListPreference moonCalculatorPref = (SummaryListPreference)findPreference(key_moonCalc);
        if (moonCalculatorPref != null)
        {
            initPref_calculator(this, moonCalculatorPref, new int[] {SuntimesCalculator.FEATURE_MOON}, WidgetSettings.PREF_DEF_GENERAL_CALCULATOR_MOON);
            loadPref_calculator(this, moonCalculatorPref,"moon");
        }

        String key_observerHeight = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_GENERAL + WidgetSettings.PREF_KEY_GENERAL_OBSERVERHEIGHT;
        LengthPreference observerHeightPref = (LengthPreference) findPreference(key_observerHeight);
        if (observerHeightPref != null)
        {
            initPref_observerHeight(this, observerHeightPref);
            loadPref_observerHeight(this, observerHeightPref);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_general(PreferenceFragment fragment)
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

        String key_observerHeight = WidgetSettings.PREF_PREFIX_KEY + "0" + WidgetSettings.PREF_PREFIX_KEY_GENERAL + WidgetSettings.PREF_KEY_GENERAL_OBSERVERHEIGHT;
        LengthPreference observerHeightPref = (LengthPreference) fragment.findPreference(key_observerHeight);
        if (observerHeightPref != null)
        {
            initPref_observerHeight(fragment.getActivity(), observerHeightPref);
            loadPref_observerHeight(fragment.getActivity(), observerHeightPref);
        }
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * Calendar Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CalendarPrefsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            Intent calendarIntent = new Intent();
            calendarIntent.setComponent(new ComponentName(calendarPackage, calendarActivity));
            calendarIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                startActivity(calendarIntent);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
                return;

            } catch (Exception e) {
                Log.e("CalendarPrefs", "Unable to launch SuntimesCalendarActivity! " + e);
            }

            AppSettings.initLocale(getActivity());
            addPreferencesFromResource(R.xml.preference_calendar);
            Preference calendarReadme = findPreference("appwidget_0_calendars_readme");
            if (calendarReadme != null)
            {
                calendarReadme.setSummary(SuntimesUtils.fromHtml(getString(R.string.help_calendar)));
                calendarReadme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        Activity activity = getActivity();
                        if (activity != null) {
                            AboutDialog.openLink(activity, AboutDialog.ADDONS_URL);
                            activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
                        }
                        return false;
                    }
                });
            }
            Log.i(LOG_TAG, "CalendarPrefsFragment: Arguments: " + getArguments());
        }
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
            Log.i(LOG_TAG, "LocalePrefsFragment: Arguments: " + getArguments());

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
        ListPreference localePref = (ListPreference)fragment.findPreference(AppSettings.PREF_KEY_LOCALE);
        initPref_locale(fragment.getActivity(), localePref);
    }
    private static void initPref_locale(Activity activity, ListPreference localePref)
    {
        final String[] localeDisplay = activity.getResources().getStringArray(R.array.locale_display);
        final String[] localeDisplayNative = activity.getResources().getStringArray(R.array.locale_display_native);
        final String[] localeValues = activity.getResources().getStringArray(R.array.locale_values);

        Integer[] index = getSortedOrder(localeDisplayNative);
        CharSequence[] entries = new CharSequence[localeValues.length];
        CharSequence[] values = new CharSequence[localeValues.length];
        for (int i=0; i<index.length; i++)
        {
            int j = index[i];
            CharSequence formattedDisplayString;
            CharSequence localeDisplay_j = (localeDisplay.length > j ? localeDisplay[j] : localeValues[j]);
            CharSequence localeDisplayNative_j = (localeDisplayNative.length > j ? localeDisplayNative[j] : localeValues[j]);

            if (localeDisplay_j.equals(localeDisplayNative_j)) {
                formattedDisplayString = localeDisplayNative_j;

            } else {
                String localizedName = "(" + localeDisplay_j + ")";
                String displayString = localeDisplayNative_j + " " + localizedName;
                formattedDisplayString = SuntimesUtils.createRelativeSpan(null, displayString, localizedName, 0.7f);
            }

            entries[i] = formattedDisplayString;
            values[i] = localeValues[j];
        }

        localePref.setEntries(entries);
        localePref.setEntryValues(values);

        AppSettings.LocaleMode localeMode = AppSettings.loadLocaleModePref(activity);
        localePref.setEnabled(localeMode == AppSettings.LocaleMode.CUSTOM_LOCALE);
    }

    /**
     * @param stringArray array to perform sort on
     * @return a sorted array of indices pointing into stringArray
     */
    private static Integer[] getSortedOrder(final String[] stringArray)
    {
        Integer[] index = new Integer[stringArray.length];
        for (int i=0; i < index.length; i++)
        {
            index[i] = i;
        }
        Arrays.sort(index, new Comparator<Integer>()
        {
            public int compare(Integer i1, Integer i2)
            {
                return stringArray[i1].compareTo(stringArray[i2]);
            }
        });
        return index;
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
            Log.i(LOG_TAG, "PlacesPrefsFragment: Arguments: " + getArguments());
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
    }

    /**
     * Places Prefs - Base
     */
    private static class PlacesPrefsBase
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
        private Preference.OnPreferenceClickListener onClickManagePlaces = new Preference.OnPreferenceClickListener()
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
        private BuildPlacesTask.TaskListener clearPlacesListener = new BuildPlacesTask.TaskListener()
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
        Preference managePlacesPref = findPreference("places_manage");
        //noinspection deprecation
        Preference buildPlacesPref = findPreference("places_build");
        //noinspection deprecation
        Preference clearPlacesPref = findPreference("places_clear");
        //noinspection deprecation
        Preference exportPlacesPref = findPreference("places_export");
        placesPrefBase = new PlacesPrefsBase(this, managePlacesPref, buildPlacesPref, clearPlacesPref, exportPlacesPref);
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
            Log.i(LOG_TAG, "UIPrefsFragment: Arguments: " + getArguments());

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
        boolean[] showFields = AppSettings.loadShowFieldsPref(this);
        for (int i = 0; i<AppSettings.NUM_FIELDS; i++)
        {
            CheckBoxPreference field = (CheckBoxPreference)findPreference(AppSettings.PREF_KEY_UI_SHOWFIELDS + "_" + i);
            if (field != null) {
                initPref_ui_field(field, this, i, showFields[i]);
            }
        }

        // TODO: tapActions

        ActionButtonPreference overrideTheme_light = (ActionButtonPreference)findPreference(PREF_KEY_APPEARANCE_THEME_LIGHT);
        initPref_ui_themeOverride(this, overrideTheme_light, PREF_KEY_APPEARANCE_THEME_LIGHT);
        loadPref_ui_themeOverride(this, overrideTheme_light, PREF_KEY_APPEARANCE_THEME_LIGHT);

        ActionButtonPreference overrideTheme_dark = (ActionButtonPreference)findPreference(AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);
        initPref_ui_themeOverride(this, overrideTheme_dark, AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);
        loadPref_ui_themeOverride(this, overrideTheme_dark, AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);

        updatePref_ui_themeOverride(AppSettings.loadThemePref(this), overrideTheme_dark, overrideTheme_light);

        Preference manage_events = findPreference("manage_events");
        if (manage_events != null) {
            manage_events.setOnPreferenceClickListener(getOnManageEventsClickedListener(SuntimesSettingsActivity.this));
        }

        PreferenceCategory category = (PreferenceCategory) findPreference("custom_events");
        initPref_ui_customevents(this, category);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_ui(final PreferenceFragment fragment)
    {
        boolean[] showFields = AppSettings.loadShowFieldsPref(fragment.getActivity());
        for (int i = 0; i<AppSettings.NUM_FIELDS; i++)
        {
            CheckBoxPreference field = (CheckBoxPreference)fragment.findPreference(AppSettings.PREF_KEY_UI_SHOWFIELDS + "_" + i);
            if (field != null) {
                initPref_ui_field(field, fragment.getActivity(), i, showFields[i]);
            }
        }

        Activity activity = fragment.getActivity();

        final ActionButtonPreference tapAction_clock = (ActionButtonPreference)fragment.findPreference(PREF_KEY_UI_CLOCKTAPACTION);
        initPref_ui_tapAction(activity, tapAction_clock, PREF_KEY_UI_CLOCKTAPACTION);
        loadPref_ui_tapAction(activity, tapAction_clock, PREF_KEY_UI_CLOCKTAPACTION, PREF_DEF_UI_CLOCKTAPACTION, REQUEST_TAPACTION_CLOCK);

        final ActionButtonPreference tapAction_date0 = (ActionButtonPreference)fragment.findPreference(PREF_KEY_UI_DATETAPACTION);
        initPref_ui_tapAction(activity, tapAction_date0, PREF_KEY_UI_DATETAPACTION);
        loadPref_ui_tapAction(activity, tapAction_date0, PREF_KEY_UI_DATETAPACTION, PREF_DEF_UI_DATETAPACTION, REQUEST_TAPACTION_DATE0);

        final ActionButtonPreference tapAction_date1 = (ActionButtonPreference)fragment.findPreference(PREF_KEY_UI_DATETAPACTION1);
        initPref_ui_tapAction(activity, tapAction_date1, PREF_KEY_UI_DATETAPACTION1);
        loadPref_ui_tapAction(activity, tapAction_date1, PREF_KEY_UI_DATETAPACTION1, PREF_DEF_UI_DATETAPACTION1, REQUEST_TAPACTION_DATE1);

        final ActionButtonPreference tapAction_note = (ActionButtonPreference)fragment.findPreference(PREF_KEY_UI_NOTETAPACTION);
        initPref_ui_tapAction(activity, tapAction_note,  PREF_KEY_UI_NOTETAPACTION);
        loadPref_ui_tapAction(activity, tapAction_note,  PREF_KEY_UI_NOTETAPACTION, PREF_DEF_UI_NOTETAPACTION, REQUEST_TAPACTION_NOTE);

        final ActionButtonPreference overrideTheme_light = (ActionButtonPreference)fragment.findPreference(PREF_KEY_APPEARANCE_THEME_LIGHT);
        initPref_ui_themeOverride(activity, overrideTheme_light, PREF_KEY_APPEARANCE_THEME_LIGHT);
        loadPref_ui_themeOverride(activity, overrideTheme_light, PREF_KEY_APPEARANCE_THEME_LIGHT);

        final ActionButtonPreference overrideTheme_dark = (ActionButtonPreference)fragment.findPreference(AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);
        initPref_ui_themeOverride(activity, overrideTheme_dark, AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);
        loadPref_ui_themeOverride(activity, overrideTheme_dark, AppSettings.PREF_KEY_APPEARANCE_THEME_DARK);

        Preference manage_events = fragment.findPreference("manage_events");
        if (manage_events != null) {
            manage_events.setOnPreferenceClickListener(getOnManageEventsClickedListener(fragment.getActivity()));
            manage_events.setOrder(-91);
        }

        PreferenceCategory category = (PreferenceCategory) fragment.findPreference("custom_events");
        initPref_ui_customevents((SuntimesSettingsActivity) activity, category);

        updatePref_ui_themeOverride(AppSettings.loadThemePref(activity), overrideTheme_dark, overrideTheme_light);
    }

    private static void initPref_ui_customevents(final SuntimesSettingsActivity context, final PreferenceCategory category)
    {
        ArrayList<Preference> eventPrefs = new ArrayList<>();
        for (final String eventID : EventSettings.loadVisibleEvents(context, AlarmEventProvider.EventType.SUN_ELEVATION))
        {
            EventSettings.EventAlias alias = EventSettings.loadEvent(context, eventID);
            AlarmEventProvider.SunElevationEvent event = AlarmEventProvider.SunElevationEvent.valueOf(Uri.parse(alias.getUri()).getLastPathSegment());

            final CheckBoxPreference pref = new CheckBoxPreference(context);
            pref.setKey(AppSettings.PREF_KEY_UI_SHOWFIELDS + "_" + eventID);
            pref.setOrder((event != null ? event.getAngle() : 0));
            pref.setTitle(alias.getLabel());
            pref.setSummary(alias.getSummary(context));
            pref.setPersistent(false);
            pref.setChecked(true);
            pref.setOnPreferenceChangeListener(customEventListener(context, eventID, category, pref));
            eventPrefs.add(pref);
        }

        boolean sortByName = false;    // TODO: optional
        Collections.sort(eventPrefs, new Comparator<Preference>() {
            @Override
            public int compare(Preference o1, Preference o2) {
                return o1.getTitle().toString().compareTo(o2.getTitle().toString());
            }
        });
        for (int i=0; i<eventPrefs.size(); i++) {
            Preference p = eventPrefs.get(i);
            if (sortByName) {
                p.setOrder(i+1);
            }
            category.addPreference(p);
        }
    }

    protected static Preference.OnPreferenceChangeListener customEventListener(final SuntimesSettingsActivity context, final String eventID, final PreferenceCategory category, final CheckBoxPreference pref)
    {
        return new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                if (!checked)
                {
                    AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                            .setCancelable(false)
                            .setMessage(context.getString(R.string.editevent_dialog_showevent_off))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    EventSettings.setShown(context, eventID, false);
                                    category.removePreference(pref);
                                    context.setNeedsRecreateFlag();
                                }
                            })
                            .setNegativeButton(context.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pref.setChecked(true);
                                }
                            });
                    confirm.show();
                }
                return true;
            }
        };
    }

    private static void initPref_ui_field(CheckBoxPreference field, final Context context, final int k, boolean value)
    {
        field.setChecked(value);
        field.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o)
            {
                if (context != null) {
                    AppSettings.saveShowFieldsPref(context, k, (Boolean) o);
                    return true;
                } else return false;
            }
        });
    }

    public static Preference.OnPreferenceClickListener getOnManageEventsClickedListener(final Activity activity)
    {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                activity.startActivityForResult(new Intent(activity, EventListActivity.class), REQUEST_MANAGE_EVENTS);
                activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
                return false;
            }
        };
    }

    private static Preference.OnPreferenceChangeListener onOverrideThemeChanged(final Activity activity, final ActionButtonPreference overridePref, final int requestCode)
    {
        return new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                overridePref.setActionButtonPreferenceListener(createThemeListPreferenceListener(activity, (String)newValue, requestCode));
                Toast.makeText(activity, activity.getString(R.string.restart_required_message), Toast.LENGTH_LONG).show();
                return true;
            }
        };
    }

    private static ActionButtonPreference.ActionButtonPreferenceListener createThemeListPreferenceListener(final Activity activity, final String selectedTheme, final int requestCode)
    {
        return new ActionButtonPreference.ActionButtonPreferenceListener()
        {
            @Override
            public void onActionButtonClicked()
            {
                Intent configThemesIntent = new Intent(activity, WidgetThemeListActivity.class);
                configThemesIntent.putExtra(WidgetThemeListActivity.PARAM_NOSELECT, false);
                configThemesIntent.putExtra(WidgetThemeListActivity.PARAM_SELECTED, selectedTheme);
                activity.startActivityForResult(configThemesIntent, requestCode);
                activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
            }
        };
    }

    private static void initPref_ui_themeOverride(Activity activity, ActionButtonPreference listPref, String key) {
        initPref_ui_themeOverride(activity, listPref, key, null);
    }
    private static void initPref_ui_themeOverride(Activity activity, ActionButtonPreference listPref, String key, @Nullable String mustIncludeTheme)
    {
        if (listPref != null)
        {
            WidgetThemes.initThemes(activity);
            List<SuntimesTheme.ThemeDescriptor> themes0 = WidgetThemes.getSortedValues(true);
            ArrayList<SuntimesTheme.ThemeDescriptor> themes = new ArrayList<>();

            for (SuntimesTheme.ThemeDescriptor theme : themes0)
            {
                if (!theme.isDefault() || theme.name().equals(mustIncludeTheme)) {
                    themes.add(theme);    // hide default widget themes, show only user-created themes
                }                            // this is a workaround - the defaults have tiny (unreadable) font sizes, so we won't advertise their use
            }

            String[] themeEntries = new String[themes.size() + 1];
            String[] themeValues = new String[themes.size() + 1];

            themeValues[0] = "default";
            themeEntries[0] = activity.getString(R.string.configLabel_tagDefault);
            for (int i=0; i<themes.size(); i++)                // i:0 is reserved for "default"
            {
                themeValues[i + 1] = themes.get(i).name();
                themeEntries[i + 1] = themes.get(i).displayString();
            }

            listPref.setEntries(themeEntries);
            listPref.setEntryValues(themeValues);
        }
    }

    private static void loadPref_ui_themeOverride(Activity activity, ActionButtonPreference listPref, String key)
    {
        if (listPref != null)
        {
            boolean isLightTheme = key.equals(PREF_KEY_APPEARANCE_THEME_LIGHT);
            String themeName = ((isLightTheme ? AppSettings.loadThemeLightPref(activity) : AppSettings.loadThemeDarkPref(activity)));
            int requestCode = (isLightTheme ? REQUEST_PICKTHEME_LIGHT : REQUEST_PICKTHEME_DARK);

            int currentIndex = ((themeName != null) ? listPref.findIndexOfValue(themeName) : -1);
            if (currentIndex >= 0)
            {
                listPref.setValueIndex(currentIndex);
                listPref.setActionButtonPreferenceListener(createThemeListPreferenceListener(activity, themeName, requestCode));
                listPref.setOnPreferenceChangeListener(onOverrideThemeChanged(activity, listPref, requestCode));

            } else {
                if (WidgetThemes.valueOf(themeName) != null) {    // the theme exists but is missing from the list; reload the adapter
                    initPref_ui_themeOverride(activity, listPref, key, themeName);   // it mustInclude: themeName
                    loadPref_ui_themeOverride(activity, listPref, key);    // !! potential for recursive loop if initPref_ui_themeOverride fails to include themeName

                } else {
                    Log.w(LOG_TAG, "loadPref: Unable to load " + key + "... The list is missing an entry for the descriptor: " + themeName);
                    listPref.setValueIndex(0);
                    listPref.setActionButtonPreferenceListener(createThemeListPreferenceListener(activity, themeName, requestCode));
                    listPref.setOnPreferenceChangeListener(onOverrideThemeChanged(activity, listPref, requestCode));
                }
            }
        }
    }

    private static void updatePref_ui_themeOverride(String mode, ListPreference darkPref, ListPreference lightPref)
    {
        darkPref.setEnabled(AppSettings.THEME_DARK.equals(mode) || AppSettings.THEME_DAYNIGHT.equals(mode) || AppSettings.THEME_SYSTEM.equals(mode));
        lightPref.setEnabled(AppSettings.THEME_LIGHT.equals(mode) || AppSettings.THEME_DAYNIGHT.equals(mode) || AppSettings.THEME_SYSTEM.equals(mode));
    }

    /**
     * initPref_ui_tapAction
     */
    private static void initPref_ui_tapAction(Activity activity, ActionButtonPreference listPref, String key)
    {
        if (listPref != null)
        {
            CharSequence[] entries0 = listPref.getEntries();
            CharSequence[] values0 = listPref.getEntryValues();
            String actionID = PreferenceManager.getDefaultSharedPreferences(activity).getString(key, null);

            boolean hasValue = false;
            if (actionID != null)
            {
                for (CharSequence value : values0) {
                    if (value.equals(actionID)) {
                        hasValue = true;
                        break;
                    }
                }
            }

            if (!hasValue)
            {
                if (actionID != null && WidgetActions.hasActionLaunchPref(activity, 0, actionID))
                {
                    String title = WidgetActions.loadActionLaunchPref(activity, 0, actionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
                    String desc = WidgetActions.loadActionLaunchPref(activity, 0, actionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC);
                    String display = (desc != null && !desc.trim().isEmpty() ? desc : title);

                    CharSequence[] entries1 = new String[entries0.length + 1];
                    System.arraycopy(entries0, 0, entries1, 0, entries0.length);
                    entries1[entries0.length] = display;

                    CharSequence[] values1 = new String[values0.length + 1];
                    System.arraycopy(values0, 0, values1, 0, values0.length);
                    values1[values0.length] = actionID;

                    listPref.setEntries(entries1);
                    listPref.setEntryValues(values1);
                }
            }
        }
    }

    private static void loadPref_ui_tapAction(Activity activity, ActionButtonPreference listPref, String key, String defaultValue, final int requestCode)
    {
        if (listPref != null)
        {
            String actionID = PreferenceManager.getDefaultSharedPreferences(activity).getString(key, defaultValue);
            listPref.setActionButtonPreferenceListener(createTapActionListPreferenceListener(activity, actionID, requestCode));
            listPref.setOnPreferenceChangeListener(onTapActionChanged(activity, listPref, requestCode));

            int currentIndex = ((actionID != null) ? listPref.findIndexOfValue(actionID) : -1);
            if (currentIndex >= 0) {
                listPref.setValueIndex(currentIndex);
            } else {
                Log.w(LOG_TAG, "loadPref: Unable to load " + key + "... The list is missing an entry for the descriptor: " + actionID);
                listPref.setValueIndex(0);
            }
        }
    }

    private static ActionButtonPreference.ActionButtonPreferenceListener createTapActionListPreferenceListener(final Activity activity, final String selectedActionID, final int requestCode)
    {
        return new ActionButtonPreference.ActionButtonPreferenceListener()
        {
            @Override
            public void onActionButtonClicked()
            {
                Intent intent = new Intent(activity, ActionListActivity.class);
                intent.putExtra(ActionListActivity.PARAM_NOSELECT, false);
                intent.putExtra(ActionListActivity.PARAM_SELECTED, selectedActionID);
                activity.startActivityForResult(intent, requestCode);
                activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
            }
        };
    }

    private static Preference.OnPreferenceChangeListener onTapActionChanged(final Activity activity, final ActionButtonPreference pref, final int requestCode)
    {
        return new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                pref.setActionButtonPreferenceListener(createTapActionListPreferenceListener(activity, (String)newValue, requestCode));
                return true;
            }
        };
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    /**
     * Alarm Prefs
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AlarmPrefsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            AppSettings.initLocale(getActivity());
            Log.i(LOG_TAG, "AlarmPrefsFragment: Arguments: " + getArguments());

            PreferenceManager.setDefaultValues(getActivity(), R.xml.preference_alarms, false);
            addPreferencesFromResource(R.xml.preference_alarms);

            Activity activity = getActivity();
            if (AlarmSettings.loadPrefPowerOffAlarms(activity)) {
                checkPermissions(activity, true);
            }
        }

        @Override
        public void onResume()
        {
            super.onResume();
            initPref_alarms(AlarmPrefsFragment.this);
        }

        public static final int REQUEST_PERMISSION_POWEROFFALARMS = 100;
        protected boolean checkPermissions(Activity activity, boolean requestIfMissing)
        {
            if (ContextCompat.checkSelfPermission(activity, AlarmNotifications.PERMISSION_POWEROFFALARM) != PackageManager.PERMISSION_GRANTED) {
                if (requestIfMissing) {
                    ActivityCompat.requestPermissions(activity, new String[]{AlarmNotifications.PERMISSION_POWEROFFALARM}, REQUEST_PERMISSION_POWEROFFALARMS);
                }
                return false;
            } else return true;
        }
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {}
    }

    private void initPref_alarms()
    {
        Preference batteryOptimization = findPreference(AlarmSettings.PREF_KEY_ALARM_BATTERYOPT);
        PreferenceCategory alarmsCategory = (PreferenceCategory)findPreference(AlarmSettings.PREF_KEY_ALARM_CATEGORY);
        removePrefFromCategory(batteryOptimization, alarmsCategory);
    }

    @SuppressLint("ResourceType")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void initPref_alarms(final AlarmPrefsFragment fragment)
    {
        final Context context = fragment.getActivity();
        if (context == null) {
            return;
        }

        int[] colorAttrs = { R.attr.tagColor_warning };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int colorWarning = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.warningTag_dark));
        typedArray.recycle();

        Preference batteryOptimization = fragment.findPreference(AlarmSettings.PREF_KEY_ALARM_BATTERYOPT);
        if (batteryOptimization != null)
        {
            if (Build.VERSION.SDK_INT >= 23)
            {
                batteryOptimization.setOnPreferenceClickListener(onBatteryOptimizationClicked(context));
                if (AlarmSettings.isIgnoringBatteryOptimizations(fragment.getContext()))
                {
                    String listed = context.getString(R.string.configLabel_alarms_optWhiteList_listed);
                    batteryOptimization.setSummary(listed);
                } else {
                    String unlisted = context.getString(R.string.configLabel_alarms_optWhiteList_unlisted);
                    batteryOptimization.setSummary(SuntimesUtils.createColorSpan(null, unlisted, unlisted, colorWarning));
                }
                
            } else {
                PreferenceCategory alarmsCategory = (PreferenceCategory)fragment.findPreference(AlarmSettings.PREF_KEY_ALARM_CATEGORY);
                removePrefFromCategory(batteryOptimization, alarmsCategory);  // battery optimization is api 23+
            }
        }

        Preference notificationPrefs = fragment.findPreference(AlarmSettings.PREF_KEY_ALARM_NOTIFICATIONS);
        if (notificationPrefs != null)
        {
            boolean notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
            notificationPrefs.setOnPreferenceClickListener(onNotificationPrefsClicked(context));

            if (notificationsEnabled)
            {
                String enabledString = context.getString(R.string.configLabel_alarms_notifications_on);
                if (isDeviceSecure(context) && !notificationsOnLockScreen(context))
                {
                    String disabledString = context.getString(R.string.configLabel_alarms_notifications_off);
                    String summaryString = context.getString(R.string.configLabel_alarms_notifications_summary1, disabledString);
                    notificationPrefs.setSummary(SuntimesUtils.createColorSpan(null, summaryString, disabledString, colorWarning));
                } else {
                    notificationPrefs.setSummary(context.getString(R.string.configLabel_alarms_notifications_summary0, enabledString));
                }
            } else {
                String disabledString = context.getString(R.string.configLabel_alarms_notifications_off);
                notificationPrefs.setSummary(SuntimesUtils.createColorSpan(null, disabledString, disabledString, colorWarning));
            }
        }

        Preference volumesPrefs = fragment.findPreference(AlarmSettings.PREF_KEY_ALARM_VOLUMES);
        if (volumesPrefs != null) {
            volumesPrefs.setOnPreferenceClickListener(onVolumesPrefsClicked(context));
        }

        Preference powerOffAlarmsPref = fragment.findPreference(AlarmSettings.PREF_KEY_ALARM_POWEROFFALARMS);
        if (powerOffAlarmsPref != null)
        {
            powerOffAlarmsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    Activity activity = fragment.getActivity();
                    boolean enabled = (Boolean)newValue;
                    if (enabled && activity != null) {
                        fragment.checkPermissions(activity, true);
                    }
                    return true;
                }
            });
            powerOffAlarmsPref.setSummary(context.getString(R.string.configLabel_alarms_poweroffalarms_summary, findPermission(context, AlarmNotifications.PERMISSION_POWEROFFALARM)));
        }

        Preference showLauncher = fragment.findPreference(AlarmSettings.PREF_KEY_ALARM_SHOWLAUNCHER);
        if (showLauncher != null)
        {
            showLauncher.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    if (context != null)
                    {
                        ComponentName componentName = new ComponentName(context, "com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivityLauncher");
                        int state = (Boolean)newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                        PackageManager packageManager = context.getPackageManager();
                        packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
                        Toast.makeText(context, context.getString(R.string.reboot_required_message), Toast.LENGTH_LONG).show();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private static void removePrefFromCategory(Preference pref, PreferenceCategory category)
    {
        if (pref != null && category != null) {
            category.removePreference(pref);
        }
    }

    /***
     * Android 4 and under can enable/disable notifications per app .. the setting is located in App details.
     * Android 5 adds the ability to display notifications on the lock screen (global) .. global lock screen setting is in "Sound Settings".
     * Android 7 extends the ability to display notifications on the lock screen (per app) .. app lock screen setting is in App details.
     * Android 8 adds the ability to enable/disable notifications per channel. .. TODO
     */
    private static Preference.OnPreferenceClickListener onNotificationPrefsClicked(final Context context)
    {
        final boolean notificationsOnLockScreen = notificationsOnLockScreen(context);
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    openNotificationSettings(context);

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (!notificationsOnLockScreen) {
                        openSoundSettings(context);
                    } else {
                        openNotificationSettings(context);
                    }

                } else {
                    openNotificationSettings(context);
                }
                return false;
            }
        };
    }

    /**
     * https://stackoverflow.com/questions/32366649/any-way-to-link-to-the-android-notification-settings-for-my-app
     * @param context
     */
    public static void openNotificationSettings(@NonNull Context context)
    {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());                           // Android 5-7
            intent.putExtra("app_uid", context.getApplicationInfo().uid);                       // Android 5-7
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());    // Android 8+

        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    private static Preference.OnPreferenceClickListener onVolumesPrefsClicked(final Context context)
    {
        return new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                openSoundSettings(context);
                return false;
            }
        };
    }

    public static void openSoundSettings(@NonNull Context context)
    {
        Intent intent = new Intent();
        intent.setAction("android.settings.SOUND_SETTINGS");
        context.startActivity(intent);
    }

    /**
     * https://stackoverflow.com/questions/43438978/get-status-of-setting-control-notifications-on-your-lock-screen
     * @param context
     * @return true notifications allowed on lock screen (global setting)
     */
    public static boolean notificationsOnLockScreen(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {                  // per app "on lock screen" setting introduce in Android7
            return (Settings.Secure.getInt(context.getContentResolver(), "lock_screen_show_notifications", -1) > 0);    // TODO

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {    // global "on lock screen" setting introduced in Android5
            return (Settings.Secure.getInt(context.getContentResolver(), "lock_screen_show_notifications", -1) > 0);

        } else {
            return true;
        }
    }

    private static Preference.OnPreferenceClickListener onBatteryOptimizationClicked(final Context context)
    {
       return new Preference.OnPreferenceClickListener() {
           @Override
           public boolean onPreferenceClick(Preference preference) {
               if (Build.VERSION.SDK_INT >= 23) {
                   context.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
               }
               return false;
           }
       };
    }

    protected static boolean isDeviceSecure(Context context)
    {
        KeyguardManager keyguard = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguard != null)
        {
            if (Build.VERSION.SDK_INT >= 23) {
                return keyguard.isDeviceSecure();

            } else if (Build.VERSION.SDK_INT >= 16) {
                return keyguard.isKeyguardSecure();

            } else return false;
        } else return false;
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    private static void initPref_observerHeight(final Activity context, final LengthPreference pref)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionShadow});
        int drawableID = a.getResourceId(0, R.drawable.ic_action_shadow);
        a.recycle();

        String title = context.getString(R.string.configLabel_general_observerheight) + " [i]";
        int iconSize = (int) context.getResources().getDimension(R.dimen.prefIcon_size);
        ImageSpan shadowIcon = SuntimesUtils.createImageSpan(context, drawableID, iconSize, iconSize, 0);
        SpannableStringBuilder titleSpan = SuntimesUtils.createSpan(context, title, "[i]", shadowIcon);
        pref.setTitle(titleSpan);

        WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
        pref.setMetric(units == WidgetSettings.LengthUnit.METRIC);
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                try {
                    double doubleValue = Double.parseDouble((String)newValue);
                    if (doubleValue > 0)
                    {
                        WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
                        preference.setSummary(formatObserverHeightSummary(preference.getContext(), doubleValue, units, false));
                        return true;

                    } else return false;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
    }
    private static void loadPref_observerHeight(Context context, final LengthPreference pref)
    {
        final WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);
        double observerHeight = WidgetSettings.loadObserverHeightPref(context, 0);
        pref.setText((pref.isMetric() ? observerHeight : WidgetSettings.LengthUnit.metersToFeet(observerHeight)) + "");
        pref.setSummary(formatObserverHeightSummary(context, observerHeight, units, true));
    }
    private static CharSequence formatObserverHeightSummary(Context context, double observerHeight, WidgetSettings.LengthUnit units, boolean convert)
    {
        String observerHeightDisplay = SuntimesUtils.formatAsHeight(context, observerHeight, units, convert, 2);
        return context.getString(R.string.configLabel_general_observerheight_summary, observerHeightDisplay);
    }

    private static void initPref_altitude(final Activity context, final CheckBoxPreference altitudePref)
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

    private static void loadPref_altitude(Context context, CheckBoxPreference altitudePref)
    {
        boolean useAltitude = WidgetSettings.loadLocationAltitudeEnabledPref(context, 0);
        altitudePref.setChecked(useAltitude);
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////


    private static void initPref_timeFormat(final Activity context, final Preference timeformatPref)
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

    private static void loadPref_timeFormat(final Activity context, final ListPreference timeformatPref)
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

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    private static void initPref_calculator(Context context, final SummaryListPreference calculatorPref, String defaultCalculator)
    {
        initPref_calculator(context, calculatorPref, null, defaultCalculator);
    }
    private static void initPref_calculator(Context context, final SummaryListPreference calculatorPref, int[] requestedFeatures, String defaultCalculator)
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
    private static void loadPref_calculator(Context context, SummaryListPreference calculatorPref)
    {
        loadPref_calculator(context, calculatorPref, "");
    }
    private static void loadPref_calculator(Context context, SummaryListPreference calculatorPref, String calculatorName)
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

}
