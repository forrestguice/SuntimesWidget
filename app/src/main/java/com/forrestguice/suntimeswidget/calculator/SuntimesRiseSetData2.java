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

package com.forrestguice.suntimeswidget.calculator;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.settings.CompareMode;

import java.util.Arrays;
import java.util.Calendar;

/**
 * An extended version of SuntimesRiseSetData that fixes erroneous assumptions regarding "other"
 * times (which refers to both yesterday and tomorrow). The extended version performs three calculations;
 * yesterday / today / tomorrow (..vs today / other).
 */
public class SuntimesRiseSetData2 extends SuntimesRiseSetData
{
    private final Calendar[] calendar = {null, null, null};
    private final Calendar[] sunrise = {null, null, null};
    private final Calendar[] sunset = {null, null, null};
    private final Calendar[] daylength = {null, null, null};

    public SuntimesRiseSetData2(Object context, int appWidgetId)
    {
        super(context, appWidgetId);
        initFromSettings(context, appWidgetId);
    }
    public SuntimesRiseSetData2(Object context, int appWidgetId, String calculatorName)
    {
        super(context, appWidgetId, calculatorName);
        initFromSettings(context, appWidgetId, calculatorName);
    }
    public SuntimesRiseSetData2(SuntimesRiseSetData2 other)
    {
        super(other);
        initFromOther(other, other.layoutID);
    }
    public SuntimesRiseSetData2(SuntimesRiseSetData2 other, int layoutID)
    {
        super(other, layoutID);
        initFromOther(other, layoutID);
    }

    protected int indexOfOther()
    {
        return (compareMode == CompareMode.TOMORROW ? 2 : 0);
    }

    /**
     * result: sunrise today
     */
    @Override
    public boolean hasSunriseTimeToday()
    {
        return (sunrise[1] != null);
    }

    @Override
    public Calendar sunriseCalendarToday()
    {
        return sunrise[1];
    }

    @Override
    public Calendar sunriseCalendar(int i)
    {
        if (i >= 0 && i < sunrise.length)
            return sunrise[i];
        else return null;
    }

    /**
     * result: sunset today
     */
    @Override
    public boolean hasSunsetTimeToday()
    {
        return (sunset[1] != null);
    }

    @Override
    public Calendar sunsetCalendarToday()
    {
        return sunset[1];
    }

    @Override
    public Calendar sunsetCalendar(int i)
    {
        if (i >= 0 && i < sunset.length)
            return sunset[i];
        else return null;
    }

    /**
     * result: sunrise other
     */
    @Override
    public boolean hasSunriseTimeOther()
    {
        return (sunrise[indexOfOther()] != null);
    }

    @Override
    public Calendar sunriseCalendarOther()
    {
        return sunrise[indexOfOther()];
    }

    /**
     * result: sunset other
     */
    @Override
    public boolean hasSunsetTimeOther()
    {
        return (sunset[indexOfOther()] != null);
    }

    @Override
    public Calendar sunsetCalendarOther()
    {
        return sunset[indexOfOther()];
    }

    /**
     * @param other another instance of SuntimesRiseSetData
     * @param layoutID an R.layout.someLayoutID to be associated w/ this data
     */
    protected void initFromOther(SuntimesRiseSetData2 other, int layoutID )
    {
        initFromOther(other);

        this.layoutID = layoutID;
        this.compareMode = other.compareMode();
        this.timeMode = other.timeMode();
        this.angle = other.angle;
        this.fraction = other.fraction;
        this.offset = other.offset;

        this.dayLengthToday = other.dayLengthToday();
        this.dayLengthOther = other.dayLengthOther();
        this.dayDeltaPrefix = other.dayDeltaPrefix();

        for (int i=0; i<calendar.length; i++)
        {
            this.calendar[i] = other.calendar[i];
            this.sunrise[i] = other.sunrise[i];
            this.sunset[i] = other.sunset[i];
            this.daylength[i] = other.daylength[i];
        }
    }

    @Override
    public Calendar[] getEvents()
    {
        Calendar[] retValue = Arrays.copyOf(sunrise, sunrise.length + sunset.length + 1);   // sunrise array (3) + sunset array (3) + midnight (1)
        System.arraycopy(sunset, 0, retValue, sunrise.length, sunset.length);

        Calendar midnight = midnight();
        midnight.add(Calendar.DAY_OF_MONTH,  1);
        retValue[retValue.length-1] = midnight;

        return retValue;
    }

