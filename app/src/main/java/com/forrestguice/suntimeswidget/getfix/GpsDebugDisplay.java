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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GpsDebugDisplay
{
    /**
     * api 24+
     */
    @TargetApi(24)
    public static List<Integer> getSatellites(GnssStatus status, boolean usedInFix)
    {
        ArrayList<SatelliteInfo<Void>> satellites = new ArrayList<>();
        for (int i=0; i<status.getSatelliteCount(); i++) {
            if (!usedInFix || usedInFix && status.usedInFix(i)) {
                satellites.add(new SatelliteInfo<Void>(i, status.getSvid(i), status.getConstellationType(i), null));
            }
        }
        Collections.sort(satellites);
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i=0; i<satellites.size(); i++) {
            indices.add(satellites.get(i).i);
        }
        return indices;
    }

    public static int countSatellitesWithSignal(GnssStatus status)
    {
        int c = 0;
        for (int i=0; i<status.getSatelliteCount(); i++) {
            if ((status.hasAlmanacData(i) && status.getCn0DbHz(i) != 0))
                c++;
        }
        return c;
    }

    public static String SYMBOL_USED_IN_FIX = "∆";
    public static String SYMBOL_HAS_EPHEMERIS = "◊";
    public static String SYMBOL_HAS_ALMANAC = "#";
    public static String SYMBOL_HAS_NO_ALMANAC = "_";
    public static String ENTRY_SEPARATOR = " ";
    public static String VALUE_SEPARATOR = "_";

    public static String getSatelliteKey()
    {
        return SYMBOL_USED_IN_FIX + " used" + ", " +
                SYMBOL_HAS_EPHEMERIS + " has-ephemeris" + ", " +
                SYMBOL_HAS_ALMANAC + " has-almanac" + ", " +
                SYMBOL_HAS_NO_ALMANAC + " no-data";
    }

    @TargetApi(24)
    public static String getSatelliteReport(GnssStatus status, boolean usedInFix)
    {
        StringBuilder result = new StringBuilder();
        if (status != null)
        {
            int c = 0;
            for (int i : getSatellites(status, usedInFix))
            {
                if (c != 0) {
                    result.append(ENTRY_SEPARATOR);
                }

                String symbol = status.usedInFix(i) ? SYMBOL_USED_IN_FIX
                        : status.hasEphemerisData(i) ? SYMBOL_HAS_EPHEMERIS
                        : status.hasAlmanacData(i) ? SYMBOL_HAS_ALMANAC : SYMBOL_HAS_NO_ALMANAC;

                result.append(symbol)
                        .append(status.getSvid(i))
                        .append(constellationTypeSymbol(status.getConstellationType(i)))
                        .append(VALUE_SEPARATOR)
                        .append(status.getCn0DbHz(i));
                c++;
            }
        }
        return result.toString();
    }

    @TargetApi(24)
    public static String getConstellationCount(GnssStatus status, boolean usedInFix)
    {
        StringBuilder result = new StringBuilder();
        HashMap<Integer, Integer> count = countConstellations(status, usedInFix);

        int c = 0;
        for (int type : count.keySet())
        {
            if (c != 0) {
                result.append(", ");
            }
            result.append(count.get(type)).append(" ");
            result.append(constellationTypeLabel(type));
            c++;
        }

        return result.toString();
    }
    @TargetApi(24)
    public static HashMap<Integer, Integer> countConstellations(@Nullable GnssStatus status, boolean usedInFix)
    {
        HashMap<Integer, Integer> count = new HashMap<>();
        if (status != null) {
            for (int i : getSatellites(status, usedInFix)) {
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
            case GnssStatus.CONSTELLATION_SBAS: return "SBAS²";
            case GnssStatus.CONSTELLATION_GLONASS: return "GLONASS³";
            case GnssStatus.CONSTELLATION_QZSS: return "QZSS⁴";
            case GnssStatus.CONSTELLATION_BEIDOU: return "BEIDOU˙";
            case GnssStatus.CONSTELLATION_GALILEO: return "GALILEO˚";
            case CONSTELLATION_IRNSS: return "IRNSS´";
            case GnssStatus.CONSTELLATION_UNKNOWN: default: return "UNKNOWN";
        }
    }
    public static String constellationTypeSymbol(int type)
    {
        switch (type) {
            case GnssStatus.CONSTELLATION_GPS: return "";
            case GnssStatus.CONSTELLATION_SBAS: return "²";
            case GnssStatus.CONSTELLATION_GLONASS: return "³";
            case GnssStatus.CONSTELLATION_QZSS: return "⁴";
            case GnssStatus.CONSTELLATION_BEIDOU: return "˙";
            case GnssStatus.CONSTELLATION_GALILEO: return "˚";
            case CONSTELLATION_IRNSS: return "´";
            case GnssStatus.CONSTELLATION_UNKNOWN: default: return "̉¿";
        }
    }
    public static final int CONSTELLATION_IRNSS = 7;    // GnssStatus.CONSTELLATION_IRNSS

    /**
     * api 23-
     */
    public static List<GpsSatellite> getSatelliteList(GpsStatus status, boolean usedInFix)
    {
        ArrayList<SatelliteInfo<GpsSatellite>> satellites = new ArrayList<>();
        int i = 0;
        for (GpsSatellite satellite : status.getSatellites()) {
            if (!usedInFix || usedInFix && satellite.usedInFix()) {
                satellites.add(new SatelliteInfo<>(i, satellite.getPrn(), 0, satellite));
            }
            i++;
        }
        Collections.sort(satellites);
        ArrayList<GpsSatellite> list = new ArrayList<>();
        for (int j=0; j<satellites.size(); j++) {
            list.add(satellites.get(j).linked);
        }
        return list;
    }
    public static int getSatelliteCount(List<GpsSatellite> list, int current) {
        int i = list.size();
        return (i != 0 ? i : current);
    }
    public static String getSatelliteReport(List<GpsSatellite> list)
    {
        StringBuilder result = new StringBuilder();
        for (int i=0; i<list.size(); i++)
        {
            if (i != 0) {
                result.append(ENTRY_SEPARATOR);
            }

            GpsSatellite satellite = list.get(i);
            float snr = satellite.getSnr();

            String symbol = satellite.usedInFix() ? SYMBOL_USED_IN_FIX
                    : satellite.hasEphemeris() ? SYMBOL_HAS_EPHEMERIS
                    : satellite.hasAlmanac() ? SYMBOL_HAS_ALMANAC : SYMBOL_HAS_NO_ALMANAC;

            result.append(symbol)
                    .append(satellite.getPrn())
                    .append(VALUE_SEPARATOR)
                    .append(snr == 0 ? 0 : satellite.getSnr());
        }
        return result.toString();
    }

    /**
     * SatelliteInfo
     */
    private static class SatelliteInfo<T> implements Comparable<SatelliteInfo<T>>
    {
        public final int i;
        public final int id;
        public final int constellation;
        public final T linked;

        public SatelliteInfo(int i, int svid, int constellation, T linked) {
            this.i = i;
            this.id = svid;
            this.constellation = constellation;
            this.linked = linked;
        }

        @Override
        public int compareTo(@NonNull SatelliteInfo o) {
            if (o != null) {
                if (constellation == o.constellation)
                    return Integer.compare(id, o.id);
                else return Integer.compare(constellation, o.constellation);
            } else return 1;
        }
    }
}
