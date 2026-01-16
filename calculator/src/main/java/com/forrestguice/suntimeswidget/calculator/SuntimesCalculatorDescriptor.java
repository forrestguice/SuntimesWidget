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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculatorInfo;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

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
 *
 * The list of installed calculators should be initialized using the initCalculators() method. Using the
 * SuntimesCalculatorDescriptor.values() and SuntimesCalculatorDescriptor.valueOf() methods will
 * trigger lazy initialization.

 * SuntimesCalculatorDescriptor knows about the following implementations:
 *
 *   * sunrisesunsetlib (fallback)
 *     :: com.forrestguice.suntimeswidget.calculator.sunrisesunset_java.SunriseSunsetSuntimesCalculator.class
 *
 *   * ca.rmen.sunrisesunset
 *     :: com.forrestguice.suntimeswidget.calculator.ca.rmen.sunrisesunset.SunriseSunsetSuntimesCalculator.class
 *
 *   * time4a
 *     :: com.forrestguice.suntimeswidget.calculator.time4a.Time4ASimpleSuntimesCalculator.class
 *     :: com.forrestguice.suntimeswidget.calculator.time4a.Time4ANOAASuntimesCalculator.class
 *     :: com.forrestguice.suntimeswidget.calculator.time4a.Time4ACCSuntimesCalculator.class
 *     :: com.forrestguice.suntimeswidget.calculator.time4a.Time4A4JSuntimesCalculator.class
 *
 */
@SuppressWarnings("Convert2Diamond")
public class SuntimesCalculatorDescriptor implements Comparable, SuntimesCalculatorInfo
{
    /*public static final String CATEGORY_SUNTIMES_CALCULATOR = "com.forrestguice.suntimeswidget.SUNTIMES_CALCULATOR";
    public static final String KEY_NAME = "CalculatorName";
    public static final String KEY_DISPLAYSTRING = "CalculatorDisplayString";
    public static final String KEY_REFERENCE = "CalculatorReference";
    public static final String KEY_FEATURES = "CalculatorFeatures";*/

    private static final ArrayList<Object> calculators = new ArrayList<Object>();

    //public static final String LOGTAG = "CalculatorDescriptor";

    public static void initDefaultDescriptors(SuntimesCalculatorDescriptors value) {
        descriptors = value;
        if (initialized) {
            initCalculators();    // reinitialize
        }
    }
    private static SuntimesCalculatorDescriptors descriptors = new SuntimesCalculatorDescriptors() {
        @Override
        public SuntimesCalculatorDescriptor[] values() {
            return new SuntimesCalculatorDescriptor[] {
                    new SuntimesCalculatorDescriptor(SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.LINK, SunriseSunsetSuntimesCalculator.REF, -1, SunriseSunsetSuntimesCalculator.FEATURES)
            };
        }
    };
    
    protected static boolean initialized = false;
    public static void initCalculators()
    {
        if (descriptors != null) {
            for (SuntimesCalculatorDescriptor descriptor : descriptors.values()) {
                SuntimesCalculatorDescriptor.addValue(descriptor);
            }
        } else Log.e("initCalculators", "descriptor list is null!");


        /*SuntimesCalculatorDescriptor.addValue(SuntimesCalculatorDescriptors.SunriseSunsetJava());
        SuntimesCalculatorDescriptor.addValue(SuntimesCalculatorDescriptors.CarmenSunriseSunset());
        SuntimesCalculatorDescriptor.addValue(SuntimesCalculatorDescriptors.Time4A_Simple());
        SuntimesCalculatorDescriptor.addValue(SuntimesCalculatorDescriptors.Time4A_NOAA());
        SuntimesCalculatorDescriptor.addValue(SuntimesCalculatorDescriptors.Time4A_CC());
        SuntimesCalculatorDescriptor.addValue(SuntimesCalculatorDescriptors.Time4A_4J());*/

        /*boolean scanForPlugins = (context != null && AppSettings.loadScanForPluginsPref(context));
        if (scanForPlugins)
        {
            PackageManager packageManager = context.getPackageManager();
            Intent packageQuery = new Intent(Intent.ACTION_RUN);    // get a list of installed plugins
            packageQuery.addCategory(CATEGORY_SUNTIMES_CALCULATOR);
            List<ResolveInfo> packages = packageManager.queryIntentActivities(packageQuery, PackageManager.GET_META_DATA);
            Log.i(LOGTAG, "Scanning for calculator plugins... found " + packages.size());

            for (ResolveInfo packageInfo : packages)
            {
                if (packageInfo.activityInfo != null
                        && packageInfo.activityInfo.metaData != null)
                {
                    String calculatorName = packageInfo.activityInfo.metaData.getString(KEY_NAME);
                    String calculatorDisplayString = packageInfo.activityInfo.metaData.getString(KEY_DISPLAYSTRING);
                    String calculatorDisplayReference = packageInfo.activityInfo.metaData.getString(KEY_REFERENCE);
                    int[] calculatorFeatures = parseFlags(packageInfo.activityInfo.metaData.getString(KEY_FEATURES));

                    SuntimesCalculatorDescriptor descriptor = new SuntimesCalculatorDescriptor(calculatorName, calculatorDisplayString, calculatorDisplayReference, -1, calculatorFeatures);
                    descriptor.setIsPlugin(true);
                    SuntimesCalculatorDescriptor.addValue(descriptor);
                    Log.i(LOGTAG, "..initialized calculator plugin: " + descriptor.toString());
                }
            }
        }*/

        initialized = true;
        //Log.d("CalculatorFactory", "Initialized suntimes calculator list.");
    }

