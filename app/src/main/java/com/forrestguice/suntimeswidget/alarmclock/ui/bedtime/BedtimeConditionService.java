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

package com.forrestguice.suntimeswidget.alarmclock.ui.bedtime;

import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.ConditionProviderService;
import android.support.annotation.Nullable;

public class BedtimeConditionService extends ConditionProviderService
{
    @Override
    public void onConnected() {
    }

    @Override
    public void onSubscribe(Uri conditionId) {
    }

    @Override
    public void onUnsubscribe(Uri conditionId) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }
    protected class LocalBinder extends Binder {
        BedtimeConditionService getService() {
            return BedtimeConditionService.this;
        }
    }
}
