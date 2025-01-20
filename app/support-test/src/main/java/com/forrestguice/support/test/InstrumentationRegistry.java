package com.forrestguice.support.test;

import android.content.Context;

public class InstrumentationRegistry
{
    public static Context getTargetContext() {
        return android.support.test.InstrumentationRegistry.getTargetContext();
    }
}
