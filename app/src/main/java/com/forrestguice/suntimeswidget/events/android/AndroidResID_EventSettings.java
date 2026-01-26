/**
    Copyright (C) 2026 Forrest Guice
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

package com.forrestguice.suntimeswidget.events.android;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.events.EventSettings;
import com.forrestguice.suntimeswidget.events.EventType;

public class AndroidResID_EventSettings implements EventSettings.ResID_EventSettings
{
    @Override
    public int string_suggestEventLabel(EventType eventType)
    {
        if (eventType != null) {
            switch (eventType) {
                case SUN_ELEVATION: return R.string.editevent_dialog_label_suggested;
                case SHADOWLENGTH: return R.string.editevent_dialog_label_suggested1;
                case MOONILLUM: return R.string.moonillumevent_label_suggested;
                case MOON_ELEVATION: return R.string.moonevent_label_suggested;
                case DAYPERCENT: default: return R.string.editevent_dialog_label_suggested2;
            }
        }
        return 0;
    }
}
