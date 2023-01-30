/**
    Copyright (C) 2017-2019 Forrest Guice
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

import android.content.Context;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/**
 * Automated UI tests for the LocationConfigDialog.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LocationDialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    /**
     * UI Test
     * Set the location using the location dialog.
     */
    @Test
    public void test_setLocation()
    {
        String[] name = {TESTLOC_0_LABEL, TESTLOC_1_LABEL};
        String[] lat  = {TESTLOC_0_LAT, TESTLOC_1_LAT };
        String[] lon  = {TESTLOC_0_LON, TESTLOC_1_LON };

        int n = name.length;
        int i = new Random().nextInt(n);
        setLocation(name[i], lat[i], lon[i]);
        // TODO: verify action (may need to move this test to SuntimesActivityTest)
    }

    public void setLocation( String name, String latitude, String longitude )
    {
        showLocationDialog();
        inputLocationDialog_mode(WidgetSettings.LocationMode.CUSTOM_LOCATION);
        editLocation(name, latitude, longitude);
        applyLocationDialog(activityRule.getActivity());
    }

    /**
     * UI Test
     * Set the mode to "current location" using the location dialog.
     */
    @Test
    public void test_setLocationCurrent()
    {
        setLocationMode(WidgetSettings.LocationMode.CURRENT_LOCATION);
        // TODO: verify action (may need to move this test to SuntimesActivityTest)
    }

    public void setLocationMode( WidgetSettings.LocationMode mode )
    {
        showLocationDialog();
        inputLocationDialog_mode(mode);
        applyLocationDialog(activityRule.getActivity());
    }

    /**
     * UI Test
     * Show the location dialog, rotate, swap modes, rotate, repeatedly swap modes, and then cancel the dialog.
     */
    @Test
    public void test_showLocationDialog()
    {
        showLocationDialog();
        captureScreenshot(activityRule.getActivity(), "suntimes-dialog-location0");

        if (getLocationDialog_mode() == WidgetSettings.LocationMode.CURRENT_LOCATION)
        {
            // testing "current" mode
            verifyLocationDialogMode_current();        // should be in "current" mode
            rotateDevice(activityRule);                            // rotate
            verifyLocationDialogMode_current();        // should still be "current" mode

            inputLocationDialog_mode(WidgetSettings.LocationMode.CUSTOM_LOCATION);  // swap modes to "custom"
            rotateDevice(activityRule);                               // rotate
            verifyLocationDialogMode_custom();            // should still be in "custom" mode

            inputLocationDialog_mode(WidgetSettings.LocationMode.CURRENT_LOCATION);  // repeatedly swap
            inputLocationDialog_mode(WidgetSettings.LocationMode.CUSTOM_LOCATION);

        } else {
            // testing "custom" mode
            verifyLocationDialogState_select();         // should start in "select" state
            rotateDevice(activityRule);                             // rotate
            verifyLocationDialogState_select();         // should still be in select state

            inputLocationDialog_mode(WidgetSettings.LocationMode.CURRENT_LOCATION);  // swap modes to "current"
            rotateDevice(activityRule);                               // rotate
            verifyLocationDialogMode_current();           // should still be in "current" mode

            inputLocationDialog_mode(WidgetSettings.LocationMode.CUSTOM_LOCATION);  // repeatedly swap
            inputLocationDialog_mode(WidgetSettings.LocationMode.CURRENT_LOCATION);
        }

        cancelLocationDialog(activityRule.getActivity());
    }

    public static void showLocationDialog()
    {
        showLocationDialog(true);
    }
    public static void showLocationDialog(boolean verify)
    {
        onView(withId(R.id.action_location_add)).perform(click());   // show dialog from actionbar
        if (verify) {
            verifyLocationDialog();
        }
    }

    public static void editLocationDialog()
    {
        editLocationDialog(true);
    }
    public static void editLocationDialog(boolean verify)
    {
        onView(withId(R.id.appwidget_location_edit)).perform(click());   // click edit
        if (verify) {
            verifyLocationDialogState_edit();                                // verify edit state
        }
    }

    public static void saveLocationDialog()
    {
        onView(withId(R.id.appwidget_location_save)).perform(click());   // click save
        verifyLocationDialogState_select();                              // verify select state
    }

    public static void inputLocationDialog_edit(String name, String lat, String lon)
    {
        onView(withId(R.id.appwidget_location_name)).perform(replaceText(name));    // fill in name
        onView(withId(R.id.appwidget_location_lat)).perform(replaceText(lat));      // latitude and
        onView(withId(R.id.appwidget_location_lon)).perform(replaceText(lon));      // longitude fields
    }

    public static void inputLocationDialog_mode( WidgetSettings.LocationMode mode )
    {
        onView(withId(R.id.appwidget_location_mode)).perform(click());
        onData(allOf(is(instanceOf(WidgetSettings.LocationMode.class)), is(mode)))
                .inRoot(isPlatformPopup()).perform(click());

        if (mode == WidgetSettings.LocationMode.CURRENT_LOCATION)
            verifyLocationDialogMode_current();
        else verifyLocationDialogMode_custom();
    }

    public static WidgetSettings.LocationMode getLocationDialog_mode()
    {
        if (spinnerDisplaysText(R.id.appwidget_location_mode, WidgetSettings.LocationMode.CURRENT_LOCATION.toString()))
            return WidgetSettings.LocationMode.CURRENT_LOCATION;
        else if (spinnerDisplaysText(R.id.appwidget_location_mode, WidgetSettings.LocationMode.CUSTOM_LOCATION.toString()))
            return WidgetSettings.LocationMode.CUSTOM_LOCATION;
        else
            return null;   // unrecognized mode; fail with a null
    }

    public static void applyLocationDialog(Context context)
    {
        //String setLocationText = context.getString(R.string.location_dialog_ok);
        //onView(withText(setLocationText)).perform(click());
        onView(withId(R.id.dialog_button_accept)).perform(click());

        // TODO: verify action
        //onView(allOf(isDescendantOfA(withResourceName("app_menubar")), withText(name))).check( assertShown );      // activity title should now be updated
        //onView(allOf(isDescendantOfA(withResourceName("app_menubar")), withText(latitude))).check( assertShown );
        //onView(allOf(isDescendantOfA(withResourceName("app_menubar")), withText(longitude))).check( assertShown );
    }

    public static void cancelLocationDialog(Context context)
    {
        //String setLocationText = context.getString(R.string.location_dialog_cancel);
        //onView(withText(setLocationText)).perform(click());
        onView(withId(R.id.dialog_button_cancel)).perform(click());
    }

    public void editLocation( String name, String latitude, String longitude )
    {
        // click on the `edit` button
        editLocationDialog();                                        // click edit
        rotateDevice(activityRule);
        verifyLocationDialogState_edit();

        // fill in form fields
        inputLocationDialog_edit(name, latitude, longitude);         // input values
        rotateDevice(activityRule);
        onView(withId(R.id.appwidget_location_lat)).check(matches(withText(latitude)));    // lat, lon fields match inputs
        onView(withId(R.id.appwidget_location_lon)).check(matches(withText(longitude)));

        // click the `save` button
        saveLocationDialog();                                        // click save
        onView(withId(R.id.appwidget_location_lat)).check(matches(withText(latitude)));    // lat, lon fields match inputs
        onView(withId(R.id.appwidget_location_lon)).check(matches(withText(longitude)));
        //onView(withId(R.id.appwidget_location_nameSelect)).check(matches(withSpinnerText(containsString(name))));  // selected name matches input
    }

    public static void verifyLocationDialog()
    {
        if (getLocationDialog_mode() == WidgetSettings.LocationMode.CURRENT_LOCATION)
            verifyLocationDialogMode_current();
        else verifyLocationDialogMode_custom();
    }

    public static void verifyLocationDialogMode_current()
    {
        onView(withId(R.id.appwidget_location_auto)).check( assertEnabled );
        onView(withId(R.id.appwidget_location_name)).check( assertHidden );        // name textedit hidden
        onView(withId(R.id.appwidget_location_nameSelect)).check( assertDisabled ); // name selector disabled
        onView(withId(R.id.appwidget_location_lat)).check( assertDisabled );       // lat field disabled
        onView(withId(R.id.appwidget_location_lon)).check( assertDisabled );       // lon field disabled
        onView(withId(R.id.appwidget_location_edit)).check( assertHidden );        // edit button is hidden
        onView(withId(R.id.appwidget_location_save)).check( assertHidden );        // save button hidden
        onView(withId(R.id.appwidget_location_getfix)).check( assertHidden );
    }

    public static void verifyLocationDialogMode_custom()
    {
        if (viewIsDisplayed(R.id.appwidget_location_nameSelect))
            verifyLocationDialogState_select();
        else verifyLocationDialogState_edit();
    }

    public static void verifyLocationDialogState_select()
    {
        onView(withId(R.id.appwidget_location_name)).check( assertHidden );        // name textedit hidden
        onView(withId(R.id.appwidget_location_nameSelect)).check( assertEnabled ); // name selector enabled
        onView(withId(R.id.appwidget_location_lat)).check( assertDisabled );       // lat field disabled
        onView(withId(R.id.appwidget_location_lon)).check( assertDisabled );       // lon field disabled
        onView(withId(R.id.appwidget_location_edit)).check( assertShown );         // edit button is shown
        onView(withId(R.id.appwidget_location_save)).check( assertHidden );        // save button hidden
        onView(withId(R.id.appwidget_location_getfix)).check( assertHidden );      // gps button is hidden
        onView(withId(R.id.appwidget_location_auto)).check( assertHidden );
    }

    public static void verifyLocationDialogState_edit()
    {
        onView(withId(R.id.appwidget_location_name)).check( assertFocused );      // name textedit enabled (and focused)
        onView(withId(R.id.appwidget_location_nameSelect)).check(assertHidden);   // name selector hidden
        onView(withId(R.id.appwidget_location_lat)).check( assertEnabled );       // lat, lon now enabled
        onView(withId(R.id.appwidget_location_lon)).check( assertEnabled );
        onView(withId(R.id.appwidget_location_edit)).check( assertHidden );       // edit button is hidden
        onView(withId(R.id.appwidget_location_save)).check( assertShown );        // save button now shown
        onView(withId(R.id.appwidget_location_getfix)).check( assertEnabled );    // gps button is enabled
        onView(withId(R.id.appwidget_location_auto)).check( assertHidden );
    }
}
