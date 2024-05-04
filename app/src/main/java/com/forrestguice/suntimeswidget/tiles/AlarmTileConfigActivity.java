/**
    Copyright (C) 2024 Forrest Guice
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

import android.content.ContentValues;
import android.content.Context;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetActions;

@SuppressWarnings("Convert2Diamond")
public class AlarmTileConfigActivity extends ClockTileConfigActivity
{
    public AlarmTileConfigActivity()
    {
        super();
    }

    @Override
    protected void initViews( Context context )
    {
        super.initViews(context);
        setConfigActivityTitle(getString(R.string.app_name_alarmtile));

        showOptionLabels(true);
        hideTimeZoneSettings();
        hideLocationSettings();
    }

    @Override
    protected ContentValues launchActionIntentDefaults() {
        return WidgetActions.SuntimesAction.OPEN_ALARM_LIST.toContentValues();
    }

}