    /**
     * Calculate
     * @param context context
     */
    @Override
    public void calculate(Object context)
    {
        //Log.v("SuntimesWidgetData", "time mode: " + timeMode);
        //Log.v("SuntimesWidgetData", "location_mode: " + locationMode.name());
        //Log.v("SuntimesWidgetData", "latitude: " + location.getLatitude());
        //Log.v("SuntimesWidgetData", "longitude: " + location.getLongitude());
        //Log.v("SuntimesWidgetData", "timezone_mode: " + timezoneMode.name());
        //Log.v("SuntimesWidgetData", "timezone: " + timezone);
        //Log.v("SuntimesWidgetData", "compare mode: " + compareMode.name());

        initCalculator();
        if (calculator == null) {
            throw new IllegalStateException("calculator is null after initCalculator() was called!");
        }

        initTimezone(getDataSettings(context));

        for (int i=0; i<calendar.length; i++)
        {
            calendar[i] = Calendar.getInstance(timezone);
        }
        todaysCalendar = Calendar.getInstance(timezone);
        otherCalendar = Calendar.getInstance(timezone);

        if (todayIsNotToday())
        {
            for (int i=0; i<calendar.length; i++) {
                //noinspection ConstantConditions
                calendar[i].setTimeInMillis(todayIs.getTimeInMillis());
            }
            //noinspection ConstantConditions
            todaysCalendar.setTimeInMillis(todayIs.getTimeInMillis());
            otherCalendar.setTimeInMillis(todayIs.getTimeInMillis());
        }

        switch (compareMode)
        {
            case YESTERDAY:
                dayDeltaPrefix = R.string.delta_day_yesterday;
                otherCalendar.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case TOMORROW:
            default:
                dayDeltaPrefix = R.string.delta_day_tomorrow;
                otherCalendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
        }
        calendar[0].add(Calendar.DAY_OF_MONTH, -1);
        calendar[2].add(Calendar.DAY_OF_MONTH, 1);

        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        for (int i=0; i<calendar.length; i++)
        {
            if (angle != null)
            {
                sunrise[i] = calculator.getSunriseCalendarForDate(calendar[i], angle);
                sunset[i] = calculator.getSunsetCalendarForDate(calendar[i], angle);
                if (offset != 0) {
                    if (sunrise[i] != null) {
                        sunrise[i].add(Calendar.MILLISECOND, offset);
                    }
                    if (sunset[i] != null) {
                        sunset[i].add(Calendar.MILLISECOND, offset);
                    }
                }
                continue;
            }

            switch (timeMode)
            {
                case GOLD:
                    sunrise[i] = calculator.getMorningGoldenHourForDate(calendar[i]);
                    sunset[i] = calculator.getEveningGoldenHourForDate(calendar[i]);
                    break;

                case BLUE8:
                    sunrise[i] = calculator.getMorningBlueHourForDate(calendar[i])[0];
                    sunset[i] = calculator.getEveningBlueHourForDate(calendar[i])[1];
                    break;

                case BLUE4:
                    sunrise[i] = calculator.getMorningBlueHourForDate(calendar[i])[1];
                    sunset[i] = calculator.getEveningBlueHourForDate(calendar[i])[0];
                    break;

                case NOON:
                    sunrise[i] = sunset[i] = calculator.getSolarNoonCalendarForDate(calendar[i]);
                    break;

                case MIDNIGHT:
                    sunrise[i] = sunset[i] = calculator.getSolarMidnightCalendarForDate(calendar[i]);
                    break;

                case CIVIL:
                    sunrise[i] = calculator.getCivilSunriseCalendarForDate(calendar[i]);
                    sunset[i] = calculator.getCivilSunsetCalendarForDate(calendar[i]);
                    break;

                case NAUTICAL:
                    sunrise[i] = calculator.getNauticalSunriseCalendarForDate(calendar[i]);
                    sunset[i] = calculator.getNauticalSunsetCalendarForDate(calendar[i]);
                    break;

                case ASTRONOMICAL:
                    sunrise[i] = calculator.getAstronomicalSunriseCalendarForDate(calendar[i]);
                    sunset[i] = calculator.getAstronomicalSunsetCalendarForDate(calendar[i]);
                    break;

                case OFFICIAL:
                default:
                    sunrise[i] = calculator.getOfficialSunriseCalendarForDate(calendar[i]);
                    sunset[i] = calculator.getOfficialSunsetCalendarForDate(calendar[i]);
                    break;
            }
            if (fraction != null && sunrise[i] != null && sunset[i] != null) {
                applyFraction(sunrise[i], sunset[i], fraction);
            }
            if (offset != 0) {
                if (sunrise[i] != null) {
                    sunrise[i].add(Calendar.MILLISECOND, offset);
                }
                if (sunset[i] != null) {
                    sunset[i].add(Calendar.MILLISECOND, offset);
                }
            }
        }

        int i = indexOfOther();
        dayLengthToday = determineDayLength(sunrise[1], sunset[1]);
        dayLengthOther = determineDayLength(sunrise[i], sunset[i]);

        super.calculate(context);
    }

}


