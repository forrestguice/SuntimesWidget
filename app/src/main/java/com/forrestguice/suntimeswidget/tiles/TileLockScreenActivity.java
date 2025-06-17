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

/**
 * TileLockScreenActivity
 */
public class TileLockScreenActivity extends SuntimesTileActivity
{
    public static final String EXTRA_APPWIDGETID = "appWidgetID";

    public TileLockScreenActivity() {
        super();
    }

    @Override
    protected SuntimesTileBase initTileBase()
    {
        final int appWidgetID = getIntent().getIntExtra(EXTRA_APPWIDGETID, 0);
        switch (appWidgetID)
        {
            case AlarmTileService.ALARMTILE_APPWIDGET_ID: return new AlarmTileBase(this);
            case BedtimeTileService.BEDTIMETILE_APPWIDGET_ID: return new BedtimeTileBase(this);
            case ClockTileService.CLOCKTILE_APPWIDGET_ID: return new ClockTileBase(this);
            case NextEventTileService.NEXTEVENTTILE_APPWIDGET_ID: return new NextEventTileBase(this);
            default: return null;
        }
    }
}
