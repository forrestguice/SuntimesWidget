package com.forrestguice.support.design.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

public class PopupMenu extends android.support.v7.widget.PopupMenu
{
    public PopupMenu(@NonNull Context context, @NonNull View anchor) {
        super(context, anchor);
    }

    public PopupMenu(@NonNull Context context, @NonNull View anchor, int gravity) {
        super(context, anchor, gravity);
    }

    public PopupMenu(@NonNull Context context, @NonNull View anchor, int gravity, int popupStyleAttr, int popupStyleRes) {
        super(context, anchor, gravity, popupStyleAttr, popupStyleRes);
    }

    public interface OnDismissListener {
        void onDismiss(PopupMenu popupMenu);
    }

    public static void setOnDismissListener(final PopupMenu menu, final OnDismissListener onContextMenuDismissed)
    {
        menu.setOnDismissListener(new android.support.v7.widget.PopupMenu.OnDismissListener() {
            public void onDismiss(android.support.v7.widget.PopupMenu popupMenu) {
                onContextMenuDismissed.onDismiss(menu);
            }
        });
    }
}
