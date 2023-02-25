/**
    Copyright (C) 2018-2022 Forrest Guice
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
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class SuntimesMoonData extends SuntimesMoonData0
{
    private SuntimesCalculator.MoonTimes[] riseSet = new SuntimesCalculator.MoonTimes[3];  // [0] yesterday, [1] today, and [2] tomorrow

    public SuntimesMoonData(Context context, int appWidgetId) {
        super(context, appWidgetId);
    }
    public SuntimesMoonData(Context context, int appWidgetId, String calculatorName) {
        super(context, appWidgetId, calculatorName);
    }
    public SuntimesMoonData(SuntimesMoonData other) {
        super(other);
        initFromOther(other);
    }

    /**
     * Property: calendar ("other0")
     * 'other0' is yesterday's calendar, while 'other' is tomorrows.
     */
    protected Calendar otherCalendar0;
    public Calendar getOtherCalendar0() { return otherCalendar0; }

    /**
     * result: moonrise yesterday/today/tomorrow
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
     * result: moonset yesterday/today/tomorrow
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
     * result: illumination today (at lunar noon)
     */
    private double moonIlluminationToday;
    public double getMoonIlluminationToday()
    {
        return moonIlluminationToday;
    }
    private double moonIlluminationTomorrow;
    public double getMoonIlluminationTomorrow()
    {
        return moonIlluminationTomorrow;
    }
    public double getMoonIlluminationNow()
    {
        return (calculator == null ? -1 : calculator.getMoonIlluminationForDate( (todayIsNotToday() ? nowThen(calendar()) : now()) ));
    }
    public double getMoonIllumination(Calendar datetime) {
        return (calculator == null ? -1 : calculator.getMoonIlluminationForDate(datetime));
    }


    /**
     * result: moon transit time
     */
    private Calendar noonToday, noonTomorrow;
    public Calendar getLunarNoonToday() {
        return noonToday;
    }
    public Calendar getLunarNoonTomorrow() {
        return noonTomorrow;
    }

    private Calendar midnightToday, midnightTomorrow;
    public Calendar getLunarMidnightToday() {
        return midnightToday;
    }
    public Calendar getLunarMidnightTomorrow() {
        return midnightTomorrow;
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
     * result: phase tomorrow
     */
    private MoonPhaseDisplay moonPhaseTomorrow;
    public MoonPhaseDisplay getMoonPhaseTomorrow()
    {
        return moonPhaseTomorrow;
    }

    /**
     * result: next major phase
     */
    private SuntimesCalculator.MoonPhase moonPhaseNext;
    public SuntimesCalculator.MoonPhase getMoonPhaseNext()
    {
        return moonPhaseNext;
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

    public Calendar[] getRiseSetEvents()
    {
        Calendar midnight = midnight();
        midnight.add(Calendar.DAY_OF_MONTH, 1);
        return new Calendar[] { moonriseCalendarYesterday(), moonriseCalendarToday(), moonriseCalendarTomorrow(),
                                moonsetCalendarYesterday(), moonsetCalendarToday(), moonsetCalendarTomorrow(), midnight };
    }

    public Calendar[] getRiseSetEvents(String eventID)
    {
        if (SolarEvents.MOONRISE.name().equals(eventID)) {
            return new Calendar[] { moonriseCalendarYesterday(), moonriseCalendarToday(), moonriseCalendarTomorrow() };
        } else if (SolarEvents.MOONSET.name().equals(eventID)) {
            return new Calendar[] { moonsetCalendarYesterday(), moonsetCalendarToday(), moonsetCalendarTomorrow() };
        } else return new Calendar[] { null, null };
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
     * calculate
     */
    @Override
    public void calculate()
    {
        super.calculate();

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

        ArrayList<Calendar> midnights = findMidnight();
        if (midnights.size() > 0)
        {
            midnightToday = midnights.get(midnights.size() - 1);
            for (Calendar midnight : midnights)
            {
                if (midnight.get(Calendar.DAY_OF_YEAR) == todaysCalendar.get(Calendar.DAY_OF_YEAR)) {
                    midnightToday = midnight;
                }
                if (midnight.get(Calendar.DAY_OF_YEAR) == otherCalendar.get(Calendar.DAY_OF_YEAR)) {
                    midnightTomorrow = midnight;
                }
            }
        }
        if (midnightTomorrow == null && midnightToday != null)
        {
            midnightTomorrow = (Calendar)midnightToday.clone();
            midnightTomorrow.add(Calendar.DAY_OF_MONTH, 1);
            midnightTomorrow.add(Calendar.MINUTE, 50);   // approximate noon tomorrow
            //Log.d("DEBUG", "using approximate lunar noon tomorrow");
        }

        ArrayList<Calendar> noons = findNoon();
        if (noons.size() > 0)
        {
            noonToday = noons.get(noons.size() - 1);
            for (Calendar noon : noons)
            {
                if (noon.get(Calendar.DAY_OF_YEAR) == todaysCalendar.get(Calendar.DAY_OF_YEAR)) {
                    noonToday = noon;
                }
                if (noon.get(Calendar.DAY_OF_YEAR) == otherCalendar.get(Calendar.DAY_OF_YEAR)) {
                    noonTomorrow = noon;
                }
            }
        }
        if (noonTomorrow == null && noonToday != null)
        {
            noonTomorrow = (Calendar)noonToday.clone();
            noonTomorrow.add(Calendar.DAY_OF_MONTH, 1);
            noonTomorrow.add(Calendar.MINUTE, 50);   // approximate noon tomorrow
            //Log.d("DEBUG", "using approximate lunar noon tomorrow");
        }

        double moonIllumination = getMoonIllumination(noonToday, todaysCalendar);
        if (moonIllumination >= 0) {
            this.moonIlluminationToday = moonIllumination;
        }

        double moonIllumination1 = getMoonIllumination(noonTomorrow, otherCalendar);
        if (moonIllumination1 >= 0) {
            this.moonIlluminationTomorrow = moonIllumination1;
        }

        Calendar after = midnight();
        for (SuntimesCalculator.MoonPhase phase : SuntimesCalculator.MoonPhase.values()) {
            moonPhases.put(phase, calculator.getMoonPhaseNextDate(phase, after));
        }
        moonPhaseToday = findPhaseOf(after, true);

        Calendar midnight1 = (Calendar)after.clone();
        midnight1.add(Calendar.DAY_OF_MONTH, 1);
        moonPhaseTomorrow = findPhaseOf(midnight1);
    }

    private double getMoonIllumination(Calendar lunarNoon, Calendar today)
    {
        if (calculator != null)
        {
            if (lunarNoon != null) {
                return calculator.getMoonIlluminationForDate(lunarNoon);
            }
            Calendar solarNoon = calculator.getSolarNoonCalendarForDate(today);
            if (solarNoon != null) {
                return calculator.getMoonIlluminationForDate(solarNoon);
            }
        }
        return 0;
    }

    /**
     * Create a list of lunar noon times by examining the moonrise/moonset times from yesterday, today, and tomorrow.
     * @return an ArrayList of Calendar; contains up to 3 items (empty if not found).
     */
    private ArrayList<Calendar> findNoon()
    {
        ArrayList<Calendar> noon = new ArrayList<>();
        for (int i=0; i<riseSet.length; i++)  // for yesterday [0], today [1], and tomorrow [2]
        {
            if (riseSet[i] == null) {
                continue;
            }

            Calendar rise = riseSet[i].riseTime;
            if (rise != null)                          // check for moonrise..
            {
                Calendar set = riseSet[i].setTime;
                if (set != null && set.after(rise))    // check for moonset same day..
                {
                    noon.add(midpoint(rise, set));         // case0: moonrise / moonset same day

                } else if ((i+1) < riseSet.length) {
                    set = riseSet[i+1].setTime;
                    if (set != null)                  // check for moonset next day..
                    {
                        noon.add(midpoint(rise, set));     // case 1: moonrise / moonset straddles next day
                    }
                }
            }
        }
        return noon;
    }

    private ArrayList<Calendar> findMidnight()
    {
        ArrayList<Calendar> events = new ArrayList<>();
        for (int i=0; i<riseSet.length; i++)  // for yesterday [0], today [1], and tomorrow [2]
        {
            if (riseSet[i] == null) {
                continue;
            }

            Calendar set = riseSet[i].setTime;
            if (set != null)                          // check for moonset..
            {
                Calendar rise = riseSet[i].riseTime;
                if (rise != null && rise.after(set))    // check for moonrise same day..
                {
                    events.add(midpoint(set, rise));         // case0: moonset / moonrise same day

                } else if ((i+1) < riseSet.length) {
                    rise = riseSet[i+1].riseTime;
                    if (rise != null)                  // check for moonrise next day..
                    {
                        events.add(midpoint(set, rise));     // case 1: moonset / moonrise straddles next day
                    }
                }
            }
        }
        return events;
    }

    /**
     * Find the next major phase from date; calculate() needs to be called first.
     * @param calendar a date/time
     * @return the next major phase occurring after the supplied date/time
     */
    public SuntimesCalculator.MoonPhase nextPhase(Calendar calendar) {
        return nextPhase(moonPhases, calendar);
    }

    /**
     * Find the current major/minor phase by looking at the major phase; calculate() needs to be called first.
     * note: major phases are applied to the full calendar day, minor phases end the preceding day, and begin again the next.
     * @param calendar a date/time
     * @return a MoonPhaseDisplay enum
     */
    public MoonPhaseDisplay findPhaseOf(Calendar calendar) {
        return findPhaseOf(calendar, false);
    }

    /**
     * @param calendar a date/time
     * @param updateNext sideeffect; cache next major phase in results (@see getMoonPhaseNext)
     * @return a MoonPhaseDisplay enum
     */
    protected MoonPhaseDisplay findPhaseOf(Calendar calendar, boolean updateNext)
    {
        SuntimesCalculator.MoonPhase nextPhase = nextPhase(calendar);
        if (updateNext) {
            this.moonPhaseNext = nextPhase;
        }

        Calendar nextPhaseDate = moonPhases.get(nextPhase);
        boolean nextPhaseIsToday = (nextPhaseDate != null) &&
                                   (calendar.get(Calendar.YEAR) == nextPhaseDate.get(Calendar.YEAR)) &&
                                   (calendar.get(Calendar.DAY_OF_YEAR) == nextPhaseDate.get(Calendar.DAY_OF_YEAR));
        return (nextPhaseIsToday ? toPhase(nextPhase) : prevMinorPhase(nextPhase));
    }

    public CharSequence getMoonPhaseLabel(Context context, SuntimesCalculator.MoonPhase majorPhase) {
        return getMoonPhaseLabel(context, majorPhase, moonPhaseCalendar(majorPhase));
    }

}


