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

public class SuntimesRiseSetDataset
{
    public SuntimesRiseSetData dataActual;
    public SuntimesRiseSetData dataCivil;
    public SuntimesRiseSetData dataNautical;
    public SuntimesRiseSetData dataAstro;
    public SuntimesRiseSetData dataNoon;

    public SuntimesRiseSetDataset(Context context)
    {
        dataActual = new SuntimesRiseSetData(context, AppWidgetManager.INVALID_APPWIDGET_ID);
        dataActual.setCompareMode(WidgetSettings.CompareMode.TOMORROW);
        dataActual.setTimeMode(WidgetSettings.TimeMode.OFFICIAL);

        dataCivil = new SuntimesRiseSetData(dataActual);
        dataCivil.setTimeMode(WidgetSettings.TimeMode.CIVIL);

        dataNautical = new SuntimesRiseSetData(dataActual);
        dataNautical.setTimeMode(WidgetSettings.TimeMode.NAUTICAL);

        dataAstro = new SuntimesRiseSetData(dataActual);
        dataAstro.setTimeMode(WidgetSettings.TimeMode.ASTRONOMICAL);

        dataNoon = new SuntimesRiseSetData(dataActual);
        dataNoon.setTimeMode(WidgetSettings.TimeMode.NOON);
    }

    public SuntimesRiseSetDataset(SuntimesRiseSetData dataActual, SuntimesRiseSetData dataCivil, SuntimesRiseSetData dataNautical, SuntimesRiseSetData dataAstro, SuntimesRiseSetData dataNoon)
    {
        this.dataActual = dataActual;
        if (dataActual == null)
        {
            throw new NullPointerException("dataActual must not be null!");
        }

        this.dataCivil = dataCivil;
        if (dataCivil == null)
        {
            throw new NullPointerException("dataCivil must not be null!");
        }

        this.dataNautical = dataNautical;
        if (dataNautical == null)
        {
            throw new NullPointerException("dataNautical must not be null!");
        }

        this.dataAstro = dataAstro;
        if (dataAstro == null)
        {
            throw new NullPointerException("dataAstro must not be null!");
        }

        this.dataNoon = dataNoon;
        if (dataNoon == null)
        {
            throw new NullPointerException("dataNoon must not be null!");
        }
    }

    public void calculateData()
    {
        dataActual.calculate();
        dataCivil.calculate();
        dataNautical.calculate();
        dataAstro.calculate();
        dataNoon.calculate();
    }

    public boolean isCalculated()
    {
        return dataActual.isCalculated();
    }

    public void invalidateCalculation()
    {
        dataActual.invalidateCalculation();
        dataCivil.invalidateCalculation();
        dataNautical.invalidateCalculation();
        dataAstro.invalidateCalculation();
        dataNoon.invalidateCalculation();
    }

    public Calendar todayIs()
    {
        return dataActual.todayIs();
    }

    public boolean todayIsNotToday()
    {
        return dataActual.todayIsNotToday();
    }

    public boolean isNight()
    {
        return isNight(this.now());
    }

    public boolean isNight( Calendar dateTime )
    {
        Date time = dateTime.getTime();
        Date sunrise = dataActual.sunriseCalendarToday().getTime();
        Date sunsetAstroTwilight = dataAstro.sunsetCalendarToday().getTime();
        return (time.before(sunrise) || time.after(sunsetAstroTwilight));
    }

    public boolean isDay()
    {
        return isDay(this.now());
    }
    public boolean isDay(Calendar dateTime)
    {
        if (dataActual.calculator == null)
        {
            Calendar sunsetCal = dataActual.sunsetCalendarToday();
            if (sunsetCal == null)    // no sunset time, must be day
                return true;

            Calendar sunriseCal = dataActual.sunriseCalendarToday();
            if (sunriseCal == null)   // no sunrise time, must be night
                return false;

            Date time = dateTime.getTime();
            Date sunrise = sunriseCal.getTime();
            Date sunset = sunsetCal.getTime();
            return (time.after(sunrise) && time.before(sunset));

        } else {
            return dataActual.isDay(dateTime);
        }
    }

    public WidgetSettings.Location location()
    {
        return dataActual.location();
    }

    public TimeZone timezone()
    {
        return dataActual.timezone();
    }

    public Date date()
    {
        return dataActual.date();
    }

    public WidgetSettings.TimezoneMode timezoneMode()
    {
        return dataActual.timezoneMode();
    }

    public Calendar now()
    {
        return Calendar.getInstance(timezone());
    }
}


