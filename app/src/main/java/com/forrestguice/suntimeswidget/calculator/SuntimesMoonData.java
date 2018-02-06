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
import android.util.Log;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import java.util.Calendar;
import java.util.HashMap;

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
    private MoonPhaseDisplay moonPhaseToday;
    public MoonPhaseDisplay getMoonPhaseToday()
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
     * result: major phases (date of next: new moon, first quarter, full moon, third quarter)
     */
    private HashMap<SuntimesCalculator.MoonPhase, Calendar> moonPhases = new HashMap<>(4);
    public Calendar moonPhaseCalendar(SuntimesCalculator.MoonPhase phase)
    {
        if (moonPhases.containsKey(phase))
        {
            return moonPhases.get(phase);
        }
        return null;
    }

    /**
     * result: phase other
     */
    //private MoonPhaseDisplay moonPhaseOther;
    //public MoonPhaseDisplay getMoonPhaseOther()
    //{
        //return moonPhaseOther;
    //}

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

        this.moonPhases = new HashMap<>(other.moonPhases);

        this.moonPhaseToday = other.moonPhaseToday;
        //this.moonPhaseOther = other.moonPhaseOther;
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
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);

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

        SuntimesCalculator.MoonTimes moonTimes0 = calculator.getMoonTimesForDate(todaysCalendar);
        if (moonTimes0 != null)
        {
            this.moonriseCalendarToday = moonTimes0.riseTime;
            this.moonsetCalendarToday = moonTimes0.setTime;
        }

        double moonIllumination0 = calculator.getMoonIlluminationForDate(todaysCalendar);
        if (moonIllumination0 >= 0)
        {
            this.moonIlluminationToday = moonIllumination0;
        }

        SuntimesCalculator.MoonTimes moonTimes1 = calculator.getMoonTimesForDate(otherCalendar);
        if (moonTimes1 != null)
        {
            this.moonriseCalendarOther = moonTimes1.riseTime;
            this.moonsetCalendarOther = moonTimes1.setTime;
        }

        double moonIllumination1 = calculator.getMoonIlluminationForDate(otherCalendar);
        if (moonIllumination1 >= 0)
        {
            this.moonIlluminationOther = moonIllumination1;
        }

        Calendar startOfDay = (Calendar)todaysCalendar.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);

        for (SuntimesCalculator.MoonPhase phase : SuntimesCalculator.MoonPhase.values())
        {
            moonPhases.put(phase, calculator.getMoonPhaseNextDate(phase, startOfDay));
        }
        moonPhaseToday = findPhaseOf(startOfDay);

        super.calculate();
    }

    public SuntimesCalculator.MoonPhase nextPhase(Calendar calendar)
    {
        SuntimesCalculator.MoonPhase result = SuntimesCalculator.MoonPhase.FULL;
        long date = calendar.getTimeInMillis();

        long least = Long.MAX_VALUE;
        for (SuntimesCalculator.MoonPhase phase : moonPhases.keySet())
        {
            Calendar phaseDate = moonPhases.get(phase);
            long delta = phaseDate.getTimeInMillis() - date;
            if (delta >= 0 && delta < least)
            {
                least = delta;
                result = phase;
            }
        }
        return result;
    }

    public MoonPhaseDisplay findPhaseOf(Calendar calendar)
    {
        SuntimesCalculator.MoonPhase nextPhase = nextPhase(calendar);
        Calendar nextPhaseDate = moonPhases.get(nextPhase);
        boolean nextPhaseIsToday = (calendar.get(Calendar.YEAR) == nextPhaseDate.get(Calendar.YEAR)) &&
                                   (calendar.get(Calendar.DAY_OF_YEAR) == nextPhaseDate.get(Calendar.DAY_OF_YEAR));
        if (nextPhaseIsToday)
        {
            switch (nextPhase)             // nextPhase is today (report major phase)
            {
                case NEW: return MoonPhaseDisplay.NEW;
                case FIRST_QUARTER: return MoonPhaseDisplay.FIRST_QUARTER;
                case THIRD_QUARTER: return MoonPhaseDisplay.THIRD_QUARTER;
                case FULL:
                default: return MoonPhaseDisplay.FULL;
            }

        } else {
            switch (nextPhase)             // next phase is distant (report leading minor phase)
            {
                case NEW: return MoonPhaseDisplay.WANING_CRESCENT;
                case FIRST_QUARTER: return MoonPhaseDisplay.WAXING_CRESCENT;
                case THIRD_QUARTER: return MoonPhaseDisplay.WANING_GIBBOUS;
                case FULL:
                default: return MoonPhaseDisplay.WAXING_GIBBOUS;
            }
        }
    }

}


