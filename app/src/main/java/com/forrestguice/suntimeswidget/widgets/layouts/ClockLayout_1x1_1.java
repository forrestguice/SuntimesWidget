/**
   Copyright (C) 2025 Forrest Guice
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
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClockLayout_1x1_1 extends ClockLayout_1x1_0
{
    public ClockLayout_1x1_1() {
        super();
    }

    @Override
    public void initLayoutID() {
        this.layoutID = R.layout.layout_widget_clock_1x1_1;
    }

    @Override
    protected int chooseLayout(int position)
    {
        switch (position) {
            case 0: return R.layout.layout_widget_clock_1x1_1_align_fill;                       // fill
            case 1: case 2: case 3: return R.layout.layout_widget_clock_1x1_1_align_float_2;    // top
            case 7: case 8: case 9: return R.layout.layout_widget_clock_1x1_1_align_float_8;    // bottom
            case 4: case 6: case 5: default: return R.layout.layout_widget_clock_1x1_1;         // center
        }
    }

    @Override
    protected void updateTimeViews(Context context, int appWidgetId, RemoteViews views, Calendar now)
    {
        SimpleDateFormat hourFormat = (is24(context, appWidgetId) ? hourFormat24 : hourFormat12);
        String nowString = hourFormat.format(now.getTime()) + "\n" + minuteFormat.format(now.getTime());
        views.setTextViewText(R.id.text_time, nowString);
        views.setTextViewText(R.id.text_time_suffix, "");
    }

    protected boolean is24(Context context, int appWidgetId)
    {
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);
        switch (timeFormat)
        {
            case MODE_SUNTIMES: return SuntimesUtils.is24();
            case MODE_SYSTEM: return android.text.format.DateFormat.is24HourFormat(context);
            case MODE_12HR: return false;
            case MODE_24HR: default: return true;
        }
    }

    protected SimpleDateFormat hourFormat12 = new SimpleDateFormat("h", SuntimesUtils.getLocale());
    protected SimpleDateFormat hourFormat24 = new SimpleDateFormat("HH", SuntimesUtils.getLocale());
    protected SimpleDateFormat minuteFormat = new SimpleDateFormat("mm", SuntimesUtils.getLocale());
}