    public static void reinitCalculators()
    {
        calculators.clear();
        initCalculators();
    }

    private static int[] parseFlags( String flagString )
    {
        ArrayList<Integer> flagList = new ArrayList<>();
        String[] flags = (flagString != null ? flagString.split(",") : new String[0]);
        for (String flag : flags)
        {
            flag = flag.trim();
            try {
                flagList.add(Integer.parseInt(flag));
            } catch (NumberFormatException e) {
                Log.w("initCalculators", "ignoring invalid flag: " + flag);
            }
        }
        int[] retValue = new int[flagList.size()];
        for (int i=0; i<retValue.length; i++)
        {
            retValue[i] = flagList.get(i);
        }
        return retValue;
    }

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
        if (!initialized)
        {
            initCalculators();
        }

        SuntimesCalculatorDescriptor[] array = new SuntimesCalculatorDescriptor[calculators.size()];
        for (int i=0; i<calculators.size(); i++)
        {
            array[i] = (SuntimesCalculatorDescriptor)calculators.get(i);
        }
        return array;
    }

    public static SuntimesCalculatorDescriptor[] values(int[] requestedFeatures )
    {
        if (!initialized)
        {
            initCalculators();
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
        if (!initialized)
        {
            initCalculators();
        }

        SuntimesCalculatorDescriptor descriptor = null;
        if (value != null)
        {
            value = value.trim().toLowerCase(Locale.US);
            SuntimesCalculatorDescriptor[] values = SuntimesCalculatorDescriptor.values();
            //noinspection ForLoopReplaceableByForEach
            for (int i=0; i<values.length; i++)
            {
                SuntimesCalculatorDescriptor calculator = values[i];
                if (calculator.getName().equals(value) || value.equals("any"))
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
    private boolean isPlugin = false;

    /**
     * Create a SuntimesCalculatorDescriptor object.
     * @param name the name of the SuntimesCalculator
     * @param displayString a short display string describing the calculator
     * @param classRef a fully qualified class string that can be used to instantiate the calculator via reflection
     */
    public SuntimesCalculatorDescriptor(@NonNull String name, String displayString, String classRef)
    {
        this.name = name;
        this.displayString = displayString;
        this.calculatorRef = classRef;
    }
    public SuntimesCalculatorDescriptor(@NonNull String name, String displayString, String classRef, int resID)
    {
        this.name = name;
        this.displayString = displayString;
        this.calculatorRef = classRef;
        this.resID = resID;
    }
    public SuntimesCalculatorDescriptor(@NonNull String name, String displayString, String classRef, int resID, int[] features)
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
            if (calculator.getName().equals(this.name))
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
    @NonNull
    public String getName() {
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
    public void setDisplayString(String value) {
        displayString = value;
    }

    /**
     * @return the value of getDisplayString()
     */
    @NonNull
    public String toString() {
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

    @Override
    public int getDisplayStringResID()
    {
        return resID;
    }

    public int[] getSupportedFeatures()
    {
        return features;
    }

    public boolean hasRequestedFeature( int requestedFeature )
    {
        return hasRequestedFeatures( new int[] {requestedFeature} );
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

    public boolean isPlugin()
    {
        return isPlugin;
    }

    public void setIsPlugin( boolean value )
    {
        isPlugin = value;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || !(other instanceof SuntimesCalculatorDescriptor))
        {
            return false;

        } else {
            SuntimesCalculatorDescriptor otherDescriptor = (SuntimesCalculatorDescriptor) other;
            return this.getName().equals(otherDescriptor.getName());
        }
    }

    @Override
    public int compareTo(@NonNull Object other)
    {
        SuntimesCalculatorDescriptor otherDescriptor = (SuntimesCalculatorDescriptor)other;
        return this.getName().compareTo(otherDescriptor.getName());
    }

}
