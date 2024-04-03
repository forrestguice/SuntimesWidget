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

package com.forrestguice.suntimeswidget.graph;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;


public class LightGraphDialog extends BottomSheetDialogFragment
{
    public static final String DIALOGTAG_HELP = "lightgraph_help";
    protected static SuntimesUtils utils = new SuntimesUtils();

    protected TextView text_title;
    protected ImageButton btn_menu;

    protected LightGraphView.LightGraphOptions options = new LightGraphView.LightGraphOptions();

    public LightGraphDialog() {
        setArguments(new Bundle());
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme()) {
            @Override
            public void onBackPressed() {
                super.onBackPressed();
            }
        };
        dialog.setOnShowListener(onShowListener);
        return dialog;
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        //options.isRtl = AppSettings.isLocaleRtl(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View v = inflater.cloneInContext(context).inflate(R.layout.layout_dialog_equinox1, parent, false);
        initLocale(context);
        options.init(context);

        text_title = (TextView)v.findViewById(R.id.text_title1);
        btn_menu = (ImageButton)v.findViewById(R.id.menu_button);

        if (text_title != null) {
            //text_title.setOnClickListener(onTitleClicked);
        }
        if (btn_menu != null)
        {
            TooltipCompat.setTooltipText(btn_menu, btn_menu.getContentDescription());
            btn_menu.setOnClickListener(onMenuClicked);
            if (AppSettings.isTelevision(getActivity())) {
                btn_menu.setFocusableInTouchMode(true);
            }
        }

        // TODO: init graph

        if (savedState != null) {
            loadState(savedState);
        }

        themeViews(context);
        updateViews(getContext());
        return v;
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
                ViewUtils.initPeekHeight(getDialog(), peekViewID);
                return behavior;
            }
        }
        return null;
    }

    private final int peekViewID =  R.id.info_equinoxsolstice_flipper1;  // TODO: peek view

    private final DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener() {
        @Override
        public void onShow(DialogInterface dialogInterface) {
            Context context = getContext();
            if (context != null) {
                updateViews(getContext());
                text_title.post(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtils.initPeekHeight(getDialog(), peekViewID);
                    }
                });
            } else Log.w("LightGraphDialog.onShow", "null context! skipping update");
        }
    };

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
            /*
            options.init(themeOverride);
            card_adapter.setThemeOverride(themeOverride);

            text_title.setTextColor(options.titleColor);
            if (options.titleSizeSp != null)
            {
                text_title.setTextSize(options.titleSizeSp);
                text_title.setTypeface(text_title.getTypeface(), (options.titleBold ? Typeface.BOLD : Typeface.NORMAL));
            }

            if (options.textColor != null) {
                //text_year_length.setTextColor(options.textColor);
            }
            if (options.timeSizeSp != null) {
                //text_year_length.setTextSize(options.timeSizeSp);
            }
            */  // TODO
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            themeViews(context);
        }
    }

    public void updateViews(Context context)
    {
        /*showEmptyView(!isImplemented(card_adapter.initData(context, EquinoxDatasetAdapter.CENTER_POSITION)));
        int position = card_adapter.highlightNote(context);
        if (position != -1 && !userSwappedCard()) {
            card_view.setLayoutFrozen(false);
            card_view.scrollToPosition(position);
            card_view.setLayoutFrozen(false);
        }*/
        // TODO: update views
        Log.d("DEBUG", "LightGraphDialog updated");
    }

    protected void updateViews(Context context, SuntimesRiseSetDataset data) {
        text_title.setText(utils.calendarDateYearDisplayString(context, data.calendar()).toString());
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        //outState.putInt("currentCardPosition", currentCardPosition());
        super.onSaveInstanceState(outState);
    }

    public void loadState(Bundle bundle)
    {
        /*int cardPosition = bundle.getInt("currentCardPosition", EquinoxDatasetAdapter.CENTER_POSITION);
        if (cardPosition == RecyclerView.NO_POSITION) {
            cardPosition = EquinoxDatasetAdapter.CENTER_POSITION;
        }
        card_view.scrollToPosition(cardPosition);
        card_view.smoothScrollBy(1, 0);  // triggers a snap*/
    }

    protected void showHelp(Context context)
    {
        String topic1 = context.getString(R.string.help_general_timeMode2);      // TODO: help
        String topic2 = context.getString(R.string.help_general_timeMode2_1);
        String topic3 = context.getString(R.string.help_general_tropicalyear);
        String helpContent = context.getString(R.string.help_general3, topic1, topic2, topic3);

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
        inflater.inflate(R.menu.lightgraphmenu, menu.getMenu());
        menu.setOnMenuItemClickListener(onOverflowMenuClick);
        updateOverflowMenu(context, menu);
        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        menu.show();
        return true;
    }

    private void updateOverflowMenu(Context context, PopupMenu popup)
    {
        Menu menu = popup.getMenu();

        /*MenuItem trackingItem = menu.findItem(R.id.action_tracking_mode);
        if (trackingItem != null) {
            stripMenuItemLabel(trackingItem);
            updateTrackingMenu(trackingItem.getSubMenu(), WidgetSettings.loadTrackingModePref(context, 0));
        }
        MenuItem crossQuarterItem = menu.findItem(R.id.action_crossquarterdays);
        crossQuarterItem.setChecked(AppSettings.loadShowCrossQuarterPref(context));
        */
    }

    private final PopupMenu.OnMenuItemClickListener onOverflowMenuClick = new ViewUtils.ThrottledMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.action_share:
                    shareItem(getContext());
                    return true;

                case R.id.action_help:
                    showHelp(getContext());
                    return true;

                default:
                    return false;
            }
        }
    });

    protected void shareItem(Context context)
    {
        // TODO: share item
        /*WidgetSettings.SolsticeEquinoxMode itemMode = (itemData != null && itemData.hasExtra("mode") ? WidgetSettings.SolsticeEquinoxMode.valueOf(itemData.getStringExtra("mode")) : null);
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
        }*/
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private DialogListener dialogListener = null;
    public void setDialogListener( DialogListener listener ) {
        dialogListener = listener;
    }

    /**
     * DialogListener
     */
    public static class DialogListener
    {
        //public void onSetAlarm( WidgetSettings.SolsticeEquinoxMode suggestedEvent ) {}
        //public void onShowMap( long suggestedDate ) {}
        //public void onShowPosition( long suggestedDate ) {}
        //public void onShowDate( long suggestedDate ) {}
        public void onOptionsModified(boolean closeDialog) {}
    }
}
