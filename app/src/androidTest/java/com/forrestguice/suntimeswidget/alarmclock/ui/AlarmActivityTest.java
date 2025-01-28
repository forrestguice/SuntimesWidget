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
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTest;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
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
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertClickable;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class AlarmActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

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
    public void test_AlarmActivity()
    {
        Activity activity = activityRule.getActivity();
        AlarmActivityRobot robot = new AlarmActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)    // :
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);

        robot.showOverflowMenu(activity).sleep(1000)    // : -> Sort
                .clickOverflowMenu_sort(activity)
                .assertSortMenuShown(activity)
                .cancelSortMenu(activity);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * AlarmActivityRobot
     */
    public static class AlarmActivityRobot extends ActivityRobot<AlarmActivityRobot>
    {
        public AlarmActivityRobot() {
            setRobot(this);
        }

        protected AlarmActivityRobot showActivity(Activity activity) {
            new SuntimesActivityTest.MainActivityRobot()
                    .showSidebarMenu(activity)
                    .clickSidebarMenu_alarms(activity);
            return this;
        }

        public AlarmActivityRobot clickOverflowMenu_clear(Context context) {
            onView(withText(R.string.configAction_clearAlarms)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmActivityRobot clickOverflowMenu_import(Context context) {
            onView(withText(R.string.configAction_exportAlarms)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmActivityRobot clickOverflowMenu_export(Context context) {
            onView(withText(R.string.configAction_importAlarms)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmActivityRobot clickOverflowMenu_sort(Context context) {
            onView(withText(R.string.configAction_sortAlarms)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_clearAlarms)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public AlarmActivityRobot cancelSortMenu(Context context) {
            onView(withText(R.string.configAction_sortAlarms_by_time)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public AlarmActivityRobot assertActivityShown(Context context)
        {
            onView(allOf(withText(R.string.configLabel_alarmClock), withParent(withClassName(endsWith("Toolbar"))))).check(assertShown);
            onView(withContentDescription(R.string.configLabel_bedtime)).check(assertShown);
            onView(withContentDescription(R.string.configLabel_bedtime)).check(assertClickable);
            // TODO: fab, navDrawer
            return this;
        }
        public AlarmActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_clearAlarms)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_importAlarms)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_exportAlarms)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sortAlarms)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public AlarmActivityRobot assertSortMenuShown(Context context) {
            onView(withText(R.string.configAction_sortAlarms_by_creation)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sortAlarms_by_time)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sortAlarms_offset)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sortAlarms_enabled_first)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
    }
}
