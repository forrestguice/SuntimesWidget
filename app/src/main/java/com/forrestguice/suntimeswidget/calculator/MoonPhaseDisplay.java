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
 * MoonPhaseDisplay
 */
public enum MoonPhaseDisplay
{
    NEW("New", "New Moon", R.drawable.ic_moon_new, R.id.icon_info_moonphase_new),
    WAXING_CRESCENT("Waxing Crescent", "Waxing Crescent", R.drawable.ic_moon_waxing_crescent, R.id.icon_info_moonphase_waxing_crescent),
    FIRST_QUARTER("First Quarter", "First Quarter", R.drawable.ic_moon_waxing_quarter, R.id.icon_info_moonphase_waxing_quarter),
    WAXING_GIBBOUS("Waxing Gibbous", "Waxing Gibbous", R.drawable.ic_moon_waxing_gibbous, R.id.icon_info_moonphase_waxing_gibbous),
    FULL("Full", "Full Moon", R.drawable.ic_moon_full, R.id.icon_info_moonphase_full),
    WANING_GIBBOUS("Waning Gibbous", "Waning Gibbous", R.drawable.ic_moon_waning_gibbous, R.id.icon_info_moonphase_waning_gibbous),
    THIRD_QUARTER("Third Quarter", "Third Quarter", R.drawable.ic_moon_waning_quarter, R.id.icon_info_moonphase_waning_quarter),
    WANING_CRESCENT("Waning Crescent", "Waning Crescent", R.drawable.ic_moon_waning_crescent, R.id.icon_info_moonphase_waning_crescent);

    private int iconResource, viewResource;
    private String shortDisplayString, longDisplayString;

    private MoonPhaseDisplay(String shortDisplayString, String longDisplayString, int iconResource, int viewResource)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
        this.iconResource = iconResource;
        this.viewResource = viewResource;
    }

    public String toString()
    {
        return longDisplayString;
    }

    public int getIcon()
    {
        return iconResource;
    }

    public int getView()
    {
        return viewResource;
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
         WANING_CRESCENT.setDisplayString(context.getString(R.string.timeMode_moon_waningcrescent_short),context.getString(R.string.timeMode_moon_waningcrescent));
    }
}
