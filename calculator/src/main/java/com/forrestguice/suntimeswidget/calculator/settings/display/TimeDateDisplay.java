/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.settings.display;

import com.forrestguice.annotation.NonNull;

import com.forrestguice.suntimeswidget.calculator.TimeZones;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.util.Resources;
import com.forrestguice.util.SystemTimeFormat;
import com.forrestguice.util.text.TimeDisplayText;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeDateDisplay
{
    protected static String strTimeShortFormat12 = "h:mm\u00A0a";
    protected static String strTimeShortFormat12s = "h:mm:ss\u00A0a";
    protected static String strTimeVeryShortFormat12 = "h:mm";
    protected static String strTimeVeryShortFormat24 = "HH:mm";
    protected static String strTimeVeryShortFormat12s = "h:mm:ss";
    protected static String strTimeVeryShortFormat24s = "HH:mm:ss";
    protected static String strTimeSuffixFormat = "a";
    protected static String strTimeNone = "none";
    protected static String strTimeLoading = "...";
    protected static boolean is24 = true;

    protected static String strDateYearFormat = "yyyy";
    protected static String strDateVeryShortFormat = "MMM d";
    protected static String strDateShortFormat = "MMMM d";
    protected static String strDateLongFormat = "MMMM d, yyyy";

    protected static boolean initialized = false;

    public static void initDisplayStrings(SuntimesDataSettings context, ResID_TimeDateDisplay r)
    {
        TimeFormatMode mode = context.loadTimeFormatModePref(0);
        is24 = (mode == TimeFormatMode.MODE_SYSTEM || mode == TimeFormatMode.MODE_SUNTIMES) ? SystemTimeFormat.is24HourFormat()
                : (mode == TimeFormatMode.MODE_24HR);

        strTimeVeryShortFormat12 = context.getString(r.string_strTimeVeryShortFormat12());
        strTimeVeryShortFormat24 = context.getString(r.string_strTimeVeryShortFormat24());
        strTimeVeryShortFormat12s = context.getString(r.string_strTimeVeryShortFormat12s());
        strTimeVeryShortFormat24s = context.getString(r.string_strTimeVeryShortFormat24s());
        strTimeNone = context.getString(r.string_strTimeNone());
        strTimeLoading = context.getString(r.string_strTimeLoading());

        strDateYearFormat = context.getString(r.string_strDateYearFormat());
        strDateVeryShortFormat = context.getString(r.string_strDateVeryShortFormat());
        strDateShortFormat = context.getString(r.string_strDateShortFormat());
        strDateLongFormat = context.getString(r.string_strDateLongFormat());

        strTimeShortFormat12 = context.getString(r.string_strTimeShortFormat12(), strTimeVeryShortFormat12, strTimeSuffixFormat);        //String timeFormat = (is24 ? strTimeVeryShortFormat24 : strTimeShortFormat12);
        strTimeShortFormat12s = context.getString(r.string_strTimeShortFormat12(), strTimeVeryShortFormat12s, strTimeSuffixFormat);        //String timeFormatSec = (is24 ? strTimeVeryShortFormat24s : strTimeShortFormat12s);

        initialized = true;
    }

    public static boolean is24() {
        return is24;
    }

    public static Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * @param context a context used to access time/date settings
     * @param cal     a Calendar representing some point in time
     * @return a display string that describes the time (short format)
     */
    public TimeDisplayText calendarTimeShortDisplayString(Resources context, Calendar cal)
    {
        return calendarTimeShortDisplayString(context, cal, false);
    }
    public TimeDisplayText calendarTimeShortDisplayString(Resources context, Calendar cal, boolean showSeconds)
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
    public TimeDisplayText calendarTimeShortDisplayString(Resources context, Calendar cal, boolean showSeconds, TimeFormatMode format)
    {
        if (!initialized) {
            Log.w("SuntimesUtils", "Not initialized! (calendarTimeShortDisplayString was called anyway; using defaults)");
        }

        if (cal == null) {
            return new TimeDisplayText(strTimeNone);

        } else {
            switch (format)
            {
                case MODE_24HR:
                    return calendarTime24HrDisplayString(context, cal, showSeconds);

                case MODE_12HR:
                    return calendarTime12HrDisplayString(context, cal, showSeconds);

                case MODE_SUNTIMES:
                    return (is24 ? calendarTime24HrDisplayString(context, cal, showSeconds)
                            : calendarTime12HrDisplayString(context, cal, showSeconds));

                case MODE_SYSTEM:
                    boolean sysIs24 = SystemTimeFormat.is24HourFormat();
                    return (sysIs24 ? calendarTime24HrDisplayString(context, cal, showSeconds)
                            : calendarTime12HrDisplayString(context, cal, showSeconds));

                default:
                    return new TimeDisplayText(strTimeNone);
            }
        }
    }

    public TimeDisplayText calendarTime24HrDisplayString(Resources context, @NonNull Calendar cal, boolean showSeconds)
    {
        TimeDisplayText retValue = new TimeDisplayText(calendarTime24HrString(context, cal, showSeconds), "", "");
        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    public String calendarTime24HrString(Resources context, @NonNull Calendar cal, boolean showSeconds)
    {
        Date time = cal.getTime();
        applySpecialTimeZone(time, cal.getTimeZone());
        SimpleDateFormat timeFormat = initTimeFormat_24(showSeconds);
        timeFormat.setTimeZone(cal.getTimeZone());
        return timeFormat.format(time);
    }
    private SimpleDateFormat initTimeFormat_24(boolean showSeconds)
    {
        if (showSeconds)
        {
            if (timeFormat_24s == null)
                return (timeFormat_24s = new SimpleDateFormat(strTimeVeryShortFormat24s, getLocale()));
            else return timeFormat_24s;

        } else {
            if (timeFormat_24 == null) {
                return (timeFormat_24 = new SimpleDateFormat(strTimeVeryShortFormat24, getLocale()));
            } else return timeFormat_24;
        }
    }
    private SimpleDateFormat timeFormat_24, timeFormat_24s;

    /**
     * applySpecialTimeZone
     * An opportunity to directly modify the Date before its formatted/displayed; special timezone rules applied here.
     */
    public static void applySpecialTimeZone(@NonNull Date time, @NonNull TimeZone timezone)
    {
        String tzID = timezone.getID();
        if (tzID.equals(TimeZones.SiderealTime.TZID_GMST) || tzID.equals(TimeZones.SiderealTime.TZID_LMST)) {
            time.setTime(TimeZones.SiderealTime.gmstOffset(time.getTime()) + time.getTime());   // these already extend LocalMeanTime (so apply gmst offset only)
        }
    }

    /**
     * formats a 12hr time display string
     * @param context a context
     * @param cal a Calendar representing some point in time
     * @return a time display string (24 hr) (short format)
     */
    public TimeDisplayText calendarTime12HrDisplayString(Resources context, @NonNull Calendar cal, boolean showSeconds)
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

        SimpleDateFormat timeFormat = initTimeFormat_12(showSeconds);
        timeFormat.setTimeZone(cal.getTimeZone());
        timeFormat_12_suffix.setTimeZone(cal.getTimeZone());

        Date time = cal.getTime();
        applySpecialTimeZone(time, cal.getTimeZone());
        TimeDisplayText retValue = new TimeDisplayText(timeFormat.format(time), "", timeFormat_12_suffix.format(time));
        retValue.setRawValue(cal.getTimeInMillis());
        return retValue;
    }

    private SimpleDateFormat initTimeFormat_12(boolean showSeconds)
    {
        if (timeFormat_12_suffix == null) {
            timeFormat_12_suffix = new SimpleDateFormat(strTimeSuffixFormat, getLocale());  // a
        }
        if (showSeconds)
        {
            if (timeFormat_12s == null)
                return (timeFormat_12s = new SimpleDateFormat(strTimeVeryShortFormat12s, getLocale()));
            else return timeFormat_12s;

        } else {
            if (timeFormat_12 == null) {
                return (timeFormat_12 = new SimpleDateFormat(strTimeVeryShortFormat12, getLocale()));
            } else return timeFormat_12;
        }
    }
    private SimpleDateFormat timeFormat_12, timeFormat_12s, timeFormat_12_suffix;

    public String calendarTime12HrString(Resources context, @NonNull Calendar cal)
    {
        Locale locale = getLocale();
        SimpleDateFormat timeFormat = new SimpleDateFormat(strTimeShortFormat12, locale); // h:mm a

        Date time = cal.getTime();
        applySpecialTimeZone(time, cal.getTimeZone());
        timeFormat.setTimeZone(cal.getTimeZone());
        return timeFormat.format(time);
    }

    /**
     * @param context a context
     * @param calendar a Calendar representing some date
     * @param abbreviate true abbreviate name, false full name
     * @return day name e.g. Monday (or Mon abbreviated)
     */
    public TimeDisplayText calendarDayDisplayString(Resources context, Calendar calendar, boolean abbreviate)
    {
        if (calendar == null || context == null) {
            return new TimeDisplayText(strTimeNone);
        }

        Locale locale = getLocale();
        SimpleDateFormat dayFormat = new SimpleDateFormat((abbreviate ? "E" : "EEEE"), locale);

        Date time = calendar.getTime();
        applySpecialTimeZone(time, calendar.getTimeZone());
        dayFormat.setTimeZone(calendar.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dayFormat.format(time), "", "");
        displayText.setRawValue(calendar.getTimeInMillis());
        return displayText;
    }

    /**
     * @param context a context
     * @param cal a Calendar representing some year
     * @return a time display string
     */
    public TimeDisplayText calendarDateYearDisplayString(Resources context, Calendar cal)
    {
        if (cal == null) {
            return new TimeDisplayText(strTimeNone);
        }
        Locale locale = getLocale();
        SimpleDateFormat dateFormat = new SimpleDateFormat(strDateYearFormat, locale);
        //Log.d("DEBUG", "Year Format: " + dateFormat.toPattern() + " (" + locale.toString() + ")");
        return new TimeDisplayText(dateFormat.format(cal.getTime()), "", "");
    }

    /**
     * @param context a context
     * @param calendar  a Calendar representing some date
     * @return a time display string
     */
    public TimeDisplayText calendarDateDisplayString(Resources context, Calendar calendar) {
        return calendarDateDisplayString(context, calendar, false);
    }
    public TimeDisplayText calendarDateDisplayString(Resources context, Calendar calendar, boolean showYear) {
        return calendarDateDisplayString(context, calendar, showYear, false);
    }
    public TimeDisplayText calendarDateDisplayString(Resources context, Calendar calendar, boolean showYear, boolean abbreviate)
    {
        if (calendar == null || context == null)
        {
            return new TimeDisplayText(strTimeNone);
        }

        Locale locale = getLocale();
        SimpleDateFormat dateFormat;

        if (showYear)
            dateFormat = new SimpleDateFormat(strDateLongFormat, locale);
        else dateFormat = new SimpleDateFormat((abbreviate ? strDateVeryShortFormat : strDateShortFormat), locale);

        Date time = calendar.getTime();
        applySpecialTimeZone(time, calendar.getTimeZone());
        dateFormat.setTimeZone(calendar.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dateFormat.format(time), "", "");
        displayText.setRawValue(calendar.getTimeInMillis());
        return displayText;
    }

    /**
     * getDayString
     * @param day of week (0: sunday)
     * @return display string (e.g. Sunday)
     */
    public static String getDayString(int day) {
        String[] weekDays = getDayStrings();
        return (day >= 0 && day < weekDays.length ? weekDays[day] : "");
    }
    public static String[] getDayStrings() {
        return DateFormatSymbols.getInstance(getLocale()).getWeekdays();
    }

    /**
     * getShortDayString
     * @param day e.g. Calendar.SUNDAY
     * @return "Sun"
     */
    public static String getShortDayString(int day) {
        String[] shortWeekDays = getShortDayStrings();
        return (day >= 0 && day < shortWeekDays.length ? shortWeekDays[day] : "");
    }
    public static String[] getShortDayStrings() {
        return DateFormatSymbols.getInstance(getLocale()).getShortWeekdays();
    }

    public interface ResID_TimeDateDisplay
    {
        int string_strTimeVeryShortFormat12();
        int string_strTimeVeryShortFormat24();
        int string_strTimeVeryShortFormat12s();
        int string_strTimeVeryShortFormat24s();
        int string_strTimeNone();
        int string_strTimeLoading();

        int string_strDateYearFormat();
        int string_strDateVeryShortFormat();
        int string_strDateShortFormat();
        int string_strDateLongFormat();

        int string_strTimeShortFormat12();
    }

}
