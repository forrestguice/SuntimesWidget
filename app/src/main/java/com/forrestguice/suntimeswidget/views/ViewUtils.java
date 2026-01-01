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
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.preference.Preference;

import com.forrestguice.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.forrestguice.suntimeswidget.BuildConfig;

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

    public static void disableTouchOutsideBehavior(Dialog dialog)
    {
        Window window = (dialog != null ? dialog.getWindow() : null);
        if (window != null) {
            View decorView = window.getDecorView().findViewById(getTouchOutsideResourceID());
            decorView.setOnClickListener(null);
        }
    }

    public static int getTouchOutsideResourceID() {
        return android.support.design.R.id.touch_outside;    // support libraries
        //return com.google.android.material.R.id.touch_outside;   // androidx
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
     * ThrottledPopupMenuListener
     */
    public static class ThrottledPopupMenuListener extends PopupMenuCompat.PopupMenuListener
    {
        protected long delayMs;
        protected Long previousClickAt;
        protected PopupMenuCompat.PopupMenuListener listener;

        public ThrottledPopupMenuListener(@NonNull PopupMenuCompat.PopupMenuListener listener) {
            this(listener, 750);
        }

        public ThrottledPopupMenuListener(@NonNull PopupMenuCompat.PopupMenuListener listener, long delayMs)
        {
            this.delayMs = delayMs;
            this.listener = listener;
            if (listener == null) {
                throw new NullPointerException("OnMenuItemClickListener is null!");
            }
        }

        @Override
        public void onUpdateMenu(Context context, Menu menu) {
            listener.onUpdateMenu(context, menu);
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
