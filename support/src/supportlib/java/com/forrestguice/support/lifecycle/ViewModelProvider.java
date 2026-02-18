package com.forrestguice.support.lifecycle;

import android.arch.lifecycle.ViewModelStore;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.support.annotation.NonNull;

public abstract class ViewModelProvider extends android.arch.lifecycle.ViewModelProvider
{
    public ViewModelProvider(@NonNull ViewModelStoreOwner owner, @NonNull Factory factory) {
        super(owner, factory);
    }

    public ViewModelProvider(@NonNull ViewModelStore store, @NonNull Factory factory) {
        super(store, factory);
    }
}