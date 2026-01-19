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
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;
import com.forrestguice.suntimeswidget.alarmclock.bedtime.BedtimeAlarmHelper;
import com.forrestguice.suntimeswidget.alarmclock.bedtime.BedtimeSettings;
import com.forrestguice.suntimeswidget.navigation.SuntimesNavigation;
import com.forrestguice.util.concurrent.SimpleProgressListener;

import java.util.List;

@TargetApi(24)
public class BedtimeTileService extends SuntimesTileService
{
    public static final int BEDTIMETILE_APPWIDGET_ID = -4;

    @Override
    protected SuntimesTileBase initTileBase() {
        return new BedtimeTileBase(null);
    }

    @Override
    protected int appWidgetId() {
        return BEDTIMETILE_APPWIDGET_ID;
    }

    @Override
    public void onClick() {
        startActivityAndCollapse(SuntimesNavigation.getBedtimeIntent(this));
    }

    @Override
    protected void updateTile(Context context)
    {
        String label;
        switch (BedtimeSettings.getBedtimeState(context))
        {
            case BedtimeSettings.STATE_BEDTIME_ACTIVE:
                label = context.getString(R.string.configLabel_bedtime_tile_active);
                break;
            case BedtimeSettings.STATE_BEDTIME_PAUSED:
                label = context.getString(R.string.configLabel_bedtime_tile_paused);
                break;
            default:
                label = context.getString(R.string.configLabel_bedtime_tile_normal);
                break;
        }

        Tile tile = getQsTile();
        tile.setLabel(label);
        tile.setIcon(Icon.createWithResource(context, R.drawable.ic_action_bedtime));
        super.updateTile(context);    // calls updateTileState
    }

    @Override
    protected Tile updateTileState(final Context context, final Tile tile)
    {
        if (BedtimeSettings.isBedtimeModeActive(context)) {
            tile.setState(Tile.STATE_ACTIVE);
            return tile;
        }

        tile.setState(Tile.STATE_INACTIVE);
        long rowID = BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_BEDTIME_NOTIFY);
        if (rowID != BedtimeSettings.ID_NONE)
        {
            BedtimeAlarmHelper.loadAlarmItem(context, rowID, new SimpleProgressListener<List<AlarmClockItem>, AlarmClockItem>()
            {
                @Override
                public void onFinished(List<AlarmClockItem> result)
                {
                    AlarmClockItem item = ((result != null && result.size() > 0) ? result.get(0) : null);
                    tile.setState((item != null && item.enabled) ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
                    tile.updateTile();
                }
            });
            return tile;
        }
        return tile;
    }

}
