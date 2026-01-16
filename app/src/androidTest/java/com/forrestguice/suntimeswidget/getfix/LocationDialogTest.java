/**
    Copyright (C) 2017-2025 Forrest Guice
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
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/**
 * Automated UI tests for the LocationDialog.
 */
@LargeTest
@BehaviorTest
@RunWith(SuntimesJUnitTestRunner.class)
public class LocationDialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class, false, false);

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

    /**
     * UI Test
     * Show the location dialog, rotate, swap modes, rotate, repeatedly swap modes, and then cancel the dialog.
     */
    @Test @QuickTest
    public void test_locationDialog()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        LocationDialogRobot robot = new LocationDialogRobot();
        robot.showDialog(activity)
                .captureScreenshot(activity, "suntimes-dialog-location0")
                .assertDialogShown(activity)
                .cancelDialog(activityRule.getActivity());
    }

    @Test
    public void test_locationDialog_current()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        LocationDialogRobot robot = new LocationDialogRobot();
        robot.showDialog(activity)
                .assertDialogShown(activity);

        robot.selectLocationMode(LocationMode.CURRENT_LOCATION)
                .assertDialogMode_isCurrent()
                .doubleRotateDevice(activityRule.getActivity())
                .assertDialogMode_isCurrent();

        robot.selectLocationMode(LocationMode.CUSTOM_LOCATION)
                .doubleRotateDevice(activityRule.getActivity())
                .assertDialogMode_isCustom()
                .selectLocationMode(LocationMode.CURRENT_LOCATION)
                .selectLocationMode(LocationMode.CUSTOM_LOCATION)
                .assertDialogMode_isCustom();

        robot.cancelDialog(activityRule.getActivity());
    }

    @Test
    public void test_locationDialog_custom()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        LocationDialogRobot robot = new LocationDialogRobot();
        robot.showDialog(activity)
                .assertDialogShown(activity);

        robot.selectLocationMode(LocationMode.CUSTOM_LOCATION)
                .assertDialogMode_isCustom()
                .assertDialogState_select()
                .doubleRotateDevice(activity).sleep(1000)
                .assertDialogMode_isCustom()
                .assertDialogState_select();

        // edit -> save
        robot.clickLocationEditButton()
                .assertDialogState_edit()
                .inputLocationEditValues(TESTLOC_0_LABEL, TESTLOC_0_LAT, TESTLOC_0_LON)

                .doubleRotateDevice(activity).sleep(1000)
                .assertDialogState_edit()
                .assertLocationEditCoordinates(TESTLOC_0_LAT, TESTLOC_0_LON)

                .clickLocationEditSaveButton()
                .assertLocationEditCoordinates(TESTLOC_0_LAT, TESTLOC_0_LON);

        // edit -> cancel
        robot.clickLocationEditButton()
                .inputLocationEditValues("1", "2", "3")
                .assertLocationEditCoordinates("2", "3")
                .clickLocationEditCancelButton()
                .assertDialogState_select()
                .assertLocationEditCoordinates(TESTLOC_0_LAT, TESTLOC_0_LON);

        robot.selectLocationMode(LocationMode.CURRENT_LOCATION)
                .doubleRotateDevice(activity).sleep(1000)
                .assertDialogMode_isCurrent()
                .selectLocationMode(LocationMode.CUSTOM_LOCATION)
                .selectLocationMode(LocationMode.CURRENT_LOCATION)
                .assertDialogMode_isCurrent();

        robot.cancelDialog(activity);
    }

    @Test
    public void test_locationDialog_custom_placesActivity()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        LocationDialogRobot robot = new LocationDialogRobot();
        robot.showDialog(activity)
                .assertDialogShown(activity);

        robot.selectLocationMode(LocationMode.CUSTOM_LOCATION)
                .assertDialogMode_isCustom()
                .assertDialogState_select();

        robot.clickLocationListButton();
        new PlacesActivityTest.PlacesActivityRobot()
                .assertActivityShown(activity)
                .clickOnItem(0)
                .clickAcceptButton();
        robot.assertDialogShown(activity);
        // TODO: verify place shown

        robot.cancelDialog(activity);
    }

    private void editLocation( String name, String latitude, String longitude )
    {
        // click on the `edit` button
        LocationDialogRobot robot = new LocationDialogRobot();
        robot.clickLocationEditButton()
                .doubleRotateDevice(activityRule.getActivity())
                .assertDialogState_edit();

        // fill in form fields
        robot.inputLocationEditValues(name, latitude, longitude)
                .doubleRotateDevice(activityRule.getActivity())
                .assertLocationEditCoordinates(latitude, longitude);

        // click the `save` button
        robot.clickLocationEditSaveButton()
                .assertDialogState_select()
                .assertLocationEditCoordinates(latitude, longitude);
        //onView(withId(R.id.appwidget_location_nameSelect)).check(matches(withSpinnerText(containsString(name))));  // selected name matches input
    }

    /**
     * LocationDialogRobot
     */
    public static class LocationDialogRobot extends DialogTest.DialogRobot<LocationDialogRobot>
    {
        public LocationDialogRobot() {
            super();
            setRobot(this);
        }

        public LocationDialogRobot showDialog(Activity activity) {
            onView(ViewMatchers.withId(R.id.action_location_add)).perform(click());   // show dialog from actionbar
            return this;
        }
        @Override
        public LocationDialogRobot applyDialog(Context context) {
            onView(withId(R.id.dialog_button_accept)).perform(click());
            return this;
        }
        @Override
        public LocationDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.dialog_button_cancel)).perform(click());
            return this;
        }

        public LocationDialogRobot selectLocationMode( LocationMode mode )
        {
            onView(withId(R.id.appwidget_location_mode)).perform(click());
            onData(allOf(is(instanceOf(LocationMode.class)), is(mode)))
                    .inRoot(isPlatformPopup()).perform(click());
            return this;
        }

        public LocationDialogRobot clickLocationListButton() {
            onView(withId(R.id.appwidget_location_list)).perform(click());
            return this;
        }

        public LocationDialogRobot clickLocationEditButton() {
            onView(withId(R.id.appwidget_location_edit)).perform(click());
            return this;
        }
        public LocationDialogRobot clickLocationEditSaveButton() {
            onView(withId(R.id.appwidget_location_save)).perform(click());
            return this;
        }
        public LocationDialogRobot clickLocationEditCancelButton() {
            onView(withId(R.id.appwidget_location_cancel)).perform(click());
            return this;
        }
        public LocationDialogRobot inputLocationEditValues(String name, String latitude, String longitude) {
            inputLocationEditName(name);
            inputLocationEditLatitude(latitude);
            inputLocationEditLongitude(longitude);
            return this;
        }
        public LocationDialogRobot inputLocationEditName(String name) {
            onView(withId(R.id.appwidget_location_name)).perform(replaceText(name));    // fill in name
            return this;
        }
        public LocationDialogRobot inputLocationEditLatitude(String lat) {
            onView(withId(R.id.appwidget_location_lat)).perform(replaceText(lat));      // latitude and
            return this;
        }
        public LocationDialogRobot inputLocationEditLongitude(String lon) {
            onView(withId(R.id.appwidget_location_lon)).perform(replaceText(lon));      // longitude fields
            return this;
        }

        @Override
        public LocationDialogRobot assertDialogShown(Context context)
        {
            if (detectLocationMode() == LocationMode.CURRENT_LOCATION)
                assertDialogMode_isCurrent();
            else assertDialogMode_isCustom();
            return this;
        }

        public LocationDialogRobot assertDialogMode_isCurrent()
        {
            onView(withId(R.id.appwidget_location_auto)).check( ViewAssertionHelper.assertEnabled );
            //onView(withId(R.id.appwidget_location_name)).check( ViewAssertionHelper.assertHidden );        // name textedit hidden
            onView(withId(R.id.appwidget_location_nameSelect)).check( ViewAssertionHelper.assertDisabled ); // name selector disabled
            onView(withId(R.id.appwidget_location_lat)).check( ViewAssertionHelper.assertDisabled );       // lat field disabled
            onView(withId(R.id.appwidget_location_lon)).check( ViewAssertionHelper.assertDisabled );       // lon field disabled
            onView(withId(R.id.appwidget_location_edit)).check( ViewAssertionHelper.assertHidden );        // edit button is hidden
            onView(withId(R.id.appwidget_location_save)).check( ViewAssertionHelper.assertHidden );        // save button hidden
            onView(withId(R.id.appwidget_location_getfix)).check( ViewAssertionHelper.assertHidden );
            return this;
        }
        public LocationDialogRobot assertDialogMode_isCustom()
        {
            if (viewIsDisplayed(R.id.appwidget_location_nameSelect))
                assertDialogState_select();
            else assertDialogState_edit();
            return this;
        }

        public LocationDialogRobot assertDialogState_select()
        {
            onView(withId(R.id.appwidget_location_name)).check( ViewAssertionHelper.assertHidden );        // name textedit hidden
            onView(withId(R.id.appwidget_location_nameSelect)).check( ViewAssertionHelper.assertEnabled ); // name selector enabled
            onView(withId(R.id.appwidget_location_lat)).check( ViewAssertionHelper.assertDisabled );       // lat field disabled
            onView(withId(R.id.appwidget_location_lon)).check( ViewAssertionHelper.assertDisabled );       // lon field disabled
            onView(withId(R.id.appwidget_location_edit)).check( ViewAssertionHelper.assertShown );         // edit button is shown
            onView(withId(R.id.appwidget_location_list)).check( ViewAssertionHelper.assertShown );         // list button is shown
            onView(withId(R.id.appwidget_location_save)).check( ViewAssertionHelper.assertHidden );        // save button hidden
            onView(withId(R.id.appwidget_location_getfix)).check( ViewAssertionHelper.assertHidden );      // gps button is hidden
            onView(withId(R.id.appwidget_location_auto)).check( ViewAssertionHelper.assertHidden );
            return this;
        }
        public LocationDialogRobot assertDialogState_edit()
        {
            onView(withId(R.id.appwidget_location_name)).check( ViewAssertionHelper.assertShown );        // name textedit enabled (and shown)
            onView(withId(R.id.appwidget_location_nameSelect)).check(ViewAssertionHelper.assertHidden);   // name selector hidden
            onView(withId(R.id.appwidget_location_lat)).check( ViewAssertionHelper.assertEnabled );       // lat, lon now enabled
            onView(withId(R.id.appwidget_location_lon)).check( ViewAssertionHelper.assertEnabled );
            onView(withId(R.id.appwidget_location_edit)).check( ViewAssertionHelper.assertHidden );       // edit button is hidden
            onView(withId(R.id.appwidget_location_list)).check( ViewAssertionHelper.assertHidden );       // list button is hidden
            onView(withId(R.id.appwidget_location_save)).check( ViewAssertionHelper.assertShown );        // save button now shown
            onView(withId(R.id.appwidget_location_getfix)).check( ViewAssertionHelper.assertEnabled );    // gps button is enabled
            onView(withId(R.id.appwidget_location_auto)).check( ViewAssertionHelper.assertHidden );
            return this;
        }

        public LocationDialogRobot assertLocationEditCoordinates(String latitude, String longitude) {
            assertLocationEditLatitude(latitude);
            assertLocationEditLongitude(longitude);
            return this;
        }
        public LocationDialogRobot assertLocationEditLatitude(String latitude) {
            onView(withId(R.id.appwidget_location_lat)).check(matches(withText(latitude)));
            return this;
        }
        public LocationDialogRobot assertLocationEditLongitude(String longitude) {
            onView(withId(R.id.appwidget_location_lon)).check(matches(withText(longitude)));
            return this;
        }

        public static LocationMode detectLocationMode()
        {
            if (spinnerDisplaysText(R.id.appwidget_location_mode, LocationMode.CURRENT_LOCATION.toString()))
                return LocationMode.CURRENT_LOCATION;
            else if (spinnerDisplaysText(R.id.appwidget_location_mode, LocationMode.CUSTOM_LOCATION.toString()))
                return LocationMode.CUSTOM_LOCATION;
            else
                return null;   // unrecognized mode; fail with a null
        }

    }
}
