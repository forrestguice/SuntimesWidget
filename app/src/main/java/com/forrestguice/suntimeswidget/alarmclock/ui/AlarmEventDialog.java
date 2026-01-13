/**
    Copyright (C) 2014-2021 Forrest Guice
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
import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.text.SpannableString;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.support.widget.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.Toast;

import com.forrestguice.suntimeswidget.alarmclock.AlarmAddon;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEvent;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract;
import com.forrestguice.suntimeswidget.alarmclock.AlarmNotifications;
import com.forrestguice.suntimeswidget.alarmclock.AlarmSettings;
import com.forrestguice.suntimeswidget.calculator.SuntimesEquinoxSolsticeDataset;
import com.forrestguice.suntimeswidget.calculator.core.Location;
import com.forrestguice.suntimeswidget.calculator.core.SuntimesCalculator;

import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.calculator.SuntimesMoonData;
import com.forrestguice.suntimeswidget.calculator.SuntimesRiseSetDataset;
import com.forrestguice.suntimeswidget.events.EventListActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SolarEvents;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.views.TooltipCompat;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.util.android.AndroidResources;

import java.util.Calendar;
import java.util.List;

public class AlarmEventDialog extends BottomSheetDialogBase
{
    public static final int REQUEST_ADDON_ALARMPICKER = 3000;
    public static final int REQUEST_EVENTALIAS = 4000;

    public static final String KEY_ALARM_TYPE = "alarmdialog_alarmtype";
    public static final AlarmClockItem.AlarmType DEF_ALARM_TYPE = AlarmClockItem.AlarmType.ALARM;

    public static final String KEY_DIALOGTITLE = "alarmdialog_title";
    public static final String KEY_DIALOGSHOWFRAME = "alarmdialog_showframe";
    public static final String KEY_DIALOGSHOWDESC = "alarmdialog_showdesc";

    public static final String PREF_KEY_ALARM_LASTCHOICE = "alarmdialog_lastchoice1";
    public static final String PREF_DEF_ALARM_LASTCHOICE = SolarEvents.SUNRISE.name();

    protected static final SuntimesUtils utils = new SuntimesUtils();

    /**
     * The appWidgetID used when saving/loading choice to prefs (main app uses 0).
     */
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    public int getAppWidgetId() { return appWidgetId; }
    public void setAppWidgetId(int value) { appWidgetId = value; }

    private AlarmClockItem.AlarmType type = DEF_ALARM_TYPE;
    public AlarmClockItem.AlarmType getType() {
        return type;
    }
    public void setType(AlarmClockItem.AlarmType type)
    {
        this.type = type;
        if (getContext() != null) {
            updateViews(getContext());
        }
    }

    private String dialogTitle = null;
    public void setDialogTitle( String title ) {
        dialogTitle = title;
    }

    private boolean showFrame = true;
    public void setDialogShowFrame(boolean value) {
        showFrame = value;
    }

    private boolean showDesc = true;
    public void setDialogShowDesc(boolean value) {
        showDesc = value;
    }

    private boolean useAppLocation = false;
    public void setUseAppLocation(boolean value)
    {
        useAppLocation = value;
        if (isAdded() && getContext() != null) {
            updateViews(getContext());
        }
    }

    /**
     * The supporting datasets.
     */
    private SuntimesRiseSetDataset dataset;
    private SuntimesMoonData moondata;
    private SuntimesEquinoxSolsticeDataset equinoxdata;
    public SuntimesRiseSetDataset getData() { return dataset; }
    public SuntimesMoonData getMoonData() { return moondata; }
    public SuntimesEquinoxSolsticeDataset getEquinoxData() { return equinoxdata; }

    public void setData(Context context, SuntimesRiseSetDataset dataset, SuntimesMoonData moondata, SuntimesEquinoxSolsticeDataset equinoxdata)
    {
        this.dataset = dataset;
        this.moondata = moondata;
        this.equinoxdata = equinoxdata;
        updateAdapter(context);
        setChoice(choice);
    }

    public void updateAdapter(Context context)
    {
        Location location = moondata.location();
        adapter = AlarmEvent.createAdapter(context, WidgetSettings.loadLocalizeHemispherePref(context, 0) && moondata != null && location != null && location.getLatitudeAsDouble() < 0);
        if (dataset != null)
        {
            boolean supportsGoldBlue = dataset.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_GOLDBLUE);
            if (!supportsGoldBlue)
            {
                adapter.removeItem(SolarEvents.MORNING_BLUE8);
                adapter.removeItem(SolarEvents.MORNING_BLUE4);
                adapter.removeItem(SolarEvents.EVENING_BLUE4);
                adapter.removeItem(SolarEvents.EVENING_BLUE8);
                adapter.removeItem(SolarEvents.MORNING_GOLDEN);
                adapter.removeItem(SolarEvents.EVENING_GOLDEN);
            }

            boolean supportsMoon = moondata != null && moondata.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_MOON);
            if (!supportsMoon)
            {
                adapter.removeItem(SolarEvents.MOONRISE);
                adapter.removeItem(SolarEvents.MOONSET);
                adapter.removeItem(SolarEvents.MOONNOON);
                adapter.removeItem(SolarEvents.MOONNIGHT);
                adapter.removeItem(SolarEvents.NEWMOON);
                adapter.removeItem(SolarEvents.FIRSTQUARTER);
                adapter.removeItem(SolarEvents.FULLMOON);
                adapter.removeItem(SolarEvents.THIRDQUARTER);
            }

            boolean supportsSolstice = equinoxdata != null && equinoxdata.dataEquinoxSpring.calculatorMode().hasRequestedFeature(SuntimesCalculator.FEATURE_SOLSTICE);
            if (!supportsSolstice)
            {
                adapter.removeItem(SolarEvents.EQUINOX_SPRING);
                adapter.removeItem(SolarEvents.SOLSTICE_SUMMER);
                adapter.removeItem(SolarEvents.EQUINOX_AUTUMNAL);
                adapter.removeItem(SolarEvents.SOLSTICE_WINTER);
            }
        }

        if (spinner_scheduleMode != null) {
            spinner_scheduleMode.setAdapter(adapter);
        }
    }

    private AlarmEvent.AlarmEventAdapter adapter = null;

    /**
     * The user's alarm choice.
     */
    private String choice = null;
    public void setChoice( String choice )
    {
        if (choice != null)
        {
            this.choice = choice;
            //Log.d("DEBUG", "setChoice: " + choice);

            if (adapter != null)
            {
                Context context = getContext();
                if (context != null && !adapter.containsItem(choice))
                {
                    AlarmEvent.AlarmEventItem item = new AlarmEvent.AlarmEventItem(choice, context.getContentResolver());
                    if (item.isResolved()) {
                        adapter.insert(item, 0);
                        //Log.d("DEBUG", "inserting event into adapter: " + choice);
                    } else Log.w(getClass().getSimpleName(), "omitting unresolved event from adapter: " + choice);
                }

                if (spinner_scheduleMode != null)
                {
                    boolean found = false;
                    for (int i = 0; i < adapter.getCount(); i++)
                    {
                        AlarmEvent.AlarmEventItem item = adapter.getItem(i);
                        String eventID = item != null ? item.getEventID() : null;
                        if (eventID != null && choice.equals(item.getEventID()))
                        {
                            found = true;
                            spinner_scheduleMode.setSelection(i);
                            //Log.d("DEBUG", "setting spinner to position: " + i);
                            break;
                        }
                    }

                    //if (!found)
                    //{
                        // TODO: fallback action when the choice isn't in the adapter because it wasn't added for some reason, or it failed to resolve.. maybe display a message
                        // for now do nothing.. the spinner won't match the custom selection (instead displaying an arbitrary item), and will eventually overwrite it.
                    //}
                }
            }
        }
    }
    public String getChoice() { return choice; }

    public Location getLocation()
    {
        if (dataset != null) {
            return dataset.location();
        } else return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(requireContext(), AppSettings.loadTheme(requireContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_schedalarm, parent, false);

        if (savedState != null) {
            loadSettings(savedState);
        } else if (choice == null) {
            loadSettings(requireContext());
        }

        initViews(requireContext(), dialogContent);
        updateViews(requireContext());

        return dialogContent;
    }

    /**
     * @param savedInstanceState a Bundle containing dialog state
     * @return an AlarmDialog ready to be shown
     */
    @SuppressWarnings({"RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    /**
     * @param outState a Bundle used to save state
     */
    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        //Log.d("DEBUG", "AlarmDialog onSaveInstanceState");
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     *
     */
    private Spinner spinner_scheduleMode;
    private TextView txt_note;
    private ImageView icon_note;
    private TextView txt_location;
    private TextView txt_modeLabel;
    private TextView txt_title;
    private ImageButton btn_more;

    protected void initViews( final Context context, View dialogContent )
    {
        initColors(context);
        SuntimesUtils.initDisplayStrings(context);
        WidgetSettings.initDisplayStrings(context);
        SolarEvents.initDisplayStrings(AndroidResources.wrap(context));

        icon_note = (ImageView) dialogContent.findViewById(R.id.appwidget_schedalarm_note_icon);
        icon_note.setVisibility(View.GONE);

        txt_note = (TextView) dialogContent.findViewById(R.id.appwidget_schedalarm_note);
        txt_note.setText("");

        txt_location = (TextView) dialogContent.findViewById(R.id.appwidget_schedalarm_location);
        if (txt_location != null)
        {
            txt_location.setText("");
            txt_location.setOnClickListener(onLocationClicked);
            updateLocationIcon(context, txt_location, useAppLocation);
        }

        alarmPickers = AlarmAddon.queryEventPickers(context);
        btn_more = (ImageButton) dialogContent.findViewById(R.id.appwidget_schedalarm_more);
        if (btn_more != null)
        {
            TooltipCompat.setTooltipText(btn_more, btn_more.getContentDescription());
            btn_more.setOnClickListener(onMoreButtonClicked);
            btn_more.setVisibility(View.VISIBLE);
        }

        spinner_scheduleMode = (Spinner) dialogContent.findViewById(R.id.appwidget_schedalarm_mode);
        if (adapter != null) {
            spinner_scheduleMode.setAdapter(adapter);
            setChoice(choice);
        }

        txt_modeLabel = (TextView) dialogContent.findViewById(R.id.appwidget_schedalarm_mode_label);

        spinner_scheduleMode.setOnItemSelectedListener(
                new Spinner.OnItemSelectedListener()
                {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        Context context = getContext();
                        if (context == null) {
                            return;
                        }

                        updateLocationLabel(context, txt_location, dataset.location());

                        AlarmEvent.AlarmEventItem item = (AlarmEvent.AlarmEventItem)spinner_scheduleMode.getSelectedItem();
                        choice = item.getEventID();

                        if (choice == null) {
                            //Log.d("DEBUG", "null selection");
                            return;
                        }

                        //Log.d("DEBUG", "onItemSelected: " + choice);
                        if (listener != null) {
                            listener.onChanged(AlarmEventDialog.this);
                        }

                        if (showDesc)
                        {
                            String displayString = item.getTitle();
                            Calendar now0 = dataset.nowThen(dataset.calendar());
                            Calendar alarmCalendar = getCalendarForAlarmChoice(context, choice, now0);
                            if (alarmCalendar != null)
                            {
                                Calendar now = dataset.now();
                                if (now.after(alarmCalendar))      // getCalendarForAlarmChoice should return a datetime in the future..
                                {                                      // but supposing it doesn't (due to user defined date) then adjust alarmTime to be today
                                    alarmCalendar = SuntimesData.nowThen(alarmCalendar, now);  // and if that is also past, adjust alarmTime to be tomorrow
                                    if (now.after(alarmCalendar))
                                    {
                                        Calendar tomorrow = (Calendar)now.clone();
                                        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                                        alarmCalendar = SuntimesData.nowThen(alarmCalendar, tomorrow);
                                    }
                                }

                                String timeString =" " + utils.timeDeltaDisplayString(now.getTime(), alarmCalendar.getTime()).getValue() + " ";
                                String noteString = context.getString(R.string.schedalarm_dialog_note, timeString);
                                txt_note.setText(SuntimesUtils.createBoldColorSpan(null, noteString, timeString, color_textTimeDelta));
                                icon_note.setVisibility(View.GONE);

                                String modeDescription = context.getString((type == AlarmClockItem.AlarmType.ALARM) ? R.string.configLabel_schedalarm_mode : R.string.configLabel_schednotify_mode);
                                SuntimesUtils.announceForAccessibility(txt_note,  modeDescription + " " + displayString + ", " + txt_note.getText());   // TODO: does AlarmCreateDialog also announce?

                            } else {
                                String timeString = " " + displayString + " ";
                                String noteString = context.getString(R.string.schedalarm_dialog_note2, timeString);
                                txt_note.setText(SuntimesUtils.createBoldColorSpan(null, noteString, timeString, color_textTimeDelta));
                                icon_note.setVisibility(View.VISIBLE);
                                SuntimesUtils.announceForAccessibility(txt_note, displayString + ", " + txt_note.getText());
                            }
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent)
                    {
                    }
                }
        );

        txt_title = (TextView) dialogContent.findViewById(R.id.dialog_title);

        Button btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
        btn_cancel.setOnClickListener(onDialogCancelClick);

        Button btn_accept = (Button) dialogContent.findViewById(R.id.dialog_button_accept);
        btn_accept.setOnClickListener(onDialogAcceptClick);

        if (!showFrame)
        {
            View header = dialogContent.findViewById(R.id.dialog_frame_header);
            header.setVisibility(View.GONE);

            View footer = dialogContent.findViewById(R.id.dialog_frame_footer);
            footer.setVisibility(View.GONE);
        }

        if (!showDesc)
        {
            View layout_note = dialogContent.findViewById(R.id.appwidget_schedalarm_note_layout);
            if (layout_note != null) {
                layout_note.setVisibility(View.GONE);
            }
            txt_modeLabel.setVisibility(View.GONE);
        }
    }

    private final View.OnClickListener onMoreButtonClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (context != null) {
                PopupMenuCompat.createMenu(context, v, onMoreMenuClick).show();
            }
        }
    });
    private List<AlarmAddon.EventPickerInfo> alarmPickers = null;

    private final PopupMenuCompat.PopupMenuListener onMoreMenuClick = new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
    {
        @Override
        public void onUpdateMenu(Context context, Menu menu)
        {
            if (alarmPickers == null) {
                alarmPickers = AlarmAddon.queryEventPickers(context);
            }

            int[] attr = { R.attr.icActionExtension, R.attr.icActionEvents };
            TypedArray typedArray = context.obtainStyledAttributes(attr);
            Drawable icon = ContextCompat.getDrawable(context, typedArray.getResourceId(0, R.drawable.ic_action_extension));
            @SuppressLint("ResourceType")
            Drawable icon1 = ContextCompat.getDrawable(context, typedArray.getResourceId(1, R.drawable.ic_action_extension));    // TODO: default icon
            typedArray.recycle();

            for (int i=0; i<alarmPickers.size(); i++)
            {
                MenuItem item = menu.add(0, i, i+1, alarmPickers.get(i).getTitle());
                item.setIcon(icon);
            }

            MenuItem item0 = menu.add(0, -1, 0, context.getString(R.string.configAction_manageEvents));
            item0.setIcon(icon1);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            Context context = getContext();
            if (context == null || alarmPickers == null) {
                return false;
            }

            int i = item.getItemId();
            if (i == -1)
            {
                Intent intent = new Intent(context, EventListActivity.class);
                intent.putExtra(EventListActivity.EXTRA_EXPANDED, true);
                intent.putExtra(EventListActivity.EXTRA_LOCATION, getLocation());
                startActivityForResult(intent, REQUEST_EVENTALIAS);
                return true;

            } else if (i >= 0 && i < alarmPickers.size()) {
                AlarmAddon.EventPickerInfo picker = alarmPickers.get(item.getItemId());
                Intent intent = picker.getIntent(getLocation());
                intent.putExtra(AlarmEventContract.EXTRA_ALARM_EVENT, getChoice());
                startActivityForResult(intent, REQUEST_ADDON_ALARMPICKER);
                return true;

            } else {
                Log.w(getClass().getSimpleName(), "Invalid AlarmPicker index; ignoring selection..");
                return false;
            }
        }
    });

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_EVENTALIAS:
                if (resultCode == Activity.RESULT_OK)
                {
                    if (data != null)
                    {
                        boolean adapterModified = data.getBooleanExtra(EventListActivity.ADAPTER_MODIFIED, false);
                        if (adapterModified && getContext() != null) {
                            updateAdapter(getContext());
                        }

                        String eventUri = data.getStringExtra(EventListActivity.SELECTED_EVENTURI);
                        if (eventUri != null) {
                            setChoice(eventUri); // + AlarmEventProvider.ElevationEvent.SUFFIX_RISING);
                        }
                    }
                }
                break;

            case REQUEST_ADDON_ALARMPICKER:
                if (resultCode == Activity.RESULT_OK)
                {
                    if (data != null)
                    {
                        Uri uri = data.getData();
                        String reference = data.getStringExtra(AlarmEventContract.COLUMN_CONFIG_PROVIDER);
                        String name = data.getStringExtra(AlarmEventContract.COLUMN_EVENT_NAME);
                        //String title = data.getStringExtra(AlarmAddon.COLUMN_ALARM_TITLE);
                        //String summary = data.getStringExtra(AlarmAddon.COLUMN_ALARM_SUMMARY);
                        //Toast.makeText(getContext(), "picker result: \n" + title + " \n" + summary + "\n" + name + "\n" + reference + "\n" + uri, Toast.LENGTH_LONG).show();

                        if ((reference != null && name != null)) {
                            selectAddonAlarm(reference, name);

                        } else if (uri != null) {
                            selectAddonAlarm(uri);

                        } else {
                            Log.w(getClass().getSimpleName(), "onActivityResult: missing addon alarm data; ignoring result");
                        }
                    } else {
                        Log.w(getClass().getSimpleName(), "onActivityResult: missing addon alarm data; ignoring result");
                    }
                }
                break;
        }
    }

    protected void selectAddonAlarm(@NonNull Uri uri)
    {
        String reference = uri.getAuthority();
        String alarmName = uri.getLastPathSegment();
        if (reference != null && alarmName != null) {
            selectAddonAlarm(reference, alarmName);
        }
    }
    protected void selectAddonAlarm(@NonNull String reference, @NonNull String name)
    {
        Context context = getContext();
        ContentResolver resolver = context != null ? context.getContentResolver() : null;
        if (resolver != null)
        {
            if (AlarmAddon.checkUriPermission(context, EventUri.getEventInfoUri(reference, name)))
            {
                AlarmEvent.AlarmEventItem item = new AlarmEvent.AlarmEventItem(reference, name, resolver);
                if (item.isResolved())
                {
                    setChoice(item.getEventID());
                    if (listener != null) {
                        listener.onChanged(this);
                    }
                } else {
                    Log.w(getClass().getSimpleName(), "selectAddonAlarm: permission denied! " + reference);
                    Toast.makeText(context, context.getString(R.string.schedalarm_dialog_error2), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, context.getString(R.string.schedalarm_dialog_error2), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updateViews(Context context)
    {
        if (txt_title != null)
        {
            String titleString = (dialogTitle != null) ? dialogTitle : context.getString(R.string.configAction_setAlarm);
            txt_title.setText(titleString);
        }

        if (txt_modeLabel != null) {
            txt_modeLabel.setText(getString(type == AlarmClockItem.AlarmType.ALARM ? R.string.configLabel_schedalarm_mode : R.string.configLabel_schednotify_mode));
        }

        updateLocationIcon(context, txt_location, useAppLocation);
    }

    private int color_textTimeDelta;
    private void initColors(Context context)
    {
        int[] colorAttrs = { android.R.attr.textColorPrimary };
        TypedArray typedArray = context.obtainStyledAttributes(colorAttrs);
        int def = Color.WHITE;

        color_textTimeDelta = ContextCompat.getColor(context, typedArray.getResourceId(0, def));

        typedArray.recycle();
    }

    protected void loadSettings(Context context)
    {
        loadSettings(context, false);
    }
    protected void loadSettings(Context context, boolean overwriteCurrent)
    {
        if (overwriteCurrent || choice == null)
        {
            SharedPreferences prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0);
            choice = prefs.getString(PREF_KEY_ALARM_LASTCHOICE, PREF_DEF_ALARM_LASTCHOICE);
        }
        setChoice(choice);
    }
    protected void loadSettings(Bundle bundle)
    {
        dialogTitle = bundle.getString(KEY_DIALOGTITLE);
        showFrame = bundle.getBoolean(KEY_DIALOGSHOWFRAME);
        showDesc = bundle.getBoolean(KEY_DIALOGSHOWDESC);

        choice = bundle.getString(PREF_KEY_ALARM_LASTCHOICE);
        if (choice == null) {
            choice = PREF_DEF_ALARM_LASTCHOICE;
        }
        setChoice(choice);

        type = (AlarmClockItem.AlarmType) bundle.getSerializable(KEY_ALARM_TYPE);
        if (type == null) {
            type = DEF_ALARM_TYPE;
        }
    }

    /**
     * Save alarm choice to prefs.
     * @param context a context used to access shared prefs
     */
    protected void saveSettings(Context context)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetSettings.PREFS_WIDGET, 0).edit();
        prefs.putString(PREF_KEY_ALARM_LASTCHOICE, choice);
        prefs.apply();
    }

    /**
     * Save alarm choice to bundle.
     * @param bundle state persisted to this bundle
     */
    protected void saveSettings(Bundle bundle)
    {
        bundle.putString(KEY_DIALOGTITLE, dialogTitle);
        bundle.putBoolean(KEY_DIALOGSHOWFRAME, showFrame);
        bundle.putBoolean(KEY_DIALOGSHOWDESC, showDesc);
        bundle.putSerializable(KEY_ALARM_TYPE, type);
        bundle.putSerializable(PREF_KEY_ALARM_LASTCHOICE, choice);
    }

    /**
     * DialogListener
     */
    public interface DialogListener
    {
        void onChanged(AlarmEventDialog dialog);
        void onAccepted(AlarmEventDialog dialog);
        void onCanceled(AlarmEventDialog dialog);
        void onLocationClick(AlarmEventDialog dialog, View v);
    }

    private DialogListener listener = null;
    public void setDialogListener(DialogListener listener) {
        this.listener = listener;
    }

    /**
     * Dialog accepted listener.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener )
    {
        onAccepted = listener;
    }

    /**
     * Dialog cancelled listener.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener )
    {
        onCanceled = listener;
    }

    public Calendar getCalendarForAlarmChoice( @NonNull Context context, String choice, Calendar now )
    {
        AlarmClockItem item = createAlarmItem(context);
        boolean isSchedulable = AlarmNotifications.updateAlarmTime(context, item);
        return (isSchedulable) ? item.getCalendar() : null;
    }

    protected AlarmClockItem createAlarmItem(@NonNull Context context) {
        return AlarmListDialog.createAlarm(context, AlarmClockItem.AlarmType.ALARM, "", getChoice(), getLocation(), -1L, -1, -1, null, AlarmSettings.loadPrefVibrateDefault(context), AlarmSettings.getDefaultRingtoneUri(context, AlarmClockItem.AlarmType.ALARM), AlarmSettings.getDefaultRingtoneName(context, AlarmClockItem.AlarmType.ALARM), AlarmRepeatDialog.PREF_DEF_ALARM_REPEATDAYS);
    }

    private final View.OnClickListener onLocationClicked = new ViewUtils.ThrottledClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onLocationClick(AlarmEventDialog.this, v);
            }
        }
    });

    public static boolean updateLocationLabel(Context context, TextView text_location, Location location)
    {
        if (text_location != null)
        {
            if (location != null)
            {
                String coordString = context.getString(R.string.location_format_latlon, location.getLatitude(), location.getLongitude());
                String labelString = location.getLabel();
                String displayString = labelString + "\n" + coordString;
                SpannableString displayText = SuntimesUtils.createBoldSpan(null, displayString, labelString);
                displayText = SuntimesUtils.createRelativeSpan(displayText, displayString, coordString, 0.75f);
                text_location.setText(displayText);
                return true;

            } else {
                text_location.setText("");
                return false;
            }
        } else return false;
    }

    public static void updateLocationIcon(Context context, TextView text_location, boolean useAppLocation)
    {
        if (context != null && text_location != null)
        {
            int[] attr = { R.attr.icActionPlace, R.attr.icActionHome };
            TypedArray typedArray = context.obtainStyledAttributes(attr);
            Drawable placeIcon = ContextCompat.getDrawable(context, typedArray.getResourceId(0, R.drawable.ic_action_place));
            @SuppressLint("ResourceType")
            Drawable homeIcon = ContextCompat.getDrawable(context, typedArray.getResourceId(1, R.drawable.ic_action_home));
            typedArray.recycle();

            text_location.setCompoundDrawablesWithIntrinsicBounds(useAppLocation ? homeIcon : placeIcon, null, null, null);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    @Override
    protected boolean getBottomSheetBehavior_skipCollapsed() {
        return true;
    }
    @Override
    protected boolean getBottomSheetBehavior_hideable() {
        return true;
    }

    private final DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog) {
            // EMPTY; placeholder
        }
    };

    private final View.OnClickListener onDialogCancelClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            dismiss();
            if (onCanceled != null) {
                onCanceled.onClick(getDialog(), 0);
            }
            if (listener != null) {
                listener.onCanceled(AlarmEventDialog.this);
            }
        }
    };

    private final View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (context != null)
            {
                saveSettings(context);
                dismiss();
                if (onAccepted != null) {
                    onAccepted.onClick(getDialog(), 0);
                }
                if (listener != null) {
                    listener.onAccepted(AlarmEventDialog.this);
                }
            }
        }
    };

}