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

package com.forrestguice.suntimeswidget.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.alarmclock.bedtime.BedtimeActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;

import java.lang.ref.WeakReference;

public class SuntimesNavigation
{
    private static final int anim_in = R.anim.transition_swap_in;
    private static final int anim_out = R.anim.transition_swap_out;

    private WeakReference<Activity> activityRef;

    public SuntimesNavigation(Activity activity)
    {
        Toolbar menuBar = (Toolbar) activity.findViewById(R.id.app_menubar);
        init(activity, menuBar);
    }

    public SuntimesNavigation(Activity activity, Toolbar menuBar, int menuItemID)
    {
        this.menuItemID = menuItemID;
        init(activity, menuBar);
    }

    protected void init(Activity activity, @Nullable Toolbar menuBar)
    {
        activityRef = new WeakReference<>(activity);

        drawer = (DrawerLayout) activity.findViewById(R.id.app_drawer);
        if (drawer != null && menuBar != null)
        {
            if (AppSettings.NAVIGATION_SIDEBAR.equals(AppSettings.loadNavModePref(activity)))
            {
                drawerToggle = new ActionBarDrawerToggle(activity, drawer, menuBar, R.string.configAction_openNavDrawer, R.string.configAction_closeNavDrawer);
                drawerToggle.setDrawerIndicatorEnabled(AppSettings.NAVIGATION_SIDEBAR.equals(AppSettings.loadNavModePref(activity)));
                drawer.addDrawerListener(drawerToggle);
                drawerToggle.syncState();
            }
        }

        navigation = (NavigationView) activity.findViewById(R.id.app_navigation);
        if (navigation != null) {
            navigation.setCheckedItem(menuItemID);
            navigation.setNavigationItemSelectedListener(onNavigationItemSelectedListener0);
        }
    }

    private DrawerLayout drawer;
    public DrawerLayout getDrawerLayout() {
        return drawer;
    }

    private ActionBarDrawerToggle drawerToggle;
    public ActionBarDrawerToggle getDrawerToggle() {
        return drawerToggle;
    }

    private NavigationView navigation;
    public NavigationView getNavigationView() {
        return navigation;
    }

    private int menuItemID = -1;
    public void setCurrentMenuItemID(int id) {
        menuItemID = id;
    }

    private final NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener0 = new NavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            if (item.getItemId() == menuItemID)
            {
                closeNavigationDrawer();
                return true;
            }

            boolean result = false;
            if (onNavigationItemSelectedListener != null)
            {
                result = onNavigationItemSelectedListener.onNavigationItemSelected(item);
                if (result) {
                    closeNavigationDrawer();
                    return true;
                }
            }

            final Activity activity = activityRef.get();
            if (activity != null)
            {
                closeNavigationDrawer();

                final int itemID = item.getItemId();
                activity.getWindow().getDecorView().postDelayed(
                        new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                switch (itemID)
                                {
                                    case R.id.action_suntimes:
                                        showSuntimes(activity);
                                        break;

                                    case R.id.action_alarms:
                                        showSuntimesAlarms(activity);
                                        break;

                                    case R.id.action_settings:
                                        showSettings(activity);
                                        break;

                                    case R.id.action_about:
                                        showAbout(activity);
                                        break;
                                }
                            }
                        }, 250);

                for (int navItemID : NAV_MENU_ITEMS) {
                    if (itemID == navItemID) {
                        return true;
                    }
                }
            }
            return false;
        }
    };

    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = null;
    public void setOnNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener listener) {
        onNavigationItemSelectedListener = listener;
    }

    public void closeNavigationDrawer()
    {
        if (drawer != null)
        {
            drawer.clearFocus();
            drawer.closeDrawers();
            drawer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigation.setCheckedItem(menuItemID);
                }
            }, 500);
        }
    }
    public boolean isNavigationDrawerOpen()
    {
        if (drawer != null) {
            return drawer.isDrawerOpen(GravityCompat.START);
        }
        return false;
    }

    protected void overridePendingTransition(@NonNull Activity activity)
    {
        if (Build.VERSION.SDK_INT < 16) {   // 16+ uses ActivityOptions instead
            activity.overridePendingTransition(anim_in, anim_out);
        }
    }

    @TargetApi(16)
    public ActivityOptions getActivityOptions(@NonNull Activity activity) {
        return ActivityOptions.makeCustomAnimation(activity, anim_in, anim_out);
    }

    private void startActivity(@NonNull Activity activity, @NonNull Intent intent)
    {
        if (Build.VERSION.SDK_INT >= 16) {
            activity.startActivity(intent, getActivityOptions(activity).toBundle());
        } else {
            activity.startActivity(intent);
        }
    }
    private void startActivityForResult(@NonNull Activity activity, @NonNull Intent intent, int requestCode)
    {
        if (Build.VERSION.SDK_INT >= 16) {
            activity.startActivityForResult(intent, requestCode, getActivityOptions(activity).toBundle());
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public void showSuntimes(@NonNull final Activity activity)
    {
        Intent intent = new Intent(activity, SuntimesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activity, intent);
        overridePendingTransition(activity);
    }

    public void showSuntimesAlarms(@NonNull final Activity activity)
    {
        Intent intent = new Intent(activity, AlarmClockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activity, intent);
        overridePendingTransition(activity);
    }

    public static Intent getBedtimeIntent(Context context)
    {
        Intent intent = new Intent(context, BedtimeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static final int REQUEST_SETTINGS = 2200;
    public void showSettings(@NonNull Activity activity)
    {
        Intent intent = new Intent(activity, SuntimesSettingsActivity.class);
        startActivityForResult(activity, intent, REQUEST_SETTINGS);
        overridePendingTransition(activity);
    }

    public void showAbout(@NonNull Activity activity)
    {
        Intent about = new Intent(activity, AboutActivity.class);
        startActivity(activity, about);
        overridePendingTransition(activity);
    }

    /**
     * updates navigation items in overflow menus; with simple navigation these items are
     * hidden (shown in the sidebar instead).
     */
    public static void updateMenuNavigationItems(Context context, Menu menu)
    {
        if (menu != null && context != null)
        {
            boolean simpleNavigation = AppSettings.NAVIGATION_SIMPLE.equals(AppSettings.loadNavModePref(context));
            for (int itemID : NAV_MENU_ITEMS)
            {
                MenuItem item = menu.findItem(itemID);
                if (item != null) {
                    item.setVisible(simpleNavigation);
                }
            }
        }
    }

    public static final int[] NAV_MENU_ITEMS = new int[] {
            R.id.action_suntimes,
            R.id.action_alarms,
            R.id.action_settings,
            R.id.action_about
    };

}
