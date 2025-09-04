/**
    Copyright (C) 2018-2022 Forrest Guice
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
import android.content.res.Resources;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.getfix.BuildPlacesTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SuntimesResTest extends SuntimesActivityTestBase
{
    @Rule
    public ActivityTestRule<SuntimesActivity> activityRule = new ActivityTestRule<>(SuntimesActivity.class);

    @Test
    public void test_selectFormatGender()
    {
        Context context = activityRule.getActivity();
        String[] locales = context.getResources().getStringArray(R.array.locale_values);
        for (String languageTag : locales)
        {
            AppSettings.loadLocale(context, languageTag);
            verify_selectFormatGender("time_gender", R.string.time_gender);
            verify_selectFormatGender("date_gender", R.string.date_gender);
            verify_selectFormatGender("sunevent_phrase_gender", R.string.sunevent_phrase_gender);
            verify_selectFormatGender("shadowevent_phrase_gender", R.string.shadowevent_phrase_gender);
        }
    }

    protected void verify_selectFormatGender(String tag, int stringID)
    {
        Context context = activityRule.getActivity();
        String value = context.getString(stringID);
        assertNotNull(value);
        assertTrue(tag + " be a value supported by SelectFormat", value.equals("female") || value.equals("other"));
    }

    @Test
    public void test_stringArrays()
    {
        Context context = activityRule.getActivity();
        String[] locales = context.getResources().getStringArray(R.array.locale_values);
        for (String languageTag : locales)
        {
            AppSettings.loadLocale(context, languageTag);
            verify_stringArrayLength("locale_values", R.array.locale_values, "locale_display", R.array.locale_display);
            verify_stringArrayLength("locale_credits", R.array.locale_credits, "locale_display", R.array.locale_display);

            verify_stringArrayLength("appThemes_values", R.array.appThemes_values, "appThemes_display", R.array.appThemes_display);
            verify_stringArrayLength("appThemes_dark_values", R.array.appThemes_dark_values, "appThemes_dark_display", R.array.appThemes_dark_display);
            verify_stringArrayLength("appThemes_light_values", R.array.appThemes_light_values, "appThemes_light_display", R.array.appThemes_light_display);

            verify_stringArrayLength("textSize_values", R.array.textSize_values, "textSize_display", R.array.textSize_display);
            verify_stringArrayValuesOfEnum("textSize_values", R.array.textSize_values, AppSettings.TextSize.class);

            verify_stringArrayValuesOfEnum("localeMode_values", R.array.localeMode_values, AppSettings.LocaleMode.class);
            verify_stringArrayLength("localeMode_values", R.array.localeMode_values, "localeMode_display", R.array.localeMode_display);
            verify_stringArrayLength("localeMode_display", R.array.localeMode_display, "LocaleMode (ENUM)", AppSettings.LocaleMode.values());

            verify_stringArrayLength("solarevents_short", R.array.solarevents_short, "solarevents_long", R.array.solarevents_long);
            verify_stringArrayLength("solarevents_short", R.array.solarevents_short, "SolarEvents (ENUM)", SolarEvents.values());

            verify_stringArrayLength("solarevents_long1", R.array.solarevents_short, "solarevents_short", R.array.solarevents_short);
            verify_stringArrayLength("solarevents_long1", R.array.solarevents_long1, "solarevents_gender", R.array.solarevents_gender);
            verify_stringArrayLength("solarevents_long1", R.array.solarevents_long1, "solarevents_quantity", R.array.solarevents_quantity);

            verify_stringArrayLength("directions_short", R.array.directions_short, "directions_long", R.array.directions_long);
            verify_stringArrayLength("directions_short", R.array.directions_short, "CardinalDirection (ENUM)", SuntimesUtils.CardinalDirection.values());

            verify_stringArrayValuesOfEnum("timezoneSort_values", R.array.timezoneSort_values, WidgetTimezones.TimeZoneSort.class);
            verify_stringArrayLength("timezoneSort_values", R.array.timezoneSort_values, "timezoneSort_display", R.array.timezoneSort_display);
            verify_stringArrayLength("timezoneSort_display", R.array.timezoneSort_display, "TimeZoneSort (ENUM)", WidgetTimezones.TimeZoneSort.values());

            verify_stringArrayLength("widgetgravity", R.array.widgetgravity, "WidgetGravity (ENUM)", WidgetSettings.WidgetGravity.values());

            verify_stringArrayLength("navMode_values", R.array.navMode_values, "navMode_display", R.array.navMode_display);

            verify_stringArrayLength("compareMode_values", R.array.compareMode_values, "compareMode_display", R.array.compareMode_display);
            verify_stringArrayValuesOfEnum("compareMode_values", R.array.compareMode_values, WidgetSettings.CompareMode.class);

            verify_stringArrayLength("tapActions_titles", R.array.tapActions_titles, "tapActions_display", R.array.tapActions_display);
            verify_stringArrayLength("tapActions_display", R.array.tapActions_display, "SuntimesAction (ENUM)", WidgetActions.SuntimesAction.values());

            verify_stringArrayLength("clockTapActions_values", R.array.clockTapActions_values, "clockTapActions_display", R.array.clockTapActions_display);
            verify_enumTapActions("clockTapActions_values", R.array.clockTapActions_values);

            verify_stringArrayLength("dateTapActions_values", R.array.dateTapActions_values, "dateTapActions_display", R.array.dateTapActions_display);
            verify_enumTapActions("dateTapActions_values", R.array.dateTapActions_values);

            verify_stringArrayValuesOfEnum("timeFormatMode_values", R.array.timeFormatMode_values, WidgetSettings.TimeFormatMode.class);
            verify_stringArrayLength("timeFormatMode_values", R.array.timeFormatMode_values, "timeFormatMode_display", R.array.timeFormatMode_display);

            verify_stringArrayValuesOfEnum("lengthUnits_values", R.array.lengthUnits_values, WidgetSettings.LengthUnit.class);
            verify_stringArrayLength("lengthUnits_values", R.array.lengthUnits_values, "lengthUnits_display", R.array.lengthUnits_display);

            verify_stringArrayLength("alarm_hardwarebutton_actions_values", R.array.alarm_hardwarebutton_actions_values, "alarm_hardwarebutton_actions_display", R.array.alarm_hardwarebutton_actions_display);
            verify_stringArrayLength("getFix_maxAge_values", R.array.getFix_maxAge_values, "getFix_maxAge_display", R.array.getFix_maxAge_display);
            verify_stringArrayLength("getFix_maxElapse_values", R.array.getFix_maxElapse_values, "getFix_maxElapse_display", R.array.getFix_maxElapse_display);
            verify_stringArrayLength("noteTapActions_values", R.array.noteTapActions_values, "noteTapActions_display", R.array.noteTapActions_display);

            verify_stringArrayValuesOfEnum("solsticeTrackingMode_values", R.array.solsticeTrackingMode_values, WidgetSettings.TrackingMode.class);
            verify_stringArrayLength("solsticeTrackingMode_values", R.array.solsticeTrackingMode_values, "solsticeTrackingMode_display", R.array.solsticeTrackingMode_display);

            verify_stringArrayLength("launcherMode_values", R.array.launcherMode_values, "launcherMode_display", R.array.launcherMode_display);
            verify_stringArrayLength("emphasizefield_values", R.array.emphasizefield_values, "emphasizefield_display", R.array.emphasizefield_display);
            verify_stringArrayLength("headerText_values", R.array.headerText_values, "headerText_display", R.array.headerText_display);

            verify_stringArrayLength("dismissChallenge_values", R.array.dismissChallenge_values, "dismissChallenge_display", R.array.dismissChallenge_display);
            verify_stringArrayValuesOfEnum("dismissChallenge_values", R.array.dismissChallenge_values, AlarmSettings.DismissChallenge.class);
        }
    }

    @SuppressWarnings({"rawtypes", "unused"})
    public void verify_stringArrayValuesOfEnum(String tag1, int array1Id, Class enumClass)
    {
        Context context = activityRule.getActivity();
        String[] values = context.getResources().getStringArray(array1Id);
        for (String value : values) {
            Enum<?> e = Enum.valueOf(enumClass, value);
        }
    }

    public void verify_enumTapActions(String tag1, int array1Id)
    {
        Context context = activityRule.getActivity();
        String[] values = context.getResources().getStringArray(array1Id);
        ArrayList<String> badValues = new ArrayList<>();
        StringBuilder badValuesString = new StringBuilder();

        for (String value : values)
        {
            try {
                @SuppressWarnings("unused")
                WidgetActions.SuntimesAction action = WidgetActions.SuntimesAction.valueOf(value);

            } catch (IllegalArgumentException e) {
                badValues.add(value);
                badValuesString.append(value);
                badValuesString.append(",");
            }
        }

        assertEquals("The array + (" + tag1 + ") contains invalid enum values: " + badValuesString, 0, badValues.size());
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
        assertEquals("The size of " + tag1 + " (" + a1.length + ") and " + tag2 + " (" + a2.length + ") DOES NOT MATCH! locale: " + AppSettings.getLocale().toString(), a1.length, a2.length);
    }

    @Test
    public void test_selectFormat_offsetMessages()
    {
        Context context = activityRule.getActivity();
        String[] locales = context.getResources().getStringArray(R.array.locale_values);
        for (String languageTag : locales)
        {
            AppSettings.loadLocale(context, languageTag);
            verify_selectFormat_offsetMessage("offset_before_msg1", R.string.offset_before_msg1);
            verify_selectFormat_offsetMessage("offset_after_msg1", R.string.offset_after_msg1);
        }
    }

    public void verify_selectFormat_offsetMessage(String tag1, int stringID)
    {
        Context context = activityRule.getActivity();
        String pattern = context.getResources().getString(stringID);
        assertTrue(tag1 + " pattern contains `{0}` :: " + AppSettings.getLocale().toString(), pattern.contains("{0}"));
        assertTrue(tag1 + " pattern contains `{1, plural` :: " + AppSettings.getLocale().toString(), pattern.contains("{1, plural,"));
        assertTrue(tag1 + " pattern contains `{2, select` :: " + AppSettings.getLocale().toString(), pattern.contains("{2, select,"));
        assertTrue(tag1 + " pattern contains `{3}` :: " + AppSettings.getLocale().toString(), pattern.contains("{3}"));
        assertTrue(tag1 + " pattern contains `other {` :: " + AppSettings.getLocale().toString(), pattern.contains("other {"));
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
            verify_pluralFormat("units_miles_long", R.plurals.units_miles_long, values, displayValues);

            verify_pluralFormat("units_meters_long", R.plurals.units_meters_long, values, displayValues);
            verify_pluralFormat("units_kilometers_long", R.plurals.units_kilometers_long, values, displayValues);

            verify_pluralFormatI("units_days", R.plurals.units_days, values);
            verify_pluralFormatI("units_hours", R.plurals.units_hours, values);
            verify_pluralFormatI("units_minutes", R.plurals.units_minutes, values);
            verify_pluralFormatI("units_seconds", R.plurals.units_seconds, values);

            verify_pluralFormatI("alarmPlural", R.plurals.alarmPlural, values);
            verify_pluralFormatI("eventPlural", R.plurals.eventPlural, values);
            verify_pluralFormatI("placePlural", R.plurals.placePlural, values);
            verify_pluralFormatI("cyclePlural", R.plurals.cyclePlural, values);
            verify_pluralFormatI("itemsPlural", R.plurals.itemsPlural, values);
            verify_pluralFormatI("widgetPlural", R.plurals.widgetPlural, values);
            verify_pluralFormatI("locationdelete_dialog_success", R.plurals.locationdelete_dialog_success, values);
            verify_pluralFormatI("themePlural", R.plurals.themePlural, values);

            verify_pluralFormatI("offset_before_plural", R.plurals.offset_before_plural, values);
            verify_pluralFormatI("offset_after_plural", R.plurals.offset_after_plural, values);
            verify_pluralFormatI("offset_at_plural", R.plurals.offset_at_plural, values);
        }
    }

    public void verify_pluralFormat(String tag1, int pluralID, double[] value, String[] displayValue)
    {
        Context context = activityRule.getActivity();
        assertEquals("value[] and displayValue[] must have the same dimension!", value.length, displayValue.length);

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

    public void verify_pluralFormatI(String tag1, int pluralID, double[] value)
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

    protected HashMap<String,String> latitudeMap = new HashMap<>();    // latitude string to label
    protected HashMap<String,String> longitudeMap = new HashMap<>();   // longitude string to label

    @Test
    public void test_places()
    {
        Context context = activityRule.getActivity();
        Resources r = context.getResources();

        latitudeMap.clear();
        longitudeMap.clear();

        String[] groups = r.getStringArray(R.array.place_groups);
        for (String group : groups) {
            test_placeGroup(context, group);
        }
    }
    
    protected void test_placeGroup(Context context, String groupItem)
    {
        assertNotNull(groupItem);
        String[] groupParts = groupItem.split(",");
        assertEquals(2, groupParts.length);

        String groupName = groupParts[0].trim();
        assertFalse(groupName.isEmpty());
        int groupID = context.getResources().getIdentifier(groupName, "array", context.getPackageName());
        assertTrue(groupName + " must reference an array resource!", groupID != 0);

        String labelName = groupParts[1].trim();
        assertFalse(labelName.isEmpty());
        int labelID = context.getResources().getIdentifier(labelName, "string", context.getPackageName());
        assertTrue(labelName + " must reference a string resource!", labelID != 0);

        String[] items = context.getResources().getStringArray(groupID);
        if (groupName.startsWith("place_group"))
        {
            for (String item : items) {
                test_placeGroup(context, item);
            }
        }

        if (groupName.startsWith("places"))
        {
            for (String item : items)
            {
                assertNotNull(item);
                String[] itemParts = BuildPlacesTask.splitCSV(item, ','); //item.split(",");
                assertTrue(item + " should have at least 4 parts but has " + itemParts.length, itemParts.length >= 4);
                assertTrue(item + " should have no more than 5 parts but has " + itemParts.length, itemParts.length <= 5);   // may contain optional comment
                assertNotNull(itemParts[0]);
                assertNotNull(itemParts[1]);
                assertNotNull(itemParts[2]);
                assertNotNull(itemParts[3]);

                verifyStringIsDouble("latitude of " + itemParts[0] + " should be a double; was " + itemParts[1], itemParts[1]);
                verifyStringIsDouble("longitude of " + itemParts[0] + " should be a double; was " + itemParts[2], itemParts[2]);
                verifyStringIsDouble("altitude of " + itemParts[0] + " should be a double; was " + itemParts[3], itemParts[3]);

                String latitude = itemParts[1].trim();
                String longitude = itemParts[2].trim();
                assertFalse("places should be unique; " + itemParts[0] + " has latitude of " + latitude + ", but so does " + latitudeMap.get(latitude), latitudeMap.containsKey(latitude));
                assertFalse("places should be unique; " + itemParts[0] + " has longitude of " + longitude + ", but so does " + longitudeMap.get(longitude), longitudeMap.containsKey(longitude));
                latitudeMap.put(latitude, itemParts[0]);
                longitudeMap.put(longitude, itemParts[0]);
            }
        }
    }

    protected void verifyStringIsDouble(String message, String value)
    {
        try {
            @SuppressWarnings("unused")
            double d = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            fail(message);
        }
    }

}
