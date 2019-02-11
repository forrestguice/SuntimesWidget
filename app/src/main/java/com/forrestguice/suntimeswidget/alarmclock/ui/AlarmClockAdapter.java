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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
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
import android.widget.Toast;

import com.forrestguice.suntimeswidget.AlarmDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmDatabaseAdapter;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * AlarmClockAdapter
 */
@SuppressWarnings("Convert2Diamond")
public class AlarmClockAdapter extends ArrayAdapter<AlarmClockItem>
{
    private static final SuntimesUtils utils = new SuntimesUtils();

    private Context context;
    private long selectedItem;
    private ArrayList<AlarmClockItem> items;
    private int iconAlarm, iconNotification, iconSoundEnabled, iconSoundDisabled;
    private Drawable alarmEnabledBG, alarmDisabledBG;
    private int alarmSelectedColor, alarmEnabledColor;
    private int disabledTextColor, pressedTextColor;

    public AlarmClockAdapter(Context context)
    {
        super(context, R.layout.layout_listitem_alarmclock);
        initAdapter(context);
        this.items = new ArrayList<>();
    }

    public AlarmClockAdapter(Context context, ArrayList<AlarmClockItem> items)
    {
        super(context, R.layout.layout_listitem_alarmclock, items);
        initAdapter(context);
        this.items = items;
    }

    @SuppressLint("ResourceType")
    private void initAdapter(Context context)
    {
        this.context = context;

        int[] attrs = { R.attr.alarmCardEnabled, R.attr.alarmCardDisabled,
                R.attr.icActionAlarm, R.attr.icActionNotification, R.attr.icActionSoundEnabled, R.attr.icActionSoundDisabled,
                R.attr.text_disabledColor, R.attr.gridItemSelected, R.attr.buttonPressColor, R.attr.alarmColorEnabled};
        TypedArray a = context.obtainStyledAttributes(attrs);
        alarmEnabledBG = ContextCompat.getDrawable(context, a.getResourceId(0, R.drawable.card_alarmitem_enabled_dark));
        alarmDisabledBG = ContextCompat.getDrawable(context, a.getResourceId(1, R.drawable.card_alarmitem_disabled_dark));
        iconAlarm = a.getResourceId(2, R.drawable.ic_action_alarms);
        iconNotification = a.getResourceId(3, R.drawable.ic_action_notification);
        iconSoundEnabled = a.getResourceId(4, R.drawable.ic_action_soundenabled);
        iconSoundDisabled = a.getResourceId(5, R.drawable.ic_action_sounddisabled);
        disabledTextColor = ContextCompat.getColor(context, a.getResourceId(6, R.color.text_disabled_dark));
        alarmSelectedColor = ContextCompat.getColor(context, a.getResourceId(7, R.color.grid_selected_dark));
        pressedTextColor = ContextCompat.getColor(context, a.getResourceId(8, R.color.btn_tint_pressed_dark));
        alarmEnabledColor = ContextCompat.getColor(context, a.getResourceId(9, R.color.alarm_enabled_dark));
        a.recycle();
    }

    @Override
    public void add(AlarmClockItem item)
    {
        this.items.add(item);
        super.add(item);
    }

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

