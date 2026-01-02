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

import android.app.Activity;
import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.equinox.EquinoxCardDialogTest;
import com.forrestguice.suntimeswidget.getfix.LocationDialogTest;
import com.forrestguice.suntimeswidget.graph.LightGraphDialogTest;
import com.forrestguice.suntimeswidget.graph.LightMapDialogTest;
import com.forrestguice.suntimeswidget.map.WorldMapDialogTest;
import com.forrestguice.suntimeswidget.moon.MoonDialogTest;
import com.forrestguice.suntimeswidget.settings.AppSettings;
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
    public RetryRule retry = new RetryRule(1);

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
     * Issue 74
     * app crash (latitude edge case) described in issue #74.
     */
    @Test
    public void test_issue74()
    {
        int year = 2017; int month = 1; int day = 19;
        String latitude = "83.124";
        String longitude = "23.1592";

        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();

        // open the location dialog, and set test location
        new LocationDialogTest.LocationDialogRobot()
                .showDialog(context)
                .selectLocationMode(LocationMode.CUSTOM_LOCATION)
                .clickLocationEditButton()
                .inputLocationEditValues("TestAppCrash74", latitude, longitude)
                .assertLocationEditCoordinates(latitude, longitude)
                .applyDialog(context);

        // open the date dialog and change the date
        TimeDateDialogTest.TimeDateDialogRobot robot = new TimeDateDialogTest.TimeDateDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context)
                .selectDate(year, month, day)
                .applyDialog(context)
                .sleep(2000);

        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activityRule.getActivity());
        //SuntimesActivityTest.verifyActivity((SuntimesActivity) activityRule.getActivity());
    }

    /**
     * Issue 408
     * NullPointerException when refreshing location ("show moon" option disabled)
     */
    @Test
    public void test_issue408_refreshLocation()
    {
        config(getContext()).edit().putBoolean(AppSettings.PREF_KEY_UI_SHOWMOON, true).commit();
        WidgetSettings.saveLocationModePref(getContext(), 0, LocationMode.CURRENT_LOCATION);
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        SuntimesActivity activity = activityRule.getActivity();

        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);
        activity.finish();

        config(getContext()).edit().putBoolean(AppSettings.PREF_KEY_UI_SHOWMOON, false).commit();
        WidgetSettings.saveLocationModePref(getContext(), 0, LocationMode.CURRENT_LOCATION);
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));

        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();    // crash on refresh when "show moon" set false
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);    // wait for app to idle
    }
    @Test
    public void test_issue408_updateReceiver()
    {
        // test "show moon" enabled
        config(getContext()).edit().putBoolean(AppSettings.PREF_KEY_UI_SHOWMOON, true).commit();
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        SuntimesActivity activity = (SuntimesActivity) activityRule.getActivity();
        SuntimesActivityTest.test_mainActivity_fullUpdateReciever(activity);    // calls activity.finish()

        // test "show moon" disabled
        config(activity).edit().putBoolean(AppSettings.PREF_KEY_UI_SHOWMOON, false).commit();
        activityRule.launchActivity(activity.getIntent());
        SuntimesActivityTest.test_mainActivity_fullUpdateReciever(activity);    // crash on updateReceiver when "show moon" set false
    }

    /**
     * Issue 862
     * Crash when showing date dialog while location update is running.
     */
    @Test
    public void test_issue862_viewDate()
    {
        init_issue862();
        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();

        activityRule.getActivity().showDate();    // crashes with NPE before date dialog is shown
        new TimeDateDialogTest.TimeDateDialogAutomator()
                .clickApplyButton();    // if first NPE is avoided, there is another after dialog is dismissed
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

    protected void init_issue862()
    {
        config(getContext()).edit().putString(AppSettings.PREF_KEY_GETFIX_MAXAGE, "0");
        WidgetSettings.saveLocationModePref(getContext(), 0, LocationMode.CURRENT_LOCATION);
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        SuntimesActivity activity = activityRule.getActivity();
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);

    }

    /**
     * Issue 862
     * Crash when showing moon dialog while location update is running.
     */
    @Test
    public void test_issue862_moon()
    {
        init_issue862();
        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();

        activityRule.getActivity().showMoonDialog();    // crashes with NPE before moon dialog is shown
        new MoonDialogTest.MoonDialogRobot()
                .assertDialogShown(activityRule.getActivity());
    }

    @Test
    public void test_issue862_equinox()
    {
        init_issue862();
        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();
        activityRule.getActivity().showEquinoxDialog();
        new EquinoxCardDialogTest.EquinoxDialogRobot()
                .assertDialogShown(activityRule.getActivity());
    }
    @Test
    public void test_issue862_worldmap()
    {
        init_issue862();
        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();
        activityRule.getActivity().showWorldMapDialog();
        new WorldMapDialogTest.WorldMapDialogRobot()
                .assertDialogShown(activityRule.getActivity());
    }
    @Test
    public void test_issue862_sunlight()
    {
        init_issue862();
        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();
        activityRule.getActivity().showLightGraphDialog();
        new LightGraphDialogTest.LightGraphDialogRobot()
                .assertDialogShown(activityRule.getActivity());
    }
    @Test
    public void test_issue862_sunpos()
    {
        init_issue862();
        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();
        activityRule.getActivity().showLightMapDialog();
        new LightMapDialogTest.LightMapDialogRobot()
                .assertDialogShown(activityRule.getActivity());
    }
    @Test
    public void test_issue862_setAlarm()
    {
        init_issue862();
        new SuntimesActivityTest.MainActivityAutomator()
                .clickActionBar_refreshLocation();
        activityRule.getActivity().scheduleAlarm();
    }

}
