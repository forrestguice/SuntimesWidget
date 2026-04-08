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
import android.util.AttributeSet;
import android.view.View;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.app.AppCompatActivity;

public class WelcomeFirstPageView extends WelcomeView
{
    public WelcomeFirstPageView(Context context) {
        super(context, R.layout.layout_welcome_app);
    }
    public WelcomeFirstPageView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.layout_welcome_app);
    }
    public WelcomeFirstPageView(AppCompatActivity activity) {
        super(activity, R.layout.layout_welcome_app);
    }
    public static WelcomeFirstPageView newInstance(AppCompatActivity activity) {
        return new WelcomeFirstPageView(activity);
    }

    @Override
    public void initViews(Context context, View view) {
        super.initViews(context, view);
    }
}
