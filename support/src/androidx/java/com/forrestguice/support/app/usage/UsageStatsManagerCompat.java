package com.forrestguice.support.app.usage;

import android.app.usage.UsageStatsManager;
import android.os.Build;

public class UsageStatsManagerCompat
{
    public static final int STANDBY_BUCKET_RESTRICTED;
    static {
        if (Build.VERSION.SDK_INT >= 30) {
            STANDBY_BUCKET_RESTRICTED = UsageStatsManager.STANDBY_BUCKET_RESTRICTED;
        } else STANDBY_BUCKET_RESTRICTED = 0x0000002d;    // 45
    }
}