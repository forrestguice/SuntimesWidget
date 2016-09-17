/**
    Copyright (C) 2014 Forrest Guice
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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.forrestguice.suntimeswidget.calculator.SuntimesDataset;
import com.forrestguice.suntimeswidget.settings.WidgetSettings;

/**
 * LightMapView .. a stacked bar graph over the duration of a day showing relative duration of
 * night, day, and twilight times.
 */
public class LightMapView extends LinearLayout
{
   // private FragmentActivity myParent;
    //private boolean isInitialized = false;
    private ImageView mainView;

    public LightMapView(Context context)
    {
        super(context);
        initLayout(context);
        initViews(context);
    }

    public LightMapView(Context context, AttributeSet attribs)
    {
        super(context, attribs);
        initLayout(context);
        initViews(context);
    }

    private void initLayout(Context context)
    {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.info_time_lightmap, this);
    }

    /**
     *
     * @param context
     */
    protected void initViews( Context context )
    {
        Log.d("DEBUG", "LightMapView initViews");
        //WidgetSettings.initDisplayStrings(context);

        mainView = (ImageView) findViewById(R.id.lightmap_view);
    }

    public void onResume()
    {
        Log.d("DEBUG", "LightMapView onResume");
    }

    private static final int LAYER_NIGHT = 0;
    private static final int LAYER_ASTRO = 1;
    private static final int LAYER_NAUTICAL = 2;
    private static final int LAYER_CIVIL = 3;
    private static final int LAYER_DAY = 4;


    /**
     * @param data
     */
    public void updateViews(SuntimesDataset data)
    {
        if (mainView != null)
        {
            LayerDrawable d = (LayerDrawable) getContext().getResources().getDrawable(R.drawable.lightmap);

            int offT = 0, offB = 0;
            int offL_astro = 0, offR_astro = 0;
            int offL_nautical = 0, offR_nautical = 0;
            int offL_civil = 0, offR_civil = 0;
            int offL_day = 0, offR_day = 0;

            int w = (int)getContext().getResources().getDimensionPixelSize(R.dimen.graph_width);
            Log.d("DEBUG", "lightmap width is " + w);
            offL_day = offR_day = w/2;

            d.setLayerInset(LAYER_ASTRO, offL_astro, offT, offR_astro, offB);
            d.setLayerInset(LAYER_NAUTICAL, offL_nautical, offT, offR_nautical, offB);
            d.setLayerInset(LAYER_CIVIL, offL_civil, offT, offR_civil, offB);
            d.setLayerInset(LAYER_DAY, offL_day, offT, offR_day, offB);

            mainView.setImageDrawable(d);
            Log.d("updateViews", "lightmap updated");
        }
    }

    /**
     *
     */
    protected void loadSettings(Context context)
    {
        Log.d("DEBUG", "LightMapView loadSettings (prefs)");
        if (isInEditMode())
            return;
    }

    /**
     *
     */
    protected void loadSettings(Context context, Bundle bundle )
    {
        Log.d("DEBUG", "LightMapView loadSettings (bundle)");
    }

    /**
     *
     */
    protected boolean saveSettings(Context context)
    {
        Log.d("DEBUG", "LightMap loadSettings (prefs)");
        return false;
    }

    /**
     * @param bundle
     * @return
     */
    protected boolean saveSettings(Bundle bundle)
    {
        Log.d("DEBUG", "LightMapView saveSettings (bundle)");
        return true;
    }

}
