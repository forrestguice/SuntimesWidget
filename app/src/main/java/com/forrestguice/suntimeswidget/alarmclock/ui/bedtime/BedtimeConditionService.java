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

package com.forrestguice.suntimeswidget.alarmclock.ui.bedtime;

import android.annotation.TargetApi;
import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.service.notification.Condition;
import android.service.notification.ConditionProviderService;
import android.util.Log;

public class BedtimeConditionService extends ConditionProviderService
{
    public static final String ACTION_BEDTIME_UPDATE = "suntimeswidget.alarm.update_bedtime";
    public static final String ACTION_BEDTIME_STOP = "suntimeswidget.alarm.stop_bedtime";
    protected static final String REQUIRED_PERMISSION = "suntimes.permission.READ_CALCULATOR";

    protected boolean connected = false;

    @Override
    public void onConnected() {
        Log.d("DEBUG", "BedtimeConditionService :: onConnected");
        connected = true;
        registerReceiver(receiver, getReceiverIntentFilter(), REQUIRED_PERMISSION, null);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("DEBUG", "BedtimeConditionService :: onDestroy");
        connected = false;
        unregisterReceiver(receiver);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d("DEBUG", "BedtimeConditionService :: onReceive: " + intent.getAction());
            String action = intent.getAction();
            if (ACTION_BEDTIME_UPDATE.equals(action)) {
                notifyCondition();

            } else if (ACTION_BEDTIME_STOP.equals(action)) {
                notifyCondition(false);
                stopSelf();
            }
        }
    };
    protected IntentFilter getReceiverIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BEDTIME_UPDATE);
        filter.addAction(ACTION_BEDTIME_STOP);
        return filter;
    }

    @Override
    public void onSubscribe(Uri conditionId)
    {
        Log.d("DEBUG", "BedtimeConditionService :: onSubscribe: " + conditionId);
        notifyCondition();
    }

    protected void notifyCondition() {
        notifyCondition(BedtimeSettings.isBedtimeModeActive(this) && !BedtimeSettings.isBedtimeModePaused(this));
    }
    protected void notifyCondition(boolean value)
    {
        Log.d("DEBUG", "BedtimeConditionService :: notifyCondition: " + value);
        String conditionSummary = value ? "TODO:value:" + value : "";    // TODO
        notifyCondition( BedtimeConditionService.createAutomaticZenRuleCondition(conditionSummary, value) );
    }

    @Override
    public void onUnsubscribe(Uri conditionId) {
        Log.d("DEBUG", "BedtimeConditionService :: onUnsubscribe: " + conditionId);
    }

    @Override
    public void onRequestConditions(int relevance)
    {
        super.onRequestConditions(relevance);
        Log.d("DEBUG", "BedtimeConditionService :: onRequestConditions: " + relevance);
    }

    public static String getAutomaticZenRuleName(Context context) {
        return "Bedtime (Suntimes)";           // TODO: i18n
    }
    public static Uri getAutomaticZenRuleConditionId() {
        return Uri.parse("condition://id");
    }

    @TargetApi(24)
    public static AutomaticZenRule createAutomaticZenRule(Context context, boolean enabled)
    {
        String ruleName = getAutomaticZenRuleName(context);
        Uri conditionId = getAutomaticZenRuleConditionId();
        ComponentName componentName = new ComponentName(context, BedtimeConditionService.class);
        return new AutomaticZenRule(ruleName, componentName, conditionId, NotificationManager.INTERRUPTION_FILTER_ALARMS, enabled);
    }

    @TargetApi(24)
    public static Condition createAutomaticZenRuleCondition(String summary, boolean value) {
        return new Condition(BedtimeConditionService.getAutomaticZenRuleConditionId(), summary, (value ? Condition.STATE_TRUE : Condition.STATE_FALSE));
    }

    @TargetApi(24)
    public static void triggerBedtimeAutomaticZenRule(final Context context, boolean value)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_BEDTIME_UPDATE);
        context.sendBroadcast(intent);
    }

}
