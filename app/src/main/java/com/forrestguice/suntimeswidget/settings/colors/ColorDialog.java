/**
    Copyright (C) 2017-2020 Forrest Guice
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
import android.content.Context;
import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.slider.AlphaSlider;
import com.flask.colorpicker.slider.LightnessSlider;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class ColorDialog extends BottomSheetDialogFragment
{
    public static final String PREFS_COLORDIALOG = "ColorDialog";
    public static final String KEY_COLORPICKER = "colorPicker";
    public static final String KEY_SHOWALPHA = "showAlpha";
    public static final String KEY_COLOR = "color";
    public static final String KEY_RECENT = "recentColors";
    public static final String KEY_SUGGESTED = "suggestedColor";

    public ColorDialog() {
        setArguments(colorPagerArgs);
    }

    private Button btn_suggest;
    private Button btn_cancel;
    private ViewPager colorPager;
    private TabLayout colorPagerTabs;
    private ColorPickerPagerAdapter colorPagerAdapter;
    protected Bundle colorPagerArgs = new Bundle();

    private RecyclerView recentColors;
    private ColorsAdapter recentColors_adapter;

    public int getColor() {
        return colorPagerArgs.getInt(KEY_COLOR);
    }
    public void setColor( int color ) {
        colorPagerArgs.putInt(KEY_COLOR, color);
        if (colorPagerAdapter != null)
        {
            colorPagerAdapter.setColor(color);
            colorPagerAdapter.updateViews(getContext());
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

    public boolean showAlpha() {
        return colorPagerArgs.getBoolean(KEY_SHOWALPHA, false);
    }
    public void setShowAlpha(boolean value) {
        colorPagerArgs.putBoolean(KEY_SHOWALPHA, value);
        filterRecentColors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        Context context = getContext();
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(context));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_colors, parent, false);

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
        colorPagerTabs = (TabLayout) dialogContent.findViewById(R.id.color_pager_tabs);
        colorPager = (ViewPager) dialogContent.findViewById(R.id.color_pager);

        colorPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position)
            {
                Context context = getContext();
                if (context != null) {
                    SharedPreferences.Editor prefs = context.getSharedPreferences( PREFS_COLORDIALOG, Context.MODE_PRIVATE).edit();
                    prefs.putInt(KEY_COLORPICKER, position);
                    prefs.apply();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        colorPager.setAdapter(colorPagerAdapter = new ColorPickerPagerAdapter(getChildFragmentManager()));

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

        SnapHelper snapHelper = new LinearSnapHelper();
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

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
     * ColorChangeListener
     */
    public static abstract class ColorChangeListener {
        public void onColorChanged(int color) {}
    }

    /**
     * ColorDialogListener
     */
    public static abstract class ColorDialogListener extends ColorChangeListener
    {
        public void onAccepted(int color) {}
        public void onCanceled() {}
    }
    public ColorDialogListener colorDialogListener = null;
    public void setColorDialogListener( ColorDialogListener listener ) {
        this.colorDialogListener = listener;
    }

    /**
     * ColorsAdapter
     */
    public static class ColorsAdapter extends RecyclerView.Adapter<ColorViewHolder>
    {
        private ArrayList<Integer> colors = new ArrayList<>();

        public ColorsAdapter(List<Integer> colors) {
            this.colors.addAll(colors);
        }

        public void setColors(List<Integer> colors)
        {
            this.colors.clear();
            this.colors.addAll(colors);
            notifyDataSetChanged();
        }

        @Override
        public ColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layout = LayoutInflater.from(parent.getContext());
            View view = layout.inflate(R.layout.layout_listitem_color, parent, false);
            return new ColorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ColorViewHolder holder, int position)
        {
            Integer color = (position >= 0 && position < colors.size()) ? colors.get(position) : null;
            holder.bindColorToView(color);
            holder.colorButton.setOnClickListener(color != null ? onColorButtonClick(color) : null);
        }

        @Override
        public int getItemCount() {
            return colors.size();
        }


        private View.OnClickListener onColorButtonClick(final int color)
        {
            return new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if (onColorChangeListener != null) {
                        onColorChangeListener.onColorChanged(color);
                    }
                }
            };
        }

        private ColorChangeListener onColorChangeListener;
        public void setOnColorButtonClickListener( ColorChangeListener listener ) {
            onColorChangeListener = listener;
        }
    }

    /**
     * ColorViewHolder
     */
    public static class ColorViewHolder extends RecyclerView.ViewHolder
    {
        public Integer color;
        public ImageButton colorButton;

        public ColorViewHolder(View itemView) {
            super(itemView);
            colorButton = (ImageButton)itemView.findViewById(R.id.colorButton);
        }

        public void bindColorToView(Integer color)
        {
            this.color = color;

            if (color != null)
            {
                Drawable d = colorButton.getDrawable();
                if (d != null) {
                    GradientDrawable g = (GradientDrawable) d.mutate();
                    g.setColor(color);
                    g.invalidateSelf();
                }
            }
            colorButton.setVisibility(color != null ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * ColorPickerPagerAdapter
     */
    protected class ColorPickerPagerAdapter extends FragmentPagerAdapter
    {
        protected ColorPickerFragment[] fragments;

        public ColorPickerPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);

            if (Build.VERSION.SDK_INT >= 14) {
                fragments = new ColorPickerFragment[] { new QuadFlaskColorPickerFragment(), new QuadFlaskColorPickerFragment1(), new SimpleColorPickerFragment() };
            } else {
                fragments = new ColorPickerFragment[] { new SimpleColorPickerFragment() };
            }
        }

        @Override
        public Fragment getItem(int position)
        {
            fragments[position].setArguments(colorPagerArgs);
            return fragments[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            fragments[position] = (ColorPickerFragment)super.instantiateItem(container, position);
            fragments[position].setColorChangeListener(onColorChanged);
            return fragments[position];
        }

        private ColorChangeListener onColorChanged = new ColorChangeListener() {
            @Override
            public void onColorChanged(int color)
            {
                colorPagerArgs.putInt(KEY_COLOR, color);
                if (colorDialogListener != null) {
                    colorDialogListener.onColorChanged(color);
                }
                updateViews(getContext());
            }
        };

        @Override
        public int getCount() {
            return fragments.length;
        }

        public void setColor(int color)
        {
            for (ColorPickerFragment fragment : fragments) {
                if (fragment != null) {
                    fragment.setColor(color);
                }
            }
        }

        public void updateViews(Context context)
        {
            for (ColorPickerFragment fragment : fragments) {
                if (fragment != null) {
                    fragment.updateViews(context);
                }
            }
        }
    }

    /**
     * ColorPickerFragment
     */
    public static class ColorPickerFragment extends Fragment
    {
        public ColorPickerFragment() {
            setArguments(new Bundle());
        }

        protected ColorDialog.ColorChangeListener listener;
        public void setColorChangeListener(ColorDialog.ColorChangeListener listener) {
            this.listener = listener;
        }

        public void setColor( String hexValue ) {
            setColor(Color.parseColor(hexValue.trim()));
        }

        public void setColor( int color )
        {
            getArguments().putInt(KEY_COLOR, color);
            if (listener != null) {
                listener.onColorChanged(color);
            }
        }

        public int getColor() {
            return getArguments().getInt(KEY_COLOR, Color.WHITE);
        }

        public boolean showAlpha() {
            return getArguments().getBoolean("showAlpha", false);
        }

        public void updateViews(Context context) {}
    }

    /**
     * QuadFlaskColorPickerFragment
     * Flower Mode
     */
    public static class QuadFlaskColorPickerFragment extends ColorPickerFragment
    {
        protected AlphaSlider alphaSlider;
        protected LightnessSlider lightnessSlider;
        protected ColorPickerView colorPicker;
        protected View preview;

        protected int getLayoutResID() {
            return R.layout.layout_colors_quadflask;
        }

        protected void initViews(View view)
        {
            alphaSlider = (AlphaSlider) view.findViewById(R.id.color_alpha);
            lightnessSlider = (LightnessSlider) view.findViewById(R.id.color_lightness);
            colorPicker = (ColorPickerView) view.findViewById(R.id.color_picker);
            preview = view.findViewById(R.id.preview_color);

            colorPicker.setLightnessSlider(lightnessSlider);
            lightnessSlider.setColorPicker(colorPicker);
            alphaSlider.setColorPicker(colorPicker);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view = inflater.inflate(getLayoutResID(), container, false);
            initViews(view);

            colorPicker.addOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int color) {
                    setColor(color);
                }
            });

            updateViews(getContext());
            return view;
        }

        @Override
        public void updateViews(Context context)
        {
            alphaSlider.setVisibility(showAlpha() ? View.VISIBLE : View.GONE);
            lightnessSlider.post(new Runnable() {
                @Override
                public void run() {
                    lightnessSlider.setColor(getColor());
                    alphaSlider.setColor(getColor());
                }
            });
            colorPicker.setColor(getColor(), false);
            preview.setBackgroundColor(getColor());
        }
    }

    /**
     * QuadFlaskColorPickerFragment1
     * Circle Mode
     */
    public static class QuadFlaskColorPickerFragment1 extends QuadFlaskColorPickerFragment
    {
        @Override
        protected int getLayoutResID() {
            return R.layout.layout_colors_quadflask1;
        }

        @Override
        protected void initViews(View view)
        {
            alphaSlider = (AlphaSlider) view.findViewById(R.id.color_alpha1);
            lightnessSlider = (LightnessSlider) view.findViewById(R.id.color_lightness1);
            colorPicker = (ColorPickerView) view.findViewById(R.id.color_picker1);
            preview = view.findViewById(R.id.preview_color1);
        }
    }

}
