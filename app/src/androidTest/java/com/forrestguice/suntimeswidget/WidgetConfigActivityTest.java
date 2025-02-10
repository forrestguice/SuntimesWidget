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
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

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
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.navigationButton;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.withIndex;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class WidgetConfigActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesConfigActivity0> activityRule = new ActivityTestRule<>(SuntimesConfigActivity0.class, false, false);

    @Rule
    public RetryRule retry = new RetryRule(3);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    @Test
    public void test_WidgetConfigActivity()
    {
        Intent intent = new Intent();
        intent.putExtra(SuntimesConfigActivity0.EXTRA_RECONFIGURE, false);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, Integer.MAX_VALUE);
        activityRule.launchActivity(intent);

        Activity activity = activityRule.getActivity();
        WidgetConfigActivityRobot robot = new WidgetConfigActivityRobot()
                .assertActivityShown(activity)
                .assertHasAppWidgetId(activity, Integer.MAX_VALUE)
                .assertReconfigureMode(activity, false);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_about();
        new DialogTest.AboutDialogRobot()
                .assertDialogShown(activity)
                .cancelDialog(activity);
    }

    @Test
    public void test_WidgetConfigActivity_reconfigure()
    {
        Intent intent = new Intent();
        intent.putExtra(SuntimesConfigActivity0.EXTRA_RECONFIGURE, true);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, Integer.MAX_VALUE);
        activityRule.launchActivity(intent);

        Activity activity = activityRule.getActivity();
        WidgetConfigActivityRobot robot = new WidgetConfigActivityRobot()
                .assertActivityShown(activity)
                .assertHasAppWidgetId(activity, Integer.MAX_VALUE)
                .assertReconfigureMode(activity, true);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * WidgetConfigActivityRobot
     */
    public static class WidgetConfigActivityRobot extends ActivityRobot<WidgetConfigActivityRobot>
    {
        public WidgetConfigActivityRobot() {
            setRobot(this);
        }

        public WidgetConfigActivityRobot clickBackButton(Context context) {
            onView(navigationButton()).perform(click());
            return this;
        }

        public WidgetConfigActivityRobot clickApplyButton(Context context) {
            onView(withId(R.id.action_save)).perform(click());
            return this;
        }

        public WidgetConfigActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_aboutWidget)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public WidgetConfigActivityRobot assertActivityShown(Context context)
        {
            onView(withId(R.id.text_appwidgetid)).check(assertShown);
            onView(withId(R.id.appwidget_settings_layout)).check(assertShown);
            onView(withId(R.id.action_save)).check(assertShown);
            return this;
        }

        public WidgetConfigActivityRobot assertHasAppWidgetId(Context context, int appWidgetId) {
            onView(allOf(withId(R.id.text_appwidgetid), withText(appWidgetId + ""))).check(assertShown);
            return this;
        }

        public WidgetConfigActivityRobot assertPageIsTitled(Context context, int textResId) {
            onView(allOf(withText(textResId), withId(R.id.activity_title))).check(assertShown);
            return this;
        }

        public WidgetConfigActivityRobot assertReconfigureMode(Context context, boolean isReconfigureMode)
        {
            if (isReconfigureMode) {
                onView(allOf( withText(R.string.configAction_reconfigWidget_short),
                        isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(assertShown);
            } else {
                onView(allOf( withText(R.string.configAction_addWidget),
                        isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(assertShown);
            }
            return this;
        }

        public WidgetConfigActivityRobot assertOverflowMenuShown(Context context)
        {
            onView(withText(R.string.configAction_importWidget)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_exportWidget)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_restoreDefaults)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_aboutWidget)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
    }
}
