/**
    Copyright (C) 2025 Forrest Guice
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

package com.forrestguice.suntimeswidget.calculator.settings.android;

import android.content.Context;

import com.forrestguice.colors.Color;
import com.forrestguice.suntimeswidget.calculator.DefaultCalculatorDescriptors;
import com.forrestguice.suntimeswidget.calculator.SuntimesCalculatorDescriptor;
import com.forrestguice.suntimeswidget.calculator.SuntimesData;
import com.forrestguice.suntimeswidget.colors.android.AndroidColor;
import com.forrestguice.suntimeswidget.events.EventUri;
import com.forrestguice.suntimeswidget.events.android.AndroidEventAliasResolver;
import com.forrestguice.suntimeswidget.events.EventAlias;
import com.forrestguice.util.SystemTimeFormat;
import com.forrestguice.util.android.AndroidTimeFormat;

import com.forrestguice.suntimeswidget.BuildConfig;

public class AndroidSuntimesCalculator
{
    public static void init(Context context)
    {
        SystemTimeFormat.init(new AndroidTimeFormat(context.getApplicationContext()));
        SuntimesCalculatorDescriptor.initDefaultDescriptors(new DefaultCalculatorDescriptors());
        SuntimesData.initDataSettingsFactory(new AndroidSuntimesDataSettingsFactory());
        EventUri.setBuildConfigInfo(new BuildConfigInfo());
        EventAlias.initItemResolver(new AndroidEventAliasResolver());
        Color.init(new AndroidColor());
    }

    public static class BuildConfigInfo implements EventUri.BuildConfigInfo
    {
        @Override
        public String AUTHORITY_ROOT() {
            return BuildConfig.SUNTIMES_AUTHORITY_ROOT;
        }
    }
}
