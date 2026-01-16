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

package com.forrestguice.suntimeswidget.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.navigationButton;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@BehaviorTest
@RunWith(SuntimesJUnitTestRunner.class)
public class ActionListActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<ActionListActivity> activityRule = new ActivityTestRule<>(ActionListActivity.class, false, false);

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

    @Test @QuickTest
    public void test_ActionListActivity()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        ActionListActivityRobot robot = new ActionListActivityRobot()
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);
    }

    @Test
    public void test_ActionListActivity_clear()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        ActionListActivityRobot robot = new ActionListActivityRobot()
                .assertActivityShown(activity)
                .showOverflowMenu(activity).sleep(1000)
                .clickOverflowMenu_clear(activity)
                .assertClearDialogShown(activity, true)
                .clickConfirmCancelButton()
                .assertClearDialogShown(activity, false);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_clear(activity)
                .assertClearDialogShown(activity, true)
                .clickConfirmClearButton();
        // TODO: assert cleared
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * ActionListActivityRobot
     */
    public static class ActionListActivityRobot extends ActivityRobot<ActionListActivityRobot>
    {
        public ActionListActivityRobot() {
            setRobot(this);
        }

        public ActionListActivityRobot clickBackButton() {
            onView(navigationButton()).perform(click());
            return this;
        }

        public ActionListActivityRobot clickOverflowMenu_clear(Context context) {
            onView(withText(R.string.configAction_clearActions)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }

        public ActionListActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_clearActions)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public ActionListActivityRobot clickConfirmClearButton() {
            onView(withText(R.string.clearactions_dialog_ok)).perform(click());
            return this;
        }
        public ActionListActivityRobot clickConfirmCancelButton() {
            onView(withText(R.string.clearactions_dialog_cancel)).perform(click());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public ActionListActivityRobot assertActivityShown(Context context) {
            onView(allOf(withClassName(endsWith("TextView")), withText(R.string.loadaction_dialog_title),
                    isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(assertShown);
            return this;
        }

        public ActionListActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_clearActions)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public ActionListActivityRobot assertClearDialogShown(Context context, boolean isShown) {
            onView(withText(R.string.clearactions_dialog_msg)).check(isShown ? assertShown : doesNotExist());
            return this;
        }
    }
}
