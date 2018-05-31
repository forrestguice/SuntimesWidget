/**
    Copyright (C) 2018 Forrest Guice
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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;

@SuppressWarnings("Convert2Diamond")
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesScreenshots extends SuntimesActivityTestBase
{
    private static String version = BuildConfig.VERSION_NAME;

    private HashMap<String, ScreenshotConfig> config;
    private ScreenshotConfig defaultConfig = new ScreenshotConfig(new WidgetSettings.Location("Iron Springs", "34.58742", "-112.57367"), "US/Arizona", false);

    @Before
    public void initScreenshots()
    {
        config = new HashMap<String, ScreenshotConfig>();
        config.put("ca", new ScreenshotConfig(new WidgetSettings.Location("Barcelona", "41.3825", "2.1769"), "CET", true));
        config.put("de", new ScreenshotConfig(new WidgetSettings.Location("Berlin", "52.5243", "13.4105"), "Europe/Berlin", true));
        config.put("es_ES", new ScreenshotConfig(new WidgetSettings.Location("Madrid", "40.4378", "-3.8196"), "Europe/Madrid", true));
        config.put("fr", new ScreenshotConfig(new WidgetSettings.Location("Paris", "48.8566", "2.3518"), "Europe/Paris", true));
        config.put("hu", new ScreenshotConfig(new WidgetSettings.Location("Budapest", "47.4811", "18.9902"), "Europe/Budapest", true));
        config.put("pl", new ScreenshotConfig(new WidgetSettings.Location("Warszawa", "52.2319", "21.0067"), "Poland", true));
        config.put("nb", new ScreenshotConfig(new WidgetSettings.Location("Oslo", "59.8937", "10.6450"), "Europe/Oslo", true));

        if (!version.startsWith("v"))
            version = "v" + version;
    }

    /**
     * Make the main screenshot only (for each locale + theme).
     */
    @Test
    public void makeScreenshot()
    {
        makeScreenshots(false);
    }

    /**
     * Make a complete set of screenshots (for each locale + theme); this takes several minutes.
     */
    @Test
    public void makeScreenshots()
    {
        makeScreenshots(true);
    }

    /**
     * @param complete true make complete set of screenshots, false main screenshot only
     */
    public void makeScreenshots(boolean complete)
    {
        SuntimesActivity context = activityRule.getActivity();
        configureAppForScreenshots(context);

        String[] locales = context.getResources().getStringArray(R.array.locale_values);
        String[] themes = new String[] { AppSettings.THEME_DARK, AppSettings.THEME_LIGHT };
        for (String languageTag : locales)
        {
            for (String theme : themes)
            {
                if (complete)
                    makeScreenshots1(context, languageTag, theme);
                else makeScreenshots0(context, languageTag, theme);
            }
        }
    }

    private void makeScreenshots0(Context context, String languageTag, String theme)
    {
        configureAppForScreenshots(context, languageTag, theme);
        activityRule.launchActivity(activityRule.getActivity().getIntent());

        long waitTime = 3 * 1000;            // wait a moment
        IdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        registerIdlingResources(waitForResource);

        captureScreenshot(version + "/" + languageTag, "activity-main0-" + theme);

        unregisterIdlingResources(waitForResource);
    }

    private void makeScreenshots1(Context context, String languageTag, String theme)
    {
        configureAppForScreenshots(context, languageTag, theme);
        activityRule.launchActivity(activityRule.getActivity().getIntent());

        // dialogs
        DialogTest.showAboutDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-about-" + theme);
        DialogTest.cancelAboutDialog();

        DialogTest.showHelpDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-help-" + theme);
        DialogTest.cancelHelpDialog();

        DialogTest.showEquinoxDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-equinox-" + theme);
        DialogTest.cancelEquinoxDialog();

        DialogTest.showLightmapDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-lightmap-" + theme);
        DialogTest.cancelLightmapDialog();

        TimeZoneDialogTest.showTimezoneDialog(activityRule.getActivity());
        captureScreenshot(version + "/" + languageTag, "dialog-timezone0-" + theme);
        TimeZoneDialogTest.inputTimezoneDialog_mode(context, WidgetSettings.TimezoneMode.SOLAR_TIME);
        captureScreenshot(version + "/" + languageTag, "dialog-timezone1-" + theme);
        TimeZoneDialogTest.cancelTimezoneDialog();

        AlarmDialogTest.showAlarmDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-alarm-" + theme);
        AlarmDialogTest.cancelAlarmDialog();

        TimeDateDialogTest.showDateDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-date-" + theme);
        TimeDateDialogTest.cancelDateDialog();

        LocationDialogTest.showLocationDialog();
        captureScreenshot(version + "/" + languageTag, "dialog-location0-" + theme);
        LocationDialogTest.editLocationDialog();
        captureScreenshot(version + "/" + languageTag, "dialog-location1-" + theme);
        LocationDialogTest.cancelLocationDialog(context);

        // main activity
        captureScreenshot(version + "/" + languageTag, "activity-main0-" + theme);
    }

    private void configureAppForScreenshots(Activity context)
    {
        WidgetSettings.saveDateModePref(context, 0, WidgetSettings.DateMode.CURRENT_DATE);
        WidgetSettings.saveTrackingModePref(context, 0, WidgetSettings.TrackingMode.SOONEST);
        WidgetSettings.saveShowSecondsPref(context, 0, false);

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWWARNINGS, false);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWLIGHTMAP, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWDATASOURCE, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWEQUINOX, true);
        prefs.apply();
    }

    private void configureAppForScreenshots(Context context, String languageTag)
    {
        configureAppForScreenshots(context, languageTag, AppSettings.PREF_DEF_APPEARANCE_THEME);
    }
    private void configureAppForScreenshots(Context context, String languageTag, String theme)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(AppSettings.PREF_KEY_LOCALE_MODE, AppSettings.LocaleMode.CUSTOM_LOCALE.name());
        prefs.putString(AppSettings.PREF_KEY_LOCALE, languageTag);
        prefs.putString(AppSettings.PREF_KEY_APPEARANCE_THEME, theme);
        prefs.apply();

        ScreenshotConfig configuration = defaultConfig;
        if (config.containsKey(languageTag))
        {
            configuration = config.get(languageTag);
        }

        WidgetSettings.saveTimeFormatModePref(context, 0, configuration.timeformat);

        WidgetSettings.saveLocationModePref(context, 0, WidgetSettings.LocationMode.CUSTOM_LOCATION);
        WidgetSettings.saveLocationPref(context, 0, configuration.location);

        WidgetSettings.saveTimezoneModePref(context, 0, WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);
        WidgetSettings.saveTimezonePref(context, 0, configuration.timezoneID);
    }

    public static class ScreenshotConfig
    {
        public WidgetSettings.Location location;
        public String timezoneID;
        public WidgetSettings.TimeFormatMode timeformat;

        public ScreenshotConfig(WidgetSettings.Location location, String timezoneID, boolean format24)
        {
            this.location = location;
            this.timezoneID = timezoneID;
            this.timeformat = (format24 ? WidgetSettings.TimeFormatMode.MODE_24HR : WidgetSettings.TimeFormatMode.MODE_12HR);
        }
    }

}
