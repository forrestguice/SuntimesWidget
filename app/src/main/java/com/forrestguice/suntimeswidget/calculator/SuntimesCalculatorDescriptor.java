/**
    Copyright (C) 2014-2017 Forrest Guice
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
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Locale;

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

    public static SuntimesCalculatorDescriptor[] values( int[] requestedFeatures )
    {
        if (!SuntimesCalculatorFactory.initialized)
        {
            SuntimesCalculatorFactory.initCalculators();
        }

        ArrayList<SuntimesCalculatorDescriptor> matchingCalculators = new ArrayList<>();
        for (int i=0; i<calculators.size(); i++)
        {
            SuntimesCalculatorDescriptor descriptor = (SuntimesCalculatorDescriptor)calculators.get(i);
            if (descriptor.hasRequestedFeatures(requestedFeatures))
            {
                matchingCalculators.add(descriptor);
            }
        }
        SuntimesCalculatorDescriptor[] retValues = new SuntimesCalculatorDescriptor[matchingCalculators.size()];
        return matchingCalculators.toArray(retValues);
    }

    public static SuntimesCalculatorDescriptor valueOf(String value)
    {
        if (!SuntimesCalculatorFactory.initialized)
        {
            SuntimesCalculatorFactory.initCalculators();
        }

        SuntimesCalculatorDescriptor descriptor = null;
        if (value != null)
        {
            value = value.trim().toLowerCase(Locale.US);
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
        }

        if (descriptor == null) {
            throw new InvalidParameterException("Calculator value for " + value + " not found.");

        } else {
            return descriptor;
        }
    }

    private final String name;
    private String displayString;
    private final String calculatorRef;
    private int resID = -1;
    private int[] features = new int[] { SuntimesCalculator.FEATURE_RISESET };

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
    public SuntimesCalculatorDescriptor(String name, String displayString, String classRef, int resID)
    {
        this.name = name;
        this.displayString = displayString;
        this.calculatorRef = classRef;
        this.resID = resID;
    }
    public SuntimesCalculatorDescriptor(String name, String displayString, String classRef, int resID, int[] features)
    {
        this.name = name;
        this.displayString = displayString;
        this.calculatorRef = classRef;
        this.resID = resID;
        this.features = features;
    }

    /**
     * Get the order of this descriptor within the static list of recognized descriptors.
     * @return the order of this descriptor within the descriptor list (or -1 if not in the list)
     */
    public int ordinal()
    {
        SuntimesCalculatorDescriptor[] values = SuntimesCalculatorDescriptor.values();
        return ordinal(values);
    }
    public int ordinal( SuntimesCalculatorDescriptor[] values )
    {
        int ordinal = -1;
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
        return name;
    }

    /**
     * Get the class string that points to the calculator's implementation.
     * @return a fully qualified class string that can be instantiated via reflection to obtain a SuntimesCalculator instance
     */
    public String getReference()
    {
        return calculatorRef;
    }


    public int[] getSupportedFeatures()
    {
        return features;
    }

    public boolean hasRequestedFeatures( int[] requestedFeatures )
    {
        int[] supportedFeatures = getSupportedFeatures();
        for (int feature : requestedFeatures)
        {
            boolean isSupported = false;
            for (int supported : supportedFeatures)
            {
                if (feature == supported)
                {
                    isSupported = true;
                    break;
                }
            }
            if (!isSupported)
                return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || !(other instanceof SuntimesCalculatorDescriptor))
        {
            return false;

        } else {
            SuntimesCalculatorDescriptor otherDescriptor = (SuntimesCalculatorDescriptor) other;
            return this.name().equals(otherDescriptor.name());
        }
    }

    @Override
    public int compareTo(@NonNull Object other)
    {
        SuntimesCalculatorDescriptor otherDescriptor = (SuntimesCalculatorDescriptor)other;
        return this.name().compareTo(otherDescriptor.name());
    }

    public void initDisplayStrings( Context context )
    {
        if (resID != -1)
        {
            this.displayString = context.getString(resID);
        }
    }

    /**
     * SuntimesCalculatorDescriptorListAdapter
     */
    public static class SuntimesCalculatorDescriptorListAdapter extends ArrayAdapter<SuntimesCalculatorDescriptor>
    {
        private int layoutID, dropDownLayoutID;

        public SuntimesCalculatorDescriptorListAdapter(@NonNull Context context, @LayoutRes int resource, @LayoutRes int dropDownResource, @NonNull SuntimesCalculatorDescriptor[] entries)
        {
            super(context, resource, entries);
            this.layoutID = resource;
            this.dropDownLayoutID = dropDownResource;
            initDisplayStrings(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(this.dropDownLayoutID, parent, false);
            }
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            TextView summaryText = (TextView) view.findViewById(android.R.id.text2);

            SuntimesCalculatorDescriptor descriptor = getItem(position);
            if (descriptor != null)
            {
                text.setText(descriptor.name());
                if (summaryText != null)
                {
                    summaryText.setText(descriptor.getDisplayString());
                }

            } else {
                text.setText("");
                if (summaryText != null)
                {
                    summaryText.setText("");
                }
            }
            return view;
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View view = convertView;
            if (view == null)
            {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(this.layoutID, parent, false);
            }

            SuntimesCalculatorDescriptor descriptor = getItem(position);
            TextView text = (TextView)view.findViewById(android.R.id.text1);
            text.setText(descriptor != null ? descriptor.name() : "");
            return view;
        }

        public void initDisplayStrings(Context context)
        {
            for (SuntimesCalculatorDescriptor value : values())
            {
                value.initDisplayStrings(context);
            }
        }
    }
}
