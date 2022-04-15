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
import android.support.annotation.NonNull;

import com.forrestguice.suntimeswidget.R;

import java.util.Calendar;

/**
 * CalendarFormat
 */
public enum CalendarFormat
{
    F_ISO8601("%s", "yyyy-MM-dd"),                     // year-month-day
    F_yyyy("%s [year]", "yyyy"),                       // year only
    F_MM("%s [month]", "MM"),                          // month only
    F_dd("%s [day of month]", "dd"),                   // day_of_month only
    F_DD("%s [day of year]", "DD"),                    // day_of_year only
    F_MMMM("%s [month]", "MMMM"),                      // month name
    F_MMMM_d("%s", "MMMM d"),                          // month + day
    F_MMMM_d_yyyy("%s", "MMMM d, yyyy"),               // month day, year
    F_d_MMMM_yyyy("%s","d MMMM yyyy"),                 // day month year
    F_MMMM_d_yyyy_G("%s", "MMMM d, yyyy G"),           // month day, year era
    F_yyyy_G("%s", "yyyy G"),                          // year + era
    CUSTOM("Custom Format", null);

    private String displayString;
    private String pattern;

    private CalendarFormat(String displayString, String pattern) {
        this.displayString = displayString;
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public String toString() {
        return displayString;
    }

    public String getDisplayString() {
        return displayString != null ? displayString : "";
    }

    public void setDisplayString( String displayString ) {
        this.displayString = displayString;
    }

    public static void initDisplayStrings( Context context ) {
        initDisplayStrings(context, CalendarMode.GREGORIAN, Calendar.getInstance());
    }
    public static void initDisplayStrings(Context context, @NonNull CalendarMode mode, @NonNull Calendar now )
    {
        CUSTOM.setDisplayString(context.getString(R.string.configLabel_general_calendarFormat_custom));
        F_yyyy.setDisplayString(context.getString(R.string.configLabel_general_calendarFormat_yyyy));
        F_MM.setDisplayString(context.getString(R.string.configLabel_general_calendarFormat_MM));
        F_dd.setDisplayString(context.getString(R.string.configLabel_general_calendarFormat_dd));
        F_DD.setDisplayString(context.getString(R.string.configLabel_general_calendarFormat_DD));
        F_MMMM.setDisplayString(context.getString(R.string.configLabel_general_calendarFormat_MMMM));

        for (CalendarFormat value : CalendarFormat.values()) {
            if (value.displayString == null) {
                value.setDisplayString(CalendarMode.formatDate(mode, value.pattern, now));
            } else if (value.displayString.contains("%s")) {
                value.setDisplayString( String.format(value.displayString, CalendarMode.formatDate(mode, value.pattern, now)));
            }
        }
    }
}
