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
import android.support.annotation.NonNull;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.Calendar;
import java.util.TimeZone;

@TargetApi(24)
public class NextEventTileService extends ClockTileService
{
    public static final int NEXTEVENTTILE_APPWIDGET_ID = -2;

    @Override
    protected int appWidgetId() {
        return NEXTEVENTTILE_APPWIDGET_ID;
    }

    @Override
    @NonNull @SuppressWarnings("rawtypes")
    protected Class getConfigActivityClass(Context context) {
        return NextEventTileConfigActivity.class;
    }

    @Override
    protected void updateTile(Context context)
    {
        Tile tile = getQsTile();

        //TimeZone timezone = timezone(context);
        //String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, timezone);
        //boolean isSolarTime = WidgetTimezones.LocalMeanTime.TIMEZONEID.equals(timezone.getID()) ||
        //        WidgetTimezones.ApparentSolarTime.TIMEZONEID.equals(timezone.getID());

        initDataset(context);
        SuntimesRiseSetDataset.SearchResult result = dataset.findNextEvent();

        Calendar event = Calendar.getInstance(TimeZone.getDefault());
        event.setTimeInMillis(result.getCalendar().getTimeInMillis());

        WidgetSettings.RiseSetDataMode mode = result.getMode();

        int icon = (mode != null && mode.getTimeMode() == WidgetSettings.TimeMode.NOON) ? R.drawable.ic_noon_tile
                : (result.isRising() ? R.drawable.svg_sunrise : R.drawable.svg_sunset);

        String timeDisplay = utils.calendarTimeShortDisplayString(context, event, false).toString() + " " + (mode != null ? mode.toString() : "null"); // context.getString(result.isRising() ? R.string.sunrise : R.string.sunset);
        tile.setLabel(timeDisplay);
        tile.setIcon(Icon.createWithResource(this, icon));

        updateTileState(context, tile);
        tile.updateTile();
    }

    protected SuntimesRiseSetDataset initDataset(Context context)
    {
        if (dataset == null) {
            dataset = new SuntimesRiseSetDataset(context, appWidgetId());
            dataset.calculateData();
        }
        return dataset;
    }
    protected SuntimesRiseSetDataset dataset = null;

}
