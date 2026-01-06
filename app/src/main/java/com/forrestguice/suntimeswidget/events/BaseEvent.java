package com.forrestguice.suntimeswidget.events;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.util.Resources;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;

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
        if (offset != 0)
        {
            TimeDeltaDisplay.initDisplayStrings(context);
            String offsetDisplay = utils.timeDeltaLongDisplayString(0, offset, false).getValue();
            return context.getQuantityString((offset < 0 ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), offset, offsetDisplay);
        } else return "";
    }

    public abstract String getEventName();
    public abstract String getEventTitle(SuntimesDataSettings settings);
    public abstract String getEventPhrase(SuntimesDataSettings settings);
    public abstract String getEventGender(SuntimesDataSettings settings);
    public abstract String getEventSummary(SuntimesDataSettings settings);
}
