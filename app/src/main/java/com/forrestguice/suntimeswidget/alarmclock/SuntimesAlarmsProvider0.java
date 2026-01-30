package com.forrestguice.suntimeswidget.alarmclock;

import com.forrestguice.suntimeswidget.BuildConfig;

public class SuntimesAlarmsProvider0 extends SuntimesAlarmsProvider
{
    protected String AUTHORITY() {
        return BuildConfig.SUNTIMES_AUTHORITY_ROOT0 + AUTHORITY_SUFFIX;
    }
}