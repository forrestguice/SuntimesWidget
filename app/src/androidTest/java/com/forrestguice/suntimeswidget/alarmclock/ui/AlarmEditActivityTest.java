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

package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.app.Activity;
import android.content.Context;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
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
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.support.test.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isRoot;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.navigationButton;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withClassName;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class AlarmEditActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<AlarmClockActivity> activityRule = new ActivityTestRule<>(AlarmClockActivity.class);

    @Before
    public void beforeTest() throws IOException {
        setAnimationsEnabled(false);
        saveConfigState(activityRule.getActivity());
        overrideConfigState(activityRule.getActivity());
    }
    @After
    public void afterTest() throws IOException {
        setAnimationsEnabled(true);
        restoreConfigState(activityRule.getActivity());
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    @Test
    public void test_AlarmEditActivity()
    {
        Activity activity = activityRule.getActivity();
        AlarmEditActivityRobot robot = new AlarmEditActivityRobot()
                .showActivity(activity)
                .assertActivityShown(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .cancelOverflowMenu(activity);

        robot.showOverflowMenu(activity).sleep(1000)
                .clickOverflowMenu_options(activity)
                .assertOptionsMenuShown(activity)
                .cancelOptionsMenu(activity);


        robot.showOverflowMenu(activity).sleep(1000)
                .assertOverflowMenuShown(activity)
                .clickOverflowMenu_help();
        new DialogTest.HelpDialogRobot()
                .assertDialogShown(activity)
                .cancelDialog(activity);

        // TODO
    }

    @Test
    public void test_AlarmEditActivity_discardChanges()
    {
        Activity context = activityRule.getActivity();
        AlarmActivityTest.AlarmActivityRobot robot = new AlarmActivityTest.AlarmActivityRobot();
        AlarmCreateDialogTest.AlarmDialogRobot robot1 = AlarmActivityTest.dialogRobot();
        AlarmEditActivityTest.AlarmEditActivityRobot robot2 = new AlarmEditActivityTest.AlarmEditActivityRobot();

        robot.assertActivityShown()
                .assertAddAlarmButtonShown(true)
                .clickAddAlarmButton().sleep(1000);      // click add button

        robot1.assertDialogShown(context)
                .applyDialog(context).sleep(1000);       // click apply (add alarm)

        robot2.assertActivityShown(context)
                .clickBackButton()                           // click back (discardChanges)
                .assertDiscardChangesShown()
                .clickDiscardChanges_cancel();               // cancel "discard changes"

        robot2.assertActivityShown(context)
                .clickBackButton()                           // click back (discardChanges)
                .assertDiscardChangesShown()
                .clickDiscardChanges_discard();              // confirm "discard changes"

        robot.assertActivityShown();
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * AlarmEditActivityRobot
     */
    public static class AlarmEditActivityRobot extends ActivityRobot<AlarmEditActivityRobot>
    {
        public AlarmEditActivityRobot() {
            setRobot(this);
        }

        protected AlarmEditActivityRobot showActivity(Activity activity)
        {
            //new AlarmActivityTest.AlarmActivityRobot()
            //        .clickAlarmItem(0)
            //        .clickAlarmItem(0);

            AlarmActivityTest.AlarmActivityRobot robot = new AlarmActivityTest.AlarmActivityRobot();
            AlarmCreateDialogTest.AlarmDialogRobot robot1 = AlarmActivityTest.dialogRobot();
            robot.clickAddAlarmButton();
            robot1.applyDialog(activity);
            return this;
        }

        public AlarmEditActivityRobot clickOverflowMenu_options(Context context) {
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmEditActivityRobot cancelOptionsMenu(Context context) {
            onView(withText(R.string.configAction_setAlarmType)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public AlarmEditActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        public AlarmEditActivityRobot clickBackButton() {
            onView(isRoot()).perform(pressBack());    // TODO: via toolbar button
            return this;
        }

        public AlarmEditActivityRobot clickSaveButton() {
            onView(withText(R.string.configAction_saveAlarm)).check(assertShown);
            return this;
        }
        public AlarmEditActivityRobot clickSaveAndEnableButton() {
            onView(withText(R.string.configAction_enableAlarm)).check(assertShown);
            return this;
        }
        public AlarmEditActivityRobot clickSaveAndDisableButton() {
            onView(withText(R.string.configAction_disableAlarm)).check(assertShown);
            return this;
        }

        public AlarmEditActivityRobot clickDiscardChanges_save() {
            onView(withText(R.string.discardchanges_dialog_neutral)).perform(click());
            return this;
        }
        public AlarmEditActivityRobot clickDiscardChanges_discard() {
            onView(withText(R.string.discardchanges_dialog_ok)).perform(click());
            return this;
        }
        public AlarmEditActivityRobot clickDiscardChanges_cancel() {
            onView(withText(R.string.discardchanges_dialog_cancel)).perform(click());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public AlarmEditActivityRobot assertActivityShown(Context context)
        {
            onView(allOf( anyOf(withText(R.string.alarmMode_alarm), withText(R.string.alarmMode_notification), withText(R.string.alarmMode_notification1)),
                    withParent(withClassName(endsWith("Toolbar")))
            )).check(assertShown);
            onView(navigationButton()).check(assertShown);
            return this;
        }
        public AlarmEditActivityRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_deleteAlarm)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_options)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_help)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public AlarmEditActivityRobot assertOptionsMenuShown(Context context) {
            onView(withText(R.string.configAction_setAlarmType)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setAlarmLabel)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setAlarmNote)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setAlarmOffset)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setAlarmEvent)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setAlarmLocation)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setAlarmRepeat)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setAlarmSound)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public AlarmEditActivityRobot assertDiscardChangesShown()
        {
            onView(withText(R.string.discardchanges_dialog_ok)).check(assertShown);
            onView(withText(R.string.discardchanges_dialog_cancel)).check(assertShown);
            onView(withText(R.string.discardchanges_dialog_message)).check(assertShown);
            onView(withText(R.string.discardchanges_dialog_neutral)).check(assertShown);
            return this;
        }

        public AlarmEditActivityRobot assertShown_saveAndEnableButton(boolean isShown) {
            onView(withText(R.string.configAction_enableAlarm)).check(isShown ? assertShown : assertHidden);
            return this;
        }
        public AlarmEditActivityRobot assertShown_saveAndDisableButton(boolean isShown) {
            onView(withText(R.string.configAction_disableAlarm)).check(isShown ? assertShown : assertHidden);
            return this;
        }

    }
}
