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

import com.forrestguice.util.Resources;
import com.forrestguice.util.text.TimeDisplayText;

import java.text.NumberFormat;

public class AngleDisplay
{
    protected static String strAltSymbol = "∠";
    protected static String strRaSymbol = "α";
    protected static String strDecSymbol = "δ";
    protected static String strDegreesFormat = "%1$s\u00B0";
    protected static String strDirectionFormat = "%1$s\u00A0%2$s";
    protected static String strElevationFormat = "%1$s%2$s";
    protected static String strDeclinationFormat = "%1$s %2$s";
    protected static String strRaFormat = "%1$s %2$s";

    public static void initDisplayStrings(Resources res, ResID_AngleDisplay resIDs1, ResID_CardinalDirection resIDs2)
    {
        strAltSymbol = res.getString(resIDs1.getResID_string_altSymbol());
        strRaSymbol = res.getString(resIDs1.getResID_string_strRaSymbol());
        strDecSymbol = res.getString(resIDs1.getResID_string_strDecSymbol());

        strDegreesFormat = res.getString(resIDs1.getResID_string_strDegreesFormat());
        strDirectionFormat = res.getString(resIDs1.getResID_string_strDirectionFormat());
        strElevationFormat = res.getString(resIDs1.getResID_string_strElevationFormat());
        strRaFormat = res.getString(resIDs1.getResID_string_strRaFormat());
        strDeclinationFormat = res.getString(resIDs1.getResID_string_strDeclinationFormat());

        CardinalDirection.initDisplayStrings(res, resIDs2);
    }

    public String formatAsDegrees(double value) {
        return String.format(strDegreesFormat, NumberFormat.getNumberInstance().format(value));
    }
    public String formatAsDegrees(double value, int places)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(places);
        formatter.setMaximumFractionDigits(places);
        return String.format(strDegreesFormat, formatter.format(value));
    }
    public String formatAsDirection(double degreeValue, int places)
    {
        String degreeString = formatAsDegrees(degreeValue, places);
        CardinalDirection direction = CardinalDirection.getDirection(degreeValue);
        return formatAsDirection(degreeString, direction.getShortDisplayString());
    }
    public String formatAsDirection(String degreeString, String directionString) {
        return String.format(strDirectionFormat, degreeString, directionString);
    }
    public TimeDisplayText formatAsDirection2(double degreeValue, int places, boolean longSuffix)
    {
        String degreeString = formatAsDegrees(degreeValue, places);
        CardinalDirection direction = CardinalDirection.getDirection(degreeValue);
        return new TimeDisplayText(degreeString, "", (longSuffix ? direction.getLongDisplayString() : direction.getShortDisplayString()));
    }

    public String formatAsElevation(String degreeString, String altitudeSymbol) {
        return String.format(strElevationFormat, degreeString, altitudeSymbol);
    }
    public TimeDisplayText formatAsElevation(double degreeValue, int places) {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strAltSymbol);
    }

    public String formatAsRightAscension(String degreeString, String raSymbol) {
        return String.format(strRaFormat, degreeString, raSymbol);
    }
    public TimeDisplayText formatAsRightAscension(double degreeValue, int places) {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strRaSymbol);
    }

    public String formatAsDeclination(String degreeString, String decSymbol) {
        return String.format(strDeclinationFormat, degreeString, decSymbol);
    }
    public TimeDisplayText formatAsDeclination(double degreeValue, int places) {
        return new TimeDisplayText(formatAsDegrees(degreeValue, places), "", strDecSymbol);
    }

}
