/**
    Copyright (C) 2014 Forrest Guice
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

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SuntimesEquinoxSolsticeDataset
{
    public SuntimesEquinoxSolsticeData dataEquinoxVernal;
    public SuntimesEquinoxSolsticeData dataSolsticeSummer;
    public SuntimesEquinoxSolsticeData dataEquinoxAutumnal;
    public SuntimesEquinoxSolsticeData dataSolsticeWinter;

    public SuntimesEquinoxSolsticeDataset(Context context)
    {
        dataEquinoxVernal = new SuntimesEquinoxSolsticeData(context, AppWidgetManager.INVALID_APPWIDGET_ID);
        dataEquinoxVernal.setTimeMode(WidgetSettings.SolsticeEquinoxMode.EQUINOX_VERNAL);

        dataSolsticeSummer = new SuntimesEquinoxSolsticeData(dataEquinoxVernal);
        dataSolsticeSummer.setTimeMode(WidgetSettings.SolsticeEquinoxMode.SOLSTICE_SUMMER);

        dataEquinoxAutumnal = new SuntimesEquinoxSolsticeData(dataEquinoxVernal);
        dataEquinoxAutumnal.setTimeMode(WidgetSettings.SolsticeEquinoxMode.EQUINOX_AUTUMNAL);

        dataSolsticeWinter = new SuntimesEquinoxSolsticeData(dataEquinoxVernal);
        dataSolsticeWinter.setTimeMode(WidgetSettings.SolsticeEquinoxMode.SOLSTICE_WINTER);
    }

    public SuntimesEquinoxSolsticeDataset( SuntimesEquinoxSolsticeData dataEquinoxVernal,
                                           SuntimesEquinoxSolsticeData dataSolsticeSummer,
                                           SuntimesEquinoxSolsticeData dataEquinoxAutumnal,
                                           SuntimesEquinoxSolsticeData dataSolsticeWinter )
    {
        this.dataEquinoxVernal = dataEquinoxVernal;
        if (dataEquinoxVernal == null)
        {
            throw new NullPointerException("dataEquinoxVernal must not be null!");
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

    public void calculateData()
    {
        dataEquinoxVernal.calculate();
        dataSolsticeSummer.calculate();
        dataEquinoxAutumnal.calculate();
        dataSolsticeWinter.calculate();
    }

    public boolean isCalculated()
    {
        return dataEquinoxVernal.isCalculated();
    }

    public void invalidateCalculation()
    {
        dataEquinoxVernal.invalidateCalculation();
        dataSolsticeSummer.invalidateCalculation();
        dataEquinoxAutumnal.invalidateCalculation();
        dataSolsticeWinter.invalidateCalculation();
    }

    public boolean isImplemented()
    {
        return (dataEquinoxVernal.eventCalendarThisYear() != null);
    }

    public Calendar todayIs()
    {
        return dataEquinoxVernal.todayIs();
    }

    public boolean todayIsNotToday()
    {
        return dataEquinoxVernal.todayIsNotToday();
    }

    public String timezone()
    {
        return dataEquinoxVernal.timezone().getID();
    }

    public Calendar now()
    {
        return Calendar.getInstance(TimeZone.getTimeZone(timezone()));
    }
}


