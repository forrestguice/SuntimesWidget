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

import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewAssertion;
import android.support.test.filters.LargeTest;

import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.hamcrest.Matcher;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.DialogTest.applyDateDialog;
import static com.forrestguice.suntimeswidget.DialogTest.cancelAlarmDialog;
import static com.forrestguice.suntimeswidget.DialogTest.cancelLightmapDialog;
import static com.forrestguice.suntimeswidget.DialogTest.showDateDialog;
import static com.forrestguice.suntimeswidget.DialogTest.showLightmapDialog;
import static com.forrestguice.suntimeswidget.DialogTest.verifyAlarmDialog;
import static com.forrestguice.suntimeswidget.DialogTest.verifyLightmapDialog;
import static com.forrestguice.suntimeswidget.LocationDialogTest.applyLocationDialog;
import static com.forrestguice.suntimeswidget.LocationDialogTest.showLocationDialog;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesActivityTest extends SuntimesActivityTestBase
{
    @Test
    public void test_activity()
    {
        verifyActivity();
        captureScreenshot("suntimes-activity-main0");

        rotateDevice();
        verifyActivity();
    }

    public void verifyActivity()
    {
        verifyClock();
        verifyNote();
        verifyCard();
        verifyLightmap();
    }

    public void verifyClock()
    {
        Calendar now = Calendar.getInstance();
        SuntimesUtils.TimeDisplayText timeText = SuntimesActivity.utils.calendarTimeShortDisplayString(activityRule.getActivity(), now);
        String timezoneString = WidgetSettings.loadTimezonePref(activityRule.getActivity(), 0) + " ";

        onView(withId(R.id.text_time)).check(assertShown);
        onView(withId(R.id.text_time)).check(matches(withText(timeText.getValue())));

        onView(withId(R.id.text_time_suffix)).check(assertShown);
        onView(withId(R.id.text_time_suffix)).check(matches(withText(timeText.getSuffix())));

        onView(withId(R.id.text_timezone)).check(assertShown);
        onView(withId(R.id.text_timezone)).check(matches(withText(timezoneString)));
    }

    public void verifyNote()
    {
        onView(withId(R.id.info_note_flipper)).check(assertShown);
    }

    public void verifyCard()
    {
        onView(withId(R.id.info_time_flipper)).check(assertShown);
    }

    public void verifyLightmap()
    {
        if (AppSettings.loadShowLightmapPref(activityRule.getActivity()))
        {
            onView(withId(R.id.info_time_lightmap)).check(assertShown);
        } else {
            onView(withId(R.id.info_time_lightmap)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void test_onLightmapClick()
    {
        if (AppSettings.loadShowLightmapPref(activityRule.getActivity()))
        {
            showLightmapDialog(activityRule.getActivity());
            cancelLightmapDialog();

            onView(withId(R.id.info_time_lightmap)).perform(longClick());
            verifyLightmapDialog();
            cancelLightmapDialog();

            onView(withId(R.id.info_time_lightmap)).perform(swipeRight());
            verifyLightmapDialog();
            cancelLightmapDialog();

        } else {
            onView(withId(R.id.info_time_lightmap)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void test_onClockClick()
    {
        onView(withId(R.id.layout_clock)).perform(click());

        AppSettings.ClockTapAction tapAction = AppSettings.loadClockTapActionPref(activityRule.getActivity());
        if (tapAction == AppSettings.ClockTapAction.ALARM)
        {
            verifyAlarmDialog();
            cancelAlarmDialog();

        } else if (tapAction == AppSettings.ClockTapAction.PREV_NOTE) {
            // TODO
        } else if (tapAction == AppSettings.ClockTapAction.NEXT_NOTE) {
            // TODO
        } else {
            // TODO
        }
    }

    /**
     * appCrash74
     *
     * Test app crash (latitude edge case) described in issue #74.
     */
    @Test
    public void test_appCrash74()
    {
        showLocationDialog();

        onView(withId(R.id.appwidget_location_edit)).perform(click());
        String label = "Test Location";
        String latitude = "83.124";
        String longitude = "23.1592";
        onView(withId(R.id.appwidget_location_name)).perform(replaceText(label));
        onView(withId(R.id.appwidget_location_lat)).perform(replaceText(latitude));
        onView(withId(R.id.appwidget_location_lon)).perform(replaceText(longitude));

        applyLocationDialog(activityRule.getActivity());

        // open the date dialog (from overflow menu)
        showDateDialog(activityRule.getActivity());
        onView(withId(R.id.appwidget_date_mode)).perform(click());
        //String customDateModeString = activityRule.getActivity().getString(R.string.dateMode_custom);
        //onData(allOf(is(instanceOf(WidgetSettings.DateMode.class)), withMyValue(customDateModeString))).perform(click());

        //int year = 2017;
        //int month = 1;
        //int day = 19;
        //onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month + 1, day));

        applyDateDialog(activityRule.getActivity());
    }

    /***
     * userSwappedCard_withNextButton, userSwappedCard_withSwipe
     *
     * Test the userSwappedCard flag; when the user swaps the time card (today/tomorrow) that
     * selection should not be reverted by later ui updates. Reproduces the bug from issue #20.
     */

    @Test
    public void test_userSwappedCard_withButton()
    {
        userSwappedCard(false);
    }
    @Test
    public void test_userSwappedCard_withSwipe()
    {
        userSwappedCard(true);
    }

    private void userSwappedCard(boolean useSwipe)
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
        onView(nextButton).check(assertIsDisplayed);    // and "next" should be visible

        // click the next button
        if (useSwipe)
        {
            onView(cardFlipper).perform(swipeLeft());
        } else {
            onView(nextButton).perform(click());
        }

        // post-click checks
        onView(tomorrowCard).check(assertIsDisplayed);  // flipper should now display tomorrow
        onView(prevButton).check(assertIsDisplayed);    // "prev" should be visible

        // wait a minute (and check again)
        long waitTime = 60 * 1000;
        IdlingResource waitForResource = new ElapsedTimeIdlingResource(waitTime);
        IdlingPolicies.setMasterPolicyTimeout(waitTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitTime * 2, TimeUnit.MILLISECONDS);

        // during that minute
        // the app will update the clock and note area at least once

        registerIdlingResources(waitForResource);       // afterward...
        onView(tomorrowCard).check(assertIsDisplayed);  // should still display tomorrow
        onView(prevButton).check(assertIsDisplayed);    // and "prev" should still be visible
        unregisterIdlingResources(waitForResource);
    }
}
