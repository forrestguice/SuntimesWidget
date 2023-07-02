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

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesSettingsActivity;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;

import java.lang.ref.WeakReference;

@SuppressWarnings("Convert2Diamond")
public class SuntimesNavigation
{
    private WeakReference<Activity> activityRef;

    public SuntimesNavigation(Activity activity)
    {
        Toolbar menuBar = (Toolbar) activity.findViewById(R.id.app_menubar);
        init(activity, menuBar);
    }

    public SuntimesNavigation(Activity activity, Toolbar menuBar)
    {
        init(activity, menuBar);
    }

    protected void init(Activity activity, @Nullable Toolbar menuBar)
    {
        activityRef = new WeakReference<>(activity);

        drawer = (DrawerLayout) activity.findViewById(R.id.app_drawer);
        if (drawer != null && menuBar != null)
        {
            drawerToggle = new ActionBarDrawerToggle(activity, drawer, menuBar, R.string.configAction_openNavDrawer, R.string.configAction_closeNavDrawer);
            drawer.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
        }

        navigation = (NavigationView) activity.findViewById(R.id.app_navigation);
        if (navigation != null) {
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
    public void setIgnoreMenuItemID(int id) {
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

            Activity activity = activityRef.get();
            if (activity != null)
            {
                switch (item.getItemId())
                {
                    case R.id.action_suntimes:
                        showSuntimes(activity);
                        closeNavigationDrawer();
                        return true;

                    case R.id.action_alarms:
                        showSuntimesAlarms(activity);
                        closeNavigationDrawer();
                        return true;

                    case R.id.action_settings:
                        showSettings(activity);
                        closeNavigationDrawer();
                        return true;

                    case R.id.action_about:
                        showAbout(activity);
                        closeNavigationDrawer();
                        return true;

                    default: return false;
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
        if (drawer != null) {
            drawer.closeDrawers();
        }
    }

    protected void overridePendingTransition(@NonNull Activity activity)
    {
        activity.overridePendingTransition(R.anim.transition_swap_in, R.anim.transition_swap_out);
    }

    public void showSuntimes(@NonNull Activity activity)
    {
        Intent intent = new Intent(activity, SuntimesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        overridePendingTransition(activity);
    }

    public void showSuntimesAlarms(@NonNull Activity activity)
    {
        Intent intent = new Intent(activity, AlarmClockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        overridePendingTransition(activity);
    }

    public static final int REQUEST_SETTINGS = 2200;
    public void showSettings(@NonNull Activity activity)
    {
        Intent settingsIntent = new Intent(activity, SuntimesSettingsActivity.class);
        activity.startActivityForResult(settingsIntent, REQUEST_SETTINGS);
        overridePendingTransition(activity);
    }

    public void showAbout(@NonNull Activity activity)
    {
        Intent about = new Intent(activity, AboutActivity.class);
        activity.startActivity(about);
        overridePendingTransition(activity);
    }

}
