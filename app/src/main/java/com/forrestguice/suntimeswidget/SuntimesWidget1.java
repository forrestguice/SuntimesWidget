package com.forrestguice.suntimeswidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.layouts.SuntimesLayout;
import com.forrestguice.suntimeswidget.layouts.SuntimesLayout_1x1_0;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 *  Flippable widget
 */
public class SuntimesWidget1 extends SuntimesWidget
{
    private static final int UPDATEALARM_ID = 1;

    @Override
    protected Class getConfigClass()
    {
        return SuntimesConfigActivity1.class;
    }

    /**
     * @return an update alarm identifier for this class (SuntimesWidget1: 1)
     */
    @Override
    protected int getUpdateAlarmId()
    {
        return SuntimesWidget1.UPDATEALARM_ID;
    }

    @Override
    protected void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SuntimesWidget1.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected boolean handleClickAction(Context context, Intent intent)
    {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        int appWidgetId = (extras != null ? extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0) : 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            if (action.equals(WidgetSettings.ActionMode.ONTAP_FLIPTO_NEXTITEM.name()))
            {
                RemoteViews views = getWidgetViews(context, null, appWidgetId);
                views.showNext(R.id.view_flip);
                AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(appWidgetId, views);
                return true;
            }
        }

        return super.handleClickAction(context, intent);
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, SuntimesLayout layout)
    {
        RemoteViews views = getWidgetViews(context, appWidgetManager, appWidgetId);

        WidgetSettings.ActionMode actionMode = WidgetSettings.loadActionModePref(context, appWidgetId);
        Intent intent = new Intent(context, SuntimesWidget1.class);
        intent.setAction(actionMode.name());
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
        views.setOnClickPendingIntent(R.id.widgetframe_outer_1x1, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, null);   // null on this line to discard previously cached RemoveViews
        appWidgetManager.updateAppWidget(appWidgetId, views);  // so this next line actually updates...
    }

    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        SuntimesWidget1.updateAppWidget(context, appWidgetManager, appWidgetId, null);
    }

    protected static RemoteViews getWidgetViews(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        int minWidth = context.getResources().getInteger(R.integer.widget_size_minWidthDp);
        int minHeight = context.getResources().getInteger(R.integer.widget_size_minHeightDp);
        int[] mustFitWithinDp = {minWidth, minHeight};
        Log.d("getWidgetViews1", "0: must fit:  [" + mustFitWithinDp[0] + ", " + mustFitWithinDp[1] + "]");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            /**if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                 Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
                 int[] sizePortrait = {widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH),
                 widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)};
                 int[] sizeLandscape = {widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH),
                 widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)};

                 mustFitWithinDp[0] = Math.min(sizePortrait[0], sizeLandscape[0]);
                 mustFitWithinDp[1] = Math.min(sizePortrait[1], sizeLandscape[1]);
            }*/

            RemoteViews views;
            /*views = ((mustFitWithinDp[0] >= maxWidth1x1) ? new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x1)   // TODO: make 1x3 also flippable
                                                         : new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x1) );*/
            views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x1);

            Intent intent = new Intent(context, SuntimesWidget1Service.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            views.setRemoteAdapter(R.id.view_flip, intent);
            views.setEmptyView(R.id.view_flip, R.id.emptyView);
            return views;

        } else {
            Log.w("getWidgetViews1", "Version less than " + Build.VERSION_CODES.ICE_CREAM_SANDWICH + "!! Calling the default implementation.");
            SuntimesLayout layout = new SuntimesLayout_1x1_0();
            return layout.getViews(context);
        }
    }

}
