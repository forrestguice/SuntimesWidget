package com.forrestguice.suntimeswidget.calculator;

import com.forrestguice.suntimeswidget.BuildConfig;

public class CalculatorProvider1 extends CalculatorProvider
{
    @Override
    protected String AUTHORITY() {
        return BuildConfig.SUNTIMES_AUTHORITY_ROOT0 + AUTHORITY_SUFFIX;
    }
}
