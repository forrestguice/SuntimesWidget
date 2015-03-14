package com.forrestguice.suntimeswidget.calculator;

import android.content.Context;
import android.util.Log;

import com.forrestguice.suntimeswidget.SuntimesWidgetSettings;
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

        /**if (current.name().equals(SunriseSunsetSuntimesCalculator.NAME))
        {
            // calculator: sunrisesunsetlib
            calculator = new SunriseSunsetSuntimesCalculator();
            Log.d("createCalculator", "using calculator: " + calculator.name());

        } else {
            // default: sunrisesunsetlib
            calculator = new SunriseSunsetSuntimesCalculator();
            Log.d("createCalculator", "setting is unrecognized; using the default: " + calculator.name());
        }*/

        calculator.init(location, timezone);
        return calculator;
    }

    public static void initCalculators(Context context)
    {
        SuntimesCalculatorDescriptor calculatorSetting = new SuntimesCalculatorDescriptor(SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.NAME, SunriseSunsetSuntimesCalculator.REF);
        SuntimesCalculatorDescriptor.addValue(calculatorSetting);
    }
}
