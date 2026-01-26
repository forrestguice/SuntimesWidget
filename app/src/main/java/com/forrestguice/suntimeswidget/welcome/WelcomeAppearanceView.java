/**
    Copyright (C) 2022-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.welcome;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.WelcomeActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.support.app.AppCompatActivity;

public class WelcomeAppearanceView extends WelcomeView
{
    protected Spinner spinner;
    protected ToggleButton[] buttons = null;
    protected TextView previewDate;

    public WelcomeAppearanceView(Context context) {
        super(context, R.layout.layout_welcome_appearance);
    }
    public WelcomeAppearanceView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.layout_welcome_appearance);
    }
    public WelcomeAppearanceView(AppCompatActivity activity) {
        super(activity, R.layout.layout_welcome_appearance);
    }

    public static WelcomeAppearanceView newInstance(AppCompatActivity activity) {
        return new WelcomeAppearanceView(activity);
    }

    protected void setChecked(@Nullable RadioButton button, boolean value) {
        if (button != null) {
            button.setChecked(value);
        }
    }

    protected void setCheckedChangeListener(@Nullable RadioButton button, CompoundButton.OnCheckedChangeListener listener) {
        if (button != null) {
            button.setOnCheckedChangeListener(listener);
        }
    }

    private static final String KEY_THEMEID = "themeID";
    private static final String KEY_THEMEID1 = "themeID1";
    private static final String KEY_DARKTHEMEID = "darkThemeID";
    private static final String KEY_LIGHTTHEMEID = "lightThemeID";
    private static final String KEY_TEXTSIZE = "textSize";

    protected AppSettings.TextSize getTextSize() {
        return AppSettings.TextSize.valueOf(getArgs().getString(KEY_TEXTSIZE, AppSettings.TextSize.NORMAL.name()));
    }
    protected void setTextSize(AppSettings.TextSize textSize) {
        getArgs().putString(KEY_TEXTSIZE, textSize.name());
    }

    @Override
    public void initViews(Context context, View view)
    {
        super.initViews(context, view);

        RadioButton smallText = (RadioButton) view.findViewById(R.id.radio_text_small);
        RadioButton normalText = (RadioButton) view.findViewById(R.id.radio_text_normal);
        RadioButton largeText = (RadioButton) view.findViewById(R.id.radio_text_large);
        RadioButton xlargeText = (RadioButton) view.findViewById(R.id.radio_text_xlarge);

        AppSettings.TextSize textSize = AppSettings.TextSize.valueOf(AppSettings.loadTextSizePref(context));
        setTextSize(textSize);
        switch (textSize)
        {
            case SMALL: setChecked(smallText, true); break;
            case LARGE: setChecked(largeText, true); break;
            case XLARGE: setChecked(xlargeText, true); break;
            case NORMAL: default: setChecked(normalText, true); break;
        }
        setCheckedChangeListener(smallText, onTextSizeChecked(context, AppSettings.TextSize.SMALL));
        setCheckedChangeListener(normalText, onTextSizeChecked(context, AppSettings.TextSize.NORMAL));
        setCheckedChangeListener(largeText, onTextSizeChecked(context, AppSettings.TextSize.LARGE));
        setCheckedChangeListener(xlargeText, onTextSizeChecked(context, AppSettings.TextSize.XLARGE));

        final AppSettings.AppThemeInfo themeInfo = AppSettings.loadThemeInfo(context);
        String themeID = themeInfo.getThemeName();
        String themeID1 = AppSettings.getThemeOverride(context, themeInfo);
        final AppSettings.AppThemeInfo themeInfo1 = AppSettings.loadThemeInfo(themeID1);
        String darkThemeID = AppSettings.loadThemeDarkPref(context);
        String lightThemeID = AppSettings.loadThemeLightPref(context);
        AppSettings.AppThemeInfo darkThemeInfo = AppSettings.loadThemeInfo(darkThemeID);

        setArg(KEY_THEMEID, themeID);
        setArg(KEY_THEMEID1, themeID1);
        setArg(KEY_DARKTHEMEID, darkThemeID);
        setArg(KEY_LIGHTTHEMEID, lightThemeID);

        previewDate = (TextView) view.findViewById(R.id.text_date);
        updatePreview(context, themeInfo.getDisplayString(context));

        spinner = (Spinner) view.findViewById(R.id.spin_theme);
        if (spinner != null)
        {
            final ArrayAdapter<AppSettings.AppThemeInfo> spinnerAdapter = new AppThemeInfoAdapter(context, R.layout.layout_listitem_welcome);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            int initialPosition = spinnerAdapter.getPosition(themeID1 != null ? themeInfo1 : themeInfo);
            spinner.setSelection(initialPosition, false);
            spinner.setOnItemSelectedListener(onThemeItemSelected(initialPosition));
        }

        ToggleButton systemThemeButton = (ToggleButton) view.findViewById(R.id.button_theme_system);
        ToggleButton systemTheme1Button = (ToggleButton) view.findViewById(R.id.button_theme_system1);
        ToggleButton darkThemeButton = (ToggleButton) view.findViewById(R.id.button_theme_dark);
        ToggleButton lightThemeButton = (ToggleButton) view.findViewById(R.id.button_theme_light);
        buttons = new ToggleButton[] {systemThemeButton, systemTheme1Button, darkThemeButton, lightThemeButton};
        for (ToggleButton button : buttons) {
            if (button != null) {
                button.setChecked(false);
            }
        }

        if (systemThemeButton != null) {
            if (setChecked(systemThemeButton, AppSettings.THEME_SYSTEM.equals(themeID) && AppSettings.THEME_DEFAULT.equals(darkThemeID))) {
                updatePreview(context, themeInfo.getDisplayString(context));
            }
            systemThemeButton.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_SYSTEM, null, null));
        }

        if (systemTheme1Button != null) {
            if (setChecked(systemTheme1Button, AppSettings.THEME_SYSTEM.equals(themeID) && AppSettings.THEME_SYSTEM1.equals(darkThemeID))) {
                updatePreview(context, darkThemeInfo.getDisplayString(context));
            }
            systemTheme1Button.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_SYSTEM, AppSettings.THEME_SYSTEM1, AppSettings.THEME_SYSTEM1));
        }

        if (darkThemeButton != null) {
            if (setChecked(darkThemeButton, AppSettings.THEME_DARK.equals(themeID))) {
                updatePreview(context, themeInfo.getDisplayString(context));
            }
            darkThemeButton.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_DARK, null, null));
        }

        if (lightThemeButton != null) {
            if (setChecked(lightThemeButton, AppSettings.THEME_LIGHT.equals(themeID))) {
                updatePreview(context, themeInfo.getDisplayString(context));
            }
            lightThemeButton.setOnClickListener(onThemeButtonClicked(AppSettings.THEME_LIGHT, null, null));
        }

        Log.d("DEBUG", "Appearance: initViews: " + lightThemeID + ", " + darkThemeID + ", " + textSize + ", " + themeID + " .. " + toString());
    }

    public static class AppThemeInfoAdapter extends ArrayAdapter<AppSettings.AppThemeInfo>
    {
        protected int layout;

        public AppThemeInfoAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
            layout = resource;
            for (AppSettings.AppThemeInfo info : AppSettings.appThemeInfo()) {
                add(info);
            }
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
        }
        @NonNull @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return createView(position, convertView, parent, layout);
        }

        @SuppressLint("ResourceType")
        private View createView(int position, View convertView, ViewGroup parent, int layoutResID)
        {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(layoutResID, parent, false);
            }

            AppSettings.AppThemeInfo item = getItem(position);
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(item != null ? item.getDisplayString(getContext()) : "");
            return view;
        }
    }

    private AdapterView.OnItemSelectedListener onThemeItemSelected(final int initialPosition)
    {
        return new AdapterView.OnItemSelectedListener()
        {
            private int currentPosition = initialPosition;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (position == currentPosition) {
                    //Log.d("DEBUG", "spinner position is already at " + position + ", skipping onItemSelected...");
                    return;
                }
                currentPosition = position;
                onThemeItemSelected(parent, view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }
    private void onThemeItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        AppSettings.AppThemeInfo themeInfo = (AppSettings.AppThemeInfo) parent.getAdapter().getItem(position);
        switch (themeInfo.getThemeName())
        {
            case AppSettings.THEME_MONET_SYSTEM:
                onThemeButtonClicked(AppSettings.THEME_SYSTEM, AppSettings.THEME_MONET_SYSTEM, AppSettings.THEME_MONET_SYSTEM).onClick(view);
                break;

            case AppSettings.THEME_MONET_DARK:
                onThemeButtonClicked(AppSettings.THEME_DARK, AppSettings.THEME_MONET_LIGHT, AppSettings.THEME_MONET_DARK).onClick(view);
                break;

            case AppSettings.THEME_MONET_LIGHT:
                onThemeButtonClicked(AppSettings.THEME_LIGHT, AppSettings.THEME_MONET_LIGHT, AppSettings.THEME_MONET_DARK).onClick(view);
                break;

            case AppSettings.THEME_SYSTEM1:
                onThemeButtonClicked(AppSettings.THEME_SYSTEM, AppSettings.THEME_SYSTEM1, AppSettings.THEME_SYSTEM1).onClick(view);
                break;

            case AppSettings.THEME_DARK1:
                onThemeButtonClicked(AppSettings.THEME_DARK, AppSettings.THEME_LIGHT1, AppSettings.THEME_DARK1).onClick(view);
                break;

            case AppSettings.THEME_LIGHT1:
                onThemeButtonClicked(AppSettings.THEME_LIGHT, AppSettings.THEME_LIGHT1, AppSettings.THEME_DARK1).onClick(view);
                break;

            case AppSettings.THEME_DARK:
            case AppSettings.THEME_LIGHT:
            case AppSettings.THEME_SYSTEM:
            default:
                onThemeButtonClicked(themeInfo.getThemeName(), null, null).onClick(view);
                break;
        }
    }

    protected void updatePreview(Context context, String themeDisplay)
    {
        if (previewDate != null) {
            previewDate.setText(themeDisplay.replace(" ", "\n"));
        }
    }

    protected boolean setChecked(ToggleButton button, boolean value)
    {
        if (value) {
            for (ToggleButton b : buttons) {
                if (b != null) {
                    b.setChecked(b == button);
                }
            }
            return true;
        } else return false;
    }

    private CompoundButton.OnCheckedChangeListener onTextSizeChecked(final Context context, final AppSettings.TextSize textSize)
    {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    setTextSize(textSize);
                    AppSettings.saveTextSizePref(context, textSize);
                    recreate(getActivity());
                }
            }
        };
    }

    private OnClickListener onThemeButtonClicked(final String themeID0, final String lightID0, final String darkID0) {
        return new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Activity activity = getActivity();
                if (activity != null)
                {
                    setArg(KEY_THEMEID, themeID0);
                    setArg(KEY_LIGHTTHEMEID, lightID0);
                    setArg(KEY_DARKTHEMEID, darkID0);
                    AppSettings.saveThemeLightPref(activity, lightID0);
                    AppSettings.saveThemeDarkPref(activity, darkID0);
                    AppSettings.setThemePref(activity, themeID0);
                    AppSettings.setTheme(activity, AppSettings.loadThemePref(activity));
                    recreate(getActivity());
                }
            }
        };
    }

    @Override
    public int getPreferredIndex() {
        return 1;
    }

    private void recreate(Activity activity)
    {
        if (activity != null)
        {
            if (activity instanceof  WelcomeActivity) {
                WelcomeActivity activity1 = (WelcomeActivity)activity;
                activity1.setNeedsRecreateFlag();
            }
            activity.finish();
            activity.overridePendingTransition(R.anim.transition_restart_in, R.anim.transition_restart_out);
            activity.startActivity(activity.getIntent()
                    .putExtra(WelcomeActivity.EXTRA_PAGE, getPreferredIndex()));
        }
    }

    @Override
    public boolean saveSettings(Context context)
    {
        Bundle args = getArgs();
        String themeID = args.getString(KEY_THEMEID, null);
        String lightThemeID = args.getString(KEY_LIGHTTHEMEID, null);
        String darkThemeID = args.getString(KEY_DARKTHEMEID, null);
        AppSettings.TextSize textSize = getTextSize();
        Log.d("DEBUG", "Appearance: saveSettings: " + lightThemeID + ", " + darkThemeID + ", " + textSize + ", " + themeID + " .. " + toString());
        // the following are set when changed (on automatic transition)
        //AppSettings.saveThemeLightPref(context, lightThemeID);
        //AppSettings.saveThemeDarkPref(context, darkThemeID);
        //AppSettings.saveTextSizePref(context, textSize);
        //AppSettings.setThemePref(context, themeID);
        return true;
    }

}
