/**
    Copyright (C) 2025 Forrest Guice
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
import com.forrestguice.util.Resources;
import com.forrestguice.util.text.TimeDisplayText;

import java.text.NumberFormat;

public class LengthUnitDisplay
{
    public static void initDisplayStrings_LengthUnit(Resources context)
    {
        LengthUnit.METRIC.setDisplayString(context.getString(R.string.lengthUnits_metric));
        LengthUnit.IMPERIAL.setDisplayString(context.getString(R.string.lengthUnits_imperial));
    }

    /**
     * formatAsHeight
     */
    public static TimeDisplayText formatAsHeight(Resources context, double meters, LengthUnit units, int places, boolean shortForm)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        String formatted;

        double value;
        String unitsString;
        switch (units)
        {
            case IMPERIAL:
                value = LengthUnit.metersToFeet(meters);
                formatted = formatter.format(value);
                unitsString = (shortForm ? context.getString(R.string.units_feet_short)
                        : context.getQuantityString(R.plurals.units_feet_long, (int)value, formatted));
                break;

            case METRIC:
            default:
                value = meters;
                formatted = formatter.format(value);
                unitsString = (shortForm ? context.getString(R.string.units_meters_short)
                        : context.getQuantityString(R.plurals.units_meters_long, (int)value, formatted));
                break;
        }
        return new TimeDisplayText(formatted, unitsString, "");
    }

    /**
     * formatAsDistance
     */
    public static TimeDisplayText formatAsDistance(Resources context, double kilometers, LengthUnit units, int places, boolean shortForm)
    {
        double value;
        String unitsString;
        switch (units)
        {
            case IMPERIAL:
                value = LengthUnit.kilometersToMiles(kilometers);
                unitsString = (shortForm ? context.getString(R.string.units_miles_short) : context.getString(R.string.units_miles));
                break;

            case METRIC:
            default:
                value = kilometers;
                unitsString = (shortForm ? context.getString(R.string.units_kilometers_short) : context.getString(R.string.units_kilometers));
                break;
        }

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        return new TimeDisplayText(formatter.format(value), unitsString, "");
    }
}
