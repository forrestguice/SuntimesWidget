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

package com.forrestguice.suntimeswidget.calculator.sunrisesunset_java;

import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.luckycatlabs.sunrisesunset.Zenith;
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A thin wrapper around a SunriseSunsetCalculator instance (from sunrisesunsetlib-java) that
 * implements the interface used by the widget.
 */
public class SunriseSunsetSuntimesCalculator implements SuntimesCalculator
{
    public static final String NAME = "sunrisesunsetlib";
    public static final String REF = "com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator";
    SunriseSunsetCalculator calculator = null;
    String timezone;
    Location location;

    public SunriseSunsetSuntimesCalculator() { /* EMPTY */ }

    @Override
    public void init(WidgetSettings.Location locationSetting, String timezone)
    {
        this.location = new Location(locationSetting.getLatitude(), locationSetting.getLongitude());
        this.timezone = timezone;
        calculator = new SunriseSunsetCalculator(this.location, this.timezone);
    }

    @Override
    public String name()
    {
        return NAME;
    }

    @Override
    public Calendar getCivilSunriseCalendarForDate( Calendar date )
    {
        return calculator.getCivilSunriseCalendarForDate(date);
    }

    @Override
    public Calendar getNauticalSunriseCalendarForDate( Calendar date )
    {
         return calculator.getNauticalSunriseCalendarForDate(date);
    }

    @Override
    public Calendar getAstronomicalSunriseCalendarForDate( Calendar date )
    {
        return calculator.getAstronomicalSunriseCalendarForDate(date);
    }

    @Override
    public Calendar getOfficialSunriseCalendarForDate( Calendar date )
    {
        return calculator.getOfficialSunriseCalendarForDate(date);
    }

    @Override
    public Calendar getSolarNoonCalendarForDate(Calendar date)
    {
        long sunriseTime = getOfficialSunriseCalendarForDate(date).getTimeInMillis();
        long sunsetTime = getOfficialSunsetCalendarForDate(date).getTimeInMillis();
        long noonTime = sunriseTime + ((sunsetTime - sunriseTime) / 2L);

        Calendar noonCalendar = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        noonCalendar.setTimeInMillis(noonTime);
        return noonCalendar;
    }

    @Override
    public Calendar getCivilSunsetCalendarForDate( Calendar date )
    {
        return calculator.getCivilSunsetCalendarForDate(date);
    }

    @Override
    public Calendar getNauticalSunsetCalendarForDate( Calendar date )
    {
        return calculator.getNauticalSunsetCalendarForDate(date);
    }

    @Override
    public Calendar getAstronomicalSunsetCalendarForDate( Calendar date )
    {
        return calculator.getAstronomicalSunsetCalendarForDate(date);
    }

    @Override
    public Calendar getOfficialSunsetCalendarForDate( Calendar date )
    {
        return calculator.getOfficialSunsetCalendarForDate(date);
    }

    public static SuntimesCalculatorDescriptor getDescriptor()
    {
        return new SuntimesCalculatorDescriptor(SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.REF);
    }
}

