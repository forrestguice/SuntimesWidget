package com.forrestguice.suntimeswidget.events;

import android.content.Context;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

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

    protected int offset;            // milliseconds
    public int getOffset() {
        return offset;
    }

    protected boolean rising;
    public boolean isRising() {
        return rising;
    }

    protected String getUri(Context context) {
        return EventUri.getEventCalcUri(AUTHORITY, getEventName());
    }

    private static final SuntimesUtils utils = new SuntimesUtils();
    public String offsetDisplay(Context context)
    {
        if (offset != 0)
        {
            SuntimesUtils.initDisplayStrings(context);
            String offsetDisplay = utils.timeDeltaLongDisplayString(0, offset, false).getValue();
            return context.getResources().getQuantityString((offset < 0 ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), (int)angle, offsetDisplay);
        } else return "";
    }

    public abstract String getEventName();
    public abstract String getEventTitle(Context context);
    public abstract String getEventPhrase(Context context);
    public abstract String getEventGender(Context context);
    public abstract String getEventSummary(Context context);
}
