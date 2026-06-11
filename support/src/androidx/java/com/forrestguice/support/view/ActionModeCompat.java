package com.forrestguice.support.view;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class ActionModeCompat
{
    protected androidx.appcompat.view.ActionMode actionMode;
    public ActionModeCompat(androidx.appcompat.view.ActionMode actionMode) {
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

    public interface Callback
    {
        boolean onCreateActionMode(MenuInflater menuInflater, Menu menu);
        boolean onPrepareActionMode(ActionModeCompat actionMode, Menu menu);
        boolean onActionItemClicked(ActionModeCompat actionMode, MenuItem menuItem);
        void onDestroyActionMode(ActionModeCompat actionMode);
        void setActionMode(ActionModeCompat value);
        ActionModeCompat getActionMode();
    }

    public static abstract class CallbackBase implements Callback
    {
        private ActionModeCompat mode = null;
        @Override
        public void setActionMode(ActionModeCompat value) {
            mode = value;
        }
        @Override
        public ActionModeCompat getActionMode() {
            return mode;
        }
        @Override
        public void onDestroyActionMode(ActionModeCompat actionMode) {
            //noinspection ConstantConditions
            mode = null;
        }
    }

    public static androidx.appcompat.view.ActionMode.Callback from(final Callback callback)
    {
        return new androidx.appcompat.view.ActionMode.Callback()
        {
            @Override
            public boolean onCreateActionMode(androidx.appcompat.view.ActionMode actionMode, Menu menu) {
                return callback.onCreateActionMode(actionMode.getMenuInflater(), menu);
            }

            @Override
            public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode actionMode, Menu menu) {
                return callback.onPrepareActionMode(callback.getActionMode(), menu);
            }

            @Override
            public boolean onActionItemClicked(androidx.appcompat.view.ActionMode actionMode, MenuItem menuItem) {
                return callback.onActionItemClicked(callback.getActionMode(), menuItem);
            }

            @Override
            public void onDestroyActionMode(androidx.appcompat.view.ActionMode actionMode) {
                callback.onDestroyActionMode(callback.getActionMode());
            }
        };
    }
}