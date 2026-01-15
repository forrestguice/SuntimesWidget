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

public class AndroidResID_CalendarModeDisplay implements CalendarModeDisplay.ResID_CalendarModeDisplay
{
    @Override
    public int string_displayString(CalendarMode mode)
    {
        if (mode != null) {
            switch (mode)
            {
                case CHINESE: return R.string.calendarMode_chinese;
                case COPTIC: return R.string.calendarMode_coptic;
                case ETHIOPIAN: return R.string.calendarMode_ethiopian;
                case GREGORIAN: return R.string.calendarMode_gregorian;
                case HEBREW: return R.string.calendarMode_hebrew;
                case HIJRI_UMALQURA: return R.string.calendarMode_hijri_umalqura;
                case INDIAN: return R.string.calendarMode_indian;
                case JAPANESE: return R.string.calendarMode_japanese;
                case JULIAN: return R.string.calendarMode_julian;
                case KOREAN: return R.string.calendarMode_korean;
                case MINGUO: return R.string.calendarMode_minguo;
                case PERSIAN: return R.string.calendarMode_persian;
                case THAISOLAR: return R.string.calendarMode_thaisolar;
                case VIETNAMESE: return R.string.calendarMode_vietnamese;
            }
        }
        return 0;
    }
}
