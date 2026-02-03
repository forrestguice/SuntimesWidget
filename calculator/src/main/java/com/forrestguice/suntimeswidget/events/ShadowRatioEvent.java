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

package com.forrestguice.suntimeswidget.events;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.util.Log;

public final class ShadowRatioEvent extends BaseEvent
{
    public static final String NAME_PREFIX = "SHADOWRATIO_";

    public ShadowRatioEvent(int factor, int offset)
    {
        super(offset);
        this.factor = factor;
    }

    protected int factor;
    public int getFactor() {
        return factor;
    }

    @Override
    public String getEventTitle(SuntimesDataSettings context) {
        String eventTitle = (r != null) ? context.getString(r.string_title()) : "Shadow Ratio";
        return offsetDisplay(context.getResources()) + eventTitle + " (x" + factor + ")";
    }
    @Override
    public String getEventPhrase(SuntimesDataSettings context) {
        String eventTitle = (r != null) ? context.getString(r.string_title()) : "Shadow Ratio";
        return offsetDisplay(context.getResources()) + eventTitle + " at x" + factor;
    }
    @Override
    public String getEventGender(SuntimesDataSettings context) {
        return (r != null) ? context.getString(r.string_phrase_gender()) : "other";
    }

    @Override
    public String getEventSummary(SuntimesDataSettings context)
    {
        String eventTitle = (r != null) ? context.getString(r.string_title()) : "Shadow Ratio";
        if (offset == 0) {
            return (r != null) ? offsetDisplay(context.getResources()) + context.getString(r.string_summary_format(), eventTitle, factor)
                    : offsetDisplay(context.getResources()) + eventTitle + " (x" + factor + ")";
        } else {
            return (r != null) ? context.getString(r.string_summary_format1(), offsetDisplay(context.getResources()), eventTitle, factor)
                    : offsetDisplay(context.getResources()) + eventTitle + " (x" + factor + ")";
        }
    }

    public static boolean isShadowRatioEvent(String eventName) {
        return (eventName != null && (eventName.startsWith(NAME_PREFIX)));
    }

    /**
     * @return e.g. SHADOWRATIO_1
     *              SHADOWRATIO_2|5
     */
    @Override
    public String getEventName() {
        return getEventName(factor, offset);
    }
    public static String getEventName(int factor, int offset) {
        return NAME_PREFIX + factor + ((offset != 0) ? "|" + (int)Math.ceil(offset / 1000d / 60d) : "");
    }

    @Nullable
    public static ShadowRatioEvent valueOf(String eventName)
    {
        if (isShadowRatioEvent(eventName))
        {
            int factor;
            int offsetMinutes = 0;
            try {
                String contentString = eventName.substring(NAME_PREFIX.length());
                String[] contentParts = contentString.split("\\|");
                factor = Integer.parseInt(contentParts[0]);
                if (contentParts.length > 1) {
                    offsetMinutes = Integer.parseInt(contentParts[1]);
                }
            } catch (Exception e) {
                Log.e("ShadowRatioEvent", "createEvent: bad factor: " + eventName + ": " + e);
                return null;
            }
            return new ShadowRatioEvent(factor, (offsetMinutes * 60 * 1000));
        } else return null;
    }

    @Nullable
    protected static ResID_ShadowRatioEvent r = null;
    public static void setResIDs(@NonNull ResID_ShadowRatioEvent values) {
        r = values;
    }

    public interface ResID_ShadowRatioEvent extends ResID_BaseEvent
    {
        int string_title();
        int string_phrase_gender();
        int string_summary_format();
        int string_summary_format1();
    }
}
