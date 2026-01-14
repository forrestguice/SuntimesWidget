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

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;

import com.forrestguice.suntimeswidget.R;

public class AndroidResID_SolarEvents implements ResID_SolarEvents
{
    @Override
    public int getResID_string_typeLabel(@Nullable Integer type)
    {
        if (type == null) {
            return 0;
        }
        switch (type) {
            case SolarEvents.TYPE_SUN: return R.string.eventType_default_sun;
            case SolarEvents.TYPE_MOON: return R.string.eventType_default_moon;
            case SolarEvents.TYPE_MOONPHASE: return R.string.eventType_default_moonphases;
            case SolarEvents.TYPE_SEASON: return R.string.eventType_default_seasons;
            default: return type + R.string.eventType_default;
        }
    }

    @Override
    public int getResID_array_eventDisplayShort() {
        return R.array.solarevents_short;
    }
    @Override
    public int getResID_array_eventDisplayLong() {
        return R.array.solarevents_long;
    }

    @Override
    public int getResID_attr_icon(SolarEvents event)
    {
        switch (event)
        {
            case MORNING_ASTRONOMICAL: return R.attr.sunriseIconLarge;
            case MORNING_NAUTICAL: return R.attr.sunriseIconLarge;
            case MORNING_BLUE8: return R.attr.sunriseIconLarge;
            case MORNING_CIVIL: return R.attr.sunriseIconLarge;
            case MORNING_BLUE4: return R.attr.sunriseIconLarge;
            case SUNRISE: return R.attr.sunriseIconLarge;
            case MORNING_GOLDEN: return R.attr.sunriseIconLarge;
            case NOON: return R.attr.sunnoonIcon;
            case EVENING_GOLDEN: return R.attr.sunsetIconLarge;
            case SUNSET: return R.attr.sunsetIconLarge;
            case EVENING_BLUE4: return R.attr.sunsetIconLarge;
            case EVENING_CIVIL: return R.attr.sunsetIconLarge;
            case EVENING_BLUE8: return R.attr.sunsetIconLarge;
            case EVENING_NAUTICAL: return R.attr.sunsetIconLarge;
            case EVENING_ASTRONOMICAL: return R.attr.sunsetIconLarge;

            case MOONRISE: return R.attr.moonriseIcon;
            case MOONSET: return R.attr.moonsetIcon;

            case NEWMOON: return R.attr.moonPhaseIcon0;
            case FIRSTQUARTER: return R.attr.moonPhaseIcon1;
            case FULLMOON: return R.attr.moonPhaseIcon2;
            case THIRDQUARTER: return R.attr.moonPhaseIcon3;

            case EQUINOX_SPRING: case CROSS_SPRING: return R.attr.springColor;
            case SOLSTICE_SUMMER: case CROSS_SUMMER: return R.attr.summerColor;
            case EQUINOX_AUTUMNAL: case CROSS_AUTUMNAL: return R.attr.fallColor;
            case SOLSTICE_WINTER: case CROSS_WINTER: return R.attr.winterColor;

            case MOONNOON: return R.attr.moonnoonIcon;
            case MOONNIGHT: return R.attr.moonnightIcon;

            case MIDNIGHT: return R.attr.sunnightIcon;
            default: return 0;
        }
    }
}
