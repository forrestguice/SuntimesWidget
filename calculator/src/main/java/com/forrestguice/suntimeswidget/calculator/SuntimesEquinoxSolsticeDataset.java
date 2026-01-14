/**
    Copyright (C) 2017-2022 Forrest Guice
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

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.settings.SolsticeEquinoxMode;

import java.util.Calendar;
import java.util.TimeZone;

public class SuntimesEquinoxSolsticeDataset
{
    public SuntimesEquinoxSolsticeData dataEquinoxSpring;
    public SuntimesEquinoxSolsticeData dataSolsticeSummer;
    public SuntimesEquinoxSolsticeData dataEquinoxAutumnal;
    public SuntimesEquinoxSolsticeData dataSolsticeWinter;
    public SuntimesEquinoxSolsticeData[] dataSolsticesEquinoxes;

    public SuntimesEquinoxSolsticeDataset(Object context)
    {
        dataEquinoxSpring = new SuntimesEquinoxSolsticeData(context, 0);
        dataEquinoxSpring.setTimeMode(SolsticeEquinoxMode.EQUINOX_SPRING);
        initSolsticeEquinoxData();
    }

    public SuntimesEquinoxSolsticeDataset(Object context, int appWidgetId)
    {
        dataEquinoxSpring = new SuntimesEquinoxSolsticeData(context, appWidgetId);
        dataEquinoxSpring.setTimeMode(SolsticeEquinoxMode.EQUINOX_SPRING);
        initSolsticeEquinoxData();
    }

    protected void initSolsticeEquinoxData()
    {
        dataSolsticeSummer = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataSolsticeSummer.setTimeMode(SolsticeEquinoxMode.SOLSTICE_SUMMER);

        dataEquinoxAutumnal = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataEquinoxAutumnal.setTimeMode(SolsticeEquinoxMode.EQUINOX_AUTUMNAL);

        dataSolsticeWinter = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataSolsticeWinter.setTimeMode(SolsticeEquinoxMode.SOLSTICE_WINTER);
        dataSolsticesEquinoxes = new SuntimesEquinoxSolsticeData[] { dataEquinoxSpring, dataSolsticeSummer, dataEquinoxAutumnal, dataSolsticeWinter };
    }

    public SuntimesEquinoxSolsticeDataset( SuntimesEquinoxSolsticeData dataEquinoxSpring,
                                           SuntimesEquinoxSolsticeData dataSolsticeSummer,
                                           SuntimesEquinoxSolsticeData dataEquinoxAutumnal,
                                           SuntimesEquinoxSolsticeData dataSolsticeWinter )
    {
        this.dataEquinoxSpring = dataEquinoxSpring;
        if (dataEquinoxSpring == null) {
            throw new NullPointerException("dataEquinoxSpring must not be null!");
        }

        this.dataSolsticeSummer = dataSolsticeSummer;
        if (dataSolsticeSummer == null) {
            throw new NullPointerException("dataSolsticeSummer must not be null!");
        }

        this.dataEquinoxAutumnal = dataEquinoxAutumnal;
        if (dataEquinoxAutumnal == null) {
            throw new NullPointerException("dataEquinoxAutumnal must not be null!");
        }

        this.dataSolsticeWinter = dataSolsticeWinter;
        if (dataSolsticeWinter == null) {
            throw new NullPointerException("dataSolsticeWinter must not be null!");
        }
        dataSolsticesEquinoxes = new SuntimesEquinoxSolsticeData[] { dataEquinoxSpring, dataSolsticeSummer, dataEquinoxAutumnal, dataSolsticeWinter };
    }

    public SuntimesCalculator calculator() {
        return dataEquinoxSpring.calculator();
    }

    public void calculateData(Object context)
    {
        dataEquinoxSpring.calculate(context);
        SuntimesCalculator calculator = dataEquinoxSpring.calculator();
        SuntimesCalculatorDescriptor descriptor = dataEquinoxSpring.calculatorMode();

        dataSolsticeSummer.setCalculator(calculator, descriptor);
        dataSolsticeSummer.calculate(context);

        dataEquinoxAutumnal.setCalculator(calculator, descriptor);
        dataEquinoxAutumnal.calculate(context);
        
        dataSolsticeWinter.setCalculator(calculator, descriptor);
        dataSolsticeWinter.calculate(context);
    }

    public SuntimesEquinoxSolsticeData findSoonest(Calendar now) {
        return findClosest(now, false, true);
    }

    public SuntimesEquinoxSolsticeData findClosest(Calendar now) {
        return findClosest(now, false, false);
    }

    public SuntimesEquinoxSolsticeData findRecent(Calendar now) {
        return findClosest(now, true, false);
    }

    protected SuntimesEquinoxSolsticeData findClosest(Calendar now, boolean recent, boolean upcoming)
    {
        long timeDeltaMin = Long.MAX_VALUE;
        SuntimesEquinoxSolsticeData closest = null;
        for (SuntimesEquinoxSolsticeData data : dataset())
        {
            Calendar[] events = {data.eventCalendarThisYear(), data.eventCalendarNextYear()};
            for (Calendar event : events)
            {
                if (event != null)
                {
                    if ((upcoming && !event.after(now)) || (recent && event.after(now)))
                        continue;

                    long timeDelta = Math.abs(event.getTimeInMillis() - now.getTimeInMillis());
                    if (timeDelta < timeDeltaMin)
                    {
                        timeDeltaMin = timeDelta;
                        closest = data;
                    }
                }
            }
        }
        return closest;
    }

    public SuntimesEquinoxSolsticeData[] dataset() {
        return new SuntimesEquinoxSolsticeData[] { dataEquinoxSpring, dataSolsticeSummer, dataEquinoxAutumnal, dataSolsticeWinter };
    }

    public void setLocation(Location location)
    {
        for (SuntimesEquinoxSolsticeData data : dataSolsticesEquinoxes) {
            data.setLocation(location);
        }
    }

    public boolean isCalculated() {
        return dataEquinoxSpring.isCalculated();
    }

    public void invalidateCalculation()
    {
        for (SuntimesEquinoxSolsticeData data : dataSolsticesEquinoxes) {
            data.invalidateCalculation();
        }
    }

    public boolean isImplemented() {
        return dataEquinoxSpring.isImplemented();
    }

    public void setTodayIs(Calendar date)
    {
        for (SuntimesEquinoxSolsticeData data : dataSolsticesEquinoxes) {
            data.setTodayIs(date);
        }
    }

    public Calendar todayIs() {
        return dataEquinoxSpring.todayIs();
    }

    public boolean todayIsNotToday() {
        return dataEquinoxSpring.todayIsNotToday();
    }

    public String timezone() {
        return dataEquinoxSpring.timezone().getID();
    }

    public Calendar now() {
        return Calendar.getInstance(TimeZone.getTimeZone(timezone()));
    }

    public long tropicalYearLength()
    {
        Location location = dataEquinoxSpring.location;
        double latitude = (location != null ? location.getLatitudeAsDouble() : 0);
        SuntimesEquinoxSolsticeData data = (latitude >= 0) ? dataEquinoxSpring : dataEquinoxAutumnal;
        return data.tropicalYearLength();
    }

}
