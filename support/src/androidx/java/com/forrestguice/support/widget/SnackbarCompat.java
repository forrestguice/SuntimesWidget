package com.forrestguice.support.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.support.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarCompat
{
    protected Snackbar snackbar;
    public SnackbarCompat(Snackbar snackbar) {
        this.snackbar = snackbar;
    }

    public void addCallback(final Callback listener)
    {
        this.snackbar.setCallback(new Snackbar.Callback()
        {
            @Override
            public void onShown(Snackbar sb) {
                super.onShown(sb);
                listener.onShown(SnackbarCompat.this);
            }
            public void onDismissed(Snackbar sb, int event) {
                listener.onDismissed(SnackbarCompat.this, event);
                super.onDismissed(sb, event);
            }
        });
    }

    public void setAction(String text, View.OnClickListener listener) {
        snackbar.setAction(text, listener);
    }

    public Snackbar getSnackbar() {
        return snackbar;
    }

    public static SnackbarCompat from(Snackbar snackbar) {
        return new SnackbarCompat(snackbar);
    }

    public static final int LENGTH_INDEFINITE = Snackbar.LENGTH_INDEFINITE;
    public static final int LENGTH_SHORT = Snackbar.LENGTH_SHORT;
    public static final int LENGTH_LONG = Snackbar.LENGTH_LONG;

    public static int getSnackbarTextResourceID() {
        return android.support.design.R.id.snackbar_text;    // support libraries
        //return com.google.android.material.R.id.snackbar_text;   // androidx
    }

    public static int getSnackbarActionResourceID() {
        return android.support.design.R.id.snackbar_action;    // support libraries
        //return com.google.android.material.R.id.snackbar_action;   // androidx
    }

    public static Snackbar make(@NonNull Context context, @NonNull View view, @NonNull CharSequence text, int duration, @Nullable SnackbarTheme theme)
    {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        if (theme != null) {
            themeSnackbar(context, snackbar, theme, null);
        }
        return snackbar;
    }

    public interface SnackbarTheme
    {
        int[] colorAttrs(); /*{
            return { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor, R.attr.selectableItemBackground };
        }*/
        int[] colorAttrs_defaults(); /*{
            return { android.R.color.primary_text_dark, R.color.text_accent_dark, R.color.card_bg_dark, R.drawable.button_fab_dark };
        }*/
        int resId_buttonPadding(); /*{
            return R.dimen.snackbar_button_padding;
        }*/
    }

    @SuppressLint("ResourceType")
    protected static void themeSnackbar(Context context, Snackbar snackbar, SnackbarTheme theme, @Nullable Integer[] colorOverrides)
    {
        Integer[] colors = new Integer[] { null, null, null };
        int[] colorAttr = theme.colorAttrs();
        int[] colorAttrDef = theme.colorAttrs_defaults();
        TypedArray a = context.obtainStyledAttributes(colorAttr);
        colors[0] = ContextCompat.getColor(context, a.getResourceId(0, colorAttrDef[0]));
        colors[1] = ContextCompat.getColor(context, a.getResourceId(1, colorAttrDef[1]));
        colors[2] = ContextCompat.getColor(context, a.getResourceId(2, colorAttrDef[2]));
        Drawable buttonDrawable = ContextCompat.getDrawable(context, a.getResourceId(3, colorAttrDef[3]));
        int buttonPadding = (int)context.getResources().getDimension(theme.resId_buttonPadding());
        a.recycle();

        if (colorOverrides != null && colorOverrides.length == colors.length) {
            for (int i=0; i<colors.length; i++) {
                if (colorOverrides[i] != null) {
                    colors[i] = colorOverrides[i];
                }
            }
        }

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(colors[2]);
        snackbar.setActionTextColor(colors[1]);

        TextView snackbarText = (TextView)snackbarView.findViewById(getSnackbarTextResourceID());
        if (snackbarText != null) {
            snackbarText.setTextColor(colors[0]);
            snackbarText.setMaxLines(3);
        }

        View snackbarAction = snackbarView.findViewById(getSnackbarActionResourceID());
        if (snackbarAction != null) {
            if (Build.VERSION.SDK_INT >= 16)
            {
                snackbarAction.setBackground(buttonDrawable);
                snackbarAction.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            }
        }
    }

    public static class Callback
    {
        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        public void onShown(SnackbarCompat snackbar) {}
        public void onDismissed(SnackbarCompat snackbarCompat, int event) {}
    }

}
