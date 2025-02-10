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

package com.forrestguice.suntimeswidget.graph;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.support.test.espresso.ViewAssertionHelper;
import com.forrestguice.support.test.filters.LargeTest;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class LightGraphDialogTest extends SuntimesActivityTestBase
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

    @Test @QuickTest
    public void test_LightGraphDialog()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightGraphDialogRobot robot = new LightGraphDialogRobot();
        robot.showDialog(context)
                .assertDialogShown(context);
        //.captureScreenshot(context, "suntimes-dialog-lightgraph0")
    }

    @Test
    public void test_LightGraphDialog_menu()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        LightGraphDialogRobot robot = new LightGraphDialogRobot();
        robot.showDialog(context).assertDialogShown(context);

        robot.clickTimeZoneLabel(context)
                .assertOverflowMenuShown_TimeZone(context)
                .cancelOverflowMenu_TimeZone(context);

        robot.showOverflowMenu(context).assertOverflowMenuShown(context)
                .clickOverflowMenuItem_Options(context)
                .assertOverflowMenuShown_Options(context)
                .cancelOverflowMenu_Options(context);

        robot.showOverflowMenu(context).assertOverflowMenuShown(context)
                .clickOverflowMenuItem_TimeZone(context)
                .assertOverflowMenuShown_TimeZone(context)
                .cancelOverflowMenu_TimeZone(context);

        robot.showOverflowMenu(context).assertOverflowMenuShown(context)
                .clickOverflowMenuItem_TimeZone(context)
                .clickOverflowMenuItem_TimeZone_LocalMean(context)
                .assertTimeZoneLocalMean(context);

        robot.showOverflowMenu(context).assertOverflowMenuShown(context)
                .clickOverflowMenuItem_TimeZone(context)
                .clickOverflowMenuItem_TimeZone_Suntimes(context)
                .assertTimeZoneSuntimes(context);

        robot.clickSunPositionButton(context);
        new LightMapDialogTest.LightMapDialogRobot().assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);

        robot.doubleRotateDevice(context).assertDialogShown(context);
        robot.cancelDialog(context).assertDialogNotShown(context);
    }

    /**
     * LightGraphDialogRobot
     */
    public static class LightGraphDialogRobot extends DialogTest.DialogRobot<LightGraphDialogRobot>
    {
        public LightGraphDialogRobot() {
            super();
            setRobot(this);
        }

        public LightGraphDialogRobot showDialog(Activity context) {
            openActionBarOverflowOrOptionsMenu(context);
            onView(withText(R.string.configAction_lightGraphDialog)).perform(click());
            return this;
        }
        @Override
        public LightGraphDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.info_time_lightgraph)).perform(pressBack());
            return this;
        }
        public LightGraphDialogRobot showOverflowMenu(Context context) {
            onView(withId(R.id.menu_button)).perform(click());
            return this;
        }
        public LightGraphDialogRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightGraphDialogRobot cancelOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightGraphDialogRobot cancelOverflowMenu_TimeZone(Context context) {
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public LightGraphDialogRobot clickOverflowMenuItem_Options(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightGraphDialogRobot clickOverflowMenuItem_TimeZone(Context context) {
            onView(withText(R.string.configLabel_timezone)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightGraphDialogRobot clickOverflowMenuItem_TimeZone_LocalMean(Context context) {
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightGraphDialogRobot clickOverflowMenuItem_TimeZone_Suntimes(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightGraphDialogRobot clickTimeZoneLabel(Context context) {
            onView(withId(R.id.info_time_graph)).perform(click());
            return this;
        }
        public LightGraphDialogRobot clickOverflowMenuItem_Share(Context context) {
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public LightGraphDialogRobot clickSunPositionButton(Context context) {
            onView(withId(R.id.sunposition_button)).perform(click());
            return this;
        }

        @Override
        public LightGraphDialogRobot assertDialogShown(Context context) {
            onView(withId(R.id.layout_header)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.info_time_lightgraph)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.info_time_graph)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.info_time_graph)).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.info_time_lightmap)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.sunposition_button)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.sunposition_button)).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.menu_button)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.menu_button)).check(ViewAssertionHelper.assertClickable);
            return this;
        }
        @Override
        public LightGraphDialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.layout_header)).check(doesNotExist());
            onView(withId(R.id.info_time_lightgraph)).check(doesNotExist());
            return this;
        }
        public LightGraphDialogRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configLabel_timezone)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_share)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }
        public LightGraphDialogRobot assertOverflowMenuShown_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.graph_option_crosshair)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.graph_option_axis)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.graph_option_grid)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.graph_option_points)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }
        public LightGraphDialogRobot assertOverflowMenuShown_TimeZone(Context context) {
            onView(withText(R.string.app_name)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }
        public LightGraphDialogRobot assertTimeZoneLocalMean(Context context) {
            // TODO
            return this;
        }
        public LightGraphDialogRobot assertTimeZoneSuntimes(Context context) {
            // TODO
            return this;
        }
    }

}
