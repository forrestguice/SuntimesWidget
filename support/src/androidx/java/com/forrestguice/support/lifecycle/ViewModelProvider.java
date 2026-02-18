package com.forrestguice.support.lifecycle;

import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.annotation.NonNull;

public class ViewModelProvider extends androidx.lifecycle.ViewModelProvider
{
    public ViewModelProvider(@NonNull ViewModelStoreOwner owner) {
        super(owner);
    }

    public ViewModelProvider(@NonNull ViewModelStoreOwner owner, @NonNull Factory factory) {
        super(owner, factory);
    }

    public ViewModelProvider(@NonNull ViewModelStore store, @NonNull Factory factory) {
        super(store, factory);
    }
}
