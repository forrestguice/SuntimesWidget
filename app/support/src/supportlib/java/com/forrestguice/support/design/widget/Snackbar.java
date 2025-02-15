package com.forrestguice.support.design.widget;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Snackbar
{
    public static int getSnackbarActionResId() {
        return android.support.design.R.id.snackbar_action;
    }
    public static int getSnackbarTextResId() {
        return android.support.design.R.id.snackbar_text;
    }

    public static final int LENGTH_INDEFINITE = android.support.design.widget.Snackbar.LENGTH_INDEFINITE;
    public static final int LENGTH_SHORT = android.support.design.widget.Snackbar.LENGTH_SHORT;
    public static final int LENGTH_LONG = android.support.design.widget.Snackbar.LENGTH_LONG;

    @NonNull
    public static SnackbarInterface make(@NonNull View view, @NonNull CharSequence text, int duration) {
        return SnackbarCompat.create(android.support.design.widget.Snackbar.make(view, text, duration));
    }

    @NonNull
    public static SnackbarInterface make(@NonNull View view, @NonNull CharSequence text, int duration,
                                                              @Nullable CharSequence actionText, @Nullable View.OnClickListener actionListener)
    {
        int duration0 = (duration == LENGTH_LONG || duration == LENGTH_SHORT) ? duration : LENGTH_INDEFINITE;
        android.support.design.widget.Snackbar snackbar = android.support.design.widget.Snackbar.make(view, text, duration0);
        if (actionText != null) {
            snackbar.setAction(actionText, actionListener);
        }
        if (duration != duration0) {
            snackbar.setDuration(duration);
        }
        return SnackbarCompat.create(snackbar);
    }

    @SuppressLint("ResourceType")
    public static SnackbarInterface themeSnackbar(SnackbarInterface snackbar, Integer[] colors, Drawable buttonDrawable, int buttonPaddingPx)
    {
        View snackbarView = snackbar.get().getView();
        snackbarView.setBackgroundColor(colors[2]);
        snackbar.get().setActionTextColor(colors[1]);

        TextView snackbarText = (TextView)snackbarView.findViewById(getSnackbarTextResId());
        if (snackbarText != null) {
            snackbarText.setTextColor(colors[0]);
            snackbarText.setMaxLines(3);
        }

        View snackbarAction = snackbarView.findViewById(getSnackbarActionResId());
        if (snackbarAction != null) {
            if (Build.VERSION.SDK_INT >= 16)
            {
                snackbarAction.setBackground(buttonDrawable);
                snackbarAction.setPadding(buttonPaddingPx, buttonPaddingPx, buttonPaddingPx, buttonPaddingPx);
            }
        }
        return snackbar;
    }

    /**
     * Callback
     */
    public static abstract class Callback extends android.support.design.widget.Snackbar.Callback
    {
        public abstract void onShown(SnackbarInterface snackbar);
        public void onShown(android.support.design.widget.Snackbar snackbar) {
            onShown(SnackbarCompat.create(snackbar));
            super.onShown(snackbar);
        }

        public abstract void onDismissed(SnackbarInterface snackbar, int event);
        public void onDismissed(android.support.design.widget.Snackbar snackbar, int event) {
            onDismissed(SnackbarCompat.create(snackbar), event);
            super.onDismissed(snackbar, event);
        }
    }

    /**
     * SnackbarInterface
     */
    public interface SnackbarInterface
    {
        void show();
        android.support.design.widget.Snackbar get();
    }

    public static class SnackbarCompat implements SnackbarInterface
    {
        public SnackbarCompat(android.support.design.widget.Snackbar snackbar) {
            this.snackbar = snackbar;
        }
        protected android.support.design.widget.Snackbar snackbar;

        public void show() {
            snackbar.show();
        }

        public android.support.design.widget.Snackbar get() {
            return snackbar;
        }

        public static SnackbarInterface create(android.support.design.widget.Snackbar snackbar) {
            return new SnackbarCompat(snackbar);
        }
    }
}
