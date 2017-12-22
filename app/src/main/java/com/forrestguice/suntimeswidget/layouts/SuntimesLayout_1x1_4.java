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
import android.graphics.Bitmap;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.SuntimesUtils.TimeDisplayText;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

/**
 * A 1x1 layout that displays only the sunset time.
 */
public class SuntimesLayout_1x1_4 extends SuntimesLayout
{
    public SuntimesLayout_1x1_4()
    {
        super();
    }

    public SuntimesLayout_1x1_4(int layoutID )
    {
        this.layoutID = layoutID;
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_1x1_4;
    }


    @Override
    public void updateViews(Context context, int appWidgetId, RemoteViews views, SuntimesRiseSetData data)
    {
        super.updateViews(context, appWidgetId, views, data);
        boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, appWidgetId);
        TimeDisplayText noonString = utils.calendarTimeShortDisplayString(context, data.sunsetCalendarToday(), showSeconds);
        views.setTextViewText(R.id.text_time_noon, noonString.getValue());
        views.setTextViewText(R.id.text_time_noon_suffix, noonString.getSuffix());
    }

    @Override
    public void themeViews(Context context, RemoteViews views, SuntimesTheme theme)
    {
        super.themeViews(context, views, theme);

        int noonColor = theme.getNoonTextColor();
        int suffixColor = theme.getTimeSuffixColor();
        views.setTextColor(R.id.text_time_noon_suffix, suffixColor);
        views.setTextColor(R.id.text_time_noon, noonColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            views.setTextViewTextSize(R.id.text_time_noon_suffix, TypedValue.COMPLEX_UNIT_SP, theme.getTimeSuffixSizeSp());
            views.setTextViewTextSize(R.id.text_time_noon, TypedValue.COMPLEX_UNIT_SP, theme.getTimeSizeSp());
        }

        Bitmap noonIcon = SuntimesUtils.gradientDrawableToBitmap(context, R.drawable.ic_noon_large0, theme.getNoonIconColor(), theme.getNoonIconStrokeColor(), theme.getNoonIconStrokePixels(context));
        views.setImageViewBitmap(R.id.icon_time_noon, noonIcon);
    }
}
