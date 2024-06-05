// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020 Forrest Guice
    This file is part of Natural Hour.

    Natural Hour is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Natural Hour is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Natural Hour.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.colors;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.forrestguice.suntimeswidget.R;

public class ColorValuesFragment extends Fragment
{
    public static final String KEY_DIALOGTHEME = "themeResID";
    protected static final int DEF_DIALOGTHEME = R.style.AppTheme_Dark;

    public void setTheme(int themeResID)
    {
        Bundle args = (getArguments() != null) ? getArguments() : new Bundle();
        args.putInt(KEY_DIALOGTHEME, themeResID);
        setArguments(args);
    }
    public int getThemeResID() {
        return (getArguments() != null) ? getArguments().getInt(KEY_DIALOGTHEME, DEF_DIALOGTHEME) : DEF_DIALOGTHEME;
    }
}