    private View itemView(int position, View convertView, @NonNull final ViewGroup parent)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_listitem_alarmclock, parent, false);  // always re-inflate (ignore convertView)
        final AlarmClockItem item = ((position >= 0 && position < items.size()) ? items.get(position) : null);
        if (item == null)
        {
            Log.d(AlarmClockActivity.TAG, "itemView: position " + position + " is null!");
            view.setVisibility(View.GONE);
            return view;
        }
        final boolean isSelected = (item.rowID == selectedItem);

        //ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
        //icon.setImageResource(item.icon);

        final View card = view.findViewById(R.id.layout_alarmcard);
        if (card != null) {
            card.setBackground(item.enabled ? alarmEnabledBG : alarmDisabledBG);
        }

        final View cardBackdrop = view.findViewById(R.id.layout_alarmcard0);
        if (cardBackdrop != null && isSelected) {
            cardBackdrop.setBackgroundColor( alarmSelectedColor );
        }

        // type button
        final ImageButton typeButton = (ImageButton) view.findViewById(R.id.type_menu);
        typeButton.setImageDrawable(ContextCompat.getDrawable(context, (item.type == AlarmClockItem.AlarmType.ALARM ? iconAlarm : iconNotification)));
        typeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isSelected)
                {
                    if (item.enabled)
                        AlarmNotifications.showTimeUntilToast(context, v, item);
                    else showAlarmTypeMenu(item, typeButton, view);

                } else {
                    setSelectedItem(item.rowID);
                }
            }
        });
        if (!isSelected && !item.enabled) {
            ImageViewCompat.setImageTintList(typeButton, SuntimesUtils.colorStateList(disabledTextColor, disabledTextColor, disabledTextColor));
        } else if (item.enabled) {
            ImageViewCompat.setImageTintList(typeButton, SuntimesUtils.colorStateList(alarmEnabledColor, disabledTextColor, pressedTextColor));
        }

        // label
        final TextView text = (TextView) view.findViewById(android.R.id.text1);
        if (text != null)
        {
            text.setText(getAlarmLabel(context, item));
            text.setOnClickListener(new View.OnClickListener()
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
            if (!isSelected && !item.enabled) {
                text.setTextColor(disabledTextColor);
            } else if (item.enabled) {
                text.setTextColor(SuntimesUtils.colorStateList(alarmEnabledColor, alarmEnabledColor, pressedTextColor));
            }
        }

        // event
        final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        if (text2 != null)
        {
            text2.setText(getAlarmEvent(context, item));
            text2.setOnClickListener(new View.OnClickListener() {
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
            if (!isSelected || item.enabled) {
                text2.setTextColor(disabledTextColor);
            }
        }

        // time
        TextView text_datetime = (TextView) view.findViewById(R.id.text_datetime);
        if (text_datetime != null)
        {
            text_datetime.setText(getAlarmTime(context, item));

            if (!isSelected && !item.enabled) {
                text_datetime.setTextColor(disabledTextColor);
            } else if (item.enabled) {
                text_datetime.setTextColor(SuntimesUtils.colorStateList(alarmEnabledColor, alarmEnabledColor, pressedTextColor));
            }

            text_datetime.setOnClickListener(new View.OnClickListener()
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

        // location
        final TextView text_location = (TextView) view.findViewById(R.id.text_location_label);
        if (text_location != null)
        {
            text_location.setVisibility(item.event == null ? View.INVISIBLE : View.VISIBLE);
            AlarmDialog.updateLocationLabel(context, text_location, item.location);
            text_location.setOnClickListener(new View.OnClickListener()
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
            if (!isSelected || item.enabled) {
                Drawable[] d = SuntimesUtils.tintCompoundDrawables(text_location.getCompoundDrawables(), disabledTextColor);
                text_location.setCompoundDrawables(d[0], d[1], d[2], d[3]);
                text_location.setTextColor(disabledTextColor);
            }
        }

        // enabled / disabled
        SwitchCompat switch_enabled = (SwitchCompat) view.findViewById(R.id.switch_enabled);
        if (switch_enabled != null)
        {
            switch_enabled.setChecked(item.enabled);
            switch_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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
                    enableAlarm(item, card, isChecked);
                }
            });
        }

        // ringtone
        final TextView text_ringtone = (TextView) view.findViewById(R.id.text_ringtone);
        if (text_ringtone != null)
        {
            int iconID = item.ringtoneName != null ? iconSoundEnabled : iconSoundDisabled;
            int iconDimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20, context.getResources().getDisplayMetrics());
            ImageSpan icon = isSelected || item.enabled ? SuntimesUtils.createImageSpan(context, iconID, iconDimen, iconDimen, item.enabled ? alarmEnabledColor : 0)
                                                        : SuntimesUtils.createImageSpan(context, iconID, iconDimen, iconDimen, disabledTextColor, PorterDuff.Mode.MULTIPLY);

            final String none = context.getString(R.string.alarmOption_ringtone_none);
            String ringtoneName = isSelected ? (item.ringtoneName != null ? item.ringtoneName : none) : "";

            String ringtoneLabel = context.getString(R.string.alarmOption_ringtone_label, ringtoneName);
            SpannableStringBuilder ringtoneDisplay = SuntimesUtils.createSpan(context, ringtoneLabel, "[icon]", icon);

            text_ringtone.setText(ringtoneDisplay);
            text_ringtone.setOnClickListener(new View.OnClickListener()
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

        // vibrate
        CheckBox check_vibrate = (CheckBox) view.findViewById(R.id.check_vibrate);
        if (check_vibrate != null)
        {
            check_vibrate.setChecked(item.vibrate);
            check_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    item.vibrate = isChecked;
                    item.modified = true;
                    AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, false);
                    task.execute(item);

                    if (isChecked) {
                        Vibrator vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrate != null) {
                            vibrate.vibrate(500);
                        }
                    }
                }
            });
            check_vibrate.setEnabled(isSelected);
            if (!isSelected) {
                check_vibrate.setText("");
            }
        }

        // repeating
        TextView option_repeat = (TextView) view.findViewById(R.id.option_repeat);
        if (option_repeat != null)
        {
            boolean noRepeat = item.repeatingDays == null || item.repeatingDays.isEmpty();

            String repeatText = AlarmClockItem.repeatsEveryDay(item.repeatingDays)
                    ? context.getString(R.string.alarmOption_repeat_all)
                    : noRepeat
                            ? context.getString(R.string.alarmOption_repeat_none)
                            : AlarmRepeatDialog.getDisplayString(context, item.repeatingDays);
            option_repeat.setText( isSelected || !noRepeat ? repeatText : "" );
            if (!isSelected || item.enabled) {
                option_repeat.setTextColor(disabledTextColor);
            }

            option_repeat.setOnClickListener(new View.OnClickListener() {
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
        TextView option_offset = (TextView) view.findViewById(R.id.option_offset);
        if (option_offset != null)
        {
            if (item.offset == 0) {
                option_offset.setText(context.getString(R.string.offset_at));

            } else {
                boolean isBefore = (item.offset <= 0);
                String offsetText = utils.timeDeltaLongDisplayString(0, item.offset).getValue();
                String offsetDisplay = context.getString((isBefore ? R.string.offset_before : R.string.offset_after) , offsetText);
                Spannable offsetSpan = SuntimesUtils.createBoldSpan(null, offsetDisplay, offsetText);
                option_offset.setText(offsetSpan);
            }

            if (!isSelected || item.enabled) {
                option_offset.setTextColor(disabledTextColor);
            }

            option_offset.setOnClickListener(new View.OnClickListener()
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
        ImageButton overflow = (ImageButton) view.findViewById(R.id.overflow_menu);
        if (overflow != null)
        {
            overflow.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showOverflowMenu(item, v, view);
                }
            });
            overflow.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        }

        return view;
    }

    /**
     * @param item associated AlarmClockItem
     * @param buttonView button that triggered menu
     * @param itemView view associated with item
     */
    protected void showOverflowMenu(final AlarmClockItem item, final View buttonView, final View itemView)
    {
        PopupMenu menu = new PopupMenu(context, buttonView);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.alarmcontext, menu.getMenu());

        MenuItem[] restrictedMenuItems = new MenuItem[] {     // only permitted when alarm not already enabled
                menu.getMenu().findItem(R.id.setAlarmType),
                menu.getMenu().findItem(R.id.setAlarmTime),
                menu.getMenu().findItem(R.id.setAlarmOffset),
                menu.getMenu().findItem(R.id.setAlarmEvent),
                menu.getMenu().findItem(R.id.setAlarmLocation),
                menu.getMenu().findItem(R.id.setAlarmRepeat)
        };
        for (MenuItem menuItem : restrictedMenuItems) {
            menuItem.setEnabled(!item.enabled);
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.setAlarmType:
                        showAlarmTypeMenu(item, buttonView, itemView);
                        return true;

                    case R.id.setAlarmLabel:
                        if (adapterListener != null) {
                            adapterListener.onRequestLabel(item);
                        }
                        return true;

                    case R.id.setAlarmTime:
                        if (adapterListener != null) {
                            adapterListener.onRequestTime(item);
                        }
                        return true;

                    case R.id.setAlarmEvent:
                        if (adapterListener != null) {
                            adapterListener.onRequestSolarEvent(item);
                        }
                        return true;

                    case R.id.setAlarmOffset:
                        if (adapterListener != null) {
                            adapterListener.onRequestOffset(item);
                        }
                        return true;

                    case R.id.setAlarmLocation:
                        if (adapterListener != null) {
                            adapterListener.onRequestLocation(item);
                        }
                        return true;

                    case R.id.setAlarmRepeat:
                        if (adapterListener != null) {
                            adapterListener.onRequestRepetition(item);
                        }
                        return true;

                    case R.id.deleteAlarm:
                        confirmDeleteAlarm(item, itemView);
                        return true;

                    default:
                        return false;
                }
            }
        });

        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        menu.show();
    }

    /**
     * showAlarmTypeMenu
     * @param item AlarmClockItem
     * @param buttonView button that triggered menu
     * @param itemView view associated with item
     */
    protected void showAlarmTypeMenu(final AlarmClockItem item, final View buttonView, final View itemView)
    {
        PopupMenu menu = new PopupMenu(context, buttonView);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.alarmtype, menu.getMenu());

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.alarmTypeNotification:
                        return changeAlarmType(item, AlarmClockItem.AlarmType.NOTIFICATION);

                    case R.id.alarmTypeAlarm:
                    default:
                        return changeAlarmType(item, AlarmClockItem.AlarmType.ALARM);
                }
            }
        });

        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        menu.show();
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

                AlarmDatabaseAdapter.AlarmUpdateTask task = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, true);
                task.execute(item);
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
    protected void enableAlarm(final AlarmClockItem item, View itemView, final boolean enabled)
    {
        itemView.setBackground(enabled ? alarmEnabledBG : alarmDisabledBG);

        item.alarmtime = 0;
        item.enabled = enabled;
        item.modified = true;

        AlarmDatabaseAdapter.AlarmUpdateTask enableTask = new AlarmDatabaseAdapter.AlarmUpdateTask(context, false, false);
        enableTask.setTaskListener(new AlarmDatabaseAdapter.AlarmUpdateTask.AlarmClockUpdateTaskListener()
        {
            @Override
            public void onFinished(Boolean result, AlarmClockItem item)
            {
                if (result) {
                    AlarmClockActivity.setAlarmEnabled(context, item, enabled);
                    notifyDataSetChanged();
                } else Log.e("AlarmClockActivity", "enableAlarm: failed to save state!");
            }
        });
        enableTask.execute(item);   // TD
    }

    private static CharSequence getAlarmLabel(Context context, AlarmClockItem item)
    {
        String emptyLabel = ((item.type == AlarmClockItem.AlarmType.ALARM) ? context.getString(R.string.alarmMode_alarm) : context.getString(R.string.alarmMode_notification));
        return (item.label == null || item.label.isEmpty()) ? emptyLabel : item.label;
    }

    private static CharSequence getAlarmEvent(Context context, AlarmClockItem item)
    {
        return (item.event != null ? item.event.getLongDisplayString() : context.getString(R.string.alarmOption_solarevent_none));
    }

    private static CharSequence getAlarmTime(Context context, AlarmClockItem item)
    {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(item.timestamp);

        CharSequence alarmDesc;
        SuntimesUtils.TimeDisplayText timeText = utils.calendarTimeShortDisplayString(context, alarmTime, false);
        if (SuntimesUtils.is24()) {
            alarmDesc = timeText.getValue();

        } else {
            String timeString = timeText.getValue() + " " + timeText.getSuffix();
            alarmDesc = SuntimesUtils.createRelativeSpan(null, timeString, " " + timeText.getSuffix(), 0.40f);
        }
        return alarmDesc;
    }

    /**
     * confirmDeleteAlarm
     * @param item AlarmClockItem
     */
    protected void confirmDeleteAlarm(final AlarmClockItem item, final View itemView)
    {
        String message = context.getString(R.string.deletealarm_dialog_message, getAlarmLabel(context, item), getAlarmTime(context, item), getAlarmEvent(context, item));
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

    protected void onAlarmDeleted(boolean result, final AlarmClockItem item, View itemView)
    {
       if (result)
       {
            final Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
            animation.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation)
                {
                    items.remove(item);
                    notifyDataSetChanged();
                    Toast.makeText(context, context.getString(R.string.deletealarm_toast_success, getAlarmLabel(context, item), getAlarmTime(context, item), getAlarmEvent(context, item)), Toast.LENGTH_LONG).show();
                }
            });
            itemView.startAnimation(animation);
        }
    }

    protected AlarmClockAdapterListener adapterListener;
    public void setAdapterListener(AlarmClockAdapterListener l)
    {
        adapterListener = l;
    }

    public static abstract class AlarmClockAdapterListener
    {
        public void onRequestLabel(AlarmClockItem forItem) {}
        public void onRequestRingtone(AlarmClockItem forItem) {}
        public void onRequestSolarEvent(AlarmClockItem forItem) {}
        public void onRequestLocation(AlarmClockItem forItem) {}
        public void onRequestTime(AlarmClockItem forItem) {}
        public void onRequestOffset(AlarmClockItem forItem) {}
        public void onRequestRepetition(AlarmClockItem forItem) {}
    }
}
