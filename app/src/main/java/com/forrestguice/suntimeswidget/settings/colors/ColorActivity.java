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
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.AppSettings;

/**
 * This activity can be used to pick a color:
 * ```
 * public void showColorPicker()
 * {
 *     ArrayList<Integer> recentColors = new ArrayList<>();
 *     recentColors.add(Color.RED);
 *     int color = Color.GREEN;
 *
 *     Intent intent = new Intent(Intent.ACTION_PICK);
 *     intent.setData(Uri.parse("color://" + String.format("#%08X", color)));        // selected color as uri fragment; color://#hexColor
 *
 *     //intent.putExtra("color", color);                                            // selected color as an int (another way to do same as above)
 *     intent.putExtra("showAlpha", false);                                          // show alpha slider
 *     intent.putExtra("recentColors", recentColors);                                // show "recent" palette of colors
 *
 *     startActivityForResult(intent, REQUEST_CODE);
 * }
 * ```
 * ```
 * public void onActivityResult(int requestCode, int resultCode, Intent data)
 * {
 *     if (resultCode == RESULT_OK && resultCode == REQUEST_CODE)
 *     {
 *         int color;
 *         Uri uri = data.getData();
 *         if (uri != null)
 *         {
 *             try {
 *                 color = Color.parseColor("#" + uri.getFragment());
 *                 onColorPicked(color);    // do something with returned value
 *
 *             } catch (IllegalArgumentException e) {
 *                 Log.e("onActivityResult", "bad color uri; " + e);
 *             }
 *         }
 *     }
 * }
 * ```
 */
public class ColorActivity extends AppCompatActivity
{
    public static final String SCHEME_COLOR = "color";

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
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_colors);

        ColorDialog colorDialog = (ColorDialog) getSupportFragmentManager().findFragmentByTag(ColorChooser.DIALOGTAG_COLOR);
        if (colorDialog == null)
        {
            colorDialog = createColorDialog(getIntent());
            colorDialog.show(getSupportFragmentManager(), ColorChooser.DIALOGTAG_COLOR);
        }
    }

    protected void setBackgroundColor(int color)
    {
        View background = findViewById(R.id.layout_background);
        if (background != null) {
            background.setBackgroundColor(color);
        }
    }

    protected ColorDialog createColorDialog(Intent intent)
    {
        ColorDialog colorDialog = new ColorDialog();
        colorDialog.setRecentColors(intent.getIntegerArrayListExtra(ColorDialog.KEY_RECENT));
        colorDialog.setShowAlpha(intent.getBooleanExtra(ColorDialog.KEY_SHOWALPHA, false));

        int color = Color.WHITE;
        Uri data = intent.getData();
        if (data != null && SCHEME_COLOR.equals(data.getScheme()))
        {
            try {
                color = Color.parseColor("#" + data.getFragment());

            } catch (IllegalArgumentException e) {
                color = Color.WHITE;
                Log.e("ColorActivity", e.toString());
            }

        } else if (intent.hasExtra(ColorDialog.KEY_COLOR)) {
            color = intent.getIntExtra(ColorDialog.KEY_COLOR, Color.WHITE);
        }

        colorDialog.setColor(color);
        colorDialog.setColorDialogListener(dialogListener);
        return colorDialog;
    }

    private ColorDialog.ColorDialogListener dialogListener = new ColorDialog.ColorDialogListener()
    {
        @Override
        public void onAccepted(int color) {
            selectColor(color);
        }

        @Override
        public void onColorChanged(int color) {
            setBackgroundColor(color);
        }

        @Override
        public void onCanceled() {
            onBackPressed();
        }
    };

    protected void selectColor( int color )
    {
        Intent intent = new Intent();
        intent.setData(Uri.parse(SCHEME_COLOR + "://" + String.format("#%08X", color)));
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
        if (colorDialog != null)
        {
            colorDialog.setColorDialogListener(dialogListener);
            setBackgroundColor(colorDialog.getColor());
        }
    }
}