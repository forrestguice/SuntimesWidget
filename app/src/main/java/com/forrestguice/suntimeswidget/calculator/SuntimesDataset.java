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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SuntimesDataset
{
    public SuntimesData dataActual;
    public SuntimesData dataCivil;
    public SuntimesData dataNautical;
    public SuntimesData dataAstro;
    public SuntimesData dataNoon;

    public SuntimesDataset( SuntimesData dataActual, SuntimesData dataCivil, SuntimesData dataNautical, SuntimesData dataAstro, SuntimesData dataNoon )
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
        return isNight(this.now().getTime());
    }

    public boolean isNight( Date time )
    {
        Date sunrise = dataActual.sunriseCalendarToday().getTime();
        Date sunsetAstroTwilight = dataAstro.sunsetCalendarToday().getTime();
        return (time.before(sunrise) || time.after(sunsetAstroTwilight));
    }

    public boolean isDay()
    {
        return isDay(now().getTime());
    }

    public boolean isDay( Date time )
    {
        Calendar sunsetCal = dataActual.sunsetCalendarToday();
        if (sunsetCal == null)    // no sunset time, must be day
            return true;

        Calendar sunriseCal = dataActual.sunriseCalendarToday();
        if (sunriseCal == null)   // no sunrise time, must be night
            return false;

        Date sunrise = sunriseCal.getTime();
        Date sunset = sunsetCal.getTime();
        return (time.after(sunrise) && time.before(sunset));
    }

    public String timezone()
    {
        return dataActual.timezone();
    }

    public Calendar now()
    {
        return Calendar.getInstance(TimeZone.getTimeZone(timezone()));
    }
}


