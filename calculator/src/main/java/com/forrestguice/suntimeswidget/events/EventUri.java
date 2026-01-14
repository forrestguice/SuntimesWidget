/**
    Copyright (C) 2021-2024 Forrest Guice
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
package com.forrestguice.suntimeswidget.events;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;
import com.forrestguice.util.Log;

public class EventUri
{
    public static String getEventInfoUri(String authority, String eventID) {
        return "content://" + authority + "/" + AlarmEventContract.QUERY_EVENT_INFO + "/" + eventID;
    }

    public static String getEventCalcUri(String authority, String eventID) {
        return "content://" + authority + "/" + AlarmEventContract.QUERY_EVENT_CALC + "/" + eventID;
    }

    public static String AUTHORITY()
    {
        if (buildConfig == null) {
            Log.w("EventUri", "AUTHORITY: BuildConfig is unset! returning default...");
            return AlarmEventContract.AUTHORITY;
        } else {
            return buildConfig.AUTHORITY_ROOT() + ".event.provider";
        }
    }

    @Nullable
    protected static BuildConfigInfo buildConfig = null;
    public static void setBuildConfigInfo(BuildConfigInfo value) {
        buildConfig = value;
    }
    public interface BuildConfigInfo {
        String AUTHORITY_ROOT();
    }
}
