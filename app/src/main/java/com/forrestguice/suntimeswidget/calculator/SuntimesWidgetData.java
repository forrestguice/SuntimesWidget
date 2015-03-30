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


import android.content.Context;
import android.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetSettings;

import java.util.Calendar;
import java.util.TimeZone;

public class SuntimesWidgetData
{
    private boolean calculated = false;

    private Context context;

    private SuntimesCalculatorDescriptor calculatorMode;
    private SuntimesWidgetSettings.LocationMode locationMode;
    private SuntimesWidgetSettings.TimezoneMode timezoneMode;
    private SuntimesWidgetSettings.CompareMode compareMode;
    private SuntimesWidgetSettings.Location location;
    private SuntimesWidgetSettings.TimeMode timeMode;
    private String timezone;

    private Calendar sunriseCalendarToday;
    private Calendar sunsetCalendarToday;
    private Calendar sunriseCalendarOther;
    private Calendar sunsetCalendarOther;
    private long dayLengthToday;
    private long dayLengthOther;
    private String dayDeltaPrefix;

    private int layoutID = R.layout.layout_widget_1x1_0i;

    public SuntimesWidgetData(Context context, int appWidgetId)
    {
        this.context = context;
        initFromSettings(context, appWidgetId);
    }
    public SuntimesWidgetData( SuntimesWidgetData other )
    {
        initFromOther(other, other.layoutID);
    }
    public SuntimesWidgetData( SuntimesWidgetData other, int layoutID )
    {
        initFromOther(other, layoutID);
    }

    public int layoutID()
    {
        return layoutID;
    }
    public void setLayoutID( int id )
    {
        layoutID = id;
    }

    public boolean isCalculated()
    {
        return calculated;
    }

    public String timezone()
    {
        return timezone;
    }

    public SuntimesWidgetSettings.TimeMode timeMode()
    {
        return timeMode;
    }

    public SuntimesWidgetSettings.Location location()
    {
        return location;
    }

    public SuntimesCalculatorDescriptor calculatorMode()
    {
        return calculatorMode;
    }

    public SuntimesWidgetSettings.LocationMode locationMode()
    {
        return locationMode;
    }

    public SuntimesWidgetSettings.TimezoneMode timezoneMode()
    {
        return timezoneMode;
    }

    public SuntimesWidgetSettings.CompareMode compareMode()
    {
        return compareMode;
    }

    public Calendar sunriseCalendarToday()
    {
        return sunriseCalendarToday;
    }
    public Calendar sunsetCalendarToday()
    {
        return sunsetCalendarToday;
    }

    public Calendar sunriseCalendarOther()
    {
        return sunriseCalendarOther;
    }
    public Calendar sunsetCalendarOther()
    {
        return sunsetCalendarOther;
    }

    public String dayDeltaPrefix()
    {
        return dayDeltaPrefix;
    }

    public long dayLengthToday()
    {
        return dayLengthToday;
    }
    public long dayLengthOther()
    {
        return dayLengthOther;
    }

    private void initFromOther( SuntimesWidgetData other, int layoutID )
    {
        this.layoutID = layoutID;

        this.calculatorMode = other.calculatorMode();
        this.locationMode = other.locationMode();
        this.timezoneMode = other.timezoneMode();
        this.compareMode = other.compareMode();
        this.location = other.location();
        this.timeMode = other.timeMode();
        this.timezone = other.timezone();

        this.sunriseCalendarToday = other.sunriseCalendarToday();
        this.sunsetCalendarToday = other.sunsetCalendarToday();
        this.sunriseCalendarOther = other.sunriseCalendarOther();
        this.sunsetCalendarOther = other.sunsetCalendarOther();
        this.dayLengthToday = other.dayLengthToday();
        this.dayLengthOther = other.dayLengthOther();
        this.dayDeltaPrefix = other.dayDeltaPrefix();

        this.calculated = other.isCalculated();
    }

