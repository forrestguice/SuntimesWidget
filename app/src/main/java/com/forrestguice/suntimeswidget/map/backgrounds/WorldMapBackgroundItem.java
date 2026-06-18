/**
    Copyright (C) 2026 Forrest Guice
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

package com.forrestguice.suntimeswidget.map.backgrounds;

import static com.forrestguice.suntimeswidget.map.backgrounds.WorldMapBackgroundContract.TYPE_DAY;

/**
 * @see WorldMapBackgroundContract
 */
public class WorldMapBackgroundItem
{
    public WorldMapBackgroundItem() {}
    public WorldMapBackgroundItem(String providerUri, String type, String id, String title, String summary, String mapProjection, String mapProjectionCenter, String fileUri, String tint)
    {
        this.provider_uri = providerUri;
        this.type = type;
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.map_projection_center = parseCenter(mapProjectionCenter);
        this.map_projection = mapProjection;
        this.file_uri = fileUri;
        this.tint = Boolean.parseBoolean(tint);
        isValid = true;
    }

    public WorldMapBackgroundItem(String provider_uri, String id, String[] manifest)
    {
        this.provider_uri = provider_uri;
        this.id = id;
        this.file_uri = manifest[0];
        this.type = manifest[1];
        this.title = manifest[2];
        this.summary = manifest[3];
        this.map_projection = manifest[4];
        this.map_projection_center = parseCenter(manifest[5]);
        this.tint = Boolean.parseBoolean(manifest[6]);
        isValid = true;
    }

    protected boolean isValid = false;
    public void setIsValid(boolean value) {
        isValid = value;
    }
    public boolean isValid() {
        return isValid;
    }

    protected String id = null;
    public void setID(String value) {
        id = value;
    }
    public String getID() {
        return id;
    }

    protected String title;
    public void setTitle(String value) {
        title = (value != null ? value : "");
    }
    public String getTitle() {
        return title;
    }

    protected String summary;
    public void setSummary(String value) {
        summary = value;
    }
    public String getSummary() {
        return summary;
    }

    protected String map_projection;
    public void setMapProjection(String value) {
        map_projection = value;
    }
    public String getMapProjection() {
        return map_projection;
    }

    protected double[] map_projection_center;
    public void setMapProjectionCenter(String s) {
        map_projection_center = parseCenter(s);
    }
    public double[] getMapProjectionCenter() {
        return map_projection_center;
    }
    public String getMapProjectionCenterAsString()
    {
        if (map_projection_center != null)
        {
            StringBuilder result = new StringBuilder();
            for (int i=0; i<map_projection_center.length; i++)
            {
                result.append(map_projection_center[i]);
                if (i != map_projection_center.length-1) {
                    result.append(",");
                }
            }
            return result.toString();
        } else return null;
    }

    protected String provider_uri;
    public void setProviderUri(String value) {
        provider_uri = value;
    }
    public String getProviderUri() {
        return provider_uri;
    }

    protected String file_uri;
    public void setUri(String value) {
        file_uri = value;
    }
    public String getUri() {
        return file_uri;
    }

    protected boolean tint;
    public void setShouldTint(boolean value) {
        tint = value;
    }
    public boolean shouldTint() {
        return tint;
    }

    /**
     * @param s containing "latitude,longitude" string. e.g. "90,0" centers on north pole.
     * @return [90d,0d] or null if s was invalid
     */
    public static double[] parseCenter(String s)
    {
        double[] center = null;
        String[] center0 = (s != null ? s.split(",") : null);
        if (center0 != null)
        {
            try {
                center = new double[center0.length];
                for (int i=0; i<center0.length; i++) {
                    center[i] =  Double.parseDouble(center0[i]);
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return center;
    }

    protected String type = TYPE_DAY;
    public String getType() {
        return type;
    }
    public void setType(String value) {
        type = value;
    }
}