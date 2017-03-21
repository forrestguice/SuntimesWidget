/**
    Copyright (C) 2017 Forrest Guice
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

import android.support.annotation.Nullable;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesActivityTest
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Test
    public void userSwappedCard_withNextButton()
    {
        Matcher<View> cardFlipper = withId(R.id.info_time_flipper);
        Matcher<View> todayCard = withId(R.id.info_time_all_today);
        Matcher<View> tomorrowCard = withId(R.id.info_time_all_tomorrow);
        Matcher<View> nextButton = allOf(withId(R.id.info_time_nextbtn), isDescendantOfA(todayCard));
        Matcher<View> prevButton = allOf(withId(R.id.info_time_prevbtn), isDescendantOfA(tomorrowCard));

        ViewAssertion assertIsDisplayed = matches(isDisplayed());

        // pre-click checks
        onView(cardFlipper).check(assertIsDisplayed);   // flipper should be visible
        onView(todayCard).check(assertIsDisplayed);     // and should display today
        onView(nextButton).check(assertIsDisplayed);    // "next" should be visible

        // click the next button
        onView(nextButton).perform(click());

        // post-click checks
        onView(tomorrowCard).check(assertIsDisplayed);  // flipper should now display tomorrow
        onView(prevButton).check(assertIsDisplayed);    // "prev" should be visible

        // wait a minute (and check again)
        long waitTime = 60 * 1000;
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);

        IdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        registerIdlingResources(waitForResource);

        onView(tomorrowCard).check(assertIsDisplayed);  // should still display tomorrow
        onView(prevButton).check(assertIsDisplayed);    // and "prev" should still be visible

        unregisterIdlingResources(waitForResource);
    }

    @Test
    public void userSwappedCard_withSwipe()
    {
        Matcher<View> cardFlipper = withId(R.id.info_time_flipper);
        Matcher<View> todayCard = withId(R.id.info_time_all_today);
        Matcher<View> tomorrowCard = withId(R.id.info_time_all_tomorrow);
        Matcher<View> nextButton = allOf(withId(R.id.info_time_nextbtn), isDescendantOfA(todayCard));
        Matcher<View> prevButton = allOf(withId(R.id.info_time_prevbtn), isDescendantOfA(tomorrowCard));

        ViewAssertion assertIsDisplayed = matches(isDisplayed());

        // pre-click checks
        onView(cardFlipper).check(assertIsDisplayed);   // flipper should be visible
        onView(todayCard).check(assertIsDisplayed);     // and should display today
        onView(nextButton).check(assertIsDisplayed);    // "next" should be visible

        // click the next button
        onView(cardFlipper).perform(swipeLeft());

        // post-click checks
        onView(tomorrowCard).check(assertIsDisplayed);  // flipper should now display tomorrow
        onView(prevButton).check(assertIsDisplayed);    // "prev" should be visible

        // wait a minute (and check again)
        long waitTime = 60 * 1000;
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);

        IdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        registerIdlingResources(waitForResource);

        onView(tomorrowCard).check(assertIsDisplayed);  // should still display tomorrow
        onView(prevButton).check(assertIsDisplayed);    // and "prev" should still be visible

        unregisterIdlingResources(waitForResource);
    }

    public class ElapsedTimeIdlingResource implements IdlingResource
    {
        private final long startTime;
        private final long waitingTime;
        private ResourceCallback resourceCallback;

        public ElapsedTimeIdlingResource(long waitingTime) {
            this.startTime = System.currentTimeMillis();
            this.waitingTime = waitingTime;
        }

        @Override
        public String getName() {
            return ElapsedTimeIdlingResource.class.getName() + ":" + waitingTime;
        }

        @Override
        public boolean isIdleNow() {
            long elapsed = System.currentTimeMillis() - startTime;
            boolean idle = (elapsed >= waitingTime);
            if (idle) {
                resourceCallback.onTransitionToIdle();
            }
            return idle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }

}
