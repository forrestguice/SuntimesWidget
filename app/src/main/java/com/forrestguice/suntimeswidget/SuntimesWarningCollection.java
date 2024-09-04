/**
    Copyright (C) 2024 Forrest Guice
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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class SuntimesWarningCollection
{
    /**
     * Concrete classes must implement this method (called from constructor) to define available
     * warnings (@see `addWarning`).
     * @param context Context
     */
    protected abstract void initWarnings(Context context);

    protected WeakReference<Context> contextRef;
    protected final List<SuntimesWarning> warnings = new ArrayList<>();
    protected final HashMap<String, SuntimesWarning> warningMap = new HashMap<>();
    protected final HashMap<String, View.OnClickListener> warningActions = new HashMap<>();
    protected final HashMap<String, View> warningParent = new HashMap<>();

    public SuntimesWarningCollection(Context context, Bundle savedState)
    {
        contextRef = new WeakReference<>(context);
        initWarnings(context, savedState);
    }

    private void initWarnings(Context context, Bundle savedState)
    {
        clearWarnings();
        initWarnings(context);
        restoreWarnings(savedState);
    }

    /**
     * addWarning
     * @param context Context
     * @param warningID warningID
     * @param message warning message
     * @param parent snackbar parent view
     * @param actionLabel action button label
     * @param actionListener action button listener
     */
    public void addWarning(Context context, String warningID, String message, View parent, @Nullable String actionLabel, @Nullable View.OnClickListener actionListener)
    {
        SuntimesWarning warning = new SuntimesWarning(warningID, context, message);
        warning.setActionLabel(actionLabel);
        warningActions.put(warningID, actionListener);
        warningParent.put(warningID, parent);
        warningMap.put(warningID, warning);
        warnings.add(warning);
    }

    /**
     * setShowWarnings
     * @param value true warnings should be shown
     */
    public void setShowWarnings(boolean value) {
        showWarnings = value;
    }
    public boolean getShowWarnings() {
        return showWarnings;
    }
    protected boolean showWarnings = true;

    /**
     * showWarnings
     */
    public void showWarnings()
    {
        Context context = contextRef.get();
        if (showWarnings && context != null)
        {
            for (int i=0; i<warnings.size(); i++)
            {
                SuntimesWarning warning = warnings.get(i);
                if (warning.shouldShow() && !warning.wasDismissed())
                {
                    warning.initWarning(context, warningParent.get(warning.getId()), warningActions.get(warning.getId()));
                    warning.show();
                    return;
                }
            }
        }
        dismissWarnings();    // no warnings shown; clear previous (stale) messages
    }

    /**
     * dismissWarnings
     */
    public void dismissWarnings()
    {
        for (SuntimesWarning warning : warnings) {
            warning.dismiss();
        }
    }

    /**
     * dismissWarning
     */
    public boolean dismissWarning()
    {
        for (int i=0; i<warnings.size(); i++)
        {
            SuntimesWarning warning = warnings.get(i);
            if (warning.isShown()) {
                warning.dismiss();
                return true;
            }
        }
        return false;
    }

    /**
     * Save the state of warning objects to Bundle.
     * @param outState a Bundle to save state to
     */
    public void saveWarnings( Bundle outState )
    {
        for (SuntimesWarning warning : warnings) {
            warning.save(outState);
        }
    }

    /**
     * Restore the state of warning objects from Bundle.
     * @param savedState a Bundle containing saved state
     */
    public void restoreWarnings(Bundle savedState)
    {
        for (SuntimesWarning warning : warnings)
        {
            warning.restore(savedState);
            warning.setWarningListener(warningListener);
        }
    }

    /**
     * resetWarnings
     */
    public void resetWarnings()
    {
        for (SuntimesWarning warning : warnings) {
            warning.setShouldShow(false);
        }
    }

    /**
     * resetWarning
     * @param warningID warningID
     */
    public void resetWarning(String warningID)
    {
        SuntimesWarning warning = warningMap.get(warningID);
        if (warning != null) {
            warning.reset();
        }
    }

    /**
     * setShouldShow
     * @param warningID warningID
     * @param value true warning should be shown
     */
    public void setShouldShow(String warningID, boolean value)
    {
        SuntimesWarning warning = warningMap.get(warningID);
        if (warning != null) {
            warning.setShouldShow(value);
        }
    }

    /**
     * setWasDismissed
     * @param warningID warningiD
     * @param value true was previously dismissed
     */
    public void setWasDismissed(String warningID, boolean value)
    {
        SuntimesWarning warning = warningMap.get(warningID);
        if (warning != null) {
            warning.wasDismissed = value;
        }
    }

    /**
     * shouldShow
     * @param warningID warningID
     * @return true if warning should be shown, false otherwise
     */
    public boolean shouldShow(String warningID)
    {
        SuntimesWarning warning = warningMap.get(warningID);
        if (warning != null) {
            return warning.shouldShow();
        }
        return false;
    }

    protected void clearWarnings()
    {
        warnings.clear();
        warningActions.clear();
        warningParent.clear();
    }

    /**
     * warningListener
     */
    protected final SuntimesWarning.SuntimesWarningListener warningListener = new SuntimesWarning.SuntimesWarningListener()
    {
        @Override
        public void onShowNextWarning() {
            showWarnings();
        }
    };

}
