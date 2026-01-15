/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.settings.display;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.util.Resources;

import java.util.Calendar;

/**
 * MoonPhaseDisplay
 */
public enum MoonPhaseDisplay
{
    NEW("New", "New Moon"),
    WAXING_CRESCENT("Waxing Crescent", "Waxing Crescent"),
    FIRST_QUARTER("First Quarter", "First Quarter"),
    WAXING_GIBBOUS("Waxing Gibbous", "Waxing Gibbous"),
    FULL("Full", "Full Moon"),
    WANING_GIBBOUS("Waning Gibbous", "Waning Gibbous"),
    THIRD_QUARTER("Third Quarter", "Third Quarter"),
    WANING_CRESCENT("Waning Crescent", "Waning Crescent");

    private int iconResource = 0, viewResource = 0;
    private String shortDisplayString, longDisplayString;

    protected static ResID_MoonPhaseDisplay r = null;

    private MoonPhaseDisplay(@NonNull String shortDisplayString, @NonNull String longDisplayString)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }

    @NonNull
    public String toString() {
        return longDisplayString;
    }

    public int getIcon() {
        return getIcon(false);    // from northern hemisphere we look southward
    }

    public int getIcon(boolean northward)
    {
        if (northward) {
            switch (this) {
                case WAXING_CRESCENT: return WANING_CRESCENT.iconResource;   // swap icons
                case FIRST_QUARTER: return THIRD_QUARTER.iconResource;
                case WAXING_GIBBOUS: return WANING_GIBBOUS.iconResource;
                case THIRD_QUARTER: return FIRST_QUARTER.iconResource;
                case WANING_CRESCENT: return WAXING_CRESCENT.iconResource;
                case NEW: case FULL: default: return iconResource;
            }
        } else return iconResource;
    }

    public int getView()
    {
        return viewResource;
    }

    @NonNull
    public String getShortDisplayString() {
        return shortDisplayString;
    }

    @NonNull
    public String getLongDisplayString() {
        return longDisplayString;
    }

    public void setDisplayString(@NonNull String shortDisplayString, @NonNull String longDisplayString)
    {
        this.shortDisplayString = shortDisplayString;
        this.longDisplayString = longDisplayString;
    }

    public static void initDisplayStrings(Resources context, ResID_MoonPhaseDisplay ids)
    {
        r = ids;
        for (MoonPhaseDisplay value : MoonPhaseDisplay.values())
        {
            value.setDisplayString(context.getString(ids.string_shortDisplay(value)), context.getString(ids.string_longDisplay(value)));
            value.setIconResource(ids.drawable_icon(value));
            value.setViewResource(ids.id_view(value));
        }
    }

    public void setIconResource(int resID) {
        iconResource = resID;
    }
    public void setViewResource(int resID) {
        viewResource = resID;
    }

    public static CharSequence getMoonPhaseLabel(Resources context, SuntimesCalculator calculator, SuntimesCalculator.MoonPhase majorPhase, Calendar phaseDate)
    {
        if (majorPhase == SuntimesCalculator.MoonPhase.FULL || majorPhase == SuntimesCalculator.MoonPhase.NEW)
        {
            SuntimesCalculator.MoonPosition phasePosition = calculator.getMoonPosition(phaseDate);

            if (SuntimesMoonData.isSuperMoon(phasePosition)) {
                return (majorPhase == SuntimesCalculator.MoonPhase.NEW) ? context.getString(r.string_superNewMoon())
                        : context.getString(r.string_superFullMoon());

            } else if (SuntimesMoonData.isMicroMoon(phasePosition)) {
                return (majorPhase == SuntimesCalculator.MoonPhase.NEW) ? context.getString(r.string_microNewMoon())
                        : context.getString(r.string_microFullMoon());

            } else return SuntimesMoonData.toPhase(majorPhase).getLongDisplayString();
        } else return SuntimesMoonData.toPhase(majorPhase).getLongDisplayString();
    }

    public interface ResID_MoonPhaseDisplay
    {
        int string_microFullMoon();
        int string_microNewMoon();
        int string_superFullMoon();
        int string_superNewMoon();

        int string_shortDisplay(MoonPhaseDisplay value);
        int string_longDisplay(MoonPhaseDisplay value);
        int drawable_icon(MoonPhaseDisplay value);
        int id_view(MoonPhaseDisplay value);
    }
}
