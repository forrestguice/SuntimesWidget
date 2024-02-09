/**
    Copyright (C) 2014-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.core;

import android.content.Context;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * An interface used when calculating sunrise and sunset times. Implementations
 * of this interface are intended to be thin wrappers around third party code.
 *
 * @version 1.8.0
 */
public interface SuntimesCalculator
{
    int FEATURE_RISESET = 0;      // feature: rise, set, and twilight times (1.0.0)
    int FEATURE_SOLSTICE = 10;    // feature: solstice/equinox times (1.2.0)
    int FEATURE_ALTITUDE = 20;    // feature: altitude based refinement (1.0.0)
    int FEATURE_GOLDBLUE = 30;    // feature: gold, blue hour times (1.3.0)
    int FEATURE_MOON = 40;        // feature: moonrise, moonset, phase, illumination (1.3.0)
    int FEATURE_POSITION = 50;    // feature: sun, moon position (elevation, azimuth, etc) (1.4.0)
    int FEATURE_RISESET1 = 60;    // feature: rise/set @angle times (1.7.0, 1.7.1)
    int FEATURE_SHADOW = 70;      // feature: rise/set shadow length times (1.8.0)

    //
    // 1.0.0 sunrise, sunset, noon, twilight times
    //

    /**
     * @return the identifier for the given implementation of SuntimesCalculator
     * @since 1.0.0
     */
    String name();

    /**
     * @return an array of FEATURE flags indicating operations supported by given implementation
     * @since 1.2.0
     */
    int[] getSupportedFeatures();

    /**
     * Initialize the calculator with a given location and timezone.
     * @param location a WidgetSettings.Location object
     * @param timezone a timezone identifier
     * @since 1.0.0 (FEATURE_RISESET)
     */
    void init(Location location, String timezone );

    /**
     * Initialize the calculator with a given location and timezone.
     * @param location a WidgetSettings.Location object
     * @param timezone a timezone object
     * @since 1.1.0
     */
    void init(Location location, TimeZone timezone );

    /**
     * Initialize the calculator with the given location, timezone, and app context.
     * @param location a WidgetSettings.Location object
     * @param timezone a timezone object
     * @param context a context object
     * @since 1.2.0
     */
    void init(Location location, TimeZone timezone, Context context );

    /**
     * Morning Astronomical Twilight
     * @param date a Calendar representing a given date
     * @return a Calendar for astronomical sunrise for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getAstronomicalSunriseCalendarForDate( Calendar date );

    /**
     * Morning Nautical Twilight
     * @param date a Calendar representing a given date
     * @return a Calendar for nautical sunrise for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getNauticalSunriseCalendarForDate( Calendar date );

    /**
     * Morning Civil Twilight
     * @param date a Calendar representing a given date
     * @return a Calendar for civil sunrise for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getCivilSunriseCalendarForDate( Calendar date );

    /**
     * Sunrise
     * @param date a Calendar representing a given date
     * @return a Calendar for the official sunrise for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getOfficialSunriseCalendarForDate( Calendar date );

    /**
     * Solar Noon
     * @param date a Calendar representing a given date
     * @return a Calendar for solar noon for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getSolarNoonCalendarForDate( Calendar date );

    /**
     * Sunset
     * @param date a Calendar representing a given date
     * @return a Calendar for the official sunset for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getOfficialSunsetCalendarForDate( Calendar date );

    /**
     * Evening Civil Twilight
     * @param date a Calendar representing a given date
     * @return a Calendar for civil sunset for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getCivilSunsetCalendarForDate( Calendar date );

    /**
     * Evening Nautical Twilight
     * @param date a Calendar representing a given date
     * @return a Calendar for nautical sunset for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getNauticalSunsetCalendarForDate( Calendar date );

    /**
     * Evening Astronomical Twilight
     * @param date a Calendar representing a given date
     * @return a Calendar for astronomical sunset for the given date
     * @since 1.0.0 (FEATURE_RISESET)
     */
    Calendar getAstronomicalSunsetCalendarForDate( Calendar date );

    //
    // 1.2.0, equinox/solstice dates (FEATURE_SOLSTICE)
    //

