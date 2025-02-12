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
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import com.forrestguice.suntimeswidget.settings.AppSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasLinks;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class DialogTest extends SuntimesActivityTestBase
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

    @Test
    public void test_showHelpDialog()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        new HelpDialogRobot()
                .showDialog(context)
                .assertDialogShown(context)

                .doubleRotateDevice(context)
                .assertDialogShown(context)

                .cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_showAboutDialog()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        new AboutDialogRobot()
                .showDialog(context).assertDialogShown(context)
                //.captureScreenshot(context, "suntimes-dialog-about0")
                .doubleRotateDevice(context).assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);
    }

    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////

    /**
     * DialogRobot
     * @param <T> robot return type (implementations must call setRobot)
     */
    public static abstract class DialogRobot<T> extends Robot<T>
    {
        public DialogRobot() {
            initRobotConfig();
        }

        protected DialogRobotConfig expected;
        public void initRobotConfig() {
            setRobotConfig(new DialogRobotConfig());
        }
        public void setRobotConfig(DialogRobotConfig config) {
            expected = config;
        }

        public T applyDialog(Context context) {
            onView(withId(R.id.dialog_button_accept)).perform(click());
            return robot;
        }
        public T cancelDialog(Context context) {
            onView(withId(R.id.dialog_header)).perform(pressBack());
            return robot;
        }

        public T assertDialogShown(Context context) {
            onView(withId(R.id.dialog_header)).check(assertShown);
            return robot;
        }
        public T assertDialogNotShown(Context context) {
            onView(withId(R.id.dialog_header)).check(doesNotExist());
            return robot;
        }

        public T expandSheet() {
            onView(withId(R.id.dialog_header)).perform(swipeUp());
            return robot;
        }
        public T collapseSheet() {
            onView(withId(R.id.dialog_header)).perform(swipeDown());
            return robot;
        }
        public T assertSheetIsCollapsed(Context context) {
            onView(withId(R.id.dialog_header)).check(assertShown);
            return robot;
        }

        public static int thisYear() {
            return Calendar.getInstance().get(Calendar.YEAR);
        }
        public static int nextYear() {
            return thisYear() + 1;
        }
        public static int lastYear() {
            return thisYear() - 1;
        }

        public Calendar now(Context context) {
            return now(TimeZone.getTimeZone(WidgetSettings.loadTimezonePref(context, 0)));
        }
        public Calendar now(TimeZone timezone) {
            return Calendar.getInstance(timezone);
        }
    }

    public static class DialogRobotConfig
    {
        public Location getLocation(Context context) {
            return WidgetSettings.loadLocationPref(context, 0);
        }
    }

    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////

    /**
     * HelpDialogRobot
     */
    public static class HelpDialogRobot extends DialogRobot<HelpDialogRobot>
    {
        public HelpDialogRobot() {
            super();
            setRobot(this);
        }

        public HelpDialogRobot showDialog(Activity context) {
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
            sleep(500);
            onView(withText(R.string.configAction_help)).perform(click());
            return this;
        }
        @Override
        public HelpDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.txt_help_content)).perform(pressBack());
            return this;
        }
        @Override
        public HelpDialogRobot assertDialogShown(Context context) {
            onView(withId(R.id.txt_help_content)).check(assertShown);
            return this;
        }
        @Override
        public HelpDialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.txt_help_content)).check(doesNotExist());
            return this;
        }

        public HelpDialogRobot assertOnlineHelpButtonShown(Context context) {
            onView(withText(R.string.configAction_onlineHelp)).check(assertShown).check(assertClickable);
            return this;
        }
    }

    /**
     * AboutDialogRobot
     */
    public static class AboutDialogRobot extends DialogRobot<AboutDialogRobot>
    {
        public AboutDialogRobot() {
            super();
            setRobot(this);
        }

        public AboutDialogRobot showDialog(Activity context)
        {
            String navMode = AppSettings.loadNavModePref(context);
            if (AppSettings.NAVIGATION_SIDEBAR.equals(navMode))
            {
                onView(navigationButton()).perform(click());
                onView(withText(R.string.configAction_aboutWidget)).perform(click());

            } else {
                openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
                onView(withText(R.string.configAction_aboutWidget)).perform(click());
            }
            return this;
        }
        @Override
        public AboutDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.txt_about_name)).perform(pressBack());
            return this;
        }
        @Override
        public AboutDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.txt_about_name)).check(assertShown);
            onView(withId(R.id.txt_about_desc)).check(assertShown);
            onView(withId(R.id.txt_about_version)).check(assertShown);

            onView(withId(R.id.txt_about_url)).check(assertShown);
            onView(withId(R.id.txt_about_url)).check(matches(hasLinks()));

            onView(withId(R.id.txt_about_support)).check(assertShown);
            onView(withId(R.id.txt_about_support)).check(matches(hasLinks()));
            return this;
        }
        @Override
        public AboutDialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.txt_about_name)).check(doesNotExist());
            return this;
        }
    }
}
