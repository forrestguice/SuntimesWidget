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

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SuntimesData
{
    /**
     * Property: date ("today" ..but not really, @see todayIs)
     */
    protected Date date = new Date();
    public Date date() { return date; }

    /**
     * Property: calendar ("today" ..but not really, @see todayIs)
     */
    protected Calendar todaysCalendar;
    public Calendar calendar() { return todaysCalendar; }

    /**
     * Property: date ("other")
     */
    protected Date dateOther = new Date();
    public Date dateOther() { return dateOther; }

    /**
     * Property: calendar ("other")
     */
    protected Calendar otherCalendar;
    public Calendar getOtherCalendar() { return otherCalendar; }

    /**
     * Property: todayIs; the date to run the calculation on, if null then "now"
     */
    protected Calendar todayIs = null;
    public Calendar todayIs() { return todayIs; }
    public void setTodayIs(Calendar day) { todayIs = day; }
    public void setTodayIsToday() { todayIs = null; }
    public boolean todayIsNotToday() { return todayIs != null; }

    /**
     * Property: timezone
     */
    protected String timezone;
    public String timezone()
    {
        return timezone;
    }

    /**
     * Property: location
     */
    protected WidgetSettings.Location location;
    public WidgetSettings.Location location()
    {
        return location;
    }

    /**
     * Property: calculator (plugin descriptor)
     */
    protected SuntimesCalculatorDescriptor calculatorMode;
    public SuntimesCalculatorDescriptor calculatorMode()
    {
        return calculatorMode;
    }

    /**
     * Property: calculator (cached from calculate, potentially null)
     */
    protected SuntimesCalculator calculator = null;
    public SuntimesCalculator calculator()
    {
        return calculator;
    }

    /**
     * Property: location mode
     */
    protected WidgetSettings.LocationMode locationMode;
    public WidgetSettings.LocationMode locationMode()
    {
        return locationMode;
    }

    /**
     * Property: timezone mode
     */
    protected WidgetSettings.TimezoneMode timezoneMode;
    public WidgetSettings.TimezoneMode timezoneMode()
    {
        return timezoneMode;
    }

    /**
     * Property: isCalculated
     */
    protected boolean calculated = false;
    public boolean isCalculated()
    {
        return calculated;
    }

    /**
     * perform calculation on the data
     */
    public void calculate()
    {
        this.calculated = true;
    }

    /**
     * invalidate the calculation
     */
    public void invalidateCalculation()
    {
        this.calculated = false;
    }

    /**
     * init from another SuntimesData object
     * @param other
     */
    protected void initFromOther( SuntimesData other )
    {
        this.calculatorMode = other.calculatorMode();
        this.locationMode = other.locationMode();
        this.timezoneMode = other.timezoneMode();
        this.location = other.location();
        this.timezone = other.timezone();
        this.todayIs = other.todayIs();

        this.calculated = other.isCalculated();
    }

    /**
     * init from shared preferences
     * @param context
     * @param appWidgetId
     */
    protected void initFromSettings(Context context, int appWidgetId)
    {
        calculated = false;

        // from general settings
        calculatorMode = WidgetSettings.loadCalculatorModePref(context, appWidgetId);

        // from location settings
        location = WidgetSettings.loadLocationPref(context, appWidgetId);
        locationMode = WidgetSettings.loadLocationModePref(context, appWidgetId);

        // from timezone settings
        timezone = WidgetSettings.loadTimezonePref(context, appWidgetId);
        timezoneMode = WidgetSettings.loadTimezoneModePref(context, appWidgetId);
        if (timezoneMode == WidgetSettings.TimezoneMode.CURRENT_TIMEZONE)
        {
            timezone = TimeZone.getDefault().getID();
        }

        // from date settings
        WidgetSettings.DateMode dateMode = WidgetSettings.loadDateModePref(context, appWidgetId);
        if (dateMode == WidgetSettings.DateMode.CUSTOM_DATE)
        {
            Calendar customDate = Calendar.getInstance(TimeZone.getTimeZone(timezone));
            WidgetSettings.DateInfo dateInfo = WidgetSettings.loadDatePref(context, appWidgetId);
            if (dateInfo.isSet())
            {
                customDate.set(dateInfo.getYear(), dateInfo.getMonth(), dateInfo.getDay());
            } else {
                Log.w("SuntimesWidgetData", "Custom dateMode was set but a custom date was not! falling back today.");
            }
            setTodayIs(customDate);

        } else {
            setTodayIsToday();
        }
    }



}


