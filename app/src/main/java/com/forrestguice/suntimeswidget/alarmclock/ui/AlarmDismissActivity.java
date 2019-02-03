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

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
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
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * AlarmDismissActivity
 */
public class AlarmDismissActivity extends AppCompatActivity
{
    public static final String TAG = "AlarmReceiverDismiss";
    public static final String EXTRA_MODE = "activityMode";
    public static final String BROADCAST_UPDATE = "com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockDismissActivity.UPDATE";

    private AlarmClockItem alarm = null;
    private String mode = null;

    private TextView alarmTitle, alarmSubtitle, alarmText, infoText;
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
        infoText = (TextView)findViewById(R.id.txt_snooze);
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

    private BroadcastReceiver updateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Uri data = intent.getData();
            Log.d(TAG, "updateReceiver.onReceive: " + data);

            if (action != null) {
                if (action.equals(BROADCAST_UPDATE)) {
                    if (data != null) {
                        setAlarmID(AlarmDismissActivity.this, ContentUris.parseId(data));
                    } else Log.e(TAG, "updateReceiver.onReceive: null data!");
                } else Log.e(TAG, "updateReceiver.onReceive: unrecognized action: " + action);
            } else Log.e(TAG, "updateReceiver.onReceive: null action!");
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        IntentFilter updateFilter = new IntentFilter();
        updateFilter.addAction(BROADCAST_UPDATE);
        updateFilter.addDataScheme("content");
        registerReceiver(updateReceiver, updateFilter);
    }

    @Override
    protected void onStop()
    {
        unregisterReceiver(updateReceiver);
        super.onStop();
    }

    @Override
    public void onRestoreInstanceState( Bundle bundle )
    {
        super.onRestoreInstanceState(bundle);
        setMode(bundle.getString(EXTRA_MODE));
    }

    @Override
    public void onSaveInstanceState( Bundle bundle )
    {
        super.onSaveInstanceState(bundle);
        bundle.putString(EXTRA_MODE, mode);
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
                snoozeButton.setEnabled(false);
                dismissButton.setEnabled(false);

                Intent intent = AlarmNotifications.getAlarmIntent(AlarmDismissActivity.this, AlarmNotifications.ACTION_SNOOZE, alarm.getUri());
                sendBroadcast(intent);
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
                snoozeButton.setEnabled(false);
                dismissButton.setEnabled(false);
                Intent intent = AlarmNotifications.getAlarmIntent(AlarmDismissActivity.this, AlarmNotifications.ACTION_DISMISS, alarm.getUri());
                sendBroadcast(intent);
            }
        }
    };

    private void setMode( @Nullable String action )
    {
        String prevMode = this.mode;
        this.mode = action;

        if (AlarmNotifications.ACTION_SNOOZE.equals(action))
        {
            SuntimesUtils.initDisplayStrings(this);
            SuntimesUtils.TimeDisplayText snoozeText = utils.timeDeltaLongDisplayString(0, AlarmSettings.loadPrefAlarmSnooze(this));
            String snoozeString = getString(R.string.alarmAction_snoozeMsg, snoozeText.getValue());
            SpannableString snoozeDisplay = SuntimesUtils.createBoldSpan(null, snoozeString, snoozeText.getValue());
            infoText.setText(snoozeDisplay);
            infoText.setVisibility(View.VISIBLE);

            icon.setDisplayedChild(1);
            snoozeButton.setVisibility(View.GONE);
            snoozeButton.setEnabled(false);
            dismissButton.setEnabled(true);

            boolean needsTransition = (!AlarmNotifications.ACTION_SNOOZE.equals(prevMode));
            if (needsTransition)
                animateBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF, 1000);
            else setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF);

        } else if (AlarmNotifications.ACTION_TIMEOUT.equals(action)) {
            infoText.setText(getString(R.string.alarmAction_timeoutMsg));
            infoText.setVisibility(View.VISIBLE);
            snoozeButton.setVisibility(View.GONE);
            snoozeButton.setEnabled(false);
            dismissButton.setEnabled(true);
            icon.setDisplayedChild(2);
            setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);

        } else {
            hardwareButtonPressed = false;
            infoText.setText("");
            infoText.setVisibility(View.GONE);
            snoozeButton.setVisibility(View.VISIBLE);
            snoozeButton.setEnabled(true);
            dismissButton.setEnabled(true);
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

    private void animateBrightness(float downToValue, @SuppressWarnings("SameParameterValue") int durationMillis)
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
            public void onItemLoaded(AlarmClockItem item)
            {
                if (item != null) {
                    setAlarmItem(context, item);
                }
            }
        });
        task.execute(alarmID);
    }

    public void setAlarmItem(@NonNull Context context, @NonNull AlarmClockItem item)
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

        if (alarm.state != null)
        {
            switch (alarm.state.getState())
            {
                case AlarmState.STATE_SNOOZING:
                    setMode(AlarmNotifications.ACTION_SNOOZE);
                    break;

                case AlarmState.STATE_TIMEOUT:
                    setMode(AlarmNotifications.ACTION_TIMEOUT);
                    break;

                default:
                    setMode(null);
                    break;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event)
    {
        if (mode == null && !hardwareButtonPressed)
        {
            int action = event.getAction();
            int keyCode = event.getKeyCode();

            if (action == KeyEvent.ACTION_DOWN)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_CAMERA:
                    case KeyEvent.KEYCODE_VOLUME_UP:
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        hardwareButtonPressed = true;
                        String alarmAction = AlarmSettings.loadPrefOnHardwareButtons(AlarmDismissActivity.this);
                        Intent intent = AlarmNotifications.getAlarmIntent(AlarmDismissActivity.this, alarmAction, alarm.getUri());
                        sendBroadcast(intent);
                        return true;

                    default:
                        return super.dispatchKeyEvent(event);
                }
            } else return super.dispatchKeyEvent(event);
        } else return super.dispatchKeyEvent(event);
    }
    private boolean hardwareButtonPressed = false;

}