/**
    Copyright (C) 2022-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.equinox;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.colors.AppColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesSheetDialog;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.MenuAddon;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesActivity;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeData;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.cards.CardAdapter;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.util.Calendar;
import java.util.List;

public class EquinoxCardDialog extends BottomSheetDialogFragment
{
    public static final String DIALOGTAG_COLORS= "equinox_colors";

    public static final String DIALOGTAG_HELP = "equinox_help";
    public static final int HELP_PATH_ID = R.string.help_solstice_path;

    protected static SuntimesUtils utils = new SuntimesUtils();

    protected TextView empty, text_title;
    protected TextView text_year_length, text_year_length_label;
    protected ImageButton btn_next, btn_prev, btn_menu;

    protected EquinoxViewOptions options = new EquinoxViewOptions();

    protected void setUserSwappedCard(boolean value) {
        getArguments().putBoolean("userSwappedCard", value);
    }
    public boolean userSwappedCard() {
        return getArguments().getBoolean("userSwappedCard", false);
    }

    public EquinoxCardDialog() {
        setArguments(new Bundle());
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (hasSelection())
                {
                    setSelection((Integer) null);
                    if (AppSettings.isTelevision(getActivity())) {
                        btn_menu.requestFocus();
                    }
                } else super.onBackPressed();
            }
        };
        dialog.setOnShowListener(onShowListener);
        return dialog;
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        options.isRtl = AppSettings.isLocaleRtl(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View v = inflater.cloneInContext(context).inflate(R.layout.layout_dialog_equinox1, parent, false);
        initLocale(context);

        options.init(context);
        options.showSeconds = true;

        AppColorValuesCollection<AppColorValues> colors = new AppColorValuesCollection<>();
        boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
        ColorValues values = colors.getSelectedColors(context, (isNightMode ? 1 : 0), AppColorValues.TAG_APPCOLORS);
        if (values != null) {
            options.colors = new EquinoxColorValues(values);
        }

        empty = (TextView)v.findViewById(R.id.txt_empty);
        text_title = (TextView)v.findViewById(R.id.text_title1);
        btn_next = (ImageButton)v.findViewById(R.id.info_time_nextbtn1);
        btn_prev = (ImageButton)v.findViewById(R.id.info_time_prevbtn1);
        btn_menu = (ImageButton)v.findViewById(R.id.menu_button);
        text_year_length = (TextView)v.findViewById(R.id.info_time_year_length);
        text_year_length_label = (TextView)v.findViewById(R.id.info_time_year_length_label);

        if (text_title != null) {
            text_title.setOnClickListener(onTitleClicked);
        }
        if (btn_next != null) {
            TooltipCompat.setTooltipText(btn_next, btn_next.getContentDescription());
            btn_next.setOnClickListener(onNextClicked);
        }
        if (btn_prev != null) {
            TooltipCompat.setTooltipText(btn_prev, btn_prev.getContentDescription());
            btn_prev.setOnClickListener(onPrevClicked);
        }
        if (btn_menu != null)
        {
            TooltipCompat.setTooltipText(btn_menu, btn_menu.getContentDescription());
            btn_menu.setOnClickListener(onMenuClicked);
            if (AppSettings.isTelevision(getActivity())) {
                btn_menu.setFocusableInTouchMode(true);
            }
        }

        initCardView(context, v);

        options.trackingMode = WidgetSettings.loadTrackingModePref(getContext(), 0);
        if (savedState != null) {
            loadState(savedState);
        }

        themeViews(context);
        updateViews(getContext(), card_adapter.initData(getContext(), EquinoxDataAdapter.CENTER_POSITION));
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());

        FragmentManager fragments = getChildFragmentManager();
        ColorValuesSheetDialog colorDialog = (ColorValuesSheetDialog) fragments.findFragmentByTag(DIALOGTAG_COLORS);
        if (colorDialog != null)
        {
            boolean isNightMode = getActivity().getResources().getBoolean(R.bool.is_nightmode);
            colorDialog.setAppWidgetID((isNightMode ? 1 : 0));
            colorDialog.setColorTag(AppColorValues.TAG_APPCOLORS);
            colorDialog.setColorCollection(new AppColorValuesCollection<>(getActivity()));
            colorDialog.setDialogListener(colorDialogListener);
        }

        HelpDialog helpDialog = (HelpDialog) fragments.findFragmentByTag(DIALOGTAG_HELP);
        if (helpDialog != null) {
            helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(getActivity(), HELP_PATH_ID), DIALOGTAG_HELP);
        }
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
                ViewUtils.initPeekHeight(getDialog(), R.id.info_equinoxsolstice_flipper1);
                return behavior;
            }
        }
        return null;
    }

    private final DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialogInterface)
        {
            Context context = getContext();
            if (context != null)
            {
                updateViews(getContext());
                text_title.post(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtils.initPeekHeight(getDialog(), R.id.info_equinoxsolstice_flipper1);
                    }
                });

                if (AppSettings.isTelevision(getActivity())) {
                    btn_menu.requestFocus();
                }

            } else Log.w("EquinoxDialog.onShow", "null context! skipping update");
        }
    };

    private View.OnClickListener onTitleClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onTitleClicked(currentCardPosition());
        }
    };
    protected void onTitleClicked(int position)
    {
        if (currentCardPosition() >= 0)
        {
            int seekPosition = EquinoxDataAdapter.CENTER_POSITION;
            if (Math.abs(position - seekPosition) > SuntimesActivity.HIGHLIGHT_SCROLLING_ITEMS) {
                card_view.scrollToPosition(seekPosition);
            } else {
                card_scroller.setTargetPosition(seekPosition);
                card_layout.startSmoothScroll(card_scroller);
            }
        }
    }

    private View.OnClickListener onNextClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onNextClicked(currentCardPosition());
        }
    };
    protected void onNextClicked(int position) {
        if (position >= 0) {
            setUserSwappedCard(showNextCard(position));
        }
    }

    private View.OnClickListener onPrevClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onPrevClicked(currentCardPosition());
        }
    };
    protected void onPrevClicked(int position) {
        if (position >= 0) {
            setUserSwappedCard(showPreviousCard(position));
        }
    }

    private final View.OnClickListener onMenuClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showOverflowMenu(getContext(), v);
        }
    });

    private void themeViews(Context context)
    {
        if (themeOverride != null)
        {
            options.init(themeOverride);
            card_adapter.setThemeOverride(themeOverride);

            text_title.setTextColor(options.titleColor);
            if (options.titleSizeSp != null)
            {
                text_title.setTextSize(options.titleSizeSp);
                text_title.setTypeface(text_title.getTypeface(), (options.titleBold ? Typeface.BOLD : Typeface.NORMAL));
            }

            ImageViewCompat.setImageTintList(btn_next, SuntimesUtils.colorStateList(options.titleColor, options.disabledColor, options.pressedColor));
            ImageViewCompat.setImageTintList(btn_prev, SuntimesUtils.colorStateList(options.titleColor, options.disabledColor, options.pressedColor));

            if (options.textColor != null) {
                text_year_length.setTextColor(options.textColor);
            }
            if (options.timeSizeSp != null) {
                text_year_length.setTextSize(options.timeSizeSp);
            }
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null)
        {
            themeOverride = theme;
            if (isAdded()) {
                themeViews(context);
            }
        }
    }

    public void updateViews(Context context)
    {
        showEmptyView(!isImplemented(card_adapter.initData(context, EquinoxDatasetAdapter.CENTER_POSITION)));
        int position = card_adapter.highlightNote(context);
        if (position != -1 && !userSwappedCard()) {
            card_view.setLayoutFrozen(false);
            card_view.scrollToPosition(position);
            card_view.setLayoutFrozen(false);
        }
        if (text_year_length_label != null && options.columnWidthPx != -1)
        {
            ViewGroup.LayoutParams layoutParams = text_year_length_label.getLayoutParams();
            layoutParams.width = options.columnWidthPx;
            text_year_length_label.setLayoutParams(layoutParams);
        }
        //Log.d("DEBUG", "EquinoxDialog updated");
    }

    protected boolean isImplemented(SuntimesEquinoxSolsticeDataset data) {
        return (data != null && data.isImplemented());
    }
    protected void updateViews(Context context,  SuntimesEquinoxSolsticeDataset data) {
        updateViews(context, data.dataEquinoxSpring);
    }

    protected boolean isImplemented(SuntimesEquinoxSolsticeData data) {
        return (data != null && data.isImplemented());
    }
    protected void updateViews(Context context, SuntimesEquinoxSolsticeData data)
    {
        text_title.setText(utils.calendarDateYearDisplayString(context, data.eventCalendarThisYear()).toString());
        text_year_length.setText(styleYearDisplayText(context, data.calculator().getTropicalYearLength(data.calendar())));
    }

    protected CharSequence styleYearDisplayText(Context context, long yearLengthMillis)
    {
        double yearLengthDays = yearLengthMillis / 1000d / 60d / 60d / 24;
        String timeString = utils.timeDeltaLongDisplayString(yearLengthMillis);
        String daysString = context.getResources().getQuantityString(R.plurals.units_days, (int)yearLengthDays, utils.formatDoubleValue(yearLengthDays, 6));
        String yearString = context.getString(R.string.length_tropical_year, timeString, daysString);
        return SuntimesUtils.createBoldColorSpan(null, yearString, timeString, options.noteColor);
    }
    
    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        outState.putInt("currentCardPosition", currentCardPosition());
        super.onSaveInstanceState(outState);
    }

    public void loadState(Bundle bundle)
    {
        int cardPosition = bundle.getInt("currentCardPosition", EquinoxDatasetAdapter.CENTER_POSITION);
        if (cardPosition == RecyclerView.NO_POSITION) {
            cardPosition = EquinoxDatasetAdapter.CENTER_POSITION;
        }
        card_view.scrollToPosition(cardPosition);
        card_view.smoothScrollBy(1, 0);  // triggers a snap
    }

    protected void showHelp(Context context)
    {
        String topic1 = context.getString(R.string.help_general_timeMode2);
        String topic2 = context.getString(R.string.help_general_timeMode2_1);
        String topic3 = context.getString(R.string.help_general_tropicalyear);
        String helpContent = context.getString(R.string.help_general3, topic1, topic2, topic3);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpContent);
        helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
        helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(getActivity(), HELP_PATH_ID), DIALOGTAG_HELP);
        helpDialog.show(getChildFragmentManager(), DIALOGTAG_HELP);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean showOverflowMenu(final Context context, View view)
    {
        PopupMenu menu = new PopupMenu(context, view);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.equinoxmenu, menu.getMenu());
        menu.setOnMenuItemClickListener(onOverflowMenuClick);
        updateOverflowMenu(context, menu);
        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        menu.show();
        return true;
    }

    private void updateOverflowMenu(Context context, PopupMenu popup)
    {
        Menu menu = popup.getMenu();

        MenuItem trackingItem = menu.findItem(R.id.action_tracking_mode);
        if (trackingItem != null) {
            stripMenuItemLabel(trackingItem);
            updateTrackingMenu(trackingItem.getSubMenu(), WidgetSettings.loadTrackingModePref(context, 0));
        }

        MenuItem crossQuarterItem = menu.findItem(R.id.action_crossquarterdays);
        crossQuarterItem.setChecked(AppSettings.loadShowCrossQuarterPref(context));
    }

    private static void stripMenuItemLabel(MenuItem item) {
        String title = item.getTitle().toString();
        if (title.endsWith(":")) {
            item.setTitle(title.substring(0, title.length()-1));
        }
    }

    private void updateTrackingMenu(SubMenu trackingMenu, WidgetSettings.TrackingMode trackingMode)
    {
        if (trackingMenu != null)
        {
            MenuItem selectedItem;
            switch (trackingMode) {
                case RECENT: selectedItem = trackingMenu.findItem(R.id.trackRecent); break;
                case CLOSEST: selectedItem = trackingMenu.findItem(R.id.trackClosest); break;
                case SOONEST: default: selectedItem = trackingMenu.findItem(R.id.trackUpcoming); break;
            }
            if (selectedItem != null) {
                selectedItem.setChecked(true);
            }
        }
    }

    private void onTrackingModeChanged(Context context, int id)
    {
        WidgetSettings.TrackingMode mode = null;
        switch (id) {
            case R.id.trackRecent: mode = WidgetSettings.TrackingMode.RECENT; break;
            case R.id.trackClosest: mode = WidgetSettings.TrackingMode.CLOSEST; break;
            case R.id.trackUpcoming: mode = WidgetSettings.TrackingMode.SOONEST; break;
        }
        if (mode != null) {
            WidgetSettings.saveTrackingModePref(context, 0, mode);
            updateViews(getActivity());
            if (dialogListener != null) {
                dialogListener.onOptionsModified(true);
            }
        } else Log.w("EquinoxDialog", "setTrackingMode: invalid item id " + id);
    }

    private void onToggleCrossQuarterDays(Context context, MenuItem item)
    {
        AppSettings.saveShowCrossQuarterPref(context, !item.isChecked());
        initAdapter(context);
        updateViews(context);
        if (dialogListener != null) {
            dialogListener.onOptionsModified(false);
        }
        card_view.post(new Runnable() {
            @Override
            public void run() {
                initSheet(getDialog());    // re-init dialog peek height
            }
        });
    }

    private final PopupMenu.OnMenuItemClickListener onOverflowMenuClick = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.action_colors:
                    showColorDialog(getActivity());
                    return true;

                case R.id.trackRecent: case R.id.trackClosest: case R.id.trackUpcoming:
                    onTrackingModeChanged(getContext(), item.getItemId());
                    return true;

                case R.id.action_crossquarterdays:
                    onToggleCrossQuarterDays(getActivity(), item);
                    return true;

                case R.id.action_help:
                    showHelp(getContext());
                    return true;

                default:
                    return false;
            }
        }
    });

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void showColorDialog(Context context)
    {
        boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
        ColorValuesSheetDialog dialog = new ColorValuesSheetDialog();
        dialog.setAppWidgetID((isNightMode ? 1 : 0));
        dialog.setColorTag(AppColorValues.TAG_APPCOLORS);
        dialog.setColorCollection(new AppColorValuesCollection<>(context));
        dialog.setDialogListener(colorDialogListener);
        dialog.setFilter(new EquinoxColorValues().getColorKeys());
        dialog.show(getChildFragmentManager(), DIALOGTAG_COLORS);
    }

    private final ColorValuesSheetDialog.DialogListener colorDialogListener = new ColorValuesSheetDialog.DialogListener()
    {
        @Override
        public void onColorValuesSelected(ColorValues values)
        {
            if (values != null) {
                options.colors = new EquinoxColorValues(values);
            } else {
                options.init(getActivity());
            }
            card_adapter.notifyDataSetChanged();

            if (dialogListener != null) {
                dialogListener.onColorsModified(values);
            }
        }

        public void requestPeekHeight(int height) {}
        public void requestHideSheet() {}
        public void requestExpandSheet() {}
        public void onModeChanged(int mode) {}

        @Nullable
        @Override
        public ColorValues getDefaultValues() {
            return new AppColorValues(getActivity(), true);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean showContextMenu(final Context context, View view, final WidgetSettings.SolsticeEquinoxMode mode,  final long datetime)
    {
        PopupMenu menu = new PopupMenu(context, view, Gravity.START);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.equinoxcontext, menu.getMenu());
        menu.setOnMenuItemClickListener(onContextMenuClick);
        menu.setOnDismissListener(onContextMenuDismissed);
        updateContextMenu(context, menu, mode, datetime);
        SuntimesUtils.forceActionBarIcons(menu.getMenu());

        lockScrolling();   // prevent the popupmenu from nudging the view
        menu.show();
        return true;
    }

    private void updateContextMenu(Context context, PopupMenu menu, final WidgetSettings.SolsticeEquinoxMode mode, final long datetime)
    {
        Intent data = new Intent();
        data.putExtra(MenuAddon.EXTRA_SHOW_DATE, datetime);
        data.putExtra("mode", mode.name());

        Menu m = menu.getMenu();
        setDataToMenu(m, data);

        MenuItem alarmItem = m.findItem(R.id.action_alarm);
        if (alarmItem != null) {
            alarmItem.setVisible(AlarmSettings.hasAlarmSupport(context));
        }

        MenuItem addonSubmenuItem = m.findItem(R.id.addonSubMenu);
        if (addonSubmenuItem != null) {
            List<MenuAddon.ActivityItemInfo> addonMenuItems = MenuAddon.queryAddonMenuItems(context);
            if (!addonMenuItems.isEmpty()) {
                SuntimesUtils.forceActionBarIcons(addonSubmenuItem.getSubMenu());
                MenuAddon.populateSubMenu(addonSubmenuItem, addonMenuItems, datetime);
            } //else addonSubmenuItem.setVisible(false);
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

    private PopupMenu.OnDismissListener onContextMenuDismissed = new PopupMenu.OnDismissListener() {
        @Override
        public void onDismiss(PopupMenu menu) {
            text_title.post(new Runnable() {
                @Override
                public void run() {                      // a submenu may be shown after the popup is dismissed
                    unlockScrolling();           // so defer unlockScrolling until after it is shown
                }
            });
        }
    };

    private final PopupMenu.OnMenuItemClickListener onContextMenuClick = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
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
            WidgetSettings.SolsticeEquinoxMode itemMode = (itemData != null && itemData.hasExtra("mode") ? WidgetSettings.SolsticeEquinoxMode.valueOf(itemData.getStringExtra("mode")) : null);

            switch (item.getItemId())
            {
                case R.id.action_alarm:
                    if (dialogListener != null) {
                        dialogListener.onSetAlarm(itemMode);
                        //collapseSheet(getDialog());
                    }
                    return true;

                case R.id.action_sunposition:
                    if (dialogListener != null) {
                        dialogListener.onShowPosition(itemTime);
                        //collapseSheet(getDialog());
                    }
                    return true;

                case R.id.action_worldmap:
                    if (dialogListener != null) {
                        dialogListener.onShowMap(itemTime);
                        //collapseSheet(getDialog());
                    }
                    return true;

                case R.id.action_date:
                    if (dialogListener != null) {
                        dialogListener.onShowDate(itemTime);
                    }
                    collapseSheet(getDialog());
                    return true;

                case R.id.action_calendar:
                    openCalendar(getActivity(), itemTime);
                    return true;

                case R.id.action_share:
                    shareItem(getContext(), itemData);
                    return true;

                default:
                    return false;
            }
        }
    });

    protected void openCalendar(Context context, long itemMillis)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("content://com.android.calendar/time/" + itemMillis));
        context.startActivity(intent);
    }

    protected void shareItem(Context context, Intent itemData)
    {
        WidgetSettings.SolsticeEquinoxMode itemMode = (itemData != null && itemData.hasExtra("mode") ? WidgetSettings.SolsticeEquinoxMode.valueOf(itemData.getStringExtra("mode")) : null);
        long itemMillis = itemData != null ? itemData.getLongExtra(MenuAddon.EXTRA_SHOW_DATE, -1L) : -1L;
        if (itemMode != null && itemMillis != -1L)
        {
            Calendar itemTime = Calendar.getInstance();
            itemTime.setTimeInMillis(itemMillis);
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);

            SuntimesUtils utils = new SuntimesUtils();
            SuntimesUtils.initDisplayStrings(context);
            String itemDisplay = context.getString(R.string.share_format_equinox, itemMode, utils.calendarDateTimeDisplayString(context, itemTime, showTime, showSeconds).toString());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText(itemMode.getLongDisplayString(), itemDisplay));
                }
            } else {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setText(itemDisplay);
                }
            }
            Toast.makeText(getContext(), itemDisplay, Toast.LENGTH_SHORT).show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected RecyclerView card_view;
    protected GridLayoutManager card_layout;
    protected CardAdapter.CardViewScroller card_scroller;

    protected void initCardView(Context context, View v)
    {
        card_view = (RecyclerView)v.findViewById(R.id.info_equinoxsolstice_flipper1);
        card_view.setHasFixedSize(true);
        card_view.setItemViewCacheSize(7);
        card_view.addItemDecoration(new CardViewDecorator(context));

        card_scroller = new CardAdapter.CardViewScroller(context);
        card_view.setOnScrollListener(onCardScrollListener);
        card_view.setLayoutFrozen(false);

        SnapHelper snapHelper = new LinearSnapHelper(); //new PagerSnapHelper();
        snapHelper.attachToRecyclerView(card_view);

        initAdapter(context);
    }

    protected int card_itemsPerPage = WidgetSettings.SolsticeEquinoxMode.values().length;
    protected int card_orientation = LinearLayoutManager.HORIZONTAL;
    protected EquinoxDataAdapter card_adapter;

    protected void initAdapter(Context context)
    {
        boolean southernHemisphere = (WidgetSettings.loadLocalizeHemispherePref(context, 0)) && (WidgetSettings.loadLocationPref(context, 0).getLatitudeAsDouble() < 0);
        WidgetSettings.SolsticeEquinoxMode[] modes = AppSettings.loadShowCrossQuarterPref(context) ? WidgetSettings.SolsticeEquinoxMode.values(southernHemisphere)
                                                                                                   : WidgetSettings.SolsticeEquinoxMode.partialValues(southernHemisphere);
        card_orientation = LinearLayoutManager.HORIZONTAL;
        card_itemsPerPage = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? modes.length : Math.max(4, modes.length / 2));
        options.highlightPosition = -1;

        card_adapter = new EquinoxDataAdapter(context, modes, options);
        card_adapter.setAdapterListener(cardListener);
        card_view.setAdapter(card_adapter);

        card_layout = new GridLayoutManager(context, card_itemsPerPage, card_orientation, false);
        card_view.setLayoutManager(card_layout);
        card_view.scrollToPosition(EquinoxDatasetAdapter.CENTER_POSITION + modes.length);

        //ViewGroup.LayoutParams params = card_view.getLayoutParams();
        //params.height = (int)Math.ceil(card_itemsPerPage * context.getResources().getDimension(R.dimen.equinoxItem_height)) + 2;
        //card_view.setLayoutParams(params);
    }

    private final RecyclerView.OnScrollListener onCardScrollListener = new RecyclerView.OnScrollListener()
    {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy)
        {
            int position = currentCardPosition();
            if (position >= 0) {
                //Log.d("DEBUG", "onScrolled: position: " + position);
                updateViews(getContext(), card_adapter.initData(getContext(), position));
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState ==  RecyclerView.SCROLL_STATE_DRAGGING) {
                setUserSwappedCard(true);
            }
        }
    };

    private void showEmptyView( boolean show ) {
        empty.setVisibility(show ? View.VISIBLE : View.GONE);
        card_view.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void lockScrolling() {
        card_view.setLayoutFrozen(true);
    }
    public void unlockScrolling() {
        card_view.setLayoutFrozen(false);
    }

    public int currentCardPosition()
    {
        int first = card_layout.findFirstVisibleItemPosition();
        //int last = card_layout.findLastVisibleItemPosition();
        //int p = (first + last) / 2;
        //Log.d("DEBUG", "currentCardPosition: " + first + ", " + last + " => " + p);
        return first;
    }

    public boolean showNextCard(int position)
    {
        int nextPosition = (position + card_itemsPerPage);
        int n = card_adapter.getItemCount();
        if (nextPosition < n) {
            setUserSwappedCard(true);
            card_scroller.setTargetPosition(nextPosition);
            card_layout.startSmoothScroll(card_scroller);
        }
        return true;
    }

    public boolean showPreviousCard(int position)
    {
        int prevPosition = (position - card_itemsPerPage);
        if (prevPosition >= 0) {
            setUserSwappedCard(true);
            card_scroller.setTargetPosition(prevPosition);
            card_layout.startSmoothScroll(card_scroller);
        }
        return true;
    }

    private EquinoxAdapterListener cardListener = new EquinoxAdapterListener()
    {
        @Override
        public void onClick( int position ) {
            card_adapter.setSelection(position);
        }
        @Override
        public boolean onLongClick( int position ) {
            return false;
        }
        @Override
        public void onTitleClick( int position ) {
            onTitleClicked(position);
        }
        @Override
        public void onNextClick( int position ) {
            onNextClicked(position);
        }
        @Override
        public void onPrevClick( int position ) {
            onPrevClicked(position);
        }
        @Override
        public void onMenuClick(View view, int position, WidgetSettings.SolsticeEquinoxMode mode, long datetime) {
            showContextMenu(getContext(), view, mode, datetime);
        }
    };

    public static class CardViewDecorator extends RecyclerView.ItemDecoration {
        private int marginPx;
        public CardViewDecorator( Context context ) {
            marginPx = (int)context.getResources().getDimension(R.dimen.dialog_margin1);
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = outRect.right = marginPx;
            outRect.top = outRect.bottom = 0;
        }
    }

    public WidgetSettings.SolsticeEquinoxMode getSelection() {
        return card_adapter.getSelection();
    }
    public boolean hasSelection() {
        return card_adapter.hasSelection();
    }
    public void setSelection(@Nullable WidgetSettings.SolsticeEquinoxMode mode) {
        card_adapter.setSelection(mode);
    }
    public void setSelection(@Nullable Integer position) {
        card_adapter.setSelection(position);
    }

    public void adjustColumnWidth(int columnWidthPx)
    {
        options.columnWidthPx = columnWidthPx;
        if (card_adapter != null) {
            card_adapter.notifyDataSetChanged();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private EquinoxDialogListener dialogListener = null;
    public void setDialogListener( EquinoxDialogListener listener ) {
        dialogListener = listener;
    }

    /**
     * DialogListener
     */
    public static class EquinoxDialogListener
    {
        public void onSetAlarm( WidgetSettings.SolsticeEquinoxMode suggestedEvent ) {}
        public void onShowMap( long suggestedDate ) {}
        public void onShowPosition( long suggestedDate ) {}
        public void onShowDate( long suggestedDate ) {}
        public void onOptionsModified(boolean closeDialog) {}
        public void onColorsModified(ColorValues values) {}
    }


}
