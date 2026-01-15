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

public class AndroidResID_CalendarFormatDisplay implements CalendarFormatDisplay.ResID_CalendarFormatDisplay
{
    @Override
    public int string_configLabel_general_calendarFormat_custom() { return R.string.configLabel_general_calendarFormat_custom; }
    @Override
    public int string_configLabel_general_calendarFormat_yyyy() { return R.string.configLabel_general_calendarFormat_yyyy; }
    @Override
    public int string_configLabel_general_calendarFormat_EEEE() { return R.string.configLabel_general_calendarFormat_EEEE; }
    @Override
    public int string_configLabel_general_calendarFormat_MMMM() { return R.string.configLabel_general_calendarFormat_MMMM; }
    @Override
    public int string_configLabel_general_calendarFormat_dd() { return R.string.configLabel_general_calendarFormat_dd; }
    @Override
    public int string_configLabel_general_calendarFormat_DD() { return R.string.configLabel_general_calendarFormat_DD; }
}
