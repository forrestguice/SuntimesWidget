package com.forrestguice.util;

import org.junit.runners.model.InitializationError;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.internal.util.AndroidRunnerParams;

public class SuntimesJUnitTestRunner extends AndroidJUnit4ClassRunner
{
    public SuntimesJUnitTestRunner(Class<?> klass, AndroidRunnerParams runnerParams) throws InitializationError {
        super(klass, runnerParams);
    }

    public SuntimesJUnitTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
}
