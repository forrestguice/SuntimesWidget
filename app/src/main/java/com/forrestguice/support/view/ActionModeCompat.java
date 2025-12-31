package com.forrestguice.support.view;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class ActionModeCompat
{
    protected android.support.v7.view.ActionMode actionMode;
    public ActionModeCompat(android.support.v7.view.ActionMode actionMode) {
        this.actionMode = actionMode;
    }

    public void setTag(Object tag) {
        actionMode.setTag(tag);
    }
    public Object getTag() {
        return actionMode.getTag();
    }

    public void setTitle(CharSequence text) {
        actionMode.setTitle(text);
    }
    public void setTitle(int resId) {
        actionMode.setTag(resId);
    }

    public void setSubtitle(java.lang.CharSequence text) {
        actionMode.setSubtitle(text);
    }
    public void setSubtitle(int resId) {
        actionMode.setSubtitle(resId);
    }

    public void setTitleOptionalHint(boolean value) {
        actionMode.setTitleOptionalHint(value);
    }
    public boolean getTitleOptionalHint() {
        return actionMode.getTitleOptionalHint();
    }
    public boolean isTitleOptional() {
        return actionMode.isTitleOptional();
    }

    public void setCustomView(View view) {
        actionMode.setCustomView(view);
    }

    public void invalidate() {
        actionMode.invalidate();
    }

    public void finish() {
        actionMode.finish();
    }

    public Menu getMenu() {
        return actionMode.getMenu();
    }

    public CharSequence getTitle() {
        return actionMode.getTitle();
    }

    public CharSequence getSubtitle() {
        return actionMode.getSubtitle();
    }

    public View getCustomView() {
        return actionMode.getCustomView();
    }

    public MenuInflater getMenuInflater() {
        return actionMode.getMenuInflater();
    }

    public static abstract class Callback
    {
        public abstract boolean onCreateActionMode(ActionModeCompat actionMode, Menu menu);
        public abstract boolean onPrepareActionMode(ActionModeCompat actionMode, Menu menu);
        public abstract boolean onActionItemClicked(ActionModeCompat actionMode, MenuItem menuItem);
        public void onDestroyActionMode(ActionModeCompat actionMode) {
            mode = null;
        }

        private ActionModeCompat mode = null;
        public void setActionMode(ActionModeCompat value) {
            mode = value;
        }
        public ActionModeCompat getActionMode() {
            return mode;
        }
    }

    public static android.support.v7.view.ActionMode.Callback from(final Callback callback)
    {
        return new android.support.v7.view.ActionMode.Callback()
        {
            @Override
            public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
                return callback.onCreateActionMode(callback.getActionMode(), menu);
            }

            @Override
            public boolean onPrepareActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
                return callback.onPrepareActionMode(callback.getActionMode(), menu);
            }

            @Override
            public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem) {
                return callback.onActionItemClicked(callback.getActionMode(), menuItem);
            }

            @Override
            public void onDestroyActionMode(android.support.v7.view.ActionMode actionMode) {
                callback.onDestroyActionMode(callback.getActionMode());
            }
        };
    }
}