/**
    Copyright (C) 2018-2020 Forrest Guice
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
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.colors.ColorUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmScheduler;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDateDisplay;
import com.forrestguice.suntimeswidget.calculator.settings.display.TimeDeltaDisplay;
import com.forrestguice.suntimeswidget.views.SpanUtils;
import com.forrestguice.support.app.AlertDialog;
import com.forrestguice.support.content.ContextCompat;

import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.support.view.ViewCompat;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.widget.ImageViewCompat;
import com.forrestguice.support.widget.SwitchCompat;
import com.forrestguice.util.ExecutorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * AlarmClockAdapter
 */
public class AlarmItemArrayAdapter extends ArrayAdapter<AlarmClockItem>
{
    private static final TimeDeltaDisplay utils = new TimeDeltaDisplay();

    private Context context;
    private long selectedItem;
    private final ArrayList<AlarmClockItem> items;
    private int iconAlarm, iconNotification, iconSoundEnabled, iconSoundDisabled, iconAction;
    @Nullable
    private Drawable alarmEnabledBG, alarmDisabledBG;
    private int alarmSelectedColor, alarmEnabledColor;
    private int onColor, offColor, disabledColor, pressedColor;
    private SuntimesTheme suntimesTheme = null;

    protected int resource = R.layout.layout_listitem_alarmclock;

    public AlarmItemArrayAdapter(Context context)
    {
        super(context, R.layout.layout_listitem_alarmclock);
        initAdapter(context);
        this.items = new ArrayList<>();
    }

    public AlarmItemArrayAdapter(Context context, int resource)
    {
        super(context, resource);
        this.resource = resource;
        initAdapter(context);
        this.items = new ArrayList<>();
    }

    public AlarmItemArrayAdapter(Context context, int resource, ArrayList<AlarmClockItem> items)
    {
        super(context, resource, items);
        this.resource = resource;
        initAdapter(context);
        this.items = items;
    }

    public AlarmItemArrayAdapter(Context context, int resource, ArrayList<AlarmClockItem> items, SuntimesTheme theme)
    {
        super(context, resource, items);
        this.resource = resource;
        suntimesTheme = theme;
        initAdapter(context);
        this.items = items;
    }

    private void initAdapter(Context context)
    {
        this.context = context;

        themeAdapterViews();
        if (suntimesTheme != null) {
            themeAdapterViews(suntimesTheme);
        }
    }

    @SuppressLint("ResourceType")
    private void themeAdapterViews()
    {
        int[] attrs = { R.attr.alarmCardEnabled, R.attr.alarmCardDisabled,
                R.attr.icActionAlarm, R.attr.icActionNotification, R.attr.icActionSoundEnabled, R.attr.icActionSoundDisabled,
                android.R.attr.textColorPrimary, android.R.attr.textColor, R.attr.text_disabledColor, R.attr.gridItemSelected, R.attr.buttonPressColor, R.attr.alarmColorEnabled,
                R.attr.icActionExtension };
        TypedArray a = context.obtainStyledAttributes(attrs);
        alarmEnabledBG = ContextCompat.getDrawable(context, a.getResourceId(0, R.drawable.card_alarmitem_enabled_dark));
        alarmDisabledBG = ContextCompat.getDrawable(context, a.getResourceId(1, R.drawable.card_alarmitem_disabled_dark));
        iconAlarm = a.getResourceId(2, R.drawable.ic_action_alarms);
        iconNotification = a.getResourceId(3, R.drawable.ic_action_notification);
        iconSoundEnabled = a.getResourceId(4, R.drawable.ic_action_soundenabled);
        iconSoundDisabled = a.getResourceId(5, R.drawable.ic_action_sounddisabled);
        onColor = ContextCompat.getColor(context, a.getResourceId(6, android.R.color.primary_text_dark));
        offColor = ContextCompat.getColor(context, a.getResourceId(7, R.color.grey_600));
        disabledColor = ContextCompat.getColor(context, a.getResourceId(8, R.color.text_disabled_dark));
        alarmSelectedColor = ContextCompat.getColor(context, a.getResourceId(9, R.color.grid_selected_dark));
        pressedColor = ContextCompat.getColor(context, a.getResourceId(10, R.color.sunIcon_color_rising_dark));
        alarmEnabledColor = ContextCompat.getColor(context, a.getResourceId(11, R.color.alarm_enabled_dark));
        iconAction = a.getResourceId(12, R.drawable.ic_action_extension);
        a.recycle();
    }

