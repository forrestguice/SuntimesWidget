/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.util.Log;

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

            verify_stringArrayLength("lengthUnits_values", R.array.lengthUnits_values, "lengthUnits_display", R.array.lengthUnits_display);
            verify_stringArrayLength("alarm_hardwarebutton_actions_values", R.array.alarm_hardwarebutton_actions_values, "alarm_hardwarebutton_actions_display", R.array.alarm_hardwarebutton_actions_display);
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

    @Test
    public void test_plurals()
    {
        double[] values = new double[]        {   0,   0.25,   1,   1.83,   2,   3,   10 };   // trying to capture; one, none, few, many, other, ...
        String[] displayValues = new String[] { "0", "0.25", "1", "1.83", "2", "3", "10" };

        Context context = activityRule.getActivity();
        String[] locales = context.getResources().getStringArray(R.array.locale_values);
        for (String languageTag : locales)
        {
            AppSettings.loadLocale(context, languageTag);

            verify_pluralFormat("units_feet_long", R.plurals.units_feet_long, values, displayValues);
            verify_pluralFormat("units_meters_long", R.plurals.units_meters_long, values, displayValues);

            verify_pluralFormatI("units_hours", R.plurals.units_hours, values);
            verify_pluralFormatI("units_minutes", R.plurals.units_minutes, values);
            verify_pluralFormatI("units_seconds", R.plurals.units_seconds, values);
            verify_pluralFormatI("themePlural", R.plurals.themePlural, values);
            verify_pluralFormatI("offset_before_plural", R.plurals.offset_before_plural, values);
            verify_pluralFormatI("offset_after_plural", R.plurals.offset_after_plural, values);
            verify_pluralFormatI("offset_at_plural", R.plurals.offset_at_plural, values);
        }
    }

    public void verify_pluralFormat(String tag1, int pluralID, double value[], String displayValue[])
    {
        Context context = activityRule.getActivity();
        assertTrue("value[] and displayValue[] must have the same dimension!", value.length == displayValue.length);

        boolean[] r = new boolean[value.length];
        for (int i=0; i<value.length; i++)
        {
            try {
                context.getResources().getQuantityString(pluralID, (int)value[i], displayValue[i]);
                r[i] = true;

            } catch (Exception e) {
                Log.e("verify_pluralFormat", "The format of " + tag1 + " (" + value[i] + ") is INVALID! locale: " + AppSettings.getLocale().toString());
                r[i] = false;
            }
        }
        boolean allTrue = true;
        for (boolean b : r) {
            allTrue &= b;
        }
        assertTrue("The format of " + tag1 + " is INVALID! locale: " + AppSettings.getLocale().toString(), allTrue);
    }

    public void verify_pluralFormatI(String tag1, int pluralID, double value[])
    {
        Context context = activityRule.getActivity();
        boolean[] r = new boolean[value.length];
        for (int i=0; i<value.length; i++)
        {
            try {
                context.getResources().getQuantityString(pluralID, (int)value[i], (int)value[i]);
                r[i] = true;

            } catch (Exception e) {
                Log.e("verify_pluralFormat", "The format of " + tag1 + " (" + value[i] + ") is INVALID! locale: " + AppSettings.getLocale().toString());
                r[i] = false;
            }
        }
        boolean allTrue = true;
        for (boolean b : r) {
            allTrue &= b;
        }
        assertTrue("The format of " + tag1 + " is INVALID! locale: " + AppSettings.getLocale().toString(), allTrue);
    }

}
