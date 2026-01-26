package com.forrestguice.support.app;

import android.annotation.TargetApi;
import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.service.notification.Condition;
import android.util.Log;

public class NotificationManagerHelper
{
    public static boolean areNotificationsPaused(Context context)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            Object notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE);
            return invokeBooleanMethod(context, notificationManager, "areNotificationsPaused", false);
        } else return false;
    }

    public static boolean canUseFullScreenIntent(Context context)
    {
        // this method calls canUseFullScreenIntent (api34+) via reflection
        if (Build.VERSION.SDK_INT >= 34) {
            Object notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE);
            return invokeBooleanMethod(context, notificationManager, "canUseFullScreenIntent", true);
        } else return true;
    }

    protected static boolean invokeBooleanMethod(Context context, Object object, String methodName, boolean defaultValue)
    {
        if (object != null)
        {
            try {
                java.lang.reflect.Method method = object.getClass().getMethod(methodName);
                try {
                    boolean result = (boolean) method.invoke(object);
                    Log.e("NotificationManager", methodName + ": successfully invoked: returned: " + result);
                    return result;

                } catch (NullPointerException | IllegalArgumentException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    Log.e("NotificationManager", methodName + ": false; " + e);
                    return defaultValue;
                }
            } catch (SecurityException | NoSuchMethodException e) {
                Log.e("NotificationManager", methodName + ": false; " + e);
                return defaultValue;
            }
        } else {
            Log.e("NotificationManager", methodName + ": false; object is null!");
            return defaultValue;
        }
    }

    public static AutomaticZenRule setAutomaticZenRuleConfigurationActivity(AutomaticZenRule rule, ComponentName component)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            rule.setConfigurationActivity(component);
        }
        return rule;
    }

    @TargetApi(29)
    public static void setAutomaticZenRuleState(Context context, String id, Condition condition)
    {
        if (Build.VERSION.SDK_INT >= 29)
        {
            NotificationManager notifications = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifications != null) {
                notifications.setAutomaticZenRuleState(id, condition);
                Log.d("DEBUG", "setAutomaticZenRuleState: " + condition);
            }
        }
    }
}