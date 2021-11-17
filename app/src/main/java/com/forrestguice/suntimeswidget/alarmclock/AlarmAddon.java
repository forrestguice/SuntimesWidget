/**
    Copyright (C) 2021 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AlarmAddon
 * Helper methods for extending alarm functionality to addon apps (via ContentProvider, Intent, etc).
 */
@SuppressWarnings("Convert2Diamond")
public class AlarmAddon
{
    public static final String REQUIRED_PERMISSION = "suntimes.permission.READ_CALCULATOR";

    public static final String CATEGORY_SUNTIMES_ALARM = "suntimes.SUNTIMES_ALARM";
    public static final String ACTION_SUNTIMES_ADDON_ALARM = "suntimes.action.ADDON_ALARM";
    public static final String KEY_ALARM_INFO_PROVIDER = "AlarmInfoProvider";

    public static final String CATEGORY_SUNTIMES_ADDON = "suntimes.SUNTIMES_ADDON";
    public static final String ACTION_SUNTIMES_PICK_ALARM = "suntimes.action.PICK_ALARM";
    public static final String KEY_ALARM_PICKER_TITLE = "SuntimesAlarmPickerTitle";

    public static final String COLUMN_CONFIG_PROVIDER = "provider";         // String (provider reference)
    public static final String COLUMN_ALARM_NAME = "alarm_name";            // String (alarm/event ID)
    public static final String COLUMN_ALARM_TITLE = "alarm_title";          // String (display string)
    public static final String COLUMN_ALARM_SUMMARY = "alarm_summary";      // String (extended display string)
    public static final String COLUMN_ALARM_TIMEMILLIS = "alarm_time";      // long (timestamp millis)

    public static final String QUERY_ALARM_INFO = "alarmInfo";
    public static final String[] QUERY_ALARM_INFO_PROJECTION = new String[] {
            COLUMN_ALARM_NAME, COLUMN_ALARM_TITLE, COLUMN_ALARM_SUMMARY
    };
    public static final String QUERY_ALARM_CALC = "alarmCalc";
    public static final String[] QUERY_ALARM_CALC_PROJECTION = new String[] {
            COLUMN_ALARM_NAME, COLUMN_ALARM_TIMEMILLIS
    };

    /**
     * queryAlarmPickers
     */
    public static List<AlarmPickerInfo> queryAlarmPickers(@NonNull Context context)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_SUNTIMES_PICK_ALARM);
        intent.addCategory(CATEGORY_SUNTIMES_ADDON);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> packageInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_META_DATA);
        ArrayList<AlarmPickerInfo> matches = new ArrayList<>();
        for (ResolveInfo resolveInfo : packageInfo)
        {
            IntentFilter filter = resolveInfo.filter;
            if (filter != null && filter.hasAction(ACTION_SUNTIMES_PICK_ALARM) && filter.hasCategory(CATEGORY_SUNTIMES_ADDON))
            {
                try {
                    PackageInfo packageInfo0 = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    if (hasPermission(packageInfo0))
                    {
                        String title = resolveInfo.activityInfo.metaData.getString(KEY_ALARM_PICKER_TITLE);
                        matches.add(new AlarmPickerInfo(title, resolveInfo.activityInfo));
                    } else {
                        Log.w("queryAlarmPickers", "Permission denied! " + packageInfo0.packageName + " does not have required permissions.");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("queryAlarmPickers", "Package not found! " + e);
                }
            }
        }
        return matches;
    }

    /**
     * AlarmPickerInfo
     */
    public static class AlarmPickerInfo
    {
        public AlarmPickerInfo(String title, ActivityInfo info)
        {
            this.title = title;
            this.info = info;
        }

        protected String title;
        public String getTitle() {
            return title;
        }

        protected ActivityInfo info;
        public ActivityInfo getInfo() {
            return info;
        }

        public Intent getIntent()
        {
            Intent intent = new Intent(AlarmAddon.ACTION_SUNTIMES_PICK_ALARM);
            intent.setClassName(info.packageName, info.name);
            return intent;
        }

        public String toString() {
            return title;
        }
    }

    /**
     * queryAlarmInfoProviders
     */
    public static List<String> queryAlarmInfoProviders(@NonNull Context context)
    {
        ArrayList<String> references = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        Intent packageQuery = new Intent(ACTION_SUNTIMES_ADDON_ALARM);
        packageQuery.addCategory(CATEGORY_SUNTIMES_ALARM);
        List<ResolveInfo> packages = packageManager.queryIntentActivities(packageQuery, PackageManager.GET_META_DATA);
        Log.i("queryAlarmInfo", "Scanning for AlarmInfoProvider references... found " + packages.size());

        for (ResolveInfo resolveInfo : packages)
        {
            if (resolveInfo != null && resolveInfo.activityInfo != null && resolveInfo.activityInfo.metaData != null)
            {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    if (hasPermission(packageInfo))
                    {
                        String metaData = resolveInfo.activityInfo.metaData.getString(KEY_ALARM_INFO_PROVIDER);
                        String[] values = (metaData != null) ? metaData.replace(" ","").split("\\|") : new String[0];
                        references.addAll(Arrays.asList(values));
                    } else {
                        Log.w("queryAlarmInfo", "Permission denied! " + packageInfo.packageName + " does not have required permissions.");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("queryAlarmInfo", "Package not found! " + e);
                }
            }
        }
        return references;
    }

    /**
     * hasPermission
     */
    public static boolean hasPermission(@NonNull PackageInfo packageInfo)
    {
        boolean hasPermission = false;
        if (packageInfo.requestedPermissions != null)
        {
            for (String permission : packageInfo.requestedPermissions) {
                if (permission != null && permission.equals(REQUIRED_PERMISSION)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        return hasPermission;
    }

}
