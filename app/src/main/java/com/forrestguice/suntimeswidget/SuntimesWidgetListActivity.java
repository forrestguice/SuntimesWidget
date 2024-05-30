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

package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.forrestguice.suntimeswidget.actions.ActionListActivity;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SuntimesBackupLoadTask;
import com.forrestguice.suntimeswidget.settings.SuntimesBackupRestoreTask;
import com.forrestguice.suntimeswidget.settings.SuntimesBackupTask;
import com.forrestguice.suntimeswidget.settings.WidgetSettingsExportTask;
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;
import com.forrestguice.suntimeswidget.widgets.AlarmWidget0;
import com.forrestguice.suntimeswidget.widgets.AlarmWidget0_2x2;
import com.forrestguice.suntimeswidget.widgets.AlarmWidget0_3x2;
import com.forrestguice.suntimeswidget.widgets.DateWidget0;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.SuntimesConfigActivity0.EXTRA_RECONFIGURE;

public class SuntimesWidgetListActivity extends AppCompatActivity
{
    private static final String DIALOGTAG_HELP = "help";
    private static final int HELP_PATH_ID = R.string.help_widgetlist_path;

    private static final String KEY_LISTVIEW_TOP = "widgetlisttop";
    private static final String KEY_LISTVIEW_INDEX = "widgetlistindex";

    public static final int IMPORT_REQUEST = 100;
    public static final int EXPORT_REQUEST = 200;

    private ActionBar actionBar;
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

        FragmentManager fragments = getSupportFragmentManager();
        HelpDialog helpDialog = (HelpDialog) fragments.findFragmentByTag(DIALOGTAG_HELP);
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
    public void onSaveInstanceState( Bundle outState )
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
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        progressView = findViewById(R.id.progress);

