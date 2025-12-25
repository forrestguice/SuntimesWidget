/**
   Copyright (C) 2025 Forrest Guice
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

public class ClockLayout_1x1_3 extends ClockLayout_1x1_1
{
    public ClockLayout_1x1_3() {
        super();
    }

    @Override
    protected ClockFaceOptions initClockFaceOptions(Context context, int appWidgetId)
    {
        ClockFaceOptions options = super.initClockFaceOptions(context, appWidgetId);
        options.style = ClockFaceOptions.STYLE_DIGITAL1;
        return options;
    }
}
