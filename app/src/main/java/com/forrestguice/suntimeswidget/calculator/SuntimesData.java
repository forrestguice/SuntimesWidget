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
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SuntimesData
{
    private boolean calculated = false;

    private Context context;

    private SuntimesCalculatorDescriptor calculatorMode;
    private WidgetSettings.LocationMode locationMode;
    private WidgetSettings.TimezoneMode timezoneMode;
    private WidgetSettings.CompareMode compareMode;
    private WidgetSettings.Location location;
    private WidgetSettings.TimeMode timeMode;
    private String timezone;
    private Date date = new Date();
    private Date dateOther = new Date();
    private Calendar todaysCalendar;
    private Calendar otherCalendar;

    private Calendar sunriseCalendarToday;
    private Calendar sunsetCalendarToday;
    private Calendar sunriseCalendarOther;
    private Calendar sunsetCalendarOther;
    private long dayLengthToday;
    private long dayLengthOther;
    private String dayDeltaPrefix;

    private int layoutID = R.layout.layout_widget_1x1_0i;

    public SuntimesData(Context context, int appWidgetId)
    {
        this.context = context;
        initFromSettings(context, appWidgetId);
    }
    public SuntimesData(SuntimesData other)
    {
        this.context = other.context;
        initFromOther(other, other.layoutID);
    }
    public SuntimesData(SuntimesData other, int layoutID)
    {
        this.context = other.context;
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

    public Date date() { return date; }
    public Date dateOther() { return dateOther; }

    public Calendar calendar() { return todaysCalendar; }
    public Calendar getOtherCalendar() { return otherCalendar; }

    public WidgetSettings.TimeMode timeMode()
    {
        return timeMode;
    }
    public void setTimeMode( WidgetSettings.TimeMode mode )
    {
        timeMode = mode;
    }

    public WidgetSettings.Location location()
    {
        return location;
    }

    public SuntimesCalculatorDescriptor calculatorMode()
    {
        return calculatorMode;
    }

    public WidgetSettings.LocationMode locationMode()
    {
        return locationMode;
    }

    public WidgetSettings.TimezoneMode timezoneMode()
    {
        return timezoneMode;
    }

    public WidgetSettings.CompareMode compareMode()
    {
        return compareMode;
    }
    public void setCompareMode( WidgetSettings.CompareMode mode )
    {
        compareMode = mode;
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

    private void initFromOther( SuntimesData other, int layoutID )
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
        calculatorMode = WidgetSettings.loadCalculatorModePref(context, appWidgetId);
        timeMode = WidgetSettings.loadTimeModePref(context, appWidgetId);
        compareMode = WidgetSettings.loadCompareModePref(context, appWidgetId);

        // from location settings
        location = WidgetSettings.loadLocationPref(context, appWidgetId);
        locationMode = WidgetSettings.loadLocationModePref(context, appWidgetId);
        //if (locationMode == SuntimesWidgetSettings.LocationMode.CURRENT_LOCATION)
        //{
        //    //location = getCurrentLocation(context);
        //}

        // from timezone settings
        timezone = WidgetSettings.loadTimezonePref(context, appWidgetId);
        timezoneMode = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        if (timezoneMode == WidgetSettings.TimezoneMode.CURRENT_TIMEZONE)
        {
            timezone = TimeZone.getDefault().getID();
        }
    }

    public void calculate()
    {
        //Log.v("SuntimesWidgetData", "time mode: " + timeMode);
        //Log.v("SuntimesWidgetData", "location_mode: " + locationMode.name());
        //Log.v("SuntimesWidgetData", "latitude: " + location.getLatitude());
        //Log.v("SuntimesWidgetData", "longitude: " + location.getLongitude());
        //Log.v("SuntimesWidgetData", "timezone_mode: " + timezoneMode.name());
        //Log.v("SuntimesWidgetData", "timezone: " + timezone);
        //Log.v("SuntimesWidgetData", "compare mode: " + compareMode.name());

        SuntimesCalculatorFactory calculatorFactory = new SuntimesCalculatorFactory(context, calculatorMode);
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);

        todaysCalendar = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        date = todaysCalendar.getTime();

        otherCalendar = Calendar.getInstance(TimeZone.getTimeZone(timezone));
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
        dateOther = otherCalendar.getTime();

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

        //Log.d("sunset: ", sunsetCalendarToday.toString() + " (" + sunsetCalendarOther.getTimeZone().getID() + ")");
        calculated = true;
    }
}


