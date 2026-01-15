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
import com.forrestguice.util.Resources;

import java.util.Calendar;

import static com.forrestguice.suntimeswidget.calendar.CalendarFormat.*;

public class CalendarFormatDisplay
{
    public static void initDisplayStrings( Resources context, ResID_CalendarFormatDisplay r, CalendarDisplay display ) {
        initDisplayStrings(context, r, CalendarMode.GREGORIAN, Calendar.getInstance(), display);
    }
    public static void initDisplayStrings(Resources context, CalendarFormatDisplay.ResID_CalendarFormatDisplay r, @NonNull CalendarMode mode, @NonNull Calendar now, CalendarDisplay display )
    {
        CUSTOM.displayString0 = context.getString(r.string_configLabel_general_calendarFormat_custom());
        F_yyyy.displayString0 = F_yy.displayString0 = context.getString(r.string_configLabel_general_calendarFormat_yyyy());
        F_EEEE.displayString0 = F_EE.displayString0 = context.getString(r.string_configLabel_general_calendarFormat_EEEE());
        F_MMMM.displayString0 = F_MMM.displayString0 = F_MM.displayString0 = context.getString(r.string_configLabel_general_calendarFormat_MMMM());
        F_dd.displayString0 = context.getString(r.string_configLabel_general_calendarFormat_dd());
        F_DD.displayString0 = context.getString(r.string_configLabel_general_calendarFormat_DD());

        for (CalendarFormat value : CalendarFormat.values()) {
            value.initDisplayString(mode, now, display);
        }
    }

    public interface ResID_CalendarFormatDisplay
    {
        int string_configLabel_general_calendarFormat_custom();
        int string_configLabel_general_calendarFormat_yyyy();
        int string_configLabel_general_calendarFormat_EEEE();
        int string_configLabel_general_calendarFormat_MMMM();
        int string_configLabel_general_calendarFormat_dd();
        int string_configLabel_general_calendarFormat_DD();
    }
}
