/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.graph;

import android.util.Log;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.settings.TimeMode;

import java.util.Calendar;

/**
 * EarliestLatestSunriseSunsetData
 */
public class EarliestLatestSunriseSunsetData
{
    public double longitude = 0;

    public double early_sunrise_hour = -1;
    public int early_sunrise_day = -1;

    public double early_sunset_hour = -1;
    public int early_sunset_day = -1;

    public double late_sunrise_hour = -1;
    public int late_sunrise_day = -1;

    public double late_sunset_hour = -1;
    public int late_sunset_day = -1;

    public static EarliestLatestSunriseSunsetData findEarliestLatest(TimeMode mode, @NonNull SuntimesRiseSetDataset[] data)
    {
        long bench_start = System.nanoTime();
        EarliestLatestSunriseSunsetData result = new EarliestLatestSunriseSunsetData();

        if (data.length > 0 && data[0] != null) {
            result.longitude = data[0].location().getLongitudeAsDouble();
        }

        int i = 0;
        while (i < data.length)
        {
            SuntimesRiseSetData d = ((data[i] != null) ? data[i].getData(mode.name()) : null);
            Calendar risingEvent = ((d != null) ? d.sunriseCalendarToday() : null);
            if (risingEvent != null)
            {
                double lmtRisingHour = LightGraphView.lmtHour(risingEvent, result.longitude);
                if (result.early_sunrise_hour == -1 || lmtRisingHour < result.early_sunrise_hour) {
                    result.early_sunrise_hour = lmtRisingHour;
                    result.early_sunrise_day = risingEvent.get(Calendar.DAY_OF_YEAR);
                }
                if (result.late_sunrise_hour == -1 || lmtRisingHour > result.late_sunrise_hour) {
                    result.late_sunrise_hour = lmtRisingHour;
                    result.late_sunrise_day = risingEvent.get(Calendar.DAY_OF_YEAR);
                }
            }

            Calendar settingEvent = ((d != null) ? d.sunsetCalendarToday() : null);
            if (settingEvent != null)
            {
                double lmtSettingHour = LightGraphView.lmtHour(settingEvent, result.longitude);
                if (result.early_sunset_hour == -1 || lmtSettingHour < result.early_sunset_hour) {
                    result.early_sunset_hour = lmtSettingHour;
                    result.early_sunset_day = settingEvent.get(Calendar.DAY_OF_YEAR);
                }
                if (result.late_sunset_hour == -1 || lmtSettingHour > result.late_sunset_hour) {
                    result.late_sunset_hour = lmtSettingHour;
                    result.late_sunset_day = settingEvent.get(Calendar.DAY_OF_YEAR);
                }
            }
            i++;
        }

        long bench_end = System.nanoTime();
        Log.d("BENCH", "findEarliestLatest :: " + ((bench_end - bench_start) / 1000000.0) + " ms");
        return result;
    }
}
