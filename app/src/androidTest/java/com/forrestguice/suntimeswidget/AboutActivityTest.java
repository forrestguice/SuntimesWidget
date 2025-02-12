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
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.FlakyTest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.support.espresso.action.ViewActionsContrib;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShownCompletely;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.hasDrawable;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.navigationButton;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class AboutActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<AboutActivity> activityRule = new ActivityTestRule<>(AboutActivity.class, false, false);

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
    public void test_AboutActivity() {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new AboutActivityRobot()
                .assertActivityShown(activity)
                .assertPageIsShown(activity, 0);
                //.assertVersionIsShown(activity, BuildConfig.VERSION_NAME);
    }


    @Test @FlakyTest
    public void test_AboutActivity_navigation_gestures()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new AboutActivityRobot()
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
    }

    @Test
    public void test_AboutActivity_navigation_tabs()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        new AboutActivityRobot()
                .assertActivityShown(activity)
                .assertPageIsShown(activity, 0)

                .clickOnTab(1).sleep(250)
                .assertPageIsShown(activity, 1)

                .clickOnTab(2).sleep(250)
                .assertPageIsShown(activity, 2)

                .clickOnTab(0).sleep(250)
                .assertPageIsShown(activity, 0);
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
                sleep(500);
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
        public AboutActivityRobot clickOnTab(int position) {
            onView(withId(R.id.tabs)).perform(ViewActionsContrib.selectTabAtPosition(position));
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public AboutActivityRobot assertActivityShown(Context context)
        {
            onView(allOf(withText(R.string.configAction_aboutWidget), isDescendantOfA(withClassName(endsWith("Toolbar"))))).check(assertShown);
            onView(allOf(withId(R.id.container), withClassName(endsWith("ViewPager")))).check(assertShown);
            return this;
        }

        public AboutActivityRobot assertVersionIsShown(Context context, String versionString) {
            onView(withText(containsString(versionString))).check(assertShownCompletely);
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
                    onView(withId(R.id.txt_about_legal2)).check(assertShown);
                    //onView(withId(R.id.txt_about_legal3)).check(assertShown);   // TODO: flaky; view need to be scrolled first
                    //onView(withId(R.id.txt_about_media)).check(assertShown);
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
