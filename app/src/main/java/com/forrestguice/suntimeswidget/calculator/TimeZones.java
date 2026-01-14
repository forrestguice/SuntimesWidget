/**
 Copyright (C) 2014-2025 Forrest Guice
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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

import com.forrestguice.suntimes.calculator.core.Location;
import com.forrestguice.suntimes.calculator.core.SuntimesCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeZones
{
    public static final String TZID_UTC = "UTC";

    public static TimeZone localMeanTime(Location location) {
        return new LocalMeanTime(location.getLongitudeAsDouble(), LocalMeanTime.TIMEZONEID);
    }

    public static TimeZone siderealTime() {
        return new LocalMeanTime(0, SiderealTime.TZID_GMST);
    }

    public static TimeZone siderealTime(Location location) {
        return new LocalMeanTime(location.getLongitudeAsDouble(), SiderealTime.TZID_LMST);
    }

    public static TimeZone apparentSolarTime(Location location) {
        return new ApparentSolarTime(location.getLongitudeAsDouble(), ApparentSolarTime.TIMEZONEID);
    }

    public static TimeZone apparentSolarTime(Location location, SuntimesCalculator calculator) {
        return new ApparentSolarTime(location.getLongitudeAsDouble(), ApparentSolarTime.TIMEZONEID, calculator);
    }

    /**
     * LocalMeanTime : TimeZone
     */
    public static class LocalMeanTime extends TimeZone
    {
        public static final String TIMEZONEID = "LMT";

        private int rawOffset = 0;

        public LocalMeanTime(double longitude, String name)
        {
            super();
            setID(name);
            setRawOffset(findOffset(longitude));
        }

        /**
         * @param longitude a longitude value; degrees [-180, 180]
         * @return the offset of this longitude from utc (in milliseconds)
         */
        public static int findOffset( double longitude )
        {
            double offsetHrs = longitude * 24 / 360d;           // offset from gmt in hrs
            //noinspection UnnecessaryLocalVariable
            int offsetMs = (int)(offsetHrs * 60 * 60 * 1000);  // hrs * 60min in a day * 60s in a min * 1000ms in a second
            //Log.d("DEBUG", "offset: " + offsetHrs + " (" + offsetMs + ")");
            return offsetMs;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds)
        {
            return getRawOffset();
        }

        @Override
        public int getOffset( long date )
        {
            return getRawOffset();
        }

        @Override
        public int getRawOffset()
        {
            return rawOffset;
        }

        @Override
        public void setRawOffset(int offset)
        {
            rawOffset = offset;
        }

        @Override
        public boolean inDaylightTime(Date date)
        {
            return false;
        }

        @Override
        public boolean useDaylightTime()
        {
            return false;
        }

        @NonNull
        @Override
        public String toString() {
            return "id: " + getID() + ", offset: " + getRawOffset() + ", useDaylight: " + useDaylightTime();
        }
    }

    /**
     * ApparentSolarTime : TimeZone
     */
    public static class ApparentSolarTime extends LocalMeanTime
    {
        public static final String TIMEZONEID = "LTST";    // local true solar time

        public ApparentSolarTime(double longitude, String name)
        {
            super(longitude, name);
        }

        public ApparentSolarTime(double longitude, String name, @Nullable SuntimesCalculator calculator)
        {
            super(longitude, name);
            this.calculator = calculator;
        }

        @Nullable
        private SuntimesCalculator calculator = null;
        public void setCalculator(@Nullable SuntimesCalculator calculator)
        {
            this.calculator = calculator;
        }
        @Nullable
        public SuntimesCalculator getCalculator() {
            return calculator;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds)
        {
            Calendar calendar = new GregorianCalendar();
            calendar.set(year, month, day);
            return getOffset(calendar.getTimeInMillis());
        }

        /**
         * @param date a given date
         * @return ms offset with "equation of time" correction applied for the given date
         */
        @Override
        public int getOffset( long date )
        {
            eotOffset = equationOfTimeOffset(date, calculator);
            return getRawOffset() + eotOffset;
        }

        public static int equationOfTimeOffset(long date, SuntimesCalculator calculator)
        {
            if (calculator != null)
            {
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(date);
                double eotSeconds = calculator.equationOfTime(calendar);
                if (eotSeconds != Double.POSITIVE_INFINITY)
                {
                    //Log.d("ApparentSolar", "equationOfTime: using " + calculator.name() + ": eot is: " + (eotSeconds / 60d) + " minutes" );
                    return (int)(eotSeconds * 1000);

                } else {
                    //Log.d("ApparentSolar", "equationOfTime: not supported by " + calculator.name() + " using fallback: " + (equationOfTimeOffset(date) / 1000d / 60d) );
                    return equationOfTimeOffset(date);    // not supported; use fall-back implementation
                }
            } else {
                //Log.d("ApparentSolar", "equationOfTime: null calculator, using fallback: " + (equationOfTimeOffset(date) / 1000d / 60d) );
                return equationOfTimeOffset(date);      // no calculator; use fall-back implementation
            }
        }

        /**
         * @param date a given date
         * @return equation of time correction in milliseconds
         */
        public static int equationOfTimeOffset(long date)
        {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(date);
            return (int)(equationOfTimeOffset(calendar.get(Calendar.DAY_OF_YEAR)) * 60 * 1000);
        }

        /**
         * http://www.esrl.noaa.gov/gmd/grad/solcalc/solareqns.PDF
         * @param n day of year (n=1 is january 1)
         * @return equation of time correction in decimal minutes
         */
        public static double equationOfTimeOffset(int n)
        {
            while (n <= 0)    // n in range [1, 365]
            {
                n += 365;
            }
            while (n > 365)
            {
                n -= 365;
            }

            double d = (2 * Math.PI / 365.24) * (n - 1);   // fractional year (radians)
            return 229.18 * (0.000075
                    + 0.001868 * Math.cos(d)
                    - 0.032077 * Math.sin(d)
                    - 0.014615 * Math.cos(2*d)
                    - 0.040849 * Math.sin(2*d));   // .oO(a truly magical return statement)
        }

        @Override
        public boolean useDaylightTime()
        {
            return true;
        }
        public boolean observesDaylightTime()
        {
            return useDaylightTime();
        }

        @Override
        public boolean inDaylightTime(Date date)
        {
            return true;
        }

        @Override
        public int getDSTSavings() {
            return eotOffset;
        }
        private int eotOffset = 0;
    }

    /**
     * SiderealTime
     */
    public static class SiderealTime
    {
        public static final String TZID_GMST = "GMST";
        public static final String TZID_LMST = "LMST";

        public static int gmstOffset(long dateMillis)
        {
            double julianDay = julianDay(dateMillis);
            double d = julianDay - 2451545d;
            double t = (d / 36525d);
            double gmst_degrees = 280.46061837 + (360.98564736629 * d) + (0.000387933 * t * t) - ((t * t * t) / 38710000d);
            double gmst_hours = gmst_degrees * (24 / 360d);
            double utc_hours = dateMillis / (60d * 60d * 1000d);
            double offset_hours = simplifyHours(gmst_hours - utc_hours);
            return (int)(offset_hours * 60d * 60d * 1000d);
        }

        public static int lmstOffset(long dateMillis, double longitude) {
            return gmstOffset(dateMillis) + (int)((longitude * 24 / 360d) * 60 * 60 * 1000);
        }

        /**
         * https://stackoverflow.com/questions/11759992/calculating-jdayjulian-day-in-javascript
         */
        public static double julianDay(long dateMillis) {
            return (dateMillis / (24d * 60d * 60d * 1000d)) + 2440587.5;  // days + julianDay(epoch)
        }

        private static double simplifyHours(double hours)
        {
            while (hours >= 24) {
                hours -= 24;
            }
            while (hours < 0) {
                hours += 24;
            }
            return hours;
        }
    }

}
