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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.forrestguice.suntimeswidget.R;

public class ClearPlacesTask extends AsyncTask<Object, Object, Boolean>
{
    private Context myParent;
    private GetFixDatabaseAdapter db;
    private ProgressDialog progress;

    public ClearPlacesTask(Context context)
    {
        myParent = context;
        db = new GetFixDatabaseAdapter(context.getApplicationContext());
    }

    public static final long MIN_WAIT_TIME = 2000;

    @Override
    protected Boolean doInBackground(Object... params)
    {
        long startTime = System.currentTimeMillis();
        db.open();
        boolean cleared = db.clearPlaces();
        db.close();
        long endTime = System.currentTimeMillis();

        while ((endTime - startTime) < MIN_WAIT_TIME)
        {
            endTime = System.currentTimeMillis();
        }
        return cleared;
    }

    @Override
    protected void onPreExecute()
    {
        progress = ProgressDialog.show(myParent, myParent.getString(R.string.locationcleared_dialog_title), myParent.getString(R.string.locationcleared_dialog_message), true);
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        progress.dismiss();
        Toast.makeText(myParent, myParent.getString(R.string.locationcleared_toast_success), Toast.LENGTH_LONG).show();
    }
}
