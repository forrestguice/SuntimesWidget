/**
 Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.calendar;

import android.content.Context;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.TemporalType;
import net.time4j.calendar.CopticCalendar;
import net.time4j.calendar.EthiopianCalendar;
import net.time4j.calendar.HebrewCalendar;
import net.time4j.calendar.JulianCalendar;
import net.time4j.calendar.PersianCalendar;
import net.time4j.calendar.ThaiSolarCalendar;
import net.time4j.format.expert.ChronoFormatter;
import net.time4j.format.expert.PatternType;
import net.time4j.tz.ZonalOffset;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * CalendarMode
 */
public enum CalendarMode
{
    COPTIC("Coptic", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_COPTIC),
    ETHIOPIAN("Ethiopian",CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_ETHIOPIAN),
    GREGORIAN("Gregorian", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_GREGORIAN),
    HEBREW("Hebrew", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_HEBREW),
    JULIAN("Julian", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_JULIAN),
    PERSIAN("Solar Hijiri", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_PERSIAN),
    THAISOLAR("Thai Solar", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_THAISOLAR);

    private String displayString;
    private String defaultPattern;

    private CalendarMode(String displayString, String defaultPattern) {
        this.displayString = displayString;
        this.defaultPattern = defaultPattern;
    }

    public String getDefaultPattern() {
        return defaultPattern;
    }

    public String toString() {
        return displayString;
    }

    public String getDisplayString() {
        return displayString;
    }

    public void setDisplayString( String displayString ) {
        this.displayString = displayString;
    }

    public String formatDate(Calendar now) {
        return formatDate(this, defaultPattern, now);
    }
    public static String formatDate(CalendarMode calendar, String pattern, Calendar now)
    {
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(now.getTime());
        ZonalOffset offset = ZonalOffset.ofTotalSeconds(now.getTimeZone().getOffset(now.getTimeInMillis()) / 1000);
        PlainDate today = moment.toZonalTimestamp(offset).toDate();
        try {
            switch (calendar)
            {
                case THAISOLAR:
                    ChronoFormatter<ThaiSolarCalendar> thaiCalendar = ChronoFormatter.setUp(ThaiSolarCalendar.axis(), SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR).build();
                    return thaiCalendar.format(today.transform(ThaiSolarCalendar.class));

                case PERSIAN:
                    ChronoFormatter<PersianCalendar> persianCalendar = ChronoFormatter.setUp(PersianCalendar.axis(), SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return persianCalendar.format(today.transform(PersianCalendar.class));

                case ETHIOPIAN:
                    ChronoFormatter<EthiopianCalendar> ethiopianCalendar = ChronoFormatter.setUp(EthiopianCalendar.axis(), SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return ethiopianCalendar.format(today.transform(EthiopianCalendar.class));    // conversion at noon

                case HEBREW:
                    ChronoFormatter<HebrewCalendar> hebrewCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR_DATE, SuntimesUtils.getLocale(), HebrewCalendar.axis());
                    return hebrewCalendar.format(today.transform(HebrewCalendar.class));

                case JULIAN:
                    ChronoFormatter<JulianCalendar> julianCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR, SuntimesUtils.getLocale(), JulianCalendar.axis());
                    return julianCalendar.format(today.transform(JulianCalendar.class));

                case COPTIC:
                    ChronoFormatter<CopticCalendar> copticCalendar = ChronoFormatter.setUp(CopticCalendar.axis(), SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return copticCalendar.format(today.transform(CopticCalendar.class));    // conversion at noon

                case GREGORIAN:
                default:
                    SimpleDateFormat gregorian = new SimpleDateFormat(pattern, SuntimesUtils.getLocale());
                    return gregorian.format(now.getTime());
            }
        } catch (IllegalStateException e) {    // bad pattern
            Log.e("CalendarMode", "formatDate: " + e);
            return "";
        }
    }

    public static void initDisplayStrings( Context context )
    {
        COPTIC.setDisplayString(context.getString(R.string.calendarMode_coptic));
        ETHIOPIAN.setDisplayString(context.getString(R.string.calendarMode_ethiopian));
        GREGORIAN.setDisplayString(context.getString(R.string.calendarMode_gregorian));
        HEBREW.setDisplayString(context.getString(R.string.calendarMode_hebrew));
        JULIAN.setDisplayString(context.getString(R.string.calendarMode_julian));
        PERSIAN.setDisplayString(context.getString(R.string.calendarMode_persian));
        THAISOLAR.setDisplayString(context.getString(R.string.calendarMode_thaisolar));
    }
}
