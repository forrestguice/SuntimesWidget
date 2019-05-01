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
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
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
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;

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
    private int onColor, offColor, disabledColor, pressedColor;
    private SuntimesTheme suntimesTheme = null;

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

    public AlarmClockAdapter(Context context, ArrayList<AlarmClockItem> items, SuntimesTheme theme)
    {
        super(context, R.layout.layout_listitem_alarmclock, items);
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
                android.R.attr.textColorPrimary, android.R.attr.textColor, R.attr.text_disabledColor, R.attr.gridItemSelected, R.attr.buttonPressColor, R.attr.alarmColorEnabled};
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
            convertView = inflater.inflate(R.layout.layout_listitem_alarmclock, parent, false);
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

        // label
        view.text.setOnClickListener(new View.OnClickListener()
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

        // event
        view.text2.setOnClickListener(new View.OnClickListener() {
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

        // time
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

        // location
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

        // enabled / disabled
        if (view.switch_enabled != null) {
            view.switch_enabled.setOnCheckedChangeListener(onAlarmEnabledChanged(view, item, isSelected));
        }
        if (view.check_enabled != null) {
            view.check_enabled.setOnCheckedChangeListener(onAlarmEnabledChanged(view, item, isSelected));
        }

        // ringtone
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

        // vibrate
        view.check_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
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

                if (!isSelected) {
                    setSelectedItem(item.rowID);
                }
            }
        });

        // repeating
        view.option_repeat.setOnClickListener(new View.OnClickListener() {
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

        // offset (before / after)
        view.option_offset.setOnClickListener(new View.OnClickListener()
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

        // overflow menu
        view.overflow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                showOverflowMenu(item, v, view.card);
            }
        });
    }

    private void clearListeners( AlarmClockItemView view )
    {
        view.typeButton.setOnClickListener(null);
        view.text.setOnClickListener(null);
        view.text2.setOnClickListener(null);
        view.text_datetime.setOnClickListener(null);
        view.text_location.setOnClickListener(null);
        view.text_ringtone.setOnClickListener(null);
        view.check_vibrate.setOnCheckedChangeListener(null);
        view.option_repeat.setOnClickListener(null);
        view.option_offset.setOnClickListener(null);
        view.overflow.setOnClickListener(null);

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
        final boolean isSelected = (item.rowID == selectedItem);
        view.cardBackdrop.setBackgroundColor( isSelected ? ColorUtils.setAlphaComponent(alarmSelectedColor, 170) : Color.TRANSPARENT );  // 66% alpha

        // enabled / disabled
        if (Build.VERSION.SDK_INT >= 14)
        {
            view.switch_enabled.setChecked(item.enabled);
            view.switch_enabled.setThumbTintList(SuntimesUtils.colorStateList(alarmEnabledColor, offColor, disabledColor, pressedColor));
            view.switch_enabled.setTrackTintList(SuntimesUtils.colorStateList(
                    ColorUtils.setAlphaComponent(alarmEnabledColor, 85), ColorUtils.setAlphaComponent(offColor, 85),
                    ColorUtils.setAlphaComponent(disabledColor, 85), ColorUtils.setAlphaComponent(pressedColor, 85)));  // 33% alpha (85 / 255)
        } else {
            view.check_enabled.setChecked(item.enabled);
            CompoundButtonCompat.setButtonTintList(view.check_enabled, SuntimesUtils.colorStateList(alarmEnabledColor, offColor, disabledColor, pressedColor));
        }

        LayerDrawable alarmEnabledLayers = (LayerDrawable)alarmEnabledBG;
        GradientDrawable alarmEnabledLayers0 = (GradientDrawable)alarmEnabledLayers.getDrawable(0);
        alarmEnabledLayers0.setStroke((int)(3 * context.getResources().getDisplayMetrics().density), alarmEnabledColor);
        if (Build.VERSION.SDK_INT >= 16) {
            view.card.setBackground(item.enabled ? alarmEnabledBG : alarmDisabledBG);
        } else {
            view.card.setBackgroundDrawable(item.enabled ? alarmEnabledBG : alarmDisabledBG);
        }

        // type button
        view.typeButton.setImageDrawable(ContextCompat.getDrawable(context, (item.type == AlarmClockItem.AlarmType.ALARM ? iconAlarm : iconNotification)));
        view.typeButton.setContentDescription(item.type.getDisplayString());

        if (!isSelected && !item.enabled) {
            ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(disabledColor, disabledColor, disabledColor));
        } else if (item.enabled) {
            ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(alarmEnabledColor, disabledColor, pressedColor));
        } else {
            ImageViewCompat.setImageTintList(view.typeButton, SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
        }

        // label
        view.text.setText(getAlarmLabel(context, item));
        if (!isSelected && !item.enabled) {
            view.text.setTextColor(disabledColor);
        } else if (item.enabled) {
            view.text.setTextColor(SuntimesUtils.colorStateList(alarmEnabledColor, alarmEnabledColor, pressedColor));
        } else {
            view.text.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
        }

        // event
        view.text2.setText(getAlarmEvent(context, item));
        if (!isSelected || item.enabled) {
            view.text2.setTextColor(disabledColor);
        } else {
            view.text2.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
        }

        // time
        view.text_datetime.setText(getAlarmTime(context, item));
        if (!isSelected && !item.enabled) {
            view.text_datetime.setTextColor(disabledColor);
        } else if (item.enabled) {
            view.text_datetime.setTextColor(SuntimesUtils.colorStateList(alarmEnabledColor, alarmEnabledColor, pressedColor));
        } else {
            view.text_datetime.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
        }

        // location
        view.text_location.setVisibility(item.event == null ? View.INVISIBLE : View.VISIBLE);
        AlarmDialog.updateLocationLabel(context, view.text_location, item.location);

        if (!isSelected || item.enabled) {
            Drawable[] d = SuntimesUtils.tintCompoundDrawables(view.text_location.getCompoundDrawables(), disabledColor);
            view.text_location.setCompoundDrawables(d[0], d[1], d[2], d[3]);
            view.text_location.setTextColor(disabledColor);
        } else {
            Drawable[] d = SuntimesUtils.tintCompoundDrawables(view.text_location.getCompoundDrawables(), onColor);
            view.text_location.setCompoundDrawables(d[0], d[1], d[2], d[3]);
            view.text_location.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
        }

        // ringtone
        int iconID = item.ringtoneName != null ? iconSoundEnabled : iconSoundDisabled;
        int iconDimen = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20, context.getResources().getDisplayMetrics());
        ImageSpan icon = isSelected || item.enabled ? SuntimesUtils.createImageSpan(context, iconID, iconDimen, iconDimen, item.enabled ? alarmEnabledColor : 0)
                : SuntimesUtils.createImageSpan(context, iconID, iconDimen, iconDimen, disabledColor, PorterDuff.Mode.MULTIPLY);

        final String none = context.getString(R.string.alarmOption_ringtone_none);
        String ringtoneName = isSelected ? (item.ringtoneName != null ? item.ringtoneName : none) : "";

        String ringtoneLabel = context.getString(R.string.alarmOption_ringtone_label, ringtoneName);
        SpannableStringBuilder ringtoneDisplay = SuntimesUtils.createSpan(context, ringtoneLabel, "[icon]", icon);

        view.text_ringtone.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
        view.text_ringtone.setText(ringtoneDisplay);

        // vibrate
        view.check_vibrate.setChecked(item.vibrate);
        view.check_vibrate.setText( isSelected ? context.getString(R.string.alarmOption_vibrate) : "");
        if (item.enabled)
            CompoundButtonCompat.setButtonTintList(view.check_vibrate, SuntimesUtils.colorStateList(alarmEnabledColor, disabledColor, pressedColor));
        else CompoundButtonCompat.setButtonTintList(view.check_vibrate, SuntimesUtils.colorStateList((isSelected ? alarmEnabledColor : disabledColor), disabledColor, pressedColor));

        // repeating
        boolean noRepeat = item.repeatingDays == null || item.repeatingDays.isEmpty();
        String repeatText = AlarmClockItem.repeatsEveryDay(item.repeatingDays)
                ? context.getString(R.string.alarmOption_repeat_all)
                : noRepeat
                ? context.getString(R.string.alarmOption_repeat_none)
                : AlarmRepeatDialog.getDisplayString(context, item.repeatingDays);
        view.option_repeat.setText( isSelected || !noRepeat ? repeatText : "" );

        if (!isSelected || item.enabled) {
            view.option_repeat.setTextColor(disabledColor);
        } else {
            view.option_repeat.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
        }

        // offset (before / after)

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(item.timestamp);
        int alarmHour = SuntimesUtils.is24() ? alarmTime.get(Calendar.HOUR_OF_DAY) : alarmTime.get(Calendar.HOUR);

        if (item.offset == 0)
        {
            String offsetDisplay = context.getResources().getQuantityString(R.plurals.offset_at_plural, alarmHour);
            view.option_offset.setText(offsetDisplay);

        } else {
            boolean isBefore = (item.offset <= 0);
            String offsetText = utils.timeDeltaLongDisplayString(0, item.offset).getValue();
            String offsetDisplay = context.getResources().getQuantityString((isBefore ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), alarmHour, offsetText);
            Spannable offsetSpan = SuntimesUtils.createBoldSpan(null, offsetDisplay, offsetText);
            view.option_offset.setText(offsetSpan);
        }

        if (!isSelected || item.enabled) {
            view.option_offset.setTextColor(disabledColor);
        } else {
            view.option_offset.setTextColor(SuntimesUtils.colorStateList(onColor, disabledColor, pressedColor));
        }

        // overflow menu
        view.overflow.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
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

        if (Build.VERSION.SDK_INT < 11)     // TODO: add support for api10
        {
            MenuItem[] notSupportedMenuItems = new MenuItem[] {     // not supported by api level
                    menu.getMenu().findItem(R.id.setAlarmTime),
                    menu.getMenu().findItem(R.id.setAlarmOffset)
            };
            for (MenuItem menuItem : notSupportedMenuItems) {
                menuItem.setEnabled(false);
            }
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

                    case R.id.setAlarmSound:
                        if (adapterListener != null) {
                            adapterListener.onRequestRingtone(item);
                        }
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
    protected void enableAlarm(final AlarmClockItem item, final AlarmClockItemView itemView, final boolean enabled)
    {
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
                    context.sendBroadcast( enabled ? AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_SCHEDULE, item.getUri())
                                                   : AlarmNotifications.getAlarmIntent(context, AlarmNotifications.ACTION_DISABLE, item.getUri()) );
                    updateView(itemView, item);

                } else Log.e("AlarmClockActivity", "enableAlarm: failed to save state!");
            }
        });
        enableTask.execute(item);
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
                   CharSequence message = context.getString(R.string.deletealarm_toast_success, getAlarmLabel(context, item), getAlarmTime(context, item), getAlarmEvent(context, item));
                   Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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

    /**
     * AlarmClockAdapterListener
     */
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

    /**
     * AlarmClockItemView
     */
    private static class AlarmClockItemView
    {
        public View card;
        public View cardBackdrop;
        public ImageButton typeButton;
        public TextView text;
        public TextView text2;
        public TextView text_datetime;
        public TextView text_location;
        public TextView text_ringtone;
        public CheckBox check_vibrate;
        public TextView option_repeat;
        public TextView option_offset;
        public ImageButton overflow;

        public SwitchCompat switch_enabled;
        public CheckBox check_enabled;

        public AlarmClockItemView(View view)
        {
            card = view.findViewById(R.id.layout_alarmcard);
            cardBackdrop = view.findViewById(R.id.layout_alarmcard0);
            typeButton = (ImageButton) view.findViewById(R.id.type_menu);
            text = (TextView) view.findViewById(android.R.id.text1);
            text2 = (TextView) view.findViewById(android.R.id.text2);
            text_datetime = (TextView) view.findViewById(R.id.text_datetime);
            text_location = (TextView) view.findViewById(R.id.text_location_label);
            text_ringtone = (TextView) view.findViewById(R.id.text_ringtone);
            check_vibrate = (CheckBox) view.findViewById(R.id.check_vibrate);
            option_repeat = (TextView) view.findViewById(R.id.option_repeat);
            option_offset = (TextView) view.findViewById(R.id.option_offset);
            overflow = (ImageButton) view.findViewById(R.id.overflow_menu);

            if (Build.VERSION.SDK_INT >= 14) {
                switch_enabled = (SwitchCompat) view.findViewById(R.id.switch_enabled);        // switch used by api >= 14 (otherwise null)
            } else {
                check_enabled = (CheckBox) view.findViewById(R.id.switch_enabled);              // checkbox used by api < 14 (otherwise null)
            }
        }

    }
}
