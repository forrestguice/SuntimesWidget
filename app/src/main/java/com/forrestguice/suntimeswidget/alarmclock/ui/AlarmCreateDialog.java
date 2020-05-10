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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

@SuppressWarnings("Convert2Diamond")
public class AlarmCreateDialog extends DialogFragment
{
    protected TextView text_title;
    protected ViewPager pager;
    protected DialogPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_alarmcreate, parent, false);

        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }

        return dialogContent;
    }

    private void initViews(Context context, View dialogContent)
    {
        text_title = (TextView) dialogContent.findViewById(R.id.dialog_title);

        adapter = new DialogPagerAdapter(context, getChildFragmentManager());

        pager = (ViewPager) dialogContent.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);

        TabLayout tabs = (TabLayout) dialogContent.findViewById(R.id.tabLayout);
        tabs.setupWithViewPager(pager);

        ImageButton btn_cancel = (ImageButton) dialogContent.findViewById(R.id.dialog_button_cancel);
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
        return pager.getCurrentItem();
    }

    public SolarEvents getEvent()
    {
        AlarmDialog fragment = (AlarmDialog) adapter.getItem(0);
        return fragment.getChoice();
    }

    public Location getLocation()
    {
        return WidgetSettings.loadLocationPref(getActivity(), 0);   // TODO
    }

    public int getHour()
    {
        AlarmTimeDialog fragment = (AlarmTimeDialog) adapter.getItem(1);
        return fragment.getHour();
    }

    public int getMinute()
    {
        AlarmTimeDialog fragment = (AlarmTimeDialog) adapter.getItem(1);
        return fragment.getMinute();
    }

    public long getDate()
    {
        return System.currentTimeMillis();  // TODO
    }

    public String getTimeZone()
    {
        AlarmTimeDialog fragment = (AlarmTimeDialog) adapter.getItem(1);
        return fragment.getTimeZone();
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
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0: return "Event";  // TODO
                case 1: return "Time";   // TODO
                default: return null;
            }
        }

        private Fragment[] items = new Fragment[2];

        @Override
        public Fragment getItem(int position)
        {
            if (items[position] != null)
            {
                return items[position];

            } else {
                switch (position)
                {
                    case 0:
                        AlarmDialog alarmDialog = new AlarmDialog();
                        initEventDialog(contextRef.get(), alarmDialog, WidgetSettings.loadLocationPref(contextRef.get(), 0));
                        alarmDialog.setChoice(SolarEvents.SUNRISE);  // TODO
                        alarmDialog.setDialogShowFrame(false);
                        items[0] = alarmDialog;
                        return alarmDialog;

                    case 1:
                        AlarmTimeDialog timeDialog = new AlarmTimeDialog();
                        timeDialog.setTime(4, 20);  // TODO
                        timeDialog.setTimeZone(null);             // TODO
                        timeDialog.set24Hour(SuntimesUtils.is24());
                        items[1] = timeDialog;
                        return timeDialog;

                    default:
                        return null;
                }
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

}
