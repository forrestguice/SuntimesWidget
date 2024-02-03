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
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.R;

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
    @NonNull @SuppressWarnings("rawtypes")
    protected Class getConfigActivityClass(Context context) {
        return AlarmTileConfigActivity.class;
    }

    @Override
    protected void updateTile(Context context)
    {
        Tile tile = getQsTile();

        Calendar event = Calendar.getInstance(TimeZone.getDefault());
        // TODO

        int icon = R.drawable.ic_action_alarms;
        String timeDisplay = utils.calendarTimeShortDisplayString(context, event, false).toString();
        tile.setLabel(timeDisplay);
        tile.setIcon(Icon.createWithResource(this, icon));

        updateTileState(context, tile);
        tile.updateTile();
    }

    @Override
    protected Dialog createDialog(final Context context) {
        return super.createDialog(context);
    }

    @Override
    protected Drawable dialogIcon(Context context)
    {
        int icon = R.drawable.ic_action_alarms;
        Drawable d = ContextCompat.getDrawable(context, icon);

        //ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));
        //int[] attrs = { R.attr.sunriseColor, R.attr.sunsetColor };
        //TypedArray a = contextWrapper.obtainStyledAttributes(attrs);
        //int colorId = a.getResourceId(nextEvent.isRising() ? 0 : 1, R.color.text_primary);
        //a.recycle();
        //DrawableCompat.setTint(d, ContextCompat.getColor(context, colorId));
        // TODO: icon color

        return d;
    }

}
