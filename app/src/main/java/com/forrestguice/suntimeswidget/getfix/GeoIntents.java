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

package com.forrestguice.suntimeswidget.getfix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;

import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.views.Toast;

import java.util.ArrayList;
import java.util.List;

public class GeoIntents
{
    public static void shareLocation(Context context, Uri location)
    {
        Intent geoIntent0 = new Intent(Intent.ACTION_VIEW);
        geoIntent0.setData(location);

        Intent geoIntent1 = new Intent(Intent.ACTION_VIEW);
        geoIntent1.addCategory(Intent.CATEGORY_BROWSABLE);
        geoIntent1.setData(location);

        List<Intent> geoIntents = new ArrayList<Intent>();
        GeoIntents.buildIntentChooserList(context, geoIntents, geoIntent0);
        GeoIntents.buildIntentChooserList(context, geoIntents, geoIntent1);

        if (geoIntents.size() <= 0) {
            Toast.makeText(context, context.getString(R.string.configAction_mapLocation_noapp), Toast.LENGTH_LONG).show();
            return;
        }

        Intent chooserIntent = Intent.createChooser(geoIntents.remove(0), context.getString(R.string.configAction_mapLocation_chooser));
        if (Build.VERSION.SDK_INT >= 23) {
            chooserIntent.putExtra(Intent.EXTRA_ALTERNATE_INTENTS, geoIntents.toArray(new Parcelable[0]));
        } else {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, geoIntents.toArray(new Parcelable[0]));
        }
        context.startActivity(chooserIntent);
    }

    public static void buildIntentChooserList(Context context, List<Intent> intents, Intent forIntent)
    {
        List<ResolveInfo> info = context.getPackageManager().queryIntentActivities(forIntent, 0);
        if (!info.isEmpty())
        {
            for (ResolveInfo resolveInfo : info)
            {
                String packageName = resolveInfo.activityInfo.packageName;
                if (!TextUtils.equals(packageName, BuildConfig.APPLICATION_ID))
                {
                    Intent intent = new Intent(forIntent.getAction());
                    intent.setPackage(packageName);
                    intent.setData(forIntent.getData());
                    intents.add(intent);
                }
            }
        }
    }
}
