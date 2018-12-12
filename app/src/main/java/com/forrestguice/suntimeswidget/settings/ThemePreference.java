/**
    Copyright (C) 2018 Forrest Guice
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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.forrestguice.suntimeswidget.R;

public class ThemePreference extends ListPreference
{
    public ThemePreference(Context context)
    {
        super(context);
    }

    public ThemePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @TargetApi(21)
    public ThemePreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ThemePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        ImageView actionButton = (ImageView) view.findViewById(R.id.actionButton0);
        if (actionButton != null)
        {
            boolean enabled = isEnabled();
            actionButton.setEnabled(enabled);

            if (Build.VERSION.SDK_INT >= 11) {
                actionButton.setAlpha(enabled ? 1f : 0f);
            }

            actionButton.setOnClickListener(onActionClicked);
        }
    }

    private View.OnClickListener onActionClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (themePreferenceListener != null) {
                themePreferenceListener.onActionButtonClicked();
            }
        }
    };

    private ThemePreferenceListener themePreferenceListener = null;
    public static abstract class ThemePreferenceListener
    {
        public void onActionButtonClicked() {}
    }

    /**
     * setThemePreferenceListener
     * @param listener ThemePreferenceListener
     */
    public void setThemePreferenceListener( ThemePreferenceListener listener ) {
        themePreferenceListener = listener;
    }

}
