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
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.support.espresso.ViewInteractionHelper;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItemUri;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.action.ViewActionsContrib.swipeLeftTo;
import static com.forrestguice.suntimeswidget.support.espresso.action.ViewActionsContrib.swipeRightTo;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(SuntimesJUnitTestRunner.class)
public class AlarmDismissActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<AlarmDismissActivity> activityRule = new ActivityTestRule<>(AlarmDismissActivity.class);

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
    public void test_AlarmDismissActivity()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setData(ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, -1));
        activityRule.launchActivity(intent);
        new AlarmDismissActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }

    /*@Test
    public void test_AlarmDismissActivity_preview()
    {
        Intent intent = new Intent(AlarmDismissActivity.ACTION_PREVIEW);
        intent.setData(ContentUris.withAppendedId(AlarmClockItemUri.CONTENT_URI, -1));
        activityRule.launchActivity(intent);
        new AlarmDismissActivityRobot()
                .assertActivityShown(activityRule.getActivity());
    }*/

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
