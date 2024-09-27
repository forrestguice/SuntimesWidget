/**
    Copyright (C) 2017-2022 Forrest Guice
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;

import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;

import android.widget.GridView;

import android.widget.ImageView;

import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.getfix.ExportPlacesTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.widgets.WidgetListAdapter;

import java.io.File;

import static com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity.ADD_THEME_REQUEST;
import static com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity.EDIT_THEME_REQUEST;

public class WidgetThemeListActivity extends AppCompatActivity
{
    private static final String DIALOGTAG_HELP = "help";
    private static final int HELP_PATH_ID = R.string.help_themelist_path;

    public static final int WALLPAPER_DELAY = 1000;

    public static final int PICK_THEME_REQUEST = 1;
    public static final int IMPORT_REQUEST = 100;
    public static final int EXPORT_REQUEST = 200;

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
    private static ExportThemesTask exportTask = null;
    private static ImportThemesTask importTask = null;
    private static boolean isExporting = false, isImporting = false;

    private int previewID = 0;
    private boolean disallowSelect = false;
    private String preselectedTheme;
    private boolean useWallpaper = false;

    public WidgetThemeListActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        super.onCreate(icicle);
        initLocale();
        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_themelist);

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
        data.calculate(context);

        SuntimesRiseSetData noonData = new SuntimesRiseSetData(data);
        noonData.setTimeMode(WidgetSettings.TimeMode.NOON);
        noonData.calculate(context);
        data.linkData(noonData);
    }

    private void initLocale()
    {
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
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
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
            intent.putExtra(SuntimesThemeContract.THEME_NAME, theme.themeName());
            if (previewID >= 0) {
                intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, previewID);
            }
            startActivityForResult(intent, EDIT_THEME_REQUEST);
            overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
        }
    }

    protected void copyTheme( SuntimesTheme theme )
    {
        Intent intent = new Intent(this, WidgetThemeConfigActivity.class);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_MODE, WidgetThemeConfigActivity.UIMode.ADD_THEME);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_WALLPAPER, useWallpaper);
        intent.putExtra(SuntimesThemeContract.THEME_NAME, theme.themeName());
        if (previewID >= 0) {
            intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, previewID);
        }
        startActivityForResult(intent, ADD_THEME_REQUEST);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
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
        intent.putExtra(SuntimesThemeContract.THEME_NAME, theme.themeName());
        intent.putExtra(ADAPTER_MODIFIED, adapterModified);
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.transition_ok_in, R.anim.transition_ok_out);
    }

    /**
     * @param context a context used to access resources
     */
    protected boolean exportThemes(Context context, SuntimesTheme.ThemeDescriptor... themes)         // TODO: use SAF for single-theme export
    {
        if (context != null)
        {
            if (isImporting || isExporting) {
                Log.e("exportThemes", "Already busy importing/exporting! ignoring request");
                return false;

            } else {
                exportTask = new ExportThemesTask(context, "SuntimesThemes", true, true);    // export to external cache
                exportTask.setDescriptors(themes);
                exportTask.setTaskListener(exportThemesListener);
                exportTask.execute();
                return true;
            }
        }
        return false;
    }
    protected boolean exportThemes(Context context)
    {
        if (isImporting || isExporting) {
            Log.e("exportThemes", "Already busy importing/exporting! ignoring request");
            return false;
        }

        if (context != null)
        {
            String exportTarget = "SuntimesThemes";
            if (Build.VERSION.SDK_INT >= 19)
            {
                String filename = exportTarget + ExportThemesTask.FILEEXT;
                Intent intent = ExportTask.getCreateFileIntent(filename, ExportThemesTask.MIMETYPE);
                try {
                    startActivityForResult(intent, EXPORT_REQUEST);
                    return true;

                } catch (ActivityNotFoundException e) {
                    Log.e("exportThemes", "SAF is unavailable? (" + e + ").. falling back to legacy export method.");
                }
            }
            exportTask = new ExportThemesTask(context, exportTarget, true, true);    // export to external cache
            exportTask.setDescriptors(WidgetThemes.values());
            exportTask.setTaskListener(exportThemesListener);
            exportTask.execute();
            return true;
        } else return false;
    }
    protected void exportThemes(Context context, @NonNull Uri uri)
    {
        if (isImporting || isExporting) {
            Log.e("exportThemes", "Busy! Already importing/exporting.. ignoring request");
            return;
        }

        Log.i("exportThemes", "Starting export with uri: " + uri);
        exportTask = new ExportThemesTask(context, uri);
        exportTask.setDescriptors(WidgetThemes.values());
        exportTask.setTaskListener(exportThemesListener);
        exportTask.execute();
    }

    /**
     */
    private boolean importThemes( Context context )
    {
        if (context != null)
        {
            if (isImporting || isExporting) {
                Log.e("importThemes","Busy! Already importing/exporting.. ignoring request");
                return false;

            } else {
                Intent intent = ExportTask.getOpenFileIntent(ExportThemesTask.MIMETYPE);
                startActivityForResult(intent, IMPORT_REQUEST);
                return true;
            }
        }
        return false;
    }
    private boolean importThemes(Context context, @NonNull Uri uri)
    {
        if (isImporting || isExporting) {
            Log.e("importThemes","Busy! Already importing/exporting.. ignoring request");
            return false;

        } else {
            Log.i("importThemes", "Starting import task from uri: " + uri);
            importTask = new ImportThemesTask(context);
            importTask.setTaskListener(importThemesListener);
            importTask.execute(uri);
            return true;
        }
    }

    private ImportThemesTask.TaskListener importThemesListener = new ImportThemesTask.TaskListener()
    {
        public void onStarted()
        {
            isImporting = true;
            showImportProgress();
        }

        @Override
        public void onFinished(ImportThemesTask.ImportThemesResult results)
        {
            importTask = null;
            isImporting = false;
            dismissProgress();

            if (results.getResult())
            {
                int importCount = 0;
                SuntimesTheme[] themes = results.getThemes();
                for (SuntimesTheme theme : themes)
                {
                    if (theme != null)
                    {
                        if (!WidgetThemes.hasValue(theme.themeDescriptor()))
                        {
                            theme.saveTheme(WidgetThemes.getSharedPreferences(WidgetThemeListActivity.this));
                            WidgetThemes.addValue(WidgetThemeListActivity.this, theme.themeDescriptor());
                            adapterModified = true;
                            importCount++;
                            Log.w("importThemes", "Added " + theme.themeName);

                        } else {
                            Log.w("importThemes", "Skipping " + theme.themeName + " :: already installed");
                            // TODO: allow overwrite?
                        }
                    }
                }

                if (importCount > 0)
                {
                    String countString = getResources().getQuantityString(R.plurals.themePlural, importCount, importCount);
                    String successMessage = getString(R.string.msg_import_success, countString);
                    Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();

                    if (adapterModified) {
                        initThemeAdapter(WidgetThemeListActivity.this);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_import_noresults), Toast.LENGTH_LONG).show();
                }

            } else {
                String failureMessage = getString(R.string.msg_import_failure, results.getUri());
                Exception error = results.getException();
                if (error != null) {
                    failureMessage += "\n\n" + error.getLocalizedMessage();
                }
                Toast.makeText(getApplicationContext(), failureMessage, Toast.LENGTH_LONG).show();
                //Snackbar errorMsg = Snackbar.make(findViewById(android.R.id.content), failureMessage, Snackbar.LENGTH_LONG);
                //errorMsg.show();
            }
        }
    };

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

            File file = results.getExportFile();
            String path = ((file != null) ? file.getAbsolutePath() : ExportTask.getFileName(getContentResolver(), results.getExportUri()));

            if (results.getResult())
            {
                String successMessage = getString(R.string.msg_export_success, path);
                Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_LONG).show();
                // TODO: use a snackbar instead; offer 'copy path' action

                if (Build.VERSION.SDK_INT >= 19) {
                    if (results.getExportUri() == null) {
                        ExportTask.shareResult(WidgetThemeListActivity.this, results.getExportFile(), results.getMimeType());
                    }
                } else {
                    ExportTask.shareResult(WidgetThemeListActivity.this, results.getExportFile(), results.getMimeType());
                }
                return;
            }

            String failureMessage = getString(R.string.msg_export_failure, path);
            Toast.makeText(getApplicationContext(), failureMessage, Toast.LENGTH_LONG).show();
        }
    };

    private void showExportProgress()
    {
        if (progress == null || !progress.isShowing()) {
            progress = ProgressDialog.show(this, getString(R.string.themesexport_dialog_title), getString(R.string.themesexport_dialog_message), true);
        } else Log.w("showExportProgress", "progress is already showing! ignoring..");
    }

    private void showImportProgress()
    {
        if (progress == null || !progress.isShowing()) {
            progress = ProgressDialog.show(this, getString(R.string.themesimport_dialog_title), getString(R.string.themesimport_dialog_message), true);
        } else Log.w("showImportProgress", "progress is already showing! ignoring..");
    }

    private void dismissProgress()
    {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        } else Log.w("dismissProgress", "progress isn't showing! ignoring..");
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent();
        intent.putExtra(ADAPTER_MODIFIED, adapterModified);
        setResult(((adapterModified) ? Activity.RESULT_OK : Activity.RESULT_CANCELED), intent);
        finish();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
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
            case R.id.importThemes:
                importThemes(this);
                return true;

            case R.id.addTheme:
                addTheme();
                return true;

            case R.id.exportThemes:
                exportThemes(this);
                return true;

            case R.id.action_help:
                showHelp();
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

            Intent intent = getIntent();
            if (intent != null) {
                preselectedTheme = null;
                intent.putExtra(PARAM_SELECTED, (String)null);
            }
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            PopupMenuCompat.forceActionBarIcons(menu);

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

                    case R.id.exportTheme:
                        exportThemes(WidgetThemeListActivity.this, theme.themeDescriptor() );
                        return true;  // TODO: messages
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

            case EXPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        exportThemes(this, uri);
                    }
                }
                break;

            case IMPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        importThemes(this, uri);
                    }
                }
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
                String themeName = data.getStringExtra(SuntimesThemeContract.THEME_NAME);
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
                String themeName = data.getStringExtra(SuntimesThemeContract.THEME_NAME);
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

    public static void updateWidgetsMatchingTheme(Context context, String themeName) {
        WidgetListAdapter.updateWidgetsMatchingTheme(context, WidgetListAdapter.createWidgetListAdapter(context), themeName);
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

        if (isImporting && importTask != null)
        {
            importTask.pauseTask();
            importTask.clearTaskListener();
        }
        dismissProgress();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isExporting", isExporting);
        outState.putBoolean("isImporting", isImporting);

        if (selected != null)
        {
            outState.putString(SuntimesThemeContract.THEME_NAME, selected.name());
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        savedState.getBoolean("isExporting", isExporting);
        savedState.getBoolean("isImporting", isImporting);

        String themeName = savedState.getString(SuntimesThemeContract.THEME_NAME);
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
        if (isImporting && importTask != null)
        {
            importTask.setTaskListener(importThemesListener);
            showImportProgress();
            importTask.resumeTask();
        }

        FragmentManager fragments = getSupportFragmentManager();
        HelpDialog helpDialog = (HelpDialog) fragments.findFragmentByTag(DIALOGTAG_HELP);
        if (helpDialog != null) {
            helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(this, HELP_PATH_ID), DIALOGTAG_HELP);
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

                if (Build.VERSION.SDK_INT >= 12)
                {
                    if (animate) {
                        background.animate().alpha(1f).setDuration(WALLPAPER_DELAY);
                    } else background.setAlpha(1f);

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

    @SuppressLint("ResourceType")
    protected void showHelp()
    {
        int iconSize = (int) getResources().getDimension(R.dimen.helpIcon_size);
        int[] iconAttrs = { R.attr.icActionNew, R.attr.icActionCopy, R.attr.icActionEdit, R.attr.icActionDelete, R.attr.icActionSettings };
        TypedArray typedArray = obtainStyledAttributes(iconAttrs);
        ImageSpan addIcon = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(0, R.drawable.ic_action_new), iconSize, iconSize, 0);
        ImageSpan copyIcon = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(1, R.drawable.ic_action_copy), iconSize, iconSize, 0);
        ImageSpan editIcon = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(2, R.drawable.ic_action_edit), iconSize, iconSize, 0);
        ImageSpan deleteIcon = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(3, R.drawable.ic_action_discard), iconSize, iconSize, 0);
        ImageSpan defaultIcon = SuntimesUtils.createImageSpan(this, typedArray.getResourceId(4, R.drawable.ic_action_settings), iconSize, iconSize, 0);
        typedArray.recycle();

        SuntimesUtils.ImageSpanTag[] helpTags = {
                new SuntimesUtils.ImageSpanTag("[Icon Add]", addIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Copy]", copyIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Edit]", editIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Delete]", deleteIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Default]", defaultIcon)
        };
        String helpString = getString(R.string.help_themelist);
        SpannableStringBuilder helpSpan = SuntimesUtils.createSpan(this, helpString, helpTags);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpSpan);
        helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
        helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(this, HELP_PATH_ID), DIALOGTAG_HELP);
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    protected void showAbout()
    {
        Intent about = new Intent(this, AboutActivity.class);
        startActivity(about);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        PopupMenuCompat.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }
}
