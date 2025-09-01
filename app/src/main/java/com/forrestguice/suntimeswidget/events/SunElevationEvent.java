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

import android.content.Context;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

public final class SunElevationEvent extends ElevationEvent
{
    public static final String NAME_PREFIX = "SUN_";

    public SunElevationEvent(double angle, int offset, boolean rising) {
        super(angle, offset, rising);
    }

    @Override
    public String getEventTitle(Context context) {
        return offsetDisplay(context) + context.getString(R.string.sunevent_title) + " " + (rising ? "rising" : "setting") + " (" + angle + ")";   // TODO: format
    }
    @Override
    public String getEventPhrase(Context context) {
        return offsetDisplay(context) + context.getString(R.string.sunevent_title) + " " + (rising ? "rising" : "setting") + " at " + angle;   // TODO: format
    }
    @Override
    public String getEventGender(Context context) {
        return context.getString(R.string.sunevent_phrase_gender);
    }

    @Override
    public String getEventSummary(Context context)
    {
        SuntimesUtils utils = new SuntimesUtils();
        String angle = utils.formatAsElevation(getAngle(), 1).toString();
        if (offset == 0) {
            return offsetDisplay(context) + context.getString(R.string.sunevent_summary_format, context.getString(R.string.sunevent_title), angle.toString());
        } else {
            return context.getString(R.string.sunevent_summary_format1, offsetDisplay(context), context.getString(R.string.sunevent_title), angle.toString());
        }
    }

    public static boolean isElevationEvent(String eventName) {
        return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
    }

    /**
     * @return e.g. SUN_-10r     (@ -10 degrees (rising)),
     *              SUN_-10|-300000r  (5m before @ 10 degrees (rising))
     */
    @Override
    public String getEventName() {
        return getEventName(angle, offset, rising);
    }
    public static String getEventName(double angle, int offset, @Nullable Boolean rising) {
        String name = NAME_PREFIX
                + angle
                + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
        if (rising != null) {
            name += (rising ? SUFFIX_RISING : SUFFIX_SETTING);
        }
        return name;
    }

    @Nullable
    public static SunElevationEvent valueOf(String eventName)
    {
        if (isElevationEvent(eventName))
        {
            double angle;
            int offsetMinutes = 0;
            boolean hasSuffix = eventName.endsWith(SUFFIX_RISING) || eventName.endsWith(SUFFIX_SETTING);
            try {
                String contentString = eventName.substring(4, eventName.length() - (hasSuffix ? 1 : 0));
                String[] contentParts = contentString.split("\\|");

                angle = Double.parseDouble(contentParts[0]);
                if (contentParts.length > 1) {
                    offsetMinutes = Integer.parseInt(contentParts[1]);
                }

            } catch (Exception e) {
                Log.e("ElevationEvent", "createEvent: bad angle: " + eventName + ": " + e);
                return null;
            }
            boolean rising = eventName.endsWith(SUFFIX_RISING);
            return new SunElevationEvent(angle, (offsetMinutes * 60 * 1000), rising);
        } else return null;
    }
}
