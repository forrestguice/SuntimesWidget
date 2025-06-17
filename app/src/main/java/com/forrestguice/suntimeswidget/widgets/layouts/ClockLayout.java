/**
   Copyright (C) 2019-2021 Forrest Guice
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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.DataSubstitutions;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public abstract class ClockLayout extends SuntimesLayout
{

    protected boolean scaleBase = WidgetSettings.PREF_DEF_APPEARANCE_SCALEBASE;

    public ClockLayout()
    {
        initLayoutID();
    }

    /**
     * Called by widget before themeViews and updateViews to give the layout obj an opportunity to
     * modify its state based on the supplied data.
     */
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesClockData data) {
        this.scaleBase = WidgetSettings.loadScaleBasePref(context, appWidgetId);
    }

    /**
     * Apply the provided data to the RemoteViews this layout knows about.
     * @param context the android application context
     * @param appWidgetId the android widget ID to update
     * @param views the RemoteViews to apply the data to
     */
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        String titlePattern = WidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = DataSubstitutions.displayStringForTitlePattern0(context, titlePattern, data);
        CharSequence title = (boldTitle ? SuntimesUtils.createBoldSpan(null, titleText, titleText) : titleText);
        views.setTextViewText(R.id.text_title, title);
        //Log.v("DEBUG", "title text: " + titleText);
    }

}
