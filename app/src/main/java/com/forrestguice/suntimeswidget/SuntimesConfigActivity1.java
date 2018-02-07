/**
    Copyright (C) 2014-2017 Forrest Guice
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

package com.forrestguice.suntimeswidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * Widget config activity for flippable widget.
 */
public class SuntimesConfigActivity1 extends SuntimesConfigActivity0
{
    public SuntimesConfigActivity1()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_title1));
        hideOptionCompareAgainst();
        hideOption1x1LayoutMode();
        disableOptionAllowResize();
    }

    @Override
    protected void loadAppearanceSettings(Context context)
    {
        super.loadAppearanceSettings(context);

        if (spinner_1x1mode != null)
        {
            WidgetSettings.WidgetModeSun1x1 widgetMode = WidgetSettings.WidgetModeSun1x1.WIDGETMODE1x1_SUNSET;
            spinner_1x1mode.setSelection(widgetMode.ordinal());
        }

        disableOptionAllowResize();
    }

    @Override
    protected WidgetSettings.ActionMode defaultActionMode()
    {
        return WidgetSettings.ActionMode.ONTAP_FLIPTO_NEXTITEM;
    }

    @Override
    protected WidgetSettings.ActionMode[] supportedActionModes()
    {
        return WidgetSettings.ActionMode.values();
    }

    @Override
    protected void updateWidget(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SuntimesWidget1.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

}
