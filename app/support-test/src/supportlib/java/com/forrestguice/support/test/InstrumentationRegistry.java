package com.forrestguice.support.test;

import android.app.Instrumentation;
import android.content.Context;

public class InstrumentationRegistry
{
    public static Context getTargetContext() {
        return android.support.test.InstrumentationRegistry.getTargetContext();
    }

    public static Instrumentation getInstrumentation() {
        return android.support.test.InstrumentationRegistry.getInstrumentation();
    }
}
