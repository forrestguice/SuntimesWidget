/**
    Copyright (C) 2022-2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.events;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.alarmclock.AlarmAddon;
import com.forrestguice.suntimeswidget.alarmclock.AlarmEventProvider;
import com.forrestguice.suntimeswidget.settings.EditBottomSheetDialog;
import com.forrestguice.suntimeswidget.settings.TimeOffsetPickerDialog;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.suntimeswidget.settings.colors.ColorChangeListener;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooser;
import com.forrestguice.suntimeswidget.settings.colors.ColorChooserView;
import com.forrestguice.suntimeswidget.views.Toast;

import static com.forrestguice.suntimeswidget.alarmclock.AlarmEventContract.AUTHORITY;

public class EditEventDialog extends EditBottomSheetDialog
{
    public static final String ARG_DIALOGMODE = "dialogMode";
    public static final String ARG_MODIFIED = "isModified";

    private static final String DIALOGTAG_OFFSET = "eventoffset";

    public static SuntimesUtils utils = new SuntimesUtils();
    protected WidgetSettings.LengthUnit units = WidgetSettings.LengthUnit.METRIC;

    public EditEventDialog()
    {
        super();
        Bundle args = new Bundle();
        args.putInt(ARG_DIALOGMODE, DIALOG_MODE_ADD);
        args.putBoolean(ARG_MODIFIED, false);
        setArguments(args);
    }

    @Override
    protected int getLayoutID()
    {
        switch (type)
        {
            //case SHADOWLENGTH:
            //    return R.layout.layout_dialog_event_edit;    // TODO: layout for shadow length?

            case SUN_ELEVATION:
            default:
                return R.layout.layout_dialog_event_edit;
        }
    }

    /* Dialog Mode */
    public static final int DIALOG_MODE_ADD = 0;
    public static final int DIALOG_MODE_EDIT = 1;

    public int dialogMode() {
        return getArguments().getInt(ARG_DIALOGMODE);
    }
    public void setDialogMode(int mode) {
        getArguments().putInt(ARG_DIALOGMODE, mode);
    }

    /* isModified */
    public boolean isModified() {
        return getArguments().getBoolean(ARG_MODIFIED);
    }
    public void setIsModified(boolean modified) {
        getArguments().putBoolean(ARG_MODIFIED, modified);
    }

    /* EventType */
    protected AlarmEventProvider.EventType type = EventSettings.PREF_DEF_EVENT_TYPE;
    public AlarmEventProvider.EventType getEventType() {
        return type;
    }

    /* Event ID */
    protected String eventID = null;
    public String getEventID() {
        return (edit_eventID != null ? edit_eventID.getText().toString() : eventID);
    }
    public void setEventID(String id) {
        eventID = id;
        if (edit_eventID != null) {
            edit_eventID.setText(eventID);
        }
    }

    /* Event Label */
    protected String label = null;
    public String getEventLabel() {
        return (edit_label != null ? edit_label.getText().toString() : label);
    }
    public void setEventLabel(String value) {
        label = value;
        if (edit_label != null) {
            edit_label.setText(label != null ? label : eventID);
        }
    }

    /* Event Color */
    protected Integer color = EventSettings.PREF_DEF_EVENT_COLOR;
    public Integer getEventColor() {
        return (choose_color != null ? choose_color.getColor() : color);
    }
    public void setEventColor(Integer value) {
        color = value;
        if (choose_color != null) {
            choose_color.setColor(color);
        }
    }

    /* Event Uri */
    protected String uri = null;
    public String getEventUri() {
        return uri;
    }
    public void setEventUri(String value) {
        uri = value;
        if (edit_uri != null) {
            edit_uri.setText(uri);
        }
    }

    protected String uri1 = null;
    public String getEventUri1() {
        return uri1;
    }
    public void setEventUri1(String value) {
        uri1 = value;
        if (edit_uri1 != null) {
            edit_uri1.setText(uri1);
        }
    }

    /* Event Offset */
    protected void setOffset(int millis) {
        getArguments().putInt("offset", millis);
    }
    protected int getOffset() {
        return getArguments().getInt("offset", 0);
    }

    /* isShown */
    protected Boolean shown = false;
    public boolean getEventIsShown() {
        return (check_shown != null ? check_shown.isChecked() : shown);
    }
    public void setEventIsShown(boolean value)
    {
        shown = value;
        if (check_shown != null) {
            check_shown.setChecked(shown);
        }
    }

    /* Event */
    public EventSettings.EventAlias getEvent() {
        return new EventSettings.EventAlias(type, getEventID(), getEventLabel(), getEventColor(), getEventUri(), getEventIsShown());
    }
    public void setEvent(EventSettings.EventAlias event)
    {
        type = event.getType();
        setEventID(event.getID());
        setEventLabel(event.getLabel());
        setEventColor(event.getColor());
        setEventUri(event.getUri());
        setEventUri1(event.getAliasUri());
        setEventIsShown(event.isShown());
        updateViews(getActivity(), type);
    }
    public void setType(AlarmEventProvider.EventType value) {
        type = value;
    }

    protected TextView text_label, text_offset;
    protected EditText edit_eventID, edit_label, edit_uri, edit_uri1;
    protected ColorChooser choose_color;
    protected CheckBox check_shown;

    /**
     * setAngle
     */
    @SuppressLint("SetTextI18n")
    protected void setAngle(double value ) {
        angle = value;
        if (edit_angle != null) {
            edit_angle.setText(Double.toString(angle));
        }
    }
    @Nullable
    public Double getAngle()
    {
        if (edit_angle != null)
        {
            try {
                return Double.parseDouble(edit_angle.getText().toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    private double angle = 0;
    protected EditText edit_angle = null;
    protected View layout_angle = null;

    /**
     * setObjHeight
     */
    protected void setObjHeightMeters(double value)
    {
        objHeight = value;

        Context context = getActivity();
        if (edit_objHeight != null && context != null) {
            edit_objHeight.setText(SuntimesUtils.formatAsHeight(getActivity(), objHeight, units, 2, true).getValue());
        }
    }
    @Nullable
    public Double getObjHeightMeters()
    {
        Context context = getActivity();
        if (edit_objHeight != null && context != null)
        {
            try {
                double height = Double.parseDouble(edit_objHeight.getText().toString());
                return (units == WidgetSettings.LengthUnit.METRIC ? height : WidgetSettings.LengthUnit.feetToMeters(height));

            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * setShadowLength
     */
    protected void setShadowLengthMeters(double value)
    {
        shadowLength = value;

        Context context = getActivity();
        if (edit_shadowLength != null && context != null) {
            edit_shadowLength.setText(SuntimesUtils.formatAsHeight(getActivity(), shadowLength, units, 2, true).getValue());
        }
    }
    @Nullable
    public Double getShadowLengthMeters()
    {
        Context context = getActivity();
        if (edit_shadowLength != null && context != null)
        {
            try {
                double length = Double.parseDouble(edit_shadowLength.getText().toString());
                return (units == WidgetSettings.LengthUnit.METRIC ? length : WidgetSettings.LengthUnit.feetToMeters(length));

            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private double shadowLength = 0;
    private double objHeight = 0;
    protected EditText edit_shadowLength = null, edit_objHeight = null;
    protected View layout_shadowLength = null, layout_objHeight = null;
    protected TextView text_units_shadowLength = null, text_units_objHeight = null;

    /**
     * onCreateDialog
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = new BottomSheetDialog(getContext(), getTheme()) {
            @Override
            public void onBackPressed() {
                confirmDiscardChanges(getActivity());
            }
        };
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    protected void initViews(Context context, View dialogContent, @Nullable Bundle savedState)
    {
        SuntimesUtils.initDisplayStrings(context);
        units = WidgetSettings.loadLengthUnitsPref(context, 0);

        edit_eventID = (EditText) dialogContent.findViewById(R.id.edit_event_id);
        if (edit_eventID != null)
        {
            //edit_eventID.setAdapter(adapter);
            //edit_eventID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //    @Override
            //    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //        setEventID((String)parent.getItemAtPosition(position));
            //    }
            //});
            edit_eventID.addTextChangedListener(idWatcher);
        }


        text_label = (TextView) dialogContent.findViewById(R.id.text_event_label);
        if (text_label != null) {
            text_label.setText(context.getString((dialogMode() == DIALOG_MODE_ADD) ? R.string.editevent_dialog_title : R.string.editevent_dialog_title1));
        }

        edit_label = (EditText) dialogContent.findViewById(R.id.edit_event_label);
        if (edit_label != null) {
            edit_label.addTextChangedListener(labelWatcher);
        }

        edit_uri = (EditText) dialogContent.findViewById(R.id.edit_uri);
        edit_uri1 = (EditText) dialogContent.findViewById(R.id.edit_uri1);

        ColorChooserView colorView = (ColorChooserView) dialogContent.findViewById(R.id.chooser_eventColor);
        if (colorView != null) {
            choose_color = new ColorChooser(context, colorView.getLabel(), colorView.getEdit(), colorView.getButton(), "event");
        } else choose_color = new ColorChooser(context, null, null, null, "event");

        choose_color.setFragmentManager(getChildFragmentManager());
        choose_color.setCollapsed(true);
        choose_color.setColorChangeListener(onColorChanged);

        check_shown = (CheckBox) dialogContent.findViewById(R.id.check_shown);
        if (check_shown != null) {
            check_shown.setOnCheckedChangeListener(onCheckShownChanged);
        }

        ImageButton cancelButton = (ImageButton) dialogContent.findViewById(R.id.cancel_button);
        if (cancelButton != null)
        {
            cancelButton.requestFocus();
            cancelButton.setOnClickListener(onCancelButtonClicked);
        }

        ImageButton saveButton = (ImageButton) dialogContent.findViewById(R.id.save_button);
        if (saveButton != null) {
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accept();
                }
            });
        }

        layout_angle = dialogContent.findViewById(R.id.layout_event_angle);
        edit_angle = (EditText) dialogContent.findViewById(R.id.edit_event_angle);

        layout_shadowLength = dialogContent.findViewById(R.id.layout_event_length);
        edit_shadowLength = (EditText) dialogContent.findViewById(R.id.edit_event_length);
        text_units_shadowLength = (TextView) dialogContent.findViewById(R.id.text_event_length_units);

        layout_objHeight = dialogContent.findViewById(R.id.layout_event_height);
        edit_objHeight = (EditText) dialogContent.findViewById(R.id.edit_event_height);
        text_units_objHeight = (TextView) dialogContent.findViewById(R.id.text_event_height_units);

        switch (type)
        {
            case SHADOWLENGTH:
                setViewVisibility(layout_objHeight, true);
                setViewVisibility(layout_shadowLength, true);
                setViewVisibility(layout_angle, false);
                if (edit_shadowLength != null) {
                    edit_shadowLength.addTextChangedListener(lengthWatcher);
                }
                if (edit_objHeight != null) {
                    setObjHeightMeters(WidgetSettings.loadObserverHeightPref(context, 0));    // initial value from app configuration
                    edit_objHeight.addTextChangedListener(heightWatcher);
                }
                break;

            case SUN_ELEVATION:
            default:
                setViewVisibility(layout_angle, true);
                setViewVisibility(layout_objHeight, false);
                setViewVisibility(layout_shadowLength, false);
                if (edit_angle != null) {
                    edit_angle.addTextChangedListener(angleWatcher);
                }
                break;
        }

        text_offset = (TextView) dialogContent.findViewById(R.id.edit_event_offset);
        View chip_offset = dialogContent.findViewById(R.id.chip_event_offset);
        if (chip_offset != null) {
            chip_offset.setOnClickListener(onPickOffset);
        }

        if (eventID == null) {
            eventID = EventSettings.suggestEventID(context);
        }

        if (label == null || label.trim().isEmpty()) {
            label = EventSettings.suggestEventLabel(context, type);
        }

        super.initViews(context, dialogContent, savedState);
    }

    protected void setViewVisibility(View layout, boolean visible) {
        if (layout != null) {
            layout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void updateViews(Context context)
    {
        if (edit_eventID != null) {
            edit_eventID.setText(eventID);
        }
        if (edit_label != null) {
            edit_label.setText(label != null ? label : eventID);
        }
        if (edit_uri != null) {
            edit_uri.setText(uri != null ? uri : "");
        }
        if (choose_color != null) {
            choose_color.setColor(color);
        }
        if (check_shown != null) {
            check_shown.setChecked(shown);
        }
        updateViews(context, getEventType());
    }

    protected void updateViews(Context context, AlarmEventProvider.EventType type)
    {
        setEventLabel(getEventLabel());

        switch (type)
        {
            case SUN_ELEVATION:
                double angle = 0;
                AlarmEventProvider.SunElevationEvent event0 = null;
                if (uri != null) {
                    event0 = AlarmEventProvider.SunElevationEvent.valueOf(Uri.parse(uri).getLastPathSegment());
                }
                if (edit_angle != null && event0 != null) {
                    setAngle(angle = event0.getAngle());
                }

                int offset = ((event0 != null) ? event0.getOffset() : 0);
                setOffset(offset);
                if (text_offset != null)
                {
                    String offsetText = utils.timeDeltaLongDisplayString(0, offset).getValue();
                    text_offset.setText((offset != 0)
                            ? context.getResources().getQuantityString((offset < 0 ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), (int)angle, offsetText)
                            : getResources().getQuantityString(R.plurals.offset_at_plural, (int)angle));
                }
                break;

            case SHADOWLENGTH:
                double length = 0;
                AlarmEventProvider.ShadowLengthEvent shadowEvent = null;
                if (uri != null) {
                    shadowEvent = AlarmEventProvider.ShadowLengthEvent.valueOf(Uri.parse(uri).getLastPathSegment());
                }
                if (text_units_shadowLength != null) {
                    text_units_shadowLength.setText(context.getString((units == WidgetSettings.LengthUnit.METRIC) ? R.string.units_meters_short : R.string.units_feet_short));
                }
                if (text_units_objHeight != null) {
                    text_units_objHeight.setText(context.getString((units == WidgetSettings.LengthUnit.METRIC) ? R.string.units_meters_short : R.string.units_feet_short));
                }
                if (edit_shadowLength != null && shadowEvent != null) {
                    setShadowLengthMeters(shadowLength = shadowEvent.getLength());
                }
                if (edit_objHeight != null && shadowEvent != null) {
                    setObjHeightMeters(objHeight = shadowEvent.getObjHeight());
                }

                int shadowOffset = ((shadowEvent != null) ? shadowEvent.getOffset() : 0);
                setOffset(shadowOffset);
                if (text_offset != null)
                {
                    String offsetText = utils.timeDeltaLongDisplayString(0, shadowOffset).getValue();
                    text_offset.setText((shadowOffset != 0)
                            ? context.getResources().getQuantityString((shadowOffset < 0 ? R.plurals.offset_before_plural : R.plurals.offset_after_plural), (int)length, offsetText)
                            : getResources().getQuantityString(R.plurals.offset_at_plural, (int)length));
                }
                break;

            case DATE:
            case SOLAREVENT:
            default:
                break;
        }
    }

    private final View.OnClickListener onPickOffset = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (Build.VERSION.SDK_INT >= 11)
            {
                TimeOffsetPickerDialog dialog = new TimeOffsetPickerDialog();
                dialog.setFlags(false, true, true, false, true);
                dialog.setRange(0, getResources().getInteger(R.integer.maxAlarmOffsetMillis));
                dialog.setShowLabel(false);
                dialog.setValue(getOffset());
                dialog.setZeroText(getString(R.string.configAction_clearOffset));
                dialog.setDialogListener(onOffsetChanged);
                dialog.show(getChildFragmentManager(), DIALOGTAG_OFFSET);

            }  else {
                Toast.makeText(getActivity(), getString(R.string.feature_not_supported_by_api, Integer.toString(Build.VERSION.SDK_INT)), Toast.LENGTH_SHORT).show();  // TODO: support api10 requires alternative to TimePicker
            }
        }
    };
    private final TimeOffsetPickerDialog.DialogListener onOffsetChanged = new TimeOffsetPickerDialog.DialogListener()
    {
        @Override
        public void onDialogAccepted(long value)
        {
            FragmentManager fragments = getChildFragmentManager();
            TimeOffsetPickerDialog offsetDialog = (TimeOffsetPickerDialog) fragments.findFragmentByTag(DIALOGTAG_OFFSET);
            if (offsetDialog != null)
            {
                int offset = (int) value;

                Double angle = getAngle();
                if (angle == null) {
                    angle = EditEventDialog.this.angle;
                }

                String eventID;
                switch (type)
                {
                    case SHADOWLENGTH:
                        eventID = AlarmEventProvider.ShadowLengthEvent.getEventName(objHeight, shadowLength, offset, null);
                        break;

                    case SUN_ELEVATION:
                    default:
                        eventID = AlarmEventProvider.SunElevationEvent.getEventName(angle, offset, null);
                        break;

                }

                String eventUri = AlarmAddon.getEventCalcUri(AUTHORITY, eventID);
                setOffset(offset);
                setEventUri(eventUri);
                setIsModified(true);
                updateViews(getActivity());
            }
        }
    };

    @Override
    protected boolean validateInput()
    {
        boolean isValid = validateInput_id() && validateInput_label();
        switch (type)
        {
            case SHADOWLENGTH:
                isValid = validateInput_objHeight() && isValid;
                isValid = validateInput_shadowLength() && isValid;
                break;

            case SUN_ELEVATION:
            default:
                isValid = validateInput_angle() && isValid;
                break;
        }
        return isValid;
    }
    @Override
    protected void checkInput() {
        validateInput();
    }

    protected boolean validateInput_id()
    {
        String id = edit_eventID.getText().toString();
        if (id.trim().isEmpty() || id.contains(" ")) {
            edit_eventID.setError(getContext().getString(R.string.editevent_dialog_id_error));
            return false;
        }
        edit_eventID.setError(null);
        return true;
    }

    protected boolean validateInput_label()
    {
        String label = edit_label.getText().toString();
        if (label.trim().isEmpty()) {
            edit_label.setError(getContext().getString(R.string.editevent_dialog_label_error));
            return false;
        }
        edit_label.setError(null);
        return true;
    }

    protected boolean validateInput_angle()
    {
        if (edit_angle == null) {
            return true;
        }
        try {
            double angle = Double.parseDouble(edit_angle.getText().toString());
            if (angle < MIN_ANGLE || angle > MAX_ANGLE) {
                edit_angle.setError(getContext().getString(R.string.editevent_dialog_angle_error));
                return false;
            }
        } catch (NumberFormatException e) {
            edit_angle.setError(getContext().getString(R.string.editevent_dialog_angle_error));
            return false;
        }
        edit_angle.setError(null);
        return true;
    }
    public static final double MIN_ANGLE = -90;
    public static final double MAX_ANGLE = 90;

    protected boolean validateInput_shadowLength()
    {
        if (edit_shadowLength == null) {
            return true;
        }
        try {
            double length = Double.parseDouble(edit_shadowLength.getText().toString());
            if (length < MIN_LENGTH || length > MAX_LENGTH) {
                edit_shadowLength.setError(getContext().getString(R.string.editevent_dialog_length_error));
                return false;
            }
        } catch (NumberFormatException e) {
            edit_shadowLength.setError(getContext().getString(R.string.editevent_dialog_length_error));
            return false;
        }
        edit_shadowLength.setError(null);
        return true;
    }
    public static final double MIN_LENGTH = 0;      // TODO
    public static final double MAX_LENGTH = 100;    // TODO

    protected boolean validateInput_objHeight()
    {
        if (edit_objHeight == null) {
            return true;
        }
        try {
            double length = Double.parseDouble(edit_objHeight.getText().toString());
            if (length < MIN_HEIGHT || length > MAX_HEIGHT) {
                edit_objHeight.setError(getContext().getString(R.string.editevent_dialog_height_error));
                return false;
            }
        } catch (NumberFormatException e) {
            edit_objHeight.setError(getContext().getString(R.string.editevent_dialog_height_error));
            return false;
        }
        edit_objHeight.setError(null);
        return true;
    }
    public static final double MIN_HEIGHT = .01;    // TODO
    public static final double MAX_HEIGHT = 100;    // TODO

    private final TextWatcher labelWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            validateInput_label();
            setIsModified(true);
        }
    };

    private final TextWatcher idWatcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            if (validateInput_id()) {
                setEventUri1(AlarmAddon.getEventInfoUri(AUTHORITY, s.toString()));
            }
            setIsModified(true);
        }
    };

    private final TextWatcher angleWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                double angle = Double.parseDouble(s.toString());
                String eventID = AlarmEventProvider.SunElevationEvent.getEventName(angle, getOffset(), null);
                setEventUri(AlarmAddon.getEventCalcUri(AUTHORITY, eventID));
                setIsModified(true);

            } catch (NumberFormatException e) {
                Log.e("EditEventDialog", "not an angle: " + e);
            }
        }
    };

    private final TextWatcher lengthWatcher = new TextWatcher()
    {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
        @Override
        public void afterTextChanged(Editable s) {
            try {
                double lengthInput = Double.parseDouble(s.toString());
                double lengthMeters = (units == WidgetSettings.LengthUnit.METRIC ? lengthInput : WidgetSettings.LengthUnit.feetToMeters(lengthInput));
                Double objHeightMeters = getObjHeightMeters();
                if (objHeightMeters != null)
                {
                    String eventID = AlarmEventProvider.ShadowLengthEvent.getEventName(objHeightMeters, lengthMeters, getOffset(), null);
                    setEventUri(AlarmAddon.getEventCalcUri(AUTHORITY, eventID));
                    setIsModified(true);
                }

            } catch (NumberFormatException e) {
                Log.e("EditEventDialog", "not an angle: " + e);
            }
        }
    };

    private final TextWatcher heightWatcher = new TextWatcher()
    {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
        @Override
        public void afterTextChanged(Editable s) {
            try {
                double objHeightInput = Double.parseDouble(s.toString());
                double objHeightMeters = (units == WidgetSettings.LengthUnit.METRIC ? objHeightInput : WidgetSettings.LengthUnit.feetToMeters(objHeightInput));
                Double lengthMeters = getShadowLengthMeters();
                if (lengthMeters != null)
                {
                    String eventID = AlarmEventProvider.ShadowLengthEvent.getEventName(objHeightMeters, lengthMeters, getOffset(), null);
                    setEventUri(AlarmAddon.getEventCalcUri(AUTHORITY, eventID));
                    setIsModified(true);
                }

            } catch (NumberFormatException e) {
                Log.e("EditEventDialog", "not an angle: " + e);
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener onCheckShownChanged = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setIsModified(true);
        }
    };

    private final ColorChangeListener onColorChanged = new ColorChangeListener() {
        @Override
        public void onColorChanged(int color) {
            setIsModified(true);
        }
    };

    private final View.OnClickListener onCancelButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            confirmDiscardChanges(getActivity());
        }
    };

    protected void confirmDiscardChanges(final Context context)
    {
        if (isModified())
        {
            String message = context.getString(R.string.discardchanges_dialog_message);
            AlertDialog.Builder confirm = new AlertDialog.Builder(context).setMessage(message).setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(context.getString(R.string.discardchanges_dialog_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            getDialog().cancel();
                        }
                    })
                    .setNegativeButton(context.getString(R.string.discardchanges_dialog_cancel), null);
            confirm.show();

        } else {
            getDialog().cancel();
        }
    }

    @Override
    protected void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        bottomSheet.setCancelable(false);

        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

}
