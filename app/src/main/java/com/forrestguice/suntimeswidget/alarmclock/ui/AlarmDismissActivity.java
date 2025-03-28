/**
    Copyright (C) 2018-2024 Forrest Guice
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

import android.animation.ArgbEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.design.widget.FloatingActionButton;
import com.forrestguice.support.design.app.AlertDialog;
import com.forrestguice.support.design.app.AppCompatActivity;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmAddon;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.alarmclock.ui.colors.AlarmColorValues;
import com.forrestguice.suntimeswidget.alarmclock.ui.colors.BrightAlarmColorValues;
import com.forrestguice.suntimeswidget.alarmclock.ui.colors.BrightAlarmColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.WidgetThemes;
import com.forrestguice.suntimeswidget.settings.colors.ColorUtils;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/**
 * AlarmDismissActivity
 */
public class AlarmDismissActivity extends AppCompatActivity implements AlarmDismissInterface
{
    public static final String TAG = "AlarmReceiverDismiss";
    public static final String EXTRA_MODE = "activityMode";

    public static final String EXTRA_TEST = "test";
    public static final String EXTRA_TEST_CHALLENGE_ID = "testChallengeID";
    public static final String EXTRA_TEST_BRIGHTMODE = "testBrightMode";
    public static final String EXTRA_TEST_BRIGHTMODE_ID = "testBrightModeID";

    public static final String ACTION_PREVIEW = "suntimeswidget.alarm.preview";
    public static final String ACTION_SNOOZE = AlarmNotifications.ACTION_SNOOZE;
    public static final String ACTION_DISMISS = AlarmNotifications.ACTION_DISMISS;

    public static final String MODE_SCHEDULED = AlarmNotifications.ACTION_SCHEDULE;
    public static final String MODE_SOUNDING = AlarmNotifications.ACTION_SHOW;
    public static final String MODE_SNOOZING = AlarmNotifications.ACTION_SNOOZE;
    public static final String MODE_TIMEOUT = AlarmNotifications.ACTION_TIMEOUT;

    public static final int REQUEST_DISMISS_CHALLENGE = 100;

    private AlarmClockItem alarm = null;
    private String mode = null, prevMode = null;

    private boolean isTesting = false;
    private int testChallengeID = -1;

    private TextView alarmTitle, alarmSubtitle, alarmText, clockText, timezoneText, offsetText, infoText, noteText;
    private TextView[] labels;

    private FloatingActionButton backButton;
    private AlarmButton dismissButton;
    private AlarmButton snoozeButton;
    private AlarmButton[] buttons;

    private View background;
    private ViewFlipper icon;
    private ImageView iconSounding, iconSnoozing;
    private final SuntimesUtils utils = new SuntimesUtils();

