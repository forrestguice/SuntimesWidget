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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.net.Uri;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.colors.Color;
import com.forrestguice.support.widget.PopupMenuCompat;
import com.forrestguice.suntimeswidget.views.SnackbarUtils;
import com.forrestguice.suntimeswidget.views.Toast;
import com.forrestguice.suntimeswidget.HelpDialog;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.settings.WidgetActions;
import com.forrestguice.suntimeswidget.views.ViewUtils;
import com.forrestguice.support.app.FragmentManagerCompat;

import com.forrestguice.suntimeswidget.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    private static HashMap<String,PackageSuggestion> PACKAGE_SUGGESTIONS = null;
    private static final String[] MIMETYPE_SUGGESTIONS = new String[] { "*/*", "audio/*", "image/*", "text/plain", "text/html", "time/epoch", "video/*" };
    private static final String[] DATA_SUGGESTIONS = new String[] { "content:", "file:", "geo:", "http:", "https:" };

    protected View layout_label;
    protected TextView text_label, text_desc;
    protected EditText edit_label, edit_desc;

    protected EditText text_launchActivity;
    protected AutoCompleteTextView text_launchPackage;
    protected Spinner spinner_launchType;
    protected ImageButton button_menu;
    protected ImageButton button_load;
    protected ToggleButton button_launchMore;
    protected AutoCompleteTextView text_launchAction;
    protected AutoCompleteTextView text_launchData;
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

        text_launchPackage = (AutoCompleteTextView) findViewById(R.id.appwidget_action_launch_package);
        ImageButton button_launchPackageSuggest = (ImageButton) findViewById(R.id.appwidget_action_launch_package_suggest);
        if (button_launchPackageSuggest != null) {
            button_launchPackageSuggest.setOnClickListener(onSuggestPackagesClicked);
        }
        ImageButton button_launchPackageClear = (ImageButton) findViewById(R.id.appwidget_action_launch_package_clear);
        if (button_launchPackageClear != null) {
            button_launchPackageClear.setOnClickListener(onClearPackagesClicked);
        }
        text_launchPackage.addTextChangedListener(onSuggestTextChanged(text_launchPackage, button_launchPackageSuggest, button_launchPackageClear));
        text_launchPackage.setOnItemClickListener(onSuggestPackage);

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
        initAdapter(context, text_launchAction, WidgetActions.ANDROID_ACTION_SUGGESTIONS);

        ImageButton button_launchActionSuggest = (ImageButton) findViewById(R.id.appwidget_action_launch_action_suggest);
        if (button_launchActionSuggest != null) {
            button_launchActionSuggest.setOnClickListener(onSuggestButtonClicked(text_launchAction));
        }
        ImageButton button_launchActionClear = (ImageButton) findViewById(R.id.appwidget_action_launch_action_clear);
        if (button_launchActionClear != null) {
            button_launchActionClear.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    text_launchAction.requestFocus();
                    text_launchAction.setText("");
                }
            });
        }
        text_launchAction.addTextChangedListener(onSuggestTextChanged(text_launchAction, button_launchActionSuggest, button_launchActionClear));

        text_launchData = (AutoCompleteTextView) findViewById(R.id.appwidget_action_launch_data);
        initAdapter(context, text_launchData, DATA_SUGGESTIONS);

        text_launchDataType = (AutoCompleteTextView) findViewById(R.id.appwidget_action_launch_datatype);
        initAdapter(context, text_launchDataType, MIMETYPE_SUGGESTIONS);

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

    protected void initAdapter(Context context, final AutoCompleteTextView autocomplete, String[] suggestions) {
        if (autocomplete != null) {
            autocomplete.setAdapter(new ArrayAdapter<String>(context, R.layout.layout_listitem_classname, suggestions));  //android.R.layout.simple_dropdown_item_1line
        }
    }

    public static class PackageSuggestionAdapter extends ArrayAdapter<PackageSuggestion>
    {
        private int layoutResID = R.layout.layout_listitem_twoline_alt;
        public PackageSuggestionAdapter(Context context, ArrayList<PackageSuggestion> values) {
            super(context, 0, values);
        }
        public PackageSuggestionAdapter(Context context, ArrayList<PackageSuggestion> values, int layoutResID) {
            super(context, 0, values);
            this.layoutResID = layoutResID;
        }

        @Override @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            PackageSuggestion suggestion = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(layoutResID, parent, false);
            }
            TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
            if (text1 != null && suggestion != null) {
                text1.setText(suggestion.getLabel());
            }
            TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
            if (text2 != null && suggestion != null) {
                text2.setText(suggestion.getPackageName());
            }
            return convertView;
        }
    }

    /**
     * onSuggestButtonClicked .. trigger the autocomplete popup
     */
    protected View.OnClickListener onSuggestButtonClicked(final AutoCompleteTextView autocomplete)
    {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autocomplete != null) {
                    autocomplete.showDropDown();
                }
            }
        };
    }
    protected TextWatcher onSuggestTextChanged(final AutoCompleteTextView autocomplete, final ImageButton... button)
    {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                button[0].setVisibility(s.toString().isEmpty() ? View.VISIBLE : View.GONE);
                if (button.length > 1) {
                    button[1].setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
        };
    }

    /**
     * onSuggestPackagesClicked
     */
    protected View.OnClickListener onSuggestPackagesClicked = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (PACKAGE_SUGGESTIONS == null || PACKAGE_SUGGESTIONS.size() == 0) {
                PACKAGE_SUGGESTIONS = queryPackageSuggestions(getContext());
            }
            if (text_launchPackage != null && text_launchPackage.getAdapter() == null) {
                ArrayList<PackageSuggestion> suggestions = new ArrayList<>(PACKAGE_SUGGESTIONS.values());
                Collections.sort(suggestions, new Comparator<PackageSuggestion>() {
                    @Override
                    public int compare(PackageSuggestion o1, PackageSuggestion o2) {
                        return o1.label.compareTo(o2.label);
                    }
                });
                text_launchPackage.setAdapter(new PackageSuggestionAdapter(getContext(), suggestions));
            }
            if (text_launchPackage != null) {
                text_launchPackage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        text_launchPackage.showDropDown();
                    }
                }, 250);
            }
        }
    };

    protected View.OnClickListener onClearPackagesClicked = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (text_label != null && edit_label != null) {
                edit_label.setText(text_label.getText());
            }
            if (text_launchActivity != null) {
                text_launchActivity.setText("");
            }
            if (text_launchPackage != null) {
                text_launchPackage.requestFocus();
                text_launchPackage.setText("");
            }
        }
    };

    protected AdapterView.OnItemClickListener onSuggestPackage = new AdapterView.OnItemClickListener()
    {
        @SuppressLint("SetTextI18n")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            String activityText = text_launchActivity != null ? text_launchActivity.getText().toString() : "";
            PackageSuggestion suggestion = (PackageSuggestion) parent.getItemAtPosition(position);

            if (text_launchActivity != null && (activityText.trim().isEmpty() || !activityText.startsWith(suggestion.getPackageName())))
            {
                text_launchActivity.requestFocus();
                text_launchActivity.setText(suggestion != null ? suggestion.getClassName() : ".");
                text_launchActivity.setSelection(text_launchActivity.getText().length());
            }

            String labelText = edit_label != null ? edit_label.getText().toString() : "";
            String defaultLabel = getContext().getString(R.string.addaction_custtitle, "");
            if (edit_label != null && (labelText.trim().isEmpty() || labelText.startsWith(defaultLabel))) {
                edit_label.setText(suggestion != null ? suggestion.getLabel() : "");
            }
        }
    };
    public static HashMap<String,PackageSuggestion> queryPackageSuggestions(@NonNull Context context)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        HashMap<String,PackageSuggestion> map = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> packageInfo = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : packageInfo) {
            if (resolveInfo.activityInfo.packageName != null && !map.containsKey(resolveInfo.activityInfo.packageName))
            {
                String label = resolveInfo.activityInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
                PackageSuggestion suggestion = new PackageSuggestion(label, resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                map.put(resolveInfo.activityInfo.packageName, suggestion);
            }
        }
        return map;
    }

    public static final class PackageSuggestion
    {
        protected final String label, packageName, className;

        public PackageSuggestion(@NonNull String label, @NonNull String packageName, @NonNull String className) {
            this.label = label;
            this.packageName = packageName;
            this.className = className;
        }

        @NonNull
        public String getLabel() {
            return label;
        }

        @NonNull
        public String getPackageName() {
            return packageName;
        }

        @NonNull
        public String getClassName() {
            return className;
        }

        @NonNull
        public String toString() {
            return packageName;
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
            FragmentManagerCompat fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                HelpDialog helpDialog = new HelpDialog();
                helpDialog.setContent(getContext().getString(R.string.help_action_launch));
                helpDialog.setShowNeutralButton(getContext().getString(R.string.configAction_onlineHelp));
                helpDialog.setNeutralButtonListener(helpDialogListener_launchApp, HELPTAG_LAUNCH);
                if (fragmentManager.getFragmentManager() != null) {
                    helpDialog.show(fragmentManager.getFragmentManager(), DIALOGTAG_HELP);
                } else Log.w("EditActionView", "onHelpClicked; fragment manager is null!");
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

            if (!launchPackageName.trim().isEmpty())
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
                    SnackbarUtils.make(getContext(), this, getContext().getString(R.string.startaction_failed_toast, launchType), SnackbarUtils.LENGTH_LONG).show();
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
            SnackbarUtils.make(getContext(), this, getContext().getString(R.string.startaction_failed_toast, launchType), SnackbarUtils.LENGTH_LONG).show();
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
        PopupMenuCompat.createMenu(context, parent, R.menu.editintent, onMenuItemClicked).show();
    }

    protected PopupMenuCompat.PopupMenuListener onMenuItemClicked = new ViewUtils.ThrottledPopupMenuListener(new PopupMenuCompat.PopupMenuListener()
    {
        @Override
        public void onUpdateMenu(Context context, Menu menu)
        {
            MenuItem[] restrictedItems = new MenuItem[] { menu.findItem(R.id.saveIntent), menu.findItem(R.id.loadIntent) };
            for (MenuItem item : restrictedItems)
            {
                if (item != null) {
                    item.setEnabled(allowSaveLoad);
                    item.setVisible(allowSaveLoad);
                }
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.testIntent) {
                testIntent();
                return true;

            } else if (itemId == R.id.saveIntent) {
                saveIntent();
                return true;

            } else if (itemId == R.id.loadIntent) {
                loadIntent();
                return true;
            }
            return false;
        }
    });

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
        FragmentManagerCompat fragmentManager = getFragmentManager();
        if (fragmentManager != null && fragmentManager.getFragmentManager() != null) {
            saveDialog.show(fragmentManager.getFragmentManager(), DIALOGTAG_SAVE);
        } else Log.w("EditActionView", "saveIntent: fragment manager is null!");
    }

    public void loadIntent()
    {
        final Context context = getContext();
        final LoadActionDialog loadDialog = new LoadActionDialog();
        loadDialog.setData(data);
        loadDialog.setOnAcceptedListener(onLoadDialogAccepted(context, loadDialog));
        FragmentManagerCompat fragmentManager = getFragmentManager();
        if (fragmentManager != null && fragmentManager.getFragmentManager() != null) {
            loadDialog.show(fragmentManager.getFragmentManager(), DIALOGTAG_LOAD);
        } else Log.w("EditActionView", "loadIntent: fragment manager is null!");
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
    public void loadIntent(Context context, int appWidgetId, @Nullable String id) {
        loadIntent(context, appWidgetId, id, WidgetActions.defaultLaunchPrefValues());
    }
    public void loadIntent(Context context, int appWidgetId, @Nullable String id, ContentValues defaults)
    {
        String title = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE, defLaunchPrefValue(defaults, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE));
        String desc = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC, defLaunchPrefValue(defaults, WidgetActions.PREF_KEY_ACTION_LAUNCH_DESC));
        String launchString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, null, defLaunchPrefValue(defaults, ""));
        String packageString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_PACKAGE, defLaunchPrefValue(defaults, WidgetActions.PREF_KEY_ACTION_LAUNCH_PACKAGE));
        String typeString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TYPE, defLaunchPrefValue(defaults, WidgetActions.PREF_KEY_ACTION_LAUNCH_TYPE));
        String actionString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_ACTION, defLaunchPrefValue(defaults, WidgetActions.PREF_KEY_ACTION_LAUNCH_ACTION));
        String dataString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATA, defLaunchPrefValue(defaults, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATA));
        String mimeType = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATATYPE, defLaunchPrefValue(defaults, WidgetActions.PREF_KEY_ACTION_LAUNCH_DATATYPE));
        String extraString = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_EXTRAS, defLaunchPrefValue(defaults, WidgetActions.PREF_KEY_ACTION_LAUNCH_EXTRAS));
        String color0 = WidgetActions.loadActionLaunchPref(context, appWidgetId, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_COLOR);
        Integer color = (color0 != null ? Integer.parseInt(color0) : Color.WHITE);
        Set<String> tagSet = WidgetActions.loadActionTags(context, appWidgetId, id);

        initAdapter(context, text_launchAction, WidgetActions.getSuggestedActions(launchString));    // re-initialize action adapter with additional suggestions

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
    @Nullable
    private String lastLoadedID = null;

    public static String defLaunchPrefValue(@NonNull ContentValues values, String key)
    {
        if (values.containsKey(key)) {
            return values.getAsString(key);
        } else return WidgetActions.defaultLaunchPrefValue(key);
    }

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
            data.calculate(getContext());
        }
    }

    /**
     * setFragmentManager
     */
    protected WeakReference<FragmentManagerCompat> fragmentManager = null;
    public void setFragmentManager( FragmentManagerCompat fragmentManager ) {
        this.fragmentManager = new WeakReference<>(fragmentManager);
    }
    @Nullable
    public FragmentManagerCompat getFragmentManager() {
        return (fragmentManager != null ? fragmentManager.get() : null);
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
        setFragmentManager(other.getFragmentManager());
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
    public void onResume(FragmentManagerCompat fragments, @Nullable SuntimesData data )
    {
        setFragmentManager(fragments);
        setData(data);

        FragmentManagerCompat fragmentManager = getFragmentManager();
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
    private final View.OnClickListener helpDialogListener_launchApp = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Context context = getContext();
            if (context != null) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.help_url) + context.getString(R.string.help_action_path))));
            }
        }
    };


}