    public void themeAdapterViews(SuntimesTheme theme)
    {
        suntimesTheme = theme;
        //alarmEnabledBG = ContextCompat.getDrawable(context, a.getResourceId(0, R.drawable.card_alarmitem_enabled_dark));
        //alarmDisabledBG = ContextCompat.getDrawable(context, a.getResourceId(1, R.drawable.card_alarmitem_disabled_dark));

        pressedColor = theme.getActionColor();
        alarmSelectedColor = theme.getAccentColor();
        alarmEnabledColor = theme.getAccentColor();
    }

    @Override
    public void add(AlarmClockItem item)
    {
        this.items.add(item);
        super.add(item);
    }

    @TargetApi(11)
    @Override
    public void addAll (AlarmClockItem... items)
    {
        this.items.addAll(0, Arrays.asList(items));
        super.addAll(items);
    }

    /**
     * Retrieve an AlarmClockItem from the adapter using its rowID.
     * @param rowID the item's rowID
     * @return an AlarmClockItem or null if not found
     */
    public AlarmClockItem findItem( Long rowID )
    {
        if (rowID != null) {
            for (AlarmClockItem item : items) {
                if (item != null && item.rowID == rowID) {
                    return item;
                }
            }
        }
        return null;
    }

    public void setSelectedItem(long rowID)
    {
        selectedItem = rowID;
        notifyDataSetChanged();
    }

    public long getSelectedItem() {
         return selectedItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        return itemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
    {
        return itemView(position, convertView, parent);
    }

    /**
     * itemView
     */
    private View itemView(int position, View convertView, @NonNull final ViewGroup parent)
    {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        AlarmClockItemView itemView = new AlarmClockItemView(convertView);
        clearListeners(itemView);   // always clear (views are recycled and retain prev listeners)

        final AlarmClockItem item = ((position >= 0 && position < items.size()) ? items.get(position) : null);
        if (item != null) {
            updateView(itemView, item);
            setListeners(itemView, item);
        }

        return convertView;
    }

    /**
     * setListeners
     */
    private void setListeners( final AlarmClockItemView view, final AlarmClockItem item )
    {
        final boolean isSelected = (item.rowID == selectedItem);

        // type button
        if (view.typeButton != null)
        {
            view.typeButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isSelected)
                    {
                        if (item.enabled)
                            AlarmNotifications.showTimeUntilToast(context, v, item);
                        else showAlarmTypeMenu(item, view.typeButton, view.card);

                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // label
        if (view.text_label != null)
        {
            view.text_label.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        if (adapterListener != null) {
                            adapterListener.onRequestLabel(item);
                        }
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // event
        if (view.text_event != null)
        {
            view.text_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        if (adapterListener != null && !item.enabled) {
                            adapterListener.onRequestSolarEvent(item);
                        } else {
                            AlarmNotifications.showTimeUntilToast(context, v, item);
                        }
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // time
        if (view.text_datetime != null)
        {
            view.text_datetime.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        if (adapterListener != null && !item.enabled) {
                            adapterListener.onRequestTime(item);
                        } else {
                            AlarmNotifications.showTimeUntilToast(context, v, item);
                        }
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        if (view.text_date != null)
        {
            view.text_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        if (item.enabled) {
                            AlarmNotifications.showTimeUntilToast(context, v, item);
                        }
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // location
        if (view.text_location != null)
        {
            view.text_location.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        if (adapterListener != null && !item.enabled) {
                            adapterListener.onRequestLocation(item);
                        } else {
                            AlarmNotifications.showTimeUntilToast(context, v, item);
                        }
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // enabled / disabled
        if (view.switch_enabled != null) {
            view.switch_enabled.setOnCheckedChangeListener(onAlarmEnabledChanged(view, item, isSelected));
        }
        if (view.check_enabled != null) {
            view.check_enabled.setOnCheckedChangeListener(onAlarmEnabledChanged(view, item, isSelected));
        }

        // ringtone
        if (view.text_ringtone != null)
        {
            view.text_ringtone.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        if (adapterListener != null) {
                            adapterListener.onRequestRingtone(item);
                        }
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // actions
        if (view.text_action0 != null) {
            view.text_action0.setOnClickListener(onRequestActionClickListener(item, 0));
        }
        if (view.text_action1 != null) {
            view.text_action1.setOnClickListener(onRequestActionClickListener(item, 1));
        }

        // vibrate
        if (view.check_vibrate != null)
        {
            view.check_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    item.vibrate = isChecked;
                    item.modified = true;
                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, item, false, false);
                    ExecutorUtils.runTask("AlarmUpdateTask", task, task.getTaskListener());

                    if (isChecked) {
                        Vibrator vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrate != null) {
                            vibrate.vibrate(500);
                        }
                    }

                    if (!isSelected) {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // repeating
        if (view.text_repeat != null)
        {
            view.text_repeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        if (adapterListener != null && !item.enabled) {
                            adapterListener.onRequestRepetition(item);
                        } else {
                            AlarmNotifications.showTimeUntilToast(context, v, item);
                        }
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // offset (before / after)
        if (view.text_offset != null)
        {
            view.text_offset.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (isSelected) {
                        if (adapterListener != null && !item.enabled) {
                            adapterListener.onRequestOffset(item);
                        } else {
                            AlarmNotifications.showTimeUntilToast(context, v, item);
                        }
                    } else {
                        setSelectedItem(item.rowID);
                    }
                }
            });
        }

        // overflow menu
        if (view.overflow != null)
        {
            view.overflow.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    showOverflowMenu(item, v, view.card);
                }
            });
        }
    }

    private View.OnClickListener onRequestActionClickListener(final AlarmClockItem item, final int actionNum)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if ((item.rowID == selectedItem))
                {
                    if (adapterListener != null) {
                        adapterListener.onRequestAction(item, actionNum);
                    }
                } else {
                    setSelectedItem(item.rowID);
                }
            }
        };
    }


