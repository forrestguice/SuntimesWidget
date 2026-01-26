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

package com.forrestguice.suntimeswidget.calculator;

import com.forrestguice.suntimeswidget.R;

import com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4ACCSuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4ANOAASuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4ASimpleSuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.time4a.Time4ASuntimesCalculator;

public class DefaultCalculatorDescriptors implements SuntimesCalculatorDescriptors
{
    public SuntimesCalculatorDescriptor[] values()
    {
        return new SuntimesCalculatorDescriptor[] {
                DefaultCalculatorDescriptors.SunriseSunsetJava(),
                DefaultCalculatorDescriptors.CarmenSunriseSunset(),
                DefaultCalculatorDescriptors.Time4A_Simple(),
                DefaultCalculatorDescriptors.Time4A_NOAA(),
                DefaultCalculatorDescriptors.Time4A_CC(),
                DefaultCalculatorDescriptors.Time4A_4J(),
        };
    }

    public static SuntimesCalculatorDescriptor SunriseSunsetJava() {
        return new SuntimesCalculatorDescriptor(SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.LINK, SunriseSunsetSuntimesCalculator.REF,
                R.string.calculator_displayString_sunrisesunsetlib, SunriseSunsetSuntimesCalculator.FEATURES);
    }

    public static SuntimesCalculatorDescriptor CarmenSunriseSunset() {
        return new SuntimesCalculatorDescriptor(com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset.SunriseSunsetSuntimesCalculator.NAME, com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset.SunriseSunsetSuntimesCalculator.LINK, com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset.SunriseSunsetSuntimesCalculator.REF,
                R.string.calculator_displayString_caarmensunrisesunset, com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset.SunriseSunsetSuntimesCalculator.FEATURES);
    }

    public static SuntimesCalculatorDescriptor Time4A_CC() {
        return new SuntimesCalculatorDescriptor(Time4ACCSuntimesCalculator.NAME, Time4ACCSuntimesCalculator.LINK, Time4ACCSuntimesCalculator.REF, R.string.calculator_displayString_time4a_cc, Time4ACCSuntimesCalculator.FEATURES);
    }

    public static SuntimesCalculatorDescriptor Time4A_NOAA() {
        return new SuntimesCalculatorDescriptor(Time4ANOAASuntimesCalculator.NAME, Time4ANOAASuntimesCalculator.LINK, Time4ANOAASuntimesCalculator.REF, R.string.calculator_displayString_time4a_noaa, Time4ANOAASuntimesCalculator.FEATURES);
    }

    public static SuntimesCalculatorDescriptor Time4A_4J() {
        return new SuntimesCalculatorDescriptor(Time4A4JSuntimesCalculator.NAME, Time4A4JSuntimesCalculator.LINK, Time4A4JSuntimesCalculator.REF, R.string.calculator_displayString_time4a_4j, Time4A4JSuntimesCalculator.FEATURES);
    }

    public static SuntimesCalculatorDescriptor Time4A_Simple() {
        return new SuntimesCalculatorDescriptor(Time4ASimpleSuntimesCalculator.NAME, Time4ASimpleSuntimesCalculator.LINK, Time4ASimpleSuntimesCalculator.REF, R.string.calculator_displayString_time4a_simple, Time4ASuntimesCalculator.FEATURES);
    }
}
