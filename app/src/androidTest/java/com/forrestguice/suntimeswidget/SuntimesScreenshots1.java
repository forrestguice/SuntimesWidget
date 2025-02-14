/**
    Copyright (C) 2019 Forrest Guice
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
import android.os.SystemClock;
import com.forrestguice.support.test.espresso.IdlingPolicies;
import com.forrestguice.support.test.espresso.ElapsedTimeIdlingResource;
import com.forrestguice.support.test.espresso.action.ViewActions;
import com.forrestguice.support.test.filters.LargeTest;

import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.registerIdlingResources;
import static com.forrestguice.support.test.espresso.Espresso.unregisterIdlingResources;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;

@Category(UnlistedTest.class)
@SuppressWarnings("Convert2Diamond")
@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesScreenshots1 extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<AlarmClockActivity> activityRule = new ActivityTestRule<>(AlarmClockActivity.class);

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
        AlarmClockActivity activity = activityRule.getActivity();
        configureAppForTesting(activity);
        String[] locales = activity.getResources().getStringArray(R.array.locale_values);
        String[] themes = new String[] { AppSettings.THEME_DARK, AppSettings.THEME_LIGHT };
        for (String languageTag : locales) {
            for (String theme : themes) {
                makeScreenshots0(activity, languageTag, theme);
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

        activityRule.getActivity().finish();
        activityRule.launchActivity(activityRule.getActivity().getIntent());
        onView( withId(android.R.id.content)).perform(ViewActions.click());

        long waitTime = 6 * 1000;            // wait a moment
        ElapsedTimeIdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        registerIdlingResources(waitForResource);

        long t0 = SystemClock.elapsedRealtime();
        long t1 = t0 + 3000;
        while (SystemClock.elapsedRealtime() < t1) {
            // busy wait
        }

        captureScreenshot(activityRule.getActivity(), version + "/" + languageTag, "activity-alarms0-" + theme);
        unregisterIdlingResources(waitForResource);
    }
}
