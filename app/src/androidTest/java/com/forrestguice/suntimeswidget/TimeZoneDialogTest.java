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

package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.content.Context;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import android.content.Intent;

import com.forrestguice.suntimeswidget.calculator.settings.SolarTimeMode;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper;
import com.forrestguice.util.InstrumentationUtils;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.TimeZone;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

@LargeTest
@BehaviorTest
@RunWith(SuntimesJUnitTestRunner.class)
public class TimeZoneDialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class, false, false);

    @Rule
    public RetryRule retry = new RetryRule(3);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
        saveConfigState(InstrumentationUtils.getContext());
        overrideConfigState(InstrumentationUtils.getContext());
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
        restoreConfigState(InstrumentationUtils.getContext());
    }

    @Test @QuickTest
    public void test_TimezoneDialog()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        new TimeZoneDialogTest.TimeZoneDialogRobot()
                .showDialog(context)
                .assertDialogShown(context);
    }

    @Test
    public void test_TimezoneDialog_input()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        TimeZoneDialogRobot robot = new TimeZoneDialogTest.TimeZoneDialogRobot();
        robot.showDialog(context).assertDialogShown(context)
                .captureScreenshot(context, "suntimes-dialog-timezone0");

        robot.inputTimezoneDialogMode(context, TimezoneMode.CURRENT_TIMEZONE)
                .verifyTimezoneDialog_system()
                .captureScreenshot(context, "suntimes-dialog-timezone-system0");

        robot.inputTimezoneDialogMode(context, TimezoneMode.CUSTOM_TIMEZONE)
                .verifyTimezoneDialog_custom(context)
                .captureScreenshot(context, "suntimes-dialog-timezone-custom0");

        robot.inputTimezoneDialogMode(context, TimezoneMode.SOLAR_TIME)
                .verifyTimezoneDialog_solar(context)
                .captureScreenshot(context, "suntimes-dialog-timezone-solar0");

        robot.doubleRotateDevice(context).assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);
    }

    /**
     * TimeZoneDialogRobot
     */
    public static class TimeZoneDialogRobot extends DialogTest.DialogRobot<TimeZoneDialogRobot>
    {
        public TimeZoneDialogRobot() {
            super();
            setRobot(this);
        }

        public TimeZoneDialogRobot showDialog(Activity activity)
        {
            openActionBarOverflowOrOptionsMenu(InstrumentationUtils.getContext());
            onView(withText(R.string.configAction_setTimeZone)).perform(click());
            return this;
        }
        @Override
        public TimeZoneDialogRobot applyDialog(Context context) {
            onView(withText(R.string.timezone_dialog_ok)).perform(click());
            return this;
        }
        @Override
        public TimeZoneDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.appwidget_timezone_mode)).perform(pressBack());
            return this;
        }

        public TimeZoneDialogRobot inputTimezoneDialogMode(Context context, TimezoneMode mode)
        {
            onView(withId(R.id.appwidget_timezone_mode)).perform(click());
            onData(allOf(is(instanceOf(TimezoneMode.class)), is(mode))).inRoot(isPlatformPopup()).perform(click());
            verifyTimezoneDialog(context, mode);
            return this;
        }

        @Override
        public TimeZoneDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.appwidget_timezone_mode)).check(ViewAssertionHelper.assertShown);
            return this;
        }
        @Override
        public TimeZoneDialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.appwidget_timezone_mode)).check(doesNotExist());
            super.assertDialogNotShown(context);
            return this;
        }

        public TimeZoneDialogRobot verifyTimezoneDialog(Context context, TimezoneMode mode )
        {
            if (mode == null)
                return this;

            switch (mode)
            {
                case SOLAR_TIME:
                    return verifyTimezoneDialog_solar(context);

                case CURRENT_TIMEZONE:
                    return verifyTimezoneDialog_system();

                case CUSTOM_TIMEZONE:
                default:
                    return verifyTimezoneDialog_custom(context);
            }
        }

        public TimeZoneDialogRobot verifyTimezoneDialog_solar(Context context)
        {
            onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(TimezoneMode.SOLAR_TIME.toString())));

            onView(withId(R.id.appwidget_timezone_custom)).check(ViewAssertionHelper.assertHidden);
            onView(withId(R.id.sort_timezones)).check(ViewAssertionHelper.assertHidden);

            SolarTimeMode solarTimeMode = WidgetSettings.loadSolarTimeModePref(context, 0);
            onView(withId(R.id.appwidget_solartime)).check(matches(withSpinnerText( containsString(solarTimeMode.toString()) )));
            onView(withId(R.id.appwidget_solartime)).check(ViewAssertionHelper.assertEnabled);
            return this;
        }

        public TimeZoneDialogRobot verifyTimezoneDialog_system()
        {
            onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(TimezoneMode.CURRENT_TIMEZONE.toString())));
            onView(withId(R.id.appwidget_solartime)).check(ViewAssertionHelper.assertHidden);
            onView(withId(R.id.sort_timezones)).check(ViewAssertionHelper.assertHidden);

            String timezoneId = TimeZone.getDefault().getID();
            onView(withId(R.id.appwidget_timezone_custom)).check(matches(withSpinnerText( containsString(timezoneId) )));
            onView(withId(R.id.appwidget_timezone_custom)).check(ViewAssertionHelper.assertDisabled);
            return this;
        }

        public TimeZoneDialogRobot verifyTimezoneDialog_custom(Context context)
        {
            onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(TimezoneMode.CUSTOM_TIMEZONE.toString())));
            onView(withId(R.id.appwidget_solartime)).check(ViewAssertionHelper.assertHidden);

            onView(withId(R.id.sort_timezones)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.sort_timezones)).check(ViewAssertionHelper.assertClickable);

            TimezoneMode timezoneMode = WidgetSettings.loadTimezoneModePref(context, 0);
            String timezoneId = WidgetSettings.loadTimezonePref(context, 0, (timezoneMode == TimezoneMode.CUSTOM_TIMEZONE ? TimeZoneDialog.SLOT_CUSTOM0 : ""));
            onView(withId(R.id.appwidget_timezone_custom)).check(matches(withSpinnerText( containsString(timezoneId) )));
            onView(withId(R.id.appwidget_timezone_custom)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.appwidget_timezone_custom)).check(ViewAssertionHelper.assertClickable);
            return this;
        }

        public static TimezoneMode getTimezoneDialogMode()
        {
            if (spinnerDisplaysText(R.id.appwidget_timezone_mode, TimezoneMode.SOLAR_TIME.toString()))
                return TimezoneMode.SOLAR_TIME;

            else if (spinnerDisplaysText(R.id.appwidget_timezone_mode, TimezoneMode.CURRENT_TIMEZONE.toString()))
                return TimezoneMode.CURRENT_TIMEZONE;

            else if (spinnerDisplaysText(R.id.appwidget_timezone_mode, TimezoneMode.CUSTOM_TIMEZONE.toString()))
                return TimezoneMode.CUSTOM_TIMEZONE;

            else
                return null;   // unrecognized mode; fail with a null
        }

    }

}
