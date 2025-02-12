package com.forrestguice.support.design.view;

public class MenuItemCompat
{
    @java.lang.Deprecated
    public static android.view.MenuItem setOnActionExpandListener(android.view.MenuItem item, android.support.v4.view.MenuItemCompat.OnActionExpandListener listener) {
        return android.support.v4.view.MenuItemCompat.setOnActionExpandListener(item, listener);
    }

    @java.lang.Deprecated
    public interface OnActionExpandListener extends android.support.v4.view.MenuItemCompat.OnActionExpandListener
    {
        boolean onMenuItemActionExpand(android.view.MenuItem menuItem);
        boolean onMenuItemActionCollapse(android.view.MenuItem menuItem);
    }
}