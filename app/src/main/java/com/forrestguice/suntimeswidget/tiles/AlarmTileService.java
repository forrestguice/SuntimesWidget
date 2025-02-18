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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.TimeZone;

@TargetApi(24)
public class AlarmTileService extends ClockTileService
{
    public static final int ALARMTILE_APPWIDGET_ID = -3;

    @Override
    protected int appWidgetId() {
        return ALARMTILE_APPWIDGET_ID;
    }

    @Override
    protected SuntimesTileBase initTileBase() {
        return new AlarmTileBase(null);
    }
    protected AlarmTileBase getAlarmTileBase() {
        return (AlarmTileBase) base;
    }

    @Override
    protected void updateTile(Context context)
    {
        Tile tile = getQsTile();
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_action_alarms));

        Long rowID = AlarmSettings.loadUpcomingAlarmId(context);
        if (rowID != null)
        {
            AlarmClockItem item = getAlarmTileBase().initAlarmItem(context);
            int icon = (item != null) ? item.getIcon() : R.drawable.ic_action_alarms;
            tile.setIcon(Icon.createWithResource(this, icon));

            if (item != null)
            {
                WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId());
                Calendar event = Calendar.getInstance(TimeZone.getDefault());
                event.setTimeInMillis(item.alarmtime);
                String timeDisplay = utils.calendarTimeShortDisplayString(context, event, false, timeFormat).toString();    // TODO: show day
                tile.setLabel(timeDisplay);

            } else {
                tile.setLabel(context.getString(R.string.configLabel_alarms_nextAlarm_none));
            }
        } else {
            tile.setLabel(context.getString(R.string.configLabel_alarms_nextAlarm_none));
            getAlarmTileBase().clearAlarmItem();
        }

        updateTileState(context, tile);
        tile.updateTile();
    }

    @Override
    protected Tile updateTileState(Context context, Tile tile)
    {
        AlarmClockItem item = getAlarmTileBase().initAlarmItem(context);
        tile.setState((item != null && item.rowID != -1) ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);     // Tile.STATE_ACTIVE, Tile.STATE_INACTIVE, Tile.STATE_UNAVAILABLE);
        return tile;
    }

}
