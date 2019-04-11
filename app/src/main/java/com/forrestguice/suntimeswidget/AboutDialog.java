/**
    Copyright (C) 2014-2019 Forrest Guice
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
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.settings.AppSettings;

public class AboutDialog extends DialogFragment
{
    public static final String WEBSITE_URL = "https://forrestguice.github.io/SuntimesWidget/";
    public static final String ADDONS_URL = "https://forrestguice.github.io/SuntimesWidget/";
    public static final String PRIVACY_URL = "https://github.com/forrestguice/SuntimesWidget/wiki/Privacy";
    public static final String CHANGELOG_URL = "https://github.com/forrestguice/SuntimesWidget/blob/master/CHANGELOG.md";
    public static final String COMMIT_URL = "https://github.com/forrestguice/SuntimesWidget/commit/";

    public static final String KEY_ICONID = "paramIconID";
    public static final String KEY_APPNAME = "paramAppName";

    private int param_iconID = R.mipmap.ic_launcher;
    public void setIconID( int resID )
    {
        param_iconID = resID;
    }

    private int param_appName = R.string.app_name;
    public void setAppName( int resID )
    {
        param_appName = resID;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogContent = inflater.inflate(R.layout.layout_dialog_about, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                dialogContent.post(new Runnable() {
                    @Override
                    public void run()
                    {
                        Context context = getContext();
                        if (context != null) {
                            AppSettings.checkCustomPermissions(context);
                        }
                    }
                });
            }
        });

        if (savedInstanceState != null)
        {
            param_iconID = savedInstanceState.getInt(KEY_ICONID, param_iconID);
            param_appName = savedInstanceState.getInt(KEY_APPNAME, param_appName);
        }

        initViews(getActivity(), dialogContent);
        return dialog;
    }

    public static String anchor(String url, String text)
    {
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

    protected static String smallText(String text)
    {
        return "<small>" + text + "</small>";
    }

    public String htmlVersionString()
    {
        String buildString = anchor(COMMIT_URL + BuildConfig.GIT_HASH, BuildConfig.GIT_HASH) + "@" + BuildConfig.BUILD_TIME.getTime();
        String versionString = anchor(CHANGELOG_URL, BuildConfig.VERSION_NAME) + " " + smallText("(" + buildString + ")");
        if (BuildConfig.DEBUG)
        {
            versionString += " " + smallText("[" + BuildConfig.BUILD_TYPE + "]");
        }
        return getString(R.string.app_version, versionString);
    }

    protected void openLink(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        Activity activity = getActivity();
        if (activity != null && intent.resolveActivity(activity.getPackageManager()) != null)
        {
            startActivity(intent);
        }
    }

    public void initViews(Context context, View dialogContent)
    {
        TextView nameView = (TextView) dialogContent.findViewById(R.id.txt_about_name);
        nameView.setText(getString(param_appName));
        nameView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openLink(WEBSITE_URL);
            }
        });

        ImageView iconView = (ImageView) dialogContent.findViewById(R.id.txt_about_icon);
        iconView.setImageDrawable(ContextCompat.getDrawable(context, param_iconID));

        TextView versionView = (TextView) dialogContent.findViewById(R.id.txt_about_version);
        versionView.setMovementMethod(LinkMovementMethod.getInstance());
        versionView.setText(SuntimesUtils.fromHtml(htmlVersionString()));

        TextView urlView = (TextView) dialogContent.findViewById(R.id.txt_about_url);
        urlView.setMovementMethod(LinkMovementMethod.getInstance());
        urlView.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_url)));

        TextView supportView = (TextView) dialogContent.findViewById(R.id.txt_about_support);
        supportView.setMovementMethod(LinkMovementMethod.getInstance());
        supportView.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_support_url)));

        TextView legalView1 = (TextView) dialogContent.findViewById(R.id.txt_about_legal1);
        legalView1.setMovementMethod(LinkMovementMethod.getInstance());
        legalView1.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_legal1)));

        TextView legalView2 = (TextView) dialogContent.findViewById(R.id.txt_about_legal2);
        legalView2.setMovementMethod(LinkMovementMethod.getInstance());
        legalView2.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_legal2)));

        TextView legalView3 = (TextView) dialogContent.findViewById(R.id.txt_about_legal3);
        legalView3.setMovementMethod(LinkMovementMethod.getInstance());
        legalView3.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_legal3)));

        TextView legalView4 = (TextView) dialogContent.findViewById(R.id.txt_about_legal4);
        String permissionsExplained = context.getString(R.string.privacy_permission_location);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            permissionsExplained += "<br/><br/>" + context.getString(R.string.privacy_permission_storage);
        }
        String privacy = context.getString(R.string.privacy_policy, permissionsExplained);
        legalView4.setText(SuntimesUtils.fromHtml(privacy));

        TextView legalView5 = (TextView) dialogContent.findViewById(R.id.txt_about_legal5);
        legalView5.setMovementMethod(LinkMovementMethod.getInstance());
        legalView5.setText(SuntimesUtils.fromHtml(context.getString(R.string.privacy_url)));
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        outState.putInt(KEY_ICONID, param_iconID);
        outState.putInt(KEY_APPNAME, param_appName);
        super.onSaveInstanceState(outState);
    }
}
