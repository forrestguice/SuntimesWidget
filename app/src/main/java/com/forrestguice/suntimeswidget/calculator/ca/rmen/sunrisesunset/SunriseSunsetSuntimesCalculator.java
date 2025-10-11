/**
    Copyright (C) 2017-2020 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.core.Location;

import java.util.Calendar;
import java.util.TimeZone;

import ca.rmen.sunrisesunset.SunriseSunset;

public class SunriseSunsetSuntimesCalculator implements SuntimesCalculator
{
    public static final String NAME = "ca.rmen.sunrisesunset";
    public static final String REF = "com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset.SunriseSunsetSuntimesCalculator";
    public static final String LINK = "github.com/caarmen/SunriseSunset";
    public static final int[] FEATURES = new int[] { SuntimesCalculator.FEATURE_RISESET, SuntimesCalculator.FEATURE_GOLDBLUE };

    private Location location;
    private TimeZone timezone;

    public SunriseSunsetSuntimesCalculator() { /* EMPTY */ }

    @Override
    public int[] getSupportedFeatures()
    {
        return FEATURES;
    }

    @Override
    public void init(Location locationSetting, String timezone)
    {
        init(locationSetting, TimeZone.getTimeZone(timezone));
    }

    @Override
    public void init(Location location, TimeZone timezone)
    {
        this.location = location;
        this.timezone = timezone;
    }

    @Override
    public String name()
    {
        return NAME;
    }

    @Override
    public Calendar getCivilSunriseCalendarForDate( Calendar date )
    {
        Calendar[] civilTwilight = SunriseSunset.getCivilTwilight(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        if (civilTwilight == null)
            return null;
        else return civilTwilight[0];
    }

    @Override
    public Calendar getNauticalSunriseCalendarForDate( Calendar date )
    {
        Calendar[] nauticalTwilight = SunriseSunset.getNauticalTwilight(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        if (nauticalTwilight == null)
            return null;
        else return nauticalTwilight[0];
    }

    @Override
    public Calendar getAstronomicalSunriseCalendarForDate( Calendar date )
    {
        Calendar[] astroTwilight = SunriseSunset.getAstronomicalTwilight(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        if (astroTwilight == null)
            return null;
        else return astroTwilight[0];
    }

    @Override
    public Calendar getOfficialSunriseCalendarForDate( Calendar date )
    {
        Calendar[] riseset = SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        if (riseset == null)
            return null;
        else return riseset[0];
    }

    @Override
    public Calendar getSolarNoonCalendarForDate(Calendar date)
    {
        //noinspection UnnecessaryLocalVariable
        Calendar noon = SunriseSunset.getSolarNoon(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        return noon;
    }

    @Override
    public Calendar getSolarMidnightCalendarForDate(Calendar date) {
        return null;    // TODO
    }

    @Override
    public Calendar getCivilSunsetCalendarForDate( Calendar date )
    {
        Calendar[] civilTwilight = SunriseSunset.getCivilTwilight(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        if (civilTwilight == null)
            return null;
        else return civilTwilight[1];
    }

    @Override
    public Calendar getNauticalSunsetCalendarForDate( Calendar date )
    {
        Calendar[] nauticalTwilight = SunriseSunset.getNauticalTwilight(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        if (nauticalTwilight == null)
            return null;
        else return nauticalTwilight[1];
    }

    @Override
    public Calendar getAstronomicalSunsetCalendarForDate( Calendar date )
    {
        Calendar[] astroTwilight = SunriseSunset.getAstronomicalTwilight(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        if (astroTwilight == null)
            return null;
        else return astroTwilight[1];
    }

    @Override
    public Calendar getVernalEquinoxForYear(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar getSpringEquinoxForYear(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar getSummerSolsticeForYear(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar getAutumnalEquinoxForYear(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar getWinterSolsticeForYear(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar[] getMorningBlueHourForDate(Calendar date)
    {
        Calendar[] blueTimes = new Calendar[2];
        Calendar[] blueTimes0 = SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), SUN_ALTITUDE_BLUE_HIGH);
        Calendar[] blueTimes1 = SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), SUN_ALTITUDE_BLUE_LOW);

        if (blueTimes0 != null)
            blueTimes[0] = blueTimes0[0];

        if (blueTimes1 != null)
            blueTimes[1] = blueTimes1[0];

        return blueTimes;
    }

    @Override
    public Calendar[] getEveningBlueHourForDate(Calendar date)
    {
        Calendar[] blueTimes = new Calendar[2];
        Calendar[] blueTimes0 = SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), SUN_ALTITUDE_BLUE_LOW);
        Calendar[] blueTimes1 = SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), SUN_ALTITUDE_BLUE_HIGH);

        if (blueTimes0 != null)
            blueTimes[0] = blueTimes0[1];

        if (blueTimes1 != null)
            blueTimes[1] = blueTimes1[1];

        return blueTimes;
    }

    @Override
    public Calendar getMorningGoldenHourForDate(Calendar date)
    {
        Calendar[] goldenTimes = SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), SUN_ALTITUDE_GOLDEN);
        if (goldenTimes == null)
            return null;
        else return goldenTimes[0];
    }

    @Override
    public Calendar getEveningGoldenHourForDate(Calendar date)
    {
        Calendar[] goldenTimes = SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), SUN_ALTITUDE_GOLDEN);
        if (goldenTimes == null)
            return null;
        else return goldenTimes[1];
    }

    public static final double SUN_ALTITUDE_GOLDEN = 6.0;
    public static final double SUN_ALTITUDE_BLUE_HIGH = -8.0;
    public static final double SUN_ALTITUDE_BLUE_LOW = -4.0;

    @Override
    public boolean isDay(Calendar dateTime)
    {
        return SunriseSunset.isDay(dateTime, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
    }

    @Override
    public MoonTimes getMoonTimesForDate(Calendar date)
    {
        return null;
    }

    @Override
    public double getMoonIlluminationForDate(Calendar date) {
        return -1;
    }

    @Override
    public Calendar getMoonPhaseNextDate(MoonPhase phase, Calendar date)
    {
        return null;
    }

    @Override
    public SunPosition getSunPosition(Calendar dateTime)
    {
        return null;
    }

    @Override
    public MoonPosition getMoonPosition(Calendar dateTime) {
        return null;
    }

    @Override
    public double getShadowLength(double objHeight, Calendar dateTime)
    {
        return -1;
    }

    @Override
    public Calendar getTimeOfShadowBeforeNoon(Calendar dateTime, double objHeight, double shadowLength) {
        return null;
    }

    @Override
    public Calendar getTimeOfShadowAfterNoon(Calendar dateTime, double objHeight, double shadowLength) {
        return null;
    }

    @Override
    public Calendar getOfficialSunsetCalendarForDate( Calendar date )
    {
        Calendar[] riseset = SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble());
        if (riseset == null)
            return null;
        else return riseset[1];
    }

    @Override
    public double equationOfTime(Calendar dateTime)
    {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public Calendar getMoonPerigeeNextDate(Calendar date)
    {
        return null;
    }

    @Override
    public Calendar getMoonApogeeNextDate(Calendar date)
    {
        return null;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public TimeZone getTimeZone() {
        return timezone;
    }

    @Override
    public long getTropicalYearLength(Calendar date) {
        return (long)Math.floor(365.24 * 24 * 60 * 60 * 1000);
    }

    @Override
    public Calendar getSunriseCalendarForDate( Calendar date, double angle ) {
        return SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), angle)[0];
    }

    @Override
    public Calendar getSunsetCalendarForDate( Calendar date, double angle ) {
        return SunriseSunset.getSunriseSunset(date, location.getLatitudeAsDouble(), location.getLongitudeAsDouble(), angle)[1];
    }

}

