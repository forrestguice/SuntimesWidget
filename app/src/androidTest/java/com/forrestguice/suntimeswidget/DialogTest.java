/**
    Copyright (C) 2017-2018 Forrest Guice
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
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.AppSettings;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasLinks;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DialogTest extends SuntimesActivityTestBase
{
    /**
     * UI Test
     *
     * Show, rotate, and dismiss the lightmap dialog.
     */
    @Test
    public void test_showLightmapDialog()
    {
        Context context = activityRule.getActivity();
        if (AppSettings.loadShowLightmapPref(context))
        {
            showLightmapDialog(context);
            captureScreenshot("suntimes-dialog-lightmap0");

            rotateDevice();
            verifyLightmapDialog();
            cancelLightmapDialog();

        } else {
            onView(withId(R.id.info_time_lightmap)).check(matches(not(isDisplayed())));
        }
    }

    public static void showLightmapDialog(Context context)
    {
        onView(withId(R.id.info_time_lightmap)).perform(click());
        verifyLightmapDialog();
    }

    public static void verifyLightmapDialog()
    {
        onView(withId(R.id.info_time_lightmap_key)).check(assertShown);
    }

    public static void cancelLightmapDialog()
    {
        onView(withId(R.id.info_time_lightmap_key)).perform(pressBack());
        onView(withId(R.id.info_time_lightmap_key)).check(doesNotExist());
    }

    /**
     *  UI Test
     *
     *  Show, rotate, and dismiss the solstice/equinox dialog.
     */
    @Test
    public void test_showEquinoxDialog()
    {
        Context context = activityRule.getActivity();
        if (AppSettings.loadShowEquinoxPref(context))
        {
            showEquinoxDialog(context);
            captureScreenshot("suntimes-dialog-equinox0");

            verifyEquinoxDialog();
            cancelEquinoxDialog();

        } else {
            onView(withId(R.id.info_time_equinox)).check(matches(not(isDisplayed())));
        }
    }

    public static void showEquinoxDialog(Context context)
    {
        String actionText = context.getString(R.string.configAction_equinoxDialog);
        openActionBarOverflowOrOptionsMenu(context);
        onView(withText(actionText)).perform(click());
        verifyEquinoxDialog();
    }

    public static void verifyEquinoxDialog()
    {
        onView(withId(R.id.info_time_equinox)).check(assertShown);
    }

    public static void cancelEquinoxDialog()
    {
        onView(withId(R.id.info_time_equinox)).perform(pressBack());
        onView(withId(R.id.info_time_equinox)).check(doesNotExist());
    }

    /**
     *  UI Test
     *
     *  Show, rotate, and dismiss the help dialog.
     */
    @Test
    public void test_showHelpDialog()
    {
        showHelpDialog(activityRule.getActivity());
        captureScreenshot("suntimes-dialog-help0");

        rotateDevice();
        verifyHelpDialog();
        cancelHelpDialog();
    }

    public static void showHelpDialog(Context context)
    {
        String actionHelpText = context.getString(R.string.configAction_help);
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(actionHelpText)).perform(click());
        verifyHelpDialog();
    }

    public static void verifyHelpDialog()
    {
        onView(withId(R.id.txt_help_content)).check(assertShown);
    }

    public static void cancelHelpDialog()
    {
        onView(withId(R.id.txt_help_content)).perform(pressBack());
        onView(withId(R.id.txt_help_content)).check(doesNotExist());
    }

    /**
     * UI Test
     *
     * Show, rotate, and dismiss the about dialog.
     */
    @Test
    public void test_showAboutDialog()
    {
        showAboutDialog(activityRule.getActivity());
        captureScreenshot("suntimes-dialog-about0");

        rotateDevice();
        verifyAboutDialog();
        cancelAboutDialog();
    }

    public static void showAboutDialog(Context context)
    {
        String actionAboutText = context.getString(R.string.configAction_aboutWidget);
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(actionAboutText)).perform(click());
        verifyAboutDialog();
    }

    public static void verifyAboutDialog()
    {
        onView(withId(R.id.txt_about_name)).check(assertShown);
        onView(withId(R.id.txt_about_desc)).check(assertShown);
        onView(withId(R.id.txt_about_version)).check(assertShown);

        onView(withId(R.id.txt_about_url)).check(assertShown);
        onView(withId(R.id.txt_about_url)).check(matches(hasLinks()));

        onView(withId(R.id.txt_about_support)).check(assertShown);
        onView(withId(R.id.txt_about_support)).check(matches(hasLinks()));

        onView(withId(R.id.txt_about_legal1)).check(assertShown);
        onView(withId(R.id.txt_about_legal2)).check(assertShown);
        onView(withId(R.id.txt_about_legal3)).check(assertShown);
    }

    public static void cancelAboutDialog()
    {
        onView(withId(R.id.txt_about_name)).perform(pressBack());
        onView(withId(R.id.txt_about_name)).check(doesNotExist());
    }

}
