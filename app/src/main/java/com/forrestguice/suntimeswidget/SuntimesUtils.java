/**
    Copyright (C) 2014 Forrest Guice
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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.text.Spannable;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;

import java.lang.reflect.Method;
import java.text.DateFormat;

import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings.TimeFormatMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SuntimesUtils
{
    public static final String SPANTAG_DST = "[d]";
    public static final String SPANTAG_WARNING = "[w]";

    public static final int DEF_WARNING_DRAWABLE = R.drawable.ic_action_warning;
    public static final int DEF_ERROR_DRAWABLE = R.drawable.ic_action_error;
    public static final int DEF_DST_DRAWABLE = R.drawable.ic_weather_sunny;

    private static String strTimeShorter = "shorter";
    private static String strTimeLonger = "longer";
    private static String strSpace = " ";
    private static String strEmpty = "";
    private static String strYears = "y";
    private static String strWeeks = "w";
    private static String strDays = "d";
    private static String strHours = "h";
    private static String strMinutes = "m";
    private static String strSeconds = "s";
    private static String strTimeDeltaFormat = "%1$s" + strEmpty + "%2$s";
    private static String strTimeShortFormat12 = "h:mm a";
    private static String strTimeVeryShortFormat12 = "h:mm";
    private static String strTimeVeryShortFormat24 = "HH:mm";
    private static String strTimeSuffixFormat = "a";
    private static String strTimeNone = "none";
    private static String strTimeLoading = "...";
    private static boolean is24 = true;

    private static String strDateYearFormat = "yyyy";
    private static String strDateShortFormat = "MMMM d";
    private static String strDateLongFormat = "MMMM d, yyyy";
    private static String strDateTimeShortFormat = "MMMM d, h:mm a";
    private static String strDateTimeLongFormat = "MMMM d, yyyy, h:mm a";

    public SuntimesUtils()
    {
    }

    public static void initDisplayStrings(Context context)
    {
        WidgetSettings.TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(context, 0);
        is24 = (mode == TimeFormatMode.MODE_SYSTEM) ? android.text.format.DateFormat.is24HourFormat(context)
                                                    : (mode == TimeFormatMode.MODE_24HR);

        strTimeShorter = context.getString(R.string.delta_day_shorter);
        strTimeLonger = context.getString(R.string.delta_day_longer);
        strYears = context.getString(R.string.delta_years);
        strWeeks = context.getString(R.string.delta_weeks);
        strDays = context.getString(R.string.delta_days);
        strHours = context.getString(R.string.delta_hours);
        strMinutes = context.getString(R.string.delta_minutes);
        strSeconds = context.getString(R.string.delta_seconds);

        strTimeDeltaFormat = context.getString(R.string.delta_format);
        strTimeVeryShortFormat12 = context.getString(R.string.time_format_12hr_veryshort);
        strTimeVeryShortFormat24 = context.getString(R.string.time_format_24hr_veryshort);
        strTimeNone = context.getString(R.string.time_none);
        strTimeLoading = context.getString(R.string.time_loading);

        strTimeShortFormat12 = context.getString(R.string.time_format_12hr_short, strTimeVeryShortFormat12, strTimeSuffixFormat);
        String timeFormat = (is24 ? strTimeVeryShortFormat24 : strTimeShortFormat12);
        strDateTimeShortFormat = context.getString(R.string.datetime_format_short, strDateShortFormat, timeFormat);
        strDateTimeLongFormat = context.getString(R.string.datetime_format_long, strDateLongFormat, timeFormat);

        strDateYearFormat = context.getString(R.string.dateyear_format_short);
        strDateShortFormat = context.getString(R.string.date_format_short);
        strDateLongFormat = context.getString(R.string.date_format_long);
    }

    /**
     * TimeDisplayText : class
     */
    public static class TimeDisplayText
    {
        private long rawValue = 0;
        private String value;
        private String units;
        private String suffix;

        public TimeDisplayText()
        {
            this.value = "";
            this.units = "";
            this.suffix = "";
        }

        public TimeDisplayText(String value)
        {
            this.value = value;
            this.units = "";
            this.suffix = "";
        }

        public TimeDisplayText(String value, String units, String suffix)
        {
            this.value = value;
            this.units = units;
            this.suffix = suffix;
        }

        public void setRawValue(long value)
        {
            rawValue = value;
        }

        public long getRawValue()
        {
            return rawValue;
        }

        public String getValue()
        {
            return value;
        }

        public String getUnits()
        {
            return units;
        }

        public String getSuffix()
        {
            return suffix;
        }

        public void setSuffix(String suffix)
        {
            this.suffix = suffix;
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            s.append(value);

            boolean valueNotEmpty = !value.isEmpty();
            boolean unitsNotEmpty = !units.isEmpty();

            if (unitsNotEmpty)
            {
                if (valueNotEmpty)
                    s.append(" ");
                s.append(units);
            }

            if (!suffix.isEmpty())
            {
                if (valueNotEmpty || unitsNotEmpty)
                    s.append(" ");
                s.append(suffix);
            }

            return s.toString();
        }


        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || !TimeDisplayText.class.isAssignableFrom(obj.getClass()))
                return false;

            final TimeDisplayText other = (TimeDisplayText) obj;

            if (!value.equals(other.getValue()))
                return false;

            if (!units.equals(other.getUnits()))
                return false;

            //noinspection RedundantIfStatement
            if (!suffix.equals(other.getSuffix()))
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = this.value.hashCode();
            hash = hash * 37 + units.hashCode();
            hash = hash * 37 + suffix.hashCode();
            return hash;
        }
    }

    /**
     * @param context a context used to access time/date settings
     * @param cal     a Calendar representing some point in time
     * @return a display string that describes the time (short format)
     */
    public TimeDisplayText calendarTimeShortDisplayString(Context context, Calendar cal)
    {
        if (cal == null)
        {
            return new TimeDisplayText(strTimeNone);

        } else
        {
            return (is24 ? calendarTime24HrDisplayString(context, cal)
                    : calendarTime12HrDisplayString(context, cal));
        }
    }

    /**
     * formats a time display string (lets the system determine the exact format).
     *
     * @param context a context
     * @param cal     a Calendar representing some point in time
     * @return a time display string (short format)
     */
    public TimeDisplayText calendarTimeSysDisplayString(Context context, @NonNull Calendar cal)
    {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        timeFormat.setTimeZone(cal.getTimeZone());
        TimeDisplayText retValue = new TimeDisplayText(timeFormat.format(cal.getTime()), "", "");
        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    /**
     * formats a 24 hr time display string
     *
     * @param context a context
     * @param cal     a Calendar representing some point in time
     * @return a time display string (12 hr) (short format)
     */
    public TimeDisplayText calendarTime24HrDisplayString(Context context, @NonNull Calendar cal)
    {
        TimeDisplayText retValue = new TimeDisplayText(calendarTime24HrString(context, cal), "", "");
        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    public String calendarTime24HrString(Context context, @NonNull Calendar cal)
    {
        Locale locale = Resources.getSystem().getConfiguration().locale;
        SimpleDateFormat timeFormat = new SimpleDateFormat(strTimeVeryShortFormat24, locale); // HH:mm
        timeFormat.setTimeZone(cal.getTimeZone());
        return timeFormat.format(cal.getTime());
    }

    /**
     * formats a 12hr time display string
     * @param context a context
     * @param cal a Calendar representing some point in time
     * @return a time display string (24 hr) (short format)
     */
    public TimeDisplayText calendarTime12HrDisplayString(Context context, @NonNull Calendar cal)
    {
        // some locales use (or optionally allow) 12 hr time;
        //
        // `getTimeFormat` produces a localized timestring but we want the time part (6:47)
        // separate from the suffix (AM/PM) in order to let the layout define the presentation.
        //
        // a. The ICU4j `getPatternInstance` method seems to be the ideal solution (using the
        // HOURS_MINUTES pattern), but is a recent addition to android (api 24).
        //
        // b. Using toLocalizedPattern on an existing SimpleDateFormat
        // may be another solution, but leaves the problem of separating the time from the suffix
        // in a consistent way for all locales.
        //
        // c. Java 8 may introduce methods that address this, but the project currently compiles
        // using older versions of java (and it would suck to break that).
        //
        // d. A third party lib might address this, which could be added if its source is available
        // and easily included in the build from official maven repos.
        //
        // For now the work around is to define a "veryShortFormat" in strings.xml for those locales
        // that use something other than the usual "h:mm" pattern. A better solution would get this
        // from the system somehow without requiring additional translation.

        // a variety 12 hour time formats from around the world...
        //
        //   english (us):       6:47 AM        11:46 PM           (en)
        //   afrikaans:          6:47 vm.       11:46 nm.
        //   isiZulu:            6:47 Ekuseni   11:46 Ntambama
        //   bahasa (melayu):    6:47 PG        11:46 PTG
        //   bahasa (indonesia): 6.47 AM        11.46 PM           (in)
        //   dansk               6.47 AM        11.46 PM           (da)
        //   norsk bokmal        6.47 a.m.      11.46 p.m.         (nb)

        Locale locale = Resources.getSystem().getConfiguration().locale;

        SimpleDateFormat timeFormat = new SimpleDateFormat(strTimeVeryShortFormat12, locale); // h:mm
        timeFormat.setTimeZone(cal.getTimeZone());

        SimpleDateFormat suffixFormat = new SimpleDateFormat(strTimeSuffixFormat, locale);  // a
        suffixFormat.setTimeZone(cal.getTimeZone());

        Date time = cal.getTime();
        TimeDisplayText retValue = new TimeDisplayText(timeFormat.format(time), "", suffixFormat.format(time));
        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    public String calendarTime12HrString(Context context, @NonNull Calendar cal)
    {
        Locale locale = Resources.getSystem().getConfiguration().locale;
        SimpleDateFormat timeFormat = new SimpleDateFormat(strTimeShortFormat12, locale); // h:mm a
        timeFormat.setTimeZone(cal.getTimeZone());
        return timeFormat.format(cal.getTime());
    }


    /**
     * @param context
     * @param cal
     * @return
     */
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal)
    {
        Calendar now = Calendar.getInstance();
        return calendarDateTimeDisplayString(context, cal, (cal != null && (cal.get(Calendar.YEAR) != now.get(Calendar.YEAR))));
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal, boolean showYear)
    {
        if (cal == null || context == null)
        {
            return new TimeDisplayText(strTimeNone);
        }

        Locale locale = Resources.getSystem().getConfiguration().locale;
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat((showYear ? strDateTimeLongFormat : strDateTimeShortFormat), locale);
        dateTimeFormat.setTimeZone(cal.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dateTimeFormat.format(cal.getTime()), "", "");
        displayText.setRawValue(cal.getTimeInMillis());
        return displayText;

        // doesn't use the appropriate timezone (always formats to system)
        /**int formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;
        formatFlags = (showYear ? (formatFlags | DateUtils.FORMAT_SHOW_YEAR) : (formatFlags | DateUtils.FORMAT_NO_YEAR));
        return new TimeDisplayText(DateUtils.formatDateRange(context, cal.getTimeInMillis(), cal.getTimeInMillis(), formatFlags), "", "");*/

        // doesn't use app's 12hr/24hr setting, doesn't work w/ custom TimeZone objs
        /**Long time = cal.getTimeInMillis();
        String tzID = cal.getTimeZone().getID();
        Formatter formatter = new Formatter(new StringBuilder(50), Locale.getDefault());
        formatter = DateUtils.formatDateRange(context, formatter, time, time, formatFlags, tzID);
        return new TimeDisplayText(formatter.toString(), "", "");*/

        // doesn't use app's 12hr/24hr setting
        /** DateFormat timeFormat = android.text.format.DateFormat.getLongDateFormat(context);
        String value = timeFormat.format(cal.getTime());*/
    }

    /**
     * @param context
     * @param cal
     * @return
     */
    public TimeDisplayText calendarDateYearDisplayString(Context context, Calendar cal)
    {
        if (cal == null)
        {
            return new TimeDisplayText(strTimeNone);
        }
        Locale locale = Resources.getSystem().getConfiguration().locale;
        SimpleDateFormat dateFormat = new SimpleDateFormat(strDateYearFormat, locale);
        return new TimeDisplayText(dateFormat.format(cal.getTime()), "", "");
    }

    /**
     * @param c1 a Calendar representing some point in time
     * @param c2 another Calendar representing another point in time
     * @return a display string that describes the span between the two calendars
     */
    public TimeDisplayText timeDeltaDisplayString(Date c1, Date c2)
    {
        if (c1 != null && c2 != null)
        {
            TimeDisplayText displayText = timeDeltaLongDisplayString(c1.getTime(), c2.getTime());
            displayText.setSuffix("");
            return displayText;

        } else {
            TimeDisplayText displayText = new TimeDisplayText();
            displayText.setSuffix("");
            return displayText;
        }
    }

    /**
     * @param timeSpan1 first event
     * @param timeSpan2 second event
     * @return a display string that describes difference between the two spans
     */
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2)
    {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, false);
    }

    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showSeconds)
    {
        String value = strSpace;
        String units = strEmpty;
        String suffix = strEmpty;

        long timeSpan = timeSpan2 - timeSpan1;
        GregorianCalendar d = new GregorianCalendar();
        d.setTimeInMillis(timeSpan);
        long timeInMillis = d.getTimeInMillis();

        long numberOfSeconds = timeInMillis / 1000;
        suffix += ((numberOfSeconds > 0) ? strTimeLonger : strTimeShorter);
        numberOfSeconds = Math.abs(numberOfSeconds);

        long numberOfMinutes = numberOfSeconds / 60;
        long numberOfHours = numberOfMinutes / 60;
        long numberOfDays = numberOfHours / 24;
        long numberOfWeeks = numberOfDays / 7;
        long numberOfYears = numberOfDays / 365;

        long remainingWeeks = (long)(numberOfWeeks % 52.1429);
        long remainingDays = numberOfDays % 7;
        //long remainingDays = numberOfDays % 365;
        long remainingHours = numberOfHours % 24;
        long remainingMinutes = numberOfMinutes % 60;
        long remainingSeconds = numberOfSeconds % 60;

        boolean showingYears = (numberOfYears > 0);
        if (showingYears)
            value += String.format(strTimeDeltaFormat, numberOfYears, strYears);

        boolean showingWeeks = (numberOfWeeks > 0);
        if (showingWeeks)
            value += (showingYears ? strSpace : strEmpty) +
                    String.format(strTimeDeltaFormat, remainingWeeks, strWeeks);

        boolean showingDays = (remainingDays > 0);
        if (showingDays)
            value += (showingYears || showingWeeks ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingDays, strDays);

        boolean showingHours = (!showingYears && !showingWeeks && remainingHours > 0);
        if (showingHours)
            value += (showingYears || showingWeeks || showingDays ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingHours, strHours);

        boolean showingMinutes = (!showingDays && !showingWeeks && !showingYears && remainingMinutes > 0);
        if (showingMinutes)
            value += (showingYears || showingWeeks || showingDays || showingHours ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingMinutes, strMinutes);

        boolean showingSeconds = (showSeconds && !showingHours && !showingDays && !showingWeeks && !showingYears && (remainingSeconds > 0));
        if (showingSeconds)
            value += (showingMinutes ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingSeconds, strSeconds);

        if (!showingSeconds && !showingMinutes && !showingHours && !showingDays && !showingWeeks && !showingYears)
            value += String.format(strTimeDeltaFormat, "1", strMinutes);

        TimeDisplayText text = new TimeDisplayText(value.trim(), units, suffix);
        text.setRawValue(timeSpan);
        return text;
    }

    /**
     * Creates a title string from a given "title pattern".
     *
     * The following substitutions are supported:
     *   %% .. the % character
     *   %m .. the time mode (short version; e.g. civil)
     *   %M .. the time mode (long version; e.g. civil twilight)
     *   %t .. the timezoneID (e.g. US/Arizona)
     *   %loc .. the location (label/name)
     *   %lat .. the location (latitude)
     *   %lon .. the location (longitude)
     *   %s .. the data source
     *
     * @param titlePattern a pattern string (simple substitutions)
     * @return a display string suitable for display as a widget title
     */
    public String displayStringForTitlePattern(String titlePattern, SuntimesRiseSetData data)
    {
        String modePattern = "%M";
        String modePatternShort = "%m";
        String locPattern = "%loc";
        String latPattern = "%lat";
        String lonPattern = "%lon";
        String timezoneIDPattern = "%t";
        String datasourcePattern = "%s";
        String percentPattern = "%%";

        WidgetSettings.TimeMode timeMode = data.timeMode();
        WidgetSettings.Location location = data.location();
        String timezoneID = data.timezone().getID();
        String datasource = (data.calculatorMode() == null) ? "" : data.calculatorMode().name();

        String displayString = titlePattern;
        displayString = displayString.replaceAll(modePatternShort, timeMode.getShortDisplayString());
        displayString = displayString.replaceAll(modePattern, timeMode.getLongDisplayString());
        displayString = displayString.replaceAll(locPattern, location.getLabel());
        displayString = displayString.replaceAll(latPattern, location.getLatitude());
        displayString = displayString.replaceAll(lonPattern, location.getLongitude());
        displayString = displayString.replaceAll(timezoneIDPattern, timezoneID);
        displayString = displayString.replaceAll(datasourcePattern, datasource);
        displayString = displayString.replaceAll(percentPattern, "%");

        return displayString;
    }

    /**
     *
     * @param titlePattern
     * @param data
     * @return
     */
    public String displayStringForTitlePattern(String titlePattern, SuntimesEquinoxSolsticeData data)
    {
        String modePattern = "%M";
        String modePatternShort = "%m";
        String locPattern = "%loc";
        String latPattern = "%lat";
        String lonPattern = "%lon";
        String timezoneIDPattern = "%t";
        String datasourcePattern = "%s";
        String percentPattern = "%%";

        WidgetSettings.SolsticeEquinoxMode timeMode = data.timeMode();
        WidgetSettings.Location location = data.location();
        String timezoneID = data.timezone().getID();
        String datasource = data.calculatorMode().getDisplayString();

        String displayString = titlePattern;
        displayString = displayString.replaceAll(modePatternShort, timeMode.getShortDisplayString());
        displayString = displayString.replaceAll(modePattern, timeMode.getLongDisplayString());
        displayString = displayString.replaceAll(locPattern, location.getLabel());
        displayString = displayString.replaceAll(latPattern, location.getLatitude());
        displayString = displayString.replaceAll(lonPattern, location.getLongitude());
        displayString = displayString.replaceAll(timezoneIDPattern, timezoneID);
        displayString = displayString.replaceAll(datasourcePattern, datasource);
        displayString = displayString.replaceAll(percentPattern, "%");

        return displayString;
    }

    public static SpannableStringBuilder createSpan(Context context, String text, String spanTag, ImageSpan imageSpan)
    {
        ImageSpanTag[] tags = { new ImageSpanTag(spanTag, imageSpan) };
        return createSpan(context, text, tags);
    }

    public static SpannableStringBuilder createSpan(Context context, String text, ImageSpanTag[] tags)
    {
        SpannableStringBuilder span = new SpannableStringBuilder(text);
        ImageSpan blank = createImageSpan(context, R.drawable.ic_transparent, 0, 0, R.color.transparent);

        for (ImageSpanTag tag : tags)
        {
            String spanTag = tag.getTag();
            ImageSpan imageSpan = (tag.getSpan() == null) ? blank : tag.getSpan();

            int tagPos;
            while ((tagPos = text.indexOf(spanTag)) >= 0)
            {
                int tagEnd = tagPos + spanTag.length();
                //Log.d("DEBUG", "tag=" + spanTag + ", tagPos=" + tagPos + ", " + tagEnd + ", text=" + text);

                span.setSpan(createImageSpan(imageSpan), tagPos, tagEnd, ImageSpan.ALIGN_BASELINE);
                text = text.substring(0, tagPos) + tag.getBlank() + text.substring(tagEnd);
            }
        }
        return span;
    }

    public static SpannableString createColorSpan(String text, String toColorize, int color)
    {
        SpannableString span = new SpannableString(text);
        int start = text.indexOf(toColorize);
        int end = start + toColorize.length();
        span.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    public static SpannableString createBoldSpan(String text, String toBold)
    {
        SpannableString span = new SpannableString(text);
        int start = text.indexOf(toBold);
        int end = start + toBold.length();
        span.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    public static SpannableString createBoldColorSpan(String text, String toBold, int color)
    {
        SpannableString span = new SpannableString(text);
        int start = text.indexOf(toBold);
        int end = start + toBold.length();
        span.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    public static SpannableString createBoldColorSpan(String text, String toBold, int color, int pointSizePixels)
    {
        SpannableString span = new SpannableString(text);
        int start = text.indexOf(toBold);
        int end = start + toBold.length();
        span.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new AbsoluteSizeSpan(pointSizePixels), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    public static int spToPixels(Context context, int spValue)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    public static ImageSpan createWarningSpan(Context context, int height)
    {
        //noinspection SuspiciousNameCombination
        return createWarningSpan(context, height, height);
    }

    public static ImageSpan createWarningSpan(Context context, float height)
    {
        return createWarningSpan(context, (int) Math.ceil(height));
    }
    public static ImageSpan createWarningSpan(Context context, int width, int height)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionWarning, R.attr.tagColor_warning});
        int drawableID = a.getResourceId(0, DEF_WARNING_DRAWABLE);
        int colorID = a.getResourceId(1, R.color.warningTag_dark);
        a.recycle();
        return createImageSpan(context, drawableID, width, height, ContextCompat.getColor(context, colorID));
    }

    public static ImageSpan createErrorSpan(Context context, int width, int height)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionError, R.attr.tagColor_error});
        int drawableID = a.getResourceId(0, DEF_ERROR_DRAWABLE);
        int colorID = a.getResourceId(1, R.color.errorTag_dark);
        a.recycle();
        return createImageSpan(context, drawableID, width, height, ContextCompat.getColor(context, colorID));
    }

    public static ImageSpan createDstSpan(Context context, float height)
    {
        return createDstSpan(context, (int) Math.ceil(height), (int) Math.ceil(height));
    }
    public static ImageSpan createDstSpan(Context context, int width, int height)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionDst, R.attr.tagColor_dst});
        int drawableID = a.getResourceId(0, DEF_DST_DRAWABLE);
        int colorID = a.getResourceId(1, R.color.dstTag_dark);
        a.recycle();
        return createImageSpan(context, drawableID, width, height, ContextCompat.getColor(context, colorID));
    }

    public static ImageSpan createImageSpan(Context context, int drawableID, int width, int height, int tint)
    {
        Drawable drawable = null;
        try {
            drawable = context.getResources().getDrawable(drawableID);
        } catch (Exception e) {
            Log.e("createImageSpan", "invalid drawableID " + drawableID + "! ...set to null.");
        }

        if (drawable != null)
        {
            if (width > 0 && height > 0)
            {
                drawable.setBounds(0, 0, width, height);
            }
            drawable.setColorFilter(tint, PorterDuff.Mode.SRC_ATOP);
        }
        return new ImageSpan(drawable);
    }

    public static ImageSpan createImageSpan(ImageSpan other)
    {
        Drawable drawable = null;
        if (other != null)
            drawable = other.getDrawable();

        return new ImageSpan(drawable);
    }

    /**
     * utility class; [Tag, ImageSpan] tuple
     */
    public static class ImageSpanTag
    {
        private String tag;       // the tag, e.g. [w]
        private ImageSpan span;   // an ImageSpan that should be substituted for the tag
        private String blank;     // a "blank" string the same length as the tag

        public ImageSpanTag(String tag, ImageSpan span)
        {
            this.tag = tag;
            this.span = span;
            buildBlankTag();
        }

        private void buildBlankTag()
        {
            blank = "";
            for (int i=0; i<tag.length(); i++)
            {    blank += " ";    }
        }

        public String getTag()
        {
            return tag;
        }

        public ImageSpan getSpan()
        {
            return span;
        }

        public String getBlank()
        {
            return blank;
        }
    }

    /**
     * from http://stackoverflow.com/questions/18374183/how-to-show-icons-in-overflow-menu-in-actionbar
     */
    public static void forceActionBarIcons(Menu menu)
    {
        if (menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);

                } catch (Exception e) {
                    Log.e("SuntimesActivity", "failed to set show overflow icons", e);
                }
            }
        }
    }

    /**
     * @param htmlString html markup
     * @return an html span
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }

    /**
     * Creates a tinted copy of the supplied bitmap.
     * @param b a bitmap image
     * @param color a color
     * @return a bitmap tinted to color
     */
    public static Bitmap tintBitmap(Bitmap b, int color)
    {
        Bitmap tinted = Bitmap.createBitmap(b.getWidth(), b.getHeight(), b.getConfig());
        Canvas c = new Canvas(tinted);
        Paint p = new Paint();
        p.setColorFilter(new LightingColorFilter(color, 0));
        c.drawBitmap(b, 0, 0, p);
        return tinted;
    }

    /**
     * @param context app context
     * @param resourceID drawable resourceID
     * @return a Bitmap representation of the Drawable
     */
    public static Bitmap drawableToBitmap(Context context, int resourceID)
    {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resourceID, null);
        return drawableToBitmap(context, drawable);
    }

    public static Bitmap drawableToBitmap(Context context, int resourceID, boolean isInset, int fillColor)
    {
        return SuntimesUtils.drawableToBitmap(context, resourceID, isInset, fillColor, 0, 0);
    }

    public static Bitmap drawableToBitmap(Context context, int resourceID, boolean isInset, int fillColor, int strokeColor, int strokePixels)
    {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resourceID, null);
        if (isInset)
        {
            InsetDrawable inset = (InsetDrawable)drawable;
            return drawableToBitmap(context, tintDrawable(inset, fillColor, strokeColor, strokePixels));

        } else {
            GradientDrawable gradient = (GradientDrawable)drawable;
            return drawableToBitmap(context, tintDrawable(gradient, fillColor, strokeColor, strokePixels));
        }
    }

    /**
     * @param drawable a ShapeDrawable
     * @param fillColor the fill color
     * @param strokeColor the stroke color
     * @return a GradientDrawable with the given fill and stroke
     */
    public static Drawable tintDrawable(InsetDrawable drawable, int fillColor, int strokeColor, int strokePixels)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            try {
                GradientDrawable gradient = (GradientDrawable)drawable.getDrawable();
                if (gradient != null)
                {
                    SuntimesUtils.tintDrawable(gradient, fillColor, strokeColor, strokePixels);
                    return drawable;

                } else {
                    Log.w("tintDrawable", "failed to apply color! Null inset drawable.");
                    return drawable;
                }
            } catch (ClassCastException e) {
                Log.w("tintDrawable", "failed to apply color! " + e);
                return drawable;
            }
        } else {
            Log.w("tintDrawable", "failed to apply color! InsetDrawable.getDrawable requires api 19+");
            return drawable;   // not supported
        }
    }

    public static Drawable tintDrawable(GradientDrawable drawable, int fillColor, int strokeColor, int strokePixels)
    {
        drawable.setStroke(strokePixels, strokeColor);
        drawable.setColor(fillColor);
        return drawable;
    }

    /**
     * @param context app context
     * @param drawable a Drawable
     * @return a Bitmap representation of the Drawable
     */
    public static Bitmap drawableToBitmap(Context context, Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable)
        {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
