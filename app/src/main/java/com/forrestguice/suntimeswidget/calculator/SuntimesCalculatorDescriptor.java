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

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * An instance of SuntimesCalculatorDescriptor specifies a calculator's name (see name()),
 * display string (see getDisplayString()), and (fully qualified) class string that can be
 * instantiated using reflection (see getReference()).
 *
 * SuntimesCalculatorDescriptor also keeps a static list of installed calculators. Descriptors may
 * be added or removed from this list using the addValue and removeValue methods. The values() method
 * will return the list as an array (suitable for use in an adaptor), and the valueOf(String)
 * method can be used to retrieve a descriptor from this list using its name. The ordinal() method
 * will return a descriptor's order within the list.
 */
public class SuntimesCalculatorDescriptor implements Comparable
{
    private static ArrayList<Object> calculators = new ArrayList<Object>();

    public static void addValue( SuntimesCalculatorDescriptor calculator )
    {
        if (!calculators.contains(calculator))
        {
            calculators.add(calculator);
        }
    }

    public static void removeValue( SuntimesCalculatorDescriptor calculator )
    {
        calculators.remove(calculator);
    }

    public static SuntimesCalculatorDescriptor[] values()
    {
        if (!SuntimesCalculatorFactory.initialized)
        {
            SuntimesCalculatorFactory.initCalculators();
        }

        SuntimesCalculatorDescriptor[] array = new SuntimesCalculatorDescriptor[calculators.size()];
        for (int i=0; i<calculators.size(); i++)
        {
            array[i] = (SuntimesCalculatorDescriptor)calculators.get(i);
        }
        return array;
    }

    public static SuntimesCalculatorDescriptor valueOf(String value)
    {
        if (!SuntimesCalculatorFactory.initialized)
        {
            SuntimesCalculatorFactory.initCalculators();
        }

        SuntimesCalculatorDescriptor descriptor = null;
        value = value.trim().toLowerCase();
        SuntimesCalculatorDescriptor[] values = SuntimesCalculatorDescriptor.values();
        for (int i=0; i<values.length; i++)
        {
            SuntimesCalculatorDescriptor calculator = values[i];
            if (calculator.name().equals(value) || value.equals("any"))
            {
                descriptor = calculator;
                break;
            }
        }

        if (descriptor == null) {
            throw new InvalidParameterException("Calculator value for " + value + " not found. ..btw there are " + values.length + " items total.");

        } else {
            return descriptor;
        }
    }

    private String name;
    private String displayString;
    private String calculatorRef;

    /**
     * Create a SuntimesCalculatorDescriptor object.
     * @param name the name of the SuntimesCalculator
     * @param displayString a short display string describing the calculator
     * @param classRef a fully qualified class string that can be used to instantiate the calculator via reflection
     */
    public SuntimesCalculatorDescriptor(String name, String displayString, String classRef)
    {
        this.name = name;
        this.displayString = displayString;
        this.calculatorRef = classRef;
    }

    /**
     * Get the order of this descriptor within the static list of recognized descriptors.
     * @return the order of this descriptor within the descriptor list (or -1 if not in the list)
     */
    public int ordinal()
    {
        int ordinal = -1;
        SuntimesCalculatorDescriptor[] values = SuntimesCalculatorDescriptor.values();
        for (int i=0; i<values.length; i++)
        {
            SuntimesCalculatorDescriptor calculator = values[i];
            if (calculator.name().equals(this.name))
            {
                ordinal = i;
                break;
            }
        }
        return ordinal;
    }

    /**
     * Get the calculator's name.
     * @return the name of the SuntimesCalculator this descriptor represents
     */
    public String name()
    {
        return name;
    }

    /**
     * Get a descriptive string that describes the calculator.
     * @return a display string for the SuntimesCalculator this descriptor represents
     */
    public String getDisplayString()
    {
        return displayString;
    }
    /**
     * @return the value of getDisplayString()
     */
    public String toString()
    {
        return displayString;
    }

    /**
     * Get the class string that points to the calculator's implementation.
     * @return a fully qualified class string that can be instantiated via reflection to obtain a SuntimesCalculator instance
     */
    public String getReference()
    {
        return calculatorRef;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof SuntimesCalculatorDescriptor))
        {
            return false;

        } else {
            SuntimesCalculatorDescriptor otherDescriptor = (SuntimesCalculatorDescriptor) other;
            return this.name().equals(otherDescriptor.name());
        }
    }

    @Override
    public int compareTo(Object other)
    {
        SuntimesCalculatorDescriptor otherDescriptor = (SuntimesCalculatorDescriptor)other;
        return this.name().compareTo(otherDescriptor.name());
    }
}
