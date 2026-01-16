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

import android.content.Intent;
import android.os.Bundle;

import com.forrestguice.suntimeswidget.alarmclock.ui.AlarmClockActivity;
import com.forrestguice.suntimeswidget.settings.AppSettings;
import com.forrestguice.support.app.AppCompatActivity;

public class SuntimesLaunchActivity extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);

        if (AppSettings.isFirstLaunch(this)) {
            showWelcome();

        } else {
            showMain();
        }
    }

    @Override
    public void onActivityResultCompat(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResultCompat(requestCode, resultCode, data);
        showMain();
    }

    public static final int WELCOME_REQUEST = 2300;
    private final ActivityResultLauncherCompat startActivityForResult_welcome = registerForActivityResultCompat(WELCOME_REQUEST);

    public void showWelcome()
    {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivityForResult_welcome.launch(intent);
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
