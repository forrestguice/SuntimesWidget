/**
    Copyright (C) 2020 Forrest Guice
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
package com.forrestguice.suntimeswidget.alarmclock.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.alarmclock.AlarmClockItem;
import com.forrestguice.suntimeswidget.settings.AppSettings;

@SuppressWarnings("Convert2Diamond")
public class AlarmItemDialog extends DialogFragment
{
    protected TextView text_title;
    protected AlarmClockItem item = null;
    protected AlarmItemViewHolder itemView;

    public void setItem(AlarmClockItem item)
    {
        this.item = new AlarmClockItem(item);
        bindItemToHolder(item);
    }

    protected void bindItemToHolder(AlarmClockItem item)
    {
        if (itemView != null) {
            itemView.bindDataToPosition(getActivity(), item, 0);
        }
        if (text_title != null) {
            text_title.setText(item != null ? item.type.getDisplayString() : "");
        }
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_alarmitem, parent, false);

        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }

        return dialogContent;
    }

    /**
     * @param context a context used to access resources
     * @param dialogContent an inflated layout containing the dialog's other views
     */
    private void initViews(Context context, View dialogContent)
    {
        itemView = new AlarmItemViewHolder(dialogContent);
        text_title = (TextView) dialogContent.findViewById(R.id.dialog_title);

        ImageButton btn_cancel = (ImageButton) dialogContent.findViewById(R.id.dialog_button_cancel);
        if (btn_cancel != null) {
            btn_cancel.setOnClickListener(onDialogCancelClick);
        }

        ImageButton btn_accept = (ImageButton) dialogContent.findViewById(R.id.dialog_button_accept);
        if (btn_accept != null) {
            btn_accept.setOnClickListener(onDialogAcceptClick);
        }

        Button btn_neutral = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
        if (btn_neutral != null) {
            btn_neutral.setOnClickListener(onDialogNeutralClick);
        }

        bindItemToHolder(item);
    }

    /**
     *
     * @param savedInstanceState a bundle containing previously saved dialog state
     * @return a dialog instance ready to be shown
     */
    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Restore the dialog state from the provided bundle.
     * @param bundle state loaded from this Bundle
     */
    protected void loadSettings(Bundle bundle) {
        setItem((AlarmClockItem)bundle.getParcelable("item"));
    }

    /**
     * Save the dialog state to a bundle to be restored at a later time (occurs onSaveInstanceState).
     * @param bundle state persisted to this Bundle
     */
    protected void saveSettings(Bundle bundle) {
        bundle.putParcelable("item", item);
    }

    /**
     * A listener that is triggered when the dialog is accepted.
     */
    private DialogInterface.OnClickListener onAccepted = null;
    public void setOnAcceptedListener( DialogInterface.OnClickListener listener ) {
        onAccepted = listener;
    }

    /**
     * A listener that is triggered when the dialog is cancelled.
     */
    private DialogInterface.OnClickListener onCanceled = null;
    public void setOnCanceledListener( DialogInterface.OnClickListener listener ) {
        onCanceled = listener;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog) {
            // EMPTY; placeholder
        }
    };

    private View.OnClickListener onDialogNeutralClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // TODO: neutral click
        }
    };

    private View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
        }
    };

    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (onCanceled != null) {
            onCanceled.onClick(getDialog(), 0);
        }
    }

    private View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            dismiss();
            if (onAccepted != null) {
                onAccepted.onClick(getDialog(), 0);
            }
        }
    };

}
