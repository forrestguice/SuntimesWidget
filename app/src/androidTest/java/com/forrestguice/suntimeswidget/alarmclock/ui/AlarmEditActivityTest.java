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
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivityTest;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.WidgetListActivityTest;
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
public class AlarmEditActivityTest extends SuntimesActivityTestBase
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
    public void test_AlarmEditActivity()
    {
        Activity activity = activityRule.getActivity();
        AlarmEditActivityRobot robot = new AlarmEditActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);

        robot.showOverflowMenu(activity)
                .clickOverflowMenu_help();
        new DialogTest.HelpDialogRobot()
                .assertDialogShown(activity)
                .cancelDialog(activity);

        // TODO
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * AlarmEditActivityRobot
     */
    public static class AlarmEditActivityRobot extends ActivityRobot<AlarmEditActivityRobot>
    {
        public AlarmEditActivityRobot() {
            setRobot(this);
        }

        protected AlarmEditActivityRobot showActivity(Activity activity)
        {
            new AlarmActivityTest.AlarmActivityRobot()
                    .clickAlarmItem(0)
                    .clickAlarmItem(0);
            return this;
        }
        
        public AlarmEditActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public AlarmEditActivityRobot assertActivityShown(Context context)
        {
            //onView(allOf(withText(R.string.configLabel_alarmClock), withParent(withClassName(endsWith("Toolbar"))))).check(assertShown);
            //onView(withContentDescription(R.string.configLabel_bedtime)).check(assertShown);
            //onView(withContentDescription(R.string.configLabel_bedtime)).check(assertClickable);
            // TODO
            return this;
        }
        public AlarmEditActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
    }
}
