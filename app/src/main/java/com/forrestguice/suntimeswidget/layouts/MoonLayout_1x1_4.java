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
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

/**
 * Moon Primary Phase (1x1)
 */
public class MoonLayout_1x1_4 extends MoonLayout
{
    public MoonLayout_1x1_4()
    {
        super();
    }

    /**public MoonLayout_1x1_4(int layoutID)
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_4;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        boolean showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);

        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values())
        {
            views.setViewVisibility(moonPhase.getView(), View.GONE);
        }

        SuntimesCalculator.MoonPhase majorPhase = data.getMoonPhaseNext();
        if (majorPhase != null)
        {
            MoonPhaseDisplay nextPhase = SuntimesMoonData.toPhase(majorPhase);
            views.setViewVisibility(nextPhase.getView(), View.VISIBLE);

            SuntimesUtils.TimeDisplayText phaseString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(majorPhase), showTimeDate, showSeconds);
            views.setTextViewText(R.id.moonphase_major_date, phaseString.getValue());
            views.setTextViewText(R.id.moonphase_major_label, nextPhase.getLongDisplayString());
            views.setViewVisibility(R.id.moonphase_major_label, (showLabels ? View.VISIBLE : View.GONE));
        }
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        themeViewsMoonPhase(context, views, theme);
        themeViewsMoonPhaseIcons(context, views, theme);

        int timeColor = theme.getTimeColor();
        views.setTextColor(R.id.moonphase_major_date, timeColor);

        int textColor = theme.getTextColor();
        views.setTextColor(R.id.moonphase_major_label, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.moonphase_major_date, TypedValue.COMPLEX_UNIT_SP, timeSize);

            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.moonphase_major_label, TypedValue.COMPLEX_UNIT_SP, textSize);
        }
    }

    /**@Override
    public void prepareForUpdate(SuntimesMoonData data)
    {
        // EMPTY
    }*/
}

