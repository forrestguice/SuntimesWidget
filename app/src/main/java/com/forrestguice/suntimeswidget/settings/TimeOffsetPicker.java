/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ViewFlipper;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

import java.util.ArrayList;

@TargetApi(11)
public class TimeOffsetPicker extends LinearLayout
{
    @TargetApi(21)
    public TimeOffsetPicker(Context context) {
        super(context);
        initLayout(this);
    }

    public TimeOffsetPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
        initLayout(this);
    }

    @TargetApi(21)
    public TimeOffsetPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context, attrs);
        initLayout(this);
    }

    @TargetApi(21)
    public TimeOffsetPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initParams(context, attrs);
        initLayout(this);
    }

    private int param_minMs = 1, param_maxMs = 10000;
    private boolean param_showDirection = false;

    private boolean param_showSeconds = true;
    private int param_minSeconds = MIN_SECONDS;
    private int param_maxSeconds = MAX_SECONDS;
    public static final int MIN_SECONDS = 0;
    private static final int MAX_SECONDS = 59;

    private boolean param_showMinutes = true;
    private int param_minMinutes = MIN_MINUTES;
    private int param_maxMinutes = MAX_MINUTES;
    public static final int MIN_MINUTES = 0;
    public static final int MAX_MINUTES = 59;

    private boolean param_showHours = true;
    private int param_minHours = MIN_HOURS;
    private int param_maxHours = MAX_HOURS;
    public static final int MIN_HOURS = 0;
    public static final int MAX_HOURS = 23;

    private boolean param_showDays = false;
    private int param_minDays = MIN_DAYS;
    private int param_maxDays = MAX_DAYS;
    public static final int MIN_DAYS = 0;
    public static final int MAX_DAYS = 19;

    protected void initParams(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeOffsetPicker, 0, 0);
        try {
            param_minMs = a.getInt(R.styleable.TimeOffsetPicker_minValue, param_minMs);
            param_maxMs = a.getInt(R.styleable.TimeOffsetPicker_maxValue, param_maxMs);
            param_showSeconds = a.getBoolean(R.styleable.TimeOffsetPicker_allowPickSeconds, param_showSeconds);
            param_showMinutes = a.getBoolean(R.styleable.TimeOffsetPicker_allowPickMinutes, param_showMinutes);
            param_showHours = a.getBoolean(R.styleable.TimeOffsetPicker_allowPickHours, param_showHours);
            param_showDays = a.getBoolean(R.styleable.TimeOffsetPicker_allowPickDays, param_showDays);
            param_showDirection = a.getBoolean(R.styleable.TimeOffsetPicker_allowPickBeforeAfter, param_showDirection);

        } finally {
            a.recycle();
        }
        initParams(context);
    }
    protected void initParams(Context context)
    {
        if (param_minMs > param_maxMs) {  // or assert(min <= max)
            param_minMs = param_maxMs;
        }

        int minNumberOfSeconds = (param_minMs / 1000) + 1;
        int minNumberOfMinutes = minNumberOfSeconds / 60;
        int minNumberOfHours = minNumberOfMinutes / 60;
        int minDays = minNumberOfHours / 24;
        int minHours = minNumberOfHours % 24;
        int minMinutes = minNumberOfMinutes % 60;
        int minSeconds = minNumberOfSeconds % 60;

        int maxNumberOfSeconds = (param_maxMs / 1000) - 1;
        int maxNumberOfMinutes = maxNumberOfSeconds / 60;
        int maxNumberOfHours = maxNumberOfMinutes / 60;
        int maxDays = maxNumberOfHours / 24;
        int maxHours = maxNumberOfHours % 24;
        int maxMinutes = maxNumberOfMinutes % 60;
        int maxSeconds = maxNumberOfSeconds % 60;

        param_minDays = minDays;
        param_maxDays = maxDays;

        if (param_maxDays == 0) {
            param_showDays = false;
            param_minHours = minHours;
            param_maxHours = maxHours;
        } else {
            param_minHours = MIN_HOURS;
            param_maxHours = MAX_HOURS;
        }

        if (param_maxHours == 0) {
            param_showHours = false;
            param_maxMinutes = minMinutes;
            param_maxMinutes = maxMinutes;
        } else {
            param_minMinutes = MIN_MINUTES;
            param_maxMinutes = MAX_MINUTES;
        }

        if (param_maxMinutes == 0) {
            param_showMinutes = false;
            param_minSeconds = minSeconds;
            param_maxSeconds = maxSeconds;
        } else {
            param_minSeconds = MIN_SECONDS;
            param_maxSeconds = MAX_SECONDS;
        }
    }

    /**
     * setParams
     * @param context Context
     * @param min min millis
     * @param max max millis
     * @param showSeconds flag
     * @param showMinutes flag
     * @param showHours flag
     * @param showDays flag
     * @param showDirection  flag
     */
    public void setParams(Context context, int min, int max, boolean showSeconds, boolean showMinutes, boolean showHours, boolean showDays, boolean showDirection)
    {
        param_minMs = min;
        param_maxMs = max;
        param_showSeconds = showSeconds;
        param_showMinutes = showMinutes;
        param_showHours = showHours;
        param_showDays = showDays;
        param_showDirection = showDirection;
        initParams(context);
        initLayout(this);

        Log.d("DEBUG", "setParams:" );
    }

    private static int[] secondsValues;
    private static String[] secondsStrings;

    private static String[] minuteStrings;
    private static int[] minuteValues;

    private static String[] hourStrings;
    private static int[] hourValues;

    private static String[] dayStrings;
    private static int[] dayValues;

    protected void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        SuntimesUtils utils = new SuntimesUtils();

        secondsValues = new int[(param_maxSeconds - param_minSeconds) + 1];
        secondsValues[0] = param_minSeconds;
        secondsStrings = new String[secondsValues.length];
        secondsStrings[0] = (param_minSeconds == 0 ? " " : utils.timeDeltaLongDisplayString(0, secondsValues[0] * 1000, true).getValue());
        for (int i=1; i<secondsValues.length; i++) {
            secondsValues[i] = param_minSeconds + i;
            secondsStrings[i] = utils.timeDeltaLongDisplayString(0, secondsValues[i] * 1000, true).getValue();
        }

        minuteValues = new int[(param_maxMinutes - param_minMinutes) + 1];
        minuteValues[0] = param_minMinutes;
        minuteStrings = new String[minuteValues.length];
        minuteStrings[0] = (param_minMinutes == 0 ? " " : utils.timeDeltaLongDisplayString(minuteValues[0] * 1000 * 60));
        for (int i=1; i<minuteValues.length; i++) {
            minuteValues[i] = param_minMinutes + i;
            minuteStrings[i] = utils.timeDeltaLongDisplayString(minuteValues[i] * 1000 * 60);
        }

        hourValues = new int[(param_maxHours - param_minHours) + 1];
        hourValues[0] = param_minHours;
        hourStrings = new String[hourValues.length];
        hourStrings[0] = (param_minHours == 0 ? " " : utils.timeDeltaLongDisplayString(hourValues[0] * 1000 * 60 * 60));
        for (int i=1; i<hourValues.length; i++) {
            hourValues[i] = param_minHours + i;
            hourStrings[i] = utils.timeDeltaLongDisplayString(hourValues[i] * 1000 * 60 * 60);
        }

        dayValues = new int[(param_maxDays - param_minDays) + 1];
        dayValues[0] = param_minDays;
        dayStrings = new String[dayValues.length];
        dayStrings[0] = (param_minDays == 0 ? " " : utils.timeDeltaLongDisplayString(dayValues[0] * 1000 * 60 * 60 * 24));
        for (int i=1; i<dayValues.length; i++) {
            dayValues[i] = param_minDays + i;
            dayStrings[i] = utils.timeDeltaLongDisplayString(dayValues[i] * 1000 * 60 * 60 * 24);
        }
    }

    private NumberPicker pickDays, pickHours, pickMinutes, pickSeconds, pickDirection;
    private ImageButton addSeconds, addMinutes, addHours, addDays;
    private ViewFlipper flipDays, flipHours, flipMinutes, flipSeconds;

    protected View initLayout(ViewGroup root)
    {
        Context context = getContext();
        initLocale(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.layout_view_timeoffset, root, false);

        pickDays = (NumberPicker) dialogView.findViewById(R.id.pick_offset_day);
        pickHours = (NumberPicker) dialogView.findViewById(R.id.pick_offset_hour);
        pickMinutes = (NumberPicker) dialogView.findViewById(R.id.pick_offset_minute);
        pickSeconds = (NumberPicker) dialogView.findViewById(R.id.pick_offset_seconds);
        pickDirection = (NumberPicker) dialogView.findViewById(R.id.pick_offset_direction);
        final NumberPicker[] pickers = new NumberPicker[] { pickDays, pickHours, pickMinutes, pickSeconds };

        addDays = (ImageButton) dialogView.findViewById(R.id.add_offset_day);
        addHours = (ImageButton) dialogView.findViewById(R.id.add_offset_hour);
        addMinutes = (ImageButton) dialogView.findViewById(R.id.add_offset_minute);
        addSeconds = (ImageButton) dialogView.findViewById(R.id.add_offset_seconds);
        final ImageButton[] buttons = new ImageButton[] { addDays, addHours, addMinutes, addSeconds };

        flipDays = (ViewFlipper) dialogView.findViewById(R.id.flip_offset_day);
        flipHours = (ViewFlipper) dialogView.findViewById(R.id.flip_offset_hour);
        flipMinutes = (ViewFlipper) dialogView.findViewById(R.id.flip_offset_minute);
        flipSeconds = (ViewFlipper) dialogView.findViewById(R.id.flip_offset_seconds);
        final ViewFlipper[] flippers = new ViewFlipper[] {flipDays, flipHours, flipMinutes, flipSeconds };

        for (int i=0; i<buttons.length; i++) {
            if (buttons[i] != null) {
                buttons[i].setOnClickListener(onButtonClicked(flippers[i], pickers[i]));
            }
        }

        if (pickDays != null) {
            pickDays.setMinValue(dayValues[0]);
            pickDays.setMaxValue(dayValues[dayValues.length-1]);
            pickDays.setDisplayedValues(dayStrings);
            pickDays.setOnValueChangedListener(onValueChanged);
        }
        if (flipDays != null) {
            flipDays.setVisibility(param_showDays ? View.VISIBLE : View.GONE);
        }

        if (pickHours != null) {
            pickHours.setMinValue(hourValues[0]);
            pickHours.setMaxValue(hourValues[hourValues.length-1]);
            pickHours.setDisplayedValues(hourStrings);
            pickHours.setOnValueChangedListener(onValueChanged);
        }
        if (flipHours != null) {
            flipHours.setVisibility(param_showHours ? View.VISIBLE : View.GONE);
        }

        if (pickMinutes != null) {
            pickMinutes.setMinValue(minuteValues[0]);
            pickMinutes.setMaxValue(minuteValues[minuteValues.length-1]);
            pickMinutes.setDisplayedValues(minuteStrings);
            pickMinutes.setOnValueChangedListener(onValueChanged);
        }
        if (flipMinutes != null) {
            flipMinutes.setVisibility(param_showMinutes ? View.VISIBLE : View.GONE);
        }

        if (pickSeconds != null) {
            pickSeconds.setMinValue(secondsValues[0]);
            pickSeconds.setMaxValue(secondsValues[secondsValues.length-1]);
            pickSeconds.setDisplayedValues(secondsStrings);
            pickSeconds.setOnValueChangedListener(onValueChanged);
        }
        if (flipSeconds != null) {
            flipSeconds.setVisibility(param_showSeconds ? View.VISIBLE : View.GONE);
        }

        if (pickDirection != null) {
            pickDirection.setMinValue(0);
            pickDirection.setMaxValue(1);
            pickDirection.setDisplayedValues( new String[] {context.getString(R.string.offset_button_before), context.getString(R.string.offset_button_after)} );
            pickDirection.setVisibility(param_showDirection ? View.VISIBLE : View.GONE);
            pickDirection.setOnValueChangedListener(onValueChanged);
        }

        removeAllViews();
        addView(dialogView);
        return dialogView;
    }

    private View.OnClickListener onButtonClicked(final ViewFlipper flipper, final NumberPicker picker)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (flipper != null) {
                    flipper.showNext();
                    if (picker != null) {
                        picker.setValue(1);
                    }
                }
                for (MillisecondPickerViewListener listener : viewListeners) {
                    listener.onValueChanged();
                }
            }
        };
    }

    private final NumberPicker.OnValueChangeListener onValueChanged = new NumberPicker.OnValueChangeListener()
    {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            for (MillisecondPickerViewListener listener : viewListeners) {
                listener.onValueChanged();
            }
        }
    };

    /**
     * ViewListener
     */
    public interface MillisecondPickerViewListener
    {
        void onValueChanged();
    }

    public void addViewListener(MillisecondPickerViewListener listener) {
        if (!viewListeners.contains(listener)) {
            viewListeners.add(listener);
        }
    }
    public void removeViewListener(MillisecondPickerViewListener listener) {
        viewListeners.remove(listener);
    }
    private final ArrayList<MillisecondPickerViewListener> viewListeners = new ArrayList<>();

    /**
     * getSelectedValue
     * @return millisecond value
     */
    public long getSelectedValue() {
        return getSelectedValue(true);
    }
    protected long getSelectedValue(boolean clampValue)
    {
        int changedValue = 0;
        if (pickSeconds != null) {
            changedValue += pickSeconds.getValue() * 1000;
        }
        if (param_showMinutes && pickMinutes != null) {
            changedValue += pickMinutes.getValue() * 60 * 1000;
        }
        if (param_showHours && pickHours != null) {
            changedValue += pickHours.getValue() * 60 * 60 * 1000;
        }
        if (param_showDays && pickDays != null) {
            changedValue += pickDays.getValue() * 24 * 60 * 60 * 1000;
        }

        if (clampValue) {
            changedValue = clampValue(changedValue);
        }
        if (param_showDirection && pickDirection != null) {
            changedValue *= ((pickDirection.getValue()) == 0 ? -1 : 1);
        }
        return changedValue;
    }

    /**
     * setSelectedValue
     * @param value milliseconds
     */
    public void setSelectedValue(long value)
    {
        boolean isBefore = (value <= 0);
        if (isBefore) {
            value *= -1;
        }
        if (pickDirection != null) {
            pickDirection.setValue(isBefore ? 0 : 1);
        }

        int numberOfSeconds = (int) value / 1000;
        int numberOfMinutes = numberOfSeconds / 60;
        int numberOfHours = numberOfMinutes / 60;
        int numberOfDays = numberOfHours / 24;
        int remainingHours = numberOfHours % 24;
        int remainingMinutes = numberOfMinutes % 60;
        int remainingSeconds = numberOfSeconds % 60;

        if (param_showDays && pickDays != null) {
            pickDays.setValue(numberOfDays);
        }
        if (param_showHours && pickHours != null) {
            pickHours.setValue(remainingHours);
        }
        if (param_showMinutes && pickMinutes != null) {
            pickMinutes.setValue(remainingMinutes);
        }
        if (pickSeconds != null) {
            pickSeconds.setValue(remainingSeconds);
        }

        if (flipDays != null) {
            flipDays.setDisplayedChild(numberOfDays != 0 ? 1 : 0);
        }
        if (flipHours != null) {
            flipHours.setDisplayedChild(remainingHours != 0 ? 1 : 0);
        }
        if (flipMinutes != null) {
            flipMinutes.setDisplayedChild(remainingMinutes != 0 ? 1 : 0);
        }
    }

    public long getMin() {
        return param_minMs;
    }
    public long getMax() {
        return param_maxMs;
    }

    protected int clampValue(int v)
    {
        if (v > param_maxMs) {
            v = param_maxMs;
        }
        if (v < param_minMs) {
            v = param_minMs;
        }
        return v;
    }
}
