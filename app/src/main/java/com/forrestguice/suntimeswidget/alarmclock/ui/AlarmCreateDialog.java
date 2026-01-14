/**
    Copyright (C) 2020-2022 Forrest Guice
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

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.widget.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.suntimeswidget.calculator.settings.LocationMode;
import com.forrestguice.suntimeswidget.getfix.LocationConfigDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.TimeDateDialog;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.widget.TabLayout;
import com.forrestguice.util.android.AndroidResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class AlarmCreateDialog extends BottomSheetDialogBase
{
    public static final String EXTRA_MODE = "mode";             // "by event", "by time", ..
    public static final String EXTRA_ALARMTYPE = "alarmtype";
    public static final String EXTRA_HOUR = "hour";
    public static final String EXTRA_MINUTE = "minute";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_OFFSET = "offset";
    public static final String EXTRA_TIMEZONE = "timezone";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_LOCATION_FROMAPP = "useAppLocation";
    public static final String EXTRA_EVENT = "event";

    public static final long DEF_DATE = -1L;
    public static final int DEF_MODE = 1;
    public static final int DEF_HOUR = 6;
    public static final int DEF_MINUTE = 30;
    public static final String DEF_EVENT = SolarEvents.SUNRISE.name();
    public static final AlarmClockItem.AlarmType DEF_ALARMTYPE = AlarmClockItem.AlarmType.ALARM;

    public static final String EXTRA_PREVIEW_OFFSET = "previewOffset";
    public static final String EXTRA_BUTTON_ALARMLIST = "showAlarmListButton";
    public static final String EXTRA_BUTTON_DATESELECT = "showDateSelectButton";
    public static final String EXTRA_ALLOW_SELECT_TYPE = "allowSelectType";
    public static final String EXTRA_BUTTON_TZSLECT = "showTimeZoneButton";
    public static final String EXTRA_LABEL_OVERRIDE = "overrideLabel";
    public static final String EXTRA_PREVIEW_TIME = "previewTime";
    public static final String EXTRA_SHOW_TABS = "showTabs";

    public static final String DIALOG_EVENT = "AlarmEventDialog";
    public static final String DIALOG_TIME = "AlarmTimeDialog";
    public static final String DIALOG_LOCATION = "locationDialog";
    public static final String DIALOG_DATE = "dateDialog";

    public static final String PREFS_ALARMCREATE = "com.forrestguice.suntimeswidget.alarmcreate";

    protected TabLayout tabs;
    protected TextView text_title, text_offset, text_date, text_note;
    protected TextSwitcher text_time;
    protected ImageView icon_offset;
    protected Spinner spin_type;
    protected ImageButton btn_alarms;
    protected ImageButton btn_accept;
    protected SuntimesUtils utils = new SuntimesUtils();

    public AlarmCreateDialog() {
        super();

        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, DEF_MODE);
        args.putBoolean(EXTRA_PREVIEW_OFFSET, false);
        args.putBoolean(EXTRA_BUTTON_ALARMLIST, false);

        args.putLong(EXTRA_DATE, DEF_DATE);
        args.putInt(EXTRA_HOUR, DEF_HOUR);
        args.putInt(EXTRA_MINUTE, DEF_MINUTE);
        args.putLong(EXTRA_OFFSET, 0);
        args.putString(EXTRA_TIMEZONE, null);
        args.putString(EXTRA_EVENT, DEF_EVENT);
        args.putSerializable(EXTRA_ALARMTYPE, DEF_ALARMTYPE);

        setArguments(args);
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        Context context = requireContext();
        Bundle args = getArgs();
        if (getLocation() == null) {
            args.putSerializable(EXTRA_LOCATION, WidgetSettings.loadLocationPref(context, 0));
        }

        SuntimesUtils.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(AndroidResources.wrap(context));
        AlarmClockItem.AlarmType.initDisplayStrings(context);
        AlarmClockItem.AlarmTimeZone.initDisplayStrings(context);

        //setStyle(DialogBase.STYLE_NO_FRAME, R.style.AppTheme);
        super.onCreate(savedState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        Context context = requireContext();
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(context, AppSettings.loadTheme(context));
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_alarmcreate, parent, false);

        initViews(context, dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }

        TabLayout.Tab tab = tabs.getTabAt(getDialogMode());
        if (tab != null) {
            tab.select();
        }
        showFragmentForMode(context, tab != null ? tab.getPosition() : 0);
        updateViews(context);

        return dialogContent;
    }

    private void showFragmentForMode(@NonNull Context context, int mode)
    {
        switch (mode)
        {
            case 1:
                showByTimeFragment(context);
                break;

            case 0:
            default:
                showByEventFragment(context);
                break;
        }
    }

    protected void showByEventFragment(@NonNull Context context)
    {
        AlarmEventDialog fragment = new AlarmEventDialog();
        fragment.setDialogShowFrame(false);
        fragment.setDialogShowDesc(false);
        fragment.setType(getAlarmType());
        initEventDialog(context, fragment, getLocation());
        fragment.setDialogListener(new AlarmEventDialog.DialogListener()
        {
            @Override
            public void onChanged(AlarmEventDialog dialog)
            {
                if (Math.abs(getOffset()) >= (1000 * 60 * 60 * 24)) {    // clear multi-day offsets
                    getArgs().putLong(EXTRA_OFFSET, 0);
                }
                getArgs().putString(EXTRA_EVENT, dialog.getChoice());
                Log.d("DEBUG", "AlarmCreateDialog: onChanged: " + dialog.getChoice());
                getArgs().putSerializable(EXTRA_LOCATION, dialog.getLocation());
                if (getContext() != null) {
                    updateViews(getContext());
                }
            }

            @Override
            public void onAccepted(AlarmEventDialog dialog) {}

            @Override
            public void onCanceled(AlarmEventDialog dialog) {}

            @Override
            public void onLocationClick(AlarmEventDialog dialog, View v) {
                if (getContext() != null) {
                    showLocationMenu(getContext(), v);
                }
            }
        });
        fragment.setChoice(getEvent());

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer1, fragment, DIALOG_EVENT)
                .commit();
    }

    protected void showLocationMenu(final Context context, View v)
    {
        PopupMenuCompat.createMenu(context, v, R.menu.alarmlocation, new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
        {
            @Override
            public void onUpdateMenu(Context context, Menu menu)
            {
                MenuItem menuItem_locationFromApp = menu.findItem(R.id.action_location_fromApp);
                if (menuItem_locationFromApp != null) {
                    menuItem_locationFromApp.setChecked(useAppLocation());
                }
                MenuItem menuItem_location = menu.findItem(R.id.action_location_set);
                if (menuItem_location != null) {
                    menuItem_location.setEnabled(!useAppLocation());
                }
            }

            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
                Context context = getContext();
                if (context == null) {
                    return false;
                }

                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_location_fromApp) {
                    setUseAppLocation(context, !menuItem.isChecked());
                    updateViews(context);
                    return true;

                } else if (itemId == R.id.action_location_set) {
                    showLocationDialog(context);
                    return true;
                }
                return false;
            }
        })).show();
    }

    protected void showLocationDialog(Context context)
    {
        final LocationConfigDialog dialog = new LocationConfigDialog();
        dialog.setHideTitle(true);
        dialog.setHideMode(true);
        dialog.setLocation(context, getLocation());
        dialog.setDialogListener(onLocationChanged);
        dialog.show(getChildFragmentManager(), DIALOG_LOCATION);
    }
    private final LocationConfigDialog.LocationConfigDialogListener onLocationChanged = new LocationConfigDialog.LocationConfigDialogListener()
    {
        @Override
        public boolean saveSettings(Context context, LocationMode locationMode, Location location)
        {
            LocationConfigDialog dialog = (LocationConfigDialog) getChildFragmentManager().findFragmentByTag(DIALOG_LOCATION);
            if (dialog != null)
            {
                setUseAppLocation(context, false);
                setEvent(getEvent(), location);
                updateViews(context);
                return true;
            }
            return false;
        }
    };

    protected void showDateDialog(Context context)
    {
        final AlarmTimeDateDialog dialog = new AlarmTimeDateDialog();
        dialog.setTimezone(AlarmClockItem.AlarmTimeZone.getTimeZone(getTimeZone(), getLocation()));
        dialog.setOnAcceptedListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog0, int which)
            {
                Calendar now = Calendar.getInstance(dialog.getTimeZone());
                Calendar then = dialog.getDateInfo().getCalendar(dialog.getTimeZone(), getHour(), getMinute());
                Calendar then1 = Calendar.getInstance(dialog.getTimeZone());
                then1.setTimeInMillis(then.getTimeInMillis());
                then1.set(Calendar.HOUR_OF_DAY, 0);
                then1.set(Calendar.MINUTE, 0);
                then1.set(Calendar.SECOND, 0);
                setDate(now.getTimeInMillis() >= then1.getTimeInMillis() ? -1L : then.getTimeInMillis());
                if (getContext() != null) {
                    updateViews(getContext());
                }
            }
        });
        dialog.show(getChildFragmentManager(), DIALOG_DATE);
    }
    public static class AlarmTimeDateDialog extends TimeDateDialog
    {
        @Override
        protected void initViews(Context context, View dialogContent)
        {
            super.initViews(context, dialogContent);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1);
            if (Build.VERSION.SDK_INT >= 11) {
                picker.setMinDate(calendar.getTimeInMillis());
            }

            Button btn_neutral = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
            if (btn_neutral != null)
            {
                btn_neutral.setText(context.getString(R.string.configAction_clearDate));
                btn_neutral.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        AlarmTimeDateDialog.this.init(Calendar.getInstance(timezone));
                        AlarmTimeDateDialog.this.onDialogAcceptClick.onClick(v);
                    }
                });
            }
        }

        @Override
        protected void loadSettings(Context context) { /* EMPTY */ }
        @Override
        protected void saveSettings(Context context) { /* EMPTY */ }
    }

    protected void showByTimeFragment(@NonNull Context context)
    {
        AlarmTimeDialog fragment = new AlarmTimeDialog();
        fragment.setDate(getDate());
        fragment.setShowDateButton(showDateSelectButton());
        fragment.setShowTimeZoneSelect(showTimeZoneSelectButton());
        fragment.setTime(getHour(), getMinute());
        fragment.setTimeZone(getTimeZone());
        fragment.setLocation(getLocation());
        fragment.set24Hour(SuntimesUtils.is24());
        fragment.setDialogListener(new AlarmTimeDialog.DialogListener()
        {
            @Override
            public void onChanged(AlarmTimeDialog dialog)
            {
                getArgs().putLong(EXTRA_DATE, dialog.getDate());
                getArgs().putInt(EXTRA_HOUR, dialog.getHour());
                getArgs().putInt(EXTRA_MINUTE, dialog.getMinute());
                getArgs().putString(EXTRA_TIMEZONE, dialog.getTimeZone());
                if (getContext() != null) {
                    updateViews(getContext());
                }
            }

            @Override
            public void onLocationClick(AlarmTimeDialog dialog) {
                if (getContext() != null) {
                    showLocationDialog(getContext());
                }
            }

            @Override
            public void onDateClick(AlarmTimeDialog dialog) {
                if (getContext() != null) {
                    showDateDialog(getContext());
                }
            }

            @Override
            public void onAccepted(AlarmTimeDialog dialog) {
                onChanged(dialog);
            }

            @Override
            public void onCanceled(AlarmTimeDialog dialog) {}
        });

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer1, fragment, DIALOG_TIME)
                .commit();
    }

    private void initViews(final Context context, View dialogContent)
    {
        text_title = (TextView) dialogContent.findViewById(R.id.dialog_title);
        text_time = (TextSwitcher) dialogContent.findViewById(R.id.text_datetime);
        text_offset = (TextView) dialogContent.findViewById(R.id.text_datetime_offset);
        icon_offset = (ImageView) dialogContent.findViewById(R.id.icon_datetime_offset);
        text_date = (TextView) dialogContent.findViewById(R.id.text_date);
        text_note = (TextView) dialogContent.findViewById(R.id.text_note);

        spin_type = (Spinner) dialogContent.findViewById(R.id.type_spin);
        AlarmTypeAdapter adapter = new AlarmTypeAdapter(context, R.layout.layout_listitem_alarmtype);
        adapter.setLabels(getLabelOverride());
        spin_type.setAdapter(adapter);

        tabs = (TabLayout) dialogContent.findViewById(R.id.tabLayout);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                getArgs().putInt(EXTRA_MODE, tab.getPosition());
                if (getContext() != null) {
                    showFragmentForMode(getContext(), tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        Button btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
        if (btn_cancel != null) {
            TooltipCompat.setTooltipText(btn_cancel, btn_cancel.getContentDescription());
            btn_cancel.setOnClickListener(onDialogCancelClick);
        }

        btn_accept = (ImageButton) dialogContent.findViewById(R.id.dialog_button_accept);
        if (btn_accept != null) {
            TooltipCompat.setTooltipText(btn_accept, btn_accept.getContentDescription());
            btn_accept.setOnClickListener(onDialogAcceptClick);
        }

        Button btn_neutral = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
        if (btn_neutral != null) {
            btn_neutral.setOnClickListener(onDialogNeutralClick);
        }

        View layout_time = dialogContent.findViewById(R.id.layout_datetime);
        if (layout_time != null) {
            layout_time.setOnClickListener(onDialogBottomBarClick);
        }

        btn_alarms = (ImageButton) dialogContent.findViewById(R.id.dialog_button_alarms);
        if (btn_alarms != null) {
            TooltipCompat.setTooltipText(btn_alarms, btn_alarms.getContentDescription());
            btn_alarms.setOnClickListener(onDialogNeutralClick);
        }
    }

    private final AdapterView.OnItemSelectedListener onTypeSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //Log.d("DEBUG", "onItemSelected: " + position);
            setAlarmType((AlarmClockItem.AlarmType) parent.getItemAtPosition(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void updateViews(@NonNull Context context)
    {
        //noinspection ConstantConditions
        if (context == null || !isAdded()) {
            return;
        }
        detachListeners();

        tabs.setVisibility(showTabs() ? View.VISIBLE : View.GONE);
        //if (!allowPickEvent() && tabs.getTabCount() > 1) {
            //tabs.removeTabAt(0);
        //}

        if (btn_alarms != null) {
            btn_alarms.setVisibility(showAlarmListButton() ? View.VISIBLE : View.GONE);
        }

        AlarmClockItem.AlarmType alarmType = getAlarmType();
        AlarmClockItem item = createAlarm(context, AlarmCreateDialog.this, alarmType);
        item.offset = getOffset();
        boolean isSchedulable = AlarmNotifications.updateAlarmTime(context, item);

        boolean showPreview = showTimePreview();

        if (text_title != null) {
            text_title.setText(context.getString(alarmType == AlarmClockItem.AlarmType.ALARM ? R.string.configAction_addAlarm : R.string.configAction_addNotification));
        }
        if (spin_type != null) {
            spin_type.setEnabled(allowSelectType());
            spin_type.setSelection(alarmType.ordinal(), false);
        }

        if (text_offset != null) {
            text_offset.setText(isSchedulable ? AlarmEditViewHolder.displayOffset(context, item) : "");
            text_offset.setVisibility(showPreview ? View.VISIBLE : View.GONE);
        }
        if (text_time != null) {
            text_time.setText(isSchedulable ? AlarmEditViewHolder.displayAlarmTime(context, item, previewOffset()) : "");
            text_time.setVisibility(showPreview ? View.VISIBLE : View.GONE);
        }
        if (text_date != null)
        {
            text_date.setText(isSchedulable ? AlarmEditViewHolder.displayAlarmDate(context, item, previewOffset()) : "");
            text_date.setVisibility(showTimePreview() && isSchedulable && AlarmEditViewHolder.showAlarmDate(context, item) ? View.VISIBLE : View.GONE);
        }
        if (text_note != null) {    // TODO: periodic update
            text_note.setText(AlarmEditViewHolder.displayAlarmNote(context, item, isSchedulable));
        }

        attachListeners();
    }

    protected void attachListeners()
    {
        if (spin_type != null) {
            spin_type.setOnItemSelectedListener(onTypeSelected);
        }
    }

    protected void detachListeners()
    {
        if (spin_type != null) {
            spin_type.setOnItemSelectedListener(null);
        }
    }

    public static class AlarmTypeAdapter extends ArrayAdapter<AlarmClockItem.AlarmType>
    {
        protected int layout;

        public AlarmTypeAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
            layout = resource;

            if (Build.VERSION.SDK_INT >= 11) {
                addAll(AlarmClockItem.AlarmType.ALARM, AlarmClockItem.AlarmType.NOTIFICATION, AlarmClockItem.AlarmType.NOTIFICATION1);
            } else {
                for (AlarmClockItem.AlarmType type : AlarmClockItem.AlarmType.values()) {
                    add(type);
                }
            }
        }

        public String getLabel(int position)
        {
            if (labels != null && labels.size() > 0) {
                if (position >= 0 && position < getCount()) {
                    return labels.get(position);
                }
            }
            AlarmClockItem.AlarmType alarmType = getItem(position);
            return (alarmType != null ? alarmType.getDisplayString() : "");
        }
        public void setLabels(@Nullable String label)
        {
            if (label != null)
            {
                labels = new ArrayList<>(getCount());
                for (int i=0; i<getCount(); i++) {
                    this.labels.add(label);
                }
            } else {
                labels = null;
            }
        }
        public void setLabels(String[] labels) {
            this.labels = new ArrayList<>(Arrays.asList(labels));
        }
        @Nullable
        protected ArrayList<String> labels = null;

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
            Context context = getContext();
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(layout, parent, false);
            }

            int[] iconAttr = { R.attr.icActionAlarm, R.attr.icActionNotification, R.attr.icActionNotification1, R.attr.icActionNotification2 };
            TypedArray typedArray = context.obtainStyledAttributes(iconAttr);
            int res_iconAlarm = typedArray.getResourceId(0, R.drawable.ic_action_alarms);
            int res_iconNotification = typedArray.getResourceId(1, R.drawable.ic_action_notification);
            int res_iconNotification1 = typedArray.getResourceId(2, R.drawable.ic_action_notification1);
            int res_iconNotification2 = typedArray.getResourceId(3, R.drawable.ic_action_notification2);
            typedArray.recycle();

            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            AlarmClockItem.AlarmType alarmType = getItem(position);
            if (alarmType != null)
            {
                icon.setImageDrawable(null);
                int backgroundResource;
                switch (alarmType) {
                    case NOTIFICATION: backgroundResource = res_iconNotification; break;
                    case NOTIFICATION1: backgroundResource = res_iconNotification1; break;
                    case NOTIFICATION2: backgroundResource = res_iconNotification2; break;
                    case ALARM: default: backgroundResource = res_iconAlarm; break;
                }
                icon.setBackgroundResource(backgroundResource);
                text.setText(getLabel(position));
            } else {
                icon.setImageDrawable(null);
                icon.setBackgroundResource(0);
                text.setText("");
            }

            return view;
        }
    }

    @SuppressWarnings({"RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void loadSettings(Bundle bundle) {}

    public void loadSettings(Context context)
    {
        loadSettings(context.getSharedPreferences(PREFS_ALARMCREATE, 0));
    }
    public void loadSettings(SharedPreferences prefs)
    {
        Bundle args = getArgs();
        args.putInt(EXTRA_MODE, prefs.getInt(EXTRA_MODE, getDialogMode()));
        args.putInt(EXTRA_HOUR, prefs.getInt(EXTRA_HOUR, getHour()));
        args.putInt(EXTRA_MINUTE, prefs.getInt(EXTRA_MINUTE, getMinute()));
        args.putString(EXTRA_TIMEZONE, prefs.getString(EXTRA_TIMEZONE, getTimeZone()));
        args.putString(EXTRA_EVENT, prefs.getString(EXTRA_EVENT, DEF_EVENT));
        args.putSerializable(EXTRA_ALARMTYPE, AlarmClockItem.AlarmType.valueOf(prefs.getString(EXTRA_ALARMTYPE, AlarmClockItem.AlarmType.ALARM.name()), DEF_ALARMTYPE));
        setUseAppLocation(getContext(), prefs.getBoolean(EXTRA_LOCATION_FROMAPP, true));

        if (isAdded() && getContext() != null)
        {
            AlarmEventDialog fragment0 = (AlarmEventDialog) getChildFragmentManager().findFragmentByTag(DIALOG_EVENT);
            if (fragment0 != null) {
                initEventDialog(getContext(), fragment0, getLocation());
                fragment0.setChoice(getEvent());
                fragment0.setType(getAlarmType());
            }

            AlarmTimeDialog fragment1 = (AlarmTimeDialog) getChildFragmentManager().findFragmentByTag(DIALOG_TIME);
            if (fragment1 != null) {
                fragment1.setLocation(getLocation());
                fragment1.setTime(getHour(), getMinute());
                fragment1.setTimeZone(getTimeZone());
                fragment1.updateViews(getContext());
            }

            updateViews(getContext());
        }
    }

    protected void saveSettings(Bundle bundle) {}

    public void saveSettings(@Nullable Context context) {
        if (context != null) {
            saveSettings(context.getSharedPreferences(PREFS_ALARMCREATE, 0));
        }
    }
    public void saveSettings(SharedPreferences prefs)
    {
        SharedPreferences.Editor out = prefs.edit();
        out.putInt(EXTRA_MODE, getDialogMode());
        out.putInt(EXTRA_HOUR, getHour());
        out.putInt(EXTRA_MINUTE, getMinute());
        out.putString(EXTRA_TIMEZONE, getTimeZone());
        out.putString(EXTRA_EVENT, getEvent());
        out.putString(EXTRA_ALARMTYPE, getAlarmType().name());
        out.putBoolean(EXTRA_LOCATION_FROMAPP, useAppLocation());
        out.apply();
    }

    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener ) {
        onAccepted = listener;
    }

    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener ) {
        onCanceled = listener;
    }

    private DialogInterface.OnClickListener onNeutral = null;
    public void setOnNeutralListener( DialogInterface.OnClickListener listener) {
        onNeutral = listener;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        LocationConfigDialog locationDialog = (LocationConfigDialog) getChildFragmentManager().findFragmentByTag(DIALOG_LOCATION);
        if (locationDialog != null) {
            locationDialog.setDialogListener(onLocationChanged);
        }
    }

    protected int getPeekHeight() {
        return (int)getResources().getDimension(R.dimen.alarmcreate_bottomsheet_peek);
    }

    private final DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            expandSheet(getDialog(), AUTO_EXPAND_DELAY);

            if (AppSettings.isTelevision(getContext()))
            {
                if (spin_type != null && spin_type.isEnabled() && spin_type.getVisibility() == View.VISIBLE) {
                    spin_type.requestFocus();

                } else if (tabs != null && tabs.isEnabled() && tabs.getVisibility() == View.VISIBLE) {
                    tabs.requestFocus();

                } else if (btn_accept != null && btn_accept.isEnabled() && btn_accept.getVisibility() == View.VISIBLE) {
                    btn_accept.requestFocus();
                }
            }
        }
    };
    public static final int AUTO_EXPAND_DELAY = 500;

    private final View.OnClickListener onDialogNeutralClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (onNeutral != null) {
                onNeutral.onClick(getDialog(), 0);
            }
        }
    });

    private final View.OnClickListener onDialogCancelClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getShowsDialog())
            {
                if (getDialog() != null) {
                    getDialog().cancel();
                }

            } else if (onCanceled != null) {
                onCanceled.onClick(getDialog(), 0);
            }
        }
    });

    @Override
    public void onCancel(@NonNull DialogInterface dialog)
    {
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog)
    {
        super.onDismiss(dialog);
        saveSettings(getContext());
    }

    private final View.OnClickListener onDialogAcceptClick = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
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
    });

    private final View.OnClickListener onDialogBottomBarClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setPreviewOffset(!previewOffset());
            animatePreviewOffset(AlarmCreateDialog.this, previewOffset());
        }
    };

    protected void animatePreviewOffset(final AlarmCreateDialog dialog, final boolean enable)
    {
        Context context = (dialog != null ? dialog.getContext() : null);
        if (context == null || !isAdded()) {
            return;
        }

        AlarmClockItem item = createAlarm(context, dialog, getAlarmType());
        item.offset = getOffset();
        boolean isSchedulable = AlarmNotifications.updateAlarmTime(context, item);

        if (text_time != null) {
            text_time.setText(isSchedulable ? AlarmEditViewHolder.displayAlarmTime(context, item, enable) : "");
        }
        if (text_date != null) {
            text_date.setText(isSchedulable ? AlarmEditViewHolder.displayAlarmDate(context, item, enable): "");
        }

        if (Build.VERSION.SDK_INT >= 14)
        {
            if (!enable && text_offset != null) {
                text_offset.setAlpha(0.0f);
                text_offset.setVisibility(View.VISIBLE);
            }
            if (icon_offset != null) {
                icon_offset.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
            }

            if (text_offset != null)
            {
                text_offset.animate().translationY((enable ? 2 * text_offset.getHeight() : 0))
                        .alpha(enable ? 0.0f : 1.0f).setListener(new Animator.AnimatorListener() {
                    public void onAnimationCancel(Animator animation) {}
                    public void onAnimationRepeat(Animator animation) {}
                    public void onAnimationStart(Animator animation) {}
                    public void onAnimationEnd(Animator animation) {
                        onAnimatePreviewOffsetEnd(dialog, enable);
                    }
                });
            }

        } else {
            onAnimatePreviewOffsetEnd(dialog, enable);
        }
    }
    public static final int PREVIEW_OFFSET_DURATION_MILLIS = 1500;

    protected void onAnimatePreviewOffsetEnd(final AlarmCreateDialog dialog, boolean enable)
    {
        text_offset.setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
        if (enable)
        {
            text_offset.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setPreviewOffset(false);
                    animatePreviewOffset(dialog,false);
                }
            }, PREVIEW_OFFSET_DURATION_MILLIS);
        }
    }

    public boolean previewOffset() {
        return getArgs().getBoolean(EXTRA_PREVIEW_OFFSET, false);
    }
    public void setPreviewOffset(boolean value)
    {
        getArgs().putBoolean(EXTRA_PREVIEW_OFFSET, value);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    public boolean showAlarmListButton() {
        return getArgs().getBoolean(EXTRA_BUTTON_ALARMLIST, false);
    }
    public void setShowAlarmListButton(boolean value)
    {
        getArgs().putBoolean(EXTRA_BUTTON_ALARMLIST, value);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    public boolean showDateSelectButton() {
        return getArgs().getBoolean(EXTRA_BUTTON_DATESELECT, true);
    }
    public void setShowDateSelectButton(boolean value)
    {
        getArgs().putBoolean(EXTRA_BUTTON_DATESELECT, value);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    public boolean showTimePreview() {
        return getArgs().getBoolean(EXTRA_PREVIEW_TIME, true);
    }
    public void setShowTimePreview(boolean value)
    {
        getArgs().putBoolean(EXTRA_PREVIEW_TIME, value);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    public boolean showTabs() {
        return getArgs().getBoolean(EXTRA_SHOW_TABS, true);
    }
    public void setShowTabs(boolean value)
    {
        getArgs().putBoolean(EXTRA_SHOW_TABS, value);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    public boolean showTimeZoneSelectButton() {
        return getArgs().getBoolean(EXTRA_BUTTON_TZSLECT, true);
    }
    public void setShowTimeZoneSelectButton(boolean value)
    {
        getArgs().putBoolean(EXTRA_BUTTON_TZSLECT, value);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    public boolean allowSelectType() {
        return getArgs().getBoolean(EXTRA_ALLOW_SELECT_TYPE, true);
    }
    public void setAllowSelectType(boolean value)
    {
        getArgs().putBoolean(EXTRA_ALLOW_SELECT_TYPE, value);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    @Nullable
    public String getLabelOverride() {
        return getArgs().getString(EXTRA_LABEL_OVERRIDE, null);
    }
    public void setLabelOverride(@Nullable String value)
    {
        getArgs().putString(EXTRA_LABEL_OVERRIDE, value);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    public void setUseAppLocation(@Nullable Context context, boolean value)
    {
        getArgs().putBoolean(EXTRA_LOCATION_FROMAPP, value);
        if (isAdded() && context != null) {
            setEvent(getEvent(), value ? WidgetSettings.loadLocationPref(context, 0) : getLocation());
        }
    }
    public boolean useAppLocation() {
        return getArgs().getBoolean(EXTRA_LOCATION_FROMAPP);
    }

    public int getMode() {
        return (tabs != null ? tabs.getSelectedTabPosition() : getArgs().getInt(EXTRA_MODE, 0));
    }

    public String getEvent()
    {
        String event = getArgs().getString(EXTRA_EVENT);
        return (event != null ? event : DEF_EVENT);
    }
    public Location getLocation()
    {
        Location location = (Location) getArgs().getSerializable(EXTRA_LOCATION);
        return (location != null ? location
                                 : isAdded() && getContext() != null
                                             ? WidgetSettings.loadLocationPref(getContext(), 0)
                                             : WidgetSettings.loadLocationDefault());
    }
    public void setEvent( String event, Location location )
    {
        Bundle args = getArgs();
        args.putString(EXTRA_EVENT, event);
        args.putSerializable(EXTRA_LOCATION, location);

        if (isAdded() && getContext() != null)
        {
            AlarmEventDialog fragment0 = (AlarmEventDialog) getChildFragmentManager().findFragmentByTag(DIALOG_EVENT);
            if (fragment0 != null) {
                initEventDialog(getContext(), fragment0, location);
                fragment0.setChoice(event);
            }

            AlarmTimeDialog fragment1 = (AlarmTimeDialog) getChildFragmentManager().findFragmentByTag(DIALOG_TIME);
            if (fragment1 != null) {
                fragment1.setLocation(location);
                getArgs().putLong(EXTRA_DATE, fragment1.getDate());
                fragment1.updateViews(getContext());
            }
        }
    }

    public int getHour() {
        return getArgs().getInt(EXTRA_HOUR, DEF_HOUR);
    }
    public int getMinute() {
        return getArgs().getInt(EXTRA_MINUTE, DEF_MINUTE);
    }
    public long getDate() {
        return getArgs().getLong(EXTRA_DATE, DEF_DATE);
    }
    public void setDate(long date)
    {
        getArgs().putLong(EXTRA_DATE, date);

        if (isAdded() && getContext() != null)
        {
            AlarmTimeDialog fragment1 = (AlarmTimeDialog) getChildFragmentManager().findFragmentByTag(DIALOG_TIME);
            if (fragment1 != null) {
                fragment1.setDate(date);
                fragment1.updateViews(getContext());
            }
        }
    }
    public String getTimeZone() {
        return getArgs().getString(EXTRA_TIMEZONE);
    }
    public void setAlarmTime( int hour, int minute, String timezone )
    {
        Bundle args = getArgs();
        args.putLong(EXTRA_DATE, -1L);
        args.putInt(EXTRA_HOUR, hour);
        args.putInt(EXTRA_MINUTE, minute);
        args.putString(EXTRA_TIMEZONE, timezone);

        if (isAdded() && getContext() != null)
        {
            AlarmTimeDialog fragment1 = (AlarmTimeDialog) getChildFragmentManager().findFragmentByTag(DIALOG_TIME);
            if (fragment1 != null) {
                fragment1.setTime(hour, minute);
                fragment1.setTimeZone(timezone);
                fragment1.updateViews(getContext());
            }

            updateViews(getContext());
        }
    }

    public void setDialogMode(int mode) {
        getArgs().putInt(EXTRA_MODE, mode);
    }
    public int getDialogMode() {
        return getArgs().getInt(EXTRA_MODE, DEF_MODE);
    }

    public void setAlarmType(AlarmClockItem.AlarmType value)
    {
        getArgs().putSerializable(EXTRA_ALARMTYPE, value);
        if (isAdded())
        {
            AlarmEventDialog fragment = (AlarmEventDialog) getChildFragmentManager().findFragmentByTag(DIALOG_EVENT);
            if (fragment != null) {
                fragment.setType(getAlarmType());
            }
        }
        if (getContext() != null) {
            updateViews(getContext());
        }
    }
    public AlarmClockItem.AlarmType getAlarmType() {
        return (AlarmClockItem.AlarmType) getArgs().getSerializable(EXTRA_ALARMTYPE);
    }

    public void setOffset(long offset)
    {
        getArgs().putLong(EXTRA_OFFSET, offset);
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }
    public long getOffset() {
        return getArgs().getLong(EXTRA_OFFSET, 0);
    }

    private void initEventDialog(Context context, AlarmEventDialog dialog, Location forLocation)
    {
        SuntimesRiseSetDataset sunData = new SuntimesRiseSetDataset(context, 0);
        SuntimesMoonData moonData = new SuntimesMoonData(context, 0);
        SuntimesEquinoxSolsticeDataset equinoxData = new SuntimesEquinoxSolsticeDataset(context, 0);

        if (forLocation != null) {
            sunData.setLocation(forLocation);
            moonData.setLocation(forLocation);
            equinoxData.setLocation(forLocation);
        }

        sunData.calculateData(context);
        moonData.calculate(context);
        equinoxData.calculateData(context);
        dialog.setData(context, sunData, moonData, equinoxData);
        dialog.setUseAppLocation(useAppLocation());
    }

    public static AlarmClockItem createAlarm(@NonNull Context context, @NonNull AlarmCreateDialog dialog, AlarmClockItem.AlarmType type)
    {
        long date;
        int hour;
        int minute;
        String event;
        String timezone;

        if (dialog.getMode() == 0)
        {
            date = -1L;
            hour = -1;
            minute = -1;
            timezone = null;
            event = dialog.getEvent();

        } else {
            date = dialog.getDate();
            hour = dialog.getHour();
            minute = dialog.getMinute();
            timezone = dialog.getTimeZone();
            event = null;
        }
        return AlarmListDialog.createAlarm(context, type, "", event, dialog.getLocation(), date, hour, minute, timezone, AlarmSettings.loadPrefVibrateDefault(context), AlarmSettings.getDefaultRingtoneUri(context, type), AlarmSettings.getDefaultRingtoneName(context, type), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);
    }

    public static void updateAlarmItem(AlarmCreateDialog dialog, AlarmClockItem item)
    {
        item.type = dialog.getAlarmType();
        item.location = dialog.getLocation();
        item.offset = dialog.getOffset();

        if (dialog.getMode() == 0)
        {
            item.hour = -1;
            item.minute = -1;
            item.timezone = null;
            item.setEvent(dialog.getEvent());

        } else {
            item.hour = dialog.getHour();
            item.minute = dialog.getMinute();
            item.timezone = dialog.getTimeZone();
            item.setEvent(dialog.getDate() != -1L ? EventUri.getEventInfoUri(EventUri.AUTHORITY(), Long.toString(dialog.getDate())) : null);
        }
    }

}
