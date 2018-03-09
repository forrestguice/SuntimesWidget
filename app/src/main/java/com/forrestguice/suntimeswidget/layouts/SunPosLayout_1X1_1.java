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
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculator;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

/**
 * A 1x1 layout that displays right ascension and declination.
 */
public class SunPosLayout_1X1_1 extends SunPosLayout
{
    public SunPosLayout_1X1_1()
    {
        super();
    }

    public SunPosLayout_1X1_1(int layoutID )
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_1x1_6;
    }

    @Override
    public void prepareForUpdate(SuntimesRiseSetDataset dataset)
    {
        dataset.dataActual.initCalculator();  // init calculator only; skipping full calculate()
    }

    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetDataset dataset)
    {
        super.updateViews(context, appWidgetId, views, dataset);
        SuntimesCalculator calculator = dataset.dataActual.calculator();
        SuntimesCalculator.SunPosition sunPosition = calculator.getSunPosition(dataset.now());

        SuntimesUtils.TimeDisplayText rightAscDisplay = utils.formatAsDirection2(sunPosition.rightAscension, 1);
        String rightAscString = utils.formatAsDirection(rightAscDisplay.getValue(), rightAscDisplay.getSuffix());

        SpannableString rightAsc = SuntimesUtils.createColorSpan(null, rightAscString, rightAscDisplay.getValue(), highlightColor, boldTime);
        rightAsc = SuntimesUtils.createBoldColorSpan(rightAsc, rightAscString, rightAscDisplay.getSuffix(), suffixColor);
        rightAsc = SuntimesUtils.createRelativeSpan(rightAsc, rightAscString, rightAscDisplay.getSuffix(), 0.7f);
        views.setTextViewText(R.id.info_sun_rightascension_current, rightAsc);

        String declinationString = utils.formatAsDegrees(sunPosition.declination, 1);
        CharSequence declination = SuntimesUtils.createColorSpan(null, declinationString, declinationString, highlightColor, boldTime);
        views.setTextViewText(R.id.info_sun_declination_current, declination);

        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId);
        int visibility = (showLabels ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.info_sun_rightascension_current_label, visibility);
        views.setViewVisibility(R.id.info_sun_declination_current_label, visibility);
    }

    protected int highlightColor = Color.WHITE;
    protected boolean boldTime = false;
    protected float suffixSp;
    protected int suffixColor = Color.GRAY;

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);
        highlightColor = theme.getTimeColor();
        boldTime = theme.getTimeBold();
        suffixSp = theme.getTimeSuffixSizeSp();
        suffixColor = theme.getTimeSuffixColor();

        int textColor = theme.getTextColor();
        views.setTextColor(R.id.info_sun_rightascension_current_label, textColor);
        views.setTextColor(R.id.info_sun_declination_current_label, textColor);
        views.setTextColor(R.id.info_sun_rightascension_current, textColor);
        views.setTextColor(R.id.info_sun_declination_current, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            float textSize = theme.getTextSizeSp();
            views.setTextViewTextSize(R.id.info_sun_rightascension_current_label, TypedValue.COMPLEX_UNIT_SP, textSize);
            views.setTextViewTextSize(R.id.info_sun_declination_current_label, TypedValue.COMPLEX_UNIT_SP, textSize);

            float timeSize = theme.getTimeSizeSp();
            views.setTextViewTextSize(R.id.info_sun_rightascension_current, TypedValue.COMPLEX_UNIT_SP, timeSize);
            views.setTextViewTextSize(R.id.info_sun_declination_current, TypedValue.COMPLEX_UNIT_SP, timeSize);
        }
    }
}
