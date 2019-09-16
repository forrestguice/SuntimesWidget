/**
    Copyright (C) 2019 Forrest Guice
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

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import java.util.Calendar;
import java.util.HashMap;

/**
 * An alternate version of SuntimesMoonData that only calculates major phases.
 */
public class SuntimesMoonData1 extends SuntimesMoonData0
{
    public SuntimesMoonData1(Context context, int appWidgetId) {
        super(context, appWidgetId);
    }
    public SuntimesMoonData1(Context context, int appWidgetId, String calculatorName) {
        super(context, appWidgetId, calculatorName);
    }
    public SuntimesMoonData1(SuntimesMoonData1 other) {
        super(other);
        initFromOther(other);
    }

    /**
     * result: major phases (date of next: new moon, first quarter, full moon, third quarter)
     */
    private HashMap<SuntimesCalculator.MoonPhase, Calendar> moonPhases = new HashMap<>(4);
    public Calendar moonPhaseCalendar(SuntimesCalculator.MoonPhase phase)
    {
        if (moonPhases.containsKey(phase)) {
            return moonPhases.get(phase);
        }
        return null;
    }

    /**
     * init from other SuntimesEquinoxSolsticeData object
     * @param other another SuntimesEquinoxSolsticeData obj
     */
    private void initFromOther( SuntimesMoonData1 other )
    {
        super.initFromOther(other);
        this.moonPhases = new HashMap<>(other.moonPhases);
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
            todaysCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
            otherCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
        }
        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        Calendar after = (Calendar)todaysCalendar.clone();
        for (SuntimesCalculator.MoonPhase phase : SuntimesCalculator.MoonPhase.values()) {
            moonPhases.put(phase, calculator.getMoonPhaseNextDate(phase, after));
        }

        super.calculate();
    }

    /**
     * Find the next major phase from date; calculate() needs to be called first.
     * @param calendar a date/time
     * @return the next major phase occurring after the supplied date/time
     */
    public SuntimesCalculator.MoonPhase nextPhase(Calendar calendar) {
        return SuntimesMoonData0.nextPhase(moonPhases, calendar);
    }

}


