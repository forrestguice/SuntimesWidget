/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forrestguice.suntimeswidget.support.espresso.contrib;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

/**
 * Espresso action for interacting with {@link DatePicker} and {@link TimePicker}.
 *
 * <p>See <a href="http://developer.android.com/guide/topics/ui/controls/pickers.html">Pickers API
 * guide</a>
 */
public final class PickerActions {

    private PickerActions() {
        // no Instance
    }

    /**
     * Returns a {@link ViewAction} that sets a date on a {@link DatePicker}.
     *
     * @param year The year.
     * @param monthOfYear The month which is starting from zero.
     * @param dayOfMonth The day of the month.
     */
    public static ViewAction setDate(final int year, final int monthOfYear, final int dayOfMonth) {

        // monthOfYear which starts with zero in DatePicker widget.
        final int normalizedMonthOfYear = monthOfYear - 1;

        return new ViewAction() {

            @Override
            public void perform(UiController uiController, View view) {
                final DatePicker datePicker = (DatePicker) view;
                datePicker.updateDate(year, normalizedMonthOfYear, dayOfMonth);
            }

            @Override
            public String getDescription() {
                return "set date";
            }

            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(DatePicker.class), isDisplayed());
            }
        };
    }

    /**
     * Returns a {@link ViewAction} that sets a time on a {@link TimePicker}.
     *
     * @param hours the hour to set, in the range (0-23).
     * @param minutes the minute to set, in the range (0-59).
     */
    public static ViewAction setTime(final int hours, final int minutes) {

        return new ViewAction() {

            @Override
            public void perform(UiController uiController, View view) {
                final TimePicker timePicker = (TimePicker) view;
                timePicker.setCurrentHour(hours);
                timePicker.setCurrentMinute(minutes);
            }

            @Override
            public String getDescription() {
                return "set time";
            }

            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(TimePicker.class), isDisplayed());
            }
        };
    }
}