/**
 Copyright (C) 2014 Forrest Guice
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

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout_1x1_1;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout_1x1_2;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout_1x1_4;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.util.ArrayList;

/**
 * SuntimesWidgetService : RemoteViewsService
 */
@TargetApi(14)
public class SuntimesWidget1Service extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        Context context = this.getApplicationContext();
        return new SuntimesWidget1RemoteViewsFactory(context, intent);
    }
}

/**
 * SuntimesWidgetRemoteViewsFactory : RemoteViewsFactory
 */
@TargetApi(14)
class SuntimesWidget1RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
    private Context context;
    private int appWidgetId;
    private int viewCount = 0;

    private ArrayList<SuntimesRiseSetData> dataset = new ArrayList<SuntimesRiseSetData>();

    public SuntimesWidget1RemoteViewsFactory(Context context, Intent intent)
    {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                              AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate()
    {
    }

    @Override
    public void onDataSetChanged()
    {
        ArrayList<SuntimesRiseSetData> dataset0 = new ArrayList<SuntimesRiseSetData>();
        int viewCount0 = 1;

        SuntimesRiseSetData data0 = new SuntimesRiseSetData(context, appWidgetId);
        data0.calculate();
        dataset0.add(new SuntimesRiseSetData(data0, R.layout.layout_widget_1x1_1i));
        viewCount0++;

        boolean showNoon = WidgetSettings.loadShowNoonPref(context, appWidgetId);
        if (showNoon)
        {
            SuntimesRiseSetData data1 = new SuntimesRiseSetData(data0);
            data1.setTimeMode(WidgetSettings.TimeMode.NOON);
            data1.calculate();
            dataset0.add(new SuntimesRiseSetData(data1, R.layout.layout_widget_1x1_4i));
            viewCount0++;
        }

        dataset0.add(new SuntimesRiseSetData(data0, R.layout.layout_widget_1x1_2i));
        viewCount0++;

        dataset = dataset0;
        viewCount = viewCount0;

        initLocale(context);
        Log.d("DEBUG", "onDataSetChanged");
    }

    private void initLocale( Context context )
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.TimeMode.initDisplayStrings(context);
    }

    @Override
    public void onDestroy()
    {
    }

    @Override
    public int getCount()
    {
        return dataset.size();
    }

    @Override
    public RemoteViews getViewAt(int position)
    {
        SuntimesRiseSetData data = null;
        int layoutID = R.layout.layout_widget_1x1_2;
        if (position >= 0 && position < dataset.size())
        {
            data = dataset.get(position);
            layoutID = data.layoutID();
        }

        SuntimesLayout layout;
        switch(layoutID)
        {
            case R.layout.layout_widget_1x1_1:
            case R.layout.layout_widget_1x1_1i:
                layout = new SuntimesLayout_1x1_1(R.layout.layout_widget_1x1_1i);
                break;

            case R.layout.layout_widget_1x1_4:
            case R.layout.layout_widget_1x1_4i:
                layout = new SuntimesLayout_1x1_4(R.layout.layout_widget_1x1_4i);
                break;

            case R.layout.layout_widget_1x1_2:
            case R.layout.layout_widget_1x1_2i:
            default:
                layout = new SuntimesLayout_1x1_2(R.layout.layout_widget_1x1_2i);
                break;
        }
        RemoteViews views = layout.getViews(context);

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        layout.themeViews(context, views, appWidgetId);
        if (data != null)
        {
            layout.updateViews(context, appWidgetId, views, data);
        } else {
            Log.w("DEBUG", "null data! skipping update");
        }
        return views;
    }

    @Override
    public RemoteViews getLoadingView()
    {
        return null;
    }

    @Override
    public int getViewTypeCount()
    {
        return viewCount + 2;  // a different type of view for each item, plus loading view, plus error view
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

}