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

import android.content.Context;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.TemporalType;
import net.time4j.calendar.astro.AstronomicalSeason;
import net.time4j.calendar.astro.SolarTime;
import net.time4j.calendar.astro.StdSolarCalculator;
import net.time4j.calendar.astro.Twilight;
import net.time4j.engine.CalendarDate;
import net.time4j.engine.ChronoFunction;
import net.time4j.tz.TZID;
import net.time4j.tz.Timezone;
import net.time4j.tz.ZonalOffset;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public abstract class Time4ASuntimesCalculator implements SuntimesCalculator
{
    public static final int[] FEATURES = new int[] { FEATURE_RISESET, FEATURE_SOLSTICE };

    public abstract StdSolarCalculator getCalculator();

    protected SolarTime solarTime;
    protected TimeZone timezone;

    @Override
    public int[] getSupportedFeatures()
    {
        return Time4ASuntimesCalculator.FEATURES;
    }

    @Override
    public void init(WidgetSettings.Location locationSetting, String timezone)
    {
        init(locationSetting, TimeZone.getTimeZone(timezone));
    }

    @Override
    public void init(WidgetSettings.Location location, TimeZone timezone)
    {
        init(location, timezone, null);
    }

    @Override
    public void init(WidgetSettings.Location location, TimeZone timezone, Context context)
    {
        this.solarTime = SolarTime.ofLocation(location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), location.getAltitudeAsInteger(), getCalculator());
        this.timezone = timezone;
    }

    @Override
    public Calendar getCivilSunriseCalendarForDate( Calendar date )
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> civilRise = this.solarTime.sunrise(Twilight.CIVIL);
        return momentToCalendar(localDate.get(civilRise));
    }

    @Override
    public Calendar getNauticalSunriseCalendarForDate( Calendar date )
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> nauticalRise = this.solarTime.sunrise(Twilight.NAUTICAL);
        return momentToCalendar(localDate.get(nauticalRise));
    }

    @Override
    public Calendar getAstronomicalSunriseCalendarForDate( Calendar date )
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> astroRise = this.solarTime.sunrise(Twilight.ASTRONOMICAL);
        return momentToCalendar(localDate.get(astroRise));
    }

    @Override
    public Calendar getOfficialSunriseCalendarForDate( Calendar date )
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> sunrise = this.solarTime.sunrise();
        return momentToCalendar(localDate.get(sunrise));
    }

    @Override
    public Calendar getSolarNoonCalendarForDate(Calendar date)
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> noon = this.solarTime.transitAtNoon();
        return momentToCalendar(localDate.get(noon));
    }

    @Override
    public Calendar getCivilSunsetCalendarForDate( Calendar date )
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> civilSet = this.solarTime.sunset(Twilight.CIVIL);
        return momentToCalendar(localDate.get(civilSet));
    }

    @Override
    public Calendar getNauticalSunsetCalendarForDate( Calendar date )
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> nauticalSet = this.solarTime.sunset(Twilight.NAUTICAL);
        return momentToCalendar(localDate.get(nauticalSet));
    }

    @Override
    public Calendar getAstronomicalSunsetCalendarForDate( Calendar date )
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> astroSet = this.solarTime.sunset(Twilight.ASTRONOMICAL);
        return momentToCalendar(localDate.get(astroSet));
    }

    @Override
    public Calendar[] getMorningBlueHourForDate(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar[] getEveningBlueHourForDate(Calendar date)
    {
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
    public Calendar getOfficialSunsetCalendarForDate( Calendar date )
    {
        PlainDate localDate = calendarToPlainDate(date);
        ChronoFunction<CalendarDate, Moment> sunset = this.solarTime.sunset();
        return momentToCalendar(localDate.get(sunset));
    }

    @Override
    public Calendar getVernalEquinoxForYear(Calendar date)
    {
        AstronomicalSeason vernalEquinox = adjustSeasonToHemisphere(AstronomicalSeason.VERNAL_EQUINOX);
        Moment moment = vernalEquinox.inYear(date.get(Calendar.YEAR));
        return momentToCalendar(moment);
    }

    @Override
    public Calendar getSummerSolsticeForYear(Calendar date)
    {
        AstronomicalSeason summerSolstice = adjustSeasonToHemisphere(AstronomicalSeason.SUMMER_SOLSTICE);
        Moment moment = summerSolstice.inYear(date.get(Calendar.YEAR));
        return momentToCalendar(moment);
    }

    @Override
    public Calendar getAutumnalEquinoxForYear(Calendar date)
    {
        AstronomicalSeason autumnalEquinox = adjustSeasonToHemisphere(AstronomicalSeason.AUTUMNAL_EQUINOX);
        Moment moment = autumnalEquinox.inYear(date.get(Calendar.YEAR));
        return momentToCalendar(moment);
    }

    @Override
    public Calendar getWinterSolsticeForYear(Calendar date)
    {
        AstronomicalSeason winterSolstice = adjustSeasonToHemisphere(AstronomicalSeason.WINTER_SOLSTICE);
        Moment moment = winterSolstice.inYear(date.get(Calendar.YEAR));
        return momentToCalendar(moment);
    }

    @Override
    public boolean isDay(Calendar dateTime)
    {
        net.time4j.tz.Timezone tz = toTimezone(dateTime.getTimeZone());
        PlainDate localDate = calendarToPlainDate(dateTime);
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(dateTime.getTime());
        SolarTime.Sunshine sunshine = localDate.get(this.solarTime.sunshine(tz.getID()));
        return sunshine.isPresent(moment);
    }

    protected net.time4j.tz.Timezone toTimezone( java.util.TimeZone input )
    {
        String tzString = "java.util.TimeZone~" + input.getID();
        TZID tzFallback = Timezone.ofPlatform().getID();  // ofSystem().getID();
        return net.time4j.tz.Timezone.of(tzString, tzFallback);
    }

    protected PlainDate calendarToPlainDate(Calendar input)
    {
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(input.getTime());
        ZonalOffset offset = ZonalOffset.atLongitude(new BigDecimal(this.solarTime.getLongitude()));
        return moment.toZonalTimestamp(offset).toDate();
    }

    protected Calendar momentToCalendar(Moment moment)
    {
        Calendar retValue = null;
        if (moment != null)
        {
            retValue = new GregorianCalendar();
            retValue.setTimeZone(timezone);
            retValue.setTime(TemporalType.JAVA_UTIL_DATE.from(moment));
        }
        return retValue;
    }

    protected AstronomicalSeason adjustSeasonToHemisphere( AstronomicalSeason season )
    {
        boolean northernHemisphere = (this.solarTime.getLatitude() >= 0);
        if (northernHemisphere)
            return season.onNorthernHemisphere();
        else return season.onSouthernHemisphere();
    }

}

