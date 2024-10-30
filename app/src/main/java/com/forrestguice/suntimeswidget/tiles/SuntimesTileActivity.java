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

import android.app.Dialog;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

/**
 * SuntimesTileActivity; displays a "tile dialog" over the lock screen.
 * @see SuntimesTileBase
 */
public abstract class SuntimesTileActivity extends AppCompatActivity
{
    @Nullable
    protected abstract SuntimesTileBase initTileBase();
    protected SuntimesTileBase tileBase;

    public SuntimesTileActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setShowWhenLocked();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        tileBase = initTileBase();
        if (tileBase == null)
        {
            Log.e("SuntimesTileActivity", "null base! finishing...");
            finish();

        } else {
            Dialog dialog = tileBase.createDialog(this);
            if (dialog != null) {
                dialog.show();
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    protected void setShowWhenLocked()
    {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }

}
