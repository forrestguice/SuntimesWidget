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
import com.forrestguice.support.test.InstrumentationRegistry;
import com.forrestguice.support.test.espresso.ViewAssertionHelper;
import com.forrestguice.support.test.filters.LargeTest;
import com.forrestguice.support.test.rule.ActivityTestRule;
import com.forrestguice.support.test.runner.AndroidJUnit4;

import android.content.Intent;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.widget.DatePicker;

import com.forrestguice.support.test.espresso.contrib.PickerActions;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;

import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

import java.io.IOException;

/**
 * Automated UI tests for the TimeDateDialog.
 */
@LargeTest
@BehaviorTest
@RunWith(AndroidJUnit4.class)
public class TimeDateDialogTest extends SuntimesActivityTestBase
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
    public void test_TimeDateDialog()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        new TimeDateDialogRobot()
                .showDialog(context)
                .assertDialogShown(context)
                //.captureScreenshot(context, "suntimes-dialog-date0")
                .cancelDialog(context)
                .assertDialogNotShown(context);
    }

    @Test
    public void test_TimeDateDialog_rotate()
    {
        activityRule.launchActivity(new Intent(Intent.ACTION_MAIN));
        Activity context = activityRule.getActivity();
        new TimeDateDialogRobot()
                .showDialog(context)
                .assertDialogShown(context)

                .doubleRotateDevice(context)
                .assertDialogShown(context)

                .cancelDialog(context)
                .assertDialogNotShown(context);
    }

    /**
     * DateDialogRobot
     */
    public static class TimeDateDialogRobot extends DialogTest.DialogRobot<TimeDateDialogRobot>
    {
        public TimeDateDialogRobot() {
            super();
            setRobot(this);
        }

        public TimeDateDialogRobot showDialog(Activity context)
        {
            String actionDateText = context.getString(R.string.configAction_viewDate);
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
            onView(withText(actionDateText)).perform(click());
            return this;
        }
        @Override
        public TimeDateDialogRobot cancelDialog(Context context) {
            onView(withId(R.id.appwidget_date_custom)).perform(pressBack());
            return this;
        }
        public TimeDateDialogRobot selectDate( int year, int month, int day ) {
            onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(year, month, day));
            return this;
        }
        public TimeDateDialogRobot clickTodayButton() {
            onView(withText(R.string.today)).perform(click());
            return this;
        }
        public TimeDateDialogRobot clickClearButton() {
            onView(withText(R.string.configAction_clearDate)).perform(click());
            return this;
        }

        @Override
        public TimeDateDialogRobot assertDialogShown(Context context) {
            onView(withId(R.id.appwidget_date_custom)).check(ViewAssertionHelper.assertShown);
            return this;
        }
        @Override
        public TimeDateDialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.appwidget_date_custom)).check(doesNotExist());
            return this;
        }
    }

    /**
     * TimeDateDialogAutomator
     */
    public static class TimeDateDialogAutomator
    {
        protected UiDevice device;

        public TimeDateDialogAutomator() {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        }

        public TimeDateDialogAutomator clickApplyButton()
        {
            final UiObject2 button = device.findObject(By.desc("Set"));
            if (button != null && button.isClickable()) {
                button.click();
            } else {
                Assert.fail("UIObject not found: apply button");
            }
            return this;
        }
    }
}
