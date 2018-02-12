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

import com.forrestguice.suntimeswidget.SuntimesUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class SuntimesMoonData extends SuntimesData
{
    private Context context;
    private SuntimesCalculator.MoonTimes[] riseSet = new SuntimesCalculator.MoonTimes[3];

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

    protected Calendar otherCalendar0;
    public Calendar getOtherCalendar0() { return otherCalendar0; }

    /**
     * result: moonrise today
     */
    public Calendar moonriseCalendarYesterday()
    {
        if (riseSet[0] != null)
            return riseSet[0].riseTime;
        else return null;
    }
    public Calendar moonriseCalendarToday()
    {
        if (riseSet[1] != null)
            return riseSet[1].riseTime;
        else return null;
    }
    public Calendar moonriseCalendarTomorrow()
    {
        if (riseSet[2] != null)
            return riseSet[2].riseTime;
        else return null;
    }

    /**
     * result: moonset today
     */
    public Calendar moonsetCalendarYesterday()
    {
        if (riseSet[0] != null)
            return riseSet[0].setTime;
        else return null;
    }
    public Calendar moonsetCalendarToday()
    {
        if (riseSet[1] != null)
            return riseSet[1].setTime;
        else return null;
    }
    public Calendar moonsetCalendarTomorrow()
    {
        if (riseSet[2] != null)
            return riseSet[2].setTime;
        else return null;
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
     * result: moon transit time
     */
    private Calendar noonToday;
    public Calendar getLunarNoonToday()
    {
        return noonToday;
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
     * init from other SuntimesEquinoxSolsticeData object
     * @param other another SuntimesEquinoxSolsticeData obj
     */
    private void initFromOther( SuntimesMoonData other )
    {
        super.initFromOther(other);
        this.riseSet = other.riseSet;
        this.moonIlluminationToday = other.moonIlluminationToday;
        this.moonPhases = new HashMap<>(other.moonPhases);
        this.moonPhaseToday = other.moonPhaseToday;
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
    }

    /**
     * calculate
     */
    public void calculate()
    {
        SuntimesCalculatorFactory calculatorFactory = new SuntimesCalculatorFactory(context, calculatorMode);
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);

        todaysCalendar = Calendar.getInstance(timezone);
        otherCalendar = Calendar.getInstance(timezone);
        otherCalendar0 = Calendar.getInstance(timezone);

        if (todayIsNotToday())
        {
            todaysCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
            otherCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
            otherCalendar0.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
        }

        otherCalendar0.add(Calendar.DAY_OF_MONTH, -1);   // yesterday
        otherCalendar.add(Calendar.DAY_OF_MONTH, 1);   // tomorrow

        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        riseSet[0] = calculator.getMoonTimesForDate(otherCalendar0);
        riseSet[1] = calculator.getMoonTimesForDate(todaysCalendar);
        riseSet[2] = calculator.getMoonTimesForDate(otherCalendar);

        ArrayList<Calendar> noons = findNoon();
        if (noons.size() >= 1)
        {
            noonToday = noons.get(noons.size() - 1);
            for (Calendar noon : noons)
            {
                if (noon.get(Calendar.DAY_OF_YEAR) == todaysCalendar.get(Calendar.DAY_OF_YEAR))
                {
                    noonToday = noon;
                    break;
                }
            }
        }
        //SuntimesUtils utils = new SuntimesUtils();
        //Log.d("DEBUG", "transit at " + utils.calendarDateTimeDisplayString(context, transitToday));

        double moonIllumination = (noonToday != null ? calculator.getMoonIlluminationForDate(noonToday) : 0);
        if (moonIllumination >= 0)
        {
            this.moonIlluminationToday = moonIllumination;
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

    /**
     * @return a list of lunar noon times created by examining the moonrise/moonset times from yesterday, today, and tomorrow.
     */
    private ArrayList<Calendar> findNoon()
    {
        ArrayList<Calendar> noon = new ArrayList<>();
        for (int i=0; i<riseSet.length-1; i++)
        {
            Calendar rise = riseSet[i].riseTime;
            if (rise != null)
            {
                Calendar set = riseSet[i].setTime;
                if (set != null && set.after(rise))
                {
                    noon.add(midpoint(rise, set));

                } else {
                    set = riseSet[i+1].setTime;
                    if (set != null)
                    {
                        noon.add(midpoint(rise, set));
                    }
                }
            }
        }
        return noon;
    }

    /**
     * @param c1 start time
     * @param c2 end time
     * @return the midpoint between start and end.
     */
    private Calendar midpoint(Calendar c1, Calendar c2)
    {
        int midpoint = (int)((c2.getTimeInMillis() - c1.getTimeInMillis()) / 2);
        Calendar retValue = (Calendar)c1.clone();
        retValue.add(Calendar.MILLISECOND, midpoint);
        return retValue;
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
        return (nextPhaseIsToday ? toPhase(nextPhase) : prevMinorPhase(nextPhase));
    }

    public static MoonPhaseDisplay toPhase( SuntimesCalculator.MoonPhase input )
    {
        switch (input) {
            case NEW: return MoonPhaseDisplay.NEW;
            case FIRST_QUARTER: return MoonPhaseDisplay.FIRST_QUARTER;
            case THIRD_QUARTER: return MoonPhaseDisplay.THIRD_QUARTER;
            case FULL:
            default: return MoonPhaseDisplay.FULL;
        }
    }

    public static MoonPhaseDisplay prevMinorPhase(SuntimesCalculator.MoonPhase input)
    {
        switch (input)
        {
            case NEW: return MoonPhaseDisplay.WANING_CRESCENT;
            case FIRST_QUARTER: return MoonPhaseDisplay.WAXING_CRESCENT;
            case THIRD_QUARTER: return MoonPhaseDisplay.WANING_GIBBOUS;
            case FULL:
            default: return MoonPhaseDisplay.WAXING_GIBBOUS;
        }
    }

    public static MoonPhaseDisplay nextMinorPhase(SuntimesCalculator.MoonPhase input)
    {
        switch (input)
        {
            case NEW: return MoonPhaseDisplay.WAXING_CRESCENT;
            case FIRST_QUARTER: return MoonPhaseDisplay.WAXING_GIBBOUS;
            case THIRD_QUARTER: return MoonPhaseDisplay.WANING_CRESCENT;
            case FULL:
            default: return MoonPhaseDisplay.WANING_GIBBOUS;
        }
    }

}


