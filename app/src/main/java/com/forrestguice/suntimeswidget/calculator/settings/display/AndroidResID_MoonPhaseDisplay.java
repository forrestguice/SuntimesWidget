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

public class AndroidResID_MoonPhaseDisplay implements MoonPhaseDisplay.ResID_MoonPhaseDisplay
{
    @Override
    public int string_shortDisplay(MoonPhaseDisplay value)
    {
        if (value != null)
        {
            switch (value)
            {
                case NEW: return R.string.timeMode_moon_new_short;
                case WAXING_CRESCENT: return R.string.timeMode_moon_waxingcrescent_short;
                case FIRST_QUARTER: return R.string.timeMode_moon_firstquarter_short;
                case WAXING_GIBBOUS: return R.string.timeMode_moon_waxinggibbous_short;
                case FULL: return R.string.timeMode_moon_full_short;
                case WANING_GIBBOUS: return R.string.timeMode_moon_waninggibbous_short;
                case THIRD_QUARTER: return R.string.timeMode_moon_thirdquarter_short;
                case WANING_CRESCENT: return R.string.timeMode_moon_waningcrescent_short;
            }
        }
        return 0;
    }

    @Override
    public int string_longDisplay(MoonPhaseDisplay value)
    {
        if (value != null)
        {
            switch (value)
            {
                case NEW: return R.string.timeMode_moon_new;
                case WAXING_CRESCENT: return R.string.timeMode_moon_waxingcrescent;
                case FIRST_QUARTER: return R.string.timeMode_moon_firstquarter;
                case WAXING_GIBBOUS: return R.string.timeMode_moon_waxinggibbous;
                case FULL: return R.string.timeMode_moon_full;
                case WANING_GIBBOUS: return R.string.timeMode_moon_waninggibbous;
                case THIRD_QUARTER: return R.string.timeMode_moon_thirdquarter;
                case WANING_CRESCENT: return R.string.timeMode_moon_waningcrescent;
            }
        }
        return 0;
    }

    @Override
    public int string_microFullMoon() {
        return R.string.timeMode_moon_microfull;
    }
    @Override
    public int string_microNewMoon() {
        return R.string.timeMode_moon_micronew;
    }
    @Override
    public int string_superFullMoon() {
        return R.string.timeMode_moon_superfull;
    }
    @Override
    public int string_superNewMoon() {
        return R.string.timeMode_moon_supernew;
    }

    @Override
    public int drawable_icon(MoonPhaseDisplay value)
    {
        if (value != null) {
            switch (value)
            {
                case NEW: return R.drawable.ic_moon_new;
                case WAXING_CRESCENT: return R.drawable.ic_moon_waxing_crescent;
                case FIRST_QUARTER: return R.drawable.ic_moon_waxing_quarter;
                case WAXING_GIBBOUS: return R.drawable.ic_moon_waxing_gibbous;
                case FULL: return R.drawable.ic_moon_full;
                case WANING_GIBBOUS: return R.drawable.ic_moon_waning_gibbous;
                case THIRD_QUARTER: return R.drawable.ic_moon_waning_quarter;
                case WANING_CRESCENT: return R.drawable.ic_moon_waning_crescent;
            }
        }
        return 0;
    }

    @Override
    public int id_view(MoonPhaseDisplay value)
    {
        if (value != null)
        {
            switch (value)
            {
                case NEW: return R.id.icon_info_moonphase_new;
                case WAXING_CRESCENT: return R.id.icon_info_moonphase_waxing_crescent;
                case FIRST_QUARTER: return R.id.icon_info_moonphase_waxing_quarter;
                case WAXING_GIBBOUS: return R.id.icon_info_moonphase_waxing_gibbous;
                case FULL: return R.id.icon_info_moonphase_full;
                case WANING_GIBBOUS: return R.id.icon_info_moonphase_waning_gibbous;
                case THIRD_QUARTER: return R.id.icon_info_moonphase_waning_quarter;
                case WANING_CRESCENT: return R.id.icon_info_moonphase_waning_crescent;
            }
        }
        return 0;
    }
}
