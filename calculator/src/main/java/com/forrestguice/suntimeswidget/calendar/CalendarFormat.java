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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

import java.util.Calendar;

/**
 * CalendarFormat
 * @see CalendarMode
 */
public enum CalendarFormat
{
    CUSTOM("%s [custom]", null),                       // CUSTOM (search functions assume this item is at 0)
    F_ISO8601("%s", "yyyy-MM-dd"),                     // year-month-day
    F_yyyy("%s [year]", "yyyy"),                       // year only
    F_yy("%s [year]", "yy"),                           // year only
    F_MM("%s [month]", "MM"),                          // month only
    F_dd("%s [day of month]", "dd"),                   // day_of_month only
    F_DD("%s [day of year]", "DD"),                    // day_of_year only
    F_EEEE("%s [day of week]", "EEEE"),                // day name only
    F_EE("%s [day of week]", "EE"),                    // day name only
    F_MMM("%s [month]", "MMM"),                        // month only
    F_MMMM("%s [month]", "MMMM"),                      // month name
    F_MMM_d("%s", "MMM d"),                            // month + day
    F_MMMM_d("%s", "MMMM d"),                          // month + day
    F_EE_MMM_d("%s", "EE, MMM d"),                     // day name, month + day
    F_EE_MMMM_d("%s", "EE, MMMM d"),                   // day name, month + day
    F_EEEE_MMMM_d("%s", "EEEE, MMMM d"),               // day name, month + day
    F_MMMM_d_yyyy("%s", "MMMM d, yyyy"),               // month day, year
    F_d_MMMM_yyyy("%s","d MMMM yyyy"),                 // day month year
    F_MMMM_d_yyyy_G("%s", "MMMM d, yyyy G"),           // month day, year era
    F_yyyy_G("%s", "yyyy G"),                          // year + era
    ;

    protected String displayString, displayString0;
    @Nullable
    protected String pattern;

    private CalendarFormat(@NonNull String displayString, @Nullable String pattern) {
        this.displayString0 = this.displayString = displayString;
        this.pattern = pattern;
    }

    @Nullable
    public String getPattern() {
        return pattern;
    }
    public void setPattern( @Nullable String value ) {
        pattern = value;
    }

    @NonNull
    public String toString() {
        return displayString;
    }

    @NonNull
    public String getDisplayString() {
        return displayString != null ? displayString : "";
    }

    public void setDisplayString( @NonNull String displayString ) {
        this.displayString = displayString;
    }

    public void initDisplayString(@NonNull CalendarMode mode, @NonNull Calendar now, CalendarDisplay display)
    {
        if (isValidPattern(pattern)) {
            if (displayString0 == null) {
                displayString = display.formatDate(mode, pattern, now);
            } else if (displayString0.contains("%s")) {
                displayString = String.format(displayString0, display.formatDate(mode, pattern, now));
            } else displayString = displayString0;
        } else displayString = displayString0;
    }

    public static boolean isValidPattern(@Nullable String pattern) {
        return (pattern != null && !pattern.trim().isEmpty());
    }
}
