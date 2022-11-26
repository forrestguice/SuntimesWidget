/**
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimeswidget.equinox;

import android.view.View;

import com.forrestguice.suntimeswidget.settings.WidgetSettings;

public class EquinoxAdapterListener
{
    public void onClick( int position ) {}
    public boolean onLongClick( int position ) { return false; }
    public void onTitleClick( int position ) {}
    public void onNextClick( int position ) {}
    public void onPrevClick( int position ) {}
    public void onSelected( int position, WidgetSettings.SolsticeEquinoxMode mode ) {}
    public void onMenuClick( View v, int position ) {}
    public void onMenuClick( View v, int position, WidgetSettings.SolsticeEquinoxMode mode, long datetime ) {}
}