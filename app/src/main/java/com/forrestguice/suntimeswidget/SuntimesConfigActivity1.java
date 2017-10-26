/**
    Copyright (C) 2014 Forrest Guice
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
import android.widget.ArrayAdapter;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * Flippable widget config activity.
 */
public class SuntimesConfigActivity1 extends SuntimesConfigActivity
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
        hideOptionLayoutMode();
        disableOptionAllowResize();
    }

    @Override
    protected void loadAppearanceSettings(Context context)
    {
        super.loadAppearanceSettings(context);

        if (spinner_1x1mode != null)
        {
            WidgetSettings.WidgetMode1x1 widgetMode = WidgetSettings.WidgetMode1x1.WIDGETMODE1x1_SUNSET;
            spinner_1x1mode.setSelection(widgetMode.ordinal());
        }

        disableOptionAllowResize();
    }

    @Override
    protected void loadActionSettings(Context context)
    {
        if (spinner_onTap != null)
        {
            WidgetSettings.ActionMode actionMode = WidgetSettings.ActionMode.ONTAP_FLIPTO_NEXTITEM;
            spinner_onTap.setSelection(actionMode.ordinal());
        }
    }

    @Override
    protected void updateWidget(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SuntimesWidget1.updateAppWidget(context, appWidgetManager, appWidgetId, null);
    }

    @Override
    protected ArrayAdapter<WidgetSettings.ActionMode> createAdapter_actionMode()
    {
        ArrayAdapter<WidgetSettings.ActionMode> adapter = new ArrayAdapter<WidgetSettings.ActionMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.ActionMode.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

}
