package com.forrestguice.suntimeswidget.calculator.settings.android;

import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettings;
import com.forrestguice.suntimeswidget.calculator.settings.SuntimesDataSettingsFactory;
import com.forrestguice.util.Log;

public class AndroidSuntimesDataSettingsFactory implements SuntimesDataSettingsFactory
{
    @Override
    public SuntimesDataSettings getDataSettings(Object object)
    {
        if (object instanceof SuntimesDataSettings) {
            return (SuntimesDataSettings) object;

        } else if (object instanceof android.content.Context) {
            return AndroidSuntimesDataSettings.wrap((android.content.Context) object);

        } else {
            Log.e("getDataSettings", "unrecognized type! " + object.getClass().getSimpleName());
            return null;
        }
    }
}
