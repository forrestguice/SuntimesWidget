/**
    Copyright (C) 2014-2019 Forrest Guice
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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
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
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.forrestguice.suntimeswidget.themes.WidgetThemeListActivity;
import com.forrestguice.suntimeswidget.widgets.DateWidget0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.forrestguice.suntimeswidget.SuntimesConfigActivity0.EXTRA_RECONFIGURE;

public class SuntimesWidgetListActivity extends AppCompatActivity
{
    private static final String DIALOGTAG_HELP = "help";

    private static final String KEY_LISTVIEW_TOP = "widgetlisttop";
    private static final String KEY_LISTVIEW_INDEX = "widgetlistindex";

    private ActionBar actionBar;
    private ListView widgetList;
    private WidgetListAdapter widgetListAdapter;
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
        setTheme(AppSettings.loadTheme(this));
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

    /**
     * OnResume: the user is now interacting w/ the Activity (running state)
     */
    @Override
    public void onResume()
    {
        super.onResume();
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
     * ListItem representing a running widget; specifies appWidgetId, and configuration activity.
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
        public static String[] ALL_WIDGETS = new String[] {
                SuntimesWidget0.class.getName(), SuntimesWidget0_2x1.class.getName(), SuntimesWidget1.class.getName(), SolsticeWidget0.class.getName(),
                MoonWidget0.class.getName(), MoonWidget0_2x1.class.getName(), MoonWidget0_3x1.class.getName(), MoonWidget0_3x2.class.getName(),
                SuntimesWidget2.class.getName(), SuntimesWidget2_3x1.class.getName(), SuntimesWidget2_3x2.class.getName(), SuntimesWidget2_3x3.class.getName(),
                ClockWidget0.class.getName(), ClockWidget0_3x1.class.getName(), DateWidget0.class.getName()
        };

        public ComponentName[] getAllWidgetClasses()
        {
            ArrayList<ComponentName> components = new ArrayList<>();
            for (WidgetListItem widget : widgets)
            {
                ComponentName component = new ComponentName(widget.packageName, widget.widgetClass);
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

                } else if (widgetClass0.equals("ClockWidget0") || widgetClass0.equals("ClockWidget0_3x1") ||  widgetClass0.equals("DateWidget0")) {
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
            for (String widgetClass : ALL_WIDGETS) {
                items.addAll(createWidgetListItems(context, widgetManager, packageName, widgetClass));
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
