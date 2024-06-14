// SPDX-License-Identifier: GPL-3.0-or-later
/*
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

package com.forrestguice.suntimeswidget.colors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;

import com.forrestguice.suntimeswidget.equinox.EquinoxColorValues;
import com.forrestguice.suntimeswidget.graph.colors.LightGraphColorValues;
import com.forrestguice.suntimeswidget.graph.colors.LightMapColorValues;
import com.forrestguice.suntimeswidget.graph.colors.LineGraphColorValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.ToIntFunction;

/**
 * ColorValues
 */
public class AppColorValues extends ResourceColorValues
{
    public static final String TAG_GRAPH = "graph";

    private static final ResourceColorValues[] RESOURCE_VALUES = new ResourceColorValues[]{
            new LightMapColorValues(), new LightGraphColorValues(), new LineGraphColorValues(), new EquinoxColorValues()
    };

    private static final ToIntFunction<Integer> toInt = new ToIntFunction<Integer>()
    {
        @Override
        public int applyAsInt(Integer integer) {
            return integer;
        }
    };

    protected static String[] colorKeys;
    protected static final int[] colorAttrs;
    protected static final int[] colorResDark;
    protected static final int[] colorResLight;
    protected static final int[] colorResLabel;
    protected static final int[] colorFallback;

    static
    {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<Integer> attrs = new ArrayList<>();
        ArrayList<Integer> darkRes = new ArrayList<>();
        ArrayList<Integer> lightRes = new ArrayList<>();
        ArrayList<Integer> labelRes = new ArrayList<>();
        ArrayList<Integer> fallback = new ArrayList<>();

        Set<String> keySet = new TreeSet<>();
        ResourceColorValues[] r = RESOURCE_VALUES;
        OuterLoop:
        for (int i = 0; i<r.length; i++)
        {
            //keys.addAll(Arrays.asList(r[i].getColorKeys()));
            String[] s = r[i].getColorKeys();

            int[] a = r[i].getColorAttrs();
            for (int j=0; j<a.length; j++) {
                if (!keySet.contains(s[j])) {
                    attrs.add(a[j]);
                }
            }

            int[] rD = r[i].getColorsResDark();
            for (int j=0; j<rD.length; j++) {
                if (!keySet.contains(s[j])) {
                    darkRes.add(rD[j]);
                }
            }

            int[] rL = r[i].getColorsResLight();
            for (int j=0; j<rL.length; j++) {
                if (!keySet.contains(s[j])) {
                    lightRes.add(rL[j]);
                }
            }

            int[] rLabels = r[i].getColorLabelsRes();
            for (int j=0; j<rLabels.length; j++) {
                if (!keySet.contains(s[j])) {
                    labelRes.add(rLabels[j]);
                }
            }

            int[] f = r[i].getColorsFallback();
            for (int j=0; j<f.length; j++) {
                if (!keySet.contains(s[j])) {
                    fallback.add(f[j]);
                }
            }

            for (int j=0; j<s.length; j++)
            {
                if (!keySet.contains(s[j]))
                {
                    keySet.add(s[j]);
                    keys.add(s[j]);
                }
            }
        }

        colorKeys = keys.toArray(new String[0]);
        colorAttrs = Arrays.stream(attrs.toArray(new Integer[0])).mapToInt(toInt).toArray();
        colorResDark = Arrays.stream(darkRes.toArray(new Integer[0])).mapToInt(toInt).toArray();
        colorResLight = Arrays.stream(lightRes.toArray(new Integer[0])).mapToInt(toInt).toArray();
        colorResLabel = Arrays.stream(labelRes.toArray(new Integer[0])).mapToInt(toInt).toArray();
        colorFallback = Arrays.stream(fallback.toArray(new Integer[0])).mapToInt(toInt).toArray();
    }

    public String[] getColorKeys() {
        return colorKeys;
    }
    public int[] getColorAttrs() {
        return colorAttrs;
    }
    public int[] getColorLabelsRes() {
        return colorResLabel;
    }
    public int[] getColorsResDark() {
        return colorResDark;
    }
    public int[] getColorsResLight() {
        return colorResLight;
    }
    public int[] getColorsFallback() {
        return colorFallback;
    }

    public AppColorValues(ColorValues other) {
        super(other);
    }
    public AppColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    protected AppColorValues(Parcel in) {
        super(in);
    }
    public AppColorValues() {
        super();
    }
    public AppColorValues(Context context, boolean darkTheme) {
        super(context, darkTheme);
    }
    public AppColorValues(String jsonString) {
        super(jsonString);
    }

    public static final Creator<AppColorValues> CREATOR = new Creator<AppColorValues>()
    {
        public AppColorValues createFromParcel(Parcel in) {
            return new AppColorValues(in);
        }
        public AppColorValues[] newArray(int size) {
            return new AppColorValues[size];
        }
    };

    public static AppColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new AppColorValues(new AppColorValues().getDefaultValues(context, darkTheme));
    }

}
