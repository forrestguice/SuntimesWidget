/**
    Copyright (C) 2017 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.time4a;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import net.time4j.calendar.astro.StdSolarCalculator;


public class Time4ANOAASuntimesCalculator extends Time4ASuntimesCalculator implements SuntimesCalculator
{
    public static final String NAME = "time4a-noaa";
    public static final String REF = "com.forrestguice.suntimeswidget.calculator.time4a.Time4ANOAASuntimesCalculator";
    public static final String LINK = "time4j.net";

    public Time4ANOAASuntimesCalculator() { /* EMPTY */ }

    @Override
    public String name()
    {
        return NAME;
    }

    @Override
    public StdSolarCalculator getCalculator()
    {
        return StdSolarCalculator.NOAA;
    }

}

