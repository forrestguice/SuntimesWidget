/**
    Copyright (C) 2014-2018 Forrest Guice
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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;

import android.graphics.drawable.LayerDrawable;
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
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.text.DateFormat;

import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings.TimeFormatMode;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
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
    private static String strSpace = "\u00A0";
    private static String strEmpty = "";
    private static String strYears = "y";
    private static String strWeeks = "w";
    private static String strDays = "d";
    private static String strHours = "h";
    private static String strMinutes = "m";
    private static String strSeconds = "s";

    private static String strAltSymbol = "∠";
    private static String strRaSymbol = "α";
    private static String strDecSymbol = "δ";
    private static String strDegreesFormat = "%1$s\u00B0";
    private static String strDirectionFormat = "%1$s %2$s";
    private static String strElevationFormat = "%1$s%2$s";
    private static String strDeclinationFormat = "%1$s %2$s";
    private static String strRaFormat = "%1$s %2$s";

    private static String strTimeDeltaFormat = "%1$s"  + strEmpty + "%2$s";
    private static String strTimeShortFormat12 = "h:mm\u00A0a";
    private static String strTimeShortFormat12s = "h:mm:ss\u00A0a";
    private static String strTimeVeryShortFormat12 = "h:mm";
    private static String strTimeVeryShortFormat24 = "HH:mm";
    private static String strTimeVeryShortFormat12s = "h:mm:ss";
    private static String strTimeVeryShortFormat24s = "HH:mm:ss";
    private static String strTimeSuffixFormat = "a";
    private static String strTimeNone = "none";
    private static String strTimeLoading = "...";
    private static boolean is24 = true;
    private static boolean initialized = false;

    private static String strDateYearFormat = "yyyy";
    private static String strDateShortFormat = "MMMM d";
    private static String strDateLongFormat = "MMMM d, yyyy";
    private static String strDateTimeShortFormat = "MMMM d, h:mm\u00A0a";
    private static String strDateTimeLongFormat = "MMMM d, yyyy, h:mm\u00A0a";
    private static String strDateTimeShortFormatSec = "MMMM d, h:mm:ss\u00A0a";
    private static String strDateTimeLongFormatSec = "MMMM d, yyyy, h:mm:ss\u00A0a";

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

        strAltSymbol = context.getString(R.string.widgetLabel_altitude_symbol);
        strRaSymbol = context.getString(R.string.widgetLabel_rightAscension_symbol);
        strDecSymbol = context.getString(R.string.widgetLabel_declination_symbol);

        strDegreesFormat = context.getString(R.string.degrees_format);
        strDirectionFormat = context.getString(R.string.direction_format);
        strElevationFormat = context.getString(R.string.elevation_format);
        strRaFormat = context.getString(R.string.rightascension_format);
        strDeclinationFormat = context.getString(R.string.declination_format);

        strTimeDeltaFormat = context.getString(R.string.delta_format);
        strTimeVeryShortFormat12 = context.getString(R.string.time_format_12hr_veryshort);
        strTimeVeryShortFormat24 = context.getString(R.string.time_format_24hr_veryshort);
        strTimeVeryShortFormat12s = context.getString(R.string.time_format_12hr_veryshort_withseconds);
        strTimeVeryShortFormat24s = context.getString(R.string.time_format_24hr_veryshort_withseconds);
        strTimeNone = context.getString(R.string.time_none);
        strTimeLoading = context.getString(R.string.time_loading);

        strTimeShortFormat12 = context.getString(R.string.time_format_12hr_short, strTimeVeryShortFormat12, strTimeSuffixFormat);
        String timeFormat = (is24 ? strTimeVeryShortFormat24 : strTimeShortFormat12);
        strDateTimeShortFormat = context.getString(R.string.datetime_format_short, strDateShortFormat, timeFormat);
        strDateTimeLongFormat = context.getString(R.string.datetime_format_long, strDateLongFormat, timeFormat);

        strTimeShortFormat12s = context.getString(R.string.time_format_12hr_short, strTimeVeryShortFormat12s, strTimeSuffixFormat);
        String timeFormatSec = (is24 ? strTimeVeryShortFormat24s : strTimeShortFormat12s);
        strDateTimeShortFormatSec = context.getString(R.string.datetime_format_short, strDateShortFormat, timeFormatSec);
        strDateTimeLongFormatSec = context.getString(R.string.datetime_format_long, strDateLongFormat, timeFormatSec);

        strDateYearFormat = context.getString(R.string.dateyear_format_short);
        strDateShortFormat = context.getString(R.string.date_format_short);
        strDateLongFormat = context.getString(R.string.date_format_long);

        CardinalDirection.initDisplayStrings(context);
        initialized = true;
    }

    public static boolean isInitialized()
    {
        return initialized;
    }

    /**
     * CardinalDirection
     */
    public static enum CardinalDirection
    {
        NORTH(1,      "N",   "North"              , 0.0),
        NORTH_NE(2,   "NNE", "North North East"   , 22.5),
        NORTH_E(3,    "NE",  "North East"         , 45.0),

        EAST_NE(4,    "ENE", "East North East"    , 67.5),
        EAST(5,       "E",   "East"               , 90.0),
        EAST_SE(6,    "ESE", "East South East"    , 112.5),

        SOUTH_E(7,    "SE",  "South East"         , 135.0),
        SOUTH_SE(8,   "SSE", "South South East"   , 157.5),
        SOUTH(9,      "S",   "South"              , 180.0),
        SOUTH_SW(10,  "SSW", "South South West"   , 202.5),
        SOUTH_W(11,   "SW",  "South West"         , 225.0),

        WEST_SW(12,   "WSW", "West South West"    , 247.5),
        WEST(13,      "W",   "West"               , 270.0),
        WEST_NW(14,   "WNW", "West North West"    , 292.5),

        NORTH_W(15,   "NW",  "North West"         , 315.0),
        NORTH_NW(16,  "NNW", "North North West"   , 337.5),
        NORTH2(1,     "N",   "North"              , 360.0);

        private int pointNum;
        private String shortDisplayString;
        private String longDisplayString;
        private double degrees;

        private CardinalDirection(int pointNum, String shortDisplayString, String longDisplayString, double degrees)
        {
            this.pointNum = pointNum;
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
            this.degrees = degrees;
        }

        public static CardinalDirection getDirection(double degrees)
        {
            if (degrees > 360)
                degrees = degrees % 360;

            while (degrees < 0)
                degrees += 360;

            CardinalDirection result = NORTH;
            double least = Double.MAX_VALUE;
            for (CardinalDirection direction : values())
            {
                double directionDegrees = direction.getDegress();
                double diff = Math.abs(directionDegrees - degrees);
                if (diff < least)
                {
                    least = diff;
                    result = direction;
                }
            }
            return result;
        }

        public String toString()
        {
            return shortDisplayString;
        }

        public double getDegress()
        {
            return degrees;
        }

        public int getPoint()
        {
            return pointNum;
        }

        public String getShortDisplayString()
        {
            return shortDisplayString;
        }

        public String getLongDisplayString()
        {
            return longDisplayString;
        }

        public void setDisplayStrings(String shortDisplayString, String longDisplayString)
        {
            this.shortDisplayString = shortDisplayString;
            this.longDisplayString = longDisplayString;
        }

        public static void initDisplayStrings( Context context )
        {
            String[] modes_short = context.getResources().getStringArray(R.array.directions_short);
            String[] modes_long = context.getResources().getStringArray(R.array.directions_long);
            if (modes_long.length != modes_short.length)
            {
                Log.e("initDisplayStrings", "The size of directions_short and solarevents_long DOES NOT MATCH!");
                return;
            }

            CardinalDirection[] values = values();
            if (modes_long.length != values.length)
            {
                Log.e("initDisplayStrings", "The size of directions_long and SolarEvents DOES NOT MATCH!");
                return;
            }

            for (int i = 0; i < values.length; i++)
            {
                values[i].setDisplayStrings(modes_short[i], modes_long[i]);
            }
        }
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

    public static Locale getLocale()
    {
        return Locale.getDefault();
    }

    /**
     * @param context a context used to access time/date settings
     * @param cal     a Calendar representing some point in time
     * @return a display string that describes the time (short format)
     */
    public TimeDisplayText calendarTimeShortDisplayString(Context context, Calendar cal)
    {
        return calendarTimeShortDisplayString(context, cal, false);
    }
    public TimeDisplayText calendarTimeShortDisplayString(Context context, Calendar cal, boolean showSeconds)
    {
        if (!initialized)
        {
            Log.w("SuntimesUtils", "Not initialized! (calendarTimeShortDisplayString was called anyway; using defaults)");
        }

        if (cal == null)
        {
            return new TimeDisplayText(strTimeNone);

        } else {
            return (is24 ? calendarTime24HrDisplayString(context, cal, showSeconds)
                    : calendarTime12HrDisplayString(context, cal, showSeconds));
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
    public TimeDisplayText calendarTime24HrDisplayString(Context context, @NonNull Calendar cal, boolean showSeconds)
    {
        TimeDisplayText retValue = new TimeDisplayText(calendarTime24HrString(context, cal, showSeconds), "", "");
        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    public String calendarTime24HrString(Context context, @NonNull Calendar cal, boolean showSeconds)
    {
        Locale locale = getLocale();
        String format = (showSeconds ? strTimeVeryShortFormat24s : strTimeVeryShortFormat24);  // HH:mm or HH:mm:ss
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, locale);
        timeFormat.setTimeZone(cal.getTimeZone());
        return timeFormat.format(cal.getTime());
    }

    /**
     * formats a 12hr time display string
     * @param context a context
     * @param cal a Calendar representing some point in time
     * @return a time display string (24 hr) (short format)
     */
    public TimeDisplayText calendarTime12HrDisplayString(Context context, @NonNull Calendar cal, boolean showSeconds)
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

        Locale locale = getLocale();

        String format = (showSeconds ? strTimeVeryShortFormat12s : strTimeVeryShortFormat12);  // h:mm or h:mm:ss
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, locale);
        timeFormat.setTimeZone(cal.getTimeZone());

        //Log.d("DEBUG","TimeFormat: " + timeFormat.toPattern() + " (" + locale.toString() + ")");

        SimpleDateFormat suffixFormat = new SimpleDateFormat(strTimeSuffixFormat, locale);  // a
        suffixFormat.setTimeZone(cal.getTimeZone());

        Date time = cal.getTime();
        TimeDisplayText retValue = new TimeDisplayText(timeFormat.format(time), "", suffixFormat.format(time));
        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    public String calendarTime12HrString(Context context, @NonNull Calendar cal)
    {
        Locale locale = getLocale();
        SimpleDateFormat timeFormat = new SimpleDateFormat(strTimeShortFormat12, locale); // h:mm a
        timeFormat.setTimeZone(cal.getTimeZone());
        return timeFormat.format(cal.getTime());
    }

    /**
     * @param context a context
     * @param calendar a Calendar representing some date
     * @param abbreviate true abbreviate name, false full name
     * @return day name e.g. Monday (or Mon abbreviated)
     */
    public TimeDisplayText calendarDayDisplayString(Context context, Calendar calendar, boolean abbreviate)
    {
        if (calendar == null || context == null)
        {
            return new TimeDisplayText(strTimeNone);
        }

        Locale locale = getLocale();
        SimpleDateFormat dayFormat = new SimpleDateFormat((abbreviate ? "E" : "EEEE"), locale);

        dayFormat.setTimeZone(calendar.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dayFormat.format(calendar.getTime()), "", "");
        displayText.setRawValue(calendar.getTimeInMillis());
        return displayText;
    }

    /**
     * @param context a context
     * @param calendar  a Calendar representing some date
     * @return a time display string
     */
    public TimeDisplayText calendarDateDisplayString(Context context, Calendar calendar)
    {
        return calendarDateDisplayString(context, calendar, false);
    }
    public TimeDisplayText calendarDateDisplayString(Context context, Calendar calendar, boolean showYear)
    {
        if (calendar == null || context == null)
        {
            return new TimeDisplayText(strTimeNone);
        }

        Locale locale = getLocale();
        SimpleDateFormat dateFormat;

        if (showYear)
            dateFormat = new SimpleDateFormat(strDateLongFormat, locale);
        else dateFormat = new SimpleDateFormat(strDateShortFormat, locale);

        dateFormat.setTimeZone(calendar.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dateFormat.format(calendar.getTime()), "", "");
        displayText.setRawValue(calendar.getTimeInMillis());
        return displayText;
    }

    /**
     * @param context a context
     * @param cal a Calendar representing some date + time
     * @return a time display string
     */
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal)
    {
        Calendar now = Calendar.getInstance();
        return calendarDateTimeDisplayString(context, cal, (cal != null && (cal.get(Calendar.YEAR) != now.get(Calendar.YEAR))), true, false);
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, long timestamp)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return calendarDateTimeDisplayString(context, cal, true, true);
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal, boolean showTime, boolean showSeconds)
    {
        Calendar now = Calendar.getInstance();
        return calendarDateTimeDisplayString(context, cal, (cal != null && (cal.get(Calendar.YEAR) != now.get(Calendar.YEAR))), showTime, showSeconds);
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal, boolean showYear, boolean showTime, boolean showSeconds)
    {
        if (cal == null || context == null)
        {
            return new TimeDisplayText(strTimeNone);
        }

        Locale locale = getLocale();
        SimpleDateFormat dateTimeFormat;
        if (showTime) {
            if (showSeconds)
                dateTimeFormat = new SimpleDateFormat((showYear ? strDateTimeLongFormatSec : strDateTimeShortFormatSec), locale);
            else dateTimeFormat = new SimpleDateFormat((showYear ? strDateTimeLongFormat : strDateTimeShortFormat), locale);
        } else dateTimeFormat = new SimpleDateFormat((showYear ? strDateLongFormat : strDateShortFormat), locale);
        //Log.d("DEBUG","DateTimeFormat: " + dateTimeFormat.toPattern() + " (" + locale.toString() + ")");

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
     * @param context a context
     * @param cal a Calendar representing some year
     * @return a time display string
     */
    public TimeDisplayText calendarDateYearDisplayString(Context context, Calendar cal)
    {
        if (cal == null)
        {
            return new TimeDisplayText(strTimeNone);
        }
        Locale locale = getLocale();
        SimpleDateFormat dateFormat = new SimpleDateFormat(strDateYearFormat, locale);
        //Log.d("DEBUG", "Year Format: " + dateFormat.toPattern() + " (" + locale.toString() + ")");
        return new TimeDisplayText(dateFormat.format(cal.getTime()), "", "");
    }

    /**
     * @param c1 a Calendar representing some point in time
     * @param c2 another Calendar representing another point in time
     * @return a display string that describes the span between the two calendars
     */
    public TimeDisplayText timeDeltaDisplayString(Date c1, Date c2)
    {
        return timeDeltaDisplayString(c1, c2, false, true);
    }
    public TimeDisplayText timeDeltaDisplayString(Date c1, Date c2, boolean showWeeks, boolean showHours)
    {
        if (c1 != null && c2 != null)
        {
            TimeDisplayText displayText = timeDeltaLongDisplayString(c1.getTime(), c2.getTime(), showWeeks, showHours,false);
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
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, false, true, false);
    }
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showSeconds)
    {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, false, true, showSeconds);
    }

    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan, boolean showSeconds)
    {
        TimeDisplayText text = timeDeltaLongDisplayString(0, timeSpan, showSeconds);
        text.setSuffix("");
        return text;
    }

    @SuppressWarnings("ConstantConditions")
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showWeeks, boolean showHours, boolean showSeconds)
    {
        String value = strEmpty;
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
        long remainingDays = (showWeeks ? (numberOfDays % 7) : (numberOfDays % 365));
        long remainingHours = numberOfHours % 24;
        long remainingMinutes = numberOfMinutes % 60;
        long remainingSeconds = numberOfSeconds % 60;

        boolean showingYears = (numberOfYears > 0);
        if (showingYears)
            value += String.format(strTimeDeltaFormat, numberOfYears, strYears);

        boolean showingWeeks = (showWeeks && numberOfWeeks > 0);
        if (showingWeeks)
            value += (showingYears ? strSpace : strEmpty) +
                    String.format(strTimeDeltaFormat, remainingWeeks, strWeeks);

        boolean showingDays = (remainingDays > 0);
        if (showingDays)
            value += (showingYears || showingWeeks ? strSpace : strEmpty) +
                     String.format(strTimeDeltaFormat, remainingDays, strDays);

        boolean showingHours = (!showingYears && !showingWeeks && remainingHours > 0);
        boolean showingMinutes = (!showingDays && !showingWeeks && !showingYears && remainingMinutes > 0);
        boolean showingSeconds = (showSeconds && !showingDays && !showingWeeks && !showingYears && (remainingSeconds > 0));

        if (showHours || !showingYears && !showingWeeks && remainingDays < 2)
        {
            if (showingHours)
                value += (showingYears || showingWeeks || showingDays ? strSpace : strEmpty) +
                        String.format(strTimeDeltaFormat, remainingHours, strHours);

            if (showingMinutes)
                value += (showingYears || showingWeeks || showingDays || showingHours ? strSpace : strEmpty) +
                        String.format(strTimeDeltaFormat, remainingMinutes, strMinutes);

            if (showingSeconds)
                value += (showingHours || showingMinutes ? strSpace : strEmpty) +
                        String.format(strTimeDeltaFormat, remainingSeconds, strSeconds);
        }

        if (!showingSeconds && !showingMinutes && !showingHours && !showingDays && !showingWeeks && !showingYears)
        {
            if (showSeconds)
                value += String.format(strTimeDeltaFormat, "0", strSeconds);
            else value += String.format(strTimeDeltaFormat, "1", strMinutes);
        }

        TimeDisplayText text = new TimeDisplayText(value.trim(), units, suffix);
        text.setRawValue(timeSpan);
        return text;
    }

    /**
     * @param value
     * @return
     */
    public String formatAsDegrees(double value)
    {
        return String.format(strDegreesFormat, NumberFormat.getNumberInstance().format(value));
    }
    public String formatAsDegrees(double value, int places)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(places);
        formatter.setMaximumFractionDigits(places);
        return String.format(strDegreesFormat, formatter.format(value));
    }
    public String formatAsDirection(double degreeValue, int places)
    {
        String degreeString = formatAsDegrees(degreeValue, places);
        CardinalDirection direction = CardinalDirection.getDirection(degreeValue);
        return formatAsDirection(degreeString, direction.getShortDisplayString());
    }
    public String formatAsDirection(String degreeString, String directionString)
    {
        return String.format(strDirectionFormat, degreeString, directionString);
    }
    public TimeDisplayText formatAsDirection2(double degreeValue, int places, boolean longSuffix)
    {
        String degreeString = formatAsDegrees(degreeValue, places);
        CardinalDirection direction = CardinalDirection.getDirection(degreeValue);
        return new TimeDisplayText(degreeString, "", (longSuffix ? direction.getLongDisplayString() : direction.getShortDisplayString()));
    }

    public String formatAsElevation(String degreeString, String altitudeSymbol)
    {
        return String.format(strElevationFormat, degreeString, altitudeSymbol);
    }
    public TimeDisplayText formatAsElevation(double degreeValue, int places)
    {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strAltSymbol);
    }

    public String formatAsRightAscension(String degreeString, String raSymbol)
    {
        return String.format(strRaFormat, degreeString, raSymbol);
    }
    public TimeDisplayText formatAsRightAscension(double degreeValue, int places)
    {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strRaSymbol);
    }

    public String formatAsDeclination(String degreeString, String decSymbol)
    {
        return String.format(strDeclinationFormat, degreeString, decSymbol);
    }
    public TimeDisplayText formatAsDeclination(double degreeValue, int places)
    {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strDecSymbol);
    }

    /**
     * Creates a title string from a given "title pattern".
     *
     * The following substitutions are supported:
     *   %% .. the % character
     *   %m .. the mode (short version; e.g. civil, Solstice, Full)
     *   %M .. the mode (long version; e.g. civil twilight, Winter Solstice, Full Moon)
     *   %t .. the timezoneID (e.g. US/Arizona)
     *   %d .. today's date (e.g. February 12)
     *   %dd .. day name (short version; e.g. Mon)
     *   %dD .. day name (long version; e.g. Monday)
     *   %dY .. year (e.g. 2018)
     *   %loc .. the location (label/name)
     *   %lat .. the location (latitude)
     *   %lon .. the location (longitude)
     *   %s .. the data source
     *   %i .. moon illumination (SuntimesMoonData only)
     *
     * @param titlePattern a pattern string (simple substitutions)
     * @return a display string suitable for display as a widget title
     */
    public String displayStringForTitlePattern(Context context, String titlePattern, SuntimesRiseSetData data)
    {
        String displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData)data);
        String modePattern = "%M";
        String modePatternShort = "%m";
        WidgetSettings.TimeMode timeMode = data.timeMode();
        displayString = displayString.replaceAll(modePatternShort, timeMode.getShortDisplayString());
        displayString = displayString.replaceAll(modePattern, timeMode.getLongDisplayString());
        return displayString;
    }

    public String displayStringForTitlePattern(Context context, String titlePattern, SuntimesMoonData data)
    {
        String displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData)data);
        if (data != null && data.isCalculated())
        {
            String modePattern = "%M";
            String modePatternShort = "%m";
            String illumPattern = "%i";

            displayString = displayString.replaceAll(modePatternShort, data.getMoonPhaseToday().getShortDisplayString());
            displayString = displayString.replaceAll(modePattern, data.getMoonPhaseToday().getLongDisplayString());

            if (displayString.contains(illumPattern)) {
                NumberFormat percentage = NumberFormat.getPercentInstance();
                displayString = displayString.replaceAll(illumPattern, percentage.format(data.getMoonIlluminationToday()));
            }
        }
        return displayString;
    }

    public String displayStringForTitlePattern(Context context, String titlePattern, SuntimesEquinoxSolsticeData data)
    {
        String displayString = displayStringForTitlePattern(context, titlePattern, (SuntimesData)data);
        String modePattern = "%M";
        String modePatternShort = "%m";
        WidgetSettings.SolsticeEquinoxMode timeMode = data.timeMode();
        displayString = displayString.replaceAll(modePatternShort, timeMode.getShortDisplayString());
        displayString = displayString.replaceAll(modePattern, timeMode.getLongDisplayString());
        return displayString;
    }

    public String displayStringForTitlePattern(Context context, String titlePattern, SuntimesRiseSetDataset dataset)
    {
        // TODO
        return displayStringForTitlePattern(context, titlePattern, dataset.dataActual);
    }

    public String displayStringForTitlePattern(Context context, String titlePattern, SuntimesData data)
    {
        String locPattern = "%loc";
        String latPattern = "%lat";
        String lonPattern = "%lon";
        String timezoneIDPattern = "%t";
        String datasourcePattern = "%s";
        String datePattern = "%d";
        String dateYearPattern = "%dY";
        String dateDayPattern = "%dD";
        String dateDayPatternShort = "%dd";
        String dateTimePattern = "%dT";
        String dateTimePatternShort = "%dt";
        String widgetIDPattern = "%id";
        String percentPattern = "%%";

        WidgetSettings.Location location = data.location();
        String timezoneID = data.timezone().getID();
        String datasource = (data.calculatorMode() == null) ? "" : data.calculatorMode().name();
        String appWidgetID = (data.appWidgetID() != null ? String.format("%s", data.appWidgetID()) : "");

        String displayString = titlePattern;
        displayString = displayString.replaceAll(locPattern, location.getLabel());
        displayString = displayString.replaceAll(latPattern, location.getLatitude());
        displayString = displayString.replaceAll(lonPattern, location.getLongitude());
        displayString = displayString.replaceAll(timezoneIDPattern, timezoneID);
        displayString = displayString.replaceAll(datasourcePattern, datasource);
        displayString = displayString.replaceAll(widgetIDPattern, appWidgetID);

        if (displayString.contains(datePattern))
        {
            displayString = displayString.replaceAll(dateTimePatternShort, calendarTimeShortDisplayString(context, data.now(), false).toString());
            displayString = displayString.replaceAll(dateTimePattern, calendarTimeShortDisplayString(context, data.now(), true).toString());
            displayString = displayString.replaceAll(dateDayPatternShort, calendarDayDisplayString(context, data.calendar(), true).toString());
            displayString = displayString.replaceAll(dateDayPattern, calendarDayDisplayString(context, data.calendar(), false).toString());
            displayString = displayString.replaceAll(dateYearPattern, calendarDateYearDisplayString(context, data.calendar()).toString());
            displayString = displayString.replaceAll(datePattern, calendarDateDisplayString(context, data.calendar(), false).toString());
        }

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

    public static SpannableString createColorSpan(SpannableString span, String text, String toColorize, int color)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toColorize);
        if (start >= 0)
        {
            int end = start + toColorize.length();
            span.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }
    public static SpannableString createColorSpan(SpannableString span, String text, String toColorize, int color, boolean bold)
    {
        if (bold) {
            span = createBoldSpan(span, text, toColorize);
        }
        return createColorSpan(span, text, toColorize, color);
    }

    public static SpannableString createBoldSpan(SpannableString span, String text, String toBold)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toBold);
        if (start >= 0)
        {
            int end = start + toBold.length();
            span.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createBoldColorSpan(SpannableString span, String text, String toBold, int color)
    {
        return createColorSpan(createBoldSpan(span, text, toBold), text, toBold, color);
    }

    public static SpannableString createRelativeSpan(SpannableString span, String text, String toRelative, float relativeSize)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toRelative);
        if (start >= 0)
        {
            int end = start + toRelative.length();
            span.setSpan(new RelativeSizeSpan(relativeSize), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createAbsoluteSpan(SpannableString span, String text, String toAbsolute, int pointSizePixels)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toAbsolute);
        if (start >= 0)
        {
            int end = start + toAbsolute.length();
            span.setSpan(new AbsoluteSizeSpan(pointSizePixels), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static int spToPixels(Context context, float spValue)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    public static int dpToPixels(Context context, float dpValue)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
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
            drawable.mutate();    // don't cache state (or setColorFilter modifies all instances)
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
            {   //noinspection StringConcatenationInLoop
                blank += " ";
            }
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
     * @param view the ImageView
     * @param color color to apply
     */
    public static void colorizeImageView(ImageView view, int color)
    {
        if (view != null && view.getBackground() != null)
        {
            try {
                GradientDrawable d = (GradientDrawable) view.getBackground().mutate();
                d.setColor(color);
                d.invalidateSelf();

            } catch (ClassCastException e) {
                Log.w("colorizeImageView", "failed to colorize! " + e);
            }
        }
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

    public static Bitmap drawableToBitmap(Context context, int resourceID, int w, int h, boolean pxValues)
    {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resourceID, null);
        return drawableToBitmap(context, drawable, w, h, pxValues);
    }

    /**
     * @param context context used to get resources
     * @param resourceID drawable resource ID to a GradientDrawable
     * @param fillColor fill color to apply to drawable
     * @param strokeColor stroke color to apply to drawable
     * @param strokePx width of stroke (pixels)
     * @return a Bitmap of the drawable
     */
    public static Bitmap gradientDrawableToBitmap(Context context, int resourceID, int fillColor, int strokeColor, int strokePx)
    {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resourceID, null);
        GradientDrawable gradient = (GradientDrawable)drawable;

        int w = 1, h = 1;
        if (gradient != null)
        {
            w = gradient.getIntrinsicWidth();
            h = gradient.getIntrinsicHeight();
        }

        Drawable tinted =  tintDrawable(gradient, fillColor, strokeColor, strokePx);
        return drawableToBitmap(context, tinted, w, h, true);
    }

    /**
     * @param context context used to get resources
     * @param resourceID drawable resource ID to an InsetDrawable
     * @param fillColor fill color to apply to drawable
     * @param strokeColor stroke color to apply to drawable
     * @param strokePx width of stroke (pixels)
     * @return a Bitmap of the drawable
     * @deprecated all insetDrawables were replaced with layerDrawables (2/26/2018), continued use of this method probably signifies a bug.
     */
    @Deprecated
    public static Bitmap insetDrawableToBitmap(Context context, int resourceID, int fillColor, int strokeColor, int strokePx)
    {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resourceID, null);
        InsetDrawable inset = (InsetDrawable)drawable;

        int w = 1, h = 1;
        if (inset != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                Drawable wrapped = inset.getDrawable();
                if (wrapped != null)
                {
                    w = wrapped.getIntrinsicWidth();
                    h = wrapped.getIntrinsicHeight();
                }
            } else {
                w = inset.getIntrinsicWidth();
                h = inset.getIntrinsicHeight();
            }
        }

        Drawable tinted = tintDrawable(inset, fillColor, strokeColor, strokePx);
        return drawableToBitmap(context, tinted, w, h, true);
    }

    /**
     * @param context context used to get resources
     * @param resourceID drawable resourceID to a LayerDrawable
     * @param fillColor fill color to apply to drawable
     * @param strokeColor stroke color to apply to drawable
     * @param strokePx width of stroke (pixels)
     * @return a Bitmap of the drawable
     */
    public static Bitmap layerDrawableToBitmap(Context context, int resourceID, int fillColor, int strokeColor, int strokePx)
    {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resourceID, null);
        LayerDrawable layers = (LayerDrawable)drawable;

        int w = 1, h = 1;
        if (layers != null)
        {
            Drawable layer0 = layers.getDrawable(0);
            w = layers.getIntrinsicWidth();
            h = (layer0 != null ? layer0.getIntrinsicHeight() : layers.getIntrinsicHeight());
        }

        Drawable tinted = tintDrawable(layers, fillColor, strokeColor, strokePx);
        return drawableToBitmap(context, tinted, w, h, true);
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

    public static Drawable tintDrawable(LayerDrawable drawable, int fillColor, int strokeColor, int strokePixels)
    {
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        //{
            try {
                GradientDrawable gradient = (GradientDrawable)drawable.getDrawable(0);
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
        //} else {
            //Log.w("tintDrawable", "failed to apply color! InsetDrawable.getDrawable requires api 19+");
            //return drawable;   // not supported
        //}
    }

    /**
     * @param context context used to access resources
     * @param drawable a Drawable
     * @param w width (pixels or dp)
     * @param h height (pixels or dp)
     * @param pxValues true w and h are in pixels, false w and h are in dp
     * @return a Bitmap measuring w,h of the specified drawable
     */
    public static Bitmap drawableToBitmap(Context context, Drawable drawable, int w, int h, boolean pxValues)
    {
        if (drawable instanceof BitmapDrawable)
        {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        if (!pxValues)
        {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            w = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, w, metrics);
            h = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h, metrics);
        }
        //Log.d("DEBUG", "drawableToBitmap: " + drawable.toString() + "::" + w + ", " + h);

        if (w <= 0 || h <= 0)
        {
            Log.w("drawableToBitmap", "invalid width or height: " + w + ", " + h);
            w = h = 1;
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * @param view the View to trigger the accessibility event
     * @param msg text that will be read aloud (if accessibility enabled)
     */
    public static void announceForAccessibility(View view, String msg)
    {
        if (view != null && msg != null)
        {
            if (Build.VERSION.SDK_INT >= 16)
            {
                view.announceForAccessibility(msg);

            } else {
                Context context = view.getContext();
                if (context != null)
                {
                    AccessibilityManager accesibility = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
                    if (accesibility != null && accesibility.isEnabled())
                    {
                        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                        event.getText().add(msg);
                        event.setEnabled(view.isEnabled());
                        event.setClassName(view.getClass().getName());
                        event.setPackageName(context.getPackageName());

                        ViewParent parent = view.getParent();
                        if (Build.VERSION.SDK_INT >= 14 && parent != null)
                        {
                            parent.requestSendAccessibilityEvent(view, event);

                        } else {
                            accesibility.sendAccessibilityEvent(event);
                        }
                    }
                }
            }
        }
    }

}
