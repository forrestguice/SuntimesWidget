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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.settings.SolarEvents;

import java.util.Calendar;

@SuppressWarnings("Convert2Diamond")
public class AlarmItemViewHolder extends RecyclerView.ViewHolder
{
    public static SuntimesUtils utils = new SuntimesUtils();

    public int position = RecyclerView.NO_POSITION;
    public boolean selected = true;

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

    public AlarmItemViewHolder(View parent)
    {
        super(parent);

        Context context = parent.getContext();
        SuntimesUtils.initDisplayStrings(context);

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
        int[] attrs = { R.attr.icActionAlarm, R.attr.icActionNotification };
        TypedArray a = context.obtainStyledAttributes(attrs);
        res_icAlarm = a.getResourceId(0, R.drawable.ic_action_extension);
        res_icNotification = a.getResourceId(1, R.drawable.ic_action_notification);
        a.recycle();
    }

    public void bindDataToPosition(Context context, AlarmClockItem item, int position)
    {
        this.position = position;

        if (item != null)
        {
            int eventType = item.event == null ? -1 : item.event.getType();

            // alarm type menu
            menu_type.setImageDrawable(ContextCompat.getDrawable(context, (item.type == AlarmClockItem.AlarmType.ALARM ? res_icAlarm : res_icNotification)));
            menu_type.setContentDescription(item.type.getDisplayString());

            /**if (!isSelected && !item.enabled) {
                ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(disabledColor, disabledColor, disabledColor));
            } else if (item.enabled) {
                ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(alarmEnabledColor, disabledColor, pressedColor));
            } else {
                ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }*/

            edit_label.setText(item.getLabel(context));

            //text_offset.setText(item.offset + "");  // TODO
            text_location.setText(item.location.getLabel());


            // repeating days
            boolean noRepeat = item.repeatingDays == null || item.repeatingDays.isEmpty();
            String repeatText = AlarmClockItem.repeatsEveryDay(item.repeatingDays)
                    ? context.getString(R.string.alarmOption_repeat_all)
                    : noRepeat
                    ? context.getString(R.string.alarmOption_repeat_none)
                    : AlarmRepeatDialog.getDisplayString(context, item.repeatingDays);
            if (item.repeating && (eventType == SolarEvents.TYPE_MOONPHASE || eventType == SolarEvents.TYPE_SEASON)) {
                repeatText = context.getString(R.string.alarmOption_repeat);
            }
            text_repeat.setText( selected || !noRepeat ? repeatText : "" );


            text_event.setText(getAlarmEvent(context, item));
            //text_event.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));

            //text_ringtone.setText( ringtoneDisplayChip(item, isSelected) );
            //text_ringtone.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));

            //int ringtoneIconID = item.ringtoneName != null ? iconSoundEnabled : iconSoundDisabled;
            text_ringtone.setText( item.ringtoneName );

            check_vibrate.setChecked(item.vibrate);

            text_action0.setText( item.getActionID(0) );
            text_action0.setVisibility( item.actionID0 != null ? View.VISIBLE : View.GONE );
            //text_action0.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));

            text_action1.setText( item.getActionID(1) );
            text_action1.setVisibility( item.actionID1 != null ? View.VISIBLE : View.GONE );
            //text_action1.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));

        } else {
            edit_label.setText("");
            text_repeat.setText("");
            check_vibrate.setChecked(false);

        }
    }

    public static CharSequence getAlarmEvent(Context context, AlarmClockItem item)
    {
        if (item.event != null)
        {
            return item.event.getLongDisplayString();

        } else if (item.timezone != null) {
            Calendar adjustedTime = Calendar.getInstance(AlarmClockItem.AlarmTimeZone.getTimeZone(item.timezone, item.location));
            adjustedTime.set(Calendar.HOUR_OF_DAY, item.hour);
            adjustedTime.set(Calendar.MINUTE, item.minute);
            return utils.calendarTimeShortDisplayString(context, adjustedTime) + "\n" + AlarmClockItem.AlarmTimeZone.displayString(item.timezone);

        } else {
            return context.getString(R.string.alarmOption_solarevent_none);
        }
    }


}
