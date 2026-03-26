// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020-2024 Forrest Guice
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

import com.forrestguice.colors.Color;
import com.forrestguice.colors.ColorValues;
import com.forrestguice.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ColorValuesJSON
{
    public boolean loadColorValues(ColorValues colors, String jsonString)
    {
        try {
            JSONObject json = new JSONObject(jsonString);
            colors.setID(json.getString(ColorValues.KEY_ID));
            colors.setLabel(json.getString(ColorValues.KEY_LABEL));
            for (String key : colors.getColorKeys())
            {
                colors.setColor(key, json.has(key) ? Color.parseColor(json.getString(key).trim()) : colors.getFallbackColor());
                if (json.has(key + ColorValues.SUFFIX_LABEL)) {
                    colors.setLabel(key, json.getString(key + ColorValues.SUFFIX_LABEL).trim());
                }
            }
            return json.has(ColorValues.KEY_ID);

        } catch (JSONException e) {
            Log.e("ColorValues", "fromJSON: " + e);
            return false;
        }
    }

    public String toJSON(ColorValues colors) {
        return toJSON(colors, false);
    }

    public String toJSON(ColorValues colors, boolean withLabels)
    {
        JSONObject result = new JSONObject();
        try {
            result.put(ColorValues.KEY_ID, colors.getID());
            result.put(ColorValues.KEY_LABEL, colors.getLabel());
            for (String key : colors.getColorKeys())
            {
                result.put(key, "#" + Integer.toHexString(colors.getColor(key)));
                if (withLabels && colors.hasLabel(key)) {
                    result.put(key + ColorValues.SUFFIX_LABEL, colors.getLabel(key));
                }
            }
        } catch (JSONException e) {
            Log.e("ColorValues", "toJSON: " + e);
        }
        return result.toString();
    }
}