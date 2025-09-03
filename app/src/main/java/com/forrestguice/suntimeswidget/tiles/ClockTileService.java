/**
    Copyright (C) 2022 Forrest Guice
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
import com.forrestguice.suntimeswidget.calculator.settings.TimeFormatMode;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.TimeZone;

@TargetApi(24)
public class ClockTileService extends SuntimesTileService
{
    public static final int CLOCKTILE_APPWIDGET_ID = -1;

    @Override
    protected SuntimesTileBase initTileBase() {
        return new ClockTileBase(null);
    }

    @Override
    protected int appWidgetId() {
        return CLOCKTILE_APPWIDGET_ID;
    }

    @Override
    protected void updateTile(Context context)
    {
        Tile tile = getQsTile();

        TimeZone timezone = base.timezone(context);
        String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, timezone);
        boolean isSolarTime = WidgetTimezones.LocalMeanTime.TIMEZONEID.equals(timezone.getID()) ||
                WidgetTimezones.ApparentSolarTime.TIMEZONEID.equals(timezone.getID());

        TimeFormatMode formatMode = WidgetSettings.loadTimeFormatModePref(context, base.appWidgetId());
        String timeDisplay = utils.calendarTimeShortDisplayString(context, base.now(context), false, formatMode).toString() + " " + tzDisplay;
        tile.setLabel(timeDisplay);
        tile.setIcon(Icon.createWithResource(this, isSolarTime ? R.drawable.ic_weather_sunny : R.drawable.ic_action_time));

        updateTileState(context, tile).updateTile();
    }

}
