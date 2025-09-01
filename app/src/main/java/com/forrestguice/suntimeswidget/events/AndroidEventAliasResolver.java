/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;

public class AndroidEventAliasResolver implements EventItemResolver
{
    public String resolveSummary(Object contextObj, String uri)
    {
        Context context = (Context) contextObj;
        String retValue = null;
        if (uri != null && !uri.trim().isEmpty())
        {
            Cursor cursor = context.getContentResolver().query(Uri.parse(uri), new String[] { AlarmEventContract.COLUMN_EVENT_SUMMARY }, null, null, null);
            if (cursor != null)
            {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    int i = cursor.getColumnIndex(AlarmEventContract.COLUMN_EVENT_SUMMARY);
                    retValue = ((i >= 0) ? cursor.getString(i) : null);
                }
                cursor.close();
            }
        }
        return retValue;
    }
}
