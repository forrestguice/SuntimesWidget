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
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;

public class AndroidResID_LengthUnitDisplay implements LengthUnitDisplay.ResID_LengthUnitDisplay
{
    @Override
    public int resID_displayString(LengthUnit unit)
    {
        if (unit != null) {
            switch (unit) {
                case IMPERIAL: return R.string.lengthUnits_imperial;
                case METRIC: default: return R.string.lengthUnits_metric;
            }
        }
        return 0;
    }

    @Override
    public int resID_string_feet_short() { return R.string.units_feet_short; }
    @Override
    public int resID_plurals_feet_long() {  return R.plurals.units_feet_long; }

    @Override
    public int resID_string_meters_short() { return R.string.units_meters_short; }
    @Override
    public int resID_plurals_meters_long() { return R.plurals.units_meters_long; }

    @Override
    public int resID_string_miles_short() { return R.string.units_miles_short; }
    @Override
    public int resID_string_miles_long() { return R.string.units_miles; }

    @Override
    public int resID_string_kilometers_short() { return R.string.units_kilometers_short; }
    @Override
    public int resID_string_kilometers_long() { return R.string.units_kilometers; }
}
