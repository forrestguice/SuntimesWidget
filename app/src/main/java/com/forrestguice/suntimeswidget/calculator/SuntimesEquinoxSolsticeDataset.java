/**
    Copyright (C) 2017-2019 Forrest Guice
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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.support.annotation.NonNull;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.TimeZone;

public class SuntimesEquinoxSolsticeDataset
{
    public SuntimesEquinoxSolsticeData dataEquinoxSpring;
    public SuntimesEquinoxSolsticeData dataSolsticeSummer;
    public SuntimesEquinoxSolsticeData dataEquinoxAutumnal;
    public SuntimesEquinoxSolsticeData dataSolsticeWinter;

    public SuntimesEquinoxSolsticeDataset(Context context)
    {
        dataEquinoxSpring = new SuntimesEquinoxSolsticeData(context, AppWidgetManager.INVALID_APPWIDGET_ID);
        dataEquinoxSpring.setTimeMode(WidgetSettings.SolsticeEquinoxMode.EQUINOX_SPRING);

        dataSolsticeSummer = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataSolsticeSummer.setTimeMode(WidgetSettings.SolsticeEquinoxMode.SOLSTICE_SUMMER);

        dataEquinoxAutumnal = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataEquinoxAutumnal.setTimeMode(WidgetSettings.SolsticeEquinoxMode.EQUINOX_AUTUMNAL);

        dataSolsticeWinter = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataSolsticeWinter.setTimeMode(WidgetSettings.SolsticeEquinoxMode.SOLSTICE_WINTER);
    }

    public SuntimesEquinoxSolsticeDataset(Context context, int appWidgetId)
    {
        dataEquinoxSpring = new SuntimesEquinoxSolsticeData(context, appWidgetId);
        dataEquinoxSpring.setTimeMode(WidgetSettings.SolsticeEquinoxMode.EQUINOX_SPRING);

        dataSolsticeSummer = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataSolsticeSummer.setTimeMode(WidgetSettings.SolsticeEquinoxMode.SOLSTICE_SUMMER);

        dataEquinoxAutumnal = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataEquinoxAutumnal.setTimeMode(WidgetSettings.SolsticeEquinoxMode.EQUINOX_AUTUMNAL);

        dataSolsticeWinter = new SuntimesEquinoxSolsticeData(dataEquinoxSpring);
        dataSolsticeWinter.setTimeMode(WidgetSettings.SolsticeEquinoxMode.SOLSTICE_WINTER);
    }

    public SuntimesEquinoxSolsticeDataset( SuntimesEquinoxSolsticeData dataEquinoxSpring,
                                           SuntimesEquinoxSolsticeData dataSolsticeSummer,
                                           SuntimesEquinoxSolsticeData dataEquinoxAutumnal,
                                           SuntimesEquinoxSolsticeData dataSolsticeWinter )
    {
        this.dataEquinoxSpring = dataEquinoxSpring;
        if (dataEquinoxSpring == null)
        {
            throw new NullPointerException("dataEquinoxSpring must not be null!");
        }

        this.dataSolsticeSummer = dataSolsticeSummer;
        if (dataSolsticeSummer == null)
        {
            throw new NullPointerException("dataSolsticeSummer must not be null!");
        }

        this.dataEquinoxAutumnal = dataEquinoxAutumnal;
        if (dataEquinoxAutumnal == null)
        {
            throw new NullPointerException("dataEquinoxAutumnal must not be null!");
        }

        this.dataSolsticeWinter = dataSolsticeWinter;
        if (dataSolsticeWinter == null)
        {
            throw new NullPointerException("dataSolsticeWinter must not be null!");
        }
    }

    public SuntimesCalculator calculator()
    {
        return dataEquinoxSpring.calculator();
    }

    public void calculateData()
    {
        dataEquinoxSpring.calculate();
        SuntimesCalculator calculator = dataEquinoxSpring.calculator();
        SuntimesCalculatorDescriptor descriptor = dataEquinoxSpring.calculatorMode();

        dataSolsticeSummer.setCalculator(calculator, descriptor);
        dataSolsticeSummer.calculate();

        dataEquinoxAutumnal.setCalculator(calculator, descriptor);
        dataEquinoxAutumnal.calculate();
        
        dataSolsticeWinter.setCalculator(calculator, descriptor);
        dataSolsticeWinter.calculate();
    }

    public SuntimesEquinoxSolsticeData findSoonest(Calendar now)
    {
        return findClosest(now, true);
    }

    public SuntimesEquinoxSolsticeData findClosest(Calendar now)
    {
        return findClosest(now, false);
    }

    protected SuntimesEquinoxSolsticeData findClosest(Calendar now, boolean upcoming)
    {
        long timeDeltaMin = Long.MAX_VALUE;
        SuntimesEquinoxSolsticeData closest = null;
        SuntimesEquinoxSolsticeData[] dataset = {dataEquinoxSpring, dataSolsticeSummer, dataEquinoxAutumnal, dataSolsticeWinter };
        for (SuntimesEquinoxSolsticeData data : dataset)
        {
            Calendar[] events = {data.eventCalendarThisYear(), data.eventCalendarOtherYear()};
            for (Calendar event : events)
            {
                if (event != null)
                {
                    if (upcoming && !event.after(now))
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

    public void setLocation(Location location)
    {
        dataEquinoxSpring.setLocation(location);
        dataSolsticeSummer.setLocation(location);
        dataEquinoxAutumnal.setLocation(location);
        dataSolsticeWinter.setLocation(location);
    }

    public boolean isCalculated()
    {
        return dataEquinoxSpring.isCalculated();
    }

    public void invalidateCalculation()
    {
        dataEquinoxSpring.invalidateCalculation();
        dataSolsticeSummer.invalidateCalculation();
        dataEquinoxAutumnal.invalidateCalculation();
        dataSolsticeWinter.invalidateCalculation();
    }

    public boolean isImplemented() {
        return dataEquinoxSpring.isImplemented();
    }

    public void setTodayIs(Calendar date)
    {
        dataEquinoxSpring.setTodayIs(date);
        dataEquinoxAutumnal.setTodayIs(date);
        dataSolsticeSummer.setTodayIs(date);
        dataSolsticeWinter.setTodayIs(date);
    }

    public Calendar todayIs()
    {
        return dataEquinoxSpring.todayIs();
    }

    public boolean todayIsNotToday()
    {
        return dataEquinoxSpring.todayIsNotToday();
    }

    public String timezone()
    {
        return dataEquinoxSpring.timezone().getID();
    }

    public Calendar now()
    {
        return Calendar.getInstance(TimeZone.getTimeZone(timezone()));
    }

    public long tropicalYearLength()
    {
        double latitude = dataEquinoxSpring.location.getLatitudeAsDouble();
        SuntimesEquinoxSolsticeData data = (latitude >= 0) ? dataEquinoxSpring : dataEquinoxAutumnal;
        return data.eventCalendarOtherYear().getTimeInMillis() - data.eventCalendarThisYear().getTimeInMillis();
    }
}


