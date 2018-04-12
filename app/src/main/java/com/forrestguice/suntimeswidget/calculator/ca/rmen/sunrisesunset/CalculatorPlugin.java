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


package com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset;

import android.app.Activity;

import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorInfo;

public class CalculatorPlugin extends Activity implements SuntimesCalculatorInfo
{
    private static final SuntimesCalculatorDescriptor descriptor = SunriseSunsetSuntimesCalculator.getDescriptor();

    @Override
    public String getName()
    {
        return descriptor.getName();
    }

    @Override
    public String getDisplayString()
    {
        return descriptor.getDisplayString();
    }

    @Override
    public String getReference()
    {
        return descriptor.getReference();
    }

    @Override
    public int getDisplayStringResID()
    {
        return descriptor.getDisplayStringResID();
    }

    @Override
    public int[] getSupportedFeatures()
    {
        return descriptor.getSupportedFeatures();
    }
}