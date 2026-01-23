/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.about;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.annotation.NonNull;

import java.lang.ref.WeakReference;

public class AboutPagerAdapter extends PagerAdapter
{
    protected final WeakReference<Context> contextRef;

    public AboutPagerAdapter(Context context) {
        super();
        contextRef = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position)
    {
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("instantiateItem: null context!");
        }
        View view;
        switch (position) {
            case 1: view = AboutAppView.newInstance(context, AboutAppView.LAYOUT_CONTRIBUTIONS ); break;
            case 2: view = AboutAppView.newInstance(context, AboutAppView.LAYOUT_PRIVACY ); break;
            case 0: default: view = AboutAppView.newInstance(context, AboutAppView.LAYOUT_APP ); break;
        }
        collection.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == object);
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }
}
