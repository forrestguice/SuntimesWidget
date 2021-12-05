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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * AlarmAddon
 * Helper methods for extending alarm functionality to addon apps (via ContentProvider, Intent, etc).
 *
 * @see AlarmEventContract
 */
@SuppressWarnings("Convert2Diamond")
public class AlarmAddon
{
    public static final String CATEGORY_SUNTIMES_ALARM = "suntimes.SUNTIMES_ALARM";
    public static final String ACTION_SUNTIMES_ADDON_ALARM = "suntimes.action.ADDON_ALARM";
    public static final String KEY_ALARM_INFO_PROVIDER = "AlarmInfoProvider";

    public static final String CATEGORY_SUNTIMES_ADDON = "suntimes.SUNTIMES_ADDON";
    public static final String ACTION_SUNTIMES_PICK_ALARM = "suntimes.action.PICK_ALARM";
    public static final String KEY_ALARM_PICKER_TITLE = "SuntimesAlarmPickerTitle";

    public static String getAlarmInfoUri(String authority, String alarmID) {
        return "content://" + authority + "/" + AlarmEventContract.QUERY_EVENT_INFO + "/" + alarmID;
    }

    public static String getAlarmCalcUri(String authority, String alarmID) {
        return "content://" + authority + "/" + AlarmEventContract.QUERY_EVENT_CALC + "/" + alarmID;
    }

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
                        String title = resolveInfo.activityInfo.metaData.getString(KEY_ALARM_PICKER_TITLE, resolveInfo.activityInfo.name);
                        matches.add(new AlarmPickerInfo(title, resolveInfo.activityInfo));

                    } else {
                        Log.w("queryAlarmPickers", "Permission denied! " + packageInfo0.packageName + " does not have required permissions.");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("queryAlarmPickers", "Package not found! " + e);
                }
            }
        }
        Collections.sort(matches, new Comparator<AlarmPickerInfo>() {
            @Override
            public int compare(AlarmPickerInfo o1, AlarmPickerInfo o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        return matches;
    }

    /**
     * AlarmPickerInfo
     */
    public static class AlarmPickerInfo
    {
        public AlarmPickerInfo(@NonNull String title, ActivityInfo info)
        {
            this.title = title;
            this.info = info;
        }

        protected String title;
        @NonNull
        public String getTitle() {
            return title;
        }

        protected ActivityInfo info;
        public ActivityInfo getInfo() {
            return info;
        }

        public Intent getIntent() {
            return getIntent(null);
        }
        public Intent getIntent(@Nullable Location location)
        {
            Intent intent = new Intent(AlarmAddon.ACTION_SUNTIMES_PICK_ALARM);
            intent.setClassName(info.packageName, info.name);

            if (location != null)
            {
                intent.putExtra(AlarmEventContract.EXTRA_LOCATION_LABEL, location.getLabel());
                intent.putExtra(AlarmEventContract.EXTRA_LOCATION_LAT, location.getLatitudeAsDouble());
                intent.putExtra(AlarmEventContract.EXTRA_LOCATION_LON, location.getLongitudeAsDouble());
                intent.putExtra(AlarmEventContract.EXTRA_LOCATION_ALT, location.getAltitudeAsDouble());
            }
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

    public static boolean checkUriPermission(@NonNull Context context, @NonNull String eventUri)
    {
        Log.w("DEBUG", "checkUriPermission: eventUri: " + eventUri);
        boolean hasPermission = false;
        Uri uri = Uri.parse(eventUri);
        PackageManager packageManager = context.getPackageManager();
        try {
            ProviderInfo providerInfo = packageManager.resolveContentProvider(uri.getAuthority(), 0);
            PackageInfo packageInfo = packageManager.getPackageInfo(providerInfo.packageName, PackageManager.GET_PERMISSIONS);

            if (!(hasPermission = hasPermission(packageInfo))) {
                Log.w("AlarmAddon", "checkUriPermission: Permission denied! " + packageInfo.packageName + " does not have required permissions.");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AlarmAddon", "checkUriPermission: Package not found! " + e);
        }
        return hasPermission;
    }

    public static boolean queryDisplayStrings(@NonNull AlarmEvent.AlarmEventItem item, @Nullable ContentResolver resolver)
    {
        boolean retValue = false;
        String uriString = item.getUri();
        if (resolver != null && uriString != null)
        {
            Uri info_uri = Uri.parse(uriString);
            Cursor cursor = resolver.query(info_uri, AlarmEventContract.QUERY_EVENT_INFO_PROJECTION, null, null, null);
            if (cursor != null)
            {
                cursor.moveToFirst();
                int i_title = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_TITLE);
                int i_summary = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_SUMMARY);

                String titleValue = (i_title >= 0) ? cursor.getString(i_title) : null;
                item.title = titleValue != null ? titleValue : info_uri.getLastPathSegment();
                item.summary = (i_summary >= 0) ? cursor.getString(i_summary) : null;

                cursor.close();
                retValue = (titleValue != null);
            }
        }
        return retValue;
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
                if (permission != null && permission.equals(AlarmEventContract.REQUIRED_PERMISSION)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        return hasPermission;
    }

}
