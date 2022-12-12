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
import android.view.View;

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
                //return;  // TODO: call through to build-in api
            }

            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(context, tooltipText, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

}
