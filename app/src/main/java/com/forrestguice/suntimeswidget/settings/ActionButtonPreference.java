/**
    Copyright (C) 2018-2019 Forrest Guice
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
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.preference.ListPreference;

/**
 * A preference with an "action" button; should be provided with a `widgetLayout` containing
 * ImageButton with id `actionButton0`.
 */
public class ActionButtonPreference extends ListPreference
{
    public ActionButtonPreference(Context context) {
        super(context);
    }

    public ActionButtonPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setParams(context, attrs);
    }

    @TargetApi(21)
    public ActionButtonPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setParams(context, attrs);
    }

    @TargetApi(21)
    public ActionButtonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        setParams(context, attrs);
    }

    public void setParams(Context context, AttributeSet attrs)
    {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorListPreference, 0, 0);
        try {
            actionButtonContentDescription = a.getString(R.styleable.ActionButtonPreference_actionButtonContentDescription);

        } finally {
            a.recycle();
        }
    }

    private String actionButtonContentDescription = null;
    public String getActionButtonContentDescription() {
        return actionButtonContentDescription;
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
            actionButton.setContentDescription(actionButtonContentDescription);
            actionButton.setTag(getKey());

            if (Build.VERSION.SDK_INT >= 11) {
                actionButton.setAlpha(enabled ? 1f : 0f);
            }

            actionButton.setOnClickListener(onActionClicked);
        }
    }

    private final View.OnClickListener onActionClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (actionButtonPreferenceListener != null) {
                actionButtonPreferenceListener.onActionButtonClicked();
            }
        }
    };

    private ActionButtonPreferenceListener actionButtonPreferenceListener = null;
    public static abstract class ActionButtonPreferenceListener
    {
        public void onActionButtonClicked() {}
    }

    /**
     * setActionButtonPreferenceListener
     * @param listener ActionButtonPreferenceListener
     */
    public void setActionButtonPreferenceListener(ActionButtonPreferenceListener listener ) {
        actionButtonPreferenceListener = listener;
    }

}
