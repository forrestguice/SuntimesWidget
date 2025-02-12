package com.forrestguice.support.test.runner;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class AndroidJUnit4 extends BlockJUnit4ClassRunner
{
    public AndroidJUnit4(Class<?> klass)
            throws InitializationError {
        super(klass);
    }
}

