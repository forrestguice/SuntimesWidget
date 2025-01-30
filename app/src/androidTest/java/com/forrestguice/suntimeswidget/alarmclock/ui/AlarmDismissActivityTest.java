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

package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.app.Activity;
import android.content.Context;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivityTest;
import com.forrestguice.suntimeswidget.colors.ColorValuesActivityTest;
import com.forrestguice.support.test.espresso.ViewInteractionHelper;
import com.forrestguice.support.test.espresso.matcher.ViewMatchers;
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
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeLeftTo;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeRightTo;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class AlarmDismissActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<AlarmClockActivity> activityRule = new ActivityTestRule<>(AlarmClockActivity.class);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
        saveConfigState(activityRule.getActivity());
        overrideConfigState(activityRule.getActivity());
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
        restoreConfigState(activityRule.getActivity());
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    @Test
    public void test_AlarmDismissActivity()
    {
        Activity activity = activityRule.getActivity();
        AlarmDismissActivityRobot robot = new AlarmDismissActivityRobot()
                .showActivity(activity)    // TODO
                .assertActivityShown(activity);
    }

    @Test
    public void test_AlarmDismissActivity_preview()
    {
        final Activity activity = activityRule.getActivity();
        ColorValuesActivityTest.ColorValuesActivityRobot robot0 = ColorValuesActivityTest.brightAlarmColorsActivityRobot(activity);
        robot0.showActivity(activity)
                .assertActivityShown(activity)
                .showOverflowMenu(activity)
                .clickOverflowMenu_preview().sleep(1000);

        AlarmDismissActivityRobot robot = new AlarmDismissActivityRobot();
        robot.assertActivityShown(activity)
                .dragDismissButton(activity).sleep(1000);
        robot0.assertActivityShown(activity);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * AlarmDismissActivityRobot
     */
    public static class AlarmDismissActivityRobot extends ActivityRobot<AlarmDismissActivityRobot>
    {
        public AlarmDismissActivityRobot() {
            setRobot(this);
        }

        protected AlarmDismissActivityRobot showActivity(Activity activity)
        {
            //AlarmActivityTest.AlarmActivityRobot robot = new AlarmActivityTest.AlarmActivityRobot();
            //AlarmDialogTest.AlarmDialogRobot robot1 = AlarmActivityTest.dialogRobot();
            //robot.clickAddAlarmButton();
            //robot1.applyDialog(activity);
            // TODO
            return this;
        }

        public AlarmDismissActivityRobot dragDismissButton(Activity activity) {
            int x = activity.getWindow().getDecorView().getWidth();
            dismissButton().get().perform(swipeRightTo(x));
            return this;
        }
        public AlarmDismissActivityRobot dragSnoozeButton() {
            snoozeButton().get().perform(swipeLeftTo(0));
            return this;
        }
        public AlarmDismissActivityRobot clickBackButton() {
            backButton().get().perform(click());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        protected ViewInteractionHelper.ViewInteractionInterface dismissButton() {
            return ViewInteractionHelper.wrap(onView(allOf(withText(R.string.alarmAction_dismiss),
                    isDescendantOfA(withClassName(endsWith("AlarmButton")))
            )));
        }
        protected ViewInteractionHelper.ViewInteractionInterface snoozeButton() {
            return ViewInteractionHelper.wrap(onView(allOf(withText(R.string.alarmAction_snooze),
                    isDescendantOfA(withClassName(endsWith("AlarmButton")))
            )));
        }
        protected ViewInteractionHelper.ViewInteractionInterface backButton() {
            return ViewInteractionHelper.wrap(onView(allOf(withContentDescription(android.R.string.cancel),
                    withClassName(endsWith("ActionButton"))
            )));
        }

        /////////////////////////////////////////////////////////////////////////

        public AlarmDismissActivityRobot assertActivityShown(Context context)
        {
            onView(withId(R.id.txt_alarm_label)).check(assertShown);
            onView(withId(R.id.txt_alarm_note)).check(assertShown);
            onView(withId(R.id.txt_clock_time)).check(assertShown);
            dismissButton().get().check(assertShown);
            return this;
        }
        public AlarmDismissActivityRobot assertState_isSounding()
        {
            onView(withId(R.id.icon_alarm_sounding)).check(assertShown);
            onView(withId(R.id.icon_alarm_snooze)).check(assertHidden);
            onView(withId(R.id.icon_alarm_timeout)).check(assertHidden);
            dismissButton().get().check(assertShown);
            snoozeButton().get().check(assertShown);

            return this;
        }
        public AlarmDismissActivityRobot assertState_isSnoozing()
        {
            onView(withId(R.id.txt_snooze)).check(assertShown);
            onView(withId(R.id.icon_alarm_snooze)).check(assertShown);
            onView(withId(R.id.icon_alarm_sounding)).check(assertHidden);
            onView(withId(R.id.icon_alarm_timeout)).check(assertHidden);
            dismissButton().get().check(assertShown);
            snoozeButton().get().check(assertHidden);
            return this;
        }
        public AlarmDismissActivityRobot assertState_isTimedOut()
        {
            onView(withId(R.id.icon_alarm_timeout)).check(assertShown);
            onView(withId(R.id.icon_alarm_snooze)).check(assertHidden);
            onView(withId(R.id.icon_alarm_sounding)).check(assertHidden);
            dismissButton().get().check(assertShown);
            snoozeButton().get().check(assertHidden);
            return this;
        }

    }
}
