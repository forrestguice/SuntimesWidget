/**
    Copyright (C) 2023-2025 Forrest Guice
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

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.util.Resources;

public abstract class ElevationEvent extends BaseEvent
{
    public static final String SUFFIX_RISING = "r";
    public static final String SUFFIX_SETTING = "s";

    public ElevationEvent(double angle, int offset, boolean rising) {
        super(offset);
        this.angle = angle;
        this.rising = rising;
    }

    protected double angle;
    public double getAngle() {
        return angle;
    }
    public void setAngle(double value) {
        angle = value;
    }

    protected boolean rising;
    public boolean isRising() {
        return rising;
    }

    @Override
    public String offsetDisplay(Resources context)
    {
        if (offset != 0)
        {
            TimeDeltaDisplay.initDisplayStrings(context);
            String offsetDisplay = utils.timeDeltaLongDisplayString(0, offset, false).getValue();
            return context.getQuantityString((offset < 0 ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), (int)angle, offsetDisplay);
        } else return "";
    }
}
