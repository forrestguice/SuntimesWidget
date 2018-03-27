/**
    Copyright (C) 2017-2018 Forrest Guice
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
import net.time4j.calendar.astro.GeoLocation;
import net.time4j.calendar.astro.LunarTime;
import net.time4j.calendar.astro.SolarTime;
import net.time4j.calendar.astro.StdSolarCalculator;
import net.time4j.calendar.astro.SunPosition;
import net.time4j.calendar.astro.Twilight;
import net.time4j.engine.CalendarDate;
import net.time4j.engine.ChronoFunction;
import net.time4j.tz.TZID;
import net.time4j.tz.Timezone;
import net.time4j.tz.ZonalOffset;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public abstract class Time4ASuntimesCalculator implements SuntimesCalculator
{
    public static final int[] FEATURES = new int[] { FEATURE_RISESET, FEATURE_SOLSTICE, FEATURE_GOLDBLUE, FEATURE_POSITION };

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
        this.solarTime = SolarTime.ofLocation(location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), clampAltitude(location.getAltitudeAsInteger()), getCalculator());
        this.timezone = timezone;
    }

    public static final int ALTITUDE_MIN = 0;
    public static final int ALTITUDE_MAX = 11000;
    public static int clampAltitude(int value)
    {
        if (value > ALTITUDE_MAX) {
            Log.w("clampAltitude", "altitude of " + value + " is greater than " + ALTITUDE_MAX + "! clamping value..");
            return ALTITUDE_MAX;

        } else if (value < ALTITUDE_MIN) {
            Log.w("clampAltitude", "altitude of " + value + " is less than " + ALTITUDE_MIN + "! clamping value..");
            return ALTITUDE_MIN;
        }
        return value;
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
        SolarTime.Calculator calculator = solarTime.getCalculator();
        int altitude = solarTime.getAltitude();
        double latitude = solarTime.getLatitude();
        double longitude = solarTime.getLongitude();
        double geodeticAngle = calculator.getGeodeticAngle(latitude, altitude);
        double blueStartAngle = 90 + geodeticAngle + SUN_ALTITUDE_BLUE_HIGH;
        double blueEndAngle = 90 + geodeticAngle + SUN_ALTITUDE_BLUE_LOW;

        PlainDate localDate = calendarToPlainDate(date);
        Moment blueMorningStart = calculator.sunrise(localDate, latitude, longitude, blueStartAngle);
        Moment blueMorningEnd = calculator.sunrise(localDate, latitude, longitude, blueEndAngle);
        return new Calendar[] { momentToCalendar(blueMorningStart), momentToCalendar(blueMorningEnd) };
    }

    @Override
    public Calendar[] getEveningBlueHourForDate(Calendar date)
    {
        SolarTime.Calculator calculator = solarTime.getCalculator();
        int altitude = solarTime.getAltitude();
        double latitude = solarTime.getLatitude();
        double longitude = solarTime.getLongitude();
        double geodeticAngle = calculator.getGeodeticAngle(latitude, altitude);
        double blueStartAngle = 90 + geodeticAngle + SUN_ALTITUDE_BLUE_LOW;
        double blueEndAngle = 90 + geodeticAngle + SUN_ALTITUDE_BLUE_HIGH;

        PlainDate localDate = calendarToPlainDate(date);
        Moment blueEveningStart = calculator.sunset(localDate, latitude, longitude, blueStartAngle);
        Moment blueEveningEnd = calculator.sunset(localDate, latitude, longitude, blueEndAngle);
        return new Calendar[] { momentToCalendar(blueEveningStart), momentToCalendar(blueEveningEnd) };
    }

    @Override
    public Calendar getMorningGoldenHourForDate(Calendar date)
    {
        SolarTime.Calculator calculator = solarTime.getCalculator();
        int altitude = solarTime.getAltitude();
        double latitude = solarTime.getLatitude();
        double longitude = solarTime.getLongitude();
        double geodeticAngle = calculator.getGeodeticAngle(latitude, altitude);
        double goldenAngle = 90 + geodeticAngle - SUN_ALTITUDE_GOLDEN;

        PlainDate localDate = calendarToPlainDate(date);
        Moment goldMorningEnd = calculator.sunrise(localDate, latitude, longitude, goldenAngle);
        return momentToCalendar(goldMorningEnd);
    }

    @Override
    public Calendar getEveningGoldenHourForDate(Calendar date)
    {
        SolarTime.Calculator calculator = solarTime.getCalculator();
        int altitude = solarTime.getAltitude();
        double latitude = solarTime.getLatitude();
        double longitude = solarTime.getLongitude();
        double geodeticAngle = calculator.getGeodeticAngle(latitude, altitude);
        double goldenAngle = 90 + geodeticAngle - SUN_ALTITUDE_GOLDEN;

        PlainDate localDate = calendarToPlainDate(date);
        Moment goldEveningStart = this.solarTime.getCalculator().sunset(localDate, latitude, longitude, goldenAngle);
        return momentToCalendar(goldEveningStart);
    }

    public static final double SUN_ALTITUDE_GOLDEN = 6.0;
    public static final double SUN_ALTITUDE_BLUE_HIGH = 8.0;
    public static final double SUN_ALTITUDE_BLUE_LOW = 4.0;

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
        //ZonalOffset offset = ZonalOffset.atLongitude(new BigDecimal(this.solarTime.getLongitude()));
        ZonalOffset zonalOffset = ZonalOffset.ofTotalSeconds(timezone.getOffset(input.getTimeInMillis()) / 1000);
        return moment.toZonalTimestamp(zonalOffset).toDate();
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

    @Override
    public MoonTimes getMoonTimesForDate(Calendar date)
    {
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(date.getTime());
        TZID tzid = toTimezone(date.getTimeZone()).getID();
        PlainDate localDate = moment.toZonalTimestamp(tzid).toDate();

        LunarTime lunarTime = LunarTime.ofLocation(tzid, this.solarTime.getLatitude(), this.solarTime.getLongitude(), this.solarTime.getAltitude());
        LunarTime.Moonlight moonlight = lunarTime.on(localDate);

        MoonTimes result = new MoonTimes();
        result.riseTime = momentToCalendar(moonlight.moonrise()); // might be null meaning there is no moonrise
        result.setTime = momentToCalendar(moonlight.moonset()); // might be null meaning there is no moonset
        return result;
    }

    @Override
    public double getMoonIlluminationForDate(Calendar date)
    {
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(date.getTime());
        return net.time4j.calendar.astro.MoonPhase.getIllumination(moment);
    }

    @Override
    public Calendar getMoonPhaseNextDate(MoonPhase phase, Calendar date)
    {
        net.time4j.calendar.astro.MoonPhase moonPhase = toPhase(phase);
        Moment phaseMoment = moonPhase.after(TemporalType.JAVA_UTIL_DATE.translate(date.getTime()));
        return momentToCalendar(phaseMoment);
    }

    private net.time4j.calendar.astro.MoonPhase toPhase( MoonPhase input )
    {
        switch (input) {
            case NEW: return net.time4j.calendar.astro.MoonPhase.NEW_MOON;
            case FIRST_QUARTER: return net.time4j.calendar.astro.MoonPhase.FIRST_QUARTER;
            case THIRD_QUARTER: return net.time4j.calendar.astro.MoonPhase.LAST_QUARTER;
            case FULL:
            default: return net.time4j.calendar.astro.MoonPhase.FULL_MOON;
        }
    }

    @Override
    public SunPosition getSunPosition(Calendar dateTime)
    {
        Moment moment = TemporalType.JAVA_UTIL_DATE.translate(dateTime.getTime());
        net.time4j.calendar.astro.SunPosition position = net.time4j.calendar.astro.SunPosition.at(moment, solarTime);

        SunPosition result = new SunPosition();
        result.azimuth = position.getAzimuth();
        result.elevation = position.getElevation();
        result.rightAscension = position.getRightAscension();
        result.declination = position.getDeclination();
        return result;
    }

}

