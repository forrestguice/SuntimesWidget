/**
    Copyright (C) 2014-2020 Forrest Guice
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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.Log;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

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
    public static final String LINK = "github.com/mikereedell/sunrisesunsetlib-java";
    public static final int[] FEATURES = new int[] { SuntimesCalculator.FEATURE_RISESET };
    private SunriseSunsetCalculator calculator = null;
    private TimeZone param_timezone;
    private com.forrestguice.suntimeswidget.calculator.core.Location param_location;

    public SunriseSunsetSuntimesCalculator() { /* EMPTY */ }

    @Override
    public void init(com.forrestguice.suntimeswidget.calculator.core.Location locationSetting, String timezone)
    {
        init(locationSetting, TimeZone.getTimeZone(timezone));
    }

    @Override
    public void init(com.forrestguice.suntimeswidget.calculator.core.Location locationSetting, TimeZone timezone)
    {
        this.param_location = locationSetting;
        this.param_timezone = timezone;

        Location location;
        try {
            location = new Location(locationSetting.getLatitude(), locationSetting.getLongitude());

        } catch (NumberFormatException e) {
            Log.e("init", "location was invalid, falling back to default; " + e.toString());
            location = new Location("0", "0");
        }
        calculator = new SunriseSunsetCalculator(location, this.param_timezone);
    }

    @Override
    public String name()
    {
        return NAME;
    }

    @Override
    public int[] getSupportedFeatures()
    {
        return FEATURES;
    }

    @Nullable
    @Override
    public Calendar getCivilSunriseCalendarForDate( Calendar date )
    {
        return calculator.getCivilSunriseCalendarForDate(date);
    }

    @Nullable
    @Override
    public Calendar getNauticalSunriseCalendarForDate( Calendar date )
    {
         return calculator.getNauticalSunriseCalendarForDate(date);
    }

    @Nullable
    @Override
    public Calendar getAstronomicalSunriseCalendarForDate( Calendar date )
    {
        return calculator.getAstronomicalSunriseCalendarForDate(date);
    }

    @Nullable
    @Override
    public Calendar getOfficialSunriseCalendarForDate( Calendar date )
    {
        return calculator.getOfficialSunriseCalendarForDate(date);
    }

    @Nullable
    @Override
    public Calendar getSolarNoonCalendarForDate(Calendar date)
    {
        Calendar sunriseCal = getOfficialSunriseCalendarForDate(date);
        Calendar sunsetCal = getOfficialSunsetCalendarForDate(date);
        if (sunriseCal != null && sunsetCal != null)
        {
            long sunriseTime = sunriseCal.getTimeInMillis();
            long sunsetTime = sunsetCal.getTimeInMillis();
            if (sunsetTime < sunriseTime)
                sunsetTime += (24 * 60 * 60 * 1000);  // bug workaround (sunset calendar set to wrong day; 24hrs off)

            long noonTime = sunriseTime + ((sunsetTime - sunriseTime) / 2L);
            Calendar noonCalendar = Calendar.getInstance(param_timezone);
            noonCalendar.setTimeInMillis(noonTime);
            return noonCalendar;

        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Calendar getSolarMidnightCalendarForDate(Calendar date) {
        return null;    // TODO
    }

    @Nullable
    @Override
    public Calendar getCivilSunsetCalendarForDate( Calendar date )
    {
        return calculator.getCivilSunsetCalendarForDate(date);
    }

    @Nullable
    @Override
    public Calendar getNauticalSunsetCalendarForDate( Calendar date )
    {
        return calculator.getNauticalSunsetCalendarForDate(date);
    }

    @Nullable
    @Override
    public Calendar getAstronomicalSunsetCalendarForDate( Calendar date )
    {
        return calculator.getAstronomicalSunsetCalendarForDate(date);
    }

    @Nullable
    @Override
    public Calendar getVernalEquinoxForYear(Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public Calendar getSpringEquinoxForYear(Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public Calendar getSummerSolsticeForYear(Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public Calendar getAutumnalEquinoxForYear(Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public Calendar getWinterSolsticeForYear(Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public Calendar getOfficialSunsetCalendarForDate( Calendar date )
    {
        return calculator.getOfficialSunsetCalendarForDate(date);
    }

    @NonNull
    @Override
    public Calendar[] getMorningBlueHourForDate(Calendar date)
    {
        return new Calendar[] { null, null };
    }

    @NonNull
    @Override
    public Calendar[] getEveningBlueHourForDate(Calendar date)
    {
        return new Calendar[] { null, null };
    }

    @Nullable
    @Override
    public Calendar getMorningGoldenHourForDate(Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public Calendar getEveningGoldenHourForDate(Calendar date)
    {
        return null;
    }

    @Override
    public boolean isDay(Calendar dateTime)
    {
        Calendar sunsetCal = getOfficialSunriseCalendarForDate(dateTime);
        if (sunsetCal == null)    // no sunset time, must be day
            return true;

        Calendar sunriseCal = getOfficialSunsetCalendarForDate(dateTime);
        if (sunriseCal == null)   // no sunrise time, must be night
            return false;

        Date time = dateTime.getTime();
        Date sunrise = sunriseCal.getTime();
        Date sunset = sunsetCal.getTime();
        return (time.after(sunrise) && time.before(sunset));
    }

    @Nullable
    @Override
    public MoonTimes getMoonTimesForDate(Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public double getMoonIlluminationForDate(Calendar date)
    {
        return -1;
    }

    @Nullable
    @Override
    public Calendar getMoonPhaseNextDate(MoonPhase phase, Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public SunPosition getSunPosition(Calendar dateTime)
    {
        return null;
    }

    @Nullable
    @Override
    public MoonPosition getMoonPosition(Calendar dateTime) {
        return null;
    }

    @Override
    public double getShadowLength(double objHeight, Calendar dateTime) {
        return -1;
    }

    @Nullable
    @Override
    public Calendar getTimeOfShadowBeforeNoon(Calendar calendar, double objHeight, double shadowLength) {
        return null;
    }

    @Nullable
    @Override
    public Calendar getTimeOfShadowAfterNoon(Calendar calendar, double objHeight, double shadowLength) {
        return null;
    }

    @Override
    public double equationOfTime(Calendar dateTime)
    {
        return Double.POSITIVE_INFINITY;
    }

    @Nullable
    @Override
    public Calendar getMoonPerigeeNextDate(Calendar date)
    {
        return null;
    }

    @Nullable
    @Override
    public Calendar getMoonApogeeNextDate(Calendar date)
    {
        return null;
    }

    @Override
    public com.forrestguice.suntimeswidget.calculator.core.Location getLocation() {
        return param_location;
    }

    @Override
    public TimeZone getTimeZone() {
        return param_timezone;
    }

    @Override
    public long getTropicalYearLength(Calendar date) {
        return (long)Math.floor(365.24 * 24 * 60 * 60 * 1000);
    }

    @Nullable
    @Override
    public Calendar getSunriseCalendarForDate( Calendar date, double angle ) {
        return null;   // TODO: supported by this lib?
    }

    @Nullable
    @Override
    public Calendar getSunsetCalendarForDate( Calendar date, double angle ) {
        return null;   // TODO: supported by this lib?
    }

}

