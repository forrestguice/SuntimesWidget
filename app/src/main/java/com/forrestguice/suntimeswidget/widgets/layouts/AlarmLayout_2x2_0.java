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

package com.forrestguice.suntimeswidget.widgets.layouts;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.widgets.AlarmWidgetService;
import com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings;

import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetService.AlarmWidgetItemViewFactory.EXTRA_APPWIDGETID;

public class AlarmLayout_2x2_0 extends AlarmLayout_1x1_0
{
    public AlarmLayout_2x2_0() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_alarm_2x2_0;
    }

    @Override
    protected int chooseLayout(int position)
    {
        switch (position)
        {
            case 0: return R.layout.layout_widget_alarm_2x2_0_align_fill;
            case 1: return R.layout.layout_widget_alarm_2x2_0_align_float_1;
            case 2: return R.layout.layout_widget_alarm_2x2_0_align_float_2;
            case 3: return R.layout.layout_widget_alarm_2x2_0_align_float_3;
            case 4: return R.layout.layout_widget_alarm_2x2_0_align_float_4;
            case 6: return R.layout.layout_widget_alarm_2x2_0_align_float_6;
            case 7: return R.layout.layout_widget_alarm_2x2_0_align_float_7;
            case 8: return R.layout.layout_widget_alarm_2x2_0_align_float_8;
            case 9: return R.layout.layout_widget_alarm_2x2_0_align_float_9;
            case 5: default: return R.layout.layout_widget_alarm_2x2_0;
        }
    }

    @Override
    public void updateViews(final Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        views.setViewVisibility(R.id.text_label, (showLabels ? View.VISIBLE : View.GONE));
        views.setEmptyView(R.id.list_alarms, R.id.text_empty);
        views.setRemoteAdapter(R.id.list_alarms, getRemoteAdapterIntent(context, appWidgetId));
    }

    protected Intent getRemoteAdapterIntent(Context context, int appWidgetId)
    {
        Intent intent = new Intent(context, AlarmWidgetService.class);
        intent.putExtra(EXTRA_APPWIDGETID, appWidgetId);
        intent.putExtra(AlarmWidgetService.AlarmWidgetItemViewFactory.EXTRA_LAYOUTMODE, AlarmWidgetSettings.MODE_2x2);
        intent.setAction(appWidgetId + "_" + AlarmWidgetSettings.MODE_2x2);  // set action so Intent has a unique hashcode (RemoteViews are cached by Intent)
        return intent;
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        views.setTextColor(R.id.text_empty, timeColor);
    }

}
