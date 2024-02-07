/**
    Copyright (C) 2024 Forrest Guice
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
package com.forrestguice.suntimeswidget.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_ENABLEDONLY;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_SORTORDER;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_DEF_ALARMWIDGET_TYPES;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_ENABLEDONLY;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SHOWICONS;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_SORTORDER;
import static com.forrestguice.suntimeswidget.widgets.AlarmWidgetSettings.PREF_KEY_ALARMWIDGET_TYPES;

public class AlarmWidget0ConfigFragment extends DialogFragment
{
    public AlarmWidget0ConfigFragment()
    {
        super();
        Bundle defaultArgs = new Bundle();
        defaultArgs.putStringArray(PREF_KEY_ALARMWIDGET_TYPES, PREF_DEF_ALARMWIDGET_TYPES);
        defaultArgs.putInt(PREF_KEY_ALARMWIDGET_SORTORDER, PREF_DEF_ALARMWIDGET_SORTORDER);
        defaultArgs.putBoolean(PREF_KEY_ALARMWIDGET_ENABLEDONLY, PREF_DEF_ALARMWIDGET_ENABLEDONLY);
        defaultArgs.putBoolean(PREF_KEY_ALARMWIDGET_SHOWICONS, PREF_DEF_ALARMWIDGET_SHOWICONS);
        setArguments(defaultArgs);
    }

    protected int getLayoutResID() {
        return R.layout.layout_settings_general_more_alarmwidget;
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        super.onCreate(savedState);
        View dialogContent = inflater.inflate(getLayoutResID(), null);
        initViews(getActivity(), dialogContent);
        updateViews(getContext());
        return dialogContent;
    }

    protected CheckBox check_enabledOnly;
    protected CheckBox check_showIcons;
    protected TextView chip_type_alarms, chip_type_notifications;

    protected Spinner spin_sortOrder;
    protected ArrayAdapter<SortOrder> spin_sortOrder_adapter;

    protected void initViews( final Context context, View dialogContent )
    {
        check_enabledOnly = (CheckBox) dialogContent.findViewById(R.id.check_enabledOnly);
        if (check_enabledOnly != null) {
            check_enabledOnly.setOnCheckedChangeListener(onCheckedChangedListener(PREF_KEY_ALARMWIDGET_ENABLEDONLY));
        }

        check_showIcons = (CheckBox) dialogContent.findViewById(R.id.check_showIcons);
        if (check_showIcons != null) {
            check_showIcons.setOnCheckedChangeListener(onCheckedChangedListener(PREF_KEY_ALARMWIDGET_SHOWICONS));
        }

        chip_type_alarms = (TextView) dialogContent.findViewById(R.id.chip_alarmtypes_alarm);
        if (chip_type_alarms != null) {
            chip_type_alarms.setOnClickListener(onTypeChipClick);
        }

        chip_type_notifications = (TextView) dialogContent.findViewById(R.id.chip_alarmtypes_notification);
        if (chip_type_notifications != null) {
            chip_type_notifications.setOnClickListener(onTypeChipClick);
        }

        TextView chip_type_add = (TextView) dialogContent.findViewById(R.id.chip_alarmtypes_add);
        if (chip_type_add != null) {
            chip_type_add.setOnClickListener(onTypeChipClick);
        }

        spin_sortOrder = (Spinner) dialogContent.findViewById(R.id.spin_sortOrder);
        if (spin_sortOrder != null) {
            spin_sortOrder.setOnItemSelectedListener(onSortOrderSelected);
            spin_sortOrder.setAdapter(spin_sortOrder_adapter = createAdapter_sortOrder(context));
        }
    }

    protected ArrayAdapter<SortOrder> createAdapter_sortOrder(Context context)
    {
        SortOrder[] values = new SortOrder[] {new SortOrder(context, AlarmSettings.SORT_BY_ALARMTIME), new SortOrder(context, AlarmSettings.SORT_BY_CREATION)};
        ArrayAdapter<SortOrder> adapter = new ArrayAdapter<SortOrder>(context, R.layout.layout_listitem_oneline, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected AdapterView.OnItemSelectedListener onSortOrderSelected = new AdapterView.OnItemSelectedListener()
    {
        int sortOrder = PREF_DEF_ALARMWIDGET_SORTORDER;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            int order = spin_sortOrder_adapter.getItem(position).getValue();
            if (order != sortOrder) {
                sortOrder = order;
                setAlarmWidgetValue(PREF_KEY_ALARMWIDGET_SORTORDER, sortOrder);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    protected View.OnClickListener onTypeChipClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String[] selectedTypes = getAlarmWidgetStringSet(PREF_KEY_ALARMWIDGET_TYPES, PREF_DEF_ALARMWIDGET_TYPES);
            chooseAlarmTypes(getActivity(), AlarmWidgetSettings.ALL_TYPES, selectedTypes, new ChooseAlarmTypesDialogListener()
            {
                public void onClick(DialogInterface dialog, int which, String[] types, boolean[] checked)
                {
                    ArrayList<String> values = new ArrayList<>();
                    for (int i=0; i<types.length; i++) {
                        if (checked[i]) {
                            values.add(types[i]);
                        }
                    }
                    if (values.size() > 0) {
                        setAlarmWidgetValue(PREF_KEY_ALARMWIDGET_TYPES, values.toArray(new String[0]));
                    }
                }
            });
        }
    });

    /**
     * chooseAlarmTypes
     */
    protected void chooseAlarmTypes(Context context, final String[] types, final String[] selected, final ChooseAlarmTypesDialogListener onClickListener)
    {
        Set<String> selectedTypes = new TreeSet<>(Arrays.asList(selected));
        final ArrayList<Pair<Integer,CharSequence>> items = new ArrayList<>();
        final boolean[] checked = new boolean[types.length];
        for (int i=0; i<types.length; i++) {
            checked[i] = selectedTypes.contains(types[i]);
            items.add(new Pair<Integer, CharSequence>(i, displayStringForTypeName(context, types[i])));
        }

        Collections.sort(items, new Comparator<Pair<Integer,CharSequence>>()
        {
            @Override
            public int compare(Pair<Integer,CharSequence> o1, Pair<Integer,CharSequence> o2)
            {
                if (o1 == null) {
                    return -1;
                } else if (o2 == null) {
                    return 1;
                } else return o1.second.toString().compareTo(o2.second.toString());
            }
        });

        CharSequence[] displayStrings = new CharSequence[items.size()];
        for (int i=0; i<displayStrings.length; i++) {
            displayStrings[i] = items.get(i).second;
        }

        AlertDialog.Builder confirm = new AlertDialog.Builder(context)
                .setMultiChoiceItems(displayStrings, Arrays.copyOf(checked, checked.length), new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        int i = items.get(which).first;
                        checked[i] = isChecked;
                    }
                })
                .setPositiveButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onClickListener.onClick(dialog, AlertDialog.BUTTON_POSITIVE, types, checked);
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_cancel), null);
        confirm.show();
    }

    protected CharSequence displayStringForTypeName(Context context, String name)
    {
        try {
            AlarmClockItem.AlarmType type = AlarmClockItem.AlarmType.valueOf(name);
            return type.getDisplayString();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    public interface ChooseAlarmTypesDialogListener {
        void onClick(DialogInterface dialog, int which, String[] types, boolean[] checked);
    }

    /**
     * updateViews
     */
    protected void updateViews(Context context)
    {
        if (!isAdded()) {
            return;
        }

        if (check_enabledOnly != null) {
            check_enabledOnly.setChecked(getAlarmWidgetBool(PREF_KEY_ALARMWIDGET_ENABLEDONLY, check_enabledOnly.isChecked()));
        }
        if (check_showIcons != null) {
            check_showIcons.setChecked(getAlarmWidgetBool(PREF_KEY_ALARMWIDGET_SHOWICONS, check_showIcons.isChecked()));
        }

        Set<String> filterTypes = new TreeSet<String>(Arrays.asList(getAlarmWidgetStringSet(PREF_KEY_ALARMWIDGET_TYPES, PREF_DEF_ALARMWIDGET_TYPES)));

        if (chip_type_alarms != null) {
            chip_type_alarms.setVisibility(filterTypes.contains(AlarmClockItem.AlarmType.ALARM.name()) ? View.VISIBLE : View.GONE);
        }
        if (chip_type_notifications != null) {
            chip_type_notifications.setVisibility(
                    filterTypes.contains(AlarmClockItem.AlarmType.NOTIFICATION.name())
                    || filterTypes.contains(AlarmClockItem.AlarmType.NOTIFICATION1.name())
                    || filterTypes.contains(AlarmClockItem.AlarmType.NOTIFICATION2.name()) ? View.VISIBLE : View.GONE);
        }

        if (spin_sortOrder != null)
        {
            int p = findPositionForValue(spin_sortOrder_adapter, getAlarmWidgetInt(PREF_KEY_ALARMWIDGET_SORTORDER, PREF_DEF_ALARMWIDGET_SORTORDER));
            if (p >= 0) {
                spin_sortOrder.setSelection(p);
            }
        }
    }

    protected int findPositionForValue(ArrayAdapter<SortOrder> adapter, int value)
    {
        for (int i=0; i<adapter.getCount(); i++) {
            if (adapter.getItem(i).getValue() == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * onCheckedChangedListener
     */
    protected CompoundButton.OnCheckedChangeListener onCheckedChangedListener(final String key)
    {
        return new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                setAlarmWidgetValue(key, isChecked);
                if (listener != null) {
                    listener.onChanged(AlarmWidget0ConfigFragment.this, key);
                }
            }
        };
    }

    /**
     * setAlarmWidgetValue
     */
    public void setAlarmWidgetValue(String key, String value) {
        getArguments().putString(key, value);
        updateViews(getActivity());
    }
    public void setAlarmWidgetValue(String key, boolean value) {
        getArguments().putBoolean(key, value);
        updateViews(getActivity());
    }
    public void setAlarmWidgetValue(String key, int value) {
        getArguments().putInt(key, value);
        updateViews(getActivity());
    }
    public void setAlarmWidgetValue(String key, String[] value) {
        getArguments().putStringArray(key, value);
        updateViews(getActivity());
    }

    /**
     * getAlarmWidgetValue
     */
    public String getAlarmWidgetString(String key, String defaultValue) {
        return getArguments().getString(key, defaultValue);
    }
    public int getAlarmWidgetInt(String key, int defaultValue) {
        return getArguments().getInt(key, defaultValue);
    }
    public boolean getAlarmWidgetBool(String key, boolean defaultValue) {
        return getArguments().getBoolean(key, defaultValue);
    }
    public String[] getAlarmWidgetStringSet(String key, String[] defaultValue) {
        String[] value = getArguments().getStringArray(key);
        return (value != null ? value : defaultValue);
    }

    /**
     * DialogListener
     */
    public interface DialogListener {
        void onChanged(AlarmWidget0ConfigFragment dialog, String key);
    }

    private DialogListener listener = null;
    public void setDialogListener( DialogListener listener ) {
        this.listener = listener;
    }

    /**
     * SortOrder
     */
    public static class SortOrder
    {
        public SortOrder(Context context, int value) {
            setValue(context, value);
        }

        protected void setValue(Context context, int value)
        {
            this.value = value;
            switch (value)
            {
                case AlarmSettings.SORT_BY_CREATION:
                    displayString = context.getString(R.string.configAction_sortAlarms_by_creation);
                    break;

                case AlarmSettings.SORT_BY_ALARMTIME:
                default:
                    displayString = context.getString(R.string.configAction_sortAlarms_by_time);
                    break;
            }
        }

        protected int value = AlarmSettings.SORT_BY_ALARMTIME;
        public int getValue() {
            return value;
        }

        protected String displayString = "";
        public String toString() {
            return displayString;
        }
    }

}