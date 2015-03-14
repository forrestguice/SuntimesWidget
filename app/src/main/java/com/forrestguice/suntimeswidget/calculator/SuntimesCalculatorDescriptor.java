package com.forrestguice.suntimeswidget.calculator;

import java.security.InvalidParameterException;
import java.util.ArrayList;

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
        SuntimesCalculatorDescriptor[] array = new SuntimesCalculatorDescriptor[calculators.size()];
        for (int i=0; i<calculators.size(); i++)
        {
            array[i] = (SuntimesCalculatorDescriptor)calculators.get(i);
        }
        return array;
    }

    public static SuntimesCalculatorDescriptor valueOf(String value)
    {
        value = value.trim().toLowerCase();
        SuntimesCalculatorDescriptor[] values = SuntimesCalculatorDescriptor.values();
        for (int i=0; i<values.length; i++)
        {
            SuntimesCalculatorDescriptor calculator = values[i];
            if (calculator.name().equals(value) || value.equals("any"))
            {
                return values[i];
            }
        }
        throw new InvalidParameterException("Calculator value for " + value + " not found.");
    }

    private String name;
    private String displayString;
    private String calculatorRef;

    public SuntimesCalculatorDescriptor(String name, String displayString, String classRef)
    {
        this.name = name;
        this.displayString = displayString;
        this.calculatorRef = classRef;
    }

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

    public String toString()
    {
        return displayString;
    }

    public String name()
    {
        return name;
    }

    public String getDisplayString()
    {
        return displayString;
    }

    public String getReference()
    {
        return calculatorRef;
    }

    @Override
    public boolean equals(Object other)
    {
        return this.toString().equals(other.toString());
    }

    @Override
    public int compareTo(Object other)
    {
        return this.toString().compareTo(other.toString());
    }
}