    private void clearListeners( AlarmClockItemView view )
    {
        if (view.typeButton != null) {
            view.typeButton.setOnClickListener(null);
        }
        if (view.text_label != null) {
            view.text_label.setOnClickListener(null);
        }
        if (view.text_event != null) {
            view.text_event.setOnClickListener(null);
        }
        if (view.text_datetime != null) {
            view.text_datetime.setOnClickListener(null);
        }
        if (view.text_location != null) {
            view.text_location.setOnClickListener(null);
        }
        if (view.text_ringtone != null) {
            view.text_ringtone.setOnClickListener(null);
        }
        if (view.text_action0 != null) {
            view.text_action0.setOnClickListener(null);
        }
        if (view.text_action1 != null) {
            view.text_action1.setOnClickListener(null);
        }
        if (view.check_vibrate != null) {
            view.check_vibrate.setOnCheckedChangeListener(null);
        }
        if (view.text_repeat != null) {
            view.text_repeat.setOnClickListener(null);
        }
        if (view.text_offset != null) {
            view.text_offset.setOnClickListener(null);
        }
        if (view.overflow != null) {
            view.overflow.setOnClickListener(null);
        }
        if (view.switch_enabled != null) {
            view.switch_enabled.setOnCheckedChangeListener(null);
        }
        if (view.check_enabled != null) {
            view.check_enabled.setOnCheckedChangeListener(null);
        }
    }

