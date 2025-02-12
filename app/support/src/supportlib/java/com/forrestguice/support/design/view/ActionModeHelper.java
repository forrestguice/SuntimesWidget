package com.forrestguice.support.design.view;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActionModeHelper
{
    /**
     * ActionModeInterface
     */
    public interface ActionModeInterface
    {
        MenuInflater getMenuInflater();
        void setTitle(CharSequence charSequence);
        void setSubtitle(CharSequence charSequence);
        void finish();
        void invalidate();
    }

    public static ActionModeInterface wrap(ActionMode actionMode) {
        return new ActionModeWrapper(actionMode);
    }

    public static class ActionModeWrapper implements ActionModeInterface
    {
        private final ActionMode actionMode;
        public ActionModeWrapper(ActionMode actionMode) {
            this.actionMode = actionMode;
        }

        @Override
        public MenuInflater getMenuInflater() {
            return actionMode.getMenuInflater();
        }

        @Override
        public void setTitle(CharSequence charSequence) {
            actionMode.setTitle(charSequence);
        }

        @Override
        public void setSubtitle(CharSequence charSequence) {
            actionMode.setSubtitle(charSequence);
        }

        @Override
        public void finish() {
            actionMode.finish();
        }

        @Override
        public void invalidate() {
            actionMode.invalidate();
        }
    }

    /**
     * ActionModeCallback
     */
    public interface ActionModeCallback
    {
        boolean onCreateActionMode(ActionModeInterface actionMode, Menu menu);
        boolean onPrepareActionMode(ActionModeInterface actionMode, Menu menu);
        boolean onActionItemClicked(ActionModeInterface actionMode, MenuItem menuItem);
        void onDestroyActionMode(ActionModeInterface actionMode);
    }

    public static ActionMode.Callback wrap(final ActionModeCallback callback)
    {
        return new ActionMode.Callback()
        {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                return callback.onCreateActionMode(wrap(actionMode), menu);
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return callback.onPrepareActionMode(wrap(actionMode), menu);
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                return callback.onActionItemClicked(wrap(actionMode), menuItem);
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                callback.onDestroyActionMode(wrap(actionMode));
            }
        };
    }

}