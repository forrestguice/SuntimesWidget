/**
    Copyright (C) 2018 Forrest Guice
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

public class SuntimesMoonData extends SuntimesData
{
    private Context context;

    public SuntimesMoonData(Context context, int appWidgetId)
    {
        this.context = context;
        initFromSettings(context, appWidgetId);
    }
    public SuntimesMoonData(SuntimesMoonData other)
    {
        this.context = other.context;
        initFromOther(other);
    }

    /**
     * Property: timeMode
     */
    //private WidgetSettings.SolsticeEquinoxMode timeMode;
    /**public WidgetSettings.SolsticeEquinoxMode timeMode()
    {
        return timeMode;
    }*/
    /**public void setTimeMode( WidgetSettings.SolsticeEquinoxMode mode )
    {
        timeMode = mode;
    }*/

    /**
     * init from other SuntimesEquinoxSolsticeData object
     * @param other another SuntimesEquinoxSolsticeData obj
     */
    private void initFromOther( SuntimesMoonData other )
    {
        super.initFromOther(other);
        //this.timeMode = other.timeMode();
    }

    /**
     * init from shared preferences
     * @param context
     * @param appWidgetId
     */
    @Override
    public void initFromSettings(Context context, int appWidgetId)
    {
        super.initFromSettings(context, appWidgetId);
        //timeMode = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
    }

    public void calculate()
    {
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

        /**switch (timeMode)
        {
            default:
                eventCalendarThisYear = calculator.getWinterSolsticeForYear(todaysCalendar);
                eventCalendarOtherYear = calculator.getWinterSolsticeForYear(otherCalendar);
                break;
        }*/

        super.calculate();
    }
}


