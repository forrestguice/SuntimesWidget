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

/**
 * CalendarMode
 */
public enum CalendarMode
{
    CHINESE("Chinese", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_CHINESE),
    COPTIC("Coptic", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_COPTIC),
    ETHIOPIAN("Ethiopian", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_ETHIOPIAN),
    GREGORIAN("Gregorian", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_GREGORIAN),
    HEBREW("Hebrew", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_HEBREW),
    //HIJRI_DIYANET("Hijri (Turkish)", CalendarPatternDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_HIJRI_DIYANET),
    HIJRI_UMALQURA("Hijri (Umm al-Qura)", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_HIJRI_UMALQURA),
    INDIAN("Indian", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_INDIAN),
    JAPANESE("Japanese", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_JAPANESE),
    JULIAN("Julian", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_JULIAN),
    KOREAN("Korean", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_KOREAN),
    MINGUO("Minguo", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_MINGUO),
    PERSIAN("Solar Hijri", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_PERSIAN),
    THAISOLAR("Thai Solar", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_THAISOLAR),
    VIETNAMESE("Vietnamese", CalendarDefaults.PREF_DEF_CALENDAR_FORMATPATTERN_VIETNAMESE);

    private String displayString;
    private final String defaultPattern;

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
}
