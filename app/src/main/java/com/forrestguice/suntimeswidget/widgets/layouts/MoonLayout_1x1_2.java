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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;

/**
 * Moon Phase (1x1)
 */
public class MoonLayout_1x1_2 extends MoonLayout_1x1_1
{
    public MoonLayout_1x1_2()
    {
        super();
    }

    @Override
    public void initLayoutID()
    {
        this.layoutID = R.layout.layout_widget_moon_1x1_2;
    }

    @Override
    public void prepareForUpdate(Context context, int appWidgetId, SuntimesMoonData data)
    {
        super.prepareForUpdate(context, appWidgetId, data);    // super.prepareForUpdate overrides layoutID
        this.layoutID = R.layout.layout_widget_moon_1x1_2;     // TODO: support scaleBase
    }

}
