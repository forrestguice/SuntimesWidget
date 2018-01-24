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
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.TimeZone;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TimeZoneDialogTest extends SuntimesActivityTestBase
{
    /**
     * UI Test
     */
    @Test
    public void test_showTimezoneDialog()
    {
        showTimezoneDialog(activityRule.getActivity());
        verifyTimezoneDialog(activityRule.getActivity());
        captureScreenshot("suntimes-dialog-timezone0");

        inputTimezoneDialog_mode(activityRule.getActivity(), WidgetSettings.TimezoneMode.CURRENT_TIMEZONE);
        captureScreenshot("suntimes-dialog-timezone-system0");

        inputTimezoneDialog_mode(activityRule.getActivity(), WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE);
        captureScreenshot("suntimes-dialog-timezone-custom0");

        inputTimezoneDialog_mode(activityRule.getActivity(), WidgetSettings.TimezoneMode.SOLAR_TIME);
        captureScreenshot("suntimes-dialog-timezone-solar0");
        rotateDevice();
        verifyTimezoneDialog_solar(activityRule.getActivity());

        cancelTimezoneDialog();
    }

    public static void showTimezoneDialog(Activity activity)
    {
        String actionTimezoneText = activity.getString(R.string.configAction_setTimeZone);
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(actionTimezoneText)).perform(click());
    }

    public static void verifyTimezoneDialog(Context context)
    {
        onView(withId(R.id.appwidget_timezone_mode)).check(assertShown);
        verifyTimezoneDialog(context, getTimezoneDialogMode());
    }

    public static void verifyTimezoneDialog(Context context, WidgetSettings.TimezoneMode mode )
    {
        switch (mode)
        {
            case SOLAR_TIME:
                verifyTimezoneDialog_solar(context);
                break;

            case CURRENT_TIMEZONE:
                verifyTimezoneDialog_system();
                break;

            case CUSTOM_TIMEZONE:
            default:
                verifyTimezoneDialog_custom(context);
                break;
        }
    }

    public static void verifyTimezoneDialog_solar(Context context)
    {
        onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(WidgetSettings.TimezoneMode.SOLAR_TIME.toString())));

        onView(withId(R.id.appwidget_timezone_custom)).check(assertHidden);
        onView(withId(R.id.appwidget_timezone_custom_label)).check(assertHidden);

        WidgetSettings.SolarTimeMode solarTimeMode = WidgetSettings.loadSolarTimeModePref(context, 0);
        onView(withId(R.id.appwidget_solartime)).check(matches(withSpinnerText( containsString(solarTimeMode.toString()) )));
        onView(withId(R.id.appwidget_solartime)).check(assertEnabled);
    }

    public static void verifyTimezoneDialog_system()
    {
        onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(WidgetSettings.TimezoneMode.CURRENT_TIMEZONE.toString())));
        onView(withId(R.id.appwidget_solartime)).check(assertHidden);
        onView(withId(R.id.appwidget_timezone_custom_label)).check(assertDisabled);

        String timezoneId = TimeZone.getDefault().getID();
        onView(withId(R.id.appwidget_timezone_custom)).check(matches(withSpinnerText( containsString(timezoneId) )));
        onView(withId(R.id.appwidget_timezone_custom)).check(assertDisabled);
    }

    public static void verifyTimezoneDialog_custom(Context context)
    {
        onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE.toString())));
        onView(withId(R.id.appwidget_solartime)).check(assertHidden);

        onView(withId(R.id.appwidget_timezone_custom_label)).check(assertEnabled);
        onView(withId(R.id.appwidget_timezone_custom_label)).check(assertClickable);

        String timezoneId = WidgetSettings.loadTimezonePref(context, 0);
        onView(withId(R.id.appwidget_timezone_custom)).check(matches(withSpinnerText( containsString(timezoneId) )));
        onView(withId(R.id.appwidget_timezone_custom)).check(assertEnabled);
    }

    public static WidgetSettings.TimezoneMode getTimezoneDialogMode()
    {
        if (spinnerDisplaysText(R.id.appwidget_timezone_mode, WidgetSettings.TimezoneMode.SOLAR_TIME.toString()))
            return WidgetSettings.TimezoneMode.SOLAR_TIME;

        else if (spinnerDisplaysText(R.id.appwidget_timezone_mode, WidgetSettings.TimezoneMode.CURRENT_TIMEZONE.toString()))
            return WidgetSettings.TimezoneMode.CURRENT_TIMEZONE;

        else if (spinnerDisplaysText(R.id.appwidget_timezone_mode, WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE.toString()))
            return WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE;

        else
            return null;   // unrecognized mode; fail with a null
    }

    public static void inputTimezoneDialog_mode(Context context, WidgetSettings.TimezoneMode mode)
    {
        onView(withId(R.id.appwidget_timezone_mode)).perform(click());
        onData(allOf(is(instanceOf(WidgetSettings.TimezoneMode.class)), is(mode))).inRoot(isPlatformPopup()).perform(click());
        verifyTimezoneDialog(context, mode);
    }

    public static void applyTimezoneDialog(Context context)
    {
        String setTimezoneText = context.getString(R.string.timezone_dialog_ok);
        onView(withText(setTimezoneText)).perform(click());
        // TODO: verify action
    }

    public static void cancelTimezoneDialog()
    {
        onView(withId(R.id.appwidget_timezone_mode)).perform(pressBack());
        onView(withId(R.id.appwidget_timezone_mode)).check(doesNotExist());
    }
}
