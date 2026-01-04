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
import android.content.res.TypedArray;
import androidx.test.filters.FlakyTest;
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
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.hasTextColor;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertClickable;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShownCompletely;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.hasDrawable;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.withIndex;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class WelcomeActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<WelcomeActivity> activityRule = new ActivityTestRule<>(WelcomeActivity.class, false, false);

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
    public void test_WelcomeActivity_pages()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        WelcomeActivityRobot robot = new WelcomeActivityRobot();

        robot.clickNextButton()      // 0 welcome
                .assertAppearancePageIsShown(activity)

                .clickNextButton()   // 1 appearance
                .assertUIPageIsShown(activity)

                .clickNextButton()   // 2 ui
                .assertLocationPageIsShown(activity)

                .clickNextButton()   // 3 location
                .assertTimeZonePageIsShown(activity)

                .clickNextButton()   // 4 timezone
                .assertAlarmsPageIsShown(activity)

                .clickNextButton()   // 5 alarms
                .assertAboutPageIsShown(activity);

        robot.clickNextButton();     // 6 about (finish)
        //robot0.assertShown_generalSettings(activity);
    }

    @Test
    public void test_WelcomeActivity_navigation_nextAndBack()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        WelcomeActivityRobot robot = new WelcomeActivityRobot();

        // click next / prev through all pages
        robot.assertActivityShown(activity)
                .assertWelcomePageIsShown(activity);
        int c = 0;
        int n = robot.numPages() - 1;
        for (int i=0; i<n; i++) {
            robot.clickNextButton()
                    .assertPageIndicatorIsShown(activity, ++c);
        }
        robot.sleep(500)
                .assertAboutPageIsShown(activity);
        for (int i=0; i<n; i++) {
            robot.clickPrevButton()
                    .assertPageIndicatorIsShown(activity, --c);
        }
        robot.assertWelcomePageIsShown(activity);

        robot.clickPrevButton();    // back (skip)
        //robot0.assertShown_generalSettings(activity);
    }

    @Test
    public void test_WelcomeActivity_navigation_nextAndFinish()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        WelcomeActivityRobot robot = new WelcomeActivityRobot();

        robot.assertActivityShown(activity)
                .assertWelcomePageIsShown(activity);
        int c = 0;
        int n = robot.numPages() - 1;
        for (int i=0; i<n; i++) {
            robot.clickNextButton()
                    .assertPageIndicatorIsShown(activity, ++c);
        }

        robot.sleep(500)
                .assertAboutPageIsShown(activity)
                .clickNextButton();
        //robot0.assertShown_generalSettings(activity);
    }

    @Test @FlakyTest
    public void test_WelcomeActivity_navigation_gestures()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        WelcomeActivityRobot robot = new WelcomeActivityRobot();

        // swipe next / prev through all pages
        robot.assertActivityShown(activity)
                .assertWelcomePageIsShown(activity);
        int c = 0;
        int n = robot.numPages() - 1;
        for (int i=0; i<n; i++) {
            robot.swipeNext()
                    .assertPageIndicatorIsShown(activity, ++c);
        }
        robot.assertAboutPageIsShown(activity);
        robot.swipeNext()
                .assertPageIndicatorIsShown(activity, c);    // swipe next from last page does nothing
        for (int i=0; i<n; i++) {
            robot.swipePrev()
                    .assertPageIndicatorIsShown(activity, --c);
        }
        robot.swipePrev()
                .assertPageIndicatorIsShown(activity, 0)    // swipe prev from first page does nothing
                .assertWelcomePageIsShown(activity);
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
                    .clickPref_welcomeWizard();
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
            onView(withId(R.id.button_next)).check(assertShown).check(assertClickable);
            onView(withId(R.id.button_prev)).check(assertShown).check(assertClickable);
            onView(allOf(withId(R.id.container), withClassName(endsWith("ViewPager")))).check(assertShown);
            return this;
        }

        public WelcomeActivityRobot assertPageIndicatorIsShown(Context context, int index)
        {
            TypedArray typedArray = context.obtainStyledAttributes(new int[] { R.attr.text_accentColor, R.attr.text_disabledColor });
            int activeColorId = typedArray.getResourceId(0, R.color.text_accent_dark);
            int inactiveColorId = typedArray.getResourceId(1, R.color.text_disabled_dark);
            typedArray.recycle();

            int n = numPages();
            for (int i=1; i<n; i++)
            {
                onView(allOf(
                        withIndex(withParent(withId(R.id.indicator_layout)), i),
                        hasTextColor(i == index ? activeColorId : inactiveColorId),
                        withText("\u2022")
                )).check(assertShownCompletely);
            }
            return this;
        }

        public WelcomeActivityRobot assertWelcomePageIsShown(Context context)
        {
            onView(withText(R.string.app_name)).check(assertShownCompletely);
            onView(withText(R.string.app_shortdesc)).check(assertShownCompletely);
            onView(allOf( withId(R.id.icon), hasDrawable(R.drawable.ic_action_suntimes_huge),
                    hasSibling(withText(R.string.app_name))
            )).check(assertShownCompletely);
            return this;
        }
        public WelcomeActivityRobot assertAboutPageIsShown(Context context)
        {
            onView(allOf(withText(R.string.configAction_aboutWidget), withClassName(endsWith("Button"))))
                    .check(assertShownCompletely)
                    .check(assertClickable);
            //onView(allOf(withId(R.id.icon), hasDrawable(R.drawable.ic_action_suntimes_huge),
            //        hasSibling(withText(R.string.configAction_aboutWidget)))).check(assertShown);
            return this;
        }
        public WelcomeActivityRobot assertAppearancePageIsShown(Context context) {
            onView(allOf(withId(R.id.text_title), withText(R.string.configLabel_appearance))).check(assertShownCompletely);
            return this;
        }
        public WelcomeActivityRobot assertUIPageIsShown(Context context) {
            onView(allOf(withId(R.id.text_title), withText(R.string.configLabel_ui))).check(assertShownCompletely);
            return this;
        }
        public WelcomeActivityRobot assertLocationPageIsShown(Context context) {
            onView(allOf(withId(R.id.txt_title), withText(R.string.configLabel_location))).check(assertShownCompletely);
            return this;
        }
        public WelcomeActivityRobot assertTimeZonePageIsShown(Context context) {
            onView(allOf(withId(R.id.txt_about_name), withText(R.string.configLabel_timezone))).check(assertShownCompletely);
            return this;
        }
        public WelcomeActivityRobot assertAlarmsPageIsShown(Context context) {
            onView(allOf(withId(R.id.txt_title), withText(R.string.configLabel_alarmClock))).check(assertShownCompletely);
            return this;
        }

        protected int numPages() {
            return 7;
        }
    }
}
