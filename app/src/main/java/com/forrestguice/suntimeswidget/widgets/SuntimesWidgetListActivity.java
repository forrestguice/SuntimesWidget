/**
    Copyright (C) 2014-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ListView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.ExportTask;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.actions.ActionListActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SuntimesBackupLoadTask;
import com.forrestguice.suntimeswidget.settings.SuntimesBackupRestoreTask;
import com.forrestguice.suntimeswidget.settings.SuntimesBackupTask;
import com.forrestguice.suntimeswidget.settings.WidgetSettingsExportTask;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.widget.Toolbar;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.widgets.SuntimesConfigActivity0.EXTRA_RECONFIGURE;

public class SuntimesWidgetListActivity extends AppCompatActivity
{
    private static final String DIALOGTAG_HELP = "help";
    private static final int HELP_PATH_ID = R.string.help_widgetlist_path;

    private static final String KEY_LISTVIEW_TOP = "widgetlisttop";
    private static final String KEY_LISTVIEW_INDEX = "widgetlistindex";

    public static final int IMPORT_REQUEST = 100;
    public static final int EXPORT_REQUEST = 200;

    private ListView widgetList;
    private WidgetListAdapter widgetListAdapter;
    protected View progressView;
    private static final SuntimesUtils utils = new SuntimesUtils();

    public SuntimesWidgetListActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    /**
     * OnCreate: the Activity initially created
     * @param icicle a Bundle containing saved state
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        AppSettings.setTheme(this, AppSettings.loadThemePref(this));
        super.onCreate(icicle);
        SuntimesUtils.initDisplayStrings(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_activity_widgetlist);
        initViews(this);
    }

    /**
     * OnStart: the Activity becomes visible
     */
    @Override
    public void onStart()
    {
        super.onStart();
        updateViews(this);
        updateWidgetAlarms(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case EXPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        SuntimesBackupTask.exportSettings(SuntimesWidgetListActivity.this, uri, exportSettingsListener);
                    }
                }
                break;

            case IMPORT_REQUEST:
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = (data != null ? data.getData() : null);
                    if (uri != null) {
                        importSettings(SuntimesWidgetListActivity.this, uri);
                    }
                }
                break;
        }
    }

    /**
     * OnResume: the user is now interacting w/ the Activity (running state)
     */
    @Override
    public void onResume()
    {
        super.onResume();

        HelpDialog helpDialog = (HelpDialog) getSupportFragmentManager().findFragmentByTag(DIALOGTAG_HELP);
        if (helpDialog != null) {
            helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(SuntimesWidgetListActivity.this, HELP_PATH_ID), DIALOGTAG_HELP);
        }
    }

    /**
     * OnPause: the user about to interact w/ another Activity
     */
    @Override
    public void onPause()
    {
        super.onPause();
    }

    /**
     * OnStop: the Activity no longer visible
     */
    @Override
    public void onStop()
    {
        super.onStop();
    }

    /**
     * OnDestroy: the activity destroyed
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        super.onSaveInstanceState(outState);
        saveListViewPosition(outState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        restoreListViewPosition(savedState);
    }

    /**
     * ..based on stack overflow answer by ian
     * https://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */
    private void saveListViewPosition( Bundle outState)
    {
        int i = widgetList.getFirstVisiblePosition();
        outState.putInt(KEY_LISTVIEW_INDEX, i);

        int top = 0;
        View firstItem = widgetList.getChildAt(0);
        if (firstItem != null)
        {
            top = firstItem.getTop() - widgetList.getPaddingTop();
        }
        outState.putInt(KEY_LISTVIEW_TOP, top);
    }

    private void restoreListViewPosition(@NonNull Bundle savedState )
    {
        int i = savedState.getInt(KEY_LISTVIEW_INDEX, -1);
        if (i >= 0)
        {
            int top = savedState.getInt(KEY_LISTVIEW_TOP, 0);
            widgetList.setSelectionFromTop(i, top);
        }
    }

    /**
     * initialize ui/views
     * @param context a context used to access resources
     */
    protected void initViews(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);

        Toolbar menuBar = (Toolbar) findViewById(R.id.app_menubar);
        setSupportActionBar(menuBar);
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressView = findViewById(R.id.progress);

        widgetList = (ListView)findViewById(R.id.widgetList);
        widgetList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                WidgetListAdapter.WidgetListItem widgetItem = (WidgetListAdapter.WidgetListItem) widgetList.getAdapter().getItem(position);
                reconfigureWidget(widgetItem);
            }
        });

        View widgetListEmpty = findViewById(android.R.id.empty);
        widgetListEmpty.setOnClickListener(onEmptyViewClick);
        widgetList.setEmptyView(widgetListEmpty);
    }

    /**
     * onEmptyViewClick
     */
    private final View.OnClickListener onEmptyViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHelp();
        }
    };

    /**
     * updateViews
     * @param context context
     */
    protected void updateViews(@NonNull Context context)
    {
        widgetListAdapter = WidgetListAdapter.createWidgetListAdapter(context, false);
        widgetList.setAdapter(widgetListAdapter);
    }

    /**
     * showHelp
     */
    protected void showHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(getString(R.string.help_widgetlist));
        helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
        helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(SuntimesWidgetListActivity.this, HELP_PATH_ID), DIALOGTAG_HELP);
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    /**
     * showAbout
     */
    protected void showAbout()
    {
        Intent about = new Intent(this, AboutActivity.class);
        startActivity(about);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    /**
     * launchThemeEditor
     */
    protected void launchThemeEditor(Context context)
    {
        Intent configThemesIntent = new Intent(context, WidgetThemeListActivity.class);
        configThemesIntent.putExtra(WidgetThemeListActivity.PARAM_NOSELECT, true);
        startActivity(configThemesIntent);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    /**
     * launchActionList
     * @param context context
     */
    protected void launchActionList(Context context)
    {
        Intent intent = new Intent(context, ActionListActivity.class);
        intent.putExtra(WidgetThemeListActivity.PARAM_NOSELECT, true);
        startActivity(intent);
        overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void showProgress( Context context, CharSequence title, CharSequence message )
    {
        if (progressView != null) {
            progressView.setVisibility(View.VISIBLE);
        }
    }
    public void dismissProgress()
    {
        if (progressView != null) {
            progressView.setVisibility(View.GONE);
        }
    }

    /**
     * exportSettings
     * @param context Context
     */
    protected void exportSettings(Context context)
    {
        if (Build.VERSION.SDK_INT >= 19)
        {
            String filename = SuntimesBackupTask.DEF_EXPORT_TARGET + WidgetSettingsExportTask.FILEEXT;
            Intent intent = ExportTask.getCreateFileIntent(filename, WidgetSettingsExportTask.MIMETYPE);
            try {
                startActivityForResult(intent, EXPORT_REQUEST);
                return;

            } catch (ActivityNotFoundException e) {
                Log.e("ExportSettings", "SAF is unavailable? (" + e + ").. falling back to legacy export method.");
            }
        }
        SuntimesBackupTask.exportSettings(context, null, exportSettingsListener);
    }

    private final WidgetSettingsExportTask.TaskListener exportSettingsListener = new WidgetSettingsExportTask.TaskListener()
    {
        @Override
        public void onStarted()
        {
            //setRetainInstance(true);
            Context context = SuntimesWidgetListActivity.this;
            showProgress(context, context.getString(R.string.configAction_createBackup), context.getString(R.string.configAction_createBackup));
        }

        @Override
        public void onFinished(WidgetSettingsExportTask.ExportResult results)
        {
            //setRetainInstance(false);
            dismissProgress();

            Context context = SuntimesWidgetListActivity.this;
            if (context != null)
            {
                File file = results.getExportFile();
                String path = ((file != null) ? file.getAbsolutePath()
                        : ExportTask.getFileName(context.getContentResolver(), results.getExportUri()));

                if (results.getResult())
                {
                    //if (isAdded()) {
                    String successMessage = context.getString(R.string.msg_export_success, path);
                    SuntimesBackupTask.showIOResultSnackbar(context, getWindow().getDecorView(), results.getExportUri(), true, successMessage, null);
                    //}

                    if (Build.VERSION.SDK_INT >= 19) {
                        if (results.getExportUri() == null) {
                            ExportTask.shareResult(context, file, results.getMimeType());
                        }
                    } else {
                        ExportTask.shareResult(context, file, results.getMimeType());
                    }
                    return;
                }

                //if (isAdded()) {
                String failureMessage = context.getString(R.string.msg_export_failure, path);
                SuntimesBackupTask.showIOResultSnackbar(context, getWindow().getDecorView(), results.getExportUri(), false, failureMessage, null);
                //}
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void importSettings(Context context)
    {
        if (context != null) {
            startActivityForResult(ExportTask.getOpenFileIntent("text/*"), IMPORT_REQUEST);
        }
    }

    public void importSettings(final Context context, @NonNull Uri uri)
    {
        Log.i("ImportSettings", "Starting import task: " + uri);
        SuntimesBackupLoadTask task = new SuntimesBackupLoadTask(context);
        task.setTaskListener(new SuntimesBackupLoadTask.TaskListener()
        {
            @Override
            public void onStarted() {
                showProgress(context, context.getString(R.string.configAction_restoreBackup), context.getString(R.string.configAction_restoreBackup));
            }

            @Override
            public void onFinished(final SuntimesBackupLoadTask.TaskResult result)
            {
                dismissProgress();
                if (result.getResult() && result.numResults() > 0)
                {
                    final Map<String, ContentValues[]> allValues = result.getItems();
                    SuntimesBackupTask.chooseBackupContent(context, allValues.keySet(), true, new SuntimesBackupTask.ChooseBackupDialogListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which, String[] keys, boolean[] checked)
                        {
                            final Set<String> includeKeys = new TreeSet<>();
                            for (int i=0; i<keys.length; i++) {
                                if (checked[i]) {
                                    includeKeys.add(keys[i]);
                                }
                            }

                            final String[] keysThatWantMethods = new String[] { SuntimesBackupTask.KEY_WIDGETSETTINGS, SuntimesBackupTask.KEY_PLACEITEMS, SuntimesBackupTask.KEY_ALARMITEMS };
                            final Map<String, int[]> methodsForKeysThatWantMethods = new HashMap<>();
                            methodsForKeysThatWantMethods.put(SuntimesBackupTask.KEY_ALARMITEMS, SuntimesBackupRestoreTask.IMPORT_ALARMS_METHODS);
                            methodsForKeysThatWantMethods.put(SuntimesBackupTask.KEY_PLACEITEMS, SuntimesBackupRestoreTask.IMPORT_PLACES_METHODS);
                            methodsForKeysThatWantMethods.put(SuntimesBackupTask.KEY_WIDGETSETTINGS, SuntimesBackupRestoreTask.IMPORT_WIDGETS_METHODS);

                            final Map<String,Integer> methods = new HashMap<>();   // choose methods for key each; import after observing all
                            final SuntimesBackupRestoreTask.BackupKeyObserver observer = new SuntimesBackupRestoreTask.BackupKeyObserver(keysThatWantMethods, new SuntimesBackupRestoreTask.BackupKeyObserver.ObserverListener()
                            {
                                @Override
                                public void onObservingItem(final SuntimesBackupRestoreTask.BackupKeyObserver observer, final String key )
                                {
                                    if (includeKeys.contains(key))
                                    {
                                        SuntimesBackupRestoreTask.chooseImportMethod(context, key, methodsForKeysThatWantMethods.get(key), new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int importMethod) {
                                                methods.put(key, importMethod);
                                                observer.notify(key);    // trigger observeNext
                                            }
                                        });
                                    } else observer.notify(key);
                                }
                                public void onObservedAll(SuntimesBackupRestoreTask.BackupKeyObserver observer) {
                                    importSettings(context, includeKeys, methods, allValues);
                                }
                            });
                            observer.observeNext();

                            /*if (includeKeys.contains(SuntimesBackupTask.KEY_WIDGETSETTINGS))
                            {
                                SuntimesBackupRestoreTask.chooseImportMethod(context, SuntimesBackupTask.KEY_WIDGETSETTINGS, SuntimesBackupRestoreTask.IMPORT_WIDGETS_METHODS, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int widgetImportMethod) {
                                        methods.put(SuntimesBackupTask.KEY_WIDGETSETTINGS, widgetImportMethod);
                                        importSettings(context, includeKeys, methods, allValues);
                                    }
                                });
                            } else {
                                importSettings(context, includeKeys, methods, allValues);
                            }*/
                        }
                    });

                } else {
                    SuntimesBackupLoadTask.showIOResultSnackbar(context, getWindow().getDecorView(), false, 0, null);
                }
            }
        });
        task.execute(uri);
    }

    protected void importSettings(final Context context, final Set<String> keys, final Map<String,Integer> methods, final Map<String, ContentValues[]> allValues)
    {
        SuntimesBackupRestoreTask task = new SuntimesBackupRestoreTask(context);
        task.setData(allValues);
        task.setKeys(keys);
        task.setMethods(methods);
        task.setTaskListener(new SuntimesBackupRestoreTask.TaskListener()
        {
            @Override
            public void onStarted() {
                showProgress(context, context.getString(R.string.configAction_import), context.getString(R.string.configAction_import));
            }

            @Override
            public void onFinished(SuntimesBackupRestoreTask.TaskResult result)
            {
                dismissProgress();
                if (result.getResult())
                {
                    int c = result.getNumResults();
                    SuntimesBackupLoadTask.showIOResultSnackbar(context, getWindow().getDecorView(), (c > 0), c, ((c > 0) ? result.getReport() : null));

                } else {
                    SuntimesBackupLoadTask.showIOResultSnackbar(context, getWindow().getDecorView(), false, result.getNumResults(), result.getReport());
                }
            }
        });
        task.execute();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param widgetItem a WidgetListItem (referencing some widget id)
     */
    protected void reconfigureWidget(WidgetListAdapter.WidgetListItem widgetItem)
    {
        Intent configIntent = new Intent();
        configIntent.setComponent(new ComponentName(widgetItem.getPackageName(), widgetItem.getConfigClass()));
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetItem.getWidgetId());
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        configIntent.putExtra(EXTRA_RECONFIGURE, true);

        try {
            Log.i(getClass().getSimpleName(), "reconfigureWidget: " + widgetItem.getPackageName() + " :: " + widgetItem.getConfigClass());
            startActivity(configIntent);
            overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);

        } catch (ActivityNotFoundException | SecurityException e) {
            Log.e(getClass().getSimpleName(), "reconfigureWidget: " + widgetItem.getConfigClass() + " :: " + widgetItem.getConfigClass() + " :: " + e);
        }
    }

    /**
     * updateWidgetAlarms
     * @param context context
     */
    protected void updateWidgetAlarms(Context context) {
        if (widgetListAdapter != null) {
            WidgetListAdapter.updateWidgetAlarms(context, widgetListAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.widgetlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        if (itemId == R.id.action_themes) {
            launchThemeEditor(SuntimesWidgetListActivity.this);
            return true;

        } else if (itemId == R.id.action_actionlist) {
            launchActionList(SuntimesWidgetListActivity.this);
            return true;

        } else if (itemId == R.id.action_import) {
            importSettings(SuntimesWidgetListActivity.this);
            return true;

        } else if (itemId == R.id.action_export) {
            exportSettings(SuntimesWidgetListActivity.this);
            return true;

        } else if (itemId == R.id.action_help) {
            showHelp();
            return true;

        } else if (itemId == R.id.action_about) {
            showAbout();
            return true;

        } else if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, @NonNull Menu menu)
    {
        PopupMenuCompat.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
    }


}
