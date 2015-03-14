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

import com.forrestguice.suntimeswidget.SuntimesWidgetSettings;

import java.util.Calendar;

public interface SuntimesCalculator
{
    public String name();
    public void init( SuntimesWidgetSettings.Location location, String timezone );

    public Calendar getCivilSunriseCalendarForDate( Calendar date );
    public Calendar getNauticalSunriseCalendarForDate( Calendar date );
    public Calendar getAstronomicalSunriseCalendarForDate( Calendar date );
    public Calendar getOfficialSunriseCalendarForDate( Calendar date );

    public Calendar getCivilSunsetCalendarForDate( Calendar date );
    public Calendar getNauticalSunsetCalendarForDate( Calendar date );
    public Calendar getAstronomicalSunsetCalendarForDate( Calendar date );
    public Calendar getOfficialSunsetCalendarForDate( Calendar date );
}
