/**
    Copyright (C) 2024 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.alarmclock.bedtime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications.ACTION_BEDTIME;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications.ACTION_BEDTIME_DISMISS;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications.ACTION_BEDTIME_PAUSE;
import static com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications.ACTION_BEDTIME_RESUME;

public class BedtimeBroadcastReceiver extends BroadcastReceiver
{
    public static final String TAG = "BedtimeReceiver";

    public static final String[] BEDTIME_ACTIONS = new String[] {
            ACTION_BEDTIME, ACTION_BEDTIME_PAUSE, ACTION_BEDTIME_RESUME, ACTION_BEDTIME_DISMISS
    };

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        final String action = intent.getAction();
        Uri data = intent.getData();
        Log.d(TAG, "onReceive: " + action + ", " + data);
        if (action != null) {
            if (actionIsPermitted(action)) {
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(AlarmNotifications.NotificationService.getNotificationIntent(context, action, data, intent.getExtras()));
                } else {
                    context.startService(AlarmNotifications.NotificationService.getNotificationIntent(context, action, data, intent.getExtras()));
                }
            } else Log.e(TAG, "onReceive: `" + action + "` is not on the list of permitted actions! Ignoring...");
        } else Log.w(TAG, "onReceive: null action!");
    }

    protected boolean actionIsPermitted(String action)
    {
        for (String a : BEDTIME_ACTIONS) {
            if (a.equals(action)) {
                return true;
            }
        }
        return false;
    }

}
