/**
    Copyright (C) 2014-2017 Forrest Guice
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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AboutDialog extends DialogFragment
{
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogContent = inflater.inflate(R.layout.layout_dialog_about, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();

        initViews(getActivity(), dialogContent);
        return dialog;
    }

    public static final String WEBSITE_URL = "https://forrestguice.github.io/SuntimesWidget/";

    public static final String CHANGELOG_URL = "https://github.com/forrestguice/SuntimesWidget/blob/master/CHANGELOG.md";
    public static String changelogAnchor(String text)
    {
        return "<a href=\"" + CHANGELOG_URL + "\">" + text + "</a>";
    }

    public static final String COMMIT_URL = "https://github.com/forrestguice/SuntimesWidget/commit/";
    protected static String gitCommitAnchor(String gitHash)
    {
        String commitUrl = COMMIT_URL + gitHash;
        return "<a href=\"" + commitUrl + "\">" + gitHash + "</a>";
    }

    protected static String smallText(String text)
    {
        return "<small>" + text + "</small>";
    }

    public String htmlVersionString()
    {
        String buildString = gitCommitAnchor(BuildConfig.GIT_HASH) + "@" + BuildConfig.BUILD_TIME.getTime();
        String versionString = changelogAnchor(BuildConfig.VERSION_NAME) + " " + smallText("(" + buildString + ")");
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
        nameView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openLink(WEBSITE_URL);
            }
        });

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
    }
}
