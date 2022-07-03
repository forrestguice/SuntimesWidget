/**
    Copyright (C) 2017-2022 Forrest Guice
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

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
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
    public SuntimesEquinoxSolsticeData(Context context, int appWidgetId, String calculatorName)
    {
        this.context = context;
        initFromSettings(context, appWidgetId, calculatorName);
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
     * Property: localizeHemisphere
     */
    public boolean localizeHemisphere() {
        return localizeHemisphere;
    }
    public void setLocalizeHemisphere(boolean value) {
        localizeHemisphere = value;
    }
    protected boolean localizeHemisphere;

    /**
     * init from other SuntimesEquinoxSolsticeData object
     * @param other another SuntimesEquinoxSolsticeData obj
     */
    private void initFromOther( SuntimesEquinoxSolsticeData other )
    {
        super.initFromOther(other);
        this.timeMode = other.timeMode();
        this.localizeHemisphere = other.localizeHemisphere;
    }

    /**
     * init from shared preferences
     * @param context a context used to access shared prefs
     * @param appWidgetId the widgetID to load settings from (0 for app)
     */
    @Override
    public void initFromSettings(Context context, int appWidgetId, String calculatorName)
    {
        super.initFromSettings(context, appWidgetId, calculatorName);
        timeMode = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
        localizeHemisphere = WidgetSettings.loadLocalizeHemispherePref(context, appWidgetId);
    }

    /**
     * result: eventCalendarUpcoming
     */
    public Calendar eventCalendarUpcoming(Calendar now) {
        Calendar event = eventCalendarThisYear();
        if (now.after(event)) {
            event = eventCalendarNextYear();
        }
        return event;
    }
    public Calendar eventCalendarRecent(Calendar now) {
        Calendar event = eventCalendarNextYear();
        if (!now.after(event)) {
            event = eventCalendarThisYear();
        }
        return event;
    }

    public Calendar eventCalendarClosest(Calendar now)
    {
        long timeDeltaMin = Long.MAX_VALUE;
        Calendar closest = eventCalendarThisYear;
        Calendar[] events = {eventCalendarThisYear(), eventCalendarNextYear()};
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
    public Calendar eventCalendarThisYear() {
        return eventCalendarThisYear;
    }

    /**
     * result: eventCalendarLastYear
     */
    private Calendar eventCalendarLastYear;
    public Calendar eventCalendarLastYear() {
        return eventCalendarLastYear;
    }

    /**
     * result: eventCalendarNextYear
     */
    private Calendar eventCalendarNextYear;
    public Calendar eventCalendarNextYear() {
        return eventCalendarNextYear;
    }

    public void initCalculator()
    {
        initCalculator(context);
    }
    
    public void calculate()
    {
        //Log.v("SuntimesWidgetData", "time mode: " + timeMode);
        //Log.v("SuntimesWidgetData", "location_mode: " + locationMode.name());
        //Log.v("SuntimesWidgetData", "latitude: " + location.getLatitude());
        //Log.v("SuntimesWidgetData", "longitude: " + location.getLongitude());
        //Log.v("SuntimesWidgetData", "timezone_mode: " + timezoneMode.name());
        //Log.v("SuntimesWidgetData", "timezone: " + timezone);

        Location location0 = location;
        if (!localizeHemisphere && (location != null && location.getLatitudeAsDouble() < 0)) {      // calculator returns localized times; force northern hemisphere
            double northLatitude = Math.abs(location.getLatitudeAsDouble());                        // by passing a modified location during calculator init
            location = new Location(location.getLabel(), Double.toString(northLatitude), location.getLongitude(), location.getAltitude());
        }
        initCalculator(context);
        location = location0;

        initTimezone(context);

        Calendar lastYearCalendar = Calendar.getInstance(timezone);
        Calendar thisYearCalendar = todaysCalendar = Calendar.getInstance(timezone);
        Calendar nextYearCalendar = otherCalendar = Calendar.getInstance(timezone);

        if (todayIsNotToday())
        {
            lastYearCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
            thisYearCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
            nextYearCalendar.set(todayIs.get(Calendar.YEAR), todayIs.get(Calendar.MONTH), todayIs.get(Calendar.DAY_OF_MONTH));
        }

        lastYearCalendar.add(Calendar.YEAR, -1);
        nextYearCalendar.add(Calendar.YEAR, 1);

        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        switch (timeMode)
        {
            case CROSS_SPRING:
                if (location.getLatitudeAsDouble() >= 0)
                {
                    eventCalendarLastYear = null;
                    eventCalendarThisYear = midpoint(calculator.getWinterSolsticeForYear(lastYearCalendar), calculator.getSpringEquinoxForYear(thisYearCalendar));
                    eventCalendarNextYear = midpoint(calculator.getWinterSolsticeForYear(thisYearCalendar), calculator.getSpringEquinoxForYear(nextYearCalendar));
                } else {
                    eventCalendarNextYear = midpoint(calculator.getWinterSolsticeForYear(lastYearCalendar), calculator.getSpringEquinoxForYear(lastYearCalendar));
                    eventCalendarThisYear = midpoint(calculator.getWinterSolsticeForYear(thisYearCalendar), calculator.getSpringEquinoxForYear(thisYearCalendar));
                    eventCalendarNextYear = midpoint(calculator.getWinterSolsticeForYear(nextYearCalendar), calculator.getSpringEquinoxForYear(nextYearCalendar));
                }
                break;

            case CROSS_AUTUMN:
                if (location.getLatitudeAsDouble() >= 0)
                {
                    eventCalendarLastYear = midpoint(calculator.getSummerSolsticeForYear(lastYearCalendar), calculator.getAutumnalEquinoxForYear(lastYearCalendar));
                    eventCalendarThisYear = midpoint(calculator.getSummerSolsticeForYear(thisYearCalendar), calculator.getAutumnalEquinoxForYear(thisYearCalendar));
                    eventCalendarNextYear = midpoint(calculator.getSummerSolsticeForYear(nextYearCalendar), calculator.getAutumnalEquinoxForYear(nextYearCalendar));
                } else {
                    eventCalendarNextYear = null;
                    eventCalendarThisYear = midpoint(calculator.getSummerSolsticeForYear(lastYearCalendar), calculator.getAutumnalEquinoxForYear(thisYearCalendar));
                    eventCalendarNextYear = midpoint(calculator.getSummerSolsticeForYear(thisYearCalendar), calculator.getAutumnalEquinoxForYear(nextYearCalendar));
                }
                break;

            case CROSS_SUMMER:
                eventCalendarLastYear = midpoint(calculator.getSpringEquinoxForYear(lastYearCalendar), calculator.getSummerSolsticeForYear(lastYearCalendar));
                eventCalendarThisYear = midpoint(calculator.getSpringEquinoxForYear(thisYearCalendar), calculator.getSummerSolsticeForYear(thisYearCalendar));
                eventCalendarNextYear = midpoint(calculator.getSpringEquinoxForYear(nextYearCalendar), calculator.getSummerSolsticeForYear(nextYearCalendar));
                break;

            case CROSS_WINTER:
                eventCalendarLastYear = midpoint(calculator.getAutumnalEquinoxForYear(lastYearCalendar), calculator.getWinterSolsticeForYear(lastYearCalendar));
                eventCalendarThisYear = midpoint(calculator.getAutumnalEquinoxForYear(thisYearCalendar), calculator.getWinterSolsticeForYear(thisYearCalendar));
                eventCalendarNextYear = midpoint(calculator.getAutumnalEquinoxForYear(nextYearCalendar), calculator.getWinterSolsticeForYear(nextYearCalendar));
                break;

            case EQUINOX_SPRING:
                eventCalendarLastYear = calculator.getSpringEquinoxForYear(lastYearCalendar);
                eventCalendarThisYear = calculator.getSpringEquinoxForYear(thisYearCalendar);
                eventCalendarNextYear = calculator.getSpringEquinoxForYear(nextYearCalendar);
                break;

            case SOLSTICE_SUMMER:
                eventCalendarLastYear = calculator.getSummerSolsticeForYear(lastYearCalendar);
                eventCalendarThisYear = calculator.getSummerSolsticeForYear(thisYearCalendar);
                eventCalendarNextYear = calculator.getSummerSolsticeForYear(nextYearCalendar);
                break;

            case EQUINOX_AUTUMNAL:
                eventCalendarLastYear = calculator.getAutumnalEquinoxForYear(lastYearCalendar);
                eventCalendarThisYear = calculator.getAutumnalEquinoxForYear(thisYearCalendar);
                eventCalendarNextYear = calculator.getAutumnalEquinoxForYear(nextYearCalendar);
                break;

            case SOLSTICE_WINTER:
            default:
                eventCalendarLastYear = calculator.getWinterSolsticeForYear(lastYearCalendar);
                eventCalendarThisYear = calculator.getWinterSolsticeForYear(thisYearCalendar);
                eventCalendarNextYear = calculator.getWinterSolsticeForYear(nextYearCalendar);
                break;
        }

        super.calculate();
    }

    public boolean isImplemented()
    {
        SuntimesCalculatorDescriptor calculatorDesc = calculatorMode();
        return calculatorDesc.hasRequestedFeature(SuntimesCalculator.FEATURE_SOLSTICE);
    }

    public long tropicalYearLength()
    {
        Calendar c1 = eventCalendarThisYear();
        Calendar c2 = eventCalendarNextYear();
        return ((c1 != null && c2 != null) ? (c2.getTimeInMillis() - c1.getTimeInMillis()) : 0);
    }
}


