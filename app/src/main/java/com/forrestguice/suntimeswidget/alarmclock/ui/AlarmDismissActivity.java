/**
    Copyright (C) 2018-2019 Forrest Guice
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

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.ArgbEvaluator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.Calendar;

/**
 * AlarmDismissActivity
 */
public class AlarmDismissActivity extends AppCompatActivity
{
    public static final String TAG = "AlarmReceiverDismiss";
    public static final String EXTRA_MODE = "activityMode";

    private AlarmClockItem alarm = null;
    private String mode = null;

    private TextView alarmTitle, alarmSubtitle, alarmText, clockText, offsetText, infoText;
    private TextView[] labels;

    private Button snoozeButton, dismissButton;
    private Button[] buttons;

    private ViewFlipper icon;
    private ImageView iconSounding, iconSnoozing;
    private SuntimesUtils utils = new SuntimesUtils();

    private int enabledColor, disabledColor, pressedColor, textColor, timeColor, titleColor;

    private int pulseSoundingDuration = 4000;
    private int pulseSoundingColor_start, pulseSoundingColor_end;

    private int pulseSnoozingDuration = 6000;
    private int pulseSnoozingColor_start, pulseSnoozingColor_end;


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
        initTheme(this);
        super.onCreate(icicle);
        initLocale(this);
        setContentView(R.layout.layout_activity_dismissalarm);
        initViews(this);
    }

    private String appTheme;
    private int appThemeResID;
    private SuntimesTheme appThemeOverride = null;

    private void initTheme(Context context)
    {
        appTheme = AppSettings.loadThemePref(this);
        appThemeResID = AppSettings.setTheme(this, appTheme);

        String themeName = AppSettings.getThemeOverride(this, appThemeResID);
        if (themeName != null) {
            Log.i(TAG, "initTheme: Overriding \"" + appTheme + "\" using: " + themeName);
            appThemeOverride = WidgetThemes.loadTheme(this, themeName);
        }
    }

    protected void initViews(Context context)
    {
        alarmTitle = (TextView)findViewById(R.id.txt_alarm_label);
        alarmSubtitle = (TextView)findViewById(R.id.txt_alarm_label2);
        alarmText = (TextView)findViewById(R.id.txt_alarm_time);
        clockText = (TextView)findViewById(R.id.txt_clock_time);
        offsetText = (TextView)findViewById(R.id.txt_alarm_offset);
        infoText = (TextView)findViewById(R.id.txt_snooze);

        icon = (ViewFlipper)findViewById(R.id.icon_alarm);
        iconSounding = (ImageView)findViewById(R.id.icon_alarm_sounding);
        iconSnoozing = (ImageView)findViewById(R.id.icon_alarm_snooze);

        dismissButton = (Button) findViewById(R.id.btn_dismiss);
        dismissButton.setOnClickListener(onDismissClicked);

        snoozeButton = (Button) findViewById(R.id.btn_snooze);
        snoozeButton.setOnClickListener(onSnoozeClicked);

        buttons = new Button[] {snoozeButton, dismissButton};
        labels = new TextView[] {alarmSubtitle, offsetText};
        if (Build.VERSION.SDK_INT >= 12) {
            stopAnimateColors(labels, buttons);
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
            Log.d(TAG, "updateReceiver.onReceive: " + data + " :: " + action);

            if (action != null) {
                if (action.equals(AlarmNotifications.ACTION_UPDATE_UI)) {
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
        registerReceiver(updateReceiver, AlarmNotifications.getUpdateBroadcastIntentFilter());
        clockText.post(updateClockTask);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null)
        {
            try {
                Log.d(TAG, "onCreate: " + data);
                setAlarmID(this, ContentUris.parseId(data));
                screenOn();

            } catch (NumberFormatException e) {
                Log.e(TAG, "onCreate: invalid data uri! canceling...");
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            Log.e(TAG, "onCreate: missing data uri! canceling...");
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onStop()
    {
        clockText.removeCallbacks(updateClockTask);
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

    @SuppressLint("ResourceType")
    private void initLocale(Context context)
    {
        WidgetSettings.initDefaults(context);
        WidgetSettings.initDisplayStrings(context);
        SuntimesUtils.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(context);
        AlarmClockItem.AlarmTimeZone.initDisplayStrings(context);

        int[] attrs = { R.attr.sunsetColor,  R.attr.sunriseColor, R.attr.dialogBackgroundAlt,
                R.attr.text_disabledColor, R.attr.buttonPressColor, R.attr.text_disabledColor,
                android.R.attr.textColorSecondary, android.R.attr.textColorPrimary
        };
        TypedArray a = context.obtainStyledAttributes(attrs);
        pulseSoundingColor_start = ContextCompat.getColor(context, a.getResourceId(0, R.color.sunIcon_color_setting_dark));
        pulseSoundingColor_end = ContextCompat.getColor(context, a.getResourceId(1, R.color.sunIcon_color_rising_dark));
        pulseSnoozingColor_start = ContextCompat.getColor(context, a.getResourceId(2, android.R.color.darker_gray));
        pulseSnoozingColor_end = ContextCompat.getColor(context, a.getResourceId(3, android.R.color.white));
        pressedColor = enabledColor = ContextCompat.getColor(context, a.getResourceId(4, R.color.btn_tint_pressed_dark));
        disabledColor = ContextCompat.getColor(context, a.getResourceId(5, R.color.text_disabled_dark));
        textColor = ContextCompat.getColor(context, a.getResourceId(6, android.R.color.secondary_text_dark));
        timeColor = titleColor = ContextCompat.getColor(context, a.getResourceId(7, android.R.color.primary_text_dark));
        a.recycle();

        if (appThemeOverride != null)
        {
            pressedColor = enabledColor = appThemeOverride.getActionColor();
            pulseSoundingColor_start = appThemeOverride.getSunsetTextColor();
            pulseSoundingColor_end = appThemeOverride.getSunriseTextColor();
            timeColor = appThemeOverride.getTimeColor();
            titleColor = appThemeOverride.getTitleColor();
            textColor = appThemeOverride.getTextColor();
        }
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
            if (Build.VERSION.SDK_INT >= 12) {
                animateColors(labels, buttons, iconSnoozing, pulseSnoozingColor_start, pulseSnoozingColor_end, pulseSnoozingDuration, new AccelerateDecelerateInterpolator());
            }
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

            if (Build.VERSION.SDK_INT >= 17)  // BUG: on some older devices modifying brightness turns off the screen
            {
                float dimScreenValue = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
                boolean needsTransition = (!AlarmNotifications.ACTION_SNOOZE.equals(prevMode));
                if (needsTransition) {
                    animateBrightness(dimScreenValue, 1000);
                } else {
                    setBrightness(dimScreenValue);
                }
            }

        } else if (AlarmNotifications.ACTION_TIMEOUT.equals(action)) {
            if (Build.VERSION.SDK_INT >= 11) {
                stopAnimateColors(labels, buttons);
            }
            infoText.setText(getString(R.string.alarmAction_timeoutMsg));
            infoText.setVisibility(View.VISIBLE);
            snoozeButton.setVisibility(View.GONE);
            snoozeButton.setEnabled(false);
            dismissButton.setEnabled(true);
            icon.setDisplayedChild(2);
            setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);

        } else {
            if (Build.VERSION.SDK_INT >= 11) {
                animateColors(labels, buttons, iconSounding, pulseSoundingColor_start, pulseSoundingColor_end, pulseSoundingDuration, new AccelerateInterpolator());
            }
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

    private static void colorizeButtonCompoundDrawable(int color, @NonNull Button button)
    {
        Drawable[] drawables = button.getCompoundDrawables();
        for (Drawable d : drawables) {
            if (d != null) {
                d.mutate();
                d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
        button.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    private void setBrightness(float toValue)
    {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = toValue;
        getWindow().setAttributes(layoutParams);
    }

    @TargetApi(12)
    private void animateBrightness(float downToValue, @SuppressWarnings("SameParameterValue") int durationMillis)
    {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        float startValue = layoutParams.screenBrightness;
        ValueAnimator animator = ValueAnimator.ofFloat(startValue, downToValue);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @TargetApi(12)
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

    private Object animationObj;
    @TargetApi(12)
    private void animateColors(final TextView[] labels, final Button[] buttons, final ImageView icon, int startColor, int endColor, long duration, @Nullable TimeInterpolator interpolator)
    {
        if (icon != null && labels != null)
        {
            ValueAnimator animation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
            animationObj = animation;
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animator)
                {
                    int animatedValue = (int) animator.getAnimatedValue();
                    icon.setColorFilter(animatedValue);

                    for (TextView label : labels) {
                        if (label != null) {
                            label.setTextColor(animatedValue);
                        }
                    }

                    for (Button button : buttons) {
                        if (button != null) {
                            button.setTextColor(animatedValue);
                            colorizeButtonCompoundDrawable(animatedValue, button);
                        }
                    }
                }
            });
            if (Build.VERSION.SDK_INT >= 11) {
                animation.setRepeatCount(ValueAnimator.INFINITE);
                animation.setRepeatMode(ValueAnimator.REVERSE);
            }
            if (interpolator != null) {
                animation.setInterpolator(interpolator);
            }
            animation.setDuration(duration);
            animation.start();
        }
    }
    @TargetApi(11)
    private void stopAnimateColors(TextView[] labels, Button[] buttons)
    {
        clockText.setTextColor(timeColor);
        //alarmTitle.setTextColor(titleColor);

        ValueAnimator animation = (ValueAnimator)animationObj;
        if (animation != null) {
            animation.removeAllUpdateListeners();
        }

        for (TextView label : labels){
            if (label != null) {
                label.setTextColor(textColor);
            }
        }

        ColorStateList buttonColors = SuntimesUtils.colorStateList(enabledColor, disabledColor, pressedColor);
        for (Button button : buttons) {
            if (button != null) {
                button.setTextColor(buttonColors);
                colorizeButtonCompoundDrawable(enabledColor, button);
            }
        }
    }

    public void setAlarmID(final Context context, long alarmID)
    {
        AlarmDatabaseAdapter.AlarmItemTask task = new AlarmDatabaseAdapter.AlarmItemTask(context);
        task.addAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener() {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item)
            {
                if (item != null) {
                    setAlarmItem(context, item);
                }
            }
        });
        task.execute(alarmID);
    }

    public static final int CLOCK_UPDATE_RATE = 3000;
    private Runnable updateClockTask = new Runnable()
    {
        @Override
        public void run()
        {
            SuntimesUtils.TimeDisplayText timeText = utils.calendarTimeShortDisplayString(AlarmDismissActivity.this, Calendar.getInstance(), false);
            if (SuntimesUtils.is24()) {
                clockText.setText(timeText.getValue());
            } else {
                String timeString = timeText.getValue() + " " + timeText.getSuffix();
                SpannableString timeDisplay = SuntimesUtils.createRelativeSpan(null, timeString, " " + timeText.getSuffix(), 0.40f);
                clockText.setText(timeDisplay);
            }
            clockText.postDelayed(this, CLOCK_UPDATE_RATE);
        }
    };

    @SuppressLint("SetTextI18n")
    public void setAlarmItem(@NonNull Context context, @NonNull AlarmClockItem item)
    {
        alarm = item;

        String emptyLabel = context.getString(R.string.alarmMode_alarm);
        alarmTitle.setText((item.label == null || item.label.isEmpty()) ? emptyLabel : item.label);

        String eventString = alarm.getEvent();
        if (eventString != null)
        {
            AlarmEvent.AlarmEventItem eventItem = new AlarmEvent.AlarmEventItem(eventString, context.getContentResolver());
            alarmSubtitle.setText(eventItem.getTitle());
            alarmSubtitle.setVisibility(View.VISIBLE);

        } else if (alarm.timezone != null) {
            Calendar eventTime = Calendar.getInstance(AlarmClockItem.AlarmTimeZone.getTimeZone(item.timezone, item.location));
            eventTime.set(Calendar.HOUR_OF_DAY, item.hour);
            eventTime.set(Calendar.MINUTE, item.minute);
            alarmSubtitle.setText(utils.calendarTimeShortDisplayString(context, eventTime) + "\n" + AlarmClockItem.AlarmTimeZone.displayString(item.timezone));
            alarmSubtitle.setVisibility(View.VISIBLE);

         } else {
            alarmSubtitle.setVisibility(View.GONE);
        }

        Spannable offsetSpan = new SpannableString("");
        if (item.offset != 0)
        {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(item.timestamp);
            int alarmHour = alarmTime.get( SuntimesUtils.is24() ? Calendar.HOUR_OF_DAY : Calendar.HOUR );
            boolean isBefore = (item.offset <= 0);
            String offsetText = utils.timeDeltaLongDisplayString(0, item.offset).getValue();
            String offsetDisplay = context.getResources().getQuantityString((isBefore ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), alarmHour, offsetText);
            offsetSpan = SuntimesUtils.createBoldSpan(null, offsetDisplay, offsetText);
        }
        offsetText.setText(offsetSpan);

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
                case AlarmState.STATE_SOUNDING:
                    setMode(null);
                    break;

                case AlarmState.STATE_SNOOZING:
                    setMode(AlarmNotifications.ACTION_SNOOZE);
                    break;

                case AlarmState.STATE_TIMEOUT:
                    setMode(AlarmNotifications.ACTION_TIMEOUT);
                    break;

                default:
                    finish();
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

    private void screenOn()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON   // BUG: turning the screen on this way works once (first time) .. after an alarm snoozes (and the device falls back asleep) this won't work the second time
                //| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON            // a potential workaround is to keep the screen on ... but this might consume noticeable battery.
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

}