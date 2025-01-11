/**
    Copyright (C) 2018-2019 Forrest Guice
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
import com.forrestguice.support.annotation.NonNull;
import android.util.Pair;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import java.util.Calendar;
import java.util.HashMap;

public class SuntimesMoonData0 extends SuntimesData
{
    protected Context context;

    public SuntimesMoonData0(Context context, int appWidgetId)
    {
        this.context = context;
        initFromSettings(context, appWidgetId);
    }
    public SuntimesMoonData0(Context context, int appWidgetId, String calculatorName)
    {
        this.context = context;
        initFromSettings(context, appWidgetId, calculatorName);
    }
    public SuntimesMoonData0(SuntimesMoonData0 other)
    {
        this.context = other.context;
        initFromOther(other);
    }

    /**
     * @return true the calculator has features needed to calculate the data, false otherwise
     */
    public boolean isImplemented()
    {
        return calculatorMode.hasRequestedFeature(SuntimesCalculator.FEATURE_MOON);
    }

    @Override
    public SuntimesCalculatorFactory initFactory(Context context)
    {
        return new SuntimesCalculatorFactory(context, calculatorMode)
        {
            public SuntimesCalculator fallbackCalculator() {
                return new com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator();
            }
            public SuntimesCalculatorDescriptor fallbackCalculatorDescriptor() {
                return com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator.getDescriptor();
            }
        };
    }

    /**
     * @return the date and position (Pair) of the upcoming lunar apogee.
     */
    public Pair<Calendar, SuntimesCalculator.MoonPosition> getMoonApogee()
    {
        Calendar apogeeDate = calculator.getMoonApogeeNextDate(todaysCalendar);
        if (apogeeDate != null) {
            SuntimesCalculator.MoonPosition apogeePosition = calculator.getMoonPosition(apogeeDate);
            return new Pair<>(apogeeDate, apogeePosition);
        } else return null;
    }

    /**
     * @return the date and position (Pair) of the upcoming lunar perigee.
     */
    public Pair<Calendar, SuntimesCalculator.MoonPosition> getMoonPerigee()
    {
        Calendar perigeeDate = calculator.getMoonPerigeeNextDate(todaysCalendar);
        if (perigeeDate != null) {
            SuntimesCalculator.MoonPosition perigeePosition = calculator.getMoonPosition(perigeeDate);
            return new Pair<>(perigeeDate, perigeePosition);
        } else return null;
    }

    /**
     * calculate
     */
    @Override
    public void calculate()
    {
        initCalculator(context);
        initTimezone(context);

        todaysCalendar = Calendar.getInstance(timezone);
        otherCalendar = Calendar.getInstance(timezone);
        if (todayIsNotToday())
        {
            todaysCalendar.setTimeInMillis(todayIs.getTimeInMillis());
            otherCalendar.setTimeInMillis(todayIs.getTimeInMillis());
        }
        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        super.calculate();
    }

    public static boolean isSuperMoon( @NonNull SuntimesCalculator.MoonPosition position )
    {
        return position.distance < 360000;
    }

    public static boolean isMicroMoon( @NonNull SuntimesCalculator.MoonPosition position)
    {
        return position.distance > 405000;
    }
    /**
     *
     * @param c1 a start time
     * @param c2 an end time (with difference from start no greater than 48 days)
     * @return the midpoint between start and end.
     */
    public static Calendar midpoint(Calendar c1, Calendar c2)
    {
        int midpoint = (int)((c2.getTimeInMillis() - c1.getTimeInMillis()) / 2);   // int: capacity ~24 days
        Calendar retValue = (Calendar)c1.clone();
        retValue.add(Calendar.MILLISECOND, midpoint);
        return retValue;
    }

    /**
     * @param input major phase
     * @return corresponding MoonPhaseDisplay enum (direct map)
     */
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

    /**
     * @param input major phase
     * @return the minor phase comes before
     */
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

    /**
     * @param input major phase
     * @return the minor phase that comes after
     */
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

    /**
     * Find the next major phase from date.
     * @param moonPhases a HashMap containing major phases and their dates
     * @param calendar a date/time to compare against
     * @return the next major phase occurring after the supplied date/time
     */
    public SuntimesCalculator.MoonPhase nextPhase(HashMap<SuntimesCalculator.MoonPhase, Calendar> moonPhases, Calendar calendar)
    {
        SuntimesCalculator.MoonPhase result = SuntimesCalculator.MoonPhase.FULL;
        long date = calendar.getTimeInMillis();

        long least = Long.MAX_VALUE;
        for (SuntimesCalculator.MoonPhase phase : moonPhases.keySet())
        {
            Calendar phaseDate = moonPhases.get(phase);
            if (phaseDate != null)
            {
                long delta = phaseDate.getTimeInMillis() - date;
                if (delta >= 0 && delta < least)
                {
                    least = delta;
                    result = phase;
                }
            }
        }
        return result;
    }

    public CharSequence getMoonPhaseLabel(Context context, SuntimesCalculator.MoonPhase majorPhase, Calendar phaseDate)
    {
        if (majorPhase == SuntimesCalculator.MoonPhase.FULL || majorPhase == SuntimesCalculator.MoonPhase.NEW)
        {
            SuntimesCalculator.MoonPosition phasePosition = calculator.getMoonPosition(phaseDate);

            if (SuntimesMoonData.isSuperMoon(phasePosition)) {
                return (majorPhase == SuntimesCalculator.MoonPhase.NEW) ? context.getString(R.string.timeMode_moon_supernew)
                        : context.getString(R.string.timeMode_moon_superfull);

            } else if (SuntimesMoonData.isMicroMoon(phasePosition)) {
                return (majorPhase == SuntimesCalculator.MoonPhase.NEW) ? context.getString(R.string.timeMode_moon_micronew)
                        : context.getString(R.string.timeMode_moon_microfull);

            } else return SuntimesMoonData.toPhase(majorPhase).getLongDisplayString();
        } else return SuntimesMoonData.toPhase(majorPhase).getLongDisplayString();
    }

}