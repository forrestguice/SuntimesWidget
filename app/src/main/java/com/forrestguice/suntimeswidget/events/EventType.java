/**
    Copyright (C) 2021-2023 Forrest Guice
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

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;

import java.util.Set;

public enum EventType
{
    DATE,
    EVENTALIAS,
    SOLAREVENT,
    SUN_ELEVATION,
    SHADOWLENGTH;

    private EventType() //String displayString)
    {
        //this.displayString = displayString;
    }

    public static EventType[] visibleTypes() {
        return new EventType[] { EventType.SUN_ELEVATION, EventType.SHADOWLENGTH };
    }

    //private String displayString;
    //public String getDisplayString()
    //{
    //    return displayString;
    //}
    //public void setDisplayString(String value)
    //{
    //    displayString = value;
    //}
    //public static void initDisplayStrings(Context context) {
    //    SUN_ELEVATION.setDisplayString(context.getString(R.string.eventType_sun_elevation));
    //}
    //public String toString()
    //{
    //    return displayString;
    //}

    @Nullable
    public static EventType resolveEventType(SuntimesDataSettings settings, String eventID)
    {
        if (isNumeric(eventID)) {
            return EventType.DATE;
        }
        if (SunElevationEvent.isElevationEvent(eventID)) {
            return EventType.SUN_ELEVATION;
        }
        if (ShadowLengthEvent.isShadowLengthEvent(eventID)) {
            return EventType.SHADOWLENGTH;
        }
        for (SolarEvents event : SolarEvents.values()) {
            if (event.name().startsWith(eventID)) {
                return EventType.SOLAREVENT;
            }
        }
        Set<String> eventList = settings.loadEventList();
        for (String aliasID : eventList)
        {
            if (eventID.startsWith(aliasID)) {
                return EventType.EVENTALIAS;
            }
        }
        return null;
    }

    /**
     * @param eventID eventID
     * @return true all characters are numeric, false if any character is not [1,9]
     */
    protected static boolean isNumeric(@NonNull String eventID)
    {
        for (int i=0; i<eventID.length(); i++)
        {
            char c = eventID.charAt(i);
            boolean isNumeric = (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7'|| c == '8' || c == '9');
            if (!isNumeric) {
                return false;
            }
        }
        return true;
    }

}
