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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.SuntimesUtils;
import com.forrestguice.suntimeswidget.settings.WidgetActions;

import java.util.ArrayList;
import java.util.Set;

/**
 * LoadIntentDialog
 */
public class LoadIntentDialog extends EditIntentDialog
{
    @Override
    public String getIntentID()
    {
        if (spin_intentID != null) {
            IntentDisplay selected = (IntentDisplay)spin_intentID.getSelectedItem();
            return selected != null ? selected.id : null;
        } else return null;
    }

    @Override
    public String getIntentTitle()
    {
        if (spin_intentID != null) {
            IntentDisplay selected = (IntentDisplay)spin_intentID.getSelectedItem();
            return selected != null ? selected.title : null;
        } else return null;
    }

    private Spinner spin_intentID;

    @Override
    protected void initViews(Context context, View dialogContent)
    {
        spin_intentID = (Spinner) dialogContent.findViewById(R.id.spin_intentid);
        initAdapter(context);

        ImageButton button_menu = (ImageButton) dialogContent.findViewById(R.id.edit_intent_menu);
        button_menu.setOnClickListener(onMenuButtonClicked);

        super.initViews(context, dialogContent);
    }

    protected void initAdapter(Context context)
    {
        ArrayList<IntentDisplay> ids = new ArrayList<>();
        Set<String> intentIDs = WidgetActions.loadActionLaunchList(context, 0);
        for (String id : intentIDs)
        {
            String title = WidgetActions.loadActionLaunchPref(context, 0, id, WidgetActions.PREF_KEY_ACTION_LAUNCH_TITLE);
            if (title != null && !title.trim().isEmpty()) {
                ids.add(new IntentDisplay(id, title));
            }
        }
        ArrayAdapter<IntentDisplay> adapter = new ArrayAdapter<>(context, R.layout.layout_listitem_oneline, ids.toArray(new IntentDisplay[0]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_intentID.setAdapter(adapter);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.layout_dialog_intent_load;
    }

    protected View.OnClickListener onMenuButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            showOverflowMenu(getContext(), v);
        }
    };

    protected void showOverflowMenu(Context context, View parent)
    {
        PopupMenu menu = new PopupMenu(context, parent);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.editintent1, menu.getMenu());
        menu.setOnMenuItemClickListener(onMenuItemClicked);
        SuntimesUtils.forceActionBarIcons(menu.getMenu());
        prepareOverflowMenu(context, menu.getMenu());
        menu.show();
    }

    protected void prepareOverflowMenu(Context context, Menu menu)
    {
        MenuItem deleteItem = menu.findItem(R.id.deleteAction);
        if (deleteItem != null) {
            deleteItem.setEnabled(getIntentID() != null);
        }
    }

    protected PopupMenu.OnMenuItemClickListener onMenuItemClicked = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.deleteAction:
                    deleteAction();
                    return true;

                default:
                    return false;
            }
        }
    };

    private void deleteAction()
    {
        Context context = getContext();
        final String intentID = getIntentID();
        if (intentID != null && context != null)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage(context.getString(R.string.delaction_dialog_msg, intentID))
                    .setNegativeButton(context.getString(R.string.delaction_dialog_cancel), null)
                    .setPositiveButton(context.getString(R.string.delaction_dialog_ok),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WidgetActions.deleteActionLaunchPref(getContext(), 0, intentID);
                            initAdapter(getContext());
                            updateViews(getContext());
                        }
                    });
            dialog.show();
        }
    }

    public static class IntentDisplay
    {
        public String id;
        public String title;
        public IntentDisplay(String id, String title)
        {
            this.id = id;
            this.title = title;
        }
        public String toString() {
             return title;
        }
    }
}
