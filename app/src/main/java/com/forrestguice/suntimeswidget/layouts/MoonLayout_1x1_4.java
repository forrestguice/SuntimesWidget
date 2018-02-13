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

    public MoonLayout_1x1_4(int layoutID)
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_4;
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);

        for (MoonPhaseDisplay moonPhase : MoonPhaseDisplay.values())
        {
            views.setViewVisibility(moonPhase.getView(), View.GONE);
        }

        SuntimesCalculator.MoonPhase majorPhase = data.getMoonPhaseNext();
        if (majorPhase != null)
        {
            MoonPhaseDisplay nextPhase = SuntimesMoonData.toPhase(majorPhase);
            views.setViewVisibility(nextPhase.getView(), View.VISIBLE);
            //views.setTextViewText(R.id.text_info_moonphase, phase.getLongDisplayString());

            SuntimesUtils.TimeDisplayText phaseString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(majorPhase), showSeconds);
            views.setTextViewText(R.id.moonphase_major_date, phaseString.getValue());
        }
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        int timeColor = theme.getTimeColor();
        views.setTextColor(R.id.moonphase_major_date, timeColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.moonphase_major_date, TypedValue.COMPLEX_UNIT_SP, timeSize);
        }
    }

    @Override
    public void prepareForUpdate(SuntimesMoonData data)
    {
        // EMPTY
    }
}

