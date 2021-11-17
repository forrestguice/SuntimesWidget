/**
    Copyright (C) 2020 Forrest Guice
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.settings.SolarEventIcons;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetActions;

import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("Convert2Diamond")
public class AlarmEditViewHolder extends RecyclerView.ViewHolder
{
    public static SuntimesUtils utils = new SuntimesUtils();

    public int position = RecyclerView.NO_POSITION;
    public boolean selected = true;
    public boolean preview_offset = false;

    public View layout_datetime;
    public ImageView icon_datetime_offset;
    public TextView text_datetime_offset;
    public TextSwitcher text_datetime;
    public TextView text_date;
    public TextView text_note;

    public ImageButton menu_type, menu_overflow;
    public EditText edit_label;

    public View chip_offset;
    public TextView text_offset;

    public View chip_event;
    public TextView text_event;

    public View chip_location;
    public TextView text_location;

    public View chip_repeat;
    public TextView text_repeat;

    public View chip_ringtone;
    public TextView text_ringtone;

    public View chip_vibrate;
    public CheckBox check_vibrate;

    public View chip_action0;
    public TextView text_action0;

    public View chip_action1;
    public TextView text_action1;

    public int res_icAlarm, res_icNotification;
    public int res_icSoundOn, res_icSoundOff;
    public int res_colorEnabled;
    public int res_icOffset;

    public AlarmEditViewHolder(View parent)
    {
        super(parent);
        Context context = parent.getContext();
        SuntimesUtils.initDisplayStrings(context);

        layout_datetime = parent.findViewById(R.id.layout_datetime);
        icon_datetime_offset = (ImageView) parent.findViewById(R.id.icon_datetime_offset);
        text_datetime_offset = (TextView) parent.findViewById(R.id.text_datetime_offset);
        text_datetime = (TextSwitcher) parent.findViewById(R.id.text_datetime);
        text_date = (TextView) parent.findViewById(R.id.text_date);
        text_note = (TextView) parent.findViewById(R.id.text_note);

        menu_type = (ImageButton) parent.findViewById(R.id.type_menu);
        menu_overflow = (ImageButton) parent.findViewById(R.id.overflow_menu);
        edit_label = (EditText) parent.findViewById(R.id.edit_label);

        chip_offset = parent.findViewById(R.id.chip_offset);
        text_offset = (TextView) parent.findViewById(R.id.text_offset);

        chip_event = parent.findViewById(R.id.chip_event);
        text_event = (TextView) parent.findViewById(R.id.text_event);

        chip_location = parent.findViewById(R.id.chip_location);
        text_location = (TextView) parent.findViewById(R.id.text_location);

        chip_repeat = parent.findViewById(R.id.chip_repeat);
        text_repeat = (TextView) parent.findViewById(R.id.text_repeat);

        chip_ringtone = parent.findViewById(R.id.chip_ringtone);
        text_ringtone = (TextView) parent.findViewById(R.id.text_ringtone);

        chip_vibrate = parent.findViewById(R.id.chip_vibrate);
        check_vibrate = (CheckBox) parent.findViewById(R.id.check_vibrate);

        chip_action0 = parent.findViewById(R.id.chip_action0);
        text_action0 = (TextView) parent.findViewById(R.id.text_action0);

        chip_action1 = parent.findViewById(R.id.chip_action1);
        text_action1 = (TextView) parent.findViewById(R.id.text_action1);

        themeHolder(context);
    }

    @SuppressLint("ResourceType")
    public void themeHolder(Context context)
    {
        int[] attrs = { R.attr.icActionAlarm, R.attr.icActionNotification, R.attr.icActionSoundEnabled, R.attr.icActionSoundDisabled, R.attr.alarmColorEnabled, R.attr.icActionTimeReset };
        TypedArray a = context.obtainStyledAttributes(attrs);
        res_icAlarm = a.getResourceId(0, R.drawable.ic_action_extension);
        res_icNotification = a.getResourceId(1, R.drawable.ic_action_notification);
        res_icSoundOn = a.getResourceId(2, R.drawable.ic_action_soundenabled);
        res_icSoundOff = a.getResourceId(3, R.drawable.ic_action_sounddisabled);
        res_colorEnabled = a.getResourceId(4, R.color.alarm_enabled_dark);
        res_icOffset = a.getResourceId(5, R.drawable.ic_action_timereset);;
        a.recycle();
    }

    public void bindDataToPosition(Context context, AlarmClockItem item, int position)
    {
        this.position = position;

        if (item != null)
        {
            boolean isSchedulable = AlarmNotifications.updateAlarmTime(context, item, Calendar.getInstance(), false);
            float iconSize = context.getResources().getDimension(R.dimen.eventIcon_width);

            menu_type.setImageDrawable(ContextCompat.getDrawable(context, (item.type == AlarmClockItem.AlarmType.ALARM ? res_icAlarm : res_icNotification)));
            menu_type.setContentDescription(item.type.getDisplayString());

            edit_label.setText(item.getLabel(context));

            text_offset.setText(displayOffset(context, item));

            if (item.offset != 0)
            {
                int iconMargin = (int)context.getResources().getDimension(R.dimen.eventIcon_margin1);
                Drawable offsetIcon = ContextCompat.getDrawable(context, res_icOffset).mutate();
                offsetIcon = new InsetDrawable(offsetIcon, iconMargin, iconMargin, iconMargin, iconMargin);
                offsetIcon.setBounds(0, 0, (int)iconSize, (int)iconSize);
                text_offset.setCompoundDrawablePadding(iconMargin);
                text_offset.setCompoundDrawables(offsetIcon, null, null, null);

            } else {
                text_offset.setCompoundDrawables(null, null, null, null);
            }

            text_location.setText(item.location.getLabel());
            text_repeat.setText( displayRepeating(context, item, selected));

            text_event.setText(displayEvent(context, item));

            SolarEvents event = SolarEvents.valueOf(item.getEvent(), null);
            if (event != null)
            {
                Drawable eventIcon = SolarEventIcons.getIconDrawable(context, event, (int)iconSize, (int)iconSize);
                text_event.setCompoundDrawablePadding(SolarEventIcons.getIconDrawablePadding(context, event));
                text_event.setCompoundDrawables(eventIcon, null, null, null);

            } else {
                Drawable eventIcon = SolarEventIcons.getIconDrawable(context, item.timezone, (int)iconSize, (int)iconSize);
                text_event.setCompoundDrawablePadding(SolarEventIcons.getIconDrawablePadding(context, item.timezone));
                text_event.setCompoundDrawables(eventIcon, null, null, null);
            }

            Drawable ringtoneIcon = ContextCompat.getDrawable(context, (item.ringtoneName != null ? res_icSoundOn : res_icSoundOff));
            text_ringtone.setCompoundDrawablesWithIntrinsicBounds(ringtoneIcon, null, null, null);
            text_ringtone.setText( displayRingtone(context, item, selected) );

            check_vibrate.setChecked(item.vibrate);
            text_action0.setText(displayAction(context, item, 0));
            text_action1.setText(displayAction(context, item, 1));

            text_datetime_offset.setText(isSchedulable ? text_offset.getText() : "");
            text_datetime_offset.setVisibility(preview_offset ? View.INVISIBLE : View.VISIBLE);
            icon_datetime_offset.setVisibility(preview_offset ? View.VISIBLE : View.INVISIBLE);
            icon_datetime_offset.setContentDescription(context.getString( item.offset < 0 ? R.string.offset_button_before : R.string.offset_button_after));

            text_datetime.setText(isSchedulable ? displayAlarmTime(context, item, preview_offset) : "");
            ViewCompat.setTransitionName(text_datetime, "transition_" + item.rowID);

            text_date.setText(isSchedulable ? displayAlarmDate(context, item, preview_offset) : "");
            text_date.setVisibility(isSchedulable && AlarmEditViewHolder.showAlarmDate(context, item) ? View.VISIBLE : View.GONE);

            text_note.setText(AlarmEditViewHolder.displayAlarmNote(context, item, isSchedulable));


            /*if (item.enabled) {
                TextView v = (TextView)text_datetime.getCurrentView();
                v.setTextColor(ContextCompat.getColor(context, res_colorEnabled));
                v = (TextView)text_datetime.getNextView();
                v.setTextColor(ContextCompat.getColor(context, res_colorEnabled));
            }*/

        } else {
            text_datetime_offset.setText("");
            text_datetime.setText("");
            ViewCompat.setTransitionName(text_datetime, null);
            text_date.setText("");
            text_note.setText("");
            edit_label.setText("");
            text_offset.setText("");
            text_event.setText("");
            text_location.setText("");
            text_repeat.setText("");
            text_repeat.setText("");
            check_vibrate.setChecked(false);
            text_action0.setText("");
            text_action1.setText("");
        }
    }

    public void detachClickListeners()
    {
        layout_datetime.setOnClickListener(null);
        menu_type.setOnClickListener(null);
        menu_overflow.setOnClickListener(null);
        edit_label.setOnClickListener(null);
        chip_offset.setOnClickListener(null);
        chip_offset.setOnClickListener(null);
        chip_event.setOnClickListener(null);
        chip_location.setOnClickListener(null);
        chip_repeat.setOnClickListener(null);
        chip_ringtone.setOnClickListener(null);
        check_vibrate.setOnClickListener(null);
        check_vibrate.setOnCheckedChangeListener(null);
        chip_action0.setOnClickListener(null);
        chip_action1.setOnClickListener(null);
    }

    public static CharSequence displayAlarmLabel(Context context, AlarmClockItem item)
    {
        String emptyLabel = ((item.type == AlarmClockItem.AlarmType.ALARM) ? context.getString(R.string.alarmMode_alarm) : context.getString(R.string.alarmMode_notification));
        return (item.label == null || item.label.isEmpty()) ? emptyLabel : item.label;
    }

    public static CharSequence displayAlarmTime(Context context, AlarmClockItem item) {
        return displayAlarmTime(context, item, false);
    }
    public static CharSequence displayAlarmTime(Context context, AlarmClockItem item, boolean withOffset)
    {
        Calendar alarmTime = Calendar.getInstance(TimeZone.getDefault());
        alarmTime.setTimeInMillis(item.timestamp + (withOffset ? item.offset : 0));

        CharSequence alarmDesc;
        SuntimesUtils utils = new SuntimesUtils();
        SuntimesUtils.initDisplayStrings(context);
        SuntimesUtils.TimeDisplayText timeText = utils.calendarTimeShortDisplayString(context, alarmTime, false);
        if (SuntimesUtils.is24()) {
            alarmDesc = timeText.getValue();

        } else {
            String timeString = timeText.getValue() + " " + timeText.getSuffix();
            alarmDesc = SuntimesUtils.createRelativeSpan(null, timeString, " " + timeText.getSuffix(), 0.40f);
        }
        return alarmDesc;
    }

    public static CharSequence displayAlarmNote(Context context, AlarmClockItem item, boolean isSchedulable)
    {
        if (isSchedulable)
        {
            int[] attrs = { android.R.attr.textColorPrimary };   // TODO: from SuntimesTheme
            TypedArray a = context.obtainStyledAttributes(attrs);
            int noteColor = ContextCompat.getColor(context, a.getResourceId(0, R.color.text_accent_dark));
            a.recycle();

            String timeString = " " + utils.timeDeltaLongDisplayString(System.currentTimeMillis(), item.timestamp + item.offset).getValue() + " ";
            String displayString = context.getString(R.string.schedalarm_dialog_note1, timeString);
            return SuntimesUtils.createBoldColorSpan(null, displayString, timeString, noteColor);

        } else if (item.getEvent() != null) {
            AlarmEvent.AlarmEventItem eventItem = new AlarmEvent.AlarmEventItem(item.getEvent(), context.getContentResolver());
            String displayString = context.getString(R.string.schedalarm_dialog_note2, eventItem.getTitle());
            return SuntimesUtils.createBoldSpan(null, displayString, eventItem.getTitle());

        } else {
            return "";
        }
    }

    public static CharSequence displayAlarmDate(Context context, AlarmClockItem item) {
        return displayAlarmDate(context, item, false);
    }
    public static CharSequence displayAlarmDate(Context context, AlarmClockItem item, boolean withOffset)
    {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(item.timestamp + (withOffset ? item.offset : 0));

        CharSequence alarmDesc;
        SuntimesUtils.TimeDisplayText timeText = utils.calendarDateDisplayString(context, alarmTime, true);
        if (SuntimesUtils.is24()) {
            alarmDesc = timeText.getValue();

        } else {
            String timeString = timeText.getValue() + " " + timeText.getSuffix();
            alarmDesc = SuntimesUtils.createRelativeSpan(null, timeString, " " + timeText.getSuffix(), 0.40f);
        }
        return alarmDesc;
    }

    public static boolean showAlarmDate(Context context, AlarmClockItem item)
    {
        SolarEvents event = SolarEvents.valueOf(item.getEvent(), null);
        int eventType = event == null ? -1 : event.getType();
        long now = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        long delta = item.timestamp - now;
        boolean isDistant = (delta >= (48 * 60 * 60 * 1000));
        return (eventType == SolarEvents.TYPE_MOONPHASE || eventType == SolarEvents.TYPE_SEASON || isDistant);
    }

    public static CharSequence displayOffset(Context context, AlarmClockItem item)
    {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(item.timestamp);
        int alarmHour = SuntimesUtils.is24() ? alarmTime.get(Calendar.HOUR_OF_DAY) : alarmTime.get(Calendar.HOUR);

        if (item.offset == 0) {
            return context.getResources().getQuantityString(R.plurals.offset_at_plural, alarmHour);

        } else {
            boolean isBefore = (item.offset <= 0);
            String offsetText = utils.timeDeltaLongDisplayString(0, item.offset).getValue();
            String offsetDisplay = context.getResources().getQuantityString((isBefore ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), alarmHour, offsetText);
            return SuntimesUtils.createBoldSpan(null, offsetDisplay, offsetText);
        }
    }

    public static CharSequence displayRepeating(Context context, AlarmClockItem item, boolean isSelected)
    {
        SolarEvents event = SolarEvents.valueOf(item.getEvent(), null);
        int eventType = event == null ? -1 : event.getType();
        boolean noRepeat = item.repeatingDays == null || item.repeatingDays.isEmpty();
        String repeatText = AlarmClockItem.repeatsEveryDay(item.repeatingDays)
                ? context.getString(R.string.alarmOption_repeat_all)
                : noRepeat
                ? context.getString(R.string.alarmOption_repeat_none)
                : AlarmRepeatDialog.getDisplayString(context, item.repeatingDays);
        if (item.repeating && (eventType == SolarEvents.TYPE_MOONPHASE || eventType == SolarEvents.TYPE_SEASON)) {
            repeatText = context.getString(R.string.alarmOption_repeat);
        }
        return (isSelected || !noRepeat ? repeatText : "");
    }

    public static CharSequence displayRingtone(Context context, AlarmClockItem item, boolean isSelected)
    {
        final String noRingtone = context.getString(R.string.alarmOption_ringtone_none);
        return (isSelected ? (item.ringtoneName != null ? item.ringtoneName : noRingtone) : "");
    }

    public static CharSequence displayAction(Context context, AlarmClockItem item, int actionNum)
    {
        String noAction = context.getString(R.string.configLabel_action_item_none);
        String actionID = item.getActionID(actionNum);
        String actionTitle = WidgetActions.loadActionLaunchPref(context, 0, actionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
        String actionDesc = WidgetActions.loadActionLaunchPref(context, 0, actionID, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC);
        String desc = context.getString(R.string.configLabel_action_item_desc, actionDesc);
        String label = context.getString(R.string.configLabel_action_item, actionTitle, desc);

        int[] attrs = { R.attr.text_disabledColor };
        TypedArray a = context.obtainStyledAttributes(attrs);
        int descColor = ContextCompat.getColor(context, a.getResourceId(0, R.color.text_disabled_dark));
        a.recycle();

        SpannableString s = SuntimesUtils.createRelativeSpan(null, label, desc, 0.75f);
        s = SuntimesUtils.createColorSpan(s, label, desc, descColor);
        return ((actionID != null) ? s : noAction);
    }

    public static CharSequence displayEvent(Context context, AlarmClockItem item)
    {
        String eventString = item.getEvent();
        if (eventString != null) {
            AlarmEvent.AlarmEventItem eventItem = new AlarmEvent.AlarmEventItem(eventString, context.getContentResolver());
            return eventItem.getTitle();

        } else if (item.timezone != null) {
            Calendar adjustedTime = Calendar.getInstance(AlarmClockItem.AlarmTimeZone.getTimeZone(item.timezone, item.location));
            adjustedTime.set(Calendar.HOUR_OF_DAY, item.hour);
            adjustedTime.set(Calendar.MINUTE, item.minute);
            return utils.calendarTimeShortDisplayString(context, adjustedTime) + "\n" + AlarmClockItem.AlarmTimeZone.displayString(item.timezone);

        } else {
            Calendar adjustedTime = Calendar.getInstance(AlarmClockItem.AlarmTimeZone.getTimeZone(item.timezone, item.location));
            adjustedTime.set(Calendar.HOUR_OF_DAY, item.hour);
            adjustedTime.set(Calendar.MINUTE, item.minute);
            return utils.calendarTimeShortDisplayString(context, adjustedTime) + "\n" + context.getString(R.string.alarmOption_solarevent_none);
        }
    }


}
