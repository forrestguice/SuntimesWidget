/**
    Copyright (C) 2014 Forrest Guice
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

public class ClearPlacesTask extends AsyncTask<Object, Object, Boolean>
{
    public static final long MIN_WAIT_TIME = 2000;

    private GetFixDatabaseAdapter db;

    private boolean isPaused = false;
    public void pauseTask()
    {
        isPaused = true;
        //Log.d("DEBUG", "ClearPlacesTask paused");
    }
    public void resumeTask()
    {
        isPaused = false;
        //Log.d("DEBUG", "ClearPlacesTask resumed");
    }
    public boolean isPaused()
    {
        return isPaused;
    }

    public ClearPlacesTask(Context context)
    {
        db = new GetFixDatabaseAdapter(context.getApplicationContext());
    }

    @Override
    protected Boolean doInBackground(Object... params)
    {
        long startTime = System.currentTimeMillis();
        db.open();
        boolean wasCleared = db.clearPlaces();
        db.close();
        long endTime = System.currentTimeMillis();

        while ((endTime - startTime) < MIN_WAIT_TIME || isPaused)
        {
            endTime = System.currentTimeMillis();
        }
        return wasCleared;
    }

    @Override
    protected void onPreExecute()
    {
        signalStarted();
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        signalFinished(result);
    }

    /**
     * Event Listener
     */
    private TaskListener taskListener = null;
    public void setTaskListener( TaskListener listener )
    {
        taskListener = listener;
    }
    public void clearTaskListener()
    {
        taskListener = null;
    }
    public static abstract class TaskListener
    {
        public void onStarted() {}
        public void onFinished( Boolean result ) {}
    }

    private void signalStarted()
    {
        if (taskListener != null)
            taskListener.onStarted();
    }
    private void signalFinished( Boolean result )
    {
        if (taskListener != null)
            taskListener.onFinished(result);
    }
}
