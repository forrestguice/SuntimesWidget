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


import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceActivity;
import android.support.test.InstrumentationRegistry;

import android.support.test.espresso.matcher.PreferenceMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.PreferenceMatchers.withTitle;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.DialogTest.cancelLightmapDialog;
import static com.forrestguice.suntimeswidget.DialogTest.showLightmapDialog;
import static com.forrestguice.suntimeswidget.DialogTest.verifyLightmapDialog;
import static com.forrestguice.suntimeswidget.LocationDialogTest.applyLocationDialog;
import static com.forrestguice.suntimeswidget.LocationDialogTest.showLocationDialog;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesSettingsActivityTest extends SuntimesActivityTestBase
{
    /**
     * test_showSettingsActivity
     */
    @Test
    public void test_showSettingsActivity()
    {
        showSettingsActivity(activityRule.getActivity());
        captureScreenshot("suntimes-activity-settings0");
    }

    public static void showSettingsActivity(Activity activity)
    {
        String actionSettingsText = activity.getString(R.string.configAction_settings);
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(actionSettingsText)).perform(click());
        verifySettingsActivity(activity);
    }

    public static void verifySettingsActivity(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_general))).check(assertShown);
        onView(withText(activity.getString(R.string.configLabel_locale))).check(assertShown);
        onView(withText(activity.getString(R.string.configLabel_places))).check(assertShown);
        onView(withText(activity.getString(R.string.configLabel_ui))).check(assertShown);
        onView(withText(activity.getString(R.string.configLabel_widgetList))).check(assertShown);
    }

    /**
     * test_showSettingsActivity_general
     */
    @Test
    public void test_showSettingsActivity_general()
    {
        showSettingsActivity(activityRule.getActivity());
        showGeneralSettings(activityRule.getActivity());
        captureScreenshot("suntimes-activity-settings-general0");
    }

    public static void showGeneralSettings(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_general))).perform(click());
        verifyGeneralSettings();
    }

    public static void verifyGeneralSettings()
    {
        // TODO
    }

    /**
     * test_showSettingsActivity_locale
     */
    @Test
    public void test_showSettingsActivity_locale()
    {
        showSettingsActivity(activityRule.getActivity());
        showLocaleSettings(activityRule.getActivity());
        captureScreenshot("suntimes-activity-settings-locale0");
    }

    public static void showLocaleSettings(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_locale))).perform(click());
        verifyLocaleSettings();
    }

    public static void verifyLocaleSettings()
    {
        // TODO
    }

    /**
     * test_showSettingsActivity_places
     */
    @Test
    public void test_showSettingsActivity_places()
    {
        showSettingsActivity(activityRule.getActivity());
        showPlacesSettings(activityRule.getActivity());
        captureScreenshot("suntimes-activity-settings-places0");
    }

    public static void showPlacesSettings(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_places))).perform(click());
        verifyPlacesSettings();
    }

    public static void verifyPlacesSettings()
    {
        // TODO
    }

    /**
     * test_showSettingsActivity_ui
     */
    @Test
    public void test_showSettingsActivity_ui()
    {
        showSettingsActivity(activityRule.getActivity());
        showUISettings(activityRule.getActivity());
        captureScreenshot("suntimes-activity-settings-ui0");
    }

    public static void showUISettings(Activity activity)
    {
        onView(withText(activity.getString(R.string.configLabel_ui))).perform(click());
        verifyUISettings();
    }

    public static void verifyUISettings()
    {
        // TODO
    }


}
