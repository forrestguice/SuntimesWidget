/**
    Copyright (C) 2019-2023 Forrest Guice
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.Preference;
import com.forrestguice.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import com.forrestguice.support.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;

public class ViewUtils
{
    public static int ANIM_SHORT = 200;
    public static int ANIM_MEDIUM = 400;
    public static int ANIM_LONG = 500;
    public static int ANIM_VERYLONG = 1000;

    public static void initUtils(Context context)
    {
        ANIM_SHORT = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        ANIM_MEDIUM = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
        ANIM_LONG = context.getResources().getInteger(android.R.integer.config_longAnimTime);
    }

    public static void fadeInButton(final ImageButton button, final int duration)
    {
        if (Build.VERSION.SDK_INT >= 12)
        {
            button.clearAnimation();

            if (button.getVisibility() != View.VISIBLE) {
                button.setAlpha(0f);
                button.setVisibility(View.VISIBLE);
            }
            if (button.getAlpha() != 1f) {
                button.animate().setDuration(duration).alpha(1f);
            }

        } else {
            button.setVisibility(View.VISIBLE);
        }
    }

    public static void fadeOutButton(final ImageButton button, final int duration)
    {
        if (button.getVisibility() != View.GONE)
        {
            if (Build.VERSION.SDK_INT >= 12)
            {
                button.clearAnimation();
                if (button.getAlpha() == 0f) {
                    button.setVisibility(View.GONE);

                } else {
                    button.setAlpha(1f);
                    button.animate().setDuration(duration).alpha(0f).setListener(new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            super.onAnimationEnd(animation);
                            button.setVisibility(View.GONE);
                            //noinspection ConstantConditions
                            if (Build.VERSION.SDK_INT >= 12)
                            {
                                button.setAlpha(1f);
                                button.animate().setListener(null);
                                button.clearAnimation();
                            }
                        }
                    });
                }

            } else {
                button.setVisibility(View.GONE);
            }
        }
    }

    public static void initPeekHeight(DialogInterface dialog, int bottomViewResId)
    {
        if (dialog != null) {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null)
            {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                View divider1 = bottomSheet.findViewById(bottomViewResId);
                if (divider1 != null)
                {
                    Rect headerBounds = new Rect();
                    divider1.getDrawingRect(headerBounds);
                    layout.offsetDescendantRectToMyCoords(divider1, headerBounds);
                    behavior.setPeekHeight(headerBounds.bottom); // + (int)getResources().getDimension(R.dimen.dialog_margin));

                } else {
                    behavior.setPeekHeight(-1);
                }
            }
        }
    }

    public static void disableTouchOutsideBehavior(Dialog dialog)
    {
        Window window = (dialog != null ? dialog.getWindow() : null);
        if (window != null) {
            View decorView = window.getDecorView().findViewById(android.support.design.R.id.touch_outside);
            decorView.setOnClickListener(null);
        }
    }

    @SuppressLint("ResourceType")
    public static void themeSnackbar(Context context, Snackbar snackbar, Integer[] colorOverrides)
    {
        Integer[] colors = new Integer[] {null, null, null};
        int[] colorAttrs = { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor, R.attr.selectableItemBackground };
        TypedArray a = context.obtainStyledAttributes(colorAttrs);
        colors[0] = ContextCompat.getColor(context, a.getResourceId(0, android.R.color.primary_text_dark));
        colors[1] = ContextCompat.getColor(context, a.getResourceId(1, R.color.text_accent_dark));
        colors[2] = ContextCompat.getColor(context, a.getResourceId(2, R.color.card_bg_dark));
        Drawable buttonDrawable = ContextCompat.getDrawable(context, a.getResourceId(3, R.drawable.button_fab_dark));
        int buttonPadding = (int)context.getResources().getDimension(R.dimen.snackbar_button_padding);
        a.recycle();

        if (colorOverrides != null && colorOverrides.length == colors.length) {
            for (int i=0; i<colors.length; i++) {
                if (colorOverrides[i] != null) {
                    colors[i] = colorOverrides[i];
                }
            }
        }

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(colors[2]);
        snackbar.setActionTextColor(colors[1]);

        TextView snackbarText = (TextView)snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        if (snackbarText != null) {
            snackbarText.setTextColor(colors[0]);
            snackbarText.setMaxLines(3);
        }

        View snackbarAction = snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        if (snackbarAction != null) {
            if (Build.VERSION.SDK_INT >= 16)
            {
                snackbarAction.setBackground(buttonDrawable);
                snackbarAction.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            }
        }
    }

    /**
     * ThrottledClickListener
     */
    public static class ThrottledClickListener implements View.OnClickListener
    {
        protected long delayMs;
        protected Long previousClickAt;
        protected View.OnClickListener listener;

        public ThrottledClickListener(@NonNull View.OnClickListener listener) {
            this(listener, 1000);
        }

        public ThrottledClickListener(@NonNull View.OnClickListener listener, long delayMs)
        {
            this.delayMs = delayMs;
            this.listener = listener;
            if (listener == null) {
                throw new NullPointerException("OnClickListener is null!");
            }
        }

        @Override
        public void onClick(View v)
        {
            long currentClickAt = System.currentTimeMillis();
            if (previousClickAt == null || Math.abs(currentClickAt - previousClickAt) > delayMs) {
                previousClickAt = currentClickAt;
                listener.onClick(v);
            }
        }
    }

    /**
     * ThrottledMenuItemClickListener
     */
    public static class ThrottledMenuItemClickListener implements PopupMenu.OnMenuItemClickListener
    {
        protected long delayMs;
        protected Long previousClickAt;
        protected PopupMenu.OnMenuItemClickListener listener;

        public ThrottledMenuItemClickListener(@NonNull PopupMenu.OnMenuItemClickListener listener) {
            this(listener, 750);
        }

        public ThrottledMenuItemClickListener(@NonNull PopupMenu.OnMenuItemClickListener listener, long delayMs)
        {
            this.delayMs = delayMs;
            this.listener = listener;
            if (listener == null) {
                throw new NullPointerException("OnMenuItemClickListener is null!");
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            long currentClickAt = System.currentTimeMillis();
            if (previousClickAt == null || Math.abs(currentClickAt - previousClickAt) > delayMs) {
                previousClickAt = currentClickAt;
                return listener.onMenuItemClick(item);
            }
            if (BuildConfig.DEBUG) {
                Log.d("DEBUG", "onMenuItemClick: throttled: " + Math.abs(currentClickAt - previousClickAt));
            }
            return true;
        }
    }

    /**
     * ThrottledPreferenceClickListener
     */
    public static class ThrottledPreferenceClickListener implements Preference.OnPreferenceClickListener
    {
        protected long delayMs;
        protected Long previousClickAt;
        protected Preference.OnPreferenceClickListener listener;

        public ThrottledPreferenceClickListener(@NonNull Preference.OnPreferenceClickListener listener) {
            this(listener, 1000);
        }

        public ThrottledPreferenceClickListener(@NonNull Preference.OnPreferenceClickListener listener, long delayMs)
        {
            this.delayMs = delayMs;
            this.listener = listener;
            if (listener == null) {
                throw new NullPointerException("OnPreferenceClickListener is null!");
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference)
        {
            long currentClickAt = System.currentTimeMillis();
            if (previousClickAt == null || Math.abs(currentClickAt - previousClickAt) > delayMs) {
                previousClickAt = currentClickAt;
                return listener.onPreferenceClick(preference);
            }
            return false;
        }
    }

}
