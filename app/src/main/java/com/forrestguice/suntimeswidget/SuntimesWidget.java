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

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.content.Context;
import android.widget.RemoteViews;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import com.forrestguice.suntimeswidget.calculator.SuntimesWidgetData;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetSettings;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetTheme;
import com.forrestguice.suntimeswidget.settings.SuntimesWidgetThemes;

public class SuntimesWidget extends AppWidgetProvider
{
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
    {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        SuntimesWidgetThemes.initThemes(context);
        SuntimesWidgetSettings.TimeMode.initDisplayStrings(context);

        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds)
        {
            SuntimesWidgetSettings.deletePrefs(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
    }

    /**
     * @param context the application context
     * @param rows number of rows in widget
     * @param columns number of cols in widget
     * @return a RemoteViews instance for the specified widget size
     */
    private static RemoteViews getWidgetViews(Context context, int appWidgetId, int rows, int columns)
    {
        RemoteViews views;
        if (columns >= 3)
        {
            views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x3);

        } else if (columns == 2) {
            views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x2);

        } else {
            SuntimesWidgetSettings.WidgetMode1x1 mode1x1 = SuntimesWidgetSettings.load1x1ModePref(context, appWidgetId);
            views = new RemoteViews(context.getPackageName(), mode1x1.getLayoutID());

            //Intent intent = new Intent(context, SuntimesWidgetService.class);
            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            //intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            //views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_1x1);
            //views.setRemoteAdapter(R.id.view_flip, intent);
            //views.setEmptyView(R.id.view_flip, R.id.emptyView);
        }

