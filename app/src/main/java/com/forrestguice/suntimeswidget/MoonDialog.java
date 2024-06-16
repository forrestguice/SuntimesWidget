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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData0;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData1;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;
import com.forrestguice.suntimeswidget.colors.AppColorValues;
import com.forrestguice.suntimeswidget.colors.AppColorValuesCollection;
import com.forrestguice.suntimeswidget.colors.ColorValues;
import com.forrestguice.suntimeswidget.colors.ColorValuesSheetDialog;
import com.forrestguice.suntimeswidget.map.WorldMapDialog;
import com.forrestguice.suntimeswidget.moon.colors.MoonApsisColorValues;
import com.forrestguice.suntimeswidget.moon.colors.MoonPhasesColorValues;
import com.forrestguice.suntimeswidget.moon.colors.MoonRiseSetColorValues;
import com.forrestguice.suntimeswidget.moon.MoonPhaseView1;
import com.forrestguice.suntimeswidget.moon.MoonPhasesView1;
import com.forrestguice.suntimeswidget.moon.MoonRiseSetView1;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.themes.SuntimesTheme;
import com.forrestguice.suntimeswidget.moon.MoonApsisView;
import com.forrestguice.suntimeswidget.views.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.ShareUtils;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MoonDialog extends BottomSheetDialogFragment
{
    public static final String ARG_DATETIME = "datetime";
    public static final String ARG_PLAYING = "playing";
    public static final String ARG_PLAY_OFFSET = "offsetMinutes";

    public static final String DIALOGTAG_COLORS = "moon_colors";

    public static final String DIALOGTAG_HELP = "moon_help";
    public static final int HELP_PATH_ID = R.string.help_moon_path;

    public static final String MAPTAG_MOON = "_moon";

    private SuntimesUtils utils = new SuntimesUtils();

    public MoonDialog()
    {
        Bundle args = new Bundle();
        args.putLong(ARG_DATETIME, -1);
        args.putBoolean(ARG_PLAYING, false);
        args.putInt(ARG_PLAY_OFFSET, 0);
        setArguments(args);
    }

    private SuntimesMoonData data;
    public void setData( SuntimesMoonData data )
    {
        if (data != null && !data.isCalculated() && data.isImplemented()) {
            data.calculate();
        }
        this.data = data;
    }

    public void showPositionAt(@Nullable Long datetime) {
        showPositionAt(datetime, true);
    }
    public void showPositionAt(@Nullable Long datetime, boolean updateViews)
    {
        getArguments().putLong(ARG_DATETIME, (datetime == null ? -1 : datetime));
        if (isAdded() && updateViews) {
            updateViews();
        }
    }
    protected long arg_dateTime() {
        return getArguments().getLong(ARG_DATETIME, -1);
    }

    private TextView text_dialogTitle;
    private TextView text_dialogTime, text_dialogTimeOffset;
    private MoonRiseSetView1 moonriseset;
    private MoonPhaseView1 currentphase;
    private MoonPhasesView1 moonphases;
    private MoonApsisView moonapsis;
    private TextView moondistance, moondistance_label, moondistance_note;
    private ImageButton playButton, pauseButton, nextButton, prevButton, resetButton, menuButton;
    private View mediaAnchor = null;

    private int timeColor, warningColor, accentColor, normalColor, pressedColor, disabledColor;
    //private int riseColor, setColor;

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
        initLocale(getContext());
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_moon, parent, false);
        initViews(getContext(), dialogContent);
        themeViews(getContext());
        return dialogContent;
    }

    public void initLocale(Context context)
    {
        SuntimesUtils.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(context);
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
    public boolean isCollapsed()
    {
        BottomSheetBehavior bottomSheet = initSheet(getDialog());
        if (bottomSheet != null) {
            return (bottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED);
        }
        return false;
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
                text_dialogTitle.post(initPeekHeight);
            }
            startUpdateTask();
        }
    };

    public void initViews(Context context, View dialogView)
    {
        text_dialogTitle = (TextView) dialogView.findViewById(R.id.moondialog_title);

        text_dialogTime = (TextView) dialogView.findViewById(R.id.info_time_moon);
        text_dialogTimeOffset = (TextView) dialogView.findViewById(R.id.info_time_offset);

        moonriseset = (MoonRiseSetView1) dialogView.findViewById(R.id.moonriseset_view);
        currentphase = (MoonPhaseView1) dialogView.findViewById(R.id.moonphase_view);
        moonphases = (MoonPhasesView1) dialogView.findViewById(R.id.moonphases_view);
        moonapsis = (MoonApsisView) dialogView.findViewById(R.id.moonapsis_view);

        moondistance = (TextView) dialogView.findViewById(R.id.moonapsis_current_distance);
        moondistance_label = (TextView) dialogView.findViewById(R.id.moonapsis_current_label);
        moondistance_note = (TextView) dialogView.findViewById(R.id.moonapsis_current_note);
        moondistance_note.setVisibility(View.GONE);

        playButton = (ImageButton) dialogView.findViewById(R.id.media_play);
        if (playButton != null) {
            playButton.setOnClickListener(onPlayClicked);
        }
        pauseButton = (ImageButton) dialogView.findViewById(R.id.media_pause);
        if (pauseButton != null) {
            pauseButton.setOnClickListener(onPauseClicked);
        }
        nextButton = (ImageButton) dialogView.findViewById(R.id.media_next);
        if (nextButton != null) {
            TooltipCompat.setTooltipText(nextButton, nextButton.getContentDescription());
            nextButton.setOnClickListener(onNextClicked);
        }
        prevButton = (ImageButton) dialogView.findViewById(R.id.media_prev);
        if (prevButton != null) {
            TooltipCompat.setTooltipText(prevButton, prevButton.getContentDescription());
            prevButton.setOnClickListener(onPrevClicked);
        }
        resetButton = (ImageButton) dialogView.findViewById(R.id.media_reset);
        if (resetButton != null)
        {
            TooltipCompat.setTooltipText(resetButton, resetButton.getContentDescription());
            resetButton.setEnabled(false);
            resetButton.setOnClickListener(onResetClicked);
        }

        mediaAnchor = dialogView.findViewById(R.id.dialogTopRightAnchor);

        menuButton = (ImageButton) dialogView.findViewById(R.id.menu_button);
        if (menuButton != null)
        {
            TooltipCompat.setTooltipText(menuButton, menuButton.getContentDescription());
            menuButton.setOnClickListener(onMenuClicked);
            if (AppSettings.isTelevision(getActivity())) {
                menuButton.setFocusableInTouchMode(true);
            }
        }

        if (context != null) {
            currentphase.adjustColumnWidth(context.getResources().getDimensionPixelSize(R.dimen.moonphase_column0_width));
        }
        attachListeners();
    }

    protected void attachListeners()
    {
        moonriseset.setViewListener(moonriseset_listener);
        text_dialogTimeOffset.setOnClickListener(currentphase_onClickListener);
        currentphase.setOnClickListener(currentphase_onClickListener);
        currentphase.setOnLongClickListener(currentphase_onLongClickListener);
        moonphases.setViewListener(moonphases_listener);
        moonapsis.setViewListener(moonapsis_listener);
    }
    protected void detachListeners()
    {
        moonriseset.setViewListener(null);
        text_dialogTimeOffset.setOnClickListener(null);
        currentphase.setOnClickListener(null);
        currentphase.setOnLongClickListener(null);
        moonphases.setViewListener(null);
        moonapsis.setViewListener(null);
    }

    @SuppressLint("ResourceType")
    public void themeViews(Context context)
    {
        AppColorValues values = AppColorValuesCollection.initSelectedColors(getActivity());
        if (values != null) {
            currentphase.setColors(getActivity(), values);
            moonriseset.setColors(getActivity(), values);
            moonapsis.setColors(getActivity(), values);
            moonphases.setColors(getActivity(), values);
        }

        if (themeOverride != null)
        {
            int titleColor = themeOverride.getTitleColor();
            timeColor = normalColor = themeOverride.getTimeColor();
            int textColor = themeOverride.getTextColor();
            //riseColor = themeOverride.getMoonriseTextColor();
            //setColor = themeOverride.getMoonsetTextColor();
            pressedColor = accentColor = themeOverride.getAccentColor();
            warningColor = themeOverride.getActionColor();
            float timeSizeSp = themeOverride.getTimeSizeSp();
            float textSizeSp = themeOverride.getTextSizeSp();

            text_dialogTitle.setTextColor(titleColor);
            text_dialogTitle.setTextSize(themeOverride.getTitleSizeSp());
            text_dialogTitle.setTypeface(text_dialogTitle.getTypeface(), (themeOverride.getTitleBold() ? Typeface.BOLD : Typeface.NORMAL));

            text_dialogTime.setTextColor(titleColor);
            text_dialogTime.setTextSize(timeSizeSp);

            text_dialogTimeOffset.setTextColor(textColor);
            text_dialogTimeOffset.setTextSize(textSizeSp);

            moonriseset.themeViews(context, themeOverride);
            currentphase.themeViews(context, themeOverride);
            moonphases.themeViews(context, themeOverride);
            moonapsis.themeViews(context, themeOverride);

            moondistance_label.setTextColor(titleColor);
            moondistance_label.setTextSize(themeOverride.getTitleSizeSp());

            moondistance.setTextColor(textColor);
            moondistance.setTextSize(themeOverride.getTimeSuffixSizeSp());

            moondistance_note.setTextColor(timeColor);
            moondistance_note.setTextSize(textSizeSp);

        } else {
            int[] colorAttrs = { android.R.attr.textColorPrimary, R.attr.tagColor_warning, R.attr.buttonPressColor, R.attr.text_disabledColor, R.attr.text_accentColor };
            TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
            timeColor = normalColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.grey_50));
            warningColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.warningTag));
            pressedColor = ContextCompat.getColor(context, typedArray.getResourceId(2, R.color.btn_tint_pressed));
            disabledColor = ContextCompat.getColor(context, typedArray.getResourceId(3, timeColor));
            accentColor = ContextCompat.getColor(context, typedArray.getResourceId(4, R.color.text_accent));
            typedArray.recycle();
        }

        if (resetButton != null) {
            ImageViewCompat.setImageTintList(resetButton, SuntimesUtils.colorStateList(warningColor, disabledColor, pressedColor));
        }
    }

    private SuntimesTheme themeOverride = null;
    public void themeViews(Context context, SuntimesTheme theme)
    {
        if (theme != null) {
            themeOverride = theme;
            if (moonriseset != null) {
                moonriseset.themeViews(context, theme);
            }
            if (moonphases != null) {
                moonphases.themeViews(context, theme);
            }
            if (moonapsis != null) {
                moonapsis.themeViews(context, theme);
            }
        }
    }

    public void updateViews() {
        updateViews(true);
    }
    public void updateViews(boolean scrollViews)
    {
        stopUpdateTask();
        Context context = getContext();
        Calendar dateTime = getDialogCalendar();
        updateTimeText();
        moonriseset.updateViews(context);
        currentphase.updateViews(context, data, dateTime);
        moonphases.updateViews(context);
        moonapsis.updateViews(context);
        updateMoonApsis(dateTime);

        if (resetButton != null) {
            boolean resetIsPossible = isOffset(arg_dateTime() != -1 ? arg_dateTime() : getNow());
            resetButton.setEnabled(resetIsPossible);
            resetButton.setVisibility(resetIsPossible ? View.VISIBLE : View.GONE);
        }
        if (playButton != null) {
            playButton.setVisibility(isPlaying() ? View.GONE : View.VISIBLE);
        }
        if (pauseButton != null) {
            pauseButton.setVisibility(isPlaying() ? View.VISIBLE : View.GONE);
        }

        final long datetime = arg_dateTime();
        if (datetime != -1 && scrollViews)
        {
            moonriseset.scrollToDate(datetime);
            moonphases.scrollToDate(datetime);
            moonapsis.scrollToDate(datetime);
        }
        startUpdateTask();
    }

    protected void updateTimeText()
    {
        Context context = getContext();
        if (context == null) {
            return;
        }

        long nowMillis = getNow();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(getNow());

        long dialogTimeMillis = getDialogTime();

        Calendar dialogTime = Calendar.getInstance(data.timezone());
        dialogTime.setTimeInMillis(dialogTimeMillis);
        boolean nowIsAfter = now.after(dialogTime);

        String suffix = "";
        if (isOffset(nowMillis, dialogTimeMillis)) {
            suffix = ((nowIsAfter) ? context.getString(R.string.past_today) : context.getString(R.string.future_today));
        }

        SuntimesUtils.TimeDisplayText timeText = utils.calendarDateTimeDisplayString(context, dialogTime);
        if (text_dialogTime != null)
        {
            //String tzDisplay = WidgetTimezones.getTimeZoneDisplay(context, moonTime.getTimeZone());
            if (suffix.isEmpty()) {
                text_dialogTime.setText(timeText.toString());
                // dialogTime.setText(getString(R.string.datetime_format_verylong, timeText.toString(), tzDisplay));
            } else text_dialogTime.setText(SuntimesUtils.createBoldColorSpan(null, getString(R.string.datetime_format_verylong, timeText.toString(), suffix), suffix, warningColor));
        }

        if (text_dialogTimeOffset != null) {
            if (!suffix.isEmpty())
            {
                SuntimesUtils.TimeDisplayText offsetText = utils.timeDeltaLongDisplayString(nowMillis, dialogTimeMillis, false, true, false);
                offsetText.setSuffix("");
                String displayString = getContext().getString((nowIsAfter ? R.string.ago : R.string.hence), offsetText.toString());
                text_dialogTimeOffset.setText(SuntimesUtils.createBoldColorSpan(null, displayString, offsetText.toString(), warningColor));
            } else text_dialogTimeOffset.setText(" ");
        }
    }

    protected long getNow() {
        return System.currentTimeMillis();
    }
    protected long getDialogTime()
    {
        long offsetMillis = getOffsetMinutes() * 60 * 1000;
        return (arg_dateTime() != -1 ? arg_dateTime() : getNow()) + offsetMillis;
    }
    protected Calendar getDialogCalendar() {
        Calendar c = Calendar.getInstance(data != null ? data.timezone() : TimeZone.getDefault());
        c.setTimeInMillis(getDialogTime());
        return c;
    }

    protected  boolean isOffset(long nowMillis) {
        return isOffset(nowMillis, getDialogTime());
    }
    protected boolean isOffset(long nowMillis, long eventMillis) {
        return Math.abs(nowMillis - eventMillis) > 60 * 1000;
    }

    private void updateMoonApsis(Calendar dateTime)
    {
        Context context = getContext();
        if (context != null && data != null && data.isCalculated())
        {
            WidgetSettings.LengthUnit units = WidgetSettings.loadLengthUnitsPref(context, 0);

            SuntimesCalculator calculator = data.calculator();
            SuntimesCalculator.MoonPosition position = calculator.getMoonPosition(dateTime);
            if (position != null)
            {
                ColorValues colors = moonapsis.getColors();
                int risingColor = colors.getColor(MoonApsisColorValues.COLOR_MOON_APOGEE_TEXT);
                int settingColor = colors.getColor(MoonApsisColorValues.COLOR_MOON_PERIGEE_TEXT);
                SuntimesUtils.TimeDisplayText distance = SuntimesUtils.formatAsDistance(context, position.distance, units, 2, true);
                moondistance.setText(SuntimesUtils.createColorSpan(null, SuntimesUtils.formatAsDistance(context, distance), distance.getValue(), (moonapsis.isRising() ? risingColor : settingColor)));

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

    protected void scrollAllToMoonRiseSet(int position)
    {
        setData(moonriseset.getData(position));
        boolean isCentered = (position >= MoonRiseSetView1.MoonRiseSetAdapter.CENTER_POSITION && position < MoonRiseSetView1.MoonRiseSetAdapter.CENTER_POSITION + moonriseset.getItemsPerDay());
        if (isCentered)
        {
            showPositionAt(null, false);  // set position without triggering normal update..
            updateViews(false);                     // to avoid infinite-scroll (scrolling moonriseset may trigger this method)
            moonphases.scrollToCenter();
            moonapsis.scrollToCenter();

        } else {
            if (data != null)
            {
                boolean scrollForward = (data.calendar().getTimeInMillis() >= getNow());
                long datetime = data.nowThen(data.calendar()).getTimeInMillis() + ((60 * 1000) * (scrollForward ? 1 : -1));   // plus or minus a minute to round off the display value
                showPositionAt(datetime, false);     // set position without triggering normal update..
                updateViews(false);                   // to avoid infinite-scroll (scrolling moonriseset may trigger this method)
                moonphases.scrollToDate(datetime);
                moonapsis.scrollToDate(datetime);
            }
        }
    }

    private final MoonRiseSetView1.MoonRiseSetViewListener moonriseset_listener = new MoonRiseSetView1.MoonRiseSetViewListener()
    {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState, int position) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                scrollAllToMoonRiseSet(position);
            }
        }

        @Override
        public void onResetClick(View v) {
        }

        @Override
        public void onClick(View v, MoonRiseSetView1.MoonRiseSetAdapter adapter, int position, String eventID) {
            showContextMenu(getActivity(), v, adapter, position, eventID);
        }
    };

    private final MoonPhasesView1.MoonPhasesViewListener moonphases_listener = new MoonPhasesView1.MoonPhasesViewListener() {
        @Override
        public void onClick(View v, MoonPhasesView1.PhaseAdapter adapter, int position, SuntimesCalculator.MoonPhase phase) {
            showContextMenu(getActivity(), v, adapter, position, phase);
        }
    };

    private final MoonApsisView.MoonApsisViewListener moonapsis_listener = new MoonApsisView.MoonApsisViewListener() {
        @Override
        public void onClick(View v, MoonApsisView.MoonApsisAdapter adapter, int position, boolean isRising) {
            showContextMenu(getActivity(), v, adapter, position, isRising);
        }
    };

    private final View.OnClickListener currentphase_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //showMediaMenu(getActivity(), v);
            showMediaPopup(getActivity(), text_dialogTimeOffset);
        }
    };
    private final View.OnLongClickListener currentphase_onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            togglePlay();
            return true;
        }
    };

    private final View.OnClickListener onPlayClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            playMap();
        }
    };
    private final View.OnClickListener onPauseClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            stopMap(false);
        }
    };
    private final View.OnClickListener onNextClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            setOffsetMinutes(getOffsetMinutes() + WorldMapDialog.SEEK_STEPSIZE_5m);
        }
    };
    private final View.OnClickListener onPrevClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            setOffsetMinutes(getOffsetMinutes() - WorldMapDialog.SEEK_STEPSIZE_5m);
        }
    };
    private final View.OnClickListener onResetClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopMap(true);
        }
    };

    public void centerDialog()
    {
        setData(moonriseset.getDataAtCenter());
        showPositionAt(null);
        moonriseset.scrollToCenter();   // onScrollChanged triggers rest of reset sequence
        //moonphases.scrollToCenter();
        //moonapsis.scrollToCenter();
    }

    protected void toggleLunarNoon(Context context)
    {
        boolean value = AppSettings.loadShowLunarNoonPref(context);
        AppSettings.saveShowLunarNoonPref(context, !value);
        moonriseset.setShowLunarNoon(!value);
    }

    private final View.OnClickListener onMenuClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showOverflowMenu(getActivity(), v);
        }
    });

    /**
     * Overflow Menu
     */
    protected boolean showOverflowMenu(final Context context, View view)
    {
        PopupMenu menu = PopupMenuCompat.createMenu(context, view, R.menu.moonmenu, onOverflowMenuClick, null);
        updateOverflowMenu(context, menu);
        menu.show();
        return true;
    }
    private void updateOverflowMenu(Context context, PopupMenu popup)
    {
        Menu menu = popup.getMenu();
        MenuItem lunarNoonItem = menu.findItem(R.id.action_lunarnoon_show);
        if (lunarNoonItem != null) {
            lunarNoonItem.setChecked(AppSettings.loadShowLunarNoonPref(context));
        }

        MenuItem columnItem;
        switch (moonphases.numColumns())
        {
            case 2: columnItem = menu.findItem(R.id.action_phase_columns_2); break;
            case 3: columnItem = menu.findItem(R.id.action_phase_columns_3); break;
            case 4: default: columnItem = menu.findItem(R.id.action_phase_columns_4); break;
        }
        if (columnItem != null) {
            columnItem.setChecked(true);
        }
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

                case R.id.action_phase_columns_2:
                    saveMoonPhaseColumns(2);
                    return true;

                case R.id.action_phase_columns_3:
                    saveMoonPhaseColumns(3);
                    return true;

                case R.id.action_phase_columns_4:
                    saveMoonPhaseColumns(4);
                    return true;

                case R.id.action_show_controls:
                    showMediaPopup(getActivity(), text_dialogTimeOffset);
                    return true;

                case R.id.action_lunarnoon_show:
                    toggleLunarNoon(getContext());
                    return true;

                case R.id.action_help:
                    showHelp(getContext());
                    return true;

                default:
                    return false;
            }
        }
    });

    protected void saveMoonPhaseColumns(int numColumns)
    {
        AppSettings.saveMoonPhaseColumnsPref(getActivity(), numColumns);
        moonphases.setNumColumns(numColumns);
        moonphases.onSizeChanged(moonphases.getWidth(), moonphases.getHeight(), moonphases.getWidth(), moonphases.getHeight());
    }

    /**
     * MediaMenu
     */
    protected boolean showMediaMenu(final Context context, View view)
    {
        PopupMenu menu = PopupMenuCompat.createMenu(context, view, R.menu.moonmenu_media, onMediaMenuClick, null);
        updateMediaMenu(context, menu);
        menu.show();
        return true;
    }
    private void updateMediaMenu(Context context, PopupMenu popup)
    {
        Menu menu = popup.getMenu();
        MenuItem playItem = menu.findItem(R.id.action_play);
        if (playItem != null) {
            playItem.setVisible( !isPlaying() );
        }
        MenuItem pauseItem = menu.findItem(R.id.action_pause);
        if (pauseItem != null) {
            pauseItem.setVisible( isPlaying() );
        }
        MenuItem resetItem = menu.findItem(R.id.action_reset);
        if (resetItem != null) {
            resetItem.setEnabled(isOffset(arg_dateTime() != -1 ? arg_dateTime() : getNow()));
        }
    }
    private PopupMenu.OnMenuItemClickListener onMediaMenuClick = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.action_play:
                    playMap();
                    return true;

                case R.id.action_pause:
                    stopMap(false);
                    return true;

                case R.id.action_reset:
                    stopMap(true);
                    return true;

                default:
                    return false;
            }
        }
    };

    /**
     * MediaPopup
     */
    protected void showMediaPopup(@NonNull final Context context, @NonNull View v)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            PopupWindow popupWindow = new PopupWindow(createMediaPopupView(context), LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)));
            popupWindow.setOutsideTouchable(true);

            //int gravity = (Gravity.TOP | Gravity.START);
            //popupWindow.showAsDropDown((isCollapsed() ? v : text_dialogTitle), SuntimesUtils.dpToPixels(context, -8), SuntimesUtils.dpToPixels(context, 8), gravity);
            popupWindow.showAsDropDown(isCollapsed() && menuButton != null ? menuButton : text_dialogTimeOffset);
            //popupWindow.showAsDropDown(mediaAnchor != null ? mediaAnchor : text_dialogTimeOffset);
            popupWindow.showAsDropDown(menuButton);
        }
    }

    protected View createMediaPopupView(@NonNull final Context context)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater != null)
        {
            @SuppressLint("InflateParams")
            final View popupView = inflater.inflate(R.layout.layout_popup_mediacontrol, null);
            if (popupView != null)
            {
                ImageButton resetButton = (ImageButton) popupView.findViewById(R.id.media_reset);
                if (resetButton != null) {
                    resetButton.setOnClickListener(createMediaPopupListener(popupView, onResetClicked));
                    TooltipCompat.setTooltipText(resetButton, resetButton.getContentDescription());
                    ImageViewCompat.setImageTintList(resetButton, SuntimesUtils.colorStateList(warningColor, disabledColor, pressedColor));
                }
                ImageButton playButton = (ImageButton) popupView.findViewById(R.id.media_play);
                if (playButton != null) {
                    playButton.setOnClickListener(createMediaPopupListener(popupView, onPlayClicked));
                    ImageViewCompat.setImageTintList(playButton, SuntimesUtils.colorStateList(normalColor, disabledColor, pressedColor));
                }
                ImageButton pauseButton = (ImageButton) popupView.findViewById(R.id.media_pause);
                if (pauseButton != null) {
                    pauseButton.setOnClickListener(createMediaPopupListener(popupView, onPauseClicked));
                    ImageViewCompat.setImageTintList(pauseButton, SuntimesUtils.colorStateList(accentColor, disabledColor, pressedColor));
                }
                ImageButton nextButton = (ImageButton) popupView.findViewById(R.id.media_next);
                if (nextButton != null) {
                    nextButton.setOnClickListener(createMediaPopupListener(popupView, onNextClicked));
                    TooltipCompat.setTooltipText(nextButton, nextButton.getContentDescription());
                    ImageViewCompat.setImageTintList(nextButton, SuntimesUtils.colorStateList(normalColor, disabledColor, pressedColor));
                }
                ImageButton prevButton = (ImageButton) popupView.findViewById(R.id.media_prev);
                if (prevButton != null) {
                    prevButton.setOnClickListener(createMediaPopupListener(popupView, onPrevClicked));
                    TooltipCompat.setTooltipText(prevButton, prevButton.getContentDescription());
                    ImageViewCompat.setImageTintList(prevButton, SuntimesUtils.colorStateList(normalColor, disabledColor, pressedColor));
                }
            }
            updateMediaPopupView(popupView);
            return popupView;
        }
        return null;
    }
    private View.OnClickListener createMediaPopupListener(final View popupView, final View.OnClickListener listener) {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                listener.onClick(v);
                updateMediaPopupView(popupView);
            }
        };
    }
    protected void updateMediaPopupView(View popupView)
    {
        if (popupView != null)
        {
            boolean isPlaying = isPlaying();
            ImageButton resetButton = (ImageButton) popupView.findViewById(R.id.media_reset);
            if (resetButton != null) {
                resetButton.setEnabled(isOffset(arg_dateTime() != -1 ? arg_dateTime() : getNow()));
            }
            ImageButton playButton = (ImageButton) popupView.findViewById(R.id.media_play);
            if (playButton != null) {
                //playButton.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
                playButton.setVisibility(View.GONE);  // TODO: implement play update loop
            }
            ImageButton pauseButton = (ImageButton) popupView.findViewById(R.id.media_pause);
            if (pauseButton != null) {
                //pauseButton.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
                pauseButton.setVisibility(View.GONE);  // TODO: implement play update loop
            }
        }
    }

    /**
     * ContextMenu
     */
    protected boolean showContextMenu(final Context context, View view, MoonRiseSetView1.MoonRiseSetAdapter adapter, int position, String eventID)
    {
        SuntimesMoonData data = adapter.initData(context, position);
        if (data != null && eventID != null)
        {
            Calendar date = MoonRiseSetView1.MoonRiseSetEvent.getCalendarForEvent(data, eventID);
            if (date != null)
            {
                PopupMenu menu = PopupMenuCompat.createMenu(context, view, R.menu.moonriseset_context, onContextMenuClick, onMoonRiseSetContextMenuDismissed);
                updateContextMenu(context, menu, eventID, date.getTimeInMillis());
                moonriseset.lockScrolling();   // prevent the popupmenu from nudging the view
                menu.show();
                return true;
            }
        }
        return false;
    }

    protected boolean showContextMenu(final Context context, View view, MoonPhasesView1.PhaseAdapter adapter, int position, SuntimesCalculator.MoonPhase phase)
    {
        SuntimesMoonData1 data = adapter.initData(context, position);
        if (data != null)
        {
            Calendar date = data.moonPhaseCalendar(phase);
            if (date != null)
            {
                PopupMenu menu = PopupMenuCompat.createMenu(context, view, R.menu.moonphase_context, onContextMenuClick, onMoonPhaseContextMenuDismissed);
                updateContextMenu(context, menu, SolarEvents.valueOf(phase), date.getTimeInMillis());
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
            PopupMenu menu = PopupMenuCompat.createMenu(context, view, R.menu.moonapsis_context, onContextMenuClick, onMoonApsisContextMenuDismissed);
            updateContextMenu(context, menu, event.first.getTimeInMillis());
            moonapsis.lockScrolling();   // prevent the popupmenu from nudging the view
            menu.show();
            return true;
        }
        return false;
    }

    private void updateContextMenu(Context context, PopupMenu menu, final long datetime) {
        updateContextMenu(context, menu, (String) null, datetime);
    }

    private void updateContextMenu(Context context, PopupMenu menu, @Nullable SolarEvents event, final long datetime) {
        updateContextMenu(context, menu, (event != null ? event.name() : null), datetime);
    }

    private void updateContextMenu(Context context, PopupMenu menu, @Nullable String eventID, final long datetime)
    {
        Intent data = new Intent();
        data.putExtra(MenuAddon.EXTRA_SHOW_DATE, datetime);
        if (eventID != null) {
            data.putExtra("event", eventID);
        }
        updateContextMenu(context, menu, data);
    }

    private void updateContextMenu(Context context, PopupMenu menu, Intent data)
    {
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

            switch (item.getItemId())
            {
                case R.id.action_alarm:
                    if (dialogListener != null) {
                        SolarEvents event = (itemData != null && itemData.hasExtra("event") ? SolarEvents.valueOf(itemData.getStringExtra("event")) : null);
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

                case R.id.action_moon:
                    //moonriseset.setViewListener(null);
                    showPositionAt(itemTime);
                    //moonriseset.setViewListener(moonriseset_listener);
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

                case R.id.action_share:
                    shareItem(context, itemData);
                    return true;

                default:
                    return false;
            }
        }
    });

    protected void shareItem(Context context, Intent itemData)
    {
        String eventID = (itemData != null && itemData.hasExtra("event") ? itemData.getStringExtra("event") : null);
        long itemMillis = itemData != null ? itemData.getLongExtra(MenuAddon.EXTRA_SHOW_DATE, -1L) : -1L;
        if (itemMillis != -1L) {
            String displayString = (eventID != null ? SolarEvents.valueOf(eventID).getLongDisplayString() : null);
            boolean showSeconds = WidgetSettings.loadShowSecondsPref(context, 0);
            boolean showTime = WidgetSettings.loadShowTimeDatePref(context, 0);
            ShareUtils.shareItem(context, displayString, itemMillis, showTime, showSeconds);
        }
    }

    @SuppressLint("ResourceType")
    protected void showHelp(Context context)
    {
        int iconSize = (int) getResources().getDimension(R.dimen.helpIcon_size);
        int[] iconAttrs = { R.attr.moonriseColor, R.attr.moonsetColor, R.attr.moonnoonIcon, R.attr.moonnightIcon, R.attr.icActionShare };
        TypedArray typedArray = context.obtainStyledAttributes(iconAttrs);
        int moonriseColor = ContextCompat.getColor(context, typedArray.getResourceId(0, R.color.moonIcon_color_rising_dark));
        int moonsetColor = ContextCompat.getColor(context, typedArray.getResourceId(1, R.color.moonIcon_color_setting_dark));
        ImageSpan risingIcon = SuntimesUtils.createImageSpan(context, R.drawable.svg_sunrise, iconSize, iconSize, moonriseColor);
        ImageSpan settingIcon = SuntimesUtils.createImageSpan(context, R.drawable.svg_sunset, iconSize, iconSize, moonsetColor);
        ImageSpan noonIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(2, R.drawable.ic_moon_noon), iconSize, iconSize/2, moonriseColor);
        ImageSpan midnightIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(3, R.drawable.ic_moon_night), iconSize, iconSize/2, moonsetColor);
        ImageSpan shareIcon = SuntimesUtils.createImageSpan(context, typedArray.getResourceId(4, R.drawable.ic_action_share), iconSize, iconSize, 0);
        typedArray.recycle();

        SuntimesUtils.ImageSpanTag[] helpTags = {
                new SuntimesUtils.ImageSpanTag("[Icon Rising]", risingIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Setting]", settingIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Noon]", noonIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Midnight]", midnightIcon),
                new SuntimesUtils.ImageSpanTag("[Icon Share]", shareIcon),
        };
        String helpString = getString(R.string.help_general_moondialog);
        SpannableStringBuilder helpSpan = SuntimesUtils.createSpan(context, helpString, helpTags);

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.setContent(helpSpan);
        helpDialog.setShowNeutralButton(getString(R.string.configAction_onlineHelp));
        helpDialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(getActivity(), HELP_PATH_ID), DIALOGTAG_HELP);
        helpDialog.show(getChildFragmentManager(), DIALOGTAG_HELP);
    }

    private final PopupMenu.OnDismissListener onMoonRiseSetContextMenuDismissed = new PopupMenu.OnDismissListener() {
        @Override
        public void onDismiss(PopupMenu menu) {
            moonriseset.post(new Runnable() {
                @Override
                public void run() {                      // a submenu may be shown after the popup is dismissed
                    moonriseset.unlockScrolling();           // so defer unlockScrolling until after it is shown
                }
            });
        }
    };

    private final PopupMenu.OnDismissListener onMoonPhaseContextMenuDismissed = new PopupMenu.OnDismissListener() {
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

    private final PopupMenu.OnDismissListener onMoonApsisContextMenuDismissed = new PopupMenu.OnDismissListener() {
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

    protected void updateMediaButtons()
    {
        boolean isPlaying = isPlaying();
        if (playButton != null && pauseButton != null) {
            pauseButton.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
            playButton.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
        }
    }

    public boolean isPlaying() {
        return getArguments().getBoolean(ARG_PLAYING);
    }
    public int getOffsetMinutes() {
        return getArguments().getInt(ARG_PLAY_OFFSET);
    }
    public void setOffsetMinutes(int value) {
        getArguments().putInt(ARG_PLAY_OFFSET, value);
        updateViews();
    }

    public void playMap()
    {
        stopUpdateTask();
        getArguments().putBoolean(ARG_PLAYING, true);
        updateMediaButtons();
        startUpdateTask();    // TODO: 'play' update task
    }

    public void togglePlay()
    {
        if (isPlaying()) {
            stopMap(false);
        } else {
            playMap();
        }
    }

    public void stopMap(boolean reset)
    {
        stopUpdateTask();
        getArguments().putBoolean(ARG_PLAYING, false);
        if (reset) {
            getArguments().putInt(ARG_PLAY_OFFSET, 0);
        }
        updateMediaButtons();
        updateViews(false);
        startUpdateTask();
    }

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
                updateTimeText();
                currentphase.updatePosition(getDialogCalendar());
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
                currentphase.updateIllumination(getContext(), getDialogCalendar());
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

    public void showColorDialog(Context context)
    {
        boolean isNightMode = context.getResources().getBoolean(R.bool.is_nightmode);
        ColorValuesSheetDialog dialog = new ColorValuesSheetDialog();
        dialog.setAppWidgetID((isNightMode ? 1 : 0));
        dialog.setColorTag(AppColorValues.TAG_APPCOLORS);
        dialog.setColorCollection(new AppColorValuesCollection<>(context));
        dialog.setDialogListener(colorDialogListener);
        dialog.setFilter(new MoonRiseSetColorValues().getColorKeys(),
                         new MoonPhasesColorValues().getColorKeys(),
                         new MoonApsisColorValues().getColorKeys());
        dialog.show(getChildFragmentManager(), DIALOGTAG_COLORS);
    }

    private final ColorValuesSheetDialog.DialogListener colorDialogListener = new ColorValuesSheetDialog.DialogListener()
    {
        @Override
        public void onColorValuesSelected(ColorValues values)
        {
            currentphase.setColors(getActivity(), values);
            moonriseset.setColors(getActivity(), values);
            moonapsis.setColors(getActivity(), values);
            moonphases.setColors(getActivity(), values);

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
        public void onColorsModified(ColorValues values) {}
    }
}
