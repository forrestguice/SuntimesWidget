/**
   Copyright (C) 2014-2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.layouts;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public abstract class SuntimesLayout
{
    protected static final SuntimesUtils utils = new SuntimesUtils();

    protected int layoutID;

    public SuntimesLayout()
    {
        initLayoutID();
    }

    protected boolean boldTitle = false;
    protected boolean boldTime = false;

    /**
     * All SuntimesLayout subclasses must implement this method and provide a value for
     * the layoutID. The initLayoutID method should be called from the constructor.
     */
    public abstract void initLayoutID();

    /**
     * @return a layoutID that can be used to create a RemoteViews obj
     */
    public int layoutID()
    {
        return this.layoutID;
    }

    /**
     * @param context the android application context
     * @return a RemoteViews for this layout
     */
    public RemoteViews getViews(Context context)
    {
        return new RemoteViews(context.getPackageName(), layoutID);
    }

    /**
     * Apply a theme (from saved settings for the given appWidgetID) to the RemoteViews this layout
     * knows about.
     * @param context the android application context
     * @param views the RemoteViews to apply the theme to
     * @param appWidgetId the appWidgetID to use when retrieving theme settings
     */
    public void themeViews(Context context, RemoteViews views, int appWidgetId)
    {
        SuntimesTheme theme = WidgetSettings.loadThemePref(context, appWidgetId);
        themeViews(context, views, theme);
    }

    /**
     * Apply the provided theme to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param views the RemoteViews to apply the theme to
     * @param theme the theme object to apply to the views
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        // theme background
        views.setInt(R.id.widgetframe_inner, "setBackgroundResource", theme.getBackgroundId());
        // BUG: setting background screws up padding; pre jellybean versions can't correct for it!
        // either live w/ it, or move this call into if statement below .. however then the background
        // doesn't update for pre jellybean versions, confusing users into thinking themes don't work
        // at all (and they really don't considering the background is 90% of the theme).

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            // fix theme padding (setting background resets padding to 0 for some reason)
            int[] padding = theme.getPaddingPixels(context);
            views.setViewPadding(R.id.widgetframe_inner, padding[0], padding[1], padding[2], padding[3]);

            // theme title text size
            views.setTextViewTextSize(R.id.text_title, TypedValue.COMPLEX_UNIT_SP, theme.getTitleSizeSp());
        }

        // theme title and text
        int titleColor = theme.getTitleColor();
        views.setTextColor(R.id.text_title, titleColor);
        boldTitle = theme.getTitleBold();
        boldTime = theme.getTimeBold();
    }

}
