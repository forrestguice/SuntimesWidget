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

package com.forrestguice.suntimeswidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;

@SuppressWarnings("Convert2Diamond")
public class SuntimesLaunchActivity extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);

        if (AppSettings.isFirstLaunch(this)) {
            showWelcome(this);

        } else {
            showMain();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        showMain();
    }

    public static final int WELCOME_REQUEST = 2300;
    public static void showWelcome(@NonNull Activity activity)
    {
        Intent intent = new Intent(activity, WelcomeActivity.class);
        activity.startActivityForResult(intent, WELCOME_REQUEST);
    }

    protected void showMain()
    {
        startActivity(mainIntent());
        finish();
    }

    protected Intent mainIntent()
    {
        String mode = AppSettings.loadLauncherModePref(this);
        Intent intent;
        if (mode.equals(AlarmClockActivity.class.getSimpleName())) {
            intent = new Intent(this, AlarmClockActivity.class);
        } else {
            intent = new Intent(this, SuntimesActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

}
