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

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.forrestguice.support.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.forrestguice.annotation.NonNull;
import com.forrestguice.annotation.Nullable;
import com.forrestguice.suntimeswidget.dialog.BottomSheetDialogBase;
import com.forrestguice.suntimeswidget.settings.AppSettings;

import java.util.Arrays;
import java.util.Comparator;

public class AboutDialog extends BottomSheetDialogBase
{
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog)
            {
                FrameLayout layout = getBottomSheetFrameLayout(dialog);
                if (layout != null)
                {
                    layout.post(new Runnable() {
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
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_about, parent, false);

        if (savedState != null)
        {
            param_iconID = savedState.getInt(KEY_ICONID, param_iconID);
            param_appName = savedState.getInt(KEY_APPNAME, param_appName);
        }
        initViews(getActivity(), dialogContent);

        return dialogContent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    public static String anchor(String url) {
        return anchor(url, url);
    }
    public static String anchor(String url, String text) {
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

    protected static String smallText(String text)
    {
        return "<small>" + text + "</small>";
    }

    public String htmlVersionString()
    {
        String buildString = anchor(getString(R.string.help_commit_url) + BuildConfig.GIT_HASH, BuildConfig.GIT_HASH);
        String versionString = anchor(getString(R.string.help_changelog_url), BuildConfig.VERSION_NAME) + " " + smallText("(" + buildString + ")");
        if (BuildConfig.DEBUG)
        {
            versionString += " " + smallText("[" + BuildConfig.BUILD_TYPE + "]");
        }
        return getString(R.string.app_version, versionString);
    }

    public static void openLink(@Nullable Context context, @Nullable String url)
    {
        if (context == null || url == null) {
            return;
        }
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Log.e("About", "openLink: " + e);
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
                openLink(getActivity(), getString(R.string.help_app_url));
            }
        });

        ImageView iconView = (ImageView) dialogContent.findViewById(R.id.txt_about_icon);
        iconView.setImageDrawable(ContextCompat.getDrawable(context, param_iconID));

        TextView versionView = (TextView) dialogContent.findViewById(R.id.txt_about_version);
        versionView.setMovementMethod(LinkMovementMethod.getInstance());
        versionView.setText(SuntimesUtils.fromHtml(htmlVersionString()));

        TextView supportView = (TextView) dialogContent.findViewById(R.id.txt_about_support);
        supportView.setMovementMethod(LinkMovementMethod.getInstance());
        supportView.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_support_url, context.getString(R.string.help_support_url))));

        TextView legalView1 = (TextView) dialogContent.findViewById(R.id.txt_about_legal1);
        legalView1.setMovementMethod(LinkMovementMethod.getInstance());
        legalView1.setText(SuntimesUtils.fromHtml(context.getString(R.string.app_legal1)));

        TextView legalView2 = (TextView) dialogContent.findViewById(R.id.txt_about_legal2);
        legalView2.setMovementMethod(LinkMovementMethod.getInstance());
        legalView2.setText(SuntimesUtils.fromHtml(initTranslationCredits(getActivity())));

        TextView legalView3 = (TextView) dialogContent.findViewById(R.id.txt_about_legal3);
        legalView3.setMovementMethod(LinkMovementMethod.getInstance());
        legalView3.setText(SuntimesUtils.fromHtml(initLibraryCredits(getActivity())));

        TextView aboutMediaView = (TextView) dialogContent.findViewById(R.id.txt_about_media);
        aboutMediaView.setMovementMethod(LinkMovementMethod.getInstance());
        aboutMediaView.setText(SuntimesUtils.fromHtml(initMediaCredits(getActivity())));

        TextView legalView4 = (TextView) dialogContent.findViewById(R.id.txt_about_legal4);
        String permissionsExplained = context.getString(R.string.privacy_permission_location);
        if (Build.VERSION.SDK_INT <= 18) {
            permissionsExplained += "<br/><br/>" + context.getString(R.string.privacy_permission_storage);
            permissionsExplained += "<br/><br/>" + context.getString(R.string.privacy_permission_storage1);
        }

        String privacy = context.getString(R.string.privacy_policy, permissionsExplained);
        legalView4.setText(SuntimesUtils.fromHtml(privacy));

        int[] linkViews = new int[] { R.id.txt_help_url, R.id.txt_about_url, R.id.txt_about_legal5 };
        for (int resID : linkViews)
        {
            TextView text = (TextView) dialogContent.findViewById(resID);
            if (text != null) {
                text.setText(SuntimesUtils.fromHtml(anchor(text.getText().toString())));
                text.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        outState.putInt(KEY_ICONID, param_iconID);
        outState.putInt(KEY_APPNAME, param_appName);
        super.onSaveInstanceState(outState);
    }

    public static String initCredits(Activity activity, int stringResId, int entryArrayResId, int entryFormatResId)
    {
        final String[] entries = activity.getResources().getStringArray(entryArrayResId);
        StringBuilder credits = new StringBuilder();
        for (int i=0; i<entries.length; i++)
        {
            credits.append(activity.getString(entryFormatResId, entries[i]));
            if (i != entries.length-1) {
                credits.append(" <br />");
            }
        }
        return activity.getString(stringResId, credits.toString());
    }

    public static String initLibraryCredits(Activity activity) {
        return initCredits(activity, R.string.app_legal3, R.array.app_libraries, R.string.libraryCreditsFormat);
    }

    public static String initMediaCredits(Activity activity) {
        return initCredits(activity, R.string.app_about_media, R.array.app_media, R.string.libraryCreditsFormat);
    }

    public static String initTranslationCredits(@Nullable Activity activity)
    {
        if (activity == null) {
            return "";
        }

        final String[] localeValues = activity.getResources().getStringArray(R.array.locale_values);
        final String[] localeCredits = activity.getResources().getStringArray(R.array.locale_credits);
        final String[] localeDisplay = activity.getResources().getStringArray(R.array.locale_display);

        final String currentLanguage = AppSettings.getLocale().getLanguage();
        Integer[] index = new Integer[localeDisplay.length];    // sort alphabetical (localized)
        for (int i=0; i < index.length; i++) {
            index[i] = i;
        }
        Arrays.sort(index, new Comparator<Integer>() {
            public int compare(Integer i1, Integer i2) {
                if (localeValues[i1].startsWith(currentLanguage)) {
                    return -1;
                } else if (localeValues[i2].startsWith(currentLanguage)) {
                    return 1;
                } else return localeDisplay[i1].compareTo(localeDisplay[i2]);
            }
        });

        StringBuilder credits = new StringBuilder();
        for (int i=0; i<index.length; i++)
        {
            int j = index[i];

            String localeCredits_j = (localeCredits.length > j ? localeCredits[j] : "");
            if (!localeCredits[j].isEmpty())
            {
                String localeDisplay_j = (localeDisplay.length > j ? localeDisplay[j] : localeValues[j]);
                String[] authorList = localeCredits_j.split("\\|");

                String authors = "";
                if (authorList.length < 2) {
                    authors = authorList[0];

                } else if (authorList.length == 2) {
                    authors = activity.getString(R.string.authorListFormat_n, authorList[0], authorList[1]);

                } else {
                    for (int k=0; k<authorList.length-1; k++)
                    {
                        if (authors.isEmpty())
                            authors = authorList[k];
                        else authors = activity.getString(R.string.authorListFormat_i, authors, authorList[k]);
                    }
                    authors = activity.getString(R.string.authorListFormat_n, authors, authorList[authorList.length-1]);
                }

                String line = activity.getString(R.string.translationCreditsFormat, localeDisplay_j, authors);
                if (i != index.length-1) {
                    if (!line.endsWith("<br/>") && !line.endsWith("<br />"))
                        line = line + "<br/>";
                }
                credits.append(line);
            }
        }
        return activity.getString(R.string.app_legal2, credits.toString());
    }

}
