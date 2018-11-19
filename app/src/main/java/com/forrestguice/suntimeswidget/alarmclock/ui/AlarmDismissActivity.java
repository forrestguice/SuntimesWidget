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

import android.animation.ValueAnimator;
import android.app.Activity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

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
    public static final String TAG = "AlarmReceiverDismiss";
    public static final String EXTRA_SNOOZING = "isSnoozing";

    private AlarmClockItem alarm = null;
    private boolean isSnoozing = false;

    private TextView alarmTitle, alarmSubtitle, alarmText, snoozeText;
    private Button snoozeButton, dismissButton;
    private ViewFlipper icon;
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
        snoozeText = (TextView)findViewById(R.id.txt_snooze);
        icon = (ViewFlipper)findViewById(R.id.icon_alarm);

        dismissButton = (Button) findViewById(R.id.btn_dismiss);
        dismissButton.setOnClickListener(onDismissClicked);

        snoozeButton = (Button) findViewById(R.id.btn_snooze);
        snoozeButton.setOnClickListener(onSnoozeClicked);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null)
        {
            Log.d(TAG, "onCreate: " + data);
            setAlarmID(this, ContentUris.parseId(data));

        } else {
            Log.e(TAG, "onCreate: missing data uri! canceling...");
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onRestoreInstanceState( Bundle bundle )
    {
        super.onRestoreInstanceState(bundle);
        isSnoozing = bundle.getBoolean(EXTRA_SNOOZING, false);
        setMode(isSnoozing ? AlarmNotifications.ACTION_SNOOZE : null);
    }

    @Override
    public void onSaveInstanceState( Bundle bundle )
    {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(EXTRA_SNOOZING, isSnoozing);
    }

    @Override
    public void onNewIntent( Intent intent )
    {
        super.onNewIntent(intent);
        if (intent != null)
        {
            Uri newData = intent.getData();
            if (newData != null)
            {
                Log.d(TAG, "onNewIntent: " + newData);
                setAlarmID(this, ContentUris.parseId(newData));

            } else Log.w(TAG, "onNewIntent: null data!");
        } else Log.w(TAG, "onNewIntent: null Intent!");
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
                Log.d(TAG, "onSnoozeClicked");
                Intent intent = AlarmNotifications.getAlarmIntent(AlarmDismissActivity.this, alarm.getUri(), (int)alarm.rowID);
                intent.setAction(AlarmNotifications.ACTION_SNOOZE);
                sendBroadcast(intent);
                setMode(AlarmNotifications.ACTION_SNOOZE);
            }
        }
    };

    private View.OnClickListener onDismissClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (alarm != null) {
                Log.d(TAG, "onDismissedClicked");
                Intent intent = AlarmNotifications.getAlarmIntent(AlarmDismissActivity.this, alarm.getUri(), (int)alarm.rowID);
                intent.setAction(AlarmNotifications.ACTION_DISMISS);
                sendBroadcast(intent);
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    };

    private void setMode( @Nullable String action )
    {
        boolean shouldSnooze = AlarmNotifications.ACTION_SNOOZE.equals(action);
        boolean needsTransition = (shouldSnooze != isSnoozing);
        isSnoozing = shouldSnooze;

        if (shouldSnooze)
        {
            snoozeText.setText("Snoozing");            // TODO
            snoozeText.setVisibility(View.VISIBLE);
            snoozeButton.setVisibility(View.GONE);
            icon.setDisplayedChild(1);
            if (needsTransition)
                animateBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF, 1000);
            else setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF);

        } else {
            snoozeText.setVisibility(View.GONE);
            snoozeButton.setVisibility(View.VISIBLE);
            icon.setDisplayedChild(0);
            setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        }
    }

    private void setBrightness(float toValue)
    {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = toValue;
        getWindow().setAttributes(layoutParams);
    }

    private void animateBrightness(float downToValue, int durationMillis)
    {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        float startValue = layoutParams.screenBrightness;
        ValueAnimator animator = ValueAnimator.ofFloat(startValue, downToValue);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.screenBrightness = valueAnimator.getAnimatedFraction();
                getWindow().setAttributes(params);
            }
        });
        animator.setDuration(durationMillis);
        animator.reverse();
    }

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