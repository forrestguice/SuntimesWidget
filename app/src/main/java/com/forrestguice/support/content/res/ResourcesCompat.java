package com.forrestguice.support.content.res;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.forrestguice.support.annotation.NonNull;
import com.forrestguice.support.annotation.Nullable;

public class ResourcesCompat
{
    @Nullable
    public static Drawable getDrawable(@NonNull Resources res, int id, @Nullable Resources.Theme theme) throws Resources.NotFoundException {
        return android.support.v4.content.res.ResourcesCompat.getDrawable(res,id, theme);
    }
}
