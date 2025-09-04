/*
    Copyright (C) 2021 Forrest Guice
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

package com.forrestguice.suntimeswidget.actions;

/**
 * SuntimesActionsContract
 * @version 2 (0.1.1)
 *
 * CHANGES
 *   1 initial version
 *   2 adds COLUMN_ACTION_PACKAGE
 */
public interface SuntimesActionsContract
{
    String AUTHORITY = "suntimeswidget.action.provider";
    String VERSION_NAME = "v0.1.1";
    int VERSION_CODE = 2;

    String COLUMN_ACTION_PROVIDER_VERSION = "provider_version";                                       // String (provider version string)
    String COLUMN_ACTION_PROVIDER_VERSION_CODE = "provider_version_code";                             // int (provider version code)

    String COLUMN_ACTION_NAME = "name";                 // String (action ID)
    String COLUMN_ACTION_TITLE = "title";               // String (display string)
    String COLUMN_ACTION_DESC = "desc";                 // String (extended display string)
    String COLUMN_ACTION_COLOR = "color";               // int (color)
    String COLUMN_ACTION_TAGS = "tags";                 // String; a|pipe|delimited|list|of|tags

    String COLUMN_ACTION_CLASS = "launch";              // String or null; fully qualified class name
    String COLUMN_ACTION_PACKAGE = "package";           // String or null; package name
    String COLUMN_ACTION_TYPE = "type";                 // String; TYPE_*; ACTIVITY, BROADCAST, SERVICE
    String COLUMN_ACTION_ACTION = "action";             // String or null; e.g. android.action.view
    String COLUMN_ACTION_DATA = "data";                 // String or null; data uri
    String COLUMN_ACTION_MIMETYPE = "datatype";         // String or null; data mimetype
    String COLUMN_ACTION_EXTRAS = "extras";             // String or null; extras

    String TAG_DEFAULT = "default";
    String TAG_SUNTIMES = "suntimes";
    String TAG_SUNTIMESALARMS = "suntimesalarms";
    String TAG_CALENDAR = "calendar";
    String TAG_LOCATION = "location";
    String TAG_ALARM = "alarm";
    String TAG_SETTINGS = "settings";

    String TYPE_ACTIVITY = "ACTIVITY";                  // LaunchType enum names
    String TYPE_BROADCAST = "BROADCAST";
    String TYPE_SERVICE = "SERVICE";

    String QUERY_ACTIONS = "actions";
    String[] QUERY_ACTION_PROJECTION_MIN = new String[] { "_id",
            COLUMN_ACTION_NAME, COLUMN_ACTION_TITLE, COLUMN_ACTION_DESC,
    };
    String[] QUERY_ACTION_PROJECTION_FULL = new String[] { "_id",
            COLUMN_ACTION_NAME, COLUMN_ACTION_TITLE, COLUMN_ACTION_DESC, COLUMN_ACTION_COLOR, COLUMN_ACTION_TAGS,
            COLUMN_ACTION_CLASS, COLUMN_ACTION_PACKAGE, COLUMN_ACTION_TYPE, COLUMN_ACTION_ACTION, COLUMN_ACTION_DATA, COLUMN_ACTION_MIMETYPE, COLUMN_ACTION_EXTRAS
    };

}
