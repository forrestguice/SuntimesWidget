/**
    Copyright (C) 2019-2023 Forrest Guice
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

package com.forrestguice.suntimeswidget.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

import java.io.File;
import java.util.Calendar;

public class ShareUtils
{
    /**
     * shareItem; copy event display string and formatted timestamp to the clipboard.
     */
    public static void shareItem(Context context, @Nullable String itemString, long itemMillis, boolean showTime, boolean showSeconds)
    {
        if (itemMillis != -1L)
        {
            Calendar itemTime = Calendar.getInstance();
            itemTime.setTimeInMillis(itemMillis);

            SuntimesUtils utils = new SuntimesUtils();
            SuntimesUtils.initDisplayStrings(context);
            String itemDisplay = context.getString(R.string.share_format, (itemString != null ? itemString : ""), utils.calendarDateTimeDisplayString(context, itemTime, showTime, showSeconds).toString());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText((itemString != null ? itemString : itemDisplay), itemDisplay));
                }
            } else {
                @SuppressWarnings("deprecation")
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setText(itemDisplay);
                }
            }
            Toast.makeText(context, itemDisplay, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * shareFile
     */
    public static void shareFile(Context context, String authority, File file, String mimetype)
    {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(mimetype);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            Uri shareURI = FileProvider.getUriForFile(context, authority, file);
            shareIntent.putExtra(Intent.EXTRA_STREAM, shareURI);
            if (Build.VERSION.SDK_INT >= 16) {
                shareIntent.setClipData(ClipData.newRawUri("", shareURI));
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.msg_export_to)));

        } catch (Exception e) {
            Log.e("ShareUtils", "shareBitmap: Failed to share file URI! " + e);
        }
    }

}
