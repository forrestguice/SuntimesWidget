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
    F8("%s", "yyyy-MM-dd"),                           // year-month-day
    F5("%s [Year]", "yyyy"),                          // year only
    F9("%s [Month]", "MM"),                           // month only
    F1("%s [Day of Month]", "d"),                     // day_of_month only
    F7("%s [Day of Year]", "D"),                      // day_of_year only
    F6("%s [Month]", "MMMM"),                         // month name
    F2("%s", "MMMM d"),                               // month + day
    F3("%s", "MMMM d, yyyy"),                         // month day, year
    F10("%s","d MMMM yyyy"),                          // day month year
    F4("%s", "MMMM d, yyyy G"),                       // month day, year era
    F11("%s [Era]", "G"),                             // era only
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
        // TODO: others
        CUSTOM.setDisplayString(context.getString(R.string.configLabel_general_calendarFormat_custom));

        for (CalendarFormat value : CalendarFormat.values()) {
            if (value.displayString == null) {
                value.setDisplayString(CalendarMode.formatDate(mode, value.pattern, now));
            } else if (value.displayString.contains("%s")) {
                value.setDisplayString( String.format(value.displayString, CalendarMode.formatDate(mode, value.pattern, now)));
            }
        }
    }
}
