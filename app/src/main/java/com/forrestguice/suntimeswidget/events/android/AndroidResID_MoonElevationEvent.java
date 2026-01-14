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
import com.forrestguice.suntimeswidget.events.MoonElevationEvent;

public class AndroidResID_MoonElevationEvent extends AndroidResID_BaseEvent implements MoonElevationEvent.ResID_MoonElevationEvent
{
    @Override
    public int string_title() {
        return R.string.moonevent_title;
    }

    @Override
    public int string_phrase_gender() {
        return R.string.moonevent_phrase_gender;
    }

    @Override
    public int string_summary_format() {
        return R.string.moonevent_summary_format;
    }

    @Override
    public int string_summary_format1() {
        return R.string.moonevent_summary_format1;
    }
}
