package com.forrestguice.support.arch.lifecycle;

@SuppressWarnings("WeakerAccess")
public class MutableLiveData<T> extends LiveData<T>
{
    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }
}
