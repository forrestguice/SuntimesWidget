/**
    Copyright (C) 2014-2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.settings;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.util.Resources;

import java.util.Locale;

public enum SolarEvents
{
    MORNING_ASTRONOMICAL("astronomical twilight", "morning astronomical twilight", R.attr.sunriseIconLarge, 0, true), // 0
    MORNING_NAUTICAL("nautical twilight", "morning nautical twilight", R.attr.sunriseIconLarge, 0, true),             // 1
    MORNING_BLUE8("blue hour", "morning blue hour", R.attr.sunriseIconLarge, 0, true),                                // 2
    MORNING_CIVIL("civil twilight", "morning civil twilight", R.attr.sunriseIconLarge, 0, true),                      // 3
    MORNING_BLUE4("blue hour", "morning blue hour", R.attr.sunriseIconLarge, 0, true),                                // 4
    SUNRISE("sunrise", "sunrise", R.attr.sunriseIconLarge, 0, true),                                                  // 5
    MORNING_GOLDEN("golden hour", "morning golden hour", R.attr.sunriseIconLarge, 0, true),                           // 6
    NOON("solar noon", "solar noon", R.attr.sunnoonIcon, 0, false),                                                   // 7
    EVENING_GOLDEN("golden hour", "evening golden hour", R.attr.sunsetIconLarge, 0, false),                            // 8
    SUNSET("sunset", "sunset", R.attr.sunsetIconLarge, 0, false),                                                      // 9
    EVENING_BLUE4("blue hour", "evening blue hour", R.attr.sunsetIconLarge, 0, false),                                 // 10
    EVENING_CIVIL("civil twilight", "evening civil twilight", R.attr.sunsetIconLarge, 0, false),                       // 11
    EVENING_BLUE8("blue hour", "evening blue hour", R.attr.sunsetIconLarge, 0, false),                                 // 12
    EVENING_NAUTICAL("nautical twilight", "evening nautical twilight", R.attr.sunsetIconLarge, 0, false),              // 13
    EVENING_ASTRONOMICAL("astronomical twilight", "evening astronomical twilight", R.attr.sunsetIconLarge, 0, false),  // 14

    MOONRISE("moonrise", "moonrise", R.attr.moonriseIcon, 1, true),                                                 // 15
    MOONSET("moonset", "moonset", R.attr.moonsetIcon, 1, false),                                                   // 16

    NEWMOON("new moon", "new moon", R.attr.moonPhaseIcon0, 2, true),                                             // 17
    FIRSTQUARTER("first quarter", "first quarter", R.attr.moonPhaseIcon1, 2, true),                              // 18
    FULLMOON("full moon", "full moon", R.attr.moonPhaseIcon2, 2, false),                                         // 19
    THIRDQUARTER("third quarter", "third quarter", R.attr.moonPhaseIcon3, 2, false),                             // 20

    EQUINOX_SPRING("equinox", "spring equinox", R.attr.springColor, 3, true),                                         // 21
    SOLSTICE_SUMMER("solstice", "summer solstice", R.attr.summerColor, 3, false),                                     // 22
    EQUINOX_AUTUMNAL("equinox", "autumnal equinox", R.attr.fallColor, 3, false),                                      // 23
    SOLSTICE_WINTER("solstice", "winter solstice", R.attr.winterColor, 3, true),                                      // 24

    MOONNOON("lunar noon", "lunar noon", R.attr.moonnoonIcon, 1, true),                                            // 25
    MOONNIGHT("lunar midnight", "lunar midnight", R.attr.moonnightIcon, 1, false),                                  // 26

    CROSS_SPRING("cross-quarter", "spring cross-quarter", R.attr.springColor, 3, false),                          // 27
    CROSS_SUMMER("cross-quarter", "summer cross-quarter", R.attr.summerColor, 3, false),                         // 28
    CROSS_AUTUMNAL("cross-quarter", "autumnal cross-quarter", R.attr.fallColor, 3, false),                        // 29
    CROSS_WINTER("cross-quarter", "winter cross-quarter", R.attr.winterColor, 3, false),                          // 30

    MIDNIGHT("solar midnight", "solar midnight", R.attr.sunnightIcon, 0, true),                                        // 31

    ;                                                                                                    // .. R.array.solarevents_short/_long req same length/order

    private int iconResource;
    private String shortDisplayString, longDisplayString;
    public int type;
    public boolean rising;

    public static final int TYPE_SUN = 0;         // sunrise, sunset, twilight (converted using toTimeMode)
    public static final int TYPE_MOON = 1;        // moonrise, moonset, lunar noon, lunar midnight
    public static final int TYPE_MOONPHASE = 2;   // major phases (converted using toMoonPhase)
    public static final int TYPE_SEASON = 3;      // solstices & equinoxes (converted using toSolsticeEquinoxMode)

    private SolarEvents(String shortDisplayString, String longDisplayString, int iconResource, int type, boolean rising)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
        this.iconResource = iconResource;
        this.type = type;
        this.rising = rising;
    }

    public String toString()
    {
        return longDisplayString;
    }

    public int getIcon()
    {
        return iconResource;
    }

    public int getIcon(boolean southernHemisphere)
    {
        if (!southernHemisphere) {
            return iconResource;
        } else {
            switch (this) {
                case FIRSTQUARTER: return THIRDQUARTER.iconResource;
                case THIRDQUARTER: return FIRSTQUARTER.iconResource;
                case NEWMOON: case FULLMOON: default: return iconResource;
            }
        }
    }

    public int getType()
    {
        return type;
    }

    public boolean isRising()
    {
        return rising;
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

    public static void initDisplayStrings(Resources context)
    {
        String[] modes_short = context.getStringArray(R.array.solarevents_short);
        String[] modes_long = context.getStringArray(R.array.solarevents_long);
        if (modes_long.length != modes_short.length)
        {
            Log.e("initDisplayStrings", "The size of solarevents_short and solarevents_long DOES NOT MATCH! locale: " + Locale.getDefault().toString());
            return;
        }

        SolarEvents[] values = values();
        if (modes_long.length != values.length)
        {
            Log.e("initDisplayStrings", "The size of solarevents_long and SolarEvents DOES NOT MATCH! locale: " + Locale.getDefault().toString());
            return;
        }

        for (int i = 0; i < values.length; i++)
        {
            values[i].setDisplayString(modes_short[i], modes_long[i]);
        }

        MIDNIGHT.setDisplayString(context.getString(R.string.timeMode_midnight_short), context.getString(R.string.timeMode_midnight));
    }

    public static SolarEvents valueOf(String value, SolarEvents defaultType)
    {
        for (SolarEvents e : values()) {
            if (e.name().equals(value))
                return e;
        }
        return defaultType;
    }

    public static boolean hasValue(String eventID)
    {
        SolarEvents[] values = values();
        for (int i=0; i<values.length; i++) {
            if (values[i].name().equals(eventID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * SolarEventField
     */
    public static class SolarEventField
    {
        public SolarEvents event = SolarEvents.NOON;
        public Boolean tomorrow = false;

        public SolarEventField(SolarEvents event, boolean tomorrow)
        {
            this.event = event;
            this.tomorrow = tomorrow;
        }

        public boolean equals(Object obj)
        {
            if (!(obj instanceof SolarEventField))
            {
                return false;

            } else {
                SolarEventField that = (SolarEventField)obj;
                return (this.event.equals(that.event) && (this.tomorrow == that.tomorrow));
            }
        }

        public int hashCode()
        {
            int hash = this.event.hashCode();
            hash = hash * 37 + (tomorrow ? 0 : 1);
            return hash;
        }

        public String toString()
        {
            return event + " " + (tomorrow ? "tomorrow" : "today");
        }
    }

    public TimeMode toTimeMode()
    {
        return toTimeMode(this);
    }

    public SuntimesCalculator.MoonPhase toMoonPhase()
    {
        return toMoonPhase(this);
    }

    public SolsticeEquinoxMode toSolsticeEquinoxMode()
    {
        return toSolsticeEquinoxMode(this);
    }

    /**
     * toTimeMode
     * @param event SolarEvents enum
     * @return a TimeMode (or null if not applicable)
     */
    public static TimeMode toTimeMode(SolarEvents event ) {
        return toTimeMode(event.name());
    }
    public static TimeMode toTimeMode(String eventID )
    {
        if (MORNING_ASTRONOMICAL.name().equals(eventID) || EVENING_ASTRONOMICAL.name().equals(eventID))
            return TimeMode.ASTRONOMICAL;
        else if (MORNING_NAUTICAL.name().equals(eventID) || EVENING_NAUTICAL.name().equals(eventID))
            return TimeMode.NAUTICAL;
        else if (MORNING_BLUE8.name().equals(eventID) || EVENING_BLUE8.name().equals(eventID))
            return TimeMode.BLUE8;
        else if (MORNING_BLUE4.name().equals(eventID) || EVENING_BLUE4.name().equals(eventID))
            return TimeMode.BLUE4;
        else if (MORNING_CIVIL.name().equals(eventID) || EVENING_CIVIL.name().equals(eventID))
            return TimeMode.CIVIL;
        else if (MORNING_GOLDEN.name().equals(eventID) || EVENING_GOLDEN.name().equals(eventID))
            return TimeMode.GOLD;
        else if (NOON.name().equals(eventID))
            return TimeMode.NOON;
        else if (MIDNIGHT.name().equals(eventID))
            return TimeMode.MIDNIGHT;
        else if (SUNSET.name().equals(eventID) || SUNRISE.name().equals(eventID))
            return TimeMode.OFFICIAL;
        else return null;
    }
    public static SolarEvents valueOf(@Nullable TimeMode mode, boolean rising)
    {
        if (mode == null) {
            return null;
        }
        switch (mode) {
            case NOON: return SolarEvents.NOON;
            case MIDNIGHT: return SolarEvents.MIDNIGHT;
            case OFFICIAL: return (rising ? SolarEvents.SUNRISE : SolarEvents.SUNSET);
            case CIVIL: return (rising ? SolarEvents.MORNING_CIVIL : SolarEvents.EVENING_CIVIL);
            case NAUTICAL: return (rising ? SolarEvents.MORNING_NAUTICAL : SolarEvents.EVENING_NAUTICAL);
            case ASTRONOMICAL: return (rising ? SolarEvents.MORNING_ASTRONOMICAL : SolarEvents.EVENING_ASTRONOMICAL);
            case BLUE4: return (rising ? SolarEvents.MORNING_BLUE4 : SolarEvents.EVENING_BLUE4);
            case BLUE8: return (rising ? SolarEvents.MORNING_BLUE8 : SolarEvents.EVENING_BLUE8);
            case GOLD: return (rising ? SolarEvents.MORNING_GOLDEN : SolarEvents.EVENING_GOLDEN);
            default: return null;
        }
    }

    /**
     * toMoonPhaseMode
     * @param event SolarEvents enum
     * @return a MoonPhaseMode (or null if not applicable)
     */
    public static SuntimesCalculator.MoonPhase toMoonPhase(SolarEvents event)
    {
        switch (event)
        {
            case NEWMOON: return SuntimesCalculator.MoonPhase.NEW;
            case FIRSTQUARTER: return SuntimesCalculator.MoonPhase.FIRST_QUARTER;
            case FULLMOON: return SuntimesCalculator.MoonPhase.FULL;
            case THIRDQUARTER: return SuntimesCalculator.MoonPhase.THIRD_QUARTER;
        }
        return null;
    }
    public static SolarEvents valueOf(@Nullable SuntimesCalculator.MoonPhase phase)
    {
        if (phase == null) {
            return null;
        }
        switch (phase) {
            case NEW: return SolarEvents.NEWMOON;
            case FULL: return SolarEvents.FULLMOON;
            case FIRST_QUARTER: return SolarEvents.FIRSTQUARTER;
            case THIRD_QUARTER: return SolarEvents.THIRDQUARTER;
            default: return null;
        }
    }

    /**
     * toSolsticeEquinoxMode
     * @param event SolarEvents enum
     * @return a SolsticeEquinoxMode (or null if not applicable)
     */
    public static SolsticeEquinoxMode toSolsticeEquinoxMode(SolarEvents event)
    {
        switch (event)
        {
            case CROSS_SPRING: return SolsticeEquinoxMode.CROSS_SPRING;
            case CROSS_SUMMER: return SolsticeEquinoxMode.CROSS_SUMMER;
            case CROSS_AUTUMNAL: return SolsticeEquinoxMode.CROSS_AUTUMN;
            case CROSS_WINTER: return SolsticeEquinoxMode.CROSS_WINTER;
            case EQUINOX_SPRING: return SolsticeEquinoxMode.EQUINOX_SPRING;
            case SOLSTICE_SUMMER: return SolsticeEquinoxMode.SOLSTICE_SUMMER;
            case EQUINOX_AUTUMNAL: return SolsticeEquinoxMode.EQUINOX_AUTUMNAL;
            case SOLSTICE_WINTER: return SolsticeEquinoxMode.SOLSTICE_WINTER;
        }
        return null;
    }
    public static SolarEvents valueOf(@Nullable SolsticeEquinoxMode mode)
    {
        if (mode == null) {
            return null;
        }
        switch (mode) {
            case CROSS_SPRING: return SolarEvents.CROSS_SPRING;
            case CROSS_SUMMER: return SolarEvents.CROSS_SUMMER;
            case CROSS_AUTUMN: return SolarEvents.CROSS_AUTUMNAL;
            case CROSS_WINTER: return SolarEvents.CROSS_WINTER;
            case EQUINOX_SPRING: return SolarEvents.EQUINOX_SPRING;
            case SOLSTICE_SUMMER: return SolarEvents.SOLSTICE_SUMMER;
            case EQUINOX_AUTUMNAL: return SolarEvents.EQUINOX_AUTUMNAL;
            case SOLSTICE_WINTER: return SolarEvents.SOLSTICE_WINTER;
            default: return null;
        }
    }
}
