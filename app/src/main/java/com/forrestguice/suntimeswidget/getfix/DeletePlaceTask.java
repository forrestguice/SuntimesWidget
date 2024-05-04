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

package com.forrestguice.suntimeswidget.getfix;

import android.content.Context;
import android.os.AsyncTask;

public class DeletePlaceTask extends AsyncTask<Long, Object, Boolean>
{
    private final GetFixDatabaseAdapter database;
    private Long[] rowIDs = new Long[] { -1L };

    public DeletePlaceTask(Context context) {
        database = new GetFixDatabaseAdapter(context.getApplicationContext());
    }

    @Override
    protected Boolean doInBackground(Long... params)
    {
        if (params.length > 0) {
            rowIDs = params;
        }

        boolean result = false;
        database.open();
        for (long rowID : rowIDs)
        {
            if (rowID != -1) {
                result = database.removePlace(rowID);
            }
        }
        database.close();
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        if (taskListener != null)
            taskListener.onFinished(result, rowIDs);
    }

    private TaskListener taskListener = null;
    public void setTaskListener( TaskListener listener ) {
        taskListener = listener;
    }
    public static abstract class TaskListener
    {
        public void onFinished( boolean result, Long... rowIDs ) {}
    }
}
