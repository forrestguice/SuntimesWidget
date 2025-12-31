/**
    Copyright (C) 2024 Forrest Guice
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

package com.forrestguice.suntimeswidget.settings.colors.pickers;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.settings.colors.ColorChangeListener;
import com.forrestguice.suntimeswidget.settings.colors.ColorsAdapter;
import com.forrestguice.support.widget.GridLayoutManager;

import java.util.ArrayList;

public class MaterialColorPickerFragment extends ColorPickerFragment
{
    protected ColorsAdapter adapter;
    protected RecyclerView grid;
    protected MaterialColorPickerModel materialColorModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        materialColorModel = ViewModelProviders.of(getActivity()).get(MaterialColorPickerModel.class);

        View view = inflater.inflate(R.layout.layout_colors_material, container, false);
        initViews(getContext(), view);
        updateViews(getContext());
        return view;
    }

    protected void initViews(Context context, View view)
    {
        super.initViews(context, view);
        adapter = new ColorsAdapter(materialColorModel.materialColorList(context));
        adapter.setSelectedColor(getColor());
        adapter.setItemLayoutResID(R.layout.layout_listitem_color1);
        adapter.setOnColorButtonClickListener(new ColorChangeListener()
        {
            @Override
            public void onColorChanged(int color)
            {
                setColor(color, true);
                clearListeners();
                updateViews(getActivity());
                setListeners();
            }
        });

        grid = (RecyclerView) view.findViewById(R.id.color_grid);
        grid.setHasFixedSize(true);
        grid.setLayoutManager(new GridLayoutManager(getActivity(), 14, GridLayoutManager.HORIZONTAL, false));
        grid.setAdapter(adapter);
        grid.scrollToPosition(0);
    }

    @Override
    protected void setListeners() { /* EMPTY */ }

    @Override
    protected void clearListeners() { /* EMPTY */ }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateViews(Context context)
    {
        super.updateViews(context);
        adapter.setSelectedColor(getColor());
    }

    /**
     * MaterialColorPickerModel
     */
    public static class MaterialColorPickerModel extends ViewModel
    {
        protected ArrayList<Integer> materialColorList(@NonNull Context context)
        {
            if (materialColors == null)
            {
                long bench_start = System.nanoTime();
                ArrayList<Integer> colorList = new ArrayList<Integer>();
                addAll(colorList, context.getResources().getIntArray(R.array.material_cyan));
                addAll(colorList, context.getResources().getIntArray(R.array.material_teal));
                addAll(colorList, context.getResources().getIntArray(R.array.material_green));
                addAll(colorList, context.getResources().getIntArray(R.array.material_light_green));
                addAll(colorList, context.getResources().getIntArray(R.array.material_lime));
                addAll(colorList, context.getResources().getIntArray(R.array.material_yellow));
                addAll(colorList, context.getResources().getIntArray(R.array.material_amber));
                addAll(colorList, context.getResources().getIntArray(R.array.material_orange));
                addAll(colorList, context.getResources().getIntArray(R.array.material_deep_orange));
                addAll(colorList, context.getResources().getIntArray(R.array.material_red));
                addAll(colorList, context.getResources().getIntArray(R.array.material_pink));
                addAll(colorList, context.getResources().getIntArray(R.array.material_purple));
                addAll(colorList, context.getResources().getIntArray(R.array.material_deep_purple));
                addAll(colorList, context.getResources().getIntArray(R.array.material_indigo));
                addAll(colorList, context.getResources().getIntArray(R.array.material_blue));
                addAll(colorList, context.getResources().getIntArray(R.array.material_light_blue));
                addAll(colorList, context.getResources().getIntArray(R.array.material_blue_grey));
                addAll(colorList, context.getResources().getIntArray(R.array.material_grey));
                addAll(colorList, context.getResources().getIntArray(R.array.material_brown));
                long bench_end = System.nanoTime();
                Log.d("DEBUG", "materialColorList :: " + ((bench_end - bench_start) / 1000000.0) + " ms; loaded " + colorList.size() + " colors.");
                materialColors = colorList;
            }
            return materialColors;
        }
        protected ArrayList<Integer> materialColors = null;

        protected void addAll(ArrayList<Integer> list, int[] values) {
            for (int i=0; i<values.length; i++) {
                list.add(values[i]);
            }
        }
    }

}
