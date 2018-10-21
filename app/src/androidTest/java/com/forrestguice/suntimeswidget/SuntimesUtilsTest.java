/**
    Copyright (C) 2017-2018 Forrest Guice
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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.text.style.ImageSpan;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

@SuppressWarnings("ALL")
@RunWith(AndroidJUnit4.class)
public class SuntimesUtilsTest
{
    private Context mockContext;
    private SuntimesUtils utils;

    private int warningSize = 16;
    private int warningColor = Color.RED;
    private int warningDrawableID = R.drawable.ic_action_warning;
    private Drawable warningDrawable;

    @Before
    public void setup()
    {
        mockContext = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
        warningDrawable = mockContext.getResources().getDrawable(warningDrawableID);

        SuntimesUtils.initDisplayStrings(mockContext);
        utils = new SuntimesUtils();
    }

    @Test
    public void test_TimeDisplayText()
    {
        // test constructor ()
        SuntimesUtils.TimeDisplayText text1 = new SuntimesUtils.TimeDisplayText();
        assertTrue(text1.getValue().equals(""));
        assertTrue(text1.getUnits().equals(""));
        assertTrue(text1.getSuffix().equals(""));
        assertTrue("toString should be empty", text1.toString().equals(""));

        // test constructor (value)
        String value2 = "value2";
        SuntimesUtils.TimeDisplayText text2 = new SuntimesUtils.TimeDisplayText(value2);
        assertTrue(text2.getValue().equals(value2));
        assertTrue(text2.getUnits().equals(""));
        assertTrue(text2.getSuffix().equals(""));
        assertTrue("toString should be value only", text2.toString().equals(value2));

        // test constructor (value, units, suffix)
        String value3 = "value3";
        String units3 = "minutes";
        String suffix3 = "longer";
        String toString3 = value3 + " " + units3 + " " + suffix3;
        SuntimesUtils.TimeDisplayText text3 = new SuntimesUtils.TimeDisplayText(value3, units3, suffix3);
        assertTrue(text3.getValue().equals(value3));
        assertTrue(text3.getUnits().equals(units3));
        assertTrue(text3.getSuffix().equals(suffix3));
        assertTrue("toString should \"" + toString3 + "\"", text3.toString().equals(toString3));

        // test equals & setSuffix
        SuntimesUtils.TimeDisplayText text4 = new SuntimesUtils.TimeDisplayText(value3, units3, suffix3);
        assertTrue("should be equal", text3.equals(text4));

        String suffix4 = "shorter";
        text4.setSuffix(suffix4);
        assertTrue(text4.getSuffix().equals(suffix4));
        assertTrue("no longer equal", !text3.equals(text4));

        text4.setSuffix("");
        String expected4 = value3 + " " + units3;
        assertTrue("toString should be \"" + expected4 + "\"", text4.toString().equals(expected4));

        // test toString
        SuntimesUtils.TimeDisplayText text5 = new SuntimesUtils.TimeDisplayText(value3, "", suffix3);
        String expected5 = value3 + " " + suffix3;
        assertTrue("toString should be \"" + expected5 + "\"", text5.toString().equals(expected5));

        SuntimesUtils.TimeDisplayText text6 = new SuntimesUtils.TimeDisplayText("", units3, "");
        assertTrue("toString should be \"" + units3 + "\"", text6.toString().equals(units3));

        SuntimesUtils.TimeDisplayText text7 = new SuntimesUtils.TimeDisplayText("", "", suffix3);
        assertTrue("toString should be \"" + suffix3 + "\"", text7.toString().equals(suffix3));

        SuntimesUtils.TimeDisplayText text8 = new SuntimesUtils.TimeDisplayText("", units3, suffix3);
        String expected8 = units3 + " " + suffix3;
        assertTrue("toString should be \"" + expected8 + "\"", text8.toString().equals(expected8));

        // test rawValue
        long time = Calendar.getInstance().getTimeInMillis();
        text8.setRawValue(time);
        assertTrue(text8.getRawValue() == time);
    }


    @Test
    public void test_calendarTimeShortDisplayString()
    {
        WidgetSettings.TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(mockContext, 0);
        test_calendarTimeShortDisplayString_12hr();
        test_calendarTimeShortDisplayString_24hr();
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, mode);
    }

    @Test
    public void test_calendarTimeShortDisplayString_12hr()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, WidgetSettings.TimeFormatMode.MODE_12HR);
        SuntimesUtils.initDisplayStrings(mockContext);
        String[] amPm = new SimpleDateFormat("a", Locale.getDefault()).getDateFormatSymbols().getAmPmStrings();  // am/pm strings

        long utcMillis = 1493315892762L;                          // april 27
        TimeZone tzAz = TimeZone.getTimeZone("US/Arizona");       // 10:58 AM in arizona
        TimeZone tzEast = TimeZone.getTimeZone("US/Eastern");     // 1:58 PM on east coast
        test_calendarTimeShortDisplayString12hr(tzEast, utcMillis, "1:58", amPm[1]);
        test_calendarTimeShortDisplayString12hr(tzAz, utcMillis, "10:58", amPm[0]);
    }

    @Test
    public void test_calendarTimeShortDisplayString_24hr()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, WidgetSettings.TimeFormatMode.MODE_24HR);
        SuntimesUtils.initDisplayStrings(mockContext);

        long utcMillis = 1493315892762L;                          // april 27
        TimeZone tzAz = TimeZone.getTimeZone("US/Arizona");       // 10:58 in arizona
        TimeZone tzEast = TimeZone.getTimeZone("US/Eastern");     // 13:58 on east coast
        test_calendarTimeShortDisplayString24hr(tzEast, utcMillis, "13:58");
        test_calendarTimeShortDisplayString24hr(tzAz, utcMillis, "10:58");
    }

    protected SuntimesUtils.TimeDisplayText test_calendarTimeShortDisplayString12hr(TimeZone tz, long utcMillis, String expected, String expectedSuffix)
    {
        SuntimesUtils.TimeDisplayText text = test_calendarTimeShortDisplayString(tz, utcMillis, expected);
        assertTrue("suffix should be " + expectedSuffix + ", but was " + text.getSuffix(), text.getSuffix().equals(expectedSuffix));
        return text;
    }

    protected SuntimesUtils.TimeDisplayText test_calendarTimeShortDisplayString24hr(TimeZone tz, long utcMillis, String expected)
    {
        SuntimesUtils.TimeDisplayText text = test_calendarTimeShortDisplayString(tz, utcMillis, expected);
        assertTrue("suffix should be empty but was " + text.getSuffix(), text.getSuffix().isEmpty());
        return text;
    }

    protected SuntimesUtils.TimeDisplayText test_calendarTimeShortDisplayString(TimeZone tz, long utcMillis, String expected)
    {
        Calendar time = new GregorianCalendar(tz);
        time.setTimeInMillis(utcMillis);
        SuntimesUtils.TimeDisplayText text = utils.calendarTimeShortDisplayString(mockContext, time);
        assertTrue("raw value should be " + utcMillis + ", but was " + text.getRawValue(), text.getRawValue() == utcMillis);
        assertTrue("value should be " + expected + ", but was " + text.getValue(), text.getValue().equals(expected));
        assertTrue("units should be empty but was " + text.getUnits(), text.getUnits().isEmpty());
        return text;
    }


    @Test
    public void test_calendarDateTimeDisplayString_12hr()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, WidgetSettings.TimeFormatMode.MODE_12HR);
        SuntimesUtils.initDisplayStrings(mockContext);

        Calendar date0 = new GregorianCalendar(TimeZone.getTimeZone("US/Arizona"));
        Calendar date1 = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
        date0.setTimeInMillis(1493315892762L);   // april 27, 10:58 AM (arizona)
        date1.setTimeInMillis(1493315892762L);   // april 27, 1:58 PM (eastern)

        test_calendarDateTimeDisplayString(date0, "April 27, 2017, 10:58:12\u00A0AM", true, true, true);
        test_calendarDateTimeDisplayString(date1, "April 27, 2017, 1:58:12\u00A0PM", true, true, true);

        test_calendarDateTimeDisplayString(date0, "April 27, 2017, 10:58\u00A0AM", true, true, false);
        test_calendarDateTimeDisplayString(date0, "April 27, 2017", true, false, false);
        test_calendarDateTimeDisplayString(date0, "April 27", false, false, false);
        test_calendarDateTimeDisplayString(date0, "April 27, 10:58\u00A0AM", false, true, false);
        test_calendarDateTimeDisplayString(date0, "April 27, 10:58:12\u00A0AM", false, true, true);
    }

    @Test
    public void test_calendarDateTimeDisplayString_24hr()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, WidgetSettings.TimeFormatMode.MODE_24HR);
        SuntimesUtils.initDisplayStrings(mockContext);

        Calendar date0 = new GregorianCalendar(TimeZone.getTimeZone("US/Arizona"));
        Calendar date1 = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
        date0.setTimeInMillis(1493315892762L);   // april 27, 10:58 (arizona)
        date1.setTimeInMillis(1493315892762L);   // april 27, 13:58 (eastern)

        test_calendarDateTimeDisplayString(date0, "April 27, 2017, 10:58:12", true, true, true);
        test_calendarDateTimeDisplayString(date1, "April 27, 2017, 13:58:12", true, true, true);

        test_calendarDateTimeDisplayString(date0, "April 27, 2017, 10:58", true, true, false);
        test_calendarDateTimeDisplayString(date0, "April 27, 2017", true, false, false);
        test_calendarDateTimeDisplayString(date0, "April 27", false, false, false);
        test_calendarDateTimeDisplayString(date0, "April 27, 10:58", false, true, false);
        test_calendarDateTimeDisplayString(date0, "April 27, 10:58:12", false, true, true);
    }

    protected SuntimesUtils.TimeDisplayText test_calendarDateTimeDisplayString(Calendar date, String expected, boolean showTime, boolean showSeconds)
    {
        SuntimesUtils.TimeDisplayText text = utils.calendarDateTimeDisplayString(mockContext, date, showTime, showSeconds);
        assertTrue("result should be " + expected + " but was " + text.toString(), text.toString().equals(expected));
        assertTrue(text.getRawValue() == date.getTimeInMillis());
        assertTrue(text.getSuffix().isEmpty());
        return text;
    }

    protected SuntimesUtils.TimeDisplayText test_calendarDateTimeDisplayString(Calendar date, String expected, boolean showYear, boolean showTime, boolean showSeconds)
    {
        SuntimesUtils.TimeDisplayText text = utils.calendarDateTimeDisplayString(mockContext, date, showYear, showTime, showSeconds);
        assertTrue("result should be " + expected + " but was " + text.toString(), text.toString().equals(expected));
        assertTrue(text.getRawValue() == date.getTimeInMillis());
        assertTrue(text.getSuffix().isEmpty());
        return text;
    }


    @Test
    public void test_timeDeltaDisplayString()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        Date date1 = Calendar.getInstance().getTime();

        SuntimesUtils.TimeDisplayText text1 = utils.timeDeltaDisplayString(date1, null);
        assertTrue("result should be empty (null date), but was " + text1.toString(), text1.toString().isEmpty());

        SuntimesUtils.TimeDisplayText text2 = utils.timeDeltaDisplayString(null, date1);
        assertTrue("result should be empty (null date), but was " + text2.toString(), text2.toString().isEmpty());

        SuntimesUtils.TimeDisplayText text3 = utils.timeDeltaDisplayString(null, null);
        assertTrue("result should be empty (null date), but was " + text3.toString(), text3.toString().isEmpty());

        test_timeDeltaDisplayString(date1, 0, "1m");
        test_timeDeltaDisplayString(date1, 1 * MINUTE, "1m");
        test_timeDeltaDisplayString(date1, 2 * MINUTE, "2m");
        test_timeDeltaDisplayString(date1, 59 * MINUTE, "59m");
        test_timeDeltaDisplayString(date1, 60 * MINUTE, "1h");
        test_timeDeltaDisplayString(date1, 61 * MINUTE, "1h\u00A01m");
        test_timeDeltaDisplayString(date1, 1439 * MINUTE, "23h\u00A059m");
        test_timeDeltaDisplayString(date1, 1440 * MINUTE, "1d");
        test_timeDeltaDisplayString(date1, 1500 * MINUTE, "1d\u00A01h");

        test_timeDeltaDisplayString(date1, 59 * MINUTE, "59m", false, false);          // still 59m
        test_timeDeltaDisplayString(date1, 61 * MINUTE, "1h\u00A01m", false, false);   // still 1h 1m
        test_timeDeltaDisplayString(date1, 1500 * MINUTE, "1d\u00A01h", false, false);      // <2d so.. 1d 1h
        test_timeDeltaDisplayString(date1, 2820 * MINUTE, "1d\u00A023h", false, false);     // <2d so.. 1d 23h
        test_timeDeltaDisplayString(date1, 2940 * MINUTE, "2d", false, false);         // >=2d so.. 2d (not 2d 1h)

        test_timeDeltaDisplayString(date1, 7 * 1500 * MINUTE, "7d", false, false);
        test_timeDeltaDisplayString(date1, 7 * 1500 * MINUTE, "7d\u00A07h");
        test_timeDeltaDisplayString(date1, 7 * 1500 * MINUTE, "7d\u00A07h", false, true);
        test_timeDeltaDisplayString(date1, 7 * 1500 * MINUTE, "1w", true, false);
        test_timeDeltaDisplayString(date1, 7 * 1500 * MINUTE, "1w", true, true);            // 1w (not 1w 7h)

        test_timeDeltaDisplayString(date1, 15 * DAY + 15 * HOUR, "15d", false, false);
        test_timeDeltaDisplayString(date1, 15 * DAY + 15 * HOUR, "15d\u00A015h", false, true);
        test_timeDeltaDisplayString(date1, 16 * DAY + 16 * HOUR, "2w\u00A02d", true, false);  // not 2w 2d 16h (showHours ignored w/ weeks)
        test_timeDeltaDisplayString(date1, 15 * DAY + 15 * HOUR, "2w\u00A01d", true, true);   // not 2w 1d 15h (showHours ignored w/ weeks)
    }

    private static final int MINUTE = 60 * 1000;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;

    protected SuntimesUtils.TimeDisplayText test_timeDeltaDisplayString(Date date, long timeDelta, String expected)
    {
        SuntimesUtils.TimeDisplayText text = utils.timeDeltaDisplayString(date, new Date(date.getTime() + timeDelta));
        assertTrue("result should be " + expected + " but was " + text.toString(), text.toString().equals(expected));
        assertTrue(text.getRawValue() == timeDelta);
        assertTrue(text.getSuffix().isEmpty());
        return text;
    }
    protected SuntimesUtils.TimeDisplayText test_timeDeltaDisplayString(Date date, long timeDelta, String expected, boolean showWeeks, boolean showHours)
    {
        SuntimesUtils.TimeDisplayText text = utils.timeDeltaDisplayString(date, new Date(date.getTime() + timeDelta), showWeeks, showHours);
        assertTrue("result should be " + expected + " but was " + text.toString(), text.toString().equals(expected));
        assertTrue(text.getRawValue() == timeDelta);
        assertTrue(text.getSuffix().isEmpty());
        return text;
    }

    @Test
    public void test_timeDeltaLongDisplayString()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        long date1 = Calendar.getInstance().getTimeInMillis();

        test_timeDeltaLongDisplayString(date1,0, "1m shorter");
        test_timeDeltaLongDisplayString(date1,-1000 * 30, "30s shorter", true);
        test_timeDeltaLongDisplayString(date1,1000 * 30, "30s longer", true);
        test_timeDeltaLongDisplayString(date1,1000 * 30, "1m longer", false);
        test_timeDeltaLongDisplayString(date1,1 * MINUTE, "1m longer");
        test_timeDeltaLongDisplayString(date1,2 * MINUTE, "2m longer");
        test_timeDeltaLongDisplayString(date1,59 * MINUTE, "59m longer");
        test_timeDeltaLongDisplayString(date1,60 * MINUTE, "1h longer");
        test_timeDeltaLongDisplayString(date1,61 * MINUTE, "1h\u00A01m longer");
        test_timeDeltaLongDisplayString(date1,1439 * MINUTE, "23h\u00A059m longer");
        test_timeDeltaLongDisplayString(date1,1440 * MINUTE, "1d longer");
        test_timeDeltaLongDisplayString(date1,1500 * MINUTE, "1d\u00A01h longer");
        test_timeDeltaLongDisplayString(date1,30 * MINUTE + MINUTE, "31m longer", true);
        test_timeDeltaLongDisplayString(date1,30 * MINUTE + 59 * 1000, "30m\u00A059s longer", true);
        test_timeDeltaLongDisplayString(date1,660 * MINUTE + 55 * 1000, "11h\u00A055s longer", true);
    }

    protected SuntimesUtils.TimeDisplayText test_timeDeltaLongDisplayString(long date, long timeDelta, String expected)
    {
        return test_timeDeltaLongDisplayString(date, timeDelta, expected, false);
    }
    protected SuntimesUtils.TimeDisplayText test_timeDeltaLongDisplayString(long date, long timeDelta, String expected, boolean showSeconds)
    {
        SuntimesUtils.TimeDisplayText text = utils.timeDeltaLongDisplayString(date, date + timeDelta, showSeconds);
        assertTrue("result should be " + expected + ", but was " + text.toString(), text.toString().equals(expected));

        if (timeDelta <= 0)
            assertTrue(text.getSuffix().equals("shorter"));
        else assertTrue(text.getSuffix().equals("longer"));

        assertTrue(text.getRawValue() == timeDelta);
        return text;
    }

    @Test
    public void testImageSpanTag()
    {
        String tag = "[tag]";
        ImageSpan imageSpan = new ImageSpan(warningDrawable);
        SuntimesUtils.ImageSpanTag spanTag = new SuntimesUtils.ImageSpanTag(tag, imageSpan);
        assertTrue("getTag() must return the same tag set by the constructor!", spanTag.getTag().equals(tag));
        assertTrue("getSpan() must return the same span set by the constructor!", spanTag.getSpan().equals(imageSpan) );
        assertTrue("getBlank() must return a string that is the same length as the tag!", spanTag.getBlank().length() == tag.length());
        assertTrue("getBlank() must return a blank string!", spanTag.getBlank().trim().isEmpty());

        SuntimesUtils.ImageSpanTag spanTag2 = new SuntimesUtils.ImageSpanTag(tag, null);
        assertTrue("getSpan() should return null (as set by constructor)", spanTag2.getSpan() == null);
    }

    @Test
    public void testCreateImageSpan()
    {
        ImageSpan span1 = SuntimesUtils.createImageSpan(null);
        assertTrue("createImageSpan(null) must not return null!", span1 != null);

        ImageSpan span2 = SuntimesUtils.createImageSpan(mockContext, -1, warningSize, warningSize, warningColor);
        assertTrue("createImageSpan(context, invalidDrawableId, w, h, tint) must not return null!", span2 != null);

        ImageSpan span3 = SuntimesUtils.createImageSpan(mockContext, warningDrawableID, warningSize, warningSize, warningColor);
        assertTrue("createImageSpan(context, validDrawableId, w, h, tint) must not return null!", span3 != null);

        Drawable drawable3 = span3.getDrawable();
        assertTrue("drawable must not be null", drawable3 != null);
        assertTrue("createImageSpan(context, drawableID, w, h, tint) should set width to w!", drawable3.getBounds().width() == warningSize);
        assertTrue("createImageSpan(context, drawableID, w, h, tint) should set height to h!", drawable3.getBounds().height() == warningSize);

        ImageSpan span4 = SuntimesUtils.createImageSpan(span3);
        assertTrue("createImageSpan(ImageSpan) should create a new object!", span4 != span3);
        assertTrue("createImageSpan(ImageSpan) should have the same drawable!", span4.getDrawable().equals(span3.getDrawable()));
    }

}
