package com.forrestguice.support.appcompat.app;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import com.forrestguice.support.design.widget.Toolbar;

public class ActionBarDrawerToggle extends android.support.v7.app.ActionBarDrawerToggle
{
    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
    }
}
