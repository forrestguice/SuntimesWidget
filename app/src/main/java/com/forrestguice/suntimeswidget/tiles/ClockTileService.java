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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData2;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;
import java.util.TimeZone;

@TargetApi(24)
public class ClockTileService extends SuntimesTileService
{
    public static final int CLOCKTILE_APPWIDGET_ID = -1;

    public static final boolean DEF_LOCATION_FROM_APP = true;

    @Override
    protected int appWidgetId() {
        return CLOCKTILE_APPWIDGET_ID;
    }

    @NonNull
    @SuppressWarnings("rawtypes")
    protected Class getConfigActivityClass(Context context) {
        return ClockTileConfigActivity.class;
    }

    @Override
    protected void initDefaults(Context context)
    {
        super.initDefaults(context);
        WidgetSettings.saveLocationFromAppPref(context, appWidgetId(), DEF_LOCATION_FROM_APP);
    }

    @Override
    protected void updateTile(Context context)
    {
        Tile tile = getQsTile();

        TimeZone timezone = timezone(context);
        String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, timezone);
        boolean isSolarTime = WidgetTimezones.LocalMeanTime.TIMEZONEID.equals(timezone.getID()) ||
                WidgetTimezones.ApparentSolarTime.TIMEZONEID.equals(timezone.getID());

        WidgetSettings.TimeFormatMode formatMode = WidgetSettings.loadTimeFormatModePref(context, appWidgetId());
        String timeDisplay = utils.calendarTimeShortDisplayString(context, now(context), false, formatMode).toString() + " " + tzDisplay;
        tile.setLabel(timeDisplay);
        tile.setIcon(Icon.createWithResource(this, isSolarTime ? R.drawable.ic_weather_sunny : R.drawable.ic_action_time));

        updateTileState(context, tile).updateTile();
    }

    @Override
    protected Dialog createDialog(final Context context)
    {
        initLocale(context);
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));

        TimeZone timezone = timezone(context);
        Location location = location(context);
        Calendar now = now(context);

        WidgetSettings.TimeFormatMode formatMode = WidgetSettings.loadTimeFormatModePref(context, appWidgetId());
        String dateDisplay = utils.calendarDateDisplayString(context, now).toString();
        String timeDisplay = utils.calendarTimeShortDisplayString(context, now, false, formatMode).toString();
        String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, timezone);
        boolean isLocalTime = isLocalTime(timezone.getID());

        String sunriseDisplay = null, sunsetDisplay = null;
        SuntimesRiseSetData2 data = initData(context);
        if (data != null)
        {
            Calendar sunrise = data.sunriseCalendarToday();
            sunriseDisplay = utils.calendarTimeShortDisplayString(context, sunrise, false, formatMode).toString();

            Calendar sunset = data.sunsetCalendarToday();
            sunsetDisplay = utils.calendarTimeShortDisplayString(context, sunset, false, formatMode).toString();
        }


        String title = timeDisplay;
        String message = tzDisplay + (isLocalTime ? "\n" + location.getLabel() : "");

        message += "\n\n" + dateDisplay;
        if (sunriseDisplay != null) {
            message += "\n" + sunriseDisplay + " sunrise";   // TODO
        }
        if (sunsetDisplay != null) {
            message += ", " + sunsetDisplay + " sunset" ;    // TODO
        }
        
        int[] attrs = { R.attr.icActionTime, R.attr.icActionDst };
        TypedArray a = contextWrapper.obtainStyledAttributes(attrs);
        int icon = a.getResourceId(isLocalTime ? 1 : 0, R.drawable.ic_action_time);
        a.recycle();

        AlertDialog.Builder dialog = new AlertDialog.Builder(contextWrapper, android.R.style.ThemeOverlay_Material_Dialog);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIcon(ContextCompat.getDrawable(context, icon));

        dialog.setNeutralButton(context.getString(R.string.configAction_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityAndCollapse(getConfigIntent(context));
            }
        });
        dialog.setPositiveButton(getLaunchIntentTitle(context), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityAndCollapse(getLaunchIntent(context));
            }
        });

        return dialog.create();
    }

}
