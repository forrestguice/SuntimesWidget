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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;
import com.forrestguice.support.app.AppCompatActivity;

public class WelcomeUserInterfaceView extends WelcomeView
{
    public WelcomeUserInterfaceView(Context context) {
        super(context, R.layout.layout_welcome_ui);
    }
    public WelcomeUserInterfaceView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.layout_welcome_ui);
    }
    public WelcomeUserInterfaceView(AppCompatActivity activity) {
        super(activity, R.layout.layout_welcome_ui);
    }

    public static WelcomeUserInterfaceView newInstance(AppCompatActivity activity) {
        return new WelcomeUserInterfaceView(activity);
    }

    protected CheckBox check_moon;
    protected CheckBox check_astro, check_nautical, check_civil, check_noon, check_midnight, check_blue, check_gold;
    protected CheckBox check_solstice, check_crossquarter;

    @Override
    public void initViews(Context context, View view)
    {
        super.initViews(context, view);
        check_astro = (CheckBox) view.findViewById(R.id.check_show_astro);
        check_nautical = (CheckBox) view.findViewById(R.id.check_show_nautical);
        check_civil = (CheckBox) view.findViewById(R.id.check_show_civil);
        check_noon = (CheckBox) view.findViewById(R.id.check_show_noon);
        check_midnight = (CheckBox) view.findViewById(R.id.check_show_midnight);
        check_gold = (CheckBox) view.findViewById(R.id.check_show_gold);
        check_blue = (CheckBox) view.findViewById(R.id.check_show_blue);
        check_crossquarter = (CheckBox) view.findViewById(R.id.check_show_crossquarter);
        check_moon = (CheckBox) view.findViewById(R.id.check_show_moon);

        check_solstice = (CheckBox) view.findViewById(R.id.check_show_solstice);
        if (check_solstice != null) {
            check_solstice.setOnCheckedChangeListener(onCheckedChanged_showSolstice());
        }

        Log.d("DEBUG", "onClick: initViews0");
        Button button_defaults = (Button) view.findViewById(R.id.button_defaults);
        if (button_defaults != null) {
            Log.d("DEBUG", "onClick: initViews1");
            button_defaults.setOnClickListener(onClick_restoreDefaults());
        }

        loadSettings(context);
    }

    protected OnClickListener onClick_restoreDefaults() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "onClick: restoreDefaults");
                loadDefaults(getContext());
            }
        };
    }

    protected CompoundButton.OnCheckedChangeListener onCheckedChanged_showSolstice()
    {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (check_crossquarter != null) {
                    check_crossquarter.setEnabled(isChecked);
                }
            }
        };
    }

    protected void loadDefaults(@Nullable Context context)
    {
        boolean[] fields = AppSettings.loadShowFields(AppSettings.PREF_DEF_UI_SHOWFIELDS);
        if (check_astro != null) {
            check_astro.setChecked(fields[AppSettings.FIELD_ASTRO]);
        }
        if (check_nautical != null) {
            check_nautical.setChecked(fields[AppSettings.FIELD_NAUTICAL]);
        }
        if (check_civil != null) {
            check_civil.setChecked(fields[AppSettings.FIELD_CIVIL]);
        }
        if (check_noon != null) {
            check_noon.setChecked(fields[AppSettings.FIELD_NOON]);
        }
        if (check_midnight != null) {
            check_midnight.setChecked(fields[AppSettings.FIELD_MIDNIGHT]);
        }
        if (check_gold != null) {
            check_gold.setChecked(fields[AppSettings.FIELD_GOLD]);
        }
        if (check_blue != null) {
            check_blue.setChecked(fields[AppSettings.FIELD_BLUE]);
        }
        if (check_solstice != null) {
            check_solstice.setChecked(AppSettings.PREF_DEF_UI_SHOWEQUINOX);
        }
        if (check_crossquarter != null) {
            check_crossquarter.setChecked(AppSettings.PREF_DEF_UI_SHOWCROSSQUARTER);
        }
        if (check_moon != null) {
            check_moon.setChecked(AppSettings.PREF_DEF_UI_SHOWMOON);
        }
    }

    protected void loadSettings(Context context)
    {
        boolean[] fields = AppSettings.loadShowFieldsPref(context);
        if (check_astro != null) {
            check_astro.setChecked(fields[AppSettings.FIELD_ASTRO]);
        }
        if (check_nautical != null) {
            check_nautical.setChecked(fields[AppSettings.FIELD_NAUTICAL]);
        }
        if (check_civil != null) {
            check_civil.setChecked(fields[AppSettings.FIELD_CIVIL]);
        }
        if (check_noon != null) {
            check_noon.setChecked(fields[AppSettings.FIELD_NOON]);
        }
        if (check_midnight != null) {
            check_midnight.setChecked(fields[AppSettings.FIELD_MIDNIGHT]);
        }
        if (check_gold != null) {
            check_gold.setChecked(fields[AppSettings.FIELD_GOLD]);
        }
        if (check_blue != null) {
            check_blue.setChecked(fields[AppSettings.FIELD_BLUE]);
        }
        if (check_solstice != null) {
            check_solstice.setChecked(AppSettings.loadShowEquinoxPref(context));
        }
        if (check_crossquarter != null) {
            check_crossquarter.setChecked(AppSettings.loadShowCrossQuarterPref(context));
        }
        if (check_moon != null) {
            check_moon.setChecked(AppSettings.loadShowMoonPref(context));
        }
    }

    @Override
    public boolean saveSettings(Context context)
    {
        if (check_astro != null) {
            AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_ASTRO, check_astro.isChecked());
        }
        if (check_nautical != null) {
            AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_NAUTICAL, check_nautical.isChecked());
        }
        if (check_civil != null) {
            AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_CIVIL, check_civil.isChecked());
        }
        if (check_noon != null) {
            AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_NOON, check_noon.isChecked());
        }
        if (check_midnight != null) {
            AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_MIDNIGHT, check_midnight.isChecked());
        }
        if (check_gold != null) {
            AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_GOLD, check_gold.isChecked());
        }
        if (check_blue != null) {
            AppSettings.saveShowFieldsPref(context, AppSettings.FIELD_BLUE, check_blue.isChecked());
        }
        if (check_solstice != null) {
            AppSettings.saveShowEquinoxPref(context, check_solstice.isChecked());
        }
        if (check_crossquarter != null) {
            AppSettings.saveShowCrossQuarterPref(context, check_crossquarter.isChecked());
        }
        if (check_moon != null) {
            AppSettings.saveShowMoonPref(context, check_moon.isChecked());
        }
        WidgetSettings.saveLengthUnitsPref(context, 0, WidgetSettings.loadLengthUnitsPref(context, 0));
        return true;
    }
}
