/**
    Copyright (C) 2017-2021 Forrest Guice
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

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.PopupMenu;
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
import android.widget.Toast;

import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.util.Calendar;
import java.util.List;

public class EquinoxDialog extends BottomSheetDialogFragment
{
    public static final String DIALOGTAG_HELP = "equinox_help";

    private EquinoxView equinoxView;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (equinoxView.hasSelection()) {
                    equinoxView.setSelection(null);
                } else super.onBackPressed();
            }
        };
        dialog.setOnShowListener(onShowListener);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_equinox, parent, false);

        equinoxView = (EquinoxView) dialogContent.findViewById(R.id.info_time_equinox);
        equinoxView.setTrackingMode(WidgetSettings.loadTrackingModePref(getContext(), 0));
        if (savedState != null)
        {
            Log.d("DEBUG", "EquinoxDialog onCreate (restoreState)");
            overrideColumnWidthPx = savedState.getInt("overrideColumnWidthPx", overrideColumnWidthPx);
            equinoxView.loadState(savedState);
        }
        equinoxView.setViewListener(new EquinoxView.EquinoxViewListener()
        {
            @Override
            public void onMenuClick(View v, int position) {
                showOverflowMenu(getContext(), v);
            }
            @Override
            public void onMenuClick(View v, int position, WidgetSettings.SolsticeEquinoxMode mode, long datetime) {
                showContextMenu(getContext(), v, mode, datetime);
            }
        });
        themeViews(getContext());

        if (overrideColumnWidthPx >= 0) {
            equinoxView.adjustColumnWidth(overrideColumnWidthPx);
        }

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
                ViewUtils.initPeekHeight(getDialog(), R.id.info_equinoxsolstice_flipper1);
                return behavior;
            }
        }
        return null;
    }

    private DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            Context context = getContext();
            if (context != null) {
                equinoxView.updateViews(getContext());
                equinoxView.post(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtils.initPeekHeight(getDialog(), R.id.info_equinoxsolstice_flipper1);
                    }
                });
            } else Log.w("EquinoxDialog.onShow", "null context! skipping update");
        }
    };

    private void themeViews(Context context)
    {
        if (themeOverride != null) {
            equinoxView.themeViews(context, themeOverride);
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            if (equinoxView != null) {
                themeViews(context);
            }
        }
    }

    public void updateViews()
    {
        if (equinoxView != null) {
            equinoxView.updateViews(getContext());
            Log.d("DEBUG", "EquinoxDialog updated");
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        equinoxView.saveState(outState);
        outState.putInt("overrideColumnWidthPx", overrideColumnWidthPx);
        super.onSaveInstanceState(outState);
    }

    private int overrideColumnWidthPx = -1;
    public void adjustColumnWidth(int columnWidthPx) {
        overrideColumnWidthPx = columnWidthPx;
    }

    protected void showHelp(Context context)
    {
        String topic1 = context.getString(R.string.help_general_timeMode2);
        String topic2 = context.getString(R.string.help_general_tropicalyear);
        //String topic3 = context.getString(R.string.help_general_leapyear);
        String helpContent = context.getString(R.string.help_general2, topic1, topic2);  //, topic3);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpContent);
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
            updateViews();
            if (dialogListener != null) {
                dialogListener.onOptionsModified();
            }
        } else Log.w("EquinoxDialog", "setTrackingMode: invalid item id " + id);
    }

    private PopupMenu.OnMenuItemClickListener onOverflowMenuClick = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.trackRecent: case R.id.trackClosest: case R.id.trackUpcoming:
                    onTrackingModeChanged(getContext(), item.getItemId());
                    return true;

                case R.id.action_help:
                    showHelp(getContext());
                    return true;

                default:
                    return false;
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean showContextMenu(final Context context, View view, final WidgetSettings.SolsticeEquinoxMode mode,  final long datetime)
    {
        PopupMenu menu = new PopupMenu(context, view, Gravity.LEFT);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.equinoxcontext, menu.getMenu());
        menu.setOnMenuItemClickListener(onContextMenuClick);
        menu.setOnDismissListener(onContextMenuDismissed);
        updateContextMenu(context, menu, mode, datetime);
        SuntimesUtils.forceActionBarIcons(menu.getMenu());

        equinoxView.lockScrolling();   // prevent the popupmenu from nudging the view
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
            equinoxView.post(new Runnable() {
                @Override
                public void run() {                      // a submenu may be shown after the popup is dismissed
                    equinoxView.unlockScrolling();           // so defer unlockScrolling until after it is shown
                }
            });
        }
    };

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

                case R.id.action_moon:
                    if (dialogListener != null) {
                        dialogListener.onShowMoonInfo(itemTime);
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

                case R.id.action_share:
                    shareItem(getContext(), itemData);
                    return true;

                default:
                    return false;
            }
        }
    };

    protected void shareItem(Context context, Intent itemData)  // TODO: refactor to use ViewUtils after v0.15.0 branches are merged
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
        public void onShowMoonInfo( long suggestDate ) {}
        public void onShowDate( long suggestedDate ) {}
        public void onOptionsModified() {}
    }

}
