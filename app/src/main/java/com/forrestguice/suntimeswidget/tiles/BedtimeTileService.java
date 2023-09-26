/**
    Copyright (C) 2023 Forrest Guice
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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.ui.bedtime.BedtimeSettings;
import com.forrestguice.suntimeswidget.navigation.SuntimesNavigation;

@TargetApi(24)
public class BedtimeTileService extends SuntimesTileService
{
    public static final int BEDTIMETILE_APPWIDGET_ID = -3;

    @Override
    protected int appWidgetId() {
        return BEDTIMETILE_APPWIDGET_ID;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Class getConfigActivityClass(Context context) {
        return null;
    }

    @Override
    public void onClick() {
        startActivityAndCollapse(SuntimesNavigation.getBedtimeIntent(this));
    }

    @Override
    protected void updateTile(Context context)
    {
        Tile tile = getQsTile();
        tile.setLabel(context.getString(R.string.configLabel_bedtime));
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_action_bedtime));
        super.updateTile(context);    // calls updateTileState
    }

    @Override
    protected Tile updateTileState(Context context, Tile tile)
    {
        tile.setState(BedtimeSettings.isBedtimeModeActive(context) ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        return tile;
    }

}
