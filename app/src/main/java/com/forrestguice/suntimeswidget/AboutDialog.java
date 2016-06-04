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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutDialog extends Dialog
{
    private Activity myParent;

    public AboutDialog(Activity c)
    {
        super(c);
        myParent = c;
        setContentView(R.layout.layout_dialog_about);
        setCancelable(true);
    }

    public void onPrepareDialog()
    {
        setTitle(myParent.getString(R.string.about_dialog_title));

        TextView urlView = (TextView)findViewById(R.id.txt_about_url);
        urlView.setMovementMethod(LinkMovementMethod.getInstance());
        urlView.setText(Html.fromHtml(myParent.getString(R.string.app_url)));

        TextView supportView = (TextView)findViewById(R.id.txt_about_support);
        supportView.setMovementMethod(LinkMovementMethod.getInstance());
        supportView.setText(Html.fromHtml(myParent.getString(R.string.app_support_url)));

        TextView legalView = (TextView)findViewById(R.id.txt_about_legal);
        legalView.setMovementMethod(LinkMovementMethod.getInstance());
        legalView.setText(Html.fromHtml(myParent.getString(R.string.app_legal)));
    }
}