    public void initFromSettings(Context context, int appWidgetId)
    {
        calculated = false;

        // from general settings
        calculatorMode = SuntimesWidgetSettings.loadCalculatorModePref(context, appWidgetId);
        timeMode = SuntimesWidgetSettings.loadTimeModePref(context, appWidgetId);
        compareMode = SuntimesWidgetSettings.loadCompareModePref(context, appWidgetId);

        // from location settings
        location = SuntimesWidgetSettings.loadLocationPref(context, appWidgetId);
        locationMode = SuntimesWidgetSettings.loadLocationModePref(context, appWidgetId);
        if (locationMode == SuntimesWidgetSettings.LocationMode.CURRENT_LOCATION)
        {
            //location = getCurrentLocation(context);
        }

        // from timezone settings
        timezone = SuntimesWidgetSettings.loadTimezonePref(context, appWidgetId);
        timezoneMode = SuntimesWidgetSettings.loadTimezoneModePref(context, appWidgetId);
        if (timezoneMode == SuntimesWidgetSettings.TimezoneMode.CURRENT_TIMEZONE)
        {
            timezone = TimeZone.getDefault().getID();
        }
    }

    public void calculate()
    {
        // DEBUG (comment me)
        Log.v("SuntimesWidgetData", "time mode: " + timeMode);
        Log.v("SuntimesWidgetData", "location_mode: " + locationMode.name());
        Log.v("SuntimesWidgetData", "latitude: " + location.getLatitude());
        Log.v("SuntimesWidgetData", "longitude: " + location.getLongitude());
        Log.v("SuntimesWidgetData", "timezone_mode: " + timezoneMode.name());
        Log.v("SuntimesWidgetData", "timezone: " + timezone);
        Log.v("SuntimesWidgetData", "compare mode: " + compareMode.name());

        SuntimesCalculatorFactory calculatorFactory = new SuntimesCalculatorFactory(context, calculatorMode);
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);
        Calendar todaysCalendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();

        switch (compareMode)
        {
            case YESTERDAY:
                dayDeltaPrefix = context.getString(R.string.delta_day_yesterday);
                otherCalendar.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case TOMORROW:
            default:
                dayDeltaPrefix = context.getString(R.string.delta_day_tomorrow);
                otherCalendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
        }

        switch (timeMode)
        {
            case CIVIL:
                sunriseCalendarToday = calculator.getCivilSunriseCalendarForDate(todaysCalendar);
                sunsetCalendarToday = calculator.getCivilSunsetCalendarForDate(todaysCalendar);
                sunriseCalendarOther = calculator.getCivilSunriseCalendarForDate(otherCalendar);
                sunsetCalendarOther = calculator.getCivilSunsetCalendarForDate(otherCalendar);
                break;

            case NAUTICAL:
                sunriseCalendarToday = calculator.getNauticalSunriseCalendarForDate(todaysCalendar);
                sunsetCalendarToday = calculator.getNauticalSunsetCalendarForDate(todaysCalendar);
                sunriseCalendarOther = calculator.getNauticalSunriseCalendarForDate(otherCalendar);
                sunsetCalendarOther = calculator.getNauticalSunsetCalendarForDate(otherCalendar);
                break;

            case ASTRONOMICAL:
                sunriseCalendarToday = calculator.getAstronomicalSunriseCalendarForDate(todaysCalendar);
                sunsetCalendarToday = calculator.getAstronomicalSunsetCalendarForDate(todaysCalendar);
                sunriseCalendarOther = calculator.getAstronomicalSunriseCalendarForDate(otherCalendar);
                sunsetCalendarOther = calculator.getAstronomicalSunsetCalendarForDate(otherCalendar);
                break;

            case OFFICIAL:
            default:
                sunriseCalendarToday = calculator.getOfficialSunriseCalendarForDate(todaysCalendar);
                sunsetCalendarToday = calculator.getOfficialSunsetCalendarForDate(todaysCalendar);
                sunriseCalendarOther = calculator.getOfficialSunriseCalendarForDate(otherCalendar);
                sunsetCalendarOther = calculator.getOfficialSunsetCalendarForDate(otherCalendar);
                break;
        }

        dayLengthToday = sunsetCalendarToday.getTimeInMillis() - sunriseCalendarToday.getTimeInMillis();
        long sunriseOther = sunriseCalendarOther.getTime().getTime();
        long sunsetOther = sunsetCalendarOther.getTime().getTime();
        dayLengthOther = sunsetOther - sunriseOther;

        calculated = true;
    }
}


