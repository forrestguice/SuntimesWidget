/**
   Copyright (C) 2017 Forrest Guice
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
import android.text.SpannableString;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesUtils.TimeDisplayText;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * A 1x1 layout that displays either a solstice or equinox datetime.
 */
public class SuntimesLayout_1x1eq_0 extends SuntimesLayoutEq
{
    public SuntimesLayout_1x1eq_0()
    {
        super();
    }

    public SuntimesLayout_1x1eq_0(int layoutID)
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_1x1eq_0;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesEquinoxSolsticeData data)
    {
        super.updateViews(context, appWidgetId, views, data);

        if (data != null && data.isCalculated())
        {
            timeMode = data.timeMode();
            Calendar now = Calendar.getInstance();
            Calendar event = data.eventCalendarUpcoming();

            TimeDisplayText eventString = utils.calendarDateTimeDisplayString(context, event);
            views.setTextViewText(R.id.text_time_event, eventString.getValue());

            int noteStringId = R.string.hence;
            if (event.before(now))
            {
                noteStringId = R.string.ago;
            }

            String noteTime = utils.timeDeltaDisplayString(now.getTime(), event.getTime()).toString();
            String noteString = context.getString(noteStringId, noteTime);
            SpannableString noteSpan = SuntimesUtils.createBoldColorSpan(noteString, noteTime, timeColor);
            views.setTextViewText(R.id.text_time_event_note, noteSpan);

        } else {
            views.setTextViewText(R.id.text_time_event, "");
            views.setTextViewText(R.id.text_time_event_note, "");
        }
    }

    private WidgetSettings.SolsticeEquinoxMode timeMode = WidgetSettings.SolsticeEquinoxMode.EQUINOX_VERNAL;
    private int timeColor = Color.WHITE;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        timeColor = theme.getTimeColor();
        int textColor = theme.getTextColor();
        int eventColor = theme.getSeasonColor(timeMode);

        views.setTextColor(R.id.text_time_event_note, textColor);
        views.setTextColor(R.id.text_time_event, eventColor);
    }
}
