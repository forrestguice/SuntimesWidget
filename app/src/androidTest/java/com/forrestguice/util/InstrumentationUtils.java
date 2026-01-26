package com.forrestguice.util;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

public class InstrumentationUtils
{
    public static Context getContext() {
        //return androidx.test.InstrumentationRegistry.getInstrumentation().getTargetContext();
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
}
