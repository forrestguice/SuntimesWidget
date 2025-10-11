package com.forrestguice.suntimeswidget.events;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.util.Resources;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;

public abstract class ElevationEvent
{
    public static final String SUFFIX_RISING = "r";
    public static final String SUFFIX_SETTING = "s";

    public ElevationEvent(double angle, int offset, boolean rising) {
        this.angle = angle;
        this.offset = offset;
        this.rising = rising;
    }

    protected double angle;
    public double getAngle() {
        return angle;
    }
    public void setAngle(double value) {
        angle = value;
    }

    protected int offset;            // milliseconds
    public int getOffset() {
        return offset;
    }

    protected boolean rising;
    public boolean isRising() {
        return rising;
    }

    protected String getUri() {
        return EventUri.getEventCalcUri(AUTHORITY, getEventName());
    }

    private static final TimeDeltaDisplay utils = new TimeDeltaDisplay();
    public String offsetDisplay(Resources context)
    {
        if (offset != 0)
        {
            TimeDeltaDisplay.initDisplayStrings(context);
            String offsetDisplay = utils.timeDeltaLongDisplayString(0, offset, false).getValue();
            return context.getQuantityString((offset < 0 ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), (int)angle, offsetDisplay);
        } else return "";
    }

    public abstract String getEventName();
    public abstract String getEventTitle(SuntimesDataSettings settings);
    public abstract String getEventPhrase(SuntimesDataSettings settings);
    public abstract String getEventGender(SuntimesDataSettings settings);
    public abstract String getEventSummary(SuntimesDataSettings settings);
}
