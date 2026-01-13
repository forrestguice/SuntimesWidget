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

package com.forrestguice.suntimeswidget.alarmclock.bedtime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmListDialog;


import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.widget.FloatingActionButton;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.support.widget.ImageViewCompat;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.support.widget.RecyclerView;
import com.forrestguice.support.widget.SwitchCompat;
import com.forrestguice.util.text.TimeDisplayText;

import java.util.Calendar;
import java.util.TimeZone;

public abstract class BedtimeViewHolder extends RecyclerView.ViewHolder
{
    protected SuntimesUtils utils = new SuntimesUtils();

    public BedtimeViewHolder(View view)
    {
        super(view);
        clearViews();
    }

    public void bindDataToHolder(Context context, @Nullable BedtimeItem item) {
        updateViews(context, item);
    }
    protected void onRecycled() {
        clearViews();
        detachClickListeners();
        updateTask = null;
    }

    protected void attachClickListeners(Context context, @Nullable BedtimeItem item) {}
    protected void detachClickListeners()
    {
        View clickView = getClickView();
        if (clickView != null) {
            clickView.setOnClickListener(null);
        }

        View actionView = getActionView();
        if (actionView != null) {
            actionView.setOnClickListener(null);
        }

        View configActionView = getConfigureActionView();
        if (configActionView != null) {
            configActionView.setOnClickListener(null);
        }
    }

    protected void clearViews() {}
    protected void updateViews(Context context, BedtimeItem item) {}

    @Nullable
    public View getClickView() {
        return null;
    }
    @Nullable
    public View getSharedView() {
        return null;
    }
    @Nullable
    public View getActionView() {
        return null;
    }
    @Nullable
    public View getConfigureActionView() {
        return null;
    }

    @Nullable
    protected Runnable updateTask = null;
    protected void setUpdateTask(@Nullable Runnable value)
    {
        boolean wasRunning = taskIsRunning;
        if (updateTask != null) {
            stopUpdateTask();
        }
        updateTask = value;

        if (wasRunning && value != null) {
            startUpdateTask();
        }
    }

