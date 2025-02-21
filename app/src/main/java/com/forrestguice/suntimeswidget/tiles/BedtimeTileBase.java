/**
    Copyright (C) 2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.tiles;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.forrestguice.support.annotation.Nullable;

@TargetApi(24)
public class BedtimeTileBase extends SuntimesTileBase
{
    public BedtimeTileBase(@Nullable Activity activity) {
        super(activity);
    }

    @Override
    protected int appWidgetId() {
        return BedtimeTileService.BEDTIMETILE_APPWIDGET_ID;
    }

    @Nullable
    protected Dialog createDialog(final Context context) {
        return null;
    }

    @Override
    protected Intent getConfigIntent(Context context) {
        return null;
    }

    @Override
    protected Intent getLaunchIntent(Context context) {
        return null;
    }

    @Override
    @Nullable
    protected Intent getLockScreenIntent(Context context) {
        return null;
    }

    @Override
    protected Drawable getDialogIcon(Context context) {
        return null;
    }

    @Override
    protected CharSequence formatDialogTitle(Context context) {
        return null;
    }

    @Override
    protected CharSequence formatDialogMessage(Context context) {
        return null;
    }

}
