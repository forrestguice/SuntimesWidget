/**
    Copyright (C) 2018-2019 Forrest Guice
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

import android.content.Context;
import com.forrestguice.support.test.espresso.IdlingPolicies;
import com.forrestguice.support.test.filters.LargeTest;

import com.forrestguice.support.test.espresso.ElapsedTimeIdlingResource;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.forrestguice.support.test.espresso.Espresso.registerIdlingResources;
import static com.forrestguice.support.test.espresso.Espresso.unregisterIdlingResources;

@Category(UnlistedTest.class)
@SuppressWarnings("Convert2Diamond")
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesScreenshots extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Before
    public void initScreenshots() {
        initConfigurations();
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
    /**@Test
    public void makeScreenshots()
    {
        makeScreenshots(true);
    }*/

    /**
     * @param complete true make complete set of screenshots, false main screenshot only
     */
    public void makeScreenshots(boolean complete)
    {
        SuntimesActivity context = activityRule.getActivity();
        configureAppForTesting(context);

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
        SuntimesTestConfig configuration = defaultConfig;
        if (config.containsKey(languageTag)) {
            configuration = config.get(languageTag);
        }

        configureAppForTesting(context, languageTag, configuration, theme);
        activityRule.launchActivity(activityRule.getActivity().getIntent());

        long waitTime = 1 * 1000;            // wait a moment
        ElapsedTimeIdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        registerIdlingResources(waitForResource);

        for (int i = 0; i<3; i++) {
            long t = System.currentTimeMillis();
            while (System.currentTimeMillis() - t < waitTime) { /* busy */ }
        }
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "activity-main0-" + theme);

        unregisterIdlingResources(waitForResource);
    }

    private void makeScreenshots1(Context context, String languageTag, String theme)
    {
        SuntimesTestConfig configuration = defaultConfig;
        if (config.containsKey(languageTag)) {
            configuration = config.get(languageTag);
        }

        configureAppForTesting(context, languageTag, configuration, theme);
        activityRule.launchActivity(activityRule.getActivity().getIntent());

        long waitTime = 3 * 1000;            // wait a moment
        ElapsedTimeIdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        registerIdlingResources(waitForResource);

        // main activity
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "activity-main0-" + theme);

        // dialogs
        DialogTest.showAboutDialog(context, false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-about-" + theme);
        DialogTest.cancelAboutDialog();

        DialogTest.showHelpDialog(context, false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-help-" + theme);
        DialogTest.cancelHelpDialog();

        DialogTest.showEquinoxDialog(context, false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-equinox-" + theme);
        DialogTest.cancelEquinoxDialog();

        DialogTest.showLightmapDialog(context, false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-lightmap-" + theme);
        DialogTest.cancelLightmapDialog();

        TimeZoneDialogTest.showTimezoneDialog(activityRule.getActivity(), false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-timezone0-" + theme);
        TimeZoneDialogTest.inputTimezoneDialog_mode(context, WidgetSettings.TimezoneMode.SOLAR_TIME);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-timezone1-" + theme);
        TimeZoneDialogTest.cancelTimezoneDialog();

        AlarmDialogTest.showAlarmDialog(context, false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-alarm-" + theme);
        AlarmDialogTest.cancelAlarmDialog();

        TimeDateDialogTest.showDateDialog(context, false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-date-" + theme);
        TimeDateDialogTest.cancelDateDialog();

        LocationDialogTest.showLocationDialog(false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-location0-" + theme);
        LocationDialogTest.editLocationDialog(false);
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-location1-" + theme);
        LocationDialogTest.cancelLocationDialog(context);
    }

}
