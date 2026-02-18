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

package com.forrestguice.suntimeswidget.calculator.time4a;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.calendar.CalendarDisplay;
import com.forrestguice.suntimeswidget.calendar.CalendarMode;
import com.forrestguice.util.Log;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.SystemClock;
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

public class Time4JCalendarDisplay implements CalendarDisplay
{
    @NonNull
    @Override
    public String formatDate(CalendarMode calendar, String pattern, Calendar now)
    {
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(now.getTime());
        ZonalOffset offset = ZonalOffset.ofTotalSeconds(now.getTimeZone().getOffset(now.getTimeInMillis()) / 1000);
        PlainDate today = moment.toZonalTimestamp(offset).toDate();
        try {
            switch (calendar)
            {
                case THAISOLAR:
                    ChronoFormatter<ThaiSolarCalendar> thaiCalendar = ChronoFormatter.setUp(ThaiSolarCalendar.axis(), TimeDateDisplay.getLocale()).addPattern(pattern, PatternType.CLDR).build();
                    return thaiCalendar.format(today.transform(ThaiSolarCalendar.class));

                case PERSIAN:
                    ChronoFormatter<PersianCalendar> persianCalendar = ChronoFormatter.setUp(PersianCalendar.axis(), TimeDateDisplay.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return persianCalendar.format(today.transform(PersianCalendar.class));

                case ETHIOPIAN:
                    ChronoFormatter<EthiopianCalendar> ethiopianCalendar = ChronoFormatter.setUp(EthiopianCalendar.axis(), TimeDateDisplay.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return ethiopianCalendar.format(today.transform(EthiopianCalendar.class));    // conversion at noon

                case HEBREW:
                    ChronoFormatter<HebrewCalendar> hebrewCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR_DATE, TimeDateDisplay.getLocale(), HebrewCalendar.axis());
                    return hebrewCalendar.format(today.transform(HebrewCalendar.class));

                /*case HIJRI_DIYANET:
                    ChronoFormatter<HijriCalendar> hijriCalendar0 = ChronoFormatter.setUp(HijriCalendar.class, SuntimesUtils.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build()
                            .withCalendarVariant(HijriCalendar.VARIANT_DIYANET);
                    return hijriCalendar0.format(today.transform(HijriCalendar.class, HijriCalendar.VARIANT_DIYANET));*/

                case HIJRI_UMALQURA:
                    ChronoFormatter<HijriCalendar> hijriCalendar1 = ChronoFormatter.setUp(HijriCalendar.class, TimeDateDisplay.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build()
                            .withCalendarVariant(HijriCalendar.VARIANT_UMALQURA);
                    return hijriCalendar1.format(today.transform(HijriCalendar.class, HijriCalendar.VARIANT_UMALQURA));

                case JULIAN:
                    ChronoFormatter<JulianCalendar> julianCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR, TimeDateDisplay.getLocale(), JulianCalendar.axis());
                    return julianCalendar.format(today.transform(JulianCalendar.class));

                case COPTIC:
                    ChronoFormatter<CopticCalendar> copticCalendar = ChronoFormatter.setUp(CopticCalendar.axis(), TimeDateDisplay.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return copticCalendar.format(today.transform(CopticCalendar.class));    // conversion at noon

                case JAPANESE:
                    ChronoFormatter<JapaneseCalendar> japaneseCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR_DATE, TimeDateDisplay.getLocale(), JapaneseCalendar.axis());
                    return japaneseCalendar.format(today.transform(JapaneseCalendar.class));

                case MINGUO:
                    ChronoFormatter<MinguoCalendar> minguoCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR_DATE, TimeDateDisplay.getLocale(), MinguoCalendar.axis());
                    return minguoCalendar.format(today.transform(MinguoCalendar.class));

                case INDIAN:
                    ChronoFormatter<IndianCalendar> indianCalendar = ChronoFormatter.ofPattern(pattern, PatternType.CLDR_DATE, TimeDateDisplay.getLocale(), IndianCalendar.axis());
                    return indianCalendar.format(today.transform(IndianCalendar.class));

                case VIETNAMESE:
                    ChronoFormatter<VietnameseCalendar> vietnameseCalendar = ChronoFormatter.setUp(VietnameseCalendar.axis(), TimeDateDisplay.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return vietnameseCalendar.format(today.transform(VietnameseCalendar.class));

                case KOREAN:
                    ChronoFormatter<KoreanCalendar> koreanCalendar = ChronoFormatter.setUp(KoreanCalendar.axis(), TimeDateDisplay.getLocale()).addPattern(pattern, PatternType.CLDR_DATE).build();
                    return koreanCalendar.format(today.transform(KoreanCalendar.class));

                case CHINESE:
                    ChronoFormatter<ChineseCalendar> chineseCalendar = ChronoFormatter.setUp(ChineseCalendar.axis(), TimeDateDisplay.getLocale()).addPattern(pattern, PatternType.CLDR_DATE)
                            //.addText(ChineseCalendar.SOLAR_TERM)  // TODO: no @FormattableElement for SOLAR_TERM?
                            .build();
                    return chineseCalendar.format(today.transform(ChineseCalendar.class));

                case GREGORIAN:
                default:
                    SimpleDateFormat gregorian = new SimpleDateFormat(pattern, TimeDateDisplay.getLocale());
                    return gregorian.format(now.getTime());
            }
        } catch (ArithmeticException | IllegalStateException | IllegalArgumentException e) {    // bad pattern or out-of-range
            Log.e("CalendarMode", "formatDate: " + e);
            return "";
        }
    }

    @Override
    public int hijriLunarDayNumber()
    {
        HijriCalendar today = SystemClock.inLocalView().today().transform(HijriCalendar.class, HijriCalendar.VARIANT_UMALQURA);
        int dayNum = today.getDayOfMonth();
        return dayNum;
    }

}
