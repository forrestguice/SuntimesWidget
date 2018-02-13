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
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * Moon Primary Phases (3x1)
 */
public class MoonLayout_3x1_0 extends MoonLayout
{
    public MoonLayout_3x1_0()
    {
        super();
    }

    public MoonLayout_3x1_0(int layoutID)
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_3x1_0;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);

        SuntimesUtils.TimeDisplayText newMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.NEW), showSeconds);
        views.setTextViewText(R.id.moonphase_new_date, newMoonString.getValue());

        SuntimesUtils.TimeDisplayText firstQuarterMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FIRST_QUARTER), showSeconds);
        views.setTextViewText(R.id.moonphase_firstquarter_date, firstQuarterMoonString.getValue());

        SuntimesUtils.TimeDisplayText fullMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FULL), showSeconds);
        views.setTextViewText(R.id.moonphase_full_date, fullMoonString.getValue());

        SuntimesUtils.TimeDisplayText thirdQuarterMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.THIRD_QUARTER), showSeconds);
        views.setTextViewText(R.id.moonphase_thirdquarter_date, thirdQuarterMoonString.getValue());
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        int timeColor = theme.getTimeColor();
        views.setTextColor(R.id.moonphase_new_date, timeColor);
        views.setTextColor(R.id.moonphase_firstquarter_date, timeColor);
        views.setTextColor(R.id.moonphase_full_date, timeColor);
        views.setTextColor(R.id.moonphase_thirdquarter_date, timeColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.moonphase_new_date, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.moonphase_firstquarter_date, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.moonphase_full_date, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.moonphase_thirdquarter_date, TypedValue.COMPLEX_UNIT_SP, timeSize);
        }
    }

    @Override
    public void prepareForUpdate(SuntimesMoonData data)
    {
        Calendar midnight = data.midnight();
        SuntimesCalculator.MoonPhase nextPhase = data.nextPhase(midnight);
        switch (nextPhase)
        {
            case THIRD_QUARTER:
                this.layoutID = R.layout.layout_widget_moon_3x1_03;
                break;
                
            case FULL:
                this.layoutID = R.layout.layout_widget_moon_3x1_02;
                break;

            case FIRST_QUARTER:
                this.layoutID = R.layout.layout_widget_moon_3x1_01;
                break;

            case NEW:
            default:
                this.layoutID = R.layout.layout_widget_moon_3x1_0;
                break;
        }
    }
}

