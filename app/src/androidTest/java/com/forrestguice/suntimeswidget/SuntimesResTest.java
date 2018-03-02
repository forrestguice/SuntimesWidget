/**
    Copyright (C) 2018 Forrest Guice
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
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesResTest extends SuntimesActivityTestBase
{
    @Test
    public void test_stringArrays()
    {
        Context context = activityRule.getActivity();
        String[] locales = context.getResources().getStringArray(R.array.locale_values);
        for (String languageTag : locales)
        {
            AppSettings.loadLocale(context, languageTag);
            verify_stringArrayLength("locale_values", R.array.locale_values, "locale_display", R.array.locale_display);
            verify_stringArrayLength("appThemes_values", R.array.appThemes_values, "appThemes_display", R.array.appThemes_display);

            verify_stringArrayLength("localeMode_values", R.array.localeMode_values, "localeMode_display", R.array.localeMode_display);
            verify_stringArrayLength("localeMode_display", R.array.localeMode_display, "LocaleMode (ENUM)", AppSettings.LocaleMode.values());

            verify_stringArrayLength("solarevents_short", R.array.solarevents_short, "solarevents_long", R.array.solarevents_long);
            verify_stringArrayLength("solarevents_short", R.array.solarevents_short, "SolarEvents (ENUM)", SolarEvents.values());

            verify_stringArrayLength("directions_short", R.array.directions_short, "directions_long", R.array.directions_long);
            verify_stringArrayLength("directions_short", R.array.directions_short, "CardinalDirection (ENUM)", SuntimesUtils.CardinalDirection.values());

            verify_stringArrayLength("timezoneSort_values", R.array.timezoneSort_values, "timezoneSort_display", R.array.timezoneSort_display);
            verify_stringArrayLength("timezoneSort_display", R.array.timezoneSort_display, "TimeZoneSort (ENUM)", WidgetTimezones.TimeZoneSort.values());

            verify_stringArrayLength("clockTapActions_values", R.array.clockTapActions_values, "clockTapActions_display", R.array.clockTapActions_display);
            verify_stringArrayLength("clockTapActions_display", R.array.clockTapActions_display, "ClockTapAction (ENUM)", AppSettings.ClockTapAction.values());

            verify_stringArrayLength("dateTapActions_values", R.array.dateTapActions_values, "dateTapActions_display", R.array.dateTapActions_display);
            verify_stringArrayLength("dateTapActions_display", R.array.dateTapActions_display, "DateTapAction (ENUM)", AppSettings.DateTapAction.values());

            verify_stringArrayLength("timeFormatMode_values", R.array.timeFormatMode_values, "timeFormatMode_display", R.array.timeFormatMode_display);
            verify_stringArrayLength("timeFormatMode_display", R.array.timeFormatMode_display, "TimeFormatMode (ENUM)", WidgetSettings.TimeFormatMode.values());

            verify_stringArrayLength("getFix_maxAge_values", R.array.getFix_maxAge_values, "getFix_maxAge_display", R.array.getFix_maxAge_display);
            verify_stringArrayLength("getFix_maxElapse_values", R.array.getFix_maxElapse_values, "getFix_maxElapse_display", R.array.getFix_maxElapse_display);
            verify_stringArrayLength("noteTapActions_values", R.array.noteTapActions_values, "noteTapActions_display", R.array.noteTapActions_display);
            verify_stringArrayLength("solsticeTrackingMode_values", R.array.solsticeTrackingMode_values, "solsticeTrackingMode_display", R.array.solsticeTrackingMode_display);
        }
    }

    public void verify_stringArrayLength(String tag1, int array1Id, String tag2, int array2Id)
    {
        Context context = activityRule.getActivity();
        String[] a1 = context.getResources().getStringArray(array1Id);
        String[] a2 = context.getResources().getStringArray(array2Id);
        verify_arrayLength(tag1, a1, tag2, a2);
    }

    public void verify_stringArrayLength(String tag1, int array1Id, String tag2, Object[] array2)
    {
        Context context = activityRule.getActivity();
        String[] a1 = context.getResources().getStringArray(array1Id);
        verify_arrayLength(tag1, a1, tag2, array2);
    }

    public void verify_stringArrayLength(String tag1, Object[] array1, String tag2, int array2Id)
    {
        Context context = activityRule.getActivity();
        String[] a2 = context.getResources().getStringArray(array2Id);
        verify_arrayLength(tag1, array1, tag2, a2);
    }

    public void verify_arrayLength(String tag1, Object[] a1, String tag2, Object[] a2)
    {
        assertTrue("The size of " + tag1 + " and " + tag2 + "DOES NOT MATCH! locale: " + AppSettings.getLocale().toString(),
                a1.length == a2.length);
    }


}
