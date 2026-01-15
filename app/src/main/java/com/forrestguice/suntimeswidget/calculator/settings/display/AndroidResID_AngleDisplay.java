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

public class AndroidResID_AngleDisplay implements AngleDisplay.ResID_AngleDisplay
{
    @Override
    public int string_strAltSymbol() {
        return R.string.widgetLabel_altitude_symbol;
    }
    @Override
    public int string_strRaSymbol() {
        return R.string.widgetLabel_rightAscension_symbol;
    }
    @Override
    public int string_strDecSymbol() {
        return R.string.widgetLabel_declination_symbol;
    }
    @Override
    public int string_strDegreesFormat() {
        return R.string.degrees_format;
    }
    @Override
    public int string_strDirectionFormat() {
        return R.string.direction_format;
    }
    @Override
    public int string_strElevationFormat() {
        return R.string.elevation_format;
    }
    @Override
    public int string_strRaFormat() {
        return R.string.rightascension_format;
    }
    @Override
    public int string_strDeclinationFormat() {
        return R.string.declination_format;
    }
}
