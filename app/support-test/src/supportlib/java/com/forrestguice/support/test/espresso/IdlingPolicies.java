package com.forrestguice.support.test.espresso;

import java.util.concurrent.TimeUnit;

public class IdlingPolicies
{
    public static void setMasterPolicyTimeout(long timeout, TimeUnit unit) {
        android.support.test.espresso.IdlingPolicies.setMasterPolicyTimeout(timeout, unit);
    }

    public static void setIdlingResourceTimeout(long timeout, TimeUnit unit) {
        android.support.test.espresso.IdlingPolicies.setIdlingResourceTimeout(timeout, unit);
    }
}

