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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.forrestguice.suntimeswidget.calculator.SuntimesWidgetData;

import java.util.ArrayList;

/**
 * SuntimesWidgetService : RemoteViewsService
 */
public class SuntimesWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new SuntimesWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

/**
 * SuntimesWidgetRemoteViewsFactory : RemoteViewsFactory
 */
class SuntimesWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
    private Context context;
    private int appWidgetId;
    private int viewCount = 0;

    private ArrayList<SuntimesWidgetData> dataset = new ArrayList<>();

    public SuntimesWidgetRemoteViewsFactory( Context context, Intent intent )
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
        SuntimesWidgetData data = new SuntimesWidgetData(context, appWidgetId);
        data.calculate();

        dataset.clear();
        dataset.add(new SuntimesWidgetData(data, R.layout.layout_widget_1x1_0i));
        dataset.add(new SuntimesWidgetData(data, R.layout.layout_widget_1x1_1i));
        dataset.add(new SuntimesWidgetData(data, R.layout.layout_widget_1x1_2i));
        viewCount = 3;
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
        SuntimesWidgetData data = dataset.get(position);

        RemoteViews views = new RemoteViews(context.getPackageName(), data.layoutID());
        SuntimesWidget.themeViews(context, views, appWidgetId);
        SuntimesWidget.updateViews(context, appWidgetId, views, data);
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