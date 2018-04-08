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
import android.text.SpannableString;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public abstract class PositionLayout extends SuntimesLayout
{
    protected static final int DECIMAL_PLACES = 1;
    protected static final float SYMBOL_RELATIVE_SIZE = 0.7f;

    protected static final SuntimesUtils utils = new SuntimesUtils();


    protected int highlightColor = Color.WHITE;
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
    }

    protected SpannableString styleAzimuthText(SuntimesUtils.TimeDisplayText azimuthDisplay, int color)
    {
        String azimuthSymbol = azimuthDisplay.getSuffix();
        String azimuthString = utils.formatAsDirection(azimuthDisplay.getValue(), azimuthSymbol);
        SpannableString azimuth = SuntimesUtils.createColorSpan(null, azimuthString, azimuthDisplay.getValue(), color, boldTime);
        azimuth = SuntimesUtils.createBoldColorSpan(azimuth, azimuthString, azimuthSymbol, suffixColor);
        azimuth = SuntimesUtils.createRelativeSpan(azimuth, azimuthString, azimuthSymbol, SYMBOL_RELATIVE_SIZE);
        //azimuth = SuntimesUtils.createAbsoluteSpan(azimuth, azimuthString, azimuthDisplay.getSuffix(), SuntimesUtils.spToPixels(context, suffixSp));
        return azimuth;
    }

    protected SpannableString styleElevationText(double value, int color)
    {
        SuntimesUtils.TimeDisplayText elevationDisplay = utils.formatAsElevation(value, DECIMAL_PLACES);
        String elevationSymbol = elevationDisplay.getSuffix();
        String elevationString = utils.formatAsElevation(elevationDisplay.getValue(), elevationSymbol);
        SpannableString elevation = SuntimesUtils.createColorSpan(null, elevationString, elevationString, color, boldTime);
        elevation = SuntimesUtils.createBoldColorSpan(elevation, elevationString, elevationSymbol, suffixColor);
        elevation = SuntimesUtils.createRelativeSpan(elevation, elevationString, elevationSymbol, SYMBOL_RELATIVE_SIZE);
        return elevation;
    }

}
