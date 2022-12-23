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

package com.forrestguice.suntimeswidget.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;

import com.forrestguice.suntimeswidget.SuntimesUtils;

/**
 * PopupMenuCompat
 */

public class PopupMenuCompat
{
    public static PopupMenu createMenu(Context context, View view, int menuId, PopupMenu.OnMenuItemClickListener listener) {
        return createMenu(context, view, menuId, listener, null);
    }

    public static PopupMenu createMenu(Context context, View view, int menuResID, @Nullable PopupMenu.OnMenuItemClickListener onClickListener, @Nullable PopupMenu.OnDismissListener onDismissListener) {
        return createMenu(context, view, menuResID, Gravity.NO_GRAVITY, onClickListener, onDismissListener);
    }

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
        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        return menu;
    }
}
