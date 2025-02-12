/**
    Copyright (C) 2025 Forrest Guice
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

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class IssuesTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class, false, false);

    @Rule
    public RetryRule retry = new RetryRule(3);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
        saveConfigState(getContext());
        overrideConfigState(getContext());
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
        restoreConfigState(getContext());
    }

    /**
     * Issue 862
     * Crash when performing various menu actions while location update is running
     */
    @Test
    public void test_issue862()
    {
        WidgetSettings.saveLocationModePref(getContext(), 0, WidgetSettings.LocationMode.CURRENT_LOCATION);
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        SuntimesActivity activity = activityRule.getActivity();
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);

        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();
        activity.showDate();    // crashes here with NPE before date dialog is shown
        new TimeDateDialogTest.TimeDateDialogRobot()
                .assertDialogShown(activity);
    }

}
