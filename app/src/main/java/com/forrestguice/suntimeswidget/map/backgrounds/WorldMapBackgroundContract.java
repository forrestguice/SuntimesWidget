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

/**
 * WorldMapBackgroundContract
 * @version 1 (0.1.0)
 *
 * Queries:
 * content://authority/backgroundList               .. lists all backgrounds
 * content://authority/backgroundList/<projection>  .. lists all backgrounds with given projection (e.g. eqc)
 *
 * CHANGES
 *   1 initial version
 */
public interface WorldMapBackgroundContract
{
    String AUTHORITY = "suntimeswidget.map.backgrounds.provider";
    String REQUIRED_PERMISSION = "suntimes.permission.READ_CALCULATOR";

    String COLUMN_BACKGROUND_ID = "background_id";                               // id (int)
    String COLUMN_BACKGROUND_TYPE = "background_type";                           // type string (day, night)
    String COLUMN_BACKGROUND_TITLE = "background_title";                         // short display string
    String COLUMN_BACKGROUND_SUMMARY = "background_summary";                     // long display string
    String COLUMN_BACKGROUND_FILE = "background_file";                           // file uri (string)
    String COLUMN_BACKGROUND_TINT = "background_tint";                           // request tint flag (boolean)
    String COLUMN_BACKGROUND_PROJECTION = "background_projection";               // proj4 string
    String COLUMN_BACKGROUND_PROJECTION_CENTER = "background_projection_center"; // lat,lon

    String QUERY_BACKGROUND_LIST = "backgroundList";
    String[] QUERY_BACKGROUND_LIST_PROJECTION = new String[] {
            COLUMN_BACKGROUND_ID, COLUMN_BACKGROUND_TYPE,
            COLUMN_BACKGROUND_TITLE, COLUMN_BACKGROUND_SUMMARY,
            COLUMN_BACKGROUND_PROJECTION, COLUMN_BACKGROUND_PROJECTION_CENTER,
            COLUMN_BACKGROUND_FILE, COLUMN_BACKGROUND_TINT
    };

    String TYPE_DAY = "day";
    String TYPE_NIGHT = "night";

    String PROJECTION_EQC = "eqc";
    String PROJECTION_AEQD_NORTH = "aeqd_90,0";
    String PROJECTION_AEQD_SOUTH = "aeqd_-90,0";
    String PROJECTION_AEQD_ = "aeqd_";
    String PROJECTION_MERC = "merc";
    String PROJECTION_SINU = "sinu";
    String PROJECTION_VANDG = "vandg";
    String PROJECTION_MOLL = "moll";
    String PROJECTION_HAMMER = "hammer";
}