    /**
     * @param date a Calendar representing a given date
     * @return a Calendar for spring equinox (adjusted to hemisphere) for the year of the given date
     * @since 1.2.0 (FEATURE_SOLSTICE)
     * @deprecated since 1.5.1 (use getSpringEquinoxForYear instead).
     */
    @Deprecated
    Calendar getVernalEquinoxForYear( Calendar date );

    /**
     * @param date a Calendar representing a given date
     * @return a Calendar for spring equinox (adjusted to hemisphere) for the year of the given date
     * @since 1.5.1
     */
    Calendar getSpringEquinoxForYear( Calendar date );


    /**
     * @param date a Calendar representing a given date
     * @return a Calendar for summer solstice (adjusted to hemisphere) for the year of the given date
     * @since 1.2.0 (FEATURE_SOLSTICE)
     */
    Calendar getSummerSolsticeForYear( Calendar date );

    /**
     * @param date a Calendar representing a given date
     * @return a Calendar for autumnal equinox (adjusted to hemisphere) for the year of the given date
     * @since 1.2.0 (FEATURE_SOLSTICE)
     */
    Calendar getAutumnalEquinoxForYear( Calendar date );

    /**
     * @param date a Calendar representing a given date
     * @return a Calendar for winter soltice (adjusted to hemisphere) for the year of the given date
     * @since 1.2.0 (FEATURE_SOLSTICE)
     */
    Calendar getWinterSolsticeForYear( Calendar date );

    //
    // 1.3.0, blue hour, golden hour (FEATURE_GOLDBLUE)
    //

    /**
     * Morning Blue Hour
     * @param date a Calendar representing a given date
     * @return [start,end] of the morning blue hour
     * @since 1.3.0 FEATURE_GOLDBLUE
     */
    Calendar[] getMorningBlueHourForDate( Calendar date );

    /**
     * Evening Blue Hour
     * @param date a Calendar representing a given date
     * @return [start,end] of the evening blue hour
     * @since 1.3.0 FEATURE_GOLDBLUE
     */
    Calendar[] getEveningBlueHourForDate( Calendar date );

    /**
     * Morning Golden Hour
     * @param date a Calendar representing a given date
     * @return end of the morning golden hour
     * @since 1.3.0 FEATURE_GOLDBLUE
     */
    Calendar getMorningGoldenHourForDate( Calendar date );

    /**
     * Evening Golden Hour
     * @param date a Calendar representing a given date
     * @return start of the evening golden hour
     * @since 1.1.0 FEATURE_GOLDBLUE
     */
    Calendar getEveningGoldenHourForDate( Calendar date );

    //
    // 1.1.0 isDay
    //

    /**
     * @param dateTime a Calendar representing a given date and time
     * @return true day time, false is either twilight or night
     * @since 1.1.0 FEATURE_GOLDBLUE
     */
    boolean isDay( Calendar dateTime );

    //
    // 1.3.0 moonrise, moonset, phase, illumination (FEATURE_MOON)
    //

    /**
     * getMoonTimesForDate
     * @param date a Calendar representing a given date
     * @return a MoonTimes obj wrapping riseTime, setTime, etc.
     * @since 1.3.0 FEATURE_MOON
     */
    MoonTimes getMoonTimesForDate(Calendar date);
    class MoonTimes
    {
        public Calendar riseTime = null;
        public Calendar setTime = null;
    }

    /**
     * MoonIllumination
     * @param dateTime a Calendar representing a given date
     * @return an illumination value [0,1] (0%-100%)
     * @since 1.3.0 FEATURE_MOON
     */
    double getMoonIlluminationForDate(Calendar dateTime);

    /**
     * Get the date of the next major moon phase.
     * @param phase a major moon phase (NEW, FIRST_QUARTER, FULL, THIRD_QUARTER)
     * @param date a Calendar representing a given date
     * @return a Calendar for the given phase (occurring after the given date).
     * @since 1.3.0 FEATURE_MOON
     */
    Calendar getMoonPhaseNextDate(MoonPhase phase, Calendar date);
    enum MoonPhase
    {
        NEW, FIRST_QUARTER, FULL, THIRD_QUARTER;
        private MoonPhase() {}
    }

    //
    // 1.4.0+ sun, moon position (FEATURE_POSITION)
    //

