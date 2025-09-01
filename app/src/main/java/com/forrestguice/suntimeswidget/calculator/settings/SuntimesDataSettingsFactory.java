package com.forrestguice.suntimeswidget.calculator.settings;

import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;

public interface SuntimesDataSettingsFactory
{
    SuntimesDataSettings getDataSettings(Object object);
}
