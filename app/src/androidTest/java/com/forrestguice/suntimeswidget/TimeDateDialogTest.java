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

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Automated UI tests for the TimeDateDialog.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class TimeDateDialogTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    /**
     * UI Test
     * Show the date dialog, rotate, swap modes, rotate again, repeatedly swap modes,
     * and then cancel the dialog.
     */
    @Test
    public void test_showDateDialog()
    {
        showDateDialog(activityRule.getActivity());
        captureScreenshot(activityRule.getActivity(), "suntimes-dialog-date0");

        rotateDevice(activityRule);
        verifyDateDialog(activityRule.getActivity());

        cancelDateDialog();
    }

    public static void showDateDialog(Context context)
    {
        showDateDialog(context, true);
    }
    public static void showDateDialog(Context context, boolean verify)
    {
        String actionDateText = context.getString(R.string.configAction_viewDate);
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(actionDateText)).perform(click());
        if (verify) {
            verifyDateDialog(context);
        }
    }

    public static void verifyDateDialog(Context context)
    {
        onView(withId(R.id.appwidget_date_custom)).check(assertShown);

        WidgetSettings.DateMode mode = WidgetSettings.loadDateModePref(context, 0);
        if (mode == WidgetSettings.DateMode.CURRENT_DATE)
            verifyDateDialog_modeCurrent();
        else verifyDateDialog_modeCustom();
    }

    public static void verifyDateDialog_modeCurrent()
    {
        // TODO
    }

    public static void verifyDateDialog_modeCustom()
    {
        // TODO
    }

    public static WidgetSettings.DateMode getDateDialog_mode(Context context)
    {
        return WidgetSettings.loadDateModePref(context, 0);
    }

    public static void inputDateDialog_date( int year, int month, int day )
    {
        // TODO: set datepicker to year, month, day
        //onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month + 1, day));
    }

    public static void applyDateDialog(Context context)
    {
        onView(withId(R.id.dialog_button_accept)).perform(click());
        //String setDateText = context.getString(R.string.timedate_dialog_ok);
        //onView(withText(setDateText)).perform(click());
        // TODO: verify action
    }

    public static void cancelDateDialog()
    {
        onView(withId(R.id.appwidget_date_custom)).perform(pressBack());
        onView(withId(R.id.appwidget_date_custom)).check(doesNotExist());
    }

}
