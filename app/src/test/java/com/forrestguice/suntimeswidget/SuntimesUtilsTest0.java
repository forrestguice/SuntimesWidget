/**
    Copyright (C) 2017-2020 Forrest Guice
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

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import com.forrestguice.suntimeswidget.settings.AppSettings;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * non-instrumented tests moved from androidTest/SuntimesUtilsTest
 */
public class SuntimesUtilsTest0
{
    private SuntimesUtils utils;

    @Before
    public void setup() {
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

        test_timeDeltaLongDisplayString(date1,0, "1m the same");
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

        if (timeDelta == 0)
            assertTrue(text.getSuffix().equals("the same"));
        else if (timeDelta < 0)
            assertTrue(text.getSuffix().equals("shorter"));
        else assertTrue(text.getSuffix().equals("longer"));

        assertTrue(text.getRawValue() == timeDelta);
        return text;
    }

    @Test
    public void testImageSpanTag()
    {
        String tag = "[tag]";
        ImageSpan imageSpan = new ImageSpan((Drawable)null);
        SuntimesUtils.ImageSpanTag spanTag = new SuntimesUtils.ImageSpanTag(tag, imageSpan);
        assertTrue("getTag() must return the same tag set by the constructor!", spanTag.getTag().equals(tag));
        assertTrue("getSpan() must return the same span set by the constructor!", spanTag.getSpan().equals(imageSpan) );
        assertTrue("getBlank() must return a string that is the same length as the tag!", spanTag.getBlank().length() == tag.length());
        assertTrue("getBlank() must return a blank string!", spanTag.getBlank().trim().isEmpty());

        SuntimesUtils.ImageSpanTag spanTag2 = new SuntimesUtils.ImageSpanTag(tag, null);
        assertTrue("getSpan() should return null (as set by constructor)", spanTag2.getSpan() == null);
    }

}