        return views;
    }

    private static SuntimesWidgetSettings.Location getCurrentLocation(Context context)
    {
        return null;   // TODO
    }

    /**
     * @param context the application context
     * @param appWidgetManager widget manager
     * @param appWidgetId id of widget to be updated
     */
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int widgetRows = SuntimesWidgetUtils.getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        int widgetCols = SuntimesWidgetUtils.getCellsForSize(options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));

        RemoteViews views = getWidgetViews(context, appWidgetId, widgetRows, widgetCols);
        themeViews(context, views, appWidgetId);

        SuntimesWidgetData data = new SuntimesWidgetData(context, appWidgetId);
        data.calculate();
        updateViews(context, appWidgetId, views, data);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * @param context
     * @param views
     * @param appWidgetId
     */
    public static void themeViews(Context context, RemoteViews views, int appWidgetId)
    {
        SuntimesWidgetTheme theme = SuntimesWidgetSettings.loadThemePref(context, appWidgetId);
        themeViews(context, views, theme);
    }

    /**
     * @param context
     * @param views
     * @param theme
     */
    public static void themeViews(Context context, RemoteViews views, SuntimesWidgetTheme theme)
    {
        Resources resources = context.getResources();
        int[] padding = new int[] { (int)resources.getDimension(R.dimen.widget_padding_left),
                (int)resources.getDimension(R.dimen.widget_padding_top),
                (int)resources.getDimension(R.dimen.widget_padding_right),
                (int)resources.getDimension(R.dimen.widget_padding_bottom) };
        int sunriseColor = theme.getSunriseTextColor();
        int sunsetColor = theme.getSunsetTextColor();
        int suffixColor = theme.getThemeTimeSuffixColor();
        int textColor = theme.getTextColor();

        views.setInt(R.id.widgetframe_inner, "setBackgroundResource", theme.getBackgroundId());
        views.setViewPadding( R.id.widgetframe_inner, padding[0], padding[1], padding[2], padding[3] );

        views.setTextViewTextSize(R.id.text_title, TypedValue.COMPLEX_UNIT_SP, theme.getTitleSizeSp());
        views.setTextColor(R.id.text_title, theme.getTitleColor());

        views.setTextColor(R.id.text_time_sunrise_suffix, suffixColor);
        views.setTextColor(R.id.text_time_sunrise, sunriseColor);
        views.setTextColor(R.id.text_delta_sunrise, sunriseColor);

        views.setTextColor(R.id.text_time_sunset_suffix, suffixColor);
        views.setTextColor(R.id.text_time_sunset, sunsetColor);
        views.setTextColor(R.id.text_delta_sunset, sunsetColor);

        views.setTextColor(R.id.text_delta_day_prefix, textColor);
        views.setTextColor(R.id.text_delta_day_value, sunsetColor);
        views.setTextColor(R.id.text_delta_day_units, textColor);
        views.setTextColor(R.id.text_delta_day_suffix, suffixColor);
    }

    /**
     * @param context
     * @param appWidgetId
     * @param views
     * @param context
     */
    public static void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesWidgetData data)
    {
        boolean showTitle = SuntimesWidgetSettings.loadShowTitlePref(context, appWidgetId);
        String titlePattern = SuntimesWidgetSettings.loadTitleTextPref(context, appWidgetId);
        String titleText = SuntimesWidgetUtils.displayStringForTitlePattern(titlePattern, context, appWidgetId);
        views.setTextViewText(R.id.text_title, titleText);
        views.setViewVisibility(R.id.text_title, showTitle ? View.VISIBLE : View.GONE);

        // DEBUG (comment me)
        Log.v("DEBUG", "show title: " + showTitle);
        Log.v("DEBUG", "title text: " + titleText);

        // update sunrise time
        SuntimesWidgetUtils.TimeDisplayText sunriseString = SuntimesWidgetUtils.calendarTimeShortDisplayString(context, data.sunriseCalendarToday());
        views.setTextViewText(R.id.text_time_sunrise, sunriseString.getValue());
        views.setTextViewText(R.id.text_time_sunrise_suffix, sunriseString.getSuffix());

        // upset sunset time
        SuntimesWidgetUtils.TimeDisplayText sunsetString = SuntimesWidgetUtils.calendarTimeShortDisplayString(context, data.sunsetCalendarToday());
        views.setTextViewText(R.id.text_time_sunset, sunsetString.getValue());
        views.setTextViewText(R.id.text_time_sunset_suffix, sunsetString.getSuffix());

        // update sunrise delta
        //String sunriseDeltaString = calendarDeltaShortDisplayString(sunriseCalendarToday, sunriseCalendarTomorrow);
        //views.setTextViewText(R.id.text_delta_sunrise, sunriseDeltaString);

        // update sunset delta
        //String sunsetDeltaString = calendarDeltaShortDisplayString(sunsetCalendarToday, sunsetCalendarTomorrow);
        //views.setTextViewText(R.id.text_delta_sunset, sunsetDeltaString);

        // update day delta
        SuntimesWidgetUtils.TimeDisplayText dayDeltaDisplay = SuntimesWidgetUtils.timeDeltaLongDisplayString(data.dayLengthToday(), data.dayLengthOther());
        String dayDeltaValue = dayDeltaDisplay.getValue();
        String dayDeltaUnits = dayDeltaDisplay.getUnits();
        String dayDeltaSuffix = dayDeltaDisplay.getSuffix();

        views.setTextViewText(R.id.text_delta_day_prefix, data.dayDeltaPrefix());
        views.setTextViewText(R.id.text_delta_day_value, dayDeltaValue);
        views.setTextViewText(R.id.text_delta_day_units, dayDeltaUnits);
        views.setTextViewText(R.id.text_delta_day_suffix, dayDeltaSuffix);

        views.setViewVisibility(R.id.text_delta_day_units, (dayDeltaUnits.trim().equals("") ? View.GONE : View.VISIBLE));
        views.setViewVisibility(R.id.text_delta_day_suffix, (dayDeltaSuffix.trim().equals("") ? View.GONE : View.VISIBLE));

        views.setViewVisibility(R.id.text_title, ((showTitle) ? View.VISIBLE : View.GONE));
    }
}


