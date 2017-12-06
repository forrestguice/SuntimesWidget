/**
    Copyright (C) 2017 Forrest Guice
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
import android.view.View;
import android.widget.ArrayAdapter;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * Solstice / Equinox widget config activity.
 */
public class SuntimesConfigActivity2 extends SuntimesConfigActivity0
{
    public SuntimesConfigActivity2()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.configLabel_title2));

        hideOptionCompareAgainst();
        hideOption1x1LayoutMode();
        showOptionShowNoon(false);
        disableOptionAllowResize();
    }

    @Override
    protected void updateWidget(Context context)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SuntimesWidget2.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    protected void initTimeMode( Context context )
    {
        if (spinner_timeMode != null)
        {
            ArrayAdapter<WidgetSettings.SolsticeEquinoxMode> spinner_timeModeAdapter;
            spinner_timeModeAdapter = new ArrayAdapter<WidgetSettings.SolsticeEquinoxMode>(this, R.layout.layout_listitem_oneline, WidgetSettings.SolsticeEquinoxMode.values() );
            spinner_timeModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_timeMode.setAdapter(spinner_timeModeAdapter);
        }

        if (button_timeModeHelp != null)
        {
            button_timeModeHelp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    HelpDialog helpDialog = new HelpDialog();
                    helpDialog.setContent(getString(R.string.help_general_timeMode2));
                    helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
                }
            });
        }
    }

    @Override
    protected void loadTimeMode(Context context)
    {
        WidgetSettings.SolsticeEquinoxMode timeMode = WidgetSettings.loadTimeMode2Pref(context, appWidgetId);
        spinner_timeMode.setSelection(timeMode.ordinal());
    }

    @Override
    protected void saveTimeMode(Context context)
    {
        final WidgetSettings.SolsticeEquinoxMode[] timeModes = WidgetSettings.SolsticeEquinoxMode.values();
        WidgetSettings.SolsticeEquinoxMode timeMode = timeModes[ spinner_timeMode.getSelectedItemPosition()];
        WidgetSettings.saveTimeMode2Pref(context, appWidgetId, timeMode);
    }

}
