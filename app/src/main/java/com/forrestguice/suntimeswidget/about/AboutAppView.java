/**
    Copyright (C) 2019 Forrest Guice
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

package com.forrestguice.suntimeswidget.about;

import android.content.Context;
import android.os.Build;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.suntimeswidget.BuildConfig;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;

public class AboutAppView extends LinearLayout
{
    public static final int LAYOUT_APP = 0;
    public static final int LAYOUT_CONTRIBUTIONS = 1;
    public static final int LAYOUT_PRIVACY = 2;

    protected final int layoutID;
    public int getLayoutID() {
        return layoutID;
    }

    public AboutAppView(@NonNull Context context) {
        super(context);
        layoutID = 0;
        initView(context, layoutID, this);
    }

    public AboutAppView(@NonNull Context context, int layoutNumber) {
        super(context);
        layoutID = layoutNumber;
        initView(context, layoutID, this);
    }

    public static AboutAppView newInstance(Context context, int layoutNumber) {
        return new AboutAppView(context, layoutNumber);
    }

    public static View initView(Context context, int layoutID, ViewGroup viewGroup)
    {
        View view;
        switch (layoutID)
        {
            case LAYOUT_PRIVACY:
                view = inflate(context, R.layout.layout_about_privacy, viewGroup);
                break;

            case LAYOUT_CONTRIBUTIONS:
                view = inflate(context, R.layout.layout_about_contributions, viewGroup);
                break;

            case LAYOUT_APP:
            default:
                view = inflate(context, R.layout.layout_about_app, viewGroup);
                break;
        }
        updateViews(context, view);
        return view;
    }

    public static void updateViews(@NonNull Context context, View dialogContent)
    {
        TextView nameView = (TextView) dialogContent.findViewById(R.id.txt_about_name);
        if (nameView != null) {
            nameView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Context context = v.getContext();
                    if (context != null) {
                        AboutDialog.openLink(context, context.getString(R.string.help_app_url));
                    }
                }
            });
        }

        TextView versionView = (TextView) dialogContent.findViewById(R.id.txt_about_version);
        if (versionView != null) {
            versionView.setMovementMethod(LinkMovementMethod.getInstance());
            versionView.setText(SuntimesUtils.fromHtml(htmlVersionString(context)));
        }

        TextView supportView = (TextView) dialogContent.findViewById(R.id.txt_about_support);
        if (supportView != null) {
            supportView.setMovementMethod(LinkMovementMethod.getInstance());
            supportView.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_support_url, context.getString(R.string.help_support_url))));
        }

        final TextView donateView = (TextView) dialogContent.findViewById(R.id.txt_donate_url);
        if (donateView != null) {
            donateView.setVisibility(View.GONE);
            donateView.setMovementMethod(LinkMovementMethod.getInstance());
            donateView.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_donate_url, context.getString(R.string.app_name), context.getString(R.string.help_donate_url))));
        }

        CheckBox checkDonate = (CheckBox) dialogContent.findViewById(R.id.check_donate);
        if (checkDonate != null)
        {
            checkDonate.setChecked(false);
            checkDonate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (donateView != null) {
                        donateView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }

        TextView legalView1 = (TextView) dialogContent.findViewById(R.id.txt_about_legal1);
        if (legalView1 != null) {
            legalView1.setMovementMethod(LinkMovementMethod.getInstance());
            legalView1.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_legal1)));
        }

        TextView legalView2 = (TextView) dialogContent.findViewById(R.id.txt_about_legal2);
        if (legalView2 != null) {
            legalView2.setMovementMethod(LinkMovementMethod.getInstance());
            //legalView2.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_legal2)));
            legalView2.setText(SuntimesUtils.fromHtml(AboutDialog.initTranslationCredits(context)));
        }

        TextView legalView3 = (TextView) dialogContent.findViewById(R.id.txt_about_legal3);
        if (legalView3 != null) {
            legalView3.setMovementMethod(LinkMovementMethod.getInstance());
            legalView3.setText(SuntimesUtils.fromHtml(AboutDialog.initLibraryCredits(context)));
        }

        TextView aboutMediaView = (TextView) dialogContent.findViewById(R.id.txt_about_media);
        if (aboutMediaView != null) {
            aboutMediaView.setMovementMethod(LinkMovementMethod.getInstance());
            aboutMediaView.setText(SuntimesUtils.fromHtml(AboutDialog.initMediaCredits(context)));
        }

        TextView legalView4 = (TextView) dialogContent.findViewById(R.id.txt_about_legal4);
        if (legalView4 != null) {
            String permissionsExplained = context.getString(R.string.privacy_permission_location);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                permissionsExplained += "<br/><br/>" + context.getString(R.string.privacy_permission_storage);
            }
            String privacy = context.getString(R.string.privacy_policy, permissionsExplained);
            legalView4.setText(SuntimesUtils.fromHtml(privacy));
        }

        int[] linkViews = new int[] { R.id.txt_help_url, R.id.txt_about_url, R.id.txt_about_url1, R.id.txt_about_legal5 };
        for (int resID : linkViews)
        {
            TextView text = (TextView) dialogContent.findViewById(resID);
            if (text != null) {
                text.setText(SuntimesUtils.fromHtml(anchor(text.getText().toString())));
                text.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    public static String htmlVersionString(@NonNull Context context)
    {
        String buildString = anchor(context.getString(R.string.help_commit_url) + BuildConfig.GIT_HASH, BuildConfig.GIT_HASH);
        String versionString = anchor(context.getString(R.string.help_changelog_url), BuildConfig.VERSION_NAME) + " " + smallText("(" + buildString + ")");
        if (BuildConfig.DEBUG)
        {
            versionString += " " + smallText("[" + BuildConfig.BUILD_TYPE + "]");
        }
        return context.getString(R.string.app_version, versionString);
    }

    public static String smallText(String text) {
        return "<small>" + text + "</small>";
    }
    public static String anchor(String url) {
        return anchor(url, url);
    }
    public static String anchor(String url, String text) {
        return "<a href=\"" + url + "\">" + text + "</a>";
    }
}
