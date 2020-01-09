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

package com.forrestguice.suntimeswidget.actions;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.forrestguice.suntimeswidget.R;

/**
 * LoadActionDialog
 */
public class LoadActionDialog extends EditActionDialog
{
    private ActionListHelper listHelper;

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        listHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        listHelper.setFragmentManager(getFragmentManager());
        listHelper.setOnItemAcceptedListener(onItemAccepted);
        listHelper.setOnUpdateViews(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateViews(getContext());
            }
        });
        listHelper.onResume();
    }

    private View.OnClickListener onItemAccepted = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btn_accept.performClick();
        }
    };

    @Override
    public String getIntentID() {
        return listHelper.getIntentID();
    }

    @Override
    public String getIntentTitle() {
        return listHelper.getIntentTitle();
    }

    @Override
    protected void updateViews(Context context)
    {
        super.updateViews(context);
        this.btn_accept.setEnabled(listHelper.getIntentID() != null);
    }

    @Override
    protected void initViews(Context context, View dialogContent, @Nullable Bundle savedState)
    {
        super.initViews(context, dialogContent, savedState);
        listHelper = new ActionListHelper(context, getFragmentManager());
        listHelper.initViews(context, dialogContent, savedState);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.layout_dialog_intent_load;
    }

}
