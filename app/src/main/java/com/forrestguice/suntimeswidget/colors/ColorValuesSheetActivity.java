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

package com.forrestguice.suntimeswidget.colors;

import android.app.Activity;
import com.forrestguice.support.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.design.app.AppCompatActivity;
import com.forrestguice.support.design.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.colors.ColorDialog;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;

public class ColorValuesSheetActivity extends AppCompatActivity
{
    public static final String EXTRA_APPWIDGET_ID = "appWidgetID";
    public static final String EXTRA_COLORTAG = "colorTag";
    public static final String EXTRA_COLLECTION = "colorCollection";
    public static final String EXTRA_SELECTED_COLORS_ID = "colorID";

    public static final String EXTRA_TITLE = "activityTitle";
    public static final String EXTRA_SUBTITLE = "activitySubtitle";
    public static final String EXTRA_PREVIEW_KEYS = "previewKeys";
    public static final String EXTRA_PREVIEW_MODE = "previewMode";
    public static final String EXTRA_PREVIEW_INTENTBUILDER = "previewIntentBuilder";

    public static final String EXTRA_SHOW_ALPHA = ColorDialog.KEY_SHOWALPHA;

    public static final String DIALOG_SHEET = "ColorSheet";
    protected ColorValuesSheetFragment colorSheet;

    public ColorValuesSheetActivity() {
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
        setContentView(R.layout.layout_activity_colorsheet);

        Intent intent = getIntent();

        ColorValuesEditFragment.ColorValuesEditViewModel editViewModel = ViewModelProviders.of(this).get(ColorValuesEditFragment.ColorValuesEditViewModel .class);
        editViewModel.setShowAlpha(intent.getBooleanExtra(EXTRA_SHOW_ALPHA, false));
        editViewModel.setPreviewMode(intent.getIntExtra(EXTRA_PREVIEW_MODE, ColorValuesEditFragment.ColorValuesEditViewModel.PREVIEW_TEXT));

        if (intent.hasExtra(EXTRA_PREVIEW_INTENTBUILDER)) {
            previewIntentBuilder = intent.getParcelableExtra(EXTRA_PREVIEW_INTENTBUILDER);
        }

        colorSheet = (ColorValuesSheetFragment) getSupportFragmentManager().findFragmentByTag(DIALOG_SHEET);
        if (colorSheet == null)
        {
            colorSheet = new ColorValuesSheetFragment();
            colorSheet.setAppWidgetID(intent.getIntExtra(EXTRA_APPWIDGET_ID, 0));
            colorSheet.setColorTag(intent.getStringExtra(EXTRA_COLORTAG));
            colorSheet.setColorCollection((ColorValuesCollection<ColorValues>) intent.getParcelableExtra(EXTRA_COLLECTION));
            colorSheet.setPreviewKeys(intent.getStringArrayExtra(EXTRA_PREVIEW_KEYS));
            colorSheet.setMode(ColorValuesSheetFragment.MODE_SELECT);
            colorSheet.setShowBack(false);
            colorSheet.setShowMenu(false);
            colorSheet.setHideAfterSave(false);
            colorSheet.setPersistSelection(false);

            colorSheet.setFragmentListener(sheetListener);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, colorSheet, DIALOG_SHEET)
                .commit();
        getSupportFragmentManager().executePendingTransactions();

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            CharSequence title = intent.getCharSequenceExtra(EXTRA_TITLE);
            if (title != null) {
                getSupportActionBar().setTitle(title);
            }
            getSupportActionBar().setSubtitle(intent.getCharSequenceExtra(EXTRA_SUBTITLE));
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (colorSheet != null) {
            colorSheet.updateViews();
        }
    }

    private final ColorValuesSheetFragment.FragmentListener sheetListener = new ColorValuesSheetFragment.FragmentListener()
    {
        @Override
        public void requestPeekHeight(int height) {
            /* EMPTY */
        }

        @Override
        public void requestHideSheet() {
            onBackPressed();
        }

        @Override
        public void requestExpandSheet() {
            /* EMPTY */
        }

        @Override
        public void onColorValuesSelected(@Nullable ColorValues values) {
            /* EMPTY */
        }

        @Override
        public void onModeChanged(int mode) {
            invalidateOptionsMenu();
        }

        @Nullable
        @Override
        public ColorValues getDefaultValues() {
            return ((colorSheet.colorCollection != null) ? colorSheet.colorCollection.getDefaultColors(ColorValuesSheetActivity.this) : null);
        }
    };

    protected void selectColorID()
    {
        if (colorSheet.getMode() == ColorValuesSheetFragment.MODE_EDIT)
        {
            if (!colorSheet.editDialog.onSaveColorValues()) {
                return;
            }
            ColorValues values = colorSheet.editDialog.getColorValues();
            selectColorID((values != null) ? values.getID() : null);

        } else {
            selectColorID(colorSheet.listDialog.getSelectedID());
        }
    }

    protected void selectColorID( String colorID )
    {
        Intent intent = createReturnIntent();
        intent.putExtra(EXTRA_SELECTED_COLORS_ID, colorID);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Nullable
    protected String getSelectedColorID()
    {
        if (colorSheet.getMode() == ColorValuesSheetFragment.MODE_EDIT)
        {
            if (!colorSheet.editDialog.onSaveColorValues()) {
                return null;
            }
            ColorValues values = colorSheet.editDialog.getColorValues();
            return (values != null) ? values.getID() : null;

        } else {
            return colorSheet.listDialog.getSelectedID();
        }
    }

    public interface PreviewColorsIntentBuilder extends Parcelable {
        Intent getIntent(Context context, String colorsID);
    }
    public void setPreviewIntentBuilder(PreviewColorsIntentBuilder value) {
        previewIntentBuilder = value;
    }
    protected PreviewColorsIntentBuilder previewIntentBuilder;

    protected void previewColors()
    {
        if (previewIntentBuilder != null)
        {
            Intent intent = previewIntentBuilder.getIntent(this, getSelectedColorID());
            if (intent != null) {
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        setResult(Activity.RESULT_CANCELED, createReturnIntent());
        finish();
    }

    protected Intent createReturnIntent()
    {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_APPWIDGET_ID, colorSheet.getAppWidgetID());
        intent.putExtra(EXTRA_COLORTAG, colorSheet.getColorTag());
        intent.putExtra(EXTRA_COLLECTION, colorSheet.colorCollection);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_colorsheet, menu);

        MenuItem deleteItem = menu.findItem(R.id.action_colors_delete);
        if (deleteItem != null) {
            deleteItem.setEnabled(!colorSheet.getColorCollection().isDefaultColorID(colorSheet.getSelectedID()));
        }

        MenuItem previewItem = menu.findItem(R.id.action_colors_preview);
        if (previewItem != null) {
            previewItem.setVisible(previewIntentBuilder != null);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_colors_preview:
                previewColors();
                return true;

            case R.id.action_colors_select:
                selectColorID();
                return true;

            case R.id.action_colors_add:
                colorSheet.listDialog.onAddItem();
                return true;

            case R.id.action_colors_delete:
                colorSheet.listDialog.onDeleteItem();
                return true;

            case R.id.action_colors_share:
                colorSheet.listDialog.onShareColors();
                return true;

            case R.id.action_colors_import:
                colorSheet.listDialog.onImportColors();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        PopupMenuCompat.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

}