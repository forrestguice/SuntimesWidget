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
package com.forrestguice.suntimeswidget;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.views.Toast;

public class ExceptionActivity extends AppCompatActivity
{
    public static final String EXTRA_REPORT = "report";

    @Override
    protected void attachBaseContext(Context newBase)
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e("CRASH", e.getClass().getSimpleName(), e);
                System.exit(0);
            }
        });
        super.attachBaseContext(newBase);
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        final String trace = getIntent().getStringExtra(EXTRA_REPORT);
        if (trace == null)
        {
            Log.w("ExceptionActivity", EXTRA_REPORT + " is missing! finishing...");
            finish();
            return;
        }

        setContentView(R.layout.layout_activity_exception);
        initViews(this);
    }

    @SuppressLint("SetTextI18n")
    protected void initViews(Context context)
    {
        final String report = getIntent().getStringExtra(EXTRA_REPORT);

        TextView text_message = (TextView) findViewById(R.id.text_message);
        if (text_message != null) {
            text_message.setText(getString(R.string.crash_dialog_message, getString(R.string.app_name)));
        }

        TextView text_message1 = (TextView) findViewById(R.id.text_message1);
        if (text_message1 != null) {
            //text_message1.setMovementMethod(LinkMovementMethod.getInstance());
            text_message1.setText(SuntimesUtils.fromHtml(getString(R.string.crash_dialog_message1, getString(R.string.help_support_url))));
        }

        TextView text_report = (TextView) findViewById(R.id.text_exception);
        if (text_report != null) {
            text_report.setHorizontallyScrolling(true);
            text_report.setText(report);
        }

        Button button_copy = (Button) findViewById(R.id.button_copy);
        if (button_copy != null) {
            button_copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyToClipboard(ExceptionActivity.this, report);
                }
            });
        }
    }

    public static void copyToClipboard(Context context, String e)
    {
        if (context != null)
        {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null)
            {
                if (e != null)
                {
                    clipboard.setPrimaryClip(ClipData.newPlainText("CRASH", e));
                    Toast.makeText(context, "Copied to the clipboard.", Toast.LENGTH_SHORT).show();

                } else Log.e("ExceptionActivity", "copyToClipboard: failed to copy exception; null report!");
            } else Log.e("ExceptionActivity", "copyToClipboard: failed to copy exception; null clipboard!");
        } else Log.e("ExceptionActivity", "copyToClipboard: failed to copy exception; null context!");
    }

}