    public void startUpdateTask()
    {
        if (itemView != null && updateTask != null) {
            itemView.removeCallbacks(updateTask);
            itemView.post(updateTask);
            taskIsRunning = true;
        }
    }
    public void stopUpdateTask()
    {
        if (itemView != null && updateTask != null) {
            itemView.removeCallbacks(updateTask);
            taskIsRunning = false;
        }
    }
    private boolean taskIsRunning = false;

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
    }

    /**
     * AlarmBedtimeViewHolder_AlarmItem
     */
    public static abstract class AlarmBedtimeViewHolder_AlarmItem extends BedtimeViewHolder
    {
        protected View card;
        protected TextView text_label;
        protected View text_time_layout;
        protected TextView text_time;
        protected TextView text_time_suffix;
        protected SwitchCompat switch_enabled;

        protected View layout_more;
        protected TextView status_sound;
        protected ImageView status_silent;
        protected ImageView status_vibrate;

        protected FloatingActionButton button_add;
        protected FloatingActionButton button_edit;

        protected CompoundButton.OnCheckedChangeListener onSwitchChanged;

        public AlarmBedtimeViewHolder_AlarmItem(View view)
        {
            super(view);
            card = view.findViewById(R.id.card);
            text_label = (TextView) view.findViewById(R.id.text_label);
            text_time_layout = view.findViewById(R.id.text_time_layout);
            text_time = (TextView) view.findViewById(R.id.text_time);
            text_time_suffix = (TextView) view.findViewById(R.id.text_time_suffix);
            switch_enabled = (SwitchCompat) view.findViewById(R.id.switch_enabled);
            layout_more = view.findViewById(R.id.layout_more);
            status_sound = (TextView) view.findViewById(R.id.status_sound);
            status_silent = (ImageView) view.findViewById(R.id.status_silent);
            status_vibrate = (ImageView) view.findViewById(R.id.status_vibrate);
            button_add = (FloatingActionButton) view.findViewById(R.id.button_add);
            button_edit = (FloatingActionButton) view.findViewById(R.id.button_edit);
        }

        @Nullable
        public View getClickView() {
            return null;  //text_time_layout;
        }
        @Nullable
        public View getSharedView() {
            return text_time;
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
        public void bindDataToHolder(final Context context, @Nullable final BedtimeItem item)
        {
            if (item == null) {
                updateViews(context, null);
                return;
            }

            /*item.setAlarmItem(null);
            loadAlarmItem(context, item, item.getAlarmID(context), new AlarmListDialog.AlarmListTask.AlarmListTaskListener()
            {
                @Override
                public void onLoadFinished(List<AlarmClockItem> result)
                {
                    if (result != null && result.size() > 0) {
                        item.setAlarmItem(result.get(0));
                    }
                    updateViews(context, item);
                }
            });*/
            updateViews(context, item);
        }
        protected void onRecycled() {
            super.onRecycled();
            //setAlarmItem(null);
        }

        protected void loadAlarmItem(Context context, BedtimeItem item, @Nullable Long rowID, AlarmListDialog.AlarmListTask.AlarmListTaskListener taskListener)
        {
            if (rowID != null && rowID != BedtimeSettings.ID_NONE)
            {
                AlarmListDialog.AlarmListTask listTask = new AlarmListDialog.AlarmListTask(context);
                listTask.setTaskListener(taskListener);
                listTask.execute(rowID);

            } else {
                updateViews(context, item);
            }
        }

        @Override
        protected void attachClickListeners(final Context context, @Nullable BedtimeItem item)
        {
            if (switch_enabled != null) {
                switch_enabled.setOnCheckedChangeListener(onSwitchChanged = onSwitchChanged(context, item));
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

        @Override
        protected void clearViews()
        {
            super.clearViews();
            if (text_time != null) {
                text_time.setText("");
            }
            if (text_time_suffix != null) {
                text_time_suffix.setText("");
            }
            if (status_sound != null) {
                status_sound.setText("");
                status_sound.setVisibility(View.GONE);
            }
            if (status_silent != null) {
                status_silent.setVisibility(View.GONE);
            }
            if (status_vibrate != null) {
                status_vibrate.setVisibility(View.GONE);
            }
            if (button_edit != null) {
                button_edit.hide();
            }
            if (switch_enabled != null) {
                switch_enabled.setVisibility(View.INVISIBLE);
            }
            if (text_time_layout != null) {
                text_time_layout.setVisibility(View.GONE);
            }
        }

        protected void setCardBackground(Context context, int resId)
        {
            if (card != null)
            {
                Drawable background0 = ContextCompat.getDrawable(context, resId);
                Drawable background = (background0 != null ? background0.mutate() : null);
                if (Build.VERSION.SDK_INT >= 16) {
                    card.setBackground(background);
                } else {
                    card.setBackgroundDrawable(background);
                }
            }
        }

        @Override
        protected void updateViews(Context context, @Nullable BedtimeItem item)
        {
            if (item == null) {
                clearViews();
                return;
            }

            if (context != null)
            {
                int[] attrs = { R.attr.alarmColorEnabled, R.attr.colorControlNormal,
                        R.attr.alarmCardEnabled, R.attr.alarmCardDisabled };
                TypedArray a = context.obtainStyledAttributes(attrs);
                @SuppressLint("ResourceType") int colorOn = ContextCompat.getColor(context, a.getResourceId(0, R.color.alarm_enabled));
                @SuppressLint("ResourceType") int colorOff = ContextCompat.getColor(context, a.getResourceId(1, R.color.white));
                @SuppressLint("ResourceType") int cardBgOn = a.getResourceId(2, R.drawable.card_alarmitem_enabled_dark);
                @SuppressLint("ResourceType") int cardBgOff = a.getResourceId(3, R.drawable.card_alarmitem_disabled_dark);
                a.recycle();

                AlarmClockItem alarmItem = item.getAlarmItem();
                if (alarmItem != null)
                {
                    setCardBackground(context, alarmItem.enabled ? cardBgOn : cardBgOff);

                    AlarmNotifications.updateAlarmTime(context, alarmItem);
                    Calendar alarmTime = Calendar.getInstance(TimeZone.getDefault());
                    alarmTime.setTimeInMillis(alarmItem.timestamp + alarmItem.offset);
                    TimeDisplayText timeText = utils.calendarTimeShortDisplayString(context, alarmTime, false);

                    if (text_label != null)
                    {
                        Drawable d = ContextCompat.wrap(getCompoundDrawableStart(context, text_label));
                        ContextCompat.setTint(d, alarmItem.enabled ? colorOn : colorOff);
                    }
                    if (text_time != null) {
                        text_time.setText(timeText.getValue());
                        //text_time.setTextColor(item.enabled ? colorOn : colorOff);
                    }
                    if (text_time_suffix != null) {
                        text_time_suffix.setText(timeText.getSuffix());
                        //text_time_suffix.setTextColor(item.enabled ? colorOn : colorOff);
                    }
                    if (text_time_layout != null) {
                        text_time_layout.setVisibility(View.VISIBLE);
                    }
                    if (switch_enabled != null)
                    {
                        switch_enabled.setOnCheckedChangeListener(null);
                        switch_enabled.setChecked(alarmItem.enabled);
                        switch_enabled.setOnCheckedChangeListener(onSwitchChanged);
                        switch_enabled.setVisibility(View.VISIBLE);
                    }
                    if (button_edit != null) {
                        button_edit.show();  //setVisibility(View.VISIBLE);
                    }

                    boolean hasSound = (alarmItem.ringtoneURI != null);
                    if (status_sound != null)
                    {
                        status_sound.setText(hasSound ? alarmItem.ringtoneName : "");
                        status_sound.setContentDescription(context.getString(R.string.alarmOption_ringtone_ringtone));
                        status_sound.setVisibility(hasSound ? View.VISIBLE : View.GONE);
                    }
                    if (status_silent != null) {
                        status_silent.setVisibility(hasSound ? View.GONE : View.VISIBLE);
                    }
                    if (status_vibrate != null) {
                        status_vibrate.setVisibility(alarmItem.vibrate ? View.VISIBLE : View.GONE);
                    }

                } else {
                    setCardBackground(context, cardBgOff);
                    if (text_label != null)
                    {
                        Drawable d = ContextCompat.wrap(getCompoundDrawableStart(context, text_label));
                        ContextCompat.setTint(d, colorOff);
                    }
                    if (text_time_layout != null) {
                        text_time_layout.setVisibility(View.GONE);
                    }
                    if (switch_enabled != null) {
                        switch_enabled.setVisibility(View.INVISIBLE);
                    }
                    if (status_sound != null) {
                        status_sound.setText("");
                        status_sound.setVisibility(View.GONE);
                    }
                    if (status_silent != null) {
                        status_silent.setVisibility(View.GONE);
                    }
                    if (status_vibrate != null) {
                        status_vibrate.setVisibility(View.GONE);
                    }
                }
            }
        }

        protected Drawable getCompoundDrawableStart(Context context, TextView view)
        {
            if (Build.VERSION.SDK_INT >= 17) {
                return view.getCompoundDrawablesRelative()[0].mutate();

            } else {
                Drawable[] drawables = view.getCompoundDrawables();
                return (context.getResources().getBoolean(R.bool.is_rtl) ? drawables[2] : drawables[0]).mutate();
            }
        }

        protected CompoundButton.OnCheckedChangeListener onSwitchChanged(final Context context, final BedtimeItem item)
        {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    toggleAlarm(context, item.getAlarmItem(), isChecked);
                    BedtimeItem linkedItem = item.getLinkedItem();

                    if (linkedItem != null && linkedItem.shouldMirrorParent(context)) {
                        toggleAlarm(context, linkedItem.getAlarmItem(), isChecked);
                    }
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
            View layout_check_dnd = view.findViewById(R.id.layout_check_dnd);
            if (layout_check_dnd != null) {
                layout_check_dnd.setVisibility(Build.VERSION.SDK_INT >= 23 ? View.VISIBLE : View.GONE);
            }
            layout_dndWarning = view.findViewById(R.id.dndwarning_layout);
            text_dndWarning = (TextView) view.findViewById(R.id.dndwarning_text);
            button_dndWarning = (Button) view.findViewById(R.id.dndwarning_button);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_bedtime_notify;
        }

        private final CompoundButton.OnCheckedChangeListener onDndCheckChanged = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                check_dnd.setChecked(!isChecked);
            }
        };

        @Override
        protected void attachClickListeners(final Context context, @Nullable BedtimeItem item)
        {
            super.attachClickListeners(context, item);
            if (check_dnd != null)
            {
                /*check_dnd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        check_dnd.setChecked(!isChecked);
                        //BedtimeSettings.savePrefBedtimeDoNotDisturb(context, isChecked);
                        //BedtimeSettings.setAutomaticZenRule(context, switch_enabled.isChecked() && isChecked);
                        //updateViews_dndWarning(context);
                    }
                });*/
                check_dnd.setOnCheckedChangeListener(onDndCheckChanged);
                check_dnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDndMenu(context, v);
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

        protected void showDndMenu(final Context context, View view)
        {
            PopupMenuCompat.PopupMenuListener menuClickListener = new PopupMenuCompat.PopupMenuListener()
            {
                @Override
                public void onUpdateMenu(Context context, Menu menu)
                {
                    boolean dnd = BedtimeSettings.loadPrefBedtimeDoNotDisturb(context);
                    //boolean dnd = BedtimeSettings.isAutomaticZenRuleEnabled(context);  //BedtimeSettings.loadPrefBedtimeDoNotDisturb(context);

                    if (dnd)
                    {
                        int filter = BedtimeSettings.loadPrefBedtimeDoNotDisturbFilter(context);
                        switch (filter)
                        {
                            case BedtimeSettings.DND_FILTER_ALARMS:
                                MenuItem dndAlarmsItem = menu.findItem(R.id.action_dnd_alarms);
                                if (dndAlarmsItem != null) {
                                    dndAlarmsItem.setChecked(true);
                                }
                                break;

                            case BedtimeSettings.DND_FILTER_PRIORITY:
                                MenuItem dndPriorityItem = menu.findItem(R.id.action_dnd_priority);
                                if (dndPriorityItem != null) {
                                    dndPriorityItem.setChecked(true);
                                }
                                break;
                        }

                    } else {
                        MenuItem dndDisabledItem = menu.findItem(R.id.action_dnd_disable);
                        if (dndDisabledItem != null) {
                            dndDisabledItem.setChecked(true);
                        }
                    }
                }

                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_dnd_alarms) {
                        BedtimeSettings.savePrefBedtimeDoNotDisturbFilter(context, BedtimeSettings.DND_FILTER_ALARMS);
                        BedtimeSettings.savePrefBedtimeDoNotDisturb(context, true);
                        BedtimeSettings.setAutomaticZenRule(context, true);
                        updateView_dnd(context);
                        return true;
                        
                    } else if (itemId == R.id.action_dnd_priority) {
                        BedtimeSettings.savePrefBedtimeDoNotDisturbFilter(context, BedtimeSettings.DND_FILTER_PRIORITY);
                        BedtimeSettings.savePrefBedtimeDoNotDisturb(context, true);
                        BedtimeSettings.setAutomaticZenRule(context, true);
                        updateView_dnd(context);
                        return true;
                    }
                    BedtimeSettings.savePrefBedtimeDoNotDisturb(context, false);
                    BedtimeSettings.setAutomaticZenRule(context, false);
                    updateView_dnd(context);
                    return true;
                }
            };

            PopupMenuCompat.createMenu(context, view, R.menu.bedtime_dnd, menuClickListener).show();
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

        protected  void updateView_dnd(Context context)
        {
            if (check_dnd != null) {
                check_dnd.setOnCheckedChangeListener(null);
                check_dnd.setChecked(BedtimeSettings.loadPrefBedtimeDoNotDisturb(context));
                check_dnd.setOnCheckedChangeListener(onDndCheckChanged);
            }
            updateViews_dndWarning(context);
        }

        protected void updateViews_dndWarning(Context context)
        {
            boolean showDndWarning = (check_dnd != null && check_dnd.isChecked() && !BedtimeSettings.hasDoNotDisturbPermission(context));
            if (layout_dndWarning != null) {
                layout_dndWarning.setVisibility(showDndWarning ? View.VISIBLE : View.GONE);
            }
            if (text_dndWarning != null) {
                text_dndWarning.setText(showDndWarning ? SuntimesUtils.fromHtml(context.getString(R.string.privacy_permission_dnd)) : "");
            }
        }

        @Override
        protected void updateViews(Context context, BedtimeItem item)
        {
            super.updateViews(context, item);
            if (context != null)
            {
                updateView_dnd(context);

                AlarmClockItem alarmItem = item.getAlarmItem();
                if (alarmItem != null)
                {
                    if (text_label != null) {
                        text_label.setText(context.getString(R.string.msg_bedtime_set));
                    }
                    if (button_add != null) {
                        button_add.hide();  //setVisibility(View.GONE);
                    }
                    if (button_edit != null) {
                        button_edit.show();  //setVisibility(View.VISIBLE);
                    }
                } else {
                    if (text_label != null) {
                        text_label.setText(context.getString(R.string.msg_bedtime_notset));
                    }
                    if (button_add != null) {
                        button_add.show();  //setVisibility(View.VISIBLE);
                    }
                    if (button_edit != null) {
                        button_edit.hide();  //setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                if (text_label != null) {
                    text_label.setText("");
                }
                if (button_add != null) {
                    button_add.hide();  //setVisibility(View.GONE);
                }
                if (button_edit != null) {
                    button_edit.hide();  //setVisibility(View.INVISIBLE);
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
            return R.layout.layout_listitem_bedtime_reminder;
        }

        @Override
        protected void attachClickListeners(final Context context, @Nullable BedtimeItem item) {
            super.attachClickListeners(context, item);
        }

        @Override
        protected CompoundButton.OnCheckedChangeListener onSwitchChanged(final Context context, final BedtimeItem item)
        {
            return new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    BedtimeSettings.savePrefBedtimeReminder(context, isChecked);
                    if (isChecked) {
                        BedtimeAlarmHelper.setBedtimeReminder_withReminderItem(context, item.getAlarmItem(), true);
                    } else {
                        toggleAlarm(context, item.getAlarmItem(), false);
                    }
                }
            };
        }

        @Override
        protected void updateViews(Context context, BedtimeItem item)
        {
            super.updateViews(context, item);

            AlarmClockItem alarmItem = item.getAlarmItem();
            //if (layout_more != null) {
            //    layout_more.setVisibility(alarmItem != null ? View.VISIBLE : View.GONE);
            //}
            if (switch_enabled != null) {
                switch_enabled.setVisibility(View.VISIBLE);
            }
            if (button_edit != null)
            {
                //button_edit.setVisibility(alarmItem != null ? View.VISIBLE : View.GONE);
                if (alarmItem != null)
                    button_edit.show();
                else button_edit.hide();
            }
            if (text_label != null)
            {
                if (context != null)
                {
                    //String offsetString = utils.timeDeltaLongDisplayString(alarmItem != null ? alarmItem.offset : BedtimeSettings.loadPrefBedtimeReminderOffset(context));
                    long offset = BedtimeSettings.loadPrefBedtimeReminderOffset(context);
                    if (alarmItem != null)
                    {
                        BedtimeItem linkedItem = item.getLinkedItem();
                        if (linkedItem != null)
                        {
                            AlarmClockItem linkedAlarmItem = linkedItem.getAlarmItem();
                            if (linkedAlarmItem != null) {
                                offset = (alarmItem.getEvent() == null ? alarmItem.offset : alarmItem.offset - linkedAlarmItem.offset);
                            }
                        }
                    }

                    String offsetString = utils.timeDeltaLongDisplayString(offset);
                    String labelString = context.getString(R.string.msg_bedtime_reminder, offsetString);
                    CharSequence labelDisplay = SuntimesUtils.createBoldSpan(null, labelString, offsetString);
                    text_label.setText(labelDisplay);

                } else {
                    text_label.setText("");
                }
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
        protected void attachClickListeners(final Context context, @Nullable BedtimeItem item) {
            super.attachClickListeners(context, item);
        }

        @Override
        protected void updateViews(Context context, BedtimeItem item)
        {
            super.updateViews(context, item);

            AlarmClockItem alarmItem = item.getAlarmItem();
            if (layout_more != null) {
                layout_more.setVisibility(alarmItem != null ? View.VISIBLE : View.GONE);
            }

            if (alarmItem != null)
            {
                if (text_label != null) {
                    text_label.setText(context != null ? context.getString(R.string.msg_bedtime_wakeup_set) : "");
                }
                if (button_add != null) {
                    button_add.hide();  //setVisibility(View.GONE);
                }
                if (button_edit != null) {
                    button_edit.show();  //setVisibility(View.VISIBLE);
                }

            } else {
                if (text_label != null) {
                    text_label.setText(context != null ? context.getString(R.string.msg_bedtime_wakeup_notset) : "");
                }
                if (button_add != null) {
                    button_add.show();  //.setVisibility(View.VISIBLE);
                }
                if (button_edit != null) {
                    button_edit.hide();  //.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * SleepCycle
     */
    public static final class AlarmBedtimeViewHolder_SleepCycle extends BedtimeViewHolder
    {
        protected View card;
        protected ImageButton button_configure;
        protected TextView text_totalsleep;
        protected TextView text_sleepcycle;

        public AlarmBedtimeViewHolder_SleepCycle(View view)
        {
            super(view);
            card = view.findViewById(R.id.card);
            text_totalsleep = (TextView) view.findViewById(R.id.text_totalsleep);
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
        protected void updateViews(Context context, BedtimeItem item)
        {
            int[] attrs = { R.attr.alarmColorEnabled, R.attr.text_primaryColor,
                    R.attr.alarmCardEnabled, R.attr.alarmCardDisabled };
            TypedArray a = context.obtainStyledAttributes(attrs);
            @SuppressLint("ResourceType") int colorOn = ContextCompat.getColor(context, a.getResourceId(0, R.color.alarm_enabled));
            @SuppressLint("ResourceType") int colorOff = ContextCompat.getColor(context, a.getResourceId(1, R.color.text_primary_dark));
            @SuppressLint("ResourceType") int cardBgOn = a.getResourceId(2, R.drawable.card_alarmitem_enabled_dark);
            @SuppressLint("ResourceType") int cardBgOff = a.getResourceId(3, R.drawable.card_alarmitem_disabled_dark);
            a.recycle();

            AlarmClockItem bedtimeOff = item.getAlarmItem();
            boolean enabled = (bedtimeOff != null && bedtimeOff.enabled);
            setCardBackground(context, enabled ? cardBgOn : cardBgOff);

            if (item != null)
            {
                if (text_totalsleep != null)
                {
                    long sleepTotalMs = BedtimeSettings.totalSleepTimeMs(context);
                    String sleepTotalString = utils.timeDeltaLongDisplayString(sleepTotalMs);
                    String displayString = context.getString(R.string.msg_bedtime_sleep_length, sleepTotalString);
                    SpannableString sleepTimeDisplay = SuntimesUtils.createBoldSpan(null, displayString, sleepTotalString);
                    text_totalsleep.setText(sleepTimeDisplay);
                }

                if (text_sleepcycle != null)
                {
                    long sleepOffsetMs = BedtimeSettings.loadPrefSleepOffsetMs(context);
                    String offsetString = utils.timeDeltaLongDisplayString(sleepOffsetMs);

                    boolean useSleepCycle = BedtimeSettings.loadPrefUseSleepCycle(context);
                    if (useSleepCycle)
                    {
                        long sleepCycleMs = BedtimeSettings.loadPrefSleepCycleMs(context);
                        int sleepCycleCount = (int) BedtimeSettings.loadPrefSleepCycleCount(context);

                        String sleepCycleCountString = context.getResources().getQuantityString(R.plurals.cyclePlural, sleepCycleCount, sleepCycleCount);  //String.format(SuntimesUtils.getLocale(), "%.0f", sleepCycleCount);
                        String sleepCycleString = utils.timeDeltaLongDisplayString(sleepCycleMs);
                        String sleepCycleHoursString = utils.timeDeltaLongDisplayString((long)(sleepCycleMs * sleepCycleCount));

                        String displayString = (sleepOffsetMs > 0)
                                ? context.getString(R.string.msg_bedtime_sleep_length_cycles_plus, sleepCycleCountString, sleepCycleString, offsetString)
                                : context.getString(R.string.msg_bedtime_sleep_length_cycles, sleepCycleCountString, sleepCycleString);

                        SpannableString sleepTimeDisplay = SuntimesUtils.createBoldSpan(null, displayString, sleepCycleString);
                        sleepTimeDisplay = SuntimesUtils.createBoldSpan(sleepTimeDisplay, displayString, sleepCycleCountString);
                        sleepTimeDisplay = SuntimesUtils.createBoldSpan(sleepTimeDisplay, displayString, sleepCycleHoursString);
                        sleepTimeDisplay = SuntimesUtils.createBoldSpan(sleepTimeDisplay, displayString, offsetString);
                        text_sleepcycle.setText(sleepTimeDisplay);

                    } else {
                        long sleepMs = BedtimeSettings.loadPrefSleepMs(context);
                        String sleepTimeString = utils.timeDeltaLongDisplayString(sleepMs);
                        String displayString = (sleepOffsetMs > 0)
                                ? context.getString(R.string.msg_bedtime_sleep_length_other_plus, sleepTimeString, offsetString)
                                : "";

                        SpannableString sleepTimeDisplay = SuntimesUtils.createBoldSpan(null, displayString, sleepTimeString);
                        sleepTimeDisplay = SuntimesUtils.createBoldSpan(sleepTimeDisplay, displayString, offsetString);
                        text_sleepcycle.setText(sleepTimeDisplay);
                    }
                }

            } else {
                clearViews();
            }
        }

        @Override
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

        protected void setCardBackground(Context context, int resId)
        {
            if (card != null)
            {
                Drawable background0 = ContextCompat.getDrawable(context, resId);
                Drawable background = (background0 != null ? background0.mutate() : null);
                if (Build.VERSION.SDK_INT >= 16) {
                    card.setBackground(background);
                } else {
                    card.setBackgroundDrawable(background);
                }
            }
        }
    }

    /**
     * BedtimeNow
     */
    public static final class AlarmBedtimeViewHolder_BedtimeNow extends BedtimeViewHolder
    {
        protected Button nowButton;
        protected Button pauseButton;
        protected Button resumeButton;
        protected Button dismissButton;
        protected View frameLayout;
        protected View headerLayout;
        protected ImageView icon;
        protected TextSwitcher label;
        protected TextSwitcher note;

        public AlarmBedtimeViewHolder_BedtimeNow(View view)
        {
            super(view);
            nowButton = (Button) view.findViewById(R.id.button_bedtime_now);
            pauseButton = (Button) view.findViewById(R.id.button_bedtime_pause);
            resumeButton = (Button) view.findViewById(R.id.button_bedtime_resume);
            dismissButton = (Button) view.findViewById(R.id.button_bedtime_dismiss);
            headerLayout = view.findViewById(R.id.layout_header);
            frameLayout = view.findViewById(R.id.layout_frame);

            label = (TextSwitcher) view.findViewById(R.id.text_label);
            icon = (ImageView) view.findViewById(R.id.icon_label);
            note = (TextSwitcher) view.findViewById(R.id.text_note);
        }

        public static int getLayoutResID() {
            return R.layout.layout_listitem_bedtime_now;
        }

        public void bindDataToHolder(final Context context, final @Nullable BedtimeItem item)
        {
            super.bindDataToHolder(context, item);
            if (item != null)
            {
                setUpdateTask(updateTask(context, item));
                startUpdateTask();
            }
        }

        protected Runnable updateTask(final Context context, final BedtimeItem item)
        {
            return new Runnable()
            {
                @Override
                public void run()
                {
                    //Log.d("DEBUG", "updateTask: tick");
                    if (note != null && updateNote(context, item)) {
                        note.postDelayed(this, UPDATE_RATE);
                    }
                }
            };
        }
        public static final int UPDATE_RATE = 3000;

        public View getActionView() {
            return nowButton;
        }

        protected boolean updateNote(Context context, BedtimeItem item)
        {
            AlarmClockItem bedtime = item.getAlarmItem();
            if (bedtime != null && bedtime.enabled)
            {
                Calendar now = Calendar.getInstance();
                AlarmNotifications.updateAlarmTime(context, bedtime, now, true);
                String deltaString = utils.timeDeltaLongDisplayString(now.getTimeInMillis(), bedtime.timestamp + bedtime.offset).getValue();
                String noteString = context.getString(R.string.msg_bedtime_note, deltaString);
                CharSequence noteDisplay = SuntimesUtils.createBoldSpan(null, noteString, deltaString);
                note.setText(noteDisplay);
                return true;

            } else {
                note.setText("");
                note.setVisibility(View.GONE);
                return false;
            }
        }

        protected static void setTextColor(@NonNull TextSwitcher textSwitcher, int color)
        {
            View[] views = new View[] { textSwitcher.getChildAt(0), textSwitcher.getChildAt(1) };
            for (View view : views) {
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    textView.setTextColor(color);
                }
            }
        }
        @Nullable
        protected static ColorStateList getTextColors(@NonNull TextSwitcher textSwitcher)
        {
            View[] views = new View[] { textSwitcher.getChildAt(0), textSwitcher.getChildAt(1) };
            for (View view : views) {
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    return textView.getTextColors();    // return first match
                }
            }
            return null;
        }
        protected static int getTextColor(@NonNull TextSwitcher textSwitcher)
        {
            View[] views = new View[] { textSwitcher.getChildAt(0), textSwitcher.getChildAt(1) };
            for (View view : views) {
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    return textView.getCurrentTextColor();
                }
            }
            return Color.WHITE;    // TODO
        }

        /*protected static ValueAnimator animateColorChange(final ImageView view, int colorTo, int colorFrom, long duration)
        {
            ValueAnimator animation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ImageViewCompat.setImageTintList(view, ColorStateList.valueOf((Integer) animation.getAnimatedValue()));
                }
            });
            animation.setDuration(duration);
            return animation;
        }
        protected static ValueAnimator animateColorChange(final TextView text, int colorTo, int colorFrom, long duration)
        {
            ValueAnimator animation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    text.setTextColor((Integer) animation.getAnimatedValue());
                }
            });
            animation.setDuration(duration);
            return animation;
        }
        protected static ValueAnimator animateColorChange(final TextSwitcher text, int colorTo, int colorFrom, long duration)
        {
            ValueAnimator animation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTextColor(text, (Integer) animation.getAnimatedValue());
                }
            });
            animation.setDuration(duration);
            return animation;
        }*/

        @Override
        protected void updateViews(final Context context, final BedtimeItem item)
        {
            if (item != null)
            {
                int[] attrs = { R.attr.text_disabledColor, R.attr.text_primaryColor };
                TypedArray a = context.obtainStyledAttributes(attrs);
                @SuppressLint("ResourceType") final int colorOn = ContextCompat.getColor(icon.getContext(), a.getResourceId(0, R.color.text_disabled_dark));
                @SuppressLint("ResourceType") final int colorOff = ContextCompat.getColor(icon.getContext(), a.getResourceId(1, R.color.text_primary_dark));
                a.recycle();

                final boolean isActive = BedtimeSettings.isBedtimeModeActive(context);
                final boolean isPaused = BedtimeSettings.isBedtimeModePaused(context);
                int colorTo = ((isActive && !isPaused) ? colorOn : colorOff);
                int colorFrom = ((isActive && !isPaused) ? colorOff : colorOn);

                if (frameLayout != null)
                {
                    ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
                    params.height = (isActive ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT);
                    frameLayout.setLayoutParams(params);
                }
                if (headerLayout != null) {
                    headerLayout.setVisibility(isActive ? View.VISIBLE : View.GONE);
                }
                if (label != null) {
                    label.setText(SuntimesUtils.fromHtml(!isActive ? "" : context.getString(isPaused ? R.string.msg_bedtime_paused : R.string.msg_bedtime_active)));
                    //animateColorChange(label, colorTo, colorFrom, 1500).start();    // TODO: duration from resource
                    //label.setVisibility(isActive ? View.VISIBLE : View.GONE);
                    setTextColor(label, colorTo);
                }
                if (icon != null ) {
                    //animateColorChange(icon, colorTo, colorFrom, 1500).start();    // TODO: duration from resource
                    ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(colorTo));
                }
                if (note != null) {
                    note.setVisibility(isActive ? View.GONE : View.VISIBLE);
                    updateNote(context, item);
                }
                if (nowButton != null) {
                    nowButton.setVisibility(isActive ? View.GONE : View.VISIBLE);
                }
                if (dismissButton != null) {
                    dismissButton.setVisibility(isActive ? View.VISIBLE : View.GONE);
                }
                if (pauseButton != null) {
                    pauseButton.setVisibility(isActive && !isPaused ? View.VISIBLE : View.GONE);
                }
                if (resumeButton != null) {
                    resumeButton.setVisibility(isActive && isPaused ? View.VISIBLE : View.GONE);
                }
            } else {
                clearViews();
            }
        }

        @Override
        protected void clearViews()
        {
            super.clearViews();
            if (headerLayout != null) {
                headerLayout.setVisibility(View.GONE);
            }
            if (frameLayout != null)
            {
                ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                frameLayout.setLayoutParams(params);
            }
            if (label != null) {
                label.setText("");
            }
            /*if (icon != null ) {
                ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(color));
            }*/
            if (note != null) {
                note.setText("");
                note.setVisibility(View.GONE);
            }
            if (nowButton != null) {
                nowButton.setVisibility(View.GONE);
            }
            if (dismissButton != null) {
                dismissButton.setVisibility(View.GONE);
            }
            if (pauseButton != null) {
                pauseButton.setVisibility(View.GONE);
            }
            if (resumeButton != null) {
                resumeButton.setVisibility(View.GONE);
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
            if (pauseButton != null) {
                pauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BedtimeAlarmHelper.pauseBedtimeEvent(context);
                    }
                });
            }
            if (resumeButton != null) {
                resumeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BedtimeAlarmHelper.resumeBedtimeEvent(context);
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
            if (pauseButton != null) {
                pauseButton.setOnClickListener(null);
            }
            if (resumeButton != null) {
                resumeButton.setOnClickListener(null);
            }
        }

    }
}