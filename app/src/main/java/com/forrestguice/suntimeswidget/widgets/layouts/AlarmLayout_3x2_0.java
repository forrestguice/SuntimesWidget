/**
   Copyright (C) 2024 Forrest Guice
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
import android.content.Intent;

import com.forrestguice.suntimeswidget.widgets.AlarmWidgetService;
import com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings;

import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetService.AlarmWidgetItemViewFactory.EXTRA_APPWIDGETID;

public class AlarmLayout_3x2_0 extends AlarmLayout_2x2_0
{
    public AlarmLayout_3x2_0() {
        super();
    }

    @Override
    protected Intent getRemoteAdapterIntent(Context context, int appWidgetId)
    {
        Intent intent = new Intent(context, AlarmWidgetService.class);
        intent.putExtra(EXTRA_APPWIDGETID, appWidgetId);
        intent.putExtra(AlarmWidgetService.AlarmWidgetItemViewFactory.EXTRA_LAYOUTMODE, AlarmWidgetSettings.MODE_3x2);
        intent.setAction(appWidgetId + "_" + AlarmWidgetSettings.MODE_3x2);  // set action so Intent has a unique hashcode (RemoteViews are cached by Intent)
        return intent;
    }

}
