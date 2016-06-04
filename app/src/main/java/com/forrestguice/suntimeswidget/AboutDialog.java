/**
    Copyright (C) 2014 Forrest Guice
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
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
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
        View dialogContent = inflater.inflate(R.layout.layout_dialog_about, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        builder.setTitle(myParent.getString(R.string.about_dialog_title));
        AlertDialog dialog = builder.create();

        initViews(getActivity(), dialogContent);
        return dialog;
    }

    public void initViews(Context context, View dialogContent)
    {
        TextView urlView = (TextView) dialogContent.findViewById(R.id.txt_about_url);
        urlView.setMovementMethod(LinkMovementMethod.getInstance());
        urlView.setText(Html.fromHtml(context.getString(R.string.app_url)));

        TextView supportView = (TextView) dialogContent.findViewById(R.id.txt_about_support);
        supportView.setMovementMethod(LinkMovementMethod.getInstance());
        supportView.setText(Html.fromHtml(context.getString(R.string.app_support_url)));

        TextView legalView = (TextView) dialogContent.findViewById(R.id.txt_about_legal);
        legalView.setMovementMethod(LinkMovementMethod.getInstance());
        legalView.setText(Html.fromHtml(context.getString(R.string.app_legal)));
    }
}
