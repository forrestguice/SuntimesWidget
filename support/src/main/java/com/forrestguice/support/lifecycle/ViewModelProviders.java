package com.forrestguice.support.lifecycle;

import android.arch.lifecycle.ViewModelStore;
import android.arch.lifecycle.ViewModelStoreOwner;
import android.support.annotation.NonNull;

public abstract class ViewModelProviders extends android.arch.lifecycle.ViewModelProvider
{
    public ViewModelProviders(@NonNull ViewModelStoreOwner owner, @NonNull Factory factory) {
        super(owner, factory);
    }

    public ViewModelProviders(@NonNull ViewModelStore store, @NonNull Factory factory) {
        super(store, factory);
    }
}