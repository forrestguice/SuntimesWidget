/**
    Copyright (C) 2024 Forrest Guice
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
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.themes.WidgetThemeConfigActivity;
import com.forrestguice.suntimeswidget.widgets.layouts.AlarmLayout;

/**
 * Alarm widget config activity.
 */
public class AlarmWidget0ConfigActivity_3x2 extends AlarmWidget0ConfigActivity
{
    public AlarmWidget0ConfigActivity_3x2() {
        super();
    }

    @Override
    protected Class<?> getWidgetClass() {
        return AlarmWidget0_3x2.class;
    }

    @Override
    protected void initViews( Context context ) {
        super.initViews(context);
    }

    @Override
    protected void updateWidgets(Context context, int[] appWidgetIds)
    {
        Intent updateIntent = new Intent(context, getWidgetClass());
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);
    }

    @Override
    protected Intent themeEditorIntent(Context context)
    {
        Intent intent = super.themeEditorIntent(context);
        intent.putExtra(WidgetThemeConfigActivity.PARAM_PREVIEWID, WidgetThemeConfigActivity.PREVIEWID_ALARM_3x2);
        return intent;
    }

    @Override
    protected AlarmLayout defaultAlarmLayout(Context context, int appWidgetId) {
        return AlarmWidgetSettings.loadAlarm3x2ModePref_asLayout(context, appWidgetId);
    }

    @Override
    protected String getPrimaryWidgetModeSize() {
        return SIZE_3x2;
    }

    @Override
    protected TextView getPrimaryWidgetModeLabel() {
        return label_3x2mode;
    }

    @Override
    protected View[] getPrimaryWidgetModeViews() {
        return new View[] { label_3x2mode, spinner_3x2mode };
    }

    @Override
    protected View[] getSecondaryWidgetModeViews() {
        return new View[] { label_1x1mode, spinner_1x1mode, label_2x2mode, spinner_2x2mode };
    }

}
