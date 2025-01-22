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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.forrestguice.support.test.espresso.Espresso.onView;
import static com.forrestguice.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static com.forrestguice.support.test.espresso.action.ViewActions.click;
import static com.forrestguice.support.test.espresso.action.ViewActions.pressBack;
import static com.forrestguice.support.test.espresso.assertion.ViewAssertions.doesNotExist;

import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withId;
import static com.forrestguice.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Automated UI tests for the TimeDateDialog.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class TimeDateDialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Before
    public void beforeTest() {
        setAnimationsEnabled(false);
    }
    @After
    public void afterTest() {
        setAnimationsEnabled(true);
    }

    @Test
    public void test_showDateDialog()
    {
        Activity context = activityRule.getActivity();
        new TimeDateDialogRobot()
                .showDialog(context).assertDialogShown(context)
                //.captureScreenshot(context, "suntimes-dialog-date0")
                .rotateDevice(context).assertDialogShown(context)
                .cancelDialog(context).assertDialogNotShown(context);
    }

    /**
     * DateDialogRobot
     */
    public static class TimeDateDialogRobot extends DialogTest.DialogRobotBase implements DialogTest.DialogRobot
    {
        @Override
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
        @Override
        public TimeDateDialogRobot assertDialogShown(Context context)
        {
            onView(withId(R.id.appwidget_date_custom)).check(ViewAssertionHelper.assertShown);
            return this;
        }
        @Override
        public TimeDateDialogRobot assertDialogNotShown(Context context) {
            onView(withId(R.id.appwidget_date_custom)).check(doesNotExist());
            return this;
        }

        public TimeDateDialogRobot inputDate( int year, int month, int day )
        {
            // TODO: set datepicker to year, month, day
            //onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month + 1, day));
            return this;
        }
    }
}
