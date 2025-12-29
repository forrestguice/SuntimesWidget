/**
    Copyright (C) 2018-2025 Forrest Guice
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
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmCreateDialogTest;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.equinox.EquinoxCardDialogTest;
import com.forrestguice.suntimeswidget.getfix.LocationDialogTest;
import com.forrestguice.suntimeswidget.graph.LightMapDialogTest;

import com.forrestguice.suntimeswidget.settings.AppSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;

@Category(UnlistedTest.class)
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesScreenshots extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Before
    public void initScreenshots() throws IOException {
        initConfigurations();
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
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

        long waitTime = 1000;            // wait a moment
        IdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        registerIdlingResources(waitForResource);

        for (int i = 0; i<3; i++) {
            long t = System.currentTimeMillis();
            //noinspection StatementWithEmptyBody
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
        IdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        registerIdlingResources(waitForResource);

        // main activity
        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "activity-main0-" + theme);

        // dialogs

        Activity activity = activityRule.getActivity();
        new DialogTest.AboutDialogRobot().showDialog(activity)
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-about-" + theme)
                .cancelDialog(context);

        new DialogTest.HelpDialogRobot().showDialog(activity)
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-help-" + theme)
                .cancelDialog(context);

        new EquinoxCardDialogTest.EquinoxDialogRobot().showDialog(activity)
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-equinox-" + theme)
                .cancelDialog(context);

        new LightMapDialogTest.LightMapDialogRobot().showDialog(activity)
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-lightmap-" + theme)
                .cancelDialog(context);

        new TimeZoneDialogTest.TimeZoneDialogRobot().showDialog(activity)
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-timezone0-" + theme);
        new TimeZoneDialogTest.TimeZoneDialogRobot().inputTimezoneDialogMode(context, TimezoneMode.SOLAR_TIME);
        new TimeZoneDialogTest.TimeZoneDialogRobot()
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-timezone1-" + theme)
                .cancelDialog(activity);

        new AlarmCreateDialogTest.AlarmDialogRobot().showDialog(activity)
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-alarm-" + theme)
                .cancelDialog(activity);

        new TimeDateDialogTest.TimeDateDialogRobot()
                .showDialog(activity)
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-date-" + theme)
                .cancelDialog(activity);

        new LocationDialogTest.LocationDialogRobot()
                .showDialog(activity)
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-location0-" + theme);
        new LocationDialogTest.LocationDialogRobot()
                .clickLocationEditButton()
                .captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "dialog-location1-" + theme)
                .cancelDialog(activity);
    }

}
