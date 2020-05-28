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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.AlarmDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("Convert2Diamond")
public class AlarmCreateDialog extends DialogFragment {
    public static final String EXTRA_ALARMTYPE = "alarmtype";

    protected TextView text_title;
    protected Spinner spin_type;
    //protected ViewPager pager;
    //protected DialogPagerAdapter adapter;

    public AlarmCreateDialog() {
        super();

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_ALARMTYPE, AlarmClockItem.AlarmType.ALARM);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        //setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState) {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_alarmcreate, parent, false);

        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }

        showByEventFragment();

        return dialogContent;
    }


    protected void showByEventFragment()
    {
        FragmentManager fragments = getChildFragmentManager();
        FragmentTransaction transaction = fragments.beginTransaction();

        AlarmDialog fragment = new AlarmDialog();
        fragment.setDialogShowFrame(false);
        fragment.setType(getAlarmType());
        initEventDialog(getActivity(), fragment, getLocation());
        //fragment.setChoice(getEvent());

        transaction.add(R.id.fragmentContainer1, fragment, "AlarmDialog");
        transaction.commit();
    }


    public void setAlarmType(AlarmClockItem.AlarmType value) {
        getArguments().putSerializable(EXTRA_ALARMTYPE, value);
    }
    public AlarmClockItem.AlarmType getAlarmType() {
        return (AlarmClockItem.AlarmType) getArguments().getSerializable(EXTRA_ALARMTYPE);
    }

    private void initViews(final Context context, View dialogContent)
    {
        text_title = (TextView) dialogContent.findViewById(R.id.dialog_title);

        spin_type = (Spinner) dialogContent.findViewById(R.id.type_spin);
        spin_type.setAdapter(new AlarmTypeAdapter(getContext(), R.layout.layout_listitem_alarmtype));
        spin_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setAlarmType((AlarmClockItem.AlarmType) parent.getItemAtPosition(position));
                updateViews(getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //adapter = new DialogPagerAdapter(context, getChildFragmentManager());
        //pager = (ViewPager) dialogContent.findViewById(R.id.view_pager);
        //pager.setAdapter(adapter);

        TabLayout tabs = (TabLayout) dialogContent.findViewById(R.id.tabLayout);
        //tabs.setupWithViewPager(pager);

        Button btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
        if (btn_cancel != null) {
            btn_cancel.setOnClickListener(onDialogCancelClick);
        }

        ImageButton btn_accept = (ImageButton) dialogContent.findViewById(R.id.dialog_button_accept);
        if (btn_accept != null) {
            btn_accept.setOnClickListener(onDialogAcceptClick);
        }

        Button btn_neutral = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
        if (btn_neutral != null) {
            btn_neutral.setOnClickListener(onDialogNeutralClick);
        }
    }

    private void updateViews(Context context)
    {
        AlarmClockItem.AlarmType alarmType = getAlarmType();

        if (text_title != null) {
            text_title.setText(context.getString(alarmType == AlarmClockItem.AlarmType.NOTIFICATION ? R.string.configAction_addNotification : R.string.configAction_addAlarm));
        }

        if (spin_type != null) {
            spin_type.setSelection(alarmType.ordinal());
        }

        //if (adapter != null) {
        //    adapter.setAlarmType(alarmType);
        //}
    }

    public static class AlarmTypeAdapter extends ArrayAdapter<AlarmClockItem.AlarmType>
    {
        protected int layout;

        public AlarmTypeAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
            layout = resource;
            addAll(AlarmClockItem.AlarmType.values());
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }
        @NonNull @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent);
        }

        @SuppressLint("ResourceType")
        private View createView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(layout, parent, false);
            }

            int[] iconAttr = { R.attr.icActionAlarm, R.attr.icActionNotification };
            TypedArray typedArray = getContext().obtainStyledAttributes(iconAttr);
            int res_iconAlarm = typedArray.getResourceId(0, R.drawable.ic_action_alarms);
            int res_iconNotification = typedArray.getResourceId(1, R.drawable.ic_action_notification);
            typedArray.recycle();

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            AlarmClockItem.AlarmType alarmType = getItem(position);
            if (alarmType != null)
            {
                icon.setImageDrawable(null);
                icon.setBackgroundResource(alarmType == AlarmClockItem.AlarmType.NOTIFICATION ? res_iconNotification : res_iconAlarm);
                text.setText(alarmType.getDisplayString());
            } else {
                icon.setImageDrawable(null);
                icon.setBackgroundResource(0);
                text.setText("");
            }

            return view;
        }
    }

    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void loadSettings(Bundle bundle)
    {
        // TODO
    }

    protected void saveSettings(Bundle bundle)
    {
        // TODO
    }

    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener ) {
        onAccepted = listener;
    }

    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener ) {
        onCanceled = listener;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {

        }
    };

    private View.OnClickListener onDialogNeutralClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // TODO: neutral click
        }
    };

    private View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getShowsDialog())
            {
                getDialog().cancel();

            } else if (onCanceled != null) {
                onCanceled.onClick(getDialog(), 0);
            }
        }
    };

    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
        }
    }

    private View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (onAccepted != null) {
                onAccepted.onClick(getDialog(), 0);
            }
            if (getShowsDialog()) {
                dismiss();
            }
        }
    };

    public int getMode() {
        return 0;// TODO
        //return pager.getCurrentItem();
    }

    public SolarEvents getEvent() {
        return SolarEvents.SUNRISE;// TODO
        //return adapter.getEvent();
    }

    public Location getLocation() {
        return new Location("TODO", "25", "-112", "0");
        //return adapter.getLocation();
    }

    public int getHour() {
        return 0;
        //return adapter.getHour();// TODO
    }

    public int getMinute() {
        return 0;
        //return adapter.getMinute();// TODO
    }

    public long getDate() {
        return System.currentTimeMillis();  // TODO
    }

    public String getTimeZone() {
        return TimeZone.getDefault().getID();  // TODO
        //return adapter.getTimeZone();
    }

    /**
     * DialogPagerAdapter
     */
    public static class DialogPagerAdapter extends FragmentStatePagerAdapter
    {
        private WeakReference<Context> contextRef;
        public DialogPagerAdapter(Context context, FragmentManager fragments) {
            super(fragments);
            this.contextRef = new WeakReference<>(context);
            this.location = WidgetSettings.loadLocationPref(context, 0);
        }

        private Location location;
        public Location getLocation() {
            return location;
        }

        private AlarmClockItem.AlarmType alarmType = AlarmClockItem.AlarmType.ALARM;
        public void setAlarmType(AlarmClockItem.AlarmType value) {
            alarmType = value;
            updateItems();
        }

        private SolarEvents event = SolarEvents.SUNRISE;
        public void setEvent(SolarEvents value) {
            event = value;
            updateItems();
        }
        public SolarEvents getEvent() {
            return ((AlarmDialog)items[0]).getChoice();
        }

        private int hour = 4, minute = 20;
        private String timezone;
        public void setTime(int hour, int minute, String timezone)
        {
            this.hour = hour;
            this.minute = minute;
            this.timezone = timezone;
            updateItems();
        }
        public int getHour() {
            return ((AlarmTimeDialog)items[1]).getHour();
        }
        public int getMinute() {
            return ((AlarmTimeDialog)items[1]).getMinute();
        }
        public String getTimeZone() {
            return ((AlarmTimeDialog)items[1]).getTimeZone();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0: return "By Event";  // TODO
                case 1: return "At Time";   // TODO
                default: return null;
            }
        }

        protected Fragment[] items = new Fragment[2];
        public int getItemId(int position) {
            return items[position].getId();
        }

        private void updateItems()
        {
            if (items[0] != null)
            {
                AlarmDialog page0 = (AlarmDialog) items[0];
                page0.setType(alarmType);
                initEventDialog(contextRef.get(), page0, WidgetSettings.loadLocationPref(contextRef.get(), 0));
                page0.updateViews(contextRef.get());
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position)
            {
                case 0:
                    items[0] = (AlarmDialog) fragment;
                    break;
                case 1:
                    items[1] = (AlarmTimeDialog) fragment;
                    break;
            }
            return fragment;
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    AlarmDialog alarmDialog = new AlarmDialog();
                    alarmDialog.setType(alarmType);
                    alarmDialog.setChoice(event);
                    alarmDialog.setDialogShowFrame(false);
                    initEventDialog(contextRef.get(), alarmDialog, location);
                    return alarmDialog;

                case 1:
                    AlarmTimeDialog timeDialog = new AlarmTimeDialog();
                    timeDialog.setTime(hour, minute);
                    timeDialog.setTimeZone(timezone);
                    timeDialog.set24Hour(SuntimesUtils.is24());
                    return timeDialog;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        private void initEventDialog(Context context, AlarmDialog dialog, Location forLocation)
        {
            SuntimesRiseSetDataset sunData = new SuntimesRiseSetDataset(context, 0);
            SuntimesMoonData moonData = new SuntimesMoonData(context, 0);
            SuntimesEquinoxSolsticeDataset equinoxData = new SuntimesEquinoxSolsticeDataset(context, 0);

            if (forLocation != null) {
                sunData.setLocation(forLocation);
                moonData.setLocation(forLocation);
                equinoxData.setLocation(forLocation);
            }

            sunData.calculateData();
            moonData.calculate();
            equinoxData.calculateData();
            dialog.setData(context, sunData, moonData, equinoxData);
        }

    }

    private void initEventDialog(Context context, AlarmDialog dialog, Location forLocation)
    {
        SuntimesRiseSetDataset sunData = new SuntimesRiseSetDataset(context, 0);
        SuntimesMoonData moonData = new SuntimesMoonData(context, 0);
        SuntimesEquinoxSolsticeDataset equinoxData = new SuntimesEquinoxSolsticeDataset(context, 0);

        if (forLocation != null) {
            sunData.setLocation(forLocation);
            moonData.setLocation(forLocation);
            equinoxData.setLocation(forLocation);
        }

        sunData.calculateData();
        moonData.calculate();
        equinoxData.calculateData();
        dialog.setData(context, sunData, moonData, equinoxData);
    }


}
