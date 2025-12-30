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

import android.content.Context;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaceTags
{
    public static final String TAG_DEFAULT = PlaceItem.TAG_DEFAULT;
    public static final String TAG_CAPITAL = "[capital]";
    public static final String TAG_MISC = "[misc]";
    public static final String TAG_GPS = "[gps]";
    public static final String TAG_SUMMIT = "[summit]";

    public static final String[] DEFAULT_TAGS = new String[] { TAG_DEFAULT, TAG_CAPITAL, TAG_MISC, TAG_GPS, TAG_SUMMIT };

    public static final int[] ALL_TAG_ARRAYS = new int[] {
            R.array.place_tags, R.array.place_tags_AR, R.array.place_tags_AU,
            R.array.place_tags_BE, R.array.place_tags_BO, R.array.place_tags_BR,
            R.array.place_tags_CA, R.array.place_tags_CL, R.array.place_tags_CN, R.array.place_tags_CO,
            R.array.place_tags_DE, R.array.place_tags_EC, R.array.place_tags_ES,
            R.array.place_tags_FI, R.array.place_tags_FR, R.array.place_tags_GB, R.array.place_tags_GR,
            R.array.place_tags_IN, R.array.place_tags_IT, R.array.place_tags_NL, R.array.place_tags_NO,
            R.array.place_tags_MX, R.array.place_tags_PE, R.array.place_tags_PL, R.array.place_tags_PT,
            R.array.place_tags_RU, R.array.place_tags_US, R.array.place_tags_VE
    };

    public static HashMap<String, String> loadTagMap(Context context) {
        return loadTagMap(context, null, ALL_TAG_ARRAYS);
    }
    public static HashMap<String, String> loadTagMap(Context context, HashMap<String, String> map, int... arrayResID)
    {
        if (map == null) {
            map = new HashMap<>();
        }
        for (int a : arrayResID) {
            map = loadTagMap(context, a, map);
        }
        return map;
    }
    public static HashMap<String, String> loadTagMap(Context context, int arrayResID, HashMap<String, String> map)
    {
        if (map == null) {
            map = new HashMap<>();
        }
        String[] a = ((context != null) ? context.getResources().getStringArray(arrayResID) : new String[0]);
        for (String s : a)
        {
            String[] p = ((s != null) ? s.split("\\|") : new String[0]);
            if (p.length >= 2) {
                map.put(p[0], p[1]);
            }
        }
        return map;
    }

    /**
     * @param value "[a][block][of][tags]"
     * @return {"[a]", "[block]", "[of]", "[tags]"}
     */
    public static ArrayList<String> splitTags(String value)
    {
        if (value != null)
        {
            String[] values = value.split("]");
            ArrayList<String> r = new ArrayList<>();
            for (int i=0; i<values.length; i++) {
                if (values[i].startsWith("["))
                {
                    String tag = values[i] + "]";
                    r.add(tag);
                }
            }
            return r;
        } else return new ArrayList<>();
    }

    /**
     * @param value tag block; e.g. "[a][b][c][d]"
     * @param tagMap optional map of tag expansions; e.g. {"a" -> "a+, a*" }
     * @param includeOriginal include original when expanded; e.g. true
     * @return expanded tag block; e.g. "[a][b][c][d]" becomes "[a][a+][a*][b][c][d]"
     */
    public static String expandTags(String value, @NonNull HashMap<String, String> tagMap, boolean includeOriginal)
    {
        StringBuilder r = new StringBuilder(includeOriginal ? value : "");
        String[] values = value.split("]");
        for (String tag : values)
        {
            String tag0 = tag.trim() + "]";
            if (tagMap.containsKey(tag0))
            {
                String[] a = tagMap.get(tag0).split(",");
                for (String v : a)
                {
                    String expanded = "[" + v.trim() + "]";
                    if (!r.toString().contains(expanded)) {
                        r.append(expanded);
                    }
                }
            }

            if (tag0.contains("-"))
            {
                String[] tagParts = tag0.split("-");    // e.g. when [BR-SE] also match [BR]
                String tag1 = tagParts[0].trim() + "]";

                if (tagMap.containsKey(tag1))
                {
                    String[] a = tagMap.get(tag1).split(",");
                    for (String v : a)
                    {
                        String expanded = "[" + v.trim() + "]";
                        if (!r.toString().contains(expanded)) {
                            r.append(expanded);
                        }
                    }
                }
            }
        }
        return r.toString().trim();
    }

    /**
     * @param context Context
     * @param tags tag array; {"[a]","[b]","[c]"}
     * @param ignoreTags tags to ignore; {"[b]"}
     * @return display string; "a, c"
     */
    public static CharSequence tagDisplayString(Context context, ArrayList<String> tags, String[] ignoreTags)
    {
        int c = 0;
        StringBuilder r = new StringBuilder();
        outerLoop:
        for (int i=0; i<tags.size(); i++)
        {
            String tag = tags.get(i).trim();
            if (ignoreTags != null) {
                for (String ignored : ignoreTags) {
                    if (ignored != null && ignored.equals(tag)) {
                        continue outerLoop;
                    }
                }
            }
            if (c > 0) {
                r.append(", ");
            }
            r.append(tag.replaceAll("\\[", "")
                    .replaceAll("]", "").trim());
            c++;
        }
        return r.toString();
    }

}
