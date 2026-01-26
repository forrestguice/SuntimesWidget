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
    protected static String strFeetShort = "ft", strMetersShort = "m";
    protected static String strMilesShort = "mi", strMilesLong = "miles", strKilometersShort = "km", strKilometersLong = "kilometers";
    protected static int rPluralFeet = 0, rPluralMeters = 0;
    protected static String strDistanceFormat = "%1$s %2$s";

    public static void initDisplayStrings_LengthUnit(Resources context, @NonNull ResID_LengthUnitDisplay r)
    {
        strFeetShort = context.getString(r.string_feet_short());
        strMetersShort = context.getString(r.string_meters_short());

        strMilesShort = context.getString(r.string_miles_short());
        strMilesLong = context.getString(r.string_miles_long());

        strKilometersShort = context.getString(r.string_kilometers_short());
        strKilometersLong = context.getString(r.string_kilometers_long());

        rPluralFeet = r.plurals_feet_long();
        rPluralMeters = r.plurals_meters_long();

        strDistanceFormat = context.getString(r.string_distance_format());

        for (LengthUnit unit : LengthUnit.values()) {
            unit.setDisplayString(context.getString(r.string_displayString(unit)));
        }
    }

    /**
     * formatAsHeight
     */

    public static TimeDisplayText formatAsHeight(Resources context, double meters, LengthUnit units, int places, boolean shortForm) {
        return formatAsHeight(context, meters, LengthUnit.METRIC, units, places, shortForm);
    }
    public static TimeDisplayText formatAsHeight(Resources context, double value0, LengthUnit inUnits, LengthUnit toUnits, int places, boolean shortForm)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        String formatted;

        double value;
        String unitsString;
        switch (toUnits)
        {
            case IMPERIAL:
                value = (inUnits == LengthUnit.IMPERIAL ? value0 : LengthUnit.metersToFeet(value0));
                formatted = formatter.format(value);
                unitsString = ((shortForm || rPluralFeet == 0) ? strFeetShort
                        : context.getQuantityString(rPluralFeet, (int) Math.ceil(value), formatted)).replace(formatted + " ", "");
                break;

            case METRIC:
            default:
                value = (inUnits == LengthUnit.METRIC ? value0 : LengthUnit.feetToMeters(value0));
                formatted = formatter.format(value);
                unitsString = ((shortForm || rPluralMeters == 0) ? strMetersShort
                        : context.getQuantityString(rPluralMeters, (int) Math.ceil(value), formatted)).replace(formatted + " ", "");
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
                unitsString = (shortForm ? strMilesShort : strMilesLong);
                break;

            case METRIC:
            default:
                value = kilometers;
                unitsString = (shortForm ? strKilometersShort : strKilometersLong);
                break;
        }

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        return new TimeDisplayText(formatter.format(value), unitsString, "");
    }

    public static String formatAsDistance(Resources context, TimeDisplayText text) {
        return String.format(strDistanceFormat, text.getValue(), text.getUnits());
    }

    public interface ResID_LengthUnitDisplay
    {
        int string_displayString(LengthUnit unit);

        int string_feet_short();          // R.string.units_feet_short
        int string_meters_short();        // R.string.units_meters_short

        int string_miles_short();         // R.string.units_miles_short
        int string_kilometers_short();    // R.string.units_kilometers_short

        int string_miles_long();          // R.string.units_miles
        int string_kilometers_long();     // R.string.units_kilometers

        int plurals_feet_long();
        int plurals_meters_long();

        int string_distance_format();
    }
}
