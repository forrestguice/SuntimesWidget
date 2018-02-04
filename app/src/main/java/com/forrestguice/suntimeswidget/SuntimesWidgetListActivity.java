/**
    Copyright (C) 2014-2018 Forrest Guice
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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;

public class SuntimesWidgetListActivity extends AppCompatActivity
{
    private static final String DIALOGTAG_HELP = "help";

    private ListView widgetList;
    private static final SuntimesUtils utils = new SuntimesUtils();

    public SuntimesWidgetListActivity()
    {
        super();
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
        initLocale(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_widgetlist);
        initViews(this);
    }

    private void initLocale(Context context)
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
    }

    /**
     * OnStart: the Activity becomes visible
     */
    @Override
    public void onStart()
    {
        super.onStart();
        updateViews(this);
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

    /**
     * initialize ui/views
     * @param context a context used to access resources
     */
    protected void initViews(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);

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

        initHelpItem();
    }

    protected void updateViews(Context context)
    {
        widgetList.setAdapter(WidgetListAdapter.createWidgetListAdapter(context));
    }

    private void initHelpItem()
    {
        RelativeLayout helpItem = (RelativeLayout) findViewById(R.id.itemLayout);
        if (helpItem != null)
        {
            helpItem.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showHelp();
                }
            });

            TextView helpTitle = (TextView) helpItem.findViewById(android.R.id.text1);
            helpTitle.setText(getString(R.string.configLabel_widgetListHelp_title));

            TextView helpSummary = (TextView) helpItem.findViewById(android.R.id.text2);
            helpSummary.setText(getString(R.string.configLabel_widgetListHelp_summary));
        }
    }

    /**
     *
     */
    protected void showHelp()
    {
        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(getString(R.string.help_widgetlist));
        helpDialog.show(getSupportFragmentManager(), DIALOGTAG_HELP);
    }

    /**
     * @param widget a WidgetListItem (referencing some widget id)
     */
    protected void reconfigureWidget(WidgetListItem widget)
    {
        Intent configIntent = new Intent(this, widget.getConfigClass());
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.getWidgetId());
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        configIntent.putExtra(WidgetSettings.ActionMode.ONTAP_LAUNCH_CONFIG.name(), true);
        startActivity(configIntent);
    }

    /**
     * ListItem representing a running widget; specifies appWidgetId, and configuration activity.
     */
    public static class WidgetListItem
    {
        private final int appWidgetId;
        private final int icon;
        private final String title;
        private final String summary;
        private final Class configClass;

        public WidgetListItem( int appWidgetId, int icon, String title, String summary, Class configClass )
        {
            this.appWidgetId = appWidgetId;
            this.configClass = configClass;
            this.icon = icon;
            this.title = title;
            this.summary = summary;
        }

        public int getWidgetId()
        {
            return appWidgetId;
        }

        public Class getConfigClass()
        {
            return configClass;
        }

        public int getIcon()
        {
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
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return widgetItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return widgetItemView(position, convertView, parent);
        }

        private View widgetItemView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View view = convertView;
            if (convertView == null)
            {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.layout_listitem_widgets, parent, false);
            }

            WidgetListItem item = widgets.get(position);

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            icon.setImageResource(item.getIcon());

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item.getTitle());

            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            text2.setText(item.getSummary());

            return view;
        }

        public static WidgetListItem createWidgetListItem(Context context, int appWidgetId, AppWidgetManager widgetManager, SuntimesData data, String widgetTitle, int widgetNameStringId) throws ClassNotFoundException
        {
            AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(appWidgetId);
            String title = context.getString(R.string.configLabel_widgetList_itemTitle, widgetTitle);
            String type = context.getString(widgetNameStringId);
            String source = ((data.calculatorMode() == null) ? "def" : data.calculatorMode().name());
            String summary = context.getString(R.string.configLabel_widgetList_itemSummaryPattern, type, source);
            return new WidgetListItem(appWidgetId, info.icon, title, summary, Class.forName(info.configure.getClassName()) );
        }

        public static ArrayList<WidgetListItem> createWidgetListItems(Context context, AppWidgetManager widgetManager, Class widgetClass, String titlePattern)
        {
            ArrayList<WidgetListItem> items = new ArrayList<WidgetListItem>();
            int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, widgetClass));
            for (int id : ids)
            {
                try {
                    SuntimesData data;
                    String widgetTitle;
                    int widgetNameId;
                    if (widgetClass == SuntimesWidget2.class)
                    {
                        SuntimesEquinoxSolsticeData data0 =  new SuntimesEquinoxSolsticeData(context, id);
                        widgetTitle = utils.displayStringForTitlePattern(titlePattern, data0);
                        widgetNameId = R.string.app_name_widget2;
                        data = data0;

                    } else {
                        SuntimesRiseSetData data0 = new SuntimesRiseSetData(context, id);
                        widgetTitle = utils.displayStringForTitlePattern(titlePattern, data0);
                        widgetNameId = (widgetClass == SuntimesWidget1.class ? R.string.app_name_widget1 : R.string.app_name_widget0);
                        data = data0;
                    }
                    items.add(createWidgetListItem(context, id, widgetManager, data, widgetTitle, widgetNameId));
                } catch (ClassNotFoundException e) {
                    Log.e("WidgetListActivity", "configuration class for widget " + id + " missing.");
                }
            }
            return items;
        }

        public static WidgetListAdapter createWidgetListAdapter(Context context)
        {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            ArrayList<WidgetListItem> items = new ArrayList<WidgetListItem>();
            String titlePattern = context.getString(R.string.configLabel_widgetList_itemTitlePattern);
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget0.class, titlePattern));
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget0_2x1.class, titlePattern));
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget1.class, titlePattern));
            items.addAll(createWidgetListItems(context, widgetManager, SuntimesWidget2.class, titlePattern));
            return new WidgetListAdapter(context, items);
        }

    }

}
