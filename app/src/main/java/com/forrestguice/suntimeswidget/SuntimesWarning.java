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
import android.os.Build;
import android.os.Bundle;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.content.ContextCompat;
import com.forrestguice.support.design.widget.Snackbar;

import android.text.style.ImageSpan;
import android.util.Log;
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
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_CONTENT_DESCRIPTION = "contentDescription";
    public static final String KEY_ACTION_LABEL = "actionLabel";
    public static final String KEY_SHOULD_SHOW = "shouldShow";

    public SuntimesWarning(String id) {
        this.id = id;
    }

    public SuntimesWarning(String id, CharSequence message)
    {
        this.id = id;
        setMessage(message);
    }

    public SuntimesWarning(String id, Context context, String msg)
    {
        this.id = id;
        setMessage(context, msg);
    }

    protected String id;
    public String getId() {
        return id;
    }

    protected View parentView = null;
    public View getParentView() {
        return parentView;
    }

    private Snackbar.SnackbarInterface snackbar = null;
    public Snackbar.SnackbarInterface getSnackbar() {
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

    protected CharSequence message = null;
    protected CharSequence contentDescription = null;
    public CharSequence getMessage() {
        return message;
    }
    public void setMessage( CharSequence value ) {
        message = value;
        contentDescription = value;
    }
    public void setMessage( Context context, String msg )
    {
        ImageSpan warningIcon = SuntimesUtils.createWarningSpan(context, context.getResources().getDimension(R.dimen.warningIcon_size));
        message = SuntimesUtils.createSpan(context, msg, SuntimesUtils.SPANTAG_WARNING, warningIcon);
        contentDescription = msg.replaceAll(Pattern.quote(SuntimesUtils.SPANTAG_WARNING), context.getString(R.string.spanTag_warning));
    }

    protected CharSequence actionLabel = null;
    public CharSequence getActionLabel() {
        return actionLabel;
    }
    public void setActionLabel( @Nullable CharSequence value ) {
        actionLabel = value;
    }

    public void initWarning(@NonNull Context context, View view, View.OnClickListener actionListener)
    {
        parentView = view;
        wasDismissed = false;
        snackbar = Snackbar.make(parentView, message, duration);
        snackbar.get().addCallback(snackbarListener);
        setContentDescription(contentDescription);
        themeWarning(context, snackbar);

        if (actionLabel != null && actionListener != null) {
            snackbar.get().setAction(actionLabel, actionListener);
        }
    }

    @SuppressLint("ResourceType")
    private void themeWarning(@NonNull Context context, @NonNull Snackbar.SnackbarInterface snackbarWarning)
    {
        int[] colorAttrs = { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor, R.attr.selectableItemBackground };
        TypedArray a = context.obtainStyledAttributes(colorAttrs);
        int textColor = ContextCompat.getColor(context, a.getResourceId(0, android.R.color.primary_text_dark));
        int accentColor = ContextCompat.getColor(context, a.getResourceId(1, R.color.text_accent_dark));
        int backgroundColor = ContextCompat.getColor(context, a.getResourceId(2, R.color.card_bg_dark));
        Drawable buttonDrawable = ContextCompat.getDrawable(context, a.getResourceId(3, R.drawable.button_fab_dark));
        int buttonPadding = (int)context.getResources().getDimension(R.dimen.snackbar_button_padding);
        a.recycle();

        View snackbarView = snackbarWarning.get().getView();
        snackbarView.setBackgroundColor(backgroundColor);
        snackbarWarning.get().setActionTextColor(accentColor);

        TextView snackbarText = (TextView)snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        if (snackbarText != null) {
            snackbarText.setTextColor(textColor);
            snackbarText.setMaxLines(5);
        }

        View snackbarAction = snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        if (snackbarAction != null) {
            if (Build.VERSION.SDK_INT >= 16) {
                snackbarAction.setBackground(buttonDrawable);
                snackbarAction.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            }
        }
    }

    private final Snackbar.Callback snackbarListener = new Snackbar.Callback()
    {
        @Override
        public void onShown(Snackbar.SnackbarInterface snackbar) {}

        @Override
        public void onDismissed(Snackbar.SnackbarInterface snackbar, int event)
        {
            switch (event)
            {
                case DISMISS_EVENT_SWIPE:
                    wasDismissed = true;
                    snackbar.getView().post(new Runnable() {
                        @Override
                        public void run() {
                            showNextWarning();
                        }
                    });
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
        return (snackbar != null && snackbar.get().isShown());
    }

    public void show()
    {
        if (snackbar != null) {
            snackbar.show();
            snackbar.get().getView().post(new Runnable() {
                @Override
                public void run() {
                    snackbar.get().getView().requestFocus();
                }
            });
        }
        announceWarning();
    }

    public void dismiss()
    {
        if (isShown()) {
            snackbar.get().dismiss();
        }
    }

    public void reset()
    {
        wasDismissed = false;
        shouldShow = false;
    }

    public void setContentDescription( CharSequence value )
    {
        this.contentDescription = value;
        if (snackbar != null) {
            TextView snackText = (TextView) snackbar.get().getView().findViewById(android.support.design.R.id.snackbar_text);
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
            outState.putCharSequence(id + "_" + KEY_MESSAGE, message);
            outState.putCharSequence(id + "_" + KEY_CONTENT_DESCRIPTION, contentDescription);
            outState.putCharSequence(id + "_" + KEY_ACTION_LABEL, actionLabel);
            outState.putBoolean(id + "_" + KEY_WASDISMISSED, wasDismissed);
            outState.putBoolean(id + "_" + KEY_SHOULD_SHOW, shouldShow);
            outState.putInt(id + "_" + KEY_DURATION, duration);
        }
    }

    public void restore( Bundle savedState )
    {
        if (savedState != null)
        {
            message = savedState.getCharSequence(id + "_" + KEY_MESSAGE);
            contentDescription = savedState.getCharSequence(id + "_" + KEY_CONTENT_DESCRIPTION);
            actionLabel = savedState.getCharSequence(id + "_" + KEY_ACTION_LABEL);
            wasDismissed = savedState.getBoolean(id + "_" + KEY_WASDISMISSED, false);
            shouldShow = savedState.getBoolean(id + "_" + KEY_SHOULD_SHOW, false);
            duration = savedState.getInt(id + "_" + KEY_DURATION, duration);
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
