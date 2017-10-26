/**
    Copyright (C) 2017 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.time4a;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.PlainTimestamp;
import net.time4j.calendar.JulianCalendar;
import net.time4j.calendar.astro.JulianDay;
import net.time4j.calendar.astro.SolarTime;
import net.time4j.calendar.astro.Twilight;
import net.time4j.engine.CalendarDate;
import net.time4j.engine.ChronoFunction;

import java.util.Calendar;
import java.util.TimeZone;

public class SunriseSunsetSuntimesCalculator implements SuntimesCalculator
{
    public static final String NAME = "time4a";
    public static final String REF = "com.forrestguice.suntimeswidget.calculator.time4a.SunriseSunsetSuntimesCalculator";

    WidgetSettings.Location location;
    SolarTime calculator;
    String timezone;

    public SunriseSunsetSuntimesCalculator() { /* EMPTY */ }

    @Override
    public void init(WidgetSettings.Location locationSetting, String timezone)
    {
        this.location = locationSetting;
        this.calculator = SolarTime.ofLocation(this.location.getLatitudeAsDouble(), this.location.getLongitudeAsDouble());
        this.timezone = timezone;
    }

    @Override
    public void init(WidgetSettings.Location location, TimeZone timezone)
    {
        this.location = location;
        this.calculator = SolarTime.ofLocation(this.location.getLatitudeAsDouble(), this.location.getLongitudeAsDouble());
        this.timezone = timezone.getID();
    }

    @Override
    public String name()
    {
        return NAME;
    }

    @Override
    public Calendar getCivilSunriseCalendarForDate( Calendar date )
    {

        PlainDate d = null;   // todo: Calendar w/ timezone to PlainDate
        ChronoFunction<CalendarDate, Moment> civilRise = calculator.sunrise(Twilight.CIVIL);
        Moment moment = d.get(civilRise);
        PlainTimestamp timeStamp = moment.toZonalTimestamp(timezone);
        return null;          // todo: conversion back to Calendar?
    }

    @Override
    public Calendar getNauticalSunriseCalendarForDate( Calendar date )
    {
        ChronoFunction<CalendarDate, Moment> nauticalRise = calculator.sunrise(Twilight.NAUTICAL);
        return null;
    }

    @Override
    public Calendar getAstronomicalSunriseCalendarForDate( Calendar date )
    {
        ChronoFunction<CalendarDate, Moment> astroRise = calculator.sunrise(Twilight.ASTRONOMICAL);
        return null;
    }

    @Override
    public Calendar getOfficialSunriseCalendarForDate( Calendar date )
    {
        ChronoFunction<CalendarDate, Moment> rise = calculator.sunrise();
        return null;
    }

    @Override
    public Calendar getSolarNoonCalendarForDate(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar getCivilSunsetCalendarForDate( Calendar date )
    {
        ChronoFunction<CalendarDate, Moment> civilSet = calculator.sunset(Twilight.CIVIL);
        return null;
    }

    @Override
    public Calendar getNauticalSunsetCalendarForDate( Calendar date )
    {
        ChronoFunction<CalendarDate, Moment> nauticalSet = calculator.sunset(Twilight.NAUTICAL);
        return null;
    }

    @Override
    public Calendar getAstronomicalSunsetCalendarForDate( Calendar date )
    {
        ChronoFunction<CalendarDate, Moment> astroSet = calculator.sunset(Twilight.ASTRONOMICAL);
        return null;
    }

    @Override
    public Calendar[] getMorningBlueHourForDate(Calendar date)
    {
        ChronoFunction<CalendarDate, Moment> blueRise = calculator.sunrise(Twilight.BLUE_HOUR);
        return null;
    }

    @Override
    public Calendar[] getEveningBlueHourForDate(Calendar date)
    {
        ChronoFunction<CalendarDate, Moment> blueSet = calculator.sunset(Twilight.BLUE_HOUR);
        return null;
    }

    @Override
    public Calendar[] getMorningGoldenHourForDate(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar[] getEveningGoldenHourForDate(Calendar date)
    {
        return null;
    }

    @Override
    public boolean isDay(Calendar dateTime)
    {
        return false;
    }

    @Override
    public Calendar getOfficialSunsetCalendarForDate( Calendar date )
    {
        ChronoFunction<CalendarDate, Moment> set = calculator.sunset();
        return null;
    }

    public static SuntimesCalculatorDescriptor getDescriptor()
    {
        return new SuntimesCalculatorDescriptor(SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.REF);
    }
}

