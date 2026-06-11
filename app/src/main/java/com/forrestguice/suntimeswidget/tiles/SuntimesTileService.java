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

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData2;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.settings.AppSettings;

/**
 * SuntimesTileService
 * @see SuntimesTileBase
 */
@TargetApi(24)
public abstract class SuntimesTileService extends TileService
{
    protected abstract int appWidgetId();
    protected abstract SuntimesTileBase initTileBase();
    protected SuntimesTileBase base = initTileBase();

    public static final String TAG = "AlarmTile";
    protected static final TimeDateDisplay utils = new TimeDateDisplay();

    protected void initLocale(Context context) {
        SuntimesUtils.initDisplayStrings(context);
    }

    protected void updateTile(Context context) {
        updateTileState(context, getQsTile()).updateTile();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        base.initDefaults(getApplicationContext());
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
        base.initData(getApplicationContext(), true);
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
        SuntimesRiseSetData2 data = base.initData(context);
        tile.setState((data.isCalculated())
                ? data.isDay(base.now(context))
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

        Log.i(TAG, "onClick");
        if (isLocked()) {
            if (onClick_locked()) {
                return;
            }
        }
        onClick_unlocked();
    }

    /**
     * @return true click was handled (return), false click unhandled (fall through)
     */
    protected boolean onClick_locked()
    {
        Intent lockScreenIntent = base.getLockScreenIntent(getApplicationContext());
        if (lockScreenIntent != null)
        {
            lockScreenIntent.putExtra(TileLockScreenActivity.EXTRA_APPWIDGETID, appWidgetId());
            lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(lockScreenIntent);
            return true;
        }
        return false;
    }
    protected void onClick_unlocked()
    {
        ContextThemeWrapper context = new ContextThemeWrapper(getApplicationContext(), AppSettings.loadTheme(getApplicationContext()));
        Dialog dialog = base.createDialog(context);
        if (dialog != null) {
            showDialog(dialog);

        } else {
            Intent launchIntent = base.getLaunchIntent(context);
            Intent configIntent = base.getConfigIntent(context);
            if (launchIntent != null) {
                startActivityAndCollapse(launchIntent);
            } else if (configIntent != null) {
                startActivityAndCollapse(configIntent);
            }
        }
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

}
