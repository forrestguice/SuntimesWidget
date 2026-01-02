package com.forrestguice.support.lifecycle;

import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.annotation.NonNull;

public abstract class ViewModelProviders extends androidx.lifecycle.ViewModelProvider
{
    public ViewModelProviders(@NonNull ViewModelStoreOwner owner, @NonNull Factory factory) {
        super(owner, factory);
    }

    public ViewModelProviders(@NonNull ViewModelStore store, @NonNull Factory factory) {
        super(store, factory);
    }
}