        widgetList = (ListView)findViewById(R.id.widgetList);
        widgetList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                WidgetListItem widgetItem = (WidgetListItem) widgetList.getAdapter().getItem(position);
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
    private View.OnClickListener onEmptyViewClick = new View.OnClickListener() {
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
        widgetListAdapter = WidgetListAdapter.createWidgetListAdapter(context);
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
     * @param context
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
    protected void reconfigureWidget(WidgetListItem widgetItem)
    {
        Intent configIntent = new Intent();
        configIntent.setComponent(new ComponentName(widgetItem.packageName, widgetItem.configClass));
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetItem.appWidgetId);
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        configIntent.putExtra(EXTRA_RECONFIGURE, true);

        try {
            Log.i(getClass().getSimpleName(), "reconfigureWidget: " + widgetItem.packageName + " :: " + widgetItem.configClass);
            startActivity(configIntent);
            overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);

        } catch (ActivityNotFoundException | SecurityException e) {
            Log.e(getClass().getSimpleName(), "reconfigureWidget: " + widgetItem.packageName + " :: " + widgetItem.configClass + " :: " + e);
        }
    }

    /**
     * updateWidgetAlarms
     * @param context context
     */
    protected void updateWidgetAlarms(Context context)
    {
        if (widgetListAdapter != null)
        {
            for (ComponentName widgetClass : widgetListAdapter.getAllWidgetClasses())
            {
                Intent updateIntent = new Intent(SuntimesWidget0.SUNTIMES_ALARM_UPDATE);
                updateIntent.setComponent(widgetClass);
                context.sendBroadcast(updateIntent);
            }
        }
    }

    /**
     * ListItem representing a running widget; specifies appWidgetId, and configuration activity.f
     */
    public static class WidgetListItem
    {
        protected final String packageName;
        protected final String widgetClass;
        protected final String configClass;
        protected final int appWidgetId;
        protected final Drawable icon;
        protected final String title;
        protected final String summary;

        public WidgetListItem( String packageName, String widgetClass, int appWidgetId, Drawable icon, String title, String summary, String configClass )
        {
            this.packageName = packageName;
            this.widgetClass = widgetClass;
            this.appWidgetId = appWidgetId;
            this.configClass = configClass;
            this.icon = icon;
            this.title = title;
            this.summary = summary;
        }

        public String getPackageName() {
            return packageName;
        }

        public int getWidgetId()
        {
            return appWidgetId;
        }

        public String getWidgetClass() {
            return widgetClass;
        }

        public String getConfigClass()
        {
            return configClass;
        }

        public Drawable getIcon() {
            return icon;
        }

        public String getTitle()
        {
            return title;
        }

        public String getSummary()
        {
            return summary;
        }

        public String toString()
        {
            return getTitle();
        }
    }

    /**
     * A ListAdapter of WidgetListItems.
     */
    @SuppressWarnings("Convert2Diamond")
    public static class WidgetListAdapter extends ArrayAdapter<WidgetListItem>
    {
        @SuppressWarnings("rawtypes")
        public static Class[] ALL_WIDGETS = new Class[] {
                SuntimesWidget0.class, SuntimesWidget0_2x1.class, SuntimesWidget0_3x1.class, SuntimesWidget1.class, SolsticeWidget0.class,
                MoonWidget0.class, MoonWidget0_2x1.class, MoonWidget0_3x1.class, MoonWidget0_3x2.class,
                SuntimesWidget2.class, SuntimesWidget2_3x1.class, SuntimesWidget2_3x2.class, SuntimesWidget2_3x3.class,
                ClockWidget0.class, ClockWidget0_3x1.class, DateWidget0.class,
                AlarmWidget0.class, AlarmWidget0_2x2.class, AlarmWidget0_3x2.class
        };

        public ComponentName[] getAllWidgetClasses()
        {
            ArrayList<ComponentName> components = new ArrayList<>();
            for (WidgetListItem widget : widgets)
            {
                ComponentName component = new ComponentName(widget.getPackageName(), widget.getWidgetClass());
                if (!components.contains(component)) {
                    components.add(component);
                }
            }
            return components.toArray(new ComponentName[0]);
        }

        private Context context;
        private ArrayList<WidgetListItem> widgets;

        public WidgetListAdapter(Context context, ArrayList<WidgetListItem> widgets)
        {
            super(context, R.layout.layout_listitem_widgets, widgets);
            this.context = context;
            this.widgets = widgets;
        }

        @Override
        @NonNull
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            return widgetItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            return widgetItemView(position, convertView, parent);
        }

        private View widgetItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            View view = convertView;
            if (convertView == null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.layout_listitem_widgets, parent, false);
            }

            WidgetListItem item = widgets.get(position);

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            icon.setImageDrawable(item.getIcon());

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item.getTitle());

            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            text2.setText(item.getSummary());

            TextView text3 = (TextView) view.findViewById(R.id.text3);
            if (text3 != null)
            {
                text3.setText(String.format("%s", item.getWidgetId()));
            }

            return view;
        }

        public static ArrayList<WidgetListItem> createWidgetListItems(Context context, @NonNull AppWidgetManager widgetManager, @NonNull String packageName, @NonNull String widgetClass)
        {
            String titlePattern = getTitlePattern(context, widgetClass);
            ArrayList<WidgetListItem> items = new ArrayList<WidgetListItem>();
            int[] ids = widgetManager.getAppWidgetIds(new ComponentName(packageName, widgetClass));
            for (int id : ids)
            {
                AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(id);
                SuntimesData data;
                String widgetTitle;
                int widgetSummaryResID = R.string.configLabel_widgetList_itemSummaryPattern;
                String widgetType = getWidgetName(context, widgetClass);
                String widgetClass0 = simpleClassName(widgetClass);
                String configClass = info.configure.getClassName();
                int widgetIcon = info.icon;

                if (widgetClass0.equals("SolsticeWidget0"))
                {
                    SuntimesEquinoxSolsticeData data0 =  new SuntimesEquinoxSolsticeData(context, id);
                    widgetTitle = utils.displayStringForTitlePattern(context, titlePattern, data0);
                    data = data0;

                } else if (widgetClass0.equals("MoonWidget0") || widgetClass0.equals("MoonWidget0_2x1") || widgetClass0.equals("MoonWidget0_3x1") || widgetClass0.equals("MoonWidget0_3x2")) {
                    SuntimesMoonData data0 =  new SuntimesMoonData(context, id, "moon");
                    widgetTitle = utils.displayStringForTitlePattern(context, titlePattern, data0);
                    data = data0;

                } else if (widgetClass0.equals("ClockWidget0") || widgetClass0.equals("ClockWidget0_3x1") ||  widgetClass0.equals("DateWidget0") || widgetClass0.equals("AlarmWidget0")) {
                    SuntimesClockData data0 = new SuntimesClockData(context, id);
                    widgetTitle = utils.displayStringForTitlePattern(context, titlePattern, data0);
                    widgetSummaryResID = R.string.configLabel_widgetList_itemSummaryPattern1;
                    data = data0;

                } else {
                    SuntimesRiseSetData data0 = new SuntimesRiseSetData(context, id);
                    widgetTitle = utils.displayStringForTitlePattern(context, titlePattern, data0);
                    data = data0;
                }

                String title = context.getString(R.string.configLabel_widgetList_itemTitle, widgetTitle);
                String source = ((data == null || data.calculatorMode() == null) ? "def" : data.calculatorMode().getName());
                String summary = context.getString(widgetSummaryResID, widgetType, source);
                items.add(new WidgetListItem(packageName, widgetClass, id, ContextCompat.getDrawable(context, widgetIcon), title, summary, configClass));
            }
            return items;
        }

        public static ArrayList<WidgetListItem> createWidgetListItems(@NonNull Context context, @NonNull String contentUri)
        {
            if (!contentUri.endsWith("/")) {
                contentUri += "/";
            }

            ArrayList<WidgetListItem> items = new ArrayList<WidgetListItem>();
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null)
            {
                Uri uri = Uri.parse(contentUri + QUERY_WIDGET);
                Cursor cursor = resolver.query(uri, QUERY_WIDGET_PROJECTION, null, null, null);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast())
                    {
                        try {
                            int appWidgetID = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WIDGET_APPWIDGETID));    // required
                            String packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WIDGET_PACKAGENAME));    // required
                            String widgetClass = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WIDGET_CLASS));    //required

                            int i_configClass = cursor.getColumnIndex(COLUMN_WIDGET_CONFIGCLASS);   // optional
                            String configClass = ((i_configClass >= 0) ? cursor.getString(i_configClass) : null);

                            int i_title = cursor.getColumnIndex(COLUMN_WIDGET_LABEL);    // optional
                            String title = ((i_title >= 0) ? cursor.getString(i_title) : widgetClass);

                            int i_summary = cursor.getColumnIndex(COLUMN_WIDGET_SUMMARY);    // optional
                            String summary = ((i_summary >= 0) ? cursor.getString(i_summary) : packageName);

                            Drawable iconDrawable;
                            int i_icon = cursor.getColumnIndex(COLUMN_WIDGET_ICON);    // optional
                            if (i_icon >= 0) {
                                byte[] iconBlob = cursor.getBlob(i_icon);
                                Bitmap iconBitmap = (iconBlob != null ? BitmapFactory.decodeByteArray(iconBlob, 0, iconBlob.length) : null);
                                iconDrawable = (iconBitmap != null ? new BitmapDrawable(context.getResources(), iconBitmap) : ContextCompat.getDrawable(context, R.drawable.ic_action_suntimes));
                            } else {
                                iconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_action_suntimes);
                            }

                            items.add(new WidgetListItem(packageName, widgetClass, appWidgetID, iconDrawable, title, summary, configClass));

                        } catch (IllegalArgumentException e) {
                            Log.e("WidgetListActivity", "Missing column! skipping this entry.. " + e);
                        }
                        cursor.moveToNext();
                    }
                    cursor.close();
                }
            }
            return items;
        }

        public static WidgetListAdapter createWidgetListAdapter(@NonNull Context context)
        {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            ArrayList<WidgetListItem> items = new ArrayList<WidgetListItem>();
            String packageName = context.getPackageName();
            for (Class widgetClass : ALL_WIDGETS) {
                items.addAll(createWidgetListItems(context, widgetManager, packageName, widgetClass.getName()));
            }
            for (String uri : queryWidgetInfoProviders(context)) {
                items.addAll(createWidgetListItems(context, uri));
            }
            return new WidgetListAdapter(context, items);
        }

        private static String getTitlePattern(Context context, @NonNull String widgetClass)
        {
            switch (simpleClassName(widgetClass))
            {
                case "DateWidget0":
                    return context.getString(R.string.configLabel_widgetList_itemTitlePattern2);
                case "AlarmWidget0": case "AlarmWidget0_2x2":    // TODO: alarm widget title pattern
                case "ClockWidget0": case "ClockWidget0_3x1":
                case "MoonWidget0": case "MoonWidget0_2x1": case "MoonWidget0_3x1": case "MoonWidget0_3x2":
                case "SuntimesWidget2": case "SuntimesWidget2_3x1": case "SuntimesWidget2_3x2": case "SuntimesWidget2_3x3":
                    return context.getString(R.string.configLabel_widgetList_itemTitlePattern1);

                case "SuntimesWidget0": case "SuntimesWidget0_2x1": case "SuntimesWidget1": case "SolsticeWidget0":
                default: return context.getString(R.string.configLabel_widgetList_itemTitlePattern);
            }
        }

        private static String getWidgetName(Context context, @NonNull String widgetClass)
        {
            switch (simpleClassName(widgetClass))
            {
                case "SolsticeWidget0": return context.getString(R.string.app_name_solsticewidget0);
                case "AlarmWidget0": return context.getString(R.string.app_name_alarmwidget0);
                case "AlarmWidget0_2x2": return context.getString(R.string.app_name_alarmwidget0) + " (2x2)";
                case "ClockWidget0": return context.getString(R.string.app_name_clockwidget0);
                case "ClockWidget0_3x1": return context.getString(R.string.app_name_clockwidget0) + " (3x1)";
                case "DateWidget0": return context.getString(R.string.app_name_datewidget0);
                case "MoonWidget0": return context.getString(R.string.app_name_moonwidget0);
                case "MoonWidget0_2x1": return context.getString(R.string.app_name_moonwidget0) + " (2x1)";
                case "MoonWidget0_3x1": return context.getString(R.string.app_name_moonwidget0) + " (3x1)";
                case "MoonWidget0_3x2": return context.getString(R.string.app_name_moonwidget0) + " (3x2)";
                case "SuntimesWidget1": return context.getString(R.string.app_name_widget1);
                case "SuntimesWidget2": return context.getString(R.string.app_name_widget2);
                case "SuntimesWidget2_3x1": return context.getString(R.string.app_name_widget2) + " (3x1)";
                case "SuntimesWidget2_3x2": return context.getString(R.string.app_name_widget2) + " (3x2)";
                case "SuntimesWidget2_3x3": return context.getString(R.string.app_name_widget2) + " (3x3)";
                case "SuntimesWidget0_2x1": return context.getString(R.string.app_name_widget0) + " (2x1)";
                default: return context.getString(R.string.app_name_widget0);
            }
        }

        private static String simpleClassName(String className)
        {
            final int i = className.lastIndexOf(".");
            if (i > 0) {
                return className.substring(className.lastIndexOf(".") + 1);
            } else return className;
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
        switch (item.getItemId())
        {
            case R.id.action_themes:
                launchThemeEditor(SuntimesWidgetListActivity.this);
                return true;

            case R.id.action_actionlist:
                launchActionList(SuntimesWidgetListActivity.this);
                return true;

            case R.id.action_import:
                importSettings(SuntimesWidgetListActivity.this);
                return true;

            case R.id.action_export:
                exportSettings(SuntimesWidgetListActivity.this);
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

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        SuntimesUtils.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.transition_cancel_in, R.anim.transition_cancel_out);
    }

    public static final String ACTION_SUNTIMES_LISTWIDGETS = "suntimes.action.LIST_WIDGETS";
    public static final String CATEGORY_SUNTIMES_ADDON = "suntimes.SUNTIMES_ADDON";
    public static final String KEY_WIDGET_INFO_PROVIDER = "WidgetInfoProvider";
    public static final String REQUIRED_PERMISSION = "suntimes.permission.READ_CALCULATOR";

    public static final String COLUMN_WIDGET_PACKAGENAME = "packagename";
    public static final String COLUMN_WIDGET_APPWIDGETID = "appwidgetid";
    public static final String COLUMN_WIDGET_CLASS = "widgetclass";
    public static final String COLUMN_WIDGET_CONFIGCLASS = "configclass";
    public static final String COLUMN_WIDGET_LABEL = "label";
    public static final String COLUMN_WIDGET_SUMMARY = "summary";
    public static final String COLUMN_WIDGET_ICON = "icon";

    public static final String QUERY_WIDGET = "widgets";
    public static final String[] QUERY_WIDGET_PROJECTION = new String[] {
            COLUMN_WIDGET_APPWIDGETID, COLUMN_WIDGET_CLASS, COLUMN_WIDGET_CONFIGCLASS, COLUMN_WIDGET_PACKAGENAME,
            COLUMN_WIDGET_LABEL, COLUMN_WIDGET_SUMMARY, COLUMN_WIDGET_ICON
    };

    public static List<String> queryWidgetInfoProviders(@NonNull Context context)
    {
        ArrayList<String> references = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        Intent packageQuery = new Intent(ACTION_SUNTIMES_LISTWIDGETS);
        packageQuery.addCategory(CATEGORY_SUNTIMES_ADDON);
        List<ResolveInfo> packages = packageManager.queryIntentActivities(packageQuery, PackageManager.GET_META_DATA);
        Log.i("queryWidgetInfo", "Scanning for WidgetInfoProvider references... found " + packages.size());

        for (ResolveInfo resolveInfo : packages)
        {
            if (resolveInfo != null && resolveInfo.activityInfo != null && resolveInfo.activityInfo.metaData != null)
            {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
                    if (hasPermission(packageInfo, resolveInfo.activityInfo))
                    {
                        String metaData = resolveInfo.activityInfo.metaData.getString(KEY_WIDGET_INFO_PROVIDER);
                        String[] values = (metaData != null) ? metaData.replace(" ","").split("\\|") : new String[0];
                        references.addAll(Arrays.asList(values));
                    } else {
                        Log.w("queryWidgetInfo", "Permission denied! " + packageInfo.packageName + " does not have required permissions.");
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("queryWidgetInfo", "Package not found! " + e);
                }
            }
        }
        return references;
    }

    private static boolean hasPermission(@NonNull PackageInfo packageInfo, @NonNull ActivityInfo activityInfo)
    {
        boolean hasPermission = false;
        if (packageInfo.requestedPermissions != null)
        {
            for (String permission : packageInfo.requestedPermissions) {
                if (permission != null && permission.equals(REQUIRED_PERMISSION)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        return hasPermission;
    }


}
