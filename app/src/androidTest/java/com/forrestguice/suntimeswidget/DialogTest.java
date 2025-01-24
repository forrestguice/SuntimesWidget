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
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.view.View;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.map.WorldMapDialogTest;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.support.test.InstrumentationRegistry;
import com.forrestguice.support.test.espresso.ViewAssertionHelper;
import com.forrestguice.support.test.filters.LargeTest;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.AppSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeDown;
import static com.forrestguice.support.test.espresso.action.ViewActions.swipeUp;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.matches;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.hasLinks;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.navigationButton;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
    }

    @Test
    public void test_showHelpDialog()
    {
        Activity context = activityRule.getActivity();
        new HelpDialogRobot()
                .showDialog(context).assertDialogShown(context)
                //.captureScreenshot(context, "suntimes-dialog-help0")
                .rotateDevice(context).assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);
    }

    @Test
    public void test_showAboutDialog()
    {
        Activity context = activityRule.getActivity();
        new AboutDialogRobot()
                .showDialog(context).assertDialogShown(context)
                //.captureScreenshot(context, "suntimes-dialog-about0")
                .rotateDevice(context).assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);
    }

    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////

    /**
     * DialogRobot
     */
    public interface DialogRobot
    {
        DialogRobot showDialog(Activity context);
        DialogRobot applyDialog(Context context);
        DialogRobot cancelDialog(Context context);

        DialogRobot assertDialogShown(Context context);
        DialogRobot assertDialogNotShown(Context context);

        DialogRobot captureScreenshot(Activity activity, String name);
        DialogRobot captureScreenshot(Activity activity, String subdir, String name);

        DialogRobot rotateDevice(Activity activity);
        DialogRobot rotateDevice(Activity activity, int orientation);

        DialogRobot sleep(long ms);
    }

    public static class DialogRobotConfig
    {
        public Location getLocation(Context context) {
            return WidgetSettings.loadLocationPref(context, 0);
        }
    }

    public static abstract class DialogRobotBase implements DialogRobot
    {
        public DialogRobotBase() {
            initRobotConfig();
        }

        protected DialogRobotConfig expected;
        public void initRobotConfig() {
            setRobotConfig(new DialogRobotConfig());
        }
        public void setRobotConfig(DialogRobotConfig config) {
            expected = config;
        }

        @Override
        public abstract DialogRobot showDialog(Activity activity);

        @Override
        public DialogRobot applyDialog(Context context) {
            onView(withId(R.id.dialog_button_accept)).perform(click());
            return this;
        }
        @Override
        public DialogRobot cancelDialog(Context context) {
            onView(withId(R.id.dialog_header)).perform(pressBack());
            return this;
        }

        @Override
        public DialogRobot assertDialogShown(Context context) {
            onView(withId(R.id.dialog_header)).check(ViewAssertionHelper.assertShown);
            return this;
        }
        @Override
        public DialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.dialog_header)).check(doesNotExist());
            return this;
        }

        public DialogRobot expandSheet() {
            onView(withId(R.id.dialog_header)).perform(swipeUp());
            return this;
        }
        public DialogRobot collapseSheet() {
            onView(withId(R.id.dialog_header)).perform(swipeDown());
            return this;
        }
        public DialogRobot assertSheetIsCollapsed(Context context) {
            onView(withId(R.id.dialog_header)).check(ViewAssertionHelper.assertShown);
            return this;
        }

        @Override
        public DialogRobot captureScreenshot(Activity activity, String name) {
            captureScreenshot(activity, "", name);
            return this;
        }
        @Override
        public DialogRobot captureScreenshot(Activity activity, String subdir, String name) {
            SuntimesActivityTestBase.captureScreenshot(activity, subdir, name);
            return this;
        }

        @Override
        public DialogRobot rotateDevice(Activity activity)
        {
            rotateDevice(activity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            rotateDevice(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return this;
        }
        @Override
        public DialogRobot rotateDevice(Activity activity, int orientation)
        {
            activity.setRequestedOrientation(orientation);
            return this;
        }
        @Override
        public DialogRobot sleep(long ms)
        {
            SystemClock.sleep(ms);
            return this;
        }
    }

    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////

    /**
     * HelpDialogRobot
     */
    public static class HelpDialogRobot extends DialogRobotBase implements DialogRobot
    {
        @Override
        public HelpDialogRobot showDialog(Activity context) {
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
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
            onView(withId(R.id.txt_help_content)).check(ViewAssertionHelper.assertShown);
            return this;
        }
        @Override
        public HelpDialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.txt_help_content)).check(doesNotExist());
            return this;
        }
    }

    /**
     * AboutDialogRobot
     */
    public static class AboutDialogRobot extends DialogRobotBase implements DialogRobot
    {
        @Override
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
            onView(withId(R.id.txt_about_name)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.txt_about_desc)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.txt_about_version)).check(ViewAssertionHelper.assertShown);

            onView(withId(R.id.txt_about_url)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.txt_about_url)).check(matches(hasLinks()));

            onView(withId(R.id.txt_about_support)).check(ViewAssertionHelper.assertShown);
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
