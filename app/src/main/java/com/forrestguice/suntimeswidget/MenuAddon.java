/**
    Copyright (C) 2021-2022 Forrest Guice
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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MenuAddon
{
    public static String REQUIRED_PERMISSION() {
      return BuildConfig.SUNTIMES_PERMISSION_ROOT + ".permission.READ_CALCULATOR";
    }
    public static final String CATEGORY_SUNTIMES_ADDON = "suntimes.SUNTIMES_ADDON";
    public static final String ACTION_ABOUT = "suntimes.action.SHOW_ABOUT";
    public static final String ACTION_MENU_ITEM = "suntimes.action.ADDON_MENU_ITEM";
    public static final String ACTION_SHOW_DATE = "suntimes.action.SHOW_DATE";
    public static final String EXTRA_SHOW_DATE = "dateMillis";
    public static final String META_MENUITEM_TITLE = "SuntimesMenuItemTitle";

    public static void populateSubMenu(@Nullable MenuItem submenuItem, @NonNull List<ActivityItemInfo> addonItems, long datetime)
    {
        if (submenuItem != null)
        {
            SubMenu submenu = submenuItem.getSubMenu();
            if (submenu != null)
            {
                for (int i=0; i<submenu.size(); i++) {
                    submenu.getItem(i).setIntent(submenuItem.getIntent());
                }

                for (ActivityItemInfo addon : addonItems)
                {
                    MenuItem menuItem = submenu.add(Menu.NONE, Menu.NONE, Menu.NONE, addon.getTitle());
                    if (addon.getIcon() != 0) {
                        menuItem.setIcon(addon.getIcon());
                    }
                    Intent intent = addon.getIntent();
                    intent.setAction(ACTION_SHOW_DATE);
                    intent.putExtra(EXTRA_SHOW_DATE, datetime);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    menuItem.setIntent(intent);
                }
            }
        }
    }

    public static List<ActivityItemInfo> queryAddonMenuItems(@NonNull Context context)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_SHOW_DATE);
        intent.addCategory(CATEGORY_SUNTIMES_ADDON);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> packageInfo = packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_META_DATA);
        ArrayList<ActivityItemInfo> matches = new ArrayList<>();
        for (ResolveInfo resolveInfo : packageInfo)
        {
            IntentFilter filter = resolveInfo.filter;
            if (filter != null && filter.hasAction(ACTION_SHOW_DATE) && filter.hasCategory(CATEGORY_SUNTIMES_ADDON))
            {
                try {
                    PackageInfo packageInfo0 = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    if (hasPermission(packageInfo0))
                    {
                        String metadata = resolveInfo.activityInfo.metaData.getString(META_MENUITEM_TITLE);
                        String title = (metadata != null ? metadata : resolveInfo.activityInfo.name);
                        //int icon = R.drawable.ic_suntimes;    // TODO: icon
                        matches.add(new ActivityItemInfo(context, title, resolveInfo.activityInfo));

                    } else {
                        Log.w("queryAddonMenuItems", "Permission denied! " + packageInfo0.packageName + " does not have required permissions.");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("queryAddonMenuItems", "Package not found! " + e);
                }
            }
        }
        Collections.sort(matches, ActivityItemInfo.title_comparator);
        return matches;
    }
    public static boolean hasPermission(@NonNull PackageInfo packageInfo)
    {
        boolean hasPermission = false;
        if (packageInfo.requestedPermissions != null) {
            String requiredPermission = REQUIRED_PERMISSION();
            for (String permission : packageInfo.requestedPermissions) {
                if (permission != null && permission.equals(requiredPermission)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        return hasPermission;
    }

    /**
     * ActivityItemInfo
     */
    public static final class ActivityItemInfo
    {
        public ActivityItemInfo(@Nullable Context context, @NonNull String title, ActivityInfo info)
        {
            this.title = title;
            this.info = info;

            if (context != null)
            {
                TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.icActionExtension});
                this.icon = typedArray.getResourceId(0, R.drawable.ic_action_extension);
                typedArray.recycle();
            }
        }

        public ActivityItemInfo(@NonNull String title, int iconResId, ActivityInfo info)
        {
            this.title = title;
            this.icon = iconResId;
            this.info = info;
        }

        protected final String title;
        @NonNull
        public String getTitle() {
            return title;
        }

        protected int icon = 0;
        public int getIcon() {
            return icon;
        }

        protected final ActivityInfo info;
        public ActivityInfo getInfo() {
            return info;
        }

        public Intent getIntent() {
            Intent intent = new Intent();
            intent.setClassName(info.packageName, info.name);
            return intent;
        }

        @NonNull
        public String toString() {
            return title;
        }

        public static final Comparator<ActivityItemInfo> title_comparator = new Comparator<ActivityItemInfo>() {
            @Override
            public int compare(ActivityItemInfo o1, ActivityItemInfo o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        };
    }
}
