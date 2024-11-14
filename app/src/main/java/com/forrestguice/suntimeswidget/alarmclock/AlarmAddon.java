/**
    Copyright (C) 2021-2024 Forrest Guice
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
import android.os.Build;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimeswidget.calculator.core.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    public static final String ACTION_SUNTIMES_ADDON_EVENT = "suntimes.action.ADDON_EVENT";
    public static final String KEY_EVENT_INFO_PROVIDER = "EventInfoProvider";

    public static final String ACTION_SUNTIMES_DISMISS_CHALLENGE = "suntimes.action.DISMISS_CHALLENGE";
    public static final String ACTION_SUNTIMES_DISMISS_CHALLENGE_CONFIG = "suntimes.action.DISMISS_CHALLENGE_CONFIG";
    public static final String KEY_DISMISS_CHALLENGE_TITLE = "SuntimesDismissChallengeTitle";
    public static final String KEY_DISMISS_CHALLENGE_ID = "SuntimesDismissChallengeID";

    public static final String CATEGORY_SUNTIMES_ADDON = "suntimes.SUNTIMES_ADDON";
    public static final String ACTION_SUNTIMES_PICK_EVENT = "suntimes.action.PICK_EVENT";
    public static final String KEY_EVENT_PICKER_TITLE = "SuntimesEventPickerTitle";

    public static String getEventInfoUri(String authority, String eventID) {
        return "content://" + authority + "/" + AlarmEventContract.QUERY_EVENT_INFO + "/" + eventID;
    }

    public static String getEventCalcUri(String authority, String eventID) {
        return "content://" + authority + "/" + AlarmEventContract.QUERY_EVENT_CALC + "/" + eventID;
    }

    /**
     * queryAlarmDismissChallenges
     */
    public static List<DismissChallengeInfo> queryAlarmDismissChallenges(@NonNull Context context, @Nullable Long searchForID) {
        return queryAlarmDismissChallenges(context, ACTION_SUNTIMES_DISMISS_CHALLENGE, searchForID);
    }
    public static List<DismissChallengeInfo> queryAlarmDismissChallengeConfig(@NonNull Context context, @Nullable Long searchForID) {
        return queryAlarmDismissChallenges(context, ACTION_SUNTIMES_DISMISS_CHALLENGE_CONFIG, searchForID);
    }
    public static List<DismissChallengeInfo> queryAlarmDismissChallenges(@NonNull Context context, @NonNull String action, @Nullable Long searchForID)
    {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.addCategory(CATEGORY_SUNTIMES_ADDON);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> packageInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_META_DATA);
        ArrayList<DismissChallengeInfo> matches = new ArrayList<>();
        for (ResolveInfo resolveInfo : packageInfo)
        {
            IntentFilter filter = resolveInfo.filter;
            if (filter != null && filter.hasAction(action) && filter.hasCategory(CATEGORY_SUNTIMES_ADDON))
            {
                try {
                    PackageInfo packageInfo0 = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    int id = resolveInfo.activityInfo.metaData.getInt(KEY_DISMISS_CHALLENGE_ID, -1);

                    if ((searchForID == null || searchForID == id))
                    {
                        if (hasPermission(packageInfo0))
                        {
                            String title_metadata = resolveInfo.activityInfo.metaData.getString(KEY_DISMISS_CHALLENGE_TITLE);
                            String title = (title_metadata != null ? title_metadata : resolveInfo.activityInfo.name);
                            matches.add(new DismissChallengeInfo(title, resolveInfo.activityInfo, id));

                        } else {
                            Log.w("AlarmAddon", "queryAlarmDismissChallenges: Permission denied! " + packageInfo0.packageName + " does not have required permissions.");
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("AlarmAddon", "queryAlarmDismissChallenges: Package not found! " + e);
                }
            }
        }
        Collections.sort(matches, compareActivityInfo);
        return matches;
    }

    /**
     * queryEventPickers
     */
    public static List<EventPickerInfo> queryEventPickers(@NonNull Context context)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_SUNTIMES_PICK_EVENT);
        intent.addCategory(CATEGORY_SUNTIMES_ADDON);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> packageInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_META_DATA);
        ArrayList<EventPickerInfo> matches = new ArrayList<>();
        for (ResolveInfo resolveInfo : packageInfo)
        {
            IntentFilter filter = resolveInfo.filter;
            if (filter != null && filter.hasAction(ACTION_SUNTIMES_PICK_EVENT) && filter.hasCategory(CATEGORY_SUNTIMES_ADDON))
            {
                try {
                    PackageInfo packageInfo0 = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    if (hasPermission(packageInfo0))
                    {
                        String metadata = resolveInfo.activityInfo.metaData.getString(KEY_EVENT_PICKER_TITLE);
                        String title = (metadata != null ? metadata : resolveInfo.activityInfo.name);
                        matches.add(new EventPickerInfo(title, resolveInfo.activityInfo));

                    } else {
                        Log.w("queryEventPickers", "Permission denied! " + packageInfo0.packageName + " does not have required permissions.");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("queryEventPickers", "Package not found! " + e);
                }
            }
        }
        Collections.sort(matches, compareActivityInfo);
        return matches;
    }

    private static final Comparator<AddonActivityInfo> compareActivityInfo = new Comparator<AddonActivityInfo>() {
        @Override
        public int compare(AddonActivityInfo o1, AddonActivityInfo o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    };

    /**
     * AddonActivityInfo
     */
    public static class AddonActivityInfo
    {
        public AddonActivityInfo(@NonNull String title, ActivityInfo info)
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

        public String toString() {
            return title;
        }

        public Intent getIntent() {
            return getIntent(null);
        }

        public Intent getIntent(@Nullable String action)
        {
            Intent intent = new Intent();
            if (info != null) {
                intent.setClassName(info.packageName, info.name);
            }
            if (action != null) {
                intent.setAction(action);
            }
            return intent;
        }
    }

    /**
     * DismissChallengeInfo
     */
    public static class DismissChallengeInfo extends AddonActivityInfo
    {
        protected long id;

        public DismissChallengeInfo(@NonNull String title, ActivityInfo info, long challengeID) {
            super(title, info);
            this.id = challengeID;
        }

        @Override
        public Intent getIntent() {
            return getIntent(ACTION_SUNTIMES_DISMISS_CHALLENGE);
        }

        public long getDismissChallengeID() {
            return id;
        }
    }

    /**
     * EventPickerInfo
     */
    public static class EventPickerInfo extends AddonActivityInfo
    {
        public EventPickerInfo(@NonNull String title, ActivityInfo info) {
            super(title, info);
        }

        @Override
        public Intent getIntent() {
            return getIntent((Location) null);
        }
        public Intent getIntent(@Nullable Location location)
        {
            Intent intent = getIntent(AlarmAddon.ACTION_SUNTIMES_PICK_EVENT);
            if (location != null)
            {
                intent.putExtra(AlarmEventContract.EXTRA_LOCATION_LABEL, location.getLabel());
                intent.putExtra(AlarmEventContract.EXTRA_LOCATION_LAT, location.getLatitudeAsDouble());
                intent.putExtra(AlarmEventContract.EXTRA_LOCATION_LON, location.getLongitudeAsDouble());
                intent.putExtra(AlarmEventContract.EXTRA_LOCATION_ALT, location.getAltitudeAsDouble());
            }
            return intent;
        }
    }

    /**
     * queryEventInfoProviders
     */
    public static List<String> queryEventInfoProviders(@NonNull Context context)
    {
        ArrayList<String> references = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        Intent packageQuery = new Intent(ACTION_SUNTIMES_ADDON_EVENT);
        packageQuery.addCategory(CATEGORY_SUNTIMES_ALARM);
        List<ResolveInfo> packages = packageManager.queryIntentActivities(packageQuery, PackageManager.GET_META_DATA);
        Log.i("queryEventInfo", "Scanning for EventInfoProvider references... found " + packages.size());

        for (ResolveInfo resolveInfo : packages)
        {
            if (resolveInfo != null && resolveInfo.activityInfo != null && resolveInfo.activityInfo.metaData != null)
            {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    if (hasPermission(packageInfo))
                    {
                        String metaData = resolveInfo.activityInfo.metaData.getString(KEY_EVENT_INFO_PROVIDER);
                        String[] values = (metaData != null) ? metaData.replace(" ","").split("\\|") : new String[0];
                        references.addAll(Arrays.asList(values));
                    } else {
                        Log.w("queryEventInfo", "Permission denied! " + packageInfo.packageName + " does not have required permissions.");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("queryEventInfo", "Package not found! " + e);
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
            if (providerInfo != null)
            {
                PackageInfo packageInfo = packageManager.getPackageInfo(providerInfo.packageName, PackageManager.GET_PERMISSIONS);
                if (!(hasPermission = hasPermission(packageInfo))) {
                    Log.e("AlarmAddon", "checkUriPermission: Permission denied! " + packageInfo.packageName + " does not have required permissions.");
                }
            } else {
                Log.e("AlarmAddon", "checkUriPermission: failed to resolve providerInfo for " + eventUri);
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
                String titleValue = null;
                cursor.moveToFirst();
                if (!cursor.isAfterLast())
                {
                    int i_title = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_TITLE);
                    int i_summary = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_SUMMARY);
                    int i_support_repeat = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_SUPPORTS_REPEATING);
                    int i_support_offsetdays = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_SUPPORTS_OFFSETDAYS);
                    int i_requires_location = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_REQUIRES_LOCATION);

                    titleValue = (i_title >= 0) ? cursor.getString(i_title) : null;
                    item.title = titleValue != null ? titleValue : info_uri.getLastPathSegment();
                    item.summary = (i_summary >= 0) ? cursor.getString(i_summary) : null;
                    item.supports_repeating = (i_support_repeat >= 0) ? cursor.getInt(i_support_repeat) : AlarmEventContract.REPEAT_SUPPORT_DAILY;

                    if (i_support_offsetdays >= 0) {
                        String v = cursor.getString(i_support_offsetdays);
                        if (v != null) {
                            item.supports_offset_days = Boolean.parseBoolean(v);
                        }
                    }

                    if (i_requires_location >= 0) {
                        String v = cursor.getString(i_requires_location);
                        if (v != null) {
                            item.requires_location = Boolean.parseBoolean(v);
                        }
                    }

                    int i_phrase = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_PHRASE);
                    int i_phrase_gender = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_PHRASE_GENDER);
                    int i_phrase_quantity = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_PHRASE_QUANTITY);
                    String noun = (i_phrase >= 0 ? cursor.getString(i_phrase) : item.title);
                    item.phrase = new AlarmEvent.AlarmEventPhrase(
                            noun != null && !noun.trim().isEmpty() ? noun : item.title,
                            i_phrase_gender >= 0 ? cursor.getString(i_phrase_gender) : null,
                            i_phrase_quantity >= 0 ? cursor.getInt(i_phrase_quantity) : 1);
                }

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
