/**
 Copyright (C) 2022-2023 Forrest Guice
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
import net.time4j.calendar.ChineseCalendar;
import net.time4j.calendar.CopticCalendar;
import net.time4j.calendar.EthiopianCalendar;
import net.time4j.calendar.HebrewCalendar;
import net.time4j.calendar.HijriCalendar;
import net.time4j.calendar.IndianCalendar;
import net.time4j.calendar.JapaneseCalendar;
import net.time4j.calendar.JulianCalendar;
import net.time4j.calendar.KoreanCalendar;
import net.time4j.calendar.MinguoCalendar;
import net.time4j.calendar.PersianCalendar;
import net.time4j.calendar.ThaiSolarCalendar;
import net.time4j.calendar.VietnameseCalendar;
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
    CHINESE("Chinese", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_CHINESE),
    COPTIC("Coptic", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_COPTIC),
    ETHIOPIAN("Ethiopian",CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_ETHIOPIAN),
    GREGORIAN("Gregorian", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_GREGORIAN),
    HEBREW("Hebrew", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_HEBREW),
    HIJRI_DIYANET("Hijri (Turkish)", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_HIJRI_DIYANET),
    HIJRI_UMALQURA("Hijri (Umm al-Qura)", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_HIJRI_UMALQURA),
    INDIAN("Indian", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_INDIAN),
    JAPANESE("Japanese", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_JAPANESE),
    JULIAN("Julian", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_JULIAN),
    KOREAN("Korean", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_KOREAN),
    MINGUO("Minguo", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_MINGUO),
    PERSIAN("Solar Hijiri", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_PERSIAN),
    THAISOLAR("Thai Solar", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_THAISOLAR),
    VIETNAMESE("Vietnamese", CalendarSettings.PREF_DEF_CALENDAR_FORMATPATTERN_VIETNAMESE);

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

                case HIJRI_DIYANET:
                    ChronoFormatter<HijriCalendar> hijriCalendar0 = ChronoFormatter.setUp(HijriCalendar.class, SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build()
                            .withCalendarVariant(HijriCalendar.VARIANT_DIYANET);
                    return hijriCalendar0.format(today.transform(HijriCalendar.class, HijriCalendar.VARIANT_DIYANET));

                case HIJRI_UMALQURA:
                    ChronoFormatter<HijriCalendar> hijriCalendar1 = ChronoFormatter.setUp(HijriCalendar.class, SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build()
                            .withCalendarVariant(HijriCalendar.VARIANT_UMALQURA);
                    return hijriCalendar1.format(today.transform(HijriCalendar.class, HijriCalendar.VARIANT_UMALQURA));

                case JULIAN:
                    ChronoFormatter<JulianCalendar> julianCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR, SuntimesUtils.getLocale(), JulianCalendar.axis());
                    return julianCalendar.format(today.transform(JulianCalendar.class));

                case COPTIC:
                    ChronoFormatter<CopticCalendar> copticCalendar = ChronoFormatter.setUp(CopticCalendar.axis(), SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return copticCalendar.format(today.transform(CopticCalendar.class));    // conversion at noon

                case JAPANESE:
                    ChronoFormatter<JapaneseCalendar> japaneseCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR_DATE, SuntimesUtils.getLocale(), JapaneseCalendar.axis());
                    return japaneseCalendar.format(today.transform(JapaneseCalendar.class));

                case MINGUO:
                    ChronoFormatter<MinguoCalendar> minguoCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR_DATE, SuntimesUtils.getLocale(), MinguoCalendar.axis());
                    return minguoCalendar.format(today.transform(MinguoCalendar.class));

                case INDIAN:
                    ChronoFormatter<IndianCalendar> indianCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR_DATE, SuntimesUtils.getLocale(), IndianCalendar.axis());
                    return indianCalendar.format(today.transform(IndianCalendar.class));

                case VIETNAMESE:
                    ChronoFormatter<VietnameseCalendar> vietnameseCalendar = ChronoFormatter.setUp(VietnameseCalendar.axis(), SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return vietnameseCalendar.format(today.transform(VietnameseCalendar.class));

                case KOREAN:
                    ChronoFormatter<KoreanCalendar> koreanCalendar = ChronoFormatter.setUp(KoreanCalendar.axis(), SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return koreanCalendar.format(today.transform(KoreanCalendar.class));

                case CHINESE:
                    ChronoFormatter<ChineseCalendar> chineseCalendar = ChronoFormatter.setUp(ChineseCalendar.axis(), SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE)
                            //.addText(ChineseCalendar.SOLAR_TERM)  // TODO: no @FormattableElement for SOLAR_TERM?
                            .build();
                    return chineseCalendar.format(today.transform(ChineseCalendar.class));

                case GREGORIAN:
                default:
                    SimpleDateFormat gregorian = new SimpleDateFormat(pattern, SuntimesUtils.getLocale());
                    return gregorian.format(now.getTime());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {    // bad pattern
            Log.e("CalendarMode", "formatDate: " + e);
            return "";
        }
    }

    public static void initDisplayStrings( Context context )
    {
        CHINESE.setDisplayString(context.getString(R.string.calendarMode_chinese));
        COPTIC.setDisplayString(context.getString(R.string.calendarMode_coptic));
        ETHIOPIAN.setDisplayString(context.getString(R.string.calendarMode_ethiopian));
        GREGORIAN.setDisplayString(context.getString(R.string.calendarMode_gregorian));
        HEBREW.setDisplayString(context.getString(R.string.calendarMode_hebrew));
        HIJRI_DIYANET.setDisplayString(context.getString(R.string.calendarMode_hijri_diyanet));
        HIJRI_UMALQURA.setDisplayString(context.getString(R.string.calendarMode_hijri_umalqura));
        INDIAN.setDisplayString(context.getString(R.string.calendarMode_indian));
        JAPANESE.setDisplayString(context.getString(R.string.calendarMode_japanese));
        JULIAN.setDisplayString(context.getString(R.string.calendarMode_julian));
        KOREAN.setDisplayString(context.getString(R.string.calendarMode_korean));
        MINGUO.setDisplayString(context.getString(R.string.calendarMode_minguo));
        PERSIAN.setDisplayString(context.getString(R.string.calendarMode_persian));
        THAISOLAR.setDisplayString(context.getString(R.string.calendarMode_thaisolar));
        VIETNAMESE.setDisplayString(context.getString(R.string.calendarMode_vietnamese));
    }
}
