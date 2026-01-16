/**
    Copyright (C) 2018 Forrest Guice
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

package com.forrestguice.suntimeswidget.themes;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.forrestguice.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImportThemesTask extends AsyncTask<Uri, SuntimesTheme, ImportThemesTask.ImportThemesResult>
{
    public static final String TAG = "importThemesTask";
    public static final long MIN_WAIT_TIME = 2000;

    private final WeakReference<Context> contextRef;

    protected boolean isPaused = false;
    public void pauseTask()
    {
        isPaused = true;
    }
    public void resumeTask()
    {
        isPaused = false;
    }
    public boolean isPaused()
    {
        return isPaused;
    }

    public ImportThemesTask(Context context)
    {
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute()
    {
        Log.d(TAG, "onPreExecute");
        if (taskListener != null) {
            taskListener.onStarted();
        }
    }

    @Override
    protected ImportThemesResult doInBackground(Uri... params)
    {
        Log.d(TAG, "doInBackground: starting");
        Uri uri = null;
        if (params.length > 0) {
            uri = params[0];
        }

        long startTime = System.currentTimeMillis();
        boolean result = false;
        SuntimesTheme[] themes = null;
        Exception error = null;

        Context context = contextRef.get();
        if (context != null && uri != null)
        {
            SuntimesThemeIO xml = new SuntimesThemeXML();
            xml.setProgressListener(new SuntimesThemeIO.ProgressListener()
            {
                @Override
                public void onImported( SuntimesTheme theme, int i, int n ) {
                    onProgressUpdate(theme);
                }
            });

            try {
                InputStream in = context.getContentResolver().openInputStream(uri);
                if (in != null)
                {
                    Log.d(TAG, "doInBackground: reading");
                    BufferedInputStream input = new BufferedInputStream(in);
                    themes = xml.read(context, input);
                    result = true;
                    input.close();
                    error = null;

                } else {
                    Log.e(TAG, "Failed to import from " + uri + ": null input stream!");
                    result = false;
                    error = null;
                }

            } catch (FileNotFoundException e) {
                Log.e(TAG, "Failed to import from " + uri + ": " + e);
                result = false;
                themes = null;
                error = e;

            } catch (IOException e) {
                Log.e("ImportThemesTask", "Failed to import from " + uri + ": " + e);
                result = false;
                themes = null;
                error = e;
            }
        }

        Log.d(TAG, "doInBackground: waiting");
        long endTime = System.currentTimeMillis();
        while ((endTime - startTime) < MIN_WAIT_TIME || isPaused)
        {
            endTime = System.currentTimeMillis();
        }

        Log.d(TAG, "doInBackground: finishing");
        return new ImportThemesResult(result, uri, themes, error);
    }

    @Override
    protected void onProgressUpdate(SuntimesTheme... values)
    {
        super.onProgressUpdate(values);
        // TODO
    }

    @Override
    protected void onPostExecute( ImportThemesResult result )
    {
        Log.d(TAG, "onPostExecute: " + result.getResult());
        if (taskListener != null) {
            taskListener.onFinished(result);
        }
    }

    /**
     * ImportThemesResult
     */
    public static class ImportThemesResult
    {
        public ImportThemesResult(boolean result, Uri uri, @Nullable SuntimesTheme[] themes, Exception e)
        {
            this.result = result;
            this.themes = themes;
            this.uri = uri;
            this.e = e;
        }

        private final boolean result;
        public boolean getResult()
        {
            return result;
        }

        private final SuntimesTheme[] themes;
        public SuntimesTheme[] getThemes()
        {
            return themes;
        }

        private final Uri uri;
        public Uri getUri()
        {
            return uri;
        }

        public int numResults() {
            return (themes != null ? themes.length : 0);
        }

        private final Exception e;
        public Exception getException()
        {
            return e;
        }
    }

    /**
     * TaskListener
     */
    public static abstract class TaskListener
    {
        public void onStarted() {}
        public void onFinished( ImportThemesResult result ) {}
    }
    @Nullable
    protected TaskListener taskListener = null;
    public void setTaskListener( @Nullable TaskListener listener )
    {
        taskListener = listener;
    }
    public void clearTaskListener()
    {
        taskListener = null;
    }

}