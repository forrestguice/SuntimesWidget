/**
    Copyright (C) 2017-2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings.colors;

import android.app.Dialog;
import com.forrestguice.support.arch.lifecycle.Observer;
import com.forrestguice.support.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;
import com.forrestguice.support.design.widget.BottomSheetBehaviorInterface;
import com.forrestguice.support.design.widget.BottomSheetDialogFragment;
import com.forrestguice.support.design.widget.TabLayout;
import com.forrestguice.support.design.view.ViewPager;
import com.forrestguice.support.design.widget.LinearSnapHelper;
import com.forrestguice.support.design.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.suntimeswidget.settings.colors.pickers.ColorPickerFragment;
import com.forrestguice.suntimeswidget.settings.colors.pickers.ColorPickerPagerAdapter;

import java.util.ArrayList;

public class ColorDialog extends BottomSheetDialogFragment
{
    public static final String PREFS_COLORDIALOG = "ColorDialog";
    public static final String KEY_COLORPICKER = "colorPicker";
    public static final String KEY_SHOWALPHA = "showAlpha";
    public static final String KEY_COLOR = "color";
    public static final String KEY_COLOR_UNDER = "color_under";
    public static final String KEY_COLOR_OVER = "color_over";
    public static final String KEY_PREVIEW_MODE = "previewMode";
    public static final String KEY_RECENT = "recentColors";
    public static final String KEY_SUGGESTED = "suggestedColor";
    public static final String KEY_LABEL = "color_label";

    public ColorDialog() {
        setArguments(colorPagerArgs);
    }

    private Button btn_suggest;
    private Button btn_cancel;
    private ViewPager colorPager;
    protected Bundle colorPagerArgs = new Bundle();
    private ColorPickerFragment.ColorPickerModel viewModel;

    private RecyclerView recentColors;
    private ColorsAdapter recentColors_adapter;

    public int getColor() {
        return colorPagerArgs.getInt(KEY_COLOR, Color.WHITE);
    }
    public void setColor( int color )
    {
        colorPagerArgs.putInt(KEY_COLOR, color);
        if (viewModel != null) {
            viewModel.setColor(color);
        }
        if (recentColors_adapter != null) {
            recentColors_adapter.setSelectedColor(color);
        }
        if (colorDialogListener != null) {
            colorDialogListener.onColorChanged(color);
        }
    }

    @Nullable
    public Integer suggestedColor()
    {
        int suggested = colorPagerArgs.getInt(KEY_SUGGESTED, Integer.MIN_VALUE);
        return (suggested == Integer.MIN_VALUE) ? null : suggested;
    }
    public void setSuggestedColor(@Nullable Integer color)
    {
        if (color == null || color == Integer.MIN_VALUE) {
            colorPagerArgs.remove(KEY_SUGGESTED);
        } else {
            colorPagerArgs.putInt(KEY_SUGGESTED, color);
        }
        if (isAdded()) {
            updateViews(getActivity());
        }
    }

    @Nullable
    public String getColorLabel() {
        return colorPagerArgs.getString(KEY_LABEL, null);
    }
    public void setColorLabel(String value) {
        colorPagerArgs.putString(KEY_LABEL, value);
    }

    public boolean showAlpha() {
        return colorPagerArgs.getBoolean(KEY_SHOWALPHA, false);
    }
    public void setShowAlpha(boolean value)
    {
        colorPagerArgs.putBoolean(KEY_SHOWALPHA, value);
        if (viewModel != null) {
            viewModel.setShowAlpha(value);
        }
        filterRecentColors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        Context context = getContext();
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(context));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_colors, parent, false);

        viewModel = ViewModelProviders.of(getActivity()).get(ColorPickerFragment.ColorPickerModel.class);
        viewModel.setColor(getArguments().getInt(KEY_COLOR));
        viewModel.setColorUnder(getArguments().getInt(KEY_COLOR_UNDER));
        viewModel.setColorOver(getArguments().getInt(KEY_COLOR_OVER));
        viewModel.setPreviewMode(getArguments().getInt(KEY_PREVIEW_MODE));
        viewModel.setShowAlpha(showAlpha());

        if (savedState != null)
        {
            setColor(savedState.getInt(KEY_COLOR, getColor()));
            setShowAlpha(savedState.getBoolean(KEY_SHOWALPHA, showAlpha()));
            setRecentColors(savedState.getIntegerArrayList(KEY_RECENT));
        }
        initViews(getActivity(), dialogContent);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_COLORDIALOG, Context.MODE_PRIVATE);
        colorPager.setCurrentItem(prefs.getInt(KEY_COLORPICKER, 0));

        return dialogContent;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(onKeyListener);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        dialog.setOnShowListener(onShowListener);

        return dialog;
    }


    private final DialogInterface.OnShowListener onShowListener = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog)
        {
            Context context = getActivity();
            if (AppSettings.isTelevision(getActivity())) {
                btn_cancel.requestFocus();
            }
        }
    };

    private DialogInterface.OnKeyListener onKeyListener = new DialogInterface.OnKeyListener()
    {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
        {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)
            {
                getDialog().cancel();
                if (colorDialogListener != null) {
                    colorDialogListener.onCanceled();
                }
                return true;
            }
            return false;
        }
    };

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_COLOR, getColor());
        outState.putBoolean(KEY_SHOWALPHA, showAlpha());

        @SuppressWarnings("unchecked")
        ArrayList<Integer> colors = (ArrayList<Integer>)recentColors_list.clone();
        outState.putIntegerArrayList(KEY_RECENT, colors);
    }

    private void initViews(Context context, View dialogContent)
    {
        TextView subtitle = (TextView) dialogContent.findViewById(R.id.dialog_subtitle);
        if (subtitle != null)
        {
            String colorLabel = getColorLabel();
            if (colorLabel != null) {
                subtitle.setText(colorLabel);
                subtitle.setVisibility(View.VISIBLE);
            } else {
                subtitle.setVisibility(View.GONE);
            }
        }

        TabLayout colorPagerTabs = (TabLayout) dialogContent.findViewById(R.id.color_pager_tabs);
        colorPager = (ViewPager) dialogContent.findViewById(R.id.color_pager);

        colorPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position)
            {
                Context context = getContext();
                if (context != null)
                {
                    SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_COLORDIALOG, Context.MODE_PRIVATE).edit();
                    prefs.putInt(KEY_COLORPICKER, position);
                    prefs.apply();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        ColorPickerPagerAdapter colorPagerAdapter = new ColorPickerPagerAdapter(this);
        colorPagerAdapter.setAdapterListener(new ColorPickerPagerAdapter.AdapterListener()
        {
            @Override
            public void onColorChanged(int value) {
                setColor(value);
            }
        });
        colorPager.setAdapter(colorPagerAdapter);

        colorPagerTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(colorPager));
        colorPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(colorPagerTabs));

        recentColors_adapter = new ColorsAdapter(recentColors_list);
        recentColors_adapter.setOnColorButtonClickListener(new ColorChangeListener() {
            @Override
            public void onColorChanged(int color) {
                setColor(color);
            }
        });

        recentColors = (RecyclerView)dialogContent.findViewById(R.id.color_recent);
        recentColors.setHasFixedSize(true);
        recentColors.setItemViewCacheSize(16);
        recentColors.setAdapter(recentColors_adapter);
        recentColors.scrollToPosition(0);

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recentColors);

        btn_suggest = (Button) dialogContent.findViewById(R.id.dialog_button_suggest);
        if (btn_suggest != null) {
            btn_suggest.setOnClickListener(onDialogSuggestClick);
        }

        btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
        if (btn_cancel != null) {
            btn_cancel.setOnClickListener(onDialogCancelClick);
        }

        Button btn_accept = (Button) dialogContent.findViewById(R.id.dialog_button_accept);
        if (btn_accept != null) {
            btn_accept.setOnClickListener(onDialogAcceptClick);
        }

        viewModel.color.observe(getActivity(), new Observer<Integer>()
        {
            @Override
            public void onChanged(@Nullable Integer value)
            {
                if (isAdded() && getView() != null) {
                    updateViews(getActivity());
                }
            }
        });

        updateViews(context);
    }

    public void updateViews(Context context)
    {
        if (btn_suggest != null) {
            btn_suggest.setVisibility(suggestedColor() != null ? View.VISIBLE : View.GONE);
        }
    }

    private final View.OnClickListener onDialogSuggestClick = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            Integer color = suggestedColor();
            if (color != null) {
                setColor(color);
            }
        }
    };

    private final View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
            if (colorDialogListener != null) {
                colorDialogListener.onCanceled();
            }
        }
    };

    private final View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            dismiss();
            if (colorDialogListener != null) {
                colorDialogListener.onAccepted(getColor());
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetBehaviorInterface behavior = initBottomSheetBehavior(dialog);
        if (behavior != null)
        {
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehaviorInterface.STATE_EXPANDED);
        }
    }

    private ArrayList<Integer> recentColors_list = new ArrayList<>();
    public void setRecentColors(ArrayList<Integer> colors)
    {
        recentColors_list.clear();

        if (colors != null) {
            recentColors_list.addAll(colors);
            filterRecentColors();
        }

        if (recentColors_adapter != null) {
            recentColors_adapter.setColors(recentColors_list);
        }
    }

    private void filterRecentColors()
    {
        if (!showAlpha()) {
            for (int i=recentColors_list.size()-1; i >= 0; i--) {
                Integer color = recentColors_list.get(i);
                if (color != null && Color.alpha(color) != 255) {
                    recentColors_list.remove(color);
                }
            }
        }
    }

    /**
     * ColorDialogListener
     */
    public interface ColorDialogListener extends ColorChangeListener
    {
        void onAccepted(int color);
        void onCanceled();
    }
    public ColorDialogListener colorDialogListener = null;
    public void setColorDialogListener( ColorDialogListener listener ) {
        this.colorDialogListener = listener;
    }

}
