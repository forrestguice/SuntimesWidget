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
package com.forrestguice.suntimeswidget.actions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;

import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.forrestguice.suntimeswidget.settings.WidgetActions;

import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("Convert2Diamond")
public class EditActionView extends LinearLayout
{
    public static final String TAG = "EditIntent";

    public static final String DIALOGTAG_HELP = "help";
    public static final String DIALOGTAG_SAVE = "save";
    public static final String DIALOGTAG_LOAD = "load";

    protected static final String HELPTAG_LAUNCH = "action_launch";

    private static String[] ACTION_SUGGESTIONS = new String[] {
            Intent.ACTION_VIEW, Intent.ACTION_EDIT, Intent.ACTION_INSERT, Intent.ACTION_DELETE,
            Intent.ACTION_PICK, Intent.ACTION_RUN, Intent.ACTION_SEARCH, Intent.ACTION_SYNC,
            Intent.ACTION_CHOOSER, Intent.ACTION_GET_CONTENT,
            Intent.ACTION_SEND, Intent.ACTION_SENDTO, Intent.ACTION_ATTACH_DATA,
            Intent.ACTION_WEB_SEARCH, Intent.ACTION_MAIN
    };

    private static String[] MIMETYPE_SUGGESTIONS = new String[] { "text/plain" };

    protected View layout_label;
    protected TextView text_label, text_desc;
    protected EditText edit_label, edit_desc;

    protected EditText text_launchActivity;
    protected EditText text_launchPackage;
    protected Spinner spinner_launchType;
    protected ImageButton button_menu;
    protected ImageButton button_load;
    protected ToggleButton button_launchMore;
    protected AutoCompleteTextView text_launchAction;
    protected EditText text_launchData;
    protected AutoCompleteTextView text_launchDataType;
    protected EditText text_launchExtras;

    private boolean startExpanded = false;
    private boolean allowCollapse = true;
    private boolean allowSaveLoad = true;

    public EditActionView(Context context)
    {
        super(context);
        init(context, null);
    }

