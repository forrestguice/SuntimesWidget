package com.forrestguice.support.test.rule;

import android.app.Activity;

public class ActivityTestRule<T extends Activity> extends android.support.test.rule.ActivityTestRule<T>
{
    public ActivityTestRule(Class<T> activityClass) {
        super(activityClass);
    }

    public ActivityTestRule(Class<T> activityClass, boolean initialTouchMode) {
        super(activityClass, initialTouchMode);
    }

    public ActivityTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
        super(activityClass, initialTouchMode, launchActivity);
    }
}

