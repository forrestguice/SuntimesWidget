/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.support.widget;

import android.content.Context;
import androidx.appcompat.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.annotation.Nullable;

import java.lang.reflect.Method;

/**
 * PopupMenuCompat
 */

public class PopupMenuCompat
{
    @Deprecated
    public static PopupMenu createMenu(Context context, View view, int menuResID, @Nullable PopupMenu.OnMenuItemClickListener onClickListener, @Nullable PopupMenu.OnDismissListener onDismissListener) {
        return createMenu(context, view, menuResID, Gravity.NO_GRAVITY, onClickListener, onDismissListener);
    }

    @Deprecated
    public static PopupMenu createMenu(Context context, View view, int menuResID, int gravity, @Nullable PopupMenu.OnMenuItemClickListener onClickListener, @Nullable PopupMenu.OnDismissListener onDismissListener)
    {
        PopupMenu menu = new PopupMenu(context, view, gravity);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(menuResID, menu.getMenu());
        if (onDismissListener != null) {
            menu.setOnDismissListener(onDismissListener);
        }
        if (onClickListener != null) {
            menu.setOnMenuItemClickListener(onClickListener);
        }
        forceActionBarIcons(menu.getMenu());
        return menu;
    }

    public static abstract class PopupMenuListener
    {
        public abstract void onUpdateMenu(Context context, Menu menu);
        public abstract boolean onMenuItemClick(MenuItem menuItem);
        public void onDismiss() {}
        public boolean hasOnDismissListener() {
            return false;
        }
    }

    public static PopupMenu createMenu(Context context, View view, PopupMenuListener listener) {
        return createMenu(context, view, null, listener);
    }
    public static PopupMenu createMenu(Context context, View view, @Nullable Integer menuResID, @Nullable PopupMenuListener listener) {
        return createMenu(context, view, menuResID, Gravity.NO_GRAVITY, listener);
    }
    public static PopupMenu createMenu(Context context, View view, @Nullable Integer menuResID, int gravity, @Nullable final PopupMenuListener listener)
    {
        PopupMenu menu = new PopupMenu(context, view, gravity);
        if (menuResID != null) {
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(menuResID, menu.getMenu());
        }
        if (listener != null)
        {
            if (listener.hasOnDismissListener())
            {
                menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu popupMenu) {
                        listener.onDismiss();
                    }
                });
            }

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return listener.onMenuItemClick(menuItem);
                }
            });
            listener.onUpdateMenu(context, menu.getMenu());
        }
        forceActionBarIcons(menu.getMenu());
        return menu;
    }

    /**
     * from http://stackoverflow.com/questions/18374183/how-to-show-icons-in-overflow-menu-in-actionbar
     * note: when targetting api29+ this method can be replaced with PopupMenu.setForceShowIcon
     */
    @Deprecated
    public static void forceActionBarIcons(Menu menu)
    {
        if (menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);

                } catch (Exception e) {
                    Log.e("ActionBarIcons", "failed to set show overflow icons", e);
                }
            }
        }
    }

}
