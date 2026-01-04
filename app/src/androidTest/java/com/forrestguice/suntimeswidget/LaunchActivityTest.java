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

import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmActivityTest;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class LaunchActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesLaunchActivity> activityRule = new ActivityTestRule<SuntimesLaunchActivity>(SuntimesLaunchActivity.class, true, false);

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

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    @Test
    public void test_LaunchActivity_firstLaunch_skipWelcome()
    {
        config(getContext()).edit().putBoolean(AppSettings.PREF_KEY_FIRST_LAUNCH, true).apply();
        config(getContext()).edit().putString(AppSettings.PREF_KEY_LAUNCHER_MODE, AppSettings.PREF_DEF_LAUNCHER_MODE).apply();

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        WelcomeActivityTest.WelcomeActivityRobot robot = new WelcomeActivityTest.WelcomeActivityRobot()
                .assertActivityShown(activityRule.getActivity());

        robot.clickPrevButton();    // back / skip
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

    @Test
    public void test_LaunchActivity_firstLaunch_finishWelcome()
    {
        config(getContext()).edit().putBoolean(AppSettings.PREF_KEY_FIRST_LAUNCH, true).apply();
        config(getContext()).edit().putString(AppSettings.PREF_KEY_LAUNCHER_MODE, AppSettings.PREF_DEF_LAUNCHER_MODE).apply();

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        WelcomeActivityTest.WelcomeActivityRobot robot = new WelcomeActivityTest.WelcomeActivityRobot()
                .assertActivityShown(activityRule.getActivity());

        for (int i=0; i<robot.numPages(); i++) {
            robot.clickNextButton();
        }
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

    @Test
    public void test_LaunchActivity_alarms()
    {
        config(getContext()).edit().putBoolean(AppSettings.PREF_KEY_FIRST_LAUNCH, false).apply();
        config(getContext()).edit().putString(AppSettings.PREF_KEY_LAUNCHER_MODE, AlarmClockActivity.class.getSimpleName()).apply();

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new AlarmActivityTest.AlarmActivityRobot()
                .assertActivityShown();
    }

    @Test
    public void test_LaunchActivity_clock()
    {
        config(getContext()).edit().putBoolean(AppSettings.PREF_KEY_FIRST_LAUNCH, false).apply();
        config(getContext()).edit().putString(AppSettings.PREF_KEY_LAUNCHER_MODE, SuntimesActivity.class.getSimpleName()).apply();

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

}
