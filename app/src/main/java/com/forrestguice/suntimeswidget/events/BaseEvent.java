package com.forrestguice.suntimeswidget.events;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.util.Resources;

public abstract class BaseEvent
{
    public BaseEvent(int offset) {
        this.offset = offset;
    }

    protected int offset;            // milliseconds
    public int getOffset() {
        return offset;
    }

    protected String getUri() {
        return EventUri.getEventCalcUri(EventUri.AUTHORITY(), getEventName());
    }

    protected static final TimeDeltaDisplay utils = new TimeDeltaDisplay();
    public String offsetDisplay(Resources context)
    {
        if (offset != 0 && resIDs != null)
        {
            String offsetDisplay = utils.timeDeltaLongDisplayString(0, offset, false).getValue();
            return context.getQuantityString((offset < 0 ? resIDs.getResID_plurals_before() : resIDs.getResID_plurals_after()), offset, offsetDisplay);
        } else return "";
    }

    public abstract String getEventName();
    public abstract String getEventTitle(SuntimesDataSettings settings);
    public abstract String getEventPhrase(SuntimesDataSettings settings);
    public abstract String getEventGender(SuntimesDataSettings settings);
    public abstract String getEventSummary(SuntimesDataSettings settings);

    @Nullable
    protected static ResID_BaseEvent resIDs = null;
    public static void setResIDs(@NonNull ResID_BaseEvent values) {
        resIDs = values;
    }

    /**
     * ResID_BaseEvent
     */
    public interface ResID_BaseEvent
    {
        int getResID_plurals_before();    // R.plurals.offset_before_plural
        int getResID_plurals_after();     // R.plurals.offset_after_plural
    }
}