    /**
     * updateView
     */
    private void updateView(AlarmClockItemView view, @NonNull final AlarmClockItem item)
    {
        SolarEvents event = SolarEvents.valueOf(item.getEvent(), null);
        int eventType = event == null ? -1 : event.getType();
        final boolean isSelected = (item.rowID == selectedItem);
        view.cardBackdrop.setBackgroundColor( isSelected ? ColorUtils.setAlphaComponent(alarmSelectedColor, 170) : Color.TRANSPARENT );  // 66% alpha

        // enabled / disabled
        if (view.switch_enabled != null)
        {
            if (Build.VERSION.SDK_INT >= 14)
            {
                view.switch_enabled.setChecked(item.enabled);
                view.switch_enabled.setThumbTintList(SuntimesUtils.colorStateList(alarmEnabledColor, offColor, disabledColor, pressedColor));
                view.switch_enabled.setTrackTintList(SuntimesUtils.colorStateList(
                        ColorUtils.setAlphaComponent(alarmEnabledColor, 85), ColorUtils.setAlphaComponent(offColor, 85),
                        ColorUtils.setAlphaComponent(disabledColor, 85), ColorUtils.setAlphaComponent(pressedColor, 85)));  // 33% alpha (85 / 255)
            } else {
                view.check_enabled.setChecked(item.enabled);
                ViewCompat.setButtonTintList(view.check_enabled, SuntimesUtils.colorStateList(alarmEnabledColor, offColor, disabledColor, pressedColor));
            }
        }

        //LayerDrawable alarmEnabledLayers = (LayerDrawable)alarmEnabledBG;
        //GradientDrawable alarmEnabledLayers0 = (GradientDrawable)alarmEnabledLayers.getDrawable(0);
        //alarmEnabledLayers0.setStroke((int)(3 * context.getResources().getDisplayMetrics().density), alarmEnabledColor);
        if (Build.VERSION.SDK_INT >= 16) {
            view.card.setBackground(item.enabled ? alarmEnabledBG : alarmDisabledBG);
        } else {
            view.card.setBackgroundDrawable(item.enabled ? alarmEnabledBG : alarmDisabledBG);
        }

        // type button
        if (view.typeButton != null)
        {
            view.typeButton.setImageDrawable(ContextCompat.getDrawable(context, (item.type == AlarmClockItem.AlarmType.ALARM ? iconAlarm : iconNotification)));
            view.typeButton.setContentDescription(item.type != null ? item.type.getDisplayString() : AlarmClockItem.AlarmType.ALARM.getDisplayString());

            if (!isSelected && !item.enabled) {
                ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(disabledColor, disabledColor, disabledColor));
            } else if (item.enabled) {
                ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(alarmEnabledColor, disabledColor, pressedColor));
            } else {
                ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }
        }

        // label
        if (view.text_label != null)
        {
            view.text_label.setText(AlarmEditViewHolder.displayAlarmLabel(context, item));
            if (!isSelected && !item.enabled) {
                view.text_label.setTextColor(disabledColor);
            } else if (item.enabled) {
                view.text_label.setTextColor(SuntimesUtils.colorStateList(alarmEnabledColor, alarmEnabledColor, pressedColor));
            } else {
                view.text_label.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }
        }

        // event
        if (view.text_event != null)
        {
            view.text_event.setText(AlarmEditViewHolder.displayEvent(context, item));
            if (!isSelected || item.enabled) {
                view.text_event.setTextColor(disabledColor);
            } else {
                view.text_event.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }
        }

        // time
        if (view.text_datetime != null)
        {
            view.text_datetime.setText(AlarmEditViewHolder.displayAlarmTime(context, item));
            if (!isSelected && !item.enabled) {
                view.text_datetime.setTextColor(disabledColor);
            } else if (item.enabled) {
                view.text_datetime.setTextColor(SuntimesUtils.colorStateList(alarmEnabledColor, alarmEnabledColor, pressedColor));
            } else {
                view.text_datetime.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }
        }

        // date
        if (view.text_date != null)
        {
            view.text_date.setText(AlarmEditViewHolder.displayAlarmDate(context, item));
            view.text_date.setVisibility( AlarmEvent.supportsOffsetDays(eventType) ? View.VISIBLE : View.GONE);
            if (!isSelected && !item.enabled) {
                view.text_date.setTextColor(disabledColor);
            } else if (item.enabled){
                view.text_date.setTextColor(SuntimesUtils.colorStateList(alarmEnabledColor, alarmEnabledColor, pressedColor));
            } else {
                view.text_date.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }
        }

        // location
        if (view.text_location != null)
        {
            view.text_location.setVisibility((item.getEvent() == null && item.timezone == null) ? View.INVISIBLE : View.VISIBLE);
            AlarmEventDialog.updateLocationLabel(context, view.text_location, item.location);

            if (!isSelected || item.enabled) {
                Drawable[] d = SuntimesUtils.tintCompoundDrawables(view.text_location.getCompoundDrawables(), disabledColor);
                view.text_location.setCompoundDrawables(d[0], d[1], d[2], d[3]);
                view.text_location.setTextColor(disabledColor);
            } else {
                Drawable[] d = SuntimesUtils.tintCompoundDrawables(view.text_location.getCompoundDrawables(), onColor);
                view.text_location.setCompoundDrawables(d[0], d[1], d[2], d[3]);
                view.text_location.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }
        }

        // ringtone
        if (view.text_ringtone != null)
        {
            view.text_ringtone.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            view.text_ringtone.setText( ringtoneDisplayChip(item, isSelected) );
        }

        // action
        if (view.text_action0 != null)
        {
            view.text_action0.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            view.text_action0.setText( actionDisplayChip(item, 0, isSelected));
            view.text_action0.setVisibility( item.actionID0 != null ? View.VISIBLE : View.GONE );
        }

        if (view.text_action1 != null)
        {
            view.text_action1.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            view.text_action1.setText( actionDisplayChip(item, 1, isSelected));
            view.text_action1.setVisibility( item.actionID1 != null ? View.VISIBLE : View.GONE );
        }

        /*if (view.text_action2 != null)
        {
            view.text_action2.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            view.text_action2.setText( actionDisplayChip(item, 2, isSelected));
            view.text_action2.setVisibility( item.actionID2 != null ? View.VISIBLE : View.GONE );
        }*/

        // vibrate
        if (view.check_vibrate != null)
        {
            view.check_vibrate.setChecked(item.vibrate);
            view.check_vibrate.setText( isSelected ? context.getString(R.string.alarmOption_vibrate) : "");
            if (item.enabled)
                ViewCompat.setButtonTintList(view.check_vibrate, SuntimesUtils.colorStateList(alarmEnabledColor, disabledColor, pressedColor));
            else ViewCompat.setButtonTintList(view.check_vibrate, SuntimesUtils.colorStateList((isSelected ? alarmEnabledColor : disabledColor), disabledColor, pressedColor));
        }

        // repeating
        if (view.text_repeat != null)
        {
            boolean noRepeat = (item.repeatingDays != null && item.repeatingDays.isEmpty());
            String repeatText = AlarmClockItem.repeatsEveryDay(item.repeatingDays)
                    ? context.getString(R.string.alarmOption_repeat_all)
                    : noRepeat
                    ? context.getString(R.string.alarmOption_repeat_none)
                    : AlarmRepeatDialog.getDisplayString(context, item.repeatingDays);

            if (item.repeating && AlarmEvent.supportsRepeating(eventType) == AlarmEventContract.REPEAT_SUPPORT_BASIC) {
                repeatText = context.getString(R.string.alarmOption_repeat);
            }

            view.text_repeat.setText( isSelected || !noRepeat ? repeatText : "" );

            if (!isSelected || item.enabled) {
                view.text_repeat.setTextColor(disabledColor);
            } else {
                view.text_repeat.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }
        }

        // offset (before / after)
        if (view.text_offset != null)
        {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(item.timestamp);
            int alarmHour = TimeDateDisplay.is24() ? alarmTime.get(Calendar.HOUR_OF_DAY) : alarmTime.get(Calendar.HOUR);

            if (item.offset == 0)
            {
                String offsetDisplay = context.getResources().getQuantityString(R.plurals.offset_at_plural, alarmHour);
                view.text_offset.setText(offsetDisplay);

            } else {
                boolean isBefore = (item.offset <= 0);
                String offsetText = utils.timeDeltaLongDisplayString(0, item.offset).getValue();
                String offsetDisplay = context.getResources().getQuantityString((isBefore ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), alarmHour, offsetText);
                Spannable offsetSpan = SpanUtils.createBoldSpan(null, offsetDisplay, offsetText);
                view.text_offset.setText(offsetSpan);
            }

            if (!isSelected || item.enabled) {
                view.text_offset.setTextColor(disabledColor);
            } else {
                view.text_offset.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
            }
        }

        // overflow menu
        if (view.overflow != null)
        {
            view.overflow.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private CharSequence ringtoneDisplayChip(AlarmClockItem item, boolean isSelected)
    {
        int iconDimen = (int) context.getResources().getDimension(R.dimen.chipIcon_size);
        int ringtoneIconID = item.ringtoneName != null ? iconSoundEnabled : iconSoundDisabled;
        ImageSpan ringtonIcon = isSelected || item.enabled
                ? SpanUtils.createImageSpan(context, ringtoneIconID, iconDimen, iconDimen, item.enabled ? alarmEnabledColor : 0)
                : SpanUtils.createImageSpan(context, ringtoneIconID, iconDimen, iconDimen, disabledColor, PorterDuff.Mode.MULTIPLY);
        final String none = context.getString(R.string.alarmOption_ringtone_none);
        String ringtoneName = isSelected ? (item.ringtoneName != null ? item.ringtoneName : none) : "";
        String ringtoneLabel = context.getString(R.string.alarmOption_ringtone_label, ringtoneName);
        return SpanUtils.createSpan(context, ringtoneLabel, "[icon]", ringtonIcon);
    }

    private CharSequence actionDisplayChip(AlarmClockItem item, int actionNum, boolean isSelected)
    {
        int iconDimen = (int) context.getResources().getDimension(R.dimen.chipIcon_size);
        ImageSpan actionIcon = (isSelected || item.enabled)
                ? SpanUtils.createImageSpan(context, iconAction, iconDimen, iconDimen, item.enabled ? alarmEnabledColor : 0)
                : SpanUtils.createImageSpan(context, iconAction, iconDimen, iconDimen, disabledColor, PorterDuff.Mode.MULTIPLY);
        String actionName = item.getActionID(actionNum);
        String actionString = isSelected ? (actionName != null ? actionName : "") : "";
        String actionLabel = context.getString(R.string.alarmOption_action_label, actionString);
        return SpanUtils.createSpan(context, actionLabel, "[icon]", actionIcon);
    }

    /**
     * @param item associated AlarmClockItem
     * @param buttonView button that triggered menu
     * @param itemView view associated with item
     */
    protected void showOverflowMenu(final AlarmClockItem item, final View buttonView, final View itemView)
    {
        PopupMenuCompat.createMenu(context, buttonView, R.menu.alarmcontext, new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
        {
            @Override
            public void onUpdateMenu(Context context, Menu menu)
            {
                MenuItem[] restrictedMenuItems = new MenuItem[] {     // only permitted when alarm not already enabled
                        menu.findItem(R.id.setAlarmType),
                        menu.findItem(R.id.setAlarmTime),
                        menu.findItem(R.id.setAlarmOffset),
                        menu.findItem(R.id.setAlarmEvent),
                        menu.findItem(R.id.setAlarmLocation),
                        menu.findItem(R.id.setAlarmRepeat)
                };
                for (MenuItem menuItem : restrictedMenuItems) {
                    menuItem.setEnabled(!item.enabled);
                }

                if (Build.VERSION.SDK_INT < 11)     // TODO: add support for api10
                {
                    MenuItem[] notSupportedMenuItems = new MenuItem[] {     // not supported by api level
                            menu.findItem(R.id.setAlarmTime),
                            menu.findItem(R.id.setAlarmOffset)
                    };
                    for (MenuItem menuItem : notSupportedMenuItems) {
                        menuItem.setEnabled(false);
                    }
                }
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.setAlarmType) {
                    showAlarmTypeMenu(item, buttonView, itemView);
                    return true;

                } else if (itemId == R.id.setAlarmAction) {
                    if (adapterListener != null) {
                        adapterListener.onRequestAction(item, 0);  // TODO: action1
                    }
                    return true;

                } else if (itemId == R.id.setAlarmSound) {
                    if (adapterListener != null) {
                        adapterListener.onRequestRingtone(item);
                    }
                    return true;

                } else if (itemId == R.id.setAlarmDismissChallenge) {
                    if (adapterListener != null) {
                        adapterListener.onRequestDismissChallenge(item);
                    }
                    return true;

                } else if (itemId == R.id.setAlarmLabel) {
                    if (adapterListener != null) {
                        adapterListener.onRequestLabel(item);
                    }
                    return true;

                } else if (itemId == R.id.setAlarmTime) {
                    if (adapterListener != null) {
                        adapterListener.onRequestTime(item);
                    }
                    return true;

                } else if (itemId == R.id.setAlarmEvent) {
                    if (adapterListener != null) {
                        adapterListener.onRequestSolarEvent(item);
                    }
                    return true;

                } else if (itemId == R.id.setAlarmOffset) {
                    if (adapterListener != null) {
                        adapterListener.onRequestOffset(item);
                    }
                    return true;

                } else if (itemId == R.id.setAlarmLocation) {
                    if (adapterListener != null) {
                        adapterListener.onRequestLocation(item);
                    }
                    return true;

                } else if (itemId == R.id.setAlarmRepeat) {
                    if (adapterListener != null) {
                        adapterListener.onRequestRepetition(item);
                    }
                    return true;

                } else if (itemId == R.id.deleteAlarm) {
                    confirmDeleteAlarm(item, itemView);
                    return true;
                }
                return false;
            }
        })).show();
    }

    /**
     * showAlarmTypeMenu
     * @param item AlarmClockItem
     * @param buttonView button that triggered menu
     * @param itemView view associated with item
     */
    protected void showAlarmTypeMenu(final AlarmClockItem item, final View buttonView, final View itemView)
    {
        PopupMenuCompat.createMenu(context, buttonView, R.menu.alarmtype, new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
        {
            @Override
            public void onUpdateMenu(Context context, Menu menu) {
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.alarmTypeNotification) {
                    return changeAlarmType(item, AlarmClockItem.AlarmType.NOTIFICATION);

                } else if (itemId == R.id.alarmTypeNotification1) {
                    return changeAlarmType(item, AlarmClockItem.AlarmType.NOTIFICATION1);

                } else if (itemId == R.id.alarmTypeNotification2) {
                    return changeAlarmType(item, AlarmClockItem.AlarmType.NOTIFICATION2);
                }
                return changeAlarmType(item, AlarmClockItem.AlarmType.ALARM);
            }
        })).show();
    }

    protected boolean changeAlarmType(AlarmClockItem item, AlarmClockItem.AlarmType type)
    {
        if (item.type != type)
        {
            Log.d(AlarmClockActivity.TAG, "alarmTypeMenu: alarm type is changed: " + type);
            if (item.enabled)
            {
                Log.d(AlarmClockActivity.TAG, "alarmTypeMenu: alarm is enabled (reschedule required?)");
                // item is enabled; disable it or reschedule/reenable
                return false;

            } else {
                Log.d(AlarmClockActivity.TAG, "alarmTypeMenu: alarm is disabled, changing its type..");
                item.type = type;
                item.setState(AlarmState.STATE_NONE);

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, item, false, true);
                ExecutorUtils.runTask("AlarmUpdateTask", task, task.getTaskListener());
                notifyDataSetChanged();
                return true;
            }
        }
        Log.w(AlarmClockActivity.TAG, "alarmTypeMenu: alarm type is unchanged");
        return false;
    }

    /**
     * enableAlarm
     * @param item AlarmClockItem
     * @param enabled enabled/disabled
     */
    protected void enableAlarm(final AlarmClockItem item, final AlarmClockItemView itemView, final boolean enabled)
    {
        item.alarmtime = 0;
        item.enabled = enabled;
        item.modified = true;

        AlarmDatabaseAdapter.AlarmUpdateTask enableTask = new AlarmDatabaseAdapter.AlarmUpdateTask(context, item, false, false);
        enableTask.setTaskListener(new AlarmDatabaseAdapter.AlarmItemTaskListener()
        {
            @Override
            public void onFinished(AlarmDatabaseAdapter.AlarmItemTaskResult result)
            {
                AlarmClockItem item = result.getItem();
                if (result.getResult()) {
                    context.sendBroadcast( enabled ? AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri())
                                                   : AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISABLE, item.getUri()) );
                    if (!enabled) {
                        AlarmScheduler.updateAlarmTime(context, item);
                    }
                    updateView(itemView, item);

                } else Log.e("AlarmClockActivity", "enableAlarm: failed to save state!");
            }
        });
        ExecutorUtils.runTask("AlarmUpdateTask", enableTask, enableTask.getTaskListener());
    }

    private CompoundButton.OnCheckedChangeListener onAlarmEnabledChanged(final AlarmClockItemView view, final AlarmClockItem item, final boolean isSelected)
    {
        return new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (!isSelected) {
                    setSelectedItem(item.rowID);
                }
                if (isChecked) {
                    AlarmNotifications.showTimeUntilToast(context, buttonView, item);
                }
                enableAlarm(item, view, isChecked);
            }
        };
    }

    protected void onRequestDialog(final AlarmClockItem item)
    {
        if (adapterListener != null) {
            adapterListener.onRequestDialog(item);
        }
    }

    /**
     * confirmDeleteAlarm
     * @param item AlarmClockItem
     */
    protected void confirmDeleteAlarm(final AlarmClockItem item, final View itemView)
    {
        String message = context.getString(R.string.deletealarm_dialog_message, AlarmEditViewHolder.displayAlarmLabel(context, item), AlarmEditViewHolder.displayAlarmTime(context, item), AlarmEditViewHolder.displayEvent(context, item));
        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.deletealarm_dialog_title))
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(context.getString(R.string.deletealarm_dialog_ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteAlarm(item, itemView);
                    }
                })
                .setNegativeButton(context.getString(R.string.deletealarm_dialog_cancel), null);
        confirm.show();
    }

    private void deleteAlarm(final AlarmClockItem item, final View itemView) {
        Intent deleteIntent = AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DELETE, item.getUri());
        context.sendBroadcast(deleteIntent);
    }

    protected void onAlarmDeleted(boolean result, final AlarmClockItem item, final View itemView)
    {
       if (result && itemView != null)
       {
           final Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
           animation.setAnimationListener(new Animation.AnimationListener() {
               @Override
               public void onAnimationStart(Animation animation) {
               }

               @Override
               public void onAnimationRepeat(Animation animation) {
               }

               @Override
               public void onAnimationEnd(Animation animation) {
                   items.remove(item);
                   notifyDataSetChanged();
                   CharSequence message = context.getString(R.string.deletealarm_toast_success, AlarmEditViewHolder.displayAlarmLabel(context, item), AlarmEditViewHolder.displayAlarmTime(context, item), AlarmEditViewHolder.displayEvent(context, item));
                   Toast.makeText(context, message, Toast.LENGTH_LONG).show();
               }
           });
           itemView.startAnimation(animation);
       }
    }

    protected AlarmItemAdapterListener adapterListener;
    public void setAdapterListener(AlarmItemAdapterListener l)
    {
        adapterListener = l;
    }

    /**
     * AlarmClockItemView
     */
    private static class AlarmClockItemView
    {
        public View card;
        public View cardBackdrop;
        public ImageButton typeButton;
        public TextView text_label;
        public TextView text_event;
        public TextView text_date;
        public TextView text_datetime;
        public TextView text_location;
        public TextView text_ringtone;
        public TextView text_action0;
        public TextView text_action1;
        public CheckBox check_vibrate;
        public TextView text_repeat;
        public TextView text_offset;
        public ImageButton overflow;

        public SwitchCompat switch_enabled;
        public CheckBox check_enabled;

        public AlarmClockItemView(View view)
        {
            card = view.findViewById(R.id.layout_alarmcard);
            cardBackdrop = view.findViewById(R.id.layout_alarmcard0);
            typeButton = (ImageButton) view.findViewById(R.id.type_menu);
            text_label = (TextView) view.findViewById(android.R.id.text1);
            text_event = (TextView) view.findViewById(R.id.text_event);
            text_date = (TextView) view.findViewById(R.id.text_date);
            text_datetime = (TextView) view.findViewById(R.id.text_datetime);
            text_location = (TextView) view.findViewById(R.id.text_location);
            text_ringtone = (TextView) view.findViewById(R.id.text_ringtone);
            text_action0 = (TextView) view.findViewById(R.id.text_action0);
            text_action1 = (TextView) view.findViewById(R.id.text_action1);
            check_vibrate = (CheckBox) view.findViewById(R.id.check_vibrate);
            text_repeat = (TextView) view.findViewById(R.id.text_repeat);
            text_offset = (TextView) view.findViewById(R.id.text_datetime_offset);
            overflow = (ImageButton) view.findViewById(R.id.overflow_menu);

            if (Build.VERSION.SDK_INT >= 14) {
                switch_enabled = (SwitchCompat) view.findViewById(R.id.switch_enabled);        // switch used by api >= 14 (otherwise null)
            } else {
                check_enabled = (CheckBox) view.findViewById(R.id.switch_enabled);              // checkbox used by api < 14 (otherwise null)
            }
        }

    }
}
