/**
    Copyright (C) 2022-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.tiles;

import android.content.Context;

import com.forrestguice.suntimeswidget.ClockWidget0ConfigActivity;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.settings.TimezoneMode;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class ClockTileConfigActivity extends ClockWidget0ConfigActivity
{
    public ClockTileConfigActivity()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        hideAppearanceSettings();
        showOptionLabels(false);
        showOptionTitle(false);
        showOptionShowDate(false);
        hideLayoutSettings();
        setConfigActivityTitle(getString(R.string.app_name_clocktile));
        moveSectionToTop(R.id.appwidget_timezone_layout);
    }

    @Override
    protected WidgetSettings.ActionMode[] supportedActionModes() {
        return new WidgetSettings.ActionMode[] { WidgetSettings.ActionMode.ONTAP_LAUNCH_ACTIVITY };
    }

    @Override
    protected WidgetSettings.ActionMode defaultActionMode() {
        return ClockTileBase.DEF_ACTION_MODE;
    }

    @Override
    public boolean getDefaultLocationFromApp() {
        return ClockTileBase.DEF_LOCATION_FROM_APP;
    }

    @Override
    protected TimezoneMode getDefaultTimezoneMode() {
        return ClockTileBase.DEF_TIMEZONE_MODE;
    }

    @Override
    protected void onResetWidget() {
        new ClockTileBase(this).initDefaults(this);
    }

    @Override
    protected boolean supportsPreview() {
        return false;
    }

}
