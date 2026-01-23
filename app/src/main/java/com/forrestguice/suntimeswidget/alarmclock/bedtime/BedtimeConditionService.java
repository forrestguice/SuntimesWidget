/**
    Copyright (C) 2023 Forrest Guice
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

import android.annotation.TargetApi;
import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.service.notification.Condition;
import android.service.notification.ConditionProviderService;
import android.util.Log;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.app.NotificationManagerHelper;

/**
 * This ConditionProviderService is enabled by @bool/supports_condition_service; true for [api24 - api28].
 * It is not supported for earlier versions, and deprecated in later versions. The static methods
 * in this class are used to manage the state of the Bedtime AutomaticZenRule.
 */
@SuppressWarnings("deprecation")
@TargetApi(24)
public class BedtimeConditionService extends ConditionProviderService
{
    public static final String ACTION_BEDTIME_UPDATE = "suntimeswidget.alarm.update_bedtime";
    public static final String ACTION_BEDTIME_STOP = "suntimeswidget.alarm.stop_bedtime";
    protected static String REQUIRED_PERMISSION() {
        return BuildConfig.SUNTIMES_PERMISSION_ROOT + ".permission.READ_CALCULATOR";
    }

    @Override
    public void onConnected() {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "BedtimeConditionService :: onConnected");
        }
        registerReceiver(receiver, getReceiverIntentFilter(), REQUIRED_PERMISSION(), null);
    }

    @Override
    public void onDestroy()
    {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "BedtimeConditionService :: onDestroy");
        }
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (BuildConfig.DEBUG) {
                Log.d("DEBUG", "BedtimeConditionService :: onReceive: " + intent.getAction());
            }
            String action = intent.getAction();
            if (ACTION_BEDTIME_UPDATE.equals(action)) {
                notifyCondition();

            } else if (ACTION_BEDTIME_STOP.equals(action)) {
                notifyCondition(false);
                stopSelf();
            }
        }
    };
    private static IntentFilter getReceiverIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BEDTIME_UPDATE);
        filter.addAction(ACTION_BEDTIME_STOP);
        return filter;
    }

    @Override
    public void onSubscribe(Uri conditionId)
    {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "BedtimeConditionService :: onSubscribe: " + conditionId);
        }
        notifyCondition();
    }

    protected void notifyCondition() {
        notifyCondition(BedtimeSettings.isBedtimeModeActive(this) && !BedtimeSettings.isBedtimeModePaused(this));
    }
    protected void notifyCondition(boolean value)
    {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "BedtimeConditionService :: notifyCondition: " + value);
        }
        String conditionSummary = value ? getString(R.string.msg_bedtime_active) : "";
        notifyCondition( createAutomaticZenRuleCondition(conditionSummary, value) );
    }

    @Override
    public void onUnsubscribe(Uri conditionId) {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "BedtimeConditionService :: onUnsubscribe: " + conditionId);
        }
    }

    @Override
    public void onRequestConditions(int relevance)
    {
        super.onRequestConditions(relevance);
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "BedtimeConditionService :: onRequestConditions: " + relevance);
        }
    }

    public static String getAutomaticZenRuleName(Context context) {
        return context.getString(R.string.configLabel_bedtime_zenrule_name);
    }
    public static Uri getAutomaticZenRuleConditionId() {
        return Uri.parse("condition://bedtime");   // note: this is not the same as the AutomaticZenRule id, which is a random id issued when calling `addAutomaticZenRule`
    }

    @TargetApi(24)
    public static AutomaticZenRule createAutomaticZenRule(Context context, boolean enabled)
    {
        int filter = getAutomaticZenRuleFilter(context);
        String ruleName = getAutomaticZenRuleName(context);
        Uri conditionId = getAutomaticZenRuleConditionId();
        ComponentName componentName = new ComponentName(context, BedtimeConditionService.class);
        ComponentName configComponent = new ComponentName(context, BedtimeActivity.class);

        AutomaticZenRule rule = new AutomaticZenRule(ruleName, componentName, conditionId, filter, enabled);
        rule = NotificationManagerHelper.setAutomaticZenRuleConfigurationActivity(rule, configComponent);
        return rule;
    }

    @TargetApi(23)
    public static int getAutomaticZenRuleFilter(Context context)
    {
        int filter;
        int filterSetting = BedtimeSettings.loadPrefBedtimeDoNotDisturbFilter(context);
        switch (filterSetting) {
            case BedtimeSettings.DND_FILTER_ALARMS: filter = NotificationManager.INTERRUPTION_FILTER_ALARMS; break;
            case BedtimeSettings.DND_FILTER_PRIORITY: default: filter = NotificationManager.INTERRUPTION_FILTER_PRIORITY; break;
        }
        return filter;
    }

    @TargetApi(24)
    public static Condition createAutomaticZenRuleCondition(String summary, boolean value) {
        return new Condition(BedtimeConditionService.getAutomaticZenRuleConditionId(), summary, (value ? Condition.STATE_TRUE : Condition.STATE_FALSE));
    }

    @TargetApi(24)
    public static void triggerBedtimeAutomaticZenRule(final Context context, boolean value)
    {
        if (context.getResources().getBoolean(R.bool.supports_condition_service))
        {
            if (BuildConfig.DEBUG) {
                Log.d("DEBUG", "BedtimeConditionService :: triggerAutomaticZenRule: " + value);
            }
            Intent intent = new Intent();
            intent.setAction(ACTION_BEDTIME_UPDATE);
            intent.setPackage(BuildConfig.APPLICATION_ID);
            context.sendBroadcast(intent);

        } else {
            if (Build.VERSION.SDK_INT >= 29)
            {
                if (BuildConfig.DEBUG) {
                    Log.d("DEBUG", "NotificationManager :: setAutomaticZenRuleState: " + value + ", " + BedtimeSettings.getRecentAutomaticZenRuleID(context));
                }
                String conditionSummary = (value ? context.getString(R.string.msg_bedtime_active) : "");
                Condition condition = createAutomaticZenRuleCondition(conditionSummary, value);
                NotificationManagerHelper.setAutomaticZenRuleState(context, BedtimeSettings.getRecentAutomaticZenRuleID(context), condition);
            }
        }
    }

}
