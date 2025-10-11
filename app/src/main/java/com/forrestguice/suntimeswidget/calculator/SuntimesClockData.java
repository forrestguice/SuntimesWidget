/**
    Copyright (C) 2019 Forrest Guice
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

import java.util.Calendar;

public class SuntimesClockData extends SuntimesData
{
    public SuntimesClockData(Object context, int appWidgetId) {
        initFromSettings(context, appWidgetId);
    }
    public SuntimesClockData(Object context, int appWidgetId, String calculatorName) {
        initFromSettings(context, appWidgetId, calculatorName);
    }
    public SuntimesClockData(SuntimesClockData other) {
        initFromOther(other);
    }

    @Override
    public void calculate(Object context)
    {
        //Log.v("SuntimesWidgetData", "location_mode: " + locationMode.name());
        //Log.v("SuntimesWidgetData", "latitude: " + location.getLatitude());
        //Log.v("SuntimesWidgetData", "longitude: " + location.getLongitude());
        //Log.v("SuntimesWidgetData", "timezone_mode: " + timezoneMode.name());
        //Log.v("SuntimesWidgetData", "timezone: " + timezone);

        initCalculator();
        initTimezone(getDataSettings(context));   // reinit

        todaysCalendar = Calendar.getInstance(timezone);
        otherCalendar = Calendar.getInstance(timezone);

        if (todayIsNotToday())
        {
            todaysCalendar.setTimeInMillis(todayIs.getTimeInMillis());
            otherCalendar.setTimeInMillis(todayIs.getTimeInMillis());
        }

        date = todaysCalendar.getTime();
        dateOther = otherCalendar.getTime();

        super.calculate(context);
    }
}


