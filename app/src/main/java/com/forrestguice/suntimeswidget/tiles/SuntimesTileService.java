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
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.forrestguice.suntimeswidget.ClockWidget0ConfigActivity;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData2;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetTimezones;

import java.util.Calendar;
import java.util.TimeZone;

@TargetApi(24)
public abstract class SuntimesTileService extends TileService
{
    public static final String TAG = "AlarmTile";
    protected static final SuntimesUtils utils = new SuntimesUtils();

    protected int appWidgetId() {
        return 0;
    }

    @Nullable
    @SuppressWarnings("rawtypes")
    protected Class getConfigActivityClass(Context context) {
        return null;
    }

    @Nullable
    protected Dialog createDialog(final Context context) {
        return null;
    }

    protected void initDefaults(Context context) {
    }

    protected void initLocale(Context context) {
        SuntimesUtils.initDisplayStrings(context);
    }

    protected void updateTile(Context context) {
        updateTileState(context, getQsTile()).updateTile();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        initDefaults(getApplicationContext());
        Log.i(TAG, "onTileAdded");
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.i(TAG, "onTileRemoved");
    }

    @Override
    public void onStartListening()
    {
        super.onStartListening();
        initData(getApplicationContext(), true);
        initLocale(getApplicationContext());
        updateTile(getApplicationContext());
        //Log.i(TAG, "onStartListening");
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        //Log.i(TAG, "onStopListening");
    }


    protected Tile updateTileState(Context context, Tile tile)
    {
        SuntimesRiseSetData2 data = initData(context);
        tile.setState((data.isCalculated())
                ? data.isDay(now(context))
                ? Tile.STATE_ACTIVE
                : Tile.STATE_INACTIVE
                : Tile.STATE_UNAVAILABLE);
        return tile;
    }

    @Override
    public void onClick()
    {
        super.onClick();
        Tile tile = getQsTile();
        toggleState(tile).updateTile();

        Dialog dialog = createDialog(getApplicationContext());
        if (dialog != null) {
            showDialog(createDialog(getApplicationContext()));

        } else {
            Intent launchIntent = getLaunchIntent(getApplicationContext());
            Intent configIntent = getConfigIntent(getApplicationContext());
            if (launchIntent != null) {
                startActivityAndCollapse(launchIntent);
            } else if (configIntent != null) {
                startActivityAndCollapse(configIntent);
            }
        }
        //Log.i(TAG, "onClick");
    }

    protected Tile toggleState(Tile tile)
    {
        switch (tile.getState()) {
            case Tile.STATE_UNAVAILABLE: break;
            case Tile.STATE_ACTIVE: tile.setState(Tile.STATE_INACTIVE); break;
            default: tile.setState(Tile.STATE_ACTIVE); break;
        }
        return tile;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    protected Intent getConfigIntent(Context context)
    {
        Class configClass = getConfigActivityClass(context);
        if (configClass != null)
        {
            Intent intent = new Intent(context, getConfigActivityClass(context));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId());
            intent.putExtra(ClockWidget0ConfigActivity.EXTRA_RECONFIGURE, true);
            return intent;
        } else return null;
    }

    @NonNull
    protected String getLaunchIntentTitle(Context context) {
        String title = WidgetActions.loadActionLaunchPref(context, appWidgetId(), null, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
        return (title != null ? title : context.getString(R.string.app_name));
    }

    @Nullable
    protected Intent getLaunchIntent(Context context)
    {
        Intent intent = WidgetActions.createIntent(context.getApplicationContext(), appWidgetId(), null, initData(context), SuntimesActivity.class);
        if (intent != null) {
            return intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else return AlarmNotifications.getSuntimesIntent(getApplicationContext());
    }

    protected SuntimesRiseSetData2 initData(Context context) {
        return initData(context, false);
    }
    protected SuntimesRiseSetData2 initData(Context context, boolean replace)
    {
        if (data == null || replace) {
            data = new SuntimesRiseSetData2(context, appWidgetId());
            data.calculate();
        }
        return data;
    }
    protected SuntimesRiseSetData2 data = null;

    protected Location location(Context context) {
        return WidgetSettings.loadLocationPref(context, appWidgetId());
    }

    protected TimeZone timezone(Context context)
    {
        initData(context);
        return (data != null ? data.timezone() : WidgetTimezones.localMeanTime(context, location(context)));
    }

    public static boolean isLocalTime(String tzID) {
        return WidgetTimezones.LocalMeanTime.TIMEZONEID.equals(tzID) || WidgetTimezones.ApparentSolarTime.TIMEZONEID.equals(tzID)
                || WidgetTimezones.SiderealTime.TZID_LMST.equalsIgnoreCase(tzID);
    }

    protected Calendar now(Context context) {
        return Calendar.getInstance(timezone(context));
    }

}
