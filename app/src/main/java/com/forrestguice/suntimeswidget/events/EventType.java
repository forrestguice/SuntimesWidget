package com.forrestguice.suntimeswidget.events;

import android.content.Context;
import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;

import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

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
    public static EventType resolveEventType(Context context, String eventID)
    {
        if (isNumeric(eventID)) {
            return EventType.DATE;
        }
        if (AlarmEventProvider.SunElevationEvent.isElevationEvent(eventID)) {
            return EventType.SUN_ELEVATION;
        }
        if (AlarmEventProvider.ShadowLengthEvent.isShadowLengthEvent(eventID)) {
            return EventType.SHADOWLENGTH;
        }
        for (SolarEvents event : SolarEvents.values()) {
            if (event.name().startsWith(eventID)) {
                return EventType.SOLAREVENT;
            }
        }
        Set<String> eventList = EventSettings.loadEventList(context);
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
