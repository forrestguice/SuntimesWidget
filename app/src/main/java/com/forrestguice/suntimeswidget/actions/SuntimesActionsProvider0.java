package com.forrestguice.suntimeswidget.actions;

import com.forrestguice.suntimeswidget.BuildConfig;

public class SuntimesActionsProvider0 extends SuntimesActionsProvider
{
    @Override
    protected String AUTHORITY() {
        return BuildConfig.SUNTIMES_AUTHORITY_ROOT0 + AUTHORITY_SUFFIX;
    }
}