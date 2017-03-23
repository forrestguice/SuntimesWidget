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

import android.content.Context;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LocationDialogTest extends SuntimesActivityTestBase
{
    @Test
    public void test_setLocation()
    {
        String name[] = {"Test Place", "Prescott"};
        String lat[]  = {"82",         "34.5555" };
        String lon[]  = {"22",         "-112.6321"};

        int n = name.length;
        int i = new Random().nextInt(n);
        setLocation(name[i], lat[i], lon[i]);
    }

    @Test
    public void test_showLocationDialog()
    {
        showLocationDialog();
        captureScreenshot("suntimes-dialog-location0");

        rotateDevice();
        verifyLocationDialogState_select();
        cancelLocationDialog(activityRule.getActivity());
    }

    public static void showLocationDialog()
    {
        onView(withId(R.id.action_location_add)).perform(click());
        verifyLocationDialogState_select();
    }

    public static void applyLocationDialog(Context context)
    {
        String setLocationText = context.getString(R.string.location_dialog_ok);
        onView(withText(setLocationText)).perform(click());

        //onView(allOf(isDescendantOfA(withResourceName("app_menubar")), withText(name))).check( assertShown );      // activity title should now be updated
        //onView(allOf(isDescendantOfA(withResourceName("app_menubar")), withText(latitude))).check( assertShown );
        //onView(allOf(isDescendantOfA(withResourceName("app_menubar")), withText(longitude))).check( assertShown );
    }

    public static void cancelLocationDialog(Context context)
    {
        String setLocationText = context.getString(R.string.location_dialog_cancel);
        onView(withText(setLocationText)).perform(click());
    }

    public void setLocation( String name, String latitude, String longitude )
    {
        showLocationDialog();
        rotateDevice();
        verifyLocationDialogState_select();
        editLocation(name, latitude, longitude);
        applyLocationDialog(activityRule.getActivity());
    }

    public void editLocation( String name, String latitude, String longitude )
    {
        // click on the `edit` button
        onView(withId(R.id.appwidget_location_edit)).perform(click());
        verifyLocationDialogState_edit();

        rotateDevice();
        verifyLocationDialogState_edit();

        // fill in form fields
        onView(withId(R.id.appwidget_location_name)).perform(replaceText(name));    // fill in name
        onView(withId(R.id.appwidget_location_lat)).perform(replaceText(latitude));  // latitude and
        onView(withId(R.id.appwidget_location_lon)).perform(replaceText(longitude));  // longitude fields

        rotateDevice();
        onView(withId(R.id.appwidget_location_lat)).check(matches(withText(latitude)));    // lat, lon fields match inputs
        onView(withId(R.id.appwidget_location_lon)).check(matches(withText(longitude)));

        // click the `save` button
        onView(withId(R.id.appwidget_location_save)).perform(click());
        verifyLocationDialogState_select();
        onView(withId(R.id.appwidget_location_lat)).check(matches(withText(latitude)));    // lat, lon fields match inputs
        onView(withId(R.id.appwidget_location_lon)).check(matches(withText(longitude)));
        //onView(withId(R.id.appwidget_location_nameSelect)).check(matches(withSpinnerText(containsString(name))));  // selected name matches input
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
    }
}
