/**
    Copyright (C) 2014-2022 Forrest Guice
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

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;

import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Html;
import android.text.Spannable;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;

import java.text.DateFormat;

import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.suntimeswidget.calculator.settings.display.AngleDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.CardinalDirection;
import com.forrestguice.suntimeswidget.calculator.settings.display.LengthUnitDisplay;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;
import com.forrestguice.util.android.AndroidResources;
import com.forrestguice.util.text.TimeDisplayText;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.IllegalFormatConversionException;
import java.util.Locale;
import java.util.TimeZone;

public class SuntimesUtils
{
    public static final String SPANTAG_DST = "[d]";
    public static final String SPANTAG_WARNING = "[w]";

    public static final int DEF_WARNING_DRAWABLE = R.drawable.ic_action_warning;
    public static final int DEF_ERROR_DRAWABLE = R.drawable.ic_action_error;
    public static final int DEF_DST_DRAWABLE = R.drawable.ic_weather_sunny;

    protected static String strTimeShorter = "shorter";
    protected static String strTimeLonger = "longer";
    protected static String strTimeSame = "the same";
    protected static String strSpace = "\u00A0";
    public static String strEmpty = "";
    protected static String strYears = "y";
    protected static String strWeeks = "w";
    protected static String strDays = "d";
    public static String strHours = "h";
    public static String strMinutes = "m";
    public static String strSeconds = "s";

    protected static String strAltSymbol = "∠";
    protected static String strRaSymbol = "α";
    protected static String strDecSymbol = "δ";
    protected static String strDegreesFormat = "%1$s\u00B0";
    protected static String strDirectionFormat = "%1$s\u00A0%2$s";
    protected static String strElevationFormat = "%1$s%2$s";
    protected static String strDeclinationFormat = "%1$s %2$s";
    protected static String strRaFormat = "%1$s %2$s";
    protected static String strDistanceFormat = "%1$s\u00A0%2%s";

    public static String strTimeDeltaFormat = "%1$s"  + strEmpty + "%2$s";
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
    protected static boolean initialized = false;
    //private static int initCount = 0;

    protected static String strDateYearFormat = "yyyy";
    protected static String strDateVeryShortFormat = "MMM d";
    protected static String strDateShortFormat = "MMMM d";
    protected static String strDateLongFormat = "MMMM d, yyyy";
    protected static String strDateTimeVeryShortFormat = "MMM d, h:mm\u00A0a";
    protected static String strDateTimeShortFormat = "MMMM d, h:mm\u00A0a";
    protected static String strDateTimeLongFormat = "MMMM d, yyyy, h:mm\u00A0a";
    protected static String strDateTimeVeryShortFormatSec = "MMM d, h:mm:ss\u00A0a";
    protected static String strDateTimeShortFormatSec = "MMMM d, h:mm:ss\u00A0a";
    protected static String strDateTimeLongFormatSec = "MMMM d, yyyy, h:mm:ss\u00A0a";

    public SuntimesUtils()
    {
    }

    public static void initDisplayStrings(Context context)
    {
        //long bench_start = System.nanoTime();

        TimeFormatMode mode = WidgetSettings.loadTimeFormatModePref(context, 0);
        is24 = (mode == TimeFormatMode.MODE_SYSTEM || mode == TimeFormatMode.MODE_SUNTIMES) ? android.text.format.DateFormat.is24HourFormat(context)
                                                    : (mode == TimeFormatMode.MODE_24HR);

        Resources res = context.getResources();
        strTimeShorter = res.getString(R.string.delta_day_shorter);
        strTimeLonger = res.getString(R.string.delta_day_longer);
        strTimeSame = res.getString(R.string.delta_day_same);
        strYears = res.getString(R.string.delta_years);
        strWeeks = res.getString(R.string.delta_weeks);
        strDays = res.getString(R.string.delta_days);
        strHours = res.getString(R.string.delta_hours);
        strMinutes = res.getString(R.string.delta_minutes);
        strSeconds = res.getString(R.string.delta_seconds);

        strAltSymbol = res.getString(R.string.widgetLabel_altitude_symbol);
        strRaSymbol = res.getString(R.string.widgetLabel_rightAscension_symbol);
        strDecSymbol = res.getString(R.string.widgetLabel_declination_symbol);

        strDegreesFormat = res.getString(R.string.degrees_format);
        strDirectionFormat = res.getString(R.string.direction_format);
        strElevationFormat = res.getString(R.string.elevation_format);
        strRaFormat = res.getString(R.string.rightascension_format);
        strDeclinationFormat = res.getString(R.string.declination_format);
        strDistanceFormat = res.getString(R.string.distance_format);

        strTimeDeltaFormat = res.getString(R.string.delta_format);
        strTimeVeryShortFormat12 = res.getString(R.string.time_format_12hr_veryshort);
        strTimeVeryShortFormat24 = res.getString(R.string.time_format_24hr_veryshort);
        strTimeVeryShortFormat12s = res.getString(R.string.time_format_12hr_veryshort_withseconds);
        strTimeVeryShortFormat24s = res.getString(R.string.time_format_24hr_veryshort_withseconds);
        strTimeNone = res.getString(R.string.time_none);
        strTimeLoading = res.getString(R.string.time_loading);

        strDateYearFormat = res.getString(R.string.dateyear_format_short);
        strDateVeryShortFormat = res.getString(R.string.date_format_veryshort);
        strDateShortFormat = res.getString(R.string.date_format_short);
        strDateLongFormat = res.getString(R.string.date_format_long);

        strTimeShortFormat12 = res.getString(R.string.time_format_12hr_short, strTimeVeryShortFormat12, strTimeSuffixFormat);        //String timeFormat = (is24 ? strTimeVeryShortFormat24 : strTimeShortFormat12);
        strDateTimeVeryShortFormat = dateTimeFormatVeryShort(res, is24, false);  //  context.getString(R.string.datetime_format_short, strDateVeryShortFormat, timeFormat);
        strDateTimeShortFormat = dateTimeFormatShort(res, is24, false);  //  context.getString(R.string.datetime_format_short, strDateShortFormat, timeFormat);
        strDateTimeLongFormat = dateTimeFormatLong(res, is24, false);    // context.getString(R.string.datetime_format_long, strDateLongFormat, timeFormat);

        strTimeShortFormat12s = res.getString(R.string.time_format_12hr_short, strTimeVeryShortFormat12s, strTimeSuffixFormat);        //String timeFormatSec = (is24 ? strTimeVeryShortFormat24s : strTimeShortFormat12s);
        strDateTimeVeryShortFormatSec = dateTimeFormatVeryShort(res, is24, true);  // context.getString(R.string.datetime_format_short, strDateVeryShortFormat, timeFormatSec);
        strDateTimeShortFormatSec = dateTimeFormatShort(res, is24, true);  // context.getString(R.string.datetime_format_short, strDateShortFormat, timeFormatSec);
        strDateTimeLongFormatSec = dateTimeFormatLong(res, is24, true);    // context.getString(R.string.datetime_format_long, strDateLongFormat, timeFormatSec);

        //CardinalDirection.initDisplayStrings(context);
        AngleDisplay.initDisplayStrings(AndroidResources.wrap(context));
        LengthUnitDisplay.initDisplayStrings_LengthUnit(AndroidResources.wrap(context));

        initialized = true;
        ///initCount++;
        //long bench_end = System.nanoTime();
        //Log.d("DEBUG", "SuntimesUtils initialized: " + initCount + " :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
    }

    public static String dateTimeFormatVeryShort(Resources res, boolean is24, boolean showSeconds)
    {
        String timeFormat = (showSeconds ? (is24 ? strTimeVeryShortFormat24s : strTimeShortFormat12s) : (is24 ? strTimeVeryShortFormat24 : strTimeShortFormat12));
        return res.getString(R.string.datetime_format_short, strDateVeryShortFormat, timeFormat);
    }
    public static String dateTimeFormatShort(Resources res, boolean is24, boolean showSeconds)
    {
        String timeFormat = (showSeconds ? (is24 ? strTimeVeryShortFormat24s : strTimeShortFormat12s) : (is24 ? strTimeVeryShortFormat24 : strTimeShortFormat12));
        return res.getString(R.string.datetime_format_short, strDateShortFormat, timeFormat);
    }
    public static String dateTimeFormatLong(Resources res, boolean is24, boolean showSeconds)
    {
        String timeFormat = (showSeconds ? (is24 ? strTimeVeryShortFormat24s : strTimeShortFormat12s) : (is24 ? strTimeVeryShortFormat24 : strTimeShortFormat12));
        return res.getString(R.string.datetime_format_long, strDateLongFormat, timeFormat);
    }

    public static boolean isInitialized()
    {
        return initialized;
    }

    public static boolean is24()
    {
        return is24;
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
    public TimeDisplayText calendarTimeShortDisplayString(Context context, Calendar cal, boolean showSeconds, TimeFormatMode format)
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
                    boolean sysIs24 = android.text.format.DateFormat.is24HourFormat(context);
                    return (sysIs24 ? calendarTime24HrDisplayString(context, cal, showSeconds)
                            : calendarTime12HrDisplayString(context, cal, showSeconds));

                default:
                    return new TimeDisplayText(strTimeNone);
            }
        }
    }

    /**
     * getDayString
     * @param context Context
     * @param day e.g. Calendar.SUNDAY
     * @return "Sunday"
     */
    public String getDayString(Context context, int day)
    {
        return DateUtils.getDayOfWeekString(day, DateUtils.LENGTH_LONG);
    }

    /**
     * getShortDayString
     * @param context Context
     * @param day e.g. Calendar.SUNDAY
     * @return "Sun"
     */
    public String getShortDayString(Context context, int day)
    {
        String[] shortWeekDays = getShortDayStrings(context);
        return (day >= 0 && day < shortWeekDays.length ? shortWeekDays[day] : "");
    }
    public String[] getShortDayStrings(Context context)
    {
        return DateFormatSymbols.getInstance(getLocale()).getShortWeekdays();
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
        Date time = cal.getTime();
        applyTimeZone(time, cal.getTimeZone());
        timeFormat.setTimeZone(cal.getTimeZone());
        TimeDisplayText retValue = new TimeDisplayText(timeFormat.format(time), "", "");
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
        Date time = cal.getTime();
        applyTimeZone(time, cal.getTimeZone());
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
     * applyTimeZone
     * An opportunity to directly modify the Date before its formatted/displayed; apply special
     * timezone rules here.
     */
    public static void applyTimeZone(@NonNull Date time, @NonNull TimeZone timezone)
    {
        String tzID = timezone.getID();
        if (tzID.equals(WidgetTimezones.SiderealTime.TZID_GMST) || tzID.equals(WidgetTimezones.SiderealTime.TZID_LMST)) {
            time.setTime(WidgetTimezones.SiderealTime.gmstOffset(time.getTime()) + time.getTime());   // these already extend LocalMeanTime (so apply gmst offset only)
        }
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

        SimpleDateFormat timeFormat = initTimeFormat_12(showSeconds);
        timeFormat.setTimeZone(cal.getTimeZone());
        timeFormat_12_suffix.setTimeZone(cal.getTimeZone());

        Date time = cal.getTime();
        applyTimeZone(time, cal.getTimeZone());
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

    public String calendarTime12HrString(Context context, @NonNull Calendar cal)
    {
        Locale locale = getLocale();
        SimpleDateFormat timeFormat = new SimpleDateFormat(strTimeShortFormat12, locale); // h:mm a

        Date time = cal.getTime();
        applyTimeZone(time, cal.getTimeZone());
        timeFormat.setTimeZone(cal.getTimeZone());
        return timeFormat.format(time);
    }

    /**
     * @param context a context
     * @param calendar a Calendar representing some date
     * @param abbreviate true abbreviate name, false full name
     * @return day name e.g. Monday (or Mon abbreviated)
     */
    @Deprecated
    public TimeDisplayText calendarDayDisplayString(Context context, Calendar calendar, boolean abbreviate)
    {
        if (calendar == null || context == null)
        {
            return new TimeDisplayText(strTimeNone);
        }

        Locale locale = getLocale();
        SimpleDateFormat dayFormat = new SimpleDateFormat((abbreviate ? "E" : "EEEE"), locale);

        Date time = calendar.getTime();
        applyTimeZone(time, calendar.getTimeZone());
        dayFormat.setTimeZone(calendar.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dayFormat.format(time), "", "");
        displayText.setRawValue(calendar.getTimeInMillis());
        return displayText;
    }

    /**
     * @param context a context
     * @param calendar  a Calendar representing some date
     * @return a time display string
     */
    public TimeDisplayText calendarDateDisplayString(Context context, Calendar calendar) {
        return calendarDateDisplayString(context, calendar, false);
    }
    public TimeDisplayText calendarDateDisplayString(Context context, Calendar calendar, boolean showYear) {
        return calendarDateDisplayString(context, calendar, showYear, false);
    }
    public TimeDisplayText calendarDateDisplayString(Context context, Calendar calendar, boolean showYear, boolean abbreviate)
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
        applyTimeZone(time, calendar.getTimeZone());
        dateFormat.setTimeZone(calendar.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dateFormat.format(time), "", "");
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
        return calendarDateTimeDisplayString(context, cal, (cal != null && (cal.get(Calendar.YEAR) != now.get(Calendar.YEAR))), true, false, false);
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, long timestamp)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return calendarDateTimeDisplayString(context, cal, true, true);
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal, boolean showTime, boolean showSeconds) {
        return calendarDateTimeDisplayString(context, cal, showTime, showSeconds, false);
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal, boolean showTime, boolean showSeconds, boolean abbreviate) {
        Calendar now = Calendar.getInstance();
        return calendarDateTimeDisplayString(context, cal, (cal != null && (cal.get(Calendar.YEAR) != now.get(Calendar.YEAR))), showTime, showSeconds, abbreviate);
    }
    public TimeDisplayText calendarDateTimeDisplayString(@Nullable Context context, Calendar cal, boolean showYear, boolean showTime, boolean showSeconds, boolean abbreviate)
    {
        if (cal == null) {
            return new TimeDisplayText(strTimeNone);
        }

        Locale locale = getLocale();
        SimpleDateFormat dateTimeFormat;
        if (showTime) {
            if (showSeconds)
                dateTimeFormat = new SimpleDateFormat((showYear ? strDateTimeLongFormatSec : (abbreviate ? strDateTimeVeryShortFormatSec : strDateTimeShortFormatSec)), locale);
            else dateTimeFormat = new SimpleDateFormat((showYear ? strDateTimeLongFormat : (abbreviate ? strDateTimeVeryShortFormat : strDateTimeShortFormat)), locale);
        } else dateTimeFormat = new SimpleDateFormat((showYear ? strDateLongFormat : (abbreviate ? strDateVeryShortFormat : strDateShortFormat)), locale);
        //Log.d("DEBUG","DateTimeFormat: " + dateTimeFormat.toPattern() + " (" + locale.toString() + ")");

        Date time = cal.getTime();
        applyTimeZone(time, cal.getTimeZone());
        dateTimeFormat.setTimeZone(cal.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dateTimeFormat.format(time), "", "");
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

    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal, boolean showTime, boolean showSeconds, TimeFormatMode format) {
        return calendarDateTimeDisplayString(context, cal, (cal != null && (cal.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))), showTime, showSeconds, false, format);
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal, boolean showTime, boolean showSeconds, boolean abbreviate, TimeFormatMode format) {
        return calendarDateTimeDisplayString(context, cal, (cal != null && (cal.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))), showTime, showSeconds, abbreviate, format);
    }
    public TimeDisplayText calendarDateTimeDisplayString(Context context, Calendar cal, boolean showYear, boolean showTime, boolean showSeconds, boolean abbreviate, TimeFormatMode format)
    {
        if (cal == null || context == null) {
            return new TimeDisplayText(strTimeNone);
        }

        boolean formatIs24;
        switch (format) {
            case MODE_SUNTIMES: formatIs24 = is24; break;
            case MODE_SYSTEM: formatIs24 = android.text.format.DateFormat.is24HourFormat(context); break;
            case MODE_12HR: formatIs24 = false; break;
            case MODE_24HR: default: formatIs24 = true; break;
        }

        Locale locale = getLocale();
        SimpleDateFormat dateTimeFormat;
        if (showTime) {
            dateTimeFormat = new SimpleDateFormat((showYear ? dateTimeFormatLong(context.getResources(), formatIs24, showSeconds)
                    : (abbreviate ? dateTimeFormatVeryShort(context.getResources(), formatIs24, showSeconds)
                        : dateTimeFormatShort(context.getResources(), formatIs24, showSeconds))
            ), locale);
        } else dateTimeFormat = new SimpleDateFormat((showYear ? strDateLongFormat
                : (abbreviate ? strDateVeryShortFormat
                    : strDateShortFormat)
        ), locale);
        //Log.d("DEBUG","DateTimeFormat: " + dateTimeFormat.toPattern() + " (" + locale.toString() + ")");

        Date time = cal.getTime();
        applyTimeZone(time, cal.getTimeZone());
        dateTimeFormat.setTimeZone(cal.getTimeZone());
        TimeDisplayText displayText = new TimeDisplayText(dateTimeFormat.format(time), "", "");
        displayText.setRawValue(cal.getTimeInMillis());
        return displayText;
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
    @Deprecated
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2)
    {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, false, true, false);
    }
    @Deprecated
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showSeconds)
    {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, false, true, showSeconds);
    }

    @Deprecated
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan, boolean showSeconds)
    {
        TimeDisplayText text = timeDeltaLongDisplayString(0, timeSpan, showSeconds);
        text.setSuffix("");
        return text;
    }

    @Deprecated
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showWeeks, boolean showHours, boolean showSeconds) {
        return timeDeltaLongDisplayString(timeSpan1, timeSpan2, showWeeks, showHours, true, showSeconds);
    }
    public TimeDisplayText timeDeltaLongDisplayString(long timeSpan1, long timeSpan2, boolean showWeeks, boolean showHours, boolean showMinutes, boolean showSeconds)
    {
        String value = strEmpty;
        String units = strEmpty;
        String suffix = strEmpty;

        long timeSpan = timeSpan2 - timeSpan1;
        GregorianCalendar d = new GregorianCalendar();
        d.setTimeInMillis(timeSpan);
        long timeInMillis = d.getTimeInMillis();

        long numberOfSeconds = timeInMillis / 1000;
        suffix += (numberOfSeconds == 0) ? strTimeSame
                : ((numberOfSeconds > 0) ? strTimeLonger : strTimeShorter);
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
        boolean showingMinutes = (showMinutes && !showingDays && !showingWeeks && !showingYears && remainingMinutes > 0);
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

    public String timeDeltaLongDisplayString(long timeInMillis)
    {
        long numberOfSeconds = timeInMillis / 1000;
        numberOfSeconds = Math.abs(numberOfSeconds);

        long numberOfMinutes = numberOfSeconds / 60;
        long numberOfHours = numberOfMinutes / 60;
        long numberOfDays = numberOfHours / 24;

        long remainingHours = numberOfHours % 24;
        long remainingMinutes = numberOfMinutes % 60;
        long remainingSeconds = numberOfSeconds % 60;

        boolean showingDays = (numberOfDays > 0);
        boolean showingHours = (remainingHours > 0);
        boolean showingMinutes = (remainingMinutes > 0);
        boolean showingSeconds = (remainingSeconds > 0);

        String value = strEmpty;

        if (showingDays)
            value += String.format(strTimeDeltaFormat, numberOfDays, strDays);

        if (showingHours)
            value += (showingDays ? strSpace : strEmpty) +
                    String.format(strTimeDeltaFormat, remainingHours, strHours);

        if (showingMinutes)
            value += (showingDays || showingHours ? strSpace : strEmpty) +
                    String.format(strTimeDeltaFormat, remainingMinutes, strMinutes);

        //if (showingSeconds)
        //    value += (showingHours || showingMinutes ? strSpace : strEmpty) +
        //            String.format(strTimeDeltaFormat, remainingSeconds, strSeconds);

        if (!showingSeconds && !showingMinutes && !showingHours && !showingDays)
            value += String.format(strTimeDeltaFormat, "0", strSeconds);

        return value.trim();
    }

    public String formatDoubleValue(double value, int places)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(places);
        formatter.setMaximumFractionDigits(places);
        return formatter.format(value);
    }

    /**
     * @param value
     * @return
     */
    @Deprecated    // use AngleDisplay instead
    public String formatAsDegrees(double value)
    {
        return String.format(strDegreesFormat, NumberFormat.getNumberInstance().format(value));
    }
    @Deprecated    // use AngleDisplay instead
    public String formatAsDegrees(double value, int places)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(places);
        formatter.setMaximumFractionDigits(places);
        return String.format(strDegreesFormat, formatter.format(value));
    }
    @Deprecated    // use AngleDisplay instead
    public String formatAsDirection(double degreeValue, int places)
    {
        String degreeString = formatAsDegrees(degreeValue, places);
        CardinalDirection direction = CardinalDirection.getDirection(degreeValue);
        return formatAsDirection(degreeString, direction.getShortDisplayString());
    }
    @Deprecated    // use AngleDisplay instead
    public String formatAsDirection(String degreeString, String directionString)
    {
        return String.format(strDirectionFormat, degreeString, directionString);
    }
    @Deprecated    // use AngleDisplay instead
    public TimeDisplayText formatAsDirection2(double degreeValue, int places, boolean longSuffix)
    {
        String degreeString = formatAsDegrees(degreeValue, places);
        CardinalDirection direction = CardinalDirection.getDirection(degreeValue);
        return new TimeDisplayText(degreeString, "", (longSuffix ? direction.getLongDisplayString() : direction.getShortDisplayString()));
    }

    @Deprecated    // use AngleDisplay instead
    public String formatAsElevation(String degreeString, String altitudeSymbol)
    {
        return String.format(strElevationFormat, degreeString, altitudeSymbol);
    }
    @Deprecated    // use AngleDisplay instead
    public TimeDisplayText formatAsElevation(double degreeValue, int places)
    {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strAltSymbol);
    }

    @Deprecated    // use AngleDisplay instead
    public String formatAsRightAscension(String degreeString, String raSymbol)
    {
        return String.format(strRaFormat, degreeString, raSymbol);
    }
    @Deprecated    // use AngleDisplay instead
    public TimeDisplayText formatAsRightAscension(double degreeValue, int places)
    {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strRaSymbol);
    }

    @Deprecated    // use AngleDisplay instead
    public String formatAsDeclination(String degreeString, String decSymbol)
    {
        return String.format(strDeclinationFormat, degreeString, decSymbol);
    }
    @Deprecated    // use AngleDisplay instead
    public TimeDisplayText formatAsDeclination(double degreeValue, int places)
    {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strDecSymbol);
    }

    @Deprecated    // use LengthUnitDisplay instead
    public static String formatAsHeight(Context context, double value, LengthUnit units, boolean convert, int places)
    {
        int stringID;
        switch (units)
        {
            case IMPERIAL:
                if (convert) {
                    value = LengthUnit.metersToFeet(value);
                }
                stringID = R.plurals.units_feet_long;
                break;

            case METRIC:
            default:
                stringID = R.plurals.units_meters_long;
                break;
        }
        int h = ((value > 1) ? (int)Math.ceil(value)   // TODO: better use of plurals w/ fractional values..
               : (value < 1) ? 2 : 1);   // this is a hack; there must be a better way to treat fractions as plural

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);

        try {
            return context.getResources().getQuantityString(stringID, h, formatter.format(value));

        } catch (IllegalFormatConversionException e) {
            Log.e("formatAsHeight", "ignoring invalid format in stringID: " + stringID);
            return String.valueOf(value);
        }
    }

    @Deprecated    // use LengthUnitDisplay instead
    public static TimeDisplayText formatAsHeight(Context context, double meters, LengthUnit units, int places, boolean shortForm)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        String formatted;

        double value;
        String unitsString;
        switch (units)
        {
            case IMPERIAL:
                value = LengthUnit.metersToFeet(meters);
                formatted = formatter.format(value);
                unitsString = (shortForm ? context.getString(R.string.units_feet_short)
                                         : context.getResources().getQuantityString(R.plurals.units_feet_long, (int)value, formatted));
                break;

            case METRIC:
            default:
                value = meters;
                formatted = formatter.format(value);
                unitsString = (shortForm ? context.getString(R.string.units_meters_short)
                                         : context.getResources().getQuantityString(R.plurals.units_meters_long, (int)value, formatted));
                break;
        }
        return new TimeDisplayText(formatted, unitsString, "");
    }

    @Deprecated    // use LengthUnitDisplay instead
    public static TimeDisplayText formatAsDistance(Context context, double kilometers, LengthUnit units, int places, boolean shortForm)
    {
        double value;
        String unitsString;
        switch (units)
        {
            case IMPERIAL:
                value = LengthUnit.kilometersToMiles(kilometers);
                unitsString = (shortForm ? context.getString(R.string.units_miles_short) : context.getString(R.string.units_miles));
                break;

            case METRIC:
            default:
                value = kilometers;
                unitsString = (shortForm ? context.getString(R.string.units_kilometers_short) : context.getString(R.string.units_kilometers));
                break;
        }

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        return new TimeDisplayText(formatter.format(value), unitsString, "");
    }

    public static String formatAsDistance(Context context, TimeDisplayText text) {
        return String.format(strDistanceFormat, text.getValue(), text.getUnits());
    }

    public static SpannableStringBuilder createSpan(Context context, String text, String spanTag, ImageSpan imageSpan)
    {
        return createSpan(context, text, spanTag, imageSpan, ImageSpan.ALIGN_BASELINE);
    }
    public static SpannableStringBuilder createSpan(Context context, String text, String spanTag, ImageSpan imageSpan, int alignment)
    {
        ImageSpanTag[] tags = { new ImageSpanTag(spanTag, imageSpan) };
        return createSpan(context, text, tags, alignment);
    }

    public static SpannableStringBuilder createSpan(Context context, String text, ImageSpanTag[] tags) {
        return createSpan(context, text, tags, ImageSpan.ALIGN_BASELINE);
    }
    public static SpannableStringBuilder createSpan(Context context, String text, ImageSpanTag[] tags, int alignment)
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

                span.setSpan(createImageSpan(imageSpan), tagPos, tagEnd, alignment);
                text = text.substring(0, tagPos) + tag.getBlank() + text.substring(tagEnd);
            }
        }
        return span;
    }

    public static SpannableStringBuilder createSpan(Context context, CharSequence text, ImageSpanTag[] tags) {
        return createSpan(context, text, tags, ImageSpan.ALIGN_BASELINE);
    }
    public static SpannableStringBuilder createSpan(Context context, CharSequence text, ImageSpanTag[] tags, int alignment)
    {
        SpannableStringBuilder span = new SpannableStringBuilder(text);
        ImageSpan blank = createImageSpan(context, R.drawable.ic_transparent, 0, 0, R.color.transparent);

        for (ImageSpanTag tag : tags)
        {
            String spanTag = tag.getTag();
            ImageSpan imageSpan = (tag.getSpan() == null) ? blank : tag.getSpan();

            int tagPos;
            while ((tagPos = TextUtils.indexOf(text, spanTag )) >= 0)
            {
                int tagEnd = tagPos + spanTag.length();
                //Log.d("DEBUG", "tag=" + spanTag + ", tagPos=" + tagPos + ", " + tagEnd + ", text=" + text);

                span.setSpan(createImageSpan(imageSpan), tagPos, tagEnd, alignment);
                text = text.subSequence(0, tagPos) + tag.getBlank() + text.subSequence(tagEnd, text.length());
            }
        }
        return span;
    }

    public static SpannableString createRoundedBackgroundColorSpan(SpannableString span, String text, String toColorize,
                                                                   final int textColor, final boolean boldText,
                                                                   final int backgroundColor, final float cornerRadiusPx, final float paddingPx)
    {
        ReplacementSpan replacementSpan = new ReplacementSpan()
        {
            @Override
            public int getSize(@NonNull Paint p, CharSequence t, int start, int end, @Nullable Paint.FontMetricsInt fontMetrics) {
                return (int) Math.ceil(p.measureText(t, start, end) + (2 * paddingPx));
            }

            @Override
            public void draw(@NonNull Canvas c, CharSequence t, int start, int end, float x, int top, int y, int bottom, @NonNull Paint p)
            {
                p.setColor(backgroundColor);
                RectF rect = new RectF(x, top, x + p.measureText(t, start, end) + (2 * paddingPx), bottom);
                c.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, p);

                p.setColor(textColor);
                p.setTypeface(boldText ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                c.drawText(t, start, end, x + paddingPx, y, p);
            }
        };

        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toColorize);
        if (start >= 0)
        {
            int end = start + toColorize.length() + 1;  // 1 beyond last character
            span.setSpan(replacementSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createBackgroundColorSpan(SpannableString span, String text, String toColorize, int color)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toColorize);
        if (start >= 0)
        {
            int end = start + toColorize.length();
            span.setSpan(new BackgroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    public static SpannableString createUnderlineSpan(SpannableString span, String text, String toUnderline)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toUnderline);
        if (start >= 0)
        {
            int end = start + toUnderline.length();
            span.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }
    public static SpannableString createUnderlineSpan(SpannableString span, String text, String toUnderline, int color)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toUnderline);
        if (start >= 0)
        {
            UnderlineSpan underline = new UnderlineSpan();
            TextPaint paint = new TextPaint();
            paint.setColor(color);
            underline.updateDrawState(paint);
            int end = start + toUnderline.length();
            span.setSpan(underline, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
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

    public static SpannableString createItalicSpan(SpannableString span, String text, String toBold)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toBold);
        if (start >= 0)
        {
            int end = start + toBold.length();
            span.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    /**
     * TypefaceSpan
     */

    public static SpannableString createTypefaceSpan(SpannableString span, String text, String toTypeface, String typeface)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toTypeface);
        if (start >= 0)
        {
            int end = start + toTypeface.length();
            span.setSpan(new TypefaceSpan(typeface), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static class TypefaceSpan extends MetricAffectingSpan
    {
        protected final Typeface typeface;

        public TypefaceSpan(String typeface) {
            this.typeface = Typeface.create(typeface, Typeface.NORMAL);
        }

        @Override
        public void updateDrawState(TextPaint paint) {
            paint.setTypeface(typeface);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            paint.setTypeface(typeface);
        }
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
        return createImageSpan(context, drawableID, width, height, tint, PorterDuff.Mode.SRC_ATOP);
    }
    public static ImageSpan createImageSpan(Context context, int drawableID, int width, int height, int tint, PorterDuff.Mode tintMode)
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
            drawable.setColorFilter(tint, tintMode);
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

    public static Drawable tintDrawable(Drawable drawable, int fillColor, int strokeColor, int strokePixels)
    {
        Drawable d = null;
        try {
            d = tintDrawable((InsetDrawable)drawable, fillColor, strokeColor, strokePixels);

        } catch (ClassCastException e) {
            try {
                //noinspection ConstantConditions
                d = tintDrawable((LayerDrawable)drawable, fillColor, strokeColor, strokePixels);

            } catch (ClassCastException e2) {
                try {
                    //noinspection ConstantConditions
                    d = tintDrawable((GradientDrawable)drawable, fillColor, strokeColor, strokePixels);

                } catch (ClassCastException e3) {
                    Log.e("tintDrawable", "");
                }
            }
        }
        return d;
    }

    @Nullable
    public static Drawable tintDrawableCompat(Drawable d, int color)
    {
        if (d != null)
        {
            Drawable tinted = DrawableCompat.wrap(d.mutate());
            DrawableCompat.setTint(tinted, color);
            DrawableCompat.setTintMode(tinted, PorterDuff.Mode.SRC_IN);
            return tinted;
        } else return null;
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
        if (drawable != null)
        {
            drawable.setStroke(strokePixels, strokeColor);
            drawable.setColor(fillColor);
        }
        return drawable;
    }

    public static Drawable tintDrawable(LayerDrawable drawable, int fillColor, int strokeColor, int strokePixels)
    {
        if (drawable != null)
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
        } else return null;
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
        if (drawable != null)
        {
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }

    /**
     * @param view the View to trigger the accessibility event
     * @param msg text that will be read aloud (if accessibility enabled)
     */
    public static void announceForAccessibility(View view, CharSequence msg)
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

    /**
     * @param enabledColor normal state color
     * @param disabledColor disabled state color
     * @return a ColorStateList w/ enabled / disabled states (intended for text label)
     */
    public static ColorStateList colorStateList(int enabledColor, int disabledColor)
    {
        return new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_enabled}, new int[] {-android.R.attr.state_enabled}},
                new int[] {enabledColor, disabledColor}
        );
    }

    /**
     * @param enabledColor normal state color
     * @param disabledColor disabled state color
     * @param pressedColor pressed/focused color
     * @return a ColorStateList w/ pressed, enabled, and disabled states (intended for text button)
     */
    public static ColorStateList colorStateList(int enabledColor, int disabledColor, int pressedColor)
    {
        return new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_pressed},
                        new int[] { android.R.attr.state_focused},
                        new int[] {-android.R.attr.state_enabled},
                        new int[] {} },
                new int[] {pressedColor, enabledColor, disabledColor, enabledColor}
        );
    }

    public static ColorStateList colorStateList(int onColor, int offColor, int disabledColor, int pressedColor)
    {
        return new ColorStateList(
                new int[][] { new int[] {android.R.attr.state_focused},
                        new int[] {android.R.attr.state_pressed},
                        new int[] {-android.R.attr.state_enabled},
                        new int[] {android.R.attr.state_checked},
                        new int[] {} },
                new int[] {onColor, pressedColor, disabledColor, onColor, offColor}
        );
    }

    /**
     *
     * @param drawables an array of compound drawable; expects [4] {left, top, right, bottom}
     * @param tintColor the color to apply
     * @return the same array now containing tinted drawables
     */
    public static Drawable[] tintCompoundDrawables(Drawable[] drawables, int tintColor)
    {
        if (drawables.length > 0)
        {
            for (int i=0; i<drawables.length; i++)
            {
                if (drawables[i] != null)
                {
                    drawables[i] = drawables[i].mutate();
                    drawables[i].setColorFilter(tintColor, PorterDuff.Mode.MULTIPLY);
                }
            }
        }
        return drawables;
    }

}
