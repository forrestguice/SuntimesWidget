/**
   Copyright (C) 2018-2022 Forrest Guice
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
import android.graphics.Bitmap;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
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

    /**public MoonLayout_3x1_0(int layoutID)
    {
        this.layoutID = layoutID;
    }*/

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_3x1_0;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesMoonData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);

        Calendar midnight = data.midnight();
        SuntimesCalculator.MoonPhase nextPhase = data.nextPhase(midnight);

        int position = scaleBase ? 0 : WidgetSettings.loadWidgetGravityPref(context, appWidgetId);
        this.layoutID = chooseLayout(nextPhase, position);
        /*switch (nextPhase)
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
        }*/
    }

    protected int chooseLayout(SuntimesCalculator.MoonPhase nextPhase, int position)
    {
        switch (nextPhase)
        {
            case THIRD_QUARTER:
                switch (position)
                {
                    case 0: return R.layout.layout_widget_moon_3x1_03_align_fill;
                    case 1: case 2: case 3: return R.layout.layout_widget_moon_3x1_03_align_float_2;
                    case 7: case 8: case 9: return R.layout.layout_widget_moon_3x1_03_align_float_8;
                    case 5: default: return R.layout.layout_widget_moon_3x1_03;
                }

            case FULL:
                switch (position) {
                    case 0: return R.layout.layout_widget_moon_3x1_02_align_fill;
                    case 1: case 2: case 3: return R.layout.layout_widget_moon_3x1_02_align_float_2;
                    case 7: case 8: case 9: return R.layout.layout_widget_moon_3x1_02_align_float_8;
                    case 5: default: return R.layout.layout_widget_moon_3x1_02;
                }

            case FIRST_QUARTER:
                switch (position) {
                    case 0: return R.layout.layout_widget_moon_3x1_01_align_fill;
                    case 1: case 2: case 3: return R.layout.layout_widget_moon_3x1_01_align_float_2;
                    case 7: case 8: case 9: return R.layout.layout_widget_moon_3x1_01_align_float_8;
                    case 5: default: return R.layout.layout_widget_moon_3x1_01;
                }

            case NEW:
            default:
                switch (position) {
                    case 0: return R.layout.layout_widget_moon_3x1_0_align_fill;
                    case 1: case 2: case 3: return R.layout.layout_widget_moon_3x1_0_align_float_2;
                    case 7: case 8: case 9: return R.layout.layout_widget_moon_3x1_0_align_float_8;
                    case 5: default: return R.layout.layout_widget_moon_3x1_0;
                }
        }
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesMoonData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        boolean showTimeDate = WidgetSettings.loadShowTimeDatePref(context, appWidgetId);
        boolean abbreviate = WidgetSettings.loadShowAbbrMonthPref(context, appWidgetId);
        WidgetSettings.TimeFormatMode timeFormat = WidgetSettings.loadTimeFormatModePref(context, appWidgetId);

        SuntimesUtils.TimeDisplayText newMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.NEW), showTimeDate, showSeconds, abbreviate, timeFormat);
        views.setTextViewText(R.id.moonphase_new_date, newMoonString.getValue());
        views.setTextViewText(R.id.moonphase_new_label, data.getMoonPhaseLabel(context, SuntimesCalculator.MoonPhase.NEW));
        views.setViewVisibility(R.id.moonphase_new_label, (showLabels ? View.VISIBLE : View.GONE));

        SuntimesUtils.TimeDisplayText firstQuarterMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FIRST_QUARTER), showTimeDate, showSeconds, abbreviate, timeFormat);
        views.setTextViewText(R.id.moonphase_firstquarter_date, firstQuarterMoonString.getValue());
        views.setViewVisibility(R.id.moonphase_firstquarter_label, (showLabels ? View.VISIBLE : View.GONE));

        SuntimesUtils.TimeDisplayText fullMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.FULL), showTimeDate, showSeconds, abbreviate, timeFormat);
        views.setTextViewText(R.id.moonphase_full_date, fullMoonString.getValue());
        views.setTextViewText(R.id.moonphase_full_label, data.getMoonPhaseLabel(context, SuntimesCalculator.MoonPhase.FULL));
        views.setViewVisibility(R.id.moonphase_full_label, (showLabels ? View.VISIBLE : View.GONE));

        SuntimesUtils.TimeDisplayText thirdQuarterMoonString = utils.calendarDateTimeDisplayString(context, data.moonPhaseCalendar(SuntimesCalculator.MoonPhase.THIRD_QUARTER), showTimeDate, showSeconds, abbreviate, timeFormat);
        views.setTextViewText(R.id.moonphase_thirdquarter_date, thirdQuarterMoonString.getValue());
        views.setViewVisibility(R.id.moonphase_thirdquarter_label, (showLabels ? View.VISIBLE : View.GONE));
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

        int textColor = theme.getTextColor();
        views.setTextColor(R.id.moonphase_new_label, textColor);
        views.setTextColor(R.id.moonphase_firstquarter_label, textColor);
        views.setTextColor(R.id.moonphase_full_label, textColor);
        views.setTextColor(R.id.moonphase_thirdquarter_label, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.moonphase_new_date, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.moonphase_firstquarter_date, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.moonphase_full_date, TypedValue.COMPLEX_UNIT_DIP, timeSize);
            views.setTextViewTextSize(R.id.moonphase_thirdquarter_date, TypedValue.COMPLEX_UNIT_DIP, timeSize);

            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.moonphase_new_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.moonphase_firstquarter_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.moonphase_full_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
            views.setTextViewTextSize(R.id.moonphase_thirdquarter_label, TypedValue.COMPLEX_UNIT_DIP, textSize);
        }

        int colorWaxing = theme.getMoonWaxingColor();
        int colorWaning = theme.getMoonWaningColor();
        int colorFull = theme.getMoonFullColor();
        int colorNew = theme.getMoonNewColor();

        Bitmap fullMoon =  SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.FULL.getIcon(northward), colorFull, colorWaning, theme.getMoonFullStrokePixels(context));
        views.setImageViewBitmap(R.id.moonphase_full_icon, fullMoon);

        Bitmap newMoon =  SuntimesUtils.gradientDrawableToBitmap(context, MoonPhaseDisplay.NEW.getIcon(northward), colorNew, colorWaxing, theme.getMoonNewStrokePixels(context));
        views.setImageViewBitmap(R.id.moonphase_new_icon, newMoon);

        Bitmap waxingQuarter = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.FIRST_QUARTER.getIcon(northward), colorWaxing, colorWaxing, 0);
        views.setImageViewBitmap(R.id.moonphase_firstquarter_icon, waxingQuarter);

        Bitmap waningQuarter = SuntimesUtils.layerDrawableToBitmap(context, MoonPhaseDisplay.THIRD_QUARTER.getIcon(northward), colorWaning, colorWaning, 0);
        views.setImageViewBitmap(R.id.moonphase_thirdquarter_icon, waningQuarter);
    }

}