    /**
     * Get the sun's azimuth and elevation (and/or rightAscension and declination).
     * @param dateTime a Calendar representing a given date + time
     * @return a SunPosition obj wrapping azimuth, elevation, etc
     * @since 1.4.0 FEATURE_POSITION, FEATURE_RISESET
     */
    SunPosition getSunPosition(Calendar dateTime);
    class Position
    {
        public double azimuth;
        public double elevation;
        public double rightAscension;
        public double declination;
    }
    class SunPosition extends Position {}

    /**
     * Get the moon's azimuth and elevation.
     * @param dateTime a Calendar representing a given date + time
     * @return a MoonPosition obj wrapping azimuth, elevation, etc
     * @since 1.4.0 FEATURE_POSITION, FEATURE_MOON
     */
    MoonPosition getMoonPosition(Calendar dateTime);
    class MoonPosition extends Position
    {
        public double distance;
    }

    /**
     * @param objHeight height of the obj (meters)
     * @param dateTime a Calendar representing a given date + time
     * @return length of shadow (meters) or infinity
     * @since 1.4.0 FEATURE_POSITION, FEATURE_RISESET
     */
    double getShadowLength( double objHeight, Calendar dateTime );

    /**
     * @param dateTime a Calendar representing a given date + time
     * @return the equation of time value in seconds (or INFINITY if not supported)
     * @since 1.5.0 FEATURE_POSITION, FEATURE_RISESET
     */
    double equationOfTime( Calendar dateTime );

    /**
     * Get the date of the next lunar perigee.
     * @param date a Calendar representing a given date
     * @return a Calendar for next perigee (occurring on or after date).
     * @since 1.6.0 FEATURE_POSITION, FEATURE_MOON
     */
    Calendar getMoonPerigeeNextDate(Calendar date);

    /**
     * Get the date of the next lunar apogee.
     * @param date a Calendar representing a given date
     * @return a Calendar for next apogee (occurring on or after date).
     * @since 1.6.0 FEATURE_POSITION, FEATURE_MOON
     */
    Calendar getMoonApogeeNextDate(Calendar date);

    /**
     * Get the location that was passed to init.
     * @return Location
     * @since 1.6.1
     */
    Location getLocation();

    /**
     * Get the time zone that was passed to init.
     * @return TimeZone
     * @since 1.6.1
     */
    TimeZone getTimeZone();


    /**
     * Get the length of the tropical year.
     * @param date a Calendar representing a given date
     * @return tropical year length milliseconds
     * @since 1.7.0 (FEATURE_SOLSTICE)
     */
    long getTropicalYearLength( Calendar date );

    //
    // 1.7.0, (FEATURE_RISESET1)
    //

    /**
     * SunriseCalendarForDate
     * @param date a Calendar representing a given date
     * @param angle -deg below the horizon, or +deg above the horizon
     * @return time for sunrise at give angle
     * @since 1.7.0 FEATURE_RISESET1
     */
    Calendar getSunriseCalendarForDate( Calendar date, double angle );

    /**
     * SunsetCalendarForDate
     * @param date a Calendar representing a given date
     * @param angle -deg below the horizon, or +deg above the horizon
     * @return time for sunset at give angle
     * @since 1.7.0 FEATURE_RISESET1
     */
    Calendar getSunsetCalendarForDate( Calendar date, double angle );

    //
    // 1.8.0, (FEATURE_SHADOW)
    //

    /**
     * TimeOfShadowBeforeNoon
     * @param date a Calendar representing a given date
     * @param objHeight height of the obj (meters)
     * @param shadowLength length of shadow (meters)
     * @return time of shadow before noon
     * @since 1.8.0 FEATURE_SHADOW
     */
    Calendar getTimeOfShadowBeforeNoon( Calendar date, double objHeight, double shadowLength );

    /**
     * TimeOfShadowAfterNoon
     * @param date a Calendar representing a given date
     * @param objHeight height of the obj (meters)
     * @param shadowLength length of shadow (meters)
     * @return time of shadow after noon
     * @since 1.8.0 FEATURE_SHADOW
     */
    Calendar getTimeOfShadowAfterNoon( Calendar date, double objHeight, double shadowLength );

}
