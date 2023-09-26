/**
    Copyright (C) 2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.alarmclock.ui.bedtime;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public abstract class BedtimeViewHolder extends RecyclerView.ViewHolder
{
    protected SuntimesUtils utils = new SuntimesUtils();

    public BedtimeViewHolder(View view) {
        super(view);
    }

    public abstract void bindDataToHolder(Context context, @Nullable BedtimeItem item);

    protected void attachClickListeners(Context context, @Nullable BedtimeItem item) {}
    protected void detachClickListeners()
    {
        View actionView = getActionView();
        if (actionView != null) {
            actionView.setOnClickListener(null);
        }

        View configActionView = getConfigureActionView();
        if (configActionView != null) {
            configActionView.setOnClickListener(null);
        }
    }

    @Nullable
    public View getActionView() {
        return null;
    }
    @Nullable
    public View getConfigureActionView() {
        return null;
    }

    /**
     * Welcome
     */
    public static final class BedtimeViewHolder_Welcome extends BedtimeViewHolder
    {
        public BedtimeViewHolder_Welcome(View view) {
            super(view);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_alarmclock2;   // TODO
        }

        @Override
        public void bindDataToHolder(Context context, @Nullable BedtimeItem item) {
            // TODO
        }
    }

    /**
     * AlarmBedtimeViewHolder_AlarmItem
     */
    public static abstract class AlarmBedtimeViewHolder_AlarmItem extends BedtimeViewHolder
    {
        protected abstract Long getAlarmID(Context context);

        protected TextView text_label;
        protected View text_time_layout;
        protected TextView text_time;
        protected TextView text_time_suffix;
        protected Switch switch_enabled;
        protected FloatingActionButton button_add;
        protected FloatingActionButton button_edit;
        protected WeakReference<Context> contextRef;

        protected CompoundButton.OnCheckedChangeListener onSwitchChanged;

        public AlarmBedtimeViewHolder_AlarmItem(View view)
        {
            super(view);
            text_label = (TextView) view.findViewById(R.id.text_label);
            text_time_layout = view.findViewById(R.id.text_time_layout);
            text_time = (TextView) view.findViewById(R.id.text_time);
            text_time_suffix = (TextView) view.findViewById(R.id.text_time_suffix);
            switch_enabled = (Switch) view.findViewById(R.id.switch_enabled);
            button_add = (FloatingActionButton) view.findViewById(R.id.button_add);
            button_edit = (FloatingActionButton) view.findViewById(R.id.button_edit);
        }

        protected AlarmClockItem alarmItem = null;
        public AlarmClockItem getAlarmItem() {
            return alarmItem;
        }
        public void setAlarmItem(AlarmClockItem item) {
            alarmItem = item;
        }

        @Nullable
        public View getActionView() {
            return button_add;
        }

        @Nullable
        public View getConfigureActionView() {
            return button_edit;
        }

        @Override
        public void bindDataToHolder(Context context, @Nullable BedtimeItem item)
        {
            contextRef = new WeakReference<>(context);
            clearViews();
            loadAlarmItem(context, getAlarmID(context));
        }

        protected void loadAlarmItem(Context context, @Nullable Long rowID, AlarmListDialog.AlarmListTask.AlarmListTaskListener taskListener)
        {
            if (rowID != null && rowID != BedtimeSettings.ID_NONE)
            {
                AlarmListDialog.AlarmListTask listTask = new AlarmListDialog.AlarmListTask(context);
                listTask.setTaskListener(taskListener);
                listTask.execute(rowID);

            } else {
                updateViews();
            }
        }
        protected void loadAlarmItem(final Context context, Long rowID)
        {
            setAlarmItem(null);
            loadAlarmItem(context, rowID, new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
            {
                @Override
                public void onLoadFinished(List<AlarmClockItem> result)
                {
                    if (result != null && result.size() > 0) {
                        setAlarmItem(result.get(0));
                    }
                    updateViews();
                }
            });
        }

        @Override
        protected void attachClickListeners(final Context context, @Nullable BedtimeItem item)
        {
            if (switch_enabled != null) {
                switch_enabled.setOnCheckedChangeListener(onSwitchChanged = onSwitchChanged(context));
            }
        }

        @Override
        protected void detachClickListeners()
        {
            super.detachClickListeners();
            if (switch_enabled != null) {
                switch_enabled.setOnCheckedChangeListener(null);
            }
            if (button_add != null) {
                button_add.setOnClickListener(null);
            }
            if (button_edit != null) {
                button_edit.setOnClickListener(null);
            }
        }

        protected void clearViews()
        {
            /*if (text_time != null) {
                text_time.setText("");
            }
            if (text_time_suffix != null) {
                text_time_suffix.setText("");
            }*/
        }

        protected void updateViews()
        {
            Context context = contextRef.get();
            if (context != null)
            {
                AlarmClockItem item = getAlarmItem();
                if (item != null)
                {
                    AlarmNotifications.updateAlarmTime(context, item);
                    Calendar alarmTime = Calendar.getInstance(TimeZone.getDefault());
                    alarmTime.setTimeInMillis(item.timestamp + item.offset);
                    SuntimesUtils.TimeDisplayText timeText = utils.calendarTimeShortDisplayString(context, alarmTime, false);

                    if (text_time != null) {
                        text_time.setText(timeText.getValue());
                    }
                    if (text_time_suffix != null) {
                        text_time_suffix.setText(timeText.getSuffix());
                    }
                    if (text_time_layout != null) {
                        text_time_layout.setVisibility(View.VISIBLE);
                    }
                    if (switch_enabled != null)
                    {
                        switch_enabled.setOnCheckedChangeListener(null);
                        switch_enabled.setChecked(item.enabled);
                        switch_enabled.setOnCheckedChangeListener(onSwitchChanged);
                        switch_enabled.setVisibility(View.VISIBLE);
                    }
                    if (button_edit != null) {
                        button_edit.setVisibility(View.VISIBLE);
                    }

                } else {
                    if (text_time_layout != null) {
                        text_time_layout.setVisibility(View.GONE);
                    }
                    if (switch_enabled != null) {
                        switch_enabled.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }

        protected CompoundButton.OnCheckedChangeListener onSwitchChanged(final Context context)
        {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    toggleAlarm(context, getAlarmItem(), isChecked);
                }
            };
        }

        public void toggleAlarm(final Context context, @Nullable final AlarmClockItem item, final boolean enabled)
        {
            BedtimeAlarmHelper.toggleAlarmItem(context, item, enabled);
        }
    }

    /**
     * Bedtime
     */
    public static final class BedtimeViewHolder_Bedtime extends AlarmBedtimeViewHolder_AlarmItem
    {
        protected CheckBox check_dnd;
        protected View layout_dndWarning;
        protected TextView text_dndWarning;
        protected Button button_dndWarning;

        public BedtimeViewHolder_Bedtime(View view)
        {
            super(view);
            check_dnd = (CheckBox) view.findViewById(R.id.check_dnd);
            layout_dndWarning = view.findViewById(R.id.dndwarning_layout);
            text_dndWarning = (TextView) view.findViewById(R.id.dndwarning_text);
            button_dndWarning = (Button) view.findViewById(R.id.dndwarning_button);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_bedtime_notify;
        }

        @Override
        public void bindDataToHolder(Context context, @Nullable BedtimeItem item) {
            super.bindDataToHolder(context, item);
        }

        @Override
        protected Long getAlarmID(Context context) {
            return BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_BEDTIME_NOTIFY);
        }

        @Override
        protected void attachClickListeners(final Context context, @Nullable BedtimeItem item)
        {
            super.attachClickListeners(context, item);
            if (check_dnd != null)
            {
                check_dnd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        BedtimeSettings.savePrefBedtimeDoNotDisturb(context, isChecked);
                        BedtimeSettings.setAutomaticZenRule(context, switch_enabled.isChecked() && isChecked);
                        updateViews_dndWarning();
                    }
                });
            }
            if (button_dndWarning != null)
            {
                button_dndWarning.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BedtimeSettings.startDoNotDisturbAccessActivity(context);
                    }
                });
            }
        }

        public void toggleAlarm(final Context context, @Nullable final AlarmClockItem item, final boolean enabled)
        {
            super.toggleAlarm(context, item, enabled);
            BedtimeSettings.setAutomaticZenRule(context, enabled && BedtimeSettings.loadPrefBedtimeDoNotDisturb(context));
        }

        @Override
        protected void detachClickListeners()
        {
            super.detachClickListeners();
            if (check_dnd != null) {
                check_dnd.setOnCheckedChangeListener(null);
            }
            if (button_dndWarning != null) {
                button_dndWarning.setOnClickListener(null);
            }
        }

        protected void updateViews_dndWarning()
        {
            boolean showDndWarning = (check_dnd != null && check_dnd.isChecked() && !BedtimeSettings.hasDoNotDisturbPermission(contextRef.get()));
            if (layout_dndWarning != null) {
                layout_dndWarning.setVisibility(showDndWarning ? View.VISIBLE : View.GONE);
            }
            if (text_dndWarning != null) {
                text_dndWarning.setText(showDndWarning ? SuntimesUtils.fromHtml(contextRef.get().getString(R.string.privacy_permission_dnd)) : "");
            }
        }

        @Override
        protected void updateViews()
        {
            super.updateViews();

            if (check_dnd != null) {
                check_dnd.setChecked(BedtimeSettings.loadPrefBedtimeDoNotDisturb(contextRef.get()));
            }
            updateViews_dndWarning();

            if (getAlarmItem() != null)
            {
                if (text_label != null) {
                    text_label.setText("Bedtime at");    // TODO: i18n
                }
                if (button_add != null) {
                    button_add.setVisibility(View.GONE);
                }
                if (button_edit != null) {
                    button_edit.setVisibility(View.VISIBLE);
                }
            } else {
                if (text_label != null) {
                    text_label.setText("Bedtime is not set.");    // TODO: i18n
                }
                if (button_add != null) {
                    button_add.setVisibility(View.VISIBLE);
                }
                if (button_edit != null) {
                    button_edit.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    /**
     * BedtimeReminder
     */
    public static final class BedtimeViewHolder_BedtimeReminder extends AlarmBedtimeViewHolder_AlarmItem
    {
        public BedtimeViewHolder_BedtimeReminder(View view) {
            super(view);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_bedtime_reminder;    // TODO
        }

        @Override
        public void bindDataToHolder(Context context, @Nullable BedtimeItem item) {
            super.bindDataToHolder(context, item);
        }

        @Override
        protected Long getAlarmID(Context context) {
            return BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_BEDTIME_REMINDER);
        }

        @Override
        protected void attachClickListeners(final Context context, @Nullable BedtimeItem item) {
            super.attachClickListeners(context, item);
        }

        @Override
        protected CompoundButton.OnCheckedChangeListener onSwitchChanged(final Context context) {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    BedtimeSettings.savePrefBedtimeReminder(context, isChecked);
                    BedtimeAlarmHelper.setBedtimeReminder_withReminderItem(context, getAlarmItem(), isChecked);
                }
            };
        }

        @Override
        protected void updateViews()
        {
            super.updateViews();

            AlarmClockItem item = getAlarmItem();
            if (switch_enabled != null) {
                switch_enabled.setVisibility(View.VISIBLE);
            }
            if (button_edit != null) {
                button_edit.setVisibility(item != null ? View.VISIBLE : View.GONE);
            }
            if (text_label != null)
            {
                String offsetString = utils.timeDeltaLongDisplayString(item != null ? item.offset : BedtimeSettings.loadPrefBedtimeReminderOffset(contextRef.get()));
                String labelString ="Show a reminder " + offsetString + " before bedtime";    // TODO: i18n
                CharSequence labelDisplay = SuntimesUtils.createBoldSpan(null, labelString, offsetString);
                text_label.setText(labelDisplay);
            }
        }
    }

    /**
     * WakeupAlarm
     */
    public static final class AlarmBedtimeViewHolder_Wakeup extends AlarmBedtimeViewHolder_AlarmItem
    {
        public AlarmBedtimeViewHolder_Wakeup(View view) {
            super(view);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_bedtime_wakeup;
        }

        @Override
        protected Long getAlarmID(Context context) {
            return BedtimeSettings.loadAlarmID(context, BedtimeSettings.SLOT_WAKEUP_ALARM);
        }

        @Override
        public void bindDataToHolder(Context context, @Nullable BedtimeItem item) {
            super.bindDataToHolder(context, item);
        }

        @Override
        protected void attachClickListeners(final Context context, @Nullable BedtimeItem item) {
            super.attachClickListeners(context, item);
        }

        @Override
        protected void updateViews()
        {
            super.updateViews();
            if (getAlarmItem() != null)
            {
                if (text_label != null) {
                    text_label.setText("Wake up at");    // TODO: i18n
                }
                if (button_add != null) {
                    button_add.setVisibility(View.GONE);
                }
                if (button_edit != null) {
                    button_edit.setVisibility(View.VISIBLE);
                }

            } else {
                if (text_label != null) {
                    text_label.setText("Wake up time is not set.");    // TODO: i18n
                }
                if (button_add != null) {
                    button_add.setVisibility(View.VISIBLE);
                }
                if (button_edit != null) {
                    button_edit.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * SleepCycle
     */
    public static final class AlarmBedtimeViewHolder_SleepCycle extends BedtimeViewHolder
    {
        protected ImageButton button_configure;
        protected TextView text_sleepcycle;

        public AlarmBedtimeViewHolder_SleepCycle(View view)
        {
            super(view);
            text_sleepcycle = (TextView) view.findViewById(R.id.text_sleepcycle);
            button_configure = (ImageButton) view.findViewById(R.id.button_configure);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_bedtime_sleepcycle;
        }

        @Override @Nullable
        public View getActionView() {
            return null;
        }
        @Override @Nullable
        public View getConfigureActionView() {
            return button_configure;
        }

        @Override
        public void bindDataToHolder(Context context, @Nullable BedtimeItem item)
        {
            if (item != null)
            {
                if (text_sleepcycle != null)
                {
                    long sleepCycleMs = BedtimeSettings.loadPrefSleepCycleMs(context);
                    float sleepCycleCount = BedtimeSettings.loadPrefSleepCycleCount(context);

                    String sleepCycleCountString = String.format(SuntimesUtils.getLocale(), "%.0f", sleepCycleCount);
                    String sleepCycleString = utils.timeDeltaLongDisplayString(sleepCycleMs);
                    String hoursString = utils.timeDeltaLongDisplayString((long)(sleepCycleMs * sleepCycleCount));

                    String displayString = "Sleep for " + hoursString
                            + " (" + sleepCycleCountString + " sleep cycles of " + sleepCycleString + ")";  // TODO

                    SpannableString sleepTimeDisplay = SuntimesUtils.createBoldSpan(null, displayString, sleepCycleString);
                    sleepTimeDisplay = SuntimesUtils.createBoldSpan(sleepTimeDisplay, displayString, sleepCycleCountString);
                    sleepTimeDisplay = SuntimesUtils.createBoldSpan(sleepTimeDisplay, displayString, hoursString);
                    text_sleepcycle.setText(sleepTimeDisplay);
                }

            } else {
                clearViews();
            }
        }

        protected void clearViews()
        {
            if (text_sleepcycle != null) {
                text_sleepcycle.setText("");
            }
        }

        @Override
        protected void attachClickListeners(final Context context, final @Nullable BedtimeItem item) {
        }

        @Override
        protected void detachClickListeners()
        {
            super.detachClickListeners();
            if (button_configure != null) {
                button_configure.setOnClickListener(null);
            }
        }
    }

    /**
     * BedtimeNow
     */
    public static final class AlarmBedtimeViewHolder_BedtimeNow extends BedtimeViewHolder
    {
        protected Button nowButton;
        protected Button dismissButton;
        protected View headerLayout;

        public AlarmBedtimeViewHolder_BedtimeNow(View view)
        {
            super(view);
            nowButton = (Button)view.findViewById(R.id.button_bedtime_now);
            dismissButton = (Button)view.findViewById(R.id.button_bedtime_dismiss);
            headerLayout = view.findViewById(R.id.layout_header);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_bedtime_now;
        }

        public View getActionView() {
            return nowButton;
        }

        @Override
        public void bindDataToHolder(Context context, @Nullable BedtimeItem item)
        {
            if (item != null)
            {
                boolean isActive = BedtimeSettings.isBedtimeModeActive(context);
                if (headerLayout != null) {
                    headerLayout.setVisibility(isActive ? View.VISIBLE : View.GONE);
                }
                if (nowButton != null) {
                    nowButton.setVisibility(isActive ? View.GONE : View.VISIBLE);
                }
                if (dismissButton != null) {
                    dismissButton.setVisibility(isActive ? View.VISIBLE : View.GONE);
                }
            } else {
                if (headerLayout != null) {
                    headerLayout.setVisibility(View.GONE);
                }
                if (nowButton != null) {
                    nowButton.setVisibility(View.GONE);
                }
                if (dismissButton != null) {
                    dismissButton.setVisibility(View.GONE);
                }
            }
        }

        @Override
        protected void attachClickListeners(final Context context, final @Nullable BedtimeItem item)
        {
            if (dismissButton != null) {
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BedtimeAlarmHelper.dismissBedtimeEvent(context);
                    }
                });
            }
        }

        @Override
        protected void detachClickListeners()
        {
            super.detachClickListeners();
            if (nowButton != null) {
                nowButton.setOnClickListener(null);
            }
            if (dismissButton != null) {
                dismissButton.setOnClickListener(null);
            }
        }

    }
}