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

package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.app.Activity;
import android.content.Context;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.TimePicker;

import com.forrestguice.suntimeswidget.BehaviorTest;
import com.forrestguice.suntimeswidget.DialogTest;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.support.espresso.contrib.PickerActions;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.RetryRule;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesActivityTestBase;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.TimeDateDialogTest;
import com.forrestguice.suntimeswidget.support.espresso.action.ViewActionsContrib;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.util.InstrumentationUtils;
import com.forrestguice.util.SuntimesJUnitTestRunner;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.text.TimeDisplayText;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import java.io.IOException;
import java.util.Calendar;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertClickable;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertContainsText;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertEnabled;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertHidden;
import static com.forrestguice.suntimeswidget.support.espresso.ViewAssertionHelper.assertShown;
import static com.forrestguice.suntimeswidget.support.espresso.matcher.ViewMatchersContrib.tabLayout;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasToString;

@LargeTest
@BehaviorTest
@RunWith(SuntimesJUnitTestRunner.class)
public class AlarmCreateDialogTest extends SuntimesActivityTestBase
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

    @Test
    public void test_showAlarmDialog()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        AlarmDialogRobot robot = new AlarmDialogRobot();
        robot.showDialog(context).assertDialogShown(context);
                //.captureScreenshot(activityRule.getActivity(), "suntimes-dialog-alarm0");
                //.rotateDevice(context).assertDialogShown(context)

        AlarmClockItem.AlarmType[] types = new AlarmClockItem.AlarmType[] { AlarmClockItem.AlarmType.ALARM,
                AlarmClockItem.AlarmType.NOTIFICATION, AlarmClockItem.AlarmType.NOTIFICATION1 };
        for (AlarmClockItem.AlarmType type : types) {
            robot.selectAlarmType(type)
                    .assertAlarmTypeSelected(type);
        }
        for (AlarmClockItem.AlarmType type : types)
        {
            robot.selectAlarmType(type).assertAlarmTypeSelected(type);
            for (int i : new int[] {0, 1, 0, 1}) {
                robot.selectTabAtPosition(i)
                        .assertTabAtPosition(context, i);
            }
        }
        robot.cancelDialog(context).assertDialogNotShown(context);
    }

    @Test
    public void test_showAlarmDialog_eventTab()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        AlarmDialogRobot robot = new AlarmDialogRobot();
        robot.showDialog(context).assertDialogShown(context);

        robot.selectAlarmType(AlarmClockItem.AlarmType.ALARM).selectTabAtPosition(0)
                .assertAlarmTypeSelected(AlarmClockItem.AlarmType.ALARM)
                .assertTabAtPosition(context, 0);

        SolarEvents[] events = new SolarEvents[] { SolarEvents.SUNSET, SolarEvents.NOON, SolarEvents.SUNRISE };
        for (SolarEvents event : events) {
            robot.selectAlarmDialogEvent(event).assertAlarmDialogEvent(event);
        }

        robot.showAlarmLocationMenu(context)
                .assertAlarmLocationMenuShown(context)
                .cancelAlarmLocationMenu(context).sleep(500);

        robot.showOverflowMenu(context)
                .assertOverflowMenuShown(context)
                .cancelOverflowMenu(context).sleep(500);
    }

    @Test
    public void test_showAlarmDialog_eventTab_customEvent()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        AlarmDialogRobot robot = new AlarmDialogRobot();
        robot.showDialog(context).assertDialogShown(context);

        robot.selectAlarmType(AlarmClockItem.AlarmType.ALARM).selectTabAtPosition(0).selectAlarmDialogEvent(SolarEvents.SUNRISE)
                .assertAlarmTypeSelected(AlarmClockItem.AlarmType.ALARM)
                .assertTabAtPosition(context, 0)
                .assertAlarmDialogEvent(SolarEvents.SUNRISE);

        robot.showOverflowMenu(context)
                .assertOverflowMenuShown(context)
                .clickOverflowMenuItem_manageEvents(context);
        // TODO: verify EventActivity shown
    }

    @Test
    public void test_showAlarmDialog_eventTab_withLocation()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        AlarmDialogRobot robot = new AlarmDialogRobot();
        robot.showDialog(context).assertDialogShown(context);

        robot.selectAlarmType(AlarmClockItem.AlarmType.ALARM).selectTabAtPosition(0).selectAlarmDialogEvent(SolarEvents.SUNRISE)
                .assertAlarmTypeSelected(AlarmClockItem.AlarmType.ALARM)
                .assertTabAtPosition(context, 0)
                .assertAlarmDialogEvent(SolarEvents.SUNRISE);

        robot.showAlarmLocationMenu(context);
                //.clickAlarmLocationMenu_setLocation(context);
        // TODO: verify location dialog shown, select location, verify location changed
    }

    @Test
    public void test_showAlarmDialog_timeTab()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        AlarmDialogRobot robot = new AlarmDialogRobot();
        robot.showDialog(context).assertDialogShown(context);
        robot.selectAlarmType(AlarmClockItem.AlarmType.ALARM).selectTabAtPosition(1)
                .assertAlarmTypeSelected(AlarmClockItem.AlarmType.ALARM)
                .assertTabAtPosition(context, 1);

        robot.selectTZ_ApparentSolarTime()
                .assertTZ_ApparentSolarTime(context);

        robot.selectTZ_LocalMeanTime()
                .assertTZ_LocalMeanTime(context);

        robot.selectTZ_SystemTime()
                .assertTZ_SystemTime(context);

        int[] hours = new int[] { 3, 6, 12, 18 };
        int[] minutes = new int[] { 0, 15, 30, 45 };
        for (int i=0; i<hours.length; i++) {
            for (int j=0; j<minutes.length; j++) {
                robot.selectAlarmTime(hours[i], minutes[j])
                        .assertAlarmTime(context, hours[i], minutes[j]);
            }
        }

        robot.showAlarmDateDialog();
        new TimeDateDialogTest.TimeDateDialogRobot()
                .assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);
    }

    @Test
    public void test_showAlarmDialog_timeTab_withDate()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        AlarmDialogRobot robot = new AlarmDialogRobot();
        robot.showDialog(context).assertDialogShown(context);
        robot.selectAlarmType(AlarmClockItem.AlarmType.ALARM).selectTabAtPosition(1)
                .assertAlarmTypeSelected(AlarmClockItem.AlarmType.ALARM)
                .assertTabAtPosition(context, 1)
                .assertDateNotSet(context);

        robot.showAlarmDateDialog();    // set date
        TimeDateDialogTest.TimeDateDialogRobot robot1 = new TimeDateDialogTest.TimeDateDialogRobot();
        robot1.assertDialogShown(context)
                .selectDate(2098, 2, 3)   // in the year 2525
                .applyDialog(context);
        robot.assertDateSetTo(context, 2098, 2, 3);

        robot.showAlarmDateDialog();    // clear date
        robot1.assertDialogShown(context)
                .clickClearButton();
        robot.assertDateNotSet(context);
    }

    /**
     * AlarmDialogRobot
     */
    public static class AlarmDialogRobot extends DialogTest.DialogRobot<AlarmDialogRobot>
    {
        public AlarmDialogRobot() {
            super();
            setRobot(this);
        }

        protected static TimeDateDisplay utils = new TimeDateDisplay();

        protected AlarmDialogRobotConfig expected;
        public void initRobotConfig() {
            setRobotConfig(expected = new AlarmDialogRobotConfig());
        }
        public void setRobotConfig(AlarmDialogRobotConfig config) {
            super.setRobotConfig(config);
            expected = config;
        }

        public AlarmDialogRobot showDialog(Activity context)
        {
            openActionBarOverflowOrOptionsMenu(InstrumentationUtils.getContext());
            onView(ViewMatchers.withText(R.string.configAction_setAlarm)).perform(click());
            return this;
        }

        public AlarmDialogRobot selectAlarmType(AlarmClockItem.AlarmType type)
        {
            onView(withId(R.id.type_spin)).perform(click());
            onData(allOf(is(instanceOf(AlarmClockItem.AlarmType.class)), is(type))).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmDialogRobot assertAlarmTypeSelected(AlarmClockItem.AlarmType type)
        {
            spinnerDisplaysText(R.id.type_spin, type.getDisplayString());
            return this;
        }

        public AlarmDialogRobot showOverflowMenu(Context context) {
            onView(withId(R.id.appwidget_schedalarm_more)).perform(click());
            return this;
        }
        public AlarmDialogRobot cancelOverflowMenu(Context context) {
            onView(withText(R.string.configAction_manageEvents)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public AlarmDialogRobot clickOverflowMenuItem_manageEvents(Context context) {
            onView(withText(R.string.configAction_manageEvents)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }

        @Override
        public AlarmDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.dialog_header)).check(assertShown);
            onView(allOf(withId(R.id.dialog_button_accept), hasSibling(withId(R.id.type_spin)))).check(assertShown);
            //onView(withId(R.id.text_datetime)).check(assertShown);
            onView(withId(R.id.type_spin)).check(assertShown);
            onView(tabLayout()).check(assertShown);

            if (expected.showAlarmListButton()) {
                onView(withId(R.id.dialog_button_alarms)).check(assertShown);
            } else {
                onView(withId(R.id.dialog_button_alarms)).check(assertHidden);
            }
            return this;
        }

        public AlarmDialogRobot pressAlarmListButton()
        {
            if (expected.showAlarmListButton()) {
                onView(withId(R.id.dialog_button_alarms)).perform(click());
            } else Log.w("TEST", "skipping call to pressAlarmListButton (hidden)");
            return this;
        }

        public AlarmDialogRobot assertOverflowMenuShown(Context context) {
            onView(withText(R.string.configAction_manageEvents)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }

        public AlarmDialogRobot selectTabAtPosition(int tabPosition) {
            onView(withId(R.id.tabLayout)).perform(ViewActionsContrib.selectTabAtPosition(tabPosition));
            return this;
        }
        public AlarmDialogRobot assertTabAtPosition(Context context, int tabPosition)
        {
            Location location = expected.getLocation(context);
            switch (tabPosition)
            {
                case 1:
                    onView(withText(R.string.schedalarm_dialog_tab_time)).check(matches(isSelected()));
                    onView(withId(R.id.modepicker)).check(assertShown);
                    onView(withId(R.id.datePicker)).check(assertShown);
                    onView(withId(R.id.timepicker)).check(assertShown);
                    onView(withId(R.id.modepicker)).check(assertClickable);
                    onView(withId(R.id.datePicker)).check(assertClickable);
                    break;

                case 0:
                default:
                    onView(withText(R.string.schedalarm_dialog_tab_event)).check(matches(isSelected()));
                    onView(withId(R.id.appwidget_schedalarm_mode)).check(assertShown);
                    onView(withId(R.id.appwidget_schedalarm_mode)).check(assertEnabled);
                    onView(withId(R.id.appwidget_schedalarm_mode)).check(assertClickable);
                    onView(withId(R.id.appwidget_schedalarm_location)).check(assertShown);
                    onView(withId(R.id.appwidget_schedalarm_location)).check(assertClickable);
                    onView(withId(R.id.appwidget_schedalarm_location)).check(assertContainsText(location.getLabel()));
                    onView(withId(R.id.appwidget_schedalarm_more)).check(assertShown);
                    onView(withId(R.id.appwidget_schedalarm_more)).check(assertEnabled);
                    onView(withId(R.id.appwidget_schedalarm_more)).check(assertClickable);
                    break;
            }
            return this;
        }

        public AlarmDialogRobot selectAlarmTime(int hours, int minutes)
        {
            onView(isAssignableFrom(TimePicker.class)).perform(PickerActions.setTime(hours, minutes));
            return this;
        }
        public AlarmDialogRobot assertAlarmTime(Context context, int hours, int minutes)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hours);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, 0);

            TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, 0);
            TimeDisplayText text = utils.calendarTimeShortDisplayString(AndroidResources.wrap(context), calendar, false, timeFormat);
            onView( allOf(withParent(withId(R.id.text_datetime)), isAssignableFrom(TextView.class), isDisplayed()) )
                    .check(assertContainsText(text.toString()));
            return this;
        }

        public AlarmDialogRobot showAlarmDateDialog() {
            onView(withId(R.id.datePicker)).perform(click());
            return this;
        }

        public AlarmDialogRobot selectTZ_ApparentSolarTime() {
            onView(withId(R.id.modepicker)).perform(click());
            onView(withText(R.string.time_apparent)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmDialogRobot assertTZ_ApparentSolarTime(Context context) {
            spinnerDisplaysText(context, R.id.modepicker, R.string.time_apparent);
            onView(withId(R.id.locationPicker)).check(assertShown);
            onView(withId(R.id.locationPicker)).check(assertClickable);
            return this;
        }

        public AlarmDialogRobot selectTZ_LocalMeanTime() {
            onView(withId(R.id.modepicker)).perform(click());
            onView(withText(R.string.time_localMean)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmDialogRobot assertTZ_LocalMeanTime(Context context) {
            spinnerDisplaysText(context, R.id.modepicker, R.string.time_localMean);
            onView(withId(R.id.locationPicker)).check(assertShown);
            onView(withId(R.id.locationPicker)).check(assertClickable);
            return this;
        }

        public AlarmDialogRobot selectTZ_SystemTime() {
            onView(withId(R.id.modepicker)).perform(click());
            onView(withText(R.string.timezoneMode_current)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmDialogRobot assertTZ_SystemTime(Context context) {
            spinnerDisplaysText(context, R.id.modepicker, R.string.timezoneMode_current);
            onView(withId(R.id.locationPicker)).check(assertHidden);
            return this;
        }

        public AlarmDialogRobot assertDateNotSet(Context context) {
            onView(withId(R.id.datePicker)).check(matches(withText(equalTo(""))));
            return this;
        }
        public AlarmDialogRobot assertDateSetTo(Context context, int year, int month, int day) {
            onView(withId(R.id.datePicker)).check(assertContainsText("" + year));
            return this;
        }

        public AlarmDialogRobot selectAlarmDialogEvent(SolarEvents mode)
        {
            onView(withId(R.id.appwidget_schedalarm_mode)).perform(click());
            onData(allOf(is(instanceOf(AlarmEvent.AlarmEventItem.class)), hasToString(mode.toString())))
                    .inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmDialogRobot assertAlarmDialogEvent(SolarEvents mode)
        {
            spinnerDisplaysText(R.id.appwidget_schedalarm_mode, mode.toString());
            return this;
        }

        public AlarmDialogRobot showAlarmLocationMenu(Context context) {
            onView(withId(R.id.appwidget_schedalarm_location)).perform(click());
            return this;
        }
        public AlarmDialogRobot clickAlarmLocationMenu_setLocation(Context context) {
            onView(withText(R.string.configAction_setAlarmLocation)).inRoot(isPlatformPopup()).perform(click());
            return this;
        }
        public AlarmDialogRobot cancelAlarmLocationMenu(Context context) {
            onView(withText(R.string.configLabel_location_fromapp)).inRoot(isPlatformPopup()).perform(pressBack());
            return this;
        }
        public AlarmDialogRobot assertAlarmLocationMenuShown(Context context)
        {
            onView(withText(R.string.configLabel_location_fromapp)).inRoot(isPlatformPopup()).check(assertShown);
            onView(withText(R.string.configAction_setAlarmLocation)).inRoot(isPlatformPopup()).check(assertShown);
            return this;
        }
    }

    public static class AlarmDialogRobotConfig extends DialogTest.DialogRobotConfig
    {
        public boolean showAlarmListButton() {
            return true;
        }
    }

}
