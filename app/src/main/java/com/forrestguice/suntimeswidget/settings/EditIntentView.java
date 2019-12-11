/**
    Copyright (C) 2019 Forrest Guice
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
package com.forrestguice.suntimeswidget.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;

import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;

import java.util.ArrayList;
import java.util.Set;

@SuppressWarnings("Convert2Diamond")
public class EditIntentView extends LinearLayout
{
    public static final String TAG = "EditIntent";

    protected static final String DIALOGTAG_HELP = "help";
    protected static final String DIALOGTAG_SAVE = "save";
    protected static final String DIALOGTAG_LOAD = "load";

    protected static final String HELPTAG_LAUNCH = "action_launch";

    private static String[] ACTION_SUGGESTIONS = new String[] {
            Intent.ACTION_VIEW, Intent.ACTION_EDIT, Intent.ACTION_INSERT, Intent.ACTION_DELETE,
            Intent.ACTION_PICK, Intent.ACTION_RUN, Intent.ACTION_SEARCH, Intent.ACTION_SYNC,
            Intent.ACTION_CHOOSER, Intent.ACTION_GET_CONTENT,
            Intent.ACTION_SEND, Intent.ACTION_SENDTO, Intent.ACTION_ATTACH_DATA,
            Intent.ACTION_WEB_SEARCH, Intent.ACTION_MAIN
    };

    private static String[] MIMETYPE_SUGGESTIONS = new String[] { "text/plain" };

    protected EditText text_launchActivity;
    protected Spinner spinner_launchType;
    protected ImageButton button_menu;
    protected ImageButton button_launchTest;
    protected ToggleButton button_launchMore;
    protected AutoCompleteTextView text_launchAction;
    protected EditText text_launchData;
    protected AutoCompleteTextView text_launchDataType;
    protected EditText text_launchExtras;

    public EditIntentView(Context context)
    {
        super(context);
        init(context, null);
    }

    public EditIntentView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditIntentView, 0, 0);
        try
        {
            /**String labelText = a.getString(R.styleable.ColorChooserView_labelText);
            if (label != null){
                label.setText(labelText);
                button.setContentDescription(labelText);
            }*/

        } finally {
            a.recycle();
        }
    }

    private void init(final Context context, AttributeSet attrs)
    {
        LayoutInflater.from(context).inflate(R.layout.layout_view_editintent, this, true);

        text_launchActivity = (EditText) findViewById(R.id.appwidget_action_launch);

        button_menu = (ImageButton) findViewById(R.id.appwidget_action_launch_menu);
        button_menu.setOnClickListener(onMenuButtonClicked);

        button_launchMore = (ToggleButton) findViewById(R.id.appwidget_action_launch_moreButton);
        button_launchMore.setOnCheckedChangeListener(onExpandedChanged0);

        button_launchTest = (ImageButton) findViewById(R.id.appwidget_action_launch_test);
        button_launchTest.setOnClickListener(onTestButtonClicked);

        spinner_launchType = (Spinner) findViewById(R.id.appwidget_action_launch_type);
        ArrayAdapter<WidgetActions.LaunchType> launchTypeAdapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline, WidgetActions.LaunchType.values());
        launchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_launchType.setAdapter(launchTypeAdapter);

        text_launchAction = (AutoCompleteTextView) findViewById(R.id.appwidget_action_launch_action);
        text_launchAction.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, ACTION_SUGGESTIONS));

        text_launchData = (EditText) findViewById(R.id.appwidget_action_launch_data);
        text_launchDataType = (AutoCompleteTextView) findViewById(R.id.appwidget_action_launch_datatype);
        text_launchDataType.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, MIMETYPE_SUGGESTIONS));

        text_launchExtras = (EditText) findViewById(R.id.appwidget_action_launch_extras);

        ImageButton button_launchAppHelp = (ImageButton) findViewById(R.id.appwidget_action_launch_helpButton);
        if (button_launchAppHelp != null) {
            button_launchAppHelp.setOnClickListener(onHelpClicked);
        }

        applyAttributes(context, attrs);
    }

    /**
     * onHelpClicked
     */
    protected View.OnClickListener onHelpClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            HelpDialog helpDialog = new HelpDialog();
            helpDialog.setContent(getContext().getString(R.string.help_action_launch));
            helpDialog.setShowNeutralButton(getContext().getString(R.string.configAction_restoreDefaults));
            helpDialog.setNeutralButtonListener(helpDialogListener_launchApp, HELPTAG_LAUNCH);
            helpDialog.show(fragmentManager, DIALOGTAG_HELP);
        }
    };

    /**
     * onExpandedChanged
     */
    protected CompoundButton.OnCheckedChangeListener onExpandedChanged0 = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            View layout = findViewById(R.id.appwidget_action_launch_layout);
            if (layout != null) {
                layout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }

            if (onExpandedChanged != null) {
                onExpandedChanged.onCheckedChanged(buttonView, isChecked);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener onExpandedChanged = null;
    public void setOnExpandedChangedListener( CompoundButton.OnCheckedChangeListener listener ) {
        onExpandedChanged = listener;
    }

    /**
     * onTestButtonClicked
     */
    protected View.OnClickListener onTestButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            WidgetActions.LaunchType launchType = (WidgetActions.LaunchType)spinner_launchType.getSelectedItem();
            String launchClassName = text_launchActivity.getText().toString();
            String launchAction = text_launchAction.getText().toString();
            String launchData = text_launchData.getText().toString();
            String launchDataType = text_launchDataType.getText().toString();
            String launchExtras = text_launchExtras.getText().toString();
            Intent launchIntent;
            Class<?> launchClass;
            try {
                launchClass = Class.forName(launchClassName);
                launchIntent = new Intent(getContext(), launchClass);
                WidgetActions.applyAction(launchIntent, launchAction.trim().isEmpty() ? null : launchAction);
                WidgetActions.applyData(getContext(), launchIntent, (launchData.trim().isEmpty() ? null : launchData), (launchDataType.trim().isEmpty() ? null : launchDataType), data);
                WidgetActions.applyExtras(getContext(), launchIntent, launchExtras.trim().isEmpty() ? null : launchExtras, data);
                WidgetActions.startIntent(getContext(), launchIntent, launchType.name());

            } catch (ClassNotFoundException e) {
                Log.e("LaunchApp", "LaunchApp :: " + launchClassName + " cannot be found! " + e.toString());
                Toast.makeText(getContext(), "Unable to start intent!", Toast.LENGTH_LONG).show();  // TODO: i18n
            }
        }
    };

    /**
     * onMenuButtonClicked
     */
    protected View.OnClickListener onMenuButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            showOverflowMenu(getContext(), v);
        }
    };

    protected void showOverflowMenu(Context context, View parent)
    {
        PopupMenu menu = new PopupMenu(context, parent);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.editintent, menu.getMenu());
        menu.setOnMenuItemClickListener(onMenuItemClicked);
        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        menu.show();
    }

    protected PopupMenu.OnMenuItemClickListener onMenuItemClicked = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.saveIntent:
                    final SaveIntentDialog saveDialog = new SaveIntentDialog();
                    saveDialog.setOnAcceptedListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveIntent(getContext(), 0, saveDialog.getIntentID(), saveDialog.getIntentTitle());
                            Toast.makeText(getContext(), "Saved " + saveDialog.getIntentID(), Toast.LENGTH_SHORT).show();  // TODO: i18ns
                        }
                    });
                    saveDialog.show(fragmentManager, DIALOGTAG_SAVE);
                    return true;

                case R.id.loadIntent:
                    final LoadIntentDialog loadDialog = new LoadIntentDialog();
                    loadDialog.setOnAcceptedListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadIntent(getContext(), 0, loadDialog.getIntentID());
                            Toast.makeText(getContext(), "Loaded " + loadDialog.getIntentID(), Toast.LENGTH_SHORT).show();  // TODO: i18ns
                        }
                    });
                    loadDialog.show(fragmentManager, DIALOGTAG_LOAD);
                    return true;

                default:
                    return false;
            }
        }
    };

    /**
     * saveIntent
     * @param context Context
     * @param id Intent id (or null)
     */
    public void saveIntent(Context context, int appWidgetId, @Nullable String id, @Nullable String title)
    {
        String launchString = getIntentClass();
        if (launchString.trim().isEmpty()) {
            launchString = WidgetActions.PREF_DEF_ACTION_LAUNCH;
        }
        WidgetActions.saveActionLaunchPref(context, appWidgetId, id, launchString, getIntentType().name(), getIntentAction(), getIntentData(), getIntentDataType(), getIntentExtras(), title);
    }

    /**
     * loadIntent
     * @param context Context
     * @param id Intent id (or null)
     */
    public void loadIntent(Context context, int appWidgetId, @Nullable String id)
    {
        String launchString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, null);
        String typeString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TYPE);
        String actionString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_ACTION);
        String dataString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATA);
        String mimeType = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATATYPE);
        String extraString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_EXTRAS);

        setIntentClass(launchString);
        setIntentAction((actionString != null ? actionString : ""));
        setIntentData((dataString != null ? dataString : ""));
        setIntentDataType((mimeType != null ? mimeType : ""));
        setIntentExtras((extraString != null ? extraString : ""));
        setIntentType(typeString);
    }

    protected SuntimesData data = null;
    public void setData(SuntimesData data) {
        this.data = data;
        if (!this.data.isCalculated()) {
            data.calculate();
        }
    }

    protected FragmentManager fragmentManager = null;
    public void setFragmentManager( FragmentManager fragmentManager ) {
        this.fragmentManager = fragmentManager;
    }

    /**
     * getIntentClass
     */
    public String getIntentClass()
    {
        return text_launchActivity.getText().toString();
    }
    public void setIntentClass( String className )
    {
        text_launchActivity.setText(className);
    }

    /**
     * getIntentType
     */
    public WidgetActions.LaunchType getIntentType() {
        return (WidgetActions.LaunchType)spinner_launchType.getSelectedItem();
    }
    public void setIntentType( String launchType )
    {
        for (int i=0; i < spinner_launchType.getCount(); i++)
        {
            WidgetActions.LaunchType type = (WidgetActions.LaunchType)(spinner_launchType.getItemAtPosition(i));
            if (type.name().equals(launchType))
            {
                spinner_launchType.setSelection(i);
                break;
            }
        }
    }

    /**
     * getIntentAction
     */
    public String getIntentAction() {
        return text_launchAction.getText().toString();
    }
    public void setIntentAction(String action) {
        text_launchAction.setText(action);
    }

    /**
     * getIntentData
     */
    public String getIntentData() {
        return text_launchData.getText().toString();
    }
    public void setIntentData(String data) {
        text_launchData.setText(data);
    }

    /**
     * getIntentDataType
     */
    public String getIntentDataType() {
        return text_launchDataType.getText().toString();
    }
    public void setIntentDataType( String mimeType ) {
        text_launchDataType.setText(mimeType);
    }

    /**
     * getIntentExtras
     */
    public String getIntentExtras() {
        return text_launchExtras.getText().toString();
    }
    public void setIntentExtras(String extras) {
        text_launchExtras.setText(extras);
    }

    /**
     * onResume()
     */
    public void onResume( FragmentManager fragments, @Nullable SuntimesData data )
    {
        setFragmentManager(fragments);
        setData(data);

        if (fragmentManager != null)
        {
            HelpDialog helpDialog = (HelpDialog) fragmentManager.findFragmentByTag(DIALOGTAG_HELP);
            if (helpDialog != null)
            {
                String tag = helpDialog.getListenerTag();
                if (tag != null && tag.equals(HELPTAG_LAUNCH)) {
                    helpDialog.setNeutralButtonListener(helpDialogListener_launchApp, HELPTAG_LAUNCH);
                }
            }
        }
    }

    /**
     * HelpDialog onShow (launch App)
     */
    private View.OnClickListener helpDialogListener_launchApp = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (text_launchActivity != null)
            {
                spinner_launchType.setSelection(0);
                text_launchAction.setText("");
                text_launchData.setText("");
                text_launchDataType.setText("");
                text_launchExtras.setText("");
                text_launchActivity.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH);
                text_launchActivity.selectAll();
                text_launchActivity.requestFocus();
            }

            if (fragmentManager != null)
            {
                HelpDialog helpDialog = (HelpDialog) fragmentManager.findFragmentByTag(DIALOGTAG_HELP);
                if (helpDialog != null) {
                    helpDialog.dismiss();
                }
            }
        }
    };

    /**
     * EditIntentDialog
     */
    public static abstract class EditIntentDialog extends BottomSheetDialogFragment
    {
        protected abstract String getIntentID();
        protected abstract int getLayoutID();

        protected void initViews(Context context, View dialogContent)
        {
            Button btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
            btn_cancel.setOnClickListener(onDialogCancelClick);

            Button btn_accept = (Button) dialogContent.findViewById(R.id.dialog_button_accept);
            btn_accept.setOnClickListener(onDialogAcceptClick);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
        {
            ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
            View dialogContent = inflater.cloneInContext(contextWrapper).inflate(getLayoutID(), parent, false);
            initViews(getContext(), dialogContent);
            return dialogContent;
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
            expandSheet(getDialog());
        }

        private View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
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
                dismiss();
                if (onAccepted != null) {
                    onAccepted.onClick(getDialog(), 0);
                }
            }
        };

        private void expandSheet(DialogInterface dialog)
        {
            if (dialog == null) {
                return;
            }

            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
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

    /**
     * SaveIntentDialog
     */
    public static class SaveIntentDialog extends EditIntentDialog
    {
        public String getIntentTitle()
        {
            if (edit_intentTitle != null) {
                return edit_intentTitle.getText().toString();
            } else return null;
        }

        public String getIntentID()
        {
            if (edit_intentID != null) {
                return edit_intentID.getText().toString();
            } else return null;
        }

        private EditText edit_intentTitle;
        private AutoCompleteTextView edit_intentID;
        private TextView text_note;

        @Override
        protected void initViews(Context context, View dialogContent)
        {
            final Set<String> intentIDs = WidgetActions.loadActionLaunchList(context, 0);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, intentIDs.toArray(new String[0]));

            edit_intentID = (AutoCompleteTextView) dialogContent.findViewById(R.id.edit_intent_id);
            edit_intentID.setAdapter(adapter);
            edit_intentID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = (String)parent.getItemAtPosition(position);
                    edit_intentTitle.setText(WidgetActions.loadActionLaunchPref(getContext(), 0, item, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE));
                    text_note.setVisibility(View.VISIBLE);
                }
            });
            edit_intentID.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    text_note.setVisibility( (intentIDs.contains(s.toString())) ? View.VISIBLE : View.GONE );
                }
            });

            edit_intentTitle = (EditText) dialogContent.findViewById(R.id.edit_intent_title);

            text_note = (TextView) dialogContent.findViewById(R.id.text_note);
            text_note.setVisibility(View.GONE);

            super.initViews(context, dialogContent);
        }

        @Override
        protected int getLayoutID() {
            return R.layout.layout_dialog_intent_save;
        }
    }

    /**
     * LoadIntentDialog
     */
    public static class LoadIntentDialog extends EditIntentDialog
    {
        public String getIntentID()
        {
            if (spin_intentID != null) {
                IntentDisplay selected = (IntentDisplay)spin_intentID.getSelectedItem();
                return selected != null ? selected.id : null;
            } else return null;
        }

        private Spinner spin_intentID;

        @Override
        protected void initViews(Context context, View dialogContent)
        {
            spin_intentID = (Spinner) dialogContent.findViewById(R.id.spin_intentid);

            ArrayList<IntentDisplay> ids = new ArrayList<>();
            Set<String> intentIDs = WidgetActions.loadActionLaunchList(context, 0);
            for (String id : intentIDs)
            {
                String title = WidgetActions.loadActionLaunchPref(context, 0, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
                if (title != null && !title.trim().isEmpty()) {
                    ids.add(new IntentDisplay(id, title));
                }
            }
            ArrayAdapter<IntentDisplay> adapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline, ids.toArray(new IntentDisplay[0]));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin_intentID.setAdapter(adapter);

            super.initViews(context, dialogContent);
        }

        @Override
        protected int getLayoutID() {
            return R.layout.layout_dialog_intent_load;
        }

        public static class IntentDisplay
        {
            public String id;
            public String title;
            public IntentDisplay(String id, String title)
            {
                this.id = id;
                this.title = title;
            }
            public String toString() {
                 return title;
            }
        }
    }


}
