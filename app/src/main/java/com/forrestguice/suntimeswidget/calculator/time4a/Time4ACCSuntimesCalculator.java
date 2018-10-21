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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;

import net.time4j.calendar.astro.StdSolarCalculator;


public class Time4ACCSuntimesCalculator extends Time4ASuntimesCalculator implements SuntimesCalculator
{
    public static final String NAME = "time4a-cc";
    public static final String REF = "com.forrestguice.suntimeswidget.calculator.time4a.Time4ACCSuntimesCalculator";
    public static final String LINK = "time4j.net";
    public static final int[] FEATURES = new int[] { FEATURE_RISESET, FEATURE_SOLSTICE, FEATURE_GOLDBLUE, FEATURE_POSITION, FEATURE_ALTITUDE };

    public Time4ACCSuntimesCalculator() { /* EMPTY */ }

    @Override
    public String name()
    {
        return NAME;
    }

    @Override
    public int[] getSupportedFeatures()
    {
        return Time4ACCSuntimesCalculator.FEATURES;
    }

    @Override
    public StdSolarCalculator getCalculator()
    {
        return StdSolarCalculator.CC;
    }

    public static SuntimesCalculatorDescriptor getDescriptor()
    {
        return new SuntimesCalculatorDescriptor(Time4ACCSuntimesCalculator.NAME, Time4ACCSuntimesCalculator.LINK, Time4ACCSuntimesCalculator.REF, R.string.calculator_displayString_time4a_cc, Time4ACCSuntimesCalculator.FEATURES);
    }

}

