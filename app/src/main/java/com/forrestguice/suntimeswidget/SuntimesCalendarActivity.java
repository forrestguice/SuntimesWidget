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

package com.forrestguice.suntimeswidget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarAdapter;
import com.forrestguice.suntimeswidget.calendar.SuntimesCalendarTask;
import com.forrestguice.suntimeswidget.settings.AppSettings;

public class SuntimesCalendarActivity extends AppCompatActivity
{
    public SuntimesCalendarActivity()
    {
        super();
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
        initLocale(this);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.layout_calendar);

        Button btnInit = (Button)findViewById(R.id.btn_initcalendar);
        btnInit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkPermissions(SuntimesCalendarActivity.this))
                {
                    SuntimesCalendarTask calendarTask = new SuntimesCalendarTask(SuntimesCalendarActivity.this);
                    calendarTask.execute();
                }
            }
        });

        Button btnDelete = (Button)findViewById(R.id.btn_deletecalendar);
        btnDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkPermissions(SuntimesCalendarActivity.this))
                {
                    SuntimesCalendarTask calendarTask = new SuntimesCalendarTask(SuntimesCalendarActivity.this);
                    calendarTask.setFlagClearCalendars(true);
                    calendarTask.execute();
                }
            }
        });
    }

    private void initLocale(Context context)
    {
        AppSettings.initLocale(context);
        SuntimesUtils.initDisplayStrings(context);
    }

    private boolean checkPermissions(Activity context)
    {
        int calendarPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR);
        if (calendarPermission != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(context, new String[] { Manifest.permission.WRITE_CALENDAR }, 0);
            return false;
        }
        return true;
    }

}
