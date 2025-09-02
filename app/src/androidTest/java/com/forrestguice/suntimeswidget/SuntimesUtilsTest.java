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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;
import android.text.style.ImageSpan;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.util.text.TimeDisplayText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    public void test_calendarTimeShortDisplayString()
    {
        TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(mockContext, 0);
        test_calendarTimeShortDisplayString_12hr();
        test_calendarTimeShortDisplayString_24hr();
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, mode);
    }

    @Test
    public void test_calendarTimeShortDisplayString_12hr()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, TimeFormatMode.MODE_12HR);
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
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, TimeFormatMode.MODE_24HR);
        SuntimesUtils.initDisplayStrings(mockContext);

        long utcMillis = 1493315892762L;                          // april 27
        TimeZone tzAz = TimeZone.getTimeZone("US/Arizona");       // 10:58 in arizona
        TimeZone tzEast = TimeZone.getTimeZone("US/Eastern");     // 13:58 on east coast
        test_calendarTimeShortDisplayString24hr(tzEast, utcMillis, "13:58");
        test_calendarTimeShortDisplayString24hr(tzAz, utcMillis, "10:58");
    }

    protected TimeDisplayText test_calendarTimeShortDisplayString12hr(TimeZone tz, long utcMillis, String expected, String expectedSuffix)
    {
        TimeDisplayText text = test_calendarTimeShortDisplayString(tz, utcMillis, expected);
        assertTrue("suffix should be " + expectedSuffix + ", but was " + text.getSuffix(), text.getSuffix().equals(expectedSuffix));
        return text;
    }

    protected TimeDisplayText test_calendarTimeShortDisplayString24hr(TimeZone tz, long utcMillis, String expected)
    {
        TimeDisplayText text = test_calendarTimeShortDisplayString(tz, utcMillis, expected);
        assertTrue("suffix should be empty but was " + text.getSuffix(), text.getSuffix().isEmpty());
        return text;
    }

    protected TimeDisplayText test_calendarTimeShortDisplayString(TimeZone tz, long utcMillis, String expected)
    {
        Calendar time = new GregorianCalendar(tz);
        time.setTimeInMillis(utcMillis);
        TimeDisplayText text = utils.calendarTimeShortDisplayString(mockContext, time);
        assertTrue("raw value should be " + utcMillis + ", but was " + text.getRawValue(), text.getRawValue() == utcMillis);
        assertTrue("value should be " + expected + ", but was " + text.getValue(), text.getValue().equals(expected));
        assertTrue("units should be empty but was " + text.getUnits(), text.getUnits().isEmpty());
        return text;
    }

    @Test
    public void test_calendarDateTimeDisplayString_12hr()
    {
        assertTrue("test precondition: english language", AppSettings.getLocale().getLanguage().equals("en"));
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, TimeFormatMode.MODE_12HR);
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
        WidgetSettings.saveTimeFormatModePref(mockContext, 0, TimeFormatMode.MODE_24HR);
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

    protected TimeDisplayText test_calendarDateTimeDisplayString(Calendar date, String expected, boolean showTime, boolean showSeconds)
    {
        TimeDisplayText text = utils.calendarDateTimeDisplayString(mockContext, date, showTime, showSeconds);
        assertTrue("result should be " + expected + " but was " + text.toString(), text.toString().equals(expected));
        assertTrue(text.getRawValue() == date.getTimeInMillis());
        assertTrue(text.getSuffix().isEmpty());
        return text;
    }

    protected TimeDisplayText test_calendarDateTimeDisplayString(Calendar date, String expected, boolean showYear, boolean showTime, boolean showSeconds)
    {
        TimeDisplayText text = utils.calendarDateTimeDisplayString(mockContext, date, showYear, showTime, showSeconds, false);
        assertTrue("result should be " + expected + " but was " + text.toString(), text.toString().equals(expected));
        assertTrue(text.getRawValue() == date.getTimeInMillis());
        assertTrue(text.getSuffix().isEmpty());
        return text;
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

    @Test
    public void test_initDisplayStrings_executionTime()
    {
        double bench_millis = 0, threshold_millis = 2.0;
        double bench_fast = Double.POSITIVE_INFINITY, bench_slow = 0;
        long bench_start = 0, bench_end = 0;
        int n = 100;
        for (int i = 0; i < n; i++)
        {
            bench_start = System.nanoTime();
            SuntimesUtils.initDisplayStrings(mockContext);
            bench_end = System.nanoTime();
            double bench_millis0 = ((bench_end - bench_start) / 1000000.0);
            //Log.d("SuntimesUtilsTest", "SuntimesUtils.initDisplayStrings in " + bench_millis0);
            if (bench_millis0 < bench_fast) {
                bench_fast = bench_millis0;
            }
            if (bench_millis0 > bench_slow) {
                bench_slow = bench_millis0;
            }
            bench_millis += bench_millis0;
        }
        bench_millis /= ((double)n);
        Log.d("SuntimesUtilsTest", "avg SuntimesUtils.initDisplayStrings in " + bench_millis + ", [" + bench_fast + " .. " + bench_slow + "]");
        assertTrue("initDisplayStrings takes less than " + threshold_millis + " ms", bench_millis < threshold_millis);
    }

    @Test
    public void test_calendarDateTimeDisplayString_12hr_executionTime()
    {
        SuntimesUtils.initDisplayStrings(mockContext);

        long utcMillis = 1493315892762L;                          // april 27
        TimeZone tz = TimeZone.getTimeZone("US/Arizona");
        Calendar now = Calendar.getInstance(tz);
        now.setTimeInMillis(utcMillis);

        TimeDisplayText text;
        double bench_millis = 0, threshold_millis = 2;
        double bench_fast = Double.POSITIVE_INFINITY, bench_slow = 0;
        long bench_start = 0, bench_end = 0;
        int n = 100;
        for (int i = 0; i < n; i++)
        {
            bench_start = System.nanoTime();
            text = utils.calendarTime12HrDisplayString(mockContext, now, false);
            bench_end = System.nanoTime();

            double bench_millis0 = ((bench_end - bench_start) / 1000000.0);
            //Log.d("SuntimesUtilsTest", "SuntimesUtils.calendarDateTimeDisplay in " + bench_millis0);
            if (bench_millis0 < bench_fast) {
                bench_fast = bench_millis0;
            }
            if (bench_millis0 > bench_slow) {
                bench_slow = bench_millis0;
            }
            bench_millis += bench_millis0;
        }
        bench_millis /= ((double)n);
        Log.d("SuntimesUtilsTest", "avg SuntimesUtils.calendarDateTimeDisplay in " + bench_millis + ", [" + bench_fast + " .. " + bench_slow + "]");
        assertTrue("calendarDateTimeDisplay takes less than " + threshold_millis + " ms .. took " + bench_millis, bench_millis < threshold_millis);
    }

    @Test
    public void test_calendarDateTimeDisplayString_24hr_executionTime()
    {
        SuntimesUtils.initDisplayStrings(mockContext);

        long utcMillis = 1493315892762L;                          // april 27
        TimeZone tz = TimeZone.getTimeZone("US/Arizona");
        Calendar now = Calendar.getInstance(tz);
        now.setTimeInMillis(utcMillis);

        String text;
        double bench_millis = 0, threshold_millis = 2;
        double bench_fast = Double.POSITIVE_INFINITY, bench_slow = 0;
        long bench_start = 0, bench_end = 0;
        int n = 100;
        for (int i = 0; i < n; i++)
        {
            bench_start = System.nanoTime();
            text = utils.calendarTime24HrString(mockContext, now, false);
            bench_end = System.nanoTime();

            double bench_millis0 = ((bench_end - bench_start) / 1000000.0);
            Log.d("SuntimesUtilsTest", "SuntimesUtils.initDisplayStrings in " + bench_millis0);
            if (bench_millis0 < bench_fast) {
                bench_fast = bench_millis0;
            }
            if (bench_millis0 > bench_slow) {
                bench_slow = bench_millis0;
            }
            bench_millis += bench_millis0;
        }
        bench_millis /= ((double)n);
        Log.d("SuntimesUtilsTest", "avg SuntimesUtils.calendarDateTimeDisplay in " + bench_millis + ", [" + bench_fast + " .. " + bench_slow + "]");
        assertTrue("calendarDateTimeDisplay takes less than " + threshold_millis + " ms .. took " + bench_millis, bench_millis < threshold_millis);
    }

}
