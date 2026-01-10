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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;

/**
 * TooltipCompat
 */

public class TooltipCompat
{
    /**
     * setTooltipText
     */
    public static void setTooltipText(View view, @Nullable final CharSequence tooltipText)
    {
        final Context context = view.getContext();
        if (context != null && tooltipText != null)
        {
            if (context.getApplicationContext().getApplicationInfo().targetSdkVersion >= 26) {
                //return;  // TODO: call through to built-in api
            }

            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int[] position = new int[2];
                    v.getLocationOnScreen(position);
                    position[1] += v.getHeight() / 2;

                    android.widget.Toast toast = makeText(context, tooltipText, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.START, position[0], position[1]);
                    toast.show();
                    return true;
                }
            });
        }
    }

    @SuppressLint("ShowToast")
    private static android.widget.Toast makeText(Context context, CharSequence text, int duration)
    {
        android.widget.Toast toast = android.widget.Toast.makeText(context, text, duration);
        if (context.getApplicationContext().getApplicationInfo().targetSdkVersion < 30)
        {
            View v = toast.getView();    // Toast.getView returns null for targetApi R+
            if (v != null)
            {
                v.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.tooltip_frame));
                TextView message = (TextView) v.findViewById(android.R.id.message);
                if (message != null)
                {
                    if (Build.VERSION.SDK_INT >= 23) {
                        message.setTextAppearance(R.style.TooltipTextAppearance);
                    } else {
                        int paddingDp = (int)context.getResources().getDimension(R.dimen.tooltip_margin0);
                        message.setTextAppearance(context, R.style.TooltipTextAppearance);
                        message.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
                        message.setTypeface(Typeface.DEFAULT);
                    }
                }
            }
        }
        return toast;
    }

}
