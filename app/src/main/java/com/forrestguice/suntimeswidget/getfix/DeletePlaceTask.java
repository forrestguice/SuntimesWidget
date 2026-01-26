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

import com.forrestguice.annotation.NonNull;

import java.util.concurrent.Callable;

public class DeletePlaceTask implements Callable<DeletePlaceTask.TaskResult>
{
    private final GetFixDatabaseAdapter database;
    private final Long[] rowIDs;

    public DeletePlaceTask(Context context, Long[] rowIDs) {
        database = new GetFixDatabaseAdapter(context.getApplicationContext());
        this.rowIDs = rowIDs;
    }

    @Override
    public TaskResult call() throws Exception
    {
        boolean result = false;
        database.open();
        for (long rowID : rowIDs)
        {
            if (rowID != -1) {
                result = database.removePlace(rowID);
            }
        }
        database.close();
        return new TaskResult(result, rowIDs);
    }

    public static class TaskResult
    {
        public TaskResult(@NonNull Boolean result, @NonNull Long[] rowIDs)
        {
            this.result = result;
            this.rowIDs = rowIDs;
        }

        private final Boolean result;
        @NonNull
        public Boolean getResult() {
            return result;
        }

        private final Long[] rowIDs;
        @NonNull
        public Long[] getRowIDs() {
            return rowIDs;
        }
    }
}
