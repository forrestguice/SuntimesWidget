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
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.views.SpanUtils;
import com.forrestguice.support.app.AppCompatActivity;
import com.forrestguice.support.app.FragmentManagerCompat;

import java.lang.ref.WeakReference;

/**
 * WelcomeFragment
 */
public class WelcomeView extends FrameLayout
{
    public WelcomeView(Context context) {
        super(context);
        initView(context, R.layout.layout_welcome_app);
    }
    public WelcomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, R.layout.layout_welcome_app);
    }
    public WelcomeView(Context context, int layoutResID) {
        super(context);
        initView(context, layoutResID);
    }
    public WelcomeView(Context context, AttributeSet attrs, int layoutResID) {
        super(context, attrs);
        initView(context, layoutResID);
    }
    public WelcomeView(@NonNull AppCompatActivity activity, int layoutResID) {
        super(activity);
        setActivity(activity);
        initView(getContext(), layoutResID);
    }

    public static WelcomeView newInstance(AppCompatActivity activity, int layoutResID) {
        return new WelcomeView(activity, layoutResID);
    }

    protected WeakReference<AppCompatActivity> activityRef;
    public void setActivity(AppCompatActivity activity) {
        activityRef = new WeakReference<>(activity);
    }
    @Nullable
    protected AppCompatActivity getActivity() {
        return activityRef.get();
    }

    public void initView(Context context, int layoutResID)
    {
        this.layoutResID = layoutResID;
        inflate(context, getLayoutResID(), this);
        initViews(context, this);
        updateViews(context);
    }

    public void onResume() {
        updateViews(getContext());
    }

    public void initViews(Context context, View view)
    {
        if (view != null)
        {
            int[] textViews = new int[] { R.id.text0, R.id.text1, R.id.text2, R.id.text3 };
            for (int resID : textViews) {
                TextView text = (TextView) view.findViewById(resID);
                if (text != null) {
                    text.setText(SpanUtils.fromHtml(text.getText().toString()));
                }
            }

            textViews = new int[] { R.id.link0, R.id.link1, R.id.link2, R.id.link3, R.id.link4 };
            for (int resID : textViews) {
                TextView text = (TextView) view.findViewById(resID);
                if (text != null) {
                    text.setText(SpanUtils.fromHtml(AboutActivity.anchor(text.getText().toString())));
                    text.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            final TextView donateLink = (TextView) view.findViewById(R.id.link4);
            if (donateLink != null) {
                donateLink.setVisibility(View.GONE);
                donateLink.setText(SpanUtils.fromHtml(context.getString(R.string.app_donate_url, context.getString(R.string.app_name), context.getString(R.string.help_donate_url))));
            }

            CheckBox donateCheck = (CheckBox) view.findViewById(R.id.check_donate);
            if (donateCheck != null)
            {
                donateCheck.setChecked(false);
                donateCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (donateLink != null) {
                            donateLink.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                        }
                    }
                });
            }
        }
    }

    public void updateViews(Context context) {
        /* EMPTY */
    }

    public boolean validateInput(Context context) {
        return true;
    }

    public boolean saveSettings(Context context) {
        return true;
    }

    public void onActivityResultCompat(int requestCode, int resultCode, Intent data) {
        /* EMPTY */
    }

    protected boolean isAdded() {
        return (getFragmentManager() != null);
    }

    @Nullable
    protected FragmentManagerCompat getFragmentManager() {
        AppCompatActivity activity = activityRef.get();
        return (activity != null ? FragmentManagerCompat.from(activityRef.get()) : null);
    }

    protected LayoutInflater getLayoutInflater() {
        return (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    protected int layoutResID;
    public int getLayoutResID() {
        return layoutResID;
    }

    public int getPreferredIndex() {
        return 0;
    }

    /**
     * Args
     */
    protected Bundle args = new Bundle();
    @NonNull
    public Bundle getArgs() {
        return args;
    }
    public void setArgs( @NonNull Bundle values ) {
        args = values;
    }
    public void setArg(String key, String value) {
        getArgs().putString(key, value);
    }
    public void setArg(String key, boolean value) {
        getArgs().putBoolean(key, value);
    }
    public void setArg(String key, double value) {
        getArgs().putDouble(key, value);
    }

    /**
     * ViewState
     */
    protected static class ViewState extends BaseSavedState
    {
        protected static final String STATE = "WelcomeView.STATE";

        public ViewState(Parcelable superState, Bundle bundle)
        {
            super(superState);
            this.bundle = bundle;
        }

        private final Bundle bundle;
        public Bundle getBundle() {
            return bundle;
        }
    }

    @Override
    public Parcelable onSaveInstanceState()
    {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "onSaveInstanceState: " + getId());
        }
        Bundle bundle = new Bundle();
        ViewState state = new ViewState(super.onSaveInstanceState(), getArgs());
        bundle.putParcelable(ViewState.STATE, state);
        return bundle;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if (BuildConfig.DEBUG) {
            Log.d("DEBUG", "onRestoreInstanceState0: " + getId());
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            ViewState viewState = bundle.getParcelable(ViewState.STATE);
            if (viewState != null) {
                setArgs(viewState.getBundle());
                Log.d("DEBUG", "onRestoreInstanceState1: " + getId());
                super.onRestoreInstanceState(viewState.getSuperState());
            }
        }
        super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE);
    }
}
