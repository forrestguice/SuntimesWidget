/**
    Copyright (C) 2017 Forrest Guice
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
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

/**
 * Automated UI tests for the TimeDateDialog.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class TimeDateDialogTest extends SuntimesActivityTestBase
{
    /**
     * UI Test
     * Show the date dialog, rotate, swap modes, rotate again, repeatedly swap modes,
     * and then cancel the dialog.
     */
    @Test
    public void test_showDateDialog()
    {
        showDateDialog(activityRule.getActivity());
        captureScreenshot("suntimes-dialog-date0");

        rotateDevice();
        verifyDateDialog();

        if (getDateDialog_mode() == WidgetSettings.DateMode.CURRENT_DATE)
        {
            inputDateDialog_mode(WidgetSettings.DateMode.CUSTOM_DATE);  // swap the mode
            rotateDevice();                                             // rotate and re-verify state
            verifyDateDialog_modeCustom();
            inputDateDialog_mode(WidgetSettings.DateMode.CURRENT_DATE); // repeated swaps
            inputDateDialog_mode(WidgetSettings.DateMode.CUSTOM_DATE);

        } else {
            inputDateDialog_mode(WidgetSettings.DateMode.CURRENT_DATE);
            rotateDevice();
            verifyDateDialog_modeCurrent();
            inputDateDialog_mode(WidgetSettings.DateMode.CUSTOM_DATE);
            inputDateDialog_mode(WidgetSettings.DateMode.CURRENT_DATE);
        }

        cancelDateDialog();
    }

    public static void showDateDialog(Context context)
    {
        showDateDialog(context, true);
    }
    public static void showDateDialog(Context context, boolean verify)
    {
        String actionDateText = context.getString(R.string.configAction_setDate);
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(actionDateText)).perform(click());
        if (verify) {
            verifyDateDialog();
        }
    }

    public static void verifyDateDialog()
    {
        onView(withId(R.id.appwidget_date_mode)).check(assertEnabled);
        onView(withId(R.id.appwidget_date_custom)).check(assertShown);

        if (getDateDialog_mode() == WidgetSettings.DateMode.CURRENT_DATE)
            verifyDateDialog_modeCurrent();
        else verifyDateDialog_modeCustom();
    }

    public static void verifyDateDialog_modeCurrent()
    {
        onView(withId(R.id.appwidget_date_mode)).check(matches(withSpinnerText(WidgetSettings.DateMode.CURRENT_DATE.toString())));
        onView(withId(R.id.appwidget_date_custom)).check(assertDisabled);  // custom date area is disabled
    }

    public static void verifyDateDialog_modeCustom()
    {
        onView(withId(R.id.appwidget_date_mode)).check(matches(withSpinnerText(WidgetSettings.DateMode.CUSTOM_DATE.toString())));
        onView(withId(R.id.appwidget_date_custom)).check(assertEnabled);   // custom date area is enabled
    }

    public static WidgetSettings.DateMode getDateDialog_mode()
    {
        if (spinnerDisplaysText(R.id.appwidget_date_mode, WidgetSettings.DateMode.CURRENT_DATE.toString()))
            return WidgetSettings.DateMode.CURRENT_DATE;
        else if (spinnerDisplaysText(R.id.appwidget_date_mode, WidgetSettings.DateMode.CUSTOM_DATE.toString()))
            return WidgetSettings.DateMode.CUSTOM_DATE;
        else
            return null;   // unrecognized mode; fail with a null
    }

    public static void inputDateDialog_mode()
    {
        if (getDateDialog_mode() == WidgetSettings.DateMode.CURRENT_DATE)
            inputDateDialog_mode(WidgetSettings.DateMode.CUSTOM_DATE);
        else inputDateDialog_mode(WidgetSettings.DateMode.CURRENT_DATE);    // swap the mode
    }

    public static void inputDateDialog_mode( WidgetSettings.DateMode mode )
    {
        onView(withId(R.id.appwidget_date_mode)).perform(click());
        onData(allOf(is(instanceOf(WidgetSettings.DateMode.class)), is(mode)))
              .inRoot(isPlatformPopup()).perform(click());

        if (mode == WidgetSettings.DateMode.CURRENT_DATE)
            verifyDateDialog_modeCurrent();
        else verifyDateDialog_modeCustom();
    }

    public static void inputDateDialog_date( int year, int month, int day )
    {
        // TODO: set datepicker to year, month, day
        //onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month + 1, day));
    }

    public static void applyDateDialog(Context context)
    {
        String setDateText = context.getString(R.string.timedate_dialog_ok);
        onView(withText(setDateText)).perform(click());
        // TODO: verify action
    }

    public static void cancelDateDialog()
    {
        onView(withId(R.id.appwidget_date_mode)).perform(pressBack());
        onView(withId(R.id.appwidget_date_mode)).check(doesNotExist());
    }

}
