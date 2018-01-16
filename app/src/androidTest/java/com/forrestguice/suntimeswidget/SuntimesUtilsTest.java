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

        Calendar timeEast = new GregorianCalendar(tzEast);
        timeEast.setTimeInMillis(utcMillis);

        SuntimesUtils.TimeDisplayText text0 = utils.calendarTimeShortDisplayString(mockContext, timeEast);
        assertTrue("raw value should be " + utcMillis + ", but was " + text0.getRawValue(), text0.getRawValue() == utcMillis);
        assertTrue("value should be 1:58, but was " + text0.getValue(), text0.getValue().equals("1:58"));
        assertTrue("suffix should be " + amPm[1] + ", but was " + text0.getSuffix(), text0.getSuffix().equals(amPm[1]));
        assertTrue("units should be empty but was " + text0.getUnits(), text0.getUnits().isEmpty());

        Calendar timeAz = new GregorianCalendar(tzAz);
        timeAz.setTimeInMillis(utcMillis);

        SuntimesUtils.TimeDisplayText text1 = utils.calendarTimeShortDisplayString(mockContext, timeAz);
        assertTrue("raw value should be " + utcMillis + ", but was " + text1.getRawValue(), text1.getRawValue() == utcMillis);
        assertTrue("value should be 10:58, but was " + text1.getValue(), text1.getValue().equals("10:58"));
        assertTrue("suffix should be " + amPm[0] + ", but was " + text1.getSuffix(), text1.getSuffix().equals(amPm[0]));
        assertTrue("units should be empty but was " + text1.getUnits(), text1.getUnits().isEmpty());
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

        Calendar timeEast = new GregorianCalendar(tzEast);
        timeEast.setTimeInMillis(utcMillis);

        SuntimesUtils.TimeDisplayText text0 = utils.calendarTimeShortDisplayString(mockContext, timeEast);
        assertTrue("raw value should be " + utcMillis + ", but was " + text0.getRawValue(), text0.getRawValue() == utcMillis);
        assertTrue("value should be 13:58, but was " + text0.getValue(), text0.getValue().equals("13:58"));
        assertTrue("suffix should be empty but was " + text0.getSuffix(), text0.getSuffix().isEmpty());
        assertTrue("units should be empty but was " + text0.getUnits(), text0.getUnits().isEmpty());

        Calendar timeAz = new GregorianCalendar(tzAz);
        timeAz.setTimeInMillis(utcMillis);

        SuntimesUtils.TimeDisplayText text1 = utils.calendarTimeShortDisplayString(mockContext, timeAz);
        assertTrue("raw value should be " + utcMillis + ", but was " + text1.getRawValue(), text1.getRawValue() == utcMillis);
        assertTrue("value should be 10:58, but was " + text1.getValue(), text1.getValue().equals("10:58"));
        assertTrue("suffix should be empty but was " + text1.getSuffix(), text1.getSuffix().isEmpty());
        assertTrue("units should be empty but was " + text1.getUnits(), text1.getUnits().isEmpty());
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

        SuntimesUtils.TimeDisplayText text4 = utils.timeDeltaDisplayString(date1, date1);
        assertTrue("result should be 1m (delta of 0), but was " + text4.toString(), text4.toString().equals("1m"));
        assertTrue(text4.getSuffix().isEmpty());

        SuntimesUtils.TimeDisplayText text5 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 1 * 60 * 1000));
        assertTrue("result should be 1m, but was " + text5.toString(), text5.toString().equals("1m"));

        SuntimesUtils.TimeDisplayText text6 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 2 * 60 * 1000));
        assertTrue("result should be 2m, but was " + text6.toString(), text6.toString().equals("2m"));

        SuntimesUtils.TimeDisplayText text7 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 59 * 60 * 1000));
        assertTrue("result should be 59m, but was " + text7.toString(), text7.toString().equals("59m"));

        SuntimesUtils.TimeDisplayText text8 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 60 * 60 * 1000));
        assertTrue("result should be 1h, but was " + text8.toString(), text8.toString().equals("1h"));

        SuntimesUtils.TimeDisplayText text9 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 61 * 60 * 1000));
        assertTrue("result should be 1h 1m, but was " + text9.toString(), text9.toString().equals("1h 1m"));

        SuntimesUtils.TimeDisplayText text10 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 1439 * 60 * 1000));
        assertTrue("result should be 23h 59m, but was " + text10.toString(), text10.toString().equals("23h 59m"));

        SuntimesUtils.TimeDisplayText text11 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 1440 * 60 * 1000));
        assertTrue("result should be 1d, but was " + text11.toString(), text11.toString().equals("1d"));
        assertTrue(text11.getRawValue() == 1440 * 60 * 1000);

        //SuntimesUtils.TimeDisplayText text12 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 1441 * 60 * 1000));
        //assertTrue("result should be 1d 1m, but was " + text12.toString(), text12.toString().equals("1d 1m"));

        SuntimesUtils.TimeDisplayText text13 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 1500 * 60 * 1000));
        assertTrue("result should be 1d 1h, but was " + text13.toString(), text13.toString().equals("1d 1h"));

        //SuntimesUtils.TimeDisplayText text14 = utils.timeDeltaDisplayString(date1, new Date(date1.getTime() + 1501 * 60 * 1000));
        //assertTrue("result should be 1d 1h 1m, but was " + text14.toString(), text14.toString().equals("1d 1h 1m"));
        //assertTrue(text14.getRawValue() == 1501 * 60 * 1000);
    }

    @Test
    public void test_timeDeltaLongDisplayString()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        long date1 = Calendar.getInstance().getTimeInMillis();

        SuntimesUtils.TimeDisplayText text1 = utils.timeDeltaLongDisplayString(date1 + 1000 * 30, date1, true);
        assertTrue("result should be 30s shorter, but was " + text1.toString(), text1.toString().equals("30s shorter"));
        assertTrue(text1.getSuffix().equals("shorter"));
        assertTrue(text1.getRawValue() == -1000 * 30);

        SuntimesUtils.TimeDisplayText text2 = utils.timeDeltaLongDisplayString(date1, date1 + 1000 * 30, true);
        assertTrue("result should be 30s longer, but was " + text2.toString(), text2.toString().equals("30s longer"));
        assertTrue(text2.getSuffix().equals("longer"));
        assertTrue(text2.getRawValue() == 1000 * 30);

        SuntimesUtils.TimeDisplayText text3 = utils.timeDeltaLongDisplayString(date1, date1 + 1000 * 30, false);
        assertTrue("result should be 1m longer, but was " + text3.toString(), text3.toString().equals("1m longer"));
        assertTrue(text3.getRawValue() == 1000 * 30);

        SuntimesUtils.TimeDisplayText text4 = utils.timeDeltaLongDisplayString(date1, date1);
        assertTrue("result should be 1m (delta of 0), but was " + text4.toString(), text4.toString().startsWith("1m"));

        SuntimesUtils.TimeDisplayText text5 = utils.timeDeltaLongDisplayString(date1, date1 + 1 * 60 * 1000);
        assertTrue("result should be 1m longer, but was " + text5.toString(), text5.toString().equals("1m longer"));

        SuntimesUtils.TimeDisplayText text6 = utils.timeDeltaLongDisplayString(date1, date1 + 2 * 60 * 1000);
        assertTrue("result should be 2m longer, but was " + text6.toString(), text6.toString().equals("2m longer"));

        SuntimesUtils.TimeDisplayText text7 = utils.timeDeltaLongDisplayString(date1, date1 + 59 * 60 * 1000);
        assertTrue("result should be 59m longer, but was " + text7.toString(), text7.toString().equals("59m longer"));

        SuntimesUtils.TimeDisplayText text8 = utils.timeDeltaLongDisplayString(date1, date1 + 60 * 60 * 1000);
        assertTrue("result should be 1h longer, but was " + text8.toString(), text8.toString().equals("1h longer"));

        SuntimesUtils.TimeDisplayText text9 = utils.timeDeltaLongDisplayString(date1, date1 + 61 * 60 * 1000);
        assertTrue("result should be 1h 1m longer, but was " + text9.toString(), text9.toString().equals("1h 1m longer"));

        SuntimesUtils.TimeDisplayText text10 = utils.timeDeltaLongDisplayString(date1, date1 + 1439 * 60 * 1000);
        assertTrue("result should be 23h 59m longer, but was " + text10.toString(), text10.toString().equals("23h 59m longer"));

        SuntimesUtils.TimeDisplayText text11 = utils.timeDeltaLongDisplayString(date1, date1 + 1440 * 60 * 1000);
        assertTrue("result should be 1d longer, but was " + text11.toString(), text11.toString().equals("1d longer"));

        //SuntimesUtils.TimeDisplayText text12 = utils.timeDeltaLongDisplayString(date1, date1 + 1441 * 60 * 1000);
        //assertTrue("result should be 1d 1m longer, but was " + text12.toString(), text12.toString().equals("1d 1m longer"));

        SuntimesUtils.TimeDisplayText text13 = utils.timeDeltaLongDisplayString(date1, date1 + 1500 * 60 * 1000);
        assertTrue("result should be 1d 1h longer, but was " + text13.toString(), text13.toString().equals("1d 1h longer"));

        //SuntimesUtils.TimeDisplayText text14 = utils.timeDeltaLongDisplayString(date1, date1 + 1501 * 60 * 1000);
        //assertTrue("result should be 1d 1h 1m longer, but was " + text14.toString(), text14.toString().equals("1d 1h 1m longer"));
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
