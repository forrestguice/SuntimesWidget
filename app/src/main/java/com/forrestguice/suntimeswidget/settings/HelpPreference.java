/**
    Copyright (C) 2022 Forrest Guice
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
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.DialogPreference;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

/**
 * A "preference" (non-persistent) that displays help content in a dialog.
 */
@TargetApi(11)
public class HelpPreference extends DialogPreference
{
    private CharSequence helpText = "";
    private String helpLink = null;
    private String helpPath = "";
    private TextView helpView;

    @TargetApi(21)
    public HelpPreference(Context context) {
        super(context);
    }

    public HelpPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHelpText(context, attrs);
    }

    @TargetApi(21)
    public HelpPreference(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setHelpText(context, attrs);
    }

    @TargetApi(21)
    public HelpPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        setHelpText(context, attrs);
    }

    @Override
    protected View onCreateDialogView()
    {
        Context context = getContext();

        float marginTopBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());
        float marginLeftRight;
        TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.dialogPreferredPadding });
        marginLeftRight = context.getResources().getDimension(a.getResourceId(0, R.dimen.settingsGroup_margin));
        a.recycle();

        helpView = new TextView(getContext());
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.START;
        params1.setMargins((int)marginLeftRight, (int)marginTopBottom, (int)marginLeftRight, (int)marginTopBottom);
        helpView.setLayoutParams(params1);

        LinearLayout dialogView = new LinearLayout(getContext());
        dialogView.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        dialogView.setLayoutParams(layoutParams);

        dialogView.addView(helpView);
        return dialogView;
    }

    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);
        helpView.setText(helpText);
    }

    @Override
    protected void onDialogClosed(boolean result)
    {
        if (result && (getPositiveButtonText() != null && helpLink != null)) {
            openHelpLink(getContext());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int i) {
        return a.getInt(i, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    }

    public void setHelpText(Context context, @Nullable AttributeSet attrs)
    {
        if (attrs != null)
        {
            String buttonText = null;
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HelpPreference, 0, 0);
            try {
                this.helpText = SuntimesUtils.fromHtml(a.getString(R.styleable.HelpPreference_helpText));
                helpLink = a.getString(R.styleable.HelpPreference_helpLink);
                helpPath = a.getString(R.styleable.HelpPreference_helpPath);
                buttonText = a.getString(R.styleable.HelpPreference_moreHelpButtonText);
            } finally {
                a.recycle();
            }
            setPositiveButtonText(buttonText);
        }
    }
    public CharSequence getHelpText() {
        return helpText;
    }

    public void openHelpLink(Context context)
    {
        if (context != null && helpLink != null) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(helpLink + helpPath)));
        }
    }
    public String getHelpLink() {
        return helpLink;
    }
    public String getHelpPath() {
        return helpPath;
    }

}
