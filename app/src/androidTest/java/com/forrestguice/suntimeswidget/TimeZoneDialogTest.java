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
import android.content.Intent;

import com.forrestguice.support.test.InstrumentationRegistry;
import com.forrestguice.support.test.espresso.ViewAssertionHelper;
import com.forrestguice.support.test.filters.LargeTest;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.TimeZone;

import static com.forrestguice.support.test.espresso.Espresso.onData;
import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.matches;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class TimeZoneDialogTest extends SuntimesActivityTestBase
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

        robot.inputTimezoneDialogMode(context, WidgetSettings.TimezoneMode.CURRENT_TIMEZONE)
                .verifyTimezoneDialog_system()
                .captureScreenshot(context, "suntimes-dialog-timezone-system0");

        robot.inputTimezoneDialogMode(context, WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE)
                .verifyTimezoneDialog_custom(context)
                .captureScreenshot(context, "suntimes-dialog-timezone-custom0");

        robot.inputTimezoneDialogMode(context, WidgetSettings.TimezoneMode.SOLAR_TIME)
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
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
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

        public TimeZoneDialogRobot inputTimezoneDialogMode(Context context, WidgetSettings.TimezoneMode mode)
        {
            onView(withId(R.id.appwidget_timezone_mode)).perform(click());
            onData(allOf(is(instanceOf(WidgetSettings.TimezoneMode.class)), is(mode))).inRoot(isPlatformPopup()).perform(click());
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

        public TimeZoneDialogRobot verifyTimezoneDialog(Context context, WidgetSettings.TimezoneMode mode )
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
            onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(WidgetSettings.TimezoneMode.SOLAR_TIME.toString())));

            onView(withId(R.id.appwidget_timezone_custom)).check(ViewAssertionHelper.assertHidden);
            onView(withId(R.id.sort_timezones)).check(ViewAssertionHelper.assertHidden);

            WidgetSettings.SolarTimeMode solarTimeMode = WidgetSettings.loadSolarTimeModePref(context, 0);
            onView(withId(R.id.appwidget_solartime)).check(matches(withSpinnerText( containsString(solarTimeMode.toString()) )));
            onView(withId(R.id.appwidget_solartime)).check(ViewAssertionHelper.assertEnabled);
            return this;
        }

        public TimeZoneDialogRobot verifyTimezoneDialog_system()
        {
            onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(WidgetSettings.TimezoneMode.CURRENT_TIMEZONE.toString())));
            onView(withId(R.id.appwidget_solartime)).check(ViewAssertionHelper.assertHidden);
            onView(withId(R.id.sort_timezones)).check(ViewAssertionHelper.assertHidden);

            String timezoneId = TimeZone.getDefault().getID();
            onView(withId(R.id.appwidget_timezone_custom)).check(matches(withSpinnerText( containsString(timezoneId) )));
            onView(withId(R.id.appwidget_timezone_custom)).check(ViewAssertionHelper.assertDisabled);
            return this;
        }

        public TimeZoneDialogRobot verifyTimezoneDialog_custom(Context context)
        {
            onView(withId(R.id.appwidget_timezone_mode)).check(matches(withSpinnerText(WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE.toString())));
            onView(withId(R.id.appwidget_solartime)).check(ViewAssertionHelper.assertHidden);

            onView(withId(R.id.sort_timezones)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.sort_timezones)).check(ViewAssertionHelper.assertClickable);

            WidgetSettings.TimezoneMode timezoneMode = WidgetSettings.loadTimezoneModePref(context, 0);
            String timezoneId = WidgetSettings.loadTimezonePref(context, 0, (timezoneMode == WidgetSettings.TimezoneMode.CUSTOM_TIMEZONE ? TimeZoneDialog.SLOT_CUSTOM0 : ""));
            onView(withId(R.id.appwidget_timezone_custom)).check(matches(withSpinnerText( containsString(timezoneId) )));
            onView(withId(R.id.appwidget_timezone_custom)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.appwidget_timezone_custom)).check(ViewAssertionHelper.assertClickable);
            return this;
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

    }

}
