/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.equinox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

public class EquinoxViewOptions
{
    public boolean isRtl = false;
    public boolean minimized = false;
    public boolean centered = false;
    public int columnWidthPx = -1;
    public int highlightPosition = -1;

    public WidgetSettings.TrackingMode trackingMode = WidgetSettings.TrackingMode.SOONEST;
    public boolean showSeconds = false;
    public boolean showDate = true;

    public int titleColor, noteColor, disabledColor, pressedColor;
    public EquinoxColorValues colors = new EquinoxColorValues();
    //public Integer[] seasonColors = new Integer[4];
    public Integer labelColor, textColor;
    public int resID_buttonPressColor;

    public Float timeSizeSp = null;
    public Float titleSizeSp = null;
    public boolean titleBold = false;

    public SuntimesTheme themeOverride = null;

    @SuppressLint("ResourceType")
    public void init(Context context)
    {
        colors = new EquinoxColorValues(context, true);

        int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.text_disabledColor, R.attr.buttonPressColor,
                             R.attr.table_springColor, R.attr.table_summerColor, R.attr.table_fallColor, R.attr.table_winterColor };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        textColor = labelColor = titleColor = noteColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.transparent));
        disabledColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.text_disabled_dark));
        resID_buttonPressColor = typedArray.getResourceId(2, R.color.btn_tint_pressed_dark);
        pressedColor = ContextCompat.getColor(context, resID_buttonPressColor);
        //seasonColors[0] = ContextCompat.getColor(context, typedArray.getResourceId(3, R.color.springColor_dark));
        //seasonColors[1] = ContextCompat.getColor(context, typedArray.getResourceId(4, R.color.summerColor_dark));
        //seasonColors[2] = ContextCompat.getColor(context, typedArray.getResourceId(5, R.color.fallColor_dark));
        //seasonColors[3] =  ContextCompat.getColor(context, typedArray.getResourceId(6, R.color.winterColor_dark));
        titleSizeSp = timeSizeSp = null;
        typedArray.recycle();
    }

    @Deprecated
    public void init(SuntimesTheme theme)
    {
        if (theme != null)
        {
            titleColor = theme.getTitleColor();
            noteColor = theme.getTimeColor();
            labelColor = theme.getTitleColor();
            textColor = theme.getTextColor();
            pressedColor = theme.getActionColor();
            colors.setColor(EquinoxColorValues.COLOR_SPRING_TEXT, theme.getSpringColor());
            colors.setColor(EquinoxColorValues.COLOR_SUMMER_TEXT, theme.getSummerColor());
            colors.setColor(EquinoxColorValues.COLOR_AUTUMN_TEXT, theme.getFallColor());
            colors.setColor(EquinoxColorValues.COLOR_WINTER_TEXT, theme.getWinterColor());
            timeSizeSp = theme.getTimeSizeSp();
            titleSizeSp = theme.getTitleSizeSp();
            titleBold = theme.getTitleBold();
        }
    }

    public int getColorForMode(WidgetSettings.SolsticeEquinoxMode mode)
    {
        switch (mode) {
            case CROSS_WINTER: case SOLSTICE_WINTER: return colors.getColor(EquinoxColorValues.COLOR_WINTER_TEXT);
            case CROSS_AUTUMN: case EQUINOX_AUTUMNAL: return colors.getColor(EquinoxColorValues.COLOR_AUTUMN_TEXT);
            case CROSS_SUMMER: case SOLSTICE_SUMMER: return colors.getColor(EquinoxColorValues.COLOR_SUMMER_TEXT);
            case CROSS_SPRING: case EQUINOX_SPRING:
            default: return colors.getColor(EquinoxColorValues.COLOR_SPRING_TEXT);
        }
    }
}