/**
    Copyright (C) 2019 Forrest Guice
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
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

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
        button.clearAnimation();
        if (button.getVisibility() != View.VISIBLE) {
            button.setAlpha(0f);
            button.setVisibility(View.VISIBLE);
        }
        if (button.getAlpha() != 1f) {
            button.animate().setDuration(duration).alpha(1f);
        }
    }

    public static void fadeOutButton(final ImageButton button, final int duration)
    {
        if (button.getVisibility() != View.GONE)
        {
            button.clearAnimation();
            if (button.getAlpha() == 0f) {
                button.setVisibility(View.GONE);

            } else {
                button.setAlpha(1f);
                button.animate().setDuration(duration).alpha(0f).setListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        button.setVisibility(View.GONE);
                        button.setAlpha(1f);
                        button.animate().setListener(null);
                        button.clearAnimation();
                    }
                });
            }
        }
    }
}
