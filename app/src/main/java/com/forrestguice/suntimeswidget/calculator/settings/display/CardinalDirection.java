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
import com.forrestguice.util.Resources;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.R;

public enum CardinalDirection
{
    NORTH(1,      "N",   "North"              , 0.0),
    NORTH_NE(2,   "NNE", "North North East"   , 22.5),
    NORTH_E(3,    "NE",  "North East"         , 45.0),

    EAST_NE(4,    "ENE", "East North East"    , 67.5),
    EAST(5,       "E",   "East"               , 90.0),
    EAST_SE(6,    "ESE", "East South East"    , 112.5),

    SOUTH_E(7,    "SE",  "South East"         , 135.0),
    SOUTH_SE(8,   "SSE", "South South East"   , 157.5),
    SOUTH(9,      "S",   "South"              , 180.0),
    SOUTH_SW(10,  "SSW", "South South West"   , 202.5),
    SOUTH_W(11,   "SW",  "South West"         , 225.0),

    WEST_SW(12,   "WSW", "West South West"    , 247.5),
    WEST(13,      "W",   "West"               , 270.0),
    WEST_NW(14,   "WNW", "West North West"    , 292.5),

    NORTH_W(15,   "NW",  "North West"         , 315.0),
    NORTH_NW(16,  "NNW", "North North West"   , 337.5),
    NORTH2(1,     "N",   "North"              , 360.0);

    private final int pointNum;
    private String shortDisplayString;
    private String longDisplayString;
    private final double degrees;

    private CardinalDirection(int pointNum, @NonNull String shortDisplayString, @NonNull String longDisplayString, double degrees)
    {
        this.pointNum = pointNum;
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
        this.degrees = degrees;
    }

    public static CardinalDirection getDirection(double degrees)
    {
        if (degrees > 360)
            degrees = degrees % 360;

        while (degrees < 0)
            degrees += 360;

        CardinalDirection result = NORTH;
        double least = Double.MAX_VALUE;
        for (CardinalDirection direction : values())
        {
            double directionDegrees = direction.getDegress();
            double diff = Math.abs(directionDegrees - degrees);
            if (diff < least)
            {
                least = diff;
                result = direction;
            }
        }
        return result;
    }

    @NonNull
    public String toString() {
        return shortDisplayString;
    }

    public double getDegress()
    {
        return degrees;
    }

    public int getPoint()
    {
        return pointNum;
    }

    @NonNull
    public String getShortDisplayString()
    {
        return shortDisplayString;
    }

    @NonNull
    public String getLongDisplayString()
    {
        return longDisplayString;
    }

    public void setDisplayStrings(@NonNull String shortDisplayString, @NonNull String longDisplayString)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }

    public static void initDisplayStrings( Resources res )
    {
        String[] modes_short = res.getStringArray(R.array.directions_short);
        String[] modes_long = res.getStringArray(R.array.directions_long);
        if (modes_long.length != modes_short.length)
        {
            Log.e("initDisplayStrings", "The size of directions_short and solarevents_long DOES NOT MATCH!");
            return;
        }

        CardinalDirection[] values = values();
        if (modes_long.length != values.length)
        {
            Log.e("initDisplayStrings", "The size of directions_long and SolarEvents DOES NOT MATCH!");
            return;
        }

        for (int i = 0; i < values.length; i++)
        {
            values[i].setDisplayStrings(modes_short[i], modes_long[i]);
        }
    }
}
