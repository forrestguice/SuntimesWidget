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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.util.Resources;

public class CalendarModeDisplay
{
    public static void initDisplayStrings( Resources context )
    {
        CalendarMode.CHINESE.setDisplayString(context.getString(R.string.calendarMode_chinese));
        CalendarMode.COPTIC.setDisplayString(context.getString(R.string.calendarMode_coptic));
        CalendarMode.ETHIOPIAN.setDisplayString(context.getString(R.string.calendarMode_ethiopian));
        CalendarMode.GREGORIAN.setDisplayString(context.getString(R.string.calendarMode_gregorian));
        CalendarMode.HEBREW.setDisplayString(context.getString(R.string.calendarMode_hebrew));
        //CalendarMode.HIJRI_DIYANET.setDisplayString(context.getString(R.string.calendarMode_hijri_diyanet));
        CalendarMode.HIJRI_UMALQURA.setDisplayString(context.getString(R.string.calendarMode_hijri_umalqura));
        CalendarMode.INDIAN.setDisplayString(context.getString(R.string.calendarMode_indian));
        CalendarMode.JAPANESE.setDisplayString(context.getString(R.string.calendarMode_japanese));
        CalendarMode.JULIAN.setDisplayString(context.getString(R.string.calendarMode_julian));
        CalendarMode.KOREAN.setDisplayString(context.getString(R.string.calendarMode_korean));
        CalendarMode.MINGUO.setDisplayString(context.getString(R.string.calendarMode_minguo));
        CalendarMode.PERSIAN.setDisplayString(context.getString(R.string.calendarMode_persian));
        CalendarMode.THAISOLAR.setDisplayString(context.getString(R.string.calendarMode_thaisolar));
        CalendarMode.VIETNAMESE.setDisplayString(context.getString(R.string.calendarMode_vietnamese));
    }
}
