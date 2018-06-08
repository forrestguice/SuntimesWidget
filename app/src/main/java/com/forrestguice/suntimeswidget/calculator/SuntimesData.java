/**
    Copyright (C) 2014-2018 Forrest Guice
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
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SuntimesData
{
    public static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    /**
     * Property: appWidgetID
     * The appWidgetID that was used to initialize from settings (cached), may be null.
     */
    protected Integer appWidgetID = null;
    public Integer appWidgetID()
    {
        return appWidgetID;
    }

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
    protected TimeZone timezone;
    public TimeZone timezone()
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
     * @param other another instance of SuntimesData
     */
    protected void initFromOther( SuntimesData other )
    {
        this.appWidgetID = other.appWidgetID;
        this.calculatorMode = other.calculatorMode();
        this.locationMode = other.locationMode();
        this.timezoneMode = other.timezoneMode();
        this.location = other.location();
        this.timezone = other.timezone();
        this.todayIs = other.todayIs();

        this.calculator = other.calculator;
        this.calculated = other.isCalculated();
        //this.date = other.date();
        //this.dateOther = other.dateOther();
    }

    /**
     * init from shared preferences
     * @param context a context used to access shared prefs
     * @param appWidgetId the widgetID to load settings from (0 for app)
     */
    protected void initFromSettings(Context context, int appWidgetId)
    {
        initFromSettings(context, appWidgetId, "");
    }
    protected void initFromSettings(Context context, int appWidgetId, String calculatorName)
    {
        this.appWidgetID = appWidgetId;
        calculated = false;

        // from general settings
        calculatorMode = WidgetSettings.loadCalculatorModePref(context, appWidgetId, calculatorName);

        // from location settings
        location = WidgetSettings.loadLocationPref(context, appWidgetId);
        locationMode = WidgetSettings.loadLocationModePref(context, appWidgetId);

        // from timezone settings
        timezone = TimeZone.getTimeZone(WidgetSettings.loadTimezonePref(context, appWidgetId));
        timezoneMode = WidgetSettings.loadTimezoneModePref(context, appWidgetId);

        switch (timezoneMode)
        {
            case CUSTOM_TIMEZONE:
                // empty; use preset timezone value
                break;

            case CURRENT_TIMEZONE:
                timezone = TimeZone.getDefault();
                break;

            case SOLAR_TIME:
                WidgetSettings.SolarTimeMode solarMode = WidgetSettings.loadSolarTimeModePref(context, appWidgetId);
                switch (solarMode)
                {
                    case APPARENT_SOLAR_TIME:
                        timezone = WidgetTimezones.apparentSolarTime(context, location);
                        break;

                    default:
                        timezone = WidgetTimezones.localMeanTime(context, location);
                        break;
                }
                break;
        }

        // from date settings
        WidgetSettings.DateMode dateMode = WidgetSettings.loadDateModePref(context, appWidgetId);
        if (dateMode == WidgetSettings.DateMode.CUSTOM_DATE)
        {
            Calendar customDate = Calendar.getInstance(timezone);
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

    public void setCalculator(SuntimesCalculator calculator, SuntimesCalculatorDescriptor descriptor)
    {
        this.calculator = calculator;
        this.calculatorMode = descriptor;
    }

    public void initCalculator(Context context)
    {
        if (this.calculator != null)
            return;

        final SuntimesCalculatorFactory calculatorFactory = initFactory(context);
        calculatorFactory.setFactoryListener(new SuntimesCalculatorFactory.FactoryListener()
        {
            @Override
            public void onCreateFallback(SuntimesCalculatorDescriptor descriptor)
            {
                Log.w("initCalculator",  "failed to initCalculator; using fallback...");
                calculatorMode = descriptor;
            }
        });

        if (calculatorMode == null) {
            calculatorMode = calculatorFactory.fallbackCalculatorDescriptor();
        }
        this.calculator = calculatorFactory.createCalculator(location, timezone);
    }

    public SuntimesCalculatorFactory initFactory(Context context)
    {
        return new SuntimesCalculatorFactory(context, calculatorMode);
    }

    /**
     * @return the start of today (@see calendar()), at 0h 0m 0s
     */
    public Calendar midnight()
    {
        Calendar midnight = null;
        if (todaysCalendar != null)
        {
            midnight = (Calendar) todaysCalendar.clone();
            midnight.set(Calendar.HOUR_OF_DAY, 0);
            midnight.set(Calendar.MINUTE, 0);
            midnight.set(Calendar.SECOND, 0);
        }
        return midnight;
    }

    /**
     * @return
     */
    public Calendar now()
    {
        return Calendar.getInstance(timezone());
    }

    /**
     * @param date
     * @return
     */
    public Calendar nowThen(Calendar date)
    {
        Calendar nowThen = now();
        nowThen.set(Calendar.YEAR, date.get(Calendar.YEAR));
        nowThen.set(Calendar.MONTH, date.get(Calendar.MONTH));
        nowThen.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
        return nowThen;
    }

    public static Calendar nowThen(Calendar now, Calendar date)
    {
        Calendar nowThen = (Calendar)now.clone();
        nowThen.set(Calendar.YEAR, date.get(Calendar.YEAR));
        nowThen.set(Calendar.MONTH, date.get(Calendar.MONTH));
        nowThen.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
        return nowThen;
    }

}


