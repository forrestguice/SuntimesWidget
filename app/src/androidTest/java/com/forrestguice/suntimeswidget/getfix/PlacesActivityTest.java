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

package com.forrestguice.suntimeswidget.getfix;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivityTest;
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
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class PlacesActivityTest extends SuntimesActivityTestBase
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
    public void test_PlacesActivity()
    {
        Activity activity = activityRule.getActivity();
        PlacesActivityRobot robot = new PlacesActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * PlacesActivityRobot
     */
    public static class PlacesActivityRobot extends ActivityRobot<PlacesActivityRobot>
    {
        public PlacesActivityRobot() {
            setRobot(this);
        }

        protected PlacesActivityRobot showActivity(Activity activity) {
            new SuntimesSettingsActivityTest.SettingsActivityRobot()
                    .showActivity(activity)
                    .clickHeader_placeSettings()
                    .clickPref_managePlaces();
            return this;
        }

        public PlacesActivityRobot sleep(long ms) {
            SystemClock.sleep(ms);
            return this;
        }
        public PlacesActivityRobot doubleRotateDevice(Activity activity)
        {
            rotateDevice(activity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            sleep(1000);
            rotateDevice(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return this;
        }
        public PlacesActivityRobot rotateDevice(Activity activity, int orientation) {
            activity.setRequestedOrientation(orientation);
            return this;
        }

        public PlacesActivityRobot clickBackButton(Context context) {
            // TODO
            return this;
        }
        public PlacesActivityRobot clickSearchButton(Context context) {
            onView(withContentDescription(R.string.configAction_search)).check(assertShown);
            return this;
        }
        public PlacesActivityRobot clickSearchClearButton(Context context) {
            // TODO
            return this;
        }
        public PlacesActivityRobot inputSearchField(Context context, String text) {
            // TODO
            return this;
        }

        public PlacesActivityRobot showOverflowMenu(Context context) {
            openActionBarOverflowOrOptionsMenu(context);
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_add(Context context) {
            onView(withText(R.string.configAction_addPlace)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_build(Context context) {
            onView(withText(R.string.configLabel_places_build)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_clear(Context context) {
            onView(withText(R.string.configAction_clearPlaces)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_import(Context context) {
            onView(withText(R.string.configAction_importPlaces)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot clickOverflowMenu_export(Context context) {
            onView(withText(R.string.configAction_exportPlaces)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public PlacesActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_clearPlaces)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public PlacesActivityRobot assertActivityShown(Context context)
        {
            //onView(withId(R.id.info_time_utc)).check(assertShown);
            // TODO: activityTitle
            // TODO: backButton
            // TODO: searchButton
            return this;
        }

        public PlacesActivityRobot assertSearchButtonShown(Context context, boolean isShown) {
            // TODO
            return this;
        }
        public PlacesActivityRobot assertSearchFieldShown(Context context, boolean isShown) {
            // TODO
            return this;
        }

        public PlacesActivityRobot assertListIsEmpty(Context context) {
            // TODO
            return this;
        }

        public PlacesActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_addPlace)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configLabel_places_build)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_clearPlaces)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_importPlaces)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_exportPlaces)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

    }
}
