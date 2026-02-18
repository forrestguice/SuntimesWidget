package com.forrestguice.support.lifecycle;

import com.forrestguice.annotation.NonNull;

import androidx.lifecycle.ViewModelStoreOwner;

public class ViewModelProviders
{
    public static androidx.lifecycle.ViewModelProvider of(@NonNull ViewModelStoreOwner store) {
        return new androidx.lifecycle.ViewModelProvider(store);
    }
}
