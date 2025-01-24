/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.moon;

import android.app.Activity;
import android.content.Context;

import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.support.test.espresso.ViewAssertionHelper;
import com.forrestguice.support.test.filters.LargeTest;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

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
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MoonDialogTest extends SuntimesActivityTestBase
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
    public void test_showMoonDialog()
    {
        Activity context = activityRule.getActivity();
        MoonDialogRobot robot = new MoonDialogRobot()
                .showDialog(context)
                .assertDialogShown(context);
                //.captureScreenshot(context, "suntimes-dialog-moon0");

        robot.showOverflowMenu(context)
                .clickOverflowMenu_Help(context)
                .assertOverflowMenu_Help(context).sleep(500)
                .cancelOverflowMenu_Help(context).sleep(1000);

        robot.showOverflowMenu(context)
                .assertOverflowMenuShown(context)
                .cancelOverflowMenu(context).sleep(1000);

        robot.showOverflowMenu(context)
                .clickOverflowMenu_Controls(context)
                .assertOverflowMenu_Controls(context).sleep(500)
                .cancelOverflowMenu_Controls(context).sleep(1000);

        robot.showOverflowMenu(context)
                .clickOverflowMenu_Options(context)
                .assertOverflowMenu_Options(context).sleep(500)
                .cancelOverflowMenu_Options(context).sleep(1000);

        robot.clickPositionArea(context)
                .assertOverflowMenu_Controls(context).sleep(500)
                .cancelOverflowMenu_Controls(context).sleep(1000);

        robot.clickPositionArea(context)
                .clickOverflowMenu_Controls_Next(context)
                .cancelOverflowMenu_Controls(context)
                .assertCanBeReset(context).sleep(500)
                .clickResetButton(context)
                .assertIsReset(context).sleep(1000);

        robot.clickPositionArea(context)
                .clickOverflowMenu_Controls_Previous(context)
                .cancelOverflowMenu_Controls(context)
                .assertCanBeReset(context).sleep(500);
        robot.rotateDevice(context).assertDialogShown(context)
                .assertCanBeReset(context).sleep(1000)
                .clickResetButton(context)
                .assertIsReset(context);

        robot.cancelDialog(context)
                .assertDialogNotShown(context);
    }

    /**
     * MoonDialogRobot
     */
    public static class MoonDialogRobot extends DialogTest.DialogRobotBase implements DialogTest.DialogRobot
    {
        @Override
        public MoonDialogRobot sleep(long ms) {
            super.sleep(ms);
            return this;
        }

        @Override
        public MoonDialogRobot rotateDevice(Activity activity) {
            super.rotateDevice(activity);
            return this;
        }

        @Override
        public MoonDialogRobot showDialog(Activity context) {
            openActionBarOverflowOrOptionsMenu(context);
            onView(withText(R.string.configAction_moon)).perform(click());
            return this;
        }

        public MoonDialogRobot clickPositionArea(Context context) {
            onView(withId(R.id.moonphase_view)).perform(click());
            return this;
        }
        public MoonDialogRobot clickResetButton(Context context) {
            onView(withId(R.id.media_reset)).perform(click());
            return this;
        }

        public MoonDialogRobot showOverflowMenu(Context context) {
            onView(withId(R.id.menu_button)).perform(click());
            return this;
        }
        public MoonDialogRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.moon_dialog_controls)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public MoonDialogRobot clickOverflowMenu_Controls(Context context) {
            onView(withText(R.string.moon_dialog_controls)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public MoonDialogRobot clickOverflowMenu_Controls_Next(Context context) {
            onView(withId(R.id.media_next)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public MoonDialogRobot clickOverflowMenu_Controls_Previous(Context context) {
            onView(withId(R.id.media_prev)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public MoonDialogRobot cancelOverflowMenu_Controls(Context context) {
            onView(withId(R.id.media_prev)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public MoonDialogRobot clickOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public MoonDialogRobot clickOverflowMenu_Options_Colors(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public MoonDialogRobot cancelOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public MoonDialogRobot clickOverflowMenu_Help(Context context) {
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public MoonDialogRobot cancelOverflowMenu_Help(Context context) {
            onView(withId(R.id.txt_help_content)).perform(pressBack());
            return this;
        }

        @Override
        public MoonDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.dialog_header)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.info_time_moon)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.menu_button)).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.menu_button)).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.menu_button)).check(ViewAssertionHelper.assertClickable);
            return this;
        }
        @Override
        public MoonDialogRobot assertDialogNotShown(Context context)
        {
            super.assertDialogNotShown(context);
            return this;
        }
        public MoonDialogRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.moon_dialog_controls)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            return this;
        }
        public MoonDialogRobot assertOverflowMenu_Controls(Context context) {
            onView(withId(R.id.media_prev)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.media_prev)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.media_prev)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertClickable);
            onView(withId(R.id.media_next)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            onView(withId(R.id.media_next)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertEnabled);
            onView(withId(R.id.media_next)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertClickable);
            return this;
        }
        public MoonDialogRobot assertOverflowMenu_Options(Context context) {
            onView(withText(R.string.configAction_colors)).inRoot(isPlatformPopup()).check(ViewAssertionHelper.assertShown);
            // TODO: other options
            return this;
        }
        public MoonDialogRobot assertOverflowMenu_Help(Context context) {
            onView(withId(R.id.txt_help_content)).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_onlineHelp)).check(ViewAssertionHelper.assertShown);
            onView(withText(R.string.configAction_onlineHelp)).check(ViewAssertionHelper.assertClickable);
            onView(withText(R.string.configAction_onlineHelp)).check(ViewAssertionHelper.assertEnabled);
            return this;
        }

        public MoonDialogRobot assertIsReset(Context context) {
            onView(withId(R.id.media_reset)).check(ViewAssertionHelper.assertHidden);
            return this;
        }
        public MoonDialogRobot assertCanBeReset(Context context) {
            onView(withId(R.id.media_reset)).check(ViewAssertionHelper.assertShown);
            return this;
        }
    }
}
