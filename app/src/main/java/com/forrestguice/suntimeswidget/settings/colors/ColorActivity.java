/**
    Copyright (C) 2020 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings.colors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.AppSettings;

public class ColorActivity extends AppCompatActivity
{
    public ColorActivity() {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppSettings.initLocale(newBase));
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_colors);

        Intent intent = getIntent();
        ColorDialog colorDialog = new ColorDialog();
        colorDialog.setRecentColors(intent.getIntegerArrayListExtra(ColorDialog.KEY_RECENT));
        colorDialog.setShowAlpha(intent.getBooleanExtra(ColorDialog.KEY_SHOWALPHA, false));

        if (intent.hasExtra(ColorDialog.KEY_COLOR)) {
            colorDialog.setColor(intent.getIntExtra(ColorDialog.KEY_COLOR, Color.WHITE));
        } else colorDialog.setColor(Color.WHITE);

        colorDialog.setColorDialogListener(dialogListener);
        colorDialog.show(getSupportFragmentManager(), ColorChooser.DIALOGTAG_COLOR);
    }

    private ColorDialog.ColorDialogListener dialogListener = new ColorDialog.ColorDialogListener()
    {
        @Override
        public void onAccepted(int color) {
            selectColor(color);
        }

        @Override
        public void onCanceled() {
            onBackPressed();
        }
    };

    protected void selectColor( int color )
    {
        Intent intent = new Intent();
        intent.putExtra(ColorDialog.KEY_COLOR, color);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ColorDialog colorDialog = (ColorDialog) getSupportFragmentManager().findFragmentByTag(ColorChooser.DIALOGTAG_COLOR);
        if (colorDialog != null) {
            colorDialog.setColorDialogListener(dialogListener);
        }
    }
}