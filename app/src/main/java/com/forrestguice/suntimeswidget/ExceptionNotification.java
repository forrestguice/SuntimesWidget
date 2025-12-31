/**
    Copyright (C) 2025 Forrest Guice
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
package com.forrestguice.suntimeswidget;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.app.NotificationCompat;

public abstract class ExceptionNotification
{
    protected abstract String getChannelID();
    protected abstract String getChannelTitle(Context context);
    protected abstract String getChannelDesc(Context context);

    @TargetApi(24)
    protected int getChannelImportance() {
        return NotificationManager.IMPORTANCE_HIGH;
    }

    protected abstract String getNotificationTitle(Context context);
    protected abstract String getNotificationMessage(Context context);
    protected abstract String getNotificationActionText(Context context);
    protected abstract int getNotificationIconResID();

    protected int getNotificationVisibility() {
        return NotificationCompat.VISIBILITY_PUBLIC;
    }
    protected int getNotificationPriority() {
        return NotificationCompat.PRIORITY_HIGH;
    }
    protected String getNotificationCategory() {
        return NotificationCompat.CATEGORY_ERROR;
    }

    @Nullable
    protected abstract Intent getCrashReportActivityIntent(Context context, String report);

    public Notification createNotification(Context context, String report)
    {
        NotificationCompat.Builder builder = createNotificationBuilder(context);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setVisibility(getNotificationVisibility());
        builder.setPriority(getNotificationPriority());
        builder.setCategory(getNotificationCategory());
        builder.setSmallIcon(getNotificationIconResID());
        builder.setAutoCancel(true);

        String title = getNotificationTitle(context);
        String message = getNotificationMessage(context);
        if (title != null) {
            builder.setContentTitle(title);
        }
        builder.setContentText(message);

        PendingIntent intent = PendingIntent.getActivity(context, message.hashCode(), getCrashReportActivityIntent(context, report), PendingIntent.FLAG_UPDATE_CURRENT);
        String actionText = getNotificationActionText(context);
        if (actionText != null) {
            builder.addAction(getNotificationIconResID(), getNotificationActionText(context), intent);
        } else {
            builder.setContentIntent(intent);
        }
        return builder.build();
    }

    protected NotificationCompat.Builder createNotificationBuilder(Context context)
    {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new NotificationCompat.Builder(context, createNotificationChannel(context));
        } else {
            //noinspection deprecation
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    protected String createNotificationChannel(Context context)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            NotificationChannel channel = new NotificationChannel(getChannelID(), getChannelTitle(context), getChannelImportance());
            channel.setDescription(getChannelDesc(context));
            notificationManager.createNotificationChannel(channel);
            return getChannelID();
        }
        return "";
    }
}
