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
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesScreenshots extends SuntimesActivityTestBase
{
    private static String version = BuildConfig.VERSION_NAME;

    @Before
    public void initScreenshots()
    {
        if (!version.startsWith("v"))
            version = "v" + version;
    }

    @Test
    public void makeScreenshots()
    {
        SuntimesActivity context = activityRule.getActivity();
        configureAppForScreenshots(context);

        String[] locales = context.getResources().getStringArray(R.array.locale_values);
        for (String languageTag : locales)
        {
            makeScreenshots(context, languageTag);
        }
    }

    private void makeScreenshots(Context context, String languageTag)
    {
        configureAppForScreenshots(context, languageTag);
        activityRule.launchActivity(activityRule.getActivity().getIntent());

        // dialogs
        DialogTest.showAboutDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-about");
        DialogTest.cancelAboutDialog();

        DialogTest.showHelpDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-help");
        DialogTest.cancelHelpDialog();

        DialogTest.showEquinoxDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-equinox");
        DialogTest.cancelEquinoxDialog();

        DialogTest.showLightmapDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-lightmap");
        DialogTest.cancelLightmapDialog();

        TimeZoneDialogTest.showTimezoneDialog(activityRule.getActivity());
        captureScreenshot(version + "/" + languageTag, "dialog-timezone0");
        TimeZoneDialogTest.inputTimezoneDialog_mode(context, WidgetSettings.TimezoneMode.SOLAR_TIME);
        captureScreenshot(version + "/" + languageTag, "dialog-timezone1");
        TimeZoneDialogTest.cancelTimezoneDialog();

        AlarmDialogTest.showAlarmDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-alarm");
        AlarmDialogTest.cancelAlarmDialog();

        TimeDateDialogTest.showDateDialog(context);
        captureScreenshot(version + "/" + languageTag, "dialog-date");
        TimeDateDialogTest.cancelDateDialog();

        LocationDialogTest.showLocationDialog();
        captureScreenshot(version + "/" + languageTag, "dialog-location0");
        LocationDialogTest.editLocationDialog();
        captureScreenshot(version + "/" + languageTag, "dialog-location1");
        LocationDialogTest.cancelLocationDialog(context);

        // main activity
        captureScreenshot(version + "/" + languageTag, "activity-main0");
    }

    private void configureAppForScreenshots(Activity context)
    {
        WidgetSettings.saveDateModePref(context, 0, WidgetSettings.DateMode.CURRENT_DATE);

        WidgetSettings.saveLocationModePref(context, 0, WidgetSettings.LocationMode.CUSTOM_LOCATION);
        WidgetSettings.saveLocationPref(context, 0, new WidgetSettings.Location(TESTLOC_2_LABEL, TESTLOC_2_LAT, TESTLOC_2_LON));

        WidgetSettings.saveTimezoneModePref(context, 0, WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);
        WidgetSettings.saveTimezonePref(context, 0, TESTTZID_2);

        WidgetSettings.saveTrackingModePref(context, 0, WidgetSettings.TrackingMode.SOONEST);

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWWARNINGS, false);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWLIGHTMAP, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWDATASOURCE, true);
        prefs.putBoolean(AppSettings.PREF_KEY_UI_SHOWEQUINOX, true);
        prefs.apply();
    }

    private void configureAppForScreenshots(Context context, String languageTag)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(AppSettings.PREF_KEY_LOCALE_MODE, AppSettings.LocaleMode.CUSTOM_LOCALE.name());
        prefs.putString(AppSettings.PREF_KEY_LOCALE, languageTag);
        prefs.apply();
    }

}
