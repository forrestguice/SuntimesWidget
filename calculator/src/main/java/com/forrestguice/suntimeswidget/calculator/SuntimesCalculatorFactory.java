/**
    Copyright (C) 2014-2018 Forrest Guice
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

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import java.util.TimeZone;

/**
 * A factory class that creates instances of SuntimesCalculator. The specific implementation returned
 * by the factory's createCalculator method is controlled by the SuntimesCalculatorDescriptor arg
 * passed to the constructor.

 * The SuntimesCalculatorDescriptor specifies a (fully qualified) class string that is instantiated
 * using reflection when the createCalculator method is called. The descriptor identifies the
 * calculator using name(), the class to instantiate using getReference(), and the value to display
 * in the UI using getDisplayString().
 */
public class SuntimesCalculatorFactory
{
    private SuntimesCalculatorDescriptor current;

    /**
     * Create a SuntimesCalculatorFactory object with default implementation.
     */
    public SuntimesCalculatorFactory()
    {
        init(null);
    }

    /**
     * Create a SuntimesCalculatorFactory object.
     * @param calculatorSetting a SuntimesCalculatorDescriptor that specifies the implementation this factory creates
     */
    public SuntimesCalculatorFactory(@Nullable SuntimesCalculatorDescriptor calculatorSetting)
    {
        init(calculatorSetting);
    }

    private void init(@Nullable SuntimesCalculatorDescriptor calculatorSetting)
    {
        if (!SuntimesCalculatorDescriptor.initialized)
        {
            SuntimesCalculatorDescriptor.initCalculators();
        }

        if (calculatorSetting == null)
        {
            SuntimesCalculatorDescriptor desc = fallbackCalculatorDescriptor();
            SuntimesCalculatorDescriptor.addValue(desc);  // redundant
            this.current = desc;

        } else {
            this.current = calculatorSetting;
        }
    }

    /**
     * Create a calculator for a given location and timezone using the calculator descriptor that was
     * passed to the factory when it was created.
     * @param location a SuntimesWidgetSettings.Location specifying latitude and longitude
     * @param timezone a timezone string
     * @return a calculator object that implements SuntimesCalculator
     */
    public SuntimesCalculator createCalculator(Location location, TimeZone timezone)
    {
        //long bench_start = System.nanoTime();
        SuntimesCalculator calculator;
        try {
            //Log.d("createCalculator", "trying .oO( " + current.getReference() + " )");
            Class<?> calculatorClass = Class.forName(current.getReference());  // may fail if using proguard without exempting key classes
            //Log.d("createCalculator", "found class " + calculatorClass.getName());
            calculator = (SuntimesCalculator)calculatorClass.newInstance();
            //Log.d("createCalculator", "using .oO( " + calculator.name() + " ): " + timezone);

        } catch (Exception e1) {
            calculator = fallbackCalculator();
            signalCreatedFallback(fallbackCalculatorDescriptor());
            Log.e("createCalculator", "fail! .oO( " + current.getReference() + "), so instantiating default: " + calculator.getClass().getName() + " :: " + timezone);
        }
        calculator.init(location, timezone);

        //long bench_end = System.nanoTime();
        //Log.d("DEBUG", "created " + calculator.name() + " :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
        return calculator;
    }

    public SuntimesCalculator fallbackCalculator()
    {
        return new com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator();
    }
    public SuntimesCalculatorDescriptor fallbackCalculatorDescriptor()
    {
        return new SuntimesCalculatorDescriptor(SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.LINK, SunriseSunsetSuntimesCalculator.REF, -1, SunriseSunsetSuntimesCalculator.FEATURES);
    }

    /**
     * FactoryListener
     */
    public static abstract class FactoryListener
    {
        public void onCreateFallback(SuntimesCalculatorDescriptor descriptor) {}
    }

    @Nullable
    protected FactoryListener factoryListener = null;
    public void setFactoryListener( @Nullable FactoryListener listener )
    {
        factoryListener = listener;
    }
    public void clearFactoryListener()
    {
        factoryListener = null;
    }
    private void signalCreatedFallback(SuntimesCalculatorDescriptor descriptor)
    {
        if (factoryListener != null)
        {
            factoryListener.onCreateFallback(descriptor);
        }
    }
}
