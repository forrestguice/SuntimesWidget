/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator;

import android.content.Context;
import com.forrestguice.suntimeswidget.R;

/**
 * MoonPhase
 */
public enum MoonPhase
{
    NEW("New", "New Moon", R.drawable.ic_sunrise_large),         // TODO: icons
    WAXING_CRESCENT("Waxing Crescent", "Waxing Crescent", R.drawable.ic_sunrise_large),
    FIRST_QUARTER("First Quarter", "First Quarter", R.drawable.ic_sunrise_large),
    WAXING_GIBBOUS("Waxing Gibbous", "Waxing Gibbous", R.drawable.ic_sunrise_large),
    FULL("Full", "Full Moon", R.drawable.ic_noon_large),
    WANING_GIBBOUS("Waning Gibbous", "Waning Gibbous", R.drawable.ic_sunset_large),
    THIRD_QUARTER("Third Quarter", "Third Quarter", R.drawable.ic_sunset_large),
    WANING_CRESCENT("Waxing Crescent", "Waxing Crescent", R.drawable.ic_sunset_large);

    private int iconResource;
    private String shortDisplayString, longDisplayString;

    private MoonPhase(String shortDisplayString, String longDisplayString, int iconResource)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
        this.iconResource = iconResource;
    }

    public String toString()
    {
        return longDisplayString;
    }

    public int getIcon()
    {
        return iconResource;
    }

    public String getShortDisplayString()
    {
        return shortDisplayString;
    }

    public String getLongDisplayString()
    {
        return longDisplayString;
    }

    public void setDisplayString(String shortDisplayString, String longDisplayString)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }

    public static void initDisplayStrings(Context context)
    {
         NEW.setDisplayString(context.getString(R.string.timeMode_moon_new_short), context.getString(R.string.timeMode_moon_new));
         WAXING_CRESCENT.setDisplayString(context.getString(R.string.timeMode_moon_waxingcrescent_short), context.getString(R.string.timeMode_moon_waxingcrescent));
         FIRST_QUARTER.setDisplayString(context.getString(R.string.timeMode_moon_firstquarter_short), context.getString(R.string.timeMode_moon_firstquarter));
         WAXING_GIBBOUS.setDisplayString(context.getString(R.string.timeMode_moon_waxinggibbous_short), context.getString(R.string.timeMode_moon_waxinggibbous));
         FULL.setDisplayString(context.getString(R.string.timeMode_moon_full_short), context.getString(R.string.timeMode_moon_full));
         WANING_GIBBOUS.setDisplayString(context.getString(R.string.timeMode_moon_waninggibbous_short), context.getString(R.string.timeMode_moon_waninggibbous));
         THIRD_QUARTER.setDisplayString(context.getString(R.string.timeMode_moon_thirdquarter_short), context.getString(R.string.timeMode_moon_thirdquarter));
         WAXING_CRESCENT.setDisplayString(context.getString(R.string.timeMode_moon_waxingcrescent_short),context.getString(R.string.timeMode_moon_waxingcrescent));
    }
}
