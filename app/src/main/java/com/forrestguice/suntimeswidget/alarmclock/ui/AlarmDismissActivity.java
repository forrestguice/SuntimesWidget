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

package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * AlarmDismissActivity
 */
public class AlarmDismissActivity extends AppCompatActivity
{
    private AlarmClockItem alarm = null;
    private TextView alarmTitle, alarmSubtitle, alarmText;
    private SuntimesUtils utils = new SuntimesUtils();

    public AlarmDismissActivity()
    {
        super();
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Context context = AppSettings.initLocale(newBase);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        setTheme(AppSettings.loadTheme(this));
        super.onCreate(icicle);
        initLocale(this);

        setContentView(R.layout.layout_activity_dismissalarm);
        alarmTitle = (TextView)findViewById(R.id.txt_alarm_label);
        alarmSubtitle = (TextView)findViewById(R.id.txt_alarm_label2);
        alarmText = (TextView)findViewById(R.id.txt_alarm_time);

        Button dismissButton = (Button) findViewById(R.id.btn_dismiss);
        dismissButton.setOnClickListener(onDismissClicked);

        Button snoozeButton = (Button) findViewById(R.id.btn_snooze);
        snoozeButton.setOnClickListener(onSnoozeClicked);

        Uri data = getIntent().getData();
        if (data != null) {
            if (AlarmNotifications.ACTION_DISMISS.equals(getIntent().getAction()))
            {
                Log.d("AlarmDismissActivity", "onCreate: ACTION_HANDLED: " + data);
                setResult(RESULT_CANCELED);
                finish();
            } else {
                setAlarmID(this, ContentUris.parseId(data));
            }
        } else {
            Log.e("AlarmDismissActivity", "Missing data uri! canceling..");
            finish();
        }
    }

    @Override
    public void onNewIntent( Intent intent )
    {
        super.onNewIntent(intent);
        if (intent != null)
        {
            String action = intent.getAction();
            if (action != null)
            {
                Uri newData = intent.getData();
                if (newData != null)
                {
                    if (action.equals(AlarmNotifications.ACTION_DISMISS)) {
                        Log.d("AlarmDismissActivity", "onNewIntent: ACTION_HANDLED: " + newData);
                        setResult(Activity.RESULT_CANCELED);
                        finish();

                    } else if (action.equals(AlarmNotifications.ACTION_SNOOZE)) {
                        // TODO

                    } else if (action.equals(Intent.ACTION_VIEW)) {
                        Log.d("AlarmDismissActivity", "onNewIntent: ACTION_VIEW: " + newData);
                        // TODO: what happens if two alarms overlap? this activity is already showing (onNewIntent called on second alarm)
                        setAlarmID(this, ContentUris.parseId(newData));

                    } else Log.e("AlarmDismissActivity", "onNewIntent: Unrecognized action! " + action);
                } else Log.w("AlarmDismissActivity", "onNewIntent: null data!");
            } else Log.w("AlarmDismissActivity", "onNewIntent: null action!");
        } else Log.w("AlarmDismissActivity", "onNewIntent: null Intent!");
    }

    private void initLocale(Context context)
    {
        WidgetSettings.initDefaults(context);
        WidgetSettings.initDisplayStrings(context);
        SuntimesUtils.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(context);
    }

    private View.OnClickListener onSnoozeClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (alarm != null) {
                Intent intent = AlarmNotifications.getSnoozeAlarmIntent(AlarmDismissActivity.this, alarm.getUri(), (int)alarm.rowID);
                sendBroadcast(intent);
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    };

    private View.OnClickListener onDismissClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (alarm != null) {
                Intent intent = AlarmNotifications.getDismissAlarmIntent(AlarmDismissActivity.this, alarm.getUri(), (int)alarm.rowID);
                sendBroadcast(intent);
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    };

    public void setAlarmID(final Context context, long alarmID)
    {
        AlarmDatabaseAdapter.AlarmItemTask task = new AlarmDatabaseAdapter.AlarmItemTask(context);
        task.setAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmItemTask.AlarmItemTaskListener() {
            @Override
            public void onItemLoaded(AlarmClockItem item) {
                setAlarmItem(context, item);
            }
        });
        task.execute(alarmID);
    }

    public void setAlarmItem(Context context, AlarmClockItem item)
    {
        alarm = item;

        String emptyLabel = context.getString(R.string.alarmMode_alarm);
        alarmTitle.setText((item.label == null || item.label.isEmpty()) ? emptyLabel : item.label);

        if (alarm.event != null) {
            alarmSubtitle.setText(item.event.getLongDisplayString());
            alarmSubtitle.setVisibility(View.VISIBLE);

        } else alarmSubtitle.setVisibility(View.GONE);

        SuntimesUtils.TimeDisplayText timeText = utils.calendarTimeShortDisplayString(context, item.getCalendar(), false);
        if (SuntimesUtils.is24()) {
            alarmText.setText(timeText.getValue());
        } else {
            String timeString = timeText.getValue() + " " + timeText.getSuffix();
            SpannableString timeDisplay = SuntimesUtils.createRelativeSpan(null, timeString, " " + timeText.getSuffix(), 0.40f);
            alarmText.setText(timeDisplay);
        }
    }

}