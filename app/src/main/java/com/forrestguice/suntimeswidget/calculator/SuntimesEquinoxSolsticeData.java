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

package com.forrestguice.suntimeswidget.calculator;

import android.content.Context;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

public class SuntimesEquinoxSolsticeData extends SuntimesData
{
    private Context context;

    public SuntimesEquinoxSolsticeData(Context context, int appWidgetId)
    {
        this.context = context;
        initFromSettings(context, appWidgetId);
    }
    public SuntimesEquinoxSolsticeData(SuntimesEquinoxSolsticeData other)
    {
        this.context = other.context;
        initFromOther(other);
    }

    /**
     * Property: timeMode
     */
    private WidgetSettings.SolsticeEquinoxMode timeMode;
    public WidgetSettings.SolsticeEquinoxMode timeMode()
    {
        return timeMode;
    }
    public void setTimeMode( WidgetSettings.SolsticeEquinoxMode mode )
    {
        timeMode = mode;
    }

    /**
     * init from other SuntimesEquinoxSolsticeData object
     * @param other another SuntimesEquinoxSolsticeData obj
     */
    private void initFromOther( SuntimesEquinoxSolsticeData other )
    {
        super.initFromOther(other);
        this.timeMode = other.timeMode();
    }

    /**
     * init from shared preferences
     * @param context a context used to access shared prefs
     * @param appWidgetId the widgetID to load settings from (0 for app)
     */
    @Override
    public void initFromSettings(Context context, int appWidgetId)
    {
        super.initFromSettings(context, appWidgetId);
        timeMode = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
    }

    /**
     * result: eventCalendarUpcoming
     */
    public Calendar eventCalendarUpcoming(Calendar now)
    {
        Calendar event = eventCalendarThisYear();
        if (now.after(event))
        {
            event = eventCalendarOtherYear();
        }
        return event;
    }

    public Calendar eventCalendarClosest(Calendar now)
    {
        long timeDeltaMin = Long.MAX_VALUE;
        Calendar closest = eventCalendarThisYear;
        Calendar[] events = {eventCalendarThisYear(), eventCalendarOtherYear()};
        for (Calendar event : events)
        {
            if (event != null)
            {
                long timeDelta = Math.abs(event.getTimeInMillis() - now.getTimeInMillis());
                if (timeDelta < timeDeltaMin)
                {
                    timeDeltaMin = timeDelta;
                    closest = event;
                }
            }
        }
        return closest;
    }

    /**
     * @return true data is stale (upcoming event is in the past)
     */
    public boolean isStale(Calendar now)
    {
        Calendar event = eventCalendarUpcoming(now);
        return now.after(event);
    }

    /**
     * result: eventCalendarThisYear
     */
    private Calendar eventCalendarThisYear;
    public Calendar eventCalendarThisYear()
    {
        return eventCalendarThisYear;
    }

    /**
     * result: eventCalendarOtherYear
     */
    private Calendar eventCalendarOtherYear;
    public Calendar eventCalendarOtherYear()
    {
        return eventCalendarOtherYear;
    }

    public void calculate()
    {
        //Log.v("SuntimesWidgetData", "time mode: " + timeMode);
        //Log.v("SuntimesWidgetData", "location_mode: " + locationMode.name());
        //Log.v("SuntimesWidgetData", "latitude: " + location.getLatitude());
        //Log.v("SuntimesWidgetData", "longitude: " + location.getLongitude());
        //Log.v("SuntimesWidgetData", "timezone_mode: " + timezoneMode.name());
        //Log.v("SuntimesWidgetData", "timezone: " + timezone);

        SuntimesCalculatorFactory calculatorFactory = new SuntimesCalculatorFactory(context, calculatorMode);
        SuntimesCalculator calculator = calculatorFactory.createCalculator(location, timezone);

        todaysCalendar = Calendar.getInstance(timezone);
        otherCalendar = Calendar.getInstance(timezone);

        if (todayIsNotToday())
        {
            todaysCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
            otherCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
        }

        otherCalendar.add(Calendar.YEAR, 1);

        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        switch (timeMode)
        {
            case EQUINOX_VERNAL:
                eventCalendarThisYear = calculator.getVernalEquinoxForYear(todaysCalendar);
                eventCalendarOtherYear = calculator.getVernalEquinoxForYear(otherCalendar);
                break;

            case SOLSTICE_SUMMER:
                eventCalendarThisYear = calculator.getSummerSolsticeForYear(todaysCalendar);
                eventCalendarOtherYear = calculator.getSummerSolsticeForYear(otherCalendar);
                break;

            case EQUINOX_AUTUMNAL:
                eventCalendarThisYear = calculator.getAutumnalEquinoxForYear(todaysCalendar);
                eventCalendarOtherYear = calculator.getAutumnalEquinoxForYear(otherCalendar);
                break;

            case SOLSTICE_WINTER:
            default:
                eventCalendarThisYear = calculator.getWinterSolsticeForYear(todaysCalendar);
                eventCalendarOtherYear = calculator.getWinterSolsticeForYear(otherCalendar);
                break;
        }

        super.calculate();
    }
}


