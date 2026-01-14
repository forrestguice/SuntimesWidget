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

public class AndroidResID_TimeDateDisplay implements TimeDateDisplay.ResID_TimeDateDisplay
{
    @Override
    public int resID_strTimeVeryShortFormat12() { return R.string.time_format_12hr_veryshort; }

    @Override
    public int resID_strTimeVeryShortFormat24() { return R.string.time_format_24hr_veryshort; }

    @Override
    public int resID_strTimeVeryShortFormat12s() { return R.string.time_format_12hr_veryshort_withseconds; }

    @Override
    public int resID_strTimeVeryShortFormat24s() { return R.string.time_format_24hr_veryshort_withseconds; }

    @Override
    public int resID_strTimeNone() { return R.string.time_none; }

    @Override
    public int resID_strTimeLoading() { return R.string.time_loading; }

    @Override
    public int resID_strDateYearFormat() { return R.string.dateyear_format_short; }

    @Override
    public int resID_strDateVeryShortFormat() { return R.string.date_format_veryshort; }

    @Override
    public int resID_strDateShortFormat() { return R.string.date_format_short; }

    @Override
    public int resID_strDateLongFormat() { return R.string.date_format_long; }

    @Override
    public int resID_strTimeShortFormat12() { return R.string.time_format_12hr_short; }


}
