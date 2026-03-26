package com.forrestguice.suntimeswidget.alarmclock;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.events.EventUri;

public class AlarmEventProvider0 extends AlarmEventProvider
{
    @Override
    protected String AUTHORITY() {
        return BuildConfig.SUNTIMES_AUTHORITY_ROOT0 + EventUri.AUTHORITY_SUFFIX;
    }
}