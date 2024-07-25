/**
    Copyright (C) 2014-2019 Forrest Guice
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

package com.forrestguice.suntimeswidget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * SuntimesWarning; wraps a Snackbar and some flags.
 */
public class SuntimesWarning
{
    public static final int ANNOUNCE_DELAY_MS = 500;
    public static final String KEY_WASDISMISSED = "userDismissedWarning";
    public static final String KEY_DURATION = "duration";

    public SuntimesWarning(String id)
    {
        this.id = id;
    }
    protected String id = "";

    private Snackbar snackbar = null;
    public Snackbar getSnackbar() {
        return snackbar;
    }

    private boolean shouldShow = false;
    public boolean shouldShow() {
        return shouldShow;
    }
    public void setShouldShow(boolean value) {
        shouldShow = value;
    }

    protected boolean wasDismissed = false;
    public boolean wasDismissed() {
        return wasDismissed;
    }

    private int duration = Snackbar.LENGTH_INDEFINITE;
    public int getDuration() {
         return duration;
    }
    public void setDuration(int value) {
        duration = value;
    }

    protected String contentDescription = null;
    protected View parentView = null;

    public void initWarning(@NonNull Context context, View view, String msg)
    {
        this.parentView = view;
        ImageSpan warningIcon = SuntimesUtils.createWarningSpan(context, context.getResources().getDimension(R.dimen.warningIcon_size));
        SpannableStringBuilder message = SuntimesUtils.createSpan(context, msg, SuntimesUtils.SPANTAG_WARNING, warningIcon);
        this.contentDescription = msg.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), context.getString(R.string.spanTag_warning));

        wasDismissed = false;
        snackbar = Snackbar.make(parentView, message, duration);
        snackbar.addCallback(snackbarListener);
        setContentDescription(contentDescription);
        themeWarning(context, snackbar);
    }

    @SuppressLint("ResourceType")
    private void themeWarning(@NonNull Context context, @NonNull Snackbar snackbarWarning)
    {
        int[] colorAttrs = { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor, R.attr.selectableItemBackground };
        TypedArray a = context.obtainStyledAttributes(colorAttrs);
        int textColor = ContextCompat.getColor(context, a.getResourceId(0, android.R.color.primary_text_dark));
        int accentColor = ContextCompat.getColor(context, a.getResourceId(1, R.color.text_accent_dark));
        int backgroundColor = ContextCompat.getColor(context, a.getResourceId(2, R.color.card_bg_dark));
        Drawable buttonDrawable = ContextCompat.getDrawable(context, a.getResourceId(3, R.drawable.button_fab_dark));
        int buttonPadding = (int)context.getResources().getDimension(R.dimen.snackbar_button_padding);
        a.recycle();

        View snackbarView = snackbarWarning.getView();
        snackbarView.setBackgroundColor(backgroundColor);
        snackbarWarning.setActionTextColor(accentColor);

        TextView snackbarText = (TextView)snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        if (snackbarText != null) {
            snackbarText.setTextColor(textColor);
            snackbarText.setMaxLines(5);
        }

        View snackbarAction = snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        if (snackbarAction != null) {
            snackbarAction.setBackground(buttonDrawable);
            snackbarAction.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        }
    }

    private Snackbar.Callback snackbarListener = new Snackbar.Callback()
    {
        @Override
        public void onDismissed(Snackbar snackbar, int event)
        {
            super.onDismissed(snackbar, event);
            switch (event)
            {
                case DISMISS_EVENT_SWIPE:
                    wasDismissed = true;
                    showNextWarning();
                    break;
            }
        }
    };

    protected void showNextWarning()
    {
        if (warningListener != null) {
            warningListener.onShowNextWarning();
        }
    }

    public boolean isShown()
    {
        return (snackbar != null && snackbar.isShown());
    }

    public void show()
    {
        if (snackbar != null) {
            snackbar.show();
        }
        announceWarning();
    }

    public void dismiss()
    {
        if (isShown()) {
            snackbar.dismiss();
        }
    }

    public void reset()
    {
        wasDismissed = false;
        shouldShow = false;
    }

    public void setContentDescription( String value )
    {
        this.contentDescription = value;
        if (snackbar != null) {
            TextView snackText = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            if (snackText != null) {
                snackText.setContentDescription(contentDescription);
            }
        }
    }

    public void announceWarning()
    {
        if (parentView != null && contentDescription != null)
        {
            parentView.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    SuntimesUtils.announceForAccessibility(parentView, contentDescription);
                }
            }, ANNOUNCE_DELAY_MS);
        }
    }

    public void save( Bundle outState )
    {
        if (outState != null)
        {
            outState.putBoolean(KEY_WASDISMISSED + id, wasDismissed);
            outState.putInt(KEY_DURATION + id, duration);
        }
    }

    public void restore( Bundle savedState )
    {
        if (savedState != null)
        {
            wasDismissed = savedState.getBoolean(KEY_WASDISMISSED + id, false);
            duration = savedState.getInt(KEY_DURATION + id, duration);
        }
    }

    public SuntimesWarningListener warningListener = null;
    public void setWarningListener(SuntimesWarningListener listener) {
        warningListener = listener;
    }
    public static abstract class SuntimesWarningListener {
        public abstract void onShowNextWarning();
    }
}