    private int pulseSoundingDuration = 4000;
    private int pulseSnoozingDuration = 6000;
    private int snoozingDimmingDuration, snoozingScreenOnDuration;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.layout_activity_dismissalarm);

        Intent intent = getIntent();
        if (intent != null) {
            isTesting = intent.getBooleanExtra(EXTRA_TEST, isTesting);
            testChallengeID = intent.getIntExtra(EXTRA_TEST_CHALLENGE_ID, testChallengeID);
            Log.d("DEBUG", "onCreate: isTesting: " + isTesting + ", testChallengeID: " + testChallengeID);
        }

        initViews(this);
    }

    private String appTheme;
    private int appThemeResID;
    private SuntimesTheme appThemeOverride = null;
    private boolean isBrightMode = false;
    private ColorValues colors;

    private void initTheme(Context context)
    {
        isBrightMode = AlarmSettings.loadPrefAlarmBrightMode(this) || getIntent().getBooleanExtra(EXTRA_TEST_BRIGHTMODE, false);
        appTheme = //(isBrightMode ? AppSettings.AppThemeInfo.getExtendedThemeName("light", AppSettings.loadTextSizePref(context)) :
                AppSettings.loadThemePref(this);
        appThemeResID = AppSettings.setTheme(this, appTheme);

        String themeName = AppSettings.getThemeOverride(this, appTheme);
        if (themeName != null && WidgetThemes.hasValue(themeName)) {
            Log.i(TAG, "initTheme: Overriding \"" + appTheme + "\" using: " + themeName);
            appThemeOverride = WidgetThemes.loadTheme(this, themeName);
        }

        if (isBrightMode)
        {
            BrightAlarmColorValuesCollection<BrightAlarmColorValues> collection = new BrightAlarmColorValuesCollection<>(context);
            String param_colorsID = getIntent().getStringExtra(EXTRA_TEST_BRIGHTMODE_ID);

            colors = (!getIntent().hasExtra(EXTRA_TEST_BRIGHTMODE)) ? collection.getSelectedColors(context, 0, BrightAlarmColorValues.TAG_ALARMCOLORS)
                                                                    : collection.getColors(context, param_colorsID);
            if (colors == null) {
                colors = new BrightAlarmColorValues(context, false);
            }

        } else {
            colors = new AlarmColorValues(context, true);
        }

    }

    protected void initViews(Context context)
    {
        background = findViewById(R.id.background);

        alarmTitle = (TextView)findViewById(R.id.txt_alarm_label);
        alarmTitle.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_PRIMARY));

        alarmSubtitle = (TextView)findViewById(R.id.txt_alarm_label2);
        alarmSubtitle.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_PRIMARY));

        alarmText = (TextView)findViewById(R.id.txt_alarm_time);
        alarmText.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_SECONDARY));

        clockText = (TextView)findViewById(R.id.txt_clock_time);
        clockText.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_TIME));

        timezoneText = (TextView)findViewById(R.id.txt_clock_timezone);
        timezoneText.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_SECONDARY));

        offsetText = (TextView)findViewById(R.id.txt_alarm_offset);
        offsetText.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_SECONDARY));

        infoText = (TextView)findViewById(R.id.txt_snooze);
        infoText.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_SECONDARY));

        noteText = (TextView)findViewById(R.id.txt_alarm_note);
        noteText.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_SECONDARY));

        icon = (ViewFlipper)findViewById(R.id.icon_alarm);
        iconSounding = (ImageView)findViewById(R.id.icon_alarm_sounding);
        iconSnoozing = (ImageView)findViewById(R.id.icon_alarm_snooze);

        dismissButton = (AlarmButton) findViewById(R.id.btn_dismiss);
        dismissButton.setOnClickListener(onDismissClicked);

        snoozeButton = (AlarmButton) findViewById(R.id.btn_snooze);
        snoozeButton.setOnClickListener(onSnoozeClicked);
        snoozeButton.setVisibility(isTesting ? View.VISIBLE : View.GONE);

        buttons = new AlarmButton[] {snoozeButton, dismissButton};

        backButton = (FloatingActionButton) findViewById(R.id.btn_back);
        backButton.setOnClickListener(onBackClicked);
        backButton.hide();

        labels = new TextView[] {alarmSubtitle, offsetText};

        resetAnimateColors(labels, buttons);
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
                Log.d(TAG, "onNewIntent: " + newData + ", action: " + intent.getAction());

                AlarmDatabaseAdapter.AlarmItemTaskListener onLoaded = null;
                if (ACTION_DISMISS.equals(intent.getAction()))
                {
                    Log.i(TAG, "onResume: ACTION_DISMISS");
                    intent.setAction(null);
                    onLoaded = new AlarmDatabaseAdapter.AlarmItemTaskListener() {
                        @Override
                        public void onFinished(Boolean result, AlarmClockItem item) {
                            dismissAlarmAfterChallenge(AlarmDismissActivity.this, dismissButton);
                        }
                    };
                }
                setAlarmID(this, newData, onLoaded);

            } else Log.w(TAG, "onNewIntent: null data!");
        } else Log.w(TAG, "onNewIntent: null Intent!");
    }

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver()
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
                        setAlarmID(AlarmDismissActivity.this, data);
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
        AlarmDatabaseAdapter.AlarmItemTaskListener onLoaded = null;
        if (ACTION_DISMISS.equals(intent.getAction()))
        {
            Log.i(TAG, "onResume: ACTION_DISMISS");
            intent.setAction(null);
            onLoaded = new AlarmDatabaseAdapter.AlarmItemTaskListener() {
                @Override
                public void onFinished(Boolean result, AlarmClockItem item) {
                    dismissAlarmAfterChallenge(AlarmDismissActivity.this, dismissButton);
                }
            };

        } else if (ACTION_SNOOZE.equals(intent.getAction())) {
            Log.i(TAG, "onResume: ACTION_SNOOZE");
            intent.setAction(null);
            snoozeAlarm(this);
        }

        Uri data = intent.getData();
        if (data != null)
        {
            try {
                Log.d(TAG, "onResume: " + data);
                setAlarmID(this, data, onLoaded);
                if (!isInteractive()) {
                    screenOn();
                }

            } catch (NumberFormatException e) {
                Log.e(TAG, "onResume: invalid data uri! canceling...");
                setResult(RESULT_CANCELED);
                finish();
            }
        } else {
            Log.e(TAG, "onResume: missing data uri! canceling...");
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
        AlarmClockItem item = bundle.getParcelable("alarmItem");
        if (item != null) {
            setAlarmItem(this, item);
        }
    }

    @Override
    public void onSaveInstanceState( Bundle bundle )
    {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable("alarmItem", this.alarm);
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

        //int[] bgColors = AlarmSettings.loadPrefAlarmBrightColors(context);
        //colors.setColor(AlarmColorValues.COLOR_BRIGHT_BACKGROUND_START, bgColors[0]);
        //colors.setColor(AlarmColorValues.COLOR_BRIGHT_BACKGROUND_END, bgColors[1]);

        pulseSoundingDuration = getResources().getInteger(R.integer.anim_alarmscreen_sounding_pulse_duration);
        pulseSnoozingDuration = getResources().getInteger(R.integer.anim_alarmscreen_snoozing_pulse_duration);
        snoozingDimmingDuration = getResources().getInteger(R.integer.anim_alarmscreen_snoozing_dimming_duration);
        snoozingScreenOnDuration = getResources().getInteger(R.integer.anim_alarmscreen_snoozing_screenon_duration);

        if (appThemeOverride != null)
        {
            colors.setColor(AlarmColorValues.COLOR_CONTROL_ENABLED, appThemeOverride.getActionColor());
            colors.setColor(AlarmColorValues.COLOR_CONTROL_PRESSED, appThemeOverride.getActionColor());
            colors.setColor(AlarmColorValues.COLOR_SOUNDING_PULSE_START, appThemeOverride.getSunsetTextColor());
            colors.setColor(AlarmColorValues.COLOR_SOUNDING_PULSE_END, appThemeOverride.getSunriseTextColor());
            colors.setColor(AlarmColorValues.COLOR_TEXT_PRIMARY, appThemeOverride.getTitleColor());
            colors.setColor(AlarmColorValues.COLOR_TEXT_SECONDARY, appThemeOverride.getTextColor());
            colors.setColor(AlarmColorValues.COLOR_TEXT_TIME, appThemeOverride.getTimeColor());
        }
    }

    @Override
    public void onBackPressed()
    {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private final View.OnClickListener onBackClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    private final View.OnClickListener onSnoozeClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (alarm != null) {
                snoozeAlarm(AlarmDismissActivity.this);
            }
        }
    };

    protected void snoozeAlarm(Context context)
    {
        snoozeButton.setEnabled(false);
        dismissButton.setEnabled(false);
        if (alarm != null) {
            sendBroadcast(AlarmNotifications.getAlarmIntent(AlarmDismissActivity.this, AlarmNotifications.ACTION_SNOOZE, alarm.getUri()));
        }
    }

    private final View.OnClickListener onDismissClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onDismissedClicked");
            dismissAlarmAfterChallenge(AlarmDismissActivity.this, v);
        }
    };


    public void dismissAlarmAfterChallenge(Context context, View v)
    {
        AlarmSettings.DismissChallenge challenge = (alarm != null ? alarm.getDismissChallenge(context) : AlarmSettings.DismissChallenge.NONE);
        if (isTesting) {
            Log.d("DEBUG", "dismissAlarmAfterChallenge: testChallengeID: " + testChallengeID);
            challenge = AlarmSettings.DismissChallenge.valueOf(testChallengeID, AlarmSettings.DismissChallenge.ADDON);
            challenge.setID(testChallengeID);
        }

        if (challenge != AlarmSettings.DismissChallenge.NONE && !AlarmNotifications.ACTION_TIMEOUT.equals(mode))
        {
            showDismissChallenge(context, getDismissChallenge(context, challenge));
        } else dismissAlarm(context);
    }

    @Override
    public Uri getAlarmUri() {
        return (alarm != null ? alarm.getUri() : null);
    }

    public void dismissAlarm(Context context)
    {
        snoozeButton.setEnabled(false);
        dismissButton.setEnabled(false);

        if (!isTesting && alarm != null) {
            sendBroadcast(AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISMISS, alarm.getUri()));
        } else {
            finish();
        }
    }

    protected void showDismissChallenge(Context context, @Nullable AlarmDismissChallenge challenge)
    {
        if (challenge != null) {
            challenge.showDismissChallenge(context, dismissButton, this);
        } else dismissAlarm(context);
    }

    private void setMode( @Nullable String mode )
    {
        this.prevMode = this.mode;
        this.mode = mode;
        updateViews(this);
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

    private ValueAnimator animateBrightness(float downToValue, @SuppressWarnings("SameParameterValue") int durationMillis)
    {
        if (Build.VERSION.SDK_INT < 12) {
            return null;
        }
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
        return animator;
    }

    /**
     * Colorable
     */
    public interface Colorable {
        void setColor(int color);
    }
    public static class ColorableView<T extends View> implements Colorable
    {
        protected final T view;
        public ColorableView(T v) {
            view = v;
        }
        public void setColor(int color) {
            if (view != null) {
                view.setBackgroundColor(color);
            }
        }
        public T getView() {
            return view;
        }
    }
    public static class ColorableTextView extends ColorableView<TextView>
    {
        public ColorableTextView(TextView v) {
            super(v);
        }
        @Override
        public void setColor(int color) {
            if (view != null) {
                view.setTextColor(color);
            }
        }
    }
    public static class ColorableAlarmButton extends ColorableView<AlarmButton>
    {
        public ColorableAlarmButton(AlarmButton v) {
            super(v);
        }
        @Override
        public void setColor(int color) {
            if (view != null) {
                view.setThumbTextColor(color);
                view.setThumbImageTint(color);
            }
        }
    }
    public static class ColorableImageView extends ColorableView<ImageView>
    {
        public ColorableImageView(ImageView v) {
            super(v);
        }
        @Override
        public void setColor(int color) {
            if (view != null) {
                view.setColorFilter(color);
            }
        }
    }

    /**
     * animateColors
     */
    @Nullable
    private static ValueAnimator animateColors(int[] colors, long duration, boolean repeat, @Nullable TimeInterpolator interpolator, final Colorable... views) {
        return animateColors(colors, duration, repeat, interpolator, null, views);
    }

    @Nullable
    private static ValueAnimator animateColors(int[] colors, long duration, boolean repeat, @Nullable TimeInterpolator interpolator, @Nullable final ValueAnimator.AnimatorUpdateListener listener, final Colorable... views)
    {
        if (Build.VERSION.SDK_INT < 12) {
            return null;
        }
        if (views != null && views.length > 0)
        {
            final ValueAnimator animation = getColorValueAnimator(colors);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animator)
                {
                    for (Colorable v : views) {
                        if (v != null) {
                            v.setColor((int) animator.getAnimatedValue());
                        }
                    }
                    if (listener != null) {
                        listener.onAnimationUpdate(animator);
                    }
                }
            });
            if (repeat) {
                animation.setRepeatCount(ValueAnimator.INFINITE);
                animation.setRepeatMode(ValueAnimator.REVERSE);
            }
            if (interpolator != null) {
                animation.setInterpolator(interpolator);
            }
            animation.setDuration(duration);
            animation.start();
            return animation;
        }
        return null;
    }

    private static ValueAnimator getColorValueAnimator(int... colors)
    {
        if (Build.VERSION.SDK_INT >= 21) {
            return ValueAnimator.ofArgb(colors);
        } else {
            ValueAnimator animator = new ValueAnimator();
            animator.setIntValues(colors);
            animator.setEvaluator(new ArgbEvaluator());
            return animator;
        }
    }

    private Object pulseAnimationObj, bgAnimationObj;

    @Nullable
    private static ValueAnimator animateColors(final TextView[] labels, final AlarmButton[] buttons1, final ImageView icon, int startColor, int endColor, long duration, @Nullable TimeInterpolator interpolator)
    {
        if (Build.VERSION.SDK_INT < 12) {
            return null;
        }

        ArrayList<Colorable> views = new ArrayList<>();
        if (icon != null) {
            views.add(new ColorableImageView(icon));
        }
        if (labels != null) {
            for (TextView label : labels) {
                if (label != null) {
                    views.add(new ColorableTextView(label));
                }
            }
        }
        if (buttons1 != null) {
            for (AlarmButton button : buttons1) {
                if (button != null) {
                    views.add(new ColorableAlarmButton(button));
                }
            }
        }
        return animateColors(new int[] { startColor, endColor }, duration, true, interpolator, views.toArray(new Colorable[0]));
    }

    private void resetAnimateColors(TextView[] labels, AlarmButton[] buttons)
    {
        if (Build.VERSION.SDK_INT < 11) {
            return;
        }

        clockText.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_TIME));
        alarmTitle.setTextColor(colors.getColor(AlarmColorValues.COLOR_TEXT_PRIMARY));

        stopAnimation((ValueAnimator) pulseAnimationObj);

        int textColor = colors.getColor(AlarmColorValues.COLOR_TEXT_SECONDARY);
        for (TextView label : labels){
            if (label != null) {
                label.setTextColor(textColor);
            }
        }

        ColorStateList buttonColors = SuntimesUtils.colorStateList(
                colors.getColor(AlarmColorValues.COLOR_CONTROL_ENABLED),
                colors.getColor(AlarmColorValues.COLOR_CONTROL_DISABLED),
                colors.getColor(AlarmColorValues.COLOR_CONTROL_PRESSED));

        for (AlarmButton button : buttons) {
            if (button != null) {
                button.setThumbTextColor(buttonColors);
                button.setThumbImageTint(colors.getColor(AlarmColorValues.COLOR_CONTROL_ENABLED));
                button.setAccentColor(colors.getColor(AlarmColorValues.COLOR_CONTROL_PRESSED));
            }
        }
    }
    private void stopAnimation(ValueAnimator animation)
    {
        if (animation != null) {
            animation.removeAllUpdateListeners();
        }
    }

    protected AlarmClockItem getPreviewAlarmItem(Context context, long alarmID)
    {
        AlarmClockItem item = new AlarmClockItem();
        item.enabled = true;
        item.rowID = alarmID;
        item.label = context.getString(R.string.configAction_preview);
        item.note = context.getString(R.string.configLabel_alarms_brightMode_summary);
        item.setState(AlarmState.STATE_SOUNDING);
        return item;
    }

    public void setAlarmID(final Context context, long alarmID) {
        setAlarmID(context, alarmID, null);
    }
    public void setAlarmID(final Context context, long alarmID, @Nullable final AlarmDatabaseAdapter.AlarmItemTaskListener listener)
    {
        if (ACTION_PREVIEW.equals(getIntent().getAction()))
        {
            setAlarmItem(context, getPreviewAlarmItem(context, alarmID));
            return;
        }

        AlarmDatabaseAdapter.AlarmItemTask task = new AlarmDatabaseAdapter.AlarmItemTask(context);
        task.addAlarmItemTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener() {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item)
            {
                if (item != null) {
                    if (item.type == AlarmClockItem.AlarmType.ALARM) {
                        setAlarmItem(context, item);

                    } else {
                        Log.w(TAG, "setAlarmID: " + item.getUri() + " not of type alarm; ignoring.");
                        result = false;
                    }
                }
                if (listener != null) {
                    listener.onFinished(result, item);
                }
            }
        });
        task.execute(alarmID);
    }

    public void setAlarmID(final Context context, Uri uri) {
        setAlarmID(context, uri, null);
    }
    public void setAlarmID(Context context, Uri uri,  @Nullable final AlarmDatabaseAdapter.AlarmItemTaskListener listener)
    {
        try {
            setAlarmID(context, ContentUris.parseId(uri), listener);

        } catch (NumberFormatException e) {
            Log.e(TAG, "setAlarmID: invalid uri! " + e);
            setAlarmID(context, -1, listener);
        }
    }

    protected TimeZone getTimeZone()
    {
        if (alarm != null && alarm.timezone != null) {
            return AlarmClockItem.AlarmTimeZone.getTimeZone(alarm.timezone, alarm.location);
        } else return TimeZone.getDefault();
    }

    public static final int CLOCK_UPDATE_RATE = 3000;
    private final Runnable updateClockTask = new Runnable()
    {
        @Override
        public void run()
        {
            TimeZone timezone = getTimeZone();
            clockText.setText(formatTimeDisplay(AlarmDismissActivity.this, Calendar.getInstance(timezone)));
            clockText.postDelayed(this, CLOCK_UPDATE_RATE);

            timezoneText.setVisibility((alarm != null && alarm.timezone != null) ? View.VISIBLE : View.GONE);
            timezoneText.setText(timezone.getID());
        }
    };

    protected Object animateBackground(int[] animColors, long duration, TimeInterpolator interpolator)
    {
        stopAnimateBackground();
        bgAnimationObj = animateColors(animColors, duration, false, interpolator, new ColorableView<>(background));
        return bgAnimationObj;
    }

    protected void stopAnimateBackground()
    {
        if (bgAnimationObj != null)
        {
            ValueAnimator bgAnimation = (ValueAnimator) bgAnimationObj;
            bgAnimation.cancel();
            bgAnimationObj = null;
        }
    }

    protected int currentTitleColor()
    {
        if (alarmTitle != null) {
            return alarmTitle.getCurrentTextColor();
        } else {
            return colors.getColor(AlarmColorValues.COLOR_TEXT_PRIMARY);
        }
    }
    protected int currentTimeColor()
    {
        if (clockText != null) {
            return clockText.getCurrentTextColor();
        } else {
            return colors.getColor(AlarmColorValues.COLOR_TEXT_TIME);
        }
    }
    protected int currentTextColor()
    {
        if (infoText != null) {
            return infoText.getCurrentTextColor();
        } else {
            return colors.getColor(AlarmColorValues.COLOR_TEXT_SECONDARY);
        }
    }

    protected int currentBackgroundColor()
    {
        Drawable d = background.getBackground();
        if (d instanceof ColorDrawable) {
            return ((ColorDrawable) d.mutate()).getColor();
        } else {
            return colors.getColor(AlarmColorValues.COLOR_BRIGHT_BACKGROUND_START);
        }
    }

    @SuppressLint("SetTextI18n")
    public void updateViews(Context context)
    {
        if (alarm != null)
        {
            String emptyLabel = context.getString(R.string.alarmMode_alarm);
            alarmTitle.setText((alarm.label == null || alarm.label.isEmpty()) ? emptyLabel : alarm.label);

            String eventString = alarm.getEvent();
            if (eventString != null)
            {
                AlarmEvent.AlarmEventItem eventItem = new AlarmEvent.AlarmEventItem(eventString, context.getContentResolver());
                alarmSubtitle.setText(eventItem.getTitle());
                alarmSubtitle.setVisibility(View.VISIBLE);

            } else if (alarm.timezone != null) {
                Calendar eventTime = Calendar.getInstance(AlarmClockItem.AlarmTimeZone.getTimeZone(alarm.timezone, alarm.location));
                eventTime.set(Calendar.HOUR_OF_DAY, alarm.hour);
                eventTime.set(Calendar.MINUTE, alarm.minute);
                alarmSubtitle.setText(utils.calendarTimeShortDisplayString(context, eventTime) + "\n" + AlarmClockItem.AlarmTimeZone.displayString(alarm.timezone));
                alarmSubtitle.setVisibility(View.VISIBLE);

            } else {
                alarmSubtitle.setVisibility(View.GONE);
            }

            alarmText.setText(formatTimeDisplay(context, alarm.getCalendar()));
            offsetText.setText(formatOffsetDisplay(context));

            if (alarm.note != null) {
                noteText.setText(utils.displayStringForTitlePattern(context, alarm.note, AlarmNotifications.getData(context, alarm)));
            } else noteText.setText("");


        } else {    // null alarm item
            alarmTitle.setText("");
            alarmSubtitle.setText("");
            alarmText.setText("");
            offsetText.setText("");
        }

        if (MODE_SNOOZING.equals(mode))
        {
            icon.setDisplayedChild(1);
            infoText.setText(formatSnoozeDisplay());
            infoText.setVisibility(View.VISIBLE);
            snoozeButton.setVisibility(View.GONE);
            snoozeButton.setEnabled(false);
            dismissButton.setEnabled(true);
            backButton.show();

            pulseAnimationObj = animateColors(labels, buttons, iconSnoozing, colors.getColor(AlarmColorValues.COLOR_SNOOZING_PULSE_START), colors.getColor(AlarmColorValues.COLOR_SNOOZING_PULSE_END), pulseSnoozingDuration, new AccelerateDecelerateInterpolator());
            if (isBrightMode)
            {
                int snoozeBackgroundColor = colors.getColor(AlarmColorValues.COLOR_BRIGHT_BACKGROUND_START);
                int snoozeTitleColor = getContrastingTextColor(snoozeBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_PRIMARY_INVERSE, AlarmColorValues.COLOR_TEXT_PRIMARY);
                int snoozeTimeColor = getContrastingTextColor(snoozeBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_TIME_INVERSE, AlarmColorValues.COLOR_TEXT_TIME);
                int snoozeTextColor = getContrastingTextColor(snoozeBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_SECONDARY_INVERSE, AlarmColorValues.COLOR_TEXT_SECONDARY);

                animateBackground(new int[] { currentBackgroundColor(), snoozeBackgroundColor }, 1500, new LinearInterpolator());
                animateColors(new int[] { currentTitleColor(), snoozeTitleColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(alarmTitle));
                animateColors(new int[] { currentTimeColor(), snoozeTimeColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(clockText));
                animateColors(new int[] { currentTextColor(), snoozeTextColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(infoText), new ColorableTextView(timezoneText));
            }

            if (Build.VERSION.SDK_INT >= 17)  // BUG: on some older devices modifying brightness turns off the screen
            {
                float dimScreenValue = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
                boolean needsTransition = (!AlarmNotifications.ACTION_SNOOZE.equals(prevMode));
                if (needsTransition) {
                    animateBrightness(dimScreenValue, snoozingDimmingDuration);
                } else {
                    setBrightness(dimScreenValue);
                }
                allowScreenOffAfterDelay(snoozingScreenOnDuration);
            }

        } else if (MODE_TIMEOUT.equals(mode)) {
            icon.setDisplayedChild(2);
            infoText.setText(getString(R.string.alarmAction_timeoutMsg));
            infoText.setVisibility(View.VISIBLE);
            snoozeButton.setVisibility(View.GONE);
            snoozeButton.setEnabled(false);
            dismissButton.setEnabled(true);
            backButton.show();

            resetAnimateColors(labels, buttons);
            if (isBrightMode)
            {
                int timeoutBackgroundColor = colors.getColor(AlarmColorValues.COLOR_BRIGHT_BACKGROUND_START);
                int timeoutTitleColor = getContrastingTextColor(timeoutBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_PRIMARY, AlarmColorValues.COLOR_TEXT_PRIMARY_INVERSE);
                int timeoutTimeColor = getContrastingTextColor(timeoutBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_TIME, AlarmColorValues.COLOR_TEXT_TIME_INVERSE);
                int timeoutTextColor = getContrastingTextColor(timeoutBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_SECONDARY, AlarmColorValues.COLOR_TEXT_SECONDARY_INVERSE);

                animateBackground(new int[] { currentBackgroundColor(), timeoutBackgroundColor }, 1500, new AccelerateInterpolator());
                animateColors(new int[] { currentTitleColor(), timeoutTitleColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(alarmTitle));
                animateColors(new int[] { currentTimeColor(), timeoutTimeColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(clockText));
                animateColors(new int[] { currentTextColor(), timeoutTextColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(infoText), new ColorableTextView(timezoneText));
            }
            setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);

        } else if (MODE_SOUNDING.equals(mode)) {
            icon.setDisplayedChild(0);
            hardwareButtonPressed = false;
            infoText.setText("");
            infoText.setVisibility(View.GONE);
            snoozeButton.setVisibility(isTesting ? View.GONE : View.VISIBLE);
            snoozeButton.setEnabled(true);
            dismissButton.setEnabled(true);
            backButton.hide();

            pulseAnimationObj = animateColors(labels, buttons, iconSounding, colors.getColor(AlarmColorValues.COLOR_SOUNDING_PULSE_START), colors.getColor(AlarmColorValues.COLOR_SOUNDING_PULSE_END), pulseSoundingDuration, new AccelerateInterpolator());
            if (isBrightMode)
            {
                int soundingBackgroundColor = colors.getColor(AlarmColorValues.COLOR_BRIGHT_BACKGROUND_END);
                int soundingTitleColor = getContrastingTextColor(soundingBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_PRIMARY, AlarmColorValues.COLOR_TEXT_PRIMARY_INVERSE);
                int soundingTimeColor = getContrastingTextColor(soundingBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_TIME, AlarmColorValues.COLOR_TEXT_TIME_INVERSE);
                int soundingTextColor = getContrastingTextColor(soundingBackgroundColor, colors, AlarmColorValues.COLOR_TEXT_SECONDARY, AlarmColorValues.COLOR_TEXT_SECONDARY_INVERSE);

                animateBackground(new int[] { colors.getColor(AlarmColorValues.COLOR_BRIGHT_BACKGROUND_START), soundingBackgroundColor }, AlarmSettings.loadPrefAlarmBrightFadeIn(this), new AccelerateInterpolator());
                animateColors(new int[] { currentTitleColor(), soundingTitleColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(alarmTitle));
                animateColors(new int[] { currentTimeColor(), soundingTimeColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(clockText));
                animateColors(new int[] { currentTextColor(), soundingTextColor }, 1500, false, new LinearInterpolator(), new ColorableTextView(timezoneText));
                infoText.setTextColor(Color.TRANSPARENT);
            }
            setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);

        } else {     // } else if (MODE_SCHEDULED.equals(mode)) {
            icon.setDisplayedChild(0);
            snoozeButton.setVisibility(View.GONE);
            snoozeButton.setEnabled(false);
            dismissButton.setEnabled(false);
            backButton.hide();

            pulseAnimationObj = animateColors(labels, buttons, iconSounding, colors.getColor(AlarmColorValues.COLOR_SOUNDING_PULSE_START), colors.getColor(AlarmColorValues.COLOR_SOUNDING_PULSE_END), pulseSoundingDuration, new AccelerateInterpolator());
            setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        }
    }

    protected static int getContrastingTextColor(int backgroundColor, ColorValues colors, String textColorID0, String textColorID1)
    {
        int textColor0 = colors.getColor(textColorID0);
        int textColor1 = colors.getColor(textColorID1);
        double r0 = ColorUtils.getContrastRatio(textColor0, backgroundColor);
        double r1 = ColorUtils.getContrastRatio(textColor1, backgroundColor);
        return (r0 > r1) ? textColor0 : textColor1;
        //return ColorUtils.isTextReadable(textColor0, backgroundColor) ? textColor0 : textColor1;
    }

    protected CharSequence formatTimeDisplay(Context context, Calendar calendar)
    {
        SuntimesUtils.TimeDisplayText timeText = utils.calendarTimeShortDisplayString(context, calendar, false);
        if (SuntimesUtils.is24()) {
            return timeText.getValue();
        } else {
            String timeString = timeText.getValue() + " " + timeText.getSuffix();
            return SuntimesUtils.createRelativeSpan(null, timeString, " " + timeText.getSuffix(), 0.40f);
        }
    }

    protected CharSequence formatOffsetDisplay(Context context)
    {
        Spannable offsetSpan = new SpannableString("");
        if (alarm != null && alarm.offset != 0)
        {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(alarm.timestamp);
            int alarmHour = alarmTime.get( SuntimesUtils.is24() ? Calendar.HOUR_OF_DAY : Calendar.HOUR );
            boolean isBefore = (alarm.offset <= 0);
            String offsetText = utils.timeDeltaLongDisplayString(0, alarm.offset).getValue();
            String offsetDisplay = context.getResources().getQuantityString((isBefore ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), alarmHour, offsetText);
            offsetSpan = SuntimesUtils.createBoldSpan(null, offsetDisplay, offsetText);
        }
        return offsetSpan;
    }

    protected CharSequence formatSnoozeDisplay()
    {
        SuntimesUtils.initDisplayStrings(this);
        long snoozeMillis = (alarm != null)
                ? alarm.getFlag(AlarmClockItem.FLAG_SNOOZE, AlarmSettings.loadPrefAlarmSnooze(this))    // NPE this line after rotation
                : AlarmSettings.PREF_DEF_ALARM_SNOOZE;
        SuntimesUtils.TimeDisplayText snoozeText = utils.timeDeltaLongDisplayString(0, snoozeMillis);
        String snoozeString = getString(R.string.alarmAction_snoozeMsg, snoozeText.getValue());
        return SuntimesUtils.createBoldSpan(null, snoozeString, snoozeText.getValue());
    }

    @SuppressLint("SetTextI18n")
    public void setAlarmItem(@NonNull Context context, @NonNull AlarmClockItem item)
    {
        alarm = item;
        if (alarm.state != null)
        {
            switch (alarm.state.getState())
            {
                case AlarmState.STATE_SOUNDING: setMode(MODE_SOUNDING); break;
                case AlarmState.STATE_SNOOZING: setMode(MODE_SNOOZING); break;
                case AlarmState.STATE_TIMEOUT: setMode(MODE_TIMEOUT); break;
                case AlarmState.STATE_SCHEDULED_SOON:
                case AlarmState.STATE_SCHEDULED_DISTANT: setMode(MODE_SCHEDULED); break;
                default:
                    if (isTesting) {
                        setMode(null);

                    } else {
                        Log.i(TAG, "setAlarmItem: state is not SCHEDULED/SOUNDING/SNOOZING/TIMEOUT.. calling finish()");
                        finish();
                    }
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

    public boolean isInteractive()
    {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null)
        {
            if (Build.VERSION.SDK_INT >= 20) {
                return pm.isInteractive();
            } else return false;
        } else return false;
    }

    private void screenOn()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON   // BUG: turning the screen on this way works once (first time) .. after an alarm snoozes (and the device falls back asleep) this won't work the second time
                //| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON            // a potential workaround is to keep the screen on ... but this might consume noticeable battery.
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    private void allowScreenOff()
    {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void allowScreenOffAfterDelay(long delay)
    {
        snoozeButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                allowScreenOff();
            }
        }, delay);
    }

    public AlarmDismissInterface.AlarmDismissChallenge getDismissChallenge(Context context, AlarmSettings.DismissChallenge setting)
    {
        switch (setting) {
            case ADDON: return new AddonDismissChallenge(context, setting.getID());
            case MATH: return new MathDismissChallenge();
            case NONE: default: return null;
        }
    }

    protected void onDismissChallengeActivityResult(int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "onDismissChallengeResult: pass");
            dismissAlarm(AlarmDismissActivity.this);
            return;

        } else if (data != null) {
            if (data.getIntExtra(Intent.EXTRA_RETURN_RESULT, RESULT_CANCELED) == RESULT_OK)
            {
                Log.i(TAG, "onDismissChallengeResult: pass");
                dismissAlarm(AlarmDismissActivity.this);
                return;

            } else if (ACTION_SNOOZE.equals(data.getAction())) {
                Log.i(TAG, "onDismissChallengeResult: snooze");
                snoozeAlarm(AlarmDismissActivity.this);
                return;
            }
        }
        Log.w(TAG, "onDismissChallengeResult: fail: " + data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("DEBUG", "onActivityResult: " + requestCode + ", result: " + resultCode);
        switch (requestCode)
        {
            case REQUEST_DISMISS_CHALLENGE:
                onDismissChallengeActivityResult(resultCode, data);
                break;
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static class AddonDismissChallenge implements AlarmDismissInterface.AlarmDismissChallenge
    {
        protected long id = -1;
        protected AlarmAddon.DismissChallengeInfo info;

        public AddonDismissChallenge(Context context, long id) {
            this.id = id;
        }

        public AddonDismissChallenge(AlarmAddon.DismissChallengeInfo info) {
            this.id = info.getDismissChallengeID();
            this.info = info;
        }

        public long getDismissChallengeID() {
            return id;
        }

        public AlarmAddon.DismissChallengeInfo getInfo(Context context)
        {
            if (info == null)
            {
                List<AlarmAddon.DismissChallengeInfo> challenges = AlarmAddon.queryAlarmDismissChallenges(context, id);
                if (challenges.size() >= 1) {
                    info = challenges.get(0);
                }
            }
            return info;
        }

        @Override
        public void showDismissChallenge(Context context, View view, AlarmDismissInterface parent)
        {
            getInfo(context);
            if (info != null) {

                Intent intent = info.getIntent().setData(parent.getAlarmUri());
                Log.d("onDismissChallenge", "showDismissChallenge: intent: " + intent );
                parent.getActivity().startActivityForResult(intent, REQUEST_DISMISS_CHALLENGE);

            } else {
                Log.e(TAG, "AddonDismissChallenge: showDismissChallenge: failed to query activity info for challengeID " + id);
                parent.dismissAlarm(context);
            }
        }

        @Override
        public Dialog createDismissChallengeDialog(Context context, View view, AlarmDismissInterface parent) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * MathDismissChallenge
     */
    public static class MathDismissChallenge implements AlarmDismissInterface.AlarmDismissChallenge
    {
        public static final int ADD = 0;
        public static final int SUBTRACT = 1;
        public static final int MULTIPLY = 2;
        public static final int DIVIDE = 3;

        protected int apply(int operation, int a, int b) {
            switch (operation) {
                case DIVIDE: return a / b;
                case MULTIPLY: return a * b;
                case SUBTRACT: return a - b;
                case ADD: default: return a + b;
            }
        }

        protected String toString(int operation)
        {
            switch (operation) {
                case DIVIDE: return " ÷ ";
                case MULTIPLY: return " * ";
                case SUBTRACT: return " - ";
                case ADD: default: return " + ";
            }
        }

        public Pair<String,String> generateMathProblem()
        {
            int[] operations = new int[] { ADD, SUBTRACT, MULTIPLY, DIVIDE };

            Random r = new Random();
            int operation = operations[r.nextInt(operations.length)];

            int a, b, c;
            do {
                a = r.nextInt(9) + 1;    // [1,9]
                b = r.nextInt( 9) + 1;   // [1,9]
                c = apply(operation, a, b);

            } while (filterProblem(operation, a, b, c));

            String problem = a + toString(operation) + b;
            String solution = "" + c;
            return new Pair<>(problem, solution);
        }

        protected boolean filterProblem(int operation, int a, int b, int c)
        {
            return ((operation == MULTIPLY || operation == ADD) && (a == 1 || b == 1))     // adding 1, multiplying by 1 (too easy)
                    || (operation == SUBTRACT && b == 1)                                   // subtracting 1 (too easy)
                    || (operation == DIVIDE && (((a % b) != 0) || b == 1));                // division with remainder (too hard), division by 1 (too easy)
        }

        @Override
        public void showDismissChallenge(Context context, View view, final AlarmDismissInterface parent) {
            createDismissChallengeDialog(context, view, parent).show();
        }

        @Override
        public Dialog createDismissChallengeDialog(final Context context, final View view, final AlarmDismissInterface parent)
        {
            final Pair<String,String> problem = generateMathProblem();

            FrameLayout layout = new FrameLayout(context);
            final EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            layout.addView(editText);

            int margin = (int) context.getResources().getDimension(R.dimen.dialog_margin);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) editText.getLayoutParams();
            params.leftMargin = params.rightMargin = margin;
            editText.setLayoutParams(params);

            final AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Dialog);
            dialog.setView(layout);
            dialog.setTitle(problem.first);
            dialog.setCancelable(true);

            dialog.setNeutralButton(context.getString(R.string.alarmDismiss_math), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            parent.dismissAlarmAfterChallenge(context, editText);
                        }
                    }, 250);
                }
            });
            dialog.setPositiveButton(context.getString(R.string.alarmAction_dismiss), onDialogAcceptListener(context, view, editText, problem, parent));

            final Dialog d = dialog.create();
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        d.dismiss();
                        onDialogAcceptListener(context, view, editText, problem, parent).onClick(d, 0);
                        return true;
                    }
                    return false;
                }
            });
            return d;
        }

        protected DialogInterface.OnClickListener onDialogAcceptListener(final Context context, final View view, final EditText editText, final Pair<String,String> problem, final AlarmDismissInterface parent)
        {
            return new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    String text = sanitizeInput(editText.getText().toString());
                    if (text != null && text.equals(problem.second)) {
                        parent.dismissAlarm(context);

                    } else {
                        view.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                parent.dismissAlarmAfterChallenge(context, editText);
                            }
                        }, 250);
                    }
                }
            };
        }

        protected String sanitizeInput(@Nullable String text)
        {
            if (text != null)
            {
                text = text.trim();
                String[] dashes = new String[] {"-","‐","‑","–","—","―"};   // hyphen, non-breaking hyphen, en-dash, em-dash, horizontal bar
                for (String dash : dashes) {
                    text = text.replaceAll(dash, "-");   // replaced with hyphen-minus
                }
                return text;
            } else return null;
        }
    }



}