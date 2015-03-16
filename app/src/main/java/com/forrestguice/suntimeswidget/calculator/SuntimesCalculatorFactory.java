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

import android.content.Context;
import android.util.Log;

import com.forrestguice.suntimeswidget.settings.SuntimesWidgetSettings;
import com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator;

public class SuntimesCalculatorFactory
{
    private SuntimesCalculatorDescriptor current;

    public SuntimesCalculatorFactory(SuntimesCalculatorDescriptor calculatorSetting)
    {
        current = calculatorSetting;
    }

    public SuntimesCalculator createCalculator(SuntimesWidgetSettings.Location location, String timezone)
    {
        SuntimesCalculator calculator;

        try {
            Class calculatorClass = Class.forName(current.getReference());
            calculator = (SuntimesCalculator)calculatorClass.newInstance();
            Log.d("createCalculator", "using calculator: " + calculator.name());

        } catch (Exception e1) {
            e1.printStackTrace();
            calculator = new SunriseSunsetSuntimesCalculator();
            Log.d("createCalculator", "failed to create calculator: " + current.name() + ", using default: " + calculator.name());
        }

        calculator.init(location, timezone);
        return calculator;
    }

    public static void initCalculators(Context context)
    {
        SuntimesCalculatorDescriptor calculatorSetting = new SuntimesCalculatorDescriptor(SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.REF);
        SuntimesCalculatorDescriptor.addValue(calculatorSetting);
    }
}
