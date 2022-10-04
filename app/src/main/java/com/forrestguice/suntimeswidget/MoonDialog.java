/**
    Copyright (C) 2018-2022 Forrest Guice
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

package com.forrestguice.suntimeswidget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData0;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData1;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.MoonApsisView;

import java.util.Calendar;
import java.util.List;

public class MoonDialog extends BottomSheetDialogFragment
{
    public static final String ARG_DATETIME = "datetime";

    private SuntimesUtils utils = new SuntimesUtils();

    public MoonDialog()
    {
        Bundle args = new Bundle();
        args.putLong(ARG_DATETIME, -1);
        setArguments(args);
    }

    private SuntimesMoonData data;
    public void setData( SuntimesMoonData data )
    {
        if (data != null && !data.isCalculated() && data.isImplemented())
        {
            data.calculate();
        }
        this.data = data;
    }

    public void showPositionAt(@Nullable Long datetime)
    {
        getArguments().putLong(ARG_DATETIME, (datetime == null ? -1 : datetime));
        if (isAdded()) {
            updateViews();
        }
    }
    protected long arg_dateTime() {
        return getArguments().getLong(ARG_DATETIME, -1);
    }

    private TextView dialogTitle;
    private MoonRiseSetView moonriseset;
    private MoonPhaseView currentphase;
    private MoonPhasesView1 moonphases;
    private MoonApsisView moonapsis;
    private TextView moondistance, moondistance_label, moondistance_note;

    private int riseColor, setColor, timeColor;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onShowListener);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_moon, parent, false);
        initViews(getContext(), dialogContent);
        themeViews(getContext());
        return dialogContent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog != null) {
            BottomSheetBehavior bottomSheet = initSheet(dialog);
            if (bottomSheet != null) {
                bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }
    private void collapseSheet(Dialog dialog)
    {
        if (dialog != null) {
            BottomSheetBehavior bottomSheet = initSheet(dialog);
            if (bottomSheet != null) {
                bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    @Nullable
    private BottomSheetBehavior initSheet(DialogInterface dialog)
    {
        if (dialog != null)
        {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null)
            {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(false);
                behavior.setSkipCollapsed(true);
                return behavior;
            }
        }
        return null;
    }

    private void initPeekHeight(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            ViewGroup dialogLayout = (LinearLayout) bottomSheet.findViewById(R.id.moondialog_layout);
            View divider1 = bottomSheet.findViewById(R.id.divider1);
            if (dialogLayout != null && divider1 != null)
            {
                Rect headerBounds = new Rect();
                divider1.getDrawingRect(headerBounds);
                dialogLayout.offsetDescendantRectToMyCoords(divider1, headerBounds);
                behavior.setPeekHeight(headerBounds.top);

            } else {
                behavior.setPeekHeight(-1);
            }
        }
    }

    private Runnable initPeekHeight = new Runnable() {
        @Override
        public void run() {
            initPeekHeight(getDialog());
        }
    };

    private DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(final DialogInterface dialog)
        {
            Context context = getContext();
            if (context != null) {
                updateViews();
                dialogTitle.post(initPeekHeight);
            }
            startUpdateTask();
        }
    };

    public void initViews(Context context, View dialogView)
    {
        dialogTitle = (TextView) dialogView.findViewById(R.id.moondialog_title);
        moonriseset = (MoonRiseSetView) dialogView.findViewById(R.id.moonriseset_view);
        currentphase = (MoonPhaseView) dialogView.findViewById(R.id.moonphase_view);

        moonphases = (MoonPhasesView1) dialogView.findViewById(R.id.moonphases_view);
        moonphases.setViewListener(moonphases_listener);

        moonapsis = (MoonApsisView) dialogView.findViewById(R.id.moonapsis_view);
        moonapsis.setViewListener(moonapsis_listener);

        moondistance = (TextView) dialogView.findViewById(R.id.moonapsis_current_distance);
        moondistance_label = (TextView) dialogView.findViewById(R.id.moonapsis_current_label);
        moondistance_note = (TextView) dialogView.findViewById(R.id.moonapsis_current_note);
        moondistance_note.setVisibility(View.GONE);

        if (context != null) {
            currentphase.adjustColumnWidth(context.getResources().getDimensionPixelSize(R.dimen.moonphase_column0_width));
        }
    }

    @SuppressLint("ResourceType")
    public void themeViews(Context context)
    {
        if (themeOverride != null)
        {
            int titleColor = themeOverride.getTitleColor();
            timeColor = themeOverride.getTimeColor();
            int textColor = themeOverride.getTextColor();
            riseColor = themeOverride.getMoonriseTextColor();
            setColor = themeOverride.getMoonsetTextColor();

            dialogTitle.setTextColor(titleColor);
            dialogTitle.setTextSize(themeOverride.getTitleSizeSp());
            dialogTitle.setTypeface(dialogTitle.getTypeface(), (themeOverride.getTitleBold() ? Typeface.BOLD : Typeface.NORMAL));

            moonriseset.themeViews(context, themeOverride);
            currentphase.themeViews(context, themeOverride);
            moonphases.themeViews(context, themeOverride);
            moonapsis.themeViews(context, themeOverride);

            moondistance_label.setTextColor(titleColor);
            moondistance_label.setTextSize(themeOverride.getTitleSizeSp());

            moondistance.setTextColor(textColor);
            moondistance.setTextSize(themeOverride.getTimeSuffixSizeSp());

            moondistance_note.setTextColor(timeColor);
            moondistance_note.setTextSize(themeOverride.getTextSizeSp());

        } else {
            int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.moonriseColor, R.attr.moonsetColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            timeColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.transparent));
            riseColor = ContextCompat.getColor(context, typedArray.getResourceId(1, timeColor));
            setColor = ContextCompat.getColor(context, typedArray.getResourceId(2, timeColor));
            typedArray.recycle();
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            if (moonriseset != null) {
                themeViews(context);
            }
        }
    }

    public void updateViews()
    {
        stopUpdateTask();
        Context context = getContext();
        moonriseset.updateViews(context, data);
        currentphase.updateViews(context, data);
        moonphases.updateViews(context);
        moonapsis.updateViews(context);
        updateMoonApsis();

        final long datetime = arg_dateTime();
        if (datetime != -1)
        {
            moonphases.scrollToDate(datetime);
        }

        startUpdateTask();
    }

    private void updateMoonApsis()
    {
        Context context = getContext();
        if (context != null && data != null && data.isCalculated())
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);

            SuntimesCalculator calculator = data.calculator();
            SuntimesCalculator.MoonPosition position = calculator.getMoonPosition(data.nowThen(data.calendar()));
            if (position != null)
            {
                SuntimesUtils.TimeDisplayText distance = SuntimesUtils.formatAsDistance(context, position.distance, units, 2, true);
                moondistance.setText(SuntimesUtils.createColorSpan(null, SuntimesUtils.formatAsDistance(context, distance), distance.getValue(), (moonapsis.isRising() ? riseColor : setColor)));

                if (SuntimesMoonData.isSuperMoon(position))
                    moondistance_note.setText(context.getString(R.string.timeMode_moon_super));
                else if (SuntimesMoonData.isMicroMoon(position))
                    moondistance_note.setText(context.getString(R.string.timeMode_moon_micro));
                else moondistance_note.setText("");

                moondistance.setVisibility(View.VISIBLE);

            } else moondistance.setVisibility(View.GONE);
        } else {
            moondistance.setVisibility(View.GONE);
            moondistance_note.setVisibility(View.GONE);
        }
    }

    private MoonPhasesView1.MoonPhasesViewListener moonphases_listener = new MoonPhasesView1.MoonPhasesViewListener() {
        @Override
        public void onClick(View v, MoonPhasesView1.PhaseAdapter adapter, int position, SuntimesCalculator.MoonPhase phase) {
            showContextMenu(getActivity(), v, adapter, position, phase);
        }
    };

    private MoonApsisView.MoonApsisViewListener moonapsis_listener = new MoonApsisView.MoonApsisViewListener() {
        @Override
        public void onClick(View v, MoonApsisView.MoonApsisAdapter adapter, int position, boolean isRising) {
            showContextMenu(getActivity(), v, adapter, position, isRising);
        }
    };

    protected boolean showContextMenu(final Context context, View view, MoonPhasesView1.PhaseAdapter adapter, int position, SuntimesCalculator.MoonPhase phase)
    {
        SuntimesMoonData1 data = adapter.initData(context, position);
        if (data != null)
        {
            Calendar date = data.moonPhaseCalendar(phase);
            if (date != null)
            {
                PopupMenu menu = new PopupMenu(context, view, Gravity.LEFT);
                MenuInflater inflater = menu.getMenuInflater();
                inflater.inflate(R.menu.moonphase_context, menu.getMenu());
                menu.setOnDismissListener(onMoonPhaseContextMenuDismissed);
                menu.setOnMenuItemClickListener(onContextMenuClick);
                updateContextMenu(context, menu, SolarEvents.valueOf(phase), date.getTimeInMillis());
                SuntimesUtils.forceActionBarIcons(menu.getMenu());

                moonphases.lockScrolling();   // prevent the popupmenu from nudging the view
                menu.show();
                return true;
            }
        }
        return false;
    }

    protected boolean showContextMenu(final Context context, View view, MoonApsisView.MoonApsisAdapter adapter, int position, boolean isRising)
    {
        SuntimesMoonData0 data = adapter.initData(context, position);
        Pair<Calendar, SuntimesCalculator.MoonPosition> event = isRising ? data.getMoonApogee() : data.getMoonPerigee();

        if (event.first != null)
        {
            PopupMenu menu = new PopupMenu(context, view, Gravity.LEFT);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.moonapsis_context, menu.getMenu());
            menu.setOnDismissListener(onMoonApsisContextMenuDismissed);
            menu.setOnMenuItemClickListener(onContextMenuClick);
            updateContextMenu(context, menu, event.first.getTimeInMillis());
            SuntimesUtils.forceActionBarIcons(menu.getMenu());

            moonapsis.lockScrolling();   // prevent the popupmenu from nudging the view
            menu.show();
            return true;
        }
        return false;
    }

    private void updateContextMenu(Context context, PopupMenu menu, final long datetime) {
        updateContextMenu(context, menu, null, datetime);
    }

    private void updateContextMenu(Context context, PopupMenu menu, @Nullable SolarEvents event, final long datetime)
    {
        Intent data = new Intent();
        data.putExtra(MenuAddon.EXTRA_SHOW_DATE, datetime);
        if (event != null) {
            data.putExtra("event", event.name());
        }
        updateContextMenu(context, menu, data);
    }

    private void updateContextMenu(Context context, PopupMenu menu, Intent data)
    {
        Menu m = menu.getMenu();
        setDataToMenu(m, data);

        MenuItem addonSubmenuItem = m.findItem(R.id.addonSubMenu);
        if (addonSubmenuItem != null) {
            List<MenuAddon.ActivityItemInfo> addonMenuItems = MenuAddon.queryAddonMenuItems(context);
            if (!addonMenuItems.isEmpty()) {
                SuntimesUtils.forceActionBarIcons(addonSubmenuItem.getSubMenu());
                long datetime = data.getLongExtra(MenuAddon.EXTRA_SHOW_DATE, 0);
                MenuAddon.populateSubMenu(addonSubmenuItem, addonMenuItems, datetime);
            }
        }
    }

    private static void setDataToMenu(Menu m, Intent data)
    {
        if (m != null) {
            for (int i = 0; i < m.size(); i++) {
                m.getItem(i).setIntent(data);
                setDataToMenu(m.getItem(i).getSubMenu(), data);
            }
        }
    }

    private PopupMenu.OnMenuItemClickListener onContextMenuClick = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null) {
                return false;
            }

            Intent itemData = item.getIntent();
            long itemTime = ((itemData != null) ? itemData.getLongExtra(MenuAddon.EXTRA_SHOW_DATE, -1L) : -1L);
            SolarEvents event = (itemData != null && itemData.hasExtra("event") ? SolarEvents.valueOf(itemData.getStringExtra("event")) : null);

            switch (item.getItemId())
            {
                case R.id.action_alarm:
                    if (dialogListener != null) {
                        dialogListener.onSetAlarm(event);
                        collapseSheet(getDialog());
                    }
                    return true;

                case R.id.action_sunposition:
                    if (dialogListener != null) {
                        dialogListener.onShowPosition(itemTime);
                        collapseSheet(getDialog());
                    }
                    return true;

                case R.id.action_worldmap:
                    if (dialogListener != null) {
                        dialogListener.onShowMap(itemTime);
                        collapseSheet(getDialog());
                    }
                    return true;

                case R.id.action_date:
                    if (dialogListener != null) {
                        dialogListener.onShowDate(itemTime);
                    }
                    collapseSheet(getDialog());
                    return true;

                // TODO
                //case R.id.action_share:
                //    shareItem(getContext(), itemData);
                //    return true;

                default:
                    return false;
            }
        }
    };

    private PopupMenu.OnDismissListener onMoonPhaseContextMenuDismissed = new PopupMenu.OnDismissListener() {
        @Override
        public void onDismiss(PopupMenu menu) {
            moonapsis.post(new Runnable() {
                @Override
                public void run() {                      // a submenu may be shown after the popup is dismissed
                    moonphases.unlockScrolling();           // so defer unlockScrolling until after it is shown
                }
            });
        }
    };

    private PopupMenu.OnDismissListener onMoonApsisContextMenuDismissed = new PopupMenu.OnDismissListener() {
        @Override
        public void onDismiss(PopupMenu menu) {
            moonapsis.post(new Runnable() {
                @Override
                public void run() {                      // a submenu may be shown after the popup is dismissed
                    moonapsis.unlockScrolling();           // so defer unlockScrolling until after it is shown
                }
            });
        }
    };

    /**@Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        //moonriseset.saveState(outState);
        //currentphase.saveState(outState);
        //moonphases.saveState(outState);
    }*/

    private void startUpdateTask()
    {
        stopUpdateTask();
        if (currentphase != null) {
            currentphase.post(updateTask0);
            currentphase.post(updateTask1);
        }
    }

    private void stopUpdateTask()
    {
        if (currentphase != null) {
            currentphase.removeCallbacks(updateTask0);
            currentphase.removeCallbacks(updateTask1);
        }
    }

    public static final int UPDATE_RATE0 = 3 * 1000;       // 3sec
    private Runnable updateTask0 = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null && currentphase != null)
            {
                currentphase.updatePosition();
                currentphase.postDelayed(this, UPDATE_RATE0);
            }
        }
    };

    public static final int UPDATE_RATE1 = 5 * 60 * 1000;  // 5min
    private Runnable updateTask1 = new Runnable()
    {
        @Override
        public void run()
        {
            if (data != null && currentphase != null)
            {
                currentphase.updateIllumination(getContext());
                currentphase.postDelayed(this, UPDATE_RATE1);
            }
        }
    };

    @Override
    public void onStop()
    {
        stopUpdateTask();
        super.onStop();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private MoonDialogListener dialogListener = null;
    public void setDialogListener( MoonDialogListener listener ) {
        dialogListener = listener;
    }

    /**
     * DialogListener
     */
    public static class MoonDialogListener
    {
        public void onSetAlarm( SolarEvents suggestedEvent ) {}
        public void onShowMap( long suggestedDate ) {}
        public void onShowPosition( long suggestedDate ) {}
        public void onShowDate( long suggestedDate ) {}
    }
}
