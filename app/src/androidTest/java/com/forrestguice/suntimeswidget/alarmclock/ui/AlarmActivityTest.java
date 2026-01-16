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
import android.content.Intent;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.QuickTest;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivityTest;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivityTest;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.util.InstrumentationUtils;
import com.forrestguice.util.SuntimesJUnitTestRunner;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertClickable;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

@LargeTest
@BehaviorTest
@RunWith(SuntimesJUnitTestRunner.class)
public class AlarmActivityTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<AlarmClockActivity> activityRule = new ActivityTestRule<>(AlarmClockActivity.class, false, false);

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

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    @Test @QuickTest
    public void test_AlarmActivity()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        new AlarmActivityRobot()
                .assertActivityShown();
    }

    @Test
    public void test_AlarmActivity_menu()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity activity = activityRule.getActivity();
        AlarmActivityRobot robot = new AlarmActivityRobot()
                .assertActivityShown();

        robot.showOverflowMenu(activity).sleep(1000)    // :
                .assertOverflowMenuShown()
                .cancelOverflowMenu(activity);

        robot.showOverflowMenu(activity).sleep(1000)    // : -> Sort
                .clickOverflowMenu_sort(activity)
                .assertSortMenuShown()
                .cancelSortMenu(activity);
    }

    @Test
    public void test_AlarmActivity_navigation_sidebar()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        config(context).edit().putString(AppSettings.PREF_KEY_NAVIGATION_MODE, AppSettings.NAVIGATION_SIDEBAR).apply();
        AlarmActivityRobot robot = new AlarmActivityRobot()
                .recreateActivity(context)
                .assertActionBar_navButtonShown(true);

        robot.showOverflowMenu(context).sleep(1000)
                .assertOverflowMenuShown()
                .assertOverflowMenu_hasSimpleNavigation(false)
                .cancelOverflowMenu(context);

        robot.showSidebarMenu(context).sleep(1000)
                .assertSideBarMenuShown(context)
                .cancelSidebarMenu(context);

        robot.showSidebarMenu(context).sleep(1000)    // About
                .clickSidebarMenu_about(context);
        new DialogTest.AboutDialogRobot()
                .assertDialogShown(context)
                .cancelDialog(context);
        robot.assertActivityShown();

        robot.showSidebarMenu(context).sleep(1000)    // Settings
                .clickSidebarMenu_settings(context);
        new SuntimesSettingsActivityTest.SettingsActivityRobot()
                .assertActivityShown(context)
                .pressBack();
        robot.assertActivityShown();

        robot.showSidebarMenu(context).sleep(1000)    // Clock
                .clickSidebarMenu_clock(context);
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(context);
        onView(isRoot()).perform(pressBack());
        robot.assertActivityShown();
    }

    @Test
    public void test_AlarmActivity_navigation_simple()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        config(context).edit().putString(AppSettings.PREF_KEY_NAVIGATION_MODE, AppSettings.NAVIGATION_SIMPLE).apply();
        AlarmActivityRobot robot = new AlarmActivityRobot()
                .recreateActivity(context)
                .assertActivityShown()
                .assertActionBar_homeButtonShown(true);

        robot.showOverflowMenu(context)        // :
                .sleep(1000)
                .assertOverflowMenuShown()
                .assertOverflowMenu_hasSimpleNavigation(true)
                .cancelOverflowMenu(context);

        robot.showOverflowMenu(context)        // : -> About
                .clickOverflowMenu_about()
                .sleep(1000);
        new DialogTest.AboutDialogRobot()
                .assertDialogShown(context)
                .cancelDialog(context);
        robot.assertActivityShown();

        robot.showOverflowMenu(context)        // : -> Settings
                .clickOverflowMenu_settings()
                .sleep(1000);
        new SuntimesSettingsActivityTest.SettingsActivityRobot()
                .assertActivityShown(context)
                .pressBack();
        robot.assertActivityShown();

        robot.clickHomeButton(context)         // Home Button
                .sleep(1000);
        new SuntimesActivityTest.MainActivityRobot()
                .assertActivityShown(context);
        onView(isRoot()).perform(pressBack());
        robot.assertActivityShown();
    }

    @Test
    public void test_AlarmActivity_addAlarm()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        AlarmActivityRobot robot = new AlarmActivityRobot();
        AlarmCreateDialogTest.AlarmDialogRobot robot1 = dialogRobot();
        robot.assertActivityShown()
                .assertAddAlarmButtonShown(true)
                .clickAddAlarmButton().sleep(1000);

        //robot.assertAddAlarmButtonShown(false);   // TODO: detect fab in hidden state; it shrinks as the bottom drawer is pulled up
        robot1.assertDialogShown(context)
                .cancelDialog(context).sleep(1000);
        robot.assertAddAlarmButtonShown(true);
    }

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////

    protected static AlarmCreateDialogTest.AlarmDialogRobot dialogRobot()
    {
        AlarmCreateDialogTest.AlarmDialogRobot robot = new AlarmCreateDialogTest.AlarmDialogRobot();
        robot.setRobotConfig(new AlarmCreateDialogTest.AlarmDialogRobotConfig()
        {
            @Override
            public boolean showAlarmListButton() {
                return false;
            }
        });
        return robot;
    }

    /**
     * AlarmActivityRobot
     */
    public static class AlarmActivityRobot extends ActivityRobot<AlarmActivityRobot>
    {
        public AlarmActivityRobot() {
            setRobot(this);
        }

        protected AlarmActivityRobot showActivity(Activity activity) {
            showSidebarMenu(activity);
            clickSidebarMenu_alarms(activity);
            return this;
        }

        public AlarmActivityRobot clickAddAlarmButton() {
            onView(allOf(withContentDescription(R.string.configAction_addAlarm), withClassName(endsWith("ActionButton")))).perform(click());
            return this;
        }
        public AlarmActivityRobot clickClearSelectionButton(int position) {
            onView(allOf(withContentDescription(R.string.configAction_deselect), withClassName(endsWith("ActionButton")))).perform(click());
            return this;
        }

        public AlarmActivityRobot clickAlarmItem(int position) {
            onView(withId(R.id.layout_alarmcard)).perform(click());
            return this;
        }
        public AlarmActivityRobot clickAlarmItem_delete(int position) {
            onView(allOf( isDescendantOfA(withId(R.id.layout_alarmcard)),
                    withContentDescription(R.string.configAction_deleteAlarm)
            )).perform(click());
            return this;
        }
        public AlarmActivityRobot clickAlarmItem_dismiss(int position) {
            onView(allOf( isDescendantOfA(withId(R.id.layout_alarmcard)),
                    withText(R.string.alarmAction_dismiss)
            )).perform(click());
            return this;
        }
        public AlarmActivityRobot clickAlarmItem_snooze(int position) {
            onView(allOf( isDescendantOfA(withId(R.id.layout_alarmcard)),
                    withText(R.string.alarmAction_snooze)
            )).perform(click());
            return this;
        }

        public AlarmActivityRobot assertAlarmItem_noSelection() {
            // TODO: no items selected? clear-selection fab hidden?
            return this;
        }
        public AlarmActivityRobot assertAlarmItem_isSelected(int position, boolean isSelected) {
            // TODO: selection background shown? clear-selection fab shown?
            return this;
        }
        public AlarmActivityRobot assertAlarmItem_isEnabled(int position, boolean isEnabled) {
            // TODO: checkbox/switch isChecked? item shows enabled background?
            return this;
        }
        public AlarmActivityRobot assertAlarmItem_isType(int position, AlarmClockItem.AlarmType type) {
            // TODO: correct icon shown? correct label shown?
            return this;
        }

        public AlarmActivityRobot clickOverflowMenu_clear(Context context) {
            onView(withText(R.string.configAction_clearAlarms)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmActivityRobot clickOverflowMenu_import(Context context) {
            onView(withText(R.string.configAction_exportAlarms)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmActivityRobot clickOverflowMenu_export(Context context) {
            onView(withText(R.string.configAction_importAlarms)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmActivityRobot clickOverflowMenu_sort(Context context) {
            onView(withText(R.string.configAction_sortAlarms)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmActivityRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_clearAlarms)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public AlarmActivityRobot cancelSortMenu(Context context) {
            onView(withText(R.string.configAction_sortAlarms_by_time)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }

        /////////////////////////////////////////////////////////////////////////

        public AlarmActivityRobot assertActivityShown()
        {
            onView(allOf(withText(R.string.configLabel_alarmClock), withParent(withClassName(endsWith("Toolbar"))))).check(assertShown);
            onView(withContentDescription(R.string.configLabel_bedtime)).check(assertShown);
            onView(withContentDescription(R.string.configLabel_bedtime)).check(assertClickable);
            // TODO: fab, navDrawer
            return this;
        }
        public AlarmActivityRobot assertOverflowMenuShown() {
            onView(withText(R.string.configAction_clearAlarms)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_importAlarms)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_exportAlarms)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sortAlarms)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
        public AlarmActivityRobot assertSortMenuShown() {
            onView(withText(R.string.configAction_sortAlarms_by_creation)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sortAlarms_by_time)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sortAlarms_offset)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_sortAlarms_enabled_first)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public AlarmActivityRobot assertAddAlarmButtonShown(boolean isShown)
        {
            onView(allOf(withContentDescription(R.string.configAction_addAlarm),
                    withClassName(endsWith("ActionButton"))
            )).check(isShown ? assertShown : assertHidden);
            return this;
        }
    }
}
