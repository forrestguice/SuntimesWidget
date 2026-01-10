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
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.calculator.SuntimesClockData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.widgets.AlarmWidget0;
import com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings;

import java.util.Set;

public class AlarmLayout_2x2_1 extends AlarmLayout
{
    public AlarmLayout_2x2_1() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_alarm_2x2_1;
    }

    @Override
    protected int chooseLayout(int position)
    {
        switch (position)
        {
            case 0: return R.layout.layout_widget_alarm_2x2_1_align_fill;
            case 1: return R.layout.layout_widget_alarm_2x2_1_align_float_1;
            case 2: return R.layout.layout_widget_alarm_2x2_1_align_float_2;
            case 3: return R.layout.layout_widget_alarm_2x2_1_align_float_3;
            case 4: return R.layout.layout_widget_alarm_2x2_1_align_float_4;
            case 6: return R.layout.layout_widget_alarm_2x2_1_align_float_6;
            case 7: return R.layout.layout_widget_alarm_2x2_1_align_float_7;
            case 8: return R.layout.layout_widget_alarm_2x2_1_align_float_8;
            case 9: return R.layout.layout_widget_alarm_2x2_1_align_float_9;
            case 5: default: return R.layout.layout_widget_alarm_2x2_1;
        }
    }

    @Override
    public void updateViews(final Context context, int appWidgetId, RemoteViews views, SuntimesClockData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        String displayString = "";
        Set<String> types = AlarmWidgetSettings.loadAlarmWidgetStringSet(context, appWidgetId, AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_TYPES, AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_TYPES);
        Long upcomingAlarmId = AlarmWidget0.findUpcomingAlarmId(context, data.now().getTimeInMillis(), types.toArray(new String[0]));
        if (upcomingAlarmId == null || upcomingAlarmId == -1) {
            displayString = context.getString(R.string.configLabel_alarms_nextAlarm_none);

        } else {
            AlarmClockItem item = AlarmWidget0.loadAlarmClockItem(context, upcomingAlarmId);
            if (item != null)
            {
                displayString = formatTimeDisplayString(context, views, appWidgetId, data, item);
                updateIconView(context, views, appWidgetId, item);
                updateLabelViews(context, views, item);
                updateTimeUntilView(context, views, item);
                updateNoteView(context, views, item);

            } else {
                displayString = context.getString(R.string.configLabel_alarms_nextAlarm_error);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            if (WidgetSettings.loadScaleTextPref(context, appWidgetId))
            {
                /*int showTitle = (WidgetSettings.loadShowTitlePref(context, appWidgetId) ? 1 : 0);
                int[] maxDp = new int[] { maxDimensionsDp[0] - (paddingDp[0] + paddingDp[2]), (maxDimensionsDp[1] - (paddingDp[1] + paddingDp[3]) - ((int)titleSizeSp * showTitle)) };

                String s = (displayString.length() <= 3 ? "0:00" : displayString);
                float adjustedSizeSp = adjustTextSize(context, maxDp, "sans-serif", boldTime, s, timeSizeSp, AlarmLayout.MAX_SP, true);

                if (adjustedSizeSp != timeSizeSp) {
                    views.setTextViewTextSize(android.R.id.text2, TypedValue.COMPLEX_UNIT_DIP, adjustedSizeSp);
                }*/
            }
        }

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        views.setViewVisibility(R.id.text_label, (showLabels ? View.VISIBLE : View.GONE));
        views.setTextViewText(android.R.id.text2, (boldTime ? SuntimesUtils.createBoldSpan(null, displayString, displayString) : displayString));
    }

}
