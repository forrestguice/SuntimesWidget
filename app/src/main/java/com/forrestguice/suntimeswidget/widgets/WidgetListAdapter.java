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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

import com.forrestguice.suntimeswidget.calculator.DataSubstitutions;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.settings.android.AndroidSuntimesDataSettings;
import com.forrestguice.util.ExecutorUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A ListAdapter of WidgetListItems.
 */
@SuppressWarnings("Convert2Diamond")
public class WidgetListAdapter extends ArrayAdapter<WidgetListAdapter.WidgetListItem>
{
    private static final SuntimesUtils utils = new SuntimesUtils();

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

    private final WeakReference<Context> contextRef;
    private final ArrayList<WidgetListItem> widgets;

    public WidgetListAdapter(Context context)
    {
        super(context, R.layout.layout_listitem_widgets);
        this.contextRef = new WeakReference<>(context);
        this.widgets = new ArrayList<WidgetListItem>();
    }

    public WidgetListAdapter(Context context, ArrayList<WidgetListItem> widgets)
    {
        super(context, R.layout.layout_listitem_widgets, widgets);
        this.contextRef = new WeakReference<>(context);
        this.widgets = widgets;
    }

    public void loadItems(Context context, Class<?>[] widgetClasses)
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        String packageName = context.getPackageName();
        ArrayList<WidgetListItem> items = new ArrayList<>();
        for (Class<?> widgetClass : widgetClasses) {
            items.addAll(createWidgetListItems(context, widgetManager, packageName, widgetClass.getName()));
        }
        addAll(items);
    }

    public void loadItems(final Context context, final List<String> widgetInfoProviders, boolean blocking)
    {
        if (blocking)
        {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            for (final String uri : widgetInfoProviders)
            {
                ArrayList<WidgetListItem> items = ExecutorUtils.getResult("WidgetListAdapter", new Callable<ArrayList<WidgetListItem>>()
                {
                    @Override
                    public ArrayList<WidgetListItem> call()
                    {
                        long bench_start = System.nanoTime();
                        ArrayList<WidgetListItem> result = createWidgetListItems(context, uri);
                        Log.d("WidgetListAdapter", "BENCH: querying " + uri  + " took " + ((System.nanoTime() - bench_start) / 1000000.0) + " ms");
                        return result;
                    }
                }, MAX_WAIT_MS);
                if (items != null) {
                    addAll(items);
                }
            }
            executor.shutdownNow();

        } else {
            final Handler handler = new Handler(Looper.getMainLooper());
            initExecutorService().submit(new Runnable()
            {
                public void run()
                {
                    for (String contentUri : widgetInfoProviders)
                    {
                        final ArrayList<WidgetListItem> result = createWidgetListItems(context, contentUri);
                        handler.post(new Runnable() {
                            public void run() {
                                addAll(result);
                                cleanupExecutorService();
                            }
                        });
                    }
                }
            });
        }
    }

    public static final long MAX_WAIT_MS = 1000;

    @Nullable
    private ExecutorService executor = null;
    protected ExecutorService initExecutorService()
    {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        return executor;
    }
    protected void cleanupExecutorService()
    {
        if (executor != null) {
            if (!executor.isShutdown()) {
                executor.shutdownNow();
            }
            executor = null;
        }
    }

    @Override
    public void addAll(@NonNull Collection<? extends WidgetListItem> collection)
    {
        widgets.addAll(collection);
        super.addAll(collection);
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
            LayoutInflater inflater = LayoutInflater.from(contextRef.get());
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
                widgetTitle = DataSubstitutions.displayStringForTitlePattern0(AndroidSuntimesDataSettings.wrap(context), titlePattern, data0);
                data = data0;

            } else if (widgetClass0.equals("MoonWidget0") || widgetClass0.equals("MoonWidget0_2x1") || widgetClass0.equals("MoonWidget0_3x1") || widgetClass0.equals("MoonWidget0_3x2")) {
                SuntimesMoonData data0 =  new SuntimesMoonData(context, id, "moon");
                widgetTitle = DataSubstitutions.displayStringForTitlePattern0(AndroidSuntimesDataSettings.wrap(context), titlePattern, data0);
                data = data0;

            } else if (widgetClass0.equals("ClockWidget0") || widgetClass0.equals("ClockWidget0_3x1")
                    || widgetClass0.equals("DateWidget0")
                    || widgetClass0.equals("AlarmWidget0") || widgetClass0.equals("AlarmWidget0_2x2") || widgetClass0.equals("AlarmWidget0_3x2")) {
                SuntimesClockData data0 = new SuntimesClockData(context, id);
                widgetTitle = DataSubstitutions.displayStringForTitlePattern0(AndroidSuntimesDataSettings.wrap(context), titlePattern, data0);
                widgetSummaryResID = R.string.configLabel_widgetList_itemSummaryPattern1;
                data = data0;

            } else {
                SuntimesRiseSetData data0 = new SuntimesRiseSetData(context, id);
                widgetTitle = DataSubstitutions.displayStringForTitlePattern0(AndroidSuntimesDataSettings.wrap(context), titlePattern, data0);
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

    public static WidgetListAdapter createWidgetListAdapter(@NonNull Context context) {
        return createWidgetListAdapter(context, true);
    }
    public static WidgetListAdapter createWidgetListAdapter(@NonNull Context context, boolean blocking)
    {
        WidgetListAdapter adapter = new WidgetListAdapter(context);
        adapter.loadItems(context, ALL_WIDGETS);
        adapter.loadItems(context, queryWidgetInfoProviders(context), blocking);
        return adapter;
    }

    private static String getTitlePattern(Context context, @NonNull String widgetClass)
    {
        switch (simpleClassName(widgetClass))
        {
            case "DateWidget0":
                return context.getString(R.string.configLabel_widgetList_itemTitlePattern2);

            case "AlarmWidget0": case "AlarmWidget0_2x2": case "AlarmWidget0_3x2":
                return context.getString(R.string.configLabel_widgetList_itemTitlePattern3);

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
            case "AlarmWidget0": return context.getString(R.string.app_name_alarmwidget0) + " (1x1)";
            case "AlarmWidget0_2x2": return context.getString(R.string.app_name_alarmwidget0) + " (2x2)";
            case "AlarmWidget0_3x2": return context.getString(R.string.app_name_alarmwidget0) + " (3x2)";
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static void updateAllWidgetAlarms(Context context) {
        updateWidgetAlarms(context, WidgetListAdapter.createWidgetListAdapter(context));
    }
    public static void updateWidgetAlarms(Context context, WidgetListAdapter adapter)
    {
        if (adapter != null) {
            for (ComponentName widgetClass : adapter.getAllWidgetClasses()) {
                updateWidgetAlarms(context, widgetClass);
            }
        }
    }
    public static void updateWidgetAlarms(Context context, ComponentName widgetClass)
    {
        Intent updateIntent = new Intent(SuntimesWidget0.SUNTIMES_ALARM_UPDATE);
        updateIntent.setComponent(widgetClass);
        context.sendBroadcast(updateIntent);
    }

    public static void updateWidgetsMatchingTheme(Context context, WidgetListAdapter adapter, String themeName)
    {
        if (adapter != null) {
            for (ComponentName widgetClass : adapter.getAllWidgetClasses()) {
                updateWidgetThemes(context, widgetClass, themeName);
            }
        }
    }
    public static void updateWidgetThemes(Context context, ComponentName widgetClass, String themeName)
    {
        Intent updateIntent = new Intent(SuntimesWidget0.SUNTIMES_THEME_UPDATE);
        updateIntent.putExtra(SuntimesWidget0.KEY_THEME, themeName);
        updateIntent.setPackage(widgetClass.getPackageName());
        context.sendBroadcast(updateIntent);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ACTION_SUNTIMES_LISTWIDGETS = "suntimes.action.LIST_WIDGETS";
    public static final String CATEGORY_SUNTIMES_ADDON = "suntimes.SUNTIMES_ADDON";
    public static final String KEY_WIDGET_INFO_PROVIDER = "WidgetInfoProvider";
    public static String REQUIRED_PERMISSION() {
        return BuildConfig.SUNTIMES_PERMISSION_ROOT + ".permission.READ_CALCULATOR";
    }

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
            String requiredPermission = REQUIRED_PERMISSION();
            for (String permission : packageInfo.requestedPermissions) {
                if (permission != null && permission.equals(requiredPermission)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        return hasPermission;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

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

        public WidgetListItem( String packageName, String widgetClass, int appWidgetId, Drawable icon, @NonNull String title, String summary, String configClass )
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

        @NonNull
        public String getTitle()
        {
            return title;
        }

        public String getSummary()
        {
            return summary;
        }

        @NonNull
        public String toString()
        {
            return getTitle();
        }
    }
}
