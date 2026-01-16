/**
    Copyright (C) 2026 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.settings.display;

import com.forrestguice.suntimeswidget.R;

public class AndroidResID_TimeDeltaDisplay implements TimeDeltaDisplay.ResID_TimeDeltaDisplay
{
    @Override
    public int string_strTimeShorter() { return R.string.delta_day_shorter; }

    @Override
    public int string_strTimeLonger() { return R.string.delta_day_longer; }

    @Override
    public int string_strTimeSame() { return R.string.delta_day_same; }

    @Override
    public int string_strYears() { return R.string.delta_years; }

    @Override
    public int string_strWeeks() { return R.string.delta_weeks; }

    @Override
    public int string_strDays() { return R.string.delta_days; }

    @Override
    public int string_strHours() { return R.string.delta_hours; }

    @Override
    public int string_strMinutes() { return R.string.delta_minutes; }

    @Override
    public int string_strSeconds() { return R.string.delta_seconds; }
    
    @Override
    public int string_strTimeDeltaFormat() { return R.string.delta_format; }
}
