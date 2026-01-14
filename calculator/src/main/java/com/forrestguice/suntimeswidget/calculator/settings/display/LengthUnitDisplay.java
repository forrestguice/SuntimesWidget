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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.util.Resources;
import com.forrestguice.util.text.TimeDisplayText;

import java.text.NumberFormat;

public class LengthUnitDisplay
{
    public static void initDisplayStrings_LengthUnit(Resources context, @NonNull ResID_LengthUnitDisplay ids)
    {
        resIDs = ids;
        for (LengthUnit unit : LengthUnit.values()) {
            unit.setDisplayString(context.getString(ids.resID_displayString(unit)));
        }
    }
    protected static ResID_LengthUnitDisplay resIDs = new ResID_LengthUnitDisplay()
    {
        public int resID_displayString(LengthUnit unit) { return 0; }
        public int resID_string_feet_short() { return 0; }
        public int resID_string_meters_short() { return 0; }
        public int resID_string_miles_short() { return 0; }
        public int resID_string_kilometers_short() { return 0; }
        public int resID_string_miles_long() { return 0; }
        public int resID_string_kilometers_long() { return 0; }
        public int resID_plurals_feet_long() { return 0; }
        public int resID_plurals_meters_long() { return 0; }
    };

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
                unitsString = (shortForm ? context.getString(resIDs.resID_string_feet_short())
                        : context.getQuantityString(resIDs.resID_plurals_feet_long(), (int)value, formatted));
                break;

            case METRIC:
            default:
                value = meters;
                formatted = formatter.format(value);
                unitsString = (shortForm ? context.getString(resIDs.resID_string_meters_short())
                        : context.getQuantityString(resIDs.resID_plurals_meters_long(), (int)value, formatted));
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
                unitsString = (shortForm ? context.getString(resIDs.resID_string_miles_short()) : context.getString(resIDs.resID_string_miles_long()));
                break;

            case METRIC:
            default:
                value = kilometers;
                unitsString = (shortForm ? context.getString(resIDs.resID_string_kilometers_short()) : context.getString(resIDs.resID_string_kilometers_long()));
                break;
        }

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        return new TimeDisplayText(formatter.format(value), unitsString, "");
    }

    public interface ResID_LengthUnitDisplay
    {
        int resID_displayString(LengthUnit unit);

        int resID_string_feet_short();          // R.string.units_feet_short
        int resID_string_meters_short();        // R.string.units_meters_short

        int resID_string_miles_short();         // R.string.units_miles_short
        int resID_string_kilometers_short();    // R.string.units_kilometers_short

        int resID_string_miles_long();          // R.string.units_miles
        int resID_string_kilometers_long();     // R.string.units_kilometers

        int resID_plurals_feet_long();
        int resID_plurals_meters_long();
    }
}
