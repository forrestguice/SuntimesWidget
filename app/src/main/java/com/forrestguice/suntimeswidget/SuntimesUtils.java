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

import android.text.Spannable;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.TypedValue;

import java.text.DateFormat;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SuntimesUtils
{
    public static final String SPANTAG_WARNING = "[w]";

    private static String strTimeShorter = "shorter";
    private static String strTimeLonger = "longer";
    private static String strSpace = " ";
    private static String strEmpty = "";
    private static String strYears = "y";
    private static String strDays = "d";
    private static String strHours = "h";
    private static String strMinutes = "m";
    private static String strSeconds = "s";
    private static String strTimeDeltaFormat = "%1$s" + strEmpty + "%2$s";
    private static String strTimeVeryShortFormat = "h:mm";
    private static String strTimeSuffixFormat = "a";
    private static String strTimeNone = "none";
    private static String strTimeLoading = "...";

    public SuntimesUtils() {}

    public static void initDisplayStrings( Context context )
    {
        strTimeShorter = context.getString(R.string.delta_day_shorter);
        strTimeLonger = context.getString(R.string.delta_day_longer);
        strYears = context.getString(R.string.delta_years);
        strDays = context.getString(R.string.delta_days);
        strHours = context.getString(R.string.delta_hours);
        strMinutes = context.getString(R.string.delta_minutes);
        strSeconds = context.getString(R.string.delta_seconds);
        strTimeDeltaFormat = context.getString(R.string.delta_format);
        strTimeVeryShortFormat = context.getString(R.string.time_format_12hr_veryshort);
        strTimeNone = context.getString(R.string.time_none);
        strTimeLoading = context.getString(R.string.time_loading);
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

        public void setRawValue( long value )
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
        public void setSuffix( String suffix )
        {
            this.suffix = suffix;
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            s.append(value);

            if (!units.isEmpty())
            {
                s.append(" ");
                s.append(units);
            }

            if (!suffix.isEmpty())
            {
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

            final TimeDisplayText other = (TimeDisplayText)obj;

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
     * @param context
     * @param cal
     * @return
     */
    public TimeDisplayText calendarTimeShortDisplayString(Context context, Calendar cal)
    {
        if (cal == null)
        {
            return new TimeDisplayText(strTimeNone);
        }

        Date time = cal.getTime();
        TimeDisplayText retValue;

        boolean is24 = android.text.format.DateFormat.is24HourFormat(context);
        if (is24)
        {
            // most locales seem to use 24 hour time
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            timeFormat.setTimeZone(cal.getTimeZone());
            retValue = new TimeDisplayText(timeFormat.format(time), "", "");

        } else {
            // other locales use (or optionally allow) 12 hr time;
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

            SimpleDateFormat timeFormat = new SimpleDateFormat(strTimeVeryShortFormat, locale); // h:mm
            timeFormat.setTimeZone(cal.getTimeZone());

            SimpleDateFormat suffixFormat = new SimpleDateFormat(strTimeSuffixFormat, locale);  // a
            suffixFormat.setTimeZone(cal.getTimeZone());

            retValue = new TimeDisplayText( timeFormat.format(time), "", suffixFormat.format(time) );
        }

        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    /**
     * @param c1
     * @param c2
     * @return
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
     * @return a TimeDisplayText object that describes difference between the two spans
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
        long numberOfYears = numberOfDays / 365;

        long remainingDays = numberOfDays % 365;
        long remainingHours = numberOfHours % 24;
        long remainingMinutes = numberOfMinutes % 60;
        long remainingSeconds = numberOfSeconds % 60;

        boolean showingYears = (numberOfYears > 0);
        if (showingYears)
            value += String.format(strTimeDeltaFormat, numberOfYears, strYears);

        boolean showingDays = (remainingDays > 0);
        if (showingDays)
            value += (showingYears ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingDays, strDays);

        boolean showingHours = (remainingHours > 0);
        if (showingHours)
            value += (showingYears || showingDays ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingHours, strHours);

        boolean showingMinutes = (remainingMinutes > 0);
        if (showingMinutes)
            value += (showingYears || showingDays || showingHours ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingMinutes, strMinutes);

        boolean showingSeconds = (showSeconds && !showingHours && !showingDays && !showingYears && (remainingSeconds > 0));
        if (showingSeconds)
            value += (showingMinutes ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingSeconds, strSeconds);

        if (!showingSeconds && !showingMinutes && !showingHours && !showingDays && !showingYears)
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

    /**
     * @param text a pre-formatted datestring
     * @param warningSpan an ImageSpan to be substituted in place of all [w] tags (or null to remove tag).
     * @return a SpannableStringBuilder with tags replaced with appropriate ImageSpans
     */
    public static SpannableStringBuilder createSpan(String text, ImageSpan warningSpan)
    {
        SpannableStringBuilder dateSpan = new SpannableStringBuilder(text);
        int tagPos_warning = text.indexOf(SPANTAG_WARNING);
        if (tagPos_warning >= 0)
        {
            if (warningSpan != null)
            {
                dateSpan.setSpan(warningSpan, tagPos_warning, tagPos_warning + SPANTAG_WARNING.length(), ImageSpan.ALIGN_BASELINE);
            } else {
                dateSpan.replace(tagPos_warning, tagPos_warning + SPANTAG_WARNING.length(), new SpannableString(""));
            }
        }
        return dateSpan;
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
        return createWarningSpan(context, (int)Math.ceil(height));
    }
    public static ImageSpan createWarningSpan(Context context, int width, int height)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionWarning});
        int drawableID = a.getResourceId(0, R.drawable.ic_action_warning);
        a.recycle();
        int warningTint = context.getResources().getColor(R.color.warning);
        return createImageSpan(context, drawableID, width, height, warningTint);
    }
    public static ImageSpan createErrorSpan(Context context, int width, int height)
    {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.icActionError});
        int drawableID = a.getResourceId(0, R.drawable.ic_action_error);
        a.recycle();
        int errorTint = context.getResources().getColor(R.color.error);
        return createImageSpan(context, drawableID, width, height, errorTint);
    }
    public static ImageSpan createImageSpan(Context context, int drawableID, int width, int height, int tint)
    {
        Drawable drawable = context.getResources().getDrawable(drawableID);
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

}
