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

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.util.Log;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.settings.LengthUnit;
import com.forrestguice.util.text.TimeDisplayText;

public final class ShadowLengthEvent extends ElevationEvent
{
    public static final String NAME_PREFIX = "SHADOW_";

    public ShadowLengthEvent(double objHeight, double length, int offset, boolean rising)
    {
        super(0, offset, rising);
        this.objHeight = objHeight;
        this.length = length;

        if (this.objHeight != 0 && this.length != 0) {
            this.angle = Math.toDegrees(Math.atan(this.objHeight / this.length));
        }
    }

    protected double length;    // meters
    public double getLength() {
        return length;
    }

    protected double objHeight;    // meters
    public double getObjHeight() {
        return objHeight;
    }

    @Override
    public String getEventTitle(SuntimesDataSettings context) {
        return offsetDisplay(context.getResources()) + context.getString(R.string.shadowevent_title) + " " + (rising ? "rising" : "setting") + " (" + angle + ")";   // TODO: format
    }

    @Override
    public String getEventPhrase(SuntimesDataSettings context) {
        return offsetDisplay(context.getResources()) + context.getString(R.string.shadowevent_title) + " " + (rising ? "rising" : "setting") + " at " + angle;   // TODO: format
    }

    @Override
    public String getEventGender(SuntimesDataSettings context) {
        return context.getString(R.string.shadowevent_phrase_gender);
    }

    @Override
    public String getEventSummary(SuntimesDataSettings context)
    {
        LengthUnit units = context.loadLengthUnitsPref(0);
        String height = SuntimesUtils.formatAsHeight(context.getResources(), getObjHeight(), units, 1, true).getValue();

        TimeDisplayText t = SuntimesUtils.formatAsHeight(context.getResources(), getLength(), units, 1, true);
        String length = context.getString(R.string.units_format_short, t.getValue(), t.getUnits());

        if (offset == 0) {
            return offsetDisplay(context.getResources()) + context.getString(R.string.shadowevent_summary_format, context.getString(R.string.shadowevent_title), height, length);
        } else {
            return context.getString(R.string.shadowevent_summary_format1, offsetDisplay(context.getResources()), context.getString(R.string.shadowevent_title), height, length);
        }
    }

    public static boolean isShadowLengthEvent(String eventName) {
        return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
    }

    /**
     * @return e.g. SHADOW_1:10r          (@ 10 meters (rising)),
     *              SHADOW_1:10|-300000r  (5m before @ 10 meters (rising))
     */
    @Override
    public String getEventName() {
        return getEventName(objHeight, length, offset, rising);
    }
    public static String getEventName(double objHeight, double length, int offset, @Nullable Boolean rising) {
        String name = NAME_PREFIX
                + objHeight + ":" + length
                + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
        if (rising != null) {
            name += (rising ? SUFFIX_RISING : SUFFIX_SETTING);
        }
        return name;
    }

    @Nullable
    public static ShadowLengthEvent valueOf(String eventName)
    {
        if (isShadowLengthEvent(eventName))
        {
            double height = 1, length = 1;
            int offsetMinutes = 0;
            boolean hasSuffix = eventName.endsWith(SUFFIX_RISING) || eventName.endsWith(SUFFIX_SETTING);
            try {
                String contentString = eventName.substring(7, eventName.length() - (hasSuffix ? 1 : 0));    // SHADOW_<contentString>
                String[] contentParts = contentString.split("\\|");

                String[] shadowParts = contentParts[0].split(":");
                if (shadowParts.length > 1)
                {
                    height = Double.parseDouble(shadowParts[0]);
                    length = Double.parseDouble(shadowParts[1]);

                } else if (shadowParts.length > 0) {
                    height = 1;
                    length = Double.parseDouble(shadowParts[0]);
                }

                if (contentParts.length > 1) {
                    offsetMinutes = Integer.parseInt(contentParts[1]);
                }

            } catch (Exception e) {
                Log.e("ShadowLengthEvent", "createEvent: bad length: " + e);
                return null;
            }
            boolean rising = eventName.endsWith(SUFFIX_RISING);
            return new ShadowLengthEvent(height, length, (offsetMinutes * 60 * 1000), rising);
        } else return null;
    }
}
