/**
    Copyright (C) 2017-2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.themes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;

import android.widget.GridView;

import android.widget.ImageView;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.AboutDialog;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesWidget0;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.getfix.ExportPlacesTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;

import java.io.File;

import static com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity.ADD_THEME_REQUEST;
import static com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity.EDIT_THEME_REQUEST;

public class WidgetThemeListActivity extends AppCompatActivity
{
    public static final String DIALOGTAG_ABOUT = "about";

    public static final int WALLPAPER_DELAY = 1000;

    public static final int PICK_THEME_REQUEST = 1;
    public static final String ADAPTER_MODIFIED = "isModified";

    public static final String PARAM_SELECTED = "selected";
    public static final String PARAM_NOSELECT = "noselect";

    private boolean adapterModified = false;
    private GridView gridView;
    private ActionBar actionBar;

    protected ActionMode actionMode = null;
    private WidgetThemeActionCompat themeActions;
    private SuntimesTheme.ThemeDescriptor selected = null;

    private ProgressDialog progress;
    private ExportThemesTask exportTask = null;
    private boolean isExporting = false;

    private int previewID = 0;
    private boolean disallowSelect = false;
    private String preselectedTheme;
    private boolean useWallpaper = false;

    public WidgetThemeListActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
        initLocale();
        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_themelist);

        Intent intent = getIntent();
        previewID = intent.getIntExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, previewID);
        disallowSelect = intent.getBooleanExtra(PARAM_NOSELECT, disallowSelect);
        preselectedTheme = intent.getStringExtra(PARAM_SELECTED);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        useWallpaper = prefs.getBoolean(WidgetThemeConfigActivity.PARAM_WALLPAPER, useWallpaper);
        useWallpaper = intent.getBooleanExtra(WidgetThemeConfigActivity.PARAM_WALLPAPER, useWallpaper);

        WidgetThemes.initThemes(this);
        initData(this);
        initViews(this);

        if (preselectedTheme != null && !preselectedTheme.trim().isEmpty())
        {
            int i = adapter.ordinal(preselectedTheme);
            SuntimesTheme.ThemeDescriptor theme = (SuntimesTheme.ThemeDescriptor) adapter.getItem(i);
            triggerActionMode(null, theme);
            gridView.setSelection(i);
        }
    }

    private SuntimesRiseSetData data;
    private void initData(Context context)
    {
        data = new SuntimesRiseSetData(context, AppWidgetManager.INVALID_APPWIDGET_ID);   // use app configuration
        data.setCompareMode(WidgetSettings.CompareMode.TOMORROW);
        data.setTimeMode(WidgetSettings.TimeMode.OFFICIAL);
        data.calculate();

        SuntimesRiseSetData noonData = new SuntimesRiseSetData(data);
        noonData.setTimeMode(WidgetSettings.TimeMode.NOON);
        noonData.calculate();
        data.linkData(noonData);
    }

    private void initLocale()
    {
        AppSettings.initLocale(this);
        WidgetSettings.initDefaults(this);
        WidgetSettings.initDisplayStrings(this);
    }

    protected void initViews( Context context )
    {
        initActionBar(context);
        themeActions = new WidgetThemeActionCompat(context);

        gridView = (GridView)findViewById(R.id.themegrid);
        initThemeAdapter(context);

        View bottomBanner = findViewById(R.id.themegrid_bottom);
        if (bottomBanner != null)
        {
            bottomBanner.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    toggleWallpaper();
                }
            });
        }
    }

    protected void initActionBar( Context context )
    {
        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private WidgetThemes.ThemeGridAdapter adapter;

    protected void initThemeAdapter(Context context)
    {
        adapter = new WidgetThemes.ThemeGridAdapter(context, WidgetThemes.sortedValues(true));
        adapter.setRiseSet(data.sunriseCalendarToday(), data.sunsetCalendarToday(), data.getLinked().sunriseCalendarToday());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                if (position == 0 && adapter.showingAddButton())
                {
                    addTheme();

                } else {
                    SuntimesTheme.ThemeDescriptor theme = (SuntimesTheme.ThemeDescriptor) adapter.getItem(position);
                    triggerActionMode(v, theme);
                }
            }
        });
    }

    private boolean triggerActionMode(View view, SuntimesTheme.ThemeDescriptor themeDesc)
    {
        if (actionMode == null)
        {
            selected = themeDesc;
            adapter.setSelected(selected);
            if (themeDesc != null)
            {
                themeActions.setTheme(this, themeDesc);
                actionMode = startSupportActionMode(themeActions);
                actionMode.setTitle(themeDesc.displayString());
            }
            return true;

        } else {
            actionMode.finish();
            triggerActionMode(view, themeDesc);
            return false;
        }
    }

    protected void addTheme()
    {
        if (actionMode != null)
        {
            actionMode.finish();
        }

        Intent intent = new Intent(this, WidgetThemeConfigActivity.class);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_MODE, WidgetThemeConfigActivity.UIMode.ADD_THEME);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_WALLPAPER, useWallpaper);
        if (previewID >= 0)
        {
            intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, previewID);
        }
        startActivityForResult(intent, ADD_THEME_REQUEST);
    }

    protected void editTheme( SuntimesTheme theme )
    {
        if (theme.isDefault())
        {
            copyTheme(theme);

        } else {
            Intent intent = new Intent(this, WidgetThemeConfigActivity.class);
            intent.putExtra(WidgetThemeConfigActivity.PARAM_MODE, WidgetThemeConfigActivity.UIMode.EDIT_THEME);
            intent.putExtra(WidgetThemeConfigActivity.PARAM_WALLPAPER, useWallpaper);
            intent.putExtra(SuntimesTheme.THEME_NAME, theme.themeName());
            if (previewID >= 0) {
                intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, previewID);
            }
            startActivityForResult(intent, EDIT_THEME_REQUEST);
        }
    }

    protected void copyTheme( SuntimesTheme theme )
    {
        Intent intent = new Intent(this, WidgetThemeConfigActivity.class);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_MODE, WidgetThemeConfigActivity.UIMode.ADD_THEME);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_WALLPAPER, useWallpaper);
        intent.putExtra(SuntimesTheme.THEME_NAME, theme.themeName());
        if (previewID >= 0) {
            intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, previewID);
        }
        startActivityForResult(intent, ADD_THEME_REQUEST);
    }

    protected void deleteTheme(final SuntimesTheme theme)
    {
        if (!theme.isDefault())
        {
            final Context context = this;
            AlertDialog.Builder confirm = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.deletetheme_dialog_title))
                    .setMessage(getString(R.string.deletetheme_dialog_message, theme.themeDisplayString()))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(getString(R.string.deletetheme_dialog_ok), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            if (WidgetThemes.removeValue(context, theme.themeDescriptor()))
                            {
                                theme.deleteTheme(WidgetThemes.getSharedPreferences(context));
                                adapterModified = true;
                                initThemeAdapter(context);
                                Toast.makeText(context, context.getString(R.string.deletetheme_toast_success, theme.themeName()), Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.deletetheme_dialog_cancel), null);

            confirm.show();
        }
    }

    protected void selectTheme( SuntimesTheme theme )
    {
        Intent intent = new Intent();
        intent.putExtra(SuntimesTheme.THEME_NAME, theme.themeName());
        intent.putExtra(ADAPTER_MODIFIED, adapterModified);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * @param context a context used to access resources
     */
    private boolean exportThemes( Context context )
    {
        if (context != null)
        {
            exportTask = new ExportThemesTask(context, "SuntimesThemes", true, true);    // export to external cache
            exportTask.setDescriptors(WidgetThemes.values());
            exportTask.setTaskListener(exportThemesListener);
            exportTask.execute();
            return true;
        }
        return false;
    }

    private ExportPlacesTask.TaskListener exportThemesListener = new ExportTask.TaskListener()
    {
        public void onStarted()
        {
            isExporting = true;
            showExportProgress();
        }

        @Override
        public void onFinished(ExportPlacesTask.ExportResult results)
        {
            exportTask = null;
            isExporting = false;
            dismissProgress();

            if (results.getResult())
            {
                String successMessage = getString(R.string.msg_export_success, results.getExportFile().getAbsolutePath());
                Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(results.getExportFile()));
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.msg_export_to)));

            } else {
                File file = results.getExportFile();
                String path = ((file != null) ? file.getAbsolutePath() : "<path>");
                String failureMessage = getString(R.string.msg_export_failure, path);
                Toast.makeText(getApplicationContext(), failureMessage, Toast.LENGTH_LONG).show();
            }
        }
    };

    private void showExportProgress()
    {
        progress = ProgressDialog.show(this, getString(R.string.themesexport_dialog_title), getString(R.string.themesexport_dialog_message), true);
    }

    private void dismissProgress()
    {
        if (progress != null && progress.isShowing())
        {
            progress.dismiss();
        }
    }

    /**
     * @param context a context used to access resources
     */
    private void importThemes( Context context )
    {
        // TODO
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.putExtra(ADAPTER_MODIFIED, adapterModified);
        setResult(((adapterModified) ? Activity.RESULT_OK : Activity.RESULT_CANCELED), intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.themelist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            //case R.id.importThemes:
            //    importThemes(this);
            //    return true;

            case R.id.addTheme:
                addTheme();
                return true;

            case R.id.exportThemes:
                exportThemes(this);
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * WidgetThemeActionCompat
     */
    private class WidgetThemeActionCompat implements android.support.v7.view.ActionMode.Callback
    {
        private SuntimesTheme theme = null;

        public WidgetThemeActionCompat(Context context) {}

        public void setTheme(Context context, SuntimesTheme.ThemeDescriptor themeDesc )
        {
            this.theme = WidgetThemes.loadTheme(context, themeDesc.name());
        }

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.themecontext, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            actionMode = null;
            selected = null;
            //noinspection ConstantConditions
            adapter.setSelected(selected);
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            SuntimesUtils.forceActionBarIcons(menu);

            MenuItem selectItem = menu.findItem(R.id.selectTheme);
            selectItem.setVisible( !disallowSelect );

            MenuItem deleteItem = menu.findItem(R.id.deleteTheme);
            deleteItem.setVisible( !theme.isDefault() );  // not allowed to delete default

            MenuItem editItem = menu.findItem(R.id.editTheme);
            editItem.setVisible( !theme.isDefault() );    // not allowed to edit default

            return false;
        }

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item)
        {
            if (theme != null)
            {
                switch (item.getItemId())
                {
                    case R.id.selectTheme:
                        mode.finish();
                        selectTheme(theme);
                        return true;

                    case R.id.editTheme:
                        editTheme(theme);
                        //mode.finish();    // TODO: is it OK to startActivity w/out finishing ActionMode? the transition looks better this way.
                        return true;

                    case R.id.copyTheme:
                        copyTheme(theme);
                        //mode.finish();    // TODO: is it OK to startActivity w/out finishing ActionMode? the transition looks better this way.
                        return true;

                    case R.id.deleteTheme:
                        deleteTheme(theme);
                        mode.finish();
                        return true;
                }
            }
            mode.finish();
            return false;
        }
    }

    /**
     * @param requestCode ADD_THEME_REQUEST or EDIT_THEME_REQUEST
     * @param resultCode request result
     * @param data result data (containing: SuntimesTheme.THEME_NAME)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case ADD_THEME_REQUEST:
                onAddThemeResult(resultCode, data);
                break;

            case EDIT_THEME_REQUEST:
                onEditThemeResult(resultCode, data);
                break;
        }
    }

    protected void onAddThemeResult(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            adapterModified = true;
            initThemeAdapter(this);

            if (data != null)
            {
                String themeName = data.getStringExtra(SuntimesTheme.THEME_NAME);
                int i = adapter.ordinal(themeName);

                SuntimesTheme.ThemeDescriptor theme = (SuntimesTheme.ThemeDescriptor) adapter.getItem(i);
                triggerActionMode(null, theme);
                gridView.setSelection(i);

                Toast.makeText(this, getString(R.string.addtheme_toast_success, themeName), Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onEditThemeResult(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            adapterModified = true;
            initThemeAdapter(this);

            if (data != null)
            {
                String themeName = data.getStringExtra(SuntimesTheme.THEME_NAME);
                int i = adapter.ordinal(themeName);

                SuntimesTheme.ThemeDescriptor theme = (SuntimesTheme.ThemeDescriptor) adapter.getItem(i);
                theme.updateDescriptor(this, WidgetThemes.PREFS_THEMES);
                triggerActionMode(null, theme);
                gridView.setSelection(i);

                Toast.makeText(this, getString(R.string.edittheme_toast_success, themeName), Toast.LENGTH_LONG).show();
                updateWidgetsMatchingTheme(this, themeName);
            }
        }
    }

    public static void updateWidgetsMatchingTheme(Context context, String themeName)
    {
        Intent updateIntent = new Intent();
        updateIntent.setAction(SuntimesWidget0.SUNTIMES_THEME_UPDATE);
        updateIntent.putExtra(SuntimesWidget0.KEY_THEME, themeName);
        context.sendBroadcast(updateIntent);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (isExporting && exportTask != null)
        {
            exportTask.pauseTask();
            exportTask.clearTaskListener();
        }
        dismissProgress();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);

        if (selected != null)
        {
            outState.putString(SuntimesTheme.THEME_NAME, selected.name());
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);

        String themeName = savedState.getString(SuntimesTheme.THEME_NAME);
        if (themeName != null)
        {
            int i = adapter.ordinal(themeName);
            SuntimesTheme.ThemeDescriptor theme = (SuntimesTheme.ThemeDescriptor) adapter.getItem(i);
            triggerActionMode(null, theme);
            gridView.setSelection(i);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (useWallpaper)
        {
            initWallpaper(false);
        }
        if (isExporting && exportTask != null)
        {
            exportTask.setDescriptors(WidgetThemes.values());
            exportTask.setTaskListener(exportThemesListener);
            showExportProgress();
            exportTask.resumeTask();
        }
    }

    /**
     * Set activity background to match home screen wallpaper.
     */
    protected void initWallpaper(boolean animate)
    {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        if (wallpaperManager != null)
        {
            ImageView background = (ImageView)findViewById(R.id.themegrid_background);
            Drawable wallpaper = wallpaperManager.getDrawable();
            if (background != null && wallpaper != null)
            {
                background.setImageDrawable(wallpaper);
                background.setVisibility(View.VISIBLE);

                if (animate && (Build.VERSION.SDK_INT >= 12))
                {
                    background.animate().alpha(1f).setDuration(WALLPAPER_DELAY);

                } else if (Build.VERSION.SDK_INT >= 11) {
                    background.setAlpha(1f);
                }
            }
        }
    }

    protected void hideWallpaper()
    {
        ImageView background = (ImageView)findViewById(R.id.themegrid_background);
        if (background != null)
        {
            if (Build.VERSION.SDK_INT >= 12)
            {
                background.animate().alpha(0f).setDuration(WALLPAPER_DELAY);

            } else if (Build.VERSION.SDK_INT >= 11) {
                background.setAlpha(0f);

            } else {
                background.setVisibility(View.GONE);
            }
        }
    }

    protected void toggleWallpaper()
    {
        useWallpaper = !useWallpaper;

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefs.putBoolean(WidgetThemeConfigActivity.PARAM_WALLPAPER, useWallpaper);
        prefs.apply();

        if (useWallpaper)
            initWallpaper(true);
        else hideWallpaper();
    }

    protected void showAbout()
    {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), DIALOGTAG_ABOUT);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        SuntimesUtils.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }
}
