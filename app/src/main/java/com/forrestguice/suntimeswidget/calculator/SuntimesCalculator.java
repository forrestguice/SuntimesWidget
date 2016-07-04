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

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;

/**
 * An interface used by the widget when calculating/displaying sunrise and sunset times. Instances
 * of this interface are intended to be thin wrappers around third party code.
 */
public interface SuntimesCalculator
{
    String name();
    void init( WidgetSettings.Location location, String timezone );

    Calendar getAstronomicalSunriseCalendarForDate( Calendar date );
    Calendar getNauticalSunriseCalendarForDate( Calendar date );
    Calendar getCivilSunriseCalendarForDate( Calendar date );
    Calendar getOfficialSunriseCalendarForDate( Calendar date );

    Calendar getSolarNoonCalendarForDate( Calendar date );

    Calendar getOfficialSunsetCalendarForDate( Calendar date );
    Calendar getCivilSunsetCalendarForDate( Calendar date );
    Calendar getNauticalSunsetCalendarForDate( Calendar date );
    Calendar getAstronomicalSunsetCalendarForDate( Calendar date );

}
