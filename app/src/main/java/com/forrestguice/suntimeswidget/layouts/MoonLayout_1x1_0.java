/**
   Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.layouts;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public class MoonLayout_1x1_0 extends MoonLayout
{
    public MoonLayout_1x1_0()
    {
        super();
    }

    public MoonLayout_1x1_0(int layoutID)
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_1x1eq_0;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
    }

    private int timeColor = Color.WHITE;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        timeColor = theme.getTimeColor();
        int textColor = theme.getTextColor();
        //views.setTextColor(R.id.text_time_event_note, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            float timeSize = theme.getTimeSizeSp();

            //views.setTextViewTextSize(R.id.text_time_event_note, TypedValue.COMPLEX_UNIT_SP, textSize);
            //views.setTextViewTextSize(R.id.text_time_event, TypedValue.COMPLEX_UNIT_SP, timeSize);
        }
    }

    @Override
    public void prepareForUpdate(SuntimesMoonData data)
    {
        // EMPTY
    }
}
