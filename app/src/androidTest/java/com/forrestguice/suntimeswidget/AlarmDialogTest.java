/**
    Copyright (C) 2017-2019 Forrest Guice
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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.SolarEvents;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AlarmDialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    /**
     * UI Test
     *
     * Show, rotate, and dismiss the alarm dialog.
     */
    @Test
    public void test_showAlarmDialog()
    {
        showAlarmDialog(activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-dialog-alarm0");

        //rotateDevice(activityRule);
        verifyAlarmDialog();
        cancelAlarmDialog();
    }

    public static void showAlarmDialog(Context context)
    {
        showAlarmDialog(context, true);
    }
    public static void showAlarmDialog(Context context, boolean verify)
    {
        String actionAboutText = context.getString(R.string.configAction_setAlarm);
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(actionAboutText)).perform(click());
        if (verify) {
            verifyAlarmDialog();
        }
    }

    public static void inputAlarmDialog_alarm(SolarEvents mode)
    {
        onView(withId(R.id.appwidget_schedalarm_mode)).perform(click());
        onData(allOf(is(instanceOf(SolarEvents.class)), is(mode))).inRoot(isPlatformPopup()).perform(click());
        // TODO: verify action
    }

    public static void verifyAlarmDialog()
    {
        onView(withId(R.id.dialog_header)).check(assertShown);
        //onView(withId(R.id.appwidget_schedalarm_note)).check(assertShown);
    }

    public static void applyAlarmDialog(Context context)
    {
        onView(withId(R.id.dialog_button_accept)).perform(click());
        // TODO: verify action
    }

    public static void cancelAlarmDialog()
    {
        onView(withId(R.id.dialog_header)).perform(pressBack());
        onView(withId(R.id.dialog_header)).check(doesNotExist());
    }

}
