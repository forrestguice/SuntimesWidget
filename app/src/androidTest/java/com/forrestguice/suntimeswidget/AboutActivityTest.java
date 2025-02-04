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

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.support.test.InstrumentationRegistry;
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
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertClickable;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShownCompletely;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeLeft;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeRight;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.hasDrawable;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchersContrib.navigationButton;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class AboutActivityTest extends SuntimesActivityTestBase
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
    public void test_AboutActivity_navigation()
    {
        Activity activity = activityRule.getActivity();
        AboutActivityRobot robot = new AboutActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity)
                .assertPageIsShown(activity, 0)

                .swipeNext().sleep(500)
                .assertPageIsShown(activity, 1)
                .swipeNext().sleep(500)
                .assertPageIsShown(activity, 2)

                .swipePrev().sleep(500)
                .assertPageIsShown(activity, 1)
                .swipePrev().sleep(500)
                .assertPageIsShown(activity, 0);

        robot.clickHomeButton(activity);
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(activity);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * AboutActivityRobot
     */
    public static class AboutActivityRobot extends ActivityRobot<AboutActivityRobot>
    {
        public AboutActivityRobot() {
            setRobot(this);
        }

        protected AboutActivityRobot showActivity(Activity activity)
        {
            String navMode = AppSettings.loadNavModePref(activity);
            if (AppSettings.NAVIGATION_SIDEBAR.equals(navMode))
            {
                onView(navigationButton()).perform(click());
                onView(withText(R.string.configAction_aboutWidget)).perform(click());

            } else {
                openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
                onView(withText(R.string.configAction_aboutWidget)).perform(click());
            }
            return this;
        }

        public AboutActivityRobot swipeNext() {
            onView(allOf(withId(R.id.container), withClassName(endsWith("ViewPager")))).perform(swipeLeft());
            return this;
        }
        public AboutActivityRobot swipePrev() {
            onView(allOf(withId(R.id.container), withClassName(endsWith("ViewPager")))).perform(swipeRight());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public AboutActivityRobot assertActivityShown(Context context)
        {
            onView(allOf(withText(R.string.configAction_aboutWidget), isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(assertShown);
            onView(allOf(withId(R.id.container), withClassName(endsWith("ViewPager")))).check(assertShown);
            return this;
        }

        public AboutActivityRobot assertPageIsShown(Context context, int position) {
            switch (position)
            {
                case 2:
                    onView(withId(R.id.txt_about_legal4)).check(assertShownCompletely);
                    onView(withId(R.id.txt_about_legal5)).check(assertShownCompletely);
                    break;

                case 1:
                    onView(withId(R.id.txt_about_legal1)).check(assertShownCompletely);
                    onView(withId(R.id.txt_about_url1)).check(assertShownCompletely);
                    onView(withId(R.id.txt_about_legal2)).check(assertShownCompletely);
                    onView(withId(R.id.txt_about_legal3)).check(assertShownCompletely);
                    onView(withId(R.id.txt_about_media)).check(assertShownCompletely);
                    break;

                case 0:
                default:
                    onView(withText(R.string.app_shortdesc)).check(assertShownCompletely);
                    onView(withId(R.id.txt_about_version)).check(assertShownCompletely);
                    onView(withId(R.id.txt_about_support)).check(assertShownCompletely);
                    onView(withId(R.id.txt_about_url)).check(assertShownCompletely);
                    onView(withId(R.id.check_donate)).check(assertShownCompletely);
                    onView(allOf(withId(R.id.txt_about_icon), hasDrawable(R.drawable.ic_action_suntimes_huge))).check(assertShownCompletely);
                    break;
            }
            return this;
        }

        protected int numPages() {
            return 3;
        }
    }
}
