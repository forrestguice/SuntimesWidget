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
import android.content.Context;

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
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeLeft;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeRight;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class WelcomeActivityTest extends SuntimesActivityTestBase
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
    public void test_WelcomeActivity()
    {
        Activity activity = activityRule.getActivity();
        WelcomeActivityRobot robot = new WelcomeActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);
        robot.clickPrevButton();
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * WidgetListActivityRobot
     */
    public static class WelcomeActivityRobot extends ActivityRobot<WelcomeActivityRobot>
    {
        public WelcomeActivityRobot() {
            setRobot(this);
        }

        protected WelcomeActivityRobot showActivity(Activity activity) {
            new SuntimesSettingsActivityTest.SettingsActivityRobot()
                    .showActivity(activity)
                    .clickHeader_generalSettings()
                    .clickPref_welcomeWizard(activity);
            return this;
        }

        public WelcomeActivityRobot swipeNext() {
            onView(allOf(withId(R.id.container), withClassName(endsWith("ViewPager")))).perform(swipeLeft());
            return this;
        }
        public WelcomeActivityRobot swipePrev() {
            onView(allOf(withId(R.id.container), withClassName(endsWith("ViewPager")))).perform(swipeRight());
            return this;
        }
        public WelcomeActivityRobot clickNextButton() {
            onView(withId(R.id.button_next)).perform(click());
            return this;
        }
        public WelcomeActivityRobot clickPrevButton() {
            onView(withId(R.id.button_prev)).perform(click());
            return this;
        }
        public WelcomeActivityRobot clickSkipButton() {
            onView(withText(R.string.welcome_action_skip)).perform(click());
            return this;
        }
        public WelcomeActivityRobot clickFinishButton() {
            onView(withText(R.string.welcome_action_done)).perform(click());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public WelcomeActivityRobot assertActivityShown(Context context)
        {
            onView(withId(R.id.button_next)).check(assertShown);
            onView(withId(R.id.button_prev)).check(assertShown);
            onView(allOf(withId(R.id.container), withClassName(endsWith("ViewPager")))).check(assertShown);
            return this;
        }

    }
}
