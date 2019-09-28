/**
    Copyright (C) 2017-2019 Forrest Guice
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

import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.slider.AlphaSlider;
import com.forrestguice.suntimeswidget.R;

import java.util.ArrayList;
import java.util.List;

public class ColorDialog extends BottomSheetDialogFragment
{
    public ColorDialog() {}

    private ColorPickerView colorPicker;
    private AlphaSlider alphaSlider;

    private RecyclerView recentColors;
    private ColorsAdapter recentColors_adapter;
    private LinearLayoutManager recentColors_layout;

    private int color = Color.WHITE;
    public int getColor()
    {
        return color;
    }
    public void setColor( int color )
    {
        this.color = color;
    }

    private boolean showAlpha = false;
    public void setShowAlpha(boolean value)
    {
        this.showAlpha = value;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_colors, parent, false);

        if (savedState != null)
        {
            setColor(savedState.getInt("color", getColor()));
            showAlpha = savedState.getBoolean("showAlpha", showAlpha);
            setRecentColors(savedState.getIntegerArrayList("recentColors"));
        }
        initViews(getActivity(), dialogContent);

        return dialogContent;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putInt("color", getColor());
        outState.putBoolean("showAlpha", showAlpha);
        outState.putIntegerArrayList("recentColors", recentColors_list);
    }

    private void initViews(Context context, View dialogContent)
    {
        alphaSlider = (AlphaSlider)dialogContent.findViewById(R.id.color_alpha);
        alphaSlider.setVisibility(showAlpha ? View.VISIBLE : View.GONE);

        colorPicker = (ColorPickerView)dialogContent.findViewById(R.id.color_picker);
        colorPicker.setColor(color, false);
        colorPicker.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                setColor(color);
            }
        });

        recentColors_adapter = new ColorsAdapter(recentColors_list);
        recentColors_adapter.setOnColorButtonClickListener(new ColorChangeListener() {
            @Override
            public void onColorChanged(int color) {
                colorPicker.setColor(color, false);
                setColor(color);
            }
        });

        recentColors_layout = new LinearLayoutManager(context);
        recentColors_layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        recentColors = (RecyclerView)dialogContent.findViewById(R.id.color_recent);
        recentColors.setHasFixedSize(true);
        recentColors.setItemViewCacheSize(16);
        recentColors.setLayoutManager(recentColors_layout);
        recentColors.setAdapter(recentColors_adapter);
        recentColors.scrollToPosition(0);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recentColors);

        Button btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
        btn_cancel.setOnClickListener(onDialogCancelClick);

        Button btn_accept = (Button) dialogContent.findViewById(R.id.dialog_button_accept);
        btn_accept.setOnClickListener(onDialogAcceptClick);
    }

    private View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
        }
    };

    private View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            dismiss();
            if (colorChangeListener != null) {
                colorChangeListener.onColorChanged(getColor());
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
        recentColors_list.addAll(colors);

        if (recentColors_adapter != null) {
            recentColors_adapter.setColors(colors);
        }
    }

    /**
     * ColorChangeListener
     */
    public static abstract class ColorChangeListener
    {
        public void onColorChanged(int color) {}
    }
    public ColorChangeListener colorChangeListener = null;
    public void setColorChangeListener( ColorChangeListener listener )
    {
        this.colorChangeListener = listener;
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

}
