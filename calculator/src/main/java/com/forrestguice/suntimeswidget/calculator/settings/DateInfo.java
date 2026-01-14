/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.settings;

import java.util.Calendar;
import java.util.TimeZone;

public class DateInfo
{
    private int year = -1, month = -1, day = -1;

    public DateInfo(Calendar date)
    {
        this(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }
    public DateInfo(int year, int month, int day )
    {
        this.year = year;
        this.month = month;
        this.day = day;
    }
    public DateInfo(long timestamp)
    {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);
        this.year = date.get(Calendar.YEAR);
        this.month = date.get(Calendar.MONTH);
        this.day = date.get(Calendar.DAY_OF_MONTH);
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }

    public Calendar getCalendar(TimeZone timezone, int hour, int minute) {
        Calendar calendar = Calendar.getInstance(timezone);
        calendar.set(getYear(), getMonth(), getDay(), hour, minute, 0);
        return calendar;
    }

    public boolean isSet()
    {
        return (year != -1 && month != -1 && day != -1);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DateInfo))
        {
            return false;
        } else {
            DateInfo that = (DateInfo)obj;
            return (this.getYear() == that.getYear()) && (this.getMonth() == that.getMonth()) && (this.getDay() == that.getDay());
        }
    }

    @Override
    public int hashCode()
    {
        int hash = Integer.valueOf(year).hashCode();
        hash = hash * 37 + (Integer.valueOf(month).hashCode());
        hash = hash * 37 + (Integer.valueOf(day).hashCode());
        return hash;
    }

    public static boolean isToday(DateInfo date)
    {
        DateInfo now = new DateInfo(Calendar.getInstance());
        return now.equals(date);
    }
}
