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
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import java.util.Calendar;

public class SuntimesMoonData extends SuntimesData
{
    private Context context;

    public SuntimesMoonData(Context context, int appWidgetId)
    {
        this.context = context;
        initFromSettings(context, appWidgetId);
    }
    public SuntimesMoonData(SuntimesMoonData other)
    {
        this.context = other.context;
        initFromOther(other);
    }

    /**
     * Property: compare mode
     */
    private WidgetSettings.CompareMode compareMode;
    public WidgetSettings.CompareMode compareMode()
    {
        return compareMode;
    }
    public void setCompareMode( WidgetSettings.CompareMode mode )
    {
        compareMode = mode;
    }

    /**
     * result: moonrise today
     */
    private Calendar moonriseCalendarToday;
    public Calendar moonriseCalendarToday()
    {
        return moonriseCalendarToday;
    }

    /**
     * result: moonset today
     */
    private Calendar moonsetCalendarToday;
    public Calendar moonsetCalendarToday()
    {
        return moonsetCalendarToday;
    }

    /**
     * result: illumination today
     */
    private double moonIlluminationToday;
    public double getMoonIlluminationToday()
    {
        return moonIlluminationToday;
    }

    /**
     * result: phase today
     */
    private MoonPhase moonPhaseToday;
    public MoonPhase getMoonPhaseToday()
    {
        return moonPhaseToday;
    }

    /**
     * result: moonrise other
     */
    private Calendar moonriseCalendarOther;
    public Calendar moonriseCalendarOther()
    {
        return moonriseCalendarOther;
    }

    /**
     * result: moonset other
     */
    private Calendar moonsetCalendarOther;
    public Calendar moonsetCalendarOther()
    {
        return moonsetCalendarOther;
    }

    /**
     * result: illumination other
     */
    private double moonIlluminationOther;
    public double getMoonIlluminationOther()
    {
        return moonIlluminationOther;
    }

    /**
     * result: phase other
     */
    private MoonPhase moonPhaseOther;
    public MoonPhase getMoonPhaseOther()
    {
        return moonPhaseOther;
    }

    /**
     * init from other SuntimesEquinoxSolsticeData object
     * @param other another SuntimesEquinoxSolsticeData obj
     */
    private void initFromOther( SuntimesMoonData other )
    {
        super.initFromOther(other);
        this.compareMode = other.compareMode();

        this.moonriseCalendarToday = other.moonriseCalendarToday;
        this.moonriseCalendarOther = other.moonriseCalendarOther;

        this.moonsetCalendarToday = other.moonsetCalendarToday;
        this.moonsetCalendarOther = other.moonsetCalendarOther;

        this.moonIlluminationToday = other.moonIlluminationToday;
        this.moonIlluminationOther = other.moonIlluminationOther;

        this.moonPhaseToday = other.moonPhaseToday;
        this.moonPhaseOther = other.moonPhaseOther;
    }

    /**
     * init from shared preferences
     * @param context
     * @param appWidgetId
     */
    @Override
    public void initFromSettings(Context context, int appWidgetId)
    {
        super.initFromSettings(context, appWidgetId);
        this.compareMode = WidgetSettings.loadCompareModePref(context, appWidgetId);
    }

    public void calculate()
    {
        SuntimesCalculatorFactory calculatorFactory = new SuntimesCalculatorFactory(context, calculatorMode);
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);  // TODO

        todaysCalendar = Calendar.getInstance(timezone);
        otherCalendar = Calendar.getInstance(timezone);

        if (todayIsNotToday())
        {
            todaysCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
            otherCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
        }

        switch (compareMode)
        {
            case YESTERDAY:
                otherCalendar.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case TOMORROW:
            default:
                otherCalendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
        }

        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        this.moonriseCalendarToday = todaysCalendar;  // TODO
        this.moonsetCalendarToday = todaysCalendar;  // TODO
        this.moonriseCalendarOther = otherCalendar;  // TODO
        this.moonsetCalendarOther = otherCalendar;  // TODO
        this.moonIlluminationToday = 0.5;  // TODO
        this.moonIlluminationOther = 1;  // TODO
        this.moonPhaseToday = MoonPhase.NEW;  // TODO
        this.moonPhaseOther = MoonPhase.WAXING_CRESCENT;  // TODO

        super.calculate();
    }

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

}


