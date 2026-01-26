/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.widgets;

import android.content.Context;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * ClockWidget0ConfigActivity3x1 .. the 3x1 version of the widget. The only difference between
 * the 3x1 widget and a resized 1x1 widget is the suggested defaults.
 */
public class ClockWidget0ConfigActivity_3x1 extends ClockWidget0ConfigActivity
{
    public ClockWidget0ConfigActivity_3x1()
    {
        super();
    }

    @Override
    protected Class<?> getWidgetClass() {
        return ClockWidget0_3x1.class;
    }

    @Override
    protected void initViews( Context context ) {
        super.initViews(context);
    }

    public static final boolean DEF_SHOWTITLE = false;
    public static final boolean DEF_SHOWLABELS = false;

    @Override
    protected void loadTitleSettings(Context context)
    {
        super.loadTitleSettings(context);

        boolean showTitle = WidgetSettings.loadShowTitlePref(context, appWidgetId, DEF_SHOWTITLE);
        checkbox_showTitle.setChecked(showTitle);
        setTitleTextEnabled(showTitle);
    }

    @Override
    protected void loadShowLabels(Context context)
    {
        boolean showLabels = WidgetSettings.loadShowLabelsPref(context, appWidgetId, DEF_SHOWLABELS);
        checkbox_showLabels.setChecked(showLabels);
    }

    @Override
    protected String getPrimaryWidgetModeSize() {
        return SIZE_3x1;
    }

}
