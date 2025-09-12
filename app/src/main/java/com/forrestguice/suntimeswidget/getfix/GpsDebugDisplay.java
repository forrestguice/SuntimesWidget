/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.getfix;

import android.annotation.TargetApi;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * GpsDebugDisplay
 */
public class GpsDebugDisplay
{
    /**
     * api 24+
     */
    @TargetApi(24)
    public static List<Integer> getSatellites(GnssStatus status)
    {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i=0; i<status.getSatelliteCount(); i++) {
            if (status.getCn0DbHz(i) != 0) {
                indices.add(i);
            }
        }
        return indices;
    }
    @TargetApi(24)
    public static String getSignalToNoiseRatio(GnssStatus status)
    {
        StringBuilder result = new StringBuilder();
        if (status != null)
        {
            int c = 0;
            for (int i : getSatellites(status))
            {
                if (c != 0) {
                    result.append("|");
                }
                result.append(status.getCn0DbHz(i));
                c++;
            }
        }
        return result.toString();
    }
    @TargetApi(24)
    public static String getConstellationCount(GnssStatus status)
    {
        StringBuilder result = new StringBuilder();
        HashMap<Integer, Integer> count = countConstellations(status);

        int c = 0;
        for (int type : count.keySet())
        {
            if (c != 0) {
                result.append("|");
            }
            result.append(constellationTypeLabel(type)).append(" ");
            result.append(count.get(type));
            c++;
        }

        return result.toString();
    }
    @TargetApi(24)
    public static HashMap<Integer, Integer> countConstellations(@Nullable GnssStatus status)
    {
        HashMap<Integer, Integer> count = new HashMap<>();
        if (status != null) {
            for (int i : getSatellites(status)) {
                int type = status.getConstellationType(i);
                int c = (count.containsKey(type) ? count.get(type) : 0);
                count.put(type, ++c);
            }
        }
        return count;
    }
    @TargetApi(24)
    public static String constellationTypeLabel(int type)
    {
        switch (type) {
            case GnssStatus.CONSTELLATION_GPS: return "GPS";
            case GnssStatus.CONSTELLATION_SBAS: return "SBAS";
            case GnssStatus.CONSTELLATION_GLONASS: return "GLONASS";
            case GnssStatus.CONSTELLATION_QZSS: return "QZSS";
            case GnssStatus.CONSTELLATION_BEIDOU: return "BEIDOU";
            case GnssStatus.CONSTELLATION_GALILEO: return "GALILEO";
            case GnssStatus.CONSTELLATION_UNKNOWN: default: return "UNKNOWN";
        }
    }

    /**
     * api 23-
     */
    public static List<GpsSatellite> getSatelliteList(GpsStatus status)
    {
        ArrayList<GpsSatellite> list = new ArrayList<>();
        for (GpsSatellite satellite : status.getSatellites()) {
            if (satellite.usedInFix()) {
                list.add(satellite);
            }
        }
        return list;
    }
    public static int getSatelliteCount(List<GpsSatellite> list, int current) {
        int i = list.size();
        return (i != 0 ? i : current);
    }
    public static String getSignalToNoiseRatio(List<GpsSatellite> list)
    {
        StringBuilder result = new StringBuilder();
        for (int i=0; i<list.size(); i++) {
            if (i != 0) {
                result.append("|");
            }
            result.append(list.get(i).getSnr());
        }
        return result.toString();
    }
}