    public EditActionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditActionView, 0, 0);
        try {
            startExpanded = a.getBoolean(R.styleable.EditActionView_startExpanded, startExpanded);
            allowCollapse = a.getBoolean(R.styleable.EditActionView_allowCollapse, allowCollapse);
            allowSaveLoad = a.getBoolean(R.styleable.EditActionView_allowSaveLoad, allowSaveLoad);
        } finally {
            a.recycle();
        }
    }

    private void init(final Context context, AttributeSet attrs)
    {
        applyAttributes(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_view_editintent, this, true);

        text_desc = (TextView) findViewById(R.id.appwidget_action_desc);
        text_label = (TextView) findViewById(R.id.appwidget_action_label);

        layout_label = findViewById(R.id.appwidget_action_label_layout);
        layout_label.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                button_load.performClick();
            }
        });

        edit_label = (EditText) findViewById(R.id.appwidget_action_label_edit);
        edit_desc = (EditText) findViewById(R.id.appwidget_action_desc_edit);

        text_launchActivity = (EditText) findViewById(R.id.appwidget_action_launch);
        text_launchPackage = (EditText) findViewById(R.id.appwidget_action_launch_package);

        button_menu = (ImageButton) findViewById(R.id.appwidget_action_launch_menu);
        button_menu.setOnClickListener(onMenuButtonClicked);

        button_load = (ImageButton) findViewById(R.id.appwidget_action_launch_load);
        button_load.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadIntent();
            }
        });

        button_launchMore = (ToggleButton) findViewById(R.id.appwidget_action_launch_moreButton);
        button_launchMore.setOnCheckedChangeListener(onExpandedChanged0);
        button_launchMore.setVisibility(allowCollapse ? View.VISIBLE : View.GONE);
        button_launchMore.setChecked(startExpanded);

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

        text_label.setVisibility(View.VISIBLE);
        edit_label.setVisibility(View.INVISIBLE);
        button_menu.setVisibility(View.GONE);
        button_load.setVisibility(View.VISIBLE);
        View layout = findViewById(R.id.appwidget_action_launch_layout);
        if (layout != null) {
            layout.setVisibility(View.GONE);
        }

        if (startExpanded) {
            setExpanded(true);
        }
    }

    /**
     * onHelpClicked
     */
    protected View.OnClickListener onHelpClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (fragmentManager != null) {
                HelpDialog helpDialog = new HelpDialog();
                helpDialog.setContent(getContext().getString(R.string.help_action_launch));
                helpDialog.setShowNeutralButton(getContext().getString(R.string.configAction_onlineHelp));
                helpDialog.setNeutralButtonListener(helpDialogListener_launchApp, HELPTAG_LAUNCH);
                helpDialog.show(fragmentManager, DIALOGTAG_HELP);
            }
        }
    };

    /**
     * onExpandedChanged
     */
    protected CompoundButton.OnCheckedChangeListener onExpandedChanged0 = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setExpanded(isChecked);
        }
    };

    private void setExpanded(boolean expanded)
    {
        View layout = findViewById(R.id.appwidget_action_launch_layout);
        if (layout != null) {
            layout.setVisibility(expanded ? View.VISIBLE : View.GONE);
        }

        button_load.setVisibility(expanded ? View.GONE : View.VISIBLE);
        button_menu.setVisibility(expanded ? View.VISIBLE : View.GONE);

        layout_label.setVisibility(expanded ? View.GONE : View.VISIBLE);
        edit_label.setVisibility(expanded ? View.VISIBLE : View.INVISIBLE);

        if (!expanded) {
            text_label.setText(edit_label.getText().toString());
            text_desc.setText(edit_desc.getText().toString());
        }

        if (onExpandedChanged != null) {
            onExpandedChanged.onCheckedChanged(button_launchMore, expanded);
        }
    }

    private CompoundButton.OnCheckedChangeListener onExpandedChanged = null;
    public void setOnExpandedChangedListener( CompoundButton.OnCheckedChangeListener listener ) {
        onExpandedChanged = listener;
    }

    /**
     * testIntent
     */
    private void testIntent()
    {
        WidgetActions.LaunchType launchType = (WidgetActions.LaunchType)spinner_launchType.getSelectedItem();
        String launchClassName = text_launchActivity.getText().toString();
        String launchAction = text_launchAction.getText().toString();
        String launchData = text_launchData.getText().toString();
        String launchDataType = text_launchDataType.getText().toString();
        String launchExtras = text_launchExtras.getText().toString();
        Intent launchIntent;

        if (!launchClassName.trim().isEmpty())
        {
            String launchPackageName = text_launchPackage.getText().toString();

            if (launchPackageName != null && !launchPackageName.trim().isEmpty())
            {
                launchIntent = new Intent();
                launchIntent.setClassName(launchPackageName, launchClassName);

            } else {
                Class<?> launchClass;
                try {
                    launchClass = Class.forName(launchClassName);
                    launchIntent = new Intent(getContext(), launchClass);

                } catch (Exception e) {
                    Log.e(TAG, "testIntent: " + launchClassName + " cannot be found! " + e);
                    Snackbar snackbar = Snackbar.make(this, getContext().getString(R.string.startaction_failed_toast, launchType), Snackbar.LENGTH_LONG);
                    SuntimesUtils.themeSnackbar(getContext(), snackbar, null);
                    snackbar.show();
                    return;
                }
            }

        } else {
            launchIntent = new Intent();
        }

        WidgetActions.applyAction(launchIntent, launchAction.trim().isEmpty() ? null : launchAction);
        WidgetActions.applyData(getContext(), launchIntent, (launchData.trim().isEmpty() ? null : launchData), (launchDataType.trim().isEmpty() ? null : launchDataType), data);
        WidgetActions.applyExtras(getContext(), launchIntent, launchExtras.trim().isEmpty() ? null : launchExtras, data);

        try {
            WidgetActions.startIntent(getContext(), launchIntent, launchType.name());

        } catch (Exception e) {
            Log.e(TAG, "testIntent: unable to start + " + launchType + " :: " + e);
            Snackbar snackbar = Snackbar.make(this, getContext().getString(R.string.startaction_failed_toast, launchType), Snackbar.LENGTH_LONG);
            SuntimesUtils.themeSnackbar(getContext(), snackbar, null);
            snackbar.show();
        }
    }

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

        MenuItem[] restrictedItems = new MenuItem[] { menu.getMenu().findItem(R.id.saveIntent), menu.getMenu().findItem(R.id.loadIntent) };
        for (MenuItem item : restrictedItems)
        {
            if (item != null) {
                item.setEnabled(allowSaveLoad);
                item.setVisible(allowSaveLoad);
            }
        }

        menu.show();
    }

    protected PopupMenu.OnMenuItemClickListener onMenuItemClicked = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.testIntent:
                    testIntent();
                    return true;

                case R.id.saveIntent:
                    saveIntent();
                    return true;

                case R.id.loadIntent:
                    loadIntent();
                    return true;

                default:
                    return false;
            }
        }
    };

    public void saveIntent()
    {
        final Context context = getContext();
        final SaveActionDialog saveDialog = new SaveActionDialog();
        saveDialog.setIntentID(lastLoadedID);
        saveDialog.setIntentTitle(edit_label.getText().toString());

        saveDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                saveDialog.setValuesFrom(EditActionView.this);
            }
        });
        saveDialog.setOnAcceptedListener(onSaveDialogAccepted(context, saveDialog));
        saveDialog.show(fragmentManager, DIALOGTAG_SAVE);
    }

    public void loadIntent()
    {
        final Context context = getContext();
        final LoadActionDialog loadDialog = new LoadActionDialog();
        loadDialog.setData(data);
        loadDialog.setOnAcceptedListener(onLoadDialogAccepted(context, loadDialog));
        loadDialog.show(fragmentManager, DIALOGTAG_LOAD);
    }

    private DialogInterface.OnClickListener onSaveDialogAccepted(final Context context, final SaveActionDialog saveDialog)
    {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initFromOther(saveDialog.getEdit());
                saveIntent(context, 0, saveDialog.getIntentID(), saveDialog.getIntentTitle(), saveDialog.getIntentDesc());
                Toast.makeText(context, context.getString(R.string.saveaction_toast, saveDialog.getIntentTitle(), saveDialog.getIntentID()), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private DialogInterface.OnClickListener onLoadDialogAccepted(final Context context, final LoadActionDialog loadDialog)
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                loadIntent(context, 0, loadDialog.getIntentID());
                //Toast.makeText(context, context.getString(R.string.loadaction_toast, loadDialog.getIntentTitle()), Toast.LENGTH_SHORT).show();
            }
        };
    }

    /**
     * saveIntent
     * @param context Context
     * @param id Intent id (or null)
     */
    public void saveIntent(Context context, int appWidgetId, @Nullable String id, @Nullable String title, @Nullable String desc)
    {
        WidgetActions.saveActionLaunchPref(context, title, desc, getIntentColor(), getIntentTags().toArray(new String[0]), appWidgetId, id, getIntentClass(), getIntentPackage(), getIntentType().name(), getIntentAction(), getIntentData(), getIntentDataType(), getIntentExtras());
        lastLoadedID = id;
    }

    /**
     * loadIntent
     * @param context Context
     * @param id Intent id (or null)
     */
    public void loadIntent(Context context, int appWidgetId, @Nullable String id)
    {
        String title = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
        String desc = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC);
        String launchString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, null);
        String packageString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_PACKAGE);
        String typeString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TYPE);
        String actionString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_ACTION);
        String dataString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATA);
        String mimeType = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATATYPE);
        String extraString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_EXTRAS);
        Integer color = Integer.parseInt(WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_COLOR));
        Set<String> tagSet = WidgetActions.loadActionTags(context, appWidgetId, id);

        setIntentTitle(title);
        setIntentDesc(desc);
        setIntentClass(launchString);
        setIntentPackage(packageString);
        setIntentAction((actionString != null ? actionString : ""));
        setIntentData((dataString != null ? dataString : ""));
        setIntentDataType((mimeType != null ? mimeType : ""));
        setIntentExtras((extraString != null ? extraString : ""));
        setIntentType(typeString);
        setIntentTags(tagSet);
        setIntentColor(color);
        lastLoadedID = id;
    }
    private String lastLoadedID = null;

    /**
     * restoreDefaults
     */
    public void restoreDefaults()
    {
        setIntentType(WidgetActions.PREF_DEF_ACTION_LAUNCH_TYPE.name());
        text_label.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH_TITLE);
        edit_label.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH_TITLE);
        text_desc.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH_DESC);
        edit_desc.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH_DESC);
        text_launchAction.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH_ACTION);
        text_launchData.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH_DATA);
        text_launchDataType.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH_DATATYPE);
        text_launchExtras.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH_EXTRAS);
        text_launchPackage.setText(WidgetActions.PREF_KEY_ACTION_LAUNCH_PACKAGE);
        text_launchActivity.setText(WidgetActions.PREF_DEF_ACTION_LAUNCH);
        text_launchActivity.selectAll();
        text_launchActivity.requestFocus();
        intentColor = WidgetActions.PREF_DEF_ACTION_LAUNCH_COLOR;
        tags = new TreeSet<>();
    }

    /**
     * setData
     */
    protected SuntimesData data = null;
    public void setData(SuntimesData data) {
        this.data = data;
        if (!this.data.isCalculated()) {
            data.calculate();
        }
    }

    /**
     * setFragmentManager
     */
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
     * getIntentPackage
     */
    public String getIntentPackage() {
        return text_launchPackage.getText().toString();
    }
    public void setIntentPackage( String packageName )
    {
        text_launchPackage.setText(packageName);
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
     * getIntentTitle
     */
    public String getIntentTitle() {
        return edit_label.getText().toString();
    }
    public void setIntentTitle( String title ) {
        text_label.setText(title);
        edit_label.setText(title);
    }

    /**
     * getIntentDesc
     */
    public String getIntentDesc() {
        return edit_desc.getText().toString();
    }
    public void setIntentDesc( String desc ) {
        text_desc.setText(desc);
        edit_desc.setText(desc);
    }

    private Integer intentColor = null;
    public Integer getIntentColor() {
        return intentColor;
    }
    public void setIntentColor(Integer color) {
        intentColor = color;
    }

    private TreeSet<String> tags = new TreeSet<>();
    public Set<String> getIntentTags() {
        return tags;
    }
    public void setIntentTags(Set<String> values) {
        tags = new TreeSet<>(values);
    }

    /**

    /**
     * initFromOther
     */
    public void initFromOther(EditActionView other )
    {
        setFragmentManager(other.fragmentManager);
        setIntentTitle(other.getIntentTitle());
        setIntentDesc(other.getIntentDesc());
        setIntentType(other.getIntentType().name());
        setIntentClass(other.getIntentClass());
        setIntentPackage(other.getIntentPackage());
        setIntentAction(other.getIntentAction());
        setIntentData(other.getIntentData());
        setIntentDataType(other.getIntentDataType());
        setIntentExtras(other.getIntentExtras());
        setIntentTags(other.getIntentTags());
        setIntentColor(other.getIntentColor());
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

            SaveActionDialog saveDialog = (SaveActionDialog) fragmentManager.findFragmentByTag(DIALOGTAG_SAVE);
            if (saveDialog != null)
            {
                saveDialog.setOnAcceptedListener(onSaveDialogAccepted(getContext(), saveDialog));
                saveDialog.getEdit().setFragmentManager(fragments);
                saveDialog.getEdit().setData(data);
            }

            LoadActionDialog loadDialog = (LoadActionDialog) fragmentManager.findFragmentByTag(DIALOGTAG_LOAD);
            if (loadDialog != null)
            {
                loadDialog.setData(data);
                loadDialog.setOnAcceptedListener(onLoadDialogAccepted(getContext(), loadDialog));
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
            Context context = getContext();
            if (context != null) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.help_action_url))));
            }
        }
    };